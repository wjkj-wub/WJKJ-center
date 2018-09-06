<#import "/macros/pager.ftl" as p >
<html>
<head>
<link rel="stylesheet" href="${ctx}/static/lib/chosen/chosen.css" />
<script type="text/javascript" src="${ctx}/static/lib/chosen/chosen.min.js"></script>
<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
<style type="text/css">
.form-condensed .form-control {
	margin-top: 3px;
}
input[disabled] {
    background-color: #FFFFFF!important;
    -webkit-box-shadow: inset 0 1px 1px rgba(0, 0, 0, .075);
    box-shadow: inset 0 1px 1px rgba(0, 0, 0, 0);
    border: 0;
}
.form-condensed .btn {
    padding: 5px 12px;
}
.imgs-container {
	max-height: 400px;
    overflow: auto;
    border-radius: 5px;
    border: solid 1px #CCC;
    padding: 10px;
}
.imgs-container img {
	width: 30%;
    margin: 0 10px 10px 0;
}
textarea {
	border-radius: 5px;
    border: solid 1px #CCC;
    padding: 10px;
}
</style>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>娱乐赛审核发放</strong>
 		</li>
		<li class="active">
			审核
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
		<div class="mb10">
			<div class="col-md-2">
				<input type="text" name="telephone" value="${(params.telephone)!}" class="form-control" placeholder="输入领奖者号码">
			</div>
			<#if Session.user.userType == 1>
				<div class="col-md-2">
					<select class="form-control" name="sysUserId">
						<option value="">全部帐号</option>
						<#if appealUsers??>
						<#list appealUsers as u>
							<option value="${(u.id)!}"<#if (params.sysUserId)?? && (u.id)?? && u.id?string == params.sysUserId> selected</#if>>${(u.realname)!}</option>
						</#list>
						</#if>
					</select>
				</div>
			</#if>
			<div class="col-md-2">
				<select class="form-control" name="activityId">
					<option value="">全部赛事</option>
					<#if activitys??>
					<#list activitys as a>
						<option value="${(a.id)!}"<#if (params.activityId)?? && (a.id)?? && a.id?string == params.activityId> selected</#if>>${(a.title)!}</option>
					</#list>
					</#if>
				</select>
			</div>
			<div class="col-md-2">
				<input type="text" name="beginDate" value="${(params.beginDate)!}" class="form-control" placeholder="提交时间(起)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:beginPick})">
			</div>
			<div class="col-md-2">
				<input type="text" name="endDate" value="${(params.endDate)!}" class="form-control" placeholder="提交时间(止)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:endPick})">
			</div>
			<button id="screen" type="button" class="btn btn-success">筛选</button>
			<a class="btn btn-info" href="1">清空</a>
			
			<button id="export-all" type="button" class="btn btn-success">导出</button>
		</div>
		<div class="mb10">
			<div class="col-md-8" style="margin-bottom: 10px;">
				<input type="hidden" name="state" value="${(params.state)!}" />
				<input type="hidden" name="isSpecial" value="${(params.special)!}" />
				<div class="btn-group" data-toggle="buttons-checkbox">
					<button state="0" type="button" class="btn <#if (params.state!) == "0">btn-danger<#else>btn-info</#if>">待重审</button>
					<button state="2" type="button" class="btn <#if (params.state!) == "2">btn-danger<#else>btn-info</#if>">申诉通过</button>
					<button state="1" type="button" class="btn <#if (params.state!) == "1">btn-danger<#else>btn-info</#if>">申诉驳回</button>
					<button state="3" type="button" class="btn <#if (params.state!) == "3">btn-danger<#else>btn-info</#if>">申诉处理</button>
					<button state="4" type="button" class="btn <#if (params.state!) == "4">btn-danger<#else>btn-info</#if>">结束</button>
					<button state="" type="button" class="btn <#if (params.state!) == "" && (params.isSpecial!) == "">btn-danger<#else>btn-info</#if>">全部</button>
				</div>
			</div>
		</div>
		<script type="text/javascript">
			$('button[state], button[special]').on('click', function(event) {
				var $form = $('#search');
				var state = $(this).attr('state');
				var $state = $form.find('input[name="state"]');
				var $special = $form.find('input[name="isSpecial"]');
				if(state) {
					$state.val(state);
					$special.val('');
				} else {
					$state.val('');
					$special.val($(this).attr('special'));
				}
				$form.submit();
			});
			
			// 查询
			var $form = $('#search');
			$('#screen').on('click', function() {
				$form.prop('action', '${ctx}/amuse/appeal/list/1').prop('target', '_self').submit();
			});
			// 导出当前页
			$('#export-all').on('click', function() {
				$form.prop('action', '${ctx}/amuse/appeal/export/0').prop('target', '_blank').submit();
			});
		</script>
		</form>
	</div>
	
	<!-- 批量审核 -->
	<#if Session.user.userType == 1 || Session.user.userType == 14>
	<div class="mb10">
		<div class="col-md-10">
			<div class="btn-group">
				<#if Session.user.userType == 1 && (!(params.state)?? || params.state == "" || params.state == "0")>
				<a class="btn btn-success" href="javascript:allot();">任务分配</a>
				<#-- <#elseif Session.user.userType == 14 && (params.state) == "0">
				<button id="batch-dropdown" type="button" class="btn btn-success dropdown-toggle" data-toggle="dropdown">
					批量操作 <span class="caret"></span> <span class="sr-only"></span>
				</button>
				<ul class="dropdown-menu" role="menu">
					<li><a href="javascript:multiAppeal(1);">申诉驳回</a></li>
					<li><a href="javascript:multiAppeal(2);">申诉通过</a></li>
					<li><a href="javascript:multiAppeal(3);">申诉处理</a></li>
				</ul> -->
				</#if>
			</div>
		</div>
	</div>
	</#if>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<#if Session.user.userType == 1>
				<th>
					<input id="checkAll" type="checkbox" />
				</th>
			</#if>
			<th>订单编号</th>
			<th>赛事名</th>
			<th>奖励</th>
			<th>领奖者账号</th>
			<th>领奖者昵称</th>
			<th>网娱账号</th>
			<th>状态</th>
			<th>备注</th>
			<th>操作</th>
		</tr>
		<#if list??>
		<#list list as o>
			<tr>
				<#if Session.user.userType == 1>
					<td>
						<#if Session.user.userType == 1 && (o.state)?? && o.state == 0 && !(o.updateUserId)??>
							<input type="checkbox" name="id" value="${(o.id)!}" />
						<#-- <#elseif Session.user.userType == 14 && (o.state)?? && (o.updateUserId)?? && o.state = 0 && o.updateUserId == Session.user.id>
							<input type="checkbox" name="id" value="${(o.id)!}" /> 因操作不一致暂时取消批量操作 -->
						</#if>
					</td>
				</#if>
				<td>${(o.serial)!}</td>
				<td>${(o.activityName)!}</td>
				<td>
					<#if (o.activityReward??) && o.activityReward?length gt 0>
						<button type="button" class="btn btn-default" data-toggle="tooltip" data-placement="top" data-original-title="${o.activityReward!}">
							<#if o.activityReward?length gt 5>
								${o.activityReward?substring(0,5)!}
							<#else>
								${o.activityReward!}
							</#if>
						</button>
					</#if>
				</td>
				<td>
					<#if (o.awardType)??>
						<#if o.awardType == 1>
							${o.username!} (网娱帐号)
						<#elseif o.awardType == 2>
							<#if o.awardSubType??>
								<#if o.awardSubType == 3>
									${o.recordTelephone!} (手机号)
								<#elseif o.awardSubType == 4>
									${o.recordTelephone!} (手机号)
								<#elseif o.awardSubType == 5>
									${o.qq!} (QQ帐号)
								</#if>
							</#if>
						<#elseif o.awardType == 3>
							${o.recordTelephone!} (手机号)
						</#if>
					</#if>
				</td>
				<td>${(o.gameAccount)!}</td>
				<td>${(o.username)!}</td>
				<td>
					<#if (o.state)??>
						<#if o.state == 0>
							待重审
						<#elseif o.state == 1>
							申诉驳回
						<#elseif o.state == 2>
							申诉通过
						<#elseif o.state == 3>
							申诉处理
						<#elseif o.state == 4>
							结束
						</#if>
					</#if>
				</td>
				<td>
					<#if (o.remark??) && o.remark?length gt 0>
						<button type="button" class="btn btn-default" data-toggle="tooltip" data-placement="top" data-original-title="${o.remark!}">
							<#if o.remark?length gt 5>
								${o.remark?substring(0,5)!}
							<#else>
								${o.remark!}
							</#if>
						</button>
					</#if>
				</td>
				<td>
					<#if (o.state)?? && (o.updateUserId)?? && o.state = 0 && o.updateUserId == Session.user.id>
						<button verify="${(o.id)!}" type="button" class="btn btn-info">处理</button>
					<#else>
						<button edit="${(o.id)!}" type="button" class="btn btn-success">查看</button>
					</#if>
				</td>
			</tr>
		</#list>
		</#if>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	
	<#-- 编辑，新增 -->
	<div id="modal-editor" class="modal fade">
		<div class="modal-dialog" style="width:700px;">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title">标题</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post" enctype="multipart/form-data">
						<input type="hidden" name="id" />
						<div class="form-group fail-appeal">
							<label class="col-md-2 control-label">人证时间</label>
							<div class="col-md-10">
								<input type="text" name="createDate" class="form-control" disabled />
							</div>
						</div>
						<div class="form-group fail-appeal">
							<label class="col-md-2 control-label">凭证内容</label>
						</div>
						<div class="form-group fail-appeal">
							<label class="col-md-1 control-label"></label>
							<div class="col-md-10">
								<textarea name="describes" style="width:100%;height:100px;" readonly></textarea>
							</div>
						</div>
						<div class="form-group fail-appeal">
							<label class="col-md-1 control-label"></label>
							<div class="col-md-10">
								<div id="imgs" class="imgs-container"></div>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">申诉时间</label>
							<div class="col-md-10">
								<input type="text" name="appealCreateDate" class="form-control" disabled />
							</div>
						</div>
						<div class="form-group suc-appeal">
							<label class="col-md-2 control-label">申诉原因</label>
						</div>
						<div class="form-group suc-appeal">
							<label class="col-md-1 control-label"></label>
							<div class="col-md-10">
								<textarea name="reason" style="width:100%;height:100px;" readonly></textarea>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">申诉内容</label>
						</div>
						<div class="form-group">
							<label class="col-md-1 control-label"></label>
							<div class="col-md-10">
								<textarea name="appealDescribes" style="width:100%;height:100px;" readonly></textarea>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-1 control-label"></label>
							<div class="col-md-10">
								<div id="appealImgs" class="imgs-container"></div>
							</div>
						</div>
						<div class="form-group fail-appeal">
							<label class="col-md-2 control-label">参赛人资料</label>
						</div>
						<div class="form-group fail-appeal">
							<label class="col-md-2 control-label">领奖者号码：</label>
							<div class="col-md-10">
								<input type="text" class="form-control" name="telephone" disabled />
							</div>
						</div>
						<div class="form-group fail-appeal">
							<label class="col-md-2 control-label">QQ号码：</label>
							<div class="col-md-10">
								<input type="text" class="form-control" name="qq" disabled />
							</div>
						</div>
						<div class="form-group fail-appeal">
							<label class="col-md-2 control-label">游戏账号：</label>
							<div class="col-md-10">
								<input type="text" class="form-control" name="gameAccount" disabled />
							</div>
						</div>
						<div class="form-group fail-appeal">
							<label class="col-md-2 control-label">比赛区服：</label>
							<div class="col-md-10">
								<input type="text" class="form-control" name="server" disabled />
							</div>
						</div>
						<div class="form-group fail-appeal">
							<label class="col-md-2 control-label">赛事资料</label>
						</div>
						<div class="form-group fail-appeal">
							<label class="col-md-2 control-label">赛事名：</label>
							<div class="col-md-10">
								<input type="text" class="form-control" name="activityName" disabled />
							</div>
						</div>
						<div class="form-group fail-appeal">
							<label class="col-md-2 control-label">比赛时间：</label>
							<div class="col-md-10">
								<input type="text" class="form-control" name="startDate" disabled />
							</div>
						</div>
						<div class="form-group fail-appeal">
							<label class="col-md-2 control-label">比赛奖励：</label>
							<div class="col-md-10">
								<input type="text" class="form-control" name="activityReward" disabled />
							</div>
						</div>
						<div  id="div-operate">
							<div class="form-group">
								<label class="col-md-2 control-label">备注</label>
							</div>
							<div class="form-group">
								<label class="col-md-1 control-label"></label>
								<div class="col-md-10">
									<select class="form-control" name="remark">
									<#if feedbacks??>
									<#list feedbacks as f>
										<option>${(f.content)!}</option>
									</#list>
									</#if>
									<option value="-1">添加反馈文案(50字以内)</option>
									</select>
								</div>
							</div>
							<div class="form-group" id="cusRemark">
								<label class="col-md-1 control-label"></label>
								<div class="col-md-10">
									<textarea class="form-control" placeholder="添加反馈文案" maxlength="50"></textarea> 
								</div>
							</div>
							
							<div class="form-group suc-appeal">
								<label class="col-md-5 control-label"></label>
								<div class="col-md-6">
									<button appeal="3" class="btn btn-success">申诉处理</button>
								</div>
							</div>
							
							<div class="form-group fail-appeal">
								<label class="col-md-3 control-label"></label>
								<div class="col-md-3">
									<button appeal="1" class="btn btn-success">申诉通过</button>
								</div>
								<div class="col-md-5">
									<button appeal="2" class="btn btn-danger">申诉驳回</button>
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
	<!-- 手动分配审核 -->
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
								<#if appealUsers??>
								<#list appealUsers as u>
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
	<script type="text/javascript">
	// 初始化操作中提示框
	$('#modal-operating').modal({
		keyboard : false,
		show : false,
		backdrop : 'static'
	});
	
	// 审核列表全选按钮
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
	
	// 打开模态框
	function showEditor(title) {
		_$editor.find('.modal-title').html(title);
		_$editor.modal('show');
	}
	
	// 关闭模态框
	function hideEditor() {
		_$editor.modal('hide');
	}
	
	function showDeal(id) {
		$.api('${ctx}/amuse/appeal/detail', {id: id}, function(d) {
			var o = d.object;
			var createDateStr = null;
			if(o.createDate) {
				createDateStr = new Date(o.createDate).Format('yyyy-MM-dd hh:mm:ss');
			}
			var appealCreateDateStr = null;
			if(o.appealCreateDate) {
				appealCreateDateStr = new Date(o.appealCreateDate).Format('yyyy-MM-dd hh:mm:ss');
			}
			var startDateStr = null;
			if(o.startDate) {
				startDateStr = new Date(o.startDate).Format('yyyy-MM-dd hh:mm:ss');
			}
			$.fillForm({
				id: o.id,
				createDate: createDateStr,
				describes: o.describes,
				telephone: o.telephone,
				qq: o.qq,
				gameAccount: o.gameAccount,
				server: o.server,
				activityName: o.activityName,
				startDate: startDateStr,
				activityReward: o.activityReward,
				appealCreateDate: appealCreateDateStr,
				appealDescribes: o.appealDescribes,
				appealDescribes: o.remark,
				reason: o.reason,
			}, _$editor);
			
			// 显示图片
			function img(src) {
				return '<a href="${imgServer!}' + src + '" target="_blank"><img src="${imgServer!}/' + src + '" /></a>';
			}
			var imgs = o.imgs;
			var $imgs = _$editor.find('#imgs');
			$imgs.html('');
			if(imgs && imgs.length > 0) {
				for(var i=0; i<imgs.length; i++) {
					var imgObj = imgs[i];
					$imgs.append(img(imgObj.img));
				}
			} else {
				$imgs.append('未上传图片');
			}
			
			var appealImgs = o.appealImgs;
			var $appealImgs = _$editor.find('#appealImgs');
			$appealImgs.html('');
			if(appealImgs && appealImgs.length > 0) {
				for(var i=0; i<appealImgs.length; i++) {
					var imgObj = appealImgs[i];
					$appealImgs.append(img(imgObj.img));
				}
			} else {
				$appealImgs.append('未上传图片');
			}
			
			if(o.state == 2 || o.state == 6) {
				_$editor.find('.fail-appeal').show();
				_$editor.find('.suc-appeal').hide();
			} else {
				_$editor.find('.fail-appeal').hide();
				_$editor.find('.suc-appeal').show();
			}
			
			$('select[name="remark"]').change();
			showEditor('比赛名称：' + o.activityName);
		}, function(d) {
			alert('请求数据异常：' + d.result);
		}, {
			async: false,
		});
	}
	
	// 显示模态框，编辑
	$('button[edit]').on('click', function(event) {
		_$editor.find('#div-operate').hide();
		_$editor.find('#submit').hide();
		// 初始化表单数据
		var id = $(this).attr('edit');
		
		showDeal(id);
	});
	
	// 处理
	$('button[verify]').on('click', function(event) {
		var id = $(this).attr('verify');
		showDeal(id);
		_$editor.find('#div-operate').show();
		_$editor.find('#submit').show();
	});
	
	// 监听备注下拉框
	$('select[name="remark"]').on('change', function(event) {
		var val = $(this).val();
		if(val == '-1') {
			_$editor.find('#cusRemark').show();
		} else {
			_$editor.find('#cusRemark').hide().find('textarea').val('');
		}
	});
	
	// 提交表单
	_$editor.find('[appeal]').on('click', function(event) {
		var $this = $(this);
		var operate = $this.attr('appeal');
		var remark = _$editor.find('[name="remark"]').val();
		if(remark == '-1') {
			remark = _$editor.find('#cusRemark textarea').val();
		}
		
		var $form = _$editor.find('form');
		var id = $form.find('input[name="id"]').val();
		$this.prop('disabled', true);
		
		$.api('${ctx}/amuse/appeal/operate', {id: id, operate: operate, remark: remark}, function(d) {
			window.location.reload();
		}, false, {
			complete: function() {
				$this.prop('disabled', false);
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
	function multiAppeal(operate) {
		var oldParams = 'operate=' + operate;
		var params = genIdsParam(oldParams);
		if(params == oldParams) {
			alert('请选择要操作的审核项');
			return;
		}
		
		$.confirm('确认操作吗?', function() {
			$('#modal-operating').modal('show');
			$.api('${ctx}/amuse/appeal/operate', params, function(d) {
				window.location.reload();
			}, false, {
				complete: function() {
					$this.prop('disabled', false);
				}
			});
		});
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
	
	// 手动分配申诉
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
		$.api('${ctx}/amuse/appeal/allot', params, function(d) {
			window.location.reload();
		}, false, {
			complete: function() {
				$this.prop('disabled', false);
			}
		});
	});
	</#if>
	</script>
</body>
</html>