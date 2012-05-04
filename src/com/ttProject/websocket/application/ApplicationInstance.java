package com.ttProject.websocket.application;

import java.util.HashSet;
import java.util.Set;

import com.ttProject.websocket.server.WebSocketConnection;

public abstract class ApplicationInstance {
	private static final Set<WebSocketConnection> connections = new HashSet<WebSocketConnection>();
	public void addConnection(WebSocketConnection connect) {
		synchronized(connections) {
			connections.add(connect);
		}
	}
	public int removeConnection(WebSocketConnection connect) {
		synchronized(connections) {
			connections.remove(connect);
			return connections.size();
		}
	}
	public Set<WebSocketConnection> getConnections() {
		return connections;
	}
	public abstract void onConnect(WebSocketConnection connect);
	public abstract void onDisconnect(WebSocketConnection connect);
	public abstract void onAppStart();
	public abstract void onAppStop();
	public abstract void onReceiveData(WebSocketConnection connect, String data);
}
