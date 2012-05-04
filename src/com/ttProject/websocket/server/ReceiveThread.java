package com.ttProject.websocket.server;

import java.nio.ByteBuffer;


public class ReceiveThread extends TaskThread {
	public ReceiveThread(WebSocketConnection connect, ByteBuffer buffer) {
		super(connect, buffer);
	}
	@Override
	public void run() {
		connect.receive(buffer);
	}
}
