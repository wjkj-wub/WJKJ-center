<#import "/macros/pager.ftl" as p >
<html>
<head>
	<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
	<script type="text/javascript" editor-ctx="${ctx!}" src="${ctx!}/static/plugin/editor.js"></script>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
			<strong>赛事管理</strong>
		</li>
		<li class="active">
			悬赏令列表
		</li>
	</ul>
	<div class="col-md-10">
		<ul class="nav nav-tabs">
			<#if itemId == 1>
			  <li class="active"><a href="/bounty/list/1?itemId=1">英雄联盟</a></li>
			  <li><a href="/bounty/list/1?itemId=3">王者荣耀</a></li>
			<#elseif itemId == 3>
			  <li><a href="/bounty/list/1?itemId=1">英雄联盟</a></li>
			  <li class="active"><a href="/bounty/list/1?itemId=3">王者荣耀</a></li>
			  
			</#if>
		</ul>
	</div>
	<div class="col-md-2">
		<button type="button" class="btn btn-info" onclick="editRule(${itemId!})">编辑玩法说明</button>
		<button type="button" class="btn btn-info" onclick="javascript:void(window.location.href='/bounty/edit?itemId=${itemId!}')">新增下一期</button>
	</div>
	<table class="table table-striped table-hover">	
		<tr>
			<th>id</th>
			<th>期数</th>
			<th>开始时间</th>
			<th>结束时间</th>
			<th>奖金</th>
			<th>悬赏条件</th>
			<th>实际参与</th>
			<th>当前状态</th>
			<th>操作</th>
		</tr>
		<#if list??>
			<#list list as o>
			<tr>
				<td>${(o.id)!}</td>
				<td>${(o.time)!}</td>
				<td>${(o.startTime?string('yyyy-MM-dd HH:mm:ss'))!}</td>
				<td>${(o.endTime?string('yyyy-MM-dd HH:mm:ss'))!}</td>
				<td>${o.reward!""}</td>
				<td>${o.target!""}</td>
				<td>${o.applyNum!""}</td>
				<td>
					<#if (o.status)??>
						<#if o.status == 1>
							进行中
						<#elseif o.status == 2>
							待审核
						<#elseif o.status == 3>
							审核完成
						<#elseif o.status == 0>
							待进行
						</#if>
					</#if>
				</td>
				<td>
					<#if ((o.status)!0) == 1>
						<a type="button" class="btn btn-info" href="${ctx}/bounty/prizeCheck/list/${(o.id)!}/1">审核</a>
					</#if>
					<#if ((o.status)!0) == 2>
						<a type="button" class="btn btn-info" href="${ctx}/bounty/prizeCheck/list/${(o.id)!}/1">审核</a>
					</#if>
					<#if ((o.status)!0) == 3>
						<a type="button" class="btn btn-info" href="${ctx}/bounty/prizeCheck/list/${(o.id)!}/1">审核</a>
					</#if>
					<#if ((o.status)!0) == 0>
						<a type="button" class="btn btn-info" href="${ctx}/bounty/edit?id=${(o.id)!}&itemId=${itemId!}">编辑</a>
					</#if>
				</td>
			</tr>
			</#list>
		</#if>
	</table>
	<div id="modal-editor" class="modal fade">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideEditor()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title">编辑悬赏令规则</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form_infom" method="post">
						<div class="form-group">
							<label class="col-md-2 control-label">规则说明</label>
							<div class="col-md-10">
								<textarea id="editor-detail" name="rule" placeholder="Balabala" autofocus></textarea>
								<script type="text/javascript">
									_editor = editor($('#editor-detail'));
								</script>
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" onclick="hideEditor()">关闭</button>
					<button id="save-rule" type="button" class="btn btn-primary">保存</button>
				</div>
			</div>
		</div>
	</div>
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage /> 
<script>
	// 初始化模态框
	var _$editor = $('#modal-editor');
	var _$editor_addInfom = $('#modal-addInfom');
	var $form = _$editor.find('form');
	_$editor.modal({
		keyboard : false,
		show : false
	})
	$.fillForm({
		rule: '${rule!""}'
	}, _$editor);
	
	// 关闭模态框
	function hideEditor() {
		_$editor.modal('hide');
		_$editor_addInfom.modal('hide');
	}
		// 初始化编辑窗口宽度
	function showEditor() {
		var width = 1000;
		var height = $(window).height() - 400;
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
	function editRule(itemId){
		showEditor();
	}
	$("#save-rule").click(function(){
		$form.ajaxSubmit({
			url:'${ctx}/bounty/rule?itemId=${itemId!}',
		    type:'post',
		    success: function(d){
		    	if(d.code == 0) {
		    		alert("保存成功!");
		    		window.location.reload();
		    	} else {
		    		alert(d.result);
		    	}
			},
		});
	});
</script>
<script type="text/javascript">
	function go(page) {
		var $search = $('#search');
		if($search.length > 0) {
			$search.attr('action', page);
			$search.submit();
		} else {
			window.location.href = page+"?itemId="+${itemId!};
		}
	}
</script>
</body>
</html>