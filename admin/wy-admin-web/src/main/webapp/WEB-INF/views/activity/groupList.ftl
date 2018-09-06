<#import "/macros/pager.ftl" as p >
<html>
<head>
<script type="text/javascript" src="${ctx!}/static/plugin/My97DatePicker/WdatePicker.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/ztree/js/jquery.ztree.all-3.5.min.js"></script>
<script type="text/javascript" src="${ctx!}/static/plugin/ztree/js/jquery.ztree.exhide-3.5.min.js"></script>
<link rel="stylesheet" type="text/css" href="${ctx!}/static/plugin/ztree/css/zTreeStyle.css"/>
<style type="text/css">
	li{
		list-style: none;
	}
	.content{
		overflow: auto;
	}
	.round{
		width: 280px;
		float: left;
	}
	.round ul{
		margin-bottom: 20px;
		position: relative;
		padding-left: 30px;
		box-sizing: border-box;
	}
	.round li{
		list-style: none;
		background: #ccc;
		line-height: 32px;
		margin-bottom: 1px;
		position: relative;
		cursor: pointer;
		padding:0;
		overflow:hidden; 
		white-space:nowrap; 
		text-overflow:ellipsis;
		width:230px;
		padding-right: 30px;
	}
	.round span{
		display: inline-block;
		width: 32px;
		height:32px;
		text-align: center;
	}
	.round .order{			
		background: #d7d7d7;
		border-right: 1px solid #fff;
		margin-right: 10px;
	}
	.arrow{
		position: absolute;
		right: 0;
	}
	.arrow i{
		display: inline-block;
		width:0;
		height: 0;
		border:10px solid transparent;
		border-top-color:#666;
		margin-top: 12px;
	}
	.groupNum{
		position: absolute;
		vertical-align: middle;
		left:0;
		top:25px;
	}
	.place{
		width: 198px;
		position: absolute;
		display: none;
		height:300px;
		overflow:auto;
	}
	::-webkit-scrollbar-track, ::-webkit-scrollbar-track:hover {
		background-color:rgba(228,228,228,1);
	}
	.place ul{
		text-align: center;
		padding:0;
		margin-bottom:0;
	}
	.place li{
		line-height: 35px;
		background: #e4e4e4;
		cursor: pointer;
	}
	.place li:hover{
		color:#ff6600; 
	}
	.rank-hover {
		color: #ff6600;
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
			个人分组
		</li>
	</ul>	
	<input type="hidden" id="activityId" value="${(params.activity_id)!}" />
	<input type="hidden" id="netbarId" value="${(params.netbar_id)!}" />
	<input type="hidden" id="round" value="${(params.round)!}" />
	
	<div id="div-group" class="mb12">
	</div>
	<div id="chooser-rank" class="place">
		<ul>
			<li data-place="1">第一名</li>
			<li data-place="2">第二名</li>
			<li data-place="3">第三名</li>
			<li data-place="4">第四名</li>
		</ul>
	</div>
	<div class="mb12">
		<a href="${ctx!}/activityInfo/apply/<#if (type!)==1>personal</#if><#if (type!)==2 >team</#if>/1?activityId=${(activityId)!}&round=${(round)!}&netbarId=${(netbarId)!}" class="btn btn-success">返回列表</a>
	</div>
	
	<script type="text/javascript">
	// 尝试获取分组列表
	var jsonStr = '${list!}';
	var list = false;
	try {
		list = JSON.parse(jsonStr);
	} catch(expect) {
	}
	
	// 初始化数据
	if(list && list.length > 0) {
		// 初始化分组信息
		$('#div-group').html('<div class="content"></div>');
		var $content = $('#div-group .content');
		
		var $round = false;
		var index=1;
		for(var i=0; i<list.length; i++) {
			var obj = list[i];
			if(i % 8 == 0) {
				$content.append('<div class="round"></div>');
				$round = $content.find('.round:last');
			}
			
			if(i % 2 == 0) {
				$round.append('<ul><span class="groupNum">'+obj.groupNumber+'</span></ul>');
				index++;
			}
			
			var rankHtml = '<span class="arrow"><i class="iconArrow"></i></span>';
			var rankAttr = ' group-id="' + obj.id + '"';
			if(obj.rank) {
				rankHtml = '<span class="arrow" style="background: #e4e4e4;">' + obj.rank + '</span>';
				rankAttr += ' rank="' + obj.rank + '"';
			}
			$round.find('ul:last').append('<li class="list"' + rankAttr + '><span class="order">' + obj.seatNumber + '</span>' + obj.targetName + rankHtml + '</li>');
		}
		
		// 初始化分组排名选择项
		var $rankChooser = $('#chooser-rank ul');
		$rankChooser.html('');
		for(var i=1; i<=list.length; i++) {
			$rankChooser.append('<li data-place="' + i + '">第' + i + '名</li>');
		}
	}
	
	// 编辑
	$('select[edit]').on('change', function(event) {
		// 初始化表单数据
		var id = $(this).attr('edit');
		var rank='';
		if($(this).val()!=''){
			rank="&rank="+$(this).val();
		}
		$.api('${ctx}/activityGroup/rank?id=' + id +rank, {}, function(d) {
			window.location.reload();
		}, function(d) {
			alert('请求数据异常：' + d.result);
		}, {
			async: false,
		});
		
		showEditor();
	});
	
	$(document).ready(function(){
		$(".round li").click(function(){
			$(".round li").removeClass("setPlace");
			$(this).addClass("setPlace");
			var navWid=$('.menu').width();
			var left=$(this).offset().left-navWid+21;
			var top=$(this).offset().top-42;
			$(".place").css({left:left,top:top});
			$(".place").show();
			
			var rank = $(this).attr('rank');
			if(rank && rank.length > 0) {
				$('#chooser-rank li').removeClass('rank-hover');
				$('#chooser-rank').find('li[data-place="' + rank + '"]').addClass('rank-hover');
			}
			var groupId = $(this).attr('group-id');
			if(groupId) {
				$('#chooser-rank').attr('group-id', groupId);
			}
		});
		$(".content").click(function(e){
			if(e.target.className!="list setPlace" && e.target.className!="arrow" && e.target.className!="iconArrow"){
				$(".place").hide();
			}
		});
		$(".place li").click(function(e){
			var place=$(this).attr("data-place");
			var groupId = $('#chooser-rank').attr('group-id');
			
			$.api('${ctx}/activityGroup/rank?id=' + groupId + '&rank=' +place, {}, function(d) {
				window.location.reload();
				$(".setPlace .arrow").css("background","#e4e4e4").html(place);
			}, function(d) {
				alert('请求数据异常：' + d.result);
			}, {
				async: false,
			});
		})
	})
	
	</script>
	
</body>
</html>