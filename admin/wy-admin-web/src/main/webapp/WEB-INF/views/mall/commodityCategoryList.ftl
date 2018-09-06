<#import "/macros/pager.ftl" as p >
<html>
<head>
<link rel="stylesheet" href="${ctx}/static/lib/chosen/chosen.css" />
<script type="text/javascript" src="${ctx}/static/lib/chosen/chosen.min.js"></script>
<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
<style type="text/css">
.form-condensed .form-control {
	margin-top: 3px;
}
input[disabled] {
    background-color: #FFFFFF!important;
    -webkit-box-shadow: inset 0 1px 1px rgba(0, 0, 0, .075);
    box-shadow: inset 0 1px 1px rgba(0, 0, 0, 0);
    border: 0;
}
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
	width: 30%;
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
  			<strong>系统管理</strong>
 		</li>
		<li class="active">
			奖品类别
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
		<div class="mb10">
			<div class="col-md-2">
				<input type="text" name="name" value="${(params.name)!}" class="form-control" placeholder="输入奖品类别">
			</div>
			<button type="submit" class="btn btn-success">查询</button>
			<a class="btn btn-info" href="1">清空</a>
		</div>
		</form>
	</div>
	
	<div class="mb10">
		<button new-record class="btn btn-success">新增类别</button>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>序号</th>
			<th>奖品类别</th>
		</tr>
		<#if list??>
		<#list list as o>
			<tr>
				<td>${(o.id)!}</td>
				<td>${(o.name)!}</td>
			</tr>
		</#list>
		</#if>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	
	<#-- 编辑，新增 -->
	<div id="modal-editor" class="modal fade">
		<div class="modal-dialog" style="width:700px;">
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
						<div class="form-group">
							<label class="col-md-2 control-label">奖品类别</label>
							<div class="col-md-10">
								<input type="text" name="name" class="form-control" maxlength="10" placeholder="输入奖品类别（限定10个字）" />
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button id="submit" type="button" class="btn btn-success">确认审核</button>
					<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
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
	
	// 显示模态框，编辑
	$('button[new-record]').on('click', function(event) {
		$.fillForm({}, _$editor);
		showEditor('新增奖品类别');
	});
	
	// 提交表单
	_$editor.find('#submit').on('click', function(event) {
		var $form = _$editor.find('form');
		var valid = $form.formValid();
		if(valid) {
			var $this = $(this);
			$this.prop('disabled', true);
			
			$form.ajaxSubmit({
				url:'${ctx}/commodityCategory/save',
			    type:'post',
			    success: function(d){
			    	if(d.code == 0) {
			    		window.location.reload();
			    	} else {
			    		alert(d.result);
			    	}
				},
				complete: function() {
					$this.prop('disabled', false);
				}
			});
		}
	});
	</script>
</body>
</html>