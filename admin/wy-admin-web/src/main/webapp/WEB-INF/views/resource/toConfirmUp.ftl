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
	<title>上架商品服务协议确认</title>
</head>
<body>
	<header>上架商品服务协议确认</header>
    <section class="onshelf-name">
        <div class="wrap">
           <div class="name">${(info.name)!}</div> 
           <div class="label"><span>服务地区：</span>${(info.area)!}</div>
           <div class="label"><span>服务级别：</span><#if (info.qualifications)??><#if info.qualifications=0>无<#elseif info.qualifications=1>会员网吧<#elseif info.qualifications=2>黄金网吧</#if></#if></div>
        </div>
    </section>
	<section class="onshelf-item">
		<h3 class="title"><i></i>上架项目</h3>
        <div class="wrap">
            <div class="label">
            <span class="item-name">名称：</span>
                <ul class="item-list">
                    <#list subs as sub>
                    <li>${sub}</li>
                    </#list>
                </ul>
            </div>
        </div>
	</section>
    <section class="attention">
        <h3 class="title"><i></i>购买说明</h3>
        <div class="wrap">
            <div class="word">
                ${(info.description)!}
            </div>
            <!-- <a href="javascript:;" class="more">查看全部说明</a> -->
        </div>
    </section>
    <section class="attention">
        <h3 class="title"><i></i>协议内容</h3>
        <div class="wrap">
            <div class="">
                <p>您的服务将在“网娱大师”之“资源商城”上架，请确认具体服务内容是否准确、联系方式是否准确。“资源商城”在收到您的确认后，才能上架发布，提供给网吧商户购买。</p>
				<p>上架须知：</p>
				<ul class="rule-list">
					<li>“网娱大师”承诺保证“资源商城”买方真实有效，是符合国家法规的正规场所。</li>
					<li>“网娱大师”承诺按照双方线下协议约定结算款项，不拖延、不拒付。</li>
					<li>您同意授权上述信息在“网娱大师”各终端的展示、呈现，方式包括但不限于图片、视频、图文介绍等。</li>
					<li>您承诺上述信息的服务内容是您可掌控、可提供的，除不可抗力外的违约责任归您承担。并提供服务相关的道具、服装、设备等，不向买方收取额外的费用。</li>
					<li>您承诺您的服务被购买后，能够按时保质保量的完成服务，以买方满意为持续提升服务品质的宗旨，诚信、合法的获取劳动报酬。</li>
					<li>您承诺您的服务是符合国家法规的，并承担违反相关法规所造成的一切后果。</li>
					<li>您承诺不会跳过“网娱大师”给网吧商户提供服务、收取费用，“网娱大师”也不会承担由此造成的一切后果（服务纠纷、结算纠纷、安全性等）</li>
				</ul>
            </div>
        </div>
    </section>
<#if status==1><button class="confirm">同意协议并确认上架信息</button><#else><button class="confirm disabled">已确认</button></#if>
    <script src="js/zepto.js"></script>
    <script type="text/javascript" src="${ctx}/static/js/jquery.min.js"></script>
    <script>
    $(".confirm").click(function(){
    if(${status}==1){
    $.ajax({
    type:'post',
    url:'${ctx}/netbar/resource/confirmUp?propertyIds='+'${propertyIds}',
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