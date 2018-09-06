package com.miqtech.master.consts;

public class NetbarConstant {

	// 消息历史的类型
	public static final Integer NETBAR_DISCOUNT_HISTORY_TYPE_PLATFORM = 1;// 平台
	public static final Integer NETBAR_DISCOUNT_HISTORY_TYPE_MEMBER = 2;// 会员

	// 资金明细类型
	public static final Integer NETBAR_FUND_DETAIL_TYPE_CONSUME = 0;// 消费
	public static final Integer NETBAR_FUND_DETAIL_TYPE_REDBAG = 1;// 红包
	public static final Integer NETBAR_FUND_DETAIL_TYPE_WITHDRAW = 2;// 提现
	public static final Integer NETBAR_FUND_DETAIL_TYPE_RECHARGE = 3;// 充值
	public static final Integer NETBAR_FUND_DETAIL_TYPE_SETTLE = 4;// 核销
	public static final Integer NETBAR_FUND_DETAIL_TYPE_QUOTA = 5;// 配额奖金
	public static final Integer NETBAR_FUND_DETAIL_TYPE_REFUND = 6;// 现金退款
	public static final Integer NETBAR_FUND_DETAIL_TYPE_DEDUCT = 7;// 刷单扣款(配额)
	public static final Integer NETBAR_FUND_DETAIL_TYPE_DEDUCT_ACCOUNTS = 8;// 刷单扣款(资金)

	// 资金明细状态
	public static final Integer NETBAR_FUND_DETAIL_STATUS_APPLY = 1;// 申请中
	public static final Integer NETBAR_FUND_DETAIL_STATUS_CLEAR = 2;// 已提现

	// 资金明细收支
	public static final Integer NETBAR_FUND_DETAIL_DIRECTION_INCOME = 1;// 收入
	public static final Integer NETBAR_FUND_DETAIL_DIRECTION_EXPAND = -1;// 支出
	public static final Integer NETBAR_FUND_DETAIL_DIRECTION_REFUND = 0;// 退款

	// 订单状态
	public static final Integer NETBAR_RESOURCE_ORDER_STATUS_EXPIRED = -2;// 过期的
	public static final Integer NETBAR_RESOURCE_ORDER_STATUS_CANCEL = -1;// 取消
	public static final Integer NETBAR_RESOURCE_ORDER_STATUS_UNCHECK = 0;// 双方未确认
	public static final Integer NETBAR_RESOURCE_ORDER_STATUS_SERVER = 1;// 服务确认
	public static final Integer NETBAR_RESOURCE_ORDER_STATUS_NETBAR = 2;// 网吧确认
	public static final Integer NETBAR_RESOURCE_ORDER_STATUS_CHECKED = 3;// 双方确认
	public static final Integer NETBAR_RESOURCE_ORDER_STATUS_SUCCESS_EXPIRE = 4;// 服务方确认网吧未确认时过期

	// 网吧等级 网吧等级
	public static final Integer NETBAR_LEVELS_NOTMEMBER = 0;// 非会员
	public static final Integer NETBAR_LEVELS_MEMBER = 1;// 会员
	public static final Integer NETBAR_LEVELS_GOLD = 2;// 黄金
	public static final Integer NETBAR_LEVELS_DIAMOND = 3;// 钻石

	// 资源商品出售类型
	public static final Integer NETBAR_RESOURCE_COMMODITY_CATE_TYPE_DATE = 0;// 按日期
	public static final Integer NETBAR_RESOURCE_COMMODITY_CATE_TYPE_NUMBER = 1;// 按数量

	// 资源商品项目,购买资格
	public static final Integer NETBAR_RESOURCE_PROPERTY_QUALIFI_NONE = 0;// 无
	public static final Integer NETBAR_RESOURCE_PROPERTY_QUALIFI_ACCOUNT = 1;// 流水满
	public static final Integer NETBAR_RESOURCE_PROPERTY_QUALIFI_COMMODITY = 2;// 必须购买商品

	// 刮刮卡状态
	public static final Integer NETBAR_CARD_COUPON_STATUS_UNUSE = 1;// 未使用
	public static final Integer NETBAR_CARD_COUPON_STATUS_USED = 2;// 已使用
	public static final Integer NETBAR_CARD_COUPON_STATUS_APPLIED = 3;// 已申请
	public static final Integer NETBAR_CARD_COUPON_STATUS_SETTLED = 4;// 已结算

	// 刮刮卡结款状态
	public static final Integer NETBAR_CARD_COUPON_SETTLE_STATUS_UNSETTLE = 0;// 未结款
	public static final Integer NETBAR_CARD_COUPON_SETTLE_STATUS_SETTLED = 1;// 已结款

	// 网吧充值状态
	public static final Integer NETBAR_RECHARGE_STATUS_NOPAY = 1;// 待支付
	public static final Integer NETBAR_RECHARGE_STATUS_PAYED = 2;// 支付完成
	public static final Integer NETBAR_RECHARGE_STATUS_FAILED = 3;// 支付失败
}
