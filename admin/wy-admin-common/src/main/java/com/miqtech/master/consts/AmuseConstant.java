package com.miqtech.master.consts;

/**
 * 娱乐赛相关常量
 */
public class AmuseConstant {

	private AmuseConstant() {
		super();
	}

	public static final Integer VERIFY_STATE_AWAIT = 0;// 未非陪
	public static final Integer VERIFY_STATE_INVERIFY = 1;// 待审核
	public static final Integer VERIFY_STATE_REFUSED = 2;// 审核拒绝
	public static final Integer VERIFY_STATE_NOGIVE = 3;// 审核通过,奖品未发放
	public static final Integer VERIFY_STATE_GIVED = 4;// 已发放
	public static final Integer VERIFY_STATE_OVER = 5;// 认证结束
	public static final Integer VERIFY_STATE_APPEAL_PASSED = 6;// 拒绝后申诉通过

	public static final Integer APPEAL_STATE_INAPPEAL = 0;// 申诉中
	public static final Integer APPEAL_STATE_REFUSED = 1;// 申诉驳回
	public static final Integer APPEAL_STATE_PASSED = 2;// 申诉通过（认证拒绝时处理）
	public static final Integer APPEAL_STATE_DEAL = 3;// 申诉已处理（奖品发放中处理）
	public static final Integer PPEAL_STATE_OVER = 4;// 申诉结束 

	public static final String REDIS_KEY_AMUSE_ORDER_REPEAT = "wy_amuse_order_repeat";// 生成订单编号时做递增 _{tradeNoPrefix}

	/**
	 * 娱乐赛文案类型
	 */
	public static final Integer FEEDBACK_TYPE_VERIFY = 1;// 审核
	public static final Integer FEEDBACK_TYPE_ISSUE = 2;// 发放
	public static final Integer FEEDBACK_TYPE_APPEAL = 3;// 申诉
	public static final Integer FEEDBACK_TYPE_APPEAL_REASON = 4;// 申诉理由
}
