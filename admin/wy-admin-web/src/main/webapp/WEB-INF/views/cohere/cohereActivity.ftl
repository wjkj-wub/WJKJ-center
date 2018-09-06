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
	<div class="mb11 col-md-11">
		<form id="search" action="/cohere/activity/list/1" method="get">
			<div class="mb12 col-md-12">
				<div class="col-md-2">
					<input class="form-control" type="text" name="findTitle" maxlength="20" placeholder="标题" value="${(params.findTitle)!}" />
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
				<div class="col-md-2">
					<div class="col-md-6">
						<button class="btn btn-info" type="submit" >提交</button>
					</div>	
					<div class="col-md-6">
						<button class="btn btn-info" onclick="history.go(-1)">返回</button>
					</div>
				</div>
			</div>
		</form>
	</div>	
</div>
	<article id="editor" style="margin-top:50px">
	<form class="form-horizontal" role="form" method="post">
		<div class="mb6 col-md-6">
			<div class="col-md-3">
				<label class="control-label">标题</label>
			</div>
			<div class="col-md-6">
				<input type="text" class="form-control" name="title" maxlength="8" wy-required="标题名"/>
			</div>
			<br/><br/>
			<h4>设置活动道具：</h4>
			<div class="form-group col-md-12">
				<label class="col-md-2 control-label">道具一：</label>
				<div class="col-md-5">
					<input type="text" class="form-control" name="debrisOne" maxlength="8" placeholder="请输入活动道具名称" wy-required="道具名"/>
				</div>
				<div class="col-md-5">
					<input type="file" class="form-control" name="debrisOneFile" accept="image/*" wy-required="图片"/>
				</div>
			</div>
			<br/>
			<div class="form-group col-md-12">
				<label class="col-md-2 control-label">道具二：</label>
				<div class="col-md-5">
					<input type="text" class="form-control" name="debrisTwo" maxlength="8" placeholder="请输入活动道具名称" wy-required="道具名"/>
				</div>
				<div class="col-md-5">
					<input type="file" class="form-control" name="debrisTwoFile" accept="image/*" wy-required="图片"/>
				</div>
			</div>
			<br/>
			<div class="form-group col-md-12">
				<label class="col-md-2 control-label">道具三：</label>
				<div class="col-md-5">
					<input type="text" class="form-control" name="debrisThree" maxlength="8" placeholder="请输入活动道具名称" wy-required="道具名"/>
				</div>
				<div class="col-md-5">
					<input type="file" class="form-control" name="debrisThreeFile" accept="image/*" wy-required="图片"/>
				</div>
			</div>
			<br/>
			<div class="form-group col-md-12">
				<label class="col-md-2 control-label">道具四：</label>
				<div class="col-md-5">
					<input type="text" class="form-control" name="debrisFour" maxlength="8" placeholder="请输入活动道具名称" wy-required="道具名"/>
				</div>
				<div class="col-md-5">
					<input type="file" class="form-control" name="debrisFourFile" accept="image/*" wy-required="图片"/>
				</div>
			</div>
		</div>
		<div class="mb6 col-md-6">
			<div class="col-md-2">
				<h4>活动时间</h4>
			</div>
			<div class="col-md-5">
				<input class="form-control" type="text" id="activityBeginTime" name="activityBeginTime" maxlength="30" placeholder="开始时间(止)" value="${(params.activityBeginTime)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss',minDate:'#F{$dp.$D(\'activityBeginTime\',{d:0});}'})" />
			</div>
			<div class="col-md-5">
				<input class="form-control" type="text" id="activityEndTime" name="activityEndTime" maxlength="30" placeholder="结束时间(止)" value="${(params.activityEndTime)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss',minDate:'#F{$dp.$D(\'activityEndTime\',{d:0});}'})" />
			</div>
			<br/><br/>
			<h4>设置奖品：</h4>
			<div class="form-group col-md-12">
				<label class="col-md-2 control-label">奖品一：</label>
				<div class="col-md-4">
					<input type="text" class="form-control" name="prizeOne" maxlength="8" placeholder="请输入奖品名称" wy-required="奖品名"/>
				</div>
				<div class="col-md-3">
					<input type="file" class="form-control" name="prizeOneFile1" accept="image/*" wy-required="图片"/>
				</div>
				<div class="col-md-3">
					<input type="file" class="form-control" name="prizeOneFile2" accept="image/*" wy-required="图片"/>
				</div>
			</div>
			<br/>
			<div class="form-group col-md-12">
				<label class="col-md-2 control-label">奖品二：</label>
				<div class="col-md-5">
					<input type="text" class="form-control" name="prizeTwo" maxlength="8" placeholder="请输入奖品名称" wy-required="奖品名"/>
				</div>
				<div class="col-md-5">
					<input type="file" class="form-control" name="prizeTwoFile" accept="image/*" wy-required="图片"/>
				</div>
			</div>
			<br/>
			<div class="form-group col-md-12">
				<label class="col-md-2 control-label">奖品三：</label>
				<div class="col-md-5">
					<input type="text" class="form-control" name="prizeThree" maxlength="8" placeholder="请输入奖品名称" wy-required="奖品名"/>
				</div>
				<div class="col-md-5">
					<input type="file" class="form-control" name="prizeThreeFile" accept="image/*" wy-required="图片"/>
				</div>
			</div>
			<br/>
			<div class="form-group col-md-12">
				<label class="col-md-2 control-label">奖品四：</label>
				<div class="col-md-5">
					<input type="text" class="form-control" name="prizeFour" maxlength="8" placeholder="请输入奖品名称" wy-required="奖品名"/>
				</div>
				<div class="col-md-5">
					<input type="file" class="form-control" name="prizeFourFile" accept="image/*" wy-required="图片"/>
				</div>
			</div>
			<div class="col-md-12">
				<button type="button" class="btn btn-info" style="float:right" onclick="saveActivity()" id="btsub">保存，下一步</button>
			</div>
		</div>
		</form>
	</article>
	<script type="text/javascript">
	var $editor = $('#editor'),
	$form = $editor.find('form'),
	search = $("#search");
	
	$formFile = $('form[input="file"]');
	function saveActivity(){
	 	var formValid = $form.formValid(); 
		 if(formValid){
			$("#btsub").addClass("disabled");
			$("#btsub").text("保存中...");
			 $form.ajaxSubmit({
             	type: "post",
	             async:"false",
	             url: "/cohere/activity/save",
	             dataType: "json",
	             success: function(d) {
						if (d.code == 0) {
							window.location = '${ctx}/cohere/activity/edit/probability?activityId='+d.object.activityId;
						} else {
							alert(d.result);
							$("#btsub").removeClass("disabled");
							$("#btsub").text("保存，下一步");
						}
				}
 			});
		}
	}
	</script>
</body>
</html>