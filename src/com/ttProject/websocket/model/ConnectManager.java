package com.ttProject.websocket.model;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * 接続を管理するマネージャー
 * @author taktod
 */
public class ConnectManager {
	/**
	 * このマップでSocketChannelとConnectDataの結びつきを実行しておく。
	 */
	private static final Map<SocketChannel, ConnectData> connectMap = new HashMap<SocketChannel, ConnectData>();
	/**
	 * あたらしい接続を保持します。
	 */
	public void registerConnection(SocketChannel channel) {
		// コネクションを作成し、保持しておく。
	}
	/**
	 * 閉じた接続を解放しておきます。
	 * @param channel
	 */
	public void unregisterConnection(SocketChannel channel) {
		// 所属スコープから接続を取り除く。
	}
}
