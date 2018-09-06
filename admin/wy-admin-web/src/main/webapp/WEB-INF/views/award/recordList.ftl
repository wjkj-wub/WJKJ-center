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
			商品发放统计
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
			<input type="hidden" name="orderCol" value="${orderCol!}" />
			<input type="hidden" name="orderType" value="${orderType!}" />
			<div class="col-md-2" style="width:160px;">
				<input type="text" name="account" value="${(params.account)!}" class="form-control" placeholder="输入领奖者帐号">
			</div>
			<div class="col-md-2">
				<select class="form-control" name="activityId">
					<option value="">全部赛事</option>
					<#if activities??>
					<#list activities as a>
						<option value="${(a.id)!}"<#if (params.activityId)?? && (a.id)?? && a.id?string == params.activityId> selected</#if>>${(a.title)!}</option>
					</#list>
					</#if>
				</select>
			</div>
			<div class="col-md-2" style="width:160px;">
				<input type="text" name="startDate" value="${(params.startDate)!}" class="form-control" placeholder="提交时间(起)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:beginPick})">
			</div>
			<div class="col-md-2" style="width:160px;">
				<input type="text" name="endDate" value="${(params.endDate)!}" class="form-control" placeholder="提交时间(止)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:endPick})">
			</div>
			<button id="screen" class="btn btn-success">筛选</button>
			<a class="btn" href="1">清空</a>
			
			<button id="export-all" type="button" class="btn btn-success">导出</button>
			
			<div class="mb10">
				<div class="col-md-10" style="margin-bottom: 10px;">
					<input type="hidden" name="type" value="${(params.type)!}" />
					<input type="hidden" name="inventoryId" value="${(params.inventoryId)!}" />
					<div class="btn-group" data-toggle="buttons-checkbox">
						<button atype="1" type="button" class="btn <#if (params.type!) == "1">btn-danger<#else>btn-info</#if>">自有商品</button>
						<button atype="2" type="button" class="btn <#if (params.type!) == "2">btn-danger<#else>btn-info</#if>">第三方充值</button>
						<button atype="3" type="button" class="btn <#if (params.type!) == "3">btn-danger<#else>btn-info</#if>">库存商品</button>
					</div>
				</div>
			</div>
			<div class="mb10">
				<div class="col-md-10" style="margin-bottom: 10px;">
					<#if (params.type)??>
						<#if params.type == "1">
							<label><input type="radio" name="subType" value="" checked/> 全部</label>
							<label><input type="radio" name="subType" value="1"<#if (params.subType!) == "1"> checked</#if> /> 红包</label>
							<label><input type="radio" name="subType" value="2"<#if (params.subType!) == "2"> checked</#if> /> 金币</label>
						<#elseif params.type == "2">
							<label><input type="radio" name="subType" value="" checked /> 全部</label>
							<label><input type="radio" name="subType" value="3"<#if (params.subType!) == "3"> checked</#if> /> 话费</label>
							<label><input type="radio" name="subType" value="4"<#if (params.subType!) == "4"> checked</#if> /> 流量</label>
							<label><input type="radio" name="subType" value="5"<#if (params.subType!) == "5"> checked</#if> /> Q币</label>
						</#if>
					</#if>
				</div>
			</div>
			<script type="text/javascript">
				$('button[atype]').on('click', function(event) {
					var $form = $('#search');
					var type = $(this).attr('atype');
					var $type = $form.find('input[name="type"]');
					$type.val(type);
					$('input[name="subType"]:eq(0)').prop('checked', true);
					$form.submit();
				});
				$('input[name="subType"]').on('change', function(event) {
					$('#search').submit();
				});
				
				// 查询
				var $form = $('#search');
				$('#screen').on('click', function() {
					$form.prop('action', '${ctx}/award/record/list/1').prop('target', '_self').submit();
				});
				// 导出当前页
				$('#export-all').on('click', function() {
					$form.prop('action', '${ctx}/award/record/export/0').prop('target', '_blank').submit();
					$form.prop('action', '${ctx}/award/record/list/1').prop('target', '_self');
				});
			</script>
		</form>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>订单编号</th>
			<th>领奖者账号</th>
			<th>网娱账号</th>
			<th>赛事名</th>
			<th>奖品类别</th>
			<th>数量</th>
			<th>发放时间</th>
		</tr>
		<#if list??>
			<#list list as o>
				<tr>
					<td>${(o.serial)!}</td>
					<td>
					<#if (o.awardType)??>
						<#if o.awardType == 1>
							${(o.username)!}
						<#elseif o.awardType == 2>
							<#if o.awardSubType == 3>
								${(o.telephone)!}
							<#elseif o.awardSubType == 4>
								${(o.telephone)!}
							<#elseif o.awardSubType == 5>
								${(o.qq)!}
							</#if>
						<#elseif o.awardType == 3>
							${(o.telephone)!}
						</#if>
					</#if>
					</td>
					<td>${(o.username)!}</td>
					<td>${(o.activityTitle)!}</td>
					<td>
					<#if (o.awardType)??>
						<#if o.awardType == 1>
							<#if (o.awardSubType)??>
								<#if o.awardSubType == 1>
									红包
								<#elseif o.awardSubType == 2>
									金币
								</#if>
							</#if> (自有商品)
						<#elseif o.awardType == 2>
							<#if o.awardSubType == 3>
								话费
							<#elseif o.awardSubType == 4>
								流量
							<#elseif o.awardSubType == 5>
								Q币
							</#if> (第三方充值)
						<#elseif o.awardType == 3>
							${(o.awardTypeName)!} (库存商品)
						</#if>
					</#if>
					</td>
					<td>${(o.amount)!}</td>
					<td>${(o.createDate?string('yyyy-MM-dd HH:mm:ss'))!}</td>
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
								<input type="file" name="file" class="form-control" />
								<button type="button" class="btn btn-info" import>导入</button>
								<a href="${ctx}/static/example/amuse/库存商品导入模版.xls">下载模版</a>
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
	$('input[type="checkbox"][name="batch-delete"]').on('click', function(event) {
		$('input[type="checkbox"][name="delete"]').prop('checked', $(this).prop('checked'));
	});
	$('input[type="checkbox"][name="delete"]').on('click', function(event) {
		var cbs = $('input[type="checkbox"][name="delete"]');
		
		var isAllCheck = true;
		cbs.each(function() {
			if(!$(this).prop('checked')) {
				isAllCheck = false;
				return;
			}
		});
		
		$('input[type="checkbox"][name="batch-delete"]').prop('checked', isAllCheck);
	});
	
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
				}
			});
		}
	});
	
	// 批量删除
	$('button[batch-delete]').on('click', function(event) {
		var $this = $(this);
		$this.prop('disabled', true);
		$.confirm('确认批量删除吗?', function() {
			// 初始化表单数据
			var ids = '';
			$('input[type="checkbox"][name="delete"]:checked').each(function() {
				if(ids.length > 0) {
					ids += ',';
				}
				ids += $(this).val(); 
			});
			
			$('#modal-operating').modal('show');
			$.api('${ctx}/award/commodity/delete', {'ids': ids}, function(d) {
				window.location.reload();
			}, false, {
				async: true,
				complete: function() {
					$this.prop('disabled', false);
				}
			});
		});
	});
	
	// 批量删除
	$('button[delete]').on('click', function(event) {
		var $this = $(this);
		$this.prop('disabled', true);
		$.confirm('确认删除吗?', function() {
			// 初始化表单数据
			var id = $this.attr('delete');
			$.api('${ctx}/award/commodity/delete', {'ids': id}, function(d) {
				window.location.reload();
			}, false, {
				async: false,
				complete: function() {
					$this.prop('disabled', false);
				}
			});
		});
	});
	</script>
</body>
</html>