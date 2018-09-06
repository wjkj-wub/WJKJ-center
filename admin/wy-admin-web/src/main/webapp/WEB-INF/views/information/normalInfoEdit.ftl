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
				<@ec.modules modulesJson=modulesJson! infoModules=infoModules! /> 
				<div class="form-group">
					<label class="col-md-2 control-label">类型</label>
					<div class="col-md-4">
						<select class="select-3 form-control chosen" name="type">
							<option value="1">资讯</option>
							<option value="3">图集</option>
						</select>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label">是否是广告</label>
					<div class="col-md-4">
						<input type="checkbox"  name="isAd" value="1" <#if (((editObj.isAd)!0)>0)> checked="checked"</#if> />设置为广告
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label">标题</label>
					<div class="col-md-4">
						<input type="text" class="form-control" name="title" maxlength="13" placeholder="请输入标题" wy-required="标题"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label">摘要</label>
					<div class="col-md-4">
						<input type="text" class="form-control" name="brief" maxlength="26" placeholder="请输入摘要" wy-required="摘要"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label">生效时间</label>
					<div class="col-md-4">
						<input type="text" class="form-control" name="timerDateStr" placeholder="请输入生效时间" wy-required="生效时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss',minDate:'${(.now?string('yyyy-MM-dd HH:mm:ss'))!}'})" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label">创建人</label>
					<div class="col-md-4">
						<input type="text" class="form-control" name="creater" maxlength="10" placeholder="请输入创建人" wy-required="创建人"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label">资讯来源</label>
					<div class="col-md-4">
						<input type="text" class="form-control" name="source" maxlength="10" placeholder="请输入资讯来源" wy-required="资讯来源"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label">缩略图</label>
					<div class="col-md-4">
						<input type="file" class="form-control" name="iconFile" accept="image/*"/>
					</div>
				</div>
				<@ec.editType editObj=editObj! infoImgsJson=infoImgsJson! modelsJson=modelsJson! />
				<#if !editObj?? || ((editObj.isPublished)!0) != 1>
					<div class="form-group">
						<label class="col-md-2 control-label">保存草稿</label>
						<div class="col-md-4" style="padding-top:6px">
							<input type="checkbox" id="isPublished"<#if editObj??> checked</#if> name="isPublished" value="0"/>
						</div>
					</div>
				</#if>
				<div class="form-group">
					<div class="col-md-offset-2 col-md-10">
						<a type="button" class="btn btn-info" href="${ctx}/overActivity/list/1/1"><i class="icon-angle-left"></i> 返回列表</a>
						<button id="submit" type="button" class="btn btn-primary">保存</button>
					</div>
				</div>
			</form>
		</article>
		<script type="text/javascript">
			// 定义编辑器及表单
			var $editor = $('#editor'),
				$form = $editor.find('form');
			var $isPublished = $("#isPublished");
			// 填充标题
			<#assign insert = !(editObj.id)??>
			$editor.find('legend').html('<#if insert>录入内容<#else>编辑内容</#if>');
			
			// 初始化编辑框
			$.fillForm({
				id: '${(editObj.id)!}',
				pId: '${(editObj.pId)!0}',
				isSubject: '${(editObj.isSubject)!0}',
				isShow: '${(editObj.isShow)!0}',
				isTop: '${(editObj.isTop)!0}',
				title: '${(editObj.title)!}',
				type: '${(editObj.type)!1}',
				brief: '${(editObj.brief)!}',
				timerDateStr: '${(editObj.timerDate?string('yyyy-MM-dd HH:mm:ss'))!(.now?string('yyyy-MM-dd HH:mm:ss'))}',
				creater: '${(editObj.creater)!}',
				source: '${(editObj.source)!}',
				iconFile: '${(editObj.icon)!}',
			}, $editor);
			
			$(function() {
				// 类型更改事件
				$form.find('[name="type"]').on('change', function(ev) {
					var type = $(this).val();
					changeInfoType(type);
				});
				$form.find('[name="type"]').change();
				if($isPublished.length>0){
					$form.find('[name="timerDateStr"]').removeAttr('disabled');
				}else{
					$form.find('[name="timerDateStr"]').attr('disabled', true);
				}
				
				// 检查是否已选择图片
				function imgFileValid() {
					var iconFile = '${(editObj.icon)!}';
					if(iconFile.length > 0) {// 资讯已有图片
						return true;
					}
					
					var $imgFile = $form.find('[name="iconFile"]');
					if($imgFile.val().length <= 0) {
						$imgFile.formErr('请选择缩略图');
						return false;
					}
					
					$imgFile.formCorr();
					return true;
				}
				
				// 保存事件
				$form.find('#submit').on('click', function() {
					var $this = $(this);
					var formValid = true;
					var imgValid = true;
					var modulesValid = true;
					
					// 非草稿时才做表单验证
					if($isPublished.length <= 0 || !$isPublished.prop('checked')){
						formValid = $form.formValid();
						imgValid = imgFileValid();
						modulesValid = moduleValid();
					}
					
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
									window.location = '${ctx}/overActivity/list/1/1';
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