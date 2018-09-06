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
				<strong>资讯管理</strong>
			</li>
			<li class="active">
				资讯模版管理
			</li>
		</ul>
		
		<#-- 搜索 -->
		<div class="mb12">
			<div class="mb12 col-md-12">
				<div class="col-md-4">
					<a class="btn btn-success" href="${ctx}/overActivity/model/edit">新增模版</a>
				</div>
			</div>
		</div>
		
		<#-- 表格 -->
		<table class="table table-striped table-hover">	
			<tr>
				<th>ID</th>
				<th>标题</th>
				<th>创建时间</th>
				<th>创建人</th>
				<th>操作</th>
			</tr>
			<#if list??>
				<#list list as o>
					<tr>
						<td>${(o.id)!}</td>
						<td>${(o.title)!}</td>
						<td>${(o.createDate?string('yyyy-MM-dd HH:mm:ss'))!}</td>
						<td>${(o.creater)!}</td>
						<td>
							<a type="button" class="btn btn-info" href="${ctx}/overActivity/model/edit?id=${(o.id)!}">编辑</a>
							<button delete="${(o.id)!}" type="button" class="btn btn-danger">删除</button>
						</td>
					</tr>
				</#list>
			</#if>
		</table>
		
		<#-- 分页 -->
		<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage /> 
		
		<!-- 操作中提示框 -->
		<div id="modal-operating" class="modal fade">
			<div class="modal-dialog">
				<div class="modal-content">
					<div class="modal-header">提示</div>
					<div class="modal-body" style="text-align:center;">
						<div><img src="${ctx}/static/images/loading.gif" /></div>
						处理中，请勿中断 ...
					</div>
				</div>
			</div>
		</div>
		<script type="text/javascript">
			// 初始化模态框
			$('#modal-operating').modal({
				keyboard : false,
				show : false,
				backdrop : 'static'
			});
			
			function operating() {
				$('#modal-operating').modal('show');
			}
			
			function complete() {
				$('#modal-operating').modal('hide');
			}
		</script>
		
		<script type="text/javascript">
			(function init() {
				// 删除
				$('[delete]').on('click', function(ev) {
					var $this = $(this);
					
					$.confirm('确认删除吗?', function() {
						$this.prop('disabled', true);
						var id = $this.attr('delete');
						
						operating();
						$.api('${ctx}/overActivity/model/delete', {'id': id}, function(d) {
							window.location.reload();
						}, false, {
							complete: function(xhr) {
								$this.prop('disabled', false);
								complete();
							}
						});
					});
				});
			}());
		</script>
	</body>
</html>