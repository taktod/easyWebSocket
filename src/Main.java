import com.ttProject.websocket.server.WebSocketTransport;

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
