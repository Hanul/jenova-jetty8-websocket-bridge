/**
 * Jenova Jetty8 WebSocket Bridge Client Side (included Jenova Engine Alpha-2.0)
 * 
 * @author Hanul
 * @version saychat (from 1.1)
 */
jenova = {
	websocket : {
		object : null,
		MAINTAIN_CONNECTION_TIME : 5000, // 5초마다 연결을 유지함
		convertJson : function(object) {
			var results = [];
			for ( var property in object) {
				var value = object[property];
				if (value)
					results.push(property.toString() + ':"' + value + '"');
			}
			return '{' + results.join(',') + '}';
		},
		connect : function(url, namespaceName, roomName, func, enterCallback, enterMsgs, exitCallback, exitMsgs) {
			this.close();

			var enterJson = this.convertJson(enterMsgs);
			var exitJson = this.convertJson(exitMsgs);

			// 새 웹 소켓 생성
			var openMsgParam = enterCallback && enterMsgs ? '&enterCallback=' + encodeURIComponent(enterCallback) + '&enterJson=' + encodeURIComponent(enterJson) : '';
			var closeMsgParam = exitCallback && exitMsgs ? '&exitCallback=' + encodeURIComponent(exitCallback) + '&exitJson=' + encodeURIComponent(exitJson) : '';
			var webSocket = new WebSocket(url + '?namespaceName=' + encodeURIComponent(namespaceName) + '&roomName=' + encodeURIComponent(roomName) + openMsgParam + closeMsgParam);
			if (webSocket.__comet) {
				webSocket.__comet(namespaceName, roomName, enterCallback, enterJson, exitCallback, exitJson);
			}

			webSocket.onopen = function() {
				webSocket.__maintainInterval = setInterval(function() {
					webSocket.send('');
				}, jenova.websocket.MAINTAIN_CONNECTION_TIME); // 연결을 유지함
			};

			webSocket.onmessage = function(event) {
				var data = eval('(' + event.data + ')'); // json을 받아와서
				eval(data.jsFunctionName).apply({}, data.parameters); // 실행시켜 줍니다.
			};

			webSocket.onclose = function() {
				if (webSocket.__maintainInterval) {
					clearInterval(webSocket.__maintainInterval);
				}
				if (jenova.websocket.isConnectionClose) { // 연결이 끊어지면
					if (confirm('접속이 원할하지 않습니다.\n재접속 하시겠습니까?')) {
						if (func) { // 재접속 함수가 있을 경우
							func(); // 재접속 함수 실행
						}
						jenova.websocket.connect(url, namespaceName, roomName, func); // 재접속
					} else {
						alert('접속을 해재하였습니다.\n재접속 하시려면 SayChat을 완전히 종료 후 다시 실행하셔야 합니다.');
					}
				}
				jenova.websocket.isConnectionClose = true;
			};

			this.object = webSocket;
		},
		isConnectionClose : true,
		close : function() {
			if (this.object) {
				this.isConnectionClose = false;
				this.object.close();
			}
		},
		updateExitJson : function(exitMsgs) { // 종료 JSON 업데이트
			if (this.object) {
				var exitJson = this.convertJson(exitMsgs);
				this.object.send(exitJson);
			}
		}
	}
};