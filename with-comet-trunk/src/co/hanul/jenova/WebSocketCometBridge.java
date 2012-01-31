package co.hanul.jenova;

import org.directwebremoting.ScriptBuffer;
import org.directwebremoting.ScriptSession;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.proxy.ScriptProxy;

public class WebSocketCometBridge {
	public static final String CONNECT_KEY = "connect";

	public void onOpen(String namespaceName, String roomName, String enterCallback, String enterJson, String exitCallback, String exitJson) {
		ScriptSession session = WebContextFactory.get().getScriptSession();
		ScriptProxy connection = new ScriptProxy(session);
		connection.addScript(new ScriptBuffer()); // indicator 초기화

		Connect connect = new Connect(namespaceName, roomName, enterCallback, enterJson, exitCallback, exitJson, connection, session);
		session.setAttribute(CONNECT_KEY, connect);
	}

	/**
	 * 접속 시간 제한을 늘림
	 */
	public void maintainConnection() {
		ScriptSession session = WebContextFactory.get().getScriptSession();
		Connect connect = (Connect) session.getAttribute(CONNECT_KEY);
		if (connect != null && !connect.isLimited()) {
			connect.updateTimeLimit();
		}
	}

	/**
	 * 종료시 반환하는 JSON 업데이트
	 * 
	 * @param exitJson
	 */
	public void updateExitJson(String exitJson) {
		if (exitJson != null && !exitJson.trim().equals("")) {
			ScriptSession session = WebContextFactory.get().getScriptSession();
			Connect connect = (Connect) session.getAttribute(CONNECT_KEY);
			if (connect != null && !connect.isLimited()) {
				connect.updateExitJson(exitJson);
			}
		}
	}

	public void onClose() {
		ScriptSession session = WebContextFactory.get().getScriptSession();
		Connect connect = (Connect) session.getAttribute(CONNECT_KEY);
		if (connect != null) {
			connect.close();
		}
	}
}
