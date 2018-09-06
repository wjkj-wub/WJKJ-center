<#import "/macros/pager.ftl" as p >
<html>
<head>
<link rel="stylesheet" href="${ctx}/static/lib/chosen/chosen.css" />
<script type="text/javascript" src="${ctx}/static/lib/chosen/chosen.min.js"></script>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>转盘活动管理</strong>
 		</li>
		<li class="active">
			奖品管理
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
			<div class="col-md-2">
				<select class="form-control" name="valid">
					<option value="1"<#if ((params.valid?number)!1) == 1> selected</#if>>正常</option>
					<option value="0"<#if ((params.valid?number)!1) == 0> selected</#if>>已删除</option>
				</select>
			</div>
			<button type="submit" class="btn btn-success">筛选</button>
			<button add="add_id" type="button" class="btn btn-info">新增</button>
		</form>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>图片</th>
			<th>奖品名</th>
			<th>价值描述</th>
			<th>操作</th>
		</tr>
		<#if list??>
		<#list list as o>
			<tr>
				<td><#if (o.icon)?? && o.icon?length gt 0><img src="${imgServer!}${o.icon!}" style="width:35px;height:35px;" /></#if></td>
				<td>${o.name!}</td>
				<td>${o.price!}</td>
				<td>
					<button edit="${o.id!}" icon="${o.icon!}" price="${o.price!}" name="${o.name!}" type="button" class="btn btn-info">编辑</button>
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
	<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
	<div id="modal-editor" class="modal fade">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideEditor()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 id="bianji_h4id" class="modal-title" style="display:none;">编辑奖品</h4>
					<h4 id="xinzeng_h4id" class="modal-title" style="display:none;">新增奖品</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post">
						<input type="hidden" name="id" />
						<div class="form-group">
							<label class="col-md-2 control-label">奖品名称<span id="span_id_1" style="color:red">*</span></label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="name" value="">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">价值描述<span id="span_id_2" style="color:red">*</span></label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="price" value="">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">图片<span id="span_id_3" style="color:red">*</span></label>
							<div class="col-md-10">
								<input id="file_id" type="file" name="iconFile" value="奖品图">
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" onclick="hideEditor()">关闭</button>
					<button type="button" class="btn btn-primary" onclick="submitEditor()">确定</button>
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
	
	// 初始化表单数据
	function fillForm(columns) {
		for(var k in columns) {
			var $field = _$editor.find('[name="' + k + '"]');
			$field.val(columns[k]);
		}
	}
	
	// 显示模态框，编辑
	$('button[edit]').on('click', function(event) {
		$('#xinzeng_h4id,#span_id_1,#span_id_2,#span_id_3').hide();
		$('#bianji_h4id').show();
		var id = $(this).attr('edit');
		var name = $(this).attr('name');
		var price = $(this).attr('price');
		$('#file_id').val('');
		fillForm({
			id: id,
			name: name,
			price: price
		});
		
		_$editor.modal('show');
	});
	
	// 显示模态框，新增
	$('button[add]').on('click', function(event) {
		$('#bianji_h4id').hide();
		$('#xinzeng_h4id,#span_id_1,#span_id_2,#span_id_3').show();
		$('#file_id').val('');
		fillForm({
			id: '',
			name: '',
			price: ''
		});
		_$editor.modal('show');
	});
	
	// 提交表单
	function submitEditor() {
		_$editor.find('form').ajaxSubmit({
			url:'${ctx}/prize/save',
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
			$.api('${ctx}/prize/deleteOrRecover/' + id +'/'+valid, {}, function(d) {
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