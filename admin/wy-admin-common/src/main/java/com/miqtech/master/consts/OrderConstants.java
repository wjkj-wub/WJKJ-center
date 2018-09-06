package com.miqtech.master.consts;

/**
 * 支付相关常量
 */
public class OrderConstants {

	private OrderConstants() {
		super();
	}

	/**
	 * 支付类型
	 */
	public static final Integer ORDER_TYPE_ALIPAY = 1;// 支付宝支付
	public static final Integer ORDER_TYPE_WXPAY = 2;// 微信支付

	/**
	 * 支付状态
	 */
	public static final Integer ORDER_STATUS_FAILED = -1;// 支付失败
	public static final Integer ORDER_STATUS_NOPAY = 0;// 用户未完成支付
	public static final Integer ORDER_STATUS_PAYED = 1;// 用户支付完成，网吧未申请核销
	public static final Integer ORDER_STATUS_APPLYING = 2;// 网吧已申请核销
	public static final Integer ORDER_STATUS_DISSENT = 3;// 有异议
	public static final Integer ORDER_STATUS_CLEAR = 4;// 已核销

	/**
	 * 交接班
	 */
	public static final Integer STAFF_BATCH_UNAPPLY = 1;// 员工提交，商户未申请
	public static final Integer STAFF_BATCH_APPLIED = 2;// 员工提交，商户已申请
	public static final Integer STAFF_BATCH_PAYED = 3;// 已核销
	public static final Integer OWNER_BATCH_UNPAY = 1;// 未核销
	public static final Integer OWNER_BATCH_PAIED = 2;// 已核销

	public static final Integer USE_TYPE_COST = 0;// 支付网费
	public static final Integer USE_TYPE_RECHARGE = 1;// 会员充值
	public static final Integer USE_TYPE_PC_RECHARGE = 2;// 一键充值

}
