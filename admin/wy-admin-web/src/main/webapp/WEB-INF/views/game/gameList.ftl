<#import "/macros/pager.ftl" as p >
<html>
	<head>
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
				<strong>手游管理</strong>
			</li>
			<li class="active">
				手游列表
			</li>
		</ul>
		
		<#-- 搜索 -->
		<div class="mb12">
			<form id="search" action="1" method="post">
				<div class="mb12 col-md-12">
					<div class="col-md-2">
						<input class="form-control" type="text" name="name" maxlength="30" placeholder="名称" value="${(params.name)!}" />
					</div>
					<div class="col-md-10">
						<div class="col-md-2">
							<button class="btn btn-info" type="submit">提交</button>
							<a class="btn btn-info" href="1">清空</a>
						</div>
					</div>
				</div>
				<div class="mb12 col-md-12">
					<div class="col-md-4">
						<a class="btn btn-success" href="${ctx}/game/edit">新增</a>
					</div>
				</div>
			</form>
		</div>
		
		<#-- 表格 -->
		<table class="table table-striped table-hover">	
			<tr>
				<th>ID</th>
				<th>图标</th>
				<th>名称</th>
				<th>版本号</th>
				<th>下载量</th>
				<th>操作</th>
			</tr>
			<#if list??>
				<#list list as o>
					<tr>
						<td>${(o.id)!}</td>
						<td><#if (o.icon)??><img src="${imgServer!}${(o.icon)!}" style="width:50px;height:50px;" /></#if></td>
						<td>${(o.name)!}</td>
						<td>${(o.version)!}</td>
						<td>${(o.downloadCount)!}</td>
						<td>
							<a type="button" class="btn btn-info" href="${ctx}/game/edit?id=${(o.id)!}">编辑</a>
							<a type="button" class="btn btn-danger" delete="${(o.id)!}">删除</a>
						</td>
					</tr>
				</#list>
			</#if>
		</table>
		
		<#-- 分页 -->
		<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
		
		<script type="text/javascript">
			$('[delete]').on('click', function(ev) {
				var $this = $(this);
				
				$.confirm('确认删除吗?', function() {
					var id = $this.attr('delete');
					$this.prop('disabled', true);
					$.api('${ctx}/game/delete', {id: id}, function(d) {
						window.location.reload();
					}, false, {
						complete: function() {
							$this.prop('disabled', false);
						}
					});
				});
			});
		</script>
	</body>
</html>