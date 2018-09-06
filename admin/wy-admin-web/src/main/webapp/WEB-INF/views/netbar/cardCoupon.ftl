<#import "/macros/pager.ftl" as p >
<html>
<head>
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
  			<strong>红包管理</strong>
 		</li>
		<li class="active">
			奖券管理
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
			<div class="col-md-2">
				<input type="text" class="form-control" name="name" placeholder="奖券名" value="${couponName!}" />
			</div>
			<div class="col-md-2">
				<select class="form-control" name="type">
				    <option value="">全部</option>
					<option value="1"<#if type?exists&&type=1> selected</#if>>奖券</option>
					<option value="2"<#if type?exists&&type=2> selected</#if>>实物</option>
					<option value="3"<#if type?exists&&type=3> selected</#if>>红包</option>
				</select>
			</div>
			<button type="submit" class="btn btn-success">查询</button>
		    <button add="" type="button" class="btn btn-success">新增</button>
		</form>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>ID</th>
			<th>奖券名</th>
			<th>奖券类型</th>
			<th>奖券金额</th>
			<th>抽中概率</th>
			<th>有效期</th>
			<th>参与网吧</th>
			<th>状态</th>
			<th>操作</th>
		</tr>
		<#list result as res>
			<tr>
			<td>${res.id!}</td>
			<td>${res.name}</td>
			<td><#if res.type=1>奖券<#elseif res.type=2>实物<#elseif res.type=3>红包</#if></td>	
			<td>
			${res.amount!}
			</td>
			<td>${res.probability!}</td>
			<td>${res.start_date?string("yyyy-MM-dd")}至${res.end_date?string("yyyy-MM-dd")}</td>
			<td>
			<button type="button" class="btn btn-default" data-toggle="tooltip" data-placement="top" data-original-title="${res.names!}">
					<#if (res.names)?? && res.names?length gt 15>
						${(res.names?substring(0,15))!}
					<#else>
						${res.names!}
					</#if>
			</button>
			</td>
			<td><#if res.state=0>新增<#elseif res.state=1>启用<#elseif res.state=2>禁用</#if></td>
			<td>
			<button state="${res.state!}" switch="${res.id!}" type="button" class="btn btn-info"><#if res.state=0 || res.state=2>启用<#elseif res.state=1>禁用</#if></button>
			<#if res.state=0><button del="${res.id!}" type="button" class="btn btn-info">删除</button></#if>
			</td>
			</tr>
		</#list>
	</table>
	
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
					<h4 class="modal-title">新增/编辑</h4>
				</div>
				<div class="modal-body">
				<form  class="form-horizontal form-condensed" role="form" method="post">
				        <input type="hidden" name="id" value="" />
				        <div class="form-group">
							<label class="col-md-2 control-label">奖券类型</label>
							<div class="col-md-10">
								<select id="type" name="type" onchange="showMinMoney();">
								<option value="1">奖券</option>
								<option value="2">实物</option>
								<option value="3">红包</option>
								</select>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">奖券名</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="name" wy-required="奖券名">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">使用网吧</label>
							<div class="col-md-10">
								<button id="input-areas-netbars" class="btn btn-large btn-block btn-primary" type="button">设置网吧及地点</button>
								<input id="hidden-netbars" type="hidden" name="netbars" value="" />
								<input id="hidden-areas" type="hidden" name="areas" value="" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">奖券金额</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="amount" wy-required="奖券金额" maxlength="5">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">抽中概率%</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="probability" id="probability" wy-required="抽中概率">
							</div>
						</div>
						<div class="form-group" id="minMoney">
							<label class="col-md-2 control-label">满多少可用</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="minMoney">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">有效期</label>
							<div class="col-md-10">
							<input class="form-control" type="text" id="edit-startTime" name="start_date" placeholder="有效期起始"  onclick="WdatePicker({minDate:'%y-%M-%d',isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',maxDate:'#F{$dp.$D(\'edit-endTime\',{d:0});}'})" />
							<input class="form-control" type="text" id="edit-endTime" name="end_date" placeholder="有效期截止" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',minDate:'#F{$dp.$D(\'edit-startTime\',{d:0});}'})" />
							<div id="dateDiv"></div>
							</div>
						</div>
						<div class="form-group" id="minMoney">
							<label class="col-md-2 control-label">兑奖次数</label>
							<div class="col-md-10">
							<input class="form-control" type="text" value="仅限一次"  readonly="readonly">
							</div>
						</div>
					</form>
				</div>
					
				<div class="modal-footer">
					<button type="button" class="btn btn-default" onclick="hideEditor()">关闭</button>
					<button type="button" class="btn btn-primary" onclick="submitEditor()">保存</button>
				</div>
			</div>
		</div>
	</div>
	
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
	$('button[data-toggle="tooltip"]').tooltip();
	
	function showMinMoney(){
	if($("#type").val()==3)
	$("#minMoney").show();
	else
	$("#minMoney").hide();
	if($("#type").val()==2){
	var now = new Date();
    var nowStr = now.format("yyyy-MM-dd"); 
    $("#edit-startTime").val(nowStr);
    $("#edit-endTime").val(nowStr);
    $("#dateDiv").html("默认仅限当天使用");
	}
	else{
	$("#edit-startTime").val("");
    $("#edit-endTime").val("");
    $("#dateDiv").html("");
	}
	}
	
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
	
	// 显示新增
	$('button[add]').on('click', function(event) {
	$("#dateDiv").html("");
	 $("#type").removeAttr("disabled");
		$("#minMoney").hide();
		$.fillForm({
			}, _$editor);
		_$editor.modal('show');
	});
	
	// 显示模态框
	$('button[edit]').on('click', function(event) {
	    $("#type").attr("disabled","disabled");
		// 初始化模态框宽度
		var width = 1200;
		if(width > $(window).width()) {
			width = $(window).width();
		}
		_$editor.find('.modal-dialog').css('width', width);
		
		// 初始化表单数据
		var id = $(this).attr('edit');
		$.api('${ctx}/card/coupon/detail/' + id, {}, function(d) {
		var o=d.object;
		if(o.type==3)
		$("#minMoney").show();
		else
		$("#minMoney").hide();
		initCheckedArea("330100");
			$.fillForm({
				type: o.type,
				name: o.name,
				amount: o.amount,
				probability: o.probability,
				minMoney: o.min_money,
				start_date:o.start_date,
				end_date:o.end_date,
				netbars: o.netbar_ids,
				areas: o.area_codes,
				id: o.id,
			}, _$editor);
		}, function(d) {
			alert('请求数据异常：' + d.result);
		}, {
			async: false,
		});
		_$editor.modal('show');
	});
	
	// 提交表单
	function submitEditor() {
	var form=_$editor.find('form');
	var valid = form.formValid();
	if(valid){
		form.ajaxSubmit({
			url:'${ctx}/card/coupon/save',
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
	}
	
	$('button[switch]').on('click', function(event) {
		var _this = $(this);
		_this.attr('disabled', true);
		
		var id = $(this).attr('switch');
		var str;
		var state=$(this).attr('state');
		if(state==0||state==2)
		 str="启用";
		else if(state=1)
		  str="禁用";
		
		$.confirm('确认'+str+'吗?', function() {
			$.api('${ctx}/card/coupon/switch/' + id, {}, function(d) {
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
	
	// 删除
	$('button[del]').on('click', function(event) {
		var _this = $(this);
		_this.attr('disabled', true);
		
		var id = $(this).attr('del');
		
		$.confirm('确认删除吗?', function() {
			$.api('${ctx}/card/coupon/del/' + id, {}, function(d) {
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
		var $tr = $('#input-areas-netbars').parent();
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
		var $input = $('#input-areas-netbars');
		$input.off('click');
		$input.on('click', function(event) {
			_netbars = [];
			_areas = [];
			
			var $win = $('#win');
			showEditRoundModal();
			_roundNum = parseInt($(this).parents('[' + _roundDomAttr + ']').attr('' + _roundDomAttr + ''));
			var $roundData = $(this).parent();
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
				var $roundData = $(this).parent();
				
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
		url: '${ctx!}/area/tree?areaCodes=',
		success: function(d) {
			if(d.result) {
				var result = [];
				// 产生省节点
				var provinces = d.object;
				for(var i = 0; i<provinces.length; i++) {
					var province = provinces[i];
					// 产生市节点
					var cities = [];
					if(province.children!=undefined){
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
	initTree();
	
	
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
	
	var text = document.getElementById("probability");
text.onkeyup = function(){
this.value=this.value.replace(/\D/g,'');
if(text.value>100){
  text.value = 100;
}
}
	</script>
</body>
</html>