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
			手游信息
		</li>
	</ul>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>图标</th>
			<th>名称</th>
			<th>版本</th>
			<th>评分</th>
			<th>是否推荐</th>
			<th>下载次数</th>
			<th>android版本大小</th>
			<th>ios版本大小</th>
			<th>操作</th>
		</tr>
		<tr>
			<td><img src="${imgServer!}/${gameInfo.icon!}" style="width: 50px; height: 50px;" /></td>
			<td>${gameInfo.name!}</td>
			<td>${gameInfo.version!}</td>
			<td>${gameInfo.score!}</td>
			<#if 1 == gameInfo.isRecommend>
				<td>是</td>
			<#else>
				<td>否</td>
			</#if>
			<td>${gameInfo.downloadCount!}</td>
			<td>${gameInfo.androidFileSize!}</td>
			<td>${gameInfo.iosFileSize!}</td>
			<td>
				<button edit="${gameInfo.id!}"  type="button" class="btn btn-info">编辑</button>
				<button remove="${gameInfo.id!}" type="button" class="btn btn-danger">删除</button>
			</td>
		</tr>
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
					<h4 class="modal-title">编辑广告</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post">
						<input type="hidden" name="id" />
						<div class="form-group">
							<label class="col-md-2 control-label">标题</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="title" value="">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">描述</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="describe" value="">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">类型</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="type" value="">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">网址</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="url" value="">
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
	
	// 显示模态框
	$('button[edit]').on('click', function(event) {
		var id = $(this).attr('edit');
		$.api('${ctx}/indexAdvertise/detail/' + id, {}, function(d) {
			var o = d.object;
			fillForm({
				title: o.title,
				describe: o.describe,
				type: o.type,
				url: o.url,
				id: id,
			});
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
			url:'${ctx}/indexAdvertise/save',
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
			$.api('${ctx}/indexAdvertise/delete/' + id, {}, function(d) {
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