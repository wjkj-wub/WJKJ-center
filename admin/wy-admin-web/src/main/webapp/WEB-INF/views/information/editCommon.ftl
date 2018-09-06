<#-- 产生模块选择树,并自动初始化 -->
<#macro modules modulesJson infoModules >
	<div id="module">
		<div class="form-group">
			<label class="col-md-2 control-label">模块</label>
			<div class="col-md-6 parent" module-pid="0">
			</div>
		</div>
	</div>
	<script type="text/javascript">
		var $module = $('#module');
		$(function() {
			//检查是否草稿状态
			var $isPublished = $("#isPublished");
			if($isPublished.length>0){
				$isPublished.bind("click",function(){
					$editor.find(".help-block").css('display','none');
					$editor.find(".has-error").removeClass("has-error");
				})
			}		
		
			// 产生复选框
			function genCheckbox(id, name) {
				var html = '<label>'
					 + '	<input type="checkbox" name="moduleId" value="' + id + '" />' + name + '&nbsp;'
					 + '</label>';
				return html;
			}
			
			// 产生子模块复选区
			function genModuleHtml(node) {
				var children = node.children;
				if(children && children.length > 0) {
					var childrenHtml = '';
					for(var i=0; i<children.length; i++) {
						var c = children[i];
						childrenHtml += genCheckbox(c.id, c.name);
					}
					
					var html = '<div class="form-group">'
						 + '	<label class="col-md-2 control-label">' + node.name + ' 二级模块</label>'
						 + '	<div class="col-md-6 children" module-pid="' + node.id + '">'
						 + childrenHtml
						 + '	</div>'
						 + '</div>';
					return html;
				}
				
			}
			
			// 初始化模块选择框
			var zns = [];
			try {
				zns = JSON.parse('${modulesJson?replace('\\"', '\\\\"')!}');
			} catch(e) {
				console.log('格式化模块框数据异常:');
				console.log(e);
			}
			if(zns && zns.length > 0) {
				for(var i=0; i<zns.length; i++) {
					var zn = zns[i];
					$module.find('.parent').append(genCheckbox(zn.id, zn.name));
					
					// 增加子模复选区块
					$module.append(genModuleHtml(zn));
				}
			}
			
			// 隐藏二级模块
			$('#module .children').each(function() {
				$(this).parents('.form-group').hide();
			});
			
			// 一级模块点击事件
			$('#module .parent input[name="moduleId"]').on('change', function(ev) {
				var pid = $(this).val();
				var $group = $('#module [module-pid="' + pid + '"]').parent('.form-group');
				if($(this).prop('checked')) {
					$group.show();
				} else {
					$group.hide();
					$group.find('input[name="moduleId"]').prop('checked', false);
				}
			});
			
			<#if infoModules??>
				// 初始化勾选状态
				<#list infoModules as i>
					$('input[type="checkbox"][name="moduleId"][value="${(i.moduleId)!}"]').prop('checked', true).change();
				</#list>
			</#if>
			$module.find("[name='moduleId']").each(function(){
				$(this).on('change',function(){
					if($(this).is(':checked')){
						if($(this).val()=='30'||$(this).val()=='36'){
							$module.find("[name='moduleId']").attr('checked', false);
							$(this).prop('checked', true);
						 	$("#zhuanti").hide();
						 	$('input[type="text"][name="title"]').attr('disabled', 'disabled');
						 	$('input[type="file"][name="coverFile"]').attr('disabled', 'disabled');
						 	$('input[type="text"][name="title"]').removeAttr('wy-required');
						 	$("#suole").hide();
						 	$("#matches").show();
						 	$("#imgFileValid").val(1);
						 }else{
						 	$('input[type="text"][name="title"]').removeAttr('disabled');
						 	$('input[type="file"][name="coverFile"]').removeAttr('disabled');
						 	$('input[type="text"][name="title"]').attr('wy-required', '标题');
						 }
					 }else{
					 	$('input[type="text"][name="title"]').attr('wy-required', '标题');
						$('input[type="text"][name="title"]').removeAttr('disabled');
					 	$('input[type="file"][name="coverFile"]').removeAttr('disabled');
					 	if($(this).val()=='30'){
						 	$("#zhuanti").show();
						 	$("#suole").show();
						 	$("#matches").hide();
						 	$("#imgFileValid").val(0);
						 }
					 }
					 
				})
			});
		});
		
		// 记录选中的模块ID
		var checkedModuleIds = [];
		// 获取模块选择树中被选中的节点
		function getModuleIds() {
			checkedModuleIds = [];
			
			$module.find('input[type="checkbox"][name="moduleId"]:checked').each(function() {
				var id = $(this).val();
				checkedModuleIds.push(id);
			});
			
			return checkedModuleIds;
		}
		
		// 获取选中的模块ID字符串
		function getModuleIdsStr() {
			var moduleIdsStr = '';
			var moduleIds = getModuleIds();
			if(typeof(moduleIds) !== 'undefined' && moduleIds.length > 0) {
				for(var i=0; i<moduleIds.length; i++) {
					var id = moduleIds[i];
					
					if(moduleIdsStr.length > 0) {
						moduleIdsStr += ',';
					}
					moduleIdsStr += id;
				}
			}
			return moduleIds;
		}
		
		// 检查是否已选择模块
		function moduleValid() {
			var checkedCount = $module.find('[name="moduleId"]:checked').length;
			var $isPublished = $("#isPublished");
			var valid = checkedCount > 0;
			if(valid) {
				$('#module .form-group:last').formCorr();
				return true;
			} else {
				if($isPublished.length>0){
					var checked = $("#isPublished").prop("checked");
					if(checked){
						return true;
					}
				}
				$('#module .form-group:last').formErr('模块选项不能为空');
				return false;
			}
		}
	</script>
</#macro>



<#macro matches matchesJson matchesSelectValue >

	<div id="match">
		<div class="form-group">
			<label class="col-md-2 control-label">赛事</label>
			<div class="col-md-4">
				<select id="select-matches" class="form-control" name="activityId"></select>
			</div>
		</div>
	</div>
	<script type="text/javascript">
   			$(function() {
	   			var matches = [];
				try {
					matches = JSON.parse('${matchesJson?replace('\\"', '\\\\"')!}');
					console.log(matches);
				} catch(e) {
					console.log('格式化赛事数据异常:');
					console.log(e);
				}
				var $matches = $('#select-matches');
				$matches.html('<option value="">选择赛事信息</option>');
				var matchesValue = ${matchesSelectValue?default(0)};
				if(typeof(matches) !== 'undefined' && matches.length > 0) {
					for(var i=0; i<matches.length; i++) {
						var m = matches[i];
						if(m.id == matchesValue){
							$matches.append('<option value="' + m.id + '" selected>' + m.title + '</option>');
						}else{
							$matches.append('<option value="' + m.id + '">' + m.title + '</option>');
						}
					}
				}
			});
	</script>
</#macro>


<#macro audition auditionJson auditionSelectValue >

	<div id="audition">
		<div class="form-group">
			<label class="col-md-2 control-label">华体赛事</label>
			<div class="col-md-4">
				<select id="select-audition" class="form-control" name="auditionId"></select>
			</div>
		</div>
	</div>
	<script type="text/javascript">
   			$(function() {
	   			var auditiones = [];
				try {
				console.log("111111");
					auditiones = JSON.parse('${auditionJson?replace('\\"', '\\\\"')!}');
				} catch(e) {
					console.log('格式化华体赛事数据异常:');
					console.log(e);
				}
				var $audition = $('#select-audition');
				$audition.html('<option value="">选择华体赛事信息</option>');
				var auditionValue = ${auditionSelectValue?default(0)};
				if(typeof(auditiones) !== 'undefined' && auditiones.length > 0) {
					for(var i=0; i<auditiones.length; i++) {
						var m = auditiones[i];
						if(m.id == auditionValue){
							$audition.append('<option value="' + m.id + '" selected>' + m.name + '</option>');
						}else{
							$audition.append('<option value="' + m.id + '">' + m.name + '</option>');
						}
					}
				}
			});
	</script>
</#macro>



<#-- 产生资讯、图集编辑框 -->
<#macro editType editObj infoImgsJson modelsJson  >
	<div id="div-info">
		<div class="form-group">
			<label class="col-md-2 control-label">模版</label>
			<div class="col-md-4">
				<select id="select-models" class="form-control"></select>
			</div>
		</div>
		<div class="form-group">
			<label class="col-md-2 control-label">详情</label>
			<div class="col-md-4">
				<textarea id="editor-remark" name="remark" placeholder="详情"></textarea>
			</div>
		</div>
	</div>
	<div id="div-imgs">
		<input type="hidden" name="picSetNum" />
		<div class="form-group">
			<label class="col-md-2 control-label ">图集图片:</label>
			<div class="col-md-4"></div>
		</div>
		<div class="form-group" id="div-add">
			<label class="col-md-2 control-label"></label>
			<div class="col-md-4">
				<button type="button" class="btn btn-info" add>增加</button>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		// 更改类型
		function changeInfoType(type) {
			var $isPublished = $("#isPublished");
			if(type == 1) {// 资讯
				$('#div-info').show();
				$('#div-imgs').hide();
				$('#div-imgs .remark').removeAttr('wy-required');
				$('#div-imgs .img-file').removeAttr('wy-required');
				$('#div-info [name="remark"]').attr('wy-required', '详情');
			} else {// 图集
				$('#div-info').hide();
				$('#div-imgs').show();
				$('#div-imgs .remark').attr('wy-required', '详情');
				$('#div-imgs .img-file').attr('wy-required', '图片');
				$('#div-info [name="remark"]').removeAttr('wy-required');
			}
		}
		
		// 初始化移除按钮的事件
		function initRemoveImgBtns() {
			var $removeBtns = $('#div-imgs [remove]');
			$removeBtns.off('click');
			$removeBtns.on('click', function(ev) {
				var $this = $(this);
				$.confirm('移除的数据将丢失,是否确定移除?', function() {
					if($('#div-imgs [remove]').length <= 1) {
						alert('图集不能少于一个图片');
						return;
					}
					
					var ids = $this.attr('remove');
					var $imgDiv = $this.parents('[img]');
					if(typeof(ids) !== 'undefined' && ids.length > 0) {
						$.api('${ctx}/overActivity/removeImg', {'ids': ids}, function(d) {
							if($this.attr('multiple-remove')) {
								$imgDiv.remove();
								setNum -= 1;
							} else {
								var $imgDiv = $this.siblings('img[img-id="' + ids + '"]').parent();
								if($imgDiv.siblings('div').length > 0) {
									$imgDiv.remove();
								} else {
									$imgDiv.parents('[img]').remove();
								}
							}
							
							if($('#div-imgs div[img]').length < 1) {
								$addImg.click();
							}
						}, false, {
							async: false
						});
					} else {
						$imgDiv.remove();
					}
					$('#div-imgs input[name="picSetNum"]').val(setNum);
					
					// 重算图集下标
					var setIndex = 1;
					$('div [img]').each(function() {
						$this.find('[name="picSetIndex"]').val(setIndex);
						$this.find('.img-file').attr('name', 'imgFiles' + setIndex);
						$this.find('.remark').attr('name', 'remark' + setIndex);
						setIndex += 1;
					});
				});
			});
		}
		
		// 增加或移除图集事件
		var $addImg = $('#div-add [add]'),
			setNum = 0;
		$addImg.on('click', function(ev) {
			setNum += 1;
			$('#div-imgs input[name="picSetNum"]').val(setNum);
			
			var html = '<div class="form-group" img>'
				 + '	<div class="form-group">'
				 + '		<label class="col-md-2 control-label">图片</label>'
				 + '		<div class="col-md-4">'
				 + '			<input type="file" class="form-control img-file" name="imgFiles' + setNum + '" multiple wy-required="图片" />'
				 + '		</div>'
				 + '	</div>'
				 + '	<div class="form-group">'
				 + '		<label class="col-md-2 control-label">详情</label>'
				 + '		<div class="col-md-4">'
				 + '			<textarea class="form-control remark" name="remark' + setNum + '" maxlength="200" wy-required="详情"></textarea>'
				 + '		</div>'
				 + '	</div>'
				 + '	<div class="form-group">'
				 + '		<label class="col-md-2 control-label"></label>'
				 + '		<div class="col-md-4">'
				 + '			<button type="button" class="btn btn-info" remove>移除</button>'
				 + '		</div>'
				 + '	</div>'
				 + '</div>';
			 $('#div-add').before(html);
			
			initRemoveImgBtns();
		});
		
		$(function() {
			// 初始化文本编辑器
			_editor = editor($('#editor-remark'));
			setEditorText($('#editor-remark'), '${(editObj.remark?replace('\r', '\\r')?replace('\n', '\\n'))!}');
			
			// 初始化图集
			var infoImgs = [];
			try {
				infoImgs = JSON.parse('${infoImgsJson?replace('\\"', '\\\\"')!}');
			} catch(e) {
				console.log('格式化图集数据异常:');
				console.log(e); 
			}
			if(infoImgs && infoImgs.length > 0) {
				for(var i=0; i<infoImgs.length; i++) {
					var infoImg = infoImgs[i];
					$addImg.click();
					
					var setIndex = i+1;
					$('#div-imgs [name="remark' + setIndex + '"]')
						.val(infoImg.remark).parents('[img]')
						.append('<input type="hidden" class="ids" name="ids' + setIndex + '" value="' + infoImg.ids + '" />')
						.find('[remove]').attr('remove', infoImg.ids).attr('multiple-remove', true);
					var imgs = infoImg.imgs;
					if(imgs.length > 0) {
						for(var j=0; j<imgs.length; j++) {
							var img = imgs[j];
							$('#div-imgs [name="imgFiles' + setIndex + '"]').before('<div><img src="${imgServer!}' + img.url + '" class="preview" img-id="' + img.id + '" /><button type="button" class="btn btn-info" remove="' + img.id + '">移除</button></div>');
						}
						$('#div-imgs [name="imgFiles' + setIndex + '"]').remove();
					}
				}
				initRemoveImgBtns();
			} else {
				$addImg.click();
			}
			
			var models = [];
			try {
				models = JSON.parse('${modelsJson?replace('\\"', '\\\\"')!}');
			} catch(e) {
				console.log('格式化模版数据异常:');
				console.log(e);
			}
			var $models = $('#select-models');
			$models.html('<option value="">选择模版</option>');
			if(typeof(models) !== 'undefined' && models.length > 0) {
				for(var i=0; i<models.length; i++) {
					var m = models[i];
					$models.append('<option value="' + m.id + '">' + m.title + '</option>');
				}
			}
			$models.on('click', function(ev) {
				var modelId = $(this).val();
				if(typeof(models) !== 'undefined' && models.length > 0) {
					for(var i=0; i<models.length; i++) {
						var model = models[i];
						if(model.id == modelId) {
							setEditorText($('#editor-remark'), model.remark);
							break;
						}
					}
				}
			});
			
			
			
		});
	</script>
</#macro>

<#-- 详情视频封面 -->
<#macro videoCoverImgs editObj imgServer>
	<div id="div-videoCoverImgs">
		<input type="hidden" name="videoCoverImgsNum" />
		<div class="form-group">
			<label class="col-md-2 control-label ">视频封面图片:</label>
			<div class="col-md-4"></div>
		</div>
		<div class="form-group" id="div-videoCover-add">
			<label class="col-md-2 control-label"></label>
			<div class="col-md-4">
				<button type="button" class="btn btn-info" add>增加</button>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		var videoCoverNum = 0;
		
		// 增加视频封面数量
		function addVideoCoverNum(n) {
			videoCoverNum += n;
			$('#div-videoCoverImgs [name="videoCoverImgsNum"]').val(videoCoverNum);
		}
		
		// 初始化视频封面相关按钮事件
		function initVideoCoverBtns() {
			var $removeBtns = $('#div-videoCoverImgs [remove]');
			$removeBtns.off('click');
			$removeBtns.on('click', function(ev) {
				$(this).parents('[img]').remove();
				addVideoCoverNum(-1);
				
				// 重算图片下标
				var videoCoverIndex = 0;
				$('#div-videoCoverImgs .videoCover-file,.videoCover-url').each(function() {
					videoCoverIndex += 1;
					
					var name = '';
					if($(this).hasClass('videoCover-file')) {
						name = 'videoCoverFile' + videoCoverIndex;
					} else {
						name = 'videoCoverUrl' + videoCoverIndex;
					}
					
					$(this).attr('name', name);
				});
			});
		}
		
		// 增加视频封面
		$('#div-videoCoverImgs [add]').on('click', function(ev) {
			if(videoCoverNum >= 10) {
				alert('封面图片不能超过10张');
				return false;
			}
			
			addVideoCoverNum(1);
			var html = '<div class="form-group" img>'
				 + '	<div class="form-group">'
				 + '		<label class="col-md-2 control-label required">图片</label>'
				 + '		<div class="col-md-4">'
				 + '			<input type="file" class="form-control videoCover-file" name="videoCoverFile' + videoCoverNum + '" multiple wy-required="图片" />'
				 + '		</div>'
				 + '	</div>'
				 + '	<div class="form-group">'
				 + '		<label class="col-md-2 control-label"></label>'
				 + '		<div class="col-md-4">'
				 + '			<button type="button" class="btn btn-info" remove>移除</button>'
				 + '		</div>'
				 + '	</div>'
				 + '</div>';
			$('#div-videoCover-add').before(html);
			initVideoCoverBtns();
		});
		
		$(function() {
			// 初始化资讯视频封面图片
			var videoCoverImgs = '${(editObj.videoCoverImgs)!}';
			if(videoCoverImgs.length > 0) {
				var imgs = videoCoverImgs.split(',');
				if(imgs.length > 0) {
					for(var i=0; i<imgs.length; i++) {
						var img = imgs[i];
						if(img.length <= 0) {
							continue;
						}
						
						addVideoCoverNum(1);
						var html = '<div class="form-group" img>'
							 + '	<div class="form-group">'
							 + '		<label class="col-md-2 control-label required">图片</label>'
							 + '		<div class="col-md-4">'
							 + '			<input type="hidden" class="form-control videoCover-url" name="videoCoverUrl' + videoCoverNum + '" value="' + img + '" />'
							 + '			<img class="preview" src="${imgServer!}' + img + '" />'
							 + '		</div>'
							 + '	</div>'
							 + '	<div class="form-group">'
							 + '		<label class="col-md-2 control-label"></label>'
							 + '		<div class="col-md-4">'
							 + '			<button type="button" class="btn btn-info" remove>移除</button>'
							 + '		</div>'
							 + '	</div>'
							 + '</div>';
						$('#div-videoCover-add').before(html);
					}
					initVideoCoverBtns();
				}
			}
		})
	</script>
</#macro>