<#import "/macros/pager.ftl" as p >
<html>
<head>
<script type="text/javascript" editor-ctx="${ctx!}" src="${ctx!}/static/plugin/editor.js"></script>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>官网管理</strong>
 		</li>
		<li class="active">
			新闻动态活动
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post" >
			<div class="col-md-2">
				<input type="text" class="form-control" name="title" placeholder="标题" value="${(params.title)!}" />
			</div>
			<div class="col-md-2">
				<select class="form-control" name="type">
					<option value="" <#if !params.type??>selected</#if> >--类型--</option>
					<option value="0" <#if ((params.type!-1)?number)==0>selected</#if> >Banner（外部连接）</option>
					<option value="1" <#if ((params.type!-1)?number)==1>selected</#if> >Banner（站内详情）</option>
					<option value="2" <#if ((params.type!-1)?number)==2>selected</#if> >公司新闻</option>
					<option value="3" <#if ((params.type!-1)?number)==3>selected</#if> >企业文化</option>
					<option value="4" <#if ((params.type!-1)?number)==4>selected</#if> >English-news</option>
					<option value="5" <#if ((params.type!-1)?number)==5>selected</#if> >行业动态</option>
					<option value="6" <#if ((params.type!-1)?number)==6>selected</#if> >赛事资讯</option>
					<#--  <option value="7" <#if ((params.type!-1)?number)==7>selected</#if> >新增模块测试</option>  -->
				</select>
			</div>
			<button type="submit" class="btn btn-success" style="margin-right:20px;">查询</button>
			<a class="btn" href="1" style="margin-right:20px;">重置</a>
			<button add="" type="button" class="btn btn-info">新增</button>
		</form>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>#</th>
			<th>图片</th>
			<th>类别</th>
			<th>标题</th>
			<th>摘要</th>
			<th>新增时间</th>
			<th>操作</th>
		</tr>
		<#list list as o>
			<tr>
				<td>${o.id!}</td>
				<td><img src="${imgServer!}/${o.icon!}" style="width: 50px; height: 50px;" /></td>
				<#assign temp=(o.type!0)>
				<#if 1 == temp>
					<td>Banner（站内详情）</td>
				<#elseif 0 == temp>
					<td>Banner（外部连接）</td>
				<#elseif 2 == temp>
					<td>公司新闻</td>
				<#elseif 3 == temp>
					<td>企业文化</td>
				<#elseif 4 == temp>
					<td>English-news</td>
				<#elseif 5 == temp>
					<td>行业动态</td>
				<#elseif 6 == temp>
					<td>赛事资讯</td>
				<#--  <#elseif 7 == temp>
					<td>新增模块测试</td>  -->
				<#else>
					<td></td>
				</#if> 
				<td><#if o.title??><#if o.title?length lt 10>${o.title!}<#else>${(o.title?substring(0,10)?html)!}...</#if></#if></td>
				<td><#if o.content??><#if o.content?length lt 20>${o.content!}<#else>${(o.content?substring(0,20)?html)!}...</#if></#if></td>
				<td>${o.create_date!}</td>
				<td>
					<button edit="${o.id!}" type="button" class="btn btn-info">编辑</button>
					<button remove="${o.id!}" valid="0" type="button" class="btn btn-danger">删除</button>
				</td>
			</tr>
		</#list>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	
	<#-- 编辑/新增-->
	<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
	<div id="modal-editor" class="modal fade">
		<div class="modal-dialog" style="width:1000px">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideEditor()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 id="h4_edit_id" class="modal-title" >编辑</h4>
					<h4 id="h4_add_id" class="modal-title" >新增</h4>
				</div>
				<div class="modal-body">
					<form id="from_id_edit" class="form-horizontal form-condensed" role="form" method="post">
						<input id="input_id_id" type="hidden" name="id" />
						<div class="form-group">
							<label class="col-md-2 control-label">类型<font id="font_id_1" color="red">*</font></label>
							<div class="col-md-10">
								<select id="select_id_type" name="type" class="form-control">
									<option value="0">Banner（外部连接）</option>
									<option value="1">Banner（站内详情）</option>
									<option value="2">公司新闻</option>
									<option value="3">企业文化</option>
									<option value="4">English-news</option>
									<option value="5">行业动态</option>
									<option value="6">赛事资讯</option>
									<#--  <option value="7">新增模块测试</option>  -->
								</select>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">标题<font id="font_id_2" color="red">*</font></label>
							<div class="col-md-10">
								<input id="input_id_title" class="form-control" type="text" name="title" value="">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">摘要<font id="font_id_5" color="red">*</font></label>
							<div class="col-md-10">
								<textarea id="input_id_summary" cols="100" rows="2" name="summary" value="" ></textarea>
							</div>
						</div>
						<div class="form-group" id="div_id_content_0">
							<label class="col-md-2 control-label">URL<font id="font_id_3" color="red">*</font></label>
							<div class="col-md-10">
								<input id="input_id_url" class="form-control" type="text" name="url" value="">
							</div>
						</div>
						<div class="form-group" id="div_id_content_1">
							<label class="col-md-2 control-label">内容<font id="font_id_3" color="red">*</font></label>
							<div class="col-md-10">
								<textarea id="textarea_id_content" name="content" placeholder="" autofocus></textarea>
								<script type="text/javascript">
									_editor = editor($('#textarea_id_content'));
								</script>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">图片<font id="font_id_4" color="red">*</font></label>
							<div class="col-md-10">
								<input id="input_id_icon" type="file" name="icon_file" value="图片" style="width: 164px;">
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
			_$editor.find('input[name="' + k + '"]').val(columns[k]);
		}
	}
	
	// 显示模态框，编辑
	$('button[edit]').on('click', function(event) {
		$('#h4_edit_id').show();
		$('#h4_add_id,#font_id_1,#font_id_2,#font_id_3,#font_id_4,#font_id_5').hide();
		var id = $(this).attr('edit');
		$.ajax({
			url:'${ctx}/official/website/dynamic/info/'+id,
			data : '',
			dataType : 'json',
		    type:'post',
		    success: function(d){
		    	if(d.code == 0) {
		    		var o = d.object;
					$.fillForm({
						id: o.id,
						title: o.title,
						summary: o.summary,
						icon_file: o.icon,
					},_$editor);
					$('#select_id_type').val(o.type);
					if(o.type==0){
						$('#input_id_url').val(o.content);
						$('#div_id_content_0').show();
						$('#div_id_content_1').hide();
					}else{
						$('#input_id_url').val('http://');
						$('#div_id_content_1').show();
						$('#div_id_content_0').hide();
					}
					setEditorText($('#textarea_id_content'), o.content);
		    	} else {
		    		alert(d.result);
		    	}
			}
		});
		_$editor.modal('show');
	});
	
	// 显示模态框，新增
	$('button[add]').on('click', function(event) {
		$('#div_id_content_1').show();
		$('#div_id_content_0').hide();
		$('#h4_edit_id').hide();
		$('#h4_add_id,#font_id_1,#font_id_2,#font_id_3,#font_id_4,#font_id_5').show();
		$.fillForm({
			id: '',
			title: '',
			url: 'http://',
			icon_file: '',
		},_$editor);
		$('#select_id_type').val(1);
		setEditorText($('#textarea_id_content'), '');
		_$editor.modal('show');
	});
	
	$('#select_id_type').on('change',function(){
		if($(this).val() == 0){
			$('#div_id_content_0').show();
			$('#div_id_content_1').hide();
		}else {
			$('#div_id_content_1').show();
			$('#div_id_content_0').hide();
		}
	});
	
	// 提交表单
	function submitEditor() {
		var title = $('#input_id_title').val();
		var summary = $('#input_id_summary').val();
		var content = $('#textarea_id_content').val();
		var icon = $('#input_id_icon').val();
		var id = $('#input_id_id').val();
		var url = $('#input_id_url').val();
		var type = $('#select_id_type').val();
		if(type==0){
			content=url;
		}
		if(title=='' || summary=='' || content=='' || (icon=='' && id=='')) {
			alert('红色*选项为必填');
			return;
		}
		_$editor.find('form').ajaxSubmit({
			url:'${ctx}/official/website/dynamic/save',
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
	
	// 删除/恢复
	$('button[remove]').on('click', function(event) {
		var _this = $(this);
		_this.attr('disabled', true);
		var id = $(this).attr('remove');
		var valid = $(this).attr('valid');
		
		$.confirm('确认删除吗?', function() {
			$.api('${ctx}/official/website/dynamic/validChange/' + id + '/' + valid, {}, function(d) {
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