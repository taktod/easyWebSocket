package com.ttProject.websocket.server;

import java.nio.ByteBuffer;

/**
 * 各スレッド動作abstractクラス
 * @author taktod
 */
public abstract class TaskThread implements Runnable {
	/** 動作実行WebSocketConnectionオブジェクト */
	protected WebSocketConnection connect;
	/** 動作用データ */
	protected ByteBuffer buffer;
	/**
	 * コンストラクタ
	 * @param connect
	 * @param buffer
	 */
	public TaskThread(WebSocketConnection connect, ByteBuffer buffer) {
		this.connect = connect;
		this.buffer = buffer;
	}
}
