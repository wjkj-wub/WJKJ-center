package com.miqtech.master.consts;

/**
 * 常用常量
 */
public class CommonConstant {

	private CommonConstant() {
	}

	public static final Integer INT_BOOLEAN_FALSE = 0;
	public static final Integer INT_BOOLEAN_TRUE = 1;

	/* 用户积分，娱币的redis缓存key*/
	public final static String REDIS_USER_COIN = "redis_user_coin";
	public final static String REDIS_USER_CHIP = "redis_user_chip";
	/* 用户积分，娱币类型*/
	public static final Byte PC_FEE_TYPE_FREE = 0; // 免费
	public static final Byte PC_FEE_TYPE_COIN = 1; // 积分
	public static final Byte PC_FEE_TYPE_CHIP = 2; // 娱币

	// api接口状态码和提示信息
	/*
	 * api接口成功状态吗
	 */
	public static final int CODE_SUCCESS = 0;
	public static final String MSG_SUCCESS = "success";
	/*
	 * 用户状态码
	 */
	public static final int CODE_LOGIN_INVALID = -1;
	public static final String MSG_LOGIN_INVALID = "登录状态已失效,请登录";
	public static final int CODE_PERMISSION_DENY = -2;
	public static final String MSG_PERMISSION_DENY = "没有操作的权限";
	public static final int CODE_NOT_LOGIN = -4;
	public static final String MSG_NOT_LOGIN = "您还没有登录,请登录";
	/*
	 * 业务逻辑错误或数据错误状态码
	 */
	public static final int CODE_ERROR_LOGIC = -3;
	public static final String MSG_ERROR_LOGIC = "不正确的业务";
	public static final String MSG_ERROR_LOGIC_NULL = "请求对象不存在";
	/*
	 * 请求数据错误状态码
	 */
	public static final int CODE_ERROR_PARAM = -5;
	public static final String MSG_ERROR_PARAM = "参数错误";

	/*
	 * 数据库操作错误
	 */
	public static final int CODE_ERROR_DATABASE = -6;
	public static final String MSG_ERROR_DATABASE = "数据库操作错误";

	/**
	 * 默认头像
	 */
	public static final String IMG_DEFAULT_HEADIMG = "http://img.wangyuhudong.com/uploads/imgs/user/random/default.png";

	/**
	 * 客户端类型
	 */
	public static final Integer CLIENT_TYPE_IOS = 1;
	public static final Integer CLIENT_TYPE_ANDROID = 2;
	public static final String AREA_KEY = "wy-area-";

	public static final String MERCHANT_REDBAG_LEFT_NUM = "merchant_redbag_left_num_";//网吧在商户端购买的红包商品剩余的红包数量
	public static final String MALL_COMMODITY_ROBTREASURE_LEFT_NUM = "mall_commodity_robtreasure_left_coin_";//众筹夺宝剩余金币数
	public static final String MALL_COMMODITY_WHEEL_LEFT_NUM = "mall_commodity_wheel_left_num";//大转盘奖品剩余库存
	public static final String WHEEL_FREE_LOTTERY = "wheel_freee_lottery_";//大转盘免费抽奖
	public static final String WHEEL_LOTTERY_LEFT_NUM = "wheel_lottery_left_num_";//转盘商品剩余库存
	public static final String WHEEL_LOTTERY_NEEED_COIN = "wheel_lottery_need_coin_";
	public static final String WHEEL_USER_FIRST_LOTTERY = "wheel_user_first_lottery_";
	public static final String WHEEL_USER_PRIZES = "wheel_user_prizes_";
	public static final String NEED_COIN = "100";
	public static final String NETBAR_RECHARGE_PRIZE_LEFT_NUM = "netbar_recharge_prize_left_num_";

}
