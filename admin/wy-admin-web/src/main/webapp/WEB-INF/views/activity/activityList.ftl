<#import "/macros/pager.ftl" as p >
<html>
<head>
<script type="text/javascript" editor-ctx="${ctx!}" src="${ctx!}/static/plugin/editor.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/ztree/js/jquery.ztree.all-3.5.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/ztree/js/jquery.ztree.exhide-3.5.min.js"></script>
<link rel="stylesheet" type="text/css" href="${ctx!}/static/plugin/ztree/css/zTreeStyle.css"/>
<style>
	ul, li {
		list-style: none;
		margin: 0;
		padding: 0;
	}
	div[div-round] .btn {
		margin-bottom: 10px;
	}
	#cascader > div, #preview > div {
		min-width: 200px;
		max-width: 400px;
		float: left;
	}
	ul.selected {
		margin-left: 10px;
		max-height: 600px;
		overflow: auto;
	}
	ul.selected li {
		padding: 5px;
		border: solid 1px #CCC;
		border-radius: 3px;
		margin: 5px 0 0 2px;
		position: relative;
		box-shadow: inset 0 1px 1px rgba(0,0,0,0.075);
		-moz-box-shadow: inset 0 1px 1px rgba(0,0,0,0.075);
		-webkit-box-shadow: inset 0 1px 1px rgba(0,0,0,0.075);
		padding-right: 25px;
	}
	a.remove {
		opacity: 0.6;
		filter: alpha(opacity=60);
		display: inline-block;
		width: 16px;
		height: 16px;
		margin: 0 5px 0 0;
		position: absolute;
		right: 0;
		top: 5px;
	}
	a.remove:hover {
		opacity: 1;
		filter: alpha(opacity=100);
	}
	#preview {
		box-shadow: rgb(204, 204, 204) 0px 0px 5px 0px;
		-moz-box-shadow: rgb(204, 204, 204) 0px 0px 5px 0px;
		-webkit-box-shadow: rgb(204, 204, 204) 0px 0px 5px 0px;
		position: absolute;
		border: solid 1px #CCC;
		border-radius: 5px;
		background-color: #FFF;
		z-index: 1100;
		padding: 10px;
		display: none;
	}
	div[div-round] {
	    border-top: solid 10px #E9E9E9;
	    border-top-style: solid;
	    padding-top: 10px;
        margin-bottom: 0;
	}
	tr[round] {
		border-top: solid 10px #E9E9E9;
		border-top-style: solid;
	}
	.a-btn {
		display: inline-block;
		font-size: 20px;
	}
	.panel-tool-close {
	    background: url('${ctx!}/static/images/panel_tools.png') no-repeat -16px 0px;
	}
	#input-search {
		width: initial;
		min-width: 200px;
		max-width: 400px;
		display: inline-block;
	}
	.label {
		display: inline-block;
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
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
			<div class="col-md-2">
				<input type="text" class="form-control" name="title" placeholder="赛事名" value="${(params.title)!}" />
			</div>
			<div class="col-md-2">
				<input class="form-control" type="text" id="search-beginDate" name="beginDate" placeholder="最早开始时间" value="${(params.beginDate)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',maxDate:'#F{$dp.$D(\'search-endDate\',{d:0});}'})">
			</div>
			<div class="col-md-2">
				<input class="form-control" type="text" id="search-endDate" name="endDate" placeholder="最晚结束时间" value="${(params.endDate)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',minDate:'#F{$dp.$D(\'search-beginDate\',{d:0});}'})">
			</div>
			<div class="col-md-2">
				<select class="form-control" name="itemId">
					<option value="">全部项目</option>
					<#if items??>
					<#list items as i>
						<option value="${i.id!}"<#if (params.item_id)?? && params.item_id == i.id?string> selected</#if>>${i.name!}</option>
					</#list>
					</#if>
				</select>
			</div>
			<#if ((Session.user.userType)!1) != 10>
			<div class="col-md-2">
				<select class="form-control" name="areaCode">
					<option value="">全部地区</option>
					<#if areas??>
					<#list areas as a>
						<option value="${a.areaCode!}"<#if (params.areaCode)?? && params.areaCode == a.areaCode> selected</#if>>${a.name!}</option>
					</#list>
					</#if>
				</select>
			</div>
			</#if>
			<div class="col-md-1">
				<select class="form-control" name="status">
					<option value="">全部状态</option>
					<option value="1"<#if (params.status!) == "1"> selected</#if>>报名中</option>
					<option value="5"<#if (params.status!) == "5"> selected</#if>>进行中</option>
					<option value="2"<#if (params.status!) == "2"> selected</#if>>报名预热中</option>
					<option value="3"<#if (params.status!) == "3"> selected</#if>>报名已截止</option>
					<option value="4"<#if (params.status!) == "4"> selected</#if>>已结束</option>
				</select>
			</div>
			<button type="submit" class="btn btn-success">查询</button>
		</form>
	</div>
	
	<#-- 操作按钮 -->
	<div class="mb10">
		<button id="new-record" type="button" class="btn btn-success">新增记录</button>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>#</th>
			<th>赛事名</th>
			<th>游戏项目</th>
			<th>地区</th>
			<th>开始时间</th>
			<th>结束时间</th>
			<th>评分</th>
			<th>在APP中显示</th>
			<th>在微信中显示</th>
			<th>查看报名</th>
			<th>状态</th>
			<th>app推荐位置</th>
			<th>操作</th>
		</tr>
		<#list list as o>
			<tr>
				<td>${o.id!}</td>
				<td>${o.title!}</td>
				<td>${o.itemName!}</td>
				<td>${(o.areaName)!}</td>
				<td>${(o.startTime?string("yyyy-MM-dd"))!}</td>
				<td>${(o.endTime?string("yyyy-MM-dd"))!}</td>
				<td>${o.score!}</td>
				<td><input type="checkbox" name="isGround" act-id="${o.id!}"<#if (o.isGround!0) == 1> checked</#if>/></td>
				<td><input type="checkbox" name="inWx" act-id="${o.id!}"<#if (o.inWx!0) == 1> checked</#if>/></td>
				<td>
					<a href="${ctx!}/activityInfo/apply/choose?activityId=${o.id!}">个人报名${(o.member)!}</a> |
					<a href="${ctx!}/activityInfo/apply/choose?activityId=${o.id!}&isTeam=1">战队报名${(o.teamMember)!}</a>
				</td>
				<td>
					<#if (o.status)??>
						<#if o.status == 1>
						报名中
						<#elseif o.status == 2>
						报名预热中
						<#elseif o.status == 3>
						报名已截止
						<#elseif o.status == 4>
						已结束
						<#elseif o.status == 5>
						进行中
						<#else>
						${(o.status)!}
						</#if>
					</#if>
				</td>
				<td>
					<#if o.indexSum?? && o.indexSum gt 0>
						<span class="label label-primary">首页热门赛事</span><br/>
					</#if>
					<#if o.hallSum?? && o.hallSum gt 0>
						<span class="label label-primary">竞技大厅卡片区</span><br/>
					</#if>
					<#if o.mid1Sum?? && o.mid1Sum gt 0>
						<span class="label label-primary">腰图1</span><br/>
					</#if>
					<#if o.mid2Sum?? && o.mid2Sum gt 0>
						<span class="label label-primary">腰图2</span><br/>
					</#if>
					<#if o.recommendSum?? && o.recommendSum gt 0>
						<span class="label label-primary">官方活动推荐区</span><br/>
					</#if>
					<#if o.indexAdvSum?? && o.indexAdvSum gt 0>
						<span class="label label-primary">首页banner</span><br/>
					</#if>
					<#if o.hallAdvSum?? && o.hallAdvSum gt 0>
						<span class="label label-primary">竞技大厅banner</span><br/>
					</#if>
				</td>
				<td>
					<a type="button" class="btn btn-danger" href="${ctx}/activityInfo/changciinfo/1?activityId=${(o.id)!}">二维码</a>
					<button edit="${o.id!}" type="button" class="btn btn-info">编辑</button>
					<button remove="${o.id!}" type="button" class="btn btn-danger">删除</button>
					<#if (o.qrcode)??>
					<#else>
						<button init="${o.id!}" type="button" class="btn btn-info">初始化</button>
					</#if>
				</td>
			</tr>
		</#list>
	</table>
	<script type="text/javascript">
		// 在app中显示、在微信中显示复选框 修改事件
		$('input[name="isGround"], input[name="inWx"]').on('change', function(event) {
			// 取消勾选操作
			$this = $(this);
			$this.prop('checked', !$this.prop('checked'));
			
			$.confirm('确认勾选吗?', function() {
				var actId = $this.attr('act-id');
				var name = $this.attr('name');
				var intValue = $this.prop('checked') ? 0 : 1;
				var url = '${ctx}/activityInfo/save?id=' + actId;
				if(name == 'isGround') {
					$.api(url + '&isGround=' + intValue, {}, function(d) {$this.prop('checked', !$this.prop('checked'));console.log(d);}, function(d) {alert(d.result);});
				} else {
					$.api(url + '&inWx=' + intValue, {}, function(d) {$this.prop('checked', !$this.prop('checked'));console.log(d);}, function(d) {alert(d.result);});
				}
			}, false);
		});
	</script>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	
	<#-- 编辑 -->
	<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
	<div id="modal-editor" class="modal fade">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideEditor()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title">编辑赛事</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post">
						<input type="hidden" name="id" />
						<input type="hidden" name="roundCount" />
						<input type="hidden" name="areaCode" />
						<input type="hidden" name="sortNum" />
						<div class="form-group">
							<label class="col-md-2 control-label">标题</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="title" maxlength="45" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">简述</label>
							<div class="col-md-10">
								<textarea name="summary" style="width: 100%;height: 65px;border: 1px solid #CCC;"></textarea>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">发布人</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="releaser" maxlength="10" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">方式</label>
							<div class="col-md-10">
								<select class="form-control" name="way">
									<option value="1">线下</option>
									<option value="2">线上</option>
								</select>
							</div>
						</div>
						<div class="form-group">
							<div class="form-group" id="div-areas">
								<label class="col-md-2 control-label">归属地区</label>
								<div class="col-md-10">
									<ul id="tree-areas" class="ztree"></ul>
								</div>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">是否允许</label>
							<div class="col-md-10">
								<!-- <label><input type="checkbox" id="cbxPersonAllow" value="1">个人报名</label>
								<label><input type="checkbox" id="cbxTeamAllow" value="1">战队报名</label> -->
								<label><input type="radio" id="cbxPersonAllow" name="apply-type" value="1">个人报名</label>
								<label><input type="radio" id="cbxTeamAllow" name="apply-type" value="1">战队报名</label>
								<input type="hidden" name="personAllow"  />
								<input type="hidden" name="teamAllow"  />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">是否必填</label>
							<div class="col-md-10">
								<label><input type="checkbox" id="cbMobileRequired" property="mobileRequired" value="1">用户联系电话</label>
								<label><input type="checkbox" id="cbIdcardRequired" property="idcardRequired" value="1">用户身份证</label>
								<label><input type="checkbox" id="cbNicknameRequired" property="nicknameRequired" value="1">用户昵称</label>
								<label><input type="checkbox" id="cbQqRequired" property="qqRequired" value="1">用户QQ</label>
								<label><input type="checkbox" id="cbLaborRequired" property="laborRequired" value="1">擅长位置</label>
								<input type="hidden" name="mobileRequired"  />
								<input type="hidden" name="idcardRequired"  />
								<input type="hidden" name="nicknameRequired"  />
								<input type="hidden" name="qqRequired"  />
								<input type="hidden" name="laborRequired"  />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">在APP中发布</label>
							<div class="col-md-10">
								<select class="form-control" name="isGround">
									<option value="1">发布</option>
									<option value="0">不发布</option>
								</select>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">缩略图</label>
							<div class="col-md-10">
								<input type="file" name="icon_file" value="上传新图" accept="image/*" />
								<font class="prompt">
									请上传71:42宽高比的图片，以达到最佳显示效果
								</font>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">首图</label>
							<div class="col-md-10">
								<input type="file" name="cover_file" value="上传新图" accept="image/*" />
								<font class="prompt">
									请上传64:31宽高比的图片，以达到最佳显示效果
								</font>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">描述奖品</label>
							<div class="col-md-10">
								<textarea name="spoils" style="width: 100%;height: 65px;border: 1px solid #CCC;"></textarea>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">详情</label>
							<div class="col-md-10">
								<textarea id="editor-detail" name="remark" placeholder="Balabala" autofocus></textarea>
								<script type="text/javascript">
									_editor = editor($('#editor-detail'));
								</script>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">评分</label>
							<div class="col-md-10">
								<div class="input-group">
									<input class="form-control" type="text" name="score" value="0" readonly>
									<span class="input-group-btn"> 
										<button class="btn btn-default" type="button" onclick="addScore(1)">+</button>
										<button class="btn btn-default" type="button" onclick="addScore(-1)">-</button> 
									</span>
									<script type="text/javascript">
										function addScore(num) {
											var $score = _$editor.find('[name="score"]');
											var score = parseInt($score.val()) + num;
											var maxScore = 5;
											var minScore = 0;
											if(score > maxScore) {
												score = maxScore;
											} else if(score < minScore) {
												score = minScore;
											}
											$score.val(score);
										}
									</script>
								</div>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">游戏项目</label>
							<div class="col-md-10">
								<select class="form-control" name="itemId">
								<#if items??>
								<#list items as i>
									<option value="${i.id!}">${i.name!}</option>
								</#list>
								</#if>
								</select>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">活动开始时间</label>
							<div class="col-md-10">
								<input class="form-control" type="text" id="edit-startTime" name="startTime"  onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',maxDate:'#F{$dp.$D(\'edit-endTime\',{d:0});}'})" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">活动结束时间</label>
							<div class="col-md-10">
								<input class="form-control" type="text" id="edit-endTime" name="endTime"  onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',minDate:'#F{$dp.$D(\'edit-startTime\',{d:0});}'})" />
							</div>
						</div>
						<!-- 场次设置 -->
						<div div-round="0" class="form-group">
							<label class="col-md-2 control-label">第1场</label>
							<div class="col-md-10">
								<input class="form-control" type="text" placeholder="报名开始时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd'})" />
								<input class="form-control" type="text" placeholder="开赛开始时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd'})" />
								<input class="form-control" type="text" placeholder="比赛结束时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd'})" />
								<input class="form-control" type="text" maxlength="15" placeholder="场次说明" />
								<button class="btn btn-large btn-block btn-primary" type="button">设置网吧及地点</button>
								<input id="hidden-netbars" type="hidden" name="rounds[0].netbars"  />
								<input id="hidden-areas" type="hidden" name="rounds[0].areas"  />
							</div>
						</div>
						<div div-round="-1"><button class="btn btn-large btn-block btn-info" type="button" onclick="addRound()">添加场次</button></div>
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" onclick="hideEditor()">关闭</button>
					<button id="save-activity" type="button" class="btn btn-primary">保存</button>
				</div>
			</div>
		</div>
	</div>
	
	<#-- 新增资讯（专题） -->
	<div id="modal-addInfom" class="modal fade">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideEditor()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title">新增赛事资讯（专题）</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form_infom" method="post">
						<input type="hidden" name="category" />
						<input type="hidden" name="activityId" />
						<div class="form-group">
							<label class="col-md-2 control-label">标题</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="title" maxlength="50" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">缩略图</label>
							<div class="col-md-10">
								<input type="file" name="file" value="上传新图" accept="image/*" />
								<font class="prompt">
									请上传30:23宽高比的图片，以达到最佳显示效果
								</font>
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" onclick="hideEditor()">关闭</button>
					<button type="button" class="btn btn-primary" onclick="submitEditorInfom()">保存</button>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
	$('input[name]').attr("autocomplete", "off");
	
	// 初始化模态框
	var _$editor = $('#modal-editor');
	var _$editor_addInfom = $('#modal-addInfom');
	_$editor.modal({
		keyboard : false,
		show : false
	})
	
	// 关闭模态框
	function hideEditor() {
		_$editor.modal('hide');
		_$editor_addInfom.modal('hide');
	}
	
	// 添加场次
	function addRound() {
		$('[div-round]:eq(0)').before(roundDom());
		initAreasNetbarsEvent();
	}
	
	// 生成场次dom(round从0开始计数)
	function roundDom(round, id, beginTime, overTime, endTime, netbars, areas, remark) {
		if(typeof(round) == 'undefined') {
			round = parseInt($('div[div-round]:eq(0)').attr('div-round')) + 1;
		}
		var html = '<div div-round="' + round + '" class="form-group">'
		 + '	<input type="hidden" name="rounds[' + round + '].id" value="' + (id?id:'') + '" />'
		 + '	<input type="hidden" name="rounds[' + round + '].round" value="' + (round + 1) + '" />'
		 + '	<label class="col-md-2 control-label">第' + (round + 1) + '场</label>'
		 + '	<div class="col-md-10">'
		 + '		<input class="form-control" type="text" id="round-beginTime' + round + '" name="rounds[' + round + '].beginTime" value="' + (beginTime?beginTime:'') + '" placeholder="报名开始时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:\'yyyy-MM-dd\',maxDate:\'#F{$dp.$D(\\\'round-overTime' + round + '\\\',{d:0});}\'})">'
		 + '		<input class="form-control" type="text" id="round-overTime' + round + '" name="rounds[' + round + '].overTime" value="' + (beginTime?overTime:'') + '" placeholder="比赛开始时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:\'yyyy-MM-dd\',minDate:\'#F{$dp.$D(\\\'round-beginTime' + round + '\\\',{d:0});}\',maxDate:\'#F{$dp.$D(\\\'round-endTime' + round + '\\\',{d:0});}\'})">'
		 + '		<input class="form-control" type="text" id="round-endTime' + round + '" name="rounds[' + round + '].endTime" value="' + (beginTime?endTime:'') + '" placeholder="比赛结束时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:\'yyyy-MM-dd\',minDate:\'#F{$dp.$D(\\\'round-overTime' + round + '\\\',{d:0});}\'})">'
		 + '		<input class="form-control" type="text" maxlength="15" placeholder="场次说明" name="rounds[' + round + '].remark" value="' + (remark?remark:'') + '" />'
		 + '		<button id="input-areas-netbars" class="btn btn-large btn-block btn-primary" type="button">设置网吧及地点</button>'
		 + '		<input id="hidden-netbars" type="hidden" name="rounds[' + round + '].netbars" value="' + (netbars?netbars:'') + '" />'
		 + '		<input id="hidden-areas" type="hidden" name="rounds[' + round + '].areas" value="' + (areas?areas:'') + '" />'
		 + '	</div>'
		 + '</div>';
		
		return html;
	}
	
	// 编辑
	$('button[edit]').on('click', function(event) {
		initTree();
		// 初始化复选框事件
		$('#cbMobileRequired, #cbIdcardRequired, #cbNicknameRequired, #cbQqRequired, #cbLaborRequired').on('change', function() {
			var property = $(this).attr('property');
			var selector = 'input[name="' + property + '"]';
			if($(this).prop('checked')) {
				$(selector).val(1);
			} else {
				$(selector).val(0);
			}
		});
		
		// 初始化表单数据
		var id = $(this).attr('edit');
		$.api('${ctx}/activityInfo/detail/' + id, {}, function(d) {
			var o = d.object;
			var startTime = new Date(o.startTime).Format('yyyy-MM-dd');
			var endTime = new Date(o.endTime).Format('yyyy-MM-dd');
			$.fillForm({
				title: o.title,
				releaser: o.releaser,
				mobileRequired: o.mobileRequired,
				idcardRequired: o.idcardRequired,
				nicknameRequired: o.nicknameRequired,
				qqRequired: o.qqRequired,
				laborRequired: o.laborRequired,
				isGround: o.isGround,
				icon_file: o.icon,
				cover_file: o.cover,
				remark: o.remark,
				score: o.score,
				itemId: o.itemId,
				spoils: o.spoils,
				startTime: startTime,
				endTime: endTime,
				summary: o.summary,
				sortNum: o.sortNum,
				way: o.way,
				id: o.id,
			}, _$editor);
			
			initCheckedArea(o.areaCode);
			
			$('#cbxPersonAllow').prop('checked', o.personAllow == 1);
			
			$('#cbxTeamAllow').prop('checked', o.teamAllow == 1);
			
			if(o.mobileRequired == 1) {
				$('#cbMobileRequired').prop('checked', true);
			}
			if(o.idcardRequired == 1) {
				$('#cbIdcardRequired').prop('checked', true);
			}
			if(o.nicknameRequired == 1) {
				$('#cbNicknameRequired').prop('checked', true);
			}
			if(o.qqRequired == 1) {
				$('#cbQqRequired').prop('checked', true);
			}
			if(o.laborRequired == 1) {
				$('#cbLaborRequired').prop('checked', true);
			}
			
			// 初始化编辑器内容
			setEditorText($('#editor-detail'), o.remark);
			
			// 初始化场次信息
			initRoundEditors(o.rounds);
		}, function(d) {
			alert('请求数据异常：' + d.result);
		}, {
			async: false,
		});
		
		showEditor();
	});
	
	$('#new-record').on('click', function() {
		initTree();
		
		$.fillForm({
			icon_file: '',
			cover_file: '',
			score: 0,
			sortNum: 0,
		}, _$editor);
		
		setEditorText($('#editor-detail'), '');
		initCheckedArea('${(userAreaCode!)}');
		
		initRoundEditors();
		showEditor();
	});
	
	// 初始化编辑窗口宽度
	function showEditor() {
		var width = 1200;
		var height = $(window).height() - 200;
		if(width > $(window).width()) {
			width = $(window).width();
		}
		_$editor.find('.modal-dialog').css('width', width);
		_$editor.find('.modal-body').css({
			height: height,
			overflow: 'scroll',
		});
		_$editor.modal('show', 'fit');
	}
	
	// 初始化场次编辑区
	function initRoundEditors(rounds) {
		$('div[div-round][div-round!="-1"]').remove();
		if(rounds != undefined && rounds.length > 0) {
			for(var i=0;i< rounds.length; i++) {
				var round = rounds[i];
				var beginTime = '', overTime = '', endTime = '';
				if(round.beginTime) {
					beginTime = new Date(round.beginTime).Format('yyyy-MM-dd');
				}
				if(round.overTime) {
					overTime = new Date(round.overTime).Format('yyyy-MM-dd');
				}
				if(round.endTime) {
					endTime = new Date(round.endTime).Format('yyyy-MM-dd');
				}
				
				$('[div-round]:eq(0)').before(roundDom((round.round-1), round.id, beginTime, overTime, endTime, round.netbars, round.areas, round.remark));
			}
		} else {
			$('[div-round]:eq(0)').before(roundDom(0));
		}
		initAreasNetbarsEvent();
	}
	
	// 提交表单
	$('#save-activity').on('click', function() {
		var $this = $(this);
		$this.prop('disabled', true);
		
		var $form = _$editor.find('form');
		var roundSign = parseInt($('div[div-round]:eq(0)').attr('div-round'));
		$form.find('input[name="roundCount"]').val(roundSign + 1);
		
		// 判断描述奖品是否合法
		/* var spoils = $form.find('[name="spoils"]').val();
		if(spoils.length > 0) {
			var re = new RegExp("\n","g");
			var ms = spoils.match(re);
			if(typeof(ms) == 'undefined' || ms == null || ms.length != 2) {
				alert('请输入3段内容作为描述奖品');
				$this.prop('disabled', false);
				return;
			}
		} */
		
		// 校验图片上传格式是否为图片
		var types = "image/png,image/jpeg,image/gif";
		var iconFiles = $form.find('[name="icon_file"]').get(0).files;
		if(iconFiles.length > 0) {
			for(var i=0; i<iconFiles.length; i++) {
				var file = iconFiles[i];
				if(types.indexOf(file.type) == -1) {
					alert('缩略图图片类型错误');
					$this.prop('disabled', false);
					return;
				}
			}
		}
		var coverFiles = $form.find('[name="cover_file"]').get(0).files;
		if(coverFiles.length > 0) {
			for(var i=0; i<coverFiles.length; i++) {
				var file = coverFiles[i];
				if(types.indexOf(file.type) == -1) {
					alert('首图图片类型错误');
					$this.prop('disabled', false);
					return;
				}
			}
		}
		
		// 获取个人、战队报名允许的状况
		var personAllow = $('#cbxPersonAllow').prop('checked') ? 1 : 0;
		var teamAllow = $('#cbxTeamAllow').prop('checked') ? 1 : 0;
		$form.find('input[name="personAllow"]').val(personAllow);
		$form.find('input[name="teamAllow"]').val(teamAllow);
		
		// 保存地区信息
		var checkedAreas = getCheckedArea();
		if(checkedAreas.length > 0) {
			var n = checkedAreas[0];
			$('input[name="areaCode"]').val(n.areaCode);
		}
		
		$form.ajaxSubmit({
			url:'${ctx}/activityInfo/save',
		    type:'post',
		    success: function(d){
		    	if(d.code == 0) {
		    		window.location.reload();
		    	} else {
		    		alert(d.result);
		    		$this.prop('disabled', false);
		    	}
			},
		});
	});
	
	// 新增资讯（专题）
	$('button[addInfom]').on('click', function(event) {
		var id = $(this).attr('addInfom');
		$.fillForm({
				category: 2,
				activityId: id,
			}, _$editor_addInfom);
		_$editor_addInfom.modal('show');
	});
	
	// 提交表单，新增资讯（专题）
	function submitEditorInfom() {
		var $form = _$editor_addInfom.find('form');
		
		// 校验上传图片格式是否正确
		var types = "image/png,image/jpeg,image/gif";
		var iconFiles = $form.find('[name="file"]').get(0).files;
		if(iconFiles.length > 0) {
			for(var i=0; i<iconFiles.length; i++) {
				var file = iconFiles[i];
				if(types.indexOf(file.type) == -1) {
					alert('缩略图图片类型错误');
					$this.prop('disabled', false);
					return;
				}
			}
		}
		
		$form.ajaxSubmit({
			url:'${ctx}/activityInformation/subject/save',// ${ctx}/activityInformation/save
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
	
	// 删除
	$('button[remove]').on('click', function(event) {
		var _this = $(this);
		_this.attr('disabled', true);
		
		var id = $(this).attr('remove');
		
		$.confirm('确认删除吗?', function() {
			$.api('${ctx}/activityInfo/delete/' + id, {}, function(d) {
				window.location.reload();
			}, function(d) {
				alert('删除失败：' + d.result);
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
	
	<!-- 预览窗口 -->
	<div id="preview">
		<div>
			<div class="prompt">选中地区:</div>
			<ul id="preview-areas" class="selected"></ul>
		</div>
		<div>
			<div class="prompt">选中网吧:</div>
			<ul id="preview-netbars" class="selected"></ul>
		</div>
	</div>

	<!-- 场次设置 -->
	<div class="modal fade" id="win" style="background-color: rgba(0, 0, 0, 0.31);">
		<div class="modal-dialog" style="min-width: 990px;">
			<div class="modal-content" style="display: inline-block; padding: 10px;">
				<div id="cascader">
					<div>
						<form id="form-search" style="display: inline;">
							<input id="input-search" class="form-control" type="text" placeholder="请输入网吧名" /> 
						</form>
						<a id="search" href="javascript:void(0)">搜索</a> 
						<a id="cancel-search" href="javascript:void(0)">取消</a>
						<ul id="select-tree" class="ztree" style="max-height: 600px;overflow: auto;"></ul>
					</div>
					<div>
						<div class="prompt">选中地区:</div>
						<ul id="select-areas" class="selected"></ul>
					</div>
					<div>
						<div class="prompt">选中网吧:</div>
						<ul id="select-netbars" class="selected"></ul>
					</div>
					<div border="false" style="width: 100px;min-width: 100px;margin: 10px;position: absolute;right: 0;">
					    <a class="btn" href="javascript:void(0)" onclick="javascript:saveRound()">确定</a>
					    <a class="btn" href="javascript:void(0)" onclick="javascript:hideEditRoundModal()" style="margin-top: 10px;">取消</a>
					</div>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
	// 场次dom属性
	var _roundDomAttr = 'div-round';
	function showEditRoundModal() {
		$('#win').modal('show');
	}
	function hideEditRoundModal() {
		$('#win').modal('hide');
	}
	
	// 保存对网吧地区的编辑
	function saveRound() {
		var $tr = $('[' + _roundDomAttr + '="' + _roundNum + '"]');
		$tr.find('#hidden-areas').val(_areas.toSplitString());
		$tr.find('#hidden-netbars').val(_netbars.toSplitString());
		hideEditRoundModal();
	}

	// 全局变量
	var _expandNode = null,// 展开的节点，用于匹配加载url
		_roundNum = -1,// 当前编辑的场次
		_areas = [],//当前编辑的地区码列表
		_netbars = [],// 当前编辑的网吧id列表
		_previewRound = -1;// 当前预览的场次号
	
	// 产生li字符串
	function li(attr, name, showRemove) {
		if(typeof(showRemove) == 'undefined') {
			showRemove = true;
		}
		return '<li' + (attr ? ' ' + attr : '') + '>' + name + (!showRemove ? '' : '<a class="panel-tool-close remove" href="javascript:void(0)"></a>') + '</li>';	
	}
	
	// 选择子节点时的过滤器，须设置父级节点为全局变量_treeNode
	var _treeNode = null;
	function childrenFilter(node) {
		if(node.parentTId == _treeNode.tId) {
			 return true;
		} else {
			return false;
		}
	}
	function getChildren(treeId, treeNode) {
		_treeNode = treeNode;
		return $tree.getNodesByFilter(childrenFilter);
	}
	
	// 加载地区下的网吧数据
	function sureCityAsync(treeId, cityNode) {
		if(!cityNode.zAsync) {// 只在无数据时加载，不重复加载
			_expandNode = cityNode;
			$.fn.zTree.getZTreeObj(treeId).reAsyncChildNodes(cityNode, 'refresh', true);
		}
	}
		
	// 初始化地区、网吧输入框的点击事件
	function initAreasNetbarsEvent() {
		var $input = $('div[' + _roundDomAttr + '] #input-areas-netbars');
		$input.off('click');
		$input.on('click', function(event) {
			_netbars = [];
			_areas = [];
			
			var $win = $('#win');
			showEditRoundModal();
			_roundNum = parseInt($(this).parents('[' + _roundDomAttr + ']').attr('' + _roundDomAttr + ''));
			var $roundData = $('[' + _roundDomAttr + '="' + _roundNum + '"]');
			_previewRound = -1;
			
			// 初始化地区的选择状态
			var treeId = 'select-tree';
			$tree.checkAllNodes(false);
			_areas = $roundData.find('#hidden-areas').val().split(',');
			var $tCities = $tree.getNodesByParam('level', 1, null);
			var selectAreas = '',
				selectNetbars = '';
			for(var i in $tCities) {
				var city = $tCities[i];
				if(_areas.contains(city.areaCode)) {
					$tree.checkNode(city, true, null);
					selectAreas += li('city-areaCode="' + city.areaCode + '"', city.name);
					
					// 初始化网吧的选择状态
					_netbars = $roundData.find('#hidden-netbars').val().split(',');
					var $tNetbars = getChildren(treeId, city);
					for(var j in $tNetbars) {
						var netbar = $tNetbars[j];
						if(_netbars.contains(netbar.id)) {
							$tree.checkNode(netbar, true, null);
							selectNetbars += li('netbar-id="' + netbar.id + '"', netbar.name);
						}
					}
				}
				
				// 折叠城市
				$tree.expandNode(city, false);
			}
			$win.find('#select-areas').html(selectAreas);
			$win.find('#select-netbars').html(selectNetbars);
			initDeletBtns();
			$('#cancel-search').click();
		});
		
		$input.off('mouseover');
		$input.on('mouseover', function(event) {
			var $preview = $('#preview');
			
			// 初始化数据
			var round = parseInt($(this).parents('[' + _roundDomAttr + ']').attr('' + _roundDomAttr + ''));
			if(true || _previewRound != round) {// 因数据加载慢时会导致加载完成后也显示不出来，此处短路掉这层判断
				_previewRound = round;
				var $roundData = $('[' + _roundDomAttr + '="' + round + '"]');
				
				// 初始化地区的选择状态
				var treeId = 'select-tree';
				var tmpAreas = $roundData.find('#hidden-areas').val().split(',');
				var $tCities = $tree.getNodesByParam('level', 1, null);
				var selectAreas = '',
					selectNetbars = '';
				for(var i in $tCities) {
					var city = $tCities[i];
					if(tmpAreas.contains(city.areaCode)) {
						selectAreas += li('city-areaCode="' + city.areaCode + '"', city.name, false);
						
						// 初始化网吧的选择状态
						tmpNetbars = $roundData.find('#hidden-netbars').val().split(',');
						var $tNetbars = getChildren(treeId, city);
						for(var j in $tNetbars) {
							var netbar = $tNetbars[j];
							if(tmpNetbars.contains(netbar.id)) {
								selectNetbars += li('netbar-id="' + netbar.id + '"', netbar.name, false);
							}
						}
					}
				}
				
				$preview.find('#preview-areas').html(selectAreas);
				$preview.find('#preview-netbars').html(selectNetbars);
			}
			
			// 显示预览窗
			$preview.css({
				top: $(this).offset().top - $preview.height() - 95,
				left: $(this).offset().left - 300,
			}).show();
		});
		
		$input.off('mouseout');
		$input.on('mouseout', function() {
			var $preview = $('#preview');
			$preview.hide();
		});
		$('#preview').off('mouseover');
		$('#preview').on('mouseover', function() {
			$('#preview').show();
		});
		$('#preview').off('mouseout');
		$('#preview').on('mouseout', function() {
			$('#preview').hide();
		});
	}
	
	// 初始化删除按钮
	function initDeletBtns() {
		// 删除地区
		$('#select-areas .remove').off('click');
		$('#select-areas .remove').on('click', function() {
			var $tree = $.fn.zTree.getZTreeObj('select-tree');
			var areaCode = $(this).parent().attr('city-areacode');
			var areas = $tree.getNodesByParam('areaCode', areaCode);
			for(var i=0; i<areas.length; i++) {
				console.log(areas.length + ',' + i);
				var area = areas[i];
				if(area.checked) {
					$tree.checkNode(areas[i], false, true, true);
				}
			}
			_areas.removeEle(areaCode);
			$(this).parent().remove();
		});
		
		// 删除网吧
		$('#select-netbars .remove').off('click');
		$('#select-netbars .remove').on('click', function() {
			var $tree = $.fn.zTree.getZTreeObj('select-tree');
			var netbarId = $(this).parent().attr('netbar-id');
			var netbars = $tree.getNodesByParam('netbarId', netbarId);
			for(var i=0; i<netbars.length; i++) {
				var netbar = netbars[i];
				if(netbar && $tree.getNodesByParam('checked', true, netbar.getParentNode()).length <= 1) {
					$('li[city-areacode="' + netbar.getParentNode().areaCode + '"] .remove').click();
				}
				if(netbar.checked) {
					$tree.checkNode(netbar, false, true, true);
				}
			}
			_netbars.removeEle(netbarId);
			$(this).parent().remove();
		});
	}
	
	// 初始化地区树
	var setting = {
		check: {
			enable: true
		},
		async: {
			enable: true,
			url: function() {
				return '${ctx!}/netbar/queryByAreaCode?areaCode=' + _expandNode.areaCode;
			},
			dataFilter: function(treeId, parentNode, responseData) {// 过滤网吧数据，仅保留id、name属性
				var result = [];
				if(responseData.object) {
					for(var i=0; i<responseData.object.length; i++) {
						var netbar = responseData.object[i];
						if(netbar.id) {
							var n = {};
							n.id = netbar.id;
							n.name = netbar.name + (netbar.telephone ? '(' + netbar.telephone + ')' : '');
							n.netbarId = netbar.id;
							result.push(n);
						}
					}
				}
				return result;
			}
		},
		callback: {
			beforeExpand: function(treeId, treeNode) {// 展开事件：设置展开的节点，使加载数据时使用此节点的areaCode
				if(treeNode.level == 1) {
					_expandNode = treeNode;
				}
				return true;
			},
			beforeCheck: function(treeId, treeNode) {// 更改选择事件：同步数据
				var level = treeNode.level;
				var $win = $('#win');
				
				// 勾选或取消网吧
				function checkNetbars(treeNode, isChecked) {
					if(isChecked) {
						$win.find('#select-netbars').append(li('netbar-id="' + treeNode.id + '"', treeNode.name));
						_netbars.push(treeNode.id);
					} else {
						$win.find('#select-netbars').find('[netbar-id="' + treeNode.id + '"]').remove();
						_netbars.removeEle(treeNode.id);
					}
				}
				
				var chekced = !treeNode.checked;// 当前的选中状态（在before中checked是选前的状态，除了一级地区）
				if(level == 1) {// 市
					// 勾选或取消地区
					if(!treeNode.checked) {
						$win.find('#select-areas').append(li('city-areaCode="' + treeNode.areaCode + '"', treeNode.name));
						_areas.push(treeNode.areaCode);
					} else {
						$win.find('#select-areas').find('[city-areaCode="' + treeNode.areaCode + '"]').remove();
						_areas.removeEle(treeNode.areaCode);
					}
					
					// 勾选全部网吧，或取消所有网吧
					$('#cancel-search').click();
					var tCityNetbars = $tree.getNodesByParam('checked', !chekced, treeNode);
					for(var i in tCityNetbars) {
						var netbar = tCityNetbars[i];
						if(netbar.id) {
							checkNetbars(netbar, chekced);
						}
					}
					
					initDeletBtns();
					return true;
				} else if(level == 2) {// 网吧
					checkNetbars(treeNode, chekced);
					
					// 添加或删除地区信息
					var tCity = treeNode.getParentNode();
					if(chekced && !_areas.contains(tCity.areaCode)) {
						_areas.push(tCity.areaCode);
						$('#win').find('#select-areas').append(li('city-areaCode="' + tCity.areaCode + '"', tCity.name));
					} else if(!chekced) {
						// 判断地区是否需要移除（其下是否有选中网吧）
						var tCityNetbars = $tree.getNodesByParam('checked', true, tCity);
						if(tCityNetbars.length <= 1) {
							var areaCode = tCity.areaCode;
							$('#win').find('li[city-areacode="' + areaCode + '"]').remove();
							_areas.removeEle(areaCode);
						}
					}
					
					initDeletBtns();
					return true;
				}
				return false;// 不允许非市、网吧的勾选
			}
		},
	}
	
	// 获取地区数据（请求接口，并保留两级数据）
	var zNodes = [];
	$.ajax({
		url: '${ctx!}/area/tree',
		success: function(d) {
			if(d.result) {
				var result = [];
				// 产生省节点
				var provinces = d.object;
				for(var i = 0; i<provinces.length; i++) {
					var province = provinces[i];
					// 产生市节点
					var cities = [];
					for(var j=0; j<province.children.length; j++) {
						var city = province.children[j];
						if(city.id) {
							var c = {};
							c.id = city.id;
							c.areaCode = city.areaCode;
							c.name = city.name;
							c.isParent = true;
							cities.push(c);
						}
					}
					// 记录节点
					if(cities.length > 0) {
						var p = {};
						p.id  = province.id;
						p.areaCode = province.areaCode;
						p.name = province.name;
						p.children = cities;
						p.open = true;
						result.push(p);
					}
				}
				zNodes = result;
			}
		},
		async: false,// 确保取得数据后再初始化树
	});
	
	// 初始化地区树
	var $tree = false;
	function initTree() {
		if($tree == false) {
			$tree = $.fn.zTree.init($("#select-tree"), setting, zNodes);
			
			// 加载网吧节点
			var tCities = $tree.getNodesByParam('zAsync', false);
			for(var i in tCities) {
				var city = tCities[i];
				_expandNode = city;
				$tree.reAsyncChildNodes(city, 'refresh', false);
			}
		}
	}
	
	
	// 重置搜索
	function showAllNodes() {
		if(_hideNetbars.length > 0) {
			for(var i=0; i<_hideNetbars.length; i++) {
				var hideNetbar = _hideNetbars[i];
				if(hideNetbar && hideNetbar.level == 2) {
					$tree.showNode(hideNetbar);
				}
			}
			_hideNetbars = [];
		}
		var tAreas = $tree.getNodesByParam('level', 1);
		for(var i in tAreas) {
			$tree.expandNode(tAreas[i], false);
		}
	}
	// 搜索
	var _hideNetbars = [];
	function search() {
		var word = $('#input-search').val();
		
		if(word.length > 0) {
			showAllNodes();
			
			var showAreas = [];
			var tNetbars = $tree.getNodesByParam('level', 2);
			for(var i in tNetbars) {
				var netbar = tNetbars[i];
				if(netbar.name.indexOf(word) < 0 && netbar.id) {
					_hideNetbars.push(netbar);
					$tree.hideNode(netbar);
				} else {
					if(netbar.getParentNode) {
						var areaNode = netbar.getParentNode();
						if(!showAreas.contains(areaNode)) {
							showAreas.push(areaNode);
						}
					}
				}
			}
			for(var i=0; i<showAreas.length; i++) {
				var areaNode = showAreas[i];
				$tree.expandNode(areaNode, true);
			}
		} else {
			showAllNodes();
		}
	}
	$('#form-search').on('submit', function() {
		search();
		return false;
	});
	$('#cascader #search').on('click', function() {
		search();
	});
	$('#cancel-search').on('click', function() {
		var $input = $('#input-search').val('');
		search();
	});
	
	// 初始化点击事件
	initAreasNetbarsEvent();
	</script>
	
	<!-- 归属地设置相关 -->
	<script type="text/javascript">
	// 初始化地区树的选中状态
	function initCheckedArea(areaCode) {
		// 取消地区树的选中状态及展开状态
		var checkedNodes = $systemAreasTree.getCheckedNodes();
		for(var i=0; i<checkedNodes.length; i++) {
			var n = checkedNodes[i];
			$systemAreasTree.checkNode(n, false);
			var p = n.getParentNode();
			$systemAreasTree.expandNode(p, false);
		}
		
		// 匹配选中地区并展开
		if(areaCode && areaCode.length == 6) {
			var ns = $systemAreasTree.getNodesByParam('areaCode', areaCode.substring(0,2) + '0000');
			if(ns.length > 0) {
				$systemAreasTree.checkNode(ns[0], true);
			}
		}
		/* for(var j=0; j<ns.length; j++) {
			var n = ns[j];
			$systemAreasTree.checkNode(n, true);
			var p = n.getParentNode();
			$systemAreasTree.expandNode(p, true);
		} */
		
		// 折叠无选中状态的地区
		/* var nocheckedRootNodes = $systemAreasTree.getNodesByFilter(function(node) {
			if(node.level == 0) {
				var children = node.children;
				for(var i=0; i<children.length; i++) {
					var c = children[i];
					var cchildren = c.children;
					for(var j=0; j<cchildren.length; j++) {
						var cc = cchildren[j];
						if(cc.checked) {
							return false;
						}
					}
				}
				return true;
			}
			
			return false;
		});
		for(var i=0; i<nocheckedRootNodes.length; i++) {
			var c = nocheckedRootNodes[i];
			$systemAreasTree.expandNode(c, false);
		} */
	}
	
	// 获取选中地区
	function getCheckedArea() {
		return $systemAreasTree.getCheckedNodes();
	}
	
	// 初始化地区树样式
	$(function() {
		initAreaTree();
		
		$("#tree-areas").css({
			'max-height': 300,
			'overflow': 'auto',
		});
	});
	
	// 初始化树
	var $systemAreasTree = false;
	function initAreaTree() {
		// 初始化地区树
		var setting = {
			callback: {
				onClick: function(event, treeId, treeNode, clickFlag) {
				},
			},
			check: {
				enable: true,
				chkStyle: "radio",
				radioType: 'all',
			}
		}
		
		// 为地区数据做特殊数据设置
		function filterTreeData(nodes) {
			// 修改显示名称为 地名+code
			function changeName(node) {
				node.name = node.name + '(' + node.areaCode + ')';
				if(node.children && node.children.length > 0) {
					filterArray(node.children);
				}
				
				var areaCode = node.areaCode;
				if(areaCode.indexOf('00') >= 0) {
					node.chkDisabled = false;
				}
			}
			
			// 递归地区子集合 
			function filterArray(children) {
				for(var i=0; i<children.length; i++) {
					changeName(children[i]);
				}
			}
			
			// 遍历第一级数据
			for(var i=0; i<nodes.length; i++) {
				var n = nodes[i];
				changeName(n);
			}
		}
		
		// 初始化地区数据
		var originalNodes = '${areasStr!}';
		var zns = [];
		if(originalNodes.length > 0) {
			originalNodes = JSON.parse(originalNodes);
			for(var i=0; i<originalNodes.length; i++) {
				var node = originalNodes[i];
				node.children = [];
				zns.push(node);
			}
			
			filterTreeData(zns);
			
			for(var i=0; i<zns.length; i++) {
				zns[i].open = false;
			}
		}
		
		$systemAreasTree = $.fn.zTree.init($("#tree-areas"), setting, zns);
	}
	$('button[init]').on('click', function(event) {
		var id = $(this).attr('init');
		$.api('${ctx}/activityInfo/init/' + id, {}, function(d) {
				window.location.reload();
			}, function(d) {
				alert('初始化失败：' + d.result);
			});
		});
	</script>
</body>
</html>