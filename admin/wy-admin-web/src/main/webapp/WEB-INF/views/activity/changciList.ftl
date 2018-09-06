<#import "/macros/pager.ftl" as p >
<html>
<head>
	<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
	<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
	<script type="text/javascript" src="${ctx}/static/js/app.js"></script>
</head>
<body>	
	<div id="u164" class="text">
		<#if gameInfo??>
			<#list gameInfo as g>
				<p style="font-weight:bold;font-size:20px">${g.title!}<a style="font-size:15px" name="${imgServer!}/${g.qrcode!}" id="mainqr" >查看比赛二维码（可用于物料宣传）</a></p>
				<p>地区：${g.areaname!}</p>
				<p>报名开始时间：${g.begin_time!}</p>
				<p>比赛开始时间：${g.over_time!} &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;比赛结束时间：${g.end_time!}</p>			</#list>
		</#if>
		<br>
	</div>
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
			<input type="hidden" name="activityId" id="activityId" value="${activityId!}"/>
			<div class="col-md-2">
				<select class="form-control" name="areaInfoId">
					<option value="">全部地区</option>
					<#if areaInfo??>
						<#list areaInfo as m>
							<option value="${(m.area_code)!}"<#if (m.area_code)?? && areaInfoId??&&areaInfoId== m.area_code> selected</#if>>${(m.areaName)!}</option>
						</#list>
					</#if>
				</select>
			</div>
			<div class="col-md-2">
				<input type="text" class="form-control" name="overTime" id="overTime" placeholder="全部时间" value="${overTime!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',onpicked:beginPick})" />
			</div>
			<div class="col-md-2">
				<select class="form-control" name="netbarInfoId">
					<option value="">全部场次</option>
					<#if netbarInfo??>
						<#list netbarInfo as m>
							<option value="${(m.id)!}"<#if (m.id)?? && netbarInfoId??&&netbarInfoId== m.id> selected</#if>>${(m.netbarName)!}</option>
						</#list>
					</#if>
				</select>
			</div>
			
			<button type="submit" class="btn btn-success">查询</button>
			<a class="btn btn-info" clear="${activityId!}" href="1?activityId=${activityId!}">清空</a>
		</form>
	</div>
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>比赛地区</th>
			<th>比赛时间</th>
			<th>比赛场次</th>
			<th>场次二维码</th>
		</tr>
		<#if list??>
			<#list list as o>
				<tr>
					<td>${o.areaName!}</td>
					<td>${o.over_time?string("yyyy-MM-dd")}</td>
					<td>${(o.netbarName)!}</td>
					<td><button show="${imgServer!}/${o.qrcode!}" type="button" class="btn btn-info">查看大图</button>&nbsp;&nbsp;&nbsp;&nbsp;
					<a class="btn btn-info" href="${imgServer!}/${o.qrcode!}" download="" target="_blank">下载</a></td>
				</tr>
			</#list>
		</#if>
	</table>
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />	
	<script type="text/javascript">
	$('button[show]').on('click', function(event) {
	      var href=$(this).attr('show');
	     $('.modal-body img').attr("src" ,href);
	     $('.modal-footer a').attr("href" ,href);
		$('#modal-editor').modal('show');	
	});
	$('#mainqr').on('click', function(event) {
	    event.preventDefault();
	     var href=$('#mainqr').attr("name");
	     console.log(href);
	     $('.modal-body img').attr("src" ,href);
	     $('.modal-footer a').attr("href" ,href);
		$('#modal-editor').modal('show');	
	});
	
	</script>	
	<div id="modal-editor" class="modal fade">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" onclick="hideEditor()">
						<span>×</span><span class="sr-only">关闭</span>
					</button>
				</div>
				<div class="modal-body">
				<img />
				</div>
				<div class="modal-footer">
					<a  id="download" class="btn" download="" target="_blank" >下载二维码</a>
				</div>
			</div>
		</div>
	</div>
  <script type="text/javascript">
      $('input[name]').attr("autocomplete", "off");
	
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
  </script>	
</body>
</html>