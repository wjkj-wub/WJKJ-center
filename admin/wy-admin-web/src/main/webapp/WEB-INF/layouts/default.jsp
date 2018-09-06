<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sitemesh"
	uri="http://www.opensymphony.com/sitemesh/decorator"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8" />
<title>网娱大师后台管理系统</title>
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no">
<meta name="format-detection" content="telephone=no" />
<meta name="renderer" content="webkit" />
<meta http-equiv="Cache-Control" content="no-siteapp" />
<meta http-equiv="Pragma" content="no-cache" />
<meta http-equiv="Expires" content="0" />

<c:if test="${sessionScope.sitemesh eq 'Y'}">
	<link rel="alternate icon" type="image/png" href="${ctx}/static/images/favicon.png">
	<link rel="stylesheet" href="${ctx}/static/css/zui.css" />
	<link rel="stylesheet" href="${ctx}/static/css/zui-theme.css" />
	<link rel="stylesheet" href="${ctx}/static/css/main.css" />
	<link rel="stylesheet" href="${ctx}/static/js/lib/datetimepicker/datetimepicker.min.css" />
	<link rel="stylesheet" href="${ctx}/static/js/lib/ibox/ibox.css" />
	
	<!--[if gte IE 9]>
		<script type="text/javascript" src="${ctx}/static/js/jquery.min.js"></script>
		<script type="text/javascript" src="${ctx}/static/js/zui.min.js"></script>
		<script type="text/javascript" src="${ctx}/static/js/lib/ibox/ibox.js"></script>
	<![endif]-->

	<!--[if lt IE 9]>
		<script type="text/javascript" src="${ctx}/static/js/lib/ieonly/jquery-1.8.3.js"></script>
		<script type="text/javascript" src="${ctx}/static/js/lib/ieonly/html5shiv.js"></script>
		<script type="text/javascript" src="${ctx}/static/js/lib/ieonly/excanvas.js"></script>
		<script type="text/javascript" src="${ctx}/static/js/lib/ieonly/respond.js"></script>
		<script type="text/javascript" src="${ctx}/static/js/zui.min.js"></script>
		<script type="text/javascript" src="${ctx}/static/js/lib/ibox/ibox.js"></script>
	<![endif]-->

	<!--[if !IE]><!-->
	<script type="text/javascript" src="${ctx}/static/js/jquery.min.js"></script>
	<script type="text/javascript" src="${ctx}/static/js/zui.min.js"></script>
	<script type="text/javascript" src="${ctx}/static/js/lib/ibox/ibox.js"></script>
	<!--<![endif]-->
	
	<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
	<script type="text/javascript" src="${ctx}/static/js/app.js"></script>
</c:if>

<sitemesh:head />
</head>
<body>
	<!--[if lte IE 8]>
		<p class="browsehappy">
			你正在使用<strong>过时</strong>的浏览器，网娱大师管理系统暂不支持。 请 <a href="http://browsehappy.com/" target="_blank">升级浏览器</a>以获得更好的体验！
		</p>
		<style>
			@media only screen and (min-width: 641px) {
					.admin-offcanvas-bar {
					-webkit-transform: translate(0, 0);
					-ms-transform: translate(0, 0);
					transform: translate(0, 0);
				}
			}
		</style>
	<![endif]-->
	
	<c:if test="${sessionScope.sitemesh eq 'Y'}">
		<%@ include file="/WEB-INF/layouts/header.jsp"%>
		<div class="main">
			<%@ include file="/WEB-INF/layouts/left.jsp"%>
			<div class="col-sm-9 col-md-10 content">
				<sitemesh:body />
			</div>
		</div>
		<%@ include file="/WEB-INF/layouts/footer.jsp"%>
	</c:if>

	<c:if test="${sessionScope.sitemesh != 'Y'}">
		<sitemesh:body />
	</c:if>
</body>
</html>