<#import "/macros/pager.ftl" as p >
<html>
<head>
<link rel="stylesheet" href="${ctx}/static/lib/chosen/chosen.css" />
<script type="text/javascript" src="${ctx}/static/lib/chosen/chosen.min.js"></script>
<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
<style type="text/css">
.form-condensed .form-control {
	margin-top: 3px;
}
input[disabled] {
    background-color: #FFFFFF!important;
    -webkit-box-shadow: inset 0 1px 1px rgba(0, 0, 0, .075);
    box-shadow: inset 0 1px 1px rgba(0, 0, 0, 0);
    border: 0;
}
.form-condensed .btn {
    padding: 5px 12px;
}
#imgs {
	max-height: 400px;
    overflow: auto;
    border-radius: 5px;
    border: solid 1px #CCC;
    padding: 10px;
}
#imgs img {
	width: 30%;
    margin: 0 10px 10px 0;
}
.imgs-container {
	max-height: 400px;
    overflow: auto;
    border-radius: 5px;
    border: solid 1px #CCC;
    padding: 10px;
}
.imgs-container img {
	width: 30%;
    margin: 0 10px 10px 0;
}
textarea {
	border-radius: 5px;
    border: solid 1px #CCC;
    padding: 10px;
}
</style>
</head>
<body>
	<#-- 导航 -->
	<ul class="breadcrumb">
		<li>
			<i class="icon icon-location-arrow mr10"></i>
  			<strong>娱乐赛审核发放</strong>
 		</li>
		<li class="active">
			操作日志
		</li>
	</ul>
	
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
		<div class="mb10">
			<div class="col-md-2">
				<select class="form-control" name="sysUserId">
					<option value="">全部用户</option>
					<#if sysUsers??>
					<#list sysUsers as u>
						<option value="${(u.id)!}"<#if (params.sysUserId)?? && (u.id)?? && u.id?string == params.sysUserId> selected</#if>>${(u.realname)!}</option>
					</#list>
					</#if>
				</select>
			</div>
			<div class="col-md-2">
				<input type="text" name="info" value="${(params.info)!}" class="form-control" placeholder="输入操作内容关键词">
			</div>
			<div class="col-md-2">
				<input type="text" name="beginDate" value="${(params.beginDate)!}" class="form-control" placeholder="提交时间(起)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:beginPick})">
			</div>
			<div class="col-md-2">
				<input type="text" name="endDate" value="${(params.endDate)!}" class="form-control" placeholder="提交时间(止)" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:endPick})">
			</div>
			<button type="submit" class="btn btn-success">筛选</button>
			<a class="btn btn-info" href="1">清空</a>
		</div>
		</form>
	</div>
	
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>用户名</th>
			<th>操作内容</th>
			<th>操作时间</th>
		</tr>
		<#if list??>
		<#list list as o>
			<tr>
				<td>${(o.sysUserRealName)!}</td>
				<td>
					<#if !(o.type)??>
						${o.info!}
					<#elseif o.type == 1 || o.type == 2 || o.type == 6>
						<a class="btn btn-default" onclick="verify(${(o.thirdId)!})">${o.info!}</a>
					<#elseif o.type == 3 || o.type == 4 || o.type == 5>
						<a class="btn btn-default" onclick="appeal(${(o.thirdId)!})">${o.info!}</a>
					<#else>
						<a class="btn btn-default">${o.info!}</a>
					</#if>
				</td>
				<td>${(o.createDate?string('yyyy-MM-dd HH:mm:ss'))!}</td>
			</tr>
		</#list>
		</#if>
	</table>
	
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
	
	<#-- 审核 -->
	<div id="modal-verify-editor" class="modal fade">
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
						<div class="form-group">
							<label class="col-md-2 control-label">提交凭证时间</label>
							<div class="col-md-10">
								<input type="text" name="createDate" class="form-control" disabled />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">凭证内容</label>
						</div>
						<div class="form-group">
							<label class="col-md-1 control-label"></label>
							<div class="col-md-10">
								<textarea name="describes" style="width:100%;height:100px;" readonly></textarea>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-1 control-label"></label>
							<div class="col-md-10">
								<div id="imgs"></div>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">参赛人资料</label>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">领奖者号码：</label>
							<div class="col-md-10">
								<input type="text" class="form-control" name="telephone" disabled />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">QQ号码：</label>
							<div class="col-md-10">
								<input type="text" class="form-control" name="qq" disabled />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">游戏账号：</label>
							<div class="col-md-10">
								<input type="text" class="form-control" name="gameAccount" disabled />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">比赛区服：</label>
							<div class="col-md-10">
								<input type="text" class="form-control" name="server" disabled />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">赛事资料</label>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">赛事名：</label>
							<div class="col-md-10">
								<input type="text" class="form-control" name="activityName" disabled />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">比赛时间：</label>
							<div class="col-md-10">
								<input type="text" class="form-control" name="startDate" disabled />
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">比赛奖励：</label>
							<div class="col-md-10">
								<input type="text" class="form-control" name="activityReward" disabled />
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
				</div>
			</div>
		</div>
	</div>
	
	<#-- 申诉 -->
	<div id="modal-appeal-editor" class="modal fade">
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
						<div class="form-group fail-appeal">
							<label class="col-md-2 control-label">人证时间</label>
							<div class="col-md-10">
								<input type="text" name="createDate" class="form-control" disabled />
							</div>
						</div>
						<div class="form-group fail-appeal">
							<label class="col-md-2 control-label">凭证内容</label>
						</div>
						<div class="form-group fail-appeal">
							<label class="col-md-1 control-label"></label>
							<div class="col-md-10">
								<textarea name="describes" style="width:100%;height:100px;" readonly></textarea>
							</div>
						</div>
						<div class="form-group fail-appeal">
							<label class="col-md-1 control-label"></label>
							<div class="col-md-10">
								<div id="imgs" class="imgs-container"></div>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">申诉时间</label>
							<div class="col-md-10">
								<input type="text" name="appealCreateDate" class="form-control" disabled />
							</div>
						</div>
						<div class="form-group suc-appeal">
							<label class="col-md-2 control-label">申诉原因</label>
						</div>
						<div class="form-group suc-appeal">
							<label class="col-md-1 control-label"></label>
							<div class="col-md-10">
								<textarea name="reason" style="width:100%;height:100px;" readonly></textarea>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-2 control-label">申诉内容</label>
						</div>
						<div class="form-group">
							<label class="col-md-1 control-label"></label>
							<div class="col-md-10">
								<textarea name="appealDescribes" style="width:100%;height:100px;" readonly></textarea>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-1 control-label"></label>
							<div class="col-md-10">
								<div id="appealImgs" class="imgs-container"></div>
							</div>
						</div>
						<div class="form-group fail-appeal">
							<label class="col-md-2 control-label">参赛人资料</label>
						</div>
						<div class="form-group fail-appeal">
							<label class="col-md-2 control-label">领奖者号码：</label>
							<div class="col-md-10">
								<input type="text" class="form-control" name="telephone" disabled />
							</div>
						</div>
						<div class="form-group fail-appeal">
							<label class="col-md-2 control-label">QQ号码：</label>
							<div class="col-md-10">
								<input type="text" class="form-control" name="qq" disabled />
							</div>
						</div>
						<div class="form-group fail-appeal">
							<label class="col-md-2 control-label">游戏账号：</label>
							<div class="col-md-10">
								<input type="text" class="form-control" name="gameAccount" disabled />
							</div>
						</div>
						<div class="form-group fail-appeal">
							<label class="col-md-2 control-label">比赛区服：</label>
							<div class="col-md-10">
								<input type="text" class="form-control" name="server" disabled />
							</div>
						</div>
						<div class="form-group fail-appeal">
							<label class="col-md-2 control-label">赛事资料</label>
						</div>
						<div class="form-group fail-appeal">
							<label class="col-md-2 control-label">赛事名：</label>
							<div class="col-md-10">
								<input type="text" class="form-control" name="activityName" disabled />
							</div>
						</div>
						<div class="form-group fail-appeal">
							<label class="col-md-2 control-label">比赛时间：</label>
							<div class="col-md-10">
								<input type="text" class="form-control" name="startDate" disabled />
							</div>
						</div>
						<div class="form-group fail-appeal">
							<label class="col-md-2 control-label">比赛奖励：</label>
							<div class="col-md-10">
								<input type="text" class="form-control" name="activityReward" disabled />
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
	// 初始化tooltip
	$('button[data-toggle="tooltip"]').tooltip();
	
	// 打开模态框
	function showEditor(title, $editor) {
		$editor.find('.modal-title').html(title);
		$editor.modal('show');
	}
	
	// 关闭模态框
	function hideEditor() {
		$editor.modal('hide');
	}
	
	// 审核
	function verify(id) {
		$.api('${ctx}/amuse/verify/detail', {id: id}, function(d) {
			var $editor = $('#modal-verify-editor');
			var o = d.object;
			var startDateStr = '';
			if(o.startDate) {
				startDateStr = new Date(o.startDate).Format('yyyy-MM-dd hh:mm:ss');
			}
			var createDateStr = '';
			if(o.createDate) {
				createDateStr = new Date(o.createDate).Format('yyyy-MM-dd hh:mm:ss');
			}
			$.fillForm({
				id: o.id,
				createDate: createDateStr,
				describes: o.describes,
				telephone: o.telephone,
				qq: o.qq,
				gameAccount: o.gameAccount,
				server: o.server,
				activityName: o.activityName,
				startDate: startDateStr,
				activityReward: o.activityReward,
			}, $editor);
			
			// 显示图片
			function img(src) {
				return '<a href="${imgServer!}' + src + '" target="_blank"><img src="${imgServer!}/' + src + '" /></a>';
			}
			var imgs = o.imgs;
			var $imgs = $editor.find('#imgs');
			$imgs.html('');
			if(imgs && imgs.length > 0) {
				for(var i=0; i<imgs.length; i++) {
					var imgObj = imgs[i];
					$imgs.append(img(imgObj.img));
				}
			} else {
				$imgs.append('未上传图片');
			}
			
			$('select[name="operate"]').change();
			$('select[name="remark"]').change();
			showEditor('比赛名称：' + o.activityName, $editor);
		}, function(d) {
			alert('请求数据异常：' + d.result);
		}, {
			async: false,
		});
	}
	
	// 申诉
	function appeal(id) {
		$.api('${ctx}/amuse/appeal/detail', {id: id}, function(d) {
			var $editor = $('#modal-appeal-editor');
			var o = d.object;
			var createDateStr = null;
			if(o.createDate) {
				createDateStr = new Date(o.createDate).Format('yyyy-MM-dd hh:mm:ss');
			}
			var appealCreateDateStr = null;
			if(o.appealCreateDate) {
				appealCreateDateStr = new Date(o.appealCreateDate).Format('yyyy-MM-dd hh:mm:ss');
			}
			var startDateStr = null;
			if(o.startDate) {
				startDateStr = new Date(o.startDate).Format('yyyy-MM-dd hh:mm:ss');
			}
			$.fillForm({
				id: o.id,
				createDate: createDateStr,
				describes: o.describes,
				telephone: o.telephone,
				qq: o.qq,
				gameAccount: o.gameAccount,
				server: o.server,
				activityName: o.activityName,
				startDate: startDateStr,
				activityReward: o.activityReward,
				appealCreateDate: appealCreateDateStr,
				appealDescribes: o.appealDescribes,
				appealDescribes: o.remark,
				reason: o.reason,
			}, $editor);
			
			// 显示图片
			function img(src) {
				return '<a href="${imgServer!}' + src + '" target="_blank"><img src="${imgServer!}/' + src + '" /></a>';
			}
			var imgs = o.imgs;
			var $imgs = $editor.find('#imgs');
			$imgs.html('');
			if(imgs && imgs.length > 0) {
				for(var i=0; i<imgs.length; i++) {
					var imgObj = imgs[i];
					$imgs.append(img(imgObj.img));
				}
			} else {
				$imgs.append('未上传图片');
			}
			
			var appealImgs = o.appealImgs;
			var $appealImgs = $editor.find('#appealImgs');
			$appealImgs.html('');
			if(appealImgs && appealImgs.length > 0) {
				for(var i=0; i<appealImgs.length; i++) {
					var imgObj = appealImgs[i];
					$appealImgs.append(img(imgObj.img));
				}
			} else {
				$appealImgs.append('未上传图片');
			}
			
			if(o.state == 2 || o.state == 6) {
				$editor.find('.fail-appeal').show();
				$editor.find('.suc-appeal').hide();
			} else {
				$editor.find('.fail-appeal').hide();
				$editor.find('.suc-appeal').show();
			}
			
			$('select[name="remark"]').change();
			showEditor('比赛名称：' + o.activityName, $editor);
		}, function(d) {
			alert('请求数据异常：' + d.result);
		}, {
			async: false,
		});
	}
	</script>
</body>
</html>