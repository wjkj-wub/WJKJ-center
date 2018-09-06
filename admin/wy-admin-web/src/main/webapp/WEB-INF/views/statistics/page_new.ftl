<#assign title=''>
<#if type==1>
	<#assign title = '合作网吧趋势统计图'>
<#elseif type==2>
	<#assign title = '开通支付网吧趋势统计图'>
<#elseif type==3>
	<#assign title = '已支付网吧趋势统计图'>
<#elseif type==4>
	<#assign title = '新增用户趋势统计图'>
<#elseif type==5>
	<#assign title = '活跃用户趋势统计图'>
<#elseif type==6>
	<#assign title = 'app启动次数趋势统计图'>
<#elseif type==7>
	<#assign title = '总用户趋势统计图'>
<#else>
	<#assign title = '合作网吧趋势统计图'>
</#if>
<#assign dataColumnName=''>
<#if type==1>
	<#assign dataColumnName = '合作网吧数'>
<#elseif type==2>
	<#assign dataColumnName = '开通网吧数'>
<#elseif type==3>
	<#assign dataColumnName = '支付网吧数'>
<#elseif type==4>
	<#assign dataColumnName = '新增用户数'>
<#elseif type==5>
	<#assign dataColumnName = '活跃用户数'>
<#elseif type==6>
	<#assign dataColumnName = 'app启动次数'>
<#elseif type==7>
	<#assign dataColumnName = '总用户数'>
<#else>
	<#assign dataColumnName = '合作网吧数'>
</#if>
<#import "/macros/pager.ftl" as p >
<html>
<head>
<link rel="stylesheet" href="${ctx}/static/css/loading.css" />
</head>
<style>
</style>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
			<strong>赛事平台管理系统</strong>
		</li>
		<li class="active">
			${title!}
		</li>
	</ul>

	<#-- 搜索，分类 -->
	<div id="myChart" style="width:1150px;height:300px;margin-top:0;margin-left:50;" ></div>

	<table class="table table-striped table-hover">	
			<tr>
			<th>日期</th>
			<th>${dataColumnName!}</th>
			<#if type==1 || type==2 || type==3>
				<th>累计</th>
			</#if>
		</tr>
		<#if list?? && list?size gt 0>
			<#list list as o>
				<tr>	
					<td>${(o.createDate)!}</td>
					<td>${(o.data)!}</td>
					<#if type==1 || type==2 || type==3>
						<td>${(o.cumulative)!}</td>
					</#if>
				</tr>
			</#list>
		</#if>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	
	<#-- 编辑 -->
	<script type="text/javascript" src="${ctx!}/static/js/echarts.min.js"></script>
	<script type="text/javascript" src="${ctx!}/static/js/jquery.form.js"></script>
	<script type="text/javascript">
	var myChart = echarts.init(document.getElementById('myChart'));
	var title = '${title!}';
	var dataStr = '${chatData!}';
	var data = JSON.parse(dataStr);
	var datas = [];
	var labels = [];
	
	for(var i=0; i<data.length; i++) {
		var d = data[i];
		datas.push(d.total);
		var date = new Date(d.month).Format('yyyy-MM');
		labels.push(date);
	}
	
	$(document).ready(function(){
		var optionMonth = getOption(datas,labels,title);
		
		myChart.setOption(optionMonth);
		myChart.on('click', function (param) {
			if($("#sonChart").length>0){
				$("#sonChart").remove();
			}
			
			$('.table').attr('style','margin-top: 300px;');
			$("#myChart").append('<div id="sonChart" style="width:1150px;height:300px;margin-top:0;margin-left:50;"></div>');
			var url = '${ctx}/statistics/getDayData';
			var month = param.name;
			var optionDay = "";
			$.get(url,{type:${type},month:month},function(data){
				var dayDatas = [];
				var dayLabels = [];
				for(var i=0; i<data.length; i++) {
					dayDatas.push(data[i].total);
					var date = new Date(data[i].eDay).Format('yyyy-MM-dd');
					dayLabels.push(date);
				}
				var optionDay = getOption(dayDatas,dayLabels,month+title);
				var sonChart = echarts.init(document.getElementById('sonChart'));
				sonChart.setOption(optionDay);
			});
		});
	});

	
	function getOption(datas,dates,titleName){
		option = {
		   title: {
	        text: titleName
	   	  },
	      tooltip: {
	        trigger: 'axis'
	      },
	      grid: {
	        left: '3%',
	        right: '4%',
	        bottom: '3%',
	        containLabel: true
	      },
	      xAxis: {
	          type: 'category',
	          data: dates,
	      },
	      yAxis: {
	        type: 'value'
	      },
	      series: [{
	      	  name : '数量',
	          data : datas,
	          type : 'line',
		      label: {
					normal: {
						color: '#39bbf7',
						show: true,
						position: 'top',
						textStyle:{
							color: '#39bbf7',
						},
					}
				},
				lineStyle:{
					normal:{
						color: '#39bbf7'
					} 
				},
	      }]
		};
		return option;
	}
	</script>
</body>
</html>