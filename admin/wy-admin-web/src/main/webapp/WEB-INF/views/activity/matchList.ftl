<#import "/macros/pager.ftl" as p >
<html>
<head>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>管理系统</strong>
 		</li>
		<li class="active">
			约战管理
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
			<div class="col-md-2">
				<input type="text" class="form-control" name="username" placeholder="发起人号码" value="${(params.username)!}" />
			</div>
			<div class="col-md-2">
				<input type="text" class="form-control" name="title" placeholder="标题" value="${(params.title)!}" />
			</div>
			<div class="col-md-2">
				<select class="form-control" name="itemId">
					<option value="">全部游戏</option>
					<#if items?? && items?size gt 0>
						<#list items as i>
							<option value="${i.id!}"<#if (params.itemId!) == i.id?string> selected</#if>>${i.name!}</option>
						</#list>
					</#if>
				</select>
			</div>
			<div class="col-md-2">
				<input class="form-control" type="text" name="startDateMin" placeholder="最早开始时间" value="${(params.startDateMin)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss'})">
			</div>
			<div class="col-md-2">
				<input class="form-control" type="text" name="startDateMax" placeholder="最晚开始时间" value="${(params.startDateMax)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss'})">
			</div>
			<div class="col-md-2">
				<select class="form-control" name="way">
					<option value="">全部方式</option>
					<option value="1"<#if (params.way!) == "1"> selected</#if>>线上</option>
					<option value="2"<#if (params.way!) == "2"> selected</#if>>线下</option>
				</select>
			</div>
			<#if ((Session.user.userType)!0) != 10>
				<div class="col-md-2">
					<select class="form-control" name="areaCode">
						<option value="">全部地区</option>
						<#if provinces?? && provinces?size gt 0>
							<#list provinces as p>
								<option value="${p.areaCode!}"<#if (params.areaCode!) == p.areaCode?string> selected</#if>>${p.name!}</option>
							</#list>
						</#if>
					</select>
				</div>
			</#if>
			<button type="submit" class="btn btn-success">查询</button>
			<a class="btn btn-success" href="1">清空</a>
		</form>
	</div>
	<div class="mb10">
		<button add="" type="button" class="btn btn-info">新增约战</button>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>发起人号码</th>
			<th>标题</th>
			<th>竞技项目</th>
			<th>服务器</th>
			<th>人数</th>
			<th>胜负规则</th>
			<th>方式</th>
			<th>地点</th>
			<th>联系方式</th>
			<th>发布时间</th>
			<th>开始时间</th>
			<th>约战类型</th>
			<th>操作</th>
		</tr>
		<#list list as o>
			<tr>
				<td>${o.username!}</td>
				<td>${o.title!}</td>
				<td>
					<form id="${o.id!}" style="margin:0px;display: inline" action="/activityIteam/list/1" method="post">
						<input type="hidden" name="name" value="${o.itemName!}"/>
						<button type="submit" class="btn btn-default">${o.itemName!}</button>
					</form>
				</td>
				<td>${o.server!}</td>
				<td><a href="${ctx!}/activityMatch/members/1?matchId=${(o.id)!}">${(o.applyCount)!0}/${o.peopleNum!0}</a></td>
				<td>
					<#if (o.rule??) && o.rule?length gt 0>
						<button type="button" class="btn btn-default" data-toggle="tooltip" data-placement="top" data-original-title="${o.rule!}">
							<#if o.rule?length gt 5>
								${o.rule?substring(0,5)!}
							<#else>
								${o.rule!}
							</#if>
						</button>
					</#if>
				</td>
				<#if 1 == ((o.way?number)!1)>
				<td>线上</td>
				<#else>
				<td>线下</td>
				</#if>
				<td>
					<#if (o.way!1) == 1>
						${o.address!}
					<#else>
						${o.netbarName!}
					</#if>
				</td>
				<td><#if (o.remark)??>${o.remark?replace(' ', '<br/>')!}</#if></td>
				<td>${o.createDate!}</td>
				<td>${o.beginTime!}</td>
				<td>
					<#if (o.byMerchant)?? && o.byMerchant == 0>
						个人
					<#else>
						官方
					</#if>
				</td>
				<td>
					<button edit="${o.id!}" title="${o.title!}" itemId="${o.itemId!}" requiredServer="${o.serverRequired!}" peopleNum="${o.peopleNum!}" rule="${o.rule!}" way="${o.way!}" address="${o.address!}" remark="${o.remark!}" beginTime="${o.beginTime!}" type="button" class="btn btn-info">编辑</button>
					<button remove="${o.id!}" type="button" class="btn btn-danger">删除</button>
					<button reply="${o.id!}" type="button" class="btn btn-warning">添加评论</button>
					<!-- <form id="${o.id!}" style="margin:0px;display: inline" action="/activityInfo/apply/exportExcel" method="post">
						<input type="hidden" name="matchId" value="${o.id!}"/>
						<button type="submit" class="btn btn-success">导出报名信息</button>
					</form> -->
				</td>
			</tr>
		</#list>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	
	<#-- 回复 -->
	<div id="modal-reply" class="modal fade">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideReplyEditor()">
						<span>×</span>
					</button>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" id="modal-reply-form" role="form" method="post">
						<input type="hidden" name="infoId" id="infoId" />
						<input type="hidden" name="infoType" id="infoType" />
						<div class="form-group">
							<label class="col-md-2 control-label">评论人</label>
							<div class="col-md-4">
								<input  type="hidden" name="userId" />
								<input class="form-control" type="text" name="userNickname" value=""  placeholder="请输入评论人完整昵称或点击随机"/>
							</div>
							<span><button type="button" onclick="generateUserInfo()" >随机</button></span>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">评论</label>
							<div class="col-md-10">
								<textarea class="form-control comment-content" id="reply-detail" name="commentContent" style="height:100px;" maxlength="200"></textarea>
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-primary" onclick="submitReply()">保存</button>
				</div>
			</div>
		</div>
	</div>
	
	
	<#-- 编辑 -->
	<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
	<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
	<div id="modal-editor" class="modal fade">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideEditor()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title">编辑约战</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post">
						<input type="hidden" name="idString" />
						<div class="form-group">
							<label class="col-md-2 control-label">标题</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="title" value="">
							</div>
						</div>
						<div id="wayselect_id" class="form-group" style="">
							<label class="col-md-2 control-label">竞技项目</label>
							<div class="col-md-10">
								<select id="itemId_select_id" name="itemIdString" class="selector_itemId form-control">
									<option value="">--请选择--</option>
									<#list itemList as item>
										<option requiredShow="${item.serverRequired!}" value="${item.id!}">${item.name!}</option>
									</#list>
								</select>
							</div>
						</div>
						<div id="wayselect_id" class="form-group">
							<label class="col-md-2 control-label">方式</label>
							<div class="col-md-10">
								<select id="select_id" name="wayString" class="selector_way form-control">
									<option value="">--请选择--</option>
									<option value="1">线上</option>
									<option value="2">线下</option>
								</select>
							</div>
						</div>
						<div class="form-group" id="div-address">
							<label class="col-md-2 control-label">地点</label>
							<div class="col-md-10">
								<div id="help-netbar-name" class="input-group">
									<input class="form-control" type="text" placeholder="请输入要搜索的网吧名" />
									<span class="input-group-btn">
										<button class="btn btn-default" type="button"><i class="icon icon-search"></i></button>
									</span>
								</div>
								<select name="netbarId" class="form-control">
									<option value="">--请选择--</option>
								</select>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">人数限定</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="peopleNumString" value="">
							</div>
						</div>
						<div id="remark_text_id" class="form-group">
							<label class="col-md-2 control-label">联系方式</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="remark" value="" onfocus="changeWay()">
							</div>
						</div>
						<div id="remark_input_id" class="form-group" style="display:none;">
							<label class="col-md-2 control-label">联系方式</label>
							<div class="col-md-10">	
								<input class="form-control" type="text" name="remarkYY" placeholder="YY房号" value="">
								<input class="form-control" type="text" name="remarkWX" placeholder="微信号" value="">
								<input class="form-control" type="text" name="remarkQQ" placeholder="QQ" value="">
								<span style="color:red">至少填写一种联系方式</span>
							</div>
						</div>
						<div id="server_div_id" class="form-group" style="display:none">
							<label class="col-md-2 control-label">服务器</label>
							<div class="col-md-10">
								<select id="server_select_id" name="serverString" class="selector_server form-control">
									<option value="">--请选择--</option>
								</select>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">胜负规则</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="rule" value="">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">开始时间</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="beginTimeParam" value="" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss'})">
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" onclick="hideEditor()">关闭</button>
					<button type="button" class="btn btn-primary" onclick="submitEditor()">保存</button>
				</div>
			</div>
		</div>
	</div>
	
	<script type="text/javascript">
	$('#modal-editor form [name="wayString"]').on('change', function(event) {
		var $netbarInfo = $('#div-address');
		if($(this).val() == 1) {
			$netbarInfo.hide();
		} else {
			$netbarInfo.show();
		}
	});
	
	$('#modal-editor form #help-netbar-name button').on('click', function() {
		var $this = $(this);
		
		var netbarName = $.trim($('#modal-editor form #help-netbar-name input').val());
		if(typeof(netbarName) == 'undefined' || netbarName.length <= 0) {
			alert('请输入网吧名');
			return;
		}
		
		$this.prop('disabled', true);
		var $netbarId = $('#modal-editor form [name="netbarId"]');
		$netbarId.html('');
		$.api('${ctx}/netbar/queryByName', {name: netbarName}, function(d) {
			var netbars = d.object;
			for(var i=0; i<netbars.length; i++) {
				var n = netbars[i];
				$netbarId.append('<option value="' + n.id + '">' + n.name + '(' + n.address + ')</option>');
			}
		}, false, {
			complete: function() {
				$this.prop('disabled', false);
			}
		});
		
	});
	
	// 初始化tooltip
	$('button[data-toggle="tooltip"]').tooltip();
	
	// 初始化模态框
	var _$editor = $('#modal-editor');
	var _$addtition = $('#modal-addtition');
	_$editor.modal({
		keyboard : false,
		show : false
	})
	
	// 关闭模态框
	function hideEditor() {
		_$editor.modal('hide');
		_$addtition.modal('hide');
	}
	
	// 初始化表单数据
	function fillForm(columns) {
		for(var k in columns) {
			_$editor.find('input[name="' + k + '"]').val(columns[k]);
		}
	}
	
	// 显示模态框，编辑
	$('button[add]').on('click', function(event) {
		$('#itemId_select_id').change();
    	
		$.fillForm({
			'wayString': 1,
		}, _$editor);
		
		$('#modal-editor form [name="wayString"]').change();
		$('#modal-editor form [name="netbarId"]').html('');
		_$editor.modal('show');
	});
	
	// 显示模态框，编辑
	$('button[edit]').on('click', function(event) {
		var id = $(this).attr('edit');
		$.api('${ctx}/activityMatch/detail/' + id, {}, function(d) {
			var match = d.object;
			var $netbarId = $('#modal-editor form [name="netbarId"]');
			if(match.way == 2) {
				$netbarId.html('<option value="' + match.netbarId + '">' + match.netbarName + '(' + match.netbarAddress + ')' + '</option>');
			} else {
				$netbarId.html('');
			}
			
			$('#remark_input_id').hide();
			$('#remark_text_id').show();
			
			<!--根据值选中下拉框-->
			$(".selector_itemId").val(match.itemId);
			$(".selector_way").val(match.way);
			<!--split分割字符串-->
			var remark = match.remark;
			if(typeof(remark) != 'undefined' && remark != null && remark.length > 0) {
				var temp = match.remark.split(" ");
		    	var yy;
		    	var wx;
		    	var qq;
		    	for(var i=0;i<temp.length;i++){
		    		if(temp[i].split("YY").length > 1){
			    		yy = temp[i].split("YY")[1].split(":")[1];
		    		}
		    		if(temp[i].split("\u5fae\u4fe1").length > 1){
			    		wx = temp[i].split("\u5fae\u4fe1")[1].split(":")[1];
		    		}
		    		if(temp[i].split("QQ").length > 1){
			    		qq = temp[i].split("QQ")[1].split(":")[1];
		    		}
		    	}
			}
	    	
			$('#itemId_select_id').change();
	    	
			$.fillForm({
				'title': match.title,
				'peopleNumString': match.peopleNum,
				'rule': match.rule,
				'wayString': match.way,
				'remark': match.remark,
				'remarkYY': yy,
				'remarkWX': wx,
				'remarkQQ': qq,
				'beginTimeParam': new Date(match.beginTime).Format('yyyy-MM-dd hh:mm:ss'),
				'address': match.address,
				'itemIdString': match.itemId,
				'serverString': match.server,
				'idString': match.id,
			}, _$editor);
			
			$('#modal-editor form [name="wayString"]').change();
			_$editor.modal('show');
		});
	});
	
	// 竞技项目下拉框变化，ajax查相应服务器信息
	var editItemId = false;
	$('#itemId_select_id').bind('change', function() {
		var itemId = $("#itemId_select_id").val();
		if(itemId == editItemId) {
			return;
		}
		
		editItemId = itemId;
		var required = $("#itemId_select_id").find("option:selected").attr("requiredShow");
		if(1 == required){
			$('#server_div_id').show();
			$.api('${ctx}/activityMatch/server/' + itemId, {}, function(d) {
				var list = d.object;
				$("#server_select_id").html('');
				for (var i=0; i<list.length; i++) {
					var serverId = list[i].serverId;
					var serverName = list[i].serverName;
				    var serverAreaName = list[i].serverAreaName;
					<!--为Select追加Option下拉项-->
					$("#server_select_id").append("<option value='"+serverName+"【"+serverAreaName+"】"+"'>"+serverName+"【"+serverAreaName+"】"+"</option>");
				}
			}, function(d) {
				alert('请求数据异常：' + d.result);
			}, {
				async: false,
			});
		} else {
			$('#server_div_id').hide();
		}
	});
	
	// 方式输入转下拉框
	function changeWay(){
		$('#remark_text_id').hide();
		$('#remark_input_id').show();
	}
	
	// 提交表单
	function submitEditor() {
		_$editor.find('form').ajaxSubmit({
			url:'${ctx}/activityMatch/save',
		    type:'post',
		    success: function(d){
		    	if(d.code == 0) {
		    		window.location.reload();
		    	} else {
		    		alert(d.result);
		    	}
			}
		});
	}
	
	// 删除
	$('button[remove]').on('click', function(event) {
		var _this = $(this);
		_this.attr('disabled', true);
		
		var id = $(this).attr('remove');
		
		$.confirm('确认删除吗?', function() {
			$.api('${ctx}/activityMatch/delete/' + id, {}, function(d) {
				window.location.reload();
			}, function(d) {
				alert('删除失败：' + d.result);
			}, {
				complete: function() {
					_this.attr('disabled', false);
				}
			});
		}, undefined, {
			complete: function() {
				_this.attr('disabled', false);
			}
		});
	});
	//---------------------------------回复功能---------------------
	var _$replyEditor = $('#modal-reply');
	function showReplyEditor(id) {
		$('#infoId').val(id);
		_$replyEditor.modal('show');
	}
	
	//隐藏编辑框
	function hideReplyEditor() {
		_$replyEditor.modal('hide');
	}
	// 提交表单
		function submitReply() {
			var $form = $('#modal-reply-form')
			var valid = $form.formValid();
			if(valid) {
				$form.ajaxSubmit({
					url:'${ctx}/comment/replyactivity',
				    type:'post',
				    success: function(d){
				    	if(d == 0) {
				    		window.location.reload();
				    	} else if(d==-1) {
				    		alert("待评论信息不存在");
				    	} else if(d==-2) {
				    		alert("评论内容不能为空");
				    	} else if(d==-3) {
				    		alert("请输入昵称或点击随机");
				    	} else if(d==-4) {
				    		alert("您输入的昵称账号不存在");
				    	}
					}
				});
			}
		}
		
		
		//-------------------------------------按钮操作----------------------------------
		// 回复
		$('button[reply]').on('click', function() {
			var id = $(this).attr('reply');
			showReplyEditor(id);
		});
		
		//-----------------------随机获取用户信息-------------------------
		// 提交表单
		function generateUserInfo() {
			var $form = $('#modal-reply-form')
			var valid = $form.formValid();
			var content = $('#reply-detail').val();
			var infoId = $('#infoId').val();
			var infoType = $('#infoType').val();
			if(valid) {
				$form.ajaxSubmit({
					url:'${ctx}/comment/randomReplyUser',
				    type:'post',
				    success: function(d){
				    	$.fillForm({
							userId: d.userId,
							userNickname: d.userNickname,
							infoType:infoType,
							commentContent:content,
							infoId:infoId
						}, _$replyEditor);
					}
				});
			}
		}
	
	</script>
</body>
</html>