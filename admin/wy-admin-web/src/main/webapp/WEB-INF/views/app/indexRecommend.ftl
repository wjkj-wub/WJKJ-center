<#import "/macros/pager.ftl" as p >
<html>
<head>
<link rel="stylesheet" href="${ctx}/static/lib/chosen/chosen.css" />
<script type="text/javascript" src="${ctx}/static/lib/chosen/chosen.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>app展位管理</strong>
 		</li>
		<li class="active">
			<#if 9 gt tab>首页展位<#else>发现模块展位</#if>
		</li>
	</ul>
	<#-- 新增 -->
	<div class="mb10">
			<button add="" type="button" class="btn btn-success">新增</button>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>排序</th>
			<th>图片</th>
			<th>标题</th>
			<#if tab=1||tab=5||tab=9><th>描述</th></#if>
			<th>类型</th>
			<#if tab=1||tab=4||tab=5||tab=9><th>网址</th></#if>
			<#if tab=1||tab=5||tab=9><th>设备</th></#if>
			<th>操作</th>
		</tr>
		<#list result as res>
			<tr>
				<td>${res.sort!}</td>
				<td><#if res.img?exists><img src="http://img.wangyuhudong.com/${res.img}" width="100" height="60"/></#if></td>
				<td>${res.title!}</td>
				<#if tab=1||tab=5||tab=9><td>${res.describe!}</td></#if>
				<td>
				<#if res.type=10>
				官方赛
				<#elseif res.type=11>
			            娱乐赛
				<#elseif res.type=12>
				约战
				<#elseif res.type=5>
				推广
				<#elseif res.type=13>
				福利
				<#elseif res.type=14>
				android下载推广
				<#elseif res.type=15>
				资讯
				<#elseif res.type=16>
				自发赛
				<#elseif res.type=17>
				悬赏令
				</#if>
				</td>
				<#if tab=1||tab=4||tab=5||tab=9><td>${res.url!}</td></#if>
				<#if tab=1||tab=5||tab=9><td>
				<#if res.device_type=0>
				全部
				<#elseif res.device_type=1>
				IOS
				<#elseif res.device_type=2>
				Android
				</#if>
				</td>
				</#if>
				<td>
				<button edit="${res.id!}" type="button" category="${res.type}" class="btn btn-info">编辑</button>
				<button remove="${res.id!}" type="button" category="${res.type}" class="btn btn-danger">删除</button>
				</td>
			</tr>
		</#list>
	</table>
	
	
	<#-- 编辑，新增 -->
	<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
	<div id="modal-editor" class="modal fade">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideEditor()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post">
						<input type="hidden" name="id" id="id"/>
						<div class="form-group">
							<label class="col-md-2 control-label">类型<span id="span_id_4" style="color:red;">*</span></label>
							<div class="col-md-10">
								<select id="type" name="type" class="selector_type form-control" onchange="getTarget();">
									<#if tab=1||tab=4||tab=5||tab=9><option value="5">推广</option></#if>
									<#if tab!=8&&tab!=9><option value="10">官方赛</option></#if>
									<#if tab=1||tab=4||tab=5><option value="13">福利</option></#if>
									<#if tab=1><option value="14">android下载推广</option></#if>
									<#if tab=1><option value="15">资讯</option></#if>
									<#if tab=1><option value="16">自发赛</option></#if>
									<#if tab=1><option value="17">悬赏令</option></#if>
								</select>
							</div>
						</div>
						<div class="form-group" id="targetDiv">
							<label class="col-md-2 control-label">目标对象<span id="span_id_1" style="color:red;">*</span></label>
							<div class="col-md-10">
								<select id="targetId" name="targetId" class="chosen-select selector_type form-control">
									<option value="">--请选择--</option>
								</select>
							</div>
						</div>
						<div class="form-group" id="titleDiv">
							<label class="col-md-2 control-label">标题</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="title" value="" maxlength="25">
							</div>
						</div>
						<div class="form-group" id="describeDiv">
							<label class="col-md-2 control-label">描述</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="describe" value="">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">图片<span id="span_id_3" style="color:red;">*</span></label>
							<div class="col-md-10">
							    
								<input type="file" id="imgFile" name="imgFile" value="图片" accept="image/*">
								<font class="prompt">
									请上传64:31宽高比的图片，以达到最佳显示效果
								</font>
							</div>
						</div>
						<div class="form-group" id="urlDiv">
							<label class="col-md-2 control-label">URL</label>
							<div class="col-md-10">
								<input class="form-control" type="text" id="url" name="url" value="">
							</div>
						</div>
						<div class="form-group" id="deviceType">
							<label class="col-md-2 control-label">设备类型<span id="span_id_6" style="color:red;">*</span></label>
							<div class="col-md-10">
								<select id="select_id_deviceType" name="deviceType" class="selector_deviceType form-control">
									<option value="0">全部</option>
									<option value="1">IOS</option>
									<option value="2">Android</option>
								</select>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">排序(数字越大优先显示)</label>
							<div class="col-md-10">
								<input class="form-control" type="text" id="sort" name="sort" value="" min="0" oninput="if (! /^\d+$/ig.test(this.value)){this.value='';}">
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
	
	// 显示新增
	var tab=${tab};
	$('button[add]').on('click', function(event) {
		$('#id').val('');
		if(tab==2||tab==3||tab==6||tab==7){getTarget(10);}
		else if(tab==8){
		getTarget(11);
		}
		else{getTarget(5);}
		$.fillForm({
			imgFile: '',
		}, _$editor);
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
		var category = $(this).attr('category');
		getTarget(category);
		setTypeDisplay(category);
		$.api('${ctx}/appRecommend/detail?tab=${tab}&id=' + id+'&type='+category, {}, function(d) {
			var o = d.object;
			$.fillForm({
				type: o.type,
				targetId:o.targetId,
				title: o.title,
				describe: o.describe,
				url: o.url,
				deviceType: o.deviceType,
				sort: o.sort,
				imgFile:o.img,
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
	    var url='${ctx}/appRecommend/save?tab='+${tab};
		_$editor.find('form').ajaxSubmit({
			url:url,
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
			$.api('${ctx}/appRecommend/delete/' + id+'?tab=${tab}', {}, function(d) {
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
	
	function getTarget(type){
		$("#deviceType").show();
		if(type==undefined){
			type=$("#type").val();
			if(type==14){
				$("#deviceType").hide();
			}
		}
		setTypeDisplay(type);
		if(type==""){
			$("#targetId").html("");
		}else if(type==10||type==11||type==16||type==17){
			$.ajax({
		    type : 'post',
		    url : '${ctx}/appRecommend/activityList?type='+type+'&tab='+${tab},
		    dataType : 'json',
		    async: false, 
			    success : function(data) {
			    	$("#targetId").html("");
				    for(var i=0;i<data.length;i++){
				    $("#targetId").append('<option value="'+data[i].id+'">'+data[i].title+'</option>');
				    }
			    }
		    })
	    }else if(type==15){
			$.ajax({
			    type : 'post',
			    url : '${ctx}/appRecommend/activityList?type='+type+'&tab='+${tab},
			    dataType : 'json',
			    async: false, 
			    success : function(data) {
				    $("#targetId").html("");
				    for(var i=0;i<data.length;i++){
				    	$("#targetId").append('<option value="'+data[i].id+'">'+data[i].title+'</option>');
				    }
				    $('#targetId').chosen({
					    no_results_text: '没有找到',    // 当检索时没有找到匹配项时显示的提示文本
					    disable_search_threshold: 10, // 10 个以下的选择项则不显示检索框
					    search_contains: true         // 从任意位置开始检索
					});
			    }
		    })
	    }
    }
    $("#targetDiv").hide();
    function setTypeDisplay(type){
	    if(type==10||type==11||type==15||type==16||type==17){
			$("#targetDiv").show();
			$("#titleDiv").hide();
			$("#describeDiv").hide();
			$("#urlDiv").hide();
		}else{
			$("#targetDiv").hide();
			$("#titleDiv").show();
			$("#describeDiv").show();
			$("#urlDiv").show();
		}
    }
	</script>
</body>
</html>
