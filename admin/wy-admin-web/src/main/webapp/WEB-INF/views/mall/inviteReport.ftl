<#assign ctx = requestContext.contextPath>
<#import "/macros/pager.ftl" as p >
<html>
<head>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
<script language="JavaScript" type="text/javascript" src="${ctx}/static/js/highcharts/js/highcharts.js"></script>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>首页</strong>
 		</li>
		<li class="active">
			邀请人数统计
		</li>
	</ul>
	
	<#-- 折线图 -->
	<div class="mb10">
		<form id="report-param" action="invite" method="post">
			<input type="hidden" name="lotteryId" value="${lotteryId!}" />
			<div class="col-md-2">
				<input type="text" class="form-control" name="beginDate" placeholder="开始时间" value="${params.beginDate!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:beginPick})" />
			</div>
			<div class="col-md-2">
				<input type="text" class="form-control" name="endDate" placeholder="结束时间" value="${params.endDate!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:endPick})" />
			</div>
			<div class="col-md-2">
				<select name="order" class="form-control">
					<option value="1"<#if ((params.order)!"1") == "1"> selected</#if>>邀请数</option>
					<option value="2"<#if ((params.order)!"1") == "2"> selected</#if>>注册数</option>
				</select>
			</div>
			<div class="col-md-2">
				<input type="text" class="form-control" name="threshold" value="${params.threshold!}" placeholder="阈值" />
			</div>
			<div class="col-md-2">
				<input type="text" class="form-control" name="limit" value="${params.limit!10}" placeholder="查询数量" />
			</div>
			<div class="col-md-2">
				<button id="submit" type="submit" class="btn btn-success">查询</button>
			</div>
		</form>
	</div>
	<div id="report-coin" class="mb10">
		<div id="prompt"></div>
		<div id="container"></div>
	</div>
	
<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
<script type="text/javascript">
	// 初始化数据
	var data = <#if !statis?? || statis == "null">[]<#else>${statis!'[]'}</#if>;
	var categories = [];
	var counts = [];
	var registerCounts = [];
	
	for(var i=0; i<data.length; i++) {
		var d = data[i];
		categories.push(':username'.replace(':username', d.username).replace(':nickname', d.nickname));
		counts.push(d.count);
		registerCounts.push(d.registerCount);
	}
	
	// 初始化报表
	$('#report-coin #container').highcharts({
		chart: {
            type: 'column'
        },
        title: {
            text: '邀请状况统计'
        },
        xAxis: {
            categories: categories
        },
        yAxis: {
            title: {
                text: '金币数'
            }
        },
        credits: {
            enabled: false
        },
        series: [{
            name: '邀请人数',
            data: counts
        }, {
            name: '注册人数',
            data: registerCounts
        }]
    });
</script>
</body>
</html>