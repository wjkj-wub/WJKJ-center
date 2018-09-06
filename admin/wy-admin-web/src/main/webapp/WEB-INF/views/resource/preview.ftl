<head>
	<link rel="stylesheet" type="text/css" href="${ctx!}/static/css/store.css">
	<style type="text/css">
		li {
			list-style: none;
		}
	</style>
</head>
<body>
	<div class="product">
		<div class="detail-box clearfix">
			<div class="detail-left">
				<div class="img-box">
					<img src="${imgServer}${commodity.url!"wy_share_icon.png"}" alt="${commodity.name}">
				</div>
			</div>
			<div class="detail-right">
				<h3>${(commodity.name)!}</h3>
				<div class="price">
					<div class="full-price">
						<span class="price-label">网娱价格</span>
						<span class="disabled-price">￥${(property.rebate?string("0.##"))!}</span>
					</div>
					<div class="discount">
						<span class="price-label">等级折扣价</span>
						<span class="less-price">
							￥
							<#assign totalAmount = (property.rebate)!0>
							${(totalAmount?string("0.##"))!}
						</span>
						<span class="deduction">奖金最高可抵 ${(totalAmount * commodity.useQuoRatio)!} 元</span>
					</div>
					<#if (property.qualifiType)??>
						<#if property.qualifiType == 1 || property.qualifiType == 2>
							<div class="discount">
								<span class="price-label">使用条件</span>
								<span class="yf_col00 yf_mrl5">
									<#if property.qualifiType == 1>
										满${property.conditions}元可以使用
									</#if>
									<#if property.qualifiType == 2>
										先购买${conditionsCommodity.name}
									</#if>
								</span>
							</div>
						</#if>
					</#if>
					<div class="total-sale">
						<div >累计销售</div>
						<div class="sale-account">${(areaAndSales.count)!0}</div>
					</div>
				</div>
				<div class="item">
					<div id="properties" class="item-group clearfix"></div>
					<div class="item-service clearfix">
						<div class="item-label">网娱服务:</div>
						<div class="choose-box">
							<#if commodity.qualifications == 1>
								<span class="service g-level">会员级别</span>
							<#elseif commodity.qualifications == 2>
								<span class="service g-level">黄金级别</span>
							<#elseif commodity.qualifications == 3>
								<span class="service g-level">钻石级别</span>
							</#if>
							<span class="service region">
								<#if (areaAndSales.province)??>
									<#if areaAndSales.province == "000000">
										全国
									<#else>
										${(areaAndSales.provinceName)!}地区
									</#if>
								</#if>
							</span>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class="pay-box">
			<div class="pay-list">
				<div class="pay-left"><label>您选择的项目价格：</label><span class="orange-price">${(property.rebate?string("0.##"))!}</span></div>
				<div class="pay-right"><label>您的等级折扣价：</label><span class="orange-price">${(totalAmount?string("0.##"))!}</span></div>
			</div>
			<div class="pay-list">
				<div class="pay-left"><label>自有资金账户支付：</label><input type="number" name="amount" class="form-control" value="0" readonly></div>
				<div class="pay-right balance">（当前余额：0）</div>
			</div>
			<div class="pay-list">
				<div class="pay-left"><label>奖金账户支付：</label><input type="number" name="quota" class="form-control" value="0" readonly></div>
				<div class="pay-right balance">（当前余额：0）</div>
			</div>
		</div>
		<button id="item-submit" type="button" class="btn-info">提交申请</button>
		<div class="information">
			<div class="info-tab">
				<ul>
					<li>商品详情</li>
				</ul>
			</div>
			<div class="product-detail">
				${commodity.introduce!}
				${commodity.description!}
			</div>
		</div>
	</div>
	
	<script type="text/javascript" src="${ctx}/static/js/app.js"></script>
	<script type="text/javascript">
		<#if property??>
		// 分组项目档期
		var nameProperties = JSON.parse('${namePropertiesJSON!}');
		var $properties = $('#properties');
		var items = '';
		var settlDates = '';
		for(k in nameProperties) {
			var isChosen = k == '${(property.propertyName)!}';
			var properties = nameProperties[k];
			if(isChosen) {
				items += '<div class="choose choose-group chosen">' + k + '</div>';
				for(var i=0; i<properties.length; i++) {
					var p = properties[i];
					var settlDate = new Date(p.settlDate).Format("yyyy-MM-dd");
					var isCurr = p.id == '${property.id}';
					if(isCurr) {
						settlDates += '<div class="choose choose-group chosen">' + settlDate + '</div>';
					} else {
						settlDates += '<a href="preview?commodityId=${commodity.id}&propertyId=' + p.id + '"><div class="choose choose-group">' + settlDate + '</div></a>';
					}
				}
			} else {
				items += '<a href="preview?commodityId=${commodity.id}&propertyId=' + properties[0].id + '"><div class="choose choose-group">' + k + '</div></a>';
			}
		}
		$properties.append('<div class="item-label">选择项目:</div><div class="choose-box">' + items + '</div>');
		$properties.append('<div class="item-label">选择日期:</div><div class="choose-box">' + settlDates + '</div>');
		</#if>
	</script>
</body>