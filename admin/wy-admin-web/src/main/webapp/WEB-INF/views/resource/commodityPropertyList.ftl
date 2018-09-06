<#import "/macros/pager.ftl" as p >
<html>
<head>
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/simditor/css/simditor.css">
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/simditor/css/font-awesome.css">
<script type="text/javascript" editor-ctx="${ctx!}" src="${ctx!}/static/plugin/editor.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/module.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/hotkeys.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/uploader.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/simditor.min.js"></script>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>商品维护</strong>
 		</li>
		<li class="active">
			商品项目列表
		</li><a class="" href="${ctx}/netbar/resource/commodity/list/1" style="margin-left:30px;">返回商品列表</a>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post" >
			<input type="hidden" name="cid" value="${params.cid!}" />
			<input id="input_id_qualifications" type="hidden" name="qualifications" value="${(params.qualifications)!}" />
			<div class="mb10">
				<div class="col-md-2" style="width:120px;">
					<input type="text" class="form-control" name="pname" placeholder="项目名称" value="${(params.pname)!}" />
				</div>
				<div class="col-md-2" style="width:120px;">
					<input type="text" name="beginDate" value="${(params.beginDate)!}" class="form-control" placeholder="录入时间(起)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',maxDate:'${params.endDate!}',onpicked:beginPick})">
				</div>
				<div class="col-md-2" style="width:120px;">
					<input type="text" name="endDate" value="${(params.endDate)!}" class="form-control" placeholder="录入时间(止)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',minDate:'${params.beginDate!}',onpicked:endPick})">
				</div>
				<div class="col-md-2" style="width:120px;">
					<select class="form-control" name="status">
						<option value="" <#if !params.status??>selected</#if> >--状态--</option>
						<option value="0" <#if (params.status!)=='0'>selected</#if> >下架</option>
						<option value="1" <#if (params.status!)=='1'>selected</#if> >待确认</option>
						<option value="3" <#if (params.status!)=='3'>selected</#if> >已确认</option>
						<option value="2" <#if (params.status!)=='2'>selected</#if> >发布中</option>
					</select>
				</div>
				<div class="col-md-2" style="width:120px;">
					<select class="form-control" name="order">
						<option value="" <#if !params.order??>selected</#if> >--排序--</option>
						<option value="4" <#if (params.order!)=='4'>selected</#if> >按销量</option>
						<option value="1" <#if (params.order!)=='1'>selected</#if> >按价格</option>
						<option value="2" <#if (params.order!)=='2'>selected</#if> >按录入时间</option>
					</select>
				</div>
				<button type="submit" class="btn btn-success" style="margin-right:20px;">查询</button>
				<a class="btn" href="1?cid=${params.cid!}" style="margin-right:20px;">清空查询</a>
				<button add="${params.cid!}" type="button" class="btn btn-info">录入项目</button>
			</div>
		</form>
		<div class="col-md-8">
			<#if (params.status!) == "" || (params.status!) == "0" || (params.status!) == "2">
				<h4 style="display:inline;margin-left:50px;">批量处理：</h4>
				<button type="button" class="btn btn-success" onclick="checkNum('2')" style="margin-left: 0px;">发布</button>
				<button type="button" class="btn btn-success" onclick="checkNum('0')">下架</button>
			</#if>
		</div>
		<div class="col-md-2">
			<form id="export" action="/netbar/resource/exportExcel" method="post">
				<input type="hidden" name="page" value="${params.page!}" />
				<input type="hidden" name="cid" value="${params.cid!}" />
				<input type="hidden" name="name" value="${params.name!}" />
				<input type="hidden" name="beginDate" value="${params.beginDate!}" />
				<input type="hidden" name="endDate" value="${params.endDate!}" />
				<input type="hidden" name="areaCode" value="${params.areaCode!}" />
				<input type="hidden" name="categoryId" value="${params.categoryId!}" />
				<input type="hidden" name="categoryPid" value="${params.categoryPid!}" />
				<input type="hidden" name="status" value="${params.status!}" />
				<input type="hidden" name="qualifications" value="${params.qualifications!}" />
				<input type="hidden" name="order" value="${params.order!}" />
				<button type="submit" class="btn btn-success" style="">导出</button>
			</form>
		</div>
	</div>
	<div class="col-md-8" style="margin-bottom: 20px;">
		
	</div>
	
	<script type="text/javascript">
		$('button[qualifications]').on('click', function(event) {
			var $form = $('#search');
			$form.find('input[name="qualifications"]').val($(this).attr('qualifications'));
			$form.submit();
		});
	</script>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th><input id="selectAll_id" type="checkbox" /></th>
			<th>编号</th>
			<th>商品名称</th>
			<th>项目名称</th>
			<th>价格</th>
			<th>奖金可用</th>
			<th>单位</th>
			<th>类别</th>
			<th>细分</th>
			<th>地区</th>
			<th>资格</th>
			<th>数量或档期</th>
			<th>状态</th>
			<th>操作</th>
		</tr>
		<#list list as o>
			<tr>
				<#assign qualifications=o.qualifications!0>
				<#assign status=o.status!0>
				<#assign isTop=(o.isTop?number)!0>
				<td><label><input name="checkId" type="checkbox" value="${o.id!}"/></label></td>
				<td>${o.propertyNo!}</td>
				<td>${o.name!}</td>
				<td>${o.propertyName!}</td>
				<td>${o.price!}</td>
				<td>${(o.useQuoRatio!0)*100}%</td>
				<td>${o.measure!}</td>
				<td>${o.typeNameP!}</td>
				<td>${o.typeName!}</td>
				<td>${o.areaName!}</td>
				<td>
				<#if qualifications==0>
					无
				<#elseif qualifications==1>
					会员
				<#elseif qualifications==2>
					黄金
				</#if>
				</td>
				<td>
					<#if (o.cateType!-1)==1>
						${o.inventory!}/${o.inventoryTotal!}
					<#elseif (o.cateType!-1)==0>
						${o.settlDate!}
					</#if>
				</td>
				<td>
				<#if status==2>
					发布中
				<#elseif status==0>
					下架
				<#elseif status==1>
					待确认
				</#if>
				</td>
				<td>
					<a class="btn btn-info" href="${ctx}/netbar/resource/commodity/preview?commodityId=${o.commodityId!}&propertyId=${o.id!}" target="_blank">预览</a>
					<button edit="${o.id!}" type="button" class="btn btn-info">编辑</button>
					<#if status==2>
						<button changeStatus="${o.id!}" oper="0" type="button" class="btn btn-danger">下架</button>
					<#elseif status==0 || status==3>
						<button changeStatus="${o.id!}" oper="2" type="button" class="btn btn-danger">发布</button>
					</#if>
				</td>
			</tr>
		</#list>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	
	<#-- 编辑/新增-->
	<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
	<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
	<div id="modal-editor" class="modal fade">
		<div class="modal-dialog" style="width:1000px">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideEditor()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 id="h4_edit_id" class="modal-title" >编辑项目（提示：档期只能单个编辑）</h4>
					<h4 id="h4_add_id" class="modal-title" >新增项目</h4>
				</div>
				<div class="modal-body">
					<form id="from_id_edit" class="form-horizontal form-condensed" role="form" method="post">
						<input id="input_id_id" type="hidden" name="id"/>
						<input id="input_id_isRedbag" type="hidden" name="isRedbag"/>
						<input id="input_id_isNew" type="hidden" name="isNew"/>
						<input id="input_id_commodityId" type="hidden" name="commodityId"/>
						<input type="hidden" name="cid"/>
						<input type="hidden" name="catePid"/>
						<div class="form-group">
							<label class="col-md-2 control-label">类别选择<font id="font_id_1" color="red">*</font></label>
							<div class="col-md-10" style="width:200px;">
								<select id="select_id_categoryPid" name="categoryPid" class="form-control" disabled>
									<option value="">--请选择--</option>
									<#list superCategorys as t>
										<option value="${t.id!}">${t.name!}</option>
									</#list>
								</select>
							</div>
							<div class="col-md-10" style="width:200px;">
								<select id="select_id_categoryId" name="categoryId" class="form-control" disabled>
									<option value="">--请选择--</option>
								</select>
							</div>
						</div>
						<div id="div_id_name" class="form-group">
							<label class="col-md-2 control-label">商品名称<font id="font_id_2" color="red">*</font></label>
							<div class="col-md-10" style="width:550px;">
								<input id="input_id_name" class="form-control" type="text" name="name" value="" disabled>
							</div>
							<label class="col-md-2 control-label">设置单位<font id="font_id_2" color="red">*</font></label>
							<div class="col-md-10" style="width:100px;">
								<input id="input_id_measure" class="form-control" type="text" name="measure" value="">
							</div>
						</div>
						<div id="div_id_icon" class="form-group">
							<label class="col-md-2 control-label">商品首图<font id="font_id_3" color="red">*</font></label>
							<div class="col-md-10">
								<input id="input_id_icon" type="file" name="icon_file" value="图片" style="width:164px;" disabled>
								<font class="prompt">
									大小不超过1M
								</font>
							</div>
						</div>
						<div id="div_id_qualifications" class="form-group">
							<label class="col-md-2 control-label">基础资格</label>
							<div class="col-md-10" style="width:200px;">
								<select id="select_id_qualifications" name="qualifications" class="form-control" disabled>
									<option value="0">无</option>
									<option value="1">会员</option>
									<option value="2">黄金</option>
								</select>
							</div>
						</div>
						<div id="div_id_minusAndadd" class="form-group">
							<label class="col-md-2 control-label">编辑可售内容：</label>
							<button id="button_id_minus" type="button" class="btn btn-success" onclick="minusBlock()" style="margin-left:10px;">-</button>
							<button id="button_id_add" type="button" class="btn btn-success" onclick="addBlock()" style="margin-left:10px;">+</button>
						</div>
						<hr id="hr_id_1" style="height:1px;border:none;border-top:1px solid #555555;"/>
						<div div-round="0" class="form-group">
							<div id="div_id_xm" class="form-group">
								<label id="label_id_propertyName" class="col-md-2 control-label">项目1<font id="font_id_4" color="red">*</font></label>
								<div class="col-md-10" id="div_id_propertyName">
									<input id="input_id_propertyName" class="form-control" type="text" name="blocks[0].propertyName" value="">
								</div>
							</div>
							<div class="form-group" id="div_id_price">
								<label id="label_id_price" class="col-md-2 control-label">价格<font id="font_id_5" color="red">*</font></label>
								<div id="div_id_amount" class="col-md-10" style="width:200px;display:inline;">
									<input id="input_id_price" class="form-control" style="display:inline;" type="text" name="blocks[0].price" value="" onblur="regExpMoney('input_id_price')">
								</div>元<span id="input_id_price_ts" style="display:none">请输入金额格式</span>
								<div class="col-md-10" id="div_id_redbagPriceStr" style="width:164px;display:none;">
									<select id="select_id_redbagPriceStr" name="redbagPriceStr" class="form-control">
										<option value="2">2</option>
										<option value="5">5</option>
										<option value="10">10</option>
										<option value="50">50</option>
										<option value="100">100</option>
									</select>
								</div>
							</div>
							
							<div class="form-group" id="div_id_fakeSoldNum">
								<label id="label_id_fakeSoldNum" class="col-md-2 control-label">已售数量<font id="font_id_5" color="red">*</font></label>
								<div id="div_id_fakeSoldNum" class="col-md-10" style="width:200px;display:inline;">
									<input id="input_id_fakeSoldNum" class="form-control" style="display:inline;" type="text" name="blocks[0].fakeSoldNum" value="0" onblur="regExpMoney('input_id_fakeSoldNum')">
								</div><span id="input_id_fakeSoldNum_ts" style="display:none">请输入已售数量</span>
							</div>
							<div class="form-group">
								<label id="label_id_cateType" class="col-md-2 control-label">编辑档期或可售数量<font id="font_id_6" color="red">*</font></label>
								<div id="div_id_cateType" class="col-md-10" style="width:88px;">
									<select id="select_id_dateOrNum" name="blocks[0].cateType" class="form-control">
										<option value="0">档期</option>
										<option value="1">数量</option>
									</select>
								</div>
								<div id="div_id_settlDate" class="col-md-10" style="width:112px;">
									<input id="input_id_settlDate" class="form-control" type="text" name="settlDateString" placeholder="选择档期-->" value="" onclick="onclickOper()" onfocus="onfocusOper('')">
								</div>
								<div id="div_id_settlDates" class="col-md-10" style="width:570px;">
									<input id="input_id_settlDates" class="form-control" type="text" name="blocks[0].settlDates" value="" placeholder="已选档期（可多选）" readonly>
								</div>
								<button id="button_id_settlDates" type="button" class="btn " onclick="clearDates('')">清空</button>
								<div id="div_id_inventoryTotal" class="col-md-10" style="width:200px;display:none">
									<input id="input_id_inventoryTotal" class="form-control" type="text" name="blocks[0].inventoryTotal" value="" placeholder="输入数量" onblur="regExpInt('input_id_inventoryTotal')">
								</div><span id="input_id_inventoryTotal_ts" style="display:none">请输入正整数</span>
								<label id="label_id_unit" class="col-md-2 control-label" style="display:none">起售数量<font id="font_id_5" color="red">*</font></label>
								<div id="div_id_unit" class="col-md-10" style="width:200px;display:none">
									<input id="input_id_unit" class="form-control" type="text" name="unit" value="" placeholder="输入数量" onblur="regExpInt('input_id_unit')">
								</div><span id="input_id_unit_ts" style="display:none">请输入正整数</span>
							</div>
							<div class="form-group">
								<label id="label_id_qualifiType" class="col-md-2 control-label">购买条件</label>
								<div id="div_id_qualifiType" class="col-md-10" style="width:200px;">
									<select id="select_id_qualifiType" name="blocks[0].qualifiType" class="form-control">
										<option value="0">无</option>
										<option value="1">流水满</option>
										<option value="2">必须购买</option>
									</select>
								</div>
								<div id="div_id_flow" class="col-md-10" style="width:200px;display:block">
									<input id="input_id_conditions" class="form-control" type="text" name="blocks[0].conditions" value="" placeholder="输入流水满金额" onblur="regValueAddedMoneyCondition('input_id_conditions')">
								</div><span id="span_id_redbag">满xx金额必须≥2倍增值券金额</span><span id="input_id_conditions_ts" style="display:none">请输入金额格式</span>
								<div id="div_id_buy" class="col-md-10" style="width:200px;display:none">
									<select id="select_id_conditions" name="blocks[0].conditionsId" class="form-control">
										<#list commoditys as t>
											<option value="${t.id!}">${t.pname!}</option>
										</#list>
									</select>
								</div>
							</div>
							<hr id="hr_id_1" style="height:1px;border:none;border-top:1px solid #555555;"/>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">奖金可用比例</label>
							<div class="col-md-10" style="width:200px;">
								<input id="select_id_useQuoRatio" class="form-control" disabled />
								<#-- <select id="select_id_useQuoRatio" name="useQuoRatio" class="form-control" disabled>
									<#list 10..1 as i>
										<option value="${i/10}">${i*10}</option>
									</#list>
								</select> -->
							</div><span style="float:left">%</span>
							<label class="col-md-2 control-label">可见地区</label>
							<div class="col-md-10" style="width:200px;">
								<select id="select_id_province" name="province" class="form-control" disabled>
									<option value="000000">全国</option>
									<#list provinces as t>
										<option value="${t.areaCode!}">${t.name!}</option>
									</#list>
								</select>
							</div>
						</div>
						<div id="div_id_comTag" class="form-group" style="display:none;">
							<label class="col-md-2 control-label">标签设置</label>
							<div class="col-md-10" style="width:200px;">
								<select id="select_id_comTag" name="comTag" class="form-control" disabled>
									<option value="">无</option>
									<option value="1">促销</option>
									<option value="2">打折</option>
									<option value="3">热销</option>
								</select>
							</div>
							<label id="label_id_validity" class="col-md-2 control-label" style="display:none;">总额效期设置<font id="font_id_5" color="red">*</font></label>
							<div id="div_id_validity" class="col-md-10" style="width:200px;display:none">
								<select id="select_id_validity" name="validity" class="form-control">
									<option value="1">1</option>
									<option value="2">2</option>
									<option value="3">3</option>
									<option value="5">5</option>
									<option value="7">7</option>
								</select>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">折扣设置</label>
							<div class="col-md-10" style="width:200px;">
								<select id="select_id_goldRebate" name="goldRebate" class="form-control">
									<option value="">黄金折扣</option>
									<option value="1">无</option>
									<#list 9..1 as i>
										<option value="${i/10}">${i}折</option>
									</#list>
								</select>
							</div>
							<div class="col-md-10" style="width:200px;">
								<select id="select_id_vipRatio" name="vipRatio" class="form-control">
									<option value="">会员折扣</option>
									<option value="1">无</option>
									<#list 9..1 as i>
										<option value="${i/10}">${i}折</option>
									</#list>
								</select>
							</div>
						</div>
						<div class="form-group" style="display:none">
							<label class="col-md-2 control-label">商品介绍<font id="font_id_7" color="red">*</font></label>
							<div class="col-md-10">
								<textarea id="textarea_id_introduce" name="introduce" placeholder="" autofocus disabled></textarea>
								<script type="text/javascript">
									_editor1 = editor($('#textarea_id_introduce'));
								</script>
							</div>
						</div>
						<div class="form-group" style="display:none">
							<label class="col-md-2 control-label">购买说明<font id="font_id_8" color="red">*</font></label>
							<div class="col-md-10">
								<textarea id="textarea_id_description" name="description" placeholder="" autofocus disabled></textarea>
								<script type="text/javascript">
									_editor2 = editor($('#textarea_id_description'));
								</script>
							</div>
						</div>
						<div id="div_id_executes" class="form-group">
							<label class="col-md-2 control-label">执行人<font id="font_id_9" color="red">*</font></label>
							<div class="col-md-10" style="width:200px;">
								<input id="input_id_execute" class="form-control" type="text" name="executes" value="" disabled>
							</div>
							<label class="col-md-2 control-label">联系手机<font id="font_id_10" color="red">*</font></label>
							<div class="col-md-10" style="width:200px;">
								<input id="input_id_executePhone" class="form-control" type="text" name="executePhone" value="" onblur="regExpPhone('input_id_executePhone')" disabled>
							</div><span id="input_id_executePhone_ts" style="display:none">请输入11位正确的手机号</span>
						</div>
						<div id="div_id_isRecommend" class="form-group">
							<label class="col-md-2 control-label">是否列为推荐</label>
							<div class="col-md-10">
								<input type="radio" name="isRecommend" value="0" checked disabled>否
								<input type="radio" name="isRecommend" value="1" disabled>是
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" onclick="hideEditor()">关闭</button>
					<button id="id_submit" type="button" class="btn btn-primary" onclick="submitEditor()">保存</button>
				</div>
			</div>
		</div>
	</div>
	
	<script type="text/javascript">
	// 初始化模态框
	var _$editor = $('#modal-editor');
	var _$top = $('#modal-top');
	_$editor.modal({
		keyboard : false,
		show : false
	})
	
	// 关闭模态框
	function hideEditor() {
		_$editor.modal('hide');
	}
	function hideTop() {
		_$top.modal('hide');
	}
	
	// 初始化表单数据
	function fillForm(columns) {
		for(var k in columns) {
			_$editor.find('input[name="' + k + '"]').val(columns[k]);
		}
	}
	
	// 显示模态框，编辑
	$('button[edit]').on('click', function(event) {
		$('#h4_edit_id').show();
		$('#h4_add_id').hide();
		var id = $(this).attr('edit');
		$.ajax({
			url:'${ctx}/netbar/resource/info/'+id,
			data : '',
			dataType : 'json',
		    type:'post',
		    async:false,
		    success: function(d){
		    	if(d.code == 0) {
		    		var c = d.object.commodity;
		    		var p = d.object.property;
		    		var t = d.object.category;
		    		redbagShow(t.name);
					$.fillForm({
						id: p.id,
						isNew: '0',
						commodityId: p.commodityId,
						cid: c.id,
						catePid: t.pid,
						name: c.name,
						measure: p.measure,
						icon_file: c.url,
						unit: p.unit,
						executes: c.executes,
						executePhone: c.executePhone,
					},_$editor);
					$('#input_id_propertyName').val(p.propertyName);
					$('#input_id_price').val(p.price);
					$('#input_id_fakeSoldNum').val(p.fakeSoldNum);
					$('#input_id_fakeSoldNum').attr("readonly","readonly");
					$('#select_id_redbagPriceStr').val(p.price);
					$('#input_id_inventoryTotal').val(p.inventory);
					$('#input_id_conditions').val(p.conditions);
					$('#input_id_settlDates').val(getTime(p.settlDate));
					
					$('#select_id_categoryPid').val(t.pid);
					$('#select_id_categoryPid').trigger("change");
					$('#select_id_categoryId').val(t.id);
					$('#select_id_qualifications').val(c.qualifications);
					$('#select_id_dateOrNum').val(p.cateType);
					$('#select_id_dateOrNum').trigger("change"); 
					$('#select_id_qualifiType').val(p.qualifiType);
					$('#select_id_qualifiType').trigger("change"); 
					$('#select_id_conditions').val(p.conditions);
					$('#select_id_useQuoRatio').val(c.useQuoRatio * 100);
					$('#select_id_province').val(c.province);
					$('#select_id_comTag').val(c.comTag);
					$('#select_id_validity').val(p.validity);
					$('#select_id_vipRatio').val(p.vipRatio);
					$('#select_id_goldRebate').val(p.goldRebate);
					$('#select_id_jewelRatio').val(p.jewelRatio);
					setEditorText($('#textarea_id_introduce'), c.introduce);
					setEditorText($('#textarea_id_description'), c.description);
					$("input[type='radio'][name=isRecommend][value='"+c.isRecommend+"']").attr("checked",true);
					$("input[type='radio'][name=isTop][value='"+c.isTop+"']").attr("checked",true);
		    	} else {
		    		alert(d.result);
		    	}
			}
		});
		$('#div_id_minusAndadd').hide();
		$('#label_id_propertyName').html('项目名<font id="font_id_4" color="red">*</font>');
		_$editor.modal('show');
	});
	
	// 显示模态框，新增
	$('button[add]').on('click', function(event) {
		$('#h4_add_id').show();
		$('#h4_edit_id').hide();
		$('#div_id_minusAndadd,#button_id_minus,#button_id_add').show();
		
		var id = $(this).attr('add');
		var r=0;
		$.ajax({
			url:'${ctx}/netbar/resource/isValueAddedCard/'+id,
			data : '',
			dataType : 'json',
		    type:'post',
		    success: function(d){
		    	if(d.code == 0) {
		    		r = d.object.isRedbag;
		    		if(r==1){
			    		redbagShow('增值券');
		    		}
		    	} else {
		    		alert(d.result);
		    	}
			}
		});
		
		$.ajax({
			url:'${ctx}/netbar/resource/commodity/info/'+id,
			data : '',
			dataType : 'json',
		    type:'post',
		    success: function(d){
		    	if(d.code == 0) {
		    		var c = d.object.commodity;
		    		var t = d.object.category;
					$.fillForm({
						isRedbag: r,
						isNew: '1',
						commodityId: c.id,
						cid: c.id,
						catePid: t.pid,
						name: c.name,
						icon_file: c.url,
						executes: c.executes,
						executePhone: c.executePhone
					},_$editor);
					$('#select_id_categoryPid').val(t.pid);
					$('#select_id_categoryPid').trigger("change");
					$('#select_id_categoryId').val(t.id);
					$('#select_id_qualifications').val(c.qualifications);
					$('#select_id_useQuoRatio').val(c.useQuoRatio * 100);
					$('#select_id_province').val(c.province);
					$('#select_id_comTag').val(c.comTag);
					setEditorText($('#textarea_id_introduce'), c.introduce);
					setEditorText($('#textarea_id_description'), c.description);
					$("input[type='radio'][name=isRecommend][value='"+c.isRecommend+"']").attr("checked",true);
		    	} else {
		    		alert(d.result);
		    	}
			}
		});
		
		$.fillForm({
			id: '',
			measure: '',
			settlDateString: '',
			unit: '',
			executes: '',
			executePhone: '',
		},_$editor);
		$('#input_id_propertyName').val('');
		$('#input_id_price').val('');
		$('#input_id_fakeSoldNum').val('0');
		$('#input_id_inventoryTotal').val('');
		$('#input_id_conditions').val('');
		
		$('#select_id_dateOrNum').val('0');
		$('#select_id_qualifiType').val('0');
		$('#select_id_conditions')[0].selectedIndex=0;
		$('#select_id_validity').val('1');
		$('#select_id_vipRatio').val('');
		$('#select_id_goldRebate').val('');
		
		$('#label_id_validity,#div_id_validity,#div_id_comTag,#div_id_inventoryTotal,#div_id_unit,#div_id_flow,#label_id_unit,#span_id_redbag').hide();
		$('#div_id_name,#div_id_propertyName,#div_id_cateType,#div_id_settlDate,#div_id_settlDates,#button_id_settlDates,#div_id_qualifiType,#div_id_executes,#div_id_isRecommend,#div_id_isTop,#div_id_icon,#div_id_qualifications,#hr_id_1').show();
		$('#label_id_price').html('价格<font id="font_id_5" color="red">*</font>');
		$('#input_id_price').attr('placeholder','');
		$('#label_id_cateType').html('编辑档期或可售数量<font id="font_id_6" color="red">*</font>');
		$('#label_id_qualifiType').text('购买条件');
		$('#input_id_conditions').attr('placeholder','输入条件');
		$('[div-round]').show();
		
		_$editor.modal('show');
	});
	
	//append后的元素注册change事件
	function initCategoryIdClick() {
		$('#select_id_categoryId').off('change');
		$('#select_id_categoryId').on('change',function(){
			var name = $('#select_id_categoryId option:selected').text();
			redbagShow(name);
		});
	}
	initCategoryIdClick();
	
	function redbagShow(name){
		if(name=='增值券'){
			$('#div_id_minusAndadd,#button_id_minus,#button_id_add').hide();
			$('#label_id_validity,#div_id_validity,#div_id_comTag,#div_id_inventoryTotal,#div_id_unit,#div_id_flow,#label_id_unit,#span_id_redbag').show();
			$('#div_id_name,#div_id_propertyName,#div_id_cateType,#div_id_settlDate,#div_id_settlDates,#button_id_settlDates,#div_id_qualifiType,#div_id_buy,#div_id_executes,#div_id_isRecommend,#div_id_isTop,#div_id_qualifications,#hr_id_1').hide();
			$('#label_id_price').html('金额选择<font id="font_id_11" color="red">*</font>');
			$('#input_id_price').attr('placeholder','输入增值券金额');
			$('#label_id_cateType').html('可售数量<font id="font_id_11" color="red">*</font>');
			$('#label_id_qualifiType').html('使用条件<font id="font_id_11" color="red">*</font>');
			$('#input_id_conditions').attr('placeholder','满 _ 可用');
			$('#input_id_conditions').attr('placeholder','满 _ 可用');
			$('#select_id_qualifiType').val('3');
			$('#select_id_dateOrNum').val('1'); 
			$('[div-round]').hide();
			$('[div-round]:first').show();
			$('#div_id_icon,#div_id_amount,#div_id_xm').hide();
			$('#div_id_redbagPriceStr').show();
		}else{
			$('#div_id_minusAndadd,#button_id_minus,#button_id_add').show();
			$('#label_id_validity,#div_id_validity,#div_id_comTag,#div_id_inventoryTotal,#div_id_unit,#div_id_flow,#div_id_buy,#label_id_unit,#span_id_redbag').hide();
			$('#div_id_name,#div_id_propertyName,#div_id_cateType,#div_id_settlDate,#div_id_settlDates,#button_id_settlDates,#div_id_qualifiType,#div_id_executes,#div_id_isRecommend,#div_id_isTop,#div_id_qualifications,#hr_id_1').show();
			$('#label_id_price').html('价格<font id="font_id_5" color="red">*</font>');
			$('#input_id_price').attr('placeholder','');
			$('#label_id_cateType').html('编辑档期或可售数量<font id="font_id_6" color="red">*</font>');
			$('#label_id_qualifiType').text('购买条件');
			$('#input_id_conditions').attr('placeholder','输入条件');
			$('#select_id_qualifiType').val('0');
			$('#select_id_dateOrNum').val('0');
			$('[div-round]').show();
			$('#label_id_propertyName').html('项目1<font id="font_id_5" color="red">*</font>');
			$('#div_id_icon,#div_id_amount,#div_id_xm').show();
			$('#div_id_redbagPriceStr').hide();
		}
	}
	
	//档期插件控制
	var flag = 0;
	var dateRecord,dateRecord2,dateRecord3,dateRecord4,dateRecord5,dateRecord6;
	function onclickOper(){
		flag = 0;
		WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',minDate:'%y-%M-%d'});
	}
	function onfocusOper(blockNo){
		flag = 1;
		var isNew = $('#input_id_isNew').val();
		var date = $('#input_id_settlDate'+blockNo).val();
		var dates = $('#input_id_settlDates'+blockNo).val();
		var dateList = dates.split('|');
		for(var d in dateList){
			if(date!='' && dateList[d]==date){
				alert('不能重复选择档期');
				return false;
			}
		}
		if(isNew==0){
			$('#input_id_settlDates').attr('placeholder','修改档期');
			$('#input_id_settlDate'+blockNo).val('');
			$('#input_id_settlDates'+blockNo).val(date);
		}else{
			if(date!='' && flag==1 && (date+'_'+blockNo)!=dateRecord){
				if(dates==''){
					dates = date ;
				}else{
					dates = dates + '|' + date ;
				}
				dateRecord = date + '_' + blockNo;
				$('#input_id_settlDate'+blockNo).val('');
			}
			$('#input_id_settlDates'+blockNo).val(dates);
		}
	}
	
	function clearDates(blockNo){
		$('#input_id_settlDates'+blockNo).val('');
	}
	
	//类别选择联动
	$('#select_id_categoryPid').bind('change',function(){
		var pid = $('#select_id_categoryPid').val();
		var $categorySelect = $("#select_id_categoryId");
		var $categorySelectParent = $categorySelect.parent();
		$categorySelect.remove();
		$('#select_id_categoryId_chosen').remove();
		$categorySelectParent.append('<select id="select_id_categoryId" name="categoryId" class="form-control" disabled></select>');
		$("#select_id_categoryId").html("");
		$("#select_id_categoryId").append('<option value="">--请选择--</option>');
		categoryAppend(pid,'');
	});
	$('#select_id_categoryPid_search').bind('change',function(){
		var pid = $('#select_id_categoryPid_search').val();
		var $categorySelect = $("#select_id_categoryId_search");
		var $categorySelectParent = $categorySelect.parent();
		$categorySelect.remove();
		$('#select_id_categoryId_search_chosen').remove();
		$categorySelectParent.append('<select id="select_id_categoryId_search" name="categoryId" class="form-control"></select>');
		$("#select_id_categoryId_search").html("");
		$("#select_id_categoryId_search").append('<option value="">--细分--</option>');
		categoryAppend(pid,'_search');
	});
	function categoryAppend(pid,search){
		$.ajax({
			url:'${ctx}/netbar/resource/categorys/'+pid,
			data : '',
			dataType : 'json',
		    type:'post',
		    success: function(d){
		    	if(d.code == 0) {
		    		var list = d.object;
				    for(var i=0;i<list.length;i++){
				    	var id = list[i].id;
				    	var name = list[i].name;
				    	$("#select_id_categoryId"+search).append('<option value="'+id+'">'+name+'</option>');
				    }
			    	initCategoryIdClick();
		    	} else {
		    		alert(d.result);
		    	}
			},
			async: false
		});
	}
	
	//档期或数量change注册
	function initDateOrNumChange(blockNo){
		$('#select_id_dateOrNum'+blockNo).off('change');
		$('#select_id_dateOrNum'+blockNo).on('change',function(){
			var or = $('#select_id_dateOrNum'+blockNo).val();
			if(or==0){
				$('#div_id_settlDate'+blockNo+',#div_id_settlDates'+blockNo+',#button_id_settlDates'+blockNo).show();
				$('#div_id_inventoryTotal'+blockNo).hide();
			}else if(or==1){
				$('#div_id_settlDate'+blockNo+',#div_id_settlDates'+blockNo+',#button_id_settlDates'+blockNo).hide();
				$('#div_id_inventoryTotal'+blockNo).show();
			}else{
				$('#div_id_settlDate'+blockNo+',#div_id_settlDates'+blockNo+',#button_id_settlDates'+blockNo+',#div_id_inventoryTotal'+blockNo).hide();
			}
		});
	}
	initDateOrNumChange('');
	
	//购买条件change注册
	function initQualifiType(blockNo){
		$('#select_id_qualifiType'+blockNo).off('change');
		$('#select_id_qualifiType'+blockNo).on('change',function(){
			var type = $('#select_id_qualifiType'+blockNo).val();
			if(type==1){
				$('#input_id_conditions'+blockNo).attr('placeholder','输入流水满金额');
				$('#div_id_flow'+blockNo).show();
				$('#div_id_buy'+blockNo).hide();
			}else if(type==2){
				$('#div_id_flow'+blockNo).hide();
				$('#div_id_buy'+blockNo).show();
			}else if(type==3){
				$('#input_id_conditions'+blockNo).attr('placeholder','满 _ 可用');
				$('#div_id_flow'+blockNo).show();
				$('#div_id_buy'+blockNo).hide();
			}else if(type==0){
				$('#div_id_flow'+blockNo+',#div_id_buy'+blockNo).hide();
			}
		});
	}
	initQualifiType('');
	
	var blockCount=0;
	//删除可售内容块
	function minusBlock(){
		if(blockCount <= 0){
			alert("至少保留一个");
			return;
		}else{
			$('[div-round]:last').remove();
			blockCount--;
		}
	}
	
	//添加可售内容块
	function addBlock(){
		if(blockCount >= 5){
			alert("最多只能添加6个");
			return;
		}else{
			$('[div-round]:last').after(roundDom());
			initDateOrNumChange(blockCount+2);
			initQualifiType(blockCount+2);
			blockCount++;
		}
	}
	
	// 生成dom(blockCount从0开始计数)
	function roundDom(id, beginTime, overTime, endTime, netbars, areas, remark) {
		var html = '<div div-round="' + blockCount + '" class="form-group">'
				 + '	<div id="div_id_propertyName" class="form-group">'
				 + '		<label class="col-md-2 control-label">项目' + (blockCount + 2) + '<font id="font_id_4" color="red">*</font></label>'
				 + '		<div class="col-md-10">'
				 + '			<input id="input_id_propertyName' + (blockCount + 2) + '" class="form-control" type="text" name="blocks[' + (blockCount+1) + '].propertyName" value="">'
				 + '		</div>'
				 + '	</div>'
		 		 + '	<div class="form-group">'
				 + '		<label id="label_id_price" class="col-md-2 control-label">价格<font id="font_id_5" color="red">*</font></label>'
				 + '		<div class="col-md-10" style="width:200px;">'
				 + '			<input id="input_id_price' + (blockCount + 2) + '" class="form-control" type="text" name="blocks[' + (blockCount+1) + '].price" value="" onblur="regExpMoney(&quot;' + 'input_id_price' + (blockCount + 2) +'&quot;)">'
				 + '		</div>元<span id="input_id_price' + (blockCount + 2) + '_ts" style="display:none">请输入金额格式</span>'
				 + '	</div>'
				 + '	<div class="form-group">'
				 + '		<label id="label_id_fakeSoldNum" class="col-md-2 control-label">已售数量<font id="font_id_5" color="red">*</font></label>'
				 + '		<div class="col-md-10" style="width:200px;">'
				 + '			<input id="input_id_fakeSoldNum' + (blockCount + 2) + '" class="form-control" type="text"  name="blocks[' + (blockCount+1) + '].fakeSoldNum"   onblur="regExpMoney(&quot;' + 'input_id_fakeSoldNum' + (blockCount + 2) +'&quot;)">'
				 + '		</div><span id="input_id_fakeSoldNum' + (blockCount + 2) + '_ts" style="display:none">请输入已售数量</span>'
				 + '	</div>'
				 + '	<div class="form-group">'
				 + '		<label id="label_id_cateType" class="col-md-2 control-label">编辑档期或可售数量<font id="font_id_6" color="red">*</font></label>'
				 + '		<div id="div_id_cateType" class="col-md-10" style="width:88px;">'
				 + '			<select id="select_id_dateOrNum' + (blockCount + 2) + '" name="blocks[' + (blockCount+1) + '].cateType" class="form-control">'
				 + '				<option value="0">档期</option>'
				 + '				<option value="1">数量</option>'
				 + '			</select>'
				 + '		</div>'
				 + ' 		<div id="div_id_settlDate' + (blockCount + 2) + '" class="col-md-10" style="width:112px;">'
				 + '			<input id="input_id_settlDate' + (blockCount + 2) + '" class="form-control" type="text" name="settlDateString' + (blockCount + 2) + '" placeholder="选择档期-->" value="" onclick="onclickOper()" onfocus="onfocusOper(' + (blockCount + 2) + ')">'
				 + '		</div>'
				 + '		<div id="div_id_settlDates' + (blockCount + 2) + '" class="col-md-10" style="width:570px;">'
				 + '			<input id="input_id_settlDates' + (blockCount + 2) + '" class="form-control" type="text" name="blocks[' + (blockCount+1) + '].settlDates" value="" placeholder="已选档期（可多选）" readonly>'
				 + '		</div>'
				 + '		<button id="button_id_settlDates' + (blockCount + 2) + '" type="button" class="btn " onclick="clearDates(' + (blockCount + 2) + ')">清空</button>'
				 + '		<div id="div_id_inventoryTotal' + (blockCount + 2) + '" class="col-md-10" style="width:200px;display:none">'
				 + '			<input id="input_id_inventoryTotal' + (blockCount + 2) + '" class="form-control" type="text" name="blocks[' + (blockCount+1) + '].inventoryTotal" value="" placeholder="输入数量" onblur="regExpInt(&quot;' + 'input_id_inventoryTotal' + (blockCount + 2) +'&quot;)">'
				 + '		</div><span id="input_id_inventoryTotal' + (blockCount + 2) + '_ts" style="display:none">请输入正整数</span>'
				 + '	</div>'
				 + '	<div class="form-group">'
				 + '		<label id="label_id_qualifiType' + (blockCount + 2) + '" class="col-md-2 control-label">购买条件</label>'
				 + '		<div id="div_id_qualifiType' + (blockCount + 2) + '" class="col-md-10" style="width:200px;">'
				 + '			<select id="select_id_qualifiType' + (blockCount + 2) + '" name="blocks[' + (blockCount+1) + '].qualifiType" class="form-control">'
				 + '				<option value="0">无</option>'
				 + '				<option value="1">流水满</option>'
				 + '				<option value="2">必须购买</option>'
				 + '			</select>'
				 + '		</div>'
				 + '		<div id="div_id_flow' + (blockCount + 2) + '" class="col-md-10" style="width:200px;display:none">'
				 + '			<input id="input_id_conditions' + (blockCount + 2) + '" class="form-control" type="text" name="blocks[' + (blockCount+1) + '].conditions" value="" placeholder="输入流水满金额" onblur="regExpMoney(&quot;' + 'input_id_conditions' + (blockCount + 2) +'&quot;)">'
				 + '		</div><span id="input_id_conditions' + (blockCount + 2) + '_ts" style="display:none">请输入金额格式</span>'
				 + '		<div id="div_id_buy' + (blockCount + 2) + '" class="col-md-10" style="width:200px;display:none">'
				 + '			<select id="select_id_conditions' + (blockCount + 2) + '" name="blocks[' + (blockCount+1) + '].conditionsId" class="form-control">'
				 + '				<#list commoditys as t>'
				 + '					<option value="${t.id!}">${t.pname!}</option>'
				 + '				</#list>'
				 + '			</select>'
				 + '		</div>'
				 + '	</div>'
				 + '	<hr id="hr_id_1" style="height:1px;border:none;border-top:1px solid #555555;"/>'
				 + '</div>';
		
		return html;
	}
	
	// 更改状态
	$('button[changeStatus]').on('click', function(event) {
		var _this = $(this);
		_this.attr('disabled', true);
		var id = $(this).attr('changeStatus');
		var oper = $(this).attr('oper');
		var info;
		if(oper==0){
			info = "确认下架吗？";
		}else if(oper==2){
			info = "确认发布吗？";
		}
		
		$.confirm(info, function() {
			$.api('${ctx}/netbar/resource/changeStatus/' + id + '/' + oper, {}, function(d) {
				window.location.reload();
			}, function(d) {
				alert('操作失败：' + d.result);
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
	
	// 更改置顶
	$('button[changetop]').on('click', function(event) {
		var _this = $(this);
		_this.attr('disabled', true);
		var id = $(this).attr('changetop');
		var oper = $(this).attr('oper');
		var info;
		if(oper==0){
			info = "确认取消置顶吗？";
		}else if(oper==1){
			info = "确认置顶吗？";
		}
		
		$.confirm(info, function() {
			$.api('${ctx}/netbar/resource/changetop/' + id + '/' + oper, {}, function(d) {
				window.location.reload();
			}, function(d) {
				alert('操作失败：' + d.result);
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
	
	// 监听列表复选按钮事件
	$('#selectAll_id').on('click', function(event) {
		selectAll();
	});
	
	//全选/取消全选
	var checked = false;
	function selectAll(){
		var a = document.getElementsByName("checkId");
		if(!checked){
			for(var i = 0;i<a.length;i++){
				if(a[i].type == "checkbox") a[i].checked = true;
			}
			checked=true;
		}else{
			for(var i = 0;i<a.length;i++){
				if(a[i].type == "checkbox") a[i].checked = false;
			}
			checked=false;
		}
	}
	
	//批量操作
	 function checkNum(operate){
	    var num=0;
        var path;
        var info;
        var ids = '';
	    var a = document.getElementsByName("checkId");
	    for(var i=0;i<a.length;i++){
          if(a[i].checked==true){
				num += 1;
       	  	}
	     }
       	 if(num==0){
          	alert("请选择要操作的订单");
          	return false;
         }
       	 if(operate==1 && num>5){
          	alert("置顶商品不能超过5个");
          	return false;
         }
         $("[name='checkId']:checked").each(function(index, element) {
            if(ids==''){
            	ids = $(this).val();
         	}else{
             	ids += "," + $(this).val();
         	}
         });
         if(operate==2){
         	path = 'changeStatus';
         	info = '确定发布已选中的'+num+'个商品吗?';
         }else if(operate==0){
         	path = 'changeStatus';
         	info = '确定下架已选中的'+num+'个商品吗?';
         }
       	 $.confirm(info, function() {
			$.api('${ctx}/netbar/resource/'+path+'/'+ids + '/' + operate, {}, function(d) {
				if(d.code == 0) {
		    		window.location.reload();
		    	} else {
		    		alert(d.result);
		    	}
			});
		}, undefined, {
			complete: function() {
				$(this).attr('disabled', false);
			}
		});
     }
     
     function getTime(/** timestamp=0 **/) {
		var ts = arguments[0] || 0;
		var t,y,m,d,h,i,s;
		t = ts ? new Date(ts) : new Date();
		y = t.getFullYear();
		m = t.getMonth()+1;
		d = t.getDate();
		h = t.getHours();
		i = t.getMinutes();
		s = t.getSeconds();
		return y+'-'+(m<10?'0'+m:m)+'-'+(d<10?'0'+d:d);
	}
	// 正则表达式，验证满多少钱可用
	function regValueAddedMoneyCondition(d) {
		regExpMoney(d);
		var condition = $('#input_id_conditions').val();
		var unit = $('#select_id_redbagPriceStr').val();
		if(Number(condition)<Number(unit)*2){
			regExp = false;
			$('#input_id_conditions').val('');
			alert("满xx金额必须≥2倍增值券金额");
		}
	}
	// 正则表达式，验证金额
	function regExpMoney(d) {
		var reg = /^(([1-9]\d{0,9})|0)(\.\d{1,2})?$/;
		regExpPub(d,reg);
	}
	// 正则表达式，正整数
	function regExpInt(d) {
		var reg = /^[1-9]\d*$/;
		var id = "#"+d;
		var id_ts = id+"_ts";
		$(id_ts).html('请输入正整数');
		regExpPub(d,reg);
		
		var name = $('#select_id_categoryId option:selected').text();
		if(regExp && name=='增值券' && (d=='input_id_unit' || d=='input_id_inventoryTotal')){
			var unit = $('#input_id_unit').val();
			var inventoryTotal = $('#input_id_inventoryTotal').val();
			if(unit!='' && inventoryTotal!='' && Number(unit)>Number(inventoryTotal)){
				$('#id_submit').attr('disabled', true);
				$(id_ts).html('起售数量不能大于可售数量');
				$(id_ts).css('color','red');
				$(id_ts).show();
				$(id).focus();
				regExp = false;
			}else{
				$(id_ts).html('请输入正整数');
				$('#id_submit').attr('disabled', false);
				$(id_ts).hide();
				regExp = true;
			}
		}
	}
	// 正则表达式，11位手机号
	function regExpPhone(d) {
		var reg = /^1[3|4|5|7|8]\d{9}$/;
		regExpPub(d,reg);
	}
	
	function regExpPub(d,reg){
		var id = "#"+d;
		var id_ts = id+"_ts";
		var obj = $.trim($(id).val());
		if(obj != ""){
			if(!reg.test(obj)){      
				$('#id_submit').attr('disabled', true);
				$(id_ts).css('color','red');
				$(id_ts).show();
				$(id).focus();
				regExp = false;
			}else{
				$('#id_submit').attr('disabled', false);
				$(id_ts).hide();
				regExp = true;
			}
		}else{
			$('#id_submit').attr('disabled', false);
			$(id_ts).hide();
		}
	}
	
	function checkParams(isNew){
		var categoryId = $('#select_id_categoryId').val();
		var name = $('#input_id_name').val();
		var measure = $('#input_id_measure').val();
		var icon = $('#input_id_icon').val();
		
		var price = $('#input_id_price').val();
		var inventoryTotal = $('#input_id_inventoryTotal').val();
		
		var introduce = $('#textarea_id_introduce').val();
		var description = $('#textarea_id_description').val();
		var execute = $('#input_id_execute').val();
		var executePhone = $('#input_id_executePhone').val();
		var condition = $('#input_id_conditions').val();
		var unit = $('#input_id_unit').val();
		
		var typeName = $('#select_id_categoryId option:selected').text();
		if(typeName=='增值券'){
			if(categoryId=='' || inventoryTotal=='' || unit=='' ||condition==''){
				return false;
			}
		}else {
			if(categoryId=='' || name=='' || measure=='' || execute=='' || executePhone==''){
				return false;
			}
			for(var i=0;i<=blockCount;i++){
				var blockNo = '';
				if(blockCount>0){
					blockNo = (blockCount + 1)+'';
				}
				if(!checkBlockParams(blockNo)){
					return false;
				}
			}
		}
		
		return true;
	}
	function checkBlockParams(blockNo){
		var propertyName = $('#input_id_propertyName'+blockNo).val();
		var price = $('#input_id_price'+blockNo).val();
		var settlDates = $('#input_id_settlDates'+blockNo).val();
		var inventoryTotal = $('#input_id_inventoryTotal'+blockNo).val();
		
		if((settlDates=='' && inventoryTotal=='') || propertyName=='' || price==''){
			return false;
		}
		
		return true;
	}
	
	// 提交表单
	var regExp = true;
	function submitEditor() {
		var name = $('#select_id_categoryId option:selected').text();
		if(name=='增值券'){
			$('#input_id_isRedbag').val('1');
		}else{
			$('#input_id_isRedbag').val('0');
		}
		var isNew = $('#input_id_isNew').val();
		if(!regExp){
			alert("格式填写不正确");
		}
		if(checkParams(isNew) && regExp){
	    	$('#id_submit').attr('disabled',true);
			_$editor.find('form').ajaxSubmit({
				url:'${ctx}/netbar/resource/save',
			    type:'post',
			    success: function(d){
			    	$('#id_submit').attr('disabled',false);
			    	if(d.code == 0) {
			    		window.location.reload();
			    	} else {
			    		alert(d.result);
			    	}
				}
			});
		}else{
			alert("红色*号为必填");
		}
	}
	
	</script>
</body>
</html>