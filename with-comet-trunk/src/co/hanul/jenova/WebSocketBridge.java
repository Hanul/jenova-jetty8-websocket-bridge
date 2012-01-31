package co.hanul.jenova;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;
import org.eclipse.jetty.websocket.WebSocketServlet;

import com.google.gson.Gson;

/**
 * Jenova Jetty8 WebSocket Bridge Server Side (included Jenova Engine Alpha-2.0)
 * 
 * @author Hanul
 * @version with-comet (from 1.1)
 */
public class WebSocketBridge extends WebSocketServlet {
	private static final long serialVersionUID = 1L;

	public static final String JS_OBJECT_PREFIX = "jenova.websocket.";

	private static Map<String, Map<String, Set<Connect>>> all = new HashMap<String, Map<String, Set<Connect>>>();

	public static Map<String, Map<String, Set<Connect>>> getNamespaces() {
		return all;
	}

	public static Map<String, Set<Connect>> getRooms(String namespace) {
		return all.get(namespace);
	}

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
		String namespaceName = request.getParameter("namespaceName");
		String roomName = request.getParameter("roomName");
		String enterCallback = request.getParameter("enterCallback");
		String enterJson = request.getParameter("enterJson");
		String exitCallback = request.getParameter("exitCallback");
		String exitJson = request.getParameter("exitJson");
		return new OnMessage(namespaceName, roomName, enterCallback, enterJson, exitCallback, exitJson);
	}

	private class OnMessage implements OnTextMessage {

		private String namespaceName;
		private String roomName;
		private String enterCallback;
		private String enterJson;
		private String exitCallback;
		private String exitJson;
		private Connect connect;

		public OnMessage(String namespaceName, String roomName, String enterCallback, String enterJson, String exitCallback, String exitJson) {
			this.namespaceName = namespaceName;
			this.roomName = roomName;
			this.enterCallback = enterCallback;
			this.enterJson = enterJson;
			this.exitCallback = exitCallback;
			this.exitJson = exitJson;
		}

		@Override
		public void onOpen(Connection connection) {
			connect = new Connect(namespaceName, roomName, enterCallback, enterJson, exitCallback, exitJson, connection); // 커넥션 연결
		}

		@Override
		public void onClose(int code, String message) {
			connect.close();
		}

		@Override
		public void onMessage(String message) {
			if (message != null && connect != null && !connect.isLimited()) {
				if (message.trim().equals("")) { // 빈 메시지면
					connect.updateTimeLimit(); // 접속 시간 제한을 늘림
				} else {
					connect.updateExitJson(message); // 빈 메시지가 아니면 종료시 반환하는 JSON 업데이트
				}
			}
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
			for (Map<String, Set<Connect>> namespace : all.values()) {
				for (Set<Connect> room : namespace.values()) {
					for (Connect connect : room) {
						connect.sendMessage(json);
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
			Map<String, Set<Connect>> namespace = all.get(namespaceName);
			if (namespace != null) {
				for (Set<Connect> room : namespace.values()) {
					for (Connect connect : room) {
						connect.sendMessage(json);
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
			Map<String, Set<Connect>> namespace = all.get(namespaceName);
			if (namespace != null) {
				Set<Connect> room = namespace.get(roomName);
				if (room != null) {
					for (Connect connect : room) {
						connect.sendMessage(json);
					}
				}
			}
		}
	}

}
