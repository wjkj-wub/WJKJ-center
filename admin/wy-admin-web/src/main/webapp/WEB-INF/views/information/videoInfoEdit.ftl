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
				<input type="hidden" name="type" />
				<input type="hidden" name="isTop" />
				<input type="hidden" name="oldVideoUrl" />
				<@ec.modules modulesJson=modulesJson! infoModules=infoModules! /> 
				<div class="form-group">
					<label class="col-md-2 control-label required">标题</label>
					<div class="col-md-4">
						<input type="text" class="form-control" name="title" maxlength="15" placeholder="请输入标题" wy-required="标题" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">生效时间</label>
					<div class="col-md-4">
						<input type="text" class="form-control" name="timerDateStr" placeholder="请输入生效时间" wy-required="生效时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss',minDate:'${(.now?string('yyyy-MM-dd HH:mm:ss'))!}'})" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">创建人</label>
					<div class="col-md-4">
						<input type="text" class="form-control" name="creater" maxlength="10" placeholder="请输入创建人" wy-required="创建人" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">缩略图</label>
					<div class="col-md-4">
						<input type="file" class="form-control" name="iconFile" accept="image/*" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">关键字</label>
					<div class="col-md-4">
						<input type="text" class="form-control" name="keyword" maxlength="15" placeholder="请输入关键字" wy-required="关键字" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">首图</label>
					<div class="col-md-4">
						<input type="file" class="form-control" name="coverFile" accept="image/*" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">视频地址</label>
					<div class="col-md-4">
						<input type="text" class="form-control" name="videoUrlUpload" placeholder="点击上传视频" onclick="$('#modal-uploader').modal('show');$fileForm.find('[name=\'file\']').val('').change()" wy-required="视频地址" readonly />
						<a id="a-videoUrl" href="" target="_blank"></a>
					</div>
				</div>
				<!-- <div class="form-group">
					<label class="col-md-2 control-label required">视频</label>
					<div class="col-md-4">
						<input type="file" class="form-control" name="videoFile" accept="video/*" />
					</div>
				</div> -->
				<#if hlsTaskProgress?? && hlsTaskProgress?length gt 0>
					<div class="form-group">
						<label class="col-md-2 control-label">视频分片进度</label>
						<div class="col-md-4">
							${(hlsTaskProgress)!}%
						</div>
					</div>
				</#if>
				<div class="form-group">
					<div class="col-md-offset-2 col-md-10">
						<a type="button" class="btn btn-info" href="${ctx}/overActivity/list/4/1"><i class="icon-angle-left"></i> 返回列表</a>
						<button id="submit" type="button" class="btn btn-primary">保存</button>
					</div>
				</div>
			</form>
		</article>
		
		<!-- 操作中提示 -->
		<div id="modal-operating" class="modal fade">
			<div class="modal-dialog">
				<div class="modal-content">
					<div class="modal-header">提示</div>
					<div class="modal-body" style="text-align:center;">
						<div><img src="${ctx}/static/images/loading.gif" /></div>
						保存中，文件上传可能需要较长时间,请勿中断 ...
					</div>
				</div>
			</div>
		</div>
		
		<script type="text/javascript">
		// 初始化模态框
		$('#modal-operating').modal({
			keyboard : false,
			show : false,
			backdrop : 'static'
		});
		</script>
		
		<script type="text/javascript">
			// 定义编辑器及表单
			var $editor = $('#editor'),
				$form = $editor.find('form');
			
			// 填充标题
			<#assign insert = !(editObj.id)??>
			$editor.find('legend').html('<#if insert>录入内容<#else>编辑内容</#if>');

			$editor.find('[name="videoUrlUpload"]').on('change', function(ev) {
				var url = ('${imgServer!}' + $(this).val()).replace(/.com\/\//g, '.com/').replace(/.m3u8/, '.mp4');
				$('#a-videoUrl').attr('href', url).html(url);
			});
			
			$.fillForm({
				id: '${(editObj.id)!}',
				pId: '${(editObj.pId)!0}',
				type: '${(editObj.type)!4}',
				isSubject: '${(editObj.isSubject)!0}',
				isShow: '${(editObj.isShow)!1}',
				isTop: '${(editObj.isTop)!0}',
				title: '${(editObj.title)!}',
				brief: '${(editObj.brief)!}',
				timerDateStr: '${(editObj.timerDate?string('yyyy-MM-dd HH:mm:ss'))!(.now?string('yyyy-MM-dd HH:mm:ss'))}',
				creater: '${(editObj.creater)!}',
				videoUrl: '${(editObj.videoUrl)!}',
				keyword: '${(editObj.keyword)!}',
				iconFile: '${(editObj.icon)!}',
				coverFile: '${(editObj.cover)!}',
				videoUrlUpload: '${(editObj.videoUrl)!}',
				oldVideoUrl: '${(editObj.videoUrl)!}',
			}, $editor);
			$editor.find('[name="videoUrlUpload"]').change();
			
			var videoUrl = '${(editObj.videoUrl)!}';
			if(videoUrl.length > 0) {
				var $videoUrl = $form.find('[name="videoFile"]');
				$videoUrl.siblings('a').remove();
				$videoUrl.after('<a href="${(imgServer)}' + videoUrl + '" target="_blank">视频地址</a>');
			}
			
			$(function() {
				<#if insert>
					// 检查是否已选择图片
					function imgFileValid() {
						var result = true;
						var $imgFile = $form.find('[name="iconFile"]');
						if($imgFile.val().length <= 0) {
							$imgFile.formErr('请选择缩略图');
							result = false;
						} else {
							$imgFile.formCorr();
						}
						
						$imgFile = $form.find('[name="coverFile"]');
						if($imgFile.val().length <= 0) {
							$imgFile.formErr('请选择首图');
							result = false;
						} else {
							$imgFile.formCorr();
						}
						return result;
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
					var modulesValid = moduleValid();
					if(formValid && imgValid && modulesValid) {
						// 记录模块ID到表单中
						$form.find('[name="moduleIds"]').val(getModuleIdsStr());
						
						// 提交表单
						$this.prop('disabled', true);
						$('#modal-operating').modal('show');
						$form.ajaxSubmit({
							url:'${ctx}/overActivity/save',
							type : 'post',
							dataType: 'json',
							success : function(d) {
								if (d.code == 0) {
									window.location = '${ctx}/overActivity/list/4/1';
								} else {
									alert(d.result);
								}
							},
							complete: function() {
								$this.prop('disabled', false);
								$('#modal-operating').modal('hide');
							}
						});
					}
				});
			});
		</script>
		<!-- 视频上传 -->
		<div id="modal-uploader" class="modal fade">
			<div class="modal-dialog" style="width: 800px;">
				<div class="modal-content">
					<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal">
							<span>×</span><span class="sr-only">关闭</span>
						</button>
						<h4 class="modal-title">视频上传</h4>
					</div>
					<div class="modal-body">
						<form id="form-upload" class="form-horizontal" action="">
							<div class="form-group">
								<label class="col-md-2 control-label required">视频</label>
								<div class="col-md-8">
									<div class="input-group">
										<input type="file" class="form-control" name="file" accept="video/*" />
										<span class="input-group-btn">
											<button id="check" type="button" class="btn btn-warning">校验</button>
											<button id="upload" type="button" class="btn btn-info">上传</button>
										</span>
									</div>
								</div>
							</div>
						</form>
					</div>
					<div class="modal-footer">
						<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
					</div>
				</div>
			</div>
		</div>
		<script type="text/javascript">
		var $fileForm = $('#modal-uploader #form-upload');
		
		// 初始化操作按钮
		var checkFile = false;
		function initUploadBtn() {
			var filename = $fileForm.find('[name="file"]').val();
			var $check = $fileForm.find('#check');
			var $upload = $fileForm.find('#upload');
			if(checkFile && checkFile == filename) {// 文件已校验,可进行上传
				$check.hide();
				$upload.show();
			} else {// 未校验或文件变更
				$check.show();
				$upload.hide();
			}
		}
		$fileForm.find('[name="file"]').on('change', function(ev) {
			initUploadBtn();
		});
		$fileForm.find('[name="file"]').change();
		
		// 校验
		$fileForm.find('#check').on('click', function(ev) {
			// 检查表单必填项
			var $file = $fileForm.find('[name="file"]');
			var $prompt = $file.parent();
			var filename = $file.val();
			var fileChecked = true;
			if(filename.length <= 0) {
				$prompt.formErr('请先选择文件');
				fileChecked = false;
			} else {
				$prompt.formCorr();
			}
			
			// 校验文件
			if(fileChecked) {
				var $this = $(this);
				$this.prop('disabled', true);
				
				var infoId = $('#infoId').val();
				$prompt.formErr('文件校验中, 请稍等 ...');
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
					$fileForm.attr('action', action);
					if(params) {
						for(var k in params) {
							var value = params[k];
							var input = $fileForm.get(0).appendChild(document.createElement('input'));
							var $input = $(input);
							$input.prop('type', 'hidden');
							$input.attr('name', k);
							$input.val(value);
						}
					}
					$fileForm.append(hidden('signature', signature));
					$fileForm.append(hidden('policy', policy));
					
					// 初始化表单操作按钮
					checkFile = res.filename;
					initUploadBtn();
					
					$prompt.formCorr();
				}, false, {
					complete: function() {
						$this.prop('disabled', false);
					}
				});
			}
		});
		
		// 上传
		$fileForm.find('#upload').on('click', function(ev) {
			var $this = $(this);
			$this.prop('disabled', true);
			var $prompt = $fileForm.find('[name="file"]').parent();
			
			$prompt.formErr('上传中,请稍等 ..');
			$('#modal-operating').modal('show');
			$('#modal-uploader').modal('hide');
			$fileForm.ajaxSubmit({
				'url': $fileForm.attr('action'),
				'type': 'post',
				'dataType': 'json',
				'success': function(d) {
					var code = d.code;
					if(code == 200) {
						$form.find('input[name="videoUrlUpload"]').val(d.url).change();
						$prompt.formCorr();
					} else {
						$('#modal-uploader').modal('show');
						$prompt.formErr('操作失败,原因:' + d.message);
					}
					console.log(d);
				},
				complete: function() {
					console.log('完成');
					$('#modal-operating').modal('hide');
				}
			});
		});
		</script>
	</body>
</html>