<#import "/macros/pager.ftl" as p >
<html>
<head>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
<script type="text/javascript" editor-ctx="${ctx!}" src="${ctx!}/static/plugin/editor.js"></script>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>资讯管理</strong>
 		</li>
		<li class="active">
			普通资讯
		</li>
	</ul>
	
	<#-- 新增 -->
	<div class="mb10">
			<button add="" type="button" class="btn btn-success">新增</button>
	</div>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
		    <div class="col-md-2">
				<input type="text" class="form-control" name="title" placeholder="标题" value="${title!}" />
			</div>
			<div class="col-md-2">
				<select class="form-control" name="type">
				    <option value="">请选择类型</option>
					<option value="1"<#if type?exists&&type=1> selected</#if>>资讯</option>
					<option value="3"<#if type?exists&&type=3> selected</#if>>图集</option>
				</select>
			</div>
			<div class="col-md-2">
				<select class="form-control" name="status">
				    <option value="">请选择状态</option>
					<option value="1"<#if status?exists&&status=1> selected</#if>>有效</option>
					<option value="2"<#if status?exists&&status=2> selected</#if>>定时</option>
				</select>
			</div>
			<div class="col-md-2">
				<input type="text" class="form-control" name="date" placeholder="发布时间" value="${date!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:beginPick})" />
			</div>
			<button type="submit" class="btn btn-success">查询</button>
		</form>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>ID</th>
			<th>图片</th>
			<th>发布时间</th>
			<th>标题</th>
			<th>类型</th>
			<th>阅读数</th>
			<th>操作</th>
		</tr>
		<#list result as res>
			<tr>
			<td>${res.id!}</td>
			<td><#if (res.icon)??><img src="${imgServer!}/${res.icon!}" style="width: 50px; height: 50px;" /></#if></td>
			<td>${res.create_date?string("yyyy-MM-dd HH:mm")}</td>	
			<td>
			${res.title}
			</td>
			<td>
			<#if res.type=1>
			资讯
			<#elseif res.type=2>
			专题
			<#elseif res.type=3>
			图集
			</#if>
			</td>
			<td>
			${res.read_num}
			</td>
			<td>
			<button edit="${res.id!}" type="button" class="btn btn-info">编辑</button>
			<button remove="${res.id!}" type="button" class="btn btn-danger">删除</button>
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
					<h4 class="modal-title">新增/编辑资讯</h4>
				</div>
				<div class="modal-body">
				<form  class="form-horizontal form-condensed" role="form" method="post">
				        <input type="hidden" name="id" id="id"/>
						<div class="form-group">
							<label class="col-md-2 control-label">类型<span id="span_id_4" style="color:red;">*</span></label>
							<div class="col-md-10">
								<select id="type" name="type" class="selector_type form-control" onchange="getType();">
									<option value="1">资讯</option>
									<option value="3">图集</option>
								</select>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">标题<span id="span_id_1" style="color:red;">*</span></label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="title" maxlength="13" wy-required="标题"/>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">来源<span id="span_id_1" style="color:red;">*</span></label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="source"  maxlength="64"/>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">摘要</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="brief" maxlength="26" wy-required="摘要"/>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">定时发布时间(不填写则立即发布)</label>
							<div class="col-md-10">
								<input class="form-control" type="text" id="timerDate" name="timerDate"  onclick="WdatePicker({isShowClear:true,dateFmt:'yyyy-MM-dd HH:00:00'})" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">详情</label>
							<div class="col-md-10" id="xq">
								
							</div>
						</div>
						<div class="form-group" id="slt">
							<label class="col-md-2 control-label">缩略图<span id="span_id_1" style="color:red;">*</span></label>
							<div class="col-md-10" id="iconDiv">
								<input type="file" name="iconImg" value="缩略图">
								<font class="prompt">
									请上传30:23宽高比的图片，以达到最佳显示效果
								</font>
							</div>
						</div>
						<div class="form-group" id="tp">
							<label class="col-md-2 control-label">图片<span id="span_id_1" style="color:red;">*</span></label>
							<div class="col-md-10" id="imgDiv">
							
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
	var imgServer='${imgServer!}';
	$(document).on("click", ".del", function() {
    $(this).parent().html("");
    }); 
    $(document).on("click", ".editDel", function() {
    $(this).parent().html("");
    }); 
    $(document).on("click", "#more", function() {
     $("#imgDiv").append('<div class="fileDiv"><input type="file" name="file" value="图片"><button type="button" class="del">删除</button></div>');
    }); 
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
	
	// 显示新增
	$('button[add]').on('click', function(event) {
	var width = 1200;
		if(width > $(window).width()) {
			width = $(window).width();
		}
		_$editor.find('.modal-dialog').css('width', width);
	     $.fillForm({
		}, _$editor);
		$("#imgDiv").html('<font class="prompt">请上传64:31宽高比的图片，以达到最佳显示效果</font>');
		$("#imgDiv").append('<div class="fileDiv"><input type="file" name="file" value="图片">');
		$("#xq").html('');
	    $("#xq").append('<textarea id="editor-detail" name="remark" placeholder="Balabala" autofocus></textarea><script type="text/javascript">_editor = editor($("#editor-detail"));<\/script>');
		_$editor.modal('show');
	});

	
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
		$.api('${ctx}/activityInformation/common/detail/' + id, {}, function(d) {
			var o = d.object;
			var obj=o.obj;
			var timerDate = new Date(obj.timerDate).Format('yyyy-MM-dd hh:00:00');
			var imgs=o.imgs;
			$.fillForm({
				type: obj.type,
				title: obj.title,
				source:obj.source,
				remark:obj.remark,
				brief:obj.brief,
				iconImg: obj.icon,
				timerDate:timerDate,
				id: id,
			}, _$editor);
			$("#imgDiv").html('');
			if(obj.type==1){
			// $("#slt").show();
			$("#tp").hide();
		    $("#xq").html('');
		    $("#xq").append('<textarea id="editor-detail" name="remark" placeholder="" autofocus></textarea><script type="text/javascript">_editor = editor($("#editor-detail"));<\/script>');
				if(imgs && imgs.length > 0) {
					$("#imgDiv").append('<div class="editFileDiv"><input type="file" name="file" value="图片"><img width="200" height="150" src="'+imgServer+imgs[0].img+'"/></div>');
				}
				$("#imgDiv").append('<font class="prompt">请上传64:31宽高比的图片，以达到最佳显示效果</font>')
			}
			else{
			// $("#slt").hide();
			$("#tp").show();
		    $("#xq").html('');
		    $("#xq").append('<textarea id="txtRemark" cols="65" rows="4" maxlength="200" name="remark" placeholder="" autofocus></textarea>');
			$("#imgDiv").append('<button type="button" id="more">更多</button>');
			var url;
			for(i=0;i<imgs.length;i++){
			url=imgs[i].img;
			$("#imgDiv").append('<div class="editFileDiv"><input type="hidden" name="oldImgs" value="'+url+'"/><img width="200" height="150" src="'+imgServer+url+'"/><button type="button" class="editDel">删除</button></div>');
			}
			$("#imgDiv").append('<font class="prompt">请上传30:23宽高比的图片，以达到最佳显示效果</font>')
			}
			$("#txtRemark").val(obj.remark);
			setEditorText($('#editor-detail'), obj.remark);
		}, function(d) {
			alert('请求数据异常：' + d.result);
		}, {
			async: false,
		});
		_$editor.modal('show');
	});
	
	// 提交表单
	function submitEditor() {
	var $form=_$editor.find('form');
	var valid = $form.formValid();
	if(valid){
		$form.ajaxSubmit({
			url:'${ctx}/activityInformation/common/save',
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
	
	// 删除
	$('button[remove]').on('click', function(event) {
		var _this = $(this);
		_this.attr('disabled', true);
		
		var id = $(this).attr('remove');
		
		$.confirm('确认删除吗?', function() {
			$.api('${ctx}/activityInformation/common/delete/' + id, {}, function(d) {
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
		var banner = _this.attr('banner');
		var s;
		if(banner==0){
		s="启用";
		}else if(banner==1){
		s="禁用";
		}
		$.confirm('确认'+s+'吗?', function() {
			$.api('${ctx}/activityInformation/enabled/' + id, {}, function(d) {
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
	
	function getType(type){
	if(type==undefined){
	type=$("#type").val();
	}
	$("#imgDiv").html('');
	if(type==3){
	// $("#slt").hide();
	$("#tp").show();
	$("#xq").html('');
	$("#xq").append('<textarea cols="65" rows="4" maxlength="200" name="remark" placeholder="" autofocus></textarea>');
	$("#imgDiv").append('<font class="prompt">请上传30:23宽高比的图片，以达到最佳显示效果</font>')
	$("#imgDiv").append('<button type="button" id="more">更多</button>');
	$("#imgDiv").append('<div class="fileDiv"><input type="file" name="file" value="图片"><button type="button" class="del">删除</button></div>');
	}
	else{
	// $("#slt").show();
	$("#tp").hide();
	$("#xq").html('');
	$("#xq").append('<textarea id="editor-detail" name="remark" placeholder="Balabala" autofocus></textarea><script type="text/javascript">_editor = editor($("#editor-detail"));<\/script>');
	$("#imgDiv").append('<div class="fileDiv"><input type="file" name="file" value="图片">');
	$("#imgDiv").append('<font class="prompt">请上传64:31宽高比的图片，以达到最佳显示效果</font>')
	}
    }
    
    getType(1);
    
	</script>
</body>
</html>