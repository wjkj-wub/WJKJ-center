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
				<strong>赛事管理</strong>
			</li>
			<li class="active">
				导入战队
			</li>
		</ul>
		
		<form id="form-import" class="form-horizontal form-condensed" role="form" method="post">
			<input type="hidden" name="categoryId" value="${(params.categoryId)!}"/>
			<div class="form-group">
				<label class="col-md-2 control-label">文件</label>
				<div class="col-md-10">
					<input id="file" class="form-control" type="file" name="file" wy-required="文件" accept="application/vnd.ms-excel" />
				</div>
			</div>
			<div class="form-group">
				<label class="col-md-2 control-label"></label>
				<div class="col-md-10">
					<a href="${ctx}/static/example/activity/{赛事ID}_{场次}_{网吧名称}.xls" target="_blank">下载模版</a>
				</div>
			</div>
			<div class="form-group">
				<label class="col-md-2 control-label"></label>
				<div class="col-md-10">
					<button import type="button" class="btn btn-success">导入</button>
				</div>
			</div>
		</form>
		
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
			var $operating = $('#modal-operating');
		
			$('[import]').on('click', function(ev) {
				var $this = $(this);
				
				var $form = $('#form-import');
				var formValid = $form.formValid();
				if(formValid) {
					var fileName = $form.find('#file').val();
					fileName = fileName.substring(fileName.lastIndexOf('\\') + 1, fileName.length);
					$form.find('#file').attr('name', fileName);
					
					$this.prop('disabled', true);
					$operating.modal('show');
					$form.ajaxSubmit({
						url: '${ctx}/activityInfo/importTeams',
						type: 'post',
						success: function(d) {
							if(d.code == 0) {
								alert(d.object);
							} else {
								alert(d.result);
							}
						},
						complete: function() {
							$operating.modal('hide');
							$this.prop('disabled', false);
						}
					});
				}
			});
		</script>
	</body>
</html>