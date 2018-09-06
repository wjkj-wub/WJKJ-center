<#import "/macros/pager.ftl" as p >
<html>
<head>
<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/simditor/css/simditor.css">
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/simditor/css/font-awesome.css">
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/simditor/css/simditor.scss">
<script type="text/javascript" editor-ctx="${ctx!}" src="${ctx!}/static/plugin/simditor/editor.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/module.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/hotkeys.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/uploader.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/simditor.min.js"></script>
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
			赛事资讯管理
		</li>
	</ul>
	
	<#-- 操作按钮 -->
	<div class="mb10">
		<button id="new-record" type="button" class="btn btn-success">新增记录</button>
	</div>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
			<div class="col-md-2">
				<input type="text" name="explain" value="${(params.explain)!}" class="form-control" placeholder="说明">
			</div>
			<div class="col-md-2">
				<input type="text" name="beginTime" value="${(params.beginTime)!}" class="form-control" placeholder="开始时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss'})">
			</div>
			<div class="col-md-2">
				<input type="text" name="endTime" value="${(params.endTime)!}" class="form-control" placeholder="结束时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss'})">
			</div>
			<button type="submit" class="btn btn-success">查询</button>
		</form>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>#</th>
			<th>图标</th>
			<th>标题</th>
			<th>操作</th>
		</tr>
		<#if list??>
		<#list list as o>
			<tr>
				<td>${o.id!}</td>
				<td><img src="${imgServer!}/${(o.icon_thumb)!}" style="width:35px;height:35px;" /></td>
				<td>${(o.title)!}</td>
				<td>
					<#if (o.is_subject)?? && o.is_subject == 1><a href="1?pid=${(o.id)!}" type="button" class="btn btn-info">查看子资讯</a></#if>
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
	<div id="modal-editor" class="modal fade">
		<div class="modal-dialog">
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
						<input type="hidden" name="pId" />
						<div class="form-group">
							<label class="col-md-2 control-label">选择赛事</label>
							<div class="col-md-6">
								<select name="activityId">
									<#if activities??>
									<#list activities as a>
										<option value="${a.id!}">${a.title!}</option>
									</#list>
									</#if>
								</select>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">标题</label>
							<div class="col-md-10">
								<input type="text" name="title" class="form-control" placeholder="标题"> 
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">图标</label>
							<div class="col-md-10">
								<input class="form-control" type="file" name="iconFile" value="" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">首图</label>
							<div class="col-md-10">
								<input class="form-control" type="file" name="coverFile" value="" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">是否专题</label>
							<div class="col-md-10">
								<label><input type="radio" name="isSubject" value="1" /> 是</label>
								<label><input type="radio" name="isSubject" value="0" /> 否</label>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">是否热门</label>
							<div class="col-md-10">
								<label><input type="radio" name="isHot" value="1" /> 是</label>
								<label><input type="radio" name="isHot" value="0" /> 否</label>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">是否显示</label>
							<div class="col-md-10">
								<label><input type="radio" name="isShow" value="1" /> 是</label>
								<label><input type="radio" name="isShow" value="0" /> 否</label>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">简述</label>
							<div class="col-md-10">
								<textarea class="form-control" name="brief" style="height:100px;"></textarea>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">详情</label>
							<div class="col-md-10">
								<textarea id="editor-detail" name="remark" autofocus></textarea>
								<script type="text/javascript">
									_editor = editor($('#editor-detail'));
								</script>
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
					url:'${ctx}/overactivity/save',
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
			$.api('${ctx}/overactivity/detail/' + id, {}, function(d) {
				var o = d.object;
				$.fillForm({
					iconFile: o.icon,
					coverFile: o.cover,
					title: o.title,
					brief: o.brief,
					isSubject: o.isSubject,
					isShow: o.isShow,
					isHot: o.isHot,
					pId: o.pId,
					id: o.id,
					activityId: o.activityId,
				}, _$editor);
				setEditorText($('#editor-detail'), o.remark);
			}, function(d) {
				alert('请求数据异常：' + d.result);
			}, {
				async: false,
			});
			
			showEditor('修改周红包');
		});
		
		// 新增
		$('#new-record').on('click', function() {
			$.fillForm({
				pId: '${pid!}'
			}, _$editor);
			showEditor('新增周红包');
		});
		
		// 删除
		$('button[remove]').on('click', function() {
			var id = $(this).attr('remove');
			$.confirm('确认删除吗?', function() {
				$.api('${ctx!}/overactivity/delete/' + id, {}, function(d) {
					window.location.reload();
				});
			});
		});
	</script>
</body>
</html>