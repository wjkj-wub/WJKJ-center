<#import "/macros/pager.ftl" as p >
<html>
<head>
<script type="text/javascript" editor-ctx="${ctx!}" src="${ctx!}/static/plugin/editor.js"></script>
<link rel="stylesheet" href="${ctx}/static/lib/chosen/chosen.css" />
<script type="text/javascript" src="${ctx}/static/lib/chosen/chosen.min.js"></script>
<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
<style type="text/css">
.form-condensed .btn {
    padding: 5px 12px;
}
#imgs {
	max-height: 400px;
    overflow: auto;
    border-radius: 5px;
    border: solid 1px #CCC;
    padding: 10px;
}
#imgs img {
	width: 45%;
    margin: 0 10px 10px 0;
}
textarea {
	border-radius: 5px;
    border: solid 1px #CCC;
    padding: 10px;
}
</style>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>资讯管理</strong>
 		</li>
		<li class="active">
			专题资讯
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
		<div class="mb10">
			<input type="hidden" name="pid" value="${pid!}" />
			<div class="col-md-2">
				<input type="text" name="title" value="${(params.title)!}" class="form-control" placeholder="输入资讯标题">
			</div>
			<div class="col-md-2">
				<input type="text" name="beginDate" value="${(params.beginDate)!}" class="form-control" placeholder="发布时间(起)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:beginPick})">
			</div>
			<div class="col-md-2">
				<input type="text" name="endDate" value="${(params.endDate)!}" class="form-control" placeholder="发布时间(止)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:endPick})">
			</div>
			<button type="submit" class="btn btn-success">筛选</button>
			<a class="btn btn-info" href="1?pid=${pid!}">清空</a>
		</div>
		<script type="text/javascript">
			$('button[state]').on('click', function(event) {
				var $form = $('#search');
				$form.find('input[name="state"]').val($(this).attr('state'));
				$form.submit();
			});
		</script>
		</form>
	</div>
	<div class="mb10">
		<button add="" type="button" class="btn btn-success">新增资讯</button>
		<a class="btn btn-success" href="${ctx!}/activityInformation/subject/list/1">返回专题资讯</a>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>序号</th>
			<th>缩略图</th>
			<th>标题</th>
			<th>发布时间</th>
			<th>阅读数</th>
			<th>操作</th>
		</tr>
		<#if list??>
		<#list list as o>
			<tr>
				<td>${(o.id)!}</td>
				<td><#if (o.icon)??><img src="${imgServer!}${(o.icon)!}" style="width:30px;height:30px;" /></#if></td>
				<td>${(o.title)!}</td>
				<td>${(o.createDate?string('yyyy-MM-dd HH:mm:ss'))!}</td>
				<td>${(o.readNum)!}</td>
				<td>
					<button edit="${o.id!}" type="button" class="btn btn-info">编辑</button>
					<button remove="${o.id!}" type="button" class="btn btn-danger">删除</button>
				</td>
			</tr>
		</#list>
		</#if>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	
	<#-- 编辑，新增 -->
	<div id="modal-editor" class="modal fade">
		<div class="modal-dialog" style="width:800px;">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title">标题</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post" enctype="multipart/form-data">
						<input type="hidden" name="id" />
						<input type="hidden" name="pId" />
						<div class="form-group">
							<label class="col-md-2 control-label">资讯来源</label>
							<div class="col-md-10">
								<input type="text" class="form-control" name="source" wy-required="资讯来源" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">标题</label>
							<div class="col-md-10">
								<input type="text" class="form-control" name="title" wy-required="标题" maxlength="13" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">摘要</label>
							<div class="col-md-10">
								<input type="text" class="form-control" name="brief" wy-required="摘要" maxlength="26" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">详情</label>
							<div class="col-md-10">
								<textarea id="editor-remark" name="remark" style="width:100%;height:100px;" readonly></textarea>
								<script type="text/javascript">
									_editor = editor($('#editor-remark'));
								</script>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">缩略图</label>
							<div class="col-md-10">
								<input type="file" class="form-control" name="iconFile" accept="image/*" />
								<font class="prompt">
									请上传30:23宽高比的图片，以达到最佳显示效果
								</font>
							</div>
						</div>
						<@ec.videoCoverImgs editObj=editObj! imgServer=imgServer! />
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
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
	
	// 打开模态框
	function showEditor(title) {
		_$editor.find('.modal-title').html(title);
		_$editor.modal('show');
	}
	
	// 关闭模态框
	function hideEditor() {
		_$editor.modal('hide');
	}
	
	// 新增资讯
	$('button[add]').on('click', function(event) {
		// 初始化表单数据
		$.fillForm({
			pId: '${pid!}'
		}, _$editor);
		setEditorText($('#editor-remark'), '');
		showEditor('新增资讯');
	});
	
	// 显示模态框，编辑
	$('button[edit]').on('click', function(event) {
		// 初始化表单数据
		var id = $(this).attr('edit');
		$.api('${ctx}/activityInformation/subject/info/detail', {id: id}, function(d) {
			var o = d.object;
			$.fillForm({
				id: o.id,
				title: o.title,
				source: o.source,
				brief: o.brief,
				iconFile: o.icon,
				coverFile: o.cover,
				pId: '${pid!}'
			}, _$editor);
			
			// 初始化编辑器内容
			setEditorText($('#editor-remark'), o.remark);
			
			showEditor('编辑资讯');
		}, false, {
			async: false,
		});
	});
	
	// 提交表单
	_$editor.find('#submit').on('click', function(event) {
		var $form = _$editor.find('form');
		
		// 检查上传文件是否为图片格式
		var types = "image/png,image/jpeg,image/gif";
		var iconFiles = $form.find('[name="iconFile"]').get(0).files;
		if(iconFiles.length > 0) {
			for(var i=0; i<iconFiles.length; i++) {
				var file = iconFiles[i];
				if(types.indexOf(file.type) == -1) {
					alert('缩略图图片类型错误');
					$this.prop('disabled', false);
					return;
				}
			}
		}
		
		var valid = $form.formValid();
		if(valid) {
			var $this = $(this);
			$this.prop('disabled', true);
			$form.ajaxSubmit({
				url:'${ctx}/activityInformation/subject/info/save',
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
	$('button[remove]').on('click', function(event) {
		var id = $(this).attr('remove');
		$.confirm('确认删除吗?', function() {
			$.api('${ctx}/activityInformation/common/delete/' + id, {}, function(d) {
				if(d.code == 0) {
					window.location.reload();
				} else {
					alert(d.result);
				}
			});
		});
	});
	</script>
</body>
</html>