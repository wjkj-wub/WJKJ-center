package com.miqtech.master.consts;

public class CacheKeyConstant {

	private CacheKeyConstant() {
		super();
	}

	public static final String API_CACHE_ENTRY = "api_cache_entry";//动态入口
	public static final String API_CACHE_AD = "api_cache_ad"; //广告缓存
	public static final String API_CACHE_HOT_ACTIVITY = "api_cache_hot_activity";//热门赛事 缓存key
	public static final String API_CACHE_HOT_BATTLE = "api_cache_hot_battle"; //热门约战 缓存key
	public static final String API_CACHE_MID1 = "api_cache_mid1";//腰图1 缓存key
	public static final String API_CACHE_MID2 = "api_cache_mid2";//腰图2 缓存key
	public static final String API_CACHE_CARD = "api_cache_card";//卡片 缓存key
	public static final String API_CACHE_ACTIVITY_RECOMMEND = "api_cache_activity_recommend";//官方活动推荐缓存
	public static final String API_CACHE_AMUSE_RECOMMEND = "api_cache_amuse_recommend";//娱乐赛推荐缓存
	public static final String API_CACHE_INFOMATION_BANNER = "api_cache_infomation_banner";// 资讯banner 缓存key
	public static final String API_CACHE_INFOMATION_LIST = "api_cache_infomation_list";// 资讯列表 缓存key: api_cache_infomation_list_{page}

	public static final String API_CACHE_COMPOSITIVE_LIST = "api_cache_compositive_list";// 综合赛事 缓存key: api_cache_compositive_list_{page}
	public static final String API_CACHE_ATHLETICS_LIST = "api_cache_athletics_list";// 竞技大厅赛事列表 缓存key: api_cache_athletics_list_{page}
	public static final String API_CACHE_ATHLETICS_BATTLE_LIST = "api_cache_athletics_battle_list";//竞技大厅约战列表缓存
	public static final String API_CACHE_ATHLETICS_AMUSE_LIST = "api_cache_athletics_amuse_list";//竞技大厅娱乐赛列表缓存
	public static final String API_CACHE_MALL_LIST = "api_cache_mall_list";//金币商城列表缓存
	public static final String API_CACHE_MALL_BANNER = "api_cache_mall_banner"; //商城banner缓存

	public static final String API_CACHE_MATCH_LIST = "api_cache_match_list";// 热门约战列表 缓存key

	public static final String API_CACHE_ACTIVITY_APPLY_DATES_NETBARS = "api_cache_activity_apply_dates_netbars";// 赛事报名时间及网吧信息 缓存key: api_cache_activity_apply_dates_netbars_{activityId}

	// redis 座位key
	public static final String OET_ROUND_SEATNUMBER = "oet_round_seatnumber";

	public static final String API_INDEX_ALL_MATCH = "api_index_all_match";//首页全部赛事

	public static final String LIVE_USER_GAME_TYPE = "live_user_game_type";//主播直播游戏类型
	public static final String LIVE_USER_TITLE = "live_user_title";//主播直播标题
	public static final String LIVE_USER_SCREEN = "live_user_screen";//宽竖屏
	public static final String LIVE_USER_SOURCE = "live_user_source";//来源
	public static final String LIVE_USER_ONLINE_NUM = "live_user_online_num";//在线人数
	public static final String LIVE_USER_MAX_NUM = "live_user_max_num";//在线最高人数
	public static final String LIVE_USER_CURRENT_MAX_NUM = "live_user_current_max_num";//当前直播最高在线人数
	public static final String LIVE_VIDEO_PLAY_TIMES = "live_video_play_times_";//视频播放次数

	// 资讯列表缓存
	public static final String API_CACHE_ACTIVITY_OVER_ACTIVITY_LIST = "api_cache_activity_over_activity_list";
	public static final String API_CACHE_ACTIVITY_OVER_ACTIVITY_CATEGORY = "api_cache_activity_over_activity_category";

	public static final String QUICK_COMMENT_LIMT = "quick_comment_limit_";//快速评论限制1分钟只能发一次

	//拼碎片活动
	public static final String COHERE_DEBRIS_COUNT = "cohere_debris_count_"; //剩余碎片数量
	public static final String COHERE_PRIZE_COUNT = "cohere_prize_count_"; //剩余碎片数量

	public static final String DEBRIS_CHANCE_SOURCE = "debris_chance_source_";//抽奖机会来源
	public static final String DEBRIS_CHANCE_USED = "debris_chance_used_";//已使用抽奖机会
	public static final String DEBRIS_WX_CHANCE_USED = "debris_wx_chance_used_";//微信已使用抽奖机会
	public static final String COHERE_ACTIVITY_INFO = "cohere_activity_info_";//碎片活动详情

	public static final String COHERE_DEBRIS_TOUSE = "cohere_debris_touse_";//待融合碎片信息前缀

	//预注册
	public static final String PREPARE_REGISTER = "prepare_register";//预注册

	// 竞猜
	public static final String GUESSING_SYSTEM_COIN_COUNT = "guessing_system_coin_count";// 当前平台的金币余额

}
