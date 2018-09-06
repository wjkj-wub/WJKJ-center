<#import "/macros/pager.ftl" as p >
<html>
<head>
<link rel="stylesheet" href="${ctx}/static/lib/chosen/chosen.css" />
<script type="text/javascript" src="${ctx}/static/lib/chosen/chosen.min.js"></script>
<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>转盘活动管理</strong>
 		</li>
		<li class="active">
			奖项管理
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
			<div class="col-md-2">
				<select class="form-control" name="lotteryId">
					<option value="">全部活动</option>
					<#list lotteryList as l>
					<option value="${l.id!}"<#if ((params.lotteryId?number)!0) == l.id> selected</#if> >${l.name!}</option>
					</#list>
				</select>
			</div>
			<div class="col-md-2">
				<select class="form-control" name="valid">
					<option value="1"<#if ((params.valid?number)!1) == 1> selected</#if>>正常</option>
					<option value="0"<#if ((params.valid?number)!1) == 0> selected</#if>>已删除</option>
				</select>
			</div>
			<button type="submit" class="btn btn-success">筛选</button>
			<button add="" type="button" class="btn btn-info">新增</button>
		</form>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>活动名称</th>
			<th>奖项</th>
			<th>奖品</th>
			<th>对外显示数量</th>
			<th>权重</th>
			<th>虚拟中奖人数</th>
			<th>操作</th>
		</tr>
		<#if list??>
		<#list list as o>
			<tr>
				<td>${o.lotteryName!}</td>
				<td>${o.name!}</td>
				<td>${o.prizeName!}</td>
				<td>${o.inventory!}</td>
				<td>${o.probablity!}</td>
				<td>${o.virtualWinners!}</td>
				<td>
					<button edit="${o.id!}" lotteryId="${o.lotteryId!}" prizeId="${o.prizeId!}" name="${o.name!}" inventory="${o.inventory!}" realInventory="${o.realInventory!}" probablity="${o.probablity!}" virtualWinners="${o.virtualWinners!}" type="button" class="btn btn-info">编辑</button>
					<#if ((o.valid?number)!0) == 1>
					<button remove="${o.id!}" valid="0" type="button" class="btn btn-danger">删除</button>
					<#else>
					<button remove="${o.id!}" valid="1" type="button" class="btn btn-danger">恢复</button>
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
					<button type="button" class="close" onclick="hideEditor()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 id="bianji_h4id" class="modal-title" style="display:none;">编辑奖项</h4>
					<h4 id="xinzeng_h4id" class="modal-title" style="display:none;">新增奖项</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post" enctype="multipart/form-data">
						<input type="hidden" name="id" />
						<input type="hidden" name="lotteryId" />
						<#-- <div class="form-group">
							<label class="col-md-2 control-label">选择活动</label>
							<div class="col-md-10">
								<select id="select_id_lottery" class="form-control" name="lotteryId">
									<#if lotteryList??>
										<#list lotteryList as l>
										<option value="${l.id!}">${l.name!}</option>
										</#list>
									<#else>
										<option value="">没有录入的活动</option>
									</#if>
								</select>
							</div>
						</div> -->
						<div class="form-group">
							<label class="col-md-2 control-label">奖项名<span id="span_id_1" style="color:red">*</span></label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="name" value="" wy-required="奖项名" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">中奖概率(%)<span id="span_id_4" style="color:red">*</span></label>
							<div class="col-md-10">
								<input class="form-control" type="number" name="probablity" value="" wy-required="中奖概率" min="0" max="100" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">奖品数量(界面显示)<span id="span_id_2" style="color:red">*</span></label>
							<div class="col-md-10">
								<input class="form-control" type="number" name="inventory" value="" wy-required="奖品数量" min="-1" />
								<p><span class="label label-badge label-danger">(-1表示无限额)</span></p>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">虚拟中奖人数<span id="span_id_5" style="color:red">*</span></label>
							<div class="col-md-10">
								<input class="form-control" type="number" name="virtualWinners" value="" wy-required="虚拟中奖人数" min="0" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">实际数量<span id="span_id_3" style="color:red">*</span></label>
							<div class="col-md-10">
								<input class="form-control" type="number" name="realInventory" value="" wy-required="实际数量" min="-1" />
								<p><span class="label label-badge label-danger">(-1表示无限额)</span></p>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">奖项剩余量</label>
							<div class="col-md-10">
								<input class="form-control" type="number" name="surplus" value="" disabled />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">排序</label>
							<div class="col-md-10">
								<input class="form-control" type="number" name="order" value="" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">奖品</label>
							<div class="col-md-10">
								<select id="select_id_prize" class="form-control" name="prizeId">
									<option value="">新建奖品</option>
									<#if prizeList??>
										<#list prizeList as p>
										<option value="${p.id!}">${p.name!}</option>
										</#list>
									</#if>
								</select>
							</div>
						</div>
						<div id="div-new-prize">
							<div class="form-group">
								<label class="col-md-2 control-label">奖品名</label>
								<div class="col-md-10">
									<input class="form-control" type="text" name="prizeName" value="" wy-required="奖品名" />
								</div>
							</div>
							<div class="form-group">
								<label class="col-md-2 control-label">奖品价值</label>
								<div class="col-md-10">
									<input class="form-control" type="text" name="prizePrice" value="" wy-required="奖品价值" />
								</div>
							</div>
							<div class="form-group">
								<label class="col-md-2 control-label">奖品图标</label>
								<div class="col-md-10">
									<input class="form-control" type="file" name="prizeIconFile" value="" wy-required="奖品图标" />
								</div>
							</div>
						</div>
					</form>
					<script type="text/javascript">
						var newPrizeHtml = $('#div-new-prize').html();
						$('#select_id_prize').on('change',function() {
							var $newPrizeDiv = $('#div-new-prize');
							if($(this).val().length <= 0) {
								$newPrizeDiv.html(newPrizeHtml);
							} else {
								$newPrizeDiv.html('');
							}
						});
					</script>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" onclick="hideEditor()">关闭</button>
					<button id="submit" type="button" class="btn btn-primary">确定</button>
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
	
	// 显示模态框，编辑
	$('button[edit]').on('click', function(event) {
		// 初始化表单数据
		var id = $(this).attr('edit');
		$.api('${ctx}/award/detail/' + id, {}, function(d) {
			var o = d.object.award;
			var surplus = d.object.surplus;
			$('#xinzeng_h4id,#span_id_1,#span_id_2,#span_id_3,#span_id_4,#span_id_5').hide();
			$('#bianji_h4id').show();
			$('#select_id_lottery').val(o.lotteryId);
			$('#select_id_prize').val(o.prizeId);
			$.fillForm({
				id: o.id,
				name: o.name,
				inventory: o.inventory,
				realInventory: o.realInventory,
				probablity: o.probablity,
				virtualWinners: o.virtualWinners,
				lotteryId: o.lotteryId,
				prizeId: o.prizeId,
				order: o.order,
				surplus: surplus
			}, _$editor);
			$('#select_id_prize').change();
		}, function(d) {
			alert('请求数据异常：' + d.result);
		}, {
			async: false,
		});
		
		_$editor.modal('show');
	});
	
	// 显示模态框，新增
	$('button[add]').on('click', function(event) {
		$('#bianji_h4id').hide();
		$('#xinzeng_h4id,#span_id_1,#span_id_2,#span_id_3,#span_id_4,#span_id_5').show();
		//清除内容
		$('#select_id_lottery').val();
		$('#select_id_prize').val();
		$.fillForm({
			id: '',
			name: '',
			inventory: 0,
			realInventory: 0,
			probablity: 0,
			virtualWinners: 0,
			lotteryId: ${loggeryId!0},
			prizeId: '',
			order: 0,
			surplus: ''
		}, _$editor);
		$('#select_id_prize').change();
		_$editor.modal('show');
	});
	
	// 提交表单
	_$editor.find('#submit').on('click', function(event) {
		var $form = _$editor.find('form');
		var valid = $form.formValid();
		if(valid) {
			var $this = $(this);
			$this.prop('disabled', true);
			
			$form.ajaxSubmit({
				url:'${ctx}/award/save',
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
	
	// 删除/恢复
	$('button[remove]').on('click', function(event) {
		var _this = $(this);
		_this.attr('disabled', true);
		var id = $(this).attr('remove');
		var valid = $(this).attr('valid');
		var info;
		var info2;
		if(valid=='0'){
			info='确认删除吗？';
			info2='删除失败：';
		}else{
			info='确认恢复吗？';
			info2='恢复失败：';
		}
		$.confirm(info, function() {
			$.api('${ctx}/award/deleteOrRecover/' + id +'/'+valid, {}, function(d) {
				window.location.reload();
			}, function(d) {
				alert(info2 + d.result);
			}, {
				complete: function() {
					_this.attr('disabled', false);
				}
			});
		}, undefined, {
			complete: function() {
				_this.attr('disabled', false);
			}
		});
	});
	</script>
</body>
</html>