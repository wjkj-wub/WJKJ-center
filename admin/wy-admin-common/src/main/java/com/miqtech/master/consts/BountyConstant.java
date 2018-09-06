package com.miqtech.master.consts;

/**
 * 悬赏令相关常量
 */
public class BountyConstant {

	private BountyConstant() {
		super();
	}

	public static final String REDIS_KEY_BOUNTY_ORDER_REPEAT = "wy_bounty_order_repeat";// 生成订单编号时做递增 _{tradeNoPrefix}

	public static final Integer STATUS_UNDERWAY = 0;// 未结束
	public static final Integer STATUS_COMPLETE = 1;// 已结束
}
