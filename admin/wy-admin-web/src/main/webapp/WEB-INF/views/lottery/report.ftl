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
			财务报表
		</li>
	</ul>
	
	<#-- 操作按钮 -->
	<div class="mb10">
		<button type="button" class="btn btn-success" onclick="history.go(-1);">返回</button>
	</div>
	
	<#-- 折线图 -->
	<div id="report-redbag-param" class="mb10">
		<form id="report-param">
			<input type="hidden" name="lotteryId" value="${lotteryId!}" />
			<div class="col-md-2">
				<input type="text" class="form-control" name="beginDate" placeholder="开始时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:beginPick})" />
			</div>
			<div class="col-md-2">
				<input type="text" class="form-control" name="endDate" placeholder="结束时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:endPick})" />
			</div>
			<div class="col-md-2">
				分组：
				<label><input type="radio" name="group" value="1" placeholder="" checked />日期</label>
				<label><input type="radio" name="group" value="2" placeholder="" />月份</label>
				<label><input type="radio" name="group" value="3" placeholder="" />年份</label>
			</div>
			<div class="col-md-2">
				<button id="submit" type="button" class="btn btn-success">查询</button>
			</div>
		</form>
	</div>
	<div id="report-redbag" class="mb10">
		<div id="prompt"></div>
		<div id="container-all"></div>
		<div id="container-win"></div>
	</div>
	
<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
<script type="text/javascript">
	$('input[name="group"]').on('click', function(event) {
		$('input[name="beginDate"]').val('');
	});

	// 初始化红包报表
	var $reportParam = $('#report-param');
	var initChart = function() {
		$('#report-redbag #prompt').html('加载中');
		$reportParam.ajaxSubmit({
			url:'${ctx}/lottery/report/history',
		    type:'post',
		    success: function(d){
		    	initRedbagContainers(d);
		    	$('#report-redbag #prompt').html('');
			}
		});
	}
	$reportParam.find('#submit').on('click', function(event) {
		initChart();
	});
	initChart();
	function initRedbagContainers(d){
		if(d.code == 0) {
			$reportParam.find('[name="beginDate"]').val(d.chart.beginDate);
			$reportParam.find('[name="endDate"]').val(d.chart.endDate);
			$reportParam.find('[name="group"][value="' + d.chart.group + '"]').prop('checked', true);
			
			$('#container-all').highcharts({
		        chart: {
		            type: 'line'
		        },
		        title: {
		            text: '转盘中奖历史'
		        },
		        subtitle: {
		            text: ''
		        },
		        xAxis: {
		            categories: d.chart.dates
		        },
		        yAxis: {
		            title: {
		                text: '数量'
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
		            name: '抽奖数量',
		            data: d.chart.allCount
		        }, {
		            name: '中奖数量',
		            data: d.chart.winCount
		        }]
		    });
			
		}
	}
</script>
</body>
</html>