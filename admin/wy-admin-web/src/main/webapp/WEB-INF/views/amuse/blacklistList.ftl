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
  			<strong>娱乐赛审核发放</strong>
 		</li>
		<li class="active">
			黑名单
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
		<div class="mb10">
			<div class="col-md-2">
				<input type="text" name="account" value="${(params.account)!}" class="form-control" placeholder="输入领奖者号码">
			</div>
			<div class="col-md-2">
				<input type="text" name="username" value="${(params.username)!}" class="form-control" placeholder="输入网娱账号">
			</div>
			<div class="col-md-2">
				<input type="text" id="search-beginDate" name="beginDate" value="${(params.beginDate)!}" class="form-control" placeholder="提交时间(起)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',maxDate:'#F{$dp.$D(\'search-endDate\',{d:0});}'})">
			</div>
			<div class="col-md-2">
				<input type="text" id="search-endDate" name="endDate" value="${(params.endDate)!}" class="form-control" placeholder="提交时间(止)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',minDate:'#F{$dp.$D(\'search-beginDate\',{d:0});}'})">
			</div>
			<button type="submit" class="btn btn-success">筛选</button>
			<a class="btn btn-info" href="1">清空</a>
		</div>
		</form>
	</div>
	
	<!-- 批量审核 -->
	<div class="mb10">
		<div class="col-md-10">
			<button class="btn btn-danger" onclick="multiDelete()">批量删除</button>
			<button class="btn btn-success" onclick="multiRecover()">批量恢复</button>
			<button class="btn btn-info" onclick="showEditor('添加用户')">黑名单处理</button>
		</div>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>
				<input id="checkAll" type="checkbox" />
			</th>
			<th>序号</th>
			<th>用户手机号</th>
			<th>操作时间</th>
			<th>操作</th>
		</tr>
		<#if list??>
			<#list list as o>
				<tr>
					<td>
						<input type="checkbox" name="id" value="${(o.id)!}" />
					</td>
					<td>${(o.id)!}</td>
					<td><a href="${ctx!}/amuse/blackList/prize/list/1?userId=${(o.userId)!}">${(o.username)!}</a></td>
					<td>${(o.createDate?string('yyyy-MM-dd HH:mm:ss'))!}</td>
					<td>
						<button delete="${(o.id)!}" type="button" class="btn btn-danger">删除</button>
						<button recover="${(o.id)!}" type="button" class="btn btn-success">恢复</button>
					</td>
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
							<label class="col-md-2 control-label"></label>
							<div class="col-md-10">
								<textarea class="form-control" name="usernames" placeholder="添加用户手机号码,支持多个号码之间用逗号隔开"></textarea>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">用户导入</label>
							<div class="col-md-10">
								<input type="file" class="form-control" name="file" accept=".xls"  />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label"></label>
							<div class="col-md-10">
								<button class="btn btn-info" type="button" onclick="importExcel()" data-toggle="tooltip" data-placement="top" title="" data-original-title="请将excel文件保存为Microsoft Excel 97(*.xls)文件">点击导入</button>
								<button type="button" class="btn btn-info" onclick="window.open('${ctx}/static/example/amuse/黑名单用户导入模板.xls')" data-toggle="tooltip" data-placement="top" title="" data-original-title="请将excel文件保存为Microsoft Excel 97(*.xls)文件">下载模版</button>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">导入数量</label>
							<div class="col-md-10">
								<input class="form-control" name="importNum" readonly />
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button id="submit" type="button" class="btn btn-success">确定</button>
					<button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
				</div>
			</div>
		</div>
	</div>
	
	<!-- 处理中提示框 -->
	<div id="modal-operating" class="modal fade">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">提示</div>
				<div class="modal-body" style="text-align:center;">
					<div><img src="${ctx}/static/images/loading.gif" /></div>
					处理中，请勿中断 ...
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
	// 初始化操作中提示框
	$('#modal-operating').modal({
		keyboard : false,
		show : false,
		backdrop : 'static'
	})
	
	// 监听列表复选按钮事件
	$('#checkAll').on('click', function(event) {
		$('input[type="checkbox"][name="id"]').prop('checked', $(this).prop('checked'));
	});
	$('input[type="checkbox"][name="id"]').on('click', function(event) {
		var cbs = $('input[type="checkbox"][name="id"]');
		
		var isAllCheck = true;
		cbs.each(function() {
			if(!$(this).prop('checked')) {
				isAllCheck = false;
				return;
			}
		});
		
		$('#checkAll').prop('checked', isAllCheck);
	});
	
	// 初始化tooltip
	$('button[data-toggle="tooltip"]').tooltip();
	
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
	
	// 通过文本导入
	_$editor.find('#submit').on('click', function(event) {
		var $this = $(this);
		$this.prop('disabled', true);
		
		_$editor.find('form').ajaxSubmit({
			url:'${ctx}/amuse/blackList/import',
		    type:'post',
		    success: function(d){
		    	if(d.code == 0) {
		    		alert('已导入' + d.object + '个用户');
		    		window.location.reload();
		    	} else {
		    		alert(d.result);
		    	}
			}
		});
	});
	
	// 通过excel导入
	function importExcel() {
		var $form = _$editor.find('form');
		$form.ajaxSubmit({
			url:'${ctx}/amuse/blackList/importExcel',
		    type:'post',
		    success: function(d){
		    	if(d.code == 0) {
		    		$form.find('input[name="importNum"]').val(d.object);
		    		alert('已导入' + d.object + '个用户');
		    		window.location.reload();
		    	} else {
		    		alert(d.result);
		    	}
			},
			complete: function() {
				$this.prop('disabled', false).html('导入');
			}
		});
	}
	
	// 将勾选审核ID组合成id参数
	function genIdsParam(params) {
		if(typeof(params) !== 'string') {
			params = '';
		}
		$('input[type="checkbox"][name="id"]:checked').each(function(event) {
			if(params.length > 0) {
				params += '&';
			}
			params += 'ids=' + $(this).val();
		});
		return params;
	}
	
	// 恢复
	$('button[recover]').on('click', function(event) {
		var id = $(this).attr('recover');
		var params = 'ids=' + id;
		$.confirm('确认恢复吗?', function() {
			$('#modal-operating').modal('show');
			$.api('${ctx}/amuse/blackList/recover', params, function(d) {
				window.location.reload();
			}, false, {
				complete: function() {
					$this.prop('disabled', false);
				}
			});
		});
	});
	
	// 批量恢复
	function multiRecover() {
		var params = genIdsParam();
		if(params.length <= 0) {
			alert('请选择要操作的审核项');
			return;
		}
		
		$.confirm('确认操作吗?', function() {
			$('#modal-operating').modal('show');
			$.api('${ctx}/amuse/blackList/recover', params, function(d) {
				window.location.reload();
			}, false, {});
		});
	}
	
	// 删除
	$('button[delete]').on('click', function(event) {
		var id = $(this).attr('delete');
		var params = 'ids=' + id;
		$.confirm('确认删除吗?', function() {
			$('#modal-operating').modal('show');
			$.api('${ctx}/amuse/blackList/delete', params, function(d) {
				window.location.reload();
			}, false, {
				complete: function() {
					$this.prop('disabled', false);
				}
			});
		});
	});
	
	// 批量删除
	function multiDelete() {
		var params = genIdsParam();
		if(params.length <= 0) {
			alert('请选择要操作的审核项');
			return;
		}
		
		$.confirm('确认操作吗?', function() {
			$('#modal-operating').modal('show');
			$.api('${ctx}/amuse/blackList/delete', params, function(d) {
				window.location.reload();
			}, false, {
			});
		});
	}
	</script>
</body>
</html>