<#import "/information/editCommon.ftl" as ec >
<html>
	<head>
		<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
		<style type="text/css">
			.cover {
				padding: 5px;
			}
			.cover img {
				height: 200px;
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
					<label class="col-md-2 control-label required">版本号</label>
					<div class="col-md-4">
						<input type="text" class="form-control" name="version" maxlength="13" placeholder="请输入版本号" wy-required="版本号" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">下载次数</label>
					<div class="col-md-4">
						<input type="number" class="form-control" name="downloadCount" maxlength="13" placeholder="请输入下载次数" wy-required="下载次数" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">游戏简介</label>
					<div class="col-md-4">
						<textarea class="form-control" name="desc" maxlength="1000" placeholder="请输入游戏简介" wy-required="游戏简介" style="height:200px;"></textarea>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">内容摘要</label>
					<div class="col-md-4">
						<textarea class="form-control" name="intro" maxlength="1000" placeholder="请输入内容摘要" wy-required="内容摘要" style="height:200px;"></textarea>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">评分</label>
					<div class="col-md-4">
						<input type="number" class="form-control" name="score" maxlength="1" placeholder="请输入评分" wy-required="评分" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">推荐</label>
					<div class="col-md-4">
						<select class="form-control" name="isRecommand">
							<option value="1">是</option>
							<option value="0">否</option>
						</select>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label">IOS下载地址</label>
					<div class="col-md-4">
						<input type="text" class="form-control" name="urlIOS" maxlength="255" placeholder="请输入IOS下载地址" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">IOS客户端大小</label>
					<div class="col-md-4">
						<input type="text" class="form-control" name="iosFileSize" maxlength="13" placeholder="请输入IOS客户端大小" wy-required="IOS客户端大小" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label">Android下载地址</label>
					<div class="col-md-4">
						<input type="text" class="form-control" name="urlAndroid" maxlength="255" placeholder="请输入Android下载地址" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">Android客户端大小</label>
					<div class="col-md-4">
						<input type="text" class="form-control" name="androidFileSize" maxlength="13" placeholder="请输入Android客户端大小" wy-required="Android客户端大小" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">图标</label>
					<div class="col-md-4">
						<input type="file" class="form-control" name="iconFile" accept="image/*" />
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">轮播图片</label>
					<div class="col-md-4">
						<input type="file" class="form-control" name="coverFiles" accept="image/*" multiple />
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-offset-2 col-md-10">
						<a type="button" class="btn btn-info" href="${ctx}/game/list/1"><i class="icon-angle-left"></i> 返回列表</a>
						<button id="submit" type="button" class="btn btn-primary">保存</button>
					</div>
				</div>
			</form>
		</article>
		
		<#-- 操作中提示框 -->
		<div id="modal-operating" class="modal fade">
			<div class="modal-dialog">
				<div class="modal-content">
					<div class="modal-header">提示</div>
					<div class="modal-body" style="text-align:center;">
						<div><img src="${ctx}/static/images/loading.gif" /></div>
						处理中，请勿中断 ...
					</div>
				</div>
			</div>
		</div>
		
		<script type="text/javascript">
			// 定义编辑器及表单
			var $editor = $('#editor'),
				$form = $editor.find('form');
			// 初始化模态框
			var $operating = $('#modal-operating');
			$operating.modal({
				keyboard : false,
				show : false,
				backdrop : 'static'
			});
			
			// 填充标题
			<#assign insert = !(editObj.id)??>
			$editor.find('legend').html('<#if insert>录入手游<#else>编辑手游</#if>');
			
			// 初始化编辑框
			$.fillForm({
				id: '${(editObj.id)!}',
				name: '${(editObj.name)!}',
				version: '${(editObj.version)!}',
				downloadCount: '${(editObj.downloadCount)!}',
				desc: '${(editObj.desc?replace('\r\n', '\\r\\n'))!}',
				intro: '${(editObj.intro?replace('\r\n', '\\r\\n'))!}',
				score: '${(editObj.score)!0}',
				isRecommand: '${(editObj.isRecommand)!0}',
				urlIOS: '${(editObj.urlIOS)!}',
				iosFileSize: '${(editObj.iosFileSize)!}',
				urlAndroid: '${(editObj.urlAndroid)!}',
				androidFileSize: '${(editObj.androidFileSize)!}',
				iconFile: '${(editObj.icon)!}',
			}, $editor);
			
			// 初始化图片
			<#if imgs??>
				<#list imgs as i>
					$form.find('[name="coverFiles"]').before('<div class="cover"><img src="${imgServer!}${(i.url)!}" /> <button remove-img="${(i.id)!}" type="button" class="btn btn-info">移除</button></div>');
				</#list>
			</#if>
			
			$(function() {
				function imgValidate() {
					<#if insert>
						var result = true;
						var $iconFile = $form.find('[name="iconFile"]');
						if($iconFile.val().length <= 0) {
							$iconFile.formErr('请选择图标');
							result = false;
						}
						
						var $coverFiles = $form.find('[name="coverFiles"]');
						if($coverFiles.val().length <= 0) {
							$coverFiles.formErr('请选择轮播图片');
							result = false;
						}
						
						return result;
					<#else>
						return true;
					</#if>
				}
				
				// 保存事件
				$form.find('#submit').on('click', function() {
					var $this = $(this);
					var formValid = $form.formValid();
					var imgValid = imgValidate();
					if(formValid && imgValid) {
						// 提交表单
						$this.prop('disabled', true);
						$operating.modal('show');
						$form.ajaxSubmit({
							url:'${ctx}/game/save',
							type : 'post',
							dataType: 'json',
							success : function(d) {
								if (d.code == 0) {
									window.location = '${ctx}/game/list/1';
								} else {
									alert(d.result);
								}
							},
							complete: function() {
								$this.prop('disabled', false);
								$operating.modal('hide');
							}
						});
					}
				});
				
				// 移除图片
				$('[remove-img]').on('click', function(ev) {
					var $this = $(this);
					if($this.parent().siblings('.cover').length < 1) {
						alert('轮播图片至少须保留一张');
					} else {
						$.confirm('移除后将不可回复,确认移除吗', function() {
							var id = $this.attr('remove-img');
							$this.prop('disabled', true);
							$operating.modal('show');
							$.api('${ctx}/game/removeImg', {id: id}, function(d) {
								$this.parent().remove();
							}, false, {
								complete: function() {
									$this.prop('disabled', false);
									$operating.modal('hide');
								}
							});
						});
					}
				});
			});
		</script>
	</body>
</html>