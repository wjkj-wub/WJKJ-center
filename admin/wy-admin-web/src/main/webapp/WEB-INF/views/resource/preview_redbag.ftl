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
						<span class="disabled-price">￥
							<#assign totalAmount = property.rebate>
							${(totalAmount?string("0.##"))!}
						</span>
						<span class="yf_col00 deduction yf_vt2">
							奖金最高可抵
							${(totalAmount * commodity.useQuoRatio)!0}
							元
						</span>
					</div>
					<#if (property.qualifiType)?? && (property.qualifiType == 1 || property.qualifiType == 2)>
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
					<div class="total-sale">
						<div>累计销售</div>
						<div class="sale-account">${(areaAndSales.count)!0}</div>
					</div>
				</div>
				<div class="item yf_buy">
					<div class="yf_buy_num">
						<span class="yf_vt8 w80">购买数量：</span>
						<span>
							<input type="button" id="min" value="-" class="yf_count_min" disabled />
							<input type="text" id="totalsum" value="1" class="yf_count_num" onkeyup="this.value=this.value.replace(/\D/g,'')" maxlength="6" />
							<input type="button" id="max" value="+" class="yf_count_max" disabled />
						</span>
						<span class="yf_buy_c">×${unit!1}</span>
						<span class="yf_buy_available">（可售数量：${(property.inventory)!0}）</span>
					</div>
					<div class="yf_buy_time mt8">
						<span class="w80">有效期：</span><span class="yf_buy_days">${(property.validity)!3}天</span>
					</div>
					<div class="item-service clearfix">
						<div class="item-label">网娱服务：</div>
						<div class="choose-box">
							<#if commodity.qualifications == 1>
								<span class="service g-level">会员级别</span>
							<#elseif commodity.qualifications == 2>
								<span class="service g-level">黄金级别</span>
							<#elseif commodity.qualifications == 3>
								<span class="service g-level">钻石级别</span>
							</#if>
							<span class="service region">
								<#if areaAndSales.province == "000000">
									全国
								<#else>
									${(areaAndSales.provinceName)!}地区
								</#if>
							</span>
						</div>
					</div>
					<button id="item-submit" type="button" class="btn btn-info">提交申请</button>
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
</body>