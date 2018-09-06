<#import "/macros/pager.ftl" as p >
<html>
<head>
<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
<script type="text/javascript" src="${ctx}/static/plugin/clipboard/ZeroClipboard.js"></script>
<script type="text/javascript" src="${ctx}/static/css/main.css"></script>
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
<div class="mb12">
	<div class="mb11 col-md-11">
		<div class="form-group col-md-12">
			<label class="col-md-2 control-label">活动id:${(cohereActivity.id)!}</label>
			<label class="col-md-2 control-label">活动标题:${(cohereActivity.title)!}</label>
			<label class="col-md-2 control-label">活动时间:${(cohereActivity.beginTime)!}~${(cohereActivity.endTime)!}</label>
		</div>
	</div>	
</div>

	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th></th>
			<th>总道具发出数</th>
			<th>道具一</th>
			<th>道具二</th>
			<th>道具三</th>
			<th>道具四</th>
			<th>奖品兑换数</th>
			<th>奖品一</th>
			<th>奖品二</th>
			<th>奖品三</th>
			<th>奖品四</th>
			<th>参与人数</th>
		</tr>
		<#if todayMap??>
			<tr>
				<td>今天</td>
				<td>${(todayMap.sum1+todayMap.sum2+todayMap.sum3+todayMap.sum4)!0}</td>
				<td>${(todayMap.sum1)!0}</td>
				<td>${(todayMap.sum2)!0}</td>
				<td>${(todayMap.sum3)!0}</td>
				<td>${(todayMap.sum4)!0}</td>
				<td>${(todayMap.psum1+todayMap.psum2+todayMap.psum3+todayMap.psum4)!0}</td>
				<td>${(todayMap.psum1)!0}</td>
				<td>${(todayMap.psum2)!0}</td>
				<td>${(todayMap.psum3)!0}</td>
				<td>${(todayMap.psum4)!0}</td>
				<td>${(todayMap.personCount)!0}</td>
			</tr>
		</#if>
		<#if yesMap??>
			<tr>
				<td>昨天</td>
				<td>${(yesMap.sum1+yesMap.sum2+yesMap.sum3+yesMap.sum4)!0}</td>
				<td>${(yesMap.sum1)!0}</td>
				<td>${(yesMap.sum2)!0}</td>
				<td>${(yesMap.sum3)!0}</td>
				<td>${(yesMap.sum4)!0}</td>
				<td>${(yesMap.psum1+yesMap.psum2+yesMap.psum3+yesMap.psum4)!0}</td>
				<td>${(yesMap.psum1)!0}</td>
				<td>${(yesMap.psum2)!0}</td>
				<td>${(yesMap.psum3)!0}</td>
				<td>${(yesMap.psum4)!0}</td>
				<td>${(yesMap.personCount)!0}</td>
			</tr>
		</#if>
	</table>

	<#if type?? && type==1>
		<#-- 搜索 -->
		<div class="mb10">
			<form id="search" method="post">
				<input type="hidden" name="activityId" value="${(cohereActivity.id)!}">
				<div class="col-md-2">
					<input type="text" class="form-control" name="searchDate" placeholder="截止时间" value="${searchDate!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyyMMdd',onpicked:beginPick})" />
				</div>
				<button type="button"  id="query" class="btn btn-success">查询</button>
				<button type="button"  id="export_page" class="btn btn-success">导出</button>
			</form>
		</div>
		<#-- 表格 -->
		<table class="table table-striped table-hover">	
			<tr>
				<th><input id="selectAll_id" type="checkbox" checked="checked" /></th>
				<th>日期</th>
				<th>总道具发出数/翻牌次数</th>
				<th>奖品兑换数</th>
				<th>参与人数</th>
				<th>操作</th>
				<th></th>
			</tr>
			<#if statisticsInfoList.list??>
			<#list statisticsInfoList.list as o>
				<tr>
					<input type="hidden" name="firDraw" value="${(o.sum1)!0}"/>
					<input type="hidden" name="secDraw" value="${(o.sum2)!0}"/>
					<input type="hidden" name="thiDraw" value="${(o.sum3)!0}"/>
					<input type="hidden" name="forDraw" value="${(o.sum4)!0}"/>
					<input type="hidden" name="firPrize" value="${(o.psum1)!0}"/>
					<input type="hidden" name="secPrize" value="${(o.psum2)!0}"/>
					<input type="hidden" name="thiPrize" value="${(o.psum3)!0}"/>
					<input type="hidden" name="forPrize" value="${(o.psum4)!0}"/>
					<td><label><input name="checkId" onclick=clickaction(this) type="checkbox" value="${o.datecreate!}" checked="checked" /></label></td>
					<td>${(o.datecreate)!}</td>
					<td>
					${(o.sum1+o.sum2+o.sum3+o.sum4)!0}
					<div class="tips">
						<div class="wy-tooltip">
							<table class="table  table-striped table-hover">
								<tbody>
									<tr>
										<th>道具一</th>
										<th>道具二</th>
										<th>道具三</th>
										<th>道具四</th>
									</tr>
									<tr style="">
									</tr>
								</tbody>
							</table>
						</div>
					</div>
					</td>
					<td>${(o.psum1+o.psum2+o.psum3+o.psum4)!0}
					<div class="tips1">
						<div class="wy-tooltip">
							<table class="table  table-striped table-hover">
								<tbody>
									<tr>
										<th>奖品一</th>
										<th>奖品二</th>
										<th>奖品三</th>
										<th>奖品四</th>
									</tr>
									<tr style="">
									</tr>
								</tbody>
							</table>
						</div>
					</div>
					</td>
					<td>${(o.personCount)!0}</td>
					<td><a href="${ctx}/cohere/activity/statistics/detail/export?days=${(o.datecreate)!}&activityId=${(cohereActivity.id)!}&userIdString=all">导出当日</a></td>
					<td><a href="statistics?activityId=${(cohereActivity.id)!}&date=${(o.datecreate)!}&type=2">查看详细</a></td>
				</tr>
			</#list>
			</#if>
		</table>
		<script type="text/javascript">
			$('.tips').hover(function(){
				var content="<td>"+$(this).parents("tr").find("input[name='firDraw']").val()
							+"</td><td>"+$(this).parents("tr").find("input[name='secDraw']").val()
							+"</td><td>"+$(this).parents("tr").find("input[name='thiDraw']").val()
							+"</td><td>"+$(this).parents("tr").find("input[name='forDraw']").val()
							+"</td>";
				var obj=$(this);
				obj.find("table tr").eq(1).html(content);
				obj.find('.wy-tooltip').toggle();
			});
			$('.tips1').hover(function(){
				var content="<td>"+$(this).parents("tr").find("input[name='firPrize']").val()
							+"</td><td>"+$(this).parents("tr").find("input[name='secPrize']").val()
							+"</td><td>"+$(this).parents("tr").find("input[name='thiPrize']").val()
							+"</td><td>"+$(this).parents("tr").find("input[name='forPrize']").val()
							+"</td>";
				var obj=$(this);
				obj.find("table tr").eq(1).html(content);
				obj.find('.wy-tooltip').toggle();
			});
			// 监听列表复选按钮事件
			$('#selectAll_id').on('click', function(event) {
				selectAll();
			});
			
			//全选/取消全选
			function selectAll(){
				var checked = $('#selectAll_id').is(":checked");
				var a = document.getElementsByName("checkId");
				if(checked){
					for(var i = 0;i<a.length;i++){
						if(a[i].type == "checkbox") a[i].checked = true;
					}
				}else{
					for(var i = 0;i<a.length;i++){
						if(a[i].type == "checkbox") a[i].checked = false;
					}
				}
			}
			
			function clickaction(obj){
				if(!obj.checked){
					$("#selectAll_id").prop("checked", false);
				}
			}
			
			// 查询
			$('#query').on('click', function() {
				var $form = $('#search');
				$form.prop('action', '${ctx}/cohere/activity/statistics').submit();	
			});
			
			
			// 导出到excel
			$('#export_page').on('click', function() {
				var $form = $('#search');
				var checked=$('#selectAll_id').is(":checked");
				if(!checked){
					var dayString="";
					var a = document.getElementsByName("checkId");
					for(var i = 0;i<a.length;i++){
						if(a[i].type == "checkbox" && a[i].checked){
							var s=a[i].value;
							dayString+="'"+a[i].value+"',";
						}
					}
				}else{
					var dayString="all";
				}
				$form.prop('action', '${ctx}/cohere/activity/statistics/export?dayString='+dayString).submit();	
			});
		</script>
	</#if>
	<#if type?? && type==2>
		<div class="mb10">
			<form id="search" action="statistics" method="post">
				<input type="hidden" name="activityId" value="${(cohereActivity.id)!}">
				<input type="hidden" name="days" value="${(date)!}">
				<button type="button"  id="export_page" class="btn btn-success">导出</button>
			</form>
		</div>
		<#-- 表格 -->
		<table class="table table-striped table-hover">	
			<tr>
				<th><input id="selectAll_id" type="checkbox" checked="checked" /></th>
				<th>用户手机</th>
				<th>用户昵称</th>
				<th>注册时间</th>
				<th>道具一</th>
				<th>道具二</th>
				<th>道具三</th>
				<th>道具四</th>
				<th>奖品一</th>
				<th>奖品二</th>
				<th>奖品三</th>
				<th>奖品四</th>
				<th>操作</th>
			</tr>
			<#if statisticsInfoListDaily.list??>
			<#list statisticsInfoListDaily.list as o>
				<tr>
					<td><label><input name="checkId" onclick=clickaction(this) type="checkbox" value="${o.user_id!}" checked="checked" /></label></td>
					<td>${(o.telephone)!}</td>
					<td>${(o.nickname)!}</td>
					<td>${(o.create_date)!}</td>
					<td>${(o.firDraw)!0}</td>
					<td>${(o.secDraw)!0}</td>
					<td>${(o.thiDraw)!0}</td>
					<td>${(o.forDraw)!0}</td>
					<td>${(o.firPrize)!0}</td>
					<td>${(o.secPrize)!0}</td>
					<td>${(o.thiPrize)!0}</td>
					<td>${(o.forPrize)!0}</td>
					<td>${(o.forPrize)!0}</td>
					<td><a href="statistics?activityId=${(cohereActivity.id)!}&userId=${(o.user_id)!}&type=3">查看用户记录</a></td>
				</tr>
			</#list>
			</#if>
		</table>
		<script type="text/javascript">
			// 监听列表复选按钮事件
			$('#selectAll_id').on('click', function(event) {
				selectAll();
			});
			
			//全选/取消全选
			function selectAll(){
				var checked = $('#selectAll_id').is(":checked");
				var a = document.getElementsByName("checkId");
				if(checked){
					for(var i = 0;i<a.length;i++){
						if(a[i].type == "checkbox") a[i].checked = true;
					}
				}else{
					for(var i = 0;i<a.length;i++){
						if(a[i].type == "checkbox") a[i].checked = false;
					}
				}
			}
			
			function clickaction(obj){
				if(!obj.checked){
					$("#selectAll_id").prop("checked", false);
				}
			}
			
			
			// 导出到excel
			$('#export_page').on('click', function() {
				var $form = $('#search');
				var checked=$('#selectAll_id').is(":checked");
				if(!checked){
					var userIdString="";
					var a = document.getElementsByName("checkId");
					for(var i = 0;i<a.length;i++){
						if(a[i].type == "checkbox" && a[i].checked){
							var s=a[i].value;
							userIdString+=a[i].value+",";
						}
					}
				}else{
					var userIdString="all";
				}
				$form.prop('action', '${ctx}/cohere/activity/statistics/detail/export?userIdString='+userIdString).prop('target', '_blank').submit();	
			});
		</script>
	</#if>
	
	<#if type?? && type==3>
		<div class="mb10">
			<form id="search" action="statistics" method="post">
				<input type="hidden" name="activityId" value="${(cohereActivity.id)!}">
				<input type="hidden" name="userId" value="${(activityUserId)!}">
				<button type="button"  id="export_page" class="btn btn-success">导出</button>
			
					<label class="col-md-2 control-label">用户号码:${(userInfo.telephone)!}</label>
					<label class="col-md-2 control-label">用户昵称:${(userInfo.nickname)!}</label>
					<label class="col-md-2 control-label">注册时间:${(userInfo.createDate)!}</label>
			
			</form>
		</div>
		<#-- 表格 -->
		<table class="table table-striped table-hover">	
			<tr>
				<th><input id="selectAll_id" type="checkbox" checked="checked" /></th>
				<th>记录时间</th>
				<th>道具一</th>
				<th>道具二</th>
				<th>道具三</th>
				<th>道具四</th>
				<th>奖品一</th>
				<th>奖品二</th>
				<th>奖品三</th>
				<th>奖品四</th>
			</tr>
			<#if statisticsInfoListUser.list??>
			<#list statisticsInfoListUser.list as o>
				<tr>
					<td><label><input name="checkId" onclick=clickaction(this) type="checkbox" value="${o.days!}" checked="checked" /></label></td>
					<td>${(o.days)!}</td>
					<td>${(o.firDraw)!0}</td>
					<td>${(o.secDraw)!0}</td>
					<td>${(o.thiDraw)!0}</td>
					<td>${(o.forDraw)!0}</td>
					<td>${(o.firPrize)!0}</td>
					<td>${(o.secPrize)!0}</td>
					<td>${(o.thiPrize)!0}</td>
					<td>${(o.forPrize)!0}</td>
				</tr>
			</#list>
			</#if>
		</table>
		<script type="text/javascript">
			// 监听列表复选按钮事件
			$('#selectAll_id').on('click', function(event) {
				selectAll();
			});
			
			//全选/取消全选
			function selectAll(){
				var checked = $('#selectAll_id').is(":checked");
				var a = document.getElementsByName("checkId");
				if(checked){
					for(var i = 0;i<a.length;i++){
						if(a[i].type == "checkbox") a[i].checked = true;
					}
				}else{
					for(var i = 0;i<a.length;i++){
						if(a[i].type == "checkbox") a[i].checked = false;
					}
				}
			}
			
			function clickaction(obj){
				if(!obj.checked){
					$("#selectAll_id").prop("checked", false);
				}
			}
			
			// 导出到excel
			$('#export_page').on('click', function() {
				var $form = $('#search');
				var checked=$('#selectAll_id').is(":checked");
				if(!checked){
					var dayString="";
					var a = document.getElementsByName("checkId");
					for(var i = 0;i<a.length;i++){
						if(a[i].type == "checkbox" && a[i].checked){
							var s=a[i].value;
							dayString+="'"+a[i].value+"',";
						}
					}
				}else{
					var dayString="all";
				}
				$form.prop('action', '${ctx}/cohere/activity/statistics/user/export?dayString='+dayString).prop('target', '_blank').submit();	
			});
		</script>
	</#if>
	
	<#-- 分页 -->
<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />

</body>
</html>