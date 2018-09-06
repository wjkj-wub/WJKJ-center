package com.miqtech.master.consts.mall;

public class CommodityConstant {

	private CommodityConstant() {
		super();
	}

	/**
	 * 商品交易状态
	 */
	public static final Integer INT_STATUS_FALSE = 0; //待处理（未获得）
	public static final Integer INT_STATUS_TRUE = 1; //已处理（已获得）
	public static final Integer INT_STATUS_EXCEPTION = 2; //异常
	public static final Integer INT_STATUS_HANDLED = 3; //异常已处理

	/**
	 * 商品类别ID-->type
	 */
	public static final Long CATEGORY_ID_REDBAG = (long) 1; //类别为红包的类别ID
	public static final Long CATEGORY_ID_CDKEY = (long) 2; //CDKEY
	public static final Long CATEGORY_ID_REAL = (long) 3; //实物
	public static final Long CATEGORY_ID_RECHARGE = (long) 4; //虚拟充值
	public static final Long CATEGORY_ID_COIN = (long) 5; //金币

	/**
	 * 商品类别
	 */
	public static final Integer CATEGORY_TYPE_REDBAG = 1; //类别为红包的类别ID
	public static final Integer CATEGORY_TYPE_CDKEY = 2; //CDKEY
	public static final Integer CATEGORY_TYPE_REAL = 3; //实物
	public static final Integer CATEGORY_TYPE_RECHARGE = 4; //虚拟充值
	public static final Integer CATEGORY_TYPE_COIN = 5; //金币
	public static final Integer CATEGORY_TYPE_FLOW = 6; //流量
	public static final Integer CATEGORY_TYPE_QCOIN = 7; //Q币
	public static final Integer CATEGORY_TYPE_BILL = 8; //话费·
	public static final Integer CATEGORY_TYPE_THANKY = 99; //谢谢惠顾

	/**
	 * 商品展示区ID
	 */
	public static final Long AREA_ID_COIN = (long) 1; //金币区
	public static final Long AREA_ID_LOTTERY = (long) 2; //抽奖区
	public static final Long AREA_ID_CROWD = (long) 3;// 众筹商品区

	/**
	 * 商品兑换/抽奖时，控制用户的频次
	 */
	public static final String REDIS_COMMODITY_EXCHANGEORLOTTERY = "wy_commodity_exchangeOrLottery_";

	/**
	 * 大类别常量
	 */
	public static final Integer SUPER_TYPE_SELF = 1;// 自有物品
	public static final Integer SUPER_TYPE_RECHARGE = 2;// 充值
	public static final Integer SUPER_TYPE_RESERVE = 3;// 库存

	/*
	 * 兑换记录状态
	 */
	public static final Integer VERIFY_STATUS_AWAIT = 0;// 处理中(待审核)
	public static final Integer VERIFY_STATUS_FINISH = 1;// 已处理（发放成功）
	public static final Integer VERIFY_STATUS_APPEAL = 2;// 异常（异常待审核）
	public static final Integer VERIFY_STATUS_APPEALFINISH = 3;// 异常已处理
	public static final Integer VERIFY_STATUS_PASS = 4;// 审核通过（待发放）
	public static final Integer VERIFY_STATUS_APPEALPASS = 5;// 异常审核通过（待发放）
	public static final Integer VERIFY_STATUS_REFUSE = 6;// 审核拒绝
	public static final Integer VERIFY_STATUS_APPEALREFUSE = 7;// 异常审核拒绝
	public static final Integer VERIFY_STATUS_FAIL = 8;// 发放失败
	public static final Integer VERIFY_STATUS_APPEALFAIL = 9;// （异常）发放失败

	/**
	 * 购买记录来源
	 */
	public static final Integer COMMODITY_SOURCE_NOT_TURNTABLE = 1;// 非转盘商品
	public static final Integer COMMODITY_SOURCE_TURNTABLE = 2;// 转盘商品
	public static final Integer COMMODITY_SOURCE_BOUNTY = 3;// 悬赏令商品
}
