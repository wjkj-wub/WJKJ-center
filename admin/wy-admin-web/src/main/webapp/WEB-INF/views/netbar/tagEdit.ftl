<html>
	<head>
		<script type="text/javascript" editor-ctx="${ctx!}" src="${ctx!}/static/plugin/editor.js"></script>
		<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
		<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
	</head>
	<body>
		<article id="editor">
			<form class="form-horizontal" role="form" method="post">
				<input type="hidden" name="tagId" value="${(netbarTag.id)!}">
				<div class="form-group">
					<label class="col-md-2 control-label required">标签名</label>
					<div class="col-md-4">
						<input type="text" class="form-control" name="name" maxlength="5" placeholder="请输入标签" wy-required="标签" value="${(netbarTag.name)!}"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">分数</label>
					<div class="col-md-2">
						<select class="form-control" name="level" id="level" value="${(netbarTag.level)!}">
							<option value="1" <#if (netbarTag.level)?? && netbarTag.level==1>selected</#if>>1</option>
							<option value="2" <#if (netbarTag.level)?? && netbarTag.level==2>selected</#if>>2</option>
							<option value="3" <#if (netbarTag.level)?? && netbarTag.level==3>selected</#if>>3</option>
							<option value="4" <#if (netbarTag.level)?? && netbarTag.level==4>selected</#if>>4</option>
							<option value="5" <#if (netbarTag.level)?? && netbarTag.level==5>selected</#if>>5</option>
						</select>
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-offset-2 col-md-10">
						<a type="button" class="btn btn-info" href="${ctx}/netbat/tag/list"><i class="icon-angle-left"></i> 返回列表</a>
						<button id="submit" type="button" class="btn btn-primary">生成</button>
					</div>
				</div>
			</form>
		</article>
		<script type="text/javascript">
			// 定义编辑器及表单
			var $editor = $('#editor'),
			$form = $editor.find('form');
			// 保存事件
			$form.find('#submit').on('click', function() {
				var $this = $(this);
				var formValid = $form.formValid();
				if(formValid) {
					// 提交表单
					$this.prop('disabled', true);
					$form.ajaxSubmit({
						url:'${ctx}/netbat/tag/save',
						type : 'post',
						dataType: 'json',
						success : function(d) {
							if (d.code == 0) {
								window.location = '${ctx}/netbat/tag/list';
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