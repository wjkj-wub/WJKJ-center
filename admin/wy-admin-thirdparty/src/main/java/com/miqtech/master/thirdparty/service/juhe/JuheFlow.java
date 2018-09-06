package com.miqtech.master.thirdparty.service.juhe;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.miqtech.master.utils.EncodeUtils;

/**
 *聚合数据充值平台--游戏直充
 */

public class JuheFlow {
	private static final Logger LOGGER = LoggerFactory.getLogger(JuheFlow.class);

	public static final String DEF_CHATSET = "UTF-8";
	public static final int DEF_CONN_TIMEOUT = 30000;
	public static final int DEF_READ_TIMEOUT = 30000;
	public static String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36";

	//配置申请的KEY
	public static final String APPKEY = "2f8dda7ea0630ead725d7eb1bdc14c12";
	public static final String openId = "JH7f813c262ff1733bae896662be82b2d8";//在个人中心查询

	//2.检测号码支持的流量套餐
	public static String telcheck(String phone, String quota) {
		if (quota == null) {
			return null;
		}
		String result = null;
		String url = "http://v.juhe.cn/flow/telcheck";//请求接口地址
		Map<String, String> params = Maps.newHashMap();
		params.put("phone", phone);//要查询的手机号码
		params.put("key", APPKEY);//应用APPKEY(应用详细页查询)

		try {
			result = net(url, params, "GET");
			Gson gson = new Gson();
			JSONObject object = gson.fromJson(result, JSONObject.class);
			if (object.getInteger("error_code") == 0) {
				@SuppressWarnings("unchecked")
				List<JSONObject> resultList = (List<JSONObject>) object.get("result");
				Map<String, Object> resultMap = resultList.get(0);
				@SuppressWarnings("unchecked")
				List<Map<String, String>> flowslist = (List<Map<String, String>>) resultMap.get("flows");

				for (Map<String, String> map : flowslist) {
					if (quota.equals(map.get("v"))) {
						return map.get("id");
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("聚合平台查询流量套餐接口异常：", e);
		}
		return "";
	}

	//3.流量充值
	public static String flowCharge(Map<String, String> paramsInput) {
		String result = null;
		String url = "http://v.juhe.cn/flow/recharge";//请求接口地址
		Map<String, String> params = Maps.newHashMap();
		params.put("phone", paramsInput.get("phone"));//需要充值流量的手机号码
		params.put("pid", telcheck(paramsInput.get("phone"), paramsInput.get("quota")));//流量套餐ID
		params.put("orderid", paramsInput.get("orderid"));//自定义订单号，8-32字母数字组合
		params.put("key", APPKEY);//应用APPKEY(应用详细页查询)
		String sign = openId + APPKEY + params.get("phone") + params.get("pid") + params.get("orderid");
		params.put("sign", EncodeUtils.MD5(sign));//校验值，md5(<b>OpenID</b>+key+phone+pid+orderid)，结果转为小写

		try {
			result = net(url, params, "GET");
		} catch (Exception e) {
			LOGGER.error("聚合平台流量充值接口异常：", e);
		}

		return result;
	}

	/**
	 * @param strUrl 请求地址
	 * @param params 请求参数
	 * @param method 请求方法
	 * @return  网络请求字符串
	 * @throws Exception
	 */
	public static String net(String strUrl, Map<String, String> params, String method) throws Exception {
		HttpURLConnection conn = null;
		BufferedReader reader = null;
		String rs = null;
		try {
			StringBuffer sb = new StringBuffer();
			if (method == null || method.equals("GET")) {
				strUrl = strUrl + "?" + urlencode(params);
			}
			URL url = new URL(strUrl);
			conn = (HttpURLConnection) url.openConnection();
			if (method == null || method.equals("GET")) {
				conn.setRequestMethod("GET");
			} else {
				conn.setRequestMethod("POST");
				conn.setDoOutput(true);
			}
			conn.setRequestProperty("User-agent", userAgent);
			conn.setUseCaches(false);
			conn.setConnectTimeout(DEF_CONN_TIMEOUT);
			conn.setReadTimeout(DEF_READ_TIMEOUT);
			conn.setInstanceFollowRedirects(false);
			conn.connect();
			if (params != null && method.equals("POST")) {
				try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
					out.writeBytes(urlencode(params));
				}
			}
			InputStream is = conn.getInputStream();
			reader = new BufferedReader(new InputStreamReader(is, DEF_CHATSET));
			String strRead = null;
			while ((strRead = reader.readLine()) != null) {
				sb.append(strRead);
			}
			rs = sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				reader.close();
			}
			if (conn != null) {
				conn.disconnect();
			}
		}
		return rs;
	}

	/**
	 * 将map型转为请求参数型
	 */
	public static String urlencode(Map<String, String> data) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> i : data.entrySet()) {
			try {
				sb.append(i.getKey()).append("=").append(URLEncoder.encode(i.getValue() + "", "UTF-8")).append("&");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}