package com.miqtech.master.thirdparty.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.miqtech.master.utils.HttpRequestUtil;
import com.miqtech.master.utils.StringUtils;

/**
 * 短网址服务
 */
public class ShortUrlUtils {

	private static Logger LOGGER = LoggerFactory.getLogger(ShortUrlUtils.class);
	private static String REQUEST_URL = "http://api.t.sina.com.cn/short_url/shorten.json?source=3213676317&url_long=";// "http://dwz.wailian.work/api.php?";

	/**
	 * 将正常网址转为短网址
	 */
	public static String toShortUrl(String url) {
		if (StringUtils.isBlank(url)) {
			return null;
		}

		String response = HttpRequestUtil.sendGet(REQUEST_URL + url, "");
		if (StringUtils.isNotBlank(response)) {
			JSONObject resJson = (JSONObject) JSONObject.parseArray(response).get(0);
			String result = resJson.getString("type");
			if ("0".equals(result)) {
				return resJson.getString("url_short");
			} else {
				LOGGER.error("生成短网址失败:" + response);
			}
		}
		return null;
	}

}
