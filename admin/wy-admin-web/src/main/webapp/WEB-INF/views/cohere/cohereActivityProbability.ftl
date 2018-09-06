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
		<form id="search" action="/cohere/activity/list/1" method="post">
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
						<button class="btn btn-info" type="submit">提交</button>
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
		<div class="alert alert-info">注意：数量只能设置更多，否则设置失败；ps:数量为-99意思为无穷</div>
		<div class="mb4 col-md-4">
			<div class="col-md-3">
				<label class="control-label">标题</label>
			</div>
			<div class="col-md-6">
				<input type="hidden" name="activityId" />
				<input type="text" class="form-control" name="title" maxlength="25" placeholder="请输入标题" wy-required="标题"/>
			</div>
			<br/><br/>
			<h4>设置活动碎片概率和数量：</h4>
			<div class="form-group col-md-12">
				
				<label class="col-md-2 control-label">碎片一：</label>
				<div class="col-md-4">
					<input type="text" class="form-control" name="debrisOneNum" maxlength="25" placeholder="设置数量(-1为无穷)" wy-required="碎片"/>
				</div>
				<div class="col-md-6">
					<label class="col-md-4 control-label">分配概率</label>
					<div class="col-md-7">
						<input type="text" class="form-control probability" name="debrisOneProbability" maxlength="25"/>
					</div>
					<label class="control-label">%</label>
				</div>
			</div>
			<br/>
			<div class="form-group col-md-12">
				<label class="col-md-2 control-label">碎片二：</label>
				<div class="col-md-4">
					<input type="text" class="form-control" name="debrisTwoNum" maxlength="25" placeholder="设置数量(-1为无穷)" wy-required="碎片"/>
				</div>
				<div class="col-md-6">
					<label class="col-md-4 control-label">分配概率</label>
					<div class="col-md-7">
						<input type="text" class="form-control probability" name="debrisTwoProbability" maxlength="25"/>
					</div>
					<label class="control-label">%</label>
				</div>
			</div>
			<br/>
			<div class="form-group col-md-12">
				<label class="col-md-2 control-label">碎片三：</label>
				<div class="col-md-4">
					<input type="text" class="form-control" name="debrisThreeNum" maxlength="25" placeholder="设置数量(-1为无穷)" wy-required="碎片"/>
				</div>
				<div class="col-md-6">
					<label class="col-md-4 control-label">分配概率</label>
					<div class="col-md-7">
						<input type="text" class="form-control probability" name="debrisThreeProbability" maxlength="25"/>
					</div>
					<label class="control-label">%</label>
				</div>
			</div>
			<br/>
			<div class="form-group col-md-12">
				<label class="col-md-2 control-label">碎片四：</label>
				<div class="col-md-4">
					<input type="text" class="form-control" name="debrisFourNum" maxlength="25" placeholder="设置数量(-1为无穷)" wy-required="碎片"/>
				</div>
				<div class="col-md-6">
					<label class="col-md-4 control-label">分配概率</label>
					<div class="col-md-7">
						<input type="text" class="form-control probability" name="debrisFourProbability" maxlength="25"/>
					</div>
					<label class="control-label">%</label>
				</div>
			</div>
		</div>
		<div class="mb8 col-md-8">
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
			<h4>设置奖品中奖概率和数量：</h4>
			<div class="form-group col-md-12">
				<label class="col-md-1 control-label">奖品一：</label>
				<div class="col-md-1">
					<input type="text" class="form-control" name="prizeOneNum" maxlength="25" placeholder="设置数量(-1为无穷)" wy-required="奖品"/>
				</div>
				<div class="col-md-3">
					<label class="col-md-6 control-label">分配概率</label>
					<div class="col-md-5">
						<input type="text" class="form-control" name="prizeOneProbability" maxlength="15"/>
					</div>
					<label class="control-label">%</label>
				</div>
				<div class="col-md-4">
					<label class="col-md-7 control-label">奖品类型</label>
					<div class="col-md-5">
						<select class="form-control prizeClass" name="prizeOneType">
							<option value="0"<#if (params.prizeOneType!0) == 0> selected</#if>>未设置</option>
							<option value="1"<#if (params.prizeOneType!0) == 1> selected</#if>>CDK</option>
							<option value="2"<#if (params.prizeOneType!0) == 2> selected</#if>>Q币</option>
						</select>
					</div>
				</div>
				<div class="col-md-3">
					<label class="col-md-6 control-label">奖品价值</label>
					<div class="col-md-5">
						<input type="text" class="form-control" name="prizeOneValue" maxlength="15" wy-required="奖品价值"/>
					</div>
				</div>
			</div>
			<br/>
			<div class="form-group col-md-12">
				<label class="col-md-1 control-label">奖品二：</label>
				<div class="col-md-1">
					<input type="text" class="form-control" name="prizeTwoNum" maxlength="25" placeholder="设置数量(-1为无穷)" wy-required="奖品"/>
				</div>
				<div class="col-md-3">
					<label class="col-md-6 control-label">分配概率</label>
					<div class="col-md-5">
						<input type="text" class="form-control" name="prizeTwoProbability" maxlength="15"/>
					</div>
					<label class="control-label">%</label>
				</div>
				<div class="col-md-4">
					<label class="col-md-7 control-label">奖品类型</label>
					<div class="col-md-5">
						<select class="form-control prizeClass" name="prizeTwoType">
							<option value="0"<#if (params.prizeTwoType!0) == 0> selected</#if>>未设置</option>
							<option value="1"<#if (params.prizeTwoType!0) == 1> selected</#if>>CDK</option>
							<option value="2"<#if (params.prizeTwoType!0) == 2> selected</#if>>Q币</option>
						</select>
					</div>
				</div>
				<div class="col-md-3">
					<label class="col-md-6 control-label">奖品价值</label>
					<div class="col-md-5">
						<input type="text" class="form-control" name="prizeTwoValue" maxlength="15" wy-required="奖品价值"/>
					</div>
				</div>
			</div>
			<br/>
			<div class="form-group col-md-12">
				<label class="col-md-1 control-label">奖品三：</label>
				<div class="col-md-1">
					<input type="text" class="form-control" name="prizeThreeNum" maxlength="25" placeholder="设置数量(-1为无穷)" wy-required="奖品"/>
				</div>
				<div class="col-md-3">
					<label class="col-md-6 control-label">分配概率</label>
					<div class="col-md-5">
						<input type="text" class="form-control" name="prizeThreeProbability" maxlength="15"/>
					</div>
					<label class="control-label">%</label>
				</div>
				<div class="col-md-4">
					<label class="col-md-7 control-label">奖品类型</label>
					<div class="col-md-5">
						<select class="form-control prizeClass" name="prizeThreeType">
							<option value="0"<#if (params.prizeThreeType!0) == 0> selected</#if>>未设置</option>
							<option value="1"<#if (params.prizeThreeType!0) == 2> selected</#if>>CDK</option>
							<option value="2"<#if (params.prizeThreeType!0) == 3> selected</#if>>Q币</option>
						</select>
					</div>
				</div>
				<div class="col-md-3">
					<label class="col-md-6 control-label">奖品价值</label>
					<div class="col-md-5">
						<input type="text" class="form-control" name="prizeThreeValue" maxlength="15" wy-required="奖品价值"/>
					</div>
				</div>
			</div>
			<br/>
			<div class="form-group col-md-12">
				<label class="col-md-1 control-label">奖品四：</label>
				<div class="col-md-1">
					<input type="text" class="form-control" name="prizeFourNum" maxlength="25" placeholder="设置数量(-1为无穷)" wy-required="奖品"/>
				</div>
				<div class="col-md-3">
					<label class="col-md-6 control-label">分配概率</label>
					<div class="col-md-5">
						<input type="text" class="form-control" name="prizeFourProbability" maxlength="15"/>
					</div>
					<label class="control-label">%</label>
				</div>
				<div class="col-md-4">
					<label class="col-md-7 control-label">奖品类型</label>
					<div class="col-md-5">
						<select class="form-control prizeClass" name="prizeFourType">
							<option value="0"<#if (params.prizeFourType!0) == 0> selected</#if>>未设置</option>
							<option value="1"<#if (params.prizeFourType!0) == 1> selected</#if>>CDK</option>
							<option value="2"<#if (params.prizeFourType!0) == 2> selected</#if>>Q币</option>
						</select>
					</div>
				</div>
				<div class="col-md-3">
					<label class="col-md-6 control-label">奖品价值</label>
					<div class="col-md-5">
						<input type="text" class="form-control" name="prizeFourValue" maxlength="15" wy-required="奖品"/>
					</div>
				</div>
			</div>
		</div>
		<div class="col-md-12">
		<br/>
		<br/>
			<button type="button" class="btn btn-info" style="float:right" onclick="saveActivity(2)">保存，开启活动</button>
			<button type="button" class="btn btn-info" style="float:right;margin-right:10px" onclick="saveActivity(1)">保存，暂不开启</button>
		</div>
		</form>
	</article>
	<script type="text/javascript">
	// 定义编辑器及表单
	var $editor = $('#editor'),
		$form = $editor.find('form');
	
	// 初始化编辑框
	$.fillForm({
		activityId: '${(params.activityId)!}',
		title: '${(params.title)!}',
		debrisOneNum: '${(params.debrisOneNum)!}',
		debrisOneProbability: '${(params.debrisOneProbability)!}',
		debrisTwoNum: '${(params.debrisTwoNum)!}',
		debrisTwoProbability: '${(params.debrisTwoProbability)!}',
		debrisThreeNum: '${(params.debrisThreeNum)!}',
		debrisThreeProbability: '${(params.debrisThreeProbability)!}',
		debrisFourNum: '${(params.debrisFourNum)!}',
		debrisFourProbability: '${(params.debrisFourProbability)!}',
		prizeOneNum: '${(params.prizeOneNum)!}',
		prizeOneProbability: '${(params.prizeOneProbability)!}',
		prizeTwoNum: '${(params.prizeTwoNum)!}',
		prizeTwoProbability: '${(params.prizeTwoProbability)!}',
		prizeThreeNum: '${(params.prizeThreeNum)!}',
		prizeThreeProbability: '${(params.prizeThreeProbability)!}',
		prizeFourNum: '${(params.prizeFourNum)!}',
		prizeFourProbability: '${(params.prizeFourProbability)!}',
		prizeOneType: '${(params.prizeOneType)!}',
		prizeTwoType: '${(params.prizeTwoType)!}',
		prizeThreeType: '${(params.prizeThreeType)!}',
		prizeFourType: '${(params.prizeFourType)!}',
		prizeOneValue: '${(params.prizeOneValue)!}',
		prizeTwoValue: '${(params.prizeTwoValue)!}',
		prizeThreeValue: '${(params.prizeThreeValue)!}',
		prizeFourValue: '${(params.prizeFourValue)!}',
		activityBeginTime: '${(params.activityBeginTime?string('yyyy-MM-dd HH:mm:ss'))!}',
		activityEndTime: '${(params.activityEndTime?string('yyyy-MM-dd HH:mm:ss'))!}',
	}, $editor);
	
	function saveActivity(type){
		var total = 0;
		$formFile = $('form[input="file"]');
		var formValid = $form.formValid();
		$(".probability").each(function(){
			total += parseInt($(this).val());
		});
		if(total == 100){
			$(".prizeClass").each(function(){
				var prizeValue = $(this).val();
				if(prizeValue == 0){
					formValid = false;
				}
			})
			if(formValid){
				$form.ajaxSubmit({
		             type: "post",
		             async:"false",
		             url: "/cohere/activity/save/probability?type="+type,
		             dataType: "json",
		             success: function(d) {
			             	if(d.code == 0){
			             		window.location.href='/cohere/activity/list/1';
			             	}else{
			             		if(d.code == -1){
			             			alert(d.result);	
			             		}else{
			             			alert("保存失败！");
			             		}
			             	}
						}
		 		});
			}else{
				alert("奖品类型设置不全！");
			}
		}else{
			alert("概率总和需要设置成100！");
		}
	}
	
	</script>
</body>
</html>