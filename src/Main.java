import com.ttProject.websocket.server.WebSocketTransport;

/**
 * サーバーのエントリー動作
 * @author taktod
 */
public class Main {
	public static void main(String[] args) {
		try {
			new WebSocketTransport().run();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
