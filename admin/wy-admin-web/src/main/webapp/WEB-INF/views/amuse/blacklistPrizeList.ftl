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
  			<strong>娱乐赛审核发放</strong>
 		</li>
		<li class="active">
			黑名单用户领奖帐号
		</li>
	</ul>
	
	<div class="mb10">
		<div class="col-md-10">
			<a class="btn btn-info" href="/amuse/blackList/list/1">返回黑名单列表</a>
		</div>
		<div class="col-md-10">
			游戏账号 ${(userinfo.username)!}
		</div>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>领奖账号</th>
			<th>操作时间</th>
			<th>数量</th>
		</tr>
		<#if list??>
			<#list list as o>
				<tr>
					<td>
						
						<#if (o.awardType)??>
						<#if o.awardType == 1>
							${(o.username)!}
						<#elseif o.awardType == 2>
							<#if o.awardSubType == 3>
								${(o.telephone)!}
							<#elseif o.awardSubType == 4>
								${(o.telephone)!}
							<#elseif o.awardSubType == 5>
								${(o.qq)!}
							</#if>
						<#elseif o.awardType == 3>
							${(o.telephone)!}
						</#if>
					</#if>
					</td>
					<td>${(o.createDate?string('yyyy-MM-dd HH:mm:ss'))!}</td>
					<td>${(o.awardAmount)!}</td>
				</tr>
			</#list>
			<#if userPrizeSum??>
				<tr>
					<td colspan="2">总计</td>
					<td>${(userPrizeSum)!}</td>
				</tr>
			</#if>
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
						<div class="form-group">
							<label class="col-md-2 control-label">提交凭证时间</label>
							<div class="col-md-10">
								<input type="text" name="createDate" class="form-control" disabled />
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button id="submit" type="button" class="btn btn-success" data-dismiss="modal">确认审核</button>
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
								<#if verifyUsers??>
								<#list verifyUsers as u>
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
	})
	
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
		$.api('${ctx}/amuse/verify/detail', {id: id}, function(d) {
			var o = d.object;
			var startDateStr = '';
			if(o.startDate) {
				startDateStr = new Date(o.startDate).Format('yyyy-MM-dd hh:mm:ss');
			}
			var createDateStr = '';
			if(o.createDate) {
				createDateStr = new Date(o.createDate).Format('yyyy-MM-dd hh:mm:ss');
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
			
			$('select[name="operate"]').change();
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
	// 监听操作下拉框
	$('select[name="operate"]').on('change', function(event) {
		var val = $(this).val();
		var $remark = $('#area-remark');
		if(val == '1') {
			$remark.hide();
		} else {
			$remark.show();
		}
	});
	
	// 提交表单
	_$editor.find('#submit').on('click', function(event) {
		var operate =_$editor.find('[name="operate"]').val();
		var remark = _$editor.find('[name="remark"]').val();
		if(remark == '-1') {
			remark = _$editor.find('#cusRemark textarea').val();
		}
		
		var $form = _$editor.find('form');
		var id = $form.find('input[name="id"]').val();
		var $this = $(this);
		$this.prop('disabled', true);
		
		$.api('${ctx}/amuse/verify/operate', {id: id, operate: operate, remark: remark}, function(d) {
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
	
	<#if Session.user.userType == 12><#-- 审核员操作 -->
	// 批量审核
	function multiVerify(operate) {
		var oldParams = 'operate=' + operate;
		var params = genIdsParam(oldParams);
		if(params == oldParams) {
			alert('请选择要操作的审核项');
			return;
		}
		
		$.confirm('确认操作吗?', function() {
			$('#modal-operating').modal('show');
			$.api('${ctx}/amuse/verify/operate', params, function(d) {
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
		$.api('${ctx}/amuse/verify/allot', params, function(d) {
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