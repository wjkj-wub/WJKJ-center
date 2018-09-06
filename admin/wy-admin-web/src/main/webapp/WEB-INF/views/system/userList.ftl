<#import "/macros/pager.ftl" as p >
<html>
<head>
<script type="text/javascript" src="${ctx!}/static/plugin/ztree/js/jquery.ztree.all-3.5.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/ztree/js/jquery.ztree.exhide-3.5.min.js"></script>
<link rel="stylesheet" type="text/css" href="${ctx!}/static/plugin/ztree/css/zTreeStyle.css"/>
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
	
	<#-- 搜索 -->
	<div class="mb10">
		<div class="form-group">
			<form id="search" action="1" method="post">
				<div class="col-md-2">
					<input type="text" class="form-control" name="username" placeholder="用户名" value="${(params.username)!}" />
				</div>
				<div class="col-md-2">
					<select class="form-control" name="roleId">
						<option value="">全部角色</option>
						<#if roles??>
							<#list roles as r>
								<option value="${(r.id)!}"<#if (params.roleId)?? && params.roleId == r.id?string> selected</#if>>${(r.roleName)!}</option>
							</#list>
						</#if>
					</select>
				</div>
				<#-- <div class="col-md-2">
					<select class="form-control" name="type">
						<option value="">全部类型</option>
						<option value="1"<#if ((params.type)!-1) == "1"> selected</#if>>普通管理员</option>
						<option value="10"<#if ((params.type)!-1) == "10"> selected</#if>>赛事约战子账号</option>
						<option value="12"<#if ((params.type)!-1) == "12"> selected</#if>>娱乐赛审核帐号</option>
						<option value="13"<#if ((params.type)!-1) == "13"> selected</#if>>娱乐赛发放帐号</option>
						<option value="14"<#if ((params.type)!-1) == "14"> selected</#if>>娱乐赛申诉帐号</option>
					</select>
				</div> -->
				<div class="col-md-2">
					<select class="form-control" name="areaCode">
						<option value="">全部地区</option>
						<#if areasObj??>
							<#list areasObj as a>
								<option value="${(a.areaCode)!}"<#if (params.areaCode)?? && a.areaCode == params.areaCode> selected</#if>>${(a.name)!}</option>
							</#list>
						</#if>
					</select>
				</div>
				<button type="submit" class="btn btn-success">查询</button>
			</form>
		</div>
		<div class="form-group">
			<button add type="button" class="btn btn-info">新增用户</button>
		</div>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>#</th>
			<th>用户名</th>
			<th>帐号名称</th>
			<th>权限地区</th>
			<th>联系号码</th>
			<th>操作</th>
		</tr>
		<#if list??>
		<#list list as o>
			<tr>
				<td>${o.id!}</td>
				<td>${o.username!}</td>
				<td>${o.realname!}</td>
				<td><#if (o.areaCode)?? && o.areaCode == '000000'>全国<#else>${o.areaName!}</#if></td>
				<td>${o.telephone!}</td>
				<td>
					<button selectRole="${o.id!}" type="button" class="btn btn-info">赋予角色</button>
					<button edit="${o.id!}" type="button" class="btn btn-info">编辑</button>
					<button remove="${o.id!}" type="button" class="btn btn-danger">删除</button>
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
					<h4 class="modal-title">编辑用户</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post">
						<input type="hidden" name="id" />
						<div class="form-group">
							<label class="col-md-2 control-label">用户名</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="username" wy-required="用户名" readonly value="">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">帐号名称</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="realname" wy-required="帐号名称" value="">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">管理员类型</label>
							<div class="col-md-10">
								<select class="form-control" name="userType">
									<option value="1">普通管理员</option>
									<option value="10">赛事约战子账号</option>
									<option value="12">娱乐赛审核帐号</option>
									<option value="13">娱乐赛发放帐号</option>
									<option value="14">娱乐赛申诉帐号</option>
								</select>
							</div>
						</div>
						<!-- <div class="form-group">
							<label class="col-md-2 control-label" id="div-areas-belong">帐号归属地</label>
							<div class="col-md-10">
								<ul id="tree-areas-belong" class="ztree"></ul>
							</div>
						</div> -->
						<div class="form-group" id="div-areas">
							<label class="col-md-2 control-label">权限地区</label>
							<div class="col-md-10">
								<ul id="tree-areas" class="ztree"></ul>
							</div>
						</div>
						<div class="form-group" id="div-countrywide">
							<label class="col-md-2 control-label">权限地区</label>
							<div class="col-md-10">
								<input class="form-control" value="全国" readonly />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">密码</label>
							<div class="col-md-10">
								<input class="form-control" type="password" name="password" value="">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">联系方式</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="telephone" wy-required="联系方式" value="">
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
	
	// 添加用户
	$('button[add]').on('click', function(event) {
		// 初始化模态框宽度
		var width = 1200;
		if(width > $(window).width()) {
			width = $(window).width();
		}
		_$editor.find('.modal-dialog').css('width', width);
		
		// 初始化表单数据
		$.fillForm({
			id: '',
		}, _$editor);
		
		// 初始化地区设置
		$('[name="userType"]').change();
		
		_$editor.find('[name="username"]').prop('readonly', false);
		_$editor.modal('show');
	});
	
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
		
		$.api('${ctx}/system/user/detail/' + id, {}, function(d) {
			var o = d.object;
			$.fillForm({
				username: o.username,
				realname: o.realname,
				qq: o.qq,
				telephone: o.telephone,
				password: '',
				email: o.email,
				userType: o.userType,
				id: id,
			}, _$editor);
			
			// 初始化地区设置
			$('[name="userType"]').change();
			
			// 初始化权限地区的选择状态
			var areas = o.areas;
			checkTreeByAreaCode($tree, areas);
			
			// 初始化归属地区的选择状态
			areas = [{areaCode:o.areaCode}];
			checkTreeByAreaCode($treeBelong, areas);
		}, function(d) {
			alert('请求数据异常：' + d.result);
		}, {
			async: false,
		});
		_$editor.find('[name="username"]').prop('readonly', true);
		_$editor.modal('show');
	});
	
	// 选中$t树控件中,areaCode在areas中的项
	function checkTreeByAreaCode($t, areas) {
		// 初始化控件
		$t.checkAllNodes(false);
		$t.expandAll(false);
		
		// 匹配选中地区并展开
		for(var i=0; i<areas.length; i++) {
			var a = areas[i];
			if(a.areaCode) {
				var areaCode = a.areaCode.substring(0, 4) + '00';
				var ns = $t.getNodesByParam('areaCode', a.areaCode);
				for(var j=0; j<ns.length; j++) {
					var n = ns[j];
					var p = n.getParentNode();
					$t.expandNode(p, true);
					$t.checkNode(n, true);
				}
			}
		}
		
		// 折叠无选中状态的地区
		var nocheckedRootNodes = $t.getNodesByFilter(function(node) {
			if(node.level == 0) {
				var children = node.children;
				for(var i=0; i<children.length; i++) {
					var c = children[i];
					var cchildren = c.children;
					if(c.checked) {
						return false;
					}
					for(var j=0; j<cchildren.length; j++) {
						var cc = cchildren[j];
						if(cc.checked) {
							return false;
						}
					}
				}
				return true;
			}
			
			return false;
		});
		for(var i=0; i<nocheckedRootNodes.length; i++) {
			var c = nocheckedRootNodes[i];
			$t.expandNode(c, false);
		}
	}
	
	// 更改管理员类型
	$('[name="userType"]').on('change', function(event) {
		var userType = $(this).val();
		var $divAreas = $('#div-areas');
		var $divCountrywide = $('#div-countrywide');
		if(userType == 1) {
			$divAreas.hide();
			$divCountrywide.show();
		} else {
			$divAreas.show();
			$divCountrywide.hide();
		}
	});
	
	// 提交表单
	function submitEditor() {
		var $form = _$editor.find('form');
		var valid = $form.formValid();
		if(valid) {
			// 匹配用户地区信息
			var $area = $('#div-areas');
			function hidden(name, value) {
				return '<input type="hidden" name="' + name + '" value="' + value + '" />';
			}
			
			// 根据用户类型，匹配地区设置
			if($form.find('[name="userType"]').val() == 1) {
				$area.append(hidden('area', '000000'));				
			} else {
				var checkedNodes = $tree.getCheckedNodes();
				$area.find('[name="area"]').remove();
				for(var i=0; i<checkedNodes.length; i++) {
					var n = checkedNodes[i];
					
					$area.append(hidden('area', n.areaCode));
				}
			}
			
			// 产生归属地code的hidden
			var checkedBelongNodes = $treeBelong.getCheckedNodes();
			$form.find('[name="areaCode"]').remove();
			for(var i=0; i<checkedBelongNodes.length; i++) {
				var n = checkedBelongNodes[i];
				$form.append(hidden('areaCode', n.areaCode));
			}
			
			// 提交表单
			$form.ajaxSubmit({
				url:'${ctx}/system/user/save',
			    type:'post',
			    success: function(d) {
			    	if(d.code == 0) {
			    		window.location.reload();
			    	} else {
			    		alert(d.result);
			    	}
				}
			});
		}
	}
	
	// 删除
	$('button[remove]').on('click', function(event) {
		var _this = $(this);
		_this.attr('disabled', true);
		
		var id = $(this).attr('remove');
		
		$.confirm('确认删除吗?', function() {
			$.api('${ctx}/system/user/delete/' + id, {}, function(d) {
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
	
	<#-- 赋予角色 -->
	<div id="modal-role-selector" class="modal fade">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideChangePwd()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title">赋予角色</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post">
						<input type="hidden" name="id" />
						<div class="form-group">
							<div class="col-md-10">
								<#if roles??>
								<#list roles as r>
									<div><label><input type="checkbox" name="role" value="${r.id!}"> ${r.roleName!}</label></div>
								</#list>
								</#if>
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" onclick="hideRoleSelector()">关闭</button>
					<button id="submit" type="button" class="btn btn-primary">保存</button>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		// 初始化模态框
		var _$roleSelector = $('#modal-role-selector');
		_$roleSelector.modal({
			keyboard : false,
			show : false
		})
		
		// 关闭模态框
		function hideRoleSelector() {
			_$roleSelector.modal('hide');
		}
		function showRoleSelector() {
			_$roleSelector.modal('show');
		}
		
		// 显示选择角色框
		$('button[selectRole]').on('click', function(event) {
			// 初始化模态框宽度
			var width = 1200;
			if(width > $(window).width()) {
				width = $(window).width();
			}
			_$roleSelector.find('.modal-dialog').css('width', width);
			
			// 初始化表单数据
			var id = $(this).attr('selectRole');
			_$roleSelector.find('input[name="id"]').val(id);
			_$roleSelector.find('input[name="role"]').prop("checked", false);
			$.api('${ctx}/system/user/roles/' + id, {}, function(d) {
				var o = d.object;
				for(var i=0; i<o.length; i++) {
					var roleId = o[i].role_id;
					_$roleSelector.find('input[name="role"][value="' + roleId + '"]').prop('checked', true);
				}
			}, function(d) {
				alert('请求数据异常：' + d.result);
			}, {
				async: false,
			});
			_$roleSelector.find('[name="username"]').prop('readonly', true);
			_$roleSelector.modal('show');
		});
		
		// 提交表单
		_$roleSelector.find('#submit').on('click', function() {
			var ids = '';
			_$roleSelector.find('input[name="role"]:checked').each(function() {
				var v = $(this).val();
				if(ids.length > 0) {
					ids += ',';
				}
				ids += v;
			});
			var userId = _$roleSelector.find('input[name="id"]').val();
			
			var _this = $(this);
			_this.prop('disabled', true);
			$.api('${base!}/system/user/saveRoles', {userId: userId,roleIds: ids}, function() {
				window.location.reload();
			}, undefined, {
				complete: function() {
					_this.prop('disabled', false);
				}
			});
		});
	</script>
	
	<!-- 地区选择框相关功能 -->
	<script type="text/javascript">
	// 初始化地区树
	var setting = {
		callback: {
			onClick: function(event, treeId, treeNode, clickFlag) {
			},
		},
		check: {
			enable: true,
			chkStyle: "radio",
			radioType: 'all',
		}
	}
	
	// 为地区数据做特殊数据设置
	function filterTreeData(nodes) {
		// 修改显示名称为 地名+code
		function changeName(node) {
			node.name = node.name + '(' + node.areaCode + ')';
			if(node.children && node.children.length > 0) {
				filterArray(node.children);
			}
			
			var areaCode = node.areaCode;
			if(areaCode.indexOf('0000') >= 0) {
				node.chkDisabled = true;
			} else {
				node.children = [];
			}
		}
		
		// 递归地区子集合
		function filterArray(children) {
			for(var i=0; i<children.length; i++) {
				changeName(children[i]);
			}
		}
		
		// 遍历第一级数据
		for(var i=0; i<nodes.length; i++) {
			var n = nodes[i];
			changeName(n);
		}
	}
	
	// 初始化地区数据
	var originalNodes = '${areas!}';
	var zNodes = [];
	if(originalNodes.length > 0) {
		originalNodes = JSON.parse(originalNodes);
		zNodes = zNodes.concat(originalNodes);
		
		filterTreeData(zNodes);
		
		for(var i=0; i<zNodes.length; i++) {
			zNodes[i].open = false;
		}
	}
	
	// 初始化地区树
	var $tree = $.fn.zTree.init($("#tree-areas"), setting, zNodes);
	var $treeBelong = $.fn.zTree.init($('#tree-areas-belong'), setting, zNodes);
	
	// 初始化地区树样式
	$(function() {
		$("#tree-areas").css({
			'max-height': 300,
			'overflow': 'auto',
		});
	});
	</script>
</body>
</html>