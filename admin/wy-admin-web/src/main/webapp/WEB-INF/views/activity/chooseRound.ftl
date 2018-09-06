<html>
<head>
	<style type="text/css">
		#export {
			margin: 5px;
		}
		.round:not(:first-child) {
			margin-top: 25px;
		}
		.round > .date {
			display: inline-block;
			width: 100%;
			background-color: rgba(228, 228, 228, 1);
			padding: 2px 25px;
			color: #666;
			font-family: 'PingFangSC-Regular', 'PingFang SC';
			font-weight: 400;
			font-style: normal;
			font-size: 12px;
			margin-bottom: 5px;
		}
		.round > .netbars > a {
			margin-top: 5px;
		}
	</style>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
			<strong>管理系统</strong>
		</li>
		<li class="active">
			赛事管理
		</li>
	</ul>
	
	<!-- 导出按钮 -->
	<div id="export">
		<a id="export" class="btn btn-success" href="javascript:void(0)">导出</a>
	</div>
	<script type="text/javascript">
		$('#export').on('click', function() {
			window.open('${ctx!}/activityInfo/apply/${(isTeam==1)?string('team','personal')}/export/0?actId=${activityId!}');
		});
	</script>
	
	<!-- 场次网吧列表 -->
	<div id="container" class="mb10">
		<#if rounds?? && rounds?size gt 0>
			<div class="rounds mb10">
				<#list rounds as r>
					<div class="round">
						<span class="date">${(r.overTime?string('yyyy.MM.dd'))!}</span>
						<#if (r.netbars)?? && r.netbars?size gt 0>
							<div class="netbars">
								<#list r.netbars as n>
									<a href="${ctx}/activityInfo/apply/<#if isTeam == 1>team<#else>personal</#if>/1?activityId=${(r.activityId)!}&round=${(r.round)!}&netbarId=${(n.id)!}" class="btn btn-success">${(n.name)!}</a>
								</#list>
							</div>
						</#if>
					</div>
				</#list>
			</div>
		<#else>
			<div class="alert alert-warning">暂无比赛信息</div>
		</#if>
	</div>
</body>
</html>