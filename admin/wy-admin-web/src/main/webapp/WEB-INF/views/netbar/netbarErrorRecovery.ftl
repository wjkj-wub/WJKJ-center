<#import "/macros/pager.ftl" as p >
<html>
<head>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
<style type="text/css">
body, html{width: 100%;height: 100%;margin:0;font-family:"微软雅黑";}
#allmap{height:500px;width:100%;}
.carrousel {
  background-color: rgba(10, 10, 10, 0.8);
  display: none;
  position: fixed;
  top:0;
  left:0;
  width:100%;
  z-index:1000;
  height:100%;
}
.carrousel .wrapper {
  width: 600px;
  height: 600px;
  overflow: hidden;
}
.carrousel .wrapper > img {
  width: 100%;
  margin:auto;
}
.carrousel, .carrousel .wrapper {
  margin: auto;
}
</style>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>app网吧信息管理</strong>
 		</li>
		<li class="active">
			纠错审核以及奖励发放
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10" id="modal-editor">
		<form id="search" action="1" method="post">
			<div class="col-md-2">
				<input type="text" class="form-control" name="name" id="name" placeholder="手机号码|网吧名" />
			</div>
			<div class="col-md-2">
				<select class="form-control" name="province" id="province">
				    <option value="0">全部省</option>
			    	<#if provinceList??>
						<#list provinceList as param>
						    <option value=${param.areaCode!}>${param.name!}</option>
						</#list>
					</#if>
				</select>
			</div>
			<div class="col-md-2">
				<select class="form-control" name="city" id="city">
				    <option value="0">全部市</option>
				    <#if cityList??>
						<#list cityList as param>
						    <option value=${param.areaCode!}>${param.name!}</option>
						</#list>
					</#if>
				</select>
			</div>
			<div class="col-md-2">
				<select class="form-control" name="area" id="area">
				    <option value="0">全部区/县</option>
				    <#if areaList??>
						<#list areaList as param>
						    <option value=${param.areaCode!}>${param.name!}</option>
						</#list>
					</#if>
				</select>
			</div>
			<div class="col-md-2">
				<select class="form-control" name="state" id="state">
				    <option value="-1">审核状态</option>
				    <option value="0">待审核</option>
				    <option value="1">已审核</option>
				    <option value="2">已忽略</option>
				</select>
			</div>
			<button type="button" onclick="formSubmit()" class="btn btn-success">查询</button>
		</form>
	</div>
	
	<div class="mb10">
		<div class="col-md-10">
			未处理:${(params.noChannelNum)!0} 已处理:${(params.channelNum)!0} 总提交数:${(params.allNum)!0}
		</div>
		<div class="col-md-2">
			<button type="button" class="btn btn-success" onclick="batchChange(1)">批量采纳</button>
			<button type="button" class="btn btn-success" onclick="batchChange(2)">批量忽略</button>
		</div>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover" id="listContent">	
		<tr>
			<th><label><input type="checkbox" id="checkAll"></label></th>
			<th>ID</th>
			<th>网吧名称</th>
			<th>网吧状态</th>
			<th>所在省</th>
			<th>所在城市</th>
			<th>所在区县</th>
			<th>网吧地址</th>
			<th>网吧图片</th>
			<th>审核状态</th>
			<th>操作</th>
		</tr>
	    <#if list??>
			<#list list as param>
				<tr>
					<td rowspan='2'><input type="checkbox" nerID="${param.id!}" class="checkSelf" <#if ((param.status)!0)!=0>disabled="disabled"</#if>></td>
					<td rowspan='2'>${param.id!}</td>
					<td>${param.bname!"--"}</td>	
					<td rowspan='2'><#if ((param.isRelease)!0)==1>已发布<#else>未发布</#if></td>
					<td>${param.bprovince!"--"}</td>
					<td>${param.bcity!"--"}</td>
					<td>${param.barea!"--"}</td>
					<td><a href="javascript:void(0)" onclick="theLocation(${param.blongitude!""},${param.blatitude!""})">${param.baddress!"--"}</a></td>
					<td><img src='${param.bimg!""}' style="max-width:50px"></td>
					<td rowspan='2'>
					 <#switch param.status>
				           <#case 0>
				                                         待审核
				              <#break>
				           <#case 1>
				       		     已采纳	
				              <#break>
				           <#case 2>
				                                          已忽略
				              <#break>
				           <#default>
				                                        待审核
			        </#switch>
					</td>
					<#if ((param.status)!0)==0>
						<td><button type="button" class="btn btn-info" onclick="changeStatus(1,${param.id!})">采纳</button></td>
					<#else>
						<td><button type="button" class="btn btn-info" disabled="disabled">采纳</button></td>
					</#if>
				</tr>
				<tr>
					<td>${param.name!"--"}</td>	
					<td>${param.province!"--"}</td>
					<td>${param.city!"--"}</td>
					<td>${param.area!"--"}</td>
					<td><a href="javascript:void(0)" onclick="theLocation(${param.longitude!""},${param.latitude!""})">${param.address!"--"}</a></td>
					<td class="portrait"><img class="pic" data-src-wide='${param.img!""}' src='${param.img!""}' style="max-width:50px"></td>
					<#if ((param.status)!0)==0>
						<td><button type="button" class="btn btn-info" onclick="changeStatus(2,${param.id!})">忽略</button></td>
					<#else>
						<td><button type="button" class="btn btn-info" disabled="disabled">忽略</button></td>
					</#if>
				</tr>
			</#list>
		</#if>
	</table>
	<div id="modal-editor2" class="modal fade">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideEditor()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title">百度地图</h4>
				</div>
				<div class="modal-body">
					<div id="allmap"></div>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" onclick="hideEditor()">关闭</button>
					<button id="save-rule" type="button" class="btn btn-primary">保存</button>
				</div>
			</div>
		</div>
	</div>
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage /> 
	<script type="text/javascript" src="http://api.map.baidu.com/api?v=2.0&ak=mcDGbWtXbxVhOSkKRsboFTjG"></script> 
	<script type="text/javascript">
	// 初始化模态框
	var _$editor = $('#modal-editor');
	var $editor = $('#modal-editor2');
	$editor.modal({
		keyboard : false,
		show : false
	})
	
	// 关闭模态框
	function hideEditor() {
		$editor.modal('hide');
	}
	
	// 初始化编辑窗口宽度
	function showEditor() {
		var width = 1000;
		var height = $(window).height() - 400;
		if(width > $(window).width()) {
			width = $(window).width();
		}
		$editor.find('.modal-dialog').css('width', width);
		$editor.find('.modal-body').css({
			height: height,
			overflow: 'scroll',
		});
		$editor.modal('show', 'fit');
	}
	
	// 初始化编辑框
	$.fillForm({
		name: '${(params.name)!}',
		province: '${(params.provinceVal)!"0"}',
		area: '${(params.areaVal)!"0"}',
		city: '${(params.cityVal)!"0"}',
		state: '${(params.state)!"-1"}',
	}, _$editor);
	
	//百度地图定位
	function theLocation(longitude,latitude){
		showEditor();
		var map = new BMap.Map("allmap");
		map.centerAndZoom(new BMap.Point(116.331398,39.897445),19);
		map.enableScrollWheelZoom(true);
		map.clearOverlays(); 
		var new_point = new BMap.Point(longitude,latitude);
		var marker = new BMap.Marker(new_point);  // 创建标注
		map.addOverlay(marker);              // 将标注添加到地图中
		map.panTo(new_point);      
	}
	
	//表单提交
	function formSubmit(){
		var provinceVal = $("#province").val();//省
		var areaVal = $("#area").val();//市
		var cityVal = $("#city").val();//区
		var name = $("#name").val();
		var state = $("#state").val();
		window.location.href="/netbarErrorRecovery/list/1?name="+name+"&state="+state+"&provinceVal="+provinceVal+"&areaVal="+areaVal+"&cityVal="+cityVal;
	}
	
	$(function(){
		$("#checkAll").change(function(){
			if(!$(this).is(':checked')){
				$(".checkSelf").each(function(){
					$(this).removeAttr('checked');
				})	
			}else{
				$(".checkSelf").each(function(){
					$(this).attr('checked',true);
				})	
			}
		})
		var c = '<div class="carrousel"><div class="wrapper"><img src="" alt="BINGOO" /></div></div>';
		$("body").append(c);
		var carrousel = $(".carrousel");
		$(".portrait").click(function(e){
		  var src = $(this).find(".pic").attr("data-src-wide");
		  carrousel.find("img").attr("src",src);
		  carrousel.fadeIn(200);
		});
		carrousel.click(function(e){
		  carrousel.find("img").attr("src",'');
		  carrousel.fadeOut(200);
		} );
		$("#province").change(function(){ //获得全部市
			var provinceValue = $(this).val();
			$.ajax({
				url:'/area/areaInfo?areaCode='+provinceValue,
				type:"get",
				dataType: "json",
				success:function(d){
					if(d.code == 0){
						$("#city").children().remove();
						$("#city").append("<option value='0'>全部市</option>");
						$("#area").children().remove();
						$("#area").append("<option value='0'>全部区/县</option>");
						var a = d.object;
						var code = "";
						var name = "";
						for(var i = 0;i<a.length;i++){
							name = a[i].name;
							code = a[i].areaCode;
							$("#city").append("<option value='"+code+"'>"+name+"</option>");
						}
					}else{
						alert(d.result);
					}
				}
			});
		})
		
		$("#city").change(function(){ //获得全部县
			var cityValue = $(this).val();
			$.ajax({
				url:'/area/areaInfo?areaCode='+cityValue,
				type:"get",
				dataType: "json",
				success:function(d){
					if(d.code == 0){
						$("#area").children().remove();
						$("#area").append("<option value='0'>全部区/县</option>");
						var a = d.object;
						var code = "";
						var name = "";
						for(var i = 0;i<a.length;i++){
							name = a[i].name;
							code = a[i].areaCode;
							$("#area").append("<option value='"+code+"'>"+name+"</option>");
						}
					}else{
						alert(d.result);
					}
				}
			});
		})
	})
	
	function changeStatus(type,id){
		$.ajax({
			url:'/netbarErrorRecovery/changeStatus?type='+type+'&id='+id,
			success:function(d){
				if(d.code==0){
					window.location.reload();
				}else{
					alert(d.result);
				}
			}
		});
	}
	
	
	function batchChange(type){
		var checkAll=false;
		var checkIds="";
		if(!checkAll){
			$(".checkSelf").each(function(){
				if($(this).is(":checked")){
					checkIds+=$(this).attr("nerId")+",";
				}
			});
		}
		$.ajax({
			url:'/netbarErrorRecovery/batchChange?type='+type+'&checkIds='+checkIds,
			success:function(d){
				if(d.code==0){
					window.location.reload();
				}else{
					alert(d.result);
				}
			}
		});
	}
	</script>
</body>
</html>