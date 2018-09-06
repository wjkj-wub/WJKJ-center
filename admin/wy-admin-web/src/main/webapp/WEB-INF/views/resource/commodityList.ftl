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
<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>商品维护</strong>
 		</li>
		<li class="active">
			商品列表
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post" >
			<input id="input_id_qualifications" type="hidden" name="qualifications" value="${(params.qualifications)!}" />
			<div class="mb10">
				<div class="col-md-2" style="width:120px;">
					<input type="text" class="form-control" name="name" placeholder="商品名称" value="${(params.name)!}" />
				</div>
				<div class="col-md-2" style="width:120px;">
					<input type="text" name="beginDate" value="${(params.beginDate)!}" class="form-control" placeholder="录入时间(起)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',maxDate:'${params.endDate!}',onpicked:beginPick})">
				</div>
				<div class="col-md-2" style="width:120px;">
					<input type="text" name="endDate" value="${(params.endDate)!}" class="form-control" placeholder="录入时间(止)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',minDate:'${params.beginDate!}',onpicked:endPick})">
				</div>
				<div class="col-md-2" style="width:120px;">
					<select class="form-control" name="areaCode">
						<option value=""  >--地区--</option>
						<option value="000000" <#if (params.areaCode!)=='000000'>selected</#if> >全国</option>
						<#if provinces??>
							<#list provinces as t>
								<option value="${t.areaCode!}" <#if (params.areaCode!)==(t.areaCode)!>selected</#if> >${t.name!}</option>
							</#list>
						</#if>
					</select>
				</div>
				<div class="col-md-2" style="width:120px;">
					<select id="select_id_categoryPid_search" class="form-control" name="categoryPid">
						<option value="">--大类别--</option>
						<#list superCategorys as t>
							<option value="${t.id!}" <#if (params.categoryPid!)=='${t.id!}'>selected</#if> >${t.name!}</option>
						</#list>
					</select>
				</div>
				<div class="col-md-2" style="width:120px;">
					<select id="select_id_categoryId_search" class="form-control" name="categoryId">
						<option value="">--小类别--</option>
						<#if categorys??>
							<#list categorys as c>
							<option value="${c.id!}" <#if (params.categoryId!)=='${c.id!}'>selected</#if> >${c.name!}</option>
							</#list>
						</#if>
					</select>
				</div>
				<button type="submit" class="btn btn-success" style="margin-right:20px;">查询</button>
				<a class="btn" href="1" style="margin-right:20px;">清空查询</a>
			</div>
			<div class="col-md-8" style="margin-bottom: 20px;">
				<div class="btn-group" data-toggle="buttons-checkbox">
					<button qualifications="" type="button" class="btn <#if (params.qualifications!) == ''>btn-danger<#else>btn-</#if>">全部</button>
					<button qualifications="1" type="button" class="btn <#if (params.qualifications!) == '1'>btn-danger<#else>btn-</#if>">会员</button>
					<button qualifications="2" type="button" class="btn <#if (params.qualifications!) == '2'>btn-danger<#else>btn-</#if>">黄金</button>
				</div>
				<#if (params.status!) == "" || (params.status!) == "0" || (params.status!) == "2">
					<h4 style="display:inline;margin-left:50px;">批量处理：</h4>
					<!-- <button type="button" class="btn btn-success" onclick="checkNum('2')" style="margin-left: 50px;">发布</button>
					<button type="button" class="btn btn-success" onclick="checkNum('0')">下架</button> -->
					<button type="button" class="btn btn-success" onclick="checkNum('1')">置顶</button>
					<button type="button" class="btn btn-success" onclick="checkNum('-1')">取消置顶</button>
				</#if>
			</div>
		</form>
		<button topManage="" type="button" class="btn btn-success">置顶顺序管理</button>
		<div class="col-md-2">
			<form id="export" action="/netbar/resource/commodity/exportExcel" method="post">
				<input type="hidden" name="page" value="${params.page!}" />
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
		<button add="" type="button" class="btn btn-info">新增录入</button>
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
			<th>商品名称</th>
			<th>奖金可用</th>
			<th>类别</th>
			<th>细分</th>
			<th>地区</th>
			<th>资格</th>
			<th>操作</th>
		</tr>
		<#list list as o>
			<tr>
				<#assign qualifications=o.qualifications!0>
				<#assign isTop=(o.isTop?number)!0>
				<td><label><input name="checkId" type="checkbox" value="${o.commodityId!}"/></label></td>
				<td><a href="${ctx}/netbar/resource/list/1?cid=${o.commodityId!}">${o.name!}</a></td>
				<td>${(o.useQuoRatio!0)*100}%</td>
				<td>${o.typeNameP!}</td>
				<td>${o.typeName!}</td>
				<td>
				<#if (o.areaName!)=='ROOT'>全国
					<#else >
						${o.areaName!}
				</#if></td>
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
					<a class="btn btn-info" href="${ctx}/netbar/resource/commodity/preview?commodityId=${o.commodityId!}" target="_blank">预览</a>
					<button edit="${o.commodityId!}" type="button" class="btn btn-info">编辑</button>
					<!-- <button changeStatus="${o.commodityId!}" oper="2" type="button" class="btn btn-danger">发布</button>
					<button changeStatus="${o.commodityId!}" oper="0" type="button" class="btn btn-danger">下架</button> -->
					<#if (isTop>0)>
						<button changetop="${o.commodityId!}" oper="0" type="button" class="btn btn-danger">取消置顶</button>
					<#else>
						<button changetop="${o.commodityId!}" oper="1" type="button" class="btn btn-danger">置顶</button>
					</#if>
				</td>
			</tr>
		</#list>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	<#-- 新增div-->
	<div id="modal-editor" class="modal fade">
		<div class="modal-dialog" style="width:1000px">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideEditor()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title" >新增</h4>
				</div>
				<div class="modal-body">
					<form id="from_id_edit" class="form-horizontal form-condensed" role="form" method="post">
						<input id="input_id_id" type="hidden" name="id"/>
						<input id="input_id_isRedbag" type="hidden" name="isRedbag"/>
						<input type="hidden" name="isNew"/>
						<input type="hidden" name="isC"/>
						<input type="hidden" name="cid"/>
						<input id="input_id_commodityId" type="hidden" name="commodityId"/>
						<div class="form-group">
							<label class="col-md-2 control-label">类别选择<font id="font_id_1" color="red">*</font></label>
							<div class="col-md-10" style="width:200px;">
								<select id="select_id_categoryPid" name="categoryPid" class="form-control">
									<option value="">--请选择--</option>
									<#list superCategorys as t>
										<option value="${t.id!}">${t.name!}</option>
									</#list>
								</select>
							</div>
							<div class="col-md-10" style="width:200px;">
								<select id="select_id_categoryId" name="categoryId" class="form-control">
									<option value="">--请选择--</option>
								</select>
							</div>
						</div>
						<div id="div_id_name" class="form-group">
							<label class="col-md-2 control-label">商品名称<font id="font_id_2" color="red">*</font></label>
							<div class="col-md-10" style="width:550px;">
								<input id="input_id_name" class="form-control" type="text" name="name" value="">
							</div>
							<label class="col-md-2 control-label">设置单位<font id="font_id_2" color="red">*</font></label>
							<div class="col-md-10" style="width:100px;">
								<input id="input_id_measure" class="form-control" type="text" name="measure" value="">
							</div>
						</div>
						<div id="div_id_icon" class="form-group">
							<label class="col-md-2 control-label">商品首图<font id="font_id_3" color="red">*</font></label>
							<div class="col-md-10">
								<input id="input_id_icon" type="file" name="icon_file" value="图片" style="width:164px;">
								<font class="prompt">
									大小不超过1M
								</font>
							</div>
						</div>
						<div id="div_id_qualifications" class="form-group">
							<label class="col-md-2 control-label">基础资格</label>
							<div class="col-md-10" style="width:200px;">
								<select id="select_id_qualifications" name="qualifications" class="form-control">
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
								<label id="label_id_price" class="col-md-2 control-label">已售数量<font id="font_id_5" color="red">*</font></label>
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
								<input type="hidden" name="useQuoRatio" />
								<div class="input-group">
									<span class="input-group-btn">
										<button class="btn btn-default" type="button" onclick="addUseQuoRatio('#select_id_useQuoRatio', -1)"> - </button>
									</span>
									<input id="select_id_useQuoRatio" class="form-control" />
									<span class="input-group-btn">
										<button class="btn btn-default" type="button" onclick="addUseQuoRatio('#select_id_useQuoRatio', 1)"> + </button>
									</span>
								</div>
								<#-- <select id="select_id_useQuoRatio" name="useQuoRatio" class="form-control">
									<#list 10..0 as i>
										<option value="${i/10}">${i*10}</option>
									</#list>
								</select> -->
							</div>
							<span style="float:left;">%</span>
							<label class="col-md-2 control-label">可见地区</label>
							<div class="col-md-10" style="width:200px;">
								<select id="select_id_province" name="province" class="form-control">
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
								<select id="select_id_comTag" name="comTag" class="form-control">
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
						<div class="form-group">
							<label class="col-md-2 control-label">商品介绍<font id="font_id_7" color="red">*</font></label>
							<div class="col-md-10">
								<textarea id="textarea_id_introduce" name="introduce" placeholder="" autofocus></textarea>
								<script type="text/javascript">
									_editor1 = editor($('#textarea_id_introduce'));
								</script>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">购买说明<font id="font_id_8" color="red">*</font></label>
							<div class="col-md-10">
								<textarea id="textarea_id_description" name="description" placeholder="" autofocus></textarea>
								<script type="text/javascript">
									_editor2 = editor($('#textarea_id_description'));
								</script>
							</div>
						</div>
						<div id="div_id_executes" class="form-group">
							<label class="col-md-2 control-label">执行人<font id="font_id_9" color="red">*</font></label>
							<div class="col-md-10" style="width:200px;">
								<input id="input_id_execute" class="form-control" type="text" name="executes" value="">
							</div>
							<label class="col-md-2 control-label">联系手机<font id="font_id_10" color="red">*</font></label>
							<div class="col-md-10" style="width:200px;">
								<input id="input_id_executePhone" class="form-control" type="text" name="executePhone" value="" onblur="regExpPhone('input_id_executePhone')">
							</div><span id="input_id_executePhone_ts" style="display:none">请输入11位正确的手机号</span>
						</div>
						<div id="div_id_isRecommend" class="form-group">
							<label class="col-md-2 control-label">是否列为推荐</label>
							<div class="col-md-10">
								<input type="radio" name="isRecommend" value="0" checked>否
								<input type="radio" name="isRecommend" value="1">是
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
	
	<div id="modal-commodity" class="modal fade">
		<div class="modal-dialog" style="width:1000px">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideCommodity()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title" >编辑商品属性（不含项目）</h4>
				</div>
				<div class="modal-body">
					<form id="from_id_edit" class="form-horizontal form-condensed" role="form" method="post">
						<input id="input_id_id_c" type="hidden" name="id"/>
						<input type="hidden" name="isC"/>
						<input type="hidden" name="cid"/>
						<div class="form-group">
							<label class="col-md-2 control-label">类别选择<font id="font_id_1" color="red">*</font></label>
							<div class="col-md-10" style="width:200px;">
								<select id="select_id_categoryPid_c" name="categoryPid" class="form-control">
									<option value="">--请选择--</option>
									<#list superCategorys as t>
										<option value="${t.id!}">${t.name!}</option>
									</#list>
								</select>
							</div>
							<div class="col-md-10" style="width:200px;">
								<select id="select_id_categoryId_c" name="categoryId" class="form-control">
									<option value="">--请选择--</option>
								</select>
							</div>
						</div>
						<div id="div_id_name_c" class="form-group">
							<label class="col-md-2 control-label">商品名称<font id="font_id_2" color="red">*</font></label>
							<div class="col-md-10" style="width:550px;">
								<input id="input_id_name_c" class="form-control" type="text" name="name" value="">
							</div>
						</div>
						<div id="div_id_icon_c" class="form-group">
							<label class="col-md-2 control-label">商品首图<font id="font_id_3" color="red">*</font></label>
							<div class="col-md-10">
								<input id="input_id_icon_c" type="file" name="icon_file" value="图片" style="width:164px;">
								<font class="prompt">
									大小不超过1M
								</font>
							</div>
						</div>
						<div id="div_id_qualifications_c" class="form-group">
							<label class="col-md-2 control-label">基础资格</label>
							<div class="col-md-10" style="width:200px;">
								<select id="select_id_qualifications_c" name="qualifications" class="form-control">
									<option value="0">无</option>
									<option value="1">会员</option>
									<option value="2">黄金</option>
								</select>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">奖金可用比例</label>
							<div class="col-md-10" style="width:200px;">
								<input type="hidden" name="useQuoRatio" />
								<div class="input-group">
									<span class="input-group-btn">
										<button class="btn btn-default" type="button" onclick="addUseQuoRatio('#select_id_useQuoRatio_c', -1)"> - </button>
									</span>
									<input id="select_id_useQuoRatio_c" class="form-control" />
									<span class="input-group-btn">
										<button class="btn btn-default" type="button" onclick="addUseQuoRatio('#select_id_useQuoRatio_c', 1)"> + </button>
									</span>
								</div>
								<#-- <select id="select_id_useQuoRatio_c" name="useQuoRatio" class="form-control">
									<#list 10..0 as i>
										<option value="${i/10}">${i*10}</option>
									</#list>
								</select> -->
							</div>
							<span style="float:left;">%</span>
							<label class="col-md-2 control-label">可见地区</label>
							<div class="col-md-10" style="width:200px;">
								<select id="select_id_province_c" name="province" class="form-control">
									<option value="000000">全国</option>
									<#list provinces as t>
										<option value="${t.areaCode!}">${t.name!}</option>
									</#list>
								</select>
							</div>
						</div>
						<div id="div_id_comTag_c" class="form-group" style="display:none;">
							<label class="col-md-2 control-label">标签设置</label>
							<div class="col-md-10" style="width:200px;">
								<select id="select_id_comTag_c" name="comTag" class="form-control">
									<option value="">无</option>
									<option value="1">促销</option>
									<option value="2">打折</option>
									<option value="3">热销</option>
								</select>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">商品介绍<font id="font_id_7" color="red">*</font></label>
							<div class="col-md-10">
								<textarea id="textarea_id_introduce_c" name="introduce" placeholder="" autofocus></textarea>
								<script type="text/javascript">
									_editor1 = editor($('#textarea_id_introduce_c'));
								</script>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">购买说明<font id="font_id_8" color="red">*</font></label>
							<div class="col-md-10">
								<textarea id="textarea_id_description_c" name="description" placeholder="" autofocus></textarea>
								<script type="text/javascript">
									_editor2 = editor($('#textarea_id_description_c'));
								</script>
							</div>
						</div>
						<div id="div_id_executes_c" class="form-group">
							<label class="col-md-2 control-label">执行人<font id="font_id_9" color="red">*</font></label>
							<div class="col-md-10" style="width:200px;">
								<input id="input_id_execute_c" class="form-control" type="text" name="executes" value="">
							</div>
							<label class="col-md-2 control-label">联系手机<font id="font_id_10" color="red">*</font></label>
							<div class="col-md-10" style="width:200px;">
								<input id="input_id_executePhone_c" class="form-control" type="text" name="executePhone" value="" onblur="regExpPhone('input_id_executePhone_c')">
							</div><span id="input_id_executePhone_c_ts" style="display:none">请输入11位正确的手机号</span>
						</div>
						<div id="div_id_isRecommend_c" class="form-group">
							<label class="col-md-2 control-label">是否列为推荐</label>
							<div class="col-md-10">
								<input type="radio" name="isRecommend" value="0" checked>否
								<input type="radio" name="isRecommend" value="1">是
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" onclick="hideCommodity()">关闭</button>
					<button id="id_submit_c" type="button" class="btn btn-primary" onclick="submitCommodity()">保存</button>
				</div>
			</div>
		</div>
	</div>
	
	<div id="modal-top" class="modal fade">
		<div class="modal-dialog" style="width:800px">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideTop()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 id="h4_edit_id" class="modal-title" >置顶顺序管理（排序号为1~5）</h4>
				</div>
				<div class="modal-body">
					<form id="from_id_edit" class="form-horizontal form-condensed" role="form" method="post">
						<input id="" type="hidden" name="id"/>
						<table class="table table-striped table-hover">	
							<tr>
								<th>序号</th>
								<th>商品名称</th>
							</tr>
							<#list tops as o>
								<tr>
									<input type="hidden" name="proId" value="${o.id!}"/>
									<td><input class="form-control" style="width:50px;" type="text" name="no" value="${o.no!}"></td>
									<td>${o.pname!}</td>
								</tr>
							</#list>
						</table>
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" onclick="hideTop()">关闭</button>
					<button id="id_submitTop" type="button" class="btn btn-primary" onclick="submitTop()">确认</button>
				</div>
			</div>
		</div>
	</div>
	
	<script type="text/javascript">
	// 初始化模态框
	var _$editor = $('#modal-editor');
	var _$commodity = $('#modal-commodity');
	var _$top = $('#modal-top');
	_$editor.modal({
		keyboard : false,
		show : false
	})
	
	// 关闭模态框
	function hideEditor() {
		_$editor.modal('hide');
	}
	function hideCommodity() {
		_$commodity.modal('hide');
	}
	function hideTop() {
		_$top.modal('hide');
	}
	
	//置顶顺序管理
	$('button[topManage]').on('click', function(event) {
		_$top.modal('show');
	});
	
	//append后的元素注册change事件
	function initCategoryIdClick() {
		$('#select_id_categoryId').off('change');
		$('#select_id_categoryId').on('change',function(){
			var name = $('#select_id_categoryId option:selected').text();
			redbagShow(name);
		});
		$('#select_id_categoryId_c').off('change');
		$('#select_id_categoryId_c').on('change',function(){
			var name = $('#select_id_categoryId_c option:selected').text();
			redbagShow_c(name);
		});
	}
	initCategoryIdClick();
	
	function redbagShow(name){
		if(name=='红包'){
			alert('红包商品类别已失效');
			hideEditor();
			hideCommodity();
			return;
		}
		if(name=='增值券'){
			alert('增值券商品类别已失效');
			hideEditor();
			hideCommodity();
			return;
			$('#div_id_minusAndadd,#button_id_minus,#button_id_add').hide();
			$('#label_id_validity,#div_id_validity,#div_id_comTag,#div_id_inventoryTotal,#div_id_unit,#div_id_flow,#label_id_unit,#span_id_redbag').show();
			$('#div_id_name,#div_id_propertyName,#div_id_cateType,#div_id_settlDate,#div_id_settlDates,#button_id_settlDates,#div_id_qualifiType,#div_id_buy,#div_id_executes,#div_id_isRecommend,#div_id_isTop,#div_id_qualifications,#hr_id_1').hide();
			$('#label_id_price').html('金额选择<font id="font_id_11" color="red">*</font>');
			$('#input_id_price').attr('placeholder','输入增值券金额');
			$('#label_id_cateType').html('可售数量<font id="font_id_11" color="red">*</font>');
			$('#label_id_qualifiType').html('使用条件<font id="font_id_11" color="red">*</font>');
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
	
	function redbagShow_c(name){
		if(name=='红包'){
			alert('红包商品类别已失效');
			hideEditor();
			hideCommodity();
			return;
		}
		if(name=='增值券'){
			alert('增值券商品类别已失效');
			hideEditor();
			hideCommodity();
			return;
			$('#label_id_validity_c,#div_id_validity_c,#div_id_comTag_c,#div_id_inventoryTotal_c,#div_id_unit_c,#div_id_flow_c,#label_id_unit_c,#span_id_redbag_c').show();
			$('#div_id_name_c,#div_id_propertyName_c,#div_id_cateType_c,#div_id_settlDate_c,#div_id_settlDates_c,#button_id_settlDates_c,#div_id_qualifiType_c,#div_id_buy_c,#div_id_executes_c,#div_id_isRecommend_c,#div_id_isTop_c,#div_id_qualifications_c').hide();
			$('#label_id_price_c').html('金额选择<font id="font_id_11" color="red">*</font>');
			$('#input_id_price_c').attr('placeholder','输入增值券金额');
			$('#label_id_cateType_c').html('可售数量<font id="font_id_11" color="red">*</font>');
			$('#label_id_qualifiType_c').html('使用条件<font id="font_id_11" color="red">*</font>');
			$('#input_id_conditions_c').attr('placeholder','满 _ 可用');
			$('#select_id_qualifiType_c').val('3');
			$('#select_id_dateOrNum_c').val('1'); 
			$('[div-round]').hide();
			$('[div-round]:first').show();
			$('#div_id_icon_c,#div_id_amount_c,#div_id_xm_c').hide();
			$('#div_id_redbagPriceStr_c').show();
		}else{
			$('#label_id_validity_c,#div_id_validity_c,#div_id_comTag_c,#div_id_inventoryTotal_c,#div_id_unit_c,#div_id_flow_c,#div_id_buy_c,#label_id_unit_c,#span_id_redbag_c').hide();
			$('#div_id_name_c,#div_id_propertyName_c,#div_id_cateType_c,#div_id_settlDate_c,#div_id_settlDates_c,#button_id_settlDates_c,#div_id_qualifiType_c,#div_id_executes_c,#div_id_isRecommend_c,#div_id_isTop_c,#div_id_qualifications_c').show();
			$('#label_id_price_c').html('价格<font id="font_id_5" color="red">*</font>');
			$('#input_id_price_c').attr('placeholder','');
			$('#label_id_cateType_c').html('编辑档期或可售数量<font id="font_id_6" color="red">*</font>');
			$('#label_id_qualifiType_c').text('购买条件');
			$('#input_id_conditions_c').attr('placeholder','输入条件');
			$('#select_id_qualifiType_c').val('0');
			$('#select_id_dateOrNum_c').val('0');
			$('[div-round]').show();
			$('#label_id_propertyName_c').html('项目1<font id="font_id_5" color="red">*</font>');
			$('#div_id_icon_c,#div_id_amount_c,#div_id_xm_c').show();
			$('#div_id_redbagPriceStr_c').hide();
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
		$categorySelectParent.append('<select id="select_id_categoryId" name="categoryId" class="form-control"></select>');
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
	$('#select_id_categoryPid_c').bind('change',function(){
		var pid = $('#select_id_categoryPid_c').val();
		var $categorySelect = $("#select_id_categoryId_c");
		var $categorySelectParent = $categorySelect.parent();
		$categorySelect.remove();
		$('#select_id_categoryId_c_chosen').remove();
		$categorySelectParent.append('<select id="select_id_categoryId_c" name="categoryId" class="form-control"></select>');
		$("#select_id_categoryId_c").html("");
		$("#select_id_categoryId_c").append('<option value="">--请选择--</option>');
		categoryAppend(pid,'_c');
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
				 + '		<label id="label_id_sold_num" class="col-md-2 control-label">已售数量<font id="font_id_5" color="red">*</font></label>'
				 + '		<div class="col-md-10" style="width:200px;">'
				 + '			<input id="input_id_fakeSoldNum' + (blockCount + 2) + '" class="form-control" type="text" name="blocks[' + (blockCount+1) + '].fakeSoldNum" value="" onblur="regExpMoney(&quot;' + 'input_id_fakeSoldNum' + (blockCount + 2) +'&quot;)">'
				 + '		</div>元<span id="input_id_fakeSoldNum' + (blockCount + 2) + '_ts" style="display:none">请输入已售数量(默认为0)</span>'
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
			info = "确认将该商品的所有项目下架吗？";
		}else if(oper==2){
			info = "确认将该商品的所有项目发布吗？";
		}
		
		$.confirm(info, function() {
			$.api('${ctx}/netbar/resource/commodity/changeStatus/' + id + '/' + oper, {}, function(d) {
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
			$.api('${ctx}/netbar/resource/commodity/changetop/' + id + '/' + oper, {}, function(d) {
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
         }else if(operate==1){
         	path = 'changetop';
         	info = '确定置顶已选中的'+num+'个商品吗?';
         }else if(operate==-1){
         	path = 'changetop';
         	info = '确定取消置顶已选中的'+num+'个商品吗?';
         }
       	 $.confirm(info, function() {
			$.api('${ctx}/netbar/resource/commodity/'+path+'/'+ids + '/' + operate, {}, function(d) {
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
	
	function submitTop() {
		 var ids='';
		 var nos='';
		 
		 $("[name='proId']").each(function(index, element) {
            if(ids==''){
            	ids = $(this).val();
         	}else{
             	ids += "," + $(this).val();
         	}
         });
         
		 $("[name='no']").each(function(index, element) {
            if(nos==''){
            	nos = $(this).val();
         	}else{
             	nos += "," + $(this).val();
         	}
         });
	 	 if(ids==''){
	 	 	window.location.reload();
	 	 }
         var noList = nos.split(',');
	 	 var len = noList.length;
	 	 for(var i=0;i<len-1 ;i++ ){
		 	if(noList[i]<1 || noList[i]>5 || noList[4]<1 || noList[4]>5){
		 		alert('排序号为1~5');
		 		return false;
		 	}
	 	 	for(var j=i+1;j<len ;j++ ){
		 		if(noList[i]==noList[j]){
		 			alert('排序号不能重复');
		 			return false;
		 		}
		 	}
	 	 }
         
    	$('#id_submitTop').attr('disabled',true);
		_$top.find('form').ajaxSubmit({
			url:'${ctx}/netbar/resource/commodity/topManage/'+ids+'/'+nos,
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
	
	// 显示模态框，编辑
	$('button[edit]').on('click', function(event) {
		var id = $(this).attr('edit');
		$.ajax({
			url:'${ctx}/netbar/resource/commodity/info/'+id,
			data : '',
			dataType : 'json',
		    type:'post',
		    async:false,
		    success: function(d){
		    	if(d.code == 0) {
		    		var c = d.object.commodity;
		    		var t = d.object.category;
					$.fillForm({
						id: c.id,
						isC: '1',
						name: c.name,
						icon_file: c.url,
						executes: c.executes,
						executePhone: c.executePhone
					},_$commodity);
					$('#select_id_categoryPid_c').val(t.pid);
					$('#select_id_categoryPid_c').trigger("change");
					$('#select_id_categoryId_c').val(t.id);
					$('#select_id_qualifications_c').val(c.qualifications);
					$('#select_id_useQuoRatio_c').val(c.useQuoRatio*100).change();
					$('#select_id_province_c').val(c.province);
					$('#select_id_comTag_c').val(c.comTag);
					setEditorText($('#textarea_id_introduce_c'), c.introduce);
					setEditorText($('#textarea_id_description_c'), c.description);
					$("input[type='radio'][name=isRecommend][value='"+c.isRecommend+"']").attr("checked",true);
		    	} else {
		    		alert(d.result);
		    	}
			}
		});
		var name = $('#select_id_categoryId_c option:selected').text();
		redbagShow_c(name);
		_$commodity.modal('show');
	});
	
	function addUseQuoRatio(selector, num) {
		var oldNum = parseInt($(selector).val());
		if(isNaN(oldNum)) {
			oldNum = 0;
		}
		
		var inputNum = parseInt(num);
		if(isNaN(inputNum)) {
			inputNum = 0;
		}
		
		var result = oldNum + inputNum;
		if(result > 100) {
			result = 100;
		}
		if(result < 0) {
			result = 0;
		}
		$('.form-group #select_id_useQuoRatio, #select_id_useQuoRatio_c').val(result).change();
	}
	$('.form-group #select_id_useQuoRatio, #select_id_useQuoRatio_c').on('change', function(ev) {
		var val = $(this).val();
		val = val.replace(/\D/g, '');
		
		var num = parseInt(val);
		if(isNaN(num)) {
			num = 0;
		} else if(num > 100) {
			num = 100;
		} else if(num < 0) {
			num = 0;
		}
		
		$(this).val(num);
		$('.form-group [name="useQuoRatio"]').val(num / 100);
	});
	
	// 显示模态框，新增
	$('button[add]').on('click', function(event) {
		$('#h4_edit_id').hide();
		$('#h4_add_id,#font_id_1,#font_id_2,#font_id_3,#font_id_4,#font_id_5,#font_id_6,#font_id_7,#font_id_8,#font_id_9,#font_id_10').show();
		$('#div_id_minusAndadd,#button_id_minus,#button_id_add').show();
		$.fillForm({
			id: '',
			isNew: '1',
			isC: '1',
			cid:'0',
			name: '',
			measure: '',
			icon_file: '',
			settlDateString: '',
			unit: '',
			executes: '',
			executePhone: '',
		},_$editor);
		$('#input_id_propertyName').val('');
		$('#input_id_price').val('');
		$('#input_id_inventoryTotal').val('');
		$('#input_id_conditions').val('');
		
		$('#select_id_categoryPid').val('');
		$('#select_id_categoryId').val('');
		$('#select_id_qualifications').val('0');
		$('#select_id_dateOrNum').val('0');
		$('#select_id_qualifiType').val('0');
		$('#select_id_conditions')[0].selectedIndex=0;
		$('#select_id_useQuoRatio').val('0').change();
		$('#select_id_province').val('000000');
		$('#select_id_comTag').val('');
		$('#select_id_validity').val('1');
		$('#select_id_vipRatio').val('');
		$('#select_id_goldRebate').val('0');
		$('#select_id_jewelRatio').val('');
		setEditorText($('#textarea_id_introduce'), '');
		setEditorText($('#textarea_id_description'), '');
		$("input[type='radio'][name=isRecommend][value='']").attr("checked",true);
		$("input[type='radio'][name=isTop][value='']").attr("checked",true);
		
		<#if params.isActivityAdmin>
			$('#select_id_province').val('${userAreaCode!'000000'}');
		</#if>
		
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
	
	// 提交表单，新增
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
	
	// 提交表单，编辑
	function submitCommodity() {
		if(!regExp){
			alert("格式填写不正确");
		}
		if(checkParamsC() && regExp){
	    	$('#id_submit_c').attr('disabled',true);
	    	
			_$commodity.find('form').ajaxSubmit({
				url:'${ctx}/netbar/resource/commodity/save',
			    type:'post',
			    success: function(d){
			    	$('#id_submit_c').attr('disabled',false);
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
	
	function checkParamsC(){
		var categoryId = $('#select_id_categoryId_c').val();
		var name = $('#input_id_name_c').val();
		var introduce = $('#textarea_id_introduce_c').val();
		var description = $('#textarea_id_description_c').val();
		if(categoryId=='' || name=='' || introduce=='' || description==''){
			return false;
		}
		var typeName = $('#select_id_categoryId option:selected').text();
		if(typeName=='增值券'&& condition==''){
				return false;
		}
		return true;
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
			if(categoryId=='' || inventoryTotal=='' || unit=='' || introduce=='' || description==''|| condition==''){
				return false;
			}
		}else {
			if(categoryId=='' || name=='' || measure=='' || (icon=='' && isNew==1) || introduce=='' || description=='' || execute=='' || executePhone==''){
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
	
	</script>
</body>
</html>