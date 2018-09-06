package com.miqtech.master.thirdparty.util;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.miqtech.master.consts.MsgConstant;

import cn.jpush.api.JPushClient;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.PushPayload.Builder;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.Notification;

/**
 * 注意:setOptions(Options.newBuilder().setApnsProduction(true).build()
 * true标识ios的生产环境,false或者不设置为开发环境
 */
public class JPushUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(JPushUtils.class);

	private static boolean isOnlie = true;//配置是否是生产环境,开发时改为false

	public static boolean isOnlie() {
		return isOnlie;
	}

	public static void setOnlie(boolean isOnlie) {
		JPushUtils.isOnlie = isOnlie;
	}

	private static final String CLIENT_APP_KEY = "628c53b5691aae4d0a8c62ae";
	private static final String CLIENT_MASTER_SECRET = "6aa9ad34be7a5437570c2ed5";

	private static final JPushClient CLIENT_JPUSH_CLIENT = new JPushClient(CLIENT_MASTER_SECRET, CLIENT_APP_KEY, 3);

	public static void sendToAll(String content, String category, String extendData) {

		try {
			CLIENT_JPUSH_CLIENT.sendPush(buildIOSAlertAllLoad(content, category, null, extendData));
		} catch (Exception e) {
			LOGGER.error("ios-client-all error,the reason is  {}", e.getMessage());
		}
		try {
			CLIENT_JPUSH_CLIENT.sendPush(buildAndroidAlertAllLoad(content, category, null, extendData));
		} catch (Exception e) {
			LOGGER.error("android-client-all error,the reason is  {}", e.getMessage());
		}
	}

	public static void sendToClientTag(String tag, String content, String category, String value, String extendData) {
		try {
			CLIENT_JPUSH_CLIENT.sendPush(buildIOSTagLoad(tag, content, category, value, extendData));
		} catch (Exception e) {
			LOGGER.error("ios-client-tag error,the reason is   {}", e.getMessage());
		}

		try {
			CLIENT_JPUSH_CLIENT.sendPush(buildAndroidTagLoad(tag, content, category, value, extendData));
		} catch (Exception e) {
			LOGGER.error("android-client-tag error,the reason is   {}", e.getMessage());
		}
	}

	public static void sendToClientALias(String alias, String content, String category, String value,
			String extendData) {
		try {
			CLIENT_JPUSH_CLIENT.sendPush(buildIOSAliasLoad(alias, content, category, value, extendData));
		} catch (Exception e) {
			LOGGER.error("ios-client-alias error,the reason is  {}", e.getMessage());
		}
		try {
			CLIENT_JPUSH_CLIENT.sendPush(buildAndroidAliasLoad(alias, content, category, value, extendData));
		} catch (Exception e) {
			LOGGER.error("android-client-alias error,the reason is  {}", e.getMessage());
		}
	}

	public static PushPayload buildIOSAlertAllLoad(String content, String category, String value, String extendData) {
		Builder build = PushPayload.newBuilder();
		build = build.setPlatform(Platform.ios());//设置ios平台
		build = build.setAudience(Audience.all());
		build = build.setNotification(Notification.ios(content, fillExtras(category, value, extendData)));//设置发送内容
		build = build.setOptions(Options.newBuilder().setApnsProduction(isOnlie).build());
		return build.build();
	}

	public static PushPayload buildIOSAliasLoad(String alias, String content, String category, String value,
			String extendData) {
		Builder build = PushPayload.newBuilder();
		build = build.setPlatform(Platform.ios());//设置ios平台
		build = build.setAudience(Audience.alias(alias));//设置alias
		build = build.setNotification(Notification.ios(content, fillExtras(category, value, extendData)));//设置发送内容
		build = build.setOptions(Options.newBuilder().setApnsProduction(isOnlie).build());
		return build.build();

	}

	public static PushPayload buildIOSTagLoad(String tag, String content, String category, String value,
			String extendData) {
		Builder build = PushPayload.newBuilder();
		build = build.setPlatform(Platform.ios());//设置ios平台
		build = build.setAudience(Audience.tag(tag));//设置tag
		build = build.setNotification(Notification.ios(content, fillExtras(category, value, extendData)));//设置发送内容
		build = build.setOptions(Options.newBuilder().setApnsProduction(isOnlie).build());
		return build.build();
	}

	public static PushPayload buildAndroidAlertAllLoad(String content, String category, String value,
			String extendData) {
		Builder build = PushPayload.newBuilder();
		build = build.setPlatform(Platform.android());//设置android平台
		build = build.setAudience(Audience.all());
		build = build.setNotification(Notification.android(content, "", fillExtras(category, value, extendData)));//设置发送内容
		return build.build();
	}

	public static PushPayload buildAndroidAliasLoad(String alias, String content, String category, String value,
			String extendData) {
		Builder build = PushPayload.newBuilder();
		build = build.setPlatform(Platform.android());//设置android平台
		build = build.setAudience(Audience.alias(alias));//设置alias
		build = build.setNotification(Notification.android(content, "", fillExtras(category, value, extendData)));//设置发送内容
		return build.build();
	}

	public static PushPayload buildAndroidTagLoad(String tag, String content, String category, String value,
			String extendData) {
		Builder newBuilder = PushPayload.newBuilder();
		newBuilder = newBuilder.setPlatform(Platform.android());//设置android平台
		newBuilder = newBuilder.setAudience(Audience.tag(tag));//设置tag
		newBuilder = newBuilder
				.setNotification(Notification.android(content, "", fillExtras(category, value, extendData)));//设置发送内容
		return newBuilder.build();
	}

	private static Map<String, String> fillExtras(String category, String value, String extendData) {
		Map<String, String> extras = Maps.newHashMap();
		extras.put(MsgConstant.PUSH_MSG_TYPE_KEY, category);
		if (null != value) {
			extras.put(MsgConstant.PUSH_MSG_OBJECT_ID_KEY, value);
		}
		if (null != extendData) {
			extras.put(MsgConstant.PUSH_MSG_EXTEND_DATA, extendData);
		}
		return extras;
	}

}
