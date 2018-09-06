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
			每周红包设置
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
			<th>类型</th>
			<th>红包说明</th>
			<th>最小金额</th>
			<th>最大金额</th>
			<th>有效期</th>
			<th>开始时间</th>
			<th>结束时间</th>
			<th>操作</th>
		</tr>
		<#list list as o>
			<tr>
				<td>${o.id!}</td>
				<td><#if o.type == 0>首次登陆<#elseif o.type == 1>注册绑定<#elseif o.type == 2>预约支付<#elseif o.type == 3>每周红包<#elseif o.type == 4>分享红包</#if></td>
				<td>${o.explain!}</td>
				<td>${o.money!}</td>
				<td>${o.maxMoney!}</td>
				<td>${o.day!}</td>
				<td>${o.beginTime!}</td>
				<td>${o.endTime!}</td>
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
						<input type="hidden" name="type" />
						<input type="hidden" name="restrict" />
						<#if type == "3">
						<div class="form-group">
							<label class="col-md-2 control-label">红包金额</label>
							<div class="col-md-10">
								<div class="input-group">
									<input type="number" name="money" value="1" class="form-control" placeholder="最小金额"> 
									<span class="input-group-addon"> — </span>
									<input type="number" name="maxMoney" value="3" class="form-control" placeholder="最大金额"> 
								</div>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">红包有效期</label>
							<div class="col-md-10">
								<input class="form-control" type="number" name="day" value="3" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">红包总金额</label>
							<div class="col-md-10">
								<input class="form-control" type="number" name="totalmoney" value="150000" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">周红包时间</label>
							<div class="col-md-10">
								<div class="input-group">
									<input type="text" name="beginTime" value="" class="form-control" placeholder="开始时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd 12:00:00'})"> 
									<span class="input-group-addon"> — </span>
									<input type="text" name="endTime" value="" class="form-control" placeholder="结束时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd 23:59:59'})">
								</div>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">红包说明</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="explain" value="支付网费时使用" maxlength="22" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">已用金额</label>
							<div class="col-md-10">
								<input class="form-control" type="number" name="usedAmount" readonly value="" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">剩余金额</label>
							<div class="col-md-10">
								<input class="form-control" type="number" name="surplusAmount" readonly value="" />
							</div>
						</div>
						<#else>
						<div class="form-group">
							<label class="col-md-2 control-label">红包金额</label>
							<div class="col-md-10">
								<input type="number" name="money" value="" class="form-control" placeholder="最小金额"> 
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">红包有效期</label>
							<div class="col-md-10">
								<input class="form-control" type="number" name="day" value="" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">红包说明</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="explain" value="" maxlength="22" />
							</div>
						</div>
						</#if>
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
					url:'${ctx}/redbag/setting/save',
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
			$.api('${ctx}/redbag/setting/detail/' + id, {}, function(d) {
				var o = d.object;
				$.fillForm({
					day: o.day,
					explain: o.explain,
					money: o.money,
					maxMoney: o.maxMoney,
					beginTime: o.beginTime ? new Date(o.beginTime).Format('yyyy-MM-dd hh:mm:ss') : '',
					endTime: o.endTime ? new Date(o.endTime).Format('yyyy-MM-dd hh:mm:ss') : '',
					type: o.type,
					restrict: o.restrict,
					surplusAmount: o.surplusAmount,
					usedAmount: o.usedAmount,
					totalmoney: o.totalmoney,
					id: o.id,
				}, _$editor);
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
				type: ${type!3},
				restrict: 0,
			}, _$editor);
			showEditor('新增周红包');
		});
		
		// 删除
		$('button[remove]').on('click', function() {
			var id = $(this).attr('remove');
			$.confirm('确认删除吗?', function() {
				$.api('${ctx!}/redbag/setting/delete/' + id, {}, function(d) {
					window.location.reload();
				});
			});
		});
	</script>
</body>
</html>