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
			地区管理
		</li>
	</ul>
	
	<#-- 操作按钮 -->
	<div class="mb10">
		<button id="new-province" type="button" class="btn btn-success">添加省份</button>
		<button id="new-area" type="button" class="btn btn-success">添加城市或地区</button>
		<button id="edit-area" type="button" class="btn btn-success">编辑地区</button>
		<button id="remove-area" type="button" class="btn btn-success">删除地区</button>
		<#if onlyValidArea == "0">
			<a class="btn btn-success" href="list?onlyValidArea=1">查看有效地区</a>
		<#else>
			<a class="btn btn-success" href="list?onlyValidArea=0">查看全部地区</a>
		</#if>
		<#if onlyValidArea == "0">
		<button id="open-area" type="button" class="btn btn-success">开通地区</button>
		<#else>
		<button id="close-area" type="button" class="btn btn-success">关闭地区</button>
		</#if>
		<button id="update-area-redis" type="button" class="btn btn-success">更新地区缓存</button>
	</div>
	
	<#-- 地区树 -->
	<div class="mb10">
		<div class="col-md-8">
			<ul id="tree-areas" class="ztree"></ul>
		</div>
		<!-- 树业务 -->
		<script type="text/javascript">
		// 树设置
		var setting = {
			callback: {
				onClick: function(event, treeId, treeNode, clickFlag) {
				},
			},
		}
		
		// 为地区数据做特殊数据设置
		function filterTreeData(nodes) {
			// 修改显示名称为 地名+code
			function changeName(node) {
				node.name = node.name + '(' + node.areaCode + ')';
				if(node.children && node.children.length > 0) {
					filterArray(node.children);
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
				changeName(nodes[i]);
			}
		}
		
		// 初始化地区数据
		var originalNodes = '${areas!}';
		var zNodes = [];
		if(originalNodes.length > 0) {
			originalNodes = JSON.parse(originalNodes);
			zNodes = zNodes.concat(originalNodes);
			
			filterTreeData(zNodes);
			console.log(zNodes);
			
			for(var i=0; i<zNodes.length; i++) {
				zNodes[i].open = false;
			}
		}
		
		// 初始化地区树
		var $tree = $.fn.zTree.init($("#tree-areas"), setting, zNodes);
		
		// 初始化地区树样式
		$(function() {
			var height = $(window).height() - $('.navbar-bg').height() - $('.footer').height() - 160;
			$("#tree-areas").css({
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
							<label class="col-md-2 control-label">地区名</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="name" value="" wy-required="地区名">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">地区编码</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="areaCode" value="" wy-required="地区编码">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">拼音</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="pinyin" value="" wy-required="拼音">
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
					url:'${ctx}/area/save',
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
		// 添加省份
		$('#new-province').on('click', function() {
			$.fillForm({
				pid: 1,
			}, _$editor);
			showEditor('新增省份');
		});
		
		// 添加地区
		$('#new-area').on('click', function() {
			var node = getSelectedNode();
			if(node) {
				if(node.level < 2) {
					$.fillForm({
						pid: node.id,
					}, _$editor);
					showEditor('新增地区');
				} else {
					alert('不能为城区添加地区');
				}
			} else {
				alert('请先选择要新增子地区的节点');
			}
		});
		
		// 编辑地区
		$('#edit-area').on('click', function() {
			var _this = $(this);
			_this.prop('disabled', true);
			
			var node = getSelectedNode();
			if(node) {
				var id = node.id;
				$.api('${ctx!}/area/detail/' + id, {}, function(d) {
					var o = d.object;
					$.fillForm({
						id: o.id,
						pid: o.pid,
						name: o.name,
						areaCode: o.areaCode,
						pinyin: o.pinyin,
					}, _$editor);
					showEditor('编辑地区');
				}, function() {}, {
					complete: function() {
						_this.prop('disabled', false);
					}
				});
			} else {
				alert('请先选择要编辑的地区');
				_this.prop('disabled', false);
			}
		});
		
		// 删除地区
		$('#remove-area').on('click', function() {
			var node = getSelectedNode();
			if(node) {
				var id = node.id;
				$.confirm('删除将移除地区及下层地区，确认删除吗?', function() {
					$.api('${ctx!}/area/delete/' + id, {}, function(d) {
						window.location.reload();
					})
				});
			} else {
				alert('请先选择要删除的地区');
			}
		});
		
		//更新缓存
		$('#update-area-redis').on('click', function() {
			$.ajax({
			type : 'post',
			url : '${ctx}/area/updateAreaCodeInRedis',
			cache : false,
			dataType : 'json',
			success : function(data) {
			        alert(data.result);
			}
		});
		});
		
		//开通地区
		$('#open-area').on('click', function() {
		    var _this = $(this);
			_this.prop('disabled', true);
			
			var node = getSelectedNode();
			if(node) {
				$.ajax({
			type : 'post',
			url : '${ctx}/area/open?id='+node.id,
			cache : false,
			dataType : 'json',
			success : function(data) {
			        alert(data.result);
			}
		});
			} else {
				alert('请先选择要开通的地区');
				_this.prop('disabled', false);
			}
		});
		
		//开通地区
		$('#close-area').on('click', function() {
		    var _this = $(this);
			_this.prop('disabled', true);
			
			var node = getSelectedNode();
			if(node) {
				$.ajax({
			type : 'post',
			url : '${ctx}/area/close?id='+node.id,
			cache : false,
			dataType : 'json',
			success : function(data) {
			        alert(data.result);
			}
		});
			} else {
				alert('请先选择要关闭的地区');
				_this.prop('disabled', false);
			}
		});
	</script>
</body>
</html>