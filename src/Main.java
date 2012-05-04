import com.ttProject.websocket.server.WebSocketTransport;

public class Main {
	public static void main(String[] args) {
		try {
			WebSocketTransport wst = new WebSocketTransport();
			wst.run();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
