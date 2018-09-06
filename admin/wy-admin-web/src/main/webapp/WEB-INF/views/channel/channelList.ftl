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
  			<strong>渠道管理</strong>
 		</li>
		<li class="active">
			渠道列表
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
		<div class="mb10">
			<input type="hidden" name="pid" value="${pid!}" />
			<div class="col-md-2">
				<input type="text" name="name" value="${(params.name)!}" class="form-control" placeholder="输入渠道名">
			</div>
			<div class="col-md-2">
				<input type="text" name="beginTime" value="${(params.beginTime)!}" class="form-control" placeholder="点击时间(起)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss',onpicked:beginPick})">
			</div>
			<div class="col-md-2">
				<input type="text" name="endTime" value="${(params.endTime)!}" class="form-control" placeholder="点击时间(止)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss',onpicked:endPick})">
			</div>
			<div class="col-md-6">
				<button type="submit" class="btn btn-success">筛选</button>
				<a class="btn btn-info" href="1">清空</a>
			</div>
		</div>
		<div class="mb10">
			<div class="col-md-8">
				<input type="hidden" name="deviceType" value="${(params.deviceType)!}" />
				<div class="btn-group" data-toggle="buttons-checkbox" style="margin: 10px 0;">
					<button deviceType="" type="button" class="btn <#if (params.deviceType!) == "">btn-danger<#else>btn-info</#if>">全部</button>
					<button deviceType="2" type="button" class="btn <#if (params.deviceType!) == "2">btn-danger<#else>btn-info</#if>">IOS</button>
					<button deviceType="1" type="button" class="btn <#if (params.deviceType!) == "1">btn-danger<#else>btn-info</#if>">安卓</button>
				</div>
				<select class="form-control" name="symbiosis" style="display: inline-block; width: 200px;margin: 0 10px;"></select>
				<input type="hidden" name="valid" value="${(params.valid)!}" />
				<div class="btn-group" data-toggle="buttons-checkbox">
					<button valid="" type="button" class="btn <#if (params.valid!) == "">btn-danger<#else>btn-info</#if>">全部</button>
					<button valid="1" type="button" class="btn <#if (params.valid!) == "1">btn-danger<#else>btn-info</#if>">进行中</button>
					<button valid="0" type="button" class="btn <#if (params.valid!) == "0">btn-danger<#else>btn-info</#if>">已关闭</button>
				</div>
			</div>
		</div>
		<script type="text/javascript">
			$('button[valid], button[deviceType]').on('click', function(event) {
				var $form = $('#search');
				var attrName = 'unknow';
				if(typeof($(this).attr('valid')) !== 'undefined') {
					attrName = 'valid';
				} else {
					attrName = 'deviceType';
				}
				$form.find('input[name="' + attrName + '"]').val($(this).attr(attrName));
				$form.submit();
			});
		</script>
		</form>
	</div>
	<div class="mb10">
		<div class="col-md-8">
			<button add="" type="button" class="btn btn-success">新增渠道</button>
		</div>
		<div class="col-md-8">
			<div id="div-copy" style="padding:10px;margin: 5px;border: 1px solid #CCC;border-radius:5px;display:inline-block;">请点击<b class="prompt" style="padding: 0;">查看地址</b>按钮</div><button id="btn-copy" type="button" class="btn btn-success" disabled>复制</button>
		</div>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>渠道代码</th>
			<th>渠道名</th>
			<th>跳转地址</th>
			<th>时间段点击量</th>
			<th>合作类型</th>
			<th>总数量</th>
			<th>设备</th>
			<th>状态</th>
			<th>操作</th>
		</tr>
		<#if list??>
		<#list list as o>
			<tr>
				<td>${(o.code)!} <button id="copy-btn-${(o.id)!}" class="btn btn-info" onclick="copy('${(o.code)!}', '${(o.url)!}')">查看地址</button></td>
				<td>${(o.name)!}</td>
				<td>${(o.url)!}</td>
				<td>${(o.timeClickCount)!}</td>
				<td>${(o.symbiosis)!}</td>
				<td>${(o.clickCount)!}</td>
				<td>${((o.deviceType == 1)?string('安卓','IOS'))!}</td>
				<td>${((o.valid == 1)?string('进行中','已关闭'))!}</td>
				<td>
					<#if (o.valid)?? && o.valid == 1>
						<button type="button" class="btn btn-info" onclick="enabled(${(o.id)!}, 0, this)">关闭</button>
					<#else>
						<button type="button" class="btn btn-info" onclick="enabled(${(o.id)!}, 1, this)">开启</button>
					</#if>
				</td>
			</tr>
		</#list>
		</#if>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	
	<#-- 编辑及新增 -->
	<div id="modal-editor" class="modal fade">
		<div class="modal-dialog" style="width:700px;">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title">标题</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post" enctype="multipart/form-data">
						<input type="hidden" name="id" />
						<input type="hidden" name="pId" />
						<div class="form-group">
							<label class="col-md-2 control-label">渠道名</label>
							<div class="col-md-10">
								<input type="text" class="form-control" name="name" wy-required="渠道名" maxlength="12" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">渠道代码</label>
							<div class="col-md-10">
								<input type="text" class="form-control" name="code" wy-required="渠道代码" maxlength="12" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">设备类型</label>
							<div class="col-md-10">
								<label><input type="radio" name="deviceType" value="1" /> 安卓</label>
								<label><input type="radio" name="deviceType" value="2" /> IOS</label>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">合作类型</label>
							<div class="col-md-10">
								<select name="symbiosis" class="form-control">
								</select>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">跳转地址</label>
							<div class="col-md-10">
								<input type="text" class="form-control" name="url" wy-required="跳转地址" maxlength="255" />
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
					<button id="submit" type="button" class="btn btn-primary">保存</button>
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
	
	// 打开模态框
	function showEditor(title) {
		_$editor.find('.modal-title').html(title);
		_$editor.modal('show');
	}
	
	// 关闭模态框
	function hideEditor() {
		_$editor.modal('hide');
	}
	
	// 新增资讯
	$('button[add]').on('click', function(event) {
		// 初始化表单数据
		$.fillForm({
			deviceType: 1,
			url: 'http://',
		}, _$editor);
		showEditor('新增渠道');
	});
	
	// 显示模态框，编辑
	$('button[edit]').on('click', function(event) {
		// 初始化表单数据
		var id = $(this).attr('edit');
		$.api('${ctx}/activityInformation/subject/info/detail', {id: id}, function(d) {
			var o = d.object;
			$.fillForm({
				id: o.id,
			}, _$editor);
			
			// 初始化编辑器内容
			setEditorText($('#editor-detail'), o.remark);
			
			showEditor('编辑渠道');
		}, false, {
			async: false,
		});
	});
	
	// 提交表单
	_$editor.find('#submit').on('click', function(event) {
		var $form = _$editor.find('form');
		var valid = $form.formValid();
		if(valid) {
			var $this = $(this);
			$this.prop('disabled', true);
			$form.ajaxSubmit({
				url:'${ctx}/channel/save',
			    type:'post',
			    success: function(d){
			    	if(d.code == 0) {
			    		window.location.reload();
			    	} else {
			    		alert(d.result);
			    	}
				},
				complete: function() {
					$this.prop('disabled', false);
				}
			});
		}
	});
	
	// 删除
	$('button[remove]').on('click', function(event) {
		var id = $(this).attr('remove');
		$.confirm('确认删除吗?', function() {
			$.api('${ctx}/activityInformation/common/delete/' + id, {}, function(d) {
				if(d.code == 0) {
					window.location.reload();
				} else {
					alert(d.result);
				}
			});
		});
	});
	
	// 启用或禁用
	function enabled(id, valid, dom) {
		var $this = $(dom);
		$this.prop('disabled', true);
		$.api('${ctx}/channel/enabled', {id: id, valid: valid}, function(d) {
			window.location.reload();
		}, false, {
			complete: function() {
				$this.prop('disabled', false);
			}
		});
	}
	</script>

	<!-- 初始化合作类型 -->
	<script type="text/javascript">
	var symbiosis = ['test', 'test2'];
	var searchSymbiosis = '${(params.symbiosis)!}'; 
	$(function() {
		function option(value, html, defaultValue) {
			return '<option value="' + value + '"' + (value == defaultValue ? ' selected' : '') + '>' + html + '</option>';
		}
		
		var $searchSymbiosis = $('#search select[name="symbiosis"]');
		$searchSymbiosis.html(option('', '全部'));
		var $editorSymbiosis = _$editor.find('select[name="symbiosis"]');
		$editorSymbiosis.html('');

		for(var i=0; i<symbiosis.length; i++) {
			var s = symbiosis[i];
			$searchSymbiosis.append(option(s, s, searchSymbiosis));
			$editorSymbiosis.append(option(s, s));
		}
	});
	</script>
	
	<!-- 访问剪切板 -->
	<script type="text/javascript">
	var clickUrl = '${(channelRecordUrl)!'http://api.wangyuhudong.com/channel/record'}';
	ZeroClipboard.setMoviePath('${ctx}/static/plugin/clipboard/ZeroClipboard.swf');
	var clip = new ZeroClipboard.Client($('#btn-copy').get(0));
	clip.setHandCursor(true);
	function copy(code, redirect) {
		var text = clickUrl + '?code=' + code + '&wyRedirect=' + encodeURIComponent(redirect);
		$('#div-copy').html(text);
		clip.setText(text);
		clip.reposition();
		$('#btn-copy').prop('disabled', false);
	}
	</script>
</body>
</html>