<html>
	<head>
		<script type="text/javascript" editor-ctx="${ctx!}" src="${ctx!}/static/plugin/editor.js"></script>
		<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
		<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
		<script type="text/javascript" src="${ctx!}/static/plugin/ztree/js/jquery.ztree.all-3.5.min.js"></script>
		<script type="text/javascript" src="${ctx!}/static/plugin/ztree/js/jquery.ztree.exhide-3.5.min.js"></script>
		<link rel="stylesheet" type="text/css" href="${ctx!}/static/plugin/ztree/css/zTreeStyle.css"/>
	</head>
	<body>
		<article id="editor">
			<form class="form-horizontal" role="form" method="post">
				<input type="hidden" id="matchesLeagueId" name="matchesLeagueId" value="${(matchesLeague.id)!}">
				<input type="hidden" id="organiserId" name="organiserId" value="${organiserId!}">
				<input type="hidden" id="isInsert" name="isInsert" value="${isInsert!}">
				<div class="form-group">
					<label class="col-md-2 control-label required">赛事名</label>
					<div class="col-md-4">
						<input type="text" class="form-control" name="name" maxlength="13" placeholder="请输入主办方官名" wy-required="主办方官名" value="${(matchesLeague.name)!}" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">游戏</label>
					<div class="col-md-2">
						<select class="form-control" name="itemsId" id="itemsId" wy-required="游戏">
							<option value="">全部游戏</option>
							<#if gameList??>
							<#list gameList as i>
								<option value="${i.id!}" <#if (matchesLeague.itemsId) ?? && matchesLeague.itemsId==i.id>selected</#if>>${i.name!}</option>
							</#list>
							</#if>
						</select>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">logo</label>
					<div class="col-md-4">
						<input type="file" class="form-control" name="logo" accept="image/*"/>
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-offset-2 col-md-10">
						<a type="button" class="btn btn-info" href="${ctx}/league/list/1?organiserId=${organiserId!}"><i class="icon-angle-left"></i> 返回列表</a>
						<button id="submit" type="button" class="btn btn-primary">保存</button>
					</div>
				</div>
			</form>
		</article>
		<script type="text/javascript">
			// 定义编辑器及表单
			var $editor = $('#editor'),
			$form = $editor.find('form');
			function imgValidate() {
				var $iconFile = $form.find('[name="logo"]');
				if($iconFile.val().length <= 0) {
					if($("#isInsert")=="0"){
						$iconFile.formErr('请选择图标');
						return false;
					}
				}	
				return true;
			}
			// 保存事件
			$form.find('#submit').on('click', function() {
				var $this = $(this);
				var formValid = $form.formValid();
				var imgValid = imgValidate();
				if(formValid && imgValid) {
					// 提交表单
					$this.prop('disabled', true);
					$form.ajaxSubmit({
						url:'${ctx}/league/save',
						type : 'post',
						dataType: 'json',
						success : function(d) {
							if (d.code == 0) {
								window.location = '${ctx}/league/list/1?organiserId='+d.object;
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