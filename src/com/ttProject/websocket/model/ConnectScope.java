package com.ttProject.websocket.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConnectScope {
	private static final Map<String, ConnectScope> scopes = new HashMap<String, ConnectScope>();
	private final Set<ConnectData> connections = new HashSet<ConnectData>();
}
