<#import "/macros/pager.ftl" as p >
<html>
	<head>
		<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
		<style type="text/css">
			form .mb12.col-md-12 {
				display: inline-block;
			}
		</style>
	</head>
	<body>
		<#-- 导航 -->
		<ul class="breadcrumb">
			<li>
				<i class="icon icon-location-arrow mr10"></i>
				<strong>cdkey管理</strong>
			</li>
			<li class="active">
				dkey渠道列表
			</li>
		</ul>
		
		<#-- 搜索 -->
		<div class="mb12">
			<form id="search" action="1" method="post">
				<div class="mb12 col-md-12">
					<div class="col-md-2">
						<input class="form-control" type="text" name="name" maxlength="30" placeholder="名称" value="${(params.name)!}" />
					</div>
					<div class="col-md-4">
						<div class="col-md-6">
							<button class="btn btn-info" type="submit">提交</button>
							<a class="btn btn-info" href="1">清空</a>
						</div>
					</div>
				</div>
				<div class="mb12 col-md-12">
					<div class="col-md-4">
						<a class="btn btn-success" href="${ctx}/thirdparty/cdkey/category/edit">新增</a>
					</div>
				</div>
			</form>
		</div>
		
		<#-- 表格 -->
		<table class="table table-striped table-hover">	
			<tr>
				<th>ID</th>
				<th>名称</th>
				<th>操作</th>
			</tr>
			<#if list??>
				<#list list as o>
					<tr>
						<td>${(o.id)!}</td>
						<td>${(o.name)!}</td>
						<td>
							<a type="button" class="btn btn-info" href="${ctx}/thirdparty/cdkey/category/edit?id=${(o.id)!}">编辑</a>
							<a type="button" class="btn btn-danger" href="${ctx}/thirdparty/cdkey/list/1?categoryId=${(o.id)!}">管理cdkey</a>
							<button type="button" class="btn btn-default" showurl="mpcdkey/getThirdpartyCdkey?categoryId=${(o.id)!}">查看地址</button>
						</td>
					</tr>
				</#list>
			</#if>
		</table>
		
		<#-- 分页 -->
		<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
		
		<div id="modal-showurl" class="modal fade">
			<div class="modal-dialog">
				<div class="modal-content">
					<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal">
							<span>×</span><span class="sr-only">关闭</span>
						</button>
						<h4 class="modal-title" >查看地址</h4>
					</div>
					<div class="modal-body">
					</div>
					<div class="modal-footer">
						<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
					</div>
				</div>
			</div>
		</div>
		
		<script type="text/javascript">
			var $showUrl = $('#modal-showurl');
			$('[showurl]').on('click', function(ev) {
				var url = '${appDomain!}' + $(this).attr('showurl');
				$showUrl.find('.modal-body').html('<a href="' + url + '">' + url + '</a>');
				$showUrl.modal('show');
			});
		</script>
	</body>
</html>