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
						<input type="text" class="form-control" name="text" maxlength="45" placeholder="名称" wy-required="名称" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">任务次数</label>
					<div class="col-md-4">
						<input type="text" class="form-control" name="limit" maxlength="3" placeholder="请输入任务次数" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">单次金币奖励</label>
					<div class="col-md-4">
						<input type="text" class="form-control" name="coin" maxlength="5" placeholder="请输入单次金币奖励" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">任务说明</label>
					<div class="col-md-4">
						<textarea id="editor-remark" name="remark" maxlength="100" placeholder="请输入任务说明" wy-required="任务说明"></textarea>
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-offset-2 col-md-10">
						<a type="button" class="btn btn-info" href="${ctx}/malltask/list/1"><i class="icon-angle-left"></i> 返回列表</a>
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
			
			$(function() {
				// 初始化编辑框
				$.fillForm({
					id: '${(editObj.id)!}',
					text: '${(editObj.text)!}',
					limit: '${(editObj.limit)!}',
					coin: '${(editObj.coin)!}',
				}, $editor);
				
				var $remark = $('#editor-remark');
				_editor = editor($remark);
				setEditorText($remark, '${(editObj.remark)!}');
				
				function validateLimitCoin() {
					var result = true;
					
					var $limit = $form.find('[name="limit"]');
					var limit = parseInt($limit.val());
					if(isNaN(limit) || limit < 0 || limit > 100) {
						$limit.formErr('任务次数必须为100以内的整数');
						result = false;
					} else {
						$limit.formCorr();
					}
					
					var $coin = $form.find('[name="coin"]');
					var coin = parseInt($coin.val());
					if(isNaN(coin) || coin < 0 || coin > 10000) {
						$coin.formErr('单次金币奖励数须为10000以内的整数');
						reuslt = false;
					} else {
						$coin.formCorr();
					}
					
					return result;
				}
				
				// 保存事件
				$form.find('#submit').on('click', function() {
					var $this = $(this);
					var formValid = $form.formValid();
					var validLimitCoin = validateLimitCoin();
					if(formValid && validLimitCoin) {
						// 提交表单
						$this.prop('disabled', true);
						$form.ajaxSubmit({
							url:'${ctx}/malltask/save',
							type : 'post',
							dataType: 'json',
							success : function(d) {
								if (d.code == 0) {
									window.location = '${ctx}/malltask/list/1';
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