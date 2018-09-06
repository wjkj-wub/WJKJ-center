<#import "/macros/pager.ftl" as p >
<html>
<head>
<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li><i class="icon icon-location-arrow mr10"></i> <strong>兑换码</strong>
		</li>
		<li class="active">统计</li>
	</ul>

	<#-- 生产条件 -->
	<div class="mb10">
		<div class="form-group">
			<div class="col-md-12">
				<a class="btn btn-info" href="${ctx}/boon/cdkey/generator"><i class="icon-angle-left"></i> 返回生成页面</a>
			</div>
		</div>
		<form id="form-generate" class="form-horizontal" role="form" method="post" action="${ctx}/boon/cdkey/statis">
			<div class="form-group">
				<label class="col-md-1 control-label">用途</label>
				<div class="col-md-4">
					<input type="text" name="production" class="form-control" placeholder="最多12个字,且不可与历史重名" maxlength="12" value="${production!}" />
				</div>
				<div class="col-md-4">
					<button type="submit" class="btn btn-info">统计</button>
					<a class="btn btn-primary" href="${ctx}/boon/cdkey/export?production=${production!}" target="_blank">下载</a>
				</div>
			</div>
		</form>
	</div>
	
	<#-- 统计结果 -->
	<table class="table table-striped table-hover">
		<tr>
			<th>类型</th>
			<th>面额</th>
			<th>数量</th>
			<th>过期时间</th>
		</tr>
		<#if statis??>
			<#list statis as s>
				<tr>
					<td>${(s.type)!}</td>
					<td>${(s.amount)!}</td>
					<td>${(s.number)!}</td>
					<td>${(s.expiredDate)!}</td>
				</tr>
			</#list>
		</#if>
	</table>
	
	<#if export>
		<script type="text/javascript">
			window.open('${ctx}/boon/cdkey/export?production=${production!}');
		</script>
	</#if>
</body>
</html>