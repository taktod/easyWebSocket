package com.ttProject.websocket.server;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * 接続を管理するマネージャー
 * @author taktod
 */
public class WebSocketManager {
	/**
	 * このマップでSocketChannelとConnectDataの結びつきを実行しておく。
	 */
	private static final Map<SocketChannel, WebSocketConnection> connectMap = new HashMap<SocketChannel, WebSocketConnection>();

	private static final Map<String, WebSocketScope> scopeMap = new HashMap<String, WebSocketScope>();

	/**
	 * あたらしい接続を保持します。
	 */
	public void registerConnection(SocketChannel channel) {
		// コネクションを作成し、保持しておく。
		WebSocketConnection connectData = new WebSocketConnection(channel);
		synchronized(connectMap) {
			connectMap.put(channel, connectData);
		}
	}

	/**
	 * 閉じた接続を解放しておきます。
	 * @param channel
	 */
	public void unregisterConnection(SocketChannel channel) {
		WebSocketConnection connectData = null;
		// 所属スコープから接続を取り除く。
		synchronized(connectMap) {
			connectData = connectMap.remove(channel);
		}
		if(connectData != null) {
			connectData.close();
		}
	}

	public WebSocketConnection getConnectData(SocketChannel channel) {
		synchronized(connectMap) {
			return connectMap.get(channel);
		}
	}
}
