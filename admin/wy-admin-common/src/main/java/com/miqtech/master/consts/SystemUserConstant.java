package com.miqtech.master.consts;

public class SystemUserConstant {

	private SystemUserConstant() {
		super();
	}

	/**
	 * 管理员类型
	 */
	public static final Integer TYPE_SUPER_ADMIN = 0;
	public static final Integer TYPE_NORMAL_ADMIN = 1;
	public static final Integer TYPE_ENTERING_ADMIN = 2;// 录入管理员
	public static final Integer TYPE_ENTERING_AUDITER = 3;// 录入审核员
	public static final Integer TYPE_ENTERING_STAFF = 4;// 录入人员
	public static final Integer TYPE_ENTERING_AREA_1 = 5;
	public static final Integer TYPE_ENTERING_AREA_2 = 6;
	public static final Integer TYPE_ENTERING_AREA_3 = 7;
	public static final Integer TYPE_ENTERING_AREA_4 = 8;
	public static final Integer TYPE_MALL_ADMIN = 9;// 商城管理员
	public static final Integer TYPE_ACTIVITY_ADMIN = 10;// 赛事约战子账号
	public static final Integer TYPE_INVITE_ADMIN = 11;// 邀请码管理系统
	public static final Integer TYPE_AMUSE_VERIFY = 12;// 娱乐赛审核帐号
	public static final Integer TYPE_AMUSE_ISSUE = 13;// 娱乐赛发放帐号
	public static final Integer TYPE_AMUSE_APPEAL = 14;// 娱乐赛申诉处理帐号
	public static final Integer TYPE_MALL_VERIFY = 15;// 商城审核帐号
	public static final Integer TYPE_MALL_GRANT = 16;// 商城发放帐号

}
