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
					<input type="text" class="form-control" name="roleName" placeholder="角色名" value="${(params.roleName)!}" />
				</div>
				<button type="submit" class="btn btn-success">查询</button>
			</form>
		</div>
		<div class="form-group">
			<button add type="button" class="btn btn-info">新增角色</button>
		</div>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>#</th>
			<th>用户名</th>
			<th>操作</th>
		</tr>
		<#list list as o>
			<tr>
				<td>${o.id!}</td>
				<td>${o.roleName!}</td>
				<td>
					<button edit="${o.id!}" roleName="${o.roleName!}" type="button" class="btn btn-info">编辑</button>
					<button remove="${o.id!}" type="button" class="btn btn-danger">删除</button>
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
					<button type="button" class="close" data-dismiss="modal">
						<span aria-hidden="true">×</span>
						<span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title">编辑用户</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post">
						<input type="hidden" name="id" />
						<input type="hidden" name="operateIds" />
						<div class="form-group">
							<label class="col-md-2 control-label">角色名</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="roleName" wy-required="角色名" value="">
							</div>
							<ul id="tree-operates" class="ztree"></ul>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" onclick="hideEditor()">关闭</button>
					<button id="submit" type="button" class="btn btn-primary">保存</button>
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
		_$editor.modal('show');
		
		initTree(0);
	});
	
	// 初始化地区树样式
	var _tree;
	$(function() {
		var height = $(window).height() - $('.navbar-bg').height() - $('.footer').height() - 160;
		$("#tree-operates").css({
			'max-height': height,
			'overflow': 'auto',
		});
	});
	
	// 初始化树
	function initTree(id) {
		// 根据角色ID产生树结构
		var setting = {
			async: {
				enable: true,
				url: '${base!}/system/role/operates/' + id,
				dataFilter: function(treeId, parentNode, responseData) {
					var ops = []
					if(responseData.code == 0) {
						ops = responseData.object;
						for(var i=0; i<ops.length; i++) {
							var op = ops[i];
							op.open = true;
							if(op.children) {
								for(var j=0; j<op.children.length; j++) {
									var c = op.children[j];
									c.open = true;
								}
							}
						}
					}
					
					return ops;
				}
			},
			check: {
				enable: true,
			}
		}
		
		// 重新初始化树
		if(_tree) {
			$.fn.zTree.destroy(_tree);
		}
		_tree = $.fn.zTree.init($("#tree-operates"), setting);
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
		var roleName = $(this).attr('roleName');
		initTree(id);
		
		// 初始化表单数据
		$.fillForm({
			roleName: roleName,
			id: id,
		}, _$editor);
		
		_$editor.modal('show');
	});
	
	// 提交表单
	_$editor.find('#submit').on('click', function() {
		var $form = _$editor.find('form');
		var valid = $form.formValid();
		if(valid) {
			var ids = '';
			var nodes = _tree.getCheckedNodes();
			for(var i=0; i<nodes.length; i++) {
				var n = nodes[i];
				if(ids.length > 0) {
					ids += ',';
				}
				ids += n.id;
			}
			$form.find('input[name="operateIds"]').val(ids);
			
			var _this = $(this);
			_this.prop('disabled', true);
			$form.ajaxSubmit({
				url:'${ctx}/system/role/save',
			    type:'post',
			    success: function(d){
			    	if(d.code == 0) {
			    		window.location.reload();
			    	} else {
			    		alert(d.result);
			    	}
				},
				complete: function() {
					_this.prop('disabled', false);
				}
			});
		}
	});
	
	// 删除
	$('button[remove]').on('click', function(event) {
		var _this = $(this);
		_this.attr('disabled', true);
		
		var id = $(this).attr('remove');
		
		$.confirm('确认删除吗?', function() {
			$.api('${ctx}/system/role/delete/' + id, {}, function(d) {
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
</body>
</html>