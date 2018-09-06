<#import "/macros/pager.ftl" as p >
<html>
<head>
<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
<script type="text/javascript" src="${ctx}/static/plugin/clipboard/ZeroClipboard.js"></script>
</head>
<body>
<#-- 导航 -->
<ul class="breadcrumb">
	<li>
		<i class="icon icon-location-arrow mr10"></i>
		<strong>官方活动管理</strong>
	</li>
	<li class="active">
		官方抢皮肤管理
	</li>
</ul>
<#-- 搜索 -->
<div class="mb12">
	<div class="mb10 col-md-10">
		<form id="search" action="/cohere/activity/list/1" method="post">
			<div class="mb12 col-md-12">
				<div class="col-md-3">
					<input class="form-control" type="text" name="findTitle" maxlength="20" placeholder="标题"  value="${(params.findTitle)!}"/>
				</div>
				<div class="col-md-3">
					<input class="form-control" type="text" id="startTimeBegin" name="startTimeBegin" maxlength="30" placeholder="创建时间(起)" value="${(params.startTimeBegin)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss',maxDate:'#F{$dp.$D(\'startTimeBegin\',{d:0});}'})" />
				</div>
				<div class="col-md-3">
					<input class="form-control" type="text" id="startTimeEnd" name="startTimeEnd" maxlength="30" placeholder="创建时间(止)" value="${(params.startTimeEnd)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss',minDate:'#F{$dp.$D(\'startTimeEnd\',{d:0});}'})" />
				</div>
				<div class="col-md-2">
					<select class="form-control" name="state">
						<option value="">全部状态</option>
						<option value="1"<#if (params.state!"0") == "1"> selected</#if>>未发布</option>
						<option value="2"<#if (params.state!"0") == "2"> selected</#if>>进行中</option>
						<option value="3"<#if (params.state!"0") == "3"> selected</#if>>已过期</option>
					</select>
				</div>
				<div class="col-md-1">
					<div class="col-md-6">
						<button class="btn btn-info" type="submit">提交</button>
					</div>
				</div>
			</div>
		</form>
	</div>	
	<div class="mb2 col-md-2">
		<div class="col-md-12">
			<a class="btn btn-success" href="${ctx}/cohere/activity/edit/activity" target="_self">新增活动</a>
		</div>
	</div>
</div>
<article id="editor" style="margin-top:50px">
	<div class="col-md-12" style="margin-bottom:10px">
	<#if list??>
		<#list list as param>
		<div class="mb1 col-md-2">活动ID：${param.id!}</div>
		<div class="mb2 col-md-3">活动标题：${param.title!}</div>
		<div class="mb2 col-md-3">活动时间：${param.begin_time!} -- ${param.end_time!}</div>
		<#switch param.state>
		  <#case 1>
			<div class="mb1 col-md-1">状态： 未发布</div>
		    <#break>
		  <#case 2>
			<div class="mb1 col-md-1">状态： 进行中</div>
		    <#break>
		  <#case 3>
			<div class="mb1 col-md-1">状态： 已过期</div>
		    <#break>
		  <#default>
		</#switch>
		<div class="mb2 col-md-3">最后修改时间：${param.update_date!}</div>
		<div class="col-md-5">
			<table class="table table-striped table-hover">	
				<#if param.debriss??>
					<#list param.debriss as debris>
					<tr>
						<#if debris_index == 0>
							<th>碎片一</th>
						<#elseif debris_index == 1>
							<th>碎片二</th>
						<#elseif debris_index == 2>
							<th>碎片三</th>
						<#elseif debris_index == 3>
							<th>碎片四</th>
						</#if>
						<th>${debris.sendNum!}/<#if debris.counts == -99>无穷<#else>${debris.counts!}</#if></th>
						<th>${(debris.probability)!"0"}%</th>
					</tr>
					</#list>
				</#if>
			</table>
		</div>
		<div class="col-md-5">
			<table class="table table-striped table-hover">	
				<#if param.prizes??>
					<#list param.prizes as prize>
					<tr>
						<#if prize_index == 0>
							<th>奖品一</th>
						<#elseif prize_index == 1>
							<th>奖品二</th>
						<#elseif prize_index == 2>
							<th>奖品三</th>
						<#elseif prize_index == 3>
							<th>奖品四</th>
						</#if>
						<th>${prize.sendNum!}/<#if prize.counts == -99>无穷<#else>${prize.counts!}</#if></th>
						<th>${(prize.probability)!"0"}%</th>
					</tr>
					</#list>
				</#if>
			</table>
		</div>
		<div class="col-md-2" style="margin-bottom:12px">
			<div class="col-md-6">
				<div class="col-md-12" style="line-height:78px">
					<button class="btn btn-info btn-lg" style="margin-left:15px" onclick="javascript:window.location.href='/cohere/activity/edit/probability?activityId=${param.id!}'">编辑</button>
				</div>
				<div class="col-md-12" style="line-height:78px">
					<button class="btn btn-info btn-lg" onclick="javascript:window.location.href='/cohere/activity/statistics?activityId=${param.id!}'">查看统计</button>
				</div>
			</div>
			<div class="col-md-6" style="line-height:147px">
				<a class="btn btn-info btn-lg" href="/cohere/activity/1?activityId=${param.id!}">发奖管理</a>
			</div>
		</div>
		</#list>
	</#if>
	</div>
</article>
<#-- 分页 -->
<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />

</body>
</html>