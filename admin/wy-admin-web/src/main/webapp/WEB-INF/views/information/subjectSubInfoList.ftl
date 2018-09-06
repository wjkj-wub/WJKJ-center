<#import "/macros/pager.ftl" as p >
<html>
	<head>
		<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
		<style type="text/css">
			form .mb12.col-md-12 {
				display: inline-block;
			}
		</style>
	</head>
	<body>
		<#-- 导航 -->
		<ul class="breadcrumb">
			<li>
				<i class="icon icon-location-arrow mr10"></i>
				<strong>资讯管理</strong>
			</li>
			<li class="active">
				专题资讯 (${(subject.title)!})
			</li>
		</ul>
		
		<#-- 搜索 -->
		<div class="mb12">
			<form id="search" action="1" method="post">
				<div class="mb12 col-md-12">
					<input type="hidden" name="pid" value="${(params.pid)!}" />
					<div class="col-md-2">
						<input class="form-control" type="text" name="id" maxlength="5" placeholder="ID" value="${(params.id)!}" /	>
					</div>
					<div class="col-md-2">
						<input class="form-control" type="text" name="title" maxlength="30" placeholder="标题" value="${(params.title)!}" />
					</div>
					<div class="col-md-2">
						<select class="form-control" name="type">
							<option value="">全部类型</option>
							<option value="1"<#if (params.type!"0") == "1"> selected</#if>>资讯</option>
							<option value="3"<#if (params.type!"0") == "3"> selected</#if>>图集</option>
						</select>
					</div>
					<div class="col-md-6">
						<div class="col-md-4">
							<select class="form-control" name="efficient">
								<option value="">全部状态</option>
								<option value="1"<#if (params.efficient!"0") == "1"> selected</#if>>生效</option>
								<option value="2"<#if (params.efficient!"0") == "2"> selected</#if>>待生效</option>
								<option value="3"<#if (params.efficient!"0") == "3"> selected</#if>>失效</option>
							</select>
						</div>
					</div>
					
				</div>
				<div class="mb12 col-md-12">
					<div class="col-md-2">
						<input class="form-control" type="text" id="beginDate" name="beginDate" maxlength="30" placeholder="创建时间(起)" value="${(params.beginDate)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',maxDate:'#F{$dp.$D(\'endDate\',{d:0});}'})" />
					</div>
					<div class="col-md-2">
						<input class="form-control" type="text" id="endDate" name="endDate" maxlength="30" placeholder="创建时间(止)" value="${(params.endDate)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',minDate:'#F{$dp.$D(\'beginDate\',{d:0});}'})" />
					</div>
					<div class="col-md-2">
						<input class="form-control" type="text" id="timerBeginDate" name="timerBeginDate" maxlength="30" placeholder="生效时间(起)" value="${(params.timerBeginDate)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',maxDate:'#F{$dp.$D(\'timerEndDate\',{d:0});}'})" />
					</div>
					<div class="col-md-2">
						<input class="form-control" type="text" id="timerEndDate" name="timerEndDate" maxlength="30" placeholder="生效时间(止)" value="${(params.timerEndDate)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',minDate:'#F{$dp.$D(\'timerBeginDate\',{d:0});}'})" />
					</div>
					<div class="col-md-4">
						<div class="col-md-6">
							<button class="btn btn-info" type="submit">提交</button>
							<a class="btn btn-info" href="1">清空</a>
						</div>
					</div>
				</div>
				<div class="mb12 col-md-12">
					<div class="col-md-4">
						<a class="btn btn-success" href="${ctx}/overActivity/subjectSub/edit?pid=${(subject.id)!}">新增资讯</a>
						<a class="btn btn-success" href="${ctx}/overActivity/list/2/1">返回专题资讯</a>
					</div>
				</div>
			</form>
			<script type="text/javascript">
				(function searchParamCheck() {
					var $search = $('#search');
					$search.on('submit', function(ev) {
						var beginDate = $(this).find('[name="beginDate"]').val();
						var endDate = $(this).find('[name="endDate"]').val();
						if((beginDate.length > 0 && endDate.length <= 0) || (beginDate.length <= 0 && endDate.length > 0)) {
							alert('请完整输入创建时间');
							return false;
						}
						
						var timerBeginDate = $(this).find('[name="timerBeginDate"]').val();
						var timerEndDate = $(this).find('[name="timerEndDate"]').val();
						if((timerBeginDate.length > 0 && timerEndDate.length <= 0) || (timerBeginDate.length <= 0 && timerEndDate.length > 0)) {
							alert('请完整输入生效时间');
							return false;
						}
					});
				}());
			</script>
		</div>
		
		<#-- 表格 -->
		<table class="table table-striped table-hover">	
			<tr>
				<th>ID</th>
				<th>图片</th>
				<th>标题</th>
				<th>类型</th>
				<th>创建时间</th>
				<th>阅读数</th>
				<th>创建人</th>
				<th>状态</th>
				<th>操作</th>
			</tr>
			<#if list??>
				<#list list as o>
					<tr>
						<td>${(o.id)!}</td>
						<td><#if (o.icon)??><img src="${imgServer!}${(o.icon)!}" style="width:50px;height:50px;" /></#if></td>
						<td>${(o.title)!}</td>
						<td>
							<#if (o.type)??>
								<#if o.type == 1>
									资讯
								<#elseif o.type == 2>
									专题
								<#elseif o.type == 3>
									图集
								<#elseif o.type == 4>
									视频
								</#if>
							</#if>
						</td>
						<td>${(o.createDate?string('yyyy-MM-dd HH:mm:ss'))!}</td>
						<td>${(o.readNum)!}</td>
						<td>${(o.creater)!}</td>
						<td>
							<#if (o.efficient)??>
								<#if o.efficient == 1>
									生效
								<#elseif o.efficient == 2>
									待生效
								<#else>
									失效
								</#if>
							</#if>
						</td>
						<td>
							<a type="button" class="btn btn-info" href="${ctx}/overActivity/subjectSub/edit?pid=${(subject.id)!}&id=${(o.id)!}">编辑</a>
							<button delete="${(o.id)!}" type="button" class="btn btn-danger">删除</button>
							<button show="${(o.id)!}" show-flag="${((o.efficient!0) == 1)?string('0', '1')}" type="button" class="btn btn-info">${((o.efficient!0) == 1)?string('失效', '生效')}</button>
						</td>
					</tr>
				</#list>
			</#if>
		</table>
		
		<#-- 分页 -->
		<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage /> 
		
		<!-- 操作中提示框 -->
		<div id="modal-operating" class="modal fade">
			<div class="modal-dialog">
				<div class="modal-content">
					<div class="modal-header">提示</div>
					<div class="modal-body" style="text-align:center;">
						<div><img src="${ctx}/static/images/loading.gif" /></div>
						处理中，请勿中断 ...
					</div>
				</div>
			</div>
		</div>
		<script type="text/javascript">
			// 初始化模态框
			$('#modal-operating').modal({
				keyboard : false,
				show : false,
				backdrop : 'static'
			});
			
			function operating() {
				$('#modal-operating').modal('show');
			}
			
			function complete() {
				$('#modal-operating').modal('hide');
			}
		</script>
		
		<script type="text/javascript">
			(function init() {
				$('[data-toggle="tooltip"]').tooltip({});
				
				// 删除
				$('[delete]').on('click', function(ev) {
					var $this = $(this);
					
					$.confirm('确认删除吗?', function() {
						$this.prop('disabled', true);
						var id = $this.attr('delete');
						
						operating();
						$.api('${ctx}/overActivity/delete', {'id': id}, function(d) {
							window.location.reload();
						}, false, {
							complete: function(xhr) {
								$this.prop('disabled', false);
								complete();
							}
						});
					});
				});
				
				// 置顶
				$('[top]').on('click', function(ev) {
					var $this = $(this);
					var id = $this.attr('top');
					var top = $this.attr('top-flag');
					
					operating();
					$.api('${ctx}/overActivity/top', {'id': id, 'top': top, 'listType': '${listType!}'}, function(d) {
						window.location.reload();
					}, false, {
						complete: function(xhr) {
							$this.prop('disabled', false);
							complete();
						}
					});
				});
				
				// 生效
				$('[show]').on('click', function(ev) {
					var $this = $(this);
					var id = $this.attr('show');
					var show = $this.attr('show-flag');
					
					operating();
					$.api('${ctx}/overActivity/show', {'id': id, 'show': show}, function(d) {
						window.location.reload();
					}, false, {
						complete: function(xhr) {
							$this.prop('disabled', false);
							complete();
						}
					});
				});
			}());
		</script>
	</body>
</html>