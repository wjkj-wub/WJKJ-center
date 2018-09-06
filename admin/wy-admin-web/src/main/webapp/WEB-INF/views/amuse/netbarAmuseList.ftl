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
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>管理系统</strong>
 		</li>
		<li class="active">
			网吧娱乐赛事列表
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
			<div class="col-md-2" style="width:160px;">
				<input type="text" class="form-control" name="title" placeholder="赛事名" value="${(params.title)!}" />
			</div>
			<div class="col-md-2" style="width:160px;">
				<input type="text" class="form-control" name="netbarName" placeholder="网吧" value="${(params.netbarName)!}" />
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
		</form>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>#</th>
			<th>赛事名</th>
			<th>网吧名</th>
			<th>网吧地址</th>
			<th>奖品类别</th>
			<th>网吧联系号码</th>
			<th>报名人数</th>
			<th>服务器</th>
			<th>app推荐位置</th>
			<th>开始时间</th>
			<th>结束时间</th>
			<th>状态</th>
			<th>操作</th>
		</tr>
		<#if list??>
		<#list list as o>
			<tr>
				<td>${o.id!}</td>
				<td>${o.title!}</td>
				<#assign temp=(o.way!0)>
				<td>${o.netbarName!}</td>
				<td>${o.address!}</td>
				<td>${o.awardTypeName!}</td>
				<td>${o.telephone!}</td>
				<td><a href="${ctx}/amuse/applyList/1?activityId=${o.id!}&flag=1"><u>${o.applyNum!'0'}/${o.maxNum!'∞'}</u></a></td>
				<td>${o.server!}</td>
				<#assign index=(o.indexShow!)>
				<#assign mid=(o.midShow!)>
				<#assign hot=(o.hotShow!)>
				<td>
				<#list index?split(",") as i>
					<#if '0' == i><span class="label">首页热门赛事</span><#elseif '1' == i><span class="label">竞技大厅</span></#if>
				</#list>
				<#list mid?split(",") as m>
					<#if '1' == m><span class="label">腰图1</span><#elseif '2' == m><span class="label">腰图2</span></#if>
				</#list>
				<#list hot?split(",") as h>
					<#if '2' == h><span class="label">热门娱乐赛</span><#elseif '5' == h><span class="label">竞技大厅卡片区</span><#elseif '7' == h><span class="label">娱乐赛推荐</span></#if>
				</#list>
				</td>
				<td>${o.startDate!}</td>
				<td>${o.endDate!}</td>
				<#assign state=(o.state!-1)>
				<#assign isEnd=(o.isEnd!-1)>
				<td>
					<#if 0 == state>
						待审核<#if 1 == isEnd>[已过期]</#if>
					<#elseif 1 == state>
						已拒绝<#if 1 == isEnd>[已过期]</#if>
					<#elseif 2 == state>
						已发布<#if 1 == isEnd>[已过期]</#if>
					<#elseif 3 == state>
						未发布<#if 1 == isEnd>[已过期]</#if>
					<#elseif 1 == isEnd>
						[已过期]
					</#if>
				</td>
				
				<td>
				<#if state == 0>
					<button edit="${o.id!}" icon="${o.mainIcon!}" iconId="${o.iconId!}" banner="${o.banner!}" bannerId="${o.bannerId!}" title="${o.title!}" subTitle="${o.sub_title!}" itemId="${o.itemId!}" awardType="${o.awardType!}" awardSubType="${o.awardSubType!}" awardAmount="${o.awardAmount!}" takeType="${o.takeType!}" state="${o.state!}" netbarName="${o.netbarName!}" server="${o.server!}" maxNum="${o.maxNum!}" virtualApply="${o.virtualApply!}" contact="${o.contact!}" contactType="${o.contactType!}" applyStart="${o.applyStart!}" startDate="${o.startDate!}" endDate="${o.endDate!}" verifyEndDate="${o.verifyEndDate!}" telReq="${o.telReq!}" qqReq="${o.qqReq!}" nameReq="${o.nameReq!}" accountReq="${o.accountReq!}" serverReq="${o.serverReq!}" idCardReq="${o.idCardReq!}" verifyContent="${o.verifyContent!}" grantMsg="${(o.grantMsg)!}" type="button" class="btn btn-info">审核</button>
				<#else>
					<button edit="${o.id!}" icon="${o.mainIcon!}" iconId="${o.iconId!}" banner="${o.banner!}" bannerId="${o.bannerId!}" title="${o.title!}" subTitle="${o.sub_title!}" itemId="${o.itemId!}" awardType="${o.awardType!}" awardSubType="${o.awardSubType!}" awardAmount="${o.awardAmount!}" takeType="${o.takeType!}" state="${o.state!}" netbarName="${o.netbarName!}" server="${o.server!}" maxNum="${o.maxNum!}" virtualApply="${o.virtualApply!}" contact="${o.contact!}" contactType="${o.contactType!}" applyStart="${o.applyStart!}" startDate="${o.startDate!}" endDate="${o.endDate!}" verifyEndDate="${o.verifyEndDate!}" telReq="${o.telReq!}" qqReq="${o.qqReq!}" nameReq="${o.nameReq!}" accountReq="${o.accountReq!}" serverReq="${o.serverReq!}" idCardReq="${o.idCardReq!}" verifyContent="${o.verifyContent!}" grantMsg="${(o.grantMsg)!}" type="button" class="btn btn-info">编辑</button>
					<#if state == 2>
						<button release="${o.id!}" state="3" title="${o.title!}" type="button" class="btn btn-danger">不发布</button>
					<#elseif state == 3>
						<button release="${o.id!}" state="2" title="${o.title!}" type="button" class="btn btn-danger">发布</button>
					</#if>
				</#if>
				</td>
			</tr>
		</#list>
		</#if>
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
					<h4 id="bianji_h4id" class="modal-title" style="">编辑赛事</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post">
						<input type="hidden" name="id" />
						<input type="hidden" name="type" value=""/>
						<input type="hidden" name="iconId" />
						<input type="hidden" name="bannerId" />
						<div class="form-group">
							<label class="col-md-2 control-label">标题</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="title" value="" maxlength="50">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">副标题</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="subTitle" value="" maxlength="80">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">游戏项目</label>
							<div class="col-md-10">
								<select id="select_id_itemId" class="form-control" name="itemId">
									<#list itemList as i>
									<option value="${i.item_id!}">${i.item_name!}</option>
									</#list>
								</select>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">赛事地点</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="netbarName" value="" disabled="disabled;">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">服务器描述</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="server" value="" maxlength="20">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">略缩图<font id="font_id_1" color="red">*</font></label>
							<div class="col-md-10">
								<input type="file" name="icon_File" value="列表图" accept="image/*">
								<font class="prompt">
									请上传248:168宽高比的图片，以达到最佳显示效果
								</font>
							</div>
						</div>
						<!-- <div class="form-group">
							<label class="col-md-2 control-label">首图<font id="font_id_2" color="red">*</font></label>
							<div class="col-md-10">
								<input type="file" name="banner_File" value="Banner">请上传640*310
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
								<input class="form-control" type="number" name="awardAmount" value="" min="0" max="9999999" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">奖励描述</label>
							<div class="col-md-10">
								<textarea id="textarea_id_reward" name="reward" cols="145" rows="8" value=""></textarea>
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
							<label class="col-md-2 control-label">规则</label>
							<div class="col-md-10">
								<textarea id="textarea_id_rule" name="rule" placeholder="" autofocus></textarea>
								<script type="text/javascript">
									_editor = editor($('#textarea_id_rule'));
								</script>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">联系方式</label>
							<div class="col-md-10">
								<select id="select_id_contactType" name="contactType" class="form-control">
									<option value="1">手机号</option>
									<option value="2">QQ号</option>
									<option value="3">YY号</option>
									<option value="4">微信号</option>
								</select>
							</div>
							<div class="col-md-10">
								<input class="form-control" type="text" name="contact" value="" placeholder="请输入" style="height:20px;" maxlength="15">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">是否必填</label>
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
							<label class="col-md-2 control-label">报名类型</label>
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
								<input id="input_id_tel" class="form-control" style="width:920px;display:inline;" type="text" name="maxNum" value="" onblur="regExpPosInt('input_id_tel')"  maxlength="10">
								<span id="input_id_tel_ts">请输入正整数</span>
							</div>
						</div>
						<div id="div_id_tel" class="form-group">
							<label class="col-md-2 control-label">虚拟报名人数</label>
							<div class="col-md-10">
								<input id="input_id_vir" class="form-control" style="width:920px;display:inline;" type="text" name="virtualApply" value="" onblur="regExpPosInt('input_id_vir')" maxlength="10">
								<span id="input_id_vir_ts">请输入正整数</span>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">活动开始时间</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="startDateString" value="" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss'})">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">活动结束时间</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="endDateString" value="" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss'})">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">报名开始时间</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="applyStartDateString" value="" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss'})">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">认证提交截止时间</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="verifyEndDateString" value="" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss'})">
							</div>
						</div>
						<hr style="height:1px;border:none;border-top:1px solid #555555;" />
						<div class="form-group">  
							<label class="col-md-2 control-label">审核处理<font id="font_id_3" color="red">*</font></label>
							<div class="col-md-10">
								<input type="radio" name="state" value="2">通过并发布
								<input type="radio" name="state" value="1">拒绝
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">备注</label>
							<div class="col-md-10">
								<textarea id="textarea_id_remark" name="remark" cols="80" rows="3" value="" placeholder="请输入拒绝理由，告知网吧商户"></textarea>
							</div>
						</div>
						
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" onclick="hideEditor()">取消</button>
					<button type="button" class="btn btn-primary" onclick="submitEditor()">确定</button>
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
	
	// 奖品类型管理
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
				<#if awardTypeList??>
				<#list awardTypeList as a>
				{'value': '${a.id!}', 'text': '${a.name!}'},
				</#list>
				</#if>
			];
		}
		
		var $awardSubType = _$editor.find('[name="awardSubType"]');
		$awardSubType.html('');
		for(var i=0; i<items.length; i++) {
			var item = items[i];
			$awardSubType.append('<option value="' + item['value'] + '">' + item['text'] + '</option>');
		}
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
						+ option('10', '10元')
						+ option('20', '20元')
						+ option('30', '30元')
						+ option('50', '50元')
						+ option('100', '100元')
						+ option('300', '300元')
						+ '</select>');
			} else if(st == 4) {// 流量
				$awardAmountDiv.html('<select class="form-control" name="awardAmount">'
						+ option('10', '10M')
						+ option('30', '30M')
						+ option('70', '70M')
						+ option('500', '500M')
						+ option('1024', '1024M')
						+ option('2048', '2048M')
						+ '</select>');
			} else {
				$awardAmountDiv.html('<input type="number" class="form-control" name="awardAmount" min="0" max="100000" />');
			}
		} else {
			$awardAmountDiv.html('<input type="number" class="form-control" name="awardAmount" min="0" max="100000" />');
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
		
		var netbarName = $(this).attr('netbarName');
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

		var verifyContent = $(this).attr('verifyContent');
		var grantMsg = $(this).attr('grantMsg');
		
		_$editor.find('[name="awardType"]').val(awardType).change();
		_$editor.find('[name="awardSubType"]').val(awardSubType).change();
		
		$.fillForm({
			title: title,
			subTitle: subTitle,
			netbarName: netbarName,
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
			awardType: awardType,
			awardSubType: awardSubType,
			awardAmount: awardAmount,
			verifyContent: verifyContent,
			grantMsg: grantMsg,
			id: id
		}, _$editor);
		// select
		$('#select_id_itemId').val(itemId);
		$('#select_id_awardType').val(awardType);
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
	
		$.ajax({
			url:'${ctx}/netbarAmuse/info/'+id,
			data : '',
			dataType : 'json',
		    type:'post',
		    success: function(d){
		    	if(d.code == 0) {
		    		$("#textarea_id_reward").val(d.object.reward);	//给textarea赋值
		    		
		    		setEditorText($('#textarea_id_rule'), d.object.rule);
		    	} else {
		    		alert(d.result);
		    	}
			}
		});
		
		if(iconId==''){
			$('#font_id_1').show();
		}else{
			$('#font_id_1').hide();
		}
		if(bannerId==''){
			$('#font_id_2').show();
		}else{
			$('#font_id_2').hide();
		}
		_$editor.modal('show');
	});
	
	// 提交表单
	function submitEditor() {
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
	
		$form.ajaxSubmit({
			url:'${ctx}/netbarAmuse/save',
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
			$.api('${ctx}/netbarAmuse/stateChange/' + id + '/' + state, {}, function(d) {
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