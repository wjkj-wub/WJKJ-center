<#import "/macros/pager.ftl" as p >
<html>
<head>
<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li><i class="icon icon-location-arrow mr10"></i> <strong>兑换码</strong>
		</li>
		<li class="active">导入LOL皮肤兑换码</li>
	</ul>

	<#-- 生产条件 -->
	<div class="mb10">
		<form id="form-import" class="form-horizontal" role="form" method="post">
			<div class="form-group">
				<label class="col-md-2 control-label">用户导入</label>
				<div class="col-md-10">
					<input type="file" class="form-control" name="file" accept=".xls"  />
				</div>
			</div>
			<div class="form-group">
				<label class="col-md-2 control-label"></label>
				<div class="col-md-10">
					<button id="import" class="btn btn-info" type="button" data-toggle="tooltip" data-placement="top" title="" data-original-title="请将excel文件保存为Microsoft Excel 97(*.xls)文件">点击导入</button>
					<button type="button" class="btn btn-info" onclick="window.open('${ctx}/static/example/boon/皮肤Cdeky导入模版.xls')" data-toggle="tooltip" data-placement="top" title="" data-original-title="请将excel文件保存为Microsoft Excel 97(*.xls)文件">下载模版</button>
					<a class="btn btn-info" href="${ctx}/boon/cdkey/list/1?production=201608腾讯皮肤">查看皮肤CDKEY</a>
				</div>
			</div>
		</form>
	</div>
	<script type="text/javascript">
		var $form = $('#form-import');
		
		// 通过excel导入
		$('#import').on('click', function(ev) {
			var $this = $(this);
			$this.prop('disabled', true);
			$form.ajaxSubmit({
				url:'${ctx}/boon/cdkey/importSkin',
				type:'post',
				success: function(d){
					if(d.code == 0) {
						$form.find('input[name="importNum"]').val(d.object);
						alert('已导入' + d.object + '个cekey');
						window.location.reload();
					} else {
						alert(d.result);
					}
				},
				complete: function() {
					$this.prop('disabled', false);
				}
			});
		});
	</script>
</body>
</html>