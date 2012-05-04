package com.ttProject.websocket.application;

import java.util.Set;

import com.ttProject.websocket.server.WebSocketConnection;

/**
 * アプリケーション動作定義
 * @author taktod
 */
public class Application extends ApplicationInstance {
	@Override
	public void onConnect(WebSocketConnection connect) {
		// handshakeを実行して、成立した直後に呼び出される。
		System.out.println("connect...:" + connect.getRemoteAddress());
	}
	@Override
	public void onDisconnect(WebSocketConnection connect) {
		// 切断を検知したときに呼び出される
		System.out.println("disconnect...");
	}
	@Override
	public void onAppStart() {
		// アクセスルームの位置がはじめての場合に呼び出される。
		System.out.println("appStart...");
	}
	@Override
	public void onAppStop() {
		// アクセスルームから接続がいなくなったときに呼び出される。
		System.out.println("appStop...");
	}
	@Override
	public void onReceiveData(WebSocketConnection connect, String data) {
		// ルーム上のコネクションのどれかからデータをうけとったら呼び出される。
		System.out.println("message receive...:" + data);
		// 同じスコープにつながっている全員にメッセージをおくっておく。
		Set<WebSocketConnection> connections = getConnections();
		synchronized(connections) {
			for(WebSocketConnection conn : connections) {
				try {
					conn.send(data);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
