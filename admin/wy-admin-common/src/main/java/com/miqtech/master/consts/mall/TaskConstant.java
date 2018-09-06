package com.miqtech.master.consts.mall;

import com.google.common.base.Joiner;

public class TaskConstant {

	public static final Joiner JOINER = Joiner.on("_");

	private TaskConstant() {
		super();
	}

	/**
	 * 任务表常量
	 */
	public static final Integer TASK_TYPE_DAILY = 1;// 每日任务
	public static final Integer TASK_TYPE_TEACHING = 2;// 新手任务
	// 每日任务
	public static final Integer TASK_DAILY_OPEN_CLIENT = 1;// 打开一次网娱大师
	public static final Integer TASK_DAILY_SHARE = 8;//分享一次网娱大师的内容
	public static final Integer TASK_DAILY_EVALUATE_NETBAR = 10;// 评价网吧
	public static final Integer TASK_DAILY_EVALUATE_ACTIVITY_INFO = 11;//  给网娱资讯写评论一次
	public static final Integer TASK_DAILY_ERROR_NETBAR = 12;//  提交网吧纠错信息

	// 新手任务
	public static final Integer TASK_TEACHING_COMPLETE_USERINFO = 1;// 完善用户信息
	public static final Integer TASK_TEACHING_FIRST_PAY = 3;// 首次支付
	public static final Integer TASK_TEACHING_OVER_10_WORD_COMMENT = 7;// 给资讯写10个字以上的评论
	public static final Integer TASK_TEACHING_COMMENT_NETBAR = 8;// 为网吧添加一次评分
	public static final Integer TASK_TEACHING_JOIN_BOUNTRY = 9;//  参加一次悬赏令活动
	public static final Integer TASK_TEACHING_REG = 10;//  完成手机号码的注册
	public static final Integer TASK_TEACHING_MALL_SIGN = 11;//  完成一次金币商城内签到

	/**
	 * redis key
	 */
	public static final String REDIS_TASK_COMPLETE = "wy_task_complete";// 记录用户当天对于某任务的完成数：wy_task_complete_type_id_userId
	public static final String REDIS_TASK_DAILY_SIGN = "wy_task_daily_sign";// 记录用户当天是否已签到（到0点失效）：wy_task_daily_sign_userId

}
