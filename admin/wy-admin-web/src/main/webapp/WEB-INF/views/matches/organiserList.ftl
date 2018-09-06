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
			主办方管理
		</li>
	</ul>
	
	<#-- 操作按钮 -->
	<div class="mb10">
		<a href="${ctx!}/organiser/edit" type="button" class="btn btn-success">新增主办方</a>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>#</th>
			<th>主办方官名</th>
			<th>主办方logo</th>
			<th>操作</th>
		</tr>
		<#if list??>
			<#list list as o>
				<tr>
					<td>${o.id!}</td>
					<td>${o.name!}</td>
					<td><img src="${imgServer!}/${(o.logo)!}" style="width: 50px; height: 50px;" /></td>
					<td>
						<a href="${ctx!}/league/list/1?organiserId=${o.id!}" type="button" class="btn btn-info">赛事列表</a>
						<a href="${ctx!}/organiser/edit?organiserId=${o.id!}" type="button" class="btn btn-danger">编辑</a>
						<a href="javascript:void(0)" organiserId=${o.id!} type="button" class="btn btn-danger deleteOrganiser">删除</a>
					</td>
				</tr>
			</#list>
		</#if>
	</table>
	<script type="text/javascript">
		$(function(){
			$(".deleteOrganiser").click(function(){
				var organiserId = $(this).attr("organiserId");
				$.get("${ctx!}/organiser/delete?organiserId="+organiserId,function(data,status){
				    alert("删除成功！");
				    window.location.reload();
				  });
			})
		})
	</script>
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	
	
</body>
</html>