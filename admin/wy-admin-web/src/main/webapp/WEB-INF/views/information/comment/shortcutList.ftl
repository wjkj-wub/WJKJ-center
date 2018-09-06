<#import "/macros/pager.ftl" as p >
<html>
<head>
<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>管理系统</strong>
 		</li>
		<li class="active">
			评论管理/一键回复管理
		</li>
	</ul>
	<button id="add"  type="button" class="btn btn-info pull-right">新增一键回复</button>
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>图片</th>
			<th>内容</th>
			<th>操作</th>
		</tr>
		<#if list??>
		<#list list as o>
			<tr>
				<td>
				<img src="${imgServer!}${o.img!}" style="width:36px;height:36px;" />
				</td>
				<td>${(o.comment)!}</td>
				<td>
					<button edit="${o.id!}"  type="button" class="btn btn-warning">编辑</button>
				<#if o_index = 0><button down="${o.id!}"  type="button" class="btn btn-info">下移</button></#if><!--判断是否是第一个元素-->
				<#if !o_has_next><button up="${o.id!}"  type="button" class="btn btn-info">上移</button></#if><!--判断是否是最后一个元素-->
				<#if o_index !=0&&o_has_next>	
					<button up="${o.id!}"  type="button" class="btn btn-info">上移</button>
					<button down="${o.id!}"  type="button" class="btn btn-info">下移</button>
				</#if>			
					<button delete="${o.id!}"  type="button" class="btn btn-danger">删除</button>
				</td>
			</tr>
		</#list>
		</#if>
	</table>

	<#-- 新增、编辑 -->
	<div id="modal-add" class="modal fade">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideReplyEditor()">
						<span>×</span>
					</button>
				</div>
				<div class="modal-body">
					<form class="form-horizontal form-condensed" id="modal-reply-form" role="form" method="post">
						<input type="hidden" name="id" id="infoId" value="0"/>
						<div class="form-group">
							<label class="col-md-2 control-label">回复内容</label>
							<div class="col-md-10">
									<textarea class="form-control comment-content" id="reply-detail" name="comment" style="height:20px;" maxlength="5" wy-required="回复内容"></textarea>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">上传图片</label>
							<div class="col-md-10">
								<img id="imgfile" style="width:36px;height:36px;" />
								<input type="file" id="iconFile" name="iconFile" />
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" onclick="hideReplyEditor()">取消</button>
					<button type="button" class="btn btn-primary" onclick="submitReply()">保存</button>
				</div>
			</div>
		</div>
	</div>
	
	
	
	<script type="text/javascript">
		//评论内容-tip
		$('button[data-toggle="tooltip"]').tooltip();
		var _$replyEditor = $('#modal-add');
		function showReplyEditor(id) {
			$('#infoId').val(id);
			 console.log(id);
			$.ajax({
				url:'${ctx}/comment/shortcut/info?id='+id,
				type:'post',
				success:function(data){
					if(data.code==0){
						var info=data.object;
						$('#reply-detail').val(info.comment);
						if(info.img.length){
							$('#imgfile').show()
							$('#imgfile').attr("src",'${imgServer!}'+info.img);
						}
					}
				}
			});
			$('#modal-add').find('input, textarea').formCorr();
			_$replyEditor.modal('show');
		}
		
		function showReplyAdd() {
			$('#infoId').val("");
			$('#reply-detail').val('');
			$('#imgfile').attr("src",'');
			$('#iconFile').val('');
			$('#imgfile').hide()
			_$replyEditor.modal('show');
			
			$('#modal-add').find('input, textarea').formCorr();
		}
		//隐藏编辑框
		function hideReplyEditor() {
			_$replyEditor.modal('hide');
		}
		
		function imgValidate() {
			var $imgFile = $('#modal-add input[name="iconFile"]');
			$imgFile.formCorr();
			var src = $('#imgfile').attr('src');
			if(src && src.length > 0) {
				return true;
			}
			if($imgFile.val().length > 0) {
				return true;
			}
			
			$imgFile.formErr('请选择图片');
			return false;
		}
		
		// 提交表单
		function submitReply() {
			var $form = $('#modal-reply-form')
			var valid = $form.formValid();
			var imgValid = imgValidate();
			if(valid && imgValid) {
				$form.ajaxSubmit({
					url:'${ctx}/comment/shortcut/save',
					type:'post',
					success: function(d){
						if(d.code== 0) {
							window.location.reload();
						}else if(d.code==-1){
						
						}else{
							alert(d.result);
							window.location.reload();
						}
					}
				});
			}
		}	
		//-------------------------------------按钮操作----------------------------------
		$('button[edit]').on('click', function() {
			var id = $(this).attr('edit');
			showReplyEditor(id);
		});
		
		$('button[up]').on('click', function() {
			var id = $(this).attr('up');
			$.ajax({url:'${ctx}/comment/shortcut/move?id='+id+'&move=-1',
				type:'post',
				success:function(data){
					if(data.code==0){
						window.location.reload();
					}
				}
			 });
		});
		
		$('button[down]').on('click', function() {
			var id = $(this).attr('down');
			$.ajax({url:'${ctx}/comment/shortcut/move?id='+id+'&move=1',
					type:'post',
					success:function(data){
						if(data.code==0){
							window.location.reload();
						}
					}
			 });
		});
		
		$('button[delete]').on('click', function() {
			 var id = $(this).attr('delete');
			$.confirm('确认删除吗?', function() {
				$.api('${ctx}/comment/shortcut/delete?id=' + id, {}, function(d) {
					if(d.code==0){
					  window.location.reload();
					}else if(d.code==-1){
						alert("评论内容数量不能少于4个。");
					}else{
						alert("删除失败,请重新删除");	
					}
				});
			});
		});
		$('#add').on('click', function() {
			showReplyAdd();
		});
		
	</script>
</body>
</html>