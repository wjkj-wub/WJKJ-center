package com.miqtech.master.consts.award;

/**
 * 奖品发放相关常量
 */
public class AwardConstant {

	private AwardConstant() {
		super();
	}

	/**
	 * 大类
	 */
	public static final Integer TYPE_OWN = 1;// 自有商品
	public static final Integer TYPE_RECHARGE = 2;// 充值
	public static final Integer TYPE_REPERTORY = 3;// 库存

	/**
	 * 小类
	 */
	public static final Integer SUB_TYPE_OWN_REDBAG = 1;// 自有红包
	public static final Integer SUB_TYPE_OWN_COIN = 2;// 自有金币
	public static final Integer SUB_TYPE_RECHARGE_TELEPHOEN = 3;// 充值话费
	public static final Integer SUB_TYPE_RECHARGE_FLOW = 4;// 充值流量
	public static final Integer SUB_TYPE_RECHARGE_QQ_COIN = 5;// Q币

	/**
	 * 发放来源类型
	 */
	public static final Integer SOURCE_TYPE_AMUSE = 1;// 娱乐赛
	public static final Integer SOURCE_TYPE_MALL = 2;// 金币商城

	/**
	 * 状态
	 */
	public static final Integer STATUS_COMMIT = 0;// 提交(未成功)
	public static final Integer STATUS_API_FAIL = 1;// 接口返回失败
	public static final Integer STATUS_MSG_FAIL = 2;// 短信通知失败
	public static final Integer STATUS_SUCCESS = 3;// 成功
}
