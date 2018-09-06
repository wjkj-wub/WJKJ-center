<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<nav class="navbar navbar-bg">
	<div class="navbar-header">
		<div class="navbar-brand">
			<img src="${ctx}/static/images/logo.png" />
		</div>
	</div>
	<a class="iconfont exitbtn" href="${ctx}/logout">&#xe605;</a> 
	<div class="collapse navbar-collapse navbar-cont">
		<span class="navbar-title">网娱大师管理系统</span>
		<div class="nav navbar-nav navbar-right">
			<span class="username">账号：${sessionScope.name}</span> 
			<a id="change-pwd" href="javascript:void(0)">
				<img class="userhead" src="<c:choose><c:when test="${!empty sessionScope.icon}">http://img.wangyuhudong.com/${sessionScope.icon}</c:when><c:otherwise>http://wymaster.b0.upaiyun.com/wy.jpg</c:otherwise></c:choose>" />
			</a>
		</div>
	</div>
</nav>
<!-- 修改密码 -->
<div id="head-modal-change-pwd" class="modal fade">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal">
					<span>×</span><span class="sr-only">关闭</span>
				</button>
				<h4 class="modal-title">修改资料</h4>
			</div>
			<div class="modal-body">
				<form class="form-horizontal form-condensed" role="form" method="post" enctype="multipart/form-data">
					<div class="form-group">
						<label class="col-md-2 control-label">头像</label>
						<div class="col-md-10">
							<input class="form-control" type="file" name="iconFile">
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-2 control-label">名称</label>
						<div class="col-md-10">
							<input class="form-control" type="text" name="realname" wy-required="名称">
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-2 control-label">赛事账户昵称</label>
						<div class="col-md-10">
							<input class="form-control" type="text" name="oetName" wy-required="自发赛账户名称">
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-2 control-label">原密码</label>
						<div class="col-md-10">
							<input class="form-control" type="password" name="oldPassword" wy-required="原密码">
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-2 control-label">密码</label>
						<div class="col-md-10">
							<input class="form-control" type="password" name="password" wy-required="密码">
						</div>
					</div>
				</form>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
				<button id="submit" type="button" class="btn btn-primary">保存</button>
			</div>
		</div>
	</div>
</div>
<script type="text/javascript">
	// 初始化模态框
	var _$changePwdModal = $('#head-modal-change-pwd');
	_$changePwdModal.modal({
		keyboard : false,
		show : false
	})

	// 关闭模态框
	function hideChangePwd() {
		 _$changePwdModal.modal('hide');
	}
	
	// 显示模态框
	$('#change-pwd').on('click', function() {
		$.fillForm({
			iconFile: '${sessionScope.icon}',
			realname: '${sessionScope.name}',
			oetName: '${sessionScope.oetName}',
		}, _$changePwdModal);
		_$changePwdModal.modal('show', 'center');
	});
	
	// 修改密码
	$('#head-modal-change-pwd #submit').on('click', function() {
		var $this = $(this);
		
		var $password = $('#head-modal-change-pwd input[name="password"]');
		var $form = $('#head-modal-change-pwd form');
		if($form.formValid()) {
			var password = $form.find('[name="password"]').val();
			var oldPassword = $form.find('[name="oldPassword"]').val();
			
			$this.prop('disabled', true);
			$form.ajaxSubmit({
				url: '${ctx}/changePwd',
				success: function(d) {
					if(d.code == 0) {
						alert('修改成功');
						window.location.reload();
					} else {
						alert(d.result)
					}
				},
				complete: function() {
					$this.prop('disabled', false);
					hideChangePwd();
				}
			});
		}
	});
</script>
