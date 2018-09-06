<html>
	<head>
		<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
		<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
	</head>
	<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
			<strong>官方活动管理</strong>
		</li>
		<li class="active">
			悬赏令管理
		</li>
	</ul>
	<#if (flag!0) == 1>
		<div class="mb10">
			<div class="panel panel-warning">
			  <div class="panel-body">
				<div class="col-md-12">
					<label class="control-label">当前还有正在发布中的悬赏令!请审核正在进行中的悬赏令</label>
				</div>
			  </div>
			</div>
		</div>
	</#if>
	<article id="editor">
		<form class="form-horizontal" role="form" method="post">
			<legend></legend>
			<input type="hidden" name="id" />
			<div class="form-group">
				<label class="col-md-2 control-label">奖励金额</label>
				<div class="col-md-4">
					<input type="text" class="form-control" name="prizeNum" id="prizeNum" maxlength="32" placeholder="请输入奖励数额" wy-required="奖励数额"/>
				</div>
				<div class="col-md-2">
					<select class="form-control" name="prizeType" id="prizeType">
						<option value="1"<#if ((info.prizeType)!"1") == "1"> selected</#if>>网娱金币</option>
					</select>
				</div>
			</div>
			<div class="form-group">
				<label class="col-md-2 control-label">活动开始时间</label>
				<div class="col-md-4">
					<input type="text" class="form-control" id="startTimeStr" name="startTimeStr" placeholder="请输入活动开始时间" wy-required="活动开始时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss',maxDate:'#F{$dp.$D(\'endTimeStr\',{d:0});}'})" />
				</div>
			</div>
			<div class="form-group">
				<label class="col-md-2 control-label">活动结束时间</label>
				<div class="col-md-4">
					<input type="text" class="form-control" id="endTimeStr" name="endTimeStr" placeholder="请输入活动结束时间" wy-required="活动结束时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss',minDate:'#F{$dp.$D(\'startTimeStr\',{d:0});}'})" />
				</div>
			</div>
			<div class="form-group">
				<label class="col-md-2 control-label">活动目标</label>
				<div class="col-md-2">
					<select class="form-control" name="targetType" id="targetType" >
						<#if ((itemId)!"1") == "1">
							<option value="1"<#if ((params.targetType)!"1") == "1"> selected</#if>>击杀</option>
							<option value="2"<#if ((params.targetType)!"1") == "2"> selected</#if>>金钱</option>
							<option value="3"<#if ((params.targetType)!"1") == "3"> selected</#if>>助攻</option>
							<option value="6"<#if ((params.targetType)!"1") == "6"> selected</#if>>死亡</option>
							<option value="5"<#if ((params.targetType)!"1") == "5"> selected</#if>>补兵</option>
							<option value="4"<#if ((params.targetType)!"1") == "4"> selected</#if>>推塔</option>
							<option value="7"<#if ((params.targetType)!"1") == "7"> selected</#if>>输出伤害</option>
							<option value="8"<#if ((params.targetType)!"1") == "8"> selected</#if>>承受伤害</option>
						<#else>
							<option value="1"<#if ((params.targetType)!"1") == "1"> selected</#if>>击杀</option>
							<option value="2"<#if ((params.targetType)!"1") == "2"> selected</#if>>金钱</option>
							<option value="3"<#if ((params.targetType)!"1") == "3"> selected</#if>>助攻</option>
							<option value="6"<#if ((params.targetType)!"1") == "6"> selected</#if>>死亡</option>
							<option value="9"<#if ((params.targetType)!"1") == "9"> selected</#if>>评分数</option>
							<option value="10"<#if ((params.targetType)!"1") == "10"> selected</#if>>比赛时长</option>
							<option value="11"<#if ((params.targetType)!"1") == "11"> selected</#if>>我方总击杀数</option>
							<option value="12"<#if ((params.targetType)!"1") == "12"> selected</#if>>敌方总击杀数</option>
						</#if>
					</select>
				</div>
				<div class="col-md-2">
					<input type="text" class="form-control"  id="targetNum" name="targetNum" maxlength="32" placeholder="请输入目标数量" wy-required="目标"/>
				</div>
			</div>
			<input type="hidden" id="reward" name="reward"/>
			<input type="hidden" id="target" name="target"/>
			<input type="hidden" id="itemId" name="itemId" value=${(itemId)!"1"}/>
			<div class="form-group col-md-12">
				<label class="col-md-2 control-label">列表图：</label>
				<div class="col-md-3">
					<input type="file" class="form-control" name="icon" accept="image/*"/>
				</div>
			</div>
			<div class="form-group col-md-12">
				<label class="col-md-2 control-label">详情图：</label>
				<div class="col-md-3">
					<input type="file" class="form-control" name="cover" accept="image/*"/>
				</div>
			</div>
			<div class="form-group col-md-12">
				<label class="col-md-2 control-label">游戏类型图：</label>
				<div class="col-md-3">
					<input type="file" class="form-control" name="itemIcon" accept="image/*"/>
				</div>
			</div>
			<div class="form-group">
				<label class="col-md-2 control-label">每日提醒</label>
			</div>
			<div class="form-group">
				<label class="col-md-2 control-label">第一天:</label>
				<div class="col-md-2">
					<select class="form-control" name="dayTipOne" id="dayTipOne">
						<#if ((itemId)!"1") == "1">
							<option value="1"<#if ((params.dayTipOne)!"1") == "1"> selected</#if>>击杀</option>
							<option value="2"<#if ((params.dayTipOne)!"1") == "2"> selected</#if>>金钱</option>
							<option value="3"<#if ((params.dayTipOne)!"1") == "3"> selected</#if>>助攻</option>
							<option value="6"<#if ((params.dayTipOne)!"1") == "6"> selected</#if>>死亡</option>
							<option value="5"<#if ((params.dayTipOne)!"1") == "5"> selected</#if>>补兵</option>
							<option value="4"<#if ((params.dayTipOne)!"1") == "4"> selected</#if>>推塔</option>
							<option value="7"<#if ((params.dayTipOne)!"1") == "7"> selected</#if>>输出伤害</option>
							<option value="8"<#if ((params.dayTipOne)!"1") == "8"> selected</#if>>承受伤害</option>
						<#else>
							<option value="1"<#if ((params.dayTipOne)!"1") == "1"> selected</#if>>击杀</option>
							<option value="2"<#if ((params.dayTipOne)!"1") == "2"> selected</#if>>金钱</option>
							<option value="3"<#if ((params.dayTipOne)!"1") == "3"> selected</#if>>助攻</option>
							<option value="6"<#if ((params.dayTipOne)!"1") == "6"> selected</#if>>死亡</option>
							<option value="9"<#if ((params.dayTipOne)!"1") == "9"> selected</#if>>评分数</option>
							<option value="10"<#if ((params.dayTipOne)!"1") == "10"> selected</#if>>比赛时长</option>
							<option value="11"<#if ((params.dayTipOne)!"1") == "11"> selected</#if>>我方总击杀数</option>
							<option value="12"<#if ((params.dayTipOne)!"1") == "12"> selected</#if>>敌方总击杀数</option>
						</#if>
					</select>
				</div>
			</div>
			<div class="form-group">
				<label class="col-md-2 control-label">第二天:</label>
				<div class="col-md-2">
					<select class="form-control" name="dayTipTwo" id="dayTipTwo">
						<#if ((itemId)!"1") == "1">
							<option value="1"<#if ((params.dayTipTwo)!"1") == "1"> selected</#if>>击杀</option>
							<option value="2"<#if ((params.dayTipTwo)!"1") == "2"> selected</#if>>金钱</option>
							<option value="3"<#if ((params.dayTipTwo)!"1") == "3"> selected</#if>>助攻</option>
							<option value="6"<#if ((params.dayTipTwo)!"1") == "6"> selected</#if>>死亡</option>
							<option value="5"<#if ((params.dayTipTwo)!"1") == "5"> selected</#if>>补兵</option>
							<option value="4"<#if ((params.dayTipTwo)!"1") == "4"> selected</#if>>推塔</option>
							<option value="7"<#if ((params.dayTipTwo)!"1") == "7"> selected</#if>>输出伤害</option>
							<option value="8"<#if ((params.dayTipTwo)!"1") == "8"> selected</#if>>承受伤害</option>
						<#else>
							<option value="1"<#if ((params.dayTipTwo)!"1") == "1"> selected</#if>>击杀</option>
							<option value="2"<#if ((params.dayTipTwo)!"1") == "2"> selected</#if>>金钱</option>
							<option value="3"<#if ((params.dayTipTwo)!"1") == "3"> selected</#if>>助攻</option>
							<option value="6"<#if ((params.dayTipTwo)!"1") == "6"> selected</#if>>死亡</option>
							<option value="9"<#if ((params.dayTipTwo)!"1") == "9"> selected</#if>>评分数</option>
							<option value="10"<#if ((params.dayTipTwo)!"1") == "10"> selected</#if>>比赛时长</option>
							<option value="11"<#if ((params.dayTipTwo)!"1") == "11"> selected</#if>>我方总击杀数</option>
							<option value="12"<#if ((params.dayTipTwo)!"1") == "12"> selected</#if>>敌方总击杀数</option>
						</#if>
					</select>
				</div>
			</div>
			<div class="form-group">
				<label class="col-md-2 control-label">第三天:</label>
				<div class="col-md-2">
					<select class="form-control" name="dayTipThree" id="dayTipThree">
						<#if ((itemId)!"1") == "1">
							<option value="1"<#if ((params.dayTipThree)!"1") == "1"> selected</#if>>击杀</option>
							<option value="2"<#if ((params.dayTipThree)!"1") == "2"> selected</#if>>金钱</option>
							<option value="3"<#if ((params.dayTipThree)!"1") == "3"> selected</#if>>助攻</option>
							<option value="6"<#if ((params.dayTipThree)!"1") == "6"> selected</#if>>死亡</option>
							<option value="5"<#if ((params.dayTipThree)!"1") == "5"> selected</#if>>补兵</option>
							<option value="4"<#if ((params.dayTipThree)!"1") == "4"> selected</#if>>推塔</option>
							<option value="7"<#if ((params.dayTipThree)!"1") == "7"> selected</#if>>输出伤害</option>
							<option value="8"<#if ((params.dayTipThree)!"1") == "8"> selected</#if>>承受伤害</option>
						<#else>
							<option value="1"<#if ((params.dayTipThree)!"1") == "1"> selected</#if>>击杀</option>
							<option value="2"<#if ((params.dayTipThree)!"1") == "2"> selected</#if>>金钱</option>
							<option value="3"<#if ((params.dayTipThree)!"1") == "3"> selected</#if>>助攻</option>
							<option value="6"<#if ((params.dayTipThree)!"1") == "6"> selected</#if>>死亡</option>
							<option value="9"<#if ((params.dayTipThree)!"1") == "9"> selected</#if>>评分数</option>
							<option value="10"<#if ((params.dayTipThree)!"1") == "10"> selected</#if>>比赛时长</option>
							<option value="11"<#if ((params.dayTipThree)!"1") == "11"> selected</#if>>我方总击杀数</option>
							<option value="12"<#if ((params.dayTipThree)!"1") == "12"> selected</#if>>敌方总击杀数</option>
						</#if>
					</select>
				</div>
			</div>
			<div class="form-group">
				<label class="col-md-2 control-label">第四天:</label>
				<div class="col-md-2">
					<select class="form-control" name="dayTipFour" id="dayTipFour">
						<#if ((itemId)!"1") == "1">
							<option value="1"<#if ((params.dayTipFour)!"1") == "1"> selected</#if>>击杀</option>
							<option value="2"<#if ((params.dayTipFour)!"1") == "2"> selected</#if>>金钱</option>
							<option value="3"<#if ((params.dayTipFour)!"1") == "3"> selected</#if>>助攻</option>
							<option value="6"<#if ((params.dayTipFour)!"1") == "6"> selected</#if>>死亡</option>
							<option value="5"<#if ((params.dayTipFour)!"1") == "5"> selected</#if>>补兵</option>
							<option value="4"<#if ((params.dayTipFour)!"1") == "4"> selected</#if>>推塔</option>
							<option value="7"<#if ((params.dayTipFour)!"1") == "7"> selected</#if>>输出伤害</option>
							<option value="8"<#if ((params.dayTipFour)!"1") == "8"> selected</#if>>承受伤害</option>
						<#else>
							<option value="1"<#if ((params.dayTipFour)!"1") == "1"> selected</#if>>击杀</option>
							<option value="2"<#if ((params.dayTipFour)!"1") == "2"> selected</#if>>金钱</option>
							<option value="3"<#if ((params.dayTipFour)!"1") == "3"> selected</#if>>助攻</option>
							<option value="6"<#if ((params.dayTipFour)!"1") == "6"> selected</#if>>死亡</option>
							<option value="9"<#if ((params.dayTipFour)!"1") == "9"> selected</#if>>评分数</option>
							<option value="10"<#if ((params.dayTipFour)!"1") == "10"> selected</#if>>比赛时长</option>
							<option value="11"<#if ((params.dayTipFour)!"1") == "11"> selected</#if>>我方总击杀数</option>
							<option value="12"<#if ((params.dayTipFour)!"1") == "12"> selected</#if>>敌方总击杀数</option>
						</#if>
					</select>
				</div>
			</div>
			<div class="form-group">
				<label class="col-md-2 control-label">第五天:</label>
				<div class="col-md-2">
					<select class="form-control" name="dayTipFive" id="dayTipFive">
						<#if ((itemId)!"1") == "1">
							<option value="1"<#if ((params.dayTipFive)!"1") == "1"> selected</#if>>击杀</option>
							<option value="2"<#if ((params.dayTipFive)!"1") == "2"> selected</#if>>金钱</option>
							<option value="3"<#if ((params.dayTipFive)!"1") == "3"> selected</#if>>助攻</option>
							<option value="4"<#if ((params.dayTipFive)!"1") == "4"> selected</#if>>推塔</option>
							<option value="5"<#if ((params.dayTipFive)!"1") == "5"> selected</#if>>补兵</option>
							<option value="6"<#if ((params.dayTipFive)!"1") == "6"> selected</#if>>死亡</option>
							<option value="7"<#if ((params.dayTipFive)!"1") == "7"> selected</#if>>输出伤害</option>
							<option value="8"<#if ((params.dayTipFive)!"1") == "8"> selected</#if>>承受伤害</option>
						<#else>
							<option value="1"<#if ((params.dayTipFive)!"1") == "1"> selected</#if>>击杀</option>
							<option value="2"<#if ((params.dayTipFive)!"1") == "2"> selected</#if>>金钱</option>
							<option value="3"<#if ((params.dayTipFive)!"1") == "3"> selected</#if>>助攻</option>
							<option value="6"<#if ((params.dayTipFive)!"1") == "6"> selected</#if>>死亡</option>
							<option value="9"<#if ((params.dayTipFive)!"1") == "9"> selected</#if>>评分数</option>
							<option value="10"<#if ((params.dayTipFive)!"1") == "10"> selected</#if>>比赛时长</option>
							<option value="11"<#if ((params.dayTipFive)!"1") == "11"> selected</#if>>我方总击杀数</option>
							<option value="12"<#if ((params.dayTipFive)!"1") == "12"> selected</#if>>敌方总击杀数</option>
						</#if>
					</select>
				</div>
			</div>
			<div class="form-group">
				<div class="col-md-offset-2 col-md-10">
					<a type="button" class="btn btn-info" href="${ctx}/bounty/list/1?itemId=${(itemId)!"1"}"><i class="icon-angle-left"></i>取消</a>
					<button id="save" type="button" class="btn btn-primary">创建</button>
				</div>
			</div>
		</form>
	</article>
		<script type="text/javascript">
			// 定义编辑器及表单
			var $editor = $('#editor'),
				$form = $editor.find('form');
			
			// 初始化编辑框
			$.fillForm({
				id: '${(info.id)!}',
				targetType: '${(info.targetType)!}',
				dayTipOne: '${(params.dayTipOne)!"1"}',
				dayTipTwo: '${(params.dayTipTwo)!"1"}',
				dayTipThree: '${(params.dayTipThree)!"1"}',
				dayTipFour: '${(params.dayTipFour)!"1"}',
				dayTipFive: '${(params.dayTipFive)!"1"}',
				targetNum: '${(info.targetNum)!}',
				target: '${(info.target)!}',
				prizeType: '${(info.prizeType)!}',
				prizeNum: '${(info.prizeNum)!}',
				itemId:${(itemId)!"1"},
				icon:'${(info.icon)!}',
				cover:'${(info.cover)!}',
				startTimeStr:'${(info.startTime)!}',
				endTimeStr:'${(info.endTime)!}',
				itemIcon:'${(info.itemIcon)!}'
			}, $editor);
			
			function isRepeat(arr){
			     var hash = {};
			     for(var i in arr) {
			         if(hash[arr[i]])
			              return true;
			         hash[arr[i]] = true;
			     }
			     return false;
			 }
			 function isPInt(str) {
			    var g = /^[1-9]*[1-9][0-9]*$/;
			    return g.test(str);
			}
			$(function(){
				var vaild = false;
				$("#prizeNum").blur(function(){
					var value = $(this).val();
					if(!isPInt(value)){
						alert("请设置成整数！");
						return;
					}else{
						vaild=true;
					}
				})
				$("#save").click(function(){
					var value = $("#prizeNum").val();
					if(!isPInt(value)){
						alert("奖励请设置成整数！");
						return;
					}else{
						vaild=true;
					}
					var formValid = $form.formValid();
					var targetType = $("#targetType").val();
					var targetNum = $("#targetNum").val();
					var prizeType = $("#prizeType").val();
					var prizeNum = $("#prizeNum").val();
					var reward;
					var targetStr;
					if(prizeType == 1){//网娱金币
						reward = "网娱金币"+prizeNum;
					}else if(prizeType == 2){ //流量
						reward = "流量"+prizeNum;
					}
					if(targetType == 1){
						targetStr = "击杀数|"+targetNum;
					}else if(targetType == 2){
						targetStr = "金钱数|"+targetNum;
					}else if(targetType == 3){
						targetStr = "助攻数|"+targetNum;
					}else if(targetType == 4){
						targetStr = "推塔数|"+targetNum;
					}else if(targetType == 5){
						targetStr = "补兵数|"+targetNum;
					}else if(targetType == 6){
						targetStr = "死亡数|"+targetNum;
					}else if(targetType == 7){
						targetStr = "输出伤害|"+targetNum;
					}else if(targetType == 8){
						targetStr = "承受伤害|"+targetNum;
					}else if(targetType == 9){
						targetStr = "评分数|"+targetNum;
					}else if(targetType == 10){
						targetStr = "比赛时长数|"+targetNum;
					}else if(targetType == 11){
						targetStr = "我方总击杀数|"+targetNum;
					}else if(targetType == 12){
						targetStr = "敌方总击杀数|"+targetNum;
					}
					var test = [$("#dayTipOne").val(),$("#dayTipFive").val(),$("#dayTipTwo").val(),$("#dayTipThree").val(),$("#dayTipFour").val()];
					if(isRepeat(test)){
						alert("每日提醒需设置不同！");
						return;
					}
					$("#reward").val(reward);
					$("#target").val(targetStr);
					var startTime = new Date(new Date($("#startTimeStr").val()).Format("yyyy-MM-dd"));
					var endTime = new Date(new Date($("#endTimeStr").val()).Format("yyyy-MM-dd"));
					var days = parseInt((endTime.getTime() - startTime.getTime())/(1000 * 60 * 60 * 24));
					if(days!=6){
						alert("日期设置错误！");
						return;
					}
					if(vaild&&formValid){
						$("#save").addClass("disabled");
						$("#save").text("保存中...");
						 $form.ajaxSubmit({
			             	 type: "post",
				             async:"false",
				             url: "/bounty/save",
				             dataType: "json",
				             success: function(data) {
								if (data.code == 0) {
									window.location.href="/bounty/list/1?itemId="+${(itemId)!"1"};
								}else{
									alert(data.result);
									$("#save").removeClass("disabled");
									$("#save").text("保存失败");
								}
							}
	 					});
					}else{
						alert("内容设置不正确");
					}
				})
				<#if (readonly!"") == "1">
 					$('input,textarea,select,button').prop('readonly', true).prop('disabled', true);
	 			</#if>
				<#if (flag!0) == 1>
 					$('input,textarea,select,button').prop('readonly', true).prop('disabled', true);
	 			</#if>
			})
		</script>
	</body>
</html>