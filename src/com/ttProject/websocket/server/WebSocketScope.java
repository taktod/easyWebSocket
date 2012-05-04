package com.ttProject.websocket.server;

import java.util.HashSet;
import java.util.Set;

public class WebSocketScope {
	private final Set<WebSocketConnection> connections = new HashSet<WebSocketConnection>();
}
