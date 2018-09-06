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
			【${itemName}】-游戏区管理
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post" style="margin-left:30px;display: inline">
			<div class="col-md-2">
				<input type="text" class="form-control" name="name" placeholder="名称" value="${(params.name)!}" />
				<input type="hidden" name="itemId" value="${(params.itemId)!}"/>
				<input type="hidden" name="isParent" value="1"/>
			</div>
			<button type="submit" class="btn btn-success">查询</button>
			<button add="${(params.itemId)!}" page="${currentPage}" type="button" class="btn btn-info"  style="margin-left:30px;">新增游戏区</button>
		</form>
		<form id="return" style="margin-left:30px;display: inline" action="/activityIteam/list/1" method="post">
			<button type="submit" class="btn btn-success">返回游戏列表</button>
		</form>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>游戏区</th>
			<th>操作</th>
		</tr>
		<#list list as o>
			<tr>
				<td>${o.server_name!}</td>
				<td>
					<button edit="${o.id!}" parentName="${o.server_name!}" type="button" class="btn btn-info">编辑</button>
					<form id="${o.id!}" style="margin:0px;display: inline" action="/activityIteamServer/listServer/1" method="post">
						<input type="hidden" name="id" value="${o.id!}"/>
						<input type="hidden" name="itemId" value="${(params.itemId)!}"/>
						<input type="hidden" name="isParent" value="0"/>
						<input type="hidden" name="pId" value="${o.id!}"/>
						<button type="submit" class="btn btn-success">服务器</button>
					</form>
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
					<button type="button" class="close" onclick="hideEditor()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title">游戏区</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post">
						<input type="hidden" name="id" />
						<input type="hidden" name="itemId" />
						<div class="form-group">
							<label class="col-md-2 control-label">游戏区名称</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="parentName" value="">
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" onclick="hideEditor()">关闭</button>
					<button type="button" id="baocun" page="${currentPage}" class="btn btn-primary" onclick="submitEditor()">保存</button>
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
	
	// 初始化表单数据
	function fillForm(columns) {
		for(var k in columns) {
			_$editor.find('input[name="' + k + '"]').val(columns[k]);
		}
	}
	
	// 显示模态框，编辑
	$('button[edit]').on('click', function(event) {
		var id = $(this).attr('edit');
		var parentName = $(this).attr('parentName');
		fillForm({
			id: id,
			parentName: parentName
		});
		_$editor.modal('show');
	});
	// 显示模态框,添加
	$('button[add]').on('click', function(event) {
		fillForm({
			id: '',
			itemId: ${(params.itemId)!},
			parentName: ''
		});
		_$editor.modal('show');
	});
	
	// 提交表单
	function submitEditor() {
		_$editor.find('form').ajaxSubmit({
			url:'${ctx}/activityIteamServer/save',
		    type:'post',
		    success: function(d){
		    	if(d.code == 0) {
		    		window.location.href = '${ctx}/activityIteamServer/listServer/${currentPage}?itemId=${(params.itemId)!}&isParent=1';
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
			$.api('${ctx}/activityIteamServer/delete/' + id, {}, function(d) {
		    		window.location.href = '${ctx}/activityIteamServer/listServer/${currentPage}?itemId=${(params.itemId)!}&isParent=1';
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
	//重新加载
	function reload(){
		var $search = $('#search');
		var page = $('#baocun').attr('page');
		if($search.length > 0) {
			$search.attr('action', page);
			$search.submit();
		} else {
			window.location.href = page;
		}
	}
	</script>
</body>
</html>