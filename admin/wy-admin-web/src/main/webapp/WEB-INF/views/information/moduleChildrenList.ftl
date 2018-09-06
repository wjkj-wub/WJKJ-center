<#import "/macros/pager.ftl" as p >
<html>
	<head>
	</head>
	<body>
		<#-- 导航 -->
		<ul class="breadcrumb">
			<li>
				<i class="icon icon-location-arrow mr10"></i>
				<strong>资讯管理</strong>
			</li>
			<li class="active">
				子模块管理
			</li>
		</ul>
		
		<#-- 搜索 -->
		<div class="mb10">
			<form id="search" action="1" method="post">
				<div class="mb10">
					<div class="mb10 col-md-10 container">
						<div class="col-md-4">
							<a type="button" class="btn btn-info" href="${ctx}/overActivity/module/list/1"><i class="icon-angle-left"></i> 返回列表</a>
							<a type="button" class="btn btn-success" href="${ctx}/overActivity/module/children/edit?pid=${pid!}">新增</a>
						</div>
					</div>
				</div>
			</form>
		</div>
		
		<#-- 表格 -->
		<table class="table table-striped table-hover">	
			<tr>
				<th>排序</th>
				<th>上级模块</th>
				<th>模块名称</th>
				<th>操作</th>
			</tr>
			<#if list??>
				<#list list as o>
					<tr>
						<td>
							<select name="sortNum" order-num="${(o.orderNum)!}" module-id="${(o.id)!}"></select>
						</td>
						<td>${(o.parentName)!}</td>
						<td>${(o.name)!}</td>
						<td>
							<a type="button" class="btn btn-info" href="${ctx}/overActivity/module/children/edit?id=${(o.id)!}&pid=${pid!}">编辑</a>
							<button delete="${(o.id)!}" type="button" class="btn btn-danger">删除</button>
						</td>
					</tr>
				</#list>
			</#if>
		</table>
		
		<#-- 分页 -->
		<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage /> 
		
		<script type="text/javascript">
			(function init() {
				// 初始化排序列
				var maxSortNum = parseInt('${maxOrderNum!1}');// 排序最大值
				var selectOptions = '';
				for(var i=1; i<=maxSortNum; i++) {
					selectOptions += '<option value="' + i + '">' + i + '</option>';
				}
				$('[name="sortNum"]').each(function() {
					var orderNum = $(this).attr('order-num');
					$(this).html(selectOptions).val(orderNum);
				});
				
				// 更改排序
				$('[name="sortNum"]').on('change', function(ev) {
					var moduleId = $(this).attr('module-id');
					var orderNum = $(this).val();
					
					$.api('${ctx}/overActivity/module/order', {
						'moduleId': moduleId,
						'orderNum': orderNum
					}, function(d) {
						window.location.reload();
					});
				});
				
				// 删除
				$('[delete]').on('click', function(ev) {
					var $this = $(this);
					
					$.confirm('确认删除吗?', function() {
						$this.prop('disabled', true);
						var moduleId = $this.attr('delete');
						$.api('${ctx}/overActivity/module/delete', {
							'moduleId': moduleId
						}, function(d) {
							window.location.reload();
						}, false, {
							complete: function(xhr) {
								$this.prop('disabled', false);
							}
						});
					});
				});
			}());
		</script>
	</body>
</html>