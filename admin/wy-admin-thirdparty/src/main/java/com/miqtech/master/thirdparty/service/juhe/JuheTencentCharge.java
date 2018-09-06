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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.miqtech.master.utils.EncodeUtils;

/**
 *聚合数据充值平台--游戏直充
 */

public class JuheTencentCharge {
	private static final Logger LOGGER = LoggerFactory.getLogger(JuheTencentCharge.class);

	public static final String DEF_CHATSET = "UTF-8";
	public static final int DEF_CONN_TIMEOUT = 30000;
	public static final int DEF_READ_TIMEOUT = 30000;
	public static String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36";

	//配置申请的KEY
	public static final String APPKEY = "4b62f8294b4c89c6370b7a9e9b2d564b";

	//6.腾讯Q币会员充值
	public static String orderQ(Map<String, String> paramsInput) {
		String result = null;
		String url = "http://v.juhe.cn/tencent/onlineorder";//请求接口地址
		Map<String, String> params = Maps.newHashMap();
		params.put("proid", "50001");
		params.put("nums", paramsInput.get("nums"));//购买数量
		params.put("uorderid", paramsInput.get("uorderid"));//订单号，8-32位数字字母组合
		params.put("game_userid", paramsInput.get("game_userid"));
		params.put("key", APPKEY);//应用APPKEY(应用详细页查询)
		String sign = "50001" + paramsInput.get("nums").toString() + paramsInput.get("uorderid").toString()
				+ params.get("game_userid").toString() + APPKEY + "wangyudashi";
		params.put("sign", EncodeUtils.MD5(sign));//校验值，md5(proid+nums+uorderid+game_userid+key+uid)
		try {
			result = net(url, params, "GET");
		} catch (Exception e) {
			LOGGER.error("聚合平台游戏直充接口异常：", e);
		}

		return result;
	}

	//6.腾讯Q币会员充值
	public static String checkChargeStatus(String orderId) {
		String result = null;
		String url = "http://v.juhe.cn/tencent/ordersta";//请求接口地址
		Map<String, String> params = Maps.newHashMap();
		params.put("proid", orderId);
		params.put("key", APPKEY);//应用APPKEY(应用详细页查询)
		try {
			result = net(url, params, "GET");
		} catch (Exception e) {
			LOGGER.error("聚合平台游戏查询接口异常：", e);
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