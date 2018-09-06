<#import "/macros/pager.ftl" as p >
<html>
<head>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>官方娱乐赛管理</strong>
 		</li>
		<li class="active">
			报名人数查看
		</li>
	</ul>
	
	<#-- 搜索，分类 -->
	<div class="mb10">
		<form id="search" action="1" method="post" style="display: inline" >
			<input type="hidden" name="activityId" value="${(params.activityId)!}"/>
			<input type="hidden" name="flag" value="${flag!}"/>
			<div class="col-md-2" style="width:160px;">
				<input type="text" class="form-control" name="nickname" placeholder="用户昵称" value="${(params.nickname)!}" />
			</div>
			<div class="col-md-2" style="width:160px;">
				<input type="text" class="form-control" name="telephone" placeholder="手机号" value="${(params.telephone)!}" />
			</div>
			<div class="col-md-2" style="width:160px;">
				<input type="text" class="form-control" name="qq" placeholder="QQ号" value="${(params.qq)!}" />
			</div>
			<div class="col-md-2" style="width:160px;">
				<input type="text" name="startDate" value="${(params.startDate)!}" class="form-control" placeholder="报名时间(起)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:beginPick})">
			</div>
			<div class="col-md-2" style="width:160px;">
				<input type="text" name="endDate" value="${(params.endDate)!}" class="form-control" placeholder="报名时间(止)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:endPick})">
			</div>
			<button type="submit" class="btn btn-success" >查询</button>
			<a class="btn" href="1?activityId=${(params.activityId)!}&flag="${flag!}">清空</a>
		</form>
		<form id="return" style="margin-left:30px;display: inline" action="/<#if flag??>netbarAmuse<#else>amuse</#if>/list/1" method="post">
			<button type="submit" class="btn btn-success">返回</button>
		</form>
	</div>
	<div class="mb10">
		<form id="export" action="/amuse/exportExcel" method="post">
			<input type="hidden" name="amuseId" value="${(params.activityId)!}"/>
			<button type="submit" class="btn btn-success" style="margin-left:10px;">导出</button>
		</form>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>#</th>
			<th>昵称</th>
			<th>手机号</th>
			<th>QQ号</th>
			<th>游戏帐号</th>
			<th>游戏大区</th>
			<th>身份证</th>
			<th>团队名称</th>
			<th>报名时间</th>
		</tr>
		<#list list as o>
			<tr>
				<td>${o.id!}</td>
				<td>${o.nickname!}</td>
				<td>${o.telephone!}</td>
				<td>${o.qq!}</td>
				<td>${o.gameAccount!}</td>
				<td>${o.server!}</td>
				<td>${o.idCard!}</td>
				<td>${o.teamName!}</td>
				<td>${o.createDate!}</td>
			</tr>
		</#list>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	
	<#-- 编辑 -->
	<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
	<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
	
	<script type="text/javascript">
	// 提交表单，编辑
	function submitEditor() {
		_$editor.find('form').ajaxSubmit({
			url:'${ctx}/commodity/save',
		    type:'post',
		    success: function(d){
		    	if(d.code == 0) {
		    		window.location.reload();
		    	} else {
		    		alert(d.result);
		    	}
			}
		});
	}
	// 提交表单，新增
	function submitAdd() {
		_$editor.find('form').ajaxSubmit({
			url:'${ctx}/commodity/save',
		    type:'post',
		    success: function(d){
		    	if(d.code == 0) {
		    		window.location.reload();
		    	} else {
		    		alert(d.result);
		    	}
			}
		});
	}
	
	// 下架
	$('button[remove]').on('click', function(event) {
		var _this = $(this);
		_this.attr('disabled', true);
		var id = $(this).attr('remove');
		var commodityName = $(this).attr('commodityName');
		
		$.confirm('确认下架【'+ commodityName +'】吗?', function() {
			$.api('${ctx}/commodity/statusChange/' + id + '/' + 0, {}, function(d) {
				window.location.reload();
			}, function(d) {
				alert('下架失败：' + d.result);
			}, {
				complete: function() {
					_this.attr('disabled', false);
				}
			});
		}, undefined, {
			complete: function() {
				_this.attr('disabled', false);
			}
		});
	});
	
	</script>
</body>
</html>