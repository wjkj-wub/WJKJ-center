package com.miqtech.master.consts.uwan;

public class UwanNetbarConstant {

	private UwanNetbarConstant() {
		super();
	}

	// 网吧类型
	public static final Integer TYPE_NETBAR = 1;// 网吧
	public static final Integer TYPE_PLACE = 2;// 场馆

	// 网吧来源
	public static final Integer SOURCE_WARMUP = 1;// 预热赛
	public static final Integer SOURCE_AUDITION = 2;// 海选赛
	public static final Integer SOURCE_WARMUP_AUDITION = 3;// 预热赛+海选赛
	public static final Integer SOURCE_NETBAR = 4;// 吧内赛及网吧大战
	
	// 网吧操作相关rediskey
	public static final String REDIS_KEY_UWAN_NETBAR_MATCH_SYNCING = "wyadmin_uwan_netbar_match_syncing";
}
