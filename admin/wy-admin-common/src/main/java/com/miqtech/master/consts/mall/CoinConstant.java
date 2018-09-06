package com.miqtech.master.consts.mall;

public class CoinConstant {

	private CoinConstant() {
		super();
	}

	/**
	 * 金币收支历史的收支方向
	 */
	public static final Integer HISTORY_DIRECTION_INCOME = 1; //收入
	public static final Integer HISTORY_DIRECTION_EXPEND = -1; //支出

	/**
	 * 金币收支历史的收支类型
	 */
	public static final Integer HISTORY_TYPE_TASK = 1; // 积分任务
	public static final Integer HISTORY_TYPE_INVITATION = 2; // 邀请得积分
	public static final Integer HISTORY_TYPE_COMMODITY = 3; // 商品兑换
	public static final Integer HISTORY_TYPE_CDKEY = 4;// cdkey兑换
	public static final Integer HISTORY_TYPE_AWARD = 5;// 自有商品类型的奖品发放
	public static final Integer HISTORY_TYPE_ROBTREASURE = 6;// 众筹夺宝
	public static final Integer HISTORY_TYPE_H5GAMEAWARD = 7;// h5游戏排名奖励
	public static final Integer HISTORY_TYPE_WHEEL_LOTTERY = 8;// 转盘抽奖
	public static final Integer HISTORY_TYPE_NETBAR_ERROR_RECOVERY = 9;// 网吧纠错
	public static final Integer HISTORY_TYPE_GUESSING = 10;// 竞猜
	public static final Integer HISTORY_TYPE_PAY_ORDER = 11;// 支付订单
	public static final Integer HISTORY_TYPE_AUDITION_PRIZE = 12;// 获胜奖励
	public static final Integer HISTORY_TYPE_DOWNLOAD = 13;// 下载
	public static final Integer HISTORY_TYPE_PC_REGISTER = 14;// PC端注册赠送
	public static final Integer HISTORY_TYPE_PC_CONFRONT_APPLY = 15;// PC端对抗赛报名
	public static final Integer HISTORY_TYPE_PC_CONFRONT_REFUND = 16;// PC端对抗赛退费
	public static final Integer HISTORY_TYPE_PC_COMMODITY_CONSUME = 17;// PC端商品消费
	public static final Integer HISTORY_TYPE_PC_COMMODITY_REFUND = 18;// PC端商品退款
	public static final Integer HISTORY_TYPE_PC_DAILY_TASK_BOX = 19;// PC端每日宝箱
	public static final Integer HISTORY_TYPE_PC_RECHARGE = 20;// PC端充值

	public static final Integer INVITATION_COIN = 100; //邀请好友获得的金币

}
