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
		</tr>
		<#if list?? && list?size gt 0>
			<#list list as o>
				<tr>	
					<td>${(o.createDate)!}</td>
					<td>${(o.data)!}</td>
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
	var dataStr = '${statisticsJSONString!}';
	var data = JSON.parse(dataStr);
	var datas = [];
	var labels = [];
	for(var i=0; i<data.length; i++) {
		var d = data[i];
		datas.push(d.data);
		var date = new Date(d.createDate).Format('yyyy-MM-dd');
		labels.push(date);
	}
	option = {
		title: {
			text: title
		},
		grid: {
			left: '3%',
			right: '4%',
			bottom: '3%',
			containLabel: true
		},
		xAxis : [
			{
				name : '时间',
				nameTextStyle: {
					color: '#ccc',
					fontStyle: 'normal',
					fontFamily: '微软雅黑',
					fontSize: 14,
				},
				nameLocation: 'end',
				nameGap: 1,
				type : 'category',
				boundaryGap : false,
				data : labels,
				axisLabel: {
					show: true,
					textStyle: {
							color: '#969696'
					}
				},
				splitLine: { 
					show:true,// 分隔线
					lineStyle: {// 属性lineStyle（详见lineStyle）控制线条样式
						color: ['#20222d'],
						type: 'solid'
					},
				},
				axisLine: {
					show: true,
					lineStyle: {
						color: '#969696',
						width: 1,
						type: 'solid',
					},
				},
			}
		],
		yAxis : [
			{
				name : '数量',
				nameTextStyle: {
					color: '#ccc',
					fontStyle: 'normal',
					fontFamily: '微软雅黑',
					fontSize: 14,
				},
				nameLocation: 'end',
				nameGap: 5,
					type : 'value',
					splitLine: { 
						show:true,// 分隔线
						lineStyle: {// 属性lineStyle（详见lineStyle）控制线条样式
							color: ['#20222d'],
							type: 'solid'
						},
					},
				axisLine: {
					show: true,
					lineStyle: {
						color: '#969696',
						width: 1,
						type: 'solid',
					},
				},
			}
		],
		series : [
			{
				name:title,
				type:'line',
				stack: title,
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
				data:datas,
			}
		]
	};
	myChart.setOption(option);
	</script>
</body>
</html>