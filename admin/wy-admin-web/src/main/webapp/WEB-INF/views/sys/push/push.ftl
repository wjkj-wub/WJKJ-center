<#import "/macros/pager.ftl" as p >
<html>
<head>

<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/textext/css/textext.core.css">
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/textext/css/textext.plugin.arrow.css">
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/textext/css/textext.plugin.autocomplete.css">
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/textext/css/textext.plugin.clear.css">
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/textext/css/textext.plugin.focus.css">
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/textext/css/textext.plugin.prompt.css">
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/textext/css/textext.plugin.tags.css">
<script type="text/javascript" src="${ctx!}/static/plugin/jqueryAutocomplete/jquery.autocomplete.js"></script>

<style type="text/css">
.autocomplete-suggestion {
	border: 1px solid #CCC;
	padding: 2px 10px;
	background-color: #FFF;
	max-height: 200px;
	overflow: auto;
}

.autocomplete-suggestions{
    border: 1px solid #CCC;
    max-height:200px;
    overflow-y: auto;
    /* 防止水平滚动条 */
    overflow-x: hidden;

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
			推送消息
		</li>
	</ul>
	
	<#-- 表单内容 -->
	<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
	<div id="submit_id">
		<form class="form-horizontal form-condensed" role="form" method="post">
			<table class="table table-striped table-hover">	
					<tr>
						<td>
							<label>标题</label><br>
							<input id="title_id" type="text" name="title" value="" onblur="checkBlankTitle()" maxlength ="10"/>
							<span id="title_span_id" style="color:#ff9955;display:none">标题不能为空</span>
						</td>
					</tr>
					<tr>
						<td>
							<label>推送内容</label><br>
							<textarea id="content_id" name="content" cols="200" rows="2" value="" onblur="checkBlankContent()" maxlength="100"></textarea>
							<span id="content_span_id" style="color:#ff9955;display:none">推送内容不能为空</span>
						</td>
					</tr>
			</table>
			
			<table class="table table-striped table-hover">	
					<tr>
						<td>
							<label>推送对象</label><br>
							<input type="radio" style="margin-left:0px" name="way" value="1">单个用户
							<input type="radio" style="margin-left:30px" name="way" value="2">全体用户
							<input type="radio" style="margin-left:30px" name="way" value="3">地区用户<br><br>
							<input id="ID_input_id" style="display:none" type="text" name="id" placeholder="输入用户ID" value="" />
							<select class="form-control" name="areaCode" id="ID_area" style="display:none">
								<#if areas??>
								<#list areas as a>
									<option value="${a.areaCode!}">${a.name!}</option>
								</#list>
								</#if>
							</select>
							
						</td>
					</tr>
			</table>
			<table class="table table-striped table-hover">	
					<tr>
						<td>
							<label >推送类型</label><br>
							<select name="category" id="category_id" class="form-control" style="width:250px">
							   <option value="" > 请选择</option>
							   <option value="1" > 系统消息</option>
							   <option value="2" > 资讯</option>
							   <option value="3" > 官方赛</option>
							   <option value="6" > 金币商城</option>
							   <option value="7" > 金币商城商品</option>
							   <option value="8" > 金币任务</option>
							   <option value="9" > 悬赏令</option>
							</select>
							<div id="moudle_1" style="display:none">
								<label>请选择一级模块:</label>
								<select class="form-control" name="moduleName"    onchange="setSubModule($(this).val());" >
									<option value="0" selected="selected">--请选择--</option>
									<#if modules??>
									<#list modules as m>
										<option value="${m.id!}">${m.name!}</option>
									</#list>
									</#if>
								</select>
							</div>
							<div  id="moudle_2" style="display:none">
								<label>请选择二级模块:</label>
								<select class="form-control" name="subModuleName" id="moudle_2_select"></select>
							</div>
							<div  id="autocomplete_amuse_div" style="display:none">
								<label>请输入要查询娱乐赛标题:</label>
								<input id="autocomplete_amuse" name="amuse_title" class="autocomplete" />
							</div>
							<div  id="autocomplete_activity_div" style="display:none">
								<label>请输入要查询官方赛标题:</label>
								<input id="autocomplete_activity" name="activity_title" class="autocomplete" />
							</div>
							<div  id="autocomplete_bounty_div" style="display:none">
								<label>请输入要查询悬赏令标题:</label>
								<input id="autocomplete_bounty" name="bounty_title" class="autocomplete" />
							</div>
							<div  id="autocomplete_information_div" style="display:none">
								<label>请输入要查询资讯标题:</label>
								<input id="autocomplete_information" name="information_title" class="autocomplete" />
								<table class="table table-striped table-hover" >	
										<tr>
											<td>
												<label>兴趣推送</label><br>
												<input type="radio" style="margin-left:0px" name="fav" value="1">是
												<input type="radio" style="margin-left:30px" name="fav" value="0">否
											</td>
										</tr>
								</table>
							</div>		
							<div  id="autocomplete_commodity_div" style="display:none"  >
								<label >展示区</label><br>
									<select name="commodity_type" class="form-control" id="commodity_type" style="width:250px" >
									   <option value="" > 请选择</option>
									   <option value="3" > 众筹</option>
									   <option value="1" > 兑奖专区</option>
									</select>
									<label >商品名</label><br>
									<input class="form-control" class="autocomplete" style="width:250px"  id="autocomplete_commodity" name="commodity_title" maxlength="50" />
								</div>
							<input id="hidden_amuse" name="hidden_amuse" type="hidden" />
							<input id="hidden_activity" name="hidden_activity" type="hidden" />
							<input id="hidden_infomation" name="hidden_infomation" type="hidden" />
							<input id="hidden_commodity" name="hidden_commodity" type="hidden" />	
							<input id="hidden_bounty" name="hidden_bounty" type="hidden" />	
						</td>
					</tr>
			</table>
			
			
			<table class="table table-striped table-hover">	
					<tr>
						<td>
							<!--<label>发送时间</label><br>-->
							<!--<input type="radio" name="now" value="0" checked="checked">立即-->
							<!--<select id="select_id" name="time" class="selector_type">
								<option value="0">--请选择--</option>
								<option value="1">每小时</option>
								<option value="24">每天</option>
								<option value="168">每周</option>
							</select>定时--><br><br>
							<button type="button" class="btn btn-info" onclick="submitEditor()">发送</button>
						</td>
					</tr>
			</table>
		</form>
	</div>
	
	<#-- js -->
	<script type="text/javascript">
	$('#autocomplete_amuse_div').hide();
	$('#autocomplete_activity_div').hide();
	$('#autocomplete_bounty_div').hide();
	$('#autocomplete_information_div').hide();
	$('#autocomplete_commodity_div').hide();
	<!-- 判断显示ID输入框 -->
	$(':radio[name="way"]').click(function(){
		var way = $('input[name="way"]:checked').val();
		if(way == 1 ){
			$('#ID_input_id').show();
			$('#ID_area').hide();
		}else if(way == 3 ){
			$('#ID_input_id').hide();
			$('#ID_area').show();
		}else {
			$('#ID_area').hide();
			$('#ID_input_id').hide();
		}
	});
	$('#category_id').on('change', function(){
		$('#autocomplete_amuse_div').hide();
		$('#autocomplete_activity_div').hide();
		$('#autocomplete_bounty_div').hide();
		$('#autocomplete_information_div').hide(); 
		$('#autocomplete_commodity_div').hide();
		$('#moudle_1').hide();
		$('#moudle_2').hide();
		var category = $('#category_id').val();
		if(category == 1 ){
			$('#moudle_1').hide();
			$('#moudle_2').hide();
		}else if(category == 4 ){
			$('#autocomplete_amuse_div').show();
			$('#moudle_1').hide();
			$('#moudle_2').hide();
		}else if(category == 3 ){
			$('#autocomplete_activity_div').show();
			$('#moudle_1').hide();
			$('#moudle_2').hide();
		}else if(category == 2 ){
			$('#autocomplete_information_div').show();
			$('#moudle_1').show();
			$('#moudle_2').show();
		}else if(category == 7 ){
			$('#autocomplete_commodity_div').show();  
		}else if(category == 9 ){
			$('#autocomplete_bounty_div').show();
			$('#moudle_1').hide();
			$('#moudle_2').hide();
		}
	});
	var _$submit = $('#submit_id');
	<!-- 检查输入框是否为空 -->
	function checkBlankTitle() {
		$('#title_span_id').hide();
		var title = $("#title_id").val();
		//去掉空格
		title = title.replace(/(^\s*)|(\s*$)/g,'')
		if(title == ""){
			$('#title_span_id').show();
		}
	}
	function checkBlankContent() {
		$('#content_span_id').hide();
		var content = $("#content_id").val();
		//去掉空格
		content = content.replace(/(^\s*)|(\s*$)/g,'')
		if(content == ""){
			$('#content_span_id').show();
		}
	}
	<!-- 提交表单 -->
	function submitEditor() {
		//TODO  根据类型判断相应数据不能为空
	
	var way = $('input[name="way"]:checked').val();
	if(!way){
		alert("请选择推送目标对象");
		return ;
	}
	
	var category = $('#category_id').val();
	if(!category){
		alert("请选择推送类型");
		return ;
	}
	
	
		_$submit.find('form').ajaxSubmit({
			url:'${ctx}/push/message',
			type:'post',
			success: function(d){
				if(d.code == 0) {
					alert(d.result);
					window.location="${ctx}/push/list/1";
				} else {
					alert(d.result);
				}
			}
		});
	}
	
	function setSubModule(id) {
		$.ajax({
			type : 'get',
			url : '${ctx}/push/subModules?moduleId='+id,
			cache : false,
			dataType : 'json',
			async: false,
			success : function(data) {
					$("#moudle_2_select").html("");
					for(var i in data.subModules) {
						var p = data.subModules[i];
						$("#moudle_2_select").append(option(p.id, p.name));
					}
			}
		});
	}
	function option(id, name, selected) {
		if(name) return '<option value="' + (id?id:'') + '"' + (selected?' selected':'') + '>' + (name?name:'') + '</option>';
	}
	</script>
	<script type="text/javascript">
		// 娱乐赛
		$('#autocomplete_amuse').autocomplete({
			ajaxSettings: {
				type: 'post',
			},
			serviceUrl: '/push/amuses',
			dataType: 'json',
			onSelect: function (suggestion) {
				$('#hidden_amuse').val(suggestion.data);
			},
			transformResult: function(res) {
				var objs = res.amuses;
				if(!objs || objs.length <= 0) {
					objs = [{'title': '无'}];
				}
				
				return {
					suggestions: $.map(objs, function(obj) {
						return {'value': obj.title, 'data': obj.id};
					})
				};
			}
		});
		
		// 官方赛
		$('#autocomplete_activity').autocomplete({
			ajaxSettings: {
				type: 'post',
			},
			serviceUrl: '/push/activities',
			dataType: 'json',
			onSelect: function (suggestion) {
				$('#hidden_activity').val(suggestion.data);
			},
			transformResult: function(res) {
				var objs = res.activities;
				if(!objs || objs.length <= 0) {
					objs = [{'title': '无'}];
				}
				
				return {
					suggestions: $.map(objs, function(obj) {
						return {'value': obj.title, 'data': obj.id};
					})
				};
			}
		});
		
		// 悬赏令
		$('#autocomplete_bounty').autocomplete({
			ajaxSettings: {
				type: 'post',
			},
			serviceUrl: '/push/bounties',
			dataType: 'json',
			onSelect: function (suggestion) {
				$('#hidden_bounty').val(suggestion.data);
			},
			transformResult: function(res) {
				var objs = res.bounties;
				if(!objs || objs.length <= 0) {
					objs = [{'title': '无'}];
				}
				
				return {
					suggestions: $.map(objs, function(obj) {
						return {'value': obj.title, 'data': obj.id};
					})
				};
			}
		});
		
		// 资讯
		$('#autocomplete_information').autocomplete({
			ajaxSettings: {
				type: 'post',
			},
			params:{
				subModuleId:$("#moudle_2_select").val()
			},
			serviceUrl: '/push/informations',
			dataType: 'json',
			onSelect: function (suggestion) {
				$('#hidden_infomation').val(suggestion.data);
			},
			transformResult: function(res) {
				var objs = res.informations;
				if(!objs || objs.length <= 0) {
				console.log($("#moudle_2_select").val());
					objs = [{'title': '无'}];
				}
				resetInformationOption();
				
				return {
					suggestions: $.map(objs, function(obj) {
						return {'value': obj.title, 'data': obj.id};
					})
				};
			}
		});
		
		$("#commodity_type").on('change',function(){
		    var type=$('#commodity_type').val();
		     $('#autocomplete_commodity').val('');
		     $('#hidden_commodity').val('');    
		     if(type==''){
		     }else{
		      $('#autocomplete_commodity').unbind();	   	
    	   	// 商品
			$('#autocomplete_commodity').autocomplete({
				ajaxSettings: {
					type: 'post',
				},
				params:{
					areaId:$("#commodity_type").val(),
				},
		
				serviceUrl: '/push/commoditys',
				dataType: 'json',
				onSelect: function (suggestion) {
					$('#hidden_commodity').val(suggestion.data);
				},
				transformResult: function(res) {
					var objs = res.commoditys;
					if(!objs || objs.length <= 0) {
						objs = [{'label': '无'}];
					}
					resetCommodityOption();
					return {
						suggestions: $.map(objs, function(obj) {
							return {'value': obj.label, 'data': obj.id};
						})
					};
				}
			});
		     }
		
		
		
		});
		
		function resetInformationOption(){
			$('#autocomplete_information').autocomplete('setOptions',{params:{
				subModuleId:$("#moudle_2_select").val()
			}});
		}
		function resetCommodityOption(){
			$('#autocomplete_commodity').autocomplete('setOptions',{params:{
				areaId:$("#commodity_type").val()
			}});
		}
	</script>
</body>
</html>