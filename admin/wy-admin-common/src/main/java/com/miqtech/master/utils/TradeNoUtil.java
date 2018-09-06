package com.miqtech.master.utils;

import org.apache.commons.lang3.RandomUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TradeNoUtil {

	private TradeNoUtil() {
	}

	/**
	 * 产生商户订单号
	 */
	public static String getOutTradeNo(String... params) {
		StringBuilder sb = new StringBuilder();
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		sb.append(sdf.format(now) + "_");
		for (String p : params) {
			sb.append(p + "_");
		}

		// 拼接5位随机字符串
		String randomCase = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		for (int i = 0; i < 5; i++) {
			sb.append(randomCase.charAt(RandomUtils.nextInt(0, randomCase.length())));
		}

		return sb.toString();
	}

	/**
	 * 根据模块，类型，用户ID 产生商户订单号
	 * @param module 模块
	 * @param type	 模块-类型
	 */
	public static String getTradeNoByModuleAndTypeAndUserId(int module, int type, Long userId) {
		return TradeNoUtil.getOutTradeNo(String.valueOf(module), String.valueOf(type), userId.toString());
	}
}
