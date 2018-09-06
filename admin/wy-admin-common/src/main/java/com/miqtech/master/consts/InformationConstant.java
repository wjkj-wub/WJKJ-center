package com.miqtech.master.consts;

public class InformationConstant {
	//wy redis
	public static final String USER_KEY = "api_information_up_down_user_";
	public static final String UP_TOTAL_KEY = "api_information_up_total_";
	public static final String DOWN_TOTAL_KEY = "api_information_down_total_";
	//oet redis
	public static final String UP_OET_TOTAL_KEY = "oet_information_up_total_";
	public static final String DOWM_OET_TOTAL_KEY ="oet_information_down_total_";
	public static final String OET_USER_KEY = "oet_information_up_down_user_";
	

	/**
	 * 模块类型
	 */
	public static final Integer MODULE_TYPE_INFO = 1;// 资讯
	public static final Integer MODULE_TYPE_VIDEO = 2;//视频

	/**
	 * 资讯类型
	 */
	public static final Integer INFO_TYPE_INFO = 1;// 图文
	public static final Integer INFO_TYPE_SUBJECT = 2;// 专题
	public static final Integer INFO_TYPE_PIC_SET = 3;// 图集
	public static final Integer INFO_TYPE_VIDEO = 4;// 视频
}
