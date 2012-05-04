package com.ttProject.websocket.server;

import java.nio.ByteBuffer;

/**
 * メッセージ取得動作スレッド
 * @author taktod
 */
public class ReceiveThread extends TaskThread {
	/**
	 * コンストラクタ
	 * @param connect
	 * @param buffer
	 */
	public ReceiveThread(WebSocketConnection connect, ByteBuffer buffer) {
		super(connect, buffer);
	}
	/**
	 * 実行
	 */
	@Override
	public void run() {
		connect.receive(buffer);
	}
}
