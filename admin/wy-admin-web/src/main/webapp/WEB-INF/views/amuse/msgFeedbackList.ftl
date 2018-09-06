<#import "/macros/pager.ftl" as p >
<html>
<head>
<link rel="stylesheet" href="${ctx}/static/lib/chosen/chosen.css" />
<script type="text/javascript" src="${ctx}/static/lib/chosen/chosen.min.js"></script>
<script type="text/javascript" editor-ctx="${ctx!}" src="${ctx!}/static/plugin/editor.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/ztree/js/jquery.ztree.all-3.5.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/ztree/js/jquery.ztree.exhide-3.5.min.js"></script>
<link rel="stylesheet" type="text/css" href="${ctx!}/static/plugin/ztree/css/zTreeStyle.css"/>
<style type="text/css">
.label {
	display: inline-block;
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
			<#if isAppSetting> 申诉原因管理 <#else> 赛事文案模板 </#if>
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
			<#if isAppSetting>
				<input type="hidden" name="isAppSetting" value="1" />
			</#if>
			<div class="col-md-2">
				<input type="text" class="form-control" name="content" placeholder="通知内容" value="${(params.content)!}" />
			</div>
			<div class="col-md-8">
				<button type="submit" class="btn btn-success" style="margin-left:10px;">查询</button>
				<a class="btn" href="1<#if isAppSetting>?isAppSetting=1</#if>">清空</a>
			</div>
			<div class="col-md-8">
				<button add="" type="button" class="btn btn-info">新增文案</button>
			</div>
		</form>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>通知内容</th>
			<th>新增时间</th>
			<th>操作</th>
		</tr>
		<#if list?? && list?size gt 0>
		<#list list as o>
			<tr>
				<td>${o.content!}</td>
				<td>${o.createDate!}</td>
				<td>
					<button edit="${(o.id)!}" type="button" class="btn btn-success">编辑</button>
					<button remove="${o.id!}" type="button" class="btn btn-danger">删除</button>
				</td>
			</tr>
		</#list>
		</#if>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	
	<#-- 编辑或新增 -->
	<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
	<div id="modal-editor" class="modal fade">
		<div class="modal-dialog" style="width:1000px">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title">编辑赛事</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post">
						<input type="hidden" name="id" />
						<#if isAppSetting>
							<input type="hidden" name="type" />
						<#else>
							<div class="form-group">
								<label class="col-md-2 control-label">类型</label>
								<div class="col-md-10">
								<label><input type="radio" name="type" value="1" /> 审核</label>
								<label><input type="radio" name="type" value="2" /> 发放</label>
								<label><input type="radio" name="type" value="3" /> 申诉</label>
								</div>
							</div>
						</#if>
						<div class="form-group">
							<label class="col-md-2 control-label">内容</label>
							<div class="col-md-10">
								<textarea class="form-control" name="content" maxlength="50"></textarea>
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
					<button id="submit" type="button" class="btn btn-primary">发布</button>
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
	
	// 打开模态框
	function showEditor(title) {
		if(title) {
			_$editor.find('.modal-title').html(title);
		}
		_$editor.modal('show');
	}
	
	// 关闭模态框
	function hideEditor() {
		_$editor.modal('hide');
	}
	
	// 显示模态框，编辑
	$('button[edit]').on('click', function(event) {
		// 初始化表单数据
		var id = $(this).attr('edit');
		$.api('${ctx}/amuse/feedback/detail/' + id, {}, function(d) {
			var o = d.object;
			$.fillForm({
				id: o.id,
				type: o.type,
				content: o.content,
			}, _$editor);
			
			showEditor('编辑文案');
		}, function(d) {
			alert('请求数据异常：' + d.result);
		}, {
			async: false,
		});
	});
	
	// 新增文案
	$('button[add]').on('click', function(event) {
		$.fillForm({
			type: <#if isAppSetting> 4 <#else> 1 </#if>
		}, _$editor);
		
		showEditor('新增文案');
	});
	
	// 提交表单
	_$editor.find('#submit').on('click', function() {
		var $form = _$editor.find('form');
		var valid = $form.formValid();
		if(valid) {
			$form.ajaxSubmit({
				url:'${ctx}/amuse/feedback/save',
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
	});
	
	// 删除
	$('button[remove]').on('click', function() {
		var id = $(this).attr('remove');
		$.confirm('确认删除吗?', function() {
			$.api('${ctx!}/amuse/feedback/delete/' + id, {}, function(d) {
				window.location.reload();
			});
		});
	});
	</script>
</body>
</html>