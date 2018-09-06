package com.miqtech.master.consts;

import com.google.common.base.Joiner;

public class RedbagConstant {

	public final static Joiner joiner = Joiner.on("_");

	private RedbagConstant() {
	}

	/*
	 * 红包类型
	 */
	public static final Integer REDBAG_TYPE_LOGIN = 0;//首次登陆
	public static final Integer REDBAG_TYPE_REGISTER = 1;//注册绑定
	public static final Integer REDBAG_TYPE_ORDER = 2;//预约支付
	public static final Integer REDBAG_TYPE_WEEKLY = 3;//周红包
	public static final Integer REDBAG_TYPE_SHARE = 4;//分享红包
	public static final Integer REDBAG_TYPE_MALL = 5;// 商品红包
	public static final Integer REDBAG_TYPE_CDKEY = 6;// cdkey兑换红包
	public static final Integer REDBAG_TYPE_AWARD = 7;// 自有商品类型的奖品发放
	public static final Integer REDBAG_TYPE_BOUNTY = 11;// 悬赏令的红包发放

	// 周红包的redis key
	public static final String REDIS_KEY_REDBAG_WEEKLY_USED_AMOUNT_PREFIX = "wy_redbag_weekly_used_amount";
	public static final String REDIS_KEY_REDBAG_WEEKLY_SURPLUS_AMOUNT_PREFIX = "wy_redbag_weekly_surplus_amount";
	public static final String REDIS_KEY_REDBAG_WEEKLY_USER_RECORD_PREFIX = "wy_redbag_weekly";
	public static final String REDIS_KEY_REDBAG_WEEKLY_HAS_NOTIFY_PREFIX = "wy_redbag_weekly_hasnotify";
	public static final String REDIS_KEY_REDBAG_WEEKLY_TOTAL_AMOUNT_PREFIX = "wy_redbag_weekly_total_amount";

	// 分享红包的redis key
	public static final String REDIS_KEY_REDBAG_SHARE_USED_AMOUNT = "wy_redbag_share_used_amount";
	public static final String REDIS_KEY_REDBAG_SHARE_USED_NUMBER = "wy_redbag_share_used_number";
	public static final String REDIS_KEY_REDBAG_SHARE_HISTORY = "wy_redbag_share_history";
	public static final String REDIS_KEY_REDBAG_SHARE_VISIT = "wy_redbag_share_visit";
	public static final String REDIS_KEY_REDBAG_SHARE_ICON = "wy_redbag_share_icon";
	public static final String REDIS_KEY_REDBAG_SHARE_TITLE = "wy_redbag_share_title";
	public static final String REDIS_KEY_REDBAG_SHARE_CONTENT = "wy_redbag_share_content";
	public static final String REDIS_KEY_REDBAG_SHARE_USER_COUNT = "wy_redbag_share_usercount";
	public static final String REDIS_KEY_REDBAG_USER_GRADE = "wy_share_redbag_user_grade_";// 用户红包领取数分类，完整键：wy_share_redbag_user_grade_yyyy-mm-dd

	/**
	 * 获取记录红包使用金额的 redis key
	 */
	public static String getRedisKeyUsedAmount(Long redbagId) {
		return joiner.join(REDIS_KEY_REDBAG_WEEKLY_USED_AMOUNT_PREFIX, redbagId);
	}

	/**
	 * 获取记录红包剩余金额的 redis key
	 */
	public static String getRedisKeySurplusAmount(Long redbagId) {
		return joiner.join(REDIS_KEY_REDBAG_WEEKLY_SURPLUS_AMOUNT_PREFIX, redbagId);
	}

	/**
	 * 获取记录用户在某个红包领取记录的 redis key
	 */
	public static String getRedisKeyUserRecord(Long userId, Long redbagId) {
		return joiner.join(REDIS_KEY_REDBAG_WEEKLY_USER_RECORD_PREFIX, userId, redbagId);
	}

	/**
	 * 获取记录红包是否已推送的 redis key
	 */
	public static String getRedisKeyHasNotify(Long redbagId) {
		return joiner.join(REDIS_KEY_REDBAG_WEEKLY_HAS_NOTIFY_PREFIX, redbagId);
	}

	/**
	 * 获取记录红包总金额的 redis key
	 */
	public static String getRedisKeyTotalAmount(Long redbagId) {
		return joiner.join(REDIS_KEY_REDBAG_WEEKLY_TOTAL_AMOUNT_PREFIX, redbagId);
	}

}
