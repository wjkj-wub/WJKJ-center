package com.miqtech.master.thirdparty.util.uwan;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.miqtech.master.utils.EncodeUtils;
import com.miqtech.master.utils.HttpRequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class UwanInterface {

	private static final Logger logger = LoggerFactory.getLogger(UwanInterface.class);
	private static final Logger uwanInterfaceLogger = LoggerFactory.getLogger("uwanInterface");

	private static final String URL_API = "/api/third/wangyu";

	private static final String APP_KEY = "Ky7O8I(D(~],nwl.[jR^-wDiOtbCcW=x";
	private static final String APP_ID = "wyp5CLKN2OKJLvEj7s";

	public static final Integer TYPE_BIND_NETBAR = 102;
	public static final Integer TYPE_NETBAR_MATCHES = 300;// 查询网吧比赛结果，建议起始结束时间按天查询

	private static final String CODE_SUCCESS = "0";

	private UwanInterface() {
		super();
	}

	/**
	 * 网吧互绑
	 */
	public static boolean bindNetbar(String uwanGateway, Long wyNetbarId, Long uwanNetbarId, String netbarName) {
		if (wyNetbarId == null || uwanNetbarId == null || StringUtils.isBlank(netbarName)) {
			return false;
		}

		Integer type = TYPE_BIND_NETBAR;
		JSONObject params = new JSONObject();
		params.put("wyNetbarId", wyNetbarId);
		params.put("uwanNetbarId", uwanNetbarId);
		params.put("wyNetbarName", netbarName);
		String data = params.toJSONString();
		String response = uwanApi(uwanGateway, type, data);
		JSONObject responseJson = null;
		try {
			responseJson = JSON.parseObject(response);
		} catch (Exception e) {
			logger.error("解析响应内容异常:", e);
		}
		if (responseJson == null) {
			return false;
		}

		String code = responseJson.getString("code");
		boolean binded = CODE_SUCCESS.equals(code);
		
		if(!binded) {
			logger.error("绑定网吧失败,响应内容:" + response);
		}
		
		return binded;
	}

	/**
	 * 网吧信息查询
	 */
	public static JSONArray getNetbarMatches(String uwanGateway, Long barId, long startMatchTime, long endMatchTime) {
		if (barId == null) {
			return null;
		}

		Integer type = TYPE_NETBAR_MATCHES;
		JSONObject params = new JSONObject();
		params.put("barId", barId);
		params.put("startMatchTime", startMatchTime);
		params.put("endMatchTime", endMatchTime);
		String data = params.toJSONString();
		String response = uwanApi(uwanGateway, type, data);
		JSONObject responseJson = null;
		try {
			responseJson = JSON.parseObject(response);
		} catch (Exception e) {
			logger.error("解析响应内容异常:", e);
		}
		if (responseJson == null) {
			return null;
		}

		String code = responseJson.getString("code");
		if (!CODE_SUCCESS.equals(code)) {
			logger.error("获取网吧比赛数据失败,响应内容:" + response);
			return null;
		}

		return responseJson.getJSONObject("object").getJSONArray("matches");
	}

	/**
	 * 签名
	 */
	private static String getSign(Long timestamp, Integer type, String data) {
		if (StringUtils.isBlank(data) || timestamp == null || type == null) {
			return null;
		}

		String signStr = "appId=" + APP_ID + "&timestamp=" + timestamp + "&type=" + type + "&data=" + data + APP_KEY;
		return EncodeUtils.MD5(signStr);
	}

	/**
	 * 调用优玩接口
	 */
	private static String uwanApi(String uwanGateway, Integer type, String data) {
		if (type == null || StringUtils.isBlank(data)) {
			return null;
		}

		Map<String, String> params = Maps.newHashMap();
		params.put("appId", APP_ID);
		Long timestamp = System.currentTimeMillis();
		params.put("timestamp", timestamp.toString());
		params.put("type", type.toString());
		params.put("data", data);
		String sign = getSign(timestamp, type, data);
		params.put("sign", sign);

		uwanInterfaceLogger.debug("发起优玩接口请求:" + uwanGateway + URL_API + ";参数:" + HttpRequestUtil.genParamstrByMap(params));
		String response = HttpRequestUtil.sendPost(uwanGateway + URL_API, null, params);
		uwanInterfaceLogger.debug("得到优玩接口响应:" + response);
		return response;
	}

}
