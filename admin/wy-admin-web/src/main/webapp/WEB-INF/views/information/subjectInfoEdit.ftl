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
				<input type="hidden" name="moduleIds" />
				<input type="hidden" name="pId" />
				<input type="hidden" name="isSubject" />
				<input type="hidden" name="isShow" />
				<input type="hidden" name="isTop" />
				<input type="hidden" name="type" />
				<@ec.modules modulesJson=modulesJson! infoModules=infoModules! /> 
				<div class="form-group">
					<label class="col-md-2 control-label">专题名</label>
					<div class="col-md-4">
						<input type="text" class="form-control" name="title" maxlength="13" placeholder="请输入标题" wy-required="标题"/>
					</div>
				</div>
				<@ec.matches matchesJson=matchesJson! matchesSelectValue=matchesSelectValue!/>
				
				<@ec.audition auditionJson=auditionJson! auditionSelectValue=auditionSelectValue!/>
				<div class="form-group">
					<label class="col-md-2 control-label">缩略图</label>
					<div class="col-md-4">
						<input type="file" class="form-control" name="coverFile" accept="image/*" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">生效时间</label>
					<div class="col-md-4">
						<input type="text" class="form-control" name="timerDateStr" placeholder="请输入生效时间" wy-required="生效时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss',minDate:'${(.now?string('yyyy-MM-dd HH:mm:ss'))!}'})" />
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-offset-2 col-md-10">
						<a type="button" class="btn btn-info" href="${ctx}/overActivity/list/2/1"><i class="icon-angle-left"></i> 返回列表</a>
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
			$editor.find('legend').html('<#if insert>新增专题<#else>编辑专题</#if>');
			
			// 初始化编辑框
			$.fillForm({
				id: '${(editObj.id)!}',
				pId: '${(editObj.pId)!0}',
				isSubject: '${(editObj.isSubject)!1}',
				isShow: '${(editObj.isShow)!1}',
				isTop: '${(editObj.isTop)!0}',
				title: '${(editObj.title)!}',
				type: '${(editObj.type)!2}',
				timerDateStr: '${(editObj.timerDate?string('yyyy-MM-dd HH:mm:ss'))!(.now?string('yyyy-MM-dd HH:mm:ss'))}',
				coverFile: '${(editObj.cover)!}',
			}, $editor);
			
			$(function() {
				<#if insert>
					// 检查是否已选择图片
					function imgFileValid() {
						var $imgFile = $form.find('[name="coverFile"]');
						if($imgFile.val().length <= 0) {
							<#if (matchVaild!"0") == "0">
								$imgFile.formErr('请选择图片');
							</#if>
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
					var imgValid = imgFileValid();
					<#if (matchVaild!"0") == "1">
						imgValid = true;
					</#if>
					var modulesValid = moduleValid();
					if(formValid && imgValid && modulesValid) {
						// 记录模块ID到表单中
						$form.find('[name="moduleIds"]').val(getModuleIdsStr());
						
						// 提交表单
						$this.prop('disabled', true);
						$form.ajaxSubmit({
							url:'${ctx}/overActivity/save',
							type : 'post',
							dataType: 'json',
							success : function(d) {
								if (d.code == 0) {
									window.location = '${ctx}/overActivity/list/2/1';
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