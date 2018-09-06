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
			<button add-root type="button" class="btn btn-info">新增根菜单</button>
			<button add-child type="button" class="btn btn-info">新增子菜单</button>
			<button edit type="button" class="btn btn-info">编辑菜单</button>
			<button remove type="button" class="btn btn-info">删除菜单</button>
		</div>
	</div>
	
	<#-- 树 -->
	<div class="mb10">
		<div class="col-md-8">
			<ul id="tree-operates" class="ztree"></ul>
		</div>
		<script type="text/javascript">
		// 树设置
		var setting = {
			data: {
				key: {
					url: 'disabled',
				}
			},
			callback: {
				onClick: function(event, treeId, treeNode, clickFlag) {
				},
			},
		}
		
		// 初始化地区数据
		var zNodes = <#if operateTree??>${operateTree}<#else>[]</#if>;
		for(var i=0; i<zNodes.length; i++) {
			var n = zNodes[i];
			n.open = true;
			if(n.children && n.children.length > 0) {
				for(var j=0; j<n.children.length; j++) {
					var cn = n.children[j];
					cn.open = true;
				}
			}
		}
		
		// 初始化地区树
		var $tree = $.fn.zTree.init($("#tree-operates"), setting, zNodes);
		
		// 初始化地区树样式
		$(function() {
			var height = $(window).height() - $('.navbar-bg').height() - $('.footer').height() - 160;
			$("#tree-operates").css({
				'max-height': height,
				'overflow': 'auto',
			});
		});
		</script>
	</div>
	
	<#-- 模态框 -->
	<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
	<div id="modal-editor" class="modal fade">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideEditor()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title">编辑地区</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post">
						<input type="hidden" name="id" />
						<input type="hidden" name="pid" />
						<div class="form-group">
							<label class="col-md-2 control-label">菜单名</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="name" value="" wy-required="菜单名">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">URL</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="url" value="" wy-required="url">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">排序序号</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="orderId" value="">
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
	<#-- 模态框业务 -->
	<script type="text/javascript">
		var _$editor = $('#modal-editor');
		function showEditor(title) {
			if(title) {
				_$editor.find('.modal-title').html(title);
			}
			_$editor.modal('show');
		}
		function hideEditor() {
			_$editor.modal('hide');
		}
		function submitEditor() {
			var $form = _$editor.find('form');
			var valid = $form.formValid();
			if(valid) {
				$form.ajaxSubmit({
					url:'${ctx}/system/operate/save',
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
		}
	</script>
	
	<#-- 增删改操作 -->
	<script type="text/javascript">
		// 获取选中的节点
		function getSelectedNode() {
			var nodes = $tree.getSelectedNodes();
			if(nodes.length > 0) {
				return nodes[0];
			}
		}
		
		// 添加根菜单
		$('button[add-root]').on('click', function() {
			$.fillForm({
				pid: 0,
			}, _$editor);
			showEditor('新增菜单');
		});
		
		// 添加子菜单
		$('button[add-child]').on('click', function() {
			var node = getSelectedNode();
			if(node) {
				if(node.level < 2) {
					$.fillForm({
						pid: node.id,
					}, _$editor);
					showEditor('新增菜单');
				} else {
					alert('菜单最多不能超过三级哦亲');
				}
			} else {
				alert('请先选择要新增子节点的节点');
			}
		});
		
		// 编辑菜单
		$('button[edit]').on('click', function() {
			var node = getSelectedNode();
			if(node) {
				var o = node;
				$.fillForm({
					name: o.name,
					url: o.url,
					orderId: o.orderId,
					id: o.id,
				}, _$editor);
				showEditor('编辑菜单');
			} else {
				alert('请先选择要编辑的地区');
			}
		});
		
		// 删除地区
		$('button[remove]').on('click', function() {
			var node = getSelectedNode();
			if(node) {
				var id = node.id;
				$.confirm('删除将移除菜单及其子菜单，确认删除吗?', function() {
					$.api('${ctx!}/system/operate/delete/' + id, {}, function(d) {
						window.location.reload();
					})
				});
			} else {
				alert('请先选择要删除的地区');
			}
		});
	</script>
</body>
</html>