<#import "/macros/pager.ftl" as p >
<html>
<head>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>管理系统</strong>
 		</li>
		<li class="active">
			<#if (type!0) == 0>
			登陆红包设置
			<#elseif (type!0) == 1>
			注册绑定红包设置
			<#elseif (type!0) == 2>
			预约支付红包设置
			<#elseif (type!0) == 4>
			分享红包设置
			</#if>
		</li>
	</ul>
	
	<#-- 编辑 -->
	<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
	<div class="mb10">
		<form id="editor" class="form-horizontal form-condensed" role="form" method="post">
			<input type="hidden" name="id" value="${(redbag.id)!}"/>
			<input type="hidden" name="type" value="${type!}" />
			<#if (redbag.type)?? && redbag.type != 4>
			<div class="form-group">
				<label class="col-md-2 control-label">金额</label>
				<div class="col-md-4">
					<input class="form-control" type="number" name="money" value="${(redbag.money)!}" />
				</div>
			</div>
			<#else>
			<input type="hidden" name="money" value="0" />
			</#if>
			<div class="form-group">
				<label class="col-md-2 control-label">有效期</label>
				<div class="col-md-4">
					<input class="form-control" type="number" name="day" value="${(redbag.day)!}" />
				</div>
			</div>
			<div class="form-group">
				<label class="col-md-2 control-label">说明</label>
				<div class="col-md-4">
					<input class="form-control" type="text" name="explain" value="${(redbag.explain)!}" maxlength="22" />
				</div>
			</div>
			<div class="form-group">
				<label class="col-md-2 control-label">是否限制</label>
				<div class="col-md-4">
					<label><input type="radio" name="restrict" value="0"<#if ((redbag.restrict)!0) == 0> checked</#if> /> 否</label>
					<label><input type="radio" name="restrict" value="1"<#if ((redbag.restrict)!0) == 1> checked</#if> /> 是</label>
				</div>
			</div>
			
			<#if showShareRedbagSetting>
			<div class="form-group">
				<label class="col-md-2 control-label">分享图标</label>
				<div class="col-md-4">
					<input class="form-control" type="text" name="shareRedbagIcon" value="${shareRedbagIcon!}" maxlength="100" />
				</div>
			</div>
			<div class="form-group">
				<label class="col-md-2 control-label">分享标题</label>
				<div class="col-md-4">
					<input class="form-control" type="text" name="shareRedbagTitle" value="${shareRedbagTitle!}" maxlength="22" />
				</div>
			</div>
			<div class="form-group">
				<label class="col-md-2 control-label">分享内容</label>
				<div class="col-md-4">
					<input class="form-control" type="text" name="shareRedbagContent" value="${shareRedbagContent!}" maxlength="30" />
				</div>
			</div>
			</#if>
			
			<div class="form-group">
				<label class="col-md-2 control-label"></label>
				<div class="col-md-4">
					<input class="btn btn-sm btn-primary" value="保存" onclick="submitEditor()" />
				</div>
			</div>
		</form>
	</div>
	<#-- 模态框业务 -->
	<script type="text/javascript">
		// 提交表单
		function submitEditor() {
			var $form = $('#editor');
			var valid = $form.formValid();
			if(valid) {
				$form.ajaxSubmit({
					url:'${ctx}/redbag/setting/save',
				    type:'post',
				    success: function(d){
				    	if(d.code == 0) {
				    		// window.location.reload();
				    		alert('保存成功');
				    	} else {
				    		alert(d.result);
				    	}
					}
				});
			}
		}
	</script>
</body>
</html>