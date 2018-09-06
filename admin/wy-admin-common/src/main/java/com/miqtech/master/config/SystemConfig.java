package com.miqtech.master.config;

public class SystemConfig {
	/* 系统配置	 */
	private String appDomain = "http://api.wangyuhudong.com/";
	private String adminDomain = "http://admin.wangyuhudong.com/";
	private String merchantDomain = "http://merchant.wangyuhudong.com";
	private String imgServerDomain = "http://img.wangyuhudong.com/";
	public String environment = "dev";//环境配置
	/* 支付配置	 */
	public String weixinPayNotifyUrl = "http://api.wangyuhudong.com/pay/weixinNotify";//微信支付成功回调地址,默认正式环境

	/*极光推送tag和alias设置,默认正式环境配置*/
	public String jpushClientTag = "members";
	public String jpushClientAlias = "member_";
	public String jpushMerchantTag = "merchants";
	public String jpushMerchantAlias = "merchant_";
	private String minMoneyConfig = "1,0;2,0;3,0;4,0;5,0;6,0;7,0;8,0;9,0;10,0";//红包满多少用多少配置,如果为0表示无限制
	/*红包设置*/
	public String redbagDayLimit = "2";//默认每天只能使用两个红包
	public String weixinAppId;
	public String weixinMchId;
	public String weixinKey;
	public String alipayPartner;
	public String alipayTradePrecreateGateway;
	public String alipayTradePrecreateAppid;
	public String alipayTradePrecreatePrivateKey;
	public String alipayTradePrecreateAliPublicKey;
	public String logEnable;
	//二维码logo路径
	public String qrLogoPath;

	public String apiServers;
	public String piliEnv;//pili流环境
	
	public String uwanGateway;// 优玩网关

	public String getQrLogoPath() {
		return qrLogoPath;
	}

	public void setQrLogoPath(String qrLogoPath) {
		this.qrLogoPath = qrLogoPath;
	}

	public String getAppDomain() {
		return appDomain;
	}

	public void setAppDomain(String appDomain) {
		this.appDomain = appDomain;
	}

	public String getAdminDomain() {
		return adminDomain;
	}

	public void setAdminDomain(String adminDomain) {
		this.adminDomain = adminDomain;
	}

	public String getImgServerDomain() {
		return imgServerDomain;
	}

	public void setImgServerDomain(String imgServerDomain) {
		this.imgServerDomain = imgServerDomain;
	}

	public String getJpushClientTag() {
		return jpushClientTag;
	}

	public void setJpushClientTag(String jpushClientTag) {
		this.jpushClientTag = jpushClientTag;
	}

	public String getJpushClientAlias() {
		return jpushClientAlias;
	}

	public void setJpushClientAlias(String jpushClientAlias) {
		this.jpushClientAlias = jpushClientAlias;
	}

	public String getJpushMerchantTag() {
		return jpushMerchantTag;
	}

	public void setJpushMerchantTag(String jpushMerchantTag) {
		this.jpushMerchantTag = jpushMerchantTag;
	}

	public String getJpushMerchantAlias() {
		return jpushMerchantAlias;
	}

	public void setJpushMerchantAlias(String jpushMerchantAlias) {
		this.jpushMerchantAlias = jpushMerchantAlias;
	}

	public String getWeixinPayNotifyUrl() {
		return weixinPayNotifyUrl;
	}

	public void setWeixinPayNotifyUrl(String weixinPayNotifyUrl) {
		this.weixinPayNotifyUrl = weixinPayNotifyUrl;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getRedbagDayLimit() {
		return redbagDayLimit;
	}

	public void setRedbagDayLimit(String redbagDayLimit) {
		this.redbagDayLimit = redbagDayLimit;
	}

	public String getMinMoneyConfig() {
		return minMoneyConfig;
	}

	public void setMinMoneyConfig(String minMoneyConfig) {
		this.minMoneyConfig = minMoneyConfig;
	}

	public String getWeixinAppId() {
		return weixinAppId;
	}

	public void setWeixinAppId(String weixinAppId) {
		this.weixinAppId = weixinAppId;
	}

	public String getWeixinMchId() {
		return weixinMchId;
	}

	public void setWeixinMchId(String weixinMchId) {
		this.weixinMchId = weixinMchId;
	}

	public String getWeixinKey() {
		return weixinKey;
	}

	public void setWeixinKey(String weixinKey) {
		this.weixinKey = weixinKey;
	}

	public String getAlipayPartner() {
		return alipayPartner;
	}

	public void setAlipayPartner(String alipayPartner) {
		this.alipayPartner = alipayPartner;
	}

	public String getLogEnable() {
		return logEnable;
	}

	public void setLogEnable(String logEnable) {
		this.logEnable = logEnable;
	}

	public String getMerchantDomain() {
		return merchantDomain;
	}

	public void setMerchantDomain(String merchantDomain) {
		this.merchantDomain = merchantDomain;
	}

	public String getApiServers() {
		return apiServers;
	}

	public void setApiServers(String apiServers) {
		this.apiServers = apiServers;
	}

	public String getPiliEnv() {
		return piliEnv;
	}

	public void setPiliEnv(String piliEnv) {
		this.piliEnv = piliEnv;
	}

	public String getAlipayTradePrecreateGateway() {
		return alipayTradePrecreateGateway;
	}

	public void setAlipayTradePrecreateGateway(String alipayTradePrecreateGateway) {
		this.alipayTradePrecreateGateway = alipayTradePrecreateGateway;
	}

	public String getAlipayTradePrecreateAppid() {
		return alipayTradePrecreateAppid;
	}

	public void setAlipayTradePrecreateAppid(String alipayTradePrecreateAppid) {
		this.alipayTradePrecreateAppid = alipayTradePrecreateAppid;
	}

	public String getAlipayTradePrecreatePrivateKey() {
		return alipayTradePrecreatePrivateKey;
	}

	public void setAlipayTradePrecreatePrivateKey(String alipayTradePrecreatePrivateKey) {
		this.alipayTradePrecreatePrivateKey = alipayTradePrecreatePrivateKey;
	}

	public String getAlipayTradePrecreateAliPublicKey() {
		return alipayTradePrecreateAliPublicKey;
	}

	public void setAlipayTradePrecreateAliPublicKey(String alipayTradePrecreateAliPublicKey) {
		this.alipayTradePrecreateAliPublicKey = alipayTradePrecreateAliPublicKey;
	}

	public String getUwanGateway() {
		return uwanGateway;
	}

	public void setUwanGateway(String uwanGateway) {
		this.uwanGateway = uwanGateway;
	}

}
