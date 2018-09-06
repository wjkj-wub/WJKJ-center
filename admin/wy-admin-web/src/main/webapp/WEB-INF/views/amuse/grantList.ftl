<#import "/macros/pager.ftl" as p >
<html>
<head>
<link rel="stylesheet" href="${ctx}/static/lib/chosen/chosen.css" />
<script type="text/javascript" src="${ctx}/static/lib/chosen/chosen.min.js"></script>
<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>审核发放</strong>
 		</li>
		<li class="active">
			发放列表
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
		<div class="mb10">
			<div class="col-md-2" style="width:160px;">
				<input type="text" name="account" value="${(params.account)!}" class="form-control" placeholder="输入领奖者帐号">
			</div>
			<div class="col-md-2" style="width:160px;">
				<input type="text" name="beginDate" value="${(params.beginDate)!}" class="form-control" placeholder="提交时间(起)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:beginPick})">
			</div>
			<div class="col-md-2" style="width:160px;">
				<input type="text" name="endDate" value="${(params.endDate)!}" class="form-control" placeholder="提交时间(止)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:endPick})">
			</div>
			<button type="submit" class="btn btn-success">筛选</button>
			<a class="btn" href="1">清空</a>
		</div>
		<div class="mb10">
			<div class="col-md-8" style="margin-bottom: 10px;">
				<input type="hidden" name="state" value="${(params.state)!}" />
				<div class="btn-group" data-toggle="buttons-checkbox">
					<button state="3" type="button" class="btn <#if (params.state!) == "3">btn-danger<#else>btn-info</#if>">待发放</button>
					<button state="4" type="button" class="btn <#if (params.state!) == "4">btn-danger<#else>btn-info</#if>">完成</button>
					<button state="" type="button" class="btn <#if (params.state!) == "">btn-danger<#else>btn-info</#if>">全部</button>
				</div>
			</div>
		</div>
		<script type="text/javascript">
			$('button[state]').on('click', function(event) {
				var $form = $('#search');
				$form.find('input[name="state"]').val($(this).attr('state'));
				$form.submit();
			});
		</script>
		</form>
	</div>
	
	<!-- 批量审核 -->
	<#if Session.user.userType == 1 || Session.user.userType == 13>
	<div class="mb10">
		<div class="col-md-10">
			<div class="btn-group">
				<#if Session.user.userType == 13 && (params.state)?? && params.state == "3">
				<button type="button" class="btn btn-success dropdown-toggle" onclick="multiGrant()">批量发放</button>
				<#-- <#elseif Session.user.userType == 1 && (!(params.state)?? || params.state == "" || params.state == "3") && (!(params.isSpecial)?? || params.isSpecial == "")>
				<a class="btn btn-success" href="javascript:allot();">任务分配</a> -->
				</#if>
			</div>
		</div>
	</div>
	</#if>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<#if <#-- Session.user.userType == 1 || -->Session.user.userType == 13>
				<th>
					<input id="checkAll" type="checkbox" />
				</th>
			</#if>
			<th>订单编号</th>
			<th>奖励</th>
			<th>领奖者帐号</th>
			<th>网娱帐号</th>
			<th>对应金额(元)</th>
			<th>发放时间</th>
			<th>状态</th>
			<th>操作</th>
		</tr>
		<#if list??>
		<#list list as o>
			<tr>
				<#if <#-- Session.user.userType == 1 || -->Session.user.userType == 13>
					<td>
						<#if Session.user.userType == 13 && (o.state)?? && (o.claimUserId)?? && (o.state = 3 || o.state = 6) && o.claimUserId == Session.user.id>
							<input type="checkbox" name="id" value="${(o.id)!}" awardType="${(o.awardType)!}"/>
						<#elseif Session.user.userType == 1 && (o.state)?? && (o.state == 3 || o.state == 6)>
							<input type="checkbox" name="id" value="${(o.id)!}" />
						</#if>
					</td>
				</#if>
				<td>${o.serial!}</td>
				<td>
					<#if (o.awardType)??>
						<#if o.awardType == 1>
							<#if (o.awardSubType)??>
								<#if o.awardSubType == 1>
									<#if o.awardAmount??>${(o.awardAmount)!}元</#if>红包
								<#elseif o.awardSubType == 2>
									<#if o.awardAmount??>${(o.awardAmount)!}</#if>金币
								</#if>
							</#if> (自有商品)
						<#elseif o.awardType == 2>
							<#if o.awardSubType == 3>
								<#if o.awardAmount??>${(o.awardAmount)!}元</#if>话费
							<#elseif o.awardSubType == 4>
								<#if o.awardAmount??>${(o.awardAmount)!}M</#if>流量
							<#elseif o.awardSubType == 5>
								<#if o.awardAmount??>${(o.awardAmount)!}</#if>Q币
							</#if> (第三方充值)
						<#elseif o.awardType == 3>
							${(o.awardTypeName)!} (库存商品)
						</#if>
					</#if>
				</td>
				<td>
					<#if (o.awardType)??>
						<#if o.awardType == 1>
							${o.username!} (网娱帐号)
						<#elseif o.awardType == 2>
							<#if o.awardSubType == 3>
								${o.applyTel!} (手机号)
							<#elseif o.awardSubType == 4>
								${o.applyTel!} (手机号)
							<#elseif o.awardSubType == 5>
								${o.qq!} (QQ帐号)
							</#if>
						<#elseif o.awardType == 3>
							${o.applyTel!} (手机号)
						</#if>
					</#if>
				</td>
				<td>${o.username!}</td>
				<td>${(o.money)!}</td>
				<td>${(o.grantDate)!}</td>
				<td>
					<#if (o.state)??>
						<#if o.state == 0>
							未认领
						<#elseif o.state == 1>
							待审核
						<#elseif o.state == 2>
							拒绝
						<#elseif o.state == 3>
							待发放
						<#elseif o.state == 4>
							已发放
						<#elseif o.state == 5>
							结束
						<#elseif o.state == 6>
							审核通过
						</#if>
					</#if>
				</td>
				<td>
					<button handle="${o.id!}" loginUserId="${loginUserId!}" title="${o.title!}" startdate="${o.startdate!}"
						endDate="${o.endDate!}" reward="${o.reward!}" username="${o.username!}" remark="${o.remark!}"
						applyName="${o.applyName!}" applyTel="${o.applyTel!}" qq="${o.qq!}" idCard="${o.idCard!}" server="${o.server!}"
						gameAccount="${o.gameAccount!}" teamName="${o.teamName!}" type="button"
						<#if !o.claimUserId?? || (o.claimUserId!=loginUserId) || o.state == 4>
							class="btn" disabled="disabled"
						<#else>
							class="btn btn-info" onclick="grant('${(o.id)!}', '${(o.serial)!}', '${(o.awardType)!}', '${(o.awardSubType)!}', '${(o.awardAmount)!}')"
						</#if>>
						处理
					</button>
				</td>
			</tr>
		</#list>
		</#if>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	
	<!-- 处理中提示框 -->
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
	
	<#if Session.user.userType == 1>
	<!-- 手动分配发放 -->
	<div id="modal-allot" class="modal fade">
		<div class="modal-dialog" style="width:700px;">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title">分发</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post" enctype="multipart/form-data">
						<div class="form-group">
							<label class="col-md-2 control-label">分发账号</label>
							<div class="col-md-10">
								<select class="form-control" name="sysUserId">
								<#if grantUsers??>
								<#list grantUsers as u>
									<option value="${(u.id)!}">${(u.realname)!}</option>
								</#list>
								</#if>
								</select>
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button id="submit" type="button" class="btn btn-success" data-dismiss="modal">确认分发</button>
					<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
				</div>
			</div>
		</div>
	</div>
	</#if>
	
	<#-- 发放处理，详情 -->
	<div id="modal-editor" class="modal fade">
		<div class="modal-dialog" style="width:700px;">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideEditor()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h2 class="modal-title">比赛名称：</h2>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post" enctype="multipart/form-data">
						<input type="hidden" name="id" />
						<input type="hidden" name="loginUserId" />
						<input type="hidden" name="account" />
						<div class="form-group">
							<label class="col-md-2 control-label">比赛时间</label>
							<div class="col-md-10">
								<div class="input-group" style="width:360px;">
									<input type="text" name="startdate" class="form-control" disabled />
									<span class="input-group-btn">
										<button class="btn btn-default" type="button"> - </button>
									</span>
									<input type="text" name="endDate" class="form-control" disabled />
								</div>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">比赛奖励</label>
							<div class="col-md-10">
								<textarea name="reward" style="width:100%;height:100px;" readonly></textarea>
							</div>
						</div>
						<hr style="height:1px;border:none;border-top:1px solid #555555;" />
						<div class="form-group">
							<label class="col-md-2 control-label">领奖人ID</label>
							<div class="col-md-10">
								<input type="text" name="username" class="form-control" disabled />
							</div>
						</div>
						<hr style="height:1px;border:none;border-top:1px solid #555555;" />
						<div class="form-group" id="show_id_applyName">
							<label class="col-md-2 control-label">姓名</label>
							<div class="col-md-10">
								<input type="text" name="applyName" class="form-control" disabled />
							</div>
						</div>
						<div class="form-group" id="show_id_applyTel">
							<label class="col-md-2 control-label">联系电话</label>
							<div class="col-md-10">
								<input type="text" name="applyTel" class="form-control" disabled />
							</div>
						</div>
						<div class="form-group" id="show_id_qq">
							<label class="col-md-2 control-label">QQ号码</label>
							<div class="col-md-10">
								<input type="text" name="qq" class="form-control" disabled />
							</div>
						</div>
						<div class="form-group" id="show_id_idCard">
							<label class="col-md-2 control-label">身份证号</label>
							<div class="col-md-10">
								<input type="text" name="idCard" class="form-control" disabled />
							</div>
						</div>
						<div class="form-group" id="show_id_server">
							<label class="col-md-2 control-label">游戏区服</label>
							<div class="col-md-10">
								<input type="text" name="server" class="form-control" disabled />
							</div>
						</div>
						<div class="form-group" id="show_id_gameAccount">
							<label class="col-md-2 control-label">游戏账号</label>
							<div class="col-md-10">
								<input type="text" name="gameAccount" class="form-control" disabled />
							</div>
						</div>
						<div class="form-group" id="show_id_teamName">
							<label class="col-md-2 control-label">团队名称</label>
							<div class="col-md-10">
								<input type="text" name="teamName" class="form-control" disabled />
							</div>
						</div>
						<hr style="height:1px;border:none;border-top:1px solid #555555;" />
						<div class="form-group">
							<label class="col-md-2 control-label"> </label>
							<div class="col-md-10">
								<textarea name="remark" style="width:100%;height:100px;" placeholder="请输入备注信息..."></textarea>
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" onclick="hideEditor()">关闭</button>
					<button id="submit" type="button" class="btn btn-primary">确定</button>
				</div>
			</div>
		</div>
	</div>
	
	<#-- 库存发放 -->
	<div id="modal-repertory" class="modal fade">
		<div class="modal-dialog" style="width:700px;">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideEditor()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<div class="modal-title">发放</div>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post" enctype="multipart/form-data">
						<input type="hidden" name="id" />
						<div class="form-group">
							<label class="col-md-2 control-label">商品名</label>
							<div class="col-md-10">
								<select class="form-control" name="inventoryId">
									<option>test</option>
								</select>
							</div>
						</div>
						<!-- <div class="form-group">
							<label class="col-md-2 control-label">兑换码</label>
							<div class="col-md-10">
								<select class="form-control" name="cdkey">
									<option>test</option>
								</select>
							</div>
						</div> -->
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
					<button id="submit" type="button" class="btn btn-primary">确定</button>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
	// 初始化操作中提示框
	$('#modal-operating').modal({
		keyboard : false,
		show : false,
		backdrop : 'static'
	});
	
	// 监听列表复选按钮事件
	$('#checkAll').on('click', function(event) {
		$('input[type="checkbox"][name="id"]').prop('checked', $(this).prop('checked'));
	});
	$('input[type="checkbox"][name="id"]').on('click', function(event) {
		var cbs = $('input[type="checkbox"][name="id"]');
		
		var isAllCheck = true;
		cbs.each(function() {
			if(!$(this).prop('checked')) {
				isAllCheck = false;
				return;
			}
		});
		
		$('#checkAll').prop('checked', isAllCheck);
	});
	
	// 初始化tooltip
	$('button[data-toggle="tooltip"]').tooltip();
	
	// 初始化模态框
	var _$editor = $('#modal-editor');
	_$editor.modal({
		keyboard : false,
		show : false
	})
	
	// 关闭模态框
	function hideEditor() {
		_$editor.modal('hide');
	}
	
	<#-- // 显示模态框，发放处理信息
	$('button[handle]').on('click', function(event) {
		var id = $(this).attr('handle');
		var loginUserId = $(this).attr('loginUserId');
		var title = $(this).attr('title');
		var startdate = $(this).attr('startdate');
		var endDate = $(this).attr('endDate');
		var reward = $(this).attr('reward');
		var username = $(this).attr('username');
		var remark = $(this).attr('remark');
		var applyName = $(this).attr('applyName');
		var applyTel = $(this).attr('applyTel');
		var qq = $(this).attr('qq');
		var idCard = $(this).attr('idCard');
		var server = $(this).attr('server');
		var gameAccount = $(this).attr('gameAccount');
		var teamName = $(this).attr('teamName');
		$.fillForm({
			id: id,
			loginUserId: loginUserId,
			account: gameAccount,
			startdate: new Date(startdate).Format('yyyy-MM-dd'),
			endDate: new Date(endDate).Format('yyyy-MM-dd'),
			reward: reward,
			username: username,
			remark: remark,
			applyName: applyName,
			applyTel: applyTel,
			qq: qq,
			idCard: idCard,
			server: server,
			gameAccount: gameAccount,
			teamName: teamName,
		}, _$editor);
		if(applyName==''){
			$('#show_id_applyName').hide();
		}
		if(applyTel==''){
			$('#show_id_applyTel').hide();
		}
		if(qq==''){
			$('#show_id_qq').hide();
		}
		if(idCard==''){
			$('#show_id_idCard').hide();
		}
		if(server==''){
			$('#show_id_server').hide();
		}
		if(gameAccount==''){
			$('#show_id_gameAccount').hide();
		}
		if(teamName==''){
			$('#show_id_teamName').hide();
		}
		
		_$editor.find('.modal-title').html('比赛名称：' + title);
		_$editor.modal('show');
	}); -->
	
	// 发放
	function option(text, value) {
		return '<option value="' + value + '">' + text + '</option>';
	}
	function grant(id, serial, awardType, awardSubType, awardAmount) {
		if(awardType == 1) {// 自有
			var msg = '确认给订单' + serial + '发放' + awardAmount;
			if(awardSubType == 1) {// 红包
				$.confirm(msg + '元红包吗?', function() {
					$.api('${ctx}/amuse/grant/grant', 'id=' + id, function(d) {
						window.location.reload();
					});
				});
			} else if(awardSubType == 2) {// 金币
				$.confirm(msg + '金币吗?', function() {
					$.api('${ctx}/amuse/grant/grant', 'id=' + id, function(d) {
						window.location.reload();
					});
				});
			}
		} else if(awardType == 3) {// 库存
			var $repertory = $('#modal-repertory');
			// 通过 activityId 读取 商品列表 及 cdkey列表
			$.api('${ctx!}/award/inventory/getInventoryAndCdkey', {}, function(d) {
				var inventories = d.object.inventories;
				var cdkeys = d.object.cdkeys;
				
				var $inventoryId = $repertory.find('select[name="inventoryId"]');
				$inventoryId.html('');
				var $cdkey = $repertory.find('select[name="cdkey"]');
				$cdkey.html('');
				
				if(inventories) {
					for(var i=0; i<inventories.length; i++) {
						var inventory = inventories[i];
						$inventoryId.append(option(inventory.name, inventory.id));
					}
					
					if(cdkeys) {
						for(var i=0; i<cdkeys.length; i++) {
							var c = cdkeys[i];
							$cdkey.append(option(c.cdkey, c.cdkey));
						}
					}
				}
				
				if($inventoryId.find('option').length <= 0) {
					$inventoryId.append(option('暂无可用商品', ''));
				}
				if($cdkey.find('option').length <= 0) {
					$cdkey.append(option('暂无可用cdkey', ''));
				}
				
				$repertory.find('input[name="id"]').val('id=' + id);
				
				// 显示窗口
				$repertory.modal('show');
			}, false, {
				async: false
			});
		} else {// 其他
			$.confirm('确认发放吗?', function() {
				$.api('${ctx}/amuse/grant/grant', 'id=' + id, function(d) {
					window.location.reload();
				});
			});
		}
	}
	
	// 商品框 监听器
	$('#modal-repertory select[name="inventoryId"]').on('change', function(event) {
		var inventoryId = $(this).val();
		
		$.api('${ctx!}/award/commodity/queryUsefullyCdkeysByInventoryId', {'inventoryId': inventoryId}, function(d) {
			var cdkeys = d.object;
			if(cdkeys) {
				var $cdkey = $('#modal-repertory select[name="cdkey"]');
				$cdkey.html('');
				for(var i=0; i<cdkeys.length; i++) {
					var c = cdkeys[i];
					$cdkey.append(option(c.cdkey, c.cdkey));
				}
				if($cdkey.find('option').length <= 0) {
					$cdkey.append(option('暂无可用cdkey', ''));
				}
			}
		}, false, {
			async: false
		});
	});
	
	// 库存商品提交
	$('#modal-repertory #submit').on('click', function(event) {
		var $this = $(this);
		
		$.confirm('确认发放吗?', function(event) {
			var ids = $('#modal-repertory input[name="id"]').val();
			var inventoryId = $('#modal-repertory select[name="inventoryId"]').val();
			var param = ids + '&inventoryId=' + inventoryId;
			$.api('${ctx!}/amuse/grant/grant', param, function(d) {
				window.location.reload();
			}, false, {});	
		});
	});
	
	// 提交表单--发放
	_$editor.find('#submit').on('click', function(event) {
		var $form = _$editor.find('form');
		var valid = $form.formValid();
		if(valid) {
			var $this = $(this);
			$this.prop('disabled', true);
			
			$form.ajaxSubmit({
				url:'${ctx}/amuse/grant/handle',
			    type:'post',
			    success: function(d){
			    	if(d.code == 0) {
			    		window.location.reload();
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
	
	// 认领	
	$('button[claim]').on('click', function(event) {
		var _this = $(this);
		_this.attr('disabled', true);
		var id = $(this).attr('claim');
		var loginUserId = $(this).attr('loginUserId');
		$.api('${ctx}/amuse/grant/claim/' + id +'/'+loginUserId, {}, function(d) {
			window.location.reload();
		}, function(d) {
			alert("认领失败：" + d.result);
		}, {
			complete: function() {
				_this.attr('disabled', false);
			}
		});
	});
	
	// 将勾选审核ID组合成id参数
	function genIdsParam(params) {
		if(typeof(params) !== 'string') {
			params = '';
		}
		$('input[type="checkbox"][name="id"]:checked').each(function(event) {
			if(params.length > 0) {
				params += '&';
			}
			params += 'id=' + $(this).val();
		});
		return params;
	}
	
	<#if Session.user.userType == 13><#-- 审核员操作 -->
	// 批量审核
	function multiGrant() {
		var params = '';
		var awardType = -1;
		var continueGrant = true;
		$('input[type="checkbox"][name="id"]:checked').each(function(event) {
			// 拼接ID
			if(params.length > 0) {
				params += '&';
			}
			params += 'id=' + $(this).val();
			
			// 检查奖品类型
			var at = $(this).attr('awardType');
			if(awardType < 0) {
				awardType = at;
			} else {
				if(awardType != at && (awardType == 3 || at == 3)) {
					alert('库存商品请单独进行批量发放');
					continueGrant = false;
					return false;
				}
			}
		});
		
		if(continueGrant) {
			if(awardType == 3) {// 库存商品
				var $repertory = $('#modal-repertory');
				// 通过 activityId 读取 商品列表 及 cdkey列表
				$.api('${ctx!}/award/inventory/getInventoryAndCdkey', {}, function(d) {
					var inventories = d.object.inventories;
					
					var $inventoryId = $repertory.find('select[name="inventoryId"]');
					$inventoryId.html('');
					if(inventories) {
						for(var i=0; i<inventories.length; i++) {
							var inventory = inventories[i];
							$inventoryId.append(option(inventory.name, inventory.id));
						}
						
					}
					
					if($inventoryId.find('option').length <= 0) {
						$inventoryId.append(option('暂无可用商品', ''));
					}
					
					$repertory.find('input[name="id"]').val(params);
					
					// 显示窗口
					$repertory.modal('show');
				}, false, {
					async: false
				});
			} else {// 充值 或 自有商品
				$.confirm('确认操作吗?', function() {
					$('#modal-operating').modal('show');
					$.api('${ctx}/amuse/grant/grant', params, function(d) {
						window.location.reload();
					});
				});
				
			}
		}
	}
	<#elseif Session.user.userType == 1><#-- 管理员操作 -->
	function allot() {
		var params = genIdsParam();
		if(params.length <= 0) {
			alert('请选择要分配的审核项');
			return;
		}
		
		$('#modal-allot').modal('show');
	}
	
	$('#modal-allot #submit').on('click', function(event) {
		var sysUserId = $('#modal-allot select[name="sysUserId"]').val();
		if(typeof(sysUserId) === 'undefined' || sysUserId.length < 1) {
			alert('请选择要分发的帐号');
			return;
		}
		
		var oldParams = 'sysUserId=' + sysUserId;
		var params = genIdsParam(oldParams);
		if(params == oldParams) {
			alert('请选择要操作的审核项');
			return;
		}
		
		$('#modal-operating').modal('show');
		$.api('${ctx}/amuse/grant/allot', params, function(d) {
			window.location.reload();
		});
	});
	</#if>
	</script>
</body>
</html>