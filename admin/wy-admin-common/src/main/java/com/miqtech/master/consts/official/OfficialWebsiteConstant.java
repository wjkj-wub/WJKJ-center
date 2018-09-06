package com.miqtech.master.consts.official;

public class OfficialWebsiteConstant {

	private OfficialWebsiteConstant() {
		super();
	}

	/**
	 * 官网新闻首页redis_key
	 */
	public static final String REDIS_OFFICIALWEBSITE_BANNER = "wy_official_website_banner"; //1.轮播栏

	public static final String REDIS_OFFICIALWEBSITE_MATCH = "wy_official_website_match"; //2.最新赛事
	public static final String REDIS_OFFICIALWEBSITE_ACTIVITY = "wy_official_website_activity"; //3.精彩活动
	public static final String REDIS_OFFICIALWEBSITE_PROFESSION = "wy_official_website_profession"; //4.行业动态

	public static final String REDIS_OFFICIALWEBSITE_INFORMATION = "wy_official_website_information"; //5.资讯新闻

	public static final String REDIS_OFFICIALWEBSITE_BEFORE = "wy_official_website_before"; //6.往期赛事

	public static final String REDIS_OFFICIALWEBSITE_VIDEO = "wy_official_website_video"; //1.电竞视频
	public static final String REDIS_OFFICIALWEBSITE_TOY = "wy_official_website_toy"; //2.Top小游戏

}
