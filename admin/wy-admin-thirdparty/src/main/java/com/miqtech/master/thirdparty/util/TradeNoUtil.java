package com.miqtech.master.thirdparty.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.RandomUtils;

public class TradeNoUtil {
	/**
	 * 产生商户订单号
	 * 
	 * @param params
	 * @return
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

}
