package com.ttProject.websocket.application;

import java.nio.ByteBuffer;

import com.ttProject.websocket.server.WebSocketConnection;
import com.ttProject.websocket.server.WebSocketScope;

public class Application {
	public void onConnect(WebSocketConnection connect) {
		
	}
	public void onDisconnect(WebSocketConnection connect) {
		
	}
	public void onRoomStart(WebSocketScope scope) {
		
	}
	public void onRoomStop(WebSocketScope scope) {
		
	}
	public void onReceiveData(WebSocketConnection connect, ByteBuffer buffer) {
		
	}
}
