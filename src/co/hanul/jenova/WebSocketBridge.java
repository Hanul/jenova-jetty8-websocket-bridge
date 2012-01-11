package co.hanul.jenova;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;
import org.eclipse.jetty.websocket.WebSocketServlet;

import com.google.gson.Gson;

/**
 * Jenova Jetty8 WebSocket Bridge Server Side (included Jenova Engine Alpha-2.0)
 * 
 * @author Hanul
 * @version 1.1
 */
public class WebSocketBridge extends WebSocketServlet {
	private static final long serialVersionUID = 1L;

	private static Map<String, Map<String, Set<Connection>>> all = new HashMap<String, Map<String, Set<Connection>>>();

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
		return new OnMessage(request.getParameter("namespaceName"), request.getParameter("roomName"));
	}

	private class OnMessage implements OnTextMessage {

		private Map<String, Set<Connection>> namespace;
		private String namespaceName;
		private Set<Connection> room;
		private String roomName;
		private Connection connection;

		public OnMessage(String namespaceName, String roomName) {
			this.namespaceName = namespaceName;
			this.roomName = roomName;
		}

		@Override
		public void onOpen(Connection connection) {
			this.connection = connection; // 커넥션 연결

			if (all.get(namespaceName) == null) { // 네임스페이스가 없을 때
				all.put(namespaceName, new HashMap<String, Set<Connection>>()); // 네임스페이스를 생성
			}
			namespace = all.get(namespaceName);
			if (namespace.get(roomName) == null) { // 방이 없을 때
				namespace.put(roomName, new CopyOnWriteArraySet<Connection>()); // 방을 생성
			}
			room = namespace.get(roomName);

			room.add(connection); // 입장
		}

		@Override
		public void onClose(int code, String message) {
			room.remove(connection); // 퇴장

			if (namespace.get(roomName).isEmpty()) { // 방에 사람이 없을 때
				namespace.remove(roomName); // 방 제거
			}
			if (all.get(namespaceName).isEmpty()) { // 네임스페이스에 방이 없을 때
				all.remove(namespaceName); // 네임스페이스 제거
			}
		}

		@Override
		public void onMessage(String message) {
			// 사용하지 않습니다.
		}
	}

	/**
	 * JavaScript 함수를 호출
	 * 
	 * @param jsFunctionName
	 * @param parameters
	 * @return
	 */
	public static Call call(String jsFunctionName, Object... parameters) {
		return new Call(new Gson().toJson(new Factors(jsFunctionName, parameters)));
	}

	public static class Factors {
		private String jsFunctionName;
		private Object[] parameters;

		public Factors(String jsFunctionName, Object[] parameters) {
			this.jsFunctionName = jsFunctionName;
			this.parameters = parameters;
		}

		public String getJsFunctionName() {
			return jsFunctionName;
		}

		public void setJsFunctionName(String jsFunctionName) {
			this.jsFunctionName = jsFunctionName;
		}

		public Object[] getParameters() {
			return parameters;
		}

		public void setParameters(Object[] parameters) {
			this.parameters = parameters;
		}
	}

	public static class Call {
		private String json;

		public Call(String json) {
			this.json = json;
		}

		/**
		 * 모두에게 전파
		 * 
		 * @throws IOException
		 */
		public void toAll() throws IOException {
			for (Map<String, Set<Connection>> namespace : all.values()) {
				for (Set<Connection> room : namespace.values()) {
					for (Connection connection : room) {
						connection.sendMessage(json);
					}
				}
			}
		}

		/**
		 * 해당 네임스페이스에 있는 모두에게 전파
		 * 
		 * @param namespaceName
		 * @throws IOException
		 */
		public void toNamespace(String namespaceName) throws IOException {
			Map<String, Set<Connection>> namespace = all.get(namespaceName);
			if (namespace != null) {
				for (Set<Connection> room : namespace.values()) {
					for (Connection connection : room) {
						connection.sendMessage(json);
					}
				}
			}
		}

		/**
		 * 해당 방에 있는 모두에게 전파
		 * 
		 * @param namespaceName
		 * @param roomName
		 * @throws IOException
		 */
		public void toRoom(String namespaceName, Object roomName) throws IOException {
			Map<String, Set<Connection>> namespace = all.get(namespaceName);
			if (namespace != null) {
				Set<Connection> room = namespace.get(roomName);
				if (room != null) {
					for (Connection connection : room) {
						connection.sendMessage(json);
					}
				}
			}
		}
	}

}
