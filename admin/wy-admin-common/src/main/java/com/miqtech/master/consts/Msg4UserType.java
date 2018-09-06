package com.miqtech.master.consts;

/**
 * 客户端用户接收推送消息类型,用于消息列表查询
 */
public enum Msg4UserType {
	SYS_NOTIFY, //系统 :系统消息
	SYS_REDBAG, //系统 :红包消息
	SYS_MEMBER, //系统 :会员消息
	ORDER_RESERVE, //订单类: 预定消息
	ORDER_PAY, // 订单类:支付消息
	ACTIVITY, //活动类:赛事消息
	MATCH, // 活动类:约战消息
	AMUSE, // 活动类:娱乐赛提示信息
	COMMENT_PRAISE, //评论点赞
	COMMENT, //评论
	COMMODITY, //商品兑换
	INFORMATION, //资讯
	ROBTREASURE, //众筹夺宝
	MALL, //金币商城
	COIN_TASK, //金币任务
	AWARD_COMMODITY, //兑奖专区商品详情
	EVENT, //自发赛
	BOUNTY//悬赏令活动
}
