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
				<strong>商城管理</strong>
			</li>
			<li class="active">
				每日任务
			</li>
		</ul>
		
		<#-- 表格 -->
		<table class="table table-striped table-hover">	
			<tr>
				<th>名称</th>
				<th>任务次数</th>
				<th>单次金币奖励</th>
				<th>任务说明</th>
				<th>操作</th>
			</tr>
			<#if list??>
				<#list list as o>
					<tr>
						<td>${(o.text)!}</td>
						<td>${(o.limit)!}</td>
						<td>${(o.coin)!}</td>
						<td><a href="javascript:void(0);" preview-remark="${(o.remark?html)!}">查看</a></td>
						<td>
							<a class="btn btn-info" href="${ctx}/malltask/edit?id=${(o.id)!}">编辑</a>
						</td>
					</tr>
				</#list>
			</#if>
		</table>
		
		<#-- 分页 -->
		<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
		
		<div id="modal-preview" class="modal fade">
			<div class="modal-dialog">
				<div class="modal-content">
					<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal">
							<span>×</span><span class="sr-only">关闭</span>
						</button>
						<h4 class="modal-title">查看任务说明</h4>
					</div>
					<div class="modal-body"></div>
					<div class="modal-footer">
						<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
					</div>
				</div>
			</div>
		</div>
		<script type="text/javascript">
			$('a[preview-remark]').on('click', function(ev) {
				var remark = $(this).attr('preview-remark');
				var $modal = $('#modal-preview');
				$modal.find('.modal-body').html(remark);
				$modal.modal('show');
			});
		</script>
	</body>
</html>