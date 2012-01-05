<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="co.hanul.jenova.WebSocketBridge" %>
<%
WebSocketBridge.call("함수", "제노바", "웹소켓", "브릿지").toRoom("네임스페이스", "방1");
%>