<#import "/macros/pager.ftl" as p >
<html>
<head>
<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/simditor/css/simditor.css">
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/simditor/css/font-awesome.css">
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/simditor/css/simditor.scss">
<script type="text/javascript" editor-ctx="${ctx!}" src="${ctx!}/static/plugin/simditor/editor.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/module.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/hotkeys.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/uploader.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/simditor.min.js"></script>
<style type="text/css">
	.modal-dialog {
		width: 800px;
	}
</style>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>管理系统</strong>
 		</li>
		<li class="active">
			查看中奖名单
		</li>
	</ul>
	
	<#-- 操作按钮 -->
	<div class="mb10">
		<button type="button" class="btn btn-success" onclick="history.go(-1);">返回</button>
	</div>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
			<div class="col-md-2">
				<input type="text" name="username" value="${(params.username)!}" class="form-control" placeholder="姓名">
			</div>
			<div class="col-md-2">
				<input type="text" name="telephone" value="${(params.telephone)!}" class="form-control" placeholder="联系方式">
			</div>
			<div class="col-md-2">
				<input type="text" name="beginDate" value="${(params.beginDate)!}" class="form-control" placeholder="中奖时间(起)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:beginPick})">
			</div>
			<div class="col-md-2">
				<input type="text" name="endDate" value="${(params.endDate)!}" class="form-control" placeholder="中奖时间(止)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:endPick})">
			</div>
			<button id="btn-query" type="submit" class="btn btn-success">查询</button>
			<a class="btn btn-success" href="1">清空</a>
			<button id="btn-export" type="submit" class="btn btn-success">导出</button>
		</form>
	</div>
	<script type="text/javascript">
		// 导出当前页
		$('#btn-export').on('click', function() {
			var $form = $('#search');
			$form.prop('action', '${ctx}/lottery/history/export/${lotteryId!}/0').prop('target', '_blank').submit();
		});
		// 查询
		$('#btn-query').on('click', function() {
			var $form = $('#search');
			$form.prop('action', '1').prop('target', '_self').submit();
		});
	</script>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>#</th>
			<th>姓名</th>
			<th>联系方式</th>
			<th>中奖信息</th>
			<th>中奖时间</th>
		</tr>
		<#if list??>
		<#list list as o>
			<tr>
				<td>${o.id!}</td>
				<td>${o.userName!}</td>
				<td>${o.userTelephone!}</td>
				<td>${o.awardName!} - ${o.prizeName!}</td>
				<td>${o.createDate!}</td>
			</tr>
		</#list>
		</#if>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
</body>
</html>