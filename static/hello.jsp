<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.Date"; %>

<% int i = 1; %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
<%
out.println("你的 IP address is " + request.getRemoteAddr());
%>
i= <%= i %>
今天日期为 <%= new Date().getDate() %>
</body>
</html>