package com.ttProject.websocket.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import com.ttProject.websocket.application.ApplicationInstance;

/**
 * 接続データを保持するモデル
 * @author taktod
 */
public class WebSocketConnection {
	/** 接続実体チャンネル */
	private SocketChannel channel;
	
	/** 各種接続情報 */
	private String host; // host
	private String path; // パス
	private String origin; // origin URL
	private String version = null; // 接続バージョン指定
	private String query; // 接続時Query情報

	/** 状態保持 */
	private boolean connected = false; // 接続成立しているかフラグ
	private boolean continuousData = false; // 中途データの受信中フラグ

	/** 内部データ */
	private List<ByteBuffer> bufferList = new ArrayList<ByteBuffer>(); // 処理待ちバッファリスト
	private ByteBuffer result = null; // 結果データ
	private byte[] mask = new byte[4]; // マスクデータ(rfc6455用)
	private int maskPos; // 現在利用マスク位置
	private long size; // サイズ
	private byte first; // 初期バイト
	
	/**
	 * コンストラクタ
	 * @param channel
	 */
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
	public String getRemoteAddress() {
		return channel.socket().getInetAddress().getHostAddress();
	}
	public int getId() throws Exception {
		if(channel == null) {
			throw new Exception("invalid channel");
		}
		return channel.hashCode();
	}
	/**
	 * データ転送(生byte)
	 * @param buffer
	 * @throws IOException
	 */
	public void send(ByteBuffer buffer) throws IOException{
		channel.write(buffer);
	}
	/**
	 * データ転送(文字列)
	 * @param string
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public void send(String string) throws UnsupportedEncodingException, IOException {
		byte[] data = string.getBytes("UTF8");
		int size = data.length;
		System.out.println(size);
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
			ByteBuffer buffer = ByteBuffer.allocate(size * 2 + 8);
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
			int i = 0;
			try {
				for(i=0;i < size;i ++) {
					buffer.put((byte)data[i]);
				}
			}
			catch (Exception e) {
				System.out.println(size);
				System.out.println(i);
				e.printStackTrace();
				System.exit(-1);
			}
			buffer.flip();
			channel.write(buffer);
		}
	}
	/**
	 * データ受信
	 * @param buffer
	 */
	public void receive(ByteBuffer buffer) {
		if(isConnected()) {
			// データ受信を実施する。
			if(checkClosing(buffer)) {
				return;
			}
			analizeData(buffer);
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
	/**
	 * 受信データ解析と次の処理への受け渡し
	 * @param buffer
	 */
	private void analizeData(ByteBuffer buffer) {
		if(version == null) {
			// hybi00
			int limit = buffer.limit();
			while(buffer.position() < limit) {
				byte data = buffer.get();
				if(!continuousData) {
					if(data == 0) {
						result = ByteBuffer.allocate(buffer.capacity());
						result.clear();
						continuousData = true;
					}
					else {
						// invalid data...
						break;
					}
				}
				else {
					if(data == -1) {
						continuousData = false;
						result.flip();
						// この時点でデータ取得が完了している。
						try {
							getAppInstance().onReceiveData(this, new String(result.array(), "UTF-8").trim());
						}
						catch (Exception e) {
						}
					}
					else {
						if(result.position() > result.capacity() - 10) {
							System.out.println("バッファがいっぱいなので、フリップさせます。");
							ByteBuffer newResult = ByteBuffer.allocate(result.capacity() * 8);
							result.flip();
							newResult.put(result.array()); //最終項に勝手に0がはいるらしい。
							newResult.position(newResult.position() - 1);
							result = newResult;
						}
						result.put(data);
					}
				}
			}
		}
		else {
			// rfc6455
			while(buffer.position() < buffer.limit()) {
				if(!continuousData) {
					first = buffer.get();
					if((first & 0x0F) != 0x01) { // 文字列のみうけいれる。
						return;
					}
					continuousData = true;
					byte second = buffer.get();
					// データサイズ保持
					if((second & 0x7F) == 0x7E) {
						size = buffer.getShort();
					}
					else if((second & 0x7F) == 0x7F) {
						size = buffer.getLong();
					}
					else {
						size = second & 0x7F;
					}
					result = ByteBuffer.allocate((int)size);
					if((second & 0x80) != 0x00) {
						mask[0] = buffer.get();
						mask[1] = buffer.get();
						mask[2] = buffer.get();
						mask[3] = buffer.get();
					}
					else {
						mask[0] = 0;
						mask[1] = 0;
						mask[2] = 0;
						mask[3] = 0;
					}
					maskPos = 0;
				}
				try {
					while(maskPos < size) {
						result.put((byte)(mask[(maskPos % 4)] ^ buffer.get()));
						maskPos ++;
					}
				}
				catch (Exception e) {
					return;
				}
				continuousData = false;
				bufferList.add(result);
				if((first & 0x80) != 0x00) {
					size = 0;
					for(ByteBuffer buf : bufferList) {
						size += buf.capacity();
					}
					ByteBuffer result = ByteBuffer.allocate((int)size);
					for(ByteBuffer buf : bufferList) {
						buf.flip();
						result.put(buf);
					}
					try {
						getAppInstance().onReceiveData(this, new String(result.array(), "UTF-8").trim());
					}
					catch (Exception e) {
					}
					bufferList.clear();
				}
			}
		}
	}
	/**
	 * 切断確認処理
	 * @param buffer
	 * @return
	 */
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
	/**
	 * 停止処理
	 */
	public void close() {
		if(!connected) {
			return;
		}
		WebSocketManager manager = new WebSocketManager();
		manager.unregisterConnection(channel);
		try {
			channel.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 所属アプリケーション参照
	 * @return
	 */
	public ApplicationInstance getAppInstance() {
		WebSocketManager manager = new WebSocketManager();
		return manager.getApplication(this);
	}
}
