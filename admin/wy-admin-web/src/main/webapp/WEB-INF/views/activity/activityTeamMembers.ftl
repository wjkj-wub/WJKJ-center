<#import "/macros/pager.ftl" as p >
<html>
<head>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/ztree/js/jquery.ztree.all-3.5.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/ztree/js/jquery.ztree.exhide-3.5.min.js"></script>
<link rel="stylesheet" type="text/css" href="${ctx!}/static/plugin/ztree/css/zTreeStyle.css"/>
<style type="text/css">
	#select-tree, #select-search-tree {
		max-height: 600px;
		overflow: auto;
		position: absolute;
		background-color: #FFF;
		border-radius: 5px;
		border: solid 1px #CCC;
		min-width: 250px;
		top: 40px;
		display: none;
		z-index: 1;
	}
	#btn-select-tree {
		width: 100%;
		overflow: hidden;
	}
	.mb10 {
		display: inline-block;
	}
	.gray {
		background-color: #CCC;
	}
</style>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>管理系统</strong>
 		</li>
		<li class="active">
			战队报名
		</li>
	</ul>
	
	<#-- 搜索 -->
	<form id="search" action="1" method="post">
		<div class="mb10">
			<input type="hidden" name="activityId" value="${(params.activity_id)!}" />
			<input type="hidden" name="netbarId" value="${(params.netbar_id)!}" />
			<input type="hidden" name="round" value="${(params.round)!}" />
			<div class="col-md-2">
				<input class="form-control" name="telephone" placeholder="联系号码" value="${(params.telephone)!}" />
			</div>
			<div class="col-md-1">
				<input class="form-control" name="name" placeholder="参赛人" value="${(params.name)!}" />
			</div>
			<div class="col-md-3">
				<div class="input-group">
					<input type="text" class="form-control" placeholder="最早报名时间" name="startDate" value="${(params.startDate)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd'})"> 
					<span class="input-group-btn fix-border">
						<button class="btn btn-default" type="button">-</button>
					</span>
					<input type="text" class="form-control" placeholder="最晚报名时间" name="endDate" value="${(params.endDate)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd'})">
				</div>
			</div>
			<div class="col-md-1">
				<button id="query" class="btn btn-success">查询</button>
				<#-- <button id="export-page" class="btn btn-success">导出</button> -->
			</div>
		</div>
		<div class="mb10">
			<#-- 共${(teams?size)!}个战队 <br/> -->
			<a class="btn btn-success" href="${ctx}/activityInfo/list/1">返回赛事列表</a>
			<button id="export-all" class="btn btn-success">导出</button>
			<button id="new-record" type="button" class="btn btn-success">新增报名</button>
		</div>
		<div class="pull-right">
			<button id="rand-groups" type="button" class="btn btn-success"><i class="icon icon-random"></i>&nbsp;随机排列对战选手顺序</button>
			<button id="get-groups" type="button" class="btn btn-success ">生成/查看分组</button>
			<button id="reset-groups" type="button" class="btn btn-success ">重置分组数据</button>
		</div>
	</form>
	
	<script type="text/javascript">
		// 导出当前页
		$('#export-page').on('click', function() {
			var $form = $('#search');
			$form.prop('action', 'export/0' + "&activityId="+$('input[name="activityId"]').val()+"&netbarId="+$('input[name="netbarId"]').val()+"&round="+$('input[name="round"]').val()).prop('target', '_blank').submit();
		});
		// 导出全部
		$('#export-all').on('click', function() {
			window.open('export/0?actId=${(params.activity_id)!}' + "&activityId="+$('input[name="activityId"]').val()+"&netbarId="+$('input[name="netbarId"]').val()+"&round="+$('input[name="round"]').val());
		});
		// 查询
		$('#query').on('click', function() {
			var $form = $('#search');
			$form.prop('action', '1').prop('target', '_self').submit();
		});
		$('#get-groups').on('click', function() {
			window.location.href="${ctx!}/activityGroup/list/2?activityId="+$('input[name="activityId"]').val()+"&netbarId="+$('input[name="netbarId"]').val()+"&round="+$('input[name="round"]').val();
		});
		$('#rand-groups').on('click', function() {
			var vurl="${ctx!}/activityGroup/rand/2?activityId="+$('input[name="activityId"]').val()+"&netbarId="+$('input[name="netbarId"]').val()+"&round="+$('input[name="round"]').val();
			console.log(vurl);
			$.api(vurl, {}, function(d) {	
			if(d.code==0){
				alert("随机排序成功");
				window.location.reload();
			}else{
				alert(d.result);
			}
		}, function(d) {
			alert('请求数据异常：' + d.result);
		}, {
			async: false,
		});
		});
	</script>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>#</th>
			<th>战队名</th>
			<th>选手名称</th>
			<th>手机号码</th>
			<th>QQ</th>
			<th>身份证号码</th>
			<th>签到</th>
			<th>操作</th>
		</tr>
		<#if list??>
		<#list list as o>
			<tr>
				<td>
					<#if (o.teamCount)?? && o.teamCount lt 5>
						<i class="icon-time"></i>
					</#if>
					${o.groupNumber!}
				</td>
				<td>${o.teamName!}</td>
				<td>${o.name!}</td>
				<td>${o.telephone!}</td>
				<td>${o.qq!}</td>
				<td>${o.idCard!}</td>
				<td>
					<div class="btn-group" data-toggle="buttons" sign="${o.teamId!}">
						<label class="btn unsign<#if (o.signed!0)!=1> btn-info active</#if>">
							<input type="radio" name="signed" value="0" /> 未签到
						</label>
						<label class="btn signed<#if (o.signed!0)==1> btn-info active</#if>">
							<input type="radio" name="signed" value="1" /> 已签到
						</label>
					</div>
				</td>
				<td>
					<button edit="${o.id!}" type="button" class="btn btn-info">编辑</button>
					<button remove="${o.id!}" isMonitor="${o.isMonitor!}" type="button" class="btn btn-danger">删除</button>
				</td>
			</tr>
		</#list>
		</#if>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	
	<#-- 编辑 -->
	<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
	<div id="modal-editor" class="modal fade">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideEditor()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title">编辑战队成员</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post">
						<input type="hidden" name="id" />
						<input type="hidden" name="isMonitor" />
						<input type="hidden" name="netbarId" value="${(params.netbar_id)!}" />
						<div class="form-group">
							<label class="col-md-2 control-label">姓名</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="name" value="">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">身份证</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="idCard" value="">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">联系方式</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="telephone" value="" readonly>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">QQ</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="qq" value="">
							</div>
						</div>
						<#-- <div class="form-group">
							<label class="col-md-2 control-label">擅长位置</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="labor" value="">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">大区</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="server" readonly value="">
							</div>
						</div> -->
						<div class="form-group">
							<label class="col-md-2 control-label">战队</label>
							<div class="col-md-10">
								<select class="form-control" name="teamId">
									<#if teams??>
										<#list teams as t>
											<option value="${t.id!}">${t.name!}</option>
										</#list>
									</#if>
								</select>
							</div>
							<input id="hidden-old-teamId" type="hidden" />
							<script type="text/javascript">
							$('#modal-editor [name="teamId"]').on('change', function() {
								if($('#modal-editor input[name="isMonitor"]').val() == 1) {
									$(this).val($('#modal-editor #hidden-old-teamId').val());
									alert('队长不能更改战队');
								}
							});
							</script>
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
	$('input[name]').attr("autocomplete", "off");
	
	// 初始化模态框
	var _$editor = $('#modal-editor');
	_$editor.modal({
		keyboard : false,
		show : false
	})
	
	// 初始化编辑窗口宽度
	function showEditor() {
		var width = 1200;
		var height = $(window).height() - 200;
		if(width > $(window).width()) {
			width = $(window).width();
		}
		_$editor.find('.modal-dialog').css('width', width);
		_$editor.find('.modal-body').css({
			height: height,
			overflow: 'scroll',
		});
		_$editor.modal('show', 'fit');
	}
	
	// 关闭模态框
	function hideEditor() {
		_$editor.modal('hide');
	}
	
	// 编辑
	$('button[edit]').on('click', function(event) {
		// 初始化表单数据
		var id = $(this).attr('edit');
		$.api('${ctx}/activityInfo/apply/team/detail/' + id, {}, function(d) {
			var o = d.object;
			$.fillForm({
				netbarId: o.netbarId,
				name: o.name,
				idCard: o.idCard,
				telephone: o.telephone,
				qq: o.qq,
				labor: o.labor,
				server: o.server,
				teamId: o.teamId,
				isMonitor: o.isMonitor,
				id: o.id,
			}, _$editor);
			$('#hidden-old-teamId').val(o.teamId);
		}, function(d) {
			alert('请求数据异常：' + d.result);
		}, {
			async: false,
		});
		
		showEditor();
	});
	
	// 提交表单
	function submitEditor() {
		var $form = _$editor.find('form');
		var roundSign = parseInt($('div[div-round]:eq(0)').attr('div-round'));
		$form.find('input[name="roundCount"]').val(roundSign + 1);
		$form.ajaxSubmit({
			url:'${ctx}/activityInfo/apply/team/save',
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
		
		var msg = '确认删除吗?';
		if(_this.attr('isMonitor') == '1') {
			msg = '该用户是战队队长，将删除战队信息及成员信息，确认删除吗？';
		}
		
		$.confirm(msg, function() {
			$.api('${ctx}/activityInfo/apply/team/delete/' + id, {}, function(d) {
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
	</script>
	
	<!-- 新增个人报名 -->
	<div id="modal-newmember" class="modal fade">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title">增加参赛用户</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post">
						<input type="hidden" name="activityId" />
						<input type="hidden" name="netbarId" />
						<input type="hidden" name="round" />
						<div class="form-group">
							<label class="col-md-2 control-label">战队</label>
							<div class="col-md-10">
								<select name="teamId" class="form-control">
									<option value="">创建战队</option>
									<#if teams??>
										<#list teams as t>
											<option value="${t.id!}"<#if (params.team_id)?? && (t.id)?? && params.team_id?string == t.id?string> selected</#if>>${t.name!}</option>
										</#list>
									</#if>
								</select>
							</div>
						</div>
						<div class="form-group create-team">
							<label class="col-md-2 control-label">战队名</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="teamName" wy-required="战队名" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">队员名</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="name" wy-required="姓名" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">身份证</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="idCard" wy-required="身份证" wy-required-type="idcard" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">联系号码</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="telephone" wy-required="联系方式" wy-required-type="telephone" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">QQ</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="qq" wy-required="QQ" />
							</div>
						</div>
						<#-- <div class="form-group">
							<label class="col-md-2 control-label">擅长位置</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="labor" wy-required="擅长位置" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">大区</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="server" wy-required="大区" />
							</div>
						</div> -->
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
					<button type="button" class="btn btn-primary" id="submit">保存</button>
				</div>
			</div>
		</div>
	</div>
	
	<!-- 操作中提示 -->
	<div id="modal-operating" class="modal fade">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">提示</div>
				<div class="modal-body" style="text-align:center;">
					<div><img src="${ctx}/static/images/loading.gif" /></div>
					操作中,请耐心等候 ...
				</div>
			</div>
		</div>
	</div>
	
	<script type="text/javascript">
	$('#modal-operating').modal({
		keyboard : false,
		show : false,
		backdrop : 'static'
	});
	
	// 新增
	$('#new-record').on('click', function() {
		var $newmemberEditor = $('#modal-newmember');
		$newmemberEditor.modal('show');
		
		$.fillForm({
			activityId: '${(params.activity_id)!}',
			netbarId: '${(params.netbar_id)!}',
			round: '${(params.round)!}',
		}, $newmemberEditor);
		
		$newmemberEditor.find('select[name="round"]').change();
		$newmemberEditor.find('select[name="teamId"]').change();
	});
	
	// 新增用户
	$('#modal-newmember #submit').on('click', function(event) {
		var $form = $('#modal-newmember form');
		var valid = $form.formValid();
		if(valid) {
			var $this = $(this);
			$this.prop('disabled', true);
			
			$form.ajaxSubmit({
				url : '${ctx}/activityInfo/apply/team/apply',
				type : 'post',
				success : function(d) {
					if (d.code == 0) {
						window.location.reload();
					} else {
						alert(d.result);
					}
				},
				complete : function() {
					$this.prop('disabled', false);
				}
			});
		}
	});
	
	// 选择战队后匹配创建战队信息
	$('#modal-newmember').find('select[name="teamId"]').on('change', function(event) {
		if($(this).val() == '') {// 创建战队
			$('.create-team').show().each(function(event) {
				var $this = $(this);
				$this.find('[name="teamName"]').attr('wy-required', '战队名');
			});
		} else {// 选择战队
			$('.create-team').hide().each(function(event) {
				var $this = $(this);
				$this.find('[name="teamName"]').removeAttr('wy-required');
			});
		}
	});
	
	// 签到
	$('[sign] label').on('click', function(ev) {
		var $this = $(this);
		if($this.hasClass('active')) {
			return;
		}
	
		$('#modal-operating').modal('show');
		var id = $this.parent().attr('sign');
		var signed = $this.hasClass('signed') ? 1 : 0;
		$.api('${ctx}/activityInfo/apply/sign', {id: id, signed: signed, isTeam: 1}, function(d) {
			$('[sign="' + id + '"] label').removeClass('btn-info').removeClass('active');
			var signed = d.object;
			if(signed == 1) {
				$('[sign="' + id + '"] .signed').addClass('btn-info active');
			} else if(signed == 0) {
				$('[sign="' + id + '"] .unsign').addClass('btn-info active');
			}
		}, false, {
			complete: function() {
				$('#modal-operating').modal('hide');
			}
		});
	});
	
	$('#reset-groups').on('click', function(d) {
		var $this = $(this);
		$.confirm('重置后数据将不可回复,确认重置吗?', function() {
			$this.prop('disabled', true);
			
			$.api('${ctx}/activityGroup/reset?activityId=${(params.activity_id)!}&round=${(params.round)!}&netbarId=${(params.netbar_id)!}&isTeam=1', {}, function() {
				alert('操作成功');
				window.location.reload();
			}, false, {
				component: function() {
					$this.prop('disabled', false);
				}
			});
		});
	});
	</script>
</body>
</html>