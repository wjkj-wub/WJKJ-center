<#import "/information/editCommon.ftl" as ec >
<html>
	<head>
		<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
		<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
		<script type="text/javascript" editor-ctx="${ctx!}" src="${ctx!}/static/plugin/editor.js"></script>
		<style type="text/css">
		div[img] {
			padding: 20px 0;
			border: 1px solid #CCC;
			border-radius: 3px;
		}
		
		img.preview {
			max-width: 200px;
			margin: 5px;
		}
		</style>
	</head>
	<body>
		<article id="editor">
			<form class="form-horizontal" role="form" method="post">
				<legend></legend>
				<input type="hidden" name="id" />
				<div class="form-group">
					<label class="col-md-2 control-label required">名称</label>
					<div class="col-md-4">
						<input type="text" class="form-control" name="name" maxlength="13" placeholder="请输入名称" wy-required="名称" />
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-offset-2 col-md-10">
						<a type="button" class="btn btn-info" href="${ctx}/thirdparty/cdkey/category/1"><i class="icon-angle-left"></i> 返回列表</a>
						<button id="submit" type="button" class="btn btn-primary">保存</button>
					</div>
				</div>
			</form>
		</article>
		<script type="text/javascript">
			// 定义编辑器及表单
			var $editor = $('#editor'),
				$form = $editor.find('form');
			
			// 填充标题
			<#assign insert = !(editObj.id)??>
			$editor.find('legend').html('<#if insert>录入内容<#else>编辑内容</#if>');
			
			// 初始化编辑框
			$.fillForm({
				id: '${(editObj.id)!}',
				name: '${(editObj.name)!}',
			}, $editor);
			
			$(function() {
				// 保存事件
				$form.find('#submit').on('click', function() {
					var $this = $(this);
					var formValid = $form.formValid();
					if(formValid) {
						// 提交表单
						$this.prop('disabled', true);
						$form.ajaxSubmit({
							url:'${ctx}/thirdparty/cdkey/category/save',
							type : 'post',
							dataType: 'json',
							success : function(d) {
								if (d.code == 0) {
									window.location = '${ctx}/thirdparty/cdkey/category/1';
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
			});
		</script>
	</body>
</html>