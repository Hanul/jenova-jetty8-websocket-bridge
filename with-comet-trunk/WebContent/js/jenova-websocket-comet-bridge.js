/**
 * Jenova Jetty8 WebSocket Comet Bridge Client Side (included Jenova Engine Alpha-2.0)
 * 
 * 웹소켓을 지원하지 않을 경우 코멧으로 대신 연결시켜줍니다.
 * 
 * @require DWR
 * @author Hanul
 * @version with-comet (from 1.1)
 */
if (!("WebSocket" in this)) { // 웹소켓이 없다?
	this.WebSocket = function(url) { // 코멧 기반 웹소켓 생성
		var webSocket = this;
		webSocket.__sleep = function(milliseconds) {
			var start = new Date().getTime();
			for ( var i = 0; i < 1e7; i++) {
				if ((new Date().getTime() - start) > milliseconds) {
					break;
				}
			}
		};

		dwr.engine.setErrorHandler(function() {
		}); // 사용자의 편의를 위해 DWR 에러를 무시합니다.

		dwr.engine._pollErrorHandler = function(msg, ex) { // 접속이 끊어지면
			if (jenova.websocket.isConnectionClose) {
				setTimeout(function() {
					dwr.engine.setActiveReverseAjax(false); // 접속을 끊고
					webSocket.onclose(); // 재접속할지 물어봅니다.
				}, 1000); // 새로고침시 안뜨게 1초후에 실행
			}
		};
		webSocket.onopen = function() {
		};
		webSocket.onmessage = function(event) {
		};
		webSocket.onclose = function() {
		};
		webSocket.__comet = null;
		webSocket.__comet = function(namespaceName, roomName, enterCallback, enterJson, exitCallback, exitJson) { // 코멧으로 접속
			setTimeout(function() {
				webSocket.__sleep(100); // ie 버그로 인해 0.1초간 Sleep

				dwr.engine.setActiveReverseAjax(true);

				WebSocketCometBridge.onOpen(namespaceName, roomName, enterCallback, enterJson, exitCallback, exitJson);

				webSocket.__maintainInterval = setInterval(function() {
					WebSocketCometBridge.maintainConnection();
				}, jenova.websocket.MAINTAIN_CONNECTION_TIME); // 연결을 유지함

			}, 100); // ie 버그로 인해 0.1초후 접속
		};
		webSocket.close = function() { // 코멧 종료
			WebSocketCometBridge.onClose();
			dwr.engine.setActiveReverseAjax(false);
			webSocket.onclose();
		};
		webSocket.send = function(exitJson) { // 종료 JSON 업데이트
			WebSocketCometBridge.updateExitJson(exitJson);
		};

		window.onbeforeunload = function() { // 윈도우가 닫힐때
			WebSocketCometBridge.onClose(); // 코멧 브릿지를 종료
		};
	};
}
