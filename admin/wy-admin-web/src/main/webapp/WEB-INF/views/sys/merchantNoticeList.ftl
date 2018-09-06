<#import "/macros/pager.ftl" as p >
<html>
<head>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
<style type="text/css"> 
/* 带复选框的下拉框 */
.select_checkBox{
border:0px solid red; 
position: relative; 
display:inline-block; 
margin-left: 0px;
} 
.chartQuota{ 
height:23px; 
float:left; 
display:inline-block; 
border:0px solid black; 
position: relative; 
} 
 
.chartOptionsFlowTrend{ 
z-index:300; 
background-color:white; 
border:1px solid gray; 
display:none; 
position: absolute; 
left:0px; 
top:23px; 
width:150px; 
} 
.select_checkBox .chartOptionsFlowTrend ul{ 
float:left; 
padding: 0px; 
margin: 5px; 
} 
.select_checkBox .chartOptionsFlowTrend li{ 
/* float:left; */
display:block; 
position: relative; 
left:0px; 
margin: 0px; 
clear:both; 
} 
.select_checkBox .chartOptionsFlowTrend li *{ 
float:left; 
} 
.select_checkBox a:-webkit-any-link { 
color: -webkit-link; 
text-decoration: underline; 
cursor: auto; 
} 
.select_checkBox .chartQuota p a { 
float: left; 
height: 21px; 
outline: 0 none; 
border: 1px solid #ccc; 
line-height: 22px; 
padding: 0 5px; 
overflow: hidden; 
background: #eaeaea; 
color: #313131; 
text-decoration: none; 
} 
 
.select_checkBox .chartQuota p { 
margin:0px; 
folat:left; 
overflow: hidden; 
height: 23px; 
line-height:24px; 
display: inline-block; 
} 
.select_checkBox .chartOptionsFlowTrend p { 
height: 23px; 
line-height: 23px; 
overflow: hidden; 
position: relative; 
z-index: 2; 
background: #fefbf7; 
padding-top: 0px; 
display: inline-block; 
} 
.select_checkBox .chartOptionsFlowTrend p a { 
border: 1px solid #fff; 
margin-left: 15px;
color: #2e91da; 
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
			商户端通知管理
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post" style="display:inline;">
			<div class="col-md-2">
				<input type="text" name="title" value="${(params.title)!}" class="form-control" placeholder="标题">
			</div>
			<div class="col-md-2">
				<input type="text" class="form-control" name="beginDate" placeholder="发布时间(起)" value="${(params.beginDate)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:beginPick})" />
			</div>
			<div class="col-md-2">
				<input type="text" class="form-control" name="endDate" placeholder="发布时间(止)" value="${(params.endDate)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:endPick})" />
			</div>
			<button type="submit" class="btn btn-success">查询</button>
			<a class="btn" href="1">清空</a>
		</form>
		<button id="new-record" type="button" class="btn btn-success" style="margin-left:30px;">新增通知</button>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>标题</th>
			<th>内容</th>
			<th>发布时间</th>
			<th>阅读次数</th>
			<th>操作</th>
		</tr>
		<#list list as o>
			<tr>
				<td>${o.title!}</td>
				<td><#if o.content?length lt 20>${o.content!}<#else>${(o.content?substring(0,20))!}...</#if></td>
				<td>${o.createDate!}</td>
				<td>${o.readCount!}</td>
				<td>
					<button edit="${o.id!}" type="button" class="btn btn-info">编辑</button>
					<button remove="${o.id!}" type="button" class="btn btn-danger">删除</button>
				</td>
			</tr>
		</#list>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	
	<#-- 新增、编辑 -->
	<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
	<div id="modal-editor" class="modal fade">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideEditor()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
					<h4 class="modal-title">编辑消息</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post">
						<input type="hidden" name="id" />
						<input type="hidden" name="type" />
						<input type="hidden" name="restrict" />
						<div class="form-group">
							<label class="col-md-2 control-label">标题</label>
							<div class="col-md-10">
								<input class="form-control" type="text" name="title" value="" />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">地区</label>
							<div class="col-md-10">
								<div class="select_checkBox"> 
									<div class="chartQuota"> 
										<p><a href="javascript:;" hidefocus="true" title="请选择地区"><span>选择地区</span><b></b></a></p> 
									</div><br> 
									<div class="chartOptionsFlowTrend""> 
										<ul> 
											<input type="checkbox" name="areaCode" value="000000"><span>全国</span><br>
											<#list provinceList as p>
												<input type="checkbox" name="areaCode${p_index}" value="${p.areaCode!}"><span>${p.name!}</span><br>
											</#list>
										</ul> 
									</div> 
								</div>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">内容</label>
							<div class="col-md-10">
								<textarea name="content" rows="10" class="form-control" maxlength="300"></textarea>
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
	<#-- 模态框业务 -->
	<script type="text/javascript">
		var _$editor = $('#modal-editor');
		function showEditor(title) {
			if(title) {
				_$editor.find('.modal-title').html(title);
			}
			_$editor.modal('show');
		}
		function hideEditor() {
			_$editor.modal('hide');
		}
		
		// 提交表单
		function submitEditor() {
			var $form = _$editor.find('form');
			var valid = $form.formValid();
			if(valid) {
				$form.ajaxSubmit({
					url:'${ctx}/merchantNotice/save',
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
	</script>
	
	<script type="text/javascript">
		// 编辑
		$('button[edit]').on('click', function(event) {
			// 初始化表单数据
			var id = $(this).attr('edit');
			$.api('${ctx}/merchantNotice/detail/' + id, {}, function(d) {
				var o = d.object.notice;
				$.fillForm({
					title: o.title,
					content: o.content,
					id: o.id,
				}, _$editor);
				var areas = d.object.areaCodes;
				for(var i=0;i<areas.length;i++){
					var value = areas[i].areaCode;
					var pLength = ${provinceList?size};
					if(value=='000000'){
						$("input[type='checkbox'][name='areaCode'][value='000000']").attr("checked",true);
					}else{
						for(var j=0;j<pLength;j++){
							var name = "areaCode"+j;
							$("input[type='checkbox'][name='"+name+"'][value='"+areas[i].areaCode+"']").attr("checked",true);
						}
					}
				}
			}, function(d) {
				alert('请求数据异常：' + d.result);
			}, {
				async: false,
			});
			
			showEditor('编辑通知');
		});
		
		// 新增
		$('#new-record').on('click', function() {
			$.fillForm({
			}, _$editor);
			showEditor('新增通知');
		});
		
		// 删除
		$('button[remove]').on('click', function() {
			var id = $(this).attr('remove');
			$.confirm('确认删除吗?', function() {
				$.api('${ctx!}/merchantNotice/delete/' + id, {}, function(d) {
					window.location.reload();
				});
			});
		});
		
	$(function(){ 
		$(".select_checkBox").hover(function(){ 
			$(".chartOptionsFlowTrend").css("display","inline-block"); 
		},function(){ 
			$(".chartOptionsFlowTrend").css("display","none"); 
		}); 
	}); 
	</script>
</body>
</html>