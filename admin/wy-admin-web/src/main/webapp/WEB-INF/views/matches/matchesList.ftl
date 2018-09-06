<#import "/macros/pager.ftl" as p >
<html>
<head>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>管理系统</strong>
 		</li>
		<li class="active">
			官方赛事管理
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
			<div class="col-md-2">
				<input type="text" class="form-control" name="title" placeholder="赛事名" value="${(params.title)!}" />
			</div>
			<div class="col-md-2">
				<select class="form-control" name="organiserId">
					<option value="">主办方</option>
					<#if organiserList??>
					<#list organiserList as i>
						<option value="${i.id!}"<#if (params.organiserId)?? && params.organiserId == i.id> selected</#if>>${i.name!}</option>
					</#list>
					</#if>
				</select>
			</div>
			
			<div class="col-md-2">
				<select class="form-control" name="itemsId">
					<option value="">游戏</option>
					<#if activityItemList??>
					<#list activityItemList as i>
						<option value="${i.id!}"<#if (params.itemsId)?? && params.itemsId == i.id> selected</#if>>${i.name!}</option>
					</#list>
					</#if>
				</select>
			</div>
			
			<div class="col-md-2">
				<input class="form-control" type="text" id="search-beginDate" name="beginDate" placeholder="最早开始时间" value="${(params.beginDate)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',maxDate:'#F{$dp.$D(\'search-endDate\',{d:0});}'})">
			</div>
			<div class="col-md-2">
				<input class="form-control" type="text" id="search-endDate" name="endDate" placeholder="最晚结束时间" value="${(params.endDate)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',minDate:'#F{$dp.$D(\'search-beginDate\',{d:0});}'})">
			</div>
			<div class="col-md-2">
				<select class="form-control" name="state">
					<option value="">全部状态</option>
					<option value="1">在线</option>
					<option value="0">下线线</option>
				</select>
			</div>
			<button type="submit" class="btn btn-success">查询</button>
		</form>
	</div>
	
	<#-- 操作按钮 -->
	<div class="mb10">
		<a href="${ctx!}/matches/edit" type="button" class="btn btn-success">新增赛事</a>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>#</th>
			<th>赛事名称</th>
			<th>比赛周期</th>
			<th>主办方</th>
			<th>游戏名称</th>
			<th>赛点数量</th>
			<th>覆盖省份数量</th>
			<th>当前进度</th>
			<th>当前状态</th>
			<th>操作</th>
		</tr>
		<#if list??>
			<#list list as o>
				<tr>
					<td>${o.id!}</td>
					<td>${o.title!}</td>
					<td>${(o.start_date?string("yyyy-MM-dd"))!}~${(o.end_date?string("yyyy-MM-dd"))!}</td>
					<td>${(o.organiser)!}</td>
					<td>${(o.gameName)!}</td>
					<td>${o.cenueCount!}</td>
					<td>${o.provinceCount!}</td>
					<td><#if (o.process)??> ${o.process!}<#else>已结束</#if></td>
					<td><#if (o.state!0) ==1>在线<#else>下线</#if></td>
					<td>
						<#if (o.state!0) ==1>
							<button offline="${o.id!}" type="button" class="btn btn-info">下线</button>
						<#else>
							<button online="${o.id!}" type="button" class="btn btn-info">上线</button>
						</#if>
						<a href="${ctx!}/matches/edit?matchesId=${o.id!}" type="button" class="btn btn-danger">编辑</a>
						<button remove="${o.id!}" type="button" class="btn btn-info">维护主题</button>
					</td>
				</tr>
			</#list>
		</#if>
	</table>
	
		<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	<script type="text/javascript">
		$('button[online]').on('click', function(event) {
			$.ajax({
				url: '${ctx!}/matches/changeStatus?matchesId='+$(this).attr('online')+'&type=1',
				success: function(d) {
					if(d.code==0){
						window.location.reload();
					}else{
						alert(d.result);
					}
				},
			});
		});
		$('button[offline]').on('click', function(event) {
			$.ajax({
				url: '${ctx!}/matches/changeStatus?matchesId='+$(this).attr('offline')+'&type=2',
				success: function(d) {
					if(d.code==0){
						window.location.reload();
					}else{
						alert(d.result);
					}
				},
			});
		});
	</script>
</body>
</html>