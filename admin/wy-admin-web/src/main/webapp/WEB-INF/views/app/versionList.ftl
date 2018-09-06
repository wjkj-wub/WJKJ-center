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
			APP版本管理
		</li>
	</ul>
	
	<#-- 操作按钮 -->
	<div class="mb10">
		<button id="new-record" type="button" class="btn btn-success">新增记录</button>
	</div>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
		</form>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>#</th>
			<th>版本号</th>
			<th>地址</th>
			<th>是否强制</th>
			<th>平台</th>
			<th>版本（Android）</th>
			<th>隐藏板块（IOS）</th>
			<th>操作</th>
		</tr>
		<#list list as o>
			<tr>
				<td>${o.id!}</td>
				<td>${o.version!}</td>
				<td>${o.url!}</td>
				<td><#if ((o.isCoercive)!0) == 1>是<#else>否</#if></td>
				<td><#if (o.type)??><#if o.type == 1>Android<#elseif o.type == 2>IOS</#if></#if></td>
				<td>${o.versionCode!}</td>
				<td><#if (o.hiddenElement)??><#if o.hiddenElement == 1>是<#else>否</#if></#if></td>
				<td>
					<button edit="${o.id!}" type="button" class="btn btn-info">编辑</button>
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
		<div class="modal-dialog" style="width: 800px;">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideEditor()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title">红包设置</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post">
						<input type="hidden" name="id" />
						<div class="form-group">
							<label class="col-md-2 control-label">版本号</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="version" value="" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">地址</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="url" value="" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">是否强制</label>
							<div class="col-md-10">
								<label><input type="radio" name="isCoercive" value="0" /> 否</label>
								<label><input type="radio" name="isCoercive" value="1" /> 是</label>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">平台</label>
							<div class="col-md-10">
								<label><input type="radio" name="type" value="1" /> Android</label>
								<label><input type="radio" name="type" value="2" /> IOS</label>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">版本（Android）</label>
							<div class="col-md-10">
								<input class="form-control" type="number" name="versionCode" value="" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">隐藏板块（IOS）</label>
							<div class="col-md-10">
								<label><input type="radio" name="hiddenElement" value="0" /> 否</label>
								<label><input type="radio" name="hiddenElement" value="1" /> 是</label>
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
		
		// 提交表单
		function submitEditor() {
			var $form = _$editor.find('form');
			var valid = $form.formValid();
			if(valid) {
				$form.ajaxSubmit({
					url:'${ctx}/app/version/save',
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
		// 编辑
		$('button[edit]').on('click', function(event) {
			// 初始化表单数据
			var id = $(this).attr('edit');
			$.api('${ctx}/app/version/detail/' + id, {}, function(d) {
				var o = d.object;
				$.fillForm({
					version: o.version,
					url: o.url,
					isCoercive: o.isCoercive,
					type: o.type,
					versionCode: o.versionCode,
					hiddenElement: o.hiddenElement,
					id: o.id,
				}, _$editor);
			}, function(d) {
				alert('请求数据异常：' + d.result);
			}, {
				async: false,
			});
			
			showEditor('修改版本');
		});
		
		// 新增
		$('#new-record').on('click', function() {
			$.fillForm({
				type: 3,
				restrict: 0,
			}, _$editor);
			showEditor('新增版本');
		});
		
		// 删除
		$('button[remove]').on('click', function() {
			var id = $(this).attr('remove');
			$.confirm('确认删除吗?', function() {
				$.api('${ctx!}/app/version/delete/' + id, {}, function(d) {
					window.location.reload();
				});
			});
		});
	</script>
</body>
</html>