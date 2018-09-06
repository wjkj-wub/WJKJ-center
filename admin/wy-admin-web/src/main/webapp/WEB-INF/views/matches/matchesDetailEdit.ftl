<html>
	<head>
		<script type="text/javascript" editor-ctx="${ctx!}" src="${ctx!}/static/plugin/editor.js"></script>
		<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
		<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
		<style type="text/css">
			textarea.form-control {
				height: 100px;
			}
			#Processs, .Process {
				padding: 5px;
				margin: 5px;
				border: solid 1px #CCC;
				border-radius: 5px;
				box-shadow: 0px 0px 3px #CCC;
			}
		</style>
	</head>
	<body>
		<article id="editor">
			<form class="form-horizontal" role="form" method="post">
				<input type="hidden" name="id" id="id"/>
				<input type="hidden" name="title" id="title"/>
				<input type="hidden" name="organiserId" id="organiserId"/>
				<input type="hidden" name="itemsId" id="itemsId"/>
				<input type="hidden" name="leagueId" id="leagueId"/>
				<input type="hidden" name="video" id="video"/>
				<input type="hidden" name="prize" id="prize"/>
				<input type="hidden" name="processNames" id="processNames"/>
				<input type="hidden" name="startTimeStrs" id="startTimeStrs"/>
				<input type="hidden" name="endTimeStrs" id="endTimeStrs"/>
				<input type="hidden" name="processIds" id="processIds"/>
				<input type="hidden" name="delProcessIds" id="delProcessIds"/>
				<input type="hidden" name="icon" id="icon"/>
				<input type="hidden" name="logo" id="logo"/>
				<#if type?? && type==1>
					<div class="form-group">
						<label class="col-md-2 control-label">赛事介绍（h5）<font id="font_id_7" color="red">*</font></label>
						<div class="col-md-10">
							<textarea id="textarea_id_introduce1" name="summary" placeholder="" autofocus></textarea>
							<script type="text/javascript">
								_editor = editor($('#textarea_id_introduce1'));
							</script>
						</div>
					</div>
				</#if>
				<#if type?? && type==2>
					<div class="form-group">
						<label class="col-md-2 control-label">赛制规则（h5）<font id="font_id_7" color="red">*</font></label>
						<div class="col-md-10">
							<textarea id="textarea_id_introduce2" name="rule" placeholder="" autofocus></textarea>
							<script type="text/javascript">
								_editor = editor($('#textarea_id_introduce2'));
							</script>
						</div>
					</div>
				</#if>
				<#if type?? && type==3>
					<div class="form-group">
						<label class="col-md-2 control-label">赛事奖励（h5）<font id="font_id_7" color="red">*</font></label>
						<div class="col-md-10">
							<textarea id="textarea_id_introduce3" name="reward" placeholder="" autofocus></textarea>
							<script type="text/javascript">
								_editor = editor($('#textarea_id_introduce3'));
							</script>
						</div>
					</div>
				</#if>
				<div class="form-group">
					<div class="col-md-offset-2 col-md-10">
						<a type="button" class="btn btn-info" onclick="save();">保存</a>
					</div>
				</div>
			</form>
		</article>
		<script type="text/javascript">
			// 定义编辑器及表单
			var $editor = $('#editor'),
			$form = $editor.find('form');
			var delProcessIds="";
			// 初始化编辑框
			$.fillForm({
				id: '${(matches.id)!}',
				title: '${(matches.title)!}',
				organiserId:'${(matches.organiserId)!}',
				itemsId:'${(matches.itemsId)!}',
				leagueId:'${(matches.leagueId)!}',
				icon:	'${(matches.icon)!}',
				logo:	'${(matches.logo)!}',
				video:	'${(matches.video)!}',
				prize:	'${(matches.prize)!}',
				summary:'${(matches.summary)!}',
				rule:'${(matches.rule)!}',
				reward:'${(matches.reward)!}',
				processNames:'${(processNames)!}',
				startTimeStrs:'${(startTimeStrs)!}',
				endTimeStrs:'${(endTimeStrs)!}',
				processIds:'${(processIds)!}',
				delProcessIds:'${(delProcessIds)!}',
			}, $editor);
		
			function save(){
				var formValid = $form.formValid();
				if(formValid) {
					$form.prop('action', '${ctx!}/matches/detailSave').prop('target', '_self').submit();
					$form.submit();
				}
			}			
		</script>
		
	</body>
</html>