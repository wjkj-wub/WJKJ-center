<#import "/macros/pager.ftl" as p >
<html>
<head>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/simditor/css/simditor.css">
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/simditor/css/font-awesome.css">
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/simditor/css/simditor.scss">
<script type="text/javascript" editor-ctx="${ctx!}" src="${ctx!}/static/plugin/simditor/editor.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/module.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/hotkeys.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/uploader.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/simditor.min.js"></script>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>资讯管理</strong>
 		</li>
		<li class="active">
			专题
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
				<input type="text" class="form-control" name="date" placeholder="发布时间" value="${date!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:beginPick})" />
			</div>
			<div class="col-md-2">
				<select class="form-control" name="status">
				    <option value="">请选择状态</option>
					<option value="1"<#if status?exists&&status=1> selected</#if>>有效</option>
					<option value="2"<#if status?exists&&status=2> selected</#if>>定时</option>
				</select>
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
			<th>资讯数</th>
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
			${res.num}
			</td>
			<td>
			<button edit="${res.id!}" type="button" class="btn btn-info">编辑</button>
			<button remove="${res.id!}" type="button" class="btn btn-danger">删除</button>
			<a class="btn btn-info" href="${ctx}/activityInformation/subject/info/list/1?pid=${(res.id)!}">前往新增</a>
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
				        <!-- <div class="form-group">
							<label class="col-md-2 control-label">新增方式<span id="span_id_1" style="color:red;">*</span></label>
							<div class="col-md-10">
								<select id="type" name="category" class="selector_type form-control" onchange="getType();">
								<option value="1">单独录入</option>
								<option value="2">关联赛事</option>
								</select>
							</div>
						</div> -->
				        <div class="form-group" id="targetDiv">
							<label class="col-md-2 control-label">赛事<span id="span_id_1" style="color:red;">*</span></label>
							<div class="col-md-10">
								<select id="activityId" name="activityId" class="selector_type form-control">
								</select>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">标题<span id="span_id_1" style="color:red;">*</span></label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="title" wy-required="标题" maxlength="13" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">定时发布时间(不填写则立即发布)</label>
							<div class="col-md-10">
								<input class="form-control" type="text" id="timerDate" name="timerDate"  onclick="WdatePicker({isShowClear:true,dateFmt:'yyyy-MM-dd HH:00:00'})" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">banner图<span id="span_id_1" style="color:red;">*</span></label>
							<div class="col-md-10">
								<input type="file" name="coverFile" value="banner图">
								<font class="prompt">
									请上传64:31宽高比的图片，以达到最佳显示效果
								</font>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">缩略图<span id="span_id_1" style="color:red;">*</span></label>
							<div class="col-md-10">
								<input type="file" name="file" value="图片">
								<font class="prompt">
									请上传30:23宽高比的图片，以达到最佳显示效果
								</font>
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
	     $.fillForm({
		}, _$editor);
		getType(1);
		_$editor.modal('show');
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
		$.api('${ctx}/activityInformation/common/detail/' + id, {}, function(d) {
			var o = d.object;
			var obj=o.obj;
			var imgs=o.imgs;
			$.fillForm({
				category: o.category,
				activityId:obj.activityId,
				title: obj.title,
				source:obj.source,
				remark:obj.remark,
				brief:obj.brief,
				file:obj.icon,
				coverFile:obj.cover,
				id: id,
			}, _$editor);
		}, function(d) {
			alert('请求数据异常：' + d.result);
		}, {
			async: false,
		});
		_$editor.modal('show');
		getType();
	});
	
	// 提交表单
	function submitEditor() {
	var form=_$editor.find('form');
	var valid = form.formValid();
	if(valid){
		form.ajaxSubmit({
			url:'${ctx}/activityInformation/subject/save',
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
	

    
    $.ajax({
    type : 'post',
    url : '${ctx}/activityInformation/activityList',
    dataType : 'json',
    async: false, 
    success : function(data) {
    for(var i=0;i<data.length;i++){
    $("#activityId").append('<option value="'+data[i].id+'">'+data[i].title+'</option>');
    }
    }
    })
    
    function getType(type){
    var type;
    if(type==undefined)
    type=$("#type").val();
    if(type==2){
    $("#targetDiv").show();
    $("#activityId").attr("disabled",false);
    }
    else{
    $("#targetDiv").hide();
    $("#activityId").attr("disabled",true);
    }
    }
    $("#targetDiv").hide();
    $("#activityId").attr("disabled",true);
	</script>
</body>
</html>