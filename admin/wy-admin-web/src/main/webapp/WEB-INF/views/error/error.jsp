<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sitemesh"
	uri="http://www.opensymphony.com/sitemesh/decorator"%>
	<%response.setStatus(200);%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<head>
	<title>操作错误</title>
</head>
<header class="header">
	<div class="header-cont">
		<nav class="navbar">
		    <div class="navbar-header">
		        <div  class="navbar-brand">
		            <a href="${ctx}/login"><img src="${ctx}/static/images/logo2.png" /></a>
		        </div> 
		    </div>
		    <div class="collapse navbar-collapse navbar-cont">
		    	<div class="navbar-title">后台管理</div>
		    </div> 
		</nav>
	</div>
</header>
<div class="main">
	<div class="reg_suc_tips">
		<h2>错误信息:${msg}</h2>
	<p><a href="<c:url value="${backUrl}"/>">返回</a></p>
	</div>
</div>
