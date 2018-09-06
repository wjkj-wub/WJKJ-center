<#import "/macros/pager.ftl" as p >
<html>
<head>
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/simditor/css/simditor.css">
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/simditor/css/font-awesome.css">
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/simditor/css/simditor.scss">
<script type="text/javascript" editor-ctx="${ctx!}" src="${ctx!}/static/plugin/simditor/editor.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/module.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/hotkeys.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/uploader.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/simditor.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
<style>
	ul, li {
		list-style: none;
		margin: 0;
		padding: 0;
	}
	div[div-round] .btn {
		margin-bottom: 10px;
	}
	#cascader > div, #preview > div {
		min-width: 200px;
		max-width: 400px;
		float: left;
	}
	ul.selected {
		margin-left: 10px;
		max-height: 600px;
		overflow: auto;
	}
	ul.selected li {
		padding: 5px;
		border: solid 1px #CCC;
		border-radius: 3px;
		margin: 5px 0 0 2px;
		position: relative;
		box-shadow: inset 0 1px 1px rgba(0,0,0,0.075);
		-moz-box-shadow: inset 0 1px 1px rgba(0,0,0,0.075);
		-webkit-box-shadow: inset 0 1px 1px rgba(0,0,0,0.075);
		padding-right: 25px;
	}
	.prompt {
		height: 30px;
		padding: 0 10px;
		line-height: 30px;
	}
	a.remove {
		opacity: 0.6;
		filter: alpha(opacity=60);
		display: inline-block;
		width: 16px;
		height: 16px;
		margin: 0 5px 0 0;
		position: absolute;
		right: 0;
		top: 5px;
	}
	a.remove:hover {
		opacity: 1;
		filter: alpha(opacity=100);
	}
	#preview {
		box-shadow: rgb(204, 204, 204) 0px 0px 5px 0px;
		-moz-box-shadow: rgb(204, 204, 204) 0px 0px 5px 0px;
		-webkit-box-shadow: rgb(204, 204, 204) 0px 0px 5px 0px;
		position: absolute;
		border: solid 1px #CCC;
		border-radius: 5px;
		background-color: #FFF;
		z-index: 1100;
		padding: 10px;
		display: none;
	}
	div[div-round] {
	    border-top: solid 10px #E9E9E9;
	    border-top-style: solid;
	    padding-top: 10px;
        margin-bottom: 0;
	}
	tr[round] {
		border-top: solid 10px #E9E9E9;
		border-top-style: solid;
	}
	.a-btn {
		display: inline-block;
		font-size: 20px;
	}
	.panel-tool-close {
	    background: url('${ctx!}/static/images/panel_tools.png') no-repeat -16px 0px;
	}
	#input-search {
		width: initial;
		min-width: 200px;
		max-width: 400px;
		display: inline-block;
	}
</style>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li> 
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>订单管理</strong>
 		</li>
		<li class="active">
			${topShow!}
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="${ctx}/order/list/1" method="post" style="margin:0px;display: inline">
			<div class="col-md-2">
				<input type="text" class="form-control" name="phone" placeholder="用户手机" value="${phone!}" />
			</div>
			<div class="col-md-2">
				<input type="text" class="form-control" name="nickname" placeholder="用户昵称" value="${nickname!}" />
			</div>
			<div class="col-md-2">
				<input type="text" class="form-control" name="netbarName" placeholder="网吧名称" value="${netbarName!}" />
			</div>
			<div class="col-md-2">
				<select name="status" class="form-control">
				<option value="">全部</option>
				<option value="-1" <#if status?exists&&status=-1>selected</#if>>支付失败</option>
				<option value="0" <#if status?exists&&status=0>selected</#if>>待支付</option>
				<option value="1" <#if status?exists&&status=1>selected</#if>>支付成功</option>
				<option value="2" <#if status?exists&&status=2>selected</#if>>网娱向网吧申请付款</option>
				<option value="3" <#if status?exists&&status=3>selected</#if>>网吧有异议</option>
				<option value="4" <#if status?exists&&status=4>selected</#if>>网吧已结款</option>
				<option value="5" <#if status?exists&&status=5>selected</#if>>网娱向网吧汇款失败</option>
				</select>
			</div>
			<button type="submit" class="btn btn-success">查询</button>
		</form>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>订单id</th>
			<th>网吧</th>
			<th>用户昵称</th>
			<th>用户手机</th>
			<th>支付类型</th>
			<th>支付金额</th>
			<th>订单状态</th>
			<th>时间</th>
			<th>操作</th>
		</tr>
		<#list list as o>
			<tr>
				<td>${o.id}</td>
				<td>${o.name}</td>
				<td>${o.user_nickname}</td>
				<td>${o.username}</td>
				<td>
				<#if o.type=1>
				支付宝
				<#else>
				微信
				</#if>
				</td>
				<td>${o.amount}</td>
				<td>
				<#if o.status=-1>
				支付失败
				<#elseif o.status=0>
				待支付
				<#elseif o.status=1>
				支付成功
				<#elseif o.status=2>
				网娱向网吧申请付款
				<#elseif o.status=3>
				网吧有异议
				<#elseif o.status=4>
				网吧已结款
				<#elseif o.status=5>
				网娱向网吧汇款失败
				</#if>
				</td>
				<td>${o.create_date?string("yyyy-MM-dd HH:mm:ss")}</td>
				<td><button edit="${o.id}" type="button" class="btn btn-info">修改订单状态</button></td>
			</tr>
		</#list>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	
	<#-- 编辑 -->
	<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
	<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
	<div id="modal-editor" class="modal fade">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideEditor()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title">修改订单状态</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post">
						<input type="hidden" name="id" />
						<div id="wayselect_id" class="form-group">
							<label class="col-md-2 control-label">订单状态</label>
							<div class="col-md-2">
								<select id="" name="status" class="form-control">
									<option value="-1">支付失败</option>
				                    <option value="0">待支付</option>
				                    <option value="1">支付成功</option>
				                    <option value="2">网娱向网吧申请付款</option>
				                    <option value="3">网吧有异议</option>
				                    <option value="4">网吧已结款</option>
				                    <option value="5">网娱向网吧汇款失败</option>
								</select>
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
	
	// 显示模态框
	$('button[edit]').on('click', function(event) {
		// 初始化模态框宽度
		var width = 1200;
		if(width > $(window).width()) {
			width = $(window).width();
		}
		_$editor.find('.modal-dialog').css('width', width);
		
		// 初始化表单数据
		var id = $(this).attr('edit');
		$.api('${ctx}/order/detail/' + id, {}, function(d) {
			var o = d.object;
			$.fillForm({
				status: o.status,
				id: id,
			}, _$editor);
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
			url:'${ctx}/order/save',
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
	
	</script>
	
</body>
</html>