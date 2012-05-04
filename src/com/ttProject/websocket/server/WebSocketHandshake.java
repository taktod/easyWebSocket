package com.ttProject.websocket.server;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebSocketHandshake {
	private WebSocketConnection conn;
	private String key1;
	private String key2;
	private String origin;
	private String host;
	private String path;
	private byte[] key3;
	private String version;
	private String key;
	public WebSocketHandshake(WebSocketConnection conn) {
		this.conn = conn;
	}
	public void handShake(ByteBuffer buffer) throws Exception {
		System.out.println("start handshake...");
		byte[] b = new byte[buffer.capacity()];
		String data;
		int i = 0;
		version = "";
		for(byte bi : buffer.array()) {
			if(bi == 0x0D || bi == 0x0A) {
				// 改行まできたら、内容を調査する。
				if(b.length != 0) {
					// 取得データサイズが０でない場合
					data = (new String(b)).trim();
					System.out.println(data);
					if(data.contains("GET ")) {
						Pattern pattern = Pattern.compile("GET \\/([\\w\\/]*)\\??(.*) HTTP\\/1\\.1");
						Matcher matcher = pattern.matcher(data);
						if(matcher.find()) {
//							WebSocketConnectionManager manager = new WebSocketConnectionManager();
							if(matcher.groupCount() != 2) {
								// パス数がおかしい。
								break;
							}
							path = matcher.group(1);
							if(matcher.group(2) != null && !"".equals(matcher.group(2))) {
								path += "?" + matcher.group(2);
							}
							conn.setPath(matcher.group(1));
							conn.setQuery(matcher.group(2));
						}
					}
					else if(data.contains("Sec-WebSocket-Key1: ")) {
						key1 = data;
					}
					else if(data.contains("Sec-WebSocket-Key2: ")) {
						key2 = data;
					}
					else if(data.contains("Host: ")) {
						String[] ary = data.split("Host: ");
						host = ary[1];
						conn.setHost(ary[1]);
					}
					else if(data.contains("Origin: ")) {
						String[] ary = data.split("Origin: ");
						origin = ary[1];
						conn.setOrigin(ary[1]);
					}
					else if(data.contains("Sec-WebSocket-Key: ")) {
						String[] ary = data.split("Sec-WebSocket-Key: ");
						key = ary[1];
					}
					else if(data.contains("Sec-WebSocket-Version: ")) {
						String[] ary = data.split("Sec-WebSocket-Version: ");
						version = ary[1];
						conn.setVersion(ary[1]);
					}
				}
				i = 0;
				b = new byte[buffer.capacity()];
			}
			else {
				b[i] = bi;
				i ++;
			}
		}
		key3 = b;
		if("".equals(version)) {
			// hybi00
			try {
				doHybi00Handshake();
			}
			catch (Exception e) {
				e.printStackTrace();
				ByteBuffer buf = ByteBuffer.allocate(4);
				buf.put(new byte[]{(byte)0xFF, (byte)0x00});
				buf.flip();
				conn.send(buf);
				conn.close();
				throw e;
			}
		}
		else {
			// rfc6455
			try {
				doRFC6455Handshake();
			}
			catch (Exception e) {
				e.printStackTrace();
				conn.close();
				throw e;
			}
		}
	}
	private void doRFC6455Handshake() throws Exception {
		if(key == null) {
			throw new Exception("key data is missing!");
		}
		String guid = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] dat = (key + guid).getBytes();
			md.update(dat);
			ByteBuffer buf = ByteBuffer.allocate(2048);
			byte[] bb = {0x0D, 0x0A};
			buf.put("HTTP/1.1 101 Web Socket Protocol Handshake".getBytes());
			buf.put(bb);
			buf.put("Upgrade: WebSocket".getBytes());
			buf.put(bb);
			buf.put("Connection: Upgrade".getBytes());
			buf.put(bb);
			buf.put("Sec-WebSocket-accept: ".getBytes());
			buf.put(base64Encode(md.digest()).getBytes());
			buf.put(bb);
			buf.put(bb);
			buf.flip();
			conn.send(buf);
			conn.setConnected();
			// managerに追記が完了したことを登録しておく。
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void doHybi00Handshake() throws Exception {
		if(path == null) {
			throw new Exception("get data is invalid!");
		}
		if(key3 == null) {
			throw new Exception("last byte is incollect!");
		}
		if(key1 == null || key2 == null) {
			throw new Exception("key data is missing!");
		}
		byte[] b = new byte[16];
		int buf1 = getKeyInteger(key1);
		int buf2 = getKeyInteger(key2);
		byte[] result;
		try {
			b[0] = (byte)((buf1 & 0xFF000000) >> 24);
			b[1] = (byte)((buf1 & 0x00FF0000) >> 16);
			b[2] = (byte)((buf1 & 0x0000FF00) >> 8);
			b[3] = (byte)((buf1 & 0x000000FF));
			b[4] = (byte)((buf2 & 0xFF000000) >> 24);
			b[5] = (byte)((buf2 & 0x00FF0000) >> 16);
			b[6] = (byte)((buf2 & 0x0000FF00) >> 8);
			b[7] = (byte)((buf2 & 0x000000FF));
			b[8]  = key3[0];
			b[9]  = key3[1];
			b[10] = key3[2];
			b[11] = key3[3];
			b[12] = key3[4];
			b[13] = key3[5];
			b[14] = key3[6];
			b[15] = key3[7];
			// make MD5 byte data.
			result = crypt(b);
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new Exception("MD5 algorithm is missing.");
		}
		catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			throw new Exception("Data is too short.");
		}
		ByteBuffer buf = ByteBuffer.allocate(2048);
		byte[] bb = {0x0D, 0x0A};
		buf.put("HTTP/1.1 101 WebSocket Protocol Handshake".getBytes());
		buf.put(bb);
		buf.put("Upgrade: WebSocket".getBytes());
		buf.put(bb);
		buf.put("Connection: Upgrade".getBytes());
		buf.put(bb);
		buf.put(("Sec-WebSocket-Origin: " + origin).getBytes());
		buf.put(bb);
		buf.put(("Sec-WebSocket-Location: " + "ws://" + host + "/" + path).getBytes());
		buf.put(bb);
		buf.put("Sec-WebSocket-Protocol: sample".getBytes());
		buf.put(bb);
		buf.put(bb);
		buf.put(result);
		buf.flip();
		conn.send(buf);
		conn.setConnected();
		// scopeに登録しておく。
	}
	private Integer getKeyInteger(String key) {
		StringBuffer numList = new StringBuffer();
		int spaceCount = 0;
		for(int i=20;i < key.length(); i ++) {
			char c = key.charAt(i);
			if(c >= 0x30 && c < 0x3A) {
				numList.append(c);
			}
			else if(c == ' ') {
				spaceCount ++;
			}
		}
		return (int)(new Long(numList.toString()) / spaceCount);
	}
	private String base64Encode(byte[] bytes) {
		StringBuffer bitPattern = new StringBuffer();
		for(int i = 0;i < bytes.length;i ++) {
			int b = bytes[i];
			if(b < 0) {
				b += 256;				
			}
			String tmp = Integer.toBinaryString(b);
			while(tmp.length() < 8) {
				tmp = "0" + tmp;
			}
			bitPattern.append(tmp);
		}
		while(bitPattern.length() % 6 != 0) {
			bitPattern.append("0");
		}
		
		final String[] table = {
				 "A", "B", "C", "D", "E", "F", "G", "H",
				 "I", "J", "K", "L", "M", "N", "O", "P",
				 "Q", "R", "S", "T", "U", "V", "W", "X",
				 "Y", "Z", "a", "b", "c", "d", "e", "f",
				 "g", "h", "i", "j", "k", "l", "m", "n",
				 "o", "p", "q", "r", "s", "t", "u", "v",
				 "w", "x", "y", "z", "0", "1", "2", "3",
				 "4", "5", "6", "7", "8", "9", "+", "/"
		};
		StringBuffer encoded = new StringBuffer();
		for(int i = 0;i < bitPattern.length(); i += 6) {
			String tmp = bitPattern.substring(i, i + 6);
			int index = Integer.parseInt(tmp, 2);
			encoded.append(table[index]);
		}
		while(encoded.length() % 4 != 0) {
			encoded.append("=");
		}
		return encoded.toString();
	}
	private byte[] crypt(byte[] bytes) throws NoSuchAlgorithmException {
		if(bytes == null || bytes.length == 0) {
			throw new IllegalArgumentException("bytes for encrypt must have body");
		}
		MessageDigest md = MessageDigest.getInstance("MD5");
		return md.digest(bytes);
	}
}
