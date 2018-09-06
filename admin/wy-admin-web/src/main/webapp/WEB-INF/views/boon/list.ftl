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
				<strong>cdkey管理</strong>
			</li>
			<li class="active">
				cdkey查询
			</li>
		</ul>
		
		<#-- 搜索 -->
		<div class="mb12">
			<form id="search" action="1" method="post">
				<div class="mb12 col-md-12">
					<div class="col-md-2">
						<input class="form-control" type="text" name="cdkey" maxlength="30" placeholder="cdkey" value="${(params.cdkey)!}" />
					</div>
					<div class="col-md-2">
						<input class="form-control" type="text" name="production" maxlength="25" placeholder="用途" value="${(params.production)!}" /	>
					</div>
					<div class="col-md-2">
						<input class="form-control" type="text" id="createDateBegin" name="createDateBegin" maxlength="30" placeholder="创建时间(起)" value="${(params.createDateBegin)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',maxDate:'#F{$dp.$D(\'createDateEnd\',{d:0});}'})" />
					</div>
					<div class="col-md-2">
						<input class="form-control" type="text" id="createDateEnd" name="createDateEnd" maxlength="30" placeholder="创建时间(止)" value="${(params.createDateEnd)!}" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd',minDate:'#F{$dp.$D(\'createDateBegin\',{d:0});}'})" />
					</div>
					<div class="col-md-2">
						<select class="form-control" name="isUsed">
							<option value="">全部使用状态</option>
							<option value="1"<#if ((params.isUsed)!"-1") == "1"> selected</#if>>已用</option>
							<option value="0"<#if ((params.isUsed)!"-1") == "0"> selected</#if>>未用</option>
						</select>
					</div>
					<div class="col-md-2">
						<button class="btn btn-info" type="submit">提交</button>
						<a class="btn btn-info" href="1">清空</a>
					</div>
				</div>
				<div class="mb12 col-md-12">
					<div class="col-md-4">
						<a class="btn btn-success" href="${ctx}/overActivity/normal/edit">新增</a>
					</div>
				</div>
			</form>
		</div>
		
		<#-- 表格 -->
		<table class="table table-striped table-hover">	
			<tr>
				<th>ID</th>
				<th>cdkey</th>
				<th>用途</th>
				<th>类型</th>
				<th>额度</th>
				<th>过期时间</th>
				<th>创建时间</th>
				<th>使用情况</th>
			</tr>
			<#if list??>
				<#list list as o>
					<tr>
						<td>${(o.id)!}</td>
						<td>
							<#if (o.cdkey)??>
								<#assign substr = 3>
								<#if o.cdkey?length lte 6>
									<#assign substr = 1>
								</#if>
								<#assign listNum = o.cdkey?length-2*substr-1>
								${(o.cdkey)?substring(0, substr)}<#if listNum gte 0><#list 0..listNum as i>*</#list></#if>${(o.cdkey)?substring(o.cdkey?length-substr, o.cdkey?length)}
							</#if>
						</td>
						<td>${(o.production)!}</td>
						<td>
							<#if (o.type)??>
								<#if o.type == 1>
									红包
								<#elseif o.type == 2>
									金币
								<#elseif o.type == 3>
									皮肤CDKEY(网娱)
								<#elseif o.type == 4>
									皮肤CDKEY(腾讯)
								</#if>
							</#if>
						</td>
						<td>${(o.amount)!}</td>
						<td>${(o.expiredDate?string('yyyy-MM-dd HH:mm:ss'))!}</td>
						<td>${(o.createDate?string('yyyy-MM-dd HH:mm:ss'))!}</td>
						<td>
							<#if (o.usedDate)??>
								已用
							<#else>
								未用
							</#if>
						</td>
					</tr>
				</#list>
			</#if>
		</table>
		
		<#-- 分页 -->
		<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage /> 
	</body>
</html>