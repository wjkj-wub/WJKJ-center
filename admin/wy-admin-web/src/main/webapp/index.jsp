<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta charset="UTF-8" />
<title>网娱大师管理系统</title>
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no">
<meta name="format-detection" content="telephone=no" />
<meta name="renderer" content="webkit" />
<meta http-equiv="Cache-Control" content="no-siteapp" />
<meta http-equiv="Pragma" content="no-cache" />
<meta http-equiv="Expires" content="0" />
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

<sitemesh:head />
</head>
<body>
	<div class="login-main">
		<div class="login-cont login-wrap">
			<h2 class="tc">网娱大师管理后台</h2>
			<hr>
			<br>
			<form method="post" action="${ctx}/login" class="form-horizontal login-form">
				<c:if test="${!empty msg}">
					<div class="alert alert alert-primary alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
						<p>
							<c:out value="${msg}" />
							<br>
						</p>
					</div>
				</c:if>
				<p>
					<label class="control-label" for="content">用户名：</label>
				</p>
				<p>
					<input type="text" class="form-control" name="username" id="" phone="" value="">
				</p>
				<p>
					<label class="control-label" for="password">密码：</label>
				</p>
				<p>
					<input class="form-control" type="password" name="password" id="password" value="">
				</p>
				<p>
					<label class="control-label" for="password">验证码：</label>
				</p>
				<p>
					<input class="form-control" type="text" name="captcha" id="password" value="" autocomplete="off" style="width:50%;display:inline"/><span><a id="change-captcha" href="javascript:void(0);"><img id="captcha" src="${ctx}/captcha" style="height:32px;"/></a></span>
				</p>
				<p>
					<input type="submit" name="" value="登 录" class="btn btn-primary btn-block" >
				</p>
			</form>
		</div>
		<script type="text/javascript">
			$('#change-captcha').on('click', function(ev) {
				$('#captcha').attr('src', '${ctx}/captcha?' + new Date().getTime());
			});
		</script>
	</div>
</body>
</html>