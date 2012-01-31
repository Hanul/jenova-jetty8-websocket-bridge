package co.hanul.jenova;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.directwebremoting.ScriptSession;
import org.directwebremoting.proxy.ScriptProxy;
import org.eclipse.jetty.websocket.WebSocket.Connection;

import co.hanul.jenova.WebSocketBridge.Call;

public class Connect {

	private Connection webSocketConnection;
	private ScriptProxy cometConnection;
	private ScriptSession cometSession;

	private String namespaceName;
	private String roomName;
	private String exitCallback;
	private String exitJson;

	private final static long MAINTAIN_CONNECTION_TIME_LIMIT = 17500; // 연결 유지 제한시간은 17.5초입니다.
	private long connectionTime;

	public Connect(String namespaceName, String roomName, String enterCallback, String enterJson, String exitCallback, String exitJson, Connection webSocketConnection) {
		this.namespaceName = namespaceName;
		this.roomName = roomName;
		this.webSocketConnection = webSocketConnection;

		open(enterCallback, enterJson);
		this.exitCallback = exitCallback;
		this.exitJson = exitJson;
	}

	public Connect(String namespaceName, String roomName, String enterCallback, String enterJson, String exitCallback, String exitJson, ScriptProxy cometConnection, ScriptSession cometSession) {
		this.namespaceName = namespaceName;
		this.roomName = roomName;
		this.cometConnection = cometConnection;
		this.cometSession = cometSession;

		open(enterCallback, enterJson);
		this.exitCallback = exitCallback;
		this.exitJson = exitJson;
	}

	/**
	 * 연결을 수립
	 */
	private void open(String enterCallback, String enterJson) {
		connectionTime = System.currentTimeMillis();

		Map<String, Map<String, Set<Connect>>> all = WebSocketBridge.getNamespaces();

		if (all.get(namespaceName) == null) { // 네임스페이스가 없을 때
			all.put(namespaceName, new HashMap<String, Set<Connect>>()); // 네임스페이스를 생성
		}
		Map<String, Set<Connect>> namespace = all.get(namespaceName);
		if (namespace.get(roomName) == null) { // 방이 없을 때
			namespace.put(roomName, new CopyOnWriteArraySet<Connect>()); // 방을 생성
		}
		Set<Connect> room = namespace.get(roomName);

		room.add(this); // 입장

		if (enterCallback != null && enterJson != null) { // 입장 메시지가 있을 경우
			try {
				new Call("{jsFunctionName:'" + enterCallback + "',parameters:[" + enterJson + "]}").toRoom(namespaceName, roomName);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void sendMessage(String json) throws IOException {
		if (webSocketConnection != null && webSocketConnection.isOpen()) { // 웹소켓에 접속된 경우
			webSocketConnection.sendMessage(json);
		}
		if (cometConnection != null && !cometSession.isInvalidated()) { // Comet에 접속된 경우
			Map<String, String> param = new HashMap<String, String>();
			param.put("data", json);
			cometConnection.addFunctionCall(WebSocketBridge.JS_OBJECT_PREFIX + "object.onmessage", param);
		}
		isLimited(); // 연결 유지 제한시간이 넘은 경우 연결을 닫음
	}

	public boolean isLimited() {
		boolean isLimited = System.currentTimeMillis() - connectionTime > MAINTAIN_CONNECTION_TIME_LIMIT;
		if (isLimited) { // 연결 유지 제한시간이 넘은 경우
			close(); // 연결을 닫음
		}
		return isLimited;
	}

	public void updateTimeLimit() {
		connectionTime = System.currentTimeMillis();
	}

	public void updateExitJson(String exitJson) {
		this.exitJson = exitJson;
	}

	/**
	 * 연결을 종료
	 * 
	 * @return 퇴장 여부
	 */
	public void close() {
		if (cometSession != null) {
			cometSession.removeAttribute(WebSocketCometBridge.CONNECT_KEY);
			cometSession.invalidate();
		}

		Map<String, Map<String, Set<Connect>>> all = WebSocketBridge.getNamespaces();

		Map<String, Set<Connect>> namespace = all.get(namespaceName);
		Set<Connect> room = namespace.get(roomName);

		if (room.contains(this)) {
			room.remove(this); // 퇴장

			if (namespace.get(roomName).isEmpty()) { // 방에 사람이 없을 때
				namespace.remove(roomName); // 방 제거
			}
			if (all.get(namespaceName).isEmpty()) { // 네임스페이스에 방이 없을 때
				all.remove(namespaceName); // 네임스페이스 제거
			}

			if (exitCallback != null && exitJson != null) { // 퇴장 메시지가 있을 경우
				try {
					new Call("{jsFunctionName:'" + exitCallback + "',parameters:[" + exitJson + "]}").toRoom(namespaceName, roomName);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
