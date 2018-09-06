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
			赛事约战项目管理
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
			<div class="col-md-2">
				<input type="text" class="form-control" name="name" placeholder="名称" value="${(params.name)!}" />
			</div>
			<button type="submit" class="btn btn-success">查询</button>
			<button add="" type="button" class="btn btn-info">新增</button>
		</form>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>图标</th>
			<th>名称</th>
			<th>是否需要服务器</th>
			<th>操作</th>
		</tr>
		<#list list as o>
			<tr>
				<td><img src="${imgServer!}/${o.icon!}" style="width: 50px; height: 50px;" /></td>
				<td>${o.name!}</td>
				<#assign temp=(o.server_required!-1)>
				<#if 1 == temp>
					<td>是</td>
				<#elseif 0 == temp>
					<td>否</td>
				<#else>
					<td></td>
				</#if>
				<td>
					<button edit="${o.id!}" icon="${o.icon!}" pic="${o.pic!}" name="${o.name!}" serverRequired="${o.server_required!}" type="button" class="btn btn-info">编辑</button>
					<button remove="${o.id!}" type="button" class="btn btn-danger">删除</button>
					<#assign temp=(o.server_required!-1)>
					<#if 1 == temp>
						<form id="${o.id!}" style="margin:0px;display: inline" action="/activityIteamServer/listServer/1" method="post">
							<input type="hidden" name="id"/>
							<input type="hidden" name="itemId" value="${o.id!}"/>
							<input type="hidden" name="isParent" value="1"/>
							<button type="submit" class="btn btn-success">游戏大区</button>
						</form>
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
		<div class="modal-dialog" style="width:800px;">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideEditor()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title">编辑赛事约战项目</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post">
						<input type="hidden" name="id" />
						<div class="form-group">
							<label class="col-md-2 control-label">项目名称</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="name" value="">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">是否需要服务器</label>
							<div class="col-md-10">
								<select id="select_id_serverRequired" class="form-control" name="serverRequired">
									<option value="">请选择</option>
									<option value="0">否</option>
									<option value="1">是</option>
								</select>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">图片</label>
							<div class="col-md-10">
								<input type="file" name="picFile" value="" accept="image/*" />
								<font class="prompt">
									请上传300*300宽高的图片，以达到最佳显示效果
								</font>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">Icon</label>
							<div class="col-md-10">
								<input type="file" name="iconFile" value="上传Logo" accept="image/*" />
								<font class="prompt">
									请上传64:31宽高比的图片，以达到最佳显示效果
								</font>
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
	
	// 初始化表单数据
	function fillForm(columns) {
		for(var k in columns) {
			_$editor.find('input[name="' + k + '"]').val(columns[k]);
		}
	}
	
	// 显示模态框，编辑
	$('button[edit]').on('click', function(event) {
		var id = $(this).attr('edit');
		var name = $(this).attr('name');
		var pic = $(this).attr('pic');
		var icon = $(this).attr('icon');
		var serverRequired = $(this).attr('serverRequired');
		$.fillForm({
			id: id,
			name: name,
			picFile: pic,
			iconFile: icon
		},_$editor);
		$('#select_id_serverRequired').val(serverRequired);
		_$editor.modal('show');
	});
	
	// 显示模态框，新增
	$('button[add]').on('click', function(event) {
		_$editor.modal('show');
	});
	
	// 提交表单
	function submitEditor() {
		var $form = _$editor.find('form');
		
		// 检查上传文件是否为图片格式
		var types = "image/png,image/jpeg,image/gif";
		var iconFiles = $form.find('[name="iconFile"]').get(0).files;
		if(iconFiles.length > 0) {
			for(var i=0; i<iconFiles.length; i++) {
				var file = iconFiles[i];
				if(types.indexOf(file.type) == -1) {
					alert('Icon图片类型错误');
					$this.prop('disabled', false);
					return;
				}
			}
		}
		var picFiles = $form.find('[name="picFile"]').get(0).files;
		if(picFiles.length > 0) {
			for(var i=0; i<picFiles.length; i++) {
				var file = picFiles[i];
				if(types.indexOf(file.type) == -1) {
					alert('图片类型错误');
					$this.prop('disabled', false);
					return;
				}
			}
		}
		
		$form.ajaxSubmit({
			url:'${ctx}/activityIteam/save',
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
			$.api('${ctx}/activityIteam/delete/' + id, {}, function(d) {
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