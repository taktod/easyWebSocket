package com.ttProject.websocket.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class WebSocketClient {
	public static void main(String[] args) {
		try {
			// サーバーとの接続を実行する。
			WebSocketClient wsc = new WebSocketClient();
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String line;
			System.out.print("input : ");
			while((line = reader.readLine()) != null) {
				System.out.println("output : " + line);
				System.out.print("input : ");
			}
			reader.close();
			System.out.println("end");
		}
		catch (Exception e) {
			// TODO: handle exception
		}
	}
	public WebSocketClient() {
		try {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						System.out.println("try to connect...");
						Selector selector = Selector.open();
						InetSocketAddress address = new InetSocketAddress("49.212.39.17", 8080);
						SocketChannel channel = SocketChannel.open();	
						channel.connect(address);
						channel.register(selector, SelectionKey.OP_READ);
						while(selector.select() > 0) {
							Set<SelectionKey> keys = selector.keys();
							for(Iterator<SelectionKey> iter = keys.iterator();iter.hasNext();) {
								SelectionKey key = iter.next();
								iter.remove();
								
								if(key.isReadable()) {
									channel = (SocketChannel)key.channel();
									ByteBuffer buffer = ByteBuffer.allocate(4096);
									if(channel.read(buffer) == -1) {
										System.out.println("disconnect detected...");
										continue;
									}
									buffer.flip();
									System.out.println(new String(buffer.array()));
								}
							}
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					System.out.println("disconnect...");
				}
			});
			t.start();
		}
		catch (Exception e) {
		}
	}
}
