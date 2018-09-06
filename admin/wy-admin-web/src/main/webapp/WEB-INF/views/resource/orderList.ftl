<#import "/macros/pager.ftl" as p >
<html>
<head>
<link rel="stylesheet" href="${ctx}/static/lib/chosen/chosen.css" />
<script type="text/javascript" src="${ctx}/static/lib/chosen/chosen.min.js"></script>
<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
<style type="text/css">
button[levels] {
	width: 86px;
}
button[remarks], button[comments] {
	width: 114px;
}
.table-striped > tbody > tr.nearOverdue > td, .table-striped > tbody > tr.nearOverdue > th {
	background-color: #FFCECF;
}
</style>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>资源商城</strong>
 		</li>
		<li class="active">
			订单管理
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
		<div class="mb10">
			<div class="mb10 col-md-10 container">
				<div class="col-md-2">
					<input class="form-control" name="query" placeholder="商品名称/网吧名称/业主手机" value="${(params.query)!}" />
				</div>
				<div class="col-md-2">
					<input type="text" id="param-beginDate" name="beginDate" value="${(params.beginDate)!}" class="form-control" placeholder="提交时间(起)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',maxDate:'#F{$dp.$D(\'param-endDate\',{d:0});}'})">
				</div>
				<div class="col-md-2">
					<input type="text" id="param-endDate" name="endDate" value="${(params.endDate)!}" class="form-control" placeholder="提交时间(止)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',minDate:'#F{$dp.$D(\'param-beginDate\',{d:0});}'})">
				</div>
				<div class="col-md-2">
					<a class="btn btn-info" href="1">清空</a>
					<button id="screen" type="button" class="btn btn-success">筛选</button>
				</div>
			</div>
			<div class="mb10 col-md-10 container">
				<div class="col-md-2">
					<select class="form-control" name="status">
						<option value="">全部状态</option>
						<option value="-1"<#if (params.status!) == "-1"> selected</#if>>订单取消</option>
						<option value="0"<#if (params.status!) == "0"> selected</#if>>未确认</option>
						<option value="1"<#if (params.status!) == "1"> selected</#if>>服务确认</option>
						<option value="2"<#if (params.status!) == "2"> selected</#if>>网吧确认</option>
						<option value="3"<#if (params.status!) == "3"> selected</#if>>订单成功</option>
						<#-- <option value="4"<#if (params.status!) == "4"> selected</#if>>订单成功</option> -->
					</select>
				</div>
				<div class="col-md-2">
					<button id="export-all" type="button" class="btn btn-success">导出</button>
				</div>
			</div>
			<div class="mb10 col-md-10 container">
				<div class="col-md-4">
					<input type="hidden" name="state" value="${(params.state)!}" />
					<input type="hidden" name="hasRemark" value="${(params.hasRemark)!}" />
					<div class="btn-group" data-toggle="buttons-checkbox">
						<button remarks="" type="button" class="btn <#if (params.hasRemark!) == "">btn-danger<#else>btn-info</#if>">全部</button>
						<button remarks="1" type="button" class="btn <#if (params.hasRemark!) == "1">btn-danger<#else>btn-info</#if>">有备注</button>
						<button remarks="0" type="button" class="btn <#if (params.hasRemark!) == "0">btn-danger<#else>btn-info</#if>">无备注</button>
					</div>
				</div>
			</div>
		</div>
		<script type="text/javascript">
			// 选项点击事件
			$('button[levels], button[comments], button[remarks]').on('click', function(event) {
				var $form = $('#search');
				var levels = $(this).attr('levels'), comments = $(this).attr('comments'), remarks = $(this).attr('remarks');
				if(typeof(levels) !== 'undefined') {
					$form.find('input[name="levels"]').val(levels);
				} else if(typeof(comments) !== 'undefined') {
					$form.find('input[name="hasComment"]').val(comments);
				} else {
					$form.find('input[name="hasRemark"]').val(remarks);
				}
				$form.prop('action', '${ctx}/netbar/resource/order/list/1').prop('target', '_self').submit();
				$form.submit();
			});
			
			// 查询
			var $form = $('#search');
			$('#screen').on('click', function() {
				$form.prop('action', '${ctx}/netbar/resource/order/list/1').prop('target', '_self').submit();
			});
			// 导出当前页
			$('#export-all').on('click', function() {
				$form.prop('action', '${ctx}/netbar/resource/order/export/0').prop('target', '_blank').submit();
			});
		</script>
		</form>
	</div>
	
	<#-- 统计信息 -->
	<div class="mb10">
		<div class="col-md-10">
			总计网吧数量：${(statis.netbarCount)!0}家,总计销售金额：${(statis.sumTotalAmount)!0}元,总计订单笔数：${(statis.count)!0}笔
		</div>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>订单编号</th>
			<th>订单时间</th>
			<th>购买项目</th>
			<th>总购买金额</th>
			<th>使用资金</th>
			<th>使用配额</th>
			<th>购买网吧ID</th>
			<th>购买网吧</th>
			<th>业主电话</th>
			<th>购买场次/数量</th>
			<th>所在地区</th>
			<th>网吧级别</th>
			<th>对接人</th>
			<th>对接人电话</th>
			<th>状态</th>
			<th>操作</th>
		</tr>
		<#if list??>
		<#list list as o>
			<tr<#if o.nearOverdue == 1> class="nearOverdue"</#if>>
				<td>${(o.tradeNo)!}</td>
				<td>${(o.createDate?string('yyyy-MM-dd HH:mm:ss'))!}</td>
				<td>${(o.propertyName)!}</td>
				<td>${(o.totalAmount)!}</td>
				<td>${(o.amount)!}</td>
				<td>${(o.quotaAmount)!}</td>
				<td>${(o.netbarId)!}</td>
				<td>${(o.netbarName)!}</td>
				<td>${(o.ownerTelephone)!}</td>
				<td>
					<#if (o.cateType!1) == 0>
						${(o.serveDate?string('yyyy-MM-dd HH:mm:ss'))!}
					<#else>
						${(o.buyNum)!}
					</#if>
				</td>
				<td>
					<#if (o.provinceAreaCode)??>
						<#if o.netbarAreaCode == '000000'>
							全国
						<#else>
							${(o.provinceName)!}
						</#if>
					</#if>
				</td>
				<td>
					<#if (o.levels)??>
						<#if o.levels == 0>
							非会员
						<#elseif o.levels == 1>
							会员
						<#elseif o.levels == 2>
							黄金
						</#if>
					</#if>
				</td>
				<td>${(o.executes)!}</td>
				<td>${(o.executePhone)!}</td>
				<td>
					<#if (o.status)??>
						<#if o.status == -2>
							订单过期
						<#elseif o.status == -1>
							订单取消
						<#elseif o.status == 0>
							双方未确认
						<#elseif o.status == 1>
							服务确认
						<#elseif o.status == 2>
							网吧确认
						<#elseif o.status == 3>
							订单成功
						<#elseif o.status == 4>
							订单成功
						<#else>
							${(o.status)!}
						</#if>
					</#if>
				</td>
				<td>
					<button add-remarks="${(o.id)!}" remark="${(o.remarks)!}" type="button" class="btn btn-info">
						<#if !o.remarks?? || o.remarks?length lte 0>
							加备注
						<#else>
							查看备注
						</#if>
					</button>
					<button check-comments="${(o.id)!}" comment="${(o.comments)!}" type="button" class="btn btn-info">看评论</button>
					<#if o.status != -1 && o.status != -2>
						<button cancel="${(o.id)!}" type="button" class="btn btn-success">取消订单</button>
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
						<div class="form-group" id="remarks">
							<label class="col-md-2">备注</label>
							<div class="col-md-8">
								<input class="form-control" type="text" name="remarks" placeholder="请输入备注" maxlength="255" />
							</div>
						</div>
						<div class="form-group" id="comments">
							<label class="col-md-2">评论</label>
							<div class="col-md-8">
								<input class="form-control" type="text" name="comments" placeholder="暂无评论" maxlength="255" disabled />
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button id="submit" type="button" class="btn btn-success">确认</button>
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
	
	// 加备注
	$('button[add-remarks]').on('click', function(event) {
		var id = $(this).attr('add-remarks'), remarks = $(this).attr('remark');
		$.fillForm({
			'id': id,
			'remarks': remarks,
		}, _$editor);
		
		_$editor.find('#remarks').show().siblings('#comments').hide();
		_$editor.find('#submit').show();
		showEditor('加备注');
	});
	
	// 查看评论
	$('button[check-comments]').on('click', function(event) {
		var comments = $(this).attr('comment');
		$.fillForm({
			comments: comments,
		}, _$editor);
		
		_$editor.find('#remarks').hide().siblings('#comments').show();
		_$editor.find('#submit').hide();
		showEditor('看评论');
	});
	
	// 处理
	$('button[verify]').on('click', function(event) {
		var id = $(this).attr('verify');
		showDeal(id);
		_$editor.find('#div-operate').show();
		_$editor.find('#submit').show();
	});
	
	// 提交表单
	_$editor.find('#submit').on('click', function(event) {
		var $this = $(this);
		$this.prop('disabled', true);
		var $form = _$editor.find('form');
		$form.ajaxSubmit({
			url:'${ctx}/netbar/resource/order/addRemarks',
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
	});
	
	// 取消订单
	$('button[cancel]').on('click', function(event) {
		var id = $(this).attr('cancel');
		if(typeof(id) === 'undefined' || id.length <= 0) {
			alert('参数错误');
			return;
		}
		$.confirm('确认取消订单吗?', function(event) {
			$.api('${ctx}/netbar/resource/order/cancel/' + id, {}, function(d) {
				window.location.reload();
			});
		});
	});
	</script>
</body>
</html>