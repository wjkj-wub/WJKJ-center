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
			财务报表
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form action="" method="post">
			<div class="col-md-2">
				<input class="form-control" type="number" name="count" value="${paramCount!}" placeholder="显示天数" />
			</div>
			<button type="submit" class="btn btn-success">查询</button>
		</form>
	</div>
	
	<#-- 分享红包领取情况统计 -->
	<div class="mb10">
		<div id="getted-redbag">
		</div>
	</div>
	
	<#-- 分享红包使用情况统计 -->
	<div class="mb10">
		<div id="used-redbag">
		</div>
	</div>
<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
<script type="text/javascript">
	var gettedRedbagStatis = <#if gettedShareRedbags??>${gettedShareRedbags!}<#else>[]</#if>
	
	var categories = [${count!7}];
	var getCounts = [];
	var getAmounts = [];
	if(gettedRedbagStatis) {
		for(var i = 0; i<gettedRedbagStatis.length; i++) {
			var g = gettedRedbagStatis[i];
			categories[i] = g.date;
			getCounts.push(g.count);
			getAmounts.push(g.amount);
		}
	}
	$('#getted-redbag').highcharts({
        chart: {
            type: 'line'
        },
        title: {
            text: '分享红包领取情况统计'
        },
        subtitle: {
            text: '近${count!7}次'
        },
        xAxis: {
            categories: categories
        },
        yAxis: {
            title: {
                text: '使用量'
            }
        },
        tooltip: {
            enabled: false,
            formatter: function() {
                return '<b>'+ this.series.name +'</b><br>'+this.x +': '+ this.y +'个';
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
        series: [{
            name: '发放个数（个）',
            data: getCounts
        }, {
            name: '发放金额（元）',
            data: getAmounts
        }]
    });
	
	var usedRedbagStatis = <#if usedShareRedbags??>${usedShareRedbags!}<#else>[]</#if>
	
	var useCategories = [${count!7}];
	var useCounts = [];
	var useAmounts = [];
	if(usedRedbagStatis) {
		for(var i = 0; i<usedRedbagStatis.length; i++) {
			var g = usedRedbagStatis[i];
			useCategories[i] = g.date;
			useCounts.push(g.count);
			useAmounts.push(g.amount);
		}
	}
	$('#used-redbag').highcharts({
        chart: {
            type: 'line'
        },
        title: {
            text: '分享红包使用情况统计'
        },
        subtitle: {
            text: '近${count!7}次'
        },
        xAxis: {
            categories: useCategories
        },
        yAxis: {
            title: {
                text: '使用量'
            }
        },
        tooltip: {
            enabled: false,
            formatter: function() {
                return '<b>'+ this.series.name +'</b><br>'+this.x +': '+ this.y +'个';
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
        series: [{
            name: '使用个数（个）',
            data: useCounts
        }, {
            name: '使用金额（元）',
            data: useAmounts
        }]
    });
</script>
</body>
</html>