<!DOCTYPE html>
<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <meta name="full-screen" content="yes">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta name="apple-itunes-app" content="">
    <meta name="format-detection" content="telephone=no">
    <meta name="keywords" content="">
    <meta itemprop="name" content="">
    <meta name="description" itemprop="description" content="">
    <meta itemprop="image" content="">
    <link rel="stylesheet" href="${ctx}/static/css/message.css">
    <script async src="http://c.cnzz.com/core.php"></script>
	<title>服务被购买确认</title>
</head>
<body>
	<header>服务被购买确认</header>
    <section class="onshelf-name">
        <h3 class="title"><i></i>被购买项目</h3>
        <div class="wrap">
            <div class="name">${(info.name)!}</div> 
            <div class="label">
            <span class="item-name">被购买项目：</span>
                <ul class="item-list">
                    <li>${(info.sub_name)!}</li>
                </ul>
            </div>
            <div class="label">
            <span class="item-name">服务时间：</span>
                <ul class="item-list">
                    <li class="time">${(info.settl_date)!}</li>
                </ul>
            </div>
        </div>
    </section>
	<section class="onshelf-item">
		<h3 class="title"><i></i>购买者信息</h3>
        <div class="wrap">
           <div class="label"><span>网吧名称：</span>${(info.netbar_name)!}</div>
           <div class="label"><span>联系电话：</span>${(info.telephone)!}</div>
           <div class="label"><span>网吧地址：</span>${(info.address)!}</div>
           <div id="allmap"></div>
           <script type="text/javascript" src="http://api.map.baidu.com/api?v=2.0&ak=mcDGbWtXbxVhOSkKRsboFTjG"></script>
           <script type="text/javascript">
                // 百度地图API功能
               	var map = new BMap.Map("allmap");
                var point = new BMap.Point(${(info.longitude)!},${(info.latitude)!});
                map.centerAndZoom(point, 15);
                map.disableDragging();  
                var marker = new BMap.Marker(point);  // 创建标注
                map.addOverlay(marker);               // 将标注添加到地图中
                marker.setAnimation(BMAP_ANIMATION_BOUNCE); //跳动的动画
            </script>
        </div>
	</section>
    <section>
        <h3 class="title"><i></i>服务须知</h3>
        <div class="wrap">
            <div class="word">
            ${(info.description)!}
            </div>
            <!-- <a href="javascript:;" class="more">查看全部说明</a> -->
        </div>
    </section>
    <button class="confirm"><#if (info.status)?? && (info.status=0||info.status=2)>我确认执行此次服务<#else>已确认</#if></button>
    <script src="js/zepto.js"></script>
    <script type="text/javascript" src="${ctx}/static/js/jquery.min.js"></script>
    <script>
    $(".confirm").click(function(){
    if(${(info.status)!0}==0||${(info.status)!0}==2){
    $.ajax({
    type:'post',
    url:'${ctx}/netbar/resource/confirmB?id='+'${(info.id)!}',
    success : function(data) {
    if(data==0)
    window.location.reload();
    }
    })
    }
    })
    </script>
</body>
</html>