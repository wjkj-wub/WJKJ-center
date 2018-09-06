<#import "/macros/pager.ftl" as p >
<html>
<head>
<link rel="stylesheet" href="${ctx}/static/lib/chosen/chosen.css" />
<script type="text/javascript" src="${ctx}/static/lib/chosen/chosen.min.js"></script>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>管理系统</strong>
 		</li>
		<li class="active">
			首页广告管理
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post" style="display:inline;">
			<div class="col-md-2">
				<input type="text" class="form-control" name="title" placeholder="广告标题" value="${(params.title)!}" />
			</div>
			<div class="col-md-2">
				<select class="form-control" name="areaCode">
					<option value="">--地区--</option>
					<option value="000000" <#if ((params.areaCode)!"") == '000000'> selected</#if> >全国</option>
					<#if provinceList??>
						<#list provinceList as p>
							<option value="${p.areaCode!}"<#if (params.areaCode!) == p.areaCode!> selected</#if>>${p.name!}</option>
						</#list>
					</#if>
				</select> 
			</div>
			<button type="submit" class="btn btn-success">查询</button>
			<button add="" type="button" class="btn btn-info" style="margin-right:50px;">新增</button>
		</form>
		<button type="submit" class="btn <#if (((params.valid)!1)?number)!=1>btn-success</#if>" onclick="xianshi(1)">发布中</button>
		<button type="submit" class="btn <#if (((params.valid)!1)?number)!=0>btn-success</#if>" onclick="xianshi(0)">未发布</button>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>图标</th>
			<th>标题</th>
			<th>描述</th>
			<th>类型</th>
			<th>网址</th>
			<th>设备</th>
			<th>操作</th>
		</tr>
		<#list list as o>
			<tr>
				<td><img src="${imgServer!}/${o.img!}" style="width: 50px; height: 50px;" /></td>
				<td>${o.title!}</td>
				<td>${o.describe!}</td>
				<#assign temp=(o.type!0)>
				<#if 1 == temp>
					<td>网吧</td>
				<#elseif 2 == temp>
					<td>手游</td>
				<#elseif 3 == temp>
					<td>赛事</td>
				<#else>
					<td>其它</td>
				</#if>
				
				<#if 1 == temp>
					<td>${o.url!}</td>
				<#elseif 2 == temp>
					<td><a href="${ctx}/gameInfo/detail/${o.targetId!}">${o.url!}</a></td>
				<#elseif 3 == temp>
					<td><a href="${ctx}/activityInfo/url/${o.targetId!}">${o.url!}</a></td>
				<#else>
					<td><a href="${o.url!}">${o.url!}</a></td>
				</#if>
				
				<#assign temp=(o.deviceType!0)>
				<#if 1 == temp>
					<td>IOS</td>
				<#elseif 2 == temp>
					<td>Android</td>
				<#else>
					<td>全部</td>
				</#if>
				<td>
					<button edit="${o.id!}" title="${o.title!}" describe="${o.describe!}" type="${o.type!}" target="${o.targetId!}" url="${o.url!}" deviceType="${o.deviceType!}" type="button" class="btn btn-info">编辑</button>
					<#if ((o.valid?number)!0) == 1>
					<button remove="${o.id!}" type="button" class="btn btn-danger">取消发布</button>
					<#else>
					<button enabled="${o.id!}" type="button" class="btn btn-danger">恢复发布</button>
					</#if>
					<form id="${o.id!}" style="margin:0px;display: inline" action="/indexAdvertiseArea/list/1" method="post">
						<input type="hidden" name="adId" value="${o.id!}"/>
						<input type="hidden" name="title" value="${o.title!}"/>
						<button type="submit" class="btn btn-success">地区管理</button>
					</form>
				</td>
			</tr>
		</#list>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	
	<#-- 编辑，新增 -->
	<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
	<div id="modal-editor" class="modal fade">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideEditor()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 id="bianji_h4id" class="modal-title" style="display:none;">编辑广告</h4>
					<h4 id="xinzeng_h4id" class="modal-title" style="display:none;">录入广告</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post">
						<input type="hidden" name="id" />
						<div class="form-group">
							<label class="col-md-2 control-label">标题<span id="span_id_1" style="color:red;">*</span></label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="title" value="">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">描述<span id="span_id_2" style="color:red;">*</span></label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="describe" value="">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">图标<span id="span_id_3" style="color:red;">*</span></label>
							<div class="col-md-10">
								<input type="file" name="imgFile" value="图片">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">类型<span id="span_id_4" style="color:red;">*</span></label>
							<div class="col-md-10">
								<select id="select_id" name="type" class="selector_type form-control">
									<option value="">--请选择--</option>
									<option value="1">网吧</option>
									<option value="2">手游</option>
									<option value="3">赛事</option>
									<option value="4">网娱官方活动(H5)</option>
									<option value="5">广告</option>
								</select>
							</div>
						</div>
						<div class="form-group" id="div_id_targetId">
							<label class="col-md-2 control-label">目标对象<span id="span_id_7" style="color:red;">*</span></label>
							<div class="col-md-10" id="netbardiv_id" style="display:none;">
								<select id="netbar_select_id" name="targetIdNetbar" class="selector_target chosen-select form-control" data-placeholder="选择一个宠物...">
									<option value="">--请选择--</option>
									<#if netbarList??>
										<#list netbarList as netbar>
											<option value="${netbar.id}">${netbar.netbarName}</option>
										</#list>
									</#if>
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
										$container.css('width', '100%').find('.chosen-single span').attr('title', selectedNetbarName).html(selectedNetbarName);
									}
								</script>
							</div>
							<div class="col-md-10" id="gamediv_id" style="display:none;">
								<select id="game_select_id" name="targetIdGame" class="selector_target form-control">
									<option value="">--请选择--</option>
									<#if gameList??>
										<#list gameList as game>
											<option value="${game.id}">${game.gameName}</option>
										</#list>
									</#if>
								</select>
							</div>
							<div class="col-md-10" id="activitydiv_id" style="display:none;">
								<select id="activity_select_id" name="targetIdActivity" class="selector_target form-control">
									<option value="">--请选择--</option>
									<#if activityList??>
										<#list activityList as activity>
											<option value="${activity.id}">${activity.title}</option>
										</#list>
									</#if>
								</select>
							</div>
						</div>
						<div class="form-group" id="div_id_url">
							<label class="col-md-2 control-label">URL<span id="span_id_5" style="color:red;">*</span></label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="url" value="">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">设备类型<span id="span_id_6" style="color:red;">*</span></label>
							<div class="col-md-10">
								<select id="select_id_deviceType" name="deviceType" class="selector_deviceType form-control">
									<option value="">--请选择--</option>
									<option value="0">全部</option>
									<option value="1">IOS</option>
									<option value="2">Android</option>
								</select>
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" onclick="hideEditor()">关闭</button>
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
	
	// 初始化表单数据
	function fillForm(columns) {
		for(var k in columns) {
			var $field = _$editor.find('[name="' + k + '"]');
			$field.val(columns[k]);
		}
	}
	
	// 类型输入转下拉框
	function changeType(){
		$('#typetext_id').hide();
		$('#typeselect_id').show();
	}
	
	// 显示模态框，编辑
	$('button[edit]').on('click', function(event) {
		$('#xinzeng_h4id,#typeselect_id,#span_id_1,#span_id_2,#span_id_3,#span_id_4,#span_id_5,#span_id_6,#span_id_7').hide();
		$('#bianji_h4id,#typetext_id').show();
		var id = $(this).attr('edit');
		var title = $(this).attr('title');
		var describe = $(this).attr('describe');
		var type = $(this).attr('type');
		var target = $(this).attr('target');
		var url = $(this).attr('url');
		var deviceType = $(this).attr('deviceType');
		if(type == 1){
			$('#div_id_url,#gamediv_id,#activitydiv_id').hide();
			$('#netbardiv_id,#div_id_targetId').show();
		}else if(type == 2){
			$('#div_id_url,#netbardiv_id,#activitydiv_id').hide();
			$('#gamediv_id,#div_id_targetId').show();
		}else if(type == 3){
			$('#div_id_url,#netbardiv_id,#gamediv_id').hide();
			$('#activitydiv_id,#div_id_targetId').show();
		}else{
			$('#div_id_targetId').hide();
			$('#div_id_url').show();
		}
		<!--根据值选中下拉框-->
		$(".selector_type").val(type);
		$(".selector_target").val(target);
		$(".selector_deviceType").val(deviceType);
		
		fillForm({
			title: title,
			describe: describe,
			url: url,
			id: id
		});
		
		if(type == '1') {
			initNetbar();
		}
		
		_$editor.modal('show');
	});
	
	// 显示模态框，新增
	$('button[add]').on('click', function(event) {
		var $form = _$editor.find('form');
		$form.get(0).reset();
		$form.find('input[type="hidden"][name]').val('');
		fillForm({
			id:'',
			title: '',
			describe: '',
			url: 'http://'
		});
		$(".selector_type").val();
		$(".selector_target").val();
		$(".selector_deviceType").val();
		
		$('#bianji_h4id,#div_id_targetId').hide();
		$('#xinzeng_h4id,#div_id_url,#span_id_1,#span_id_2,#span_id_3,#span_id_4,#span_id_5,#span_id_6,#span_id_7').show();
		_$editor.modal('show');
	});
	
	// 判断显示（网吧列表，手游列表，赛事列表）
	$('#select_id').on('change',function(){
		if($(this).val() == 1){
			$('#div_id_url,#gamediv_id,#activitydiv_id').hide();
			$('#netbardiv_id,#div_id_targetId').show();
			initNetbar();
		}else if($(this).val() == 2){
			$('#div_id_url,#netbardiv_id,#activitydiv_id').hide();
			$('#gamediv_id,#div_id_targetId').show();
		}else if($(this).val() == 3){
			$('#div_id_url,#netbardiv_id,#gamediv_id').hide();
			$('#activitydiv_id,#div_id_targetId').show();
		}else {
			$('#div_id_targetId').hide();
			$('#div_id_url').show();
			fillForm({
				targetId: ''
			});
		}
	});
	
	// 下拉框选中后赋值给targetId
	$('#netbardiv_id').on('change',function(){
		var temp = $('#netbar_select_id').val();
		fillForm({
			targetId: temp
		});
	});
	$('#gamediv_id').on('change',function(){
		var temp = $('#game_select_id').val();
		fillForm({
			targetId: temp
		});
	});
	$('#activitydiv_id').on('change',function(){
		var temp = $('#activity_select_id').val();
		fillForm({
			targetId: temp
		});
	});
	
	// 点击url跳转显示
	$('#urlId').on('click', function(event) {
		
		var id = $(this).attr('targetId');
		$.api('${ctx}/gameInfo/detail/' + id, {},function(d) {
			alert('请求数据异常：' + d.result);
		}, {
			async: false,
		});
	});
	
	// 提交表单
	function submitEditor() {
		_$editor.find('form').ajaxSubmit({
			url:'${ctx}/indexAdvertise/save',
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
			$.api('${ctx}/indexAdvertise/delete/' + id, {}, function(d) {
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
	
	// 恢复
	$('button[enabled]').on('click', function(event) {
		var _this = $(this);
		_this.prop('disabled', true);
		
		var id = _this.attr('enabled');
		$.confirm('确认启用吗?', function() {
			$.api('${ctx}/indexAdvertise/recover/' + id, {}, function(d) {
				window.location.reload();
			}, function(d) {
				alert(d.result);
			}, {
				complete: function() {
					_this.prop('disabled', false);
				}
			});
		}, undefined, {
			complete: function() {
				_this.prop('disabled', false);
			}
		})
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
	
	function xianshi(valid){
		window.location.href="${ctx}/indexAdvertise/list/1?valid="+valid;
	}
	</script>
</body>
</html>