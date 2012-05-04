package com.ttProject.websocket.server;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * サーバー動作を実行するインスタンス
 * @author taktod
 *
 */
public class WebSocketTransport {
	private static final int DEFAULT_THREADS = Runtime.getRuntime().availableProcessors() * 2;
	private static final int DEFAULT_BUFFER_SIZE = 2048;
	private static final int DEFAULT_WEBSOCKET_PORT = 8080;
	private static final int DEFAULT_TIMEOUT = 1000;
//	private static final Class<?> DEFAULT_APPLICATION = Application.class;

	private int bufferSize = DEFAULT_BUFFER_SIZE;
	private int threads = DEFAULT_THREADS;
	private int port = DEFAULT_WEBSOCKET_PORT;
	private int timeout = DEFAULT_TIMEOUT;
//	private Class<?> applicationClass = DEFAULT_APPLICATION;

	private static ExecutorService executors;
	private Selector selector;

	/**
	 * コンストラクタ
	 */
	public WebSocketTransport() {
	}
	/**
	 * コンストラクタ
	 * @param bufferSize
	 * @param threads
	 * @param port
	 */
	public WebSocketTransport(int bufferSize, int threads, int port, int timeout, Class<?>applicationClass) {
		setBufferSize(bufferSize);
		setThreads(threads);
		setPort(port);
		setTimeout(timeout);
	}
	/**
	 * @param bufferSize the bufferSize to set
	 */
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}
	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}
	/**
	 * @param threads the threads to set
	 */
	public void setThreads(int threads) {
		this.threads = threads;
	}
	/**
	 * @param timeout the timeout to set
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	/**
	 * @param applicationClass the applicationClass to set
	 * /
	public void setApplicationClass(Class<?> applicationClass) {
		this.applicationClass = applicationClass;
	}
	/**
	 * サーバー開始
	 */
	public void run() {
		executors = Executors.newFixedThreadPool(threads);
		WebSocketManager manager = new WebSocketManager();
		try {
			// 下準備を実行します。
			ServerSocketChannel serverChannel = null;
			SocketChannel channel = null;
			serverChannel = ServerSocketChannel.open();
			serverChannel.socket().setReuseAddress(true); // 再起動時にすぐにポートを利用できるようにする。
			serverChannel.socket().setSoTimeout(timeout);
			serverChannel.socket().bind(new InetSocketAddress(port));
			
			serverChannel.configureBlocking(false);
			selector = Selector.open();
			serverChannel.register(selector, SelectionKey.OP_ACCEPT); // 接続Acceptのイベント監視開始
			
			// 開始を始めます。
			while(selector.select() > 0) {
				Set<SelectionKey> keys = selector.selectedKeys();
				for(Iterator<SelectionKey> iter = keys.iterator(); iter.hasNext();) {
					SelectionKey key = iter.next();
					iter.remove(); // みつけたキーを外しておく。
					
					if(key.isAcceptable()) {
						// あたらしい接続の受け入れ
						serverChannel = (ServerSocketChannel)key.channel();
						channel = serverChannel.accept(); // 新しくおこったチャンネルを取得
						channel.configureBlocking(false); // こちらもブロッキングを解除非同期で処理する。
						channel.register(selector, SelectionKey.OP_READ); // 読み込み処理監視する。
						// 接続したときの、channelはどこかに保持しておく。
						manager.registerConnection(channel);
						continue;
					}
					if(key.isReadable()) {
						// 読み込みイベント
						channel = (SocketChannel)key.channel();
						ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
						if(channel.read(buffer) == -1) {
							// 閉じるイベント
							// 切断したら、保持していたchannelを破棄する必要あり。
							manager.unregisterConnection(channel);
							continue;
						}
						WebSocketConnection connect = manager.getConnectData(channel);
						buffer.flip();
						executors.execute(new ReceiveThread(connect, buffer));
 					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
