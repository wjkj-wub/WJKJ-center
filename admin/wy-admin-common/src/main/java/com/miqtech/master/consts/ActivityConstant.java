package com.miqtech.master.consts;

public class ActivityConstant {

	/**
	 * 邀请相关
	 */
	public static final Integer INVITE_TYPE_MATCH = 1;// 约战邀请
	public static final Integer INVITE_TYPE_TEAM = 2;// 赛事战队邀请

	public static final Integer INVITE_STATUS_INIT = 0;// 等待邀请
	public static final Integer INVITE_STATUS_DENY = 1;// 拒绝
	public static final Integer INVITE_STATUS_ACCEPT = 2;// 接受

	public static final Integer MATCH_WAY_ONLINE = 1;// 线上约战类型
	public static final Integer MATCH_WAY_OFFLINE = 2;// 线下约战类型

	public static final Integer APPLY_TYPE_PERSON = 1;
	public static final Integer APPLY_TYPE_TEAM = 2;
}
