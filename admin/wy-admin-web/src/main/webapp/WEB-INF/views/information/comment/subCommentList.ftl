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
				<input type="text" name="commentId" value="${(params.commentId)!}" class="form-control" placeholder="评论id">
			</div>
			<div class="col-md-2">
				<input type="text" name="nickname" value="${(params.nickname)!}" class="form-control" placeholder="评论人昵称">
			</div>
			<div class="col-md-2">
				<input type="text" name="content" value="${(params.content)!}" class="form-control" maxlength="10" placeholder="评论内容">
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
				<input type="checkbox" name="orderLikeCount" value="1" <#if ((params.orderLikeCount!0)>0)> checked="checked"</#if> > 最热评论
			</div>
			<button type="submit" class="btn btn-success" id="search">查询</button>
			<button type="button" class="btn btn-danger" onclick="multiDelete()">批量删除</button>
			<a href="/comment/info/list/1" class="btn btn-primary">资讯模式</a>
		</form>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>
				<input id="checkAll" type="checkbox" />
			</th>
			<th>ID</th>
			<th>功能</th>
			<th>模块</th>
			<th>类型</th>
			<th>标题</th>
			<th>前置评论</th>
			<th>评论内容</th>
			<th>评论时间</th>
			<th>点赞量</th>
			<th>评论人昵称</th>
			<th>操作</th>
		</tr>
		<#if list??>
		<#list list as o>
			<tr>
				<td>
					<input type="checkbox" name="id" value="${(o.id)!}" />
				</td>
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
					<#if ((o.subjectType)!0) == 0>
						--
					<#elseif ((o.subjectType)!0) == 1>
						图文
					<#elseif ((o.subjectType)!0) == 2>
						专题
					<#elseif ((o.subjectType)!0) == 3>
						图集
					<#elseif ((o.subjectType)!0) == 4>
						视频
					</#if>
				</td>
				<td><a href="${ctx}/comment/commentlist/${o.amuse_id}/1/1">${(o.title)!}</a></td>
				<td>
				<button type="button" class="btn btn-default" data-toggle="tooltip" data-placement="top" data-original-title="${(o.preContent)!}">
					<#if (o.preContent)?? && o.preContent?length gt 15>
						${(o.preContent?substring(0,15))!}...
					<#else>
						${o.preContent!"无"}
					</#if>
				</button>
				
				</td>
				<td>
				
				
				<button type="button" class="btn btn-default" data-toggle="tooltip" data-placement="top" data-original-title="${(o.content)!}">
					<#if (o.content)?? && o.content?length gt 15>
						${(o.content?substring(0,15))!}...
					<#else>
						${o.content!}
					</#if>
				</button>
				
				</td>
				
				
				
				
				<td>${(o.create_date)!}</td>
				<td>${(o.like_count)!0}</td>
				<td>${(o.nickname)!}</td>
				<td>
					<button edit="${o.id!}" content="${o.content!}" type="button" class="btn btn-info">编辑</button>
					<button remove="${o.id!}" type="button" class="btn btn-danger">删除</button>
					<button reply="${o.id!}" type="button" class="btn btn-info">回复</button>
					<#if (o.flag) == 0>
						<button gag="${o.user_id!}" type="button" class="btn btn-danger" contentadd="">
					 		禁言
					 	</button>
					<#else>
						<button gag="${o.gagId!}" type="button" class="btn btn-danger" contentadd="${o.contentadd}">
							已禁言
						</button>
					</#if>
				</td>
			</tr>
		</#list>
		</#if>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	
	<#-- 编辑 -->
	<div id="modal-editor" class="modal fade">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideEditor()">
						<span>×</span>
					</button>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" id="modal-editor-form" role="form" method="post">
						<input type="hidden" name="commentId" id="commentId" />
						<div class="form-group">
							<label class="col-md-2 control-label">评论</label>
							<div class="col-md-10">
								<textarea class="form-control comment-content" id="editor-detail" name="commentContent" style="height:100px;"></textarea>
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" onclick="hideEditor()">关闭</button>
					<button type="button" class="btn btn-primary" onclick="submitEdit()">保存</button>
				</div>
			</div>
		</div>
	</div>
	
	
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
						<input type="hidden" name="replyCommentId" id="replyCommentId" />
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
		//---------------------------------编辑功能---------------------
		var _$editor = $('#modal-editor');
		function showEditor(id,content) {
			if(id) {
				$.fillForm({
					commentContent: content,
					commentId: id
				}, _$editor);
			}
			_$editor.modal('show');
		}
		
		//隐藏编辑框
		function hideEditor() {
			_$editor.modal('hide');
		}
		
		// 提交表单
		function submitEdit() {
			var $form = $('#modal-editor-form')
			var valid = $form.formValid();
			if(valid) {
				$form.ajaxSubmit({
					url:'${ctx}/comment/edit',
				    type:'post',
				    success: function(d){
				    	if(d == 0) {
				    		window.location.reload();
				    	} else {
				    		alert("数据不存在");
				    	}
					}
				});
			}
		}
		
		//---------------------------------回复功能---------------------
		var _$replyEditor = $('#modal-reply');
		function showReplyEditor(id) {
			$('#replyCommentId').val(id);
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
					url:'${ctx}/comment/reply',
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
		// 编辑
		$('button[edit]').on('click', function(event) {
			// 初始化表单数据
			var id = $(this).attr('edit');
			var content = $(this).attr('content');
			showEditor(id,content);
		});
		
		// 删除
		$('button[remove]').on('click', function() {
			var id = $(this).attr('remove');
			$.confirm('确认删除吗?', function() {
				$.api('${ctx!}/comment/delete/' + id, {}, function(d) {
					window.location.reload();
				});
			});
		});
		
		// 回复
		$('button[reply]').on('click', function() {
			var id = $(this).attr('reply');
			showReplyEditor(id);
		});
		
		//-----------------------随机获取用户信息-------------------------
		// 获取随机回复人信息
		function generateUserInfo() {
			var $form = $('#modal-reply-form')
			var valid = $form.formValid();
			var content = $('#reply-detail').val();
			var replyCommentId = $('#replyCommentId').val();
			if(valid) {
				$form.ajaxSubmit({
					url:'${ctx}/comment/randomReplyUser',
				    type:'post',
				    success: function(d){
				    	$.fillForm({
							userId: d.userId,
							userNickname: d.userNickname,
							commentContent:content,
							replyCommentId:replyCommentId
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
			var replyCommentId = $('#replyCommentId').val();
			if(valid) {
				$.fillForm({
							userId: 1,
							userNickname: "网娱大师",
							commentContent:content,
							replyCommentId:replyCommentId
						}, _$replyEditor);
			}
		}
		//--------------------------form表单提交日期检查-------------------------
		$(document).ready(function(){ 
	        $("form[id=search]").on('submit', function(ev){ 
	            var startDate = $("#startDate").val(); 
	            var endDate = $("#endDate").val(); 
	            if(startDate!=""){ 
		            if(endDate==""){
		            	alert("结束时间不能为空！"); 
		                return false;//此处阻止提交表单 
		            }
	            } 
	            if(endDate!=""){ 
		            if(startDate==""){
		            	alert("开始时间不能为空！"); 
		                return false;//此处阻止提交表单 
		            }
	            }  
	            if(startDate!=""&&endDate!=""){
	            var start = stringToDate(startDate);
	            var end = stringToDate(endDate);
	            	if(compareDate(start,end)){
		            	alert("开始时间不能大于结束时间！"); 
		                return false;//此处阻止提交表单 
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
		
		$('button[contentadd]').on('click', function(event) {
	      var content=$(this).attr('contentadd');
	      var gag=$(this).attr('gag');
	      if(content==""){
	      	content="<p>请选择要禁言天数</p><input type='hidden' id='userId' value='"+gag+"'><select id='days'><option value='1'>1</option><option value='3'>3</option><option value='7'>7</option><option value='-1'>永久</option></select>";
	      	$('#tips').html(content);
		  	$('#myModal').modal('show');	
	      }else{
	      	var contentArry=content.split(',');
	      	var dayAll=contentArry[0];
	      	var nowdate=new Date();
	        var datelate=(new Date(contentArry[1])).getTime();
	        var dateearly=nowdate.getTime();
	        var days=parseInt((dateearly-datelate)/1000/60/60/24);
	        var leftday=dayAll-days;
	        if(dayAll!=-1){
		        if(leftday>0){
		        	content="<input type='hidden' id='gagId' value='"+gag+"'>禁言"+dayAll+"天，还剩"+leftday+"天";
		        	$('#tips1').html(content);
			  		$('#myModal1').modal('show');	
		        }else{
		        	content="<input type='hidden' id='gagId' value='"+gag+"'>已经禁言"+dayAll+"天，次日可用";
		        	$('#tips1').html(content);
			  		$('#myModal1').modal('show');	
		        }
		    }else{
		    	content="<input type='hidden' id='gagId' value='"+gag+"'>已经永久禁言";
		        $('#tips1').html(content);
			  	$('#myModal1').modal('show');
		    }
	      }
		});
		
		function hideContentAdd(){
			 $('#myModal').modal('hide');	
		}
		
		// 保存禁言
		function submitGagAdd() {
			var $form = $('#tips');
			var userId=$('#userId').val();
			var days=$('#days').val();
			$form.ajaxSubmit({
				url:'${ctx}/comment/gagAdd/'+userId+'/'+days,
			    type:'post',
			    success: function(d){
			    	if(d.code == 0) {
			    		window.location.reload();
			    	}
				}
			});
		}
		
		//取消禁言
		function deleteGag(){
			var $form = $('#tips1');
			var gagId=$('#gagId').val();
			$form.ajaxSubmit({
				url:'${ctx}/comment/deleteGag/'+gagId,
			    type:'post',
			    success: function(d){
			    	if(d.code == 0) {
			    		window.location.reload();
			    	}
				}
			});
		}
		
		// 监听列表复选按钮事件
		$('#checkAll').on('click', function(event) {
			$('input[type="checkbox"][name="id"]').prop('checked', $(this).prop('checked'));
		});
		
		$('input[type="checkbox"][name="id"]').on('click', function(event) {
			var cbs = $('input[type="checkbox"][name="id"]');
			
			var isAllCheck = true;
			cbs.each(function() {
				if(!$(this).prop('checked')) {
					isAllCheck = false;
					return;
				}
			});
			
			$('#checkAll').prop('checked', isAllCheck);
		});
		
		// 批量删除
	function multiDelete() {
		var params = genIdsParam();
		if(params.length <= 0) {
			alert('请勾选要删除的评论');
			return;
		}
		$.confirm('确认删除吗?', function() {
			$.api('${ctx}/comment/deleteBatch/'+params, {}, function(d) {
				window.location.reload();
			}, false, {
			});
		});
	}
	
	// 将勾选ID组合成id参数
	function genIdsParam(params) {
		if(typeof(params) !== 'string') {
			params = '';
		}
		$('input[type="checkbox"][name="id"]:checked').each(function(event) {
			params += $(this).val()+',';
		});
		return params;
	}
	
	</script>
	<div class="modal fade" id="myModal">
	  <div class="modal-dialog">
  		<div class="modal-body">
  			<form id="tips"></form>
	 	</div>
	 	<div class="modal-footer">
			<button type="button" class="btn btn-default" onclick="hideContentAdd()">关闭</button>
			<button type="button" class="btn btn-primary" onclick="submitGagAdd()">保存</button>
		</div>
	  </div>
	</div>
	<div class="modal fade" id="myModal1">
	  <div class="modal-dialog">
  		<div class="modal-body">
  			<form id="tips1"></form>
	 	</div>
	 	<div class="modal-footer">
			<button type="button" class="btn btn-default" onclick="deleteGag()">取消禁言</button>
		</div>
	  </div>
	</div>
</body>
</html>