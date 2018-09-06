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
		官方抢皮肤发奖
	</li>
</ul>
<#-- 标题 -->
<div class="mb12 col-md-12">
	<div class="mb2 col-md-2">
		活动ID：${(params.activityId)!}
	</div>
	<div class="mb5 col-md-5">
		活动标题：${(params.activityTitle)!}
	</div>
	<div class="mb5 col-md-5">
		活动时间：${(params.activityBeginTime)!} -- ${(params.activityEndTime)!}
	</div>
</div>
<br>
<#-- 搜索 -->
<div class="mb12">
	<div class="mb12 col-md-12">
		<form id="search" action="/cohere/activity/1" method="get">
			<div class="mb12 col-md-12">
				<div class="col-md-3">
					<label class="col-md-4 control-label">查找用户：</label>
					<div class="col-md-8">
						<input class="form-control" type="text" name="searchUser" maxlength="20" placeholder="标题" value="${(params.searchUser)!}" />
						<input class="form-control" type="hidden" name="activityId" value="${(params.activityId)!}" />
					</div>
				</div>
				<div class="col-md-5">
					<label class="col-md-2 control-label">时间查找：</label>
					<div class="col-md-5">
						<input class="col-md-5 form-control" type="text" id="startTime" name="startTime" maxlength="10" placeholder="创建时间(起)" value="${(params.startTime)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss',maxDate:'#F{$dp.$D(\'startTime\',{d:0});}'})" />
					</div>
					<div class="col-md-5">
						<input class="col-md-5 form-control" type="text" id="endTime" name="endTime" maxlength="10" placeholder="创建时间(止)" value="${(params.endTime)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss',minDate:'#F{$dp.$D(\'endTime\',{d:0});}'})" />
					</div>
				</div>
				<div class="col-md-2">
					<label class="col-md-5 control-label">奖品筛选：</label>
					<div class="col-md-7">
						<select class="form-control" name="prizeType">
							<option value="">全部状态</option>
							<option value="1"<#if (params.prizeType!"0") == "1"> selected</#if>>奖品一</option>
							<option value="2"<#if (params.prizeType!"0") == "2"> selected</#if>>奖品二</option>
							<option value="3"<#if (params.prizeType!"0") == "3"> selected</#if>>奖品三</option>
							<option value="4"<#if (params.prizeType!"0") == "4"> selected</#if>>奖品四</option>
						</select>
					</div>
				</div>
				<div class="col-md-2">
					<label class="col-md-5 control-label">状态筛选：</label>
					<div class="col-md-7">
						<select class="form-control" name="prizeState">
							<option value="">全部状态</option>
							<option value="1"<#if (params.prizeState!"0") == "1"> selected</#if>>待发放</option>
							<option value="2"<#if (params.prizeState!"0") == "2"> selected</#if>>已发放</option>
							<option value="3"<#if (params.prizeState!"0") == "3"> selected</#if>>有疑问</option>
						</select>
					</div>
				</div>
			</div>
			<div class="col-md-2">
				<button class="btn btn-info" type="submit">提交</button>
			</div>
		</form>
	</div>
	<br>
	<div class="col-md-3" style="float:right;margin-top:10px">
		<div class="col-md-7">
			<button class="btn btn-info" type="button" onclick="exportExcel(${(params.activityId)!})">导出</button>
		</div>
	</div>		
</div>

<table class="table table-striped table-hover">	
	<tr>
		<th><label><input type="checkbox" id="checkAll"></label></th>
		<th>用户手机</th>
		<th>用户昵称</th>
		<th>兑奖时间</th>
		<th>兑奖qq</th>
		<th>所在大区</th>
		<th>游戏ID</th>
		<th>兑奖奖品名</th>
		<th>状态</th>
		<th>注册时间</th>
		<th>操作</th>
	</tr>
	<#if list??>
		<#list list as o>
			<tr>
				<th><input type="checkbox" class="checkSelf" prizeId=${o.id!}></th>
				<th style="display:none">${o.id!}</th>
				<th>${o.telephone!}</th>
				<th>${o.nickname!}</th>
				<th>${o.create_date!}</th>
				<th>${o.account!}</th>
				<th>${o.serveName!}</th>
				<th>${o.gameName!}</th>
				<th>${o.prizeName!}</th>
				<#switch o.state>
					<#case 0>
						<th>用户未申请</th>
						<#break>
		 			<#case 1>
						<th>申请未发放</th>
		 				<#break>
		 			<#case 2>
						<th>发放失败</th>
		 				<#break>
		 			<#case 3>
						<th>发放成功</th>
		 				<#break>
		 			<#case 4>
						<th>充值中</th>
		 				<#break>
		 			<#default>
				</#switch>
				<th>${o.registerTime!}</th>
				<th>
				<#switch o.btState>
					<#case 0>
						<button class="btn btn-info" type="button">已发放</button>
						<#break>
					<#case 1>
						<button class="btn btn-info" type="button" onclick="checkChargeStatusHand(${o.id!})">充值异常，手动发放</button>
						<#break>
					<#case 2>
						<button class="btn btn-info ss" type="button" onclick="sendPrize(${(params.activityId)!},${o.id!})">发放</button>
						<button class="btn btn-info" onclick="questionBtn(${o.id!})" type="button">疑问</button>
						<#break>
					<#case 3>
						<button class="btn btn-info ss" type="button" onclick="sendPrize(${(params.activityId)!},${o.id!})">发放</button>
						<button class="btn btn-info" onclick="questionBtn(${o.id!})" type="button">有疑问</button>
						<#break>
					<#case 4>
						<button class="btn btn-info" type="button" onclick="checkChargeStatusHand(${o.id!})">充值异常，手动发放</button>
						<#break>
		 			<#default>
				</#switch>
				</th>
			</tr>
		</#list>
	</#if>
</table>
<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
<script type="text/javascript">
$(function(){
	$("#checkAll").change(function(){
		if(!$(this).is(':checked')){
			$(".checkSelf").each(function(){
				$(this).removeAttr('checked');
			})	
		}else{
			$(".checkSelf").each(function(){
				if(!$(this).is(':checked')){
					var t = $(this).parent().siblings().eq(8).html();
					if(t == '申请未发放' || t == '发放失败'){
						$(this).prop('checked',true);
					}
				}
			});
		}
	});
})
function questionBtn(prizeHistoryId){
	$.ajax({
		url:'/cohere/activity/question?prizeHistoryId='+prizeHistoryId,
		type:"get",
		dataType: "json",
		success:function(d){
			if(d.code == 0){
				window.location.reload();
			}else{
				alert("质疑出错!");
			}
		}
	});
}

function checkChargeStatusHand(prizeHistoryId){
	$.ajax({
		url:'/cohere/activity/checkChargeStatusHand?prizeHistoryId='+prizeHistoryId,
		type:"get",
		dataType: "json",
		success:function(d){
			if(d.code == 0){
				alert(d.result);
				window.location.reload();
			}
		}
	});
}

function exportExcel(activityId){
	var search = window.location.search.substring(1,window.location.search.length);
	if(document.getElementById('checkAll').checked){
		window.open("${ctx}/cohere/activity/export?type=0&activityId="+activityId+"&"+search);
	}else{
	 	var prizeHistoryIds = "";
		$(".checkSelf").each(function(){
			if($(this).is(':checked')){
				prizeHistoryIds += $(this).attr("prizeId")+",";
			}
		});
		window.open("${ctx}/cohere/activity/export?type=1&activityId="+activityId+"&prizeHistoryIds="+prizeHistoryIds.substring(0,prizeHistoryIds.length-1)+"&"+search);
	}
}

function sendPrize(activityId,prizeHistoryId){
	var search = window.location.search.substring(1,window.location.search.length);
	$(".ss").addClass("disabled");
	if(prizeHistoryId == 0){
		if(document.getElementById('checkAll').checked){
			$.ajax({
				url:'/cohere/activity/sendPrize?type=1&'+search,
				type:"get",
				dataType: "json",
				success:function(d){
					if(d.code == 0){
						window.location.reload();
					}else{
						var r=confirm(d.result);
						if (r==true){
							window.location.reload();
						}else{
							window.location.reload();
						}
					}
				}
			});
		}else{
		 	var prizeHistoryIds = "";
			$(".checkSelf").each(function(){
				if($(this).is(':checked')){
					prizeHistoryIds += $(this).attr("prizeId")+",";
				}
			});
			$.ajax({
				url:'/cohere/activity/sendPrize?type=0&prizeHistoryIds='+prizeHistoryIds.substring(0,prizeHistoryIds.length-1)+'&'+search,
				type:"get",
				dataType: "json",
				success:function(d){
					if(d.code == 0){
						window.location.reload();
					}else{
						var r=confirm(d.result);
						if (r==true){
							window.location.reload();
						}else{
							window.location.reload();
						}
					}
				}
			});
		}
	}else{
		$.ajax({
			url:'/cohere/activity/sendPrize?type=0&prizeHistoryIds='+prizeHistoryId+'&activityId=${(params.activityId)!}',
			type:"get",
			dataType: "json",
			success:function(d){
				if(d.code == 0){
					window.location.reload();
				}else{
					var r=confirm(d.result);
					if (r==true){
						window.location.reload();
					}else{
						window.location.reload();
					}
				}
			}
		});
	}
}

</script>
</body>
</html>