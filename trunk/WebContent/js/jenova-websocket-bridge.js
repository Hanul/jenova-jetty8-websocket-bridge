jenova = {
	websocket : {
		conncet : function(url, namespaceName, roomName) {
			var webSocket = new WebSocket(url + '?namespaceName=' + encodeURIComponent(namespaceName) + '&roomName=' + encodeURIComponent(roomName));
			webSocket.onmessage = function(event) {
				var data = eval('(' + event.data + ')');
				eval(data.jsFunctionName).apply({}, data.parameters);
			};
		}
	}
};