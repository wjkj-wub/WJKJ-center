<#import "/information/editCommon.ftl" as ec >
<html>
	<head>
		<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
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
					<label class="col-md-2 control-label required">标题</label>
					<div class="col-md-4">
						<input type="text" class="form-control" name="title" maxlength="10" placeholder="请输入标题" wy-required="标题" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">创建人</label>
					<div class="col-md-4">
						<input type="text" class="form-control" name="creater" maxlength="10" placeholder="请输入创建人" wy-required="创建人" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">详情</label>
					<div class="col-md-4">
						<textarea id="editor-remark" name="remark" placeholder="详情" wy-required="详情"></textarea>
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-offset-2 col-md-10">
						<a type="button" class="btn btn-info" href="${ctx}/overActivity/model/list/1"><i class="icon-angle-left"></i> 返回列表</a>
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
			$editor.find('legend').html('<#if insert>新增模版<#else>编辑模版</#if>');
			
			// 初始化编辑框
			$.fillForm({
				id: '${(editObj.id)!}',
				title: '${(editObj.title)!}',
				creater: '${(editObj.creater)!}',
			}, $editor);
			
			// 初始化文本编辑器
			_editor = editor($('#editor-remark'));
			setEditorText($('#editor-remark'), '${(editObj.remark)!}');
			
			$(function() {
				// 类型更改事件
				$form.find('[name="type"]').on('change', function(ev) {
					var type = $(this).val();
					changeInfoType(type);
				});
				$form.find('[name="type"]').change();
				
				<#if insert>
					// 检查是否已选择图片
					function imgFileValid() {
						var $imgFile = $form.find('[name="iconFile"]');
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
					
					$form.find('[name="timerDateStr"]').prop('disabled', true);
				</#if>
				
				// 保存事件
				$form.find('#submit').on('click', function() {
					var $this = $(this);
					var formValid = $form.formValid();
					if(formValid) {
						// 提交表单
						$this.prop('disabled', true);
						$form.ajaxSubmit({
							url:'${ctx}/overActivity/model/save',
							type : 'post',
							dataType: 'json',
							success : function(d) {
								if (d.code == 0) {
									window.location = '${ctx}/overActivity/model/list/1';
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