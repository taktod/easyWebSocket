package com.ttProject.websocket.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WebSocketScope {
	private static final Map<String, WebSocketScope> scopes = new HashMap<String, WebSocketScope>();
	private final Set<WebSocketConnection> connections = new HashSet<WebSocketConnection>();
}
