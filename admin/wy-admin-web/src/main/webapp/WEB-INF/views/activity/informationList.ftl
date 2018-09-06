<#import "/macros/pager.ftl" as p >
<html>
<head>
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/simditor/css/simditor.css">
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/simditor/css/font-awesome.css">
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/simditor/css/simditor.scss">
<script type="text/javascript" editor-ctx="${ctx!}" src="${ctx!}/static/plugin/simditor/editor.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/module.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/hotkeys.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/uploader.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/simditor.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/ztree/js/jquery.ztree.all-3.5.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/ztree/js/jquery.ztree.exhide-3.5.min.js"></script>
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
	.prompt {
		height: 30px;
		padding: 0 10px;
		line-height: 30px;
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
			${topShow!}
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post" style="margin:0px;display: inline">
			<div class="col-md-2">
				<input type="text" class="form-control" name="title" placeholder="标题" value="${(params.name)!}" />
			</div>
			<div class="col-md-2">
				<select name="isSubject" class="form-control">
					<option value="" <#if params.isSubject??>selected</#if>>全部</option>
					<option value="0" <#if (params.isSubject!-1)?number==0>selected</#if>>资讯</option>
					<option value="1" <#if (params.isSubject!-1)?number==1>selected</#if>>专题</option>
				</select>
			</div>
			<button type="submit" class="btn btn-success">查询</button>
		</form>
		<#if params.pid??>
			<button activityId="${params.activityId!}" netbarId="${params.netbarId!}" pid="${params.pid!}" type="button" class="btn btn-info">添加旗下资讯</button>
		<#else>
			<button addInfo="" type="button" class="btn btn-info">新增资讯</button>
		</#if>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>图标</th>
			<th>专题</th>
			<th>资讯</th>
			<th>简介</th>
			<th>是否为专题</th>
			<th>是否热门</th>
			<th>是否在APP显示</th>
			<th>赛事</th>
			<th>开始时间</th>
			<th>结束时间</th>
			<th>操作</th>
		</tr>
		<#list list as o>
			<tr>
				<td><img src="${imgServer!}/${o.icon!}" style=" height: 50px;" /></td>
				<#if o.isSubject??><#assign subject=o.isSubject!></#if>
				<#if 1==subject!>
					<td>${o.title!}</td>
					<td>
						<form id="${o.id!}" style="margin:0px;display: inline" action="1" method="post">
							<input type="hidden" name="pid" value="${o.id!}"/>
							<input type="hidden" name="pName" value="${o.title!}"/>
							<input type="hidden" name="netbarId" value="${o.netbarId!}"/>
							<input type="hidden" name="activityId" value="${o.activityId!}"/>
							<button type="submit">旗下资讯</button>
						</form>
					</td>
				<#else>
					<td></td>
					<td>${o.title!}</td>
				</#if>
				<td>${o.brief!}</td>
				<#if o.isSubject??>
					<#if 1 == o.isSubject?int>
						<td>是</td>
					<#else>
						<td>否</td>
					</#if>
				<#else>
					<td></td>
				</#if>
				<#if o.isHot??>
					<#if 1 == o.isHot?int>
						<td>是</td>
					<#else>
						<td>否</td>
					</#if>
				<#else>
					<td></td>
				</#if>
				<#if o.isShow??>
					<#if 1 == o.isShow?int>
						<td>是</td>
					<#else>
						<td>否</td>
					</#if>
				<#else>
					<td></td>
				</#if>
				<td>${o.activityTitle!}</td>
				<td>${o.beginTime!}</td>
				<td>${o.overTime!}</td>
				<td>
					<button edit="${o.id!}" title="${o.title!}" brief="${o.brief!}" remark="" isSubject="${o.isSubject!}" isHot="${o.isHot!}" isShow="${o.isShow!}" beginTime="${o.beginTime!}" overTime="${o.overTime!}" type="button" class="btn btn-info">编辑</button>
					<button remove="${o.id!}" type="button" class="btn btn-danger">删除</button>
				</td>
			</tr>
		</#list>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	
	<#-- 编辑 -->
	<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
	<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
	<div id="modal-editor" class="modal fade">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideEditor()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title">编辑赛事资讯</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post">
						<input type="hidden" name="id" />
						<input type="hidden" name="pid" />
						<input type="hidden" name="netbarId" />
						<input type="hidden" name="activityId" />
						<div class="form-group">
							<label class="col-md-2 control-label">标题</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="title" value="" maxlength="50" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">简介</label>
							<div class="col-md-10">
								<textarea id="remark_textarea_id" class="form-control" name="brief" cols="74" rows="3" value=""></textarea>
							</div>
						</div>
						<div id="remark_div_id" class="form-group">
							<label class="col-md-2 control-label">说明</label>
							<div class="col-md-10">
								<textarea id="editor-remark" name="remark" placeholder="添加说明" autofocus></textarea>
								<script type="text/javascript">
									_editor = editor($('#editor-remark'));
								</script>
							</div>
						</div>
						<div id="isSubject_div_id" class="form-group">
							<label class="col-md-2 control-label">是否为专题</label>
							<div class="col-md-10">
								<input type="radio" name="isSubject" value="0">否
								<input type="radio" name="isSubject" value="1">是
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">是否热门</label>
							<div class="col-md-10">
								<input type="radio" name="isHot" value="0">否
								<input type="radio" name="isHot" value="1">是
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">是否在APP显示</label>
							<div class="col-md-10">
								<input type="radio" name="isShow" value="0">否
								<input type="radio" name="isShow" value="1">是
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">缩略图</label>
							<div class="col-md-10">
								<input type="file" name="icon_file" value="上传新图">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">首图</label>
							<div class="col-md-10">
								<input type="file" name="cover_file" value="上传新图">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">开始时间</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="beginTimeTemp" value="" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss'})">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">结束时间</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="overTimeTemp" value="" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss'})">
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
	<#-- 新增资讯（专题），与赛事没有关联-->
	<div id="modal-addInfom" class="modal fade">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideEditor()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title">新增资讯（专题），与赛事没有关联</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form_infom" method="post">
						<div class="form-group">
							<label class="col-md-2 control-label">标题</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="title" value="" maxlength="50" >
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">简介</label>
							<div class="col-md-10">
								<textarea id="remark_textarea_id" class="form-control" name="brief" cols="74" rows="3" value=""></textarea>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">说明</label>
							<div class="col-md-10">
								<textarea id="editor-remark2" name="remark" placeholder="添加说明" autofocus></textarea>
								<script type="text/javascript">
									_editor2 = editor($('#editor-remark2'));
								</script>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">是否为专题</label>
							<div class="col-md-10">
								<input type="radio" name="isSubject" value="0">否
								<input type="radio" name="isSubject" value="1">是
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">是否热门</label>
							<div class="col-md-10">
								<input type="radio" name="isHot" value="0">否
								<input type="radio" name="isHot" value="1">是
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">是否在APP显示</label>
							<div class="col-md-10">
								<input type="radio" name="isShow" value="0">否
								<input type="radio" name="isShow" value="1">是
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">缩略图</label>
							<div class="col-md-10">
								<input type="file" name="icon_file" value="上传新图">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">首图</label>
							<div class="col-md-10">
								<input type="file" name="cover_file" value="上传新图">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">开始时间</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="beginTimeTemp" value="" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss'})">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">结束时间</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="overTimeTemp" value="" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss'})">
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
	// 初始化模态框
	var _$editor = $('#modal-editor');
	_$editor.modal({
		keyboard : false,
		show : false
	})
	
	// 关闭模态框
	function hideEditor() {
		_$editor.modal('hide');
		window.location.reload();
	}
	
	// 初始化表单数据
	function fillForm(columns) {
		for(var k in columns) {
			_$editor.find('input[name="' + k + '"]').val(columns[k]);
		}
	}
	
	// 显示模态框，编辑
	$('button[edit]').on('click', function(event) {
		$('#remark_div_id,#isSubject_div_id').hide();
		var id = $(this).attr('edit');
		var title = $(this).attr('title');
		var brief = $(this).attr('brief');
		var isSubject = $(this).attr('isSubject');
		var isHot = $(this).attr('isHot');
		var isShow = $(this).attr('isShow');
		var beginTime = $(this).attr('beginTime');
		var overTime = $(this).attr('overTime');
		<!--给textarea赋值-->
		$("#remark_textarea_id").val(brief)
		<!--根据值选中单选框-->
		$("input[type='radio'][name=isSubject][value='"+isSubject+"']").attr("checked",true);
		$("input[type='radio'][name=isHot][value='"+isHot+"']").attr("checked",true);
		$("input[type='radio'][name=isShow][value='"+isShow+"']").attr("checked",true);
		fillForm({
			id: id,
			title: title,
			beginTimeTemp: beginTime,
			overTimeTemp: overTime
		});
		_$editor.modal('show');
	});
	
	
	// 提交表单，编辑
	function submitEditor() {
		_$editor.find('form').ajaxSubmit({
			url:'${ctx}/activityInformation/save',
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
	
	// 添加旗下资讯
	$('button[activityId]').on('click', function(event) {
		$('#remark_div_id').show();
		$('#isSubject_div_id').hide();
		var activityId = $(this).attr('activityId');
		var netbarId = $(this).attr('netbarId');
		var pid = $(this).attr('pid');
		fillForm({
			netbarId: netbarId,
			activityId: activityId,
			pid: pid
		});
		_$editor.modal('show');
	});
	// 新增独立资讯
	$('button[addInfo]').on('click', function(event) {
		$('#modal-addInfom').modal('show');
	});
	// 提交表单，添加资讯
	function submitEditorInfom() {
		var $form = $('#modal-addInfom').find('form');
		$form.ajaxSubmit({
			url:'${ctx}/activityInformation/save',
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
			$.api('${ctx}/activityInformation/delete/' + id, {}, function(d) {
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
</body>
</html>