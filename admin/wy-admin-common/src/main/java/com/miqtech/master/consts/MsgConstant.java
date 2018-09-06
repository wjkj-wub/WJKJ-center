package com.miqtech.master.consts;

public class MsgConstant {

	private MsgConstant() {
	}

	/**
	 * DB保存:商户消息类型
	 */
	public static final int MSG_MERCHANT_TYPE_PAY = 1;// 支付
	public static final int MSG_MERCHANT_TYPE_RESERVE = 2;// 预定
	public static final int MSG_MERCHANT_TYPE_SYS = 3;// 系统

	/**
	 * 推送类型key
	 */
	public static final String PUSH_MSG_TYPE_KEY = "wycategory";// 推行附加信息key
	public static final String PUSH_MSG_OBJECT_ID_KEY = "wyobject";// 推行附加信息object id key
	public static final String PUSH_MSG_EXTEND_DATA = "wyextend";// 推行附加信息扩展字段
	/**
	 * 推送类型值
	 */
	public static final String PUSH_MSG_TYPE_SYS = "sys";// 系统消息
	public static final String PUSH_MSG_TYPE_REDBAG = "redbag";// 红包消息
	public static final String PUSH_MSG_TYPE_REDBAG_WEEKLY = "redbag_weekly";// 每周红包推送消息
	public static final String PUSH_MSG_TYPE_ORDER_RESERVATION = "reservation";// 预定订单消息
	public static final String PUSH_MSG_TYPE_ORDER_PAY = "pay";// 支付消息
	public static final String PUSH_MSG_TYPE_ACTIVITY = "activity";// 活动赛事消息
	public static final String PUSH_MSG_TYPE_MATCH = "match";//约战消息
	public static final String PUSH_MSG_TYPE_AMUSE = "amuse";// 娱乐赛推送类型
	public static final String PUSH_MSG_TYPE_NETBAR = "netbar";//网吧消息
	public static final String PUSH_MSG_TYPE_COMMENT_PRAISE = "praise";//评论点赞
	public static final String PUSH_MSG_TYPE_COMMENT = "comment";//评论新回复
	public static final String PUSH_MSG_TYPE_COMMODITY = "commodity";// 金币商城商品兑换推送类型
	public static final String PUSH_MSG_TYPE_INFORMATION = "information";// 资讯类型
	public static final String PUSH_MSG_TYPE_ROBTREASURE = "robtreasure";// 众筹夺宝
	public static final String PUSH_MSG_TYPE_AWARD_COMMODITY = "award_commodity";// 商品详情
	public static final String PUSH_MSG_TYPE_MALL = "mall";//金币商城
	public static final String PUSH_MSG_TYPE_COIN_TASK = "coin_task";//金币任务
	public static final String PUSH_MSG_TYPE_OET = "oet";//自发赛
	public static final String PUSH_MSG_TYPE_BOUNTY = "bounty";//悬赏令活动

}
