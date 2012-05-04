package com.ttProject.websocket.server;

import java.nio.ByteBuffer;

import com.ttProject.websocket.application.Application;

public abstract class TaskThread implements Runnable {
	protected WebSocketConnection connect;
	protected ByteBuffer buffer;
	protected Application application;
	public TaskThread(WebSocketConnection connect, ByteBuffer buffer) {
		this.connect = connect;
		this.buffer = buffer;
		// connectから自分のアプリケーションを取得しておく。とれなかったらnull
		this.application = null;
	}
}
