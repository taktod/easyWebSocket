package com.ttProject.websocket.server;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import com.ttProject.websocket.application.Application;
import com.ttProject.websocket.application.ApplicationInstance;

/**
 * 接続を管理するマネージャー
 * @author taktod
 */
public class WebSocketManager {
	/**
	 * このマップでSocketChannelとConnectDataの結びつきを実行しておく。
	 */
	private static final Map<SocketChannel, WebSocketConnection> connectMap = new HashMap<SocketChannel, WebSocketConnection>();

	private static final Map<String, ApplicationInstance> applicationMap = new HashMap<String, ApplicationInstance>();

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
		WebSocketConnection connect = null;
		// 所属スコープから接続を取り除く。
		synchronized(connectMap) {
			connect = connectMap.remove(channel);
		}
		if(connect != null) {
			ApplicationInstance appInstance = applicationMap.get(connect.getPath());
			if(appInstance != null) {
				appInstance.onDisconnect(connect);
				if(appInstance.removeConnection(connect) == 0) {
					// 接続インスタンスが１つもいなくなった場合
					synchronized(applicationMap) {
						applicationMap.remove(connect.getPath());
					}
					appInstance.onAppStop();
				}
			}
			connect.close();
		}
	}

	public WebSocketConnection getConnectData(SocketChannel channel) {
		synchronized(connectMap) {
			return connectMap.get(channel);
		}
	}
	/**
	 * コネクションからアプリケーションを引き出して、登録しておく。
	 * @param connect
	 */
	public void registerApplication(WebSocketConnection connect) {
		// ApplicationInstanceにconnectionを追加する。
		ApplicationInstance appInstance;
		synchronized(applicationMap) {
			appInstance = applicationMap.get(connect.getPath());
		}
		if(appInstance == null) {
			appInstance = new Application();
			appInstance.onAppStart();
			synchronized(applicationMap) {
				applicationMap.put(connect.getPath(), appInstance);
			}
		}
		appInstance.addConnection(connect);
		appInstance.onConnect(connect);
	}
	public ApplicationInstance getApplication(WebSocketConnection connect) {
		ApplicationInstance appInstance;
		synchronized(applicationMap) {
			appInstance = applicationMap.get(connect.getPath());
		}
		if(appInstance == null) {
			// ここにくることは、あまりないとおもいたし。
			throw new RuntimeException("Access getApplication before initialize.");
		}
		return appInstance;
	}
}
