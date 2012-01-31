/**
 * Jenova Jetty8 WebSocket Bridge Client Side (included Jenova Engine Alpha-2.0)
 * 
 * @author Hanul
 * @version 1.1
 */
jenova = {
	websocket : {
		connect : function(url, namespaceName, roomName) {
			// 새 웹 소켓 생성
			var webSocket = new WebSocket(url + '?namespaceName=' + encodeURIComponent(namespaceName) + '&roomName=' + encodeURIComponent(roomName));
			webSocket.onmessage = function(event) {
				var data = eval('(' + event.data + ')'); // json을 받아와서
				eval(data.jsFunctionName).apply({}, data.parameters); // 실행시켜 줍니다.
			};
			webSocket.onclose = function() {
				// 연결이 끊어지면
				setTimeout(function() {
					jenova.websocket.conncet(url, namespaceName, roomName); // 재접속
				}, 1000); // 1초마다 시도
			};
		}
	}
};