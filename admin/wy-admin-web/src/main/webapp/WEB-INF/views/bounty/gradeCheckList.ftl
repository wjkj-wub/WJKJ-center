<#import "/macros/pager.ftl" as p >
<html>
<head>
<script type="text/javascript" editor-ctx="${ctx!}" src="${ctx!}/static/plugin/editor.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/ztree/js/jquery.ztree.all-3.5.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/ztree/js/jquery.ztree.exhide-3.5.min.js"></script>
<link rel="stylesheet" type="text/css" href="${ctx!}/static/plugin/ztree/css/zTreeStyle.css"/>
<style type="text/css">
.carrousel {
  background-color: rgba(10, 10, 10, 0.8);
  display: none;
  position: fixed;
  top:0;
  left:0;
  width:100%;
  z-index:1000;
  height:100%;
}
.carrousel .wrapper {
  width: 600px;
  height: 600px;
  overflow: hidden;
}
.carrousel .wrapper > img {
  width: 100%;
  margin:auto;
}
.carrousel, .carrousel .wrapper {
  margin: auto;
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
			悬赏令管理
		</li>
	</ul>
	
	<#-- 确认发布结果 -->
	<div class="mb10">
		<div class="panel panel-warning">
		  <div class="panel-body">
			<div class="col-md-12">
				<label class="control-label">本轮奖金设置:${(info.reward)!}</label>
			</div>
			<div class="col-md-12">
				<label class="control-label">本轮悬赏目标:${(info.target)!}</label>
				<label class="control-label">本期总参与人数:${(info.applyNum)!}</label>
			</div>
			<div class="col-md-12">
				<label class="control-label">本期中奖用户数:${(info.winNum)!}</label>
			</div>
			<div class="col-md-12">
				<label class="col-md-2 control-label">本期调整中奖数:</label>
				<div class="col-md-3">
					<input type="hidden" class="mmm" class="form-control" name="reward" value="${(info.reward)!}"/>
					<input type="text" class="mmm" class="form-control" name="virtualNum" id="virtualNum" value="${(info.virtualNum)!0}"/>
				</div>
			</div>
			<div class="col-md-12">
				<label class="col-md-3 control-label" id="perReward">本期中奖用户奖金:${(info.awardNum)!0}</label>
		  	</div>
		  </div>
		  <div class="panel-footer">
		    <button type="button" class="btn btn-success mmm"  bountyId="${bountyId!}" id="publish">确认发布本期结果</button>
		  </div>
		</div>
	</div>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
			<div class="col-md-2">
				<input type="text" class="form-control" id="username" name="telephone" placeholder="网娱账户" wy-required="网娱账户" value="${(telephone)!}"/>
			</div>
			<div class="col-md-2">
				<select class="form-control" name="isMarked">
					<option value="0"<#if ((isMarked)!0) == 0> selected</#if>>全部</option>
					<option value="1"<#if ((isMarked)!0) == 1> selected</#if>>已标记</option>
					<option value="2"<#if ((isMarked)!0) == 2> selected</#if>>未标记</option>
				</select>
			</div>
			<div class="col-md-2">
				<input class="form-control" type="text" id="search-beginDate" name="submitDateStart" placeholder="提交时间起" value="${(submitDateStart)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',maxDate:'#F{$dp.$D(\'search-endDate\',{d:0});}'})">
			</div>
			<div class="col-md-2">
				<input class="form-control" type="text" id="search-endDate" name="submitDateEnd" placeholder="提交时间止" value="${(submitDateEnd)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',minDate:'#F{$dp.$D(\'search-beginDate\',{d:0});}'})">
			</div>
			<button type="submit" class="btn btn-success">查询</button>
		</form>
	</div>
	<div class="alert alert-info">注意：标记设为0或者不设为成绩审核不通过，标记设为大于0则成绩审核通过</div>
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<input type="hidden" id="bountyId" value="${bountyId!}">
			<th>ID</th>
			<th>领奖者昵称</th>
			<th>手机号码</th>
			<th>图片</th>
			<th>提交时间</th>
			<th>标记</th>
		</tr>
		<#if list??>
			<#list list as o>
				<input type="hidden" id="userId" value="${(o.userId)!}">
				<tr>
					<td>${o.id!}</td>
					<td>${o.nickname!}</td>
					<td>${o.telephone!}</td>
					<td class="portrait">
						<#if o.img?has_content>
							<img class="pic" data-src-wide='${imgServer!}/${o.img!}' src="${imgServer!}/${o.img!}" data-toggle="lightbox" style="width: 50px; height: 50px;" show="${imgServer!}/${o.img!}"/>
						</#if>
					</td>
					<td>${(o.create_date)!}</td>
					<td><input class="mmm" type="text" value="${(o.grade)!}" onblur="saveGrade(this);" id="${o.id!}" userId="${o.userId!}"/></td>
				</tr>
			</#list>
		</#if>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	<script type="text/javascript">
	<#if ((info.status)!0) == 3>
		$('.mmm').prop('readonly', true).prop('disabled', true);
	</#if>
	<#if ((info.status)!0) == 1>
		$('.mmm').prop('readonly', true).prop('disabled', true);
	</#if>
	function saveGrade(obj){
		var r = /^-?[0-9]\d*$/;
		var gradeJudge=obj.value;
		if(gradeJudge!="" && ! r.test(gradeJudge)){
			alert("成绩需为整数");
			return false;
		}
		var params = {
			id:$(obj).attr("id"),
			bountyId : $("#bountyId").val(),
			grade : obj.value,
			userId:$(obj).attr("userId")
		};
		$.ajax({
			url : '${ctx}/bounty/saveGrade/',
			data : params,
			dataType : 'json',
			success : function(d) {
				alert(d.result);
			},
		});
	}
	$(function(){
		$("#publish").click(function(){
			var visualNum = $("#virtualNum").val();
			$(this).addClass("disabled");
			$(this).text("发放中...");
			$.ajax({
				url : '${ctx}/bounty/bountyFinish/',
				data : {visualNum:visualNum,id:${bountyId!}},
				dataType : 'json',
				success : function(d) {
					if(d.code==0){
						window.location.reload();
					}else{
						alert(d.result);
						window.location.reload();
					}
				},
			});
		});
	})
	</script>
</body>
</html>