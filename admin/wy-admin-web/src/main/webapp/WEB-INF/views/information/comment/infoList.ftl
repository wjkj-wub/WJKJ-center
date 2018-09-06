<#import "/macros/pager.ftl" as p >
<html>
<head>
<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/simditor/css/simditor.css">
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/simditor/css/font-awesome.css">
<link rel="stylesheet" type="text/css"  href="${ctx!}/static/plugin/simditor/css/simditor.scss">
<script type="text/javascript" editor-ctx="${ctx!}" src="${ctx!}/static/plugin/simditor/editor.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/module.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/hotkeys.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/uploader.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/simditor/js/simditor.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>管理系统</strong>
 		</li>
		<li class="active">
			评论管理
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
			<div class="col-md-2">
				<input type="text" name="infoId" value="${(params.infoId)!}" class="form-control" placeholder="信息ID">
			</div>
			<div class="col-md-2">
				<input type="text" name="title" value="${(params.title)!}" class="form-control" placeholder="标题">
			</div>
			
			<div class="col-md-2">
				<select class="form-control" name="tableType">
					<option value="0"<#if (params.tableType!0) == 0> selected</#if>>资讯</option>
					<option value="2"<#if (params.tableType!0) == 2> selected</#if>>官方赛</option>
					<option value="3"<#if (params.tableType!0) == 3> selected</#if>>悬赏令</option>
				</select>
			</div>
			<div class="col-md-2">
				<select class="form-control" name="moduleId">
					<option value="">全部模块</option>
					<#if modules??>
						<#list modules as m>
							<option value="${(m.id)!}"     <#if (params.moduleId!0)==m.id> selected</#if>        >${(m.name)!}</option>
						</#list>
					</#if>
				</select>
			</div>
			
			
			<div class="col-md-2">
				<select class="form-control" name="commentType">
					<option value="">全部类型</option>
					<option value="1"<#if (params.commentType!0) == 1> selected</#if>>图文</option>
					<option value="2"<#if (params.commentType!0) == 2> selected</#if>>专题</option>
					<option value="3"<#if (params.commentType!0) == 3> selected</#if>>图集</option>
					<option value="4"<#if (params.commentType!0) == 4> selected</#if>>视频</option>
				</select>
			</div>
			<div class="col-md-2">
				<input type="text" name="startDate" id="startDate" value="${(params.startDate)!}" class="form-control" placeholder="开始时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd'})">
			</div>
			<div class="col-md-2">
				<input type="text" name="endDate" id="endDate" value="${(params.endDate)!}" class="form-control" placeholder="结束时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd'})">
			</div>
			<div class="col-md-2">
				<input type="checkbox" name="orderComment" value="1" <#if ((params.orderComment!0)>0)> checked="checked"</#if> > 评论最少
			</div>
			<button type="submit" class="btn btn-success" id="search">查询</button>
			<a href="/comment/subcomment/list/1" class="btn btn-primary">评论模式</a>
		</form>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>ID</th>
			<th>功能</th>
			<th>模块</th>
			<th>类型</th>
			<th>标题</th>
			<th>生效时间</th>
			<th>评论数</th>
			<th>操作</th>
		</tr>
		<#if list??>
		<#list list as o>
			<tr>
				<td>${o.id!}</td>
				<td>
					<#if (params.tableType!0) == 0>
						资讯
					<#elseif ((params.tableType)!0) == 2>
						官方赛
					<#elseif ((params.tableType)!0) == 3>
						悬赏令
					</#if>
				</td>
				<td>${(o.moudleName)!"--"}</td>
				<td>
					<#if ((o.type)!0) == 0>
						--
					<#elseif ((o.type)!0) == 1>
						图文
					<#elseif ((o.type)!0) == 2>
						专题
					<#elseif ((o.type)!0) == 3>
						图集
					<#elseif ((o.type)!0) == 4>
						视频
					</#if>
				</td>
				
				<td>
				<button type="button" class="btn btn-default" data-toggle="tooltip" data-placement="top" data-original-title="${(o.title)!}">
					<#if ((o.amuse_id)!0) gt 0>
						<#if (o.title)?? && o.title?length gt 15>
							<a href="${ctx}/comment/commentlist/${(o.amuse_id)!}/2/1">
							${(o.title?substring(0,15))!}...
							</a>
						<#else>
							<a href="${ctx}/comment/commentlist/${(o.amuse_id)!}/2/1">
							${o.title!}
							</a>
						</#if>
					<#else>
						<#if (o.title)?? && o.title?length gt 15>
							${(o.title?substring(0,15))!}...
						<#else>
							${o.title!}
						</#if>
					</#if>
				</button>
				</td>
				<td>${(o.create_date)!}</td>
				<td>${(o.ccount)!0}</td>
				<td>
					<button reply="${o.id!}" reply-type="${params.tableType!0}" type="button" class="btn btn-info">回复</button>
				</td>
			</tr>
		</#list>
		</#if>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	
	
	
	<#-- 回复 -->
	<div id="modal-reply" class="modal fade">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideReplyEditor()">
						<span>×</span>
					</button>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" id="modal-reply-form" role="form" method="post">
						<input type="hidden" name="infoId" id="infoId" />
						<input type="hidden" name="infoType" id="infoType" />
						<div class="form-group">
							<label class="col-md-2 control-label">评论人</label>
							<div class="col-md-4">
								<input  type="hidden" name="userId" />
								<input class="form-control" type="text" name="userNickname" value=""  placeholder="请输入评论人完整昵称或点击随机"/>
							</div>
							<span><button type="button" onclick="generateUserInfo()" >随机</button><button type="button" onclick="generateKf()" >客服</button></span>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">评论</label>
							<div class="col-md-10">
								<textarea class="form-control comment-content" id="reply-detail" name="commentContent" style="height:100px;" maxlength="200"></textarea>
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-primary" onclick="submitReply()">保存</button>
				</div>
			</div>
		</div>
	</div>
	
	
	
	<script type="text/javascript">
		//评论内容-tip
		$('button[data-toggle="tooltip"]').tooltip();
		//---------------------------------回复功能---------------------
		var _$replyEditor = $('#modal-reply');
		function showReplyEditor(id,type) {
			$('#infoId').val(id);
			$('#infoType').val(type);
			_$replyEditor.modal('show');
		}
		
		//隐藏编辑框
		function hideReplyEditor() {
			_$replyEditor.modal('hide');
		}
		
		// 提交表单
		function submitReply() {
			var $form = $('#modal-reply-form')
			var valid = $form.formValid();
			if(valid) {
				$form.ajaxSubmit({
					url:'${ctx}/comment/replyInfo',
				    type:'post',
				    success: function(d){
				    	if(d == 0) {
				    		window.location.reload();
				    	} else if(d==-1) {
				    		alert("待评论信息不存在");
				    	} else if(d==-2) {
				    		alert("评论内容不能为空");
				    	} else if(d==-3) {
				    		alert("请输入昵称或点击随机");
				    	} else if(d==-4) {
				    		alert("您输入的昵称账号不存在");
				    	}
					}
				});
			}
		}
		
		
		//-------------------------------------按钮操作----------------------------------
		// 回复
		$('button[reply]').on('click', function() {
			var id = $(this).attr('reply');
			var type = $(this).attr('reply-type');
			showReplyEditor(id,type);
		});
		
		//-----------------------随机获取用户信息-------------------------
		// 提交表单
		function generateUserInfo() {
			var $form = $('#modal-reply-form')
			var valid = $form.formValid();
			var content = $('#reply-detail').val();
			var infoId = $('#infoId').val();
			var infoType = $('#infoType').val();
			if(valid) {
				$form.ajaxSubmit({
					url:'${ctx}/comment/randomReplyUser',
				    type:'post',
				    success: function(d){
				    	$.fillForm({
							userId: d.userId,
							userNickname: d.userNickname,
							infoType:infoType,
							commentContent:content,
							infoId:infoId
						}, _$replyEditor);
					}
				});
			}
		}
		// 获取客服账号信息
		function generateKf() {
			var $form = $('#modal-reply-form')
			var valid = $form.formValid();
			var content = $('#reply-detail').val();
			var infoId = $('#infoId').val();
			var infoType = $('#infoType').val();
			if(valid) {
				$.fillForm({
							userId: 1,
							userNickname: "网娱大师",
							infoType:infoType,
							commentContent:content,
							infoId:infoId
						}, _$replyEditor);
			}
		}
		//--------------------------form表单提交日期检查-------------------------
		$(document).ready(function(){ 
	        $(":submit[id=search]").click(function(check){ 
	            var startDate = $("#startDate").val(); 
	            var endDate = $("#endDate").val(); 
	            if(startDate!=""){ 
		            if(endDate==""){
		            	alert("结束时间不能为空！"); 
		                check.preventDefault();//此处阻止提交表单 
		            }
	            } 
	            if(endDate!=""){ 
		            if(startDate==""){
		            	alert("开始时间不能为空！"); 
		                check.preventDefault();//此处阻止提交表单 
		            }
	            }  
	            if(startDate!=""&&endDate!=""){
	            var start = stringToDate(startDate);
	            var end = stringToDate(endDate);
	            	if(compareDate(start,end)){
		            	alert("开始时间不能大于结束时间！"); 
		                check.preventDefault();//此处阻止提交表单 
		            }
	            }
	        }); 
    	});  
    	    var stringToDate = function(dateStr,separator){  
                                if(!separator){  
                                    separator="-";  
                                }  
                                var dateArr = dateStr.split(separator);  
                                var year = parseInt(dateArr[0]);  
                                var month;  
    							if(dateArr[1].indexOf("0") == 0){  
                                    month = parseInt(dateArr[1].substring(1));  
                                }else{  
                                     month = parseInt(dateArr[1]);  
                                }  
                                var day = parseInt(dateArr[2]);  
                                var date = new Date(year,month -1,day);  
                                return date;  
                            }  
    	var compareDate = function(date1,date2){  
		   return date1> date2;  
		}
	</script>
</body>
</html>