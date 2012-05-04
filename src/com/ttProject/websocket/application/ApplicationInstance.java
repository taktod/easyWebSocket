package com.ttProject.websocket.application;

import java.util.HashSet;
import java.util.Set;

import com.ttProject.websocket.server.WebSocketConnection;

/**
 * 各パスごとに生成されるアプリケーションの定義
 * @author taktod
 */
public abstract class ApplicationInstance {
	/** アプリケーションが持つコネクション情報 */
	private final Set<WebSocketConnection> connections = new HashSet<WebSocketConnection>();
	/**
	 * コネクション追加
	 * @param connect
	 */
	public void addConnection(WebSocketConnection connect) {
		synchronized(connections) {
			connections.add(connect);
		}
	}
	/**
	 * コネクション削除
	 * @param connect
	 * @return 残りコネクション数
	 */
	public int removeConnection(WebSocketConnection connect) {
		synchronized(connections) {
			connections.remove(connect);
			return connections.size();
		}
	}
	/**
	 * コネクションセット参照
	 * 利用時はsynchronizedをつけて、デッドロックを回避してください。
	 * @return connections
	 */
	public Set<WebSocketConnection> getConnections() {
		return connections;
	}
	/**
	 * 接続時イベント
	 * @param connect
	 */
	public abstract void onConnect(WebSocketConnection connect);
	/**
	 * 切断時イベント
	 * @param connect
	 */
	public abstract void onDisconnect(WebSocketConnection connect);
	/**
	 * アプリケーション作成時イベント
	 */
	public abstract void onAppStart();
	/**
	 * アプリケーション停止時イベント
	 */
	public abstract void onAppStop();
	/**
	 * 文字列データ取得時イベント
	 * @param connect
	 * @param data
	 */
	public abstract void onReceiveData(WebSocketConnection connect, String data);
}
