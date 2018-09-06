package com.miqtech.master.consts;

/**
 * 抽奖相关常量
 */
public class LotteryConstant {

	private LotteryConstant() {
		super();
	}

	public static final String REDIS_FREQUENCY_LOTTERY_USER = "wy_lottery_frequency";// 用户抽奖的频次控制：wy_lottery_frequency_{lotteryId}_{userId}
	public static final String REDIS_SURPLUS_LOTTERY_AWARD = "wy_lottery_surplus";// 抽奖奖项的剩余量：wy_lottery_surplus_{lotteryId}_{awardId}
}
