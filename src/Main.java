import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ttProject.websocket.server.AcceptHandler;
import com.ttProject.websocket.server.Handler;
import com.ttProject.websocket.server.WebSocketTransport;

public class Main {
	private static final int DEFAULT_WEBSOCKET_PORT = 8080;
	private static final int DEFAULT_TIMEOUT = 10000;
	private static final int DEFAULT_EXECUTOR_COUNT = 10;
	private static int port;
	private static ExecutorService executors;
	private Selector selector;
	public static void main(String[] args) {
		try {
			executors = Executors.newFixedThreadPool(DEFAULT_EXECUTOR_COUNT);
			if(args.length >= 1) {
				port = Integer.parseInt(args[0]);
			}
			else {
				port = DEFAULT_WEBSOCKET_PORT;
			}
			WebSocketTransport wst = new WebSocketTransport();
			wst.run();
//			Main m = new Main();
//			m.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public Main() {
		ServerSocketChannel sc = null;
		try {
			sc = ServerSocketChannel.open();
			sc.socket().setReuseAddress(true); // 再起動時にすぐにportを利用するための処置
			sc.socket().setSoTimeout(DEFAULT_TIMEOUT); // タイムアウトのおおきさ。
			sc.socket().bind(new InetSocketAddress(port)); // 利用ポート
			
			sc.configureBlocking(false); // 非ブロッキング
			selector = Selector.open();
			sc.register(selector, SelectionKey.OP_ACCEPT);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void start() {
		try {
			SocketChannel channel;
			while(selector.select() > 0) {
				Set<SelectionKey> keys = selector.selectedKeys();
				for(Iterator<SelectionKey> it = keys.iterator(); it.hasNext();) {
					SelectionKey key = it.next();
					it.remove();

					if(key.isAcceptable()) {
						System.out.println("acceptしました。");
						ServerSocketChannel serverChannel = (ServerSocketChannel)key.channel();
						SocketChannel socketChannel = serverChannel.accept();
						socketChannel.configureBlocking(false);
						socketChannel.register(selector, SelectionKey.OP_READ);
					}
					if(key.isReadable()) {
						System.out.println("readします。");
						channel = (SocketChannel)key.channel();
						
						ByteBuffer buffer = ByteBuffer.allocate(4096);
						if(channel.read(buffer) == -1) {
							// 閉じてる
							channel.close();
							continue;
						}
						buffer.flip();
						System.out.println(new String(buffer.array(), "UTF-8"));
						buffer = ByteBuffer.allocate(4096);
						buffer.put("あいうえお".getBytes("UTF-8"));
						buffer.flip();
						channel.write(buffer);
					}
					if(key.isWritable()) {
						
					}
					if(key.isConnectable()) {
						
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				for(SelectionKey key : selector.keys()) {
					key.channel().close();
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}
