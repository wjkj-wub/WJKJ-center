<#import "/macros/pager.ftl" as p >
<html>
<head>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>管理系统</strong>
 		</li>
		<li class="active">
			用户管理
		</li>
	</ul>
	
	<#-- 搜索
	<div class="mb10">
		<form id="search" action="1" method="post">
			<div class="col-md-2">
				<input type="text" class="form-control" name="username" placeholder="用户名" value="${(params.username)!}" />
			</div>
			<div class="col-md-2">
				<input type="text" class="form-control" name="beginDate" placeholder="起始时间" value="${(params.beginDate)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:beginPick})" />
			</div>
			<div class="col-md-2">
				<input type="text" class="form-control" name="endDate" placeholder="截止时间" value="${(params.endDate)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:endPick})" />
			</div>
			<div class="col-md-2">
				<select class="form-control" name="valid">
					<option value="1"<#if ((params.valid?number)!1) == 1> selected</#if>>正常</option>
					<option value="0"<#if ((params.valid?number)!1) == 0> selected</#if>>被禁用</option>
				</select>
			</div>
			<button type="submit" class="btn btn-success">查询</button>
			<button add="" type="button" class="btn btn-info" style="margin-left:20px;">新增注册用户</button>
		</form>
	</div>
	<div class="mb10">
		<form id="export" action="/user/exportExcel" method="post">
			<input type="hidden" name="valid" value="${params.valid!}" />
			<input type="hidden" name="username" value="${params.username!}" />
			<input type="hidden" name="beginDate" value="${params.beginDate!}" />
			<input type="hidden" name="endDate" value="${params.endDate!}" />
			<select class="form-control" name="page" style="margin-left:10px;width:130px;height:34px;display:inline">
				<option value="${currentPage!}"<#if ((params.valid?number)!1) == 1> selected</#if>>当前页</option>
				<option value=""<#if ((params.valid?number)!1) == 0> selected</#if>>全部</option>
			</select>
			<button type="submit" class="btn btn-success" style="">导出用户信息</button>
		</form>
	</div>
	 -->
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>头像</th>
			<th>用户名</th>
			<th>昵称</th>
			<th>操作</th>
		</tr>
		<#list list as o>
			<tr>
				<td><#if (o.icon)??><img src="${imgServer!}/${o.icon!}" style="width: 50px; height: 50px;" /></#if></td>
				<td>${o.username!}</td>
				<td>${o.nickname!}</td>
				<td>
					<button edit="${o.id!}" type="button" class="btn btn-info">编辑</button>
					<#if ((o.valid?number)!0) == 1>
						<button remove="${o.id!}" type="button" class="btn btn-danger">禁用</button>
					<#else>
						<button enabled="${o.id!}" type="button" class="btn btn-danger">启用</button>
					</#if>
				</td>
			</tr>
		</#list>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	
	<#-- 编辑 -->
	<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
	<div id="modal-editor" class="modal fade">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideChangePwd()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title">编辑用户</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post">
						<input type="hidden" name="id" />
						<div class="form-group">
							<label class="col-md-2 control-label">用户名</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="username" readonly value="">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">昵称</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="nickname" value="">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">积分</label>
							<div class="col-md-10">
								<input class="form-control" type="number" name="score" value="">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">密码</label>
							<div class="col-md-10">
								<input class="form-control" type="password" name="password" value="">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">真实姓名</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="realName" value="">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">身份证</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="idCard" value="">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">QQ</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="qq" value="">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">性别</label>
							<div class="col-md-10">
								<label><input type="radio" name="sex" value="0"> 男</label>
								<label><input type="radio" name="sex" value="1"> 女</label>
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
	
	<#-- 新增注册用户 -->
	<div id="modal-add" class="modal fade">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideChangePwd()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title">新增注册用户（默认密码：123456）</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post">
						<input type="hidden" name="password" value="123456" />
						<div class="form-group">
							<label class="col-md-2 control-label">手机号</label>
							<div class="col-md-10">
								<input id="input_id_mobile" class="form-control" type="text" name="mobile" value="">
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" onclick="hideEditor()">关闭</button>
					<button type="button" class="btn btn-primary" onclick="submitAdd()">提交</button>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
	// 初始化模态框
	var _$editor = $('#modal-editor');
	var _$add = $('#modal-add');
	_$editor.modal({
		keyboard : false,
		show : false
	})
	
	// 关闭模态框
	function hideEditor() {
		_$editor.modal('hide');
		_$add.modal('hide');
	}
	
	// 显示新增
	$('button[add]').on('click', function(event) {
		$('#input_id_mobile').val('');
		_$add.modal('show');
	});
	// 提交注册
	function submitAdd() {
		_$add.find('form').ajaxSubmit({
			url:'${ctx}/user/register',
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
	
	// 显示模态框
	$('button[edit]').on('click', function(event) {
		// 初始化模态框宽度
		var width = 1200;
		if(width > $(window).width()) {
			width = $(window).width();
		}
		_$editor.find('.modal-dialog').css('width', width);
		
		// 初始化表单数据
		var id = $(this).attr('edit');
		$.api('${ctx}/user/detail/' + id, {}, function(d) {
			var o = d.object;
			$.fillForm({
				username: o.username,
				nickname: o.nickname,
				realName: o.realName,
				idCard: o.idCard,
				qq: o.qq,
				sex: o.sex,
				password: '',
				score: o.score,
				id: id,
			}, _$editor);
		}, function(d) {
			alert('请求数据异常：' + d.result);
		}, {
			async: false,
		});
		_$editor.modal('show');
	});
	
	// 提交表单
	function submitEditor() {
		_$editor.find('form').ajaxSubmit({
			url:'${ctx}/user/save',
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
			$.api('${ctx}/user/delete/' + id, {}, function(d) {
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
	
	// 启用
	$('button[enabled]').on('click', function(event) {
		var _this = $(this);
		_this.prop('disabled', true);
		
		var id = _this.attr('enabled');
		$.confirm('确认启用吗?', function() {
			$.api('${ctx}/user/enabled/' + id, {}, function(d) {
				window.location.reload();
			}, function(d) {
				alert(d.result);
			}, {
				complete: function() {
					_this.prop('disabled', false);
				}
			});
		}, undefined, {
			complete: function() {
				_this.prop('disabled', false);
			}
		})
	});
	</script>
</body>
</html>