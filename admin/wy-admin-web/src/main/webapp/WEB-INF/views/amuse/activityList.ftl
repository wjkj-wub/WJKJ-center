<#import "/macros/pager.ftl" as p >
<html>
<head>
<link rel="stylesheet" href="${ctx}/static/lib/chosen/chosen.css" />
<script type="text/javascript" src="${ctx}/static/lib/chosen/chosen.min.js"></script>
<script type="text/javascript" editor-ctx="${ctx!}" src="${ctx!}/static/plugin/editor.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/ztree/js/jquery.ztree.all-3.5.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/ztree/js/jquery.ztree.exhide-3.5.min.js"></script>
<link rel="stylesheet" type="text/css" href="${ctx!}/static/plugin/ztree/css/zTreeStyle.css"/>
<style type="text/css">
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
			官方娱乐赛事列表
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
			<div class="col-md-2" style="width:160px;">
				<input type="text" class="form-control" name="title" placeholder="赛事名" value="${(params.title)!}" />
			</div>
			<div class="col-md-2" style="width:160px;">
				<select class="form-control" name="way">
					<option value=""<#if params.way??> selected</#if>>方式</option>
					<option value="2"<#if ((params.way?number)!-1) == 2> selected</#if>>线上</option>
					<option value="1"<#if ((params.way?number)!-1) == 1> selected</#if>>线下</option>
				</select>
			</div>
			<div class="col-md-2" style="width:160px;">
				<select class="form-control" name="awardType">
					<option value=""<#if params.awardType??> selected</#if>>奖品类别</option>
					<option value="1" <#if ((params.awardType?number)!-1) == 1> selected</#if>>自有物品</option>
					<option value="2" <#if ((params.awardType?number)!-1) == 2> selected</#if>>虚拟充值</option>
					<option value="3" <#if ((params.awardType?number)!-1) == 3> selected</#if>>库存物品</option>
				</select>
			</div>
			<div class="col-md-2" style="width:160px;">
				<input type="text" class="form-control" name="startDate" placeholder="开始时间" value="${(params.startDate)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:beginPick})" />
			</div>
			<div class="col-md-2" style="width:160px;">
				<input type="text" class="form-control" name="endDate" placeholder="结束时间" value="${(params.endDate)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:endPick})" />
			</div>
			<div class="col-md-2" style="width:160px;">
				<select class="form-control" name="state">
					<option value=""<#if params.state??> selected</#if>>赛事状态</option>
					<option value="2"<#if ((params.state?number)!-1) == 2> selected</#if>>已发布</option>
					<option value="3"<#if ((params.state?number)!-1) == 3> selected</#if>>未发布</option>
					<option value="0"<#if ((params.state?number)!-1) == 0> selected</#if>>待审核</option>
					<option value="1"<#if ((params.state?number)!-1) == 1> selected</#if>>已拒绝</option>
					<option value="4"<#if ((params.state?number)!-1) == 4> selected</#if>>已过期</option>
				</select>
			</div>
			<#if superAdmin??>
			<div class="col-md-2" style="width:160px;">
				<select class="form-control" name="areaCode">
					<option value=""<#if params.areaCode??> selected</#if>>地区</option>
					<#list provinceList as p>
					<option value="${p.areaCode}" <#if ((params.areaCode)!) == p.areaCode> selected</#if> >${p.name}</option>
					</#list>
				</select>
			</div>
			</#if>
			<button type="submit" class="btn btn-success" style="margin-left:10px;">查询</button>
			<a class="btn" href="1">清空</a>
			<button add="" type="button" class="btn btn-info">新增赛事</button>
		</form>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>#</th>
			<th>赛事名</th>
			<th>方式</th>
			<th>网吧名</th>
			<th>地址</th>
			<th>奖品类别</th>
			<th>服务器</th>
			<th>报名人数</th>
			<th>联系方式</th>
			<th>app推荐位置</th>
			<th>开始时间</th>
			<th>结束时间</th>
			<th>赛事状态</th>
			<th>操作</th>
		</tr>
		<#list list as o>
			<tr>
				<td>${o.id!}</td>
				<td>${o.title!}</td>
				<#assign temp=(o.way!0)>
				<#if 1 == temp>
					<td>线下</td>
				<#elseif 2 == temp>
					<td>线上</td>
				<#else>
					<td></td>
				</#if>
				<td>${o.netbarName!}</td>
				<td>${o.address!}</td>
				<td>
					<#if (o.awardType)??>
						<#if o.awardType == 1>
							<#if (o.awardSubType)??>
								自有<#if o.awardSubType == 1>红包<#elseif o.awardSubType == 2>金币</#if>
							</#if>
						<#elseif o.awardType == 2>
							<#if (o.awardSubType)??>
								充值<#if o.awardSubType == 3>话费<#elseif o.awardSubType == 4>流量<#elseif o.awardSubType == 5>Q币</#if>
							</#if>
						<#elseif o.awardType == 3>
							库存${(o.awardTypeName)!}
						</#if>
					</#if>
				</td>
				<td>${o.server!}</td>
				<td><a href="${ctx}/amuse/applyList/1?activityId=${o.id!}">${o.applyNum!'0'}/${o.maxNum!'∞'}</a></td>
				<td>${o.contact!}</td>
				<#assign index=(o.indexShow!)>
				<#assign mid=(o.midShow!)>
				<#assign hot=(o.hotShow!)>
				<td>
				<#list index?split(",") as i>
					<#if '0' == i><span class="label label-primary">首页热门赛事</span><br/><#elseif '1' == i><span class="label label-primary">竞技大厅</span><br/></#if>
				</#list>
				<#list mid?split(",") as m>
					<#if '1' == m><span class="label label-primary">腰图1</span><br/><#elseif '2' == m><span class="label label-primary">腰图2</span><br/></#if>
				</#list>
				<#list hot?split(",") as h>
					<#if '2' == h><span class="label label-primary">热门娱乐赛</span><br/><#elseif '5' == h><span class="label label-primary">竞技大厅卡片区</span><br/><#elseif '7' == h><span class="label label-primary">娱乐赛推荐</span><br/></#if>
				</#list>
				</td>
				<td>${o.startDate!}</td>
				<td>${o.endDate!}</td>
				<#assign state=(o.state!-1)>
				<#assign isEnd=(o.isEnd!-1)>
				<td>
					<#if 2 == state>
						已发布<#if 1 == isEnd>[已过期]</#if>
					<#elseif 3 == state>
						未发布<#if 1 == isEnd>[已过期]</#if>
					<#elseif 1 == isEnd>
						[已过期]
					</#if>
				</td>
				
				<td>
					<button edit="${o.id!}" icon="${o.mainIcon!}" summary="${o.summary!}" verifyContent="${o.verifyContent!}" deliverDay="${o.deliverDay!}" iconId="${o.iconId!}" banner="${o.banner!}" bannerId="${o.bannerId!}" title="${o.title!}" subTitle="${o.sub_title!}" way="${o.way!}" itemId="${o.itemId!}" awardType="${o.awardType!}" awardSubType="${o.awardSubType!}" awardAmount="${o.awardAmount!}" takeType="${o.takeType!}" state="${o.state!}" netbarId="${o.netbarId!}" server="${o.server!}" maxNum="${o.maxNum!}" virtualApply="${o.virtualApply!}" contact="${o.contact!}" contactType="${o.contactType!}" applyStart="${o.applyStart!}" startDate="${o.startDate!}" endDate="${o.endDate!}" verifyEndDate="${o.verifyEndDate!}" telReq="${o.telReq!}" qqReq="${o.qqReq!}" nameReq="${o.nameReq!}" accountReq="${o.accountReq!}" serverReq="${o.serverReq!}" idCardReq="${o.idCardReq!}" rule="${(o.rule?html)!}" reward="${(o.reward?html)!}" grantMsg="${(o.grantMsg)!}" type="button" class="btn btn-info">编辑</button>
					<button remove="${o.id!}" valid="0" title="${o.title!}" index="${o.indexShow!}" mid="${o.midShow!}" hot="${o.hotShow!}" type="button" class="btn btn-danger">删除</button>
					<#if state == 2>
						<button release="${o.id!}" state="3" title="${o.title!}" type="button" class="btn btn-danger">不发布</button>
					<#else>
						<button release="${o.id!}" state="2" title="${o.title!}" type="button" class="btn btn-danger">发布</button>
					</#if>
				</td>
			</tr>
		</#list>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	
	<#-- 编辑，新增 -->
	<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
	<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
	<div id="modal-editor" class="modal fade">
		<div class="modal-dialog" style="width:1300px">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideEditor()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 id="bianji_h4id" class="modal-title" style="display:none;">编辑赛事</h4>
					<h4 id="xinzeng_h4id" class="modal-title" style="display:none;">新增赛事（红色*为必填）</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post">
						<input type="hidden" name="id" />
						<input type="hidden" name="type"/>
						<input type="hidden" name="iconId" />
						<input type="hidden" name="bannerId" />
						<input type="hidden" name="superAdmin" value="${superAdmin!}"/>
						<div class="form-group">
							<label class="col-md-2 control-label">标题<font id="font_id_1" color="red">*</font></label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="title" maxlength="13" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">副标题<font id="font_id_2" color="red">*</font></label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="subTitle" maxlength="100">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">服务器描述<font id="font_id_3" color="red">*</font></label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="server" maxlength="20">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">方式<font id="font_id_4" color="red">*</font></label>
							<div class="col-md-10">
								<select id="select_id_way" name="way" class="form-control" <#if !superAdmin??>disabled="disabled"</#if> >
									<option value="1" <#if !superAdmin??>selected</#if> >线下</option>
									<option value="2">线上</option>
								</select>
							</div>
						</div>
						<div class="form-group" id="div_id_netbarId" style="display:none;">
							<label class="col-md-2 control-label">网吧<font id="font_id_3" color="red">*</font></label>
							<div class="col-md-10" id="netbardiv_id">
								<select id="province_select_id" name="province" class="form-control" style="width:12%;display:inline;">
									<option value="">--地区--</option>
									<#list provinceList as p>
										<option value="${p.areaCode}">${p.name}</option>
									</#list>
								</select>
								<select id="netbar_select_id" name="netbarId" class="form-control" style="width:80%;display:inline;">
									<option value="">--请选择--</option>
								</select>
								<script type="text/javascript">
									var initNetbar = function() {
										var $select = $('#netbar_select_id');
										$select.chosen();
										var $container = $('#netbar_select_id').siblings('.chosen-container');
										
										var selectedNetbarName = $select.find('option[value="' + $select.val() + '"]').html();
										if(typeof(selectedNetbarName) === 'undefined') {
											selectedNetbarName = '';
										}
										$container.css('width', '80%').find('.chosen-single span').attr('title', selectedNetbarName).html(selectedNetbarName);
									}
								</script>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">在app中发布<font id="font_id_6" color="red">*</font></label>
							<div class="col-md-10">
								<select id="select_id_state" name="state" class="form-control">
									<option value="2">发布</option>
									<option value="3">不发布</option>
								</select>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">游戏项目<font id="font_id_7" color="red">*</font></label>
							<div class="col-md-10">
								<select id="select_id_itemId" class="form-control" name="itemId">
									<#list itemList as i>
									<option value="${i.item_id!}">${i.item_name!}</option>
									</#list>
								</select>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">略缩图<font id="font_id_8" color="red">*</font></label>
							<div class="col-md-10">
								<input type="file" name="icon_File" value="列表图" accept="image/*" />
								<font class="prompt">
									请上传71:42宽高比的图片，以达到最佳显示效果
								</font>
							</div>
						</div>
						<!-- <div class="form-group">
							<label class="col-md-2 control-label">首图<font id="font_id_9" color="red">*</font></label>
							<div class="col-md-10">
								<input type="file" name="banner_File" value="Banner" accept="image/*" />
								<font class="prompt">
									请上传64:31宽高比的图片，以达到最佳显示效果
								</font>
							</div>
						</div> -->
						<div class="form-group">
							<label class="col-md-2 control-label">奖品类别<font id="font_id_10" color="red">*</font></label>
							<div class="col-md-10">
								<select id="select_id_awardType" class="form-control" name="awardType">
									<option value="1">自有物品</option>
									<option value="2">虚拟充值</option>
									<option value="3">库存物品</option>
								</select>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">奖品类别<font id="font_id_10" color="red">*</font></label>
							<div class="col-md-10">
								<select id="select_id_awardType" class="form-control" name="awardSubType">
									<option value="1">红包</option>
									<option value="2">金币</option>
								</select>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">奖品数量<font id="font_id_10" color="red">*</font></label>
							<div class="col-md-10">
								<input class="form-control" type="number" name="awardAmount" min="0" max="9999999" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">奖励描述<font id="font_id_11" color="red">*</font></label>
							<div class="col-md-10">
								<textarea id="textarea_id_reward" class="form-control" name="reward" cols="145" rows="8"></textarea>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">奖品发放周期<font id="font_id_11" color="red">*</font></label>
							<div class="col-md-10">
								<div class="input-group">
									<input class="form-control" type="number" name="deliverDay" value="0" />
									<span class="input-group-btn">
										<span class="btn btn-default" >工作日</span>
									</span>
								</div>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">简要规则<font id="font_id_11" color="red">*</font></label>
							<div class="col-md-10">
								<textarea name="summary" class="form-control" cols="100"></textarea>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">系统消息内容<font id="font_id_11" color="red">*</font></label>
							<div class="col-md-10">
								<textarea name="grantMsg" class="form-control" cols="100" maxlength="140"></textarea>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">提交凭证规范<font id="font_id_11" color="red">*</font></label>
							<div class="col-md-10">
								<textarea name="verifyContent" class="form-control" cols="100"></textarea>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">规则<font id="font_id_12" color="red">*</font></label>
							<div class="col-md-10">
								<textarea id="textarea_id_rule" name="rule" placeholder="" autofocus></textarea>
								<script type="text/javascript">
									_editor = editor($('#textarea_id_rule'));
								</script>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">联系方式<font id="font_id_13" color="red">*</font></label>
							<div class="col-md-10">
								<select id="select_id_contactType" name="contactType" class="form-control">
									<option value="1">手机号</option>
									<option value="2">QQ号</option>
									<option value="3">YY号</option>
									<option value="4">微信号</option>
								</select>
							</div>
							<div class="col-md-10">
								<input class="form-control" type="text" name="contact" placeholder="请输入" style="height:20px;" maxlength="15">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">是否必填<font id="font_id_14" color="red">*</font></label>
							<div class="col-md-10">
								<label><input type="checkbox" name="telReq" value="1">用户联系电话</label>
								<label><input type="checkbox" name="nameReq" value="1">用户姓名</label>
								<label><input type="checkbox" name="idCardReq" value="1">用户身份证</label>
								<label><input type="checkbox" name="accountReq" value="1">游戏帐号</label>
								<label><input type="checkbox" name="serverReq" value="1">游戏大区</label>
								<label><input type="checkbox" name="qqReq" value="1">用户QQ号</label>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">报名类型<font id="font_id_15" color="red">*</font></label>
							<div class="col-md-10">
								<select id="select_id_takeType" name="takeType" class="form-control">
									<option value="1">个人报名</option>
									<option value="2">团队报名</option>
								</select>
							</div>
						</div>
						<div id="div_id_tel" class="form-group">
							<label class="col-md-2 control-label">报名人数上限</label>
							<div class="col-md-10">
								<input id="input_id_tel" class="form-control" style="width:920px;display:inline;" type="text" name="maxNum" onblur="regExpPosInt('input_id_tel')" maxlength="10">
								<span id="input_id_tel_ts">请输入正整数</span>
							</div>
						</div>
						<div id="div_id_tel" class="form-group">
							<label class="col-md-2 control-label">虚拟报名人数</label>
							<div class="col-md-10">
								<input id="input_id_vir" class="form-control" style="width:920px;display:inline;" type="text" name="virtualApply" onblur="regExpPosInt('input_id_vir')" maxlength="10">
								<span id="input_id_vir_ts">请输入正整数</span>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">报名开始时间<font id="font_id_16" color="red">*</font></label>
							<div class="col-md-10">
								<input class="form-control" type="text" id="applyStartDateString" name="applyStartDateString" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss',maxDate:'#F{$dp.$D(\'startDateString\',{d:0});}'})">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">活动开始时间<font id="font_id_17" color="red">*</font></label>
							<div class="col-md-10">
								<input class="form-control" type="text" id="startDateString" name="startDateString" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss',minDate:'#F{$dp.$D(\'applyStartDateString\',{d:0});}',maxDate:'#F{$dp.$D(\'endDateString\',{d:0});}'})">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">活动结束时间<font id="font_id_18" color="red">*</font></label>
							<div class="col-md-10">
								<input class="form-control" type="text" id="endDateString" name="endDateString" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss',minDate:'#F{$dp.$D(\'startDateString\',{d:0});}'})">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">认证提交截止时间<font id="font_id_19" color="red">*</font></label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="verifyEndDateString" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss',minDate:'#F{$dp.$D(\'endDateString\',{d:0});}'})">
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" onclick="hideEditor()">取消</button>
					<button id="submit" type="button" class="btn btn-primary">发布</button>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
	// 初始化模态框
	var _$editor = $('#modal-editor');
	_$editor.modal({
		keyboard : false,
		show : false
	})
	
	// 关闭模态框
	function hideEditor() {
		_$editor.modal('hide');
	}
	
	_$editor.find('[name="awardType"]').on('change', function(event) {
		var t = $(this).val();
		
		var items = [];
		if(t == 1) {
			items = [
				{'value': '1', 'text': '红包'},
				{'value': '2', 'text': '金币'}
			];
		} else if(t == 2) {
			items = [
				{'value': '3', 'text': '话费'},
				{'value': '4', 'text': '流量'},
				{'value': '5', 'text': 'Q币'}
			];
		} else {
			items = [
				{'value': '6', 'text': 'CDKEY'},
				{'value': '7', 'text': '实物'}
			];
		}
		
		var $awardSubType = _$editor.find('[name="awardSubType"]');
		$awardSubType.html('');
		for(var i=0; i<items.length; i++) {
			var item = items[i];
			$awardSubType.append('<option value="' + item['value'] + '">' + item['text'] + '</option>');
		}
		$awardSubType.change();
	});
	
	_$editor.find('[name="awardSubType"]').on('change', function(event) {
		function option(value, text) {
			return '<option value="' + value + '">' + text + '</option>';
		}
		var t = _$editor.find('[name="awardType"]').val();
		var $awardAmountDiv = _$editor.find('[name="awardAmount"]').parent();
		if(t == 2) {// 第三方充值
			var st = $(this).val();
			if(st == 3) {// 话费
				$awardAmountDiv.html('<select class="form-control" name="awardAmount">'
						+ option('1', '1元')
						+ option('2', '2元')
						+ option('5', '5元')
						+ option('20', '20元')
						+ option('30', '30元')
						+ option('50', '50元')
						+ option('100', '100元')
						+ option('300', '300元')
						+ '</select>');
			} else if(st == 4) {// 流量
				$awardAmountDiv.html('<select class="form-control" name="awardAmount">'
						+ option('500', '500M')
						+ option('1024', '1024M')
						+ '</select>');
			} else {
				$awardAmountDiv.html('<input type="number" class="form-control" name="awardAmount" min="0" max="100000" />');
			}
		} else if(t == 1) {
			$awardAmountDiv.html('<input type="number" class="form-control" name="awardAmount" min="0" max="100000" />');
		} else {
			$awardAmountDiv.html('<input type="number" class="form-control" name="awardAmount" value="1" readonly />');
		}
	});
	
	// 显示模态框，编辑
	$('button[edit]').on('click', function(event) {
		var id = $(this).attr('edit');
		var icon = $(this).attr('icon');
		var iconId = $(this).attr('iconId');
		var banner = $(this).attr('banner');
		var bannerId = $(this).attr('bannerId');
		var title = $(this).attr('title');
		var type = $(this).attr('type');
		var way = $(this).attr('way');
		
		var subTitle = $(this).attr('subTitle');
		var state = $(this).attr('state');
		var itemId = $(this).attr('itemId');
		var awardType = $(this).attr('awardType');
		var awardSubType = $(this).attr('awardSubType');
		var awardAmount = $(this).attr('awardAmount');
		var takeType = $(this).attr('takeType');
		var telReq = $(this).attr('telReq');
		var nameReq = $(this).attr('nameReq');
		var idCardReq = $(this).attr('idCardReq');
		var accountReq = $(this).attr('accountReq');
		var serverReq = $(this).attr('serverReq');
		var qqReq = $(this).attr('qqReq');
		
		var netbarId = $(this).attr('netbarId');
		var server = $(this).attr('server');
		var maxNum = $(this).attr('maxNum');
		var virtualApply = $(this).attr('virtualApply');
		var contact = $(this).attr('contact');
		var contactType = $(this).attr('contactType');
		var isRecommend = $(this).attr('isRecommend');
		var applyStart = $(this).attr('applyStart');
		var startDate = $(this).attr('startDate');
		var endDate = $(this).attr('endDate');
		var verifyEndDate = $(this).attr('verifyEndDate');
		var rule = $(this).attr('rule');
		var reward = $(this).attr('reward');
		var summary = $(this).attr('summary');
		var verifyContent = $(this).attr('verifyContent');
		var deliverDay = $(this).attr('deliverDay');
		var grantMsg = $(this).attr('grantMsg');

		_$editor.find('[name="awardType"]').val(awardType).change();
		_$editor.find('[name="awardSubType"]').val(awardSubType).change();
		
		$.fillForm({
			title: title,
			subTitle: subTitle,
			summary: summary,
			server: server,
			maxNum: maxNum,
			virtualApply: virtualApply,
			contact: contact,
			applyStartDateString: applyStart,
			startDateString: startDate,
			endDateString: endDate,
			verifyEndDateString: verifyEndDate,
			icon_File: icon,
			banner_File: banner,
			iconId: iconId,
			bannerId: bannerId,
			reward: reward,
			awardType: awardType,
			awardSubType: awardSubType,
			awardAmount: awardAmount,
			verifyContent: verifyContent,
			deliverDay: deliverDay,
			grantMsg: grantMsg,
			id: id
		}, _$editor);
		if(way == 1){	//线下
			$('#netbar_select_id').val(netbarId);
			//initNetbar();
			$('#div_id_netbarId').show();
		}else{
			$('#div_id_netbarId').hide();
		}
		// select
		$('#select_id_way').val(way);
		$('#select_id_state').val(state);
		$('#select_id_itemId').val(itemId);
		$('#select_id_takeType').val(takeType);
		$('#select_id_contactType').val(contactType);
		$("input[type='radio'][name=isRecommend][value='"+isRecommend+"']").attr("checked",true);
		// checkbox
		$("input[type='checkbox'][name=telReq][value='"+telReq+"']").attr("checked",true);
		$("input[type='checkbox'][name=nameReq][value='"+nameReq+"']").attr("checked",true);
		$("input[type='checkbox'][name=idCardReq][value='"+idCardReq+"']").attr("checked",true);
		$("input[type='checkbox'][name=accountReq][value='"+accountReq+"']").attr("checked",true);
		$("input[type='checkbox'][name=serverReq][value='"+serverReq+"']").attr("checked",true);
		$("input[type='checkbox'][name=qqReq][value='"+qqReq+"']").attr("checked",true);
		
		$('#xinzeng_h4id,#font_id_1,#font_id_2,#font_id_3,#font_id_4,#font_id_5,#font_id_6,#font_id_7,#font_id_8,#font_id_9,#font_id_10,#font_id_11,#font_id_12,#font_id_13,#font_id_14,#font_id_15,#font_id_16,#font_id_17,#font_id_18,#font_id_19').hide();
		$('#bianji_h4id').show();
		$('#select_id_way').change();
		
		if(typeof(rule) === 'undefined') {
			rule = '';
		}
		setEditorText($('#textarea_id_rule'), rule);
		//清空网吧下拉框
		var $netbarSelect = $("#netbar_select_id");
		var $netbarSelectParent = $netbarSelect.parent();
		$netbarSelect.remove();
		$('#netbar_select_id_chosen').remove();
		$netbarSelectParent.append('<select id="netbar_select_id" name="netbarId" class="form-control" style="width:80%;display:inline;"></select>');
		$("#netbar_select_id").html("");
		$("#netbar_select_id").append('<option value="">--请选择--</option>');
		
		_$editor.modal('show');
	});
	
	// 显示模态框，新增
	$('button[add]').on('click', function(event) {
		$('#select_id_way').val('');
		$("input[type='radio'][name=isRecommend][value='']").attr("checked",true);
		$.fillForm({
			id: '',
			title: '',
			server: '',
			reward: '',
			rule: '',
			maxNum: '',
			contact: '',
			startDateString: '',
			endDateString: '',
			icon_File: '',
			banner_File: ''
		}, _$editor);
		$('#bianji_h4id').hide();
		$('#xinzeng_h4id,#font_id_1,#font_id_2,#font_id_3,#font_id_4,#font_id_5,#font_id_6,#font_id_7,#font_id_8,#font_id_9,#font_id_10,#font_id_11,#font_id_12,#font_id_13,#font_id_14,#font_id_15,#font_id_16,#font_id_17,#font_id_18,#font_id_19').show();
		$('#select_id_way').change();
		setEditorText($('#textarea_id_rule'), '');
		//清空网吧下拉框
		var $netbarSelect = $("#netbar_select_id");
		var $netbarSelectParent = $netbarSelect.parent();
		$netbarSelect.remove();
		$('#netbar_select_id_chosen').remove();
		$netbarSelectParent.append('<select id="netbar_select_id" name="netbarId" class="form-control" style="width:80%;display:inline;"></select>');
		$("#netbar_select_id").html("");
		$("#netbar_select_id").append('<option value="">--请选择--</option>');
		
		_$editor.find('[name="awardType"]').change();
		_$editor.modal('show');
	});
	
	// 判断显示（网吧列表）
	$('#select_id_way').on('change',function(){
		if($(this).val() == 1){	//线下
			//initNetbar();
			$('#div_id_netbarId').show();
		}else {
			$('#div_id_netbarId').hide();
		}
	});
	
	// 根据地区查询网吧列表
	$('#province_select_id').on('change',function(){
		var areaCode = $(this).val();
		var $netbarSelect = $("#netbar_select_id");
		var $netbarSelectParent = $netbarSelect.parent();
		$netbarSelect.remove();
		$('#netbar_select_id_chosen').remove();
		$netbarSelectParent.append('<select id="netbar_select_id" name="netbarId"></select>');
		$("#netbar_select_id").html("");
		$("#netbar_select_id").append('<option value="">--请选择--</option>');
		$.api('${ctx}/amuse/getNetbars/' + areaCode , {}, function(d) {
			var list = d.object;
		    for(var i=0;i<list.length;i++){
		    	var id = list[i].id;
		    	var name = list[i].netbarName;
		    	$("#netbar_select_id").append('<option value="'+id+'">'+name+'</option>');
		    }
		    initNetbar();
		}, function(d) {
			alert('请求数据异常：' + d.result);
		}, {
			async: false,
		});
	});
	
	// 提交表单
	_$editor.find('#submit').on('click', function(event) {
		var $form = _$editor.find('form');
		
		var types = "image/png,image/jpeg,image/gif";
		var iconFiles = $form.find('[name="icon_File"]').get(0).files;
		if(iconFiles.length > 0) {
			for(var i=0; i<iconFiles.length; i++) {
				var file = iconFiles[i];
				if(types.indexOf(file.type) == -1) {
					alert('缩略图图片类型错误');
					return;
				}
			}
		}
	 	/* var bannerFiles = $form.find('[name="banner_File"]').get(0).files;
		if(bannerFiles.length > 0) {
			for(var i=0; i<bannerFiles.length; i++) {
				var file = bannerFiles[i];
				if(types.indexOf(file.type) == -1) {
					alert('首图图片类型错误');
					return;
				}
			}
		} */
		
		var $maxNum = $form.find('[name="maxNum"]');
		var maxNum = $maxNum.val();
		if(maxNum.length <= 0) {
			alert('请输入报名上限人数');
			return;
		}
		
		var $virtualApply = $form.find('[name="virtualApply"]');
		var virtualApply = $virtualApply.val();
		if(virtualApply.length <= 0) {
			$virtualApply.val(0);
		}
		
		$form.ajaxSubmit({
			url:'${ctx}/amuse/save',
		    type:'post',
		    success: function(d){
		    	if(d.code == 0) {
		    		window.location.reload();
		    	} else {
		    		alert(d.result);
		    	}
			}
		});
	})
	
	
	// 删除
	$('button[remove]').on('click', function(event) {
		var _this = $(this);
		_this.attr('disabled', true);
		var id = _this.attr('remove');
		var valid = _this.attr('valid');
		var title = _this.attr('title');
		var index = _this.attr('index');
		var mid = _this.attr('mid');
		var hot = _this.attr('hot');
		if(index!='' || mid!='' || hot!=''){
			alert("请先删除该赛事的app推荐位置！");
			window.location.reload();
		}
		var info1,info2;
		var reValid;
		if(valid==0){
			reValid=1;
			info1='确认删除【'+ title +'】吗?';
			info2='删除失败：';
		}else{
			reValid=0
			info1='确认恢复【'+ title +'】吗?';
			info2='恢复失败：';
		}
		
		$.confirm(info1, function() {
			$.api('${ctx}/amuse/validChange/' + id + '/' + valid, {}, function(d) {
				window.location.href='${ctx}/amuse/list/1?valid=' + reValid;
			}, function(d) {
				alert(info2 + d.result);
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
	
	// 发布/不发布
	$('button[release]').on('click', function(event) {
		var _this = $(this);
		_this.attr('disabled', true);
		var id = _this.attr('release');
		var state = _this.attr('state');
		var title = _this.attr('title');
		var info3,info4;
		if(state==3){
			info3='确认取消发布【'+ title +'】吗?';
			info4='取消失败：';
		}else{
			info3='确认发布【'+ title +'】吗?';
			info4='发布失败：';
		}
		
		$.confirm(info3, function() {
			$.api('${ctx}/amuse/stateChange/' + id + '/' + state, {}, function(d) {
				window.location.reload();
			}, function(d) {
				alert(info4 + d.result);
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
	
	//带搜索的下拉框
	$('#productName').bind('input propertychange', function() {
    	var title = $(this).val();
		$.api('${ctx}/indexAdvertise/test/' + title , {}, function(d) {
			var data = d.object;
			for(var i in data){
				var t = data[i];
				$('#titleSelect_id').append("<option value='"+t.id+"'>"+t.name+"</option>");
			}
		}, function(d) {
			alert('请求数据异常：' + d.result);
		}, {
			async: false,
		});
		_$editor.modal('show');
	});
	
	// 正则表达式，验证正整数
	function regExpPosInt(d) {
		var id = "#"+d;
		var id_ts = id+"_ts";
		var obj = $.trim($(id).val());
		if(obj != ""){
			var reg = /^[1-9]\d*$/;
			if(!reg.test(obj)){      
				alert("请输入正整数");
				$(id_ts).css('color','red');
				$(id).focus();
			}else{
				$(id_ts).css('color','black');
			}
		}
	}
	</script>
</body>
</html>