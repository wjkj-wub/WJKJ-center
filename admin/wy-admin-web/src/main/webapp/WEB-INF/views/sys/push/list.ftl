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
			推送管理
		</li>
	</ul>
	<#-- 搜索 -->
	<div class="mb10">
		<form id="search" action="1" method="post">
			<div class="col-md-2">
				<input type="text" name="title" value="${(params.title)!}" class="form-control" placeholder="标题">
			</div>
			<div class="col-md-2">
				<input type="text" name="startDate" id="startDate" value="${(params.startDate)!}" class="form-control" placeholder="开始时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss'})">
			</div>
			<div class="col-md-2">
				<input type="text" name="endDate" id="endDate" value="${(params.endDate)!}" class="form-control" placeholder="结束时间" onclick="WdatePicker({isShowClear:true,readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:ss'})">
			</div>
			<div class="col-md-2">
				<select class="form-control" name="infoType">
					<option value=""<#if (params.infoType!0) == 0> selected</#if>>全部</option>
					<option value="1"<#if (params.infoType!0) == 1> selected</#if>>系统消息</option>
					<option value="2"<#if (params.infoType!0) == 2> selected</#if>>资讯</option>
					<option value="3"<#if (params.infoType!0) == 3> selected</#if>>官方赛</option>
					<option value="4"<#if (params.infoType!0) == 4> selected</#if>>娱乐赛</option>
					<option value="5" <#if (params.infoType!0) == 5> selected</#if>> 约战</option>
					<option value="6" <#if (params.infoType!0) == 6> selected</#if>> 金币商城</option>
					<option value="7" <#if (params.infoType!0) == 7> selected</#if>> 金币商城商品</option>
					<option value="8" <#if (params.infoType!0) == 8> selected</#if>> 金币任务</option>
					<option value="9" <#if (params.infoType!0) == 9> selected</#if>> 悬赏令</option>
				</select>
			</div>
			<button type="submit" id="query" class="btn btn-success">查询</button>
			<a  href="/push/pushPage" class="btn btn-primary">新增消息</a>
		</form>
	</div>
	<#-- 表格 -->
	<table class="table table-striped table-hover">	
		<tr>
			<th>标题</th>
			<th>内容</th>
			<th>推送对象</th>
			<th>用户id</th>
			<th>推送类型</th>
			<th>模块</th>
			<th>二级模块</th>
			<th>资讯</th>
			<th>推送时间</th>
		</tr>
		<#if list??>
		<#list list as o>
			<tr>
				<td>${o.title!}</td>
				<td>${o.content!}</td>
				<td>
					<#if (o.clientType!0) == 1>
						个人 
					<#elseif ((o.clientType)!0) == 2>
						全体 
					<#elseif ((o.clientType)!0) == 3>
						地区  
					</#if>
				</td>
				<td>${o.clientInfo!"全部"}</td>
				<td>
					<#if (o.infoType!0) == 1>
						系统消息
					<#elseif ((o.infoType)!0) == 2>
						资讯
					<#elseif ((o.infoType)!0) == 3>
						官方赛  
					<#elseif ((o.infoType)!0) == 4>
						娱乐赛
					<#elseif ((o.infoType)!0) == 5>
					    约战
					<#elseif ((o.infoType)!0) == 6>
					    金币商城
					<#elseif ((o.infoType)!0) == 7>
					    金币商城商品
					<#elseif ((o.infoType)!0) == 8>
					    金币任务
					<#elseif ((o.infoType)!0) == 9>
					    悬赏令
					</#if>
				</td>
				<td>${o.moduleName!"--"}</td>
				<td>${o.subModuleName!"--"}</td>
				<td>${(o.infoTitle)!"--"}</td>
				<td>${(o.createDate)!}</td>
			</tr>
		</#list>
		</#if>
	</table>
	<script type ="text/javascript">
//--------------------------form表单提交日期检查-------------------------
		$(document).ready(function(){ 
	        $(":submit[id=query]").click(function(check){ 
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
	            var start = StringToDate(startDate);
	            var end = StringToDate(endDate);
	            	if(compareDate(start,end)){
		            	alert("开始时间不能大于结束时间！"); 
		                check.preventDefault();//此处阻止提交表单 
		            }
	            }
	        }); 
    	});  
        function StringToDate(s) {

			var d = new Date();
			d.setYear(parseInt(s.substring(0,4),10));
			d.setMonth(parseInt(s.substring(5,7)-1,10));
			d.setDate(parseInt(s.substring(8,10),10));
			d.setHours(parseInt(s.substring(11,13),10));
			d.setMinutes(parseInt(s.substring(14,16),10));
			d.setSeconds(parseInt(s.substring(17,19),10));
			
			return d;
		} 
    	var compareDate = function(date1,date2){  
		   return date1> date2;  
		}

</script>
	<#-- 分页 -->
	<@p.pager currentPage=currentPage totalPage=totalPage totalCount=totalCount isLastPage=isLastPage />
</body>

</html>