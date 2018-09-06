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
			赛事管理
		</li>
	</ul>
	
	<#-- 操作按钮 -->
	<div class="mb10">
		<a href="${ctx!}/league/edit?organiserId=${organiserId!}" type="button" class="btn btn-success">新增赛事</a>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>#</th>
			<th>赛事名</th>
			<th>游戏名</th>
			<th>赛事logo</th>
			<th>操作</th>
		</tr>
		<#if list??>
			<#list list as o>
				<tr>
					<td>${o.id!}</td>
					<td>${o.name!}</td>
					<td>${(o.gameName)!}</td>
					<td><img src="${imgServer!}/${(o.logo)!}" style="width: 50px; height: 50px;" /></td>
					<td>
						<a href="${ctx!}/league/edit?organiserId=${organiserId!}&leagueId=${o.id!}" type="button" class="btn btn-danger">编辑</a>
					</td>
				</tr>
			</#list>
		</#if>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	
	
</body>
</html>