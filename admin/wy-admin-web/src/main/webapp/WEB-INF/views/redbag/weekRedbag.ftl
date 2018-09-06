<#assign ctx = requestContext.contextPath>
<#import "/macros/pager.ftl" as p >
<html>
<head>
<script type="text/javascript" src="${ctx}/static/js/highcharts/js/highcharts.js"></script>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>红包管理</strong>
 		</li>
 		<li>
   			<strong>统计报表</strong>
 		</li>
		<li class="active">
			每周抢红包统计
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form action="weekRedbag" method="post">
				前几周:<input type="number" name="week" value="${week!}" placeholder="前几周" />
			<button type="submit" class="btn btn-success">查询</button>
		</form>
	</div>
	
	<#-- 每周抢红包总个数和使用个数 -->
	<div class="mb10">
		<div id="container1">
		</div>
	</div>
	
	<#-- 每周抢红包总金额和使用金额 -->
	<div class="mb10">
		<div id="container2">
		</div>
	</div>
<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
<script type="text/javascript">
$('#container1').highcharts({
        chart: {
            type: 'line'
        },
        title: {
            text: '每周抢红包总个数和使用个数'
        },
        xAxis: {
            categories: ${categories}
        },
        yAxis: {
            title: {
                text: '个数'
            }
        },
        tooltip: {
            enabled: false,
            formatter: function() {
                return '<b>'+ this.series.name +'</b><br/>'+this.x +': '+ this.y +'个';
            }
        },
        plotOptions: {
            line: {
                dataLabels: {
                    enabled: true
                },
                enableMouseTracking: false
            }
        },
        series: ${numData}
});	

$('#container2').highcharts({
        chart: {
            type: 'line'
        },
        title: {
            text: '每周抢红包总金额和使用金额'
        },
        xAxis: {
            categories: ${categories}
        },
        yAxis: {
            title: {
                text: '金额'
            }
        },
        tooltip: {
            enabled: false,
            formatter: function() {
                return '<b>'+ this.series.name +'</b><br/>'+this.x +': '+ this.y +'元';
            }
        },
        plotOptions: {
            line: {
                dataLabels: {
                    enabled: true
                },
                enableMouseTracking: false
            }
        },
        series: ${moneyData}
});			
</script>
</body>
</html>