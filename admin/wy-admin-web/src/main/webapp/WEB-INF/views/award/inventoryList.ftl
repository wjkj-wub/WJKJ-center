<#import "/macros/pager.ftl" as p >
<html>
<head>
<link rel="stylesheet" href="${ctx}/static/lib/chosen/chosen.css" />
<script type="text/javascript" src="${ctx}/static/lib/chosen/chosen.min.js"></script>
<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>娱乐赛审核发放</strong>
 		</li>
		<li class="active">
			库存
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
			<div class="mb10">
				<input type="hidden" name="orderCol" value="${orderCol!}" />
				<input type="hidden" name="orderType" value="${orderType!}" />
				<div class="col-md-2">
					<input type="text" name="name" value="${(params.name)!}" class="form-control" placeholder="商品名">
				</div>
				<div class="col-md-2">
					<select class="form-control" name="valid">
						<option value="">全部状态</option>
						<option value="1"<#if (params.valid)?? && "1" == params.valid> selected</#if>>有效</option>
						<option value="0"<#if (params.valid)?? && "0" == params.valid> selected</#if>>无效</option>
					</select>
				</div>
				<div class="col-md-2">
					<select class="form-control" name="order">
						<option value="1"<#if (order)?? && "1" == order> selected</#if>>按导入时间先后排序</option>
						<option value="2"<#if (order)?? && "2" == order> selected</#if>>按剩余数量多少排序</option>
					</select>
				</div>
				<button id="screen" type="submit" class="btn btn-success">筛选</button>
				<a class="btn btn-info" href="1">清空</a>
			</div>
		</form>
	</div>
	<div class="mb10">
		<button new class="btn btn-success">新增商品</button>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>编号</th>
			<th>商品名</th>
			<th>剩余/总数</th>
			<th>有效期</th>
			<th>状态</th>
			<th>导入时间</th>
			<th>操作</th>
		</tr>
		<#if list??>
			<#list list as o>
				<tr>
					<td>${(o.id)!}</td>
					<td>${(o.name)!}</td>
					<td><a href="${ctx!}/award/commodity/list/1?inventoryId=${(o.id)!}">${(o.unusedCount)!}/${(o.totalCount)!}</a></td>
					<td>${(o.startTime?string('yyyy-MM-dd hh:mm:ss'))!} 至 ${(o.endTime?string('yyyy-MM-dd hh:mm:ss'))!}</td>
					<td>${((o.valid == 1 && .now gte o.startTime && .now lt o.endTime)?string('有效', '无效'))!}</td>
					<td>${(o.importTime?string('yyyy-MM-dd hh:mm:ss'))!}</td>
					<td>
						<button edit="${(o.id)!}" type="button" class="btn btn-info">编辑</button>
						<#if (o.valid)?? && o.valid == 1>
							<button disable="${(o.id)!}" type="button" class="btn btn-danger">禁用</button>
						<#else>
							<button enabled="${(o.id)!}" type="button" class="btn btn-success">启用</button>
						</#if>
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
							<label class="col-md-2 control-label">商品名</label>
							<div class="col-md-10">
								<input type="text" name="name" class="form-control" wy-required="商品名" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">有效期</label>
							<div class="col-md-10">
								<input type="text" class="form-control" id="startTime" name="startTimeStr" placeholder="开始时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss',maxDate:'#F{$dp.$D(\'endTime\',{d:0});}'})" wy-required="开始时间" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label"></label>
							<div class="col-md-10">
								<input type="text" class="form-control" id="endTime" name="endTimeStr" placeholder="结束时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss',minDate:'#F{$dp.$D(\'startTime\',{d:0});}'})" wy-required="结束时间" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">商品导入</label>
							<div class="col-md-10">
								<input type="file" name="file" class="form-control" accept=".xls" />
								<button type="button" class="btn btn-success" import data-toggle="tooltip" data-placement="top" title="" data-original-title="请将excel文件保存为Microsoft Excel 97(*.xls)文件">导入</button>
								<button type="button" class="btn btn-info" onclick="window.open('${ctx}/static/example/amuse/库存商品导入模版.xls')" data-toggle="tooltip" data-placement="top" title="" data-original-title="请将excel文件保存为Microsoft Excel 97(*.xls)文件">下载模版</button>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">导入数量</label>
							<div class="col-md-10">
								<input type="text" name="totalCount" class="form-control" readonly />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">剩余数量</label>
							<div class="col-md-10">
								<input type="text" name="unusedCount" class="form-control" readonly />
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button id="submit" type="button" class="btn btn-success">保存</button>
					<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
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
	
	<#if Session.user.userType == 1>
	<!-- 手动分配审核 -->
	<div id="modal-allot" class="modal fade">
		<div class="modal-dialog" style="width:700px;">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title">分发</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post" enctype="multipart/form-data">
						<div class="form-group">
							<label class="col-md-2 control-label">分发账号</label>
							<div class="col-md-10">
								<select class="form-control" name="sysUserId">
								<#if verifyUsers??>
								<#list verifyUsers as u>
									<option value="${(u.id)!}">${(u.realname)!}</option>
								</#list>
								</#if>
								</select>
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button id="submit" type="button" class="btn btn-success" data-dismiss="modal">确认分发</button>
					<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
				</div>
			</div>
		</div>
	</div>
	</#if>
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
	
	// 新增
	$('button[new]').on('click', function(event) {
		$.fillForm({}, _$editor);
		showEditor('新增');
	});
	
	// 编辑
	$('button[edit]').on('click', function(event) {
		// 初始化表单数据
		var id = $(this).attr('edit');
		$.api('${ctx}/award/inventory/detail', {id: id}, function(d) {
			var o = d.object;
			
			var startTimeStr = '', endTimeStr = '';
			if(o.startTime) {
				startTimeStr = new Date(o.startTime).Format('yyyy-MM-dd hh:mm:ss');
			}
			if(o.endTime) {
				endTimeStr = new Date(o.endTime).Format('yyyy-MM-dd hh:mm:ss');
			}
			
			$.fillForm({
				id: o.id,
				name: o.name,
				startTimeStr: startTimeStr,
				endTimeStr: endTimeStr,
				totalCount: o.totalCount,
				unusedCount: o.unusedCount
			}, _$editor);
			
			_$editor.find('button[import]').parents('.form-group').show();
			showEditor('编辑');
		}, function(d) {
			alert('请求数据异常：' + d.result);
		}, {
			async: false,
		});
	});
	
	// 提交表单
	_$editor.find('#submit').on('click', function(event) {
		var $form = _$editor.find('form');
		if($form.formValid()) {
			var $this = $(this);
			$this.prop('disabled', true);
			
			var $import = _$editor.find('button[import]');
			$import.prop('disabled', true);
			
			var id = $form.find('input[name="id"]').val();
			
			$form.ajaxSubmit({
				url:'${ctx}/award/inventory/save',
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
					$import.prop('disabled', false);
				}
			});
		}
	});
	
	// 禁用
	$('button[disable]').on('click', function(event) {
		var $this = $(this);
		$this.prop('disabled', true);
		$.confirm('确认禁用吗?', function() {
			// 初始化表单数据
			var id = $this.attr('disable');
			$.api('${ctx}/award/inventory/disabled', {'id': id}, function(d) {
				window.location.reload();
			}, false, {
				async: false,
				complete: function() {
					$this.prop('disabled', false);
				}
			});
		});
	});
	
	// 启用
	$('button[enabled]').on('click', function(event) {
		var $this = $(this);
		$this.prop('disabled', true);
		$.confirm('确认启用吗?', function() {
			// 初始化表单数据
			var id = $this.attr('enabled');
			$.api('${ctx}/award/inventory/enabled', {'id': id}, function(d) {
				window.location.reload();
			}, false, {
				async: false,
				complete: function() {
					$this.prop('disabled', false);
				}
			});
		});
	});
	
	// 导入
	$('button[import]').on('click', function(event) {
		var $this = $(this);
		var $submit = _$editor.find('#submit');
		$submit.prop('disabled', true);
		
		var $form = _$editor.find('form');
		if($form.formValid()) {
			$.confirm('确认导入吗?', function() {
				$this.prop('disabled', true).html('导入中..');
				var $form = _$editor.find('form');
				$form.ajaxSubmit({
					url:'${ctx}/award/inventory/import',
				    type:'post',
				    success: function(d){
				    	if(d.code == 0) {
				    		alert('导入完成');
				    		window.location.reload();
				    	} else {
				    		alert(d.result);
				    	}
					},
					complete: function() {
						$this.prop('disabled', false).html('导入');
						$submit.prop('disabled', false);
					}
				});
			}, function() {
				$this.prop('disabled', false).html('导入');
				$submit.prop('disabled', false);
			});
		}
	});
	</script>
</body>
</html>