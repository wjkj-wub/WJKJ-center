<html>
	<head>
		<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
	</head>
	<body>
		<article id="editor">
			<form class="form-horizontal" role="form" method="post">
				<legend></legend>
				<input type="hidden" name="id" />
				<input type="hidden" name="pid" />
				<div class="form-group">
					<label class="col-md-2 control-label required">模块名称</label>
					<div class="col-md-4">
						<input type="text" class="form-control" name="name" maxlength="10" placeholder="请输入模块名称" wy-required="模块名称" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">模块类型</label>
					<div class="col-md-4">
						<select class="select-3 form-control chosen" name="type" wy-required="模块类型">
							<option value="">请选择</option>
							<option value="1">资讯</option>
							<option value="2">视频</option>
							<option value="3">官方赛事</option>
							<option value="4">华体赛事</option>
						</select>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">图标</label>
					<div class="col-md-4">
						<input type="file" class="form-control" name="imgFile" />
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-offset-2 col-md-10">
						<a type="button" class="btn btn-info" href="${ctx}/overActivity/module/list/1"><i class="icon-angle-left"></i> 返回列表</a>
						<button id="submit" type="button" class="btn btn-primary">保存</button>
					</div>
				</div>
			</form>
		</article>
		<script type="text/javascript">
			// 定义编辑器及表单
			var $editor = $('#editor'),
				$form = $editor.find('form');
			
			<#if isInsert>
				// 检查是否已选择图片
				function imgFileValid() {
					var $imgFile = $form.find('[name="imgFile"]');
					if($imgFile.val().length <= 0) {
						$imgFile.formErr('请选择图片');
						return false;
					} else {
						$imgFile.formCorr();
						return true;
					}
				}
			<#else>
				// 检查是否已选择图片
				function imgFileValid() {
					return true;
				}
				
				// 编辑时不允许修改类型
				$form.find('[name="type"]').prop('disabled', true);
			</#if>
			
			// 填充标题
			$editor.find('legend').html('<#if isInsert>录入内容<#else>编辑内容</#if>');
			
			// 初始化编辑框
			$.fillForm({
				id: '${(module.id)!}',
				pid: '${(module.pid)!0}',
				name: '${(module.name)!}',
				type: '${(module.type)!}',
				imgFile: '${(module.img)!}',
			}, $editor);
			
			// 保存事件
			$form.find('#submit').on('click', function() {
				var $this = $(this);
				var formValid = $form.formValid();
				var imgValid = imgFileValid();
				if(formValid && imgValid) {
					$this.prop('disabled', true);
					$form.ajaxSubmit({
						url:'${ctx}/overActivity/module/save',
						type : 'post',
						success : function(d) {
							if (d.code == 0) {
								window.location = '${ctx}/overActivity/module/list/1';
							} else {
								alert(d.result);
							}
						},
						complete: function() {
							$this.prop('disabled', false);
						}
					});
				}
			});
		</script>
	</body>
</html>