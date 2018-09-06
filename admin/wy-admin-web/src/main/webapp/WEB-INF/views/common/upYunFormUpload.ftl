<html>
	<head>
		<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
	</head>
	<body>
		<input id="infoId" type="text" name="infoId" placeholder="请输入资讯ID" />
		<form id="upload" action="">
			<input type="file" name="file" />
			<button id="check" type="button" class="btn btn-warning">校验</button>
			<button id="upload" type="button" class="btn btn-info">上传</button>
		</form>
		<div id="result" class="help-block alert">等待上传中 ..</div>
		<script type="text/javascript">
			var $form = $('#upload');
			var $alert = $('#result');
			
			// 初始化操作按钮
			var checkFile = false;
			function initUploadBtn() {
				var filename = $form.find('[name="file"]').val();
				var $check = $form.find('#check');
				var $upload = $form.find('#upload');
				if(checkFile && checkFile == filename) {// 文件已校验,可进行上传
					$check.hide();
					$upload.show();
				} else {// 未校验或文件变更
					$check.show();
					$upload.hide();
				}
			}
			$form.find('[name="file"]').on('change', function(ev) {
				initUploadBtn();
			});
			$form.find('[name="file"]').change();
			
			// 校验
			$form.find('#check').on('click', function(ev) {
				// 检查表单必填项
				var $file = $form.find('[name="file"]');
				var filename = $file.val();
				var fileChecked = true;
				if(filename.length <= 0) {
					$file.formErr('请先选择文件');
					fileChecked = false;
				} else {
					$file.formCorr();
				}
				
				// 校验文件
				if(fileChecked) {
					var $this = $(this);
					$this.prop('disabled', true);
					
					var infoId = $('#infoId').val();
					$alert.html('文件校验中, 请稍等 ...');
					$.api('${ctx}/upYunFormApi/check', {'filename': filename, 'infoId': infoId}, function(d) {
						var res = d.object;
						// 初始化校验数据
						var action = res.action;
						var signature = res.signature;
						var policy = res.policy;
						var params = res.params;
						
						function hidden(name, value) {
							return '<input type="hidden" name="' + name + '" value="' + value + '" />';
						}
						$form.attr('action', action);
						if(params) {
							for(var k in params) {
								var value = params[k];
								var input = $form.get(0).appendChild(document.createElement('input'));
								var $input = $(input);
								$input.prop('type', 'hidden');
								$input.attr('name', k);
								$input.val(value);
							}
						}
						$form.append(hidden('signature', signature));
						$form.append(hidden('policy', policy));
						
						// 初始化表单操作按钮
						checkFile = res.filename;
						initUploadBtn();
						
						$alert.html('文件校验完成,准备上传');
					}, false, {
						complete: function() {
							$this.prop('disabled', false);
						}
					});
				}
			});
			
			// 上传
			$form.find('#upload').on('click', function(ev) {
				var $this = $(this);
				$this.prop('disabled', true);
				
				$alert.html('上传中,请稍等 ..');
				$form.ajaxSubmit({
					'url': $form.attr('action'),
					'type': 'post',
					'dataType': 'json',
					'success': function(d) {
						var code = d.code;
						if(code == 200) {
							$alert.html('操作成功,文件路径:' + d.url);
						} else {
							$alert.html('操作失败,原因:' + d.message);
						}
						console.log(d);
					},
					complete: function() {
						console.log('完成');
					}
				});
			});
		</script>
	</body>
</html>