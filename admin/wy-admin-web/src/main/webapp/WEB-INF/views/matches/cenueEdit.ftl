<html>
	<head>
		<script type="text/javascript" editor-ctx="${ctx!}" src="${ctx!}/static/plugin/editor.js"></script>
		<script type="text/javascript" src="${ctx}/static/js/jquery.form.js"></script>
		<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
	</head>
	<body>
		<div class="mb10 col-md-10">
			<form id="search" action="1" method="post">
				<input type="hidden" name="processId" id="processId" value="${(params.processId)!}">
				<input type="hidden" name="currentProcessId" id="currentProcessId" value="${(params.processId)!}">
				<input type="hidden" name="matchesId" id="matchesId" value="${(params.matchesId)!}">
				<input type="hidden" name="currentNetbarId" id="currentNetbarId">
				<input type="hidden" name="checkedId" id="checkedId" value="${(checkedIds)!}">
				<input type="hidden" name="startDate" id="startDate" value="${(startDate)!}">
				<input type="hidden" name="endDate" id="endDate" value="${(endDate)!}">
				<div class="mb12 col-md-12">
					<div class="col-md-3">
						<input class="form-control" type="text" name="netbarName" maxlength="20" placeholder="网吧名称"  value="${(params.netbarName)!}"/>
					</div>
					<div class="col-md-2">
						<select class="form-control" name="proviceName" id="proviceName">
							<option value="">全国</option>
							<#if areaList??>
							<#list areaList as i>
								<option value="${i.areaCode!}"<#if (params.proviceName)?? && params.proviceName == i.areaCode?string> selected</#if>>${i.name!}</option>
							</#list>
							</#if>
						</select>
					</div>
					<div class="col-md-2">
						<select class="form-control" name="cityName" id="cityName">
							<option value="">全市</option>
							<#if cityList??>
							<#list cityList as i>
								<option value="${i.areaCode!}"<#if (params.cityName)?? && params.cityName == i.areaCode?string> selected</#if>>${i.name!}</option>
							</#list>
							</#if>
						</select>
					</div>
					<div class="col-md-2">
						<select class="form-control" name="townName" id="townName">
							<option value="">全区</option>
							<#if townList??>
							<#list townList as i>
								<option value="${i.areaCode!}"<#if (params.townName)?? && params.townName == i.areaCode?string> selected</#if>>${i.name!}</option>
							</#list>
							</#if>
						</select>
					</div>
					<button query type="submit" class="btn btn-success">查询</button>
				</div>
			</form>
		</div>	
		<article id="editor">
			<form class="form-horizontal" role="form" method="post">
				<div class="mb12 col-md-12">
					<div class="col-md-2">
						<select class="form-control" name="processIds" id="processIds">
							<#if processList??>
							<#list processList as i>
								<option value="${i.id!}"<#if (params.processId)?? && params.processId == i.id> selected</#if>>${i.name!}</option>
							</#list>
							</#if>
						</select>
					</div>
				</div>
				<table class="table table-striped table-hover">	
					<tr>
						<th></th>
						<th>网吧名称</th>
						<th>所在省</th>
						<th>所在城市</th>
						<th>所在区县</th>
						<th>网吧地址</th>
						<th>比赛日选择</th>
						<th>对应赛区</th>
					</tr>
					<#if list??>
						<#list list as o>
							<tr>
								<td><label><input name="checkId" type="checkbox" value="${o.id!}" <#if (o.checked)??&& o.checked ==1>checked="checked"</#if>/></label></td>
								<td>${(o.name)!}</td>
								<td>${(o.firName)!}</td>
								<td>${(o.secName)!}</td>
								<td>${(o.thiName)!}</td>
								<td>${(o.address)!}</td>
								<td>
								<div class="fightDates">
									<#if o.fightDate??>
										<#list o.fightDate as i>
											<div class="fightDate">
												<input type="text" class="form-control" value="${(i)!}" id="fightDate" name="fightDate" placeholder="请输入比赛时间" wy-required="比赛时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd'})" />
											</div>
										</#list>
									<#else>
										<div class="fightDate">
											<input type="text" class="form-control" id="fightDate" name="fightDate" placeholder="请输入比赛时间" wy-required="比赛时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd'})" />
										</div>
									</#if>
									<div class="form-group"><label class="col-md-2 control-label"></label><div class="col-md-4"><button id="addDate" type="button" class="btn btn-success">添加</button></div></div>
								</div>
								</td>
								<td><input type="text" name="division" value="${(o.division)!}"></td>
							</tr>
						</#list>
					</#if>
				</table>
			</form>
		</article>
	<#-- 分页 -->
	<#macro genpage startPage endPage>
		<#list startPage..endPage as p>
			<li<#if currentPage == p> class="active"</#if>>
				<a href="javascript:<#if currentPage == p>void(0)<#else>go(${p})</#if>;">${p}</a>
			</li>
		</#list>
	</#macro>
	<#assign btnCount=5>
	<div class="clearfix">
		<div class="pull-right" style="text-align: right;">
			第${(currentPage gt totalPage)?string(totalPage, currentPage)}页,共 ${totalPage}页,共${totalCount}条记录
			<div style="text-align: right;">
				<ul class="pager">
					<#if currentPage gt 1>
						<li class="previous">
							<a href="javascript:go(1);">|&lt;</a>
						</li>
						<li>
							<a href="javascript:go(${currentPage-1});">&lt;</a>
						</li>
					<#else>
						<li class="previous disabled">
							<a href="javascript:void(0);">|&lt;</a>
						</li>
						<li class="disabled">
							<a href="javascript:void(0);">&lt;</a>
						</li>
					</#if>
					
					<#if currentPage lte btnCount>
						<#if (btnCount * 2) lt totalPage>
							<@genpage startPage=1 endPage=(btnCount * 2) />
							<li>
								<a href="javascript:go(${btnCount + 1});">...</a>
							</li>
						<#else>
							<@genpage startPage=1 endPage=(totalPage) />
						</#if>
					<#elseif currentPage gt (totalPage - btnCount)>
						<#if (btnCount * 2) lt totalPage>
							<li>
								<a href="javascript:go(${totalPage - btnCount - 1});">...</a>
							</li>
							<@genpage startPage=(totalPage - btnCount * 2) endPage=totalPage />
						<#else>
							<@genpage startPage=1 endPage=totalPage />
						</#if>
					<#else>
						<li>
							<a href="${currentPage - btnCount}">...</a>
						</li>
						<@genpage startPage=(currentPage - btnCount + 1) endPage=(currentPage + btnCount - 1) />
						<li>
							<a href="javascript:go(${currentPage + btnCount});">...</a>
						</li>
					</#if>
					
					<#if currentPage lt totalPage>
						<li>
							<a href="javascript:go(${currentPage+1});">&gt;</a>
						</li>
						<li class="next">
							<a href="javascript:go(${totalPage});">&gt;|</a>
						</li>
					<#else>
						<li class="disabled">
							<a href="javascript:void(0);">&gt;</a>
						</li>
						<li class="next disabled">
							<a href="javascript:void(0);">&gt;|</a>
						</li>
					</#if>
				</ul>
			</div>
		</div>
	</div>
	<div class="clearfix">
		<div class="pull-right" style="text-align: right;">
			<a type="button" class="btn btn-info" onclick="saveLine(1);">暂时保存下线状态</a>
			<a type="button" class="btn btn-info" onclick="saveLine(2);">确认发布</a>
		</div>
	</div>
	<script type="text/javascript">
		$('button[query]').on('click',function(ev){
			var currentNetbarId="";
			var flag=true;
			var startDate=$("#startDate").val();
			var endDate=$("#endDate").val();
			//存储信息
			$('#editor').find('tr').each(function(){
				if($(this).find("input[name='checkId']").is(':checked')){
					var netbardates="";
					$(this).find("input[name='fightDate']").each(function(){
						if($(this).val()!="" && ($(this).val()<startDate || $(this).val()>endDate)){
							alert("比赛时间不在该赛程范围内！");
							flag=false;
							return false;
						}
						netbardates+=$(this).val()+"；";
					});
					
					if(netbardates==""){
						alert("所选比赛日不能为空！");
						flag=false;
						return false;
					}
					if($(this).find("input[name='division']").val()==""){
						alert("所选对应赛区不能为空！");
						flag=false;
						return false;
						
					}
					if(currentNetbarId==""){
						currentNetbarId=":";
					}
					currentNetbarId+=$(this).find("input[name='checkId']").val()+","+netbardates+","+($(this).find("input[name='division']").val()!=""?$(this).find("input[name='division']").val():-1)+":";
				}
			});
			if(!flag){
				return false;
			}
			$("#currentNetbarId").val(currentNetbarId);
			$("#currentProcessId").val($("#processIds").val());
			var $search = $('#search');
			$search.submit();
		});
		function go(page) { 
			var currentNetbarId="";
			var flag=true;
			var startDate=$("#startDate").val();
			var endDate=$("#endDate").val();
			//存储信息
			$('#editor').find('tr').each(function(){
				if($(this).find("input[name='checkId']").is(':checked')){
					var netbardates="";
					$(this).find("input[name='fightDate']").each(function(){
						if($(this).val()!="" && ($(this).val()<startDate || $(this).val()>endDate)){
							alert("比赛时间不在该赛程范围内！");
							flag=false;
							return false;
						}
						netbardates+=$(this).val()+"；";
					});
					
					if(netbardates==""){
						alert("所选比赛日不能为空！");
						flag=false;
						return false;
					}
					if($(this).find("input[name='division']").val()==""){
						alert("所选对应赛区不能为空！");
						flag=false;
						return false;
						
					}
					if(currentNetbarId==""){
						currentNetbarId=":";
					}
					currentNetbarId+=$(this).find("input[name='checkId']").val()+","+netbardates+","+($(this).find("input[name='division']").val()!=""?$(this).find("input[name='division']").val():-1)+":";
				}
			});
			if(!flag){
				return false;
			}
			$("#currentNetbarId").val(currentNetbarId);
			$("#currentProcessId").val($("#processIds").val());
			var $search = $('#search');
			$search.attr('action', page);
			$search.submit();
		}
		$('#editor').find("select[name='processIds']").change(function() {
			var currentNetbarId="";
			var flag=true;
			var startDate=$("#startDate").val();
			var endDate=$("#endDate").val();
			//存储信息
			$('#editor').find('tr').each(function(){
				if($(this).find("input[name='checkId']").is(':checked')){
					var netbardates="";
					$(this).find("input[name='fightDate']").each(function(){
						if($(this).val()!="" && ($(this).val()<startDate || $(this).val()>endDate)){
							alert("比赛时间不在该赛程范围内！");
							flag=false;
							return false;
						}
						netbardates+=$(this).val()+"；";
					});
					
					if(netbardates==""){
						alert("所选比赛日不能为空！");
						flag=false;
						return false;
					}
					if($(this).find("input[name='division']").val()==""){
						alert("所选对应赛区不能为空！");
						flag=false;
						return false;
						
					}
					if(currentNetbarId==""){
						currentNetbarId=":";
					}
					currentNetbarId+=$(this).find("input[name='checkId']").val()+","+netbardates+","+($(this).find("input[name='division']").val()!=""?$(this).find("input[name='division']").val():-1)+":";
				}
			});
			if(!flag){
				$("#processIds").val($("#processId").val());
				return false;
			}
			$("#currentNetbarId").val(currentNetbarId);
			$("#currentProcessId").val($("#processIds").val());
			var $search = $('#search');
			var page=1;
			$search.attr('action', page);
			$search.submit();
			
		});
		$('#search').find("select[name='proviceName']").change(function() {
			$.ajax({
				url: '${ctx!}/area/areaInfo?areaCode='+$('#search').find("select[name='proviceName']").val(),
				success: function(d) {
					if(d.code==0){
						var list=d.object;
						var html="<option value=''>全市</option>";
						for(var i=0; i<list.length; i++) {
							var area=list[i];
							html+='<option value="'+area.areaCode+'">'+area.name+'</option>';
						}
						$("#cityName").empty();
						$("#cityName").append(html);
					}
				},
				async: false,
			});
		});
		
		$('#search').find("select[name='cityName']").change(function() {
			$.ajax({
				url: '${ctx!}/area/areaInfo?areaCode='+$('#search').find("select[name='cityName']").val(),
				success: function(d) {
					if(d.code==0){
						var list=d.object;
						var html="<option value=''>全区</option>";
						for(var i=0; i<list.length; i++) {
							var area=list[i];
							html+='<option value="'+area.areaCode+'">'+area.name+'</option>';
						}
						$("#townName").empty();
						$("#townName").append(html);
					}
				},
				async: false,
			});
		});
		
		
		function saveLine(type){
			var currentNetbarId="";
			var flag=true;
			var startDate=$("#startDate").val();
			var endDate=$("#endDate").val();
			//存储信息
			$('#editor').find('tr').each(function(){
				if($(this).find("input[name='checkId']").is(':checked')){
					var netbardates="";
					$(this).find("input[name='fightDate']").each(function(){
						if($(this).val()!="" && ($(this).val()<startDate || $(this).val()>endDate)){
							alert("比赛时间不在该赛程范围内！");
							flag=false;
							return false;
						}
						netbardates+=$(this).val()+"；";
					});
					
					if(netbardates==""){
						alert("所选比赛日不能为空！");
						flag=false;
						return false;
					}
					if($(this).find("input[name='division']").val()==""){
						alert("所选对应赛区不能为空！");
						flag=false;
						return false;
						
					}
					if(currentNetbarId==""){
						currentNetbarId=":";
					}
					currentNetbarId+=$(this).find("input[name='checkId']").val()+","+netbardates+","+($(this).find("input[name='division']").val()!=""?$(this).find("input[name='division']").val():-1)+":";
				}
			});
			if(!flag){
				return false;
			}
			$("#currentNetbarId").val(currentNetbarId);
			$("#currentProcessId").val($("#processIds").val());
			$.ajax({
				url: '${ctx!}/matchesCenue/saveLine',
				data: {currentNetbarId:currentNetbarId, processId:$("#processId").val(),checkedId:$("#checkedId").val(),type:type,flag:true},
             	dataType: "json",
				success: function(d) {
					if(d.code==0){
						var processHtml="";
						for(var i=0;i<d.object.processList.length;i++){
							processHtml+='<div class="form-group">'
										+'	<label class="col-md-2 control-label">赛程名</label>'
										+'	<div class="col-md-10">'
										+'		<input class="form-control" type="text" value="'+d.object.processList[i].name+'">'
										+'	</div>'
										+'</div>'
										+'<div class="form-group">'
										+'	<label class="col-md-2 control-label">赛程开始时间</label>'
										+'	<div class="col-md-10">'
										+'		<input class="form-control" type="text" value="'+d.object.processList[i].start_date+'">'
										+'	</div>'
										+'</div>'
										+'<div class="form-group">'
										+'	<label class="col-md-2 control-label">赛程结束时间</label>'
										+'	<div class="col-md-10">'
										+'		<input class="form-control" type="text" value="'+d.object.processList[i].end_date+'">'
										+'	</div>'
										+'</div>';
						}
						var html='<input type="hidden" id="type" value="'+d.object.type+'"/>'
								+'<input type="hidden" id="processIdss" value="'+d.object.processId+'"/>'
								+'<div class="form-group">'
								+'	<label class="col-md-2 control-label">赛事名称</label>'
								+'	<div class="col-md-10">'
								+'		<input class="form-control" type="text" value="'+d.object.matcheName+'">'
								+'	</div>'
								+'</div>'
								+'<div class="form-group">'
								+'	<label class="col-md-2 control-label">游戏名称</label>'
								+'	<div class="col-md-10">'
								+'		<input class="form-control" type="text" value="'+d.object.itemName+'">'
								+'	</div>'
								+'</div>'
								+'<div class="form-group">'
								+'	<label class="col-md-2 control-label">主办方</label>'
								+'	<div class="col-md-10">'
								+'		<input class="form-control" type="text" value="'+d.object.organiserName+'">'
								+'	</div>'
								+'</div>'
								+processHtml
								+'<div class="form-group">'
								+'	<label class="col-md-2 control-label">赛点数量</label>'
								+'	<div class="col-md-10">'
								+'		<input class="form-control" type="text" value="'+d.object.cenueCount+'">'
								+'	</div>'
								+'</div>'
								+'<div class="form-group">'
								+'	<label class="col-md-2 control-label">省份数</label>'
								+'	<div class="col-md-10">'
								+'		<input class="form-control" type="text" value="'+d.object.provinceCount+'">'
								+'	</div>'
								+'</div>';
						$("#addContent").empty();
						$("#addContent").append(html);
						$("#modal-editor").modal('show');
					}else{
						alert(d.result);
					}
				},
				async: false,
			});
		}
		
		$(".fightDates").find("#addDate").on('click',function(ev){
			$(this).parents('.form-group').before(getHtml());
		});
		
		function getHtml(){
			var html='<div class="fightDate">'
					+'	<input type="text" class="form-control" id="fightDate" name="fightDate" placeholder="请输入比赛时间" wy-required="比赛时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:\'yyyy-MM-dd\'})" />'
					+'</div>';
			return html;
		}
		
	</script>
	
	<div id="modal-editor" class="modal fade">
		<div class="modal-dialog" style="width:800px;">
			<div class="modal-content">
				<div class="modal-body">
					<form class="form-horizontal form-condensed" role="form" method="post" id="addContent">
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-primary" onclick="submitEditor()">确定</button>
				</div>
			</div>
		</div>
	</div>
	
	<script type="text/javascript">
		function hideEditor(){
			$("#modal-editor").modal("hide");
		}
		
		function submitEditor(){
				$.ajax({
				url: '${ctx!}/matchesCenue/saveLine',
				data: {processId:$("#processIdss").val(),type:$("#type").val(),flag:false},
             	dataType: "json",
				success: function(d) {
					if(d.code==0){
						window.location.href='/matches/list/1';
					}
				}
				});
		}
	</script>
	
	
	
	</body>
</html>