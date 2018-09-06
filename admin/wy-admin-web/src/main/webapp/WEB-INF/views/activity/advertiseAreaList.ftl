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
  			<strong>管理系统</strong>
 		</li>
		<li class="active">
			广告地区管理
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form style="margin:0px;display: inline" action="/indexAdvertise/list/1" method="post">
			<button type="submit" class="btn btn-success" style="margin-left:10px;">返回广告列表</button>
			<button add="" adId="${params.adId!}" type="button" class="btn btn-info" style="margin-left:50px;">新增地区</button>
		</form><br><br>
		<form id="search" action="1" method="post">
			<input type="hidden" name="adId" value="${params.adId!}"/>
			<input type="hidden" name="title" value="${params.title!}"/>
			<div class="col-md-2">
				<select class="form-control" name="valid">
					<option value="1"<#if ((params.valid?number)!1) == 1> selected</#if>>正常</option>
					<option value="0"<#if ((params.valid?number)!1) == 0> selected</#if>>已删除</option>
				</select>
			</div>
			<button type="submit" class="btn btn-success">筛选</button>
		</form>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>#</th>
			<th>地区</th>
			<th>操作</th>
		</tr>
		<#list list as o>
			<tr>
				<td>${o.id!}</td>
				<#if o.areaCode=='000000'>
				<td>全国</td>
				<#else>
				<td>${o.name!}</td>
				</#if>
				<td>
					<button edit="${o.id!}" areaCode="${o.areaCode!}" type="button" class="btn btn-info">编辑</button>
					<#if ((o.valid?number)!0) == 1>
						<button remove="${o.id!}" valid="0" type="button" class="btn btn-danger">删除</button>
					<#else>
						<button remove="${o.id!}" valid="1" type="button" class="btn btn-danger">恢复</button>
					</#if>
				</td>
			</tr>
		</#list>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	
	<#-- 编辑，添加 -->
	<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
	<div id="modal-editor" class="modal fade">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideEditor()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 id="bianji_h4id" class="modal-title" style="display:none;">编辑地区</h4>
					<h4 id="xinzeng_h4id" class="modal-title" style="display:none;">添加地区</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post">
						<input type="hidden" name="id" />
						<input type="hidden" name="advertiseId" />
						<input type="hidden" name="title" value="${params.title!}"/>
						<div class="form-group">
							<label class="col-md-2 control-label">地区</label>
							<div class="col-md-10">
								<select id="select_id_areaCode" name="areaCode" class="form-control">
									<option value="000000">全国</option>
									<#list provinceList as p>
									<option value="${p.areaCode!}">${p.name!}</option>
									</#list>
								</select>
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
		$('#xinzeng_h4id').hide();
		$('#bianji_h4id').show();
		var id = $(this).attr('edit');
		var areaCode = $(this).attr('areaCode');
		$('#select_id_areaCode').val(areaCode);
		
		fillForm({
			id: id
		});
		
		_$editor.modal('show');
	});
	
	// 显示模态框，新增
	$('button[add]').on('click', function(event) {
		$('#bianji_h4id').hide();
		$('#xinzeng_h4id').show();
		var adId = $(this).attr('adId');
		fillForm({
			advertiseId: adId
		});
		_$editor.modal('show');
	});
	
	// 提交表单
	function submitEditor() {
		_$editor.find('form').ajaxSubmit({
			url:'${ctx}/indexAdvertiseArea/save',
		    type:'post',
		    success: function(d){
		    	if(d.code == 0) {
		    		window.location.href = '${ctx}/indexAdvertiseArea/list/1?adId=${params.adId!}&title=${params.title!}';
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
		var valid = $(this).attr('valid');
		var info1,info2;
		if(valid==0){
			info1='确认删除？';
			info2='删除失败：';
		}else{
			info1='确认恢复？';
			info2='删除恢复：';
		}
		var params = {
				id : $(this).attr('remove'),
				valid : valid
			};
		$.confirm(info1, function() {
			$.ajax({
				url : '${ctx}/indexAdvertiseArea/updateValid/',
				data : params,
				dataType : 'json',
				success : function(d) {
					if(d.code==0){
						if(valid==0){
							window.location.href = '${ctx}/indexAdvertiseArea/list/1?adId=${params.adId!}&valid=1';
						}else{
							window.location.href = '${ctx}/indexAdvertiseArea/list/1?adId=${params.adId!}&valid=0';
						}
					}else {
						alert(info2 + d.result);
					}
				},
			});
		});
	});
	
	</script>
</body>
</html>