package com.ttProject.websocket.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * 接続データを保持するモデル
 * @author taktod
 */
public class WebSocketConnection {
	private SocketChannel channel;
	
	private String host;
	private String path;
	private String origin;
	private String version = null;
	private String query;
	
	private boolean connected = false;
	private boolean continuousData = false;
	
	private List<ByteBuffer> bufferList = new ArrayList<ByteBuffer>();
	private ByteBuffer result = null;
	private int maskPos;
	private long size;
	private byte first;
	
	public WebSocketConnection(SocketChannel channel) {
		connected = false;
		this.channel = channel;
	}
	public boolean isConnected() {
		return connected;
	}
	public void setConnected() {
		connected = true;
	}
	public String getHostPath() {
		return "ws://" + host + path;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getOrigin() {
		return origin;
	}
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	public SocketChannel getChannel() {
		return channel;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		if(path.length() != 0 && path.charAt(path.length() - 1) == '/') {
			this.path = path.substring(0, path.length() - 1);
		}
		else {
			this.path = path;
		}
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public int getId() throws Exception {
		if(channel == null) {
			throw new Exception("invalid channel");
		}
		return channel.hashCode();
	}
	public WebSocketScope getScope() {
		return null;
	}
	public void send(ByteBuffer buffer) throws IOException{
		System.out.println(new String(buffer.array()));
		channel.write(buffer);
	}
	public void send(String string) throws UnsupportedEncodingException, IOException {
		byte[] data = string.getBytes("UTF8");
		int size = data.length;
		if(version == null) {
			// hybi00(safari)
			ByteBuffer buffer = ByteBuffer.allocate(size + 4);
			buffer.put((byte)0x00);
			buffer.put(data);
			buffer.put((byte)0xFF);
			buffer.flip();
			channel.write(buffer);
		}
		else {
			// rfc6455
			ByteBuffer buffer = ByteBuffer.allocate(size + 8);
			buffer.put((byte)0x81);
			// size
			if(size < 126) {
				buffer.put((byte)size);
			}
			else if(size < 0x010000) {
				buffer.put((byte)0x7E);
				buffer.putShort((short)size);
			}
			else {
				buffer.put((byte)0x7F);
				buffer.putLong(size);
			}
			for(int i=0;i < size;i ++) {
				buffer.put((byte)data[i]);
			}
			buffer.flip();
			channel.write(buffer);
		}
	}
	public void receive(ByteBuffer buffer) {
		if(isConnected()) {
			// データ受信を実施する。
			if(checkClosing(buffer)) {
				return;
			}
//			analizeData(buffer);
		}
		else {
			// handshakeを実施する。
			WebSocketHandshake handshake = new WebSocketHandshake(this);
			try {
				handshake.handShake(buffer);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	private boolean checkClosing(ByteBuffer buffer) {
		if(continuousData) { // 続きデータ取得中なら停止バッファではない。
			return false;
		}
		byte data = buffer.get(0);
		if(version == null) {
			// hybi00
			if(data == (byte)0xFF) {
				close();
				return true;
			}
		}
		else {
			// rfc6455
			if((data & 0x08) != 0x00) {
				close();
				return true;
			}
		}
		return false;
	}
	public void close() {
		if(!connected) {
			return;
		}
		WebSocketConnectionManager manager = new WebSocketConnectionManager();
		manager.unregisterConnection(channel);
		try {
			channel.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
