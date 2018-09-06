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
  			<strong>用户反馈</strong>
 		</li>
		<li class="active">
			app用户反馈
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
			<div class="col-md-2">
				<input type="text" class="form-control" name="phone" placeholder="手机号码" value="${phone!}" />
			</div>
			<div class="col-md-2">
				<input type="text" class="form-control" name="content" placeholder="反馈内容" value="${content!}" />
			</div>
			<div class="col-md-2">
				<select class="form-control" name="state">
				    <option value="">全部</option>
					<option value="0"<#if state?exists&&state=0> selected</#if>>待处理</option>
					<option value="1"<#if state?exists&&state=1> selected</#if>>已处理</option>
					<option value="2"<#if state?exists&&state=2> selected</#if>>有异议</option>
				</select>
			</div>
			<div class="col-md-2">
				<input type="text" class="form-control" name="date" placeholder="反馈时间" value="${date!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:beginPick})" />
			</div>
			<button type="submit" class="btn btn-success">查询</button>
		</form>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>ID</th>
			<th>图片</th>
			<th>反馈时间</th>
			<th>反馈内容</th>
			<th>用户昵称</th>
			<th>联系方式</th>
			<th>状态</th>
			<th>反馈来源</th>
			<th>操作</th>
		</tr>
		<#list result as res>
			<tr>
			<td>${res.id!}</td>
			<td><#if (res.img)?? && res.img?length gt 0><img src="${imgServer!}/${res.img!}" style="width: 50px; height: 50px;" /></#if></td>
			<td>${res.create_date?string("yyyy-MM-dd HH:mm")}</td>	
			<td>
				<button type="button" class="btn btn-default" data-toggle="tooltip" data-placement="top" data-original-title="${(res.content)!}">
					<#if (res.content)?? && res.content?length gt 15>
						${(res.content?substring(0,15))!}
					<#else>
						${res.content!}
					</#if>
				</button>
			</td>
			<td>${res.nickname!}</td>
			<td>${res.phone!}</td>
			<td>
			<#if res.state=0>
			待处理
			<#elseif res.state=1>
			已处理
			<#elseif res.state=2>
			有异议
			</#if>
			</td>
			<td>
			<#if res.type=1>
			普通
			<#elseif res.type=2>
                                    娱乐赛事申诉
			</#if>
			</td>
			<td>
			<#if res.state!=1>
			<button edit="${res.id!}" type="button" category="${res.type}" class="btn btn-info">处理</button>
			</#if>
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
					<h4 class="modal-title">处理反馈</h4>
				</div>
				<div class="modal-body">
				<form  class="form-horizontal form-condensed" role="form" method="post">
						<div class="form-group">
							<label class="col-md-2 control-label">反馈内容</label>
							<div class="col-md-10">
								<textarea class="form-control" type="text" name="content" disabled></textarea>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">图片</label>
							<div class="col-md-10" id="imgDiv">
								
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">反馈时间</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="create_date" value="" disabled>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">昵称</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="nickname" value="" disabled>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">联系方式</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="phone" value="" disabled>
							</div>
						</div>
						<input type="hidden" name="id" />
						<input type="hidden" name="type" />
					    <div class="form-group">
							<label class="col-md-2 control-label">处理状况</label>
							<div class="col-md-2">
								<input class="" type="radio" name="state" value="1" checked>正常处理
								<input class="" type="radio" name="state" value="2">有异议
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">备注</label>
							<div class="col-md-10">
								<textarea class="form-control" name="remark"></textarea>
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
	$('button[data-toggle="tooltip"]').tooltip();
	
	// 初始化模态框
	var _$editor = $('#modal-editor');
	var _$add = $('#modal-add');
	_$editor.modal({
		keyboard : false,
		show : false
	})
	
	// 关闭模态框
	function hideEditor() {
		_$editor.modal('hide');
		_$add.modal('hide');
	}
	
	// 显示新增
	$('button[add]').on('click', function(event) {
		$('#input_id_mobile').val('');
		_$add.modal('show');
	});
	// 提交注册
	function submitAdd() {
		_$add.find('form').ajaxSubmit({
			url:'${ctx}/user/register',
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
		var server='${imgServer!}';
		var category= $(this).attr('category')
		$("#imgDiv").html("");
		$.api('${ctx}/suggestion/detail/' + id+'?type='+category, {}, function(d) {
			var o = d.object;
			if(o.imgs!=undefined){
			for(i=0;i<o.imgs.length;i++){
			$("#imgDiv").append('<img src="'+server+o.imgs[i]+'"/>');
			}
			}
			$.fillForm({
				content: o.content,
				create_date: o.create_date,
				nickname: o.nickname,
				phone: o.phone,
				type:o.type,
				remark:o.remark,
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
			url:'${ctx}/suggestion/deal',
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
	
	// 删除
	$('button[remove]').on('click', function(event) {
		var _this = $(this);
		_this.attr('disabled', true);
		
		var id = $(this).attr('remove');
		
		$.confirm('确认删除吗?', function() {
			$.api('${ctx}/netbar/resource/property/' + id, {}, function(d) {
				window.location.reload();
			}, function(d) {
				alert('删除失败：' + d.result);
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
	
	// 启用
	$('button[enabled]').on('click', function(event) {
		var _this = $(this);
		_this.prop('disabled', true);
		
		var id = _this.attr('enabled');
		$.confirm('确认启用吗?', function() {
			$.api('${ctx}/user/enabled/' + id, {}, function(d) {
				window.location.reload();
			}, function(d) {
				alert(d.result);
			}, {
				complete: function() {
					_this.prop('disabled', false);
				}
			});
		}, undefined, {
			complete: function() {
				_this.prop('disabled', false);
			}
		})
	});
	</script>
</body>
</html>