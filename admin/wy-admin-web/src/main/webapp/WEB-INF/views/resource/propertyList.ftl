<#import "/macros/pager.ftl" as p >
<html>
<head>
<link rel="stylesheet" href="${ctx}/static/lib/chosen/chosen.css" />
<script type="text/javascript" src="${ctx}/static/lib/chosen/chosen.min.js"></script>
<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
<script language="JavaScript" type="text/javascript" src="${ctx}/static/js/highcharts/js/highcharts.js"></script>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>资源商城</strong>
 		</li>
		<li class="active">
			商城项目管理
		</li>
	</ul>
	
	<div class="mb10">
	<strong>地区配额设置(每周五0点定时更新设置的新配额)：</strong>
	</div>
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>地区</th>
			<th>进行中会员奖金(%)</th>
			<th>设置新会员奖金(%)</th>
			<th>进行中黄金奖金(%)</th>
			<th>设置新黄金奖金(%)</th>
			<!--<th>进行中钻石奖金(%)</th>
			<th>设置新钻石奖金(%)</th>-->
			<th>操作</th>
		</tr>
		<#if ratio??>
		<#list ratio as r>
			<tr>
			<td><a class="area" area="${r.area_code}" name="${r.name!}">${r.name!}</a></td>
			<td>${r.vip_ratio!}</td>
			<td><input type="text" class="ratio" id="${r.area_code}_v" value="${r.next_vip_ratio!}" placeholder="${r.next_vip_ratio!}"/></td>
			<td>${r.gold_ratio!}</td>
			<td><input type="text" class="ratio" id="${r.area_code}_g" value="${r.next_gold_ratio!}" placeholder="${r.next_gold_ratio!}"/></td>
			<!--<td>${r.jewel_ratio!}</td>
			<td><input type="text" class="ratio" id="${r.area_code}_j"/></td>-->
				<td>
					<button type="button" class="confirm btn btn-success" code="${r.area_code}">确定</button>
				</td>
			</tr>
		</#list>
		</#if>
	</table>
	<hr/>
	<div class="mb10">
	<strong>商品类别设置：</strong>
	<button add="" type="button" class="btn btn-success">新增</button><br/><hr/>
	<#list category as c>
	<div class="mb10"><strong>大类:${c.parent.name!}</strong><button edit="${c.parent.id}" type="button" class="btn btn-success">编辑</button></div>
	<table class="table table-striped ">
	<tr>
			<th>细分类名</th>
			<th>是否app端展示</th>
			<#--<th>操作</th>-->
	</tr>
	
	<#list c.sub as s>
	<tr>
	<td>${s.name}</td>
	<td><#if s.isShowApp=1>是<#else>否</#if></td>
	<#-- <td><button edit="${s.id}" type="button" class="btn btn-success">编辑</button>&nbsp;&nbsp;<button del="${s.id}" type="button" class="btn btn-success">删除</button><br></td>-->
	</tr>
	</#list>
	</table>
	<hr/>
	</#list>
	</div>
	
	<#--新增-->
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
						<div class="form-group" id="selectType">
							<label class="col-md-2 control-label">选择大类<span id="span_id_4" style="color:red;">*</span></label>
							<div class="col-md-10">
								<select id="pid" name="pid" onchange="toggleDiv();">
								<#list parent as p>
								<option value="${p.id}">${p.name}</option>
								</#list>
								<option value="0">无</option>
								</select>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">名称<span id="span_id_4" style="color:red;">*</span></label>
							<div class="col-md-10">
								<input id="name" type="text" name="name" >
							</div>
						</div>
						<div class="form-group" id="app">
							<label class="col-md-2 control-label">app端是否显示<span id="span_id_4" style="color:red;">*</span></label>
							<div class="col-md-10">
								<input type="checkbox" name="isShowApp" value="1">
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
	
	<#-- 
		<div id="modal-editor-area" class="modal fade">
			<div class="modal-dialog">
				<div class="modal-content">
					<div id="container"></div>
				</div>
			</div>
		</div>
	-->
<script>
$(".confirm").click(function(){
var code=$(this).attr("code");
var ratio_v=$("#"+code+"_v").val();
var ratio_g=$("#"+code+"_g").val();

if(ratio_v!=""&&ratio_g!=""){
	$.ajax({
		   type : 'post',
		   url : '${ctx}/netbar/resource/property/updateRatio',
		   data: {
		    areaCode: code,
		    vip_ratio:ratio_v,
		    gold_ratio:ratio_g
		   },
		   cache : false,
		   dataType : 'json',
		   success : function(data) {
			   if(data==1){
				   $(".ratio").val("");
				   window.location.reload();
			   }
		   }
	 })
 }else{
 alert("请同时填写会员网吧和黄金网吧配额,如果不需要改变,请填写和正在使用的配额")}
})


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
	$('button[add]').on('click', function(event) {
	$("#selectType").show();
	$('#id').val('');
	$('#pid').val('0');
	$('#name').val('');
	$("#pid").removeAttr("disabled");
		_$editor.modal('show');
	});
	
	function toggleDiv(){
	var pid=$("#pid").val();
	if(pid==0)
	$("#app").hide();
	else
	$("#app").show();
	}
	
	function submitEditor() {
		var catName = $("#name").val();
		if(!catName){
			alert("类别名称不能为空");
			return;
		}
		_$editor.find('form').ajaxSubmit({
			url:'${ctx}/netbar/resource/property/save',
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
		$("#selectType").show();
		var id = $(this).attr('edit');
		$("#pid").attr("disabled","disabled");
		$.api('${ctx}/netbar/resource/property/detail?id=' + id, {}, function(d) {
			var o = d.object;
			if(o.pid==0){
			$("#selectType").hide();
			}
			$.fillForm({
				pid: o.pid,
				name:o.name,
				isShowApp: o.isShowApp,
				id: id,
			}, _$editor);
		}, function(d) {
			alert('请求数据异常：' + d.result);
		}, {
			async: false,
		});
		_$editor.modal('show');
	});
	
	//function createCharts(areaCode,name) {
	//	$("#container").html("图表加载中...<img src='${ctx}/style/m/images/waiting.gif'/>");
	//	$.ajax({
	//		url : "${ctx}/netbar/resource/property/trend?areaCode="+areaCode,
	//		type : "POST",
	//		dataType : "json",
	//		success : function(data) {
	//		if(data.status=="success"){
	//			$('#container').highcharts({
	//		        title: {
	//		            text:name+'网吧奖金最近七次趋势图'
	//		        },
	//		        xAxis: {
	//		            categories: data.categories,
	//		            labels: { rotation: -45, align: 'right', style: { fontSize: '13px', fontFamily: 'Verdana, sans-serif' } }
	//		        },
	//		        yAxis: {
	//		            min: 0,
	//		            allowDecimals:false,
	//		            title: {
	//		                text: ''
	//		            }
	//		        },
	//		         tooltip: { valueSuffix: '%' }, legend: { layout: 'vertical', align: 'right', verticalAlign: 'middle', borderWidth: 0 },
	//		        plotOptions: {
	//		            column: {
	//		                pointPadding: 0.2,
	//		                borderWidth: 0
	//		            }
	//		        },
	//		        series: data.series
	//		    });
	//		}else{
	//	
	//	}
	//		}
	//	});
	//}
	
	
	//$(".area").click(function(){
    //createCharts($(this).attr("area"),$(this).attr("name"));
    //$('#modal-editor-area').modal('show');
    //})
    
    
    // 删除
	$('button[del]').on('click', function(event) {
		var _this = $(this);
		_this.attr('disabled', true);
		
		var id = $(this).attr('del');
		
		$.confirm('确认删除吗?', function() {
			$.api('${ctx}/netbar/resource/property/del/' + id, {}, function(d) {
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
</script>
	
</body>
</html>