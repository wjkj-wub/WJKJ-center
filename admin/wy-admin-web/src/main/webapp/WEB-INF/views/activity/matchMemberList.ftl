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
			约战管理
		</li>
	</ul>
	
	<#-- 搜索 -->
	<form id="search" action="1" method="post">
		<div class="mb10">
				<input type="hidden" name="matchId" value="${(params.matchId)!}" />
				<div class="col-md-2">
					<input type="text" class="form-control" name="username" placeholder="发起人号码" value="${(params.username)!}" />
				</div>
				<div class="col-md-2">
					<input class="form-control" type="text" name="beginDate" placeholder="最早报名时间" value="${(params.beginDate)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss'})">
				</div>
				<div class="col-md-2">
					<input class="form-control" type="text" name="endDate" placeholder="最晚报名时间" value="${(params.endDate)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss'})">
				</div>
				<button id="query" type="button" class="btn btn-success">查询</button>
				<a class="btn btn-success" href="1?matchId=${(params.matchId)!}">清空</a>
		</div>
		<div class="mb10">
			<a type="button" class="btn btn-info" href="${ctx!}/activityMatch/list/1">返回约战管理列表</a>
			<button id="export-page" type="button" class="btn btn-info">导出当前页</button>
			<button id="export-all" type="button" class="btn btn-info">导出全部</button>
		</div>
	</form>
	<script type="text/javascript">
	// 导出当前页
	$('#export-page').on('click', function() {
		var $form = $('#search');
		$form.prop('action', 'export/${currentPage!0}').prop('target', '_blank').submit();
	});
	// 导出全部
	$('#export-all').on('click', function() {
		var $form = $('#search');
		$form.prop('action', 'export/0').prop('target', '_blank').submit();
	});
	// 查询
	$('#query').on('click', function() {
		var $form = $('#search');
		$form.prop('action', '1').prop('target', '_self').submit();
	});
	</script>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>序号</th>
			<th>约战名</th>
			<th>竞技项目</th>
			<th>用户昵称</th>
			<th>用户手机号码</th>
			<th>注册时间</th>
			<th>报名约战时间</th>
			<th>约战网吧名称</th>
			<th>发布时间</th>
		</tr>
		<#list list as o>
			<tr>
				<td>${(o.id)!}</td>
				<td>${(o.title)!}</td>
				<td>${(o.itemName)!}</td>
				<td>${(o.nickname)!}</td>
				<td>${(o.username)!}</td>
				<td>${(o.userCreateDate)!}</td>
				<td>${(o.applyCreateDate)!}</td>
				<td>${(o.netbarName)!}</td>
				<td>${(o.matchCreateDate)!}</td>
			</tr>
		</#list>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	
</body>
</html>