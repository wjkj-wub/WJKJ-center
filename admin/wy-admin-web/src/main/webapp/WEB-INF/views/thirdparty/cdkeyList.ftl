<#import "/macros/pager.ftl" as p >
<html>
	<head>
		<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
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
				第三方cdkey列表
			</li>
		</ul>
		
		<#-- 搜索 -->
		<div class="mb12">
			<form id="search" action="1" method="post">
				<input type="hidden" name="categoryId" value="${(params.categoryId)!}" />
				<div class="mb12 col-md-12">
					<div class="col-md-2">
						<input class="form-control" type="text" name="cdkey" maxlength="30" placeholder="cdkey" value="${(params.cdkey)!}" />
					</div>
					<div class="col-md-2">
						<select class="form-control" name="isUsed">
							<option value="">全部状态</option>
							<option value="0"<#if ((params.isUsed)!"") == "0"> selected</#if>>未使用</option>
							<option value="1"<#if ((params.isUsed)!"") == "1"> selected</#if>>已使用</option>
						</select>
					</div>
					<div class="col-md-4">
						<div class="col-md-6">
							<button class="btn btn-info" type="submit">提交</button>
							<a class="btn btn-info" href="1?categoryId=${(params.categoryId)!}">清空</a>
						</div>
					</div>
				</div>
				<div class="mb12 col-md-12">
					<div class="col-md-4">
						<a class="btn btn-success" href="${ctx}/thirdparty/cdkey/category/list/1">返回类目管理</a>
						<button import type="button" class="btn btn-success">导入</button>
					</div>
				</div>
			</form>
		</div>
		
		<#-- 表格 -->
		<table class="table table-striped table-hover">	
			<tr>
				<th>ID</th>
				<th>cdkey</th>
				<th>使用状态</th>
				<th>操作</th>
			</tr>
			<#if list??>
				<#list list as o>
					<tr>
						<td>${(o.id)!}</td>
						<td>${(o.cdkey)!}</td>
						<td>${((o.isUsed == 1)?string('已使用', '未使用'))!}</td>
						<td>
							<button delete="${(o.id)!}" type="button" class="btn btn-danger">删除</button>
						</td>
					</tr>
				</#list>
			</#if>
		</table>
		
		<#-- 分页 -->
		<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
		
		<#-- 导入窗口 -->
		<div id="modal-import" class="modal fade">
			<div class="modal-dialog">
				<div class="modal-content">
					<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal">
							<span>×</span><span class="sr-only">关闭</span>
						</button>
						<h4 class="modal-title" >导入cdkey</h4>
					</div>
					<div class="modal-body">
						<form class="form-horizontal form-condensed" role="form" method="post">
							<input type="hidden" name="categoryId" value="${(params.categoryId)!}"/>
							<div class="form-group">
								<label class="col-md-2 control-label">文件</label>
								<div class="col-md-10">
									<input class="form-control" type="file" name="file" wy-required="文件" accept="application/vnd.ms-excel" />
								</div>
							</div>
							<div class="form-group">
								<label class="col-md-2 control-label"></label>
								<div class="col-md-10">
									<a href="${ctx}/static/example/thirdparty/第三方cdkey导入模版.xls" target="_blank">下载模版</a>
								</div>
							</div>
						</form>
					</div>
					<div class="modal-footer">
						<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
						<button id="submit" type="button" class="btn btn-primary">保存</button>
					</div>
				</div>
			</div>
		</div>
		
		<#-- 操作中提示框 -->
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
			var $operating = $('#modal-operating');
			$operating.modal({
				keyboard : false,
				show : false,
				backdrop : 'static'
			});
			
			$('[delete]').on('click', function(ev) {
				var $this = $(this);
				var id = $this.attr('delete');
				
				$.confirm('确认删除吗?', function() {
					$this.prop('disabled', true);
					
					$.api('${ctx}/thirdparty/cdkey/delete', {id: id}, function(d) {
						window.location.reload();
					});
				});
			});
			
			var $importEditor = $("#modal-import");
			$('[import]').on('click', function(ev) {
				$importEditor.modal('show');
			});
			
			$importEditor.find('#submit').on('click', function(ev) {
				var $this = $(this);
				$this.prop('disabled', true);
				$importEditor.modal('hide');
				$operating.modal('show');
				
				var $form = $importEditor.find('form');
				var formValid = $form.formValid();
				if(formValid) {
					$form.ajaxSubmit({
						url: '${ctx}/thirdparty/cdkey/import',
						type: 'post',
						success: function(d) {
							if(d.code == 0) {
								window.location = '${ctx}/thirdparty/cdkey/list/1?categoryId=${(params.categoryId)!}';
							} else {
								$importEditor.modal('show');
								alert(d.result);
							}
						},
						complete: function() {
							$this.prop('disabled', false);
							$operating.modal('hide');
						}
					});
				}
			});
		</script>
	</body>
</html>