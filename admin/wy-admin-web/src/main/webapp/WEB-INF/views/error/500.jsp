<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<%@ page import="org.slf4j.Logger,org.slf4j.LoggerFactory" %>
<%	
	response.setStatus(200);
	Logger logger = LoggerFactory.getLogger("500.jsp");
	logger.error(exception.getMessage(), exception);
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta name="viewport" content="width=device-width,initial-scale=1.0, user-scalable=no, minimum-scale=1.0, maximum-scale=1.0" />
    <title>500</title>
    <style type="text/css">
    .error{width:650px; height:370px; position: absolute; top:0; left:0; right:0; bottom:0; margin: auto;}
    @media screen and (max-width:600px){
    	.error{width:100%; height:auto;}
    }
    </style>
</head>
<body>
	<img class="error" src="http://img.wangyuhudong.com/uploads/imgs/error/500.jpg" />
</body>
</html>
