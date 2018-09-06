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
			<form class="form-horizontal" role="form" method="post" enctype="multipart/form-data">
				<input type="hidden" name="id" id="${(matches.id)!}"/>
				<input type="hidden" name="processNames" id="processNames"/>
				<input type="hidden" name="delProcessIds" id="delProcessIds"/>
				<input type="hidden" name="startTimeStrs" id="startTimeStrs"/>
				<input type="hidden" name="endTimeStrs" id="endTimeStrs"/>
				<input type="hidden" name="processIds" id="processIds"/>
				<input type="hidden" name="iconIsInsert" id="iconIsInsert"/>
				<input type="hidden" name="summary" id="summary"/>
				<input type="hidden" name="rule" id="rule"/>
				<input type="hidden" name="reward" id="reward"/>
				<input type="hidden" name="icon" id="icon"/>
				<input type="hidden" name="state" id="state"/>
				<div class="form-group">
					<label class="col-md-2 control-label required">赛事名称</label>
					<div class="col-md-4">
						<input type="text" class="form-control" id="title" name="title" maxlength="15" placeholder="请输入赛事名称" wy-required="赛事名称" />
					</div>
				</div>
				
				<div class="form-group">
					<label class="col-md-2 control-label required">赛事奖励</label>
					<div class="col-md-4">
						<input type="text" class="form-control" id="prize" name="prize" maxlength="10" placeholder="请输入赛事奖励" wy-required="赛事奖励" />
					</div>
				</div>
			
			
				<div class="form-group">
					<label class="col-md-2 control-label required">主办方</label>
					<div class="col-md-4">
						<select class="select-3 form-control chosen" id="organiserId" name="organiserId" wy-required="主办方">
							<option value="">全部主办方</option>
							<#if organiserList??>
								<#list organiserList as i>
									<option value="${i.id!}"<#if (matches.organiserId)?? && matches.organiserId == i.id> selected</#if>>${i.name!}</option>
								</#list>
							</#if>
						</select>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">游戏名称</label>
					<div class="col-md-4">
						<select class="form-control" name="itemsId" id="itemsId" wy-required="游戏名程">
							<option value="">全部游戏</option>
							<#if gameList??>
							<#list gameList as i>
								<option value="${i.id!}"<#if (matches.itemsId)?? && matches.itemsId == i.id> selected</#if>>${i.name!}</option>
							</#list>
							</#if>
						</select>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label required">赛事名称</label>
					<div class="col-md-4">
						<select class="form-control" name="leagueId" id="leagueId" wy-required="赛事名程">
							<option value="">全部赛事</option>
							<#if leagueList??>
							<#list leagueList as i>
								<option value="${i.id!}"<#if (matches.leagueId)?? && matches.leagueId == i.id> selected</#if>>${i.name!}</option>
							</#list>
							</#if>
						</select>
					</div>
				</div>
				<div id="Processs">
					<div class="Process" Process-index="0">
						<input type="hidden" name="processId" id="processId"/>
					 	<div class="form-group">
							<label class="col-md-2 control-label required">赛程名称</label>
							<div class="col-md-4">
								<input type="text" class="form-control" id="processName" name="processName" maxlength="17" placeholder="请输入赛程名称" wy-required="赛程名称" />
							</div>
						</div>
					 	<div class="form-group">
							<label class="col-md-2 control-label">赛程开始时间</label>
							<div class="col-md-4">
								<input type="text" class="form-control" id="startTimeStr" name="startTimeStr" placeholder="请输入赛程开始时间" wy-required="赛程开始时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',maxDate:'#F{$dp.$D(\'endTimeStr\',{d:0});}'})" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">赛程结束时间</label>
							<div class="col-md-4">
								<input type="text" class="form-control" id="endTimeStr" name="endTimeStr" placeholder="请输入赛程结束时间" wy-required="赛程结束时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',minDate:'#F{$dp.$D(\'startTimeStr\',{d:0});}'})" />
							</div>
						</div>
						<div class="form-group" style="margin:0;"><label class="col-md-2 control-label"></label><div class="col-md-4"><button type="button" removeAward class="btn btn-danger">移除模块</button></div></div>
					</div>
					<div class="form-group"><label class="col-md-2 control-label"></label><div class="col-md-4"><button id="addAward" type="button" class="btn btn-success">点击添加更多模块</button></div></div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label">赛事主页BANNER</label>
					<div class="col-md-4">
						<input type="file" class="form-control" name="iconFile" accept="image/*"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label">视频地址</label>
					<div class="col-md-4">
						<input type="text" class="form-control" name="video" placeholder="点击上传视频" onclick="$('#modal-uploader').modal('show');$fileForm.find('[name=\'file\']').val('').change()" readonly />
						<a id="a-videoUrl" href="" target="_blank"></a>
					</div>
				</div>
				
				<div class="form-group">
					<label class="col-md-2 control-label">赛事介绍（h5）<font id="font_id_7" color="red">*</font></label>
					<div class="col-md-10">
						<button onclick="deteilEdit(1)" type="button" class="btn btn-danger">编辑</button>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label">赛制规则（h5）<font id="font_id_7" color="red">*</font></label>
					<div class="col-md-10">
						<button onclick="deteilEdit(2)" type="button" class="btn btn-danger">编辑</button>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-2 control-label">赛事奖励（h5）<font id="font_id_7" color="red">*</font></label>
					<div class="col-md-10">
						<button onclick="deteilEdit(3)" type="button" class="btn btn-danger">编辑</button>
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-offset-2 col-md-10">
						<a type="button" class="btn btn-info" onclick="temporySave(1);">暂时保存</a>
						<button type="button" class="btn btn-primary" onclick="save()">保存、下一步</button>
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
				logo:	'${(matches.logo)!}',
				iconFile:	'${(matches.icon)!}',
				icon:	'${(matches.icon)!}',
				video:	'${(matches.video)!}',
				prize:	'${(matches.prize)!}',
				summary:'${(matches.summary)!}',
				rule:'${(matches.rule)!}',
				reward:'${(matches.reward)!}',
				iconIsInsert:'${(iconIsInsert)!}',
				logoIsInsert:'${(logoIsInsert)!}',
				summary:'${(matches.summary)!}',
				rule:'${(matches.rule)!}',
				reward:'${(matches.reward)!}',
				delProcessIds:'${(delProcessIds)!}',
				state:'${(matches.state)!}',
			}, $editor);
		
			$editor.find("select[name='organiserId']").change(function(){
				$.ajax({
					url: '${ctx!}/organiserGame/gameList?organiserId='+$("#organiserId").val(),
					success: function(d) {
						if(d.code==0){
							var list=d.object;
							var html="<option value=''>全部游戏</option>";
							for(var i=0; i<list.length; i++) {
								var game=list[i];
								html+='<option value="'+game.id+'">'+game.name+'</option>';
							}
							$("#itemsId").empty();
							$("#itemsId").append(html);
						}
					},
					async: false,
				});
			})
			
			
			$editor.find("select[name='itemsId']").change(function(){
				$.ajax({
					url: '${ctx!}/league/leaguelistByOrganiserId?organiserId='+$("#organiserId").val()+'&itemsId='+$("#itemsId").val(),
					success: function(d) {
						if(d.code==0){
							var list=d.object;
							var html="<option value=''>全部赛事</option>";
							for(var i=0; i<list.length; i++) {
								var matchesLeague=list[i];
								html+='<option value="'+matchesLeague.id+'">'+matchesLeague.name+'</option>';
							}
							$("#leagueId").empty();
							$("#leagueId").append(html);
						}
					},
					async: false,
				});
			})
			
			var initProcessData = '';
			try {
				initProcessData = JSON.parse('${processList!}');
			} catch(expect) {
			}
			var $Processs = $('#Processs');
			var processIndexs = 1;
			// 产生赛程选择区的代码
			function getProcessHtml() {
				var html = '<div class="Process" Process-index="' + processIndexs + '">'
						 + '	<input type="hidden" name="processId" id="processId"/>'
						 + '	<div class="form-group">'
						 + '		<label class="col-md-2 control-label required">赛程名称</label>'
						 + '		<div class="col-md-4">'
						 + '			<input type="text" class="form-control" id="processName" name="processName" maxlength="17" placeholder="请输入赛程名称" wy-required="赛程名称" />'
						 + '		</div>'
						 + '	</div>'
						 + '	<div class="form-group">'
						 + '		<label class="col-md-2 control-label">赛程开始时间</label>'
						 + '		<div class="col-md-4">'
						 + '			<input type="text" class="form-control" id="startTimeStr" name="startTimeStr" placeholder="请输入赛程开始时间" wy-required="赛程开始时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:\'yyyy-MM-dd\'})" />'
						 + '		</div>'
						 + '	</div>'
						 + '	<div class="form-group">'
						 + '		<label class="col-md-2 control-label">赛程结束时间</label>'
						 + '		<div class="col-md-4">'
						 + '			<input type="text" class="form-control" id="endTimeStr" name="endTimeStr" placeholder="请输入赛程结束时间" wy-required="赛程结束时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:\'yyyy-MM-dd\'})" />'
						 + '		</div>'
						 + '	</div>'
						 + '	<div class="form-group" style="margin:0;"><label class="col-md-2 control-label"></label><div class="col-md-4"><button type="button" removeAward class="btn btn-danger">移除模块</button></div></div>'
						 + '</div>';
				processIndexs++;
				return html;
			}		
				
			function initProcessEvents() {		
				// 排行榜移除奖品事件
				var $removeAwards = $('#Processs [removeAward]');
				$removeAwards.off('click');
				$removeAwards.on('click', function(ev) {
					if($Processs.find('.Process').length <= 1) {
						alert('奖品至少保留一个');
						return;
					}
					
					var $this = $(this);
					$.confirm('移除后将不可恢复,确认移除吗?', function() {
						var id = $this.parents('.Process').find('#processId').val();
						if(!isNaN(parseInt(id))) {
							if(delProcessIds.length > 0) {
								delProcessIds += ',';
							}
							delProcessIds += id;
						}
						$this.parents('.Process').remove();
						$('#delProcessIds').val(delProcessIds);
					});
				});
			}
					
					
			var $addAward = $Processs.find('#addAward');
			$addAward.off('click');
			$addAward.on('click', function(ev) {
				$(this).parents('.form-group').before(getProcessHtml());
				initProcessEvents();
			});
					
			// 初始化数据
			if( initProcessData.length > 0) {
				var ipd = initProcessData[0];
				var $prize = $('#Processs .Process:last');
				$prize.find('#processName').val(ipd.name);
				$prize.find('#startTimeStr').val(typeof(ipd.start_date)!="undefined"?formatDate(new Date(ipd.start_date)):ipd.start_date);
				$prize.find('#endTimeStr').val(typeof(ipd.end_date)!="undefined"?formatDate(new Date(ipd.end_date)):ipd.end_date);
				$prize.find('#processId').val(ipd.id);
				for(var i=1; i<initProcessData.length; i++) {
					$addAward.click();
					ipd = initProcessData[i];
					$prize = $('#Processs .Process:last');
					$prize.find('#processName').val(ipd.name);
					$prize.find('#startTimeStr').val(typeof(ipd.start_date)!="undefined"?formatDate(new Date(ipd.start_date)):ipd.start_date);
					$prize.find('#endTimeStr').val(typeof(ipd.end_date)!="undefined"?formatDate(new Date(ipd.end_date)):ipd.end_date);
					$prize.find('#processId').val(ipd.id);
				}
			} 	
			
			//将时间戳格式化为日期
			function   formatDate(now)   {     
	              var   year=now.getFullYear();   
	              var   month=now.getMonth()+1;     
	              var   date=now.getDate();        
	              return   year+"-"+month+"-"+date;     
              }     
			
			// 重排下标
			function resetProcessIndexs() {
				processIndexs = 0;
				$('#Processs .Process').each(function() {
					processIndexs++;
				});
			}	
			
			// 检查是否已选择图片
			function imgFileValid() {
				var $imgFile2 = $form.find('[name="iconFile"]');
				if($imgFile2.val().length <= 0){ 
					if($("#iconIsInsert").val()=='0'){
							$imgFile2.formErr('请选择图片');
							return false;
					}
				}
				return true;
			}
			
			function checkLeague(){		
				if($("#organiserId").val=""){	
					alert("主办方不能为空");
					return false;
				}
				if($("#itemsId").val=""){	
					alert("游戏不能为空");
					return false;
				}
				if($("#leagueId").val=""){	
					alert("赛事不能为空");
					return false;
				}
				return true;
			}
			function save(){
				var formValid = $form.formValid();
				var imgValid = imgFileValid();
				var checkLeagu =checkLeague();
				
				if(formValid &&　imgValid && checkLeagu) {
				
					temporySave(2);
				}
			}
			
			function temporySave(type){
				var processNames='';
				var startTimeStrs='';
				var endTimeStrs='';
				var processIds='';
				$(".Process").each(function(i){
					processNames+=($(this).find('#processName').val()!=""?$(this).find('#processName').val():1)+",";
					startTimeStrs+=($(this).find('#startTimeStr').val()!=""?$(this).find('#startTimeStr').val():1)+",";
					endTimeStrs+=($(this).find('#endTimeStr').val()!=""?$(this).find('#endTimeStr').val():1)+",";
					processIds+=($(this).find('#processId').val()!=""?$(this).find('#processId').val():-1)+",";
				});
				
				$('#processNames').val(processNames);
				$('#startTimeStrs').val(startTimeStrs);
				$('#endTimeStrs').val(endTimeStrs);
				$('#processIds').val(processIds);
				$form.ajaxSubmit({
					url:'${ctx}/matches/save?type='+type+'&flag=false',
					type : 'post',
					dataType: 'json',
					success : function(d) {
						if (d.code == 0) {
							if(d.object.type==1){
								window.location = '${ctx}/matches/edit/?matchesId='+d.object.id;
							}else{
								window.location = '${ctx}/matchesCenue/cenueEdit/1?isFlag=1&matchesId='+d.object.id;
							}
						} else {
							alert(d.result);
						}
					}
				});
				
			}
			
			
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
						$form.find('input[name="video"]').val(d.url).change();
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
		
		function deteilEdit(detailType){
			var processNames='';
			var startTimeStrs='';
			var endTimeStrs='';
			var processIds='';
			$(".Process").each(function(i){
				processNames+=($(this).find('#processName').val()!=""?$(this).find('#processName').val():1)+",";
				startTimeStrs+=($(this).find('#startTimeStr').val()!=""?$(this).find('#startTimeStr').val():1)+",";
				endTimeStrs+=($(this).find('#endTimeStr').val()!=""?$(this).find('#endTimeStr').val():1)+",";
				processIds+=($(this).find('#processId').val()!=""?$(this).find('#processId').val():-1)+",";
			});
			
			$('#processNames').val(processNames);
			$('#startTimeStrs').val(startTimeStrs);
			$('#endTimeStrs').val(endTimeStrs);
			$('#processIds').val(processIds);
			$form.prop('action', '${ctx!}/matches/detailEdit?matchesId=${(matches.id)!}&type='+detailType).prop('target', '_self').submit();
			$form.submit();
		}
		</script>	
		
	</body>
</html>