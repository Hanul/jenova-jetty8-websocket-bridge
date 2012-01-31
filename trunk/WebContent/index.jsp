<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="co.hanul.jenova.WebSocketBridge" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Jenova WebSocket Bridge Showcase</title>
		<script src="${pageContext.request.contextPath}/js/jenova-websocket-bridge.js" type="text/javascript"></script>
		<script type="text/javascript">
		jenova.websocket.connect('ws://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}/WebSocketBridge', '네임스페이스', '방1');
		
		function 함수(a, b, c) {
			alert('파라미터 : ' + a + ' ' + b + ' ' + c);
		}
		</script>
	</head>
	<body>
		
	</body>
</html>