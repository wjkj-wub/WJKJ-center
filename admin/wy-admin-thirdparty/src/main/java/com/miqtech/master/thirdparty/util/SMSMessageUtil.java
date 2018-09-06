package com.miqtech.master.thirdparty.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.miqtech.master.utils.HttpConnectionManager;
import com.miqtech.master.utils.HttpRequestUtil;

public class SMSMessageUtil {

	private static final Logger logger = LoggerFactory.getLogger(SMSMessageUtil.class);
	public static final Joiner joiner = Joiner.on("");
	public static final String URL_MESSAGE_API = "http://182.254.141.209:8888/Modules/Interface/http/IservicesBSJY.aspx";// 短信接口地址
	public static final String API_FLAG = "sendsms";
	public static final String API_USERNAME = "mqwy";
	public static final String API_PASSWORD = "mqwy";

	public static final String KEY_MAP_RESULT = "status"; //status取值:0­未发送,1­发送成功,2­发送失败,3­反垃圾
	public static final String KEY_MAP_MSG = "msg";

	/**
	 * 发送短信
	 */
	public static Map<String, Object> sendMessage(String phoneNum, String text) {
		Map<String, Object> result = new HashMap<String, Object>();

		try {
			String requestStr = HttpRequestUtil.sendPost(URL_MESSAGE_API, getParams(phoneNum, text));
			if (requestStr.split(",")[0].trim().equals("0")) {
				result.put(KEY_MAP_RESULT, true);
				result.put(KEY_MAP_MSG, "成功");
			} else {
				result.put(KEY_MAP_RESULT, false);
				result.put(KEY_MAP_MSG, requestStr.split(",")[1].trim());
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			result.put(KEY_MAP_RESULT, false);
			result.put(KEY_MAP_MSG, "参数格式化错误");
		}

		return result;
	}

	/**
	 * 将号码和短信内容打包成请求参数
	 */
	private static String getParams(String phoneNum, String text) throws UnsupportedEncodingException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("flag", API_FLAG);
		params.put("loginname", API_USERNAME);
		params.put("password", API_PASSWORD);
		params.put("p", phoneNum);// 手机号码
		params.put("c", URLEncoder.encode(text, "UTF-8"));
		params.put("d", URLEncoder.encode(
				new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()), "UTF-8"));

		return map2ParamString(params);
	}

	/**
	 * 将map格式转换为param string格式
	 */
	private static String map2ParamString(Map<String, String> params) {
		Set<Entry<String, String>> keys = params.entrySet();

		StringBuffer paramStr = new StringBuffer();
		for (Entry<String, String> k : keys) {
			if (paramStr.length() > 0) {
				paramStr.append("&");
			}
			paramStr.append(k.getKey() + "=" + k.getValue());
		}

		return paramStr.toString();
	}

	/**
	 * 产生验证码（随机6位数字）
	 */
	public static String genAuthCode() {
		StringBuffer result = new StringBuffer();

		String base = "1234567890";
		for (int i = 0; i < 6; i++) {
			int random_num = (int) (Math.random() * new Double(base.length() - 1));
			result.append(base.charAt(random_num));
		}

		return result.toString();
	}

	//-------------------------------------------------消息模板---------------------------------------------------------
	public static String getRegisterTemplate(String value) {
		return joiner.join("您的验证码为：", value, "，请在10分钟内输入，约战·订座·电竞·优惠，尽在网娱大师。");
	}

	public static String getPasswordTemplate(String value) {
		return joiner.join("您的验证码是：", value, "，为了您的账户安全，请妥善保护好您的账号资料，约战·订座·电竞·优惠，尽在网娱大师。");
	}

	public static String getPhoneVerifyTemplate(String value, String netbarName) {
		return joiner.join("您的手机号申请绑定您的网吧", netbarName, "，验证码为：", value, "，请在10分钟内输入，欢迎使用网娱大师！");
	}

	public static String getPayaccountVerifyTemplate(String value) {
		return joiner.join("您的验证码为：", value, "，为了您的账户安全，请妥善保护好您的账号资料，约战·订座·电竞·优惠，尽在网娱大师。");
	}

	public static String getMerchantResetPasswordVerifyTemplate(String value) {
		return joiner.join("您的验证码为：", value, "，为了您的账户安全，请妥善保护好您的账号资料，约战·订座·电竞·优惠，尽在网娱大师。");
	}

	public static String getEditRebateVerifyTemplate(String value) {
		return joiner.join("您的验证码为：", value, "，为了您的账户安全，请妥善保护好您的账号资料，约战·订座·电竞·优惠，尽在网娱大师。");
	}

	public static String getMatchInvocationTemplate(String matchName, String userName, String telephone,
			String activityShareLink) {
		return joiner.join("嘿，你的小伙伴", userName, "（", StringUtils.substring(telephone, 7), "）邀请你加入他的约战：", matchName,
				"，详情查看：", activityShareLink);
	}

	public static String getActivityInvocationTemplate(String activityName, String userName, String activityShareLink) {
		return joiner.join("嘿，你的小伙伴", userName, "报名参加了", activityName, "，邀请你加入他的战队共谋巨额奖金，详情查看：", activityShareLink);
	}

	public static String getMerchantAuditedTemplate(String phoneNum) {
		return joiner.join("您的网吧已通过入驻审核，登陆账号为", phoneNum, " ，初始密码123456，请即刻登陆wb.wangyuhudong.com修改密码、丰富信息！");
	}

	//----------------------------------------------------短信服务分割线---------------------------------------------------------------------
	private static final String SMS_TEMPLATE_API = "https://api.netease.im/sms/sendtemplate.action";

	/**
	 * 发送短信
	 */
	public static Map<String, Object> sendTemplateMessage(String[] phoneNum, String templateId, String[] params) {
		Map<String, Object> postParams = Maps.newHashMap();
		postParams.put("templateid", templateId);
		postParams.put("mobiles", new Gson().toJson(phoneNum));
		postParams.put("params", new Gson().toJson(params));
		Map<String, Object> apiResult = sendPost(SMS_TEMPLATE_API, postParams);

		Map<String, Object> result = Maps.newHashMap();
		double status = NumberUtils.toDouble(apiResult.get("code").toString());
		result.put(KEY_MAP_RESULT, status == 200 ? "1" : "0");
		result.put(KEY_MAP_MSG, apiResult.get("msg"));

		logger.error("调用网易短信接口发送结果:模板id[{}],手机号:[{}].返回状态信息:[{}]", templateId, phoneNum[0], apiResult.toString());
		return result;
	}

	private static Map<String, Object> sendPost(String url, Map<String, Object> params) {
		CloseableHttpClient httpClient = HttpConnectionManager.getHttpClient();
		HttpPost post = new HttpPost(SMS_TEMPLATE_API);
		post.addHeader("AppKey", "ef6056403530ec7bc16915454e805f48");
		String currentTimeMillis = System.currentTimeMillis() + "";
		post.addHeader("CurTime", currentTimeMillis);
		String nonce = UUID.randomUUID().toString();
		String checkSum = NeteaseIMCheckSumBuilder.getCheckSum("77c88569c5d4", nonce, currentTimeMillis);
		post.addHeader("CheckSum", checkSum);
		post.addHeader("Nonce", nonce);
		post.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

		if (MapUtils.isNotEmpty(params)) {
			postParams(post, params);
		}

		CloseableHttpResponse execute = null;
		try {
			execute = httpClient.execute(post);
			HttpEntity entity = execute.getEntity();
			if (null != entity) {
				String temp = EntityUtils.toString(entity);
				Gson gson = new Gson();
				@SuppressWarnings("unchecked")
				Map<String, Object> map = gson.fromJson(temp, Map.class);
				if (MapUtils.isNotEmpty(map)) {
					return map;
				}
			}
		} catch (Exception e) {
		}
		return null;

	}

	private static HttpPost postParams(HttpPost httPost, Map<String, Object> params) {
		List<NameValuePair> nvps = Lists.newArrayList();
		Set<String> keySet = params.keySet();
		for (String key : keySet) {
			nvps.add(new BasicNameValuePair(key, params.get(key).toString()));
		}
		httPost.setEntity(new UrlEncodedFormEntity(nvps, Charset.forName("UTF-8")));
		return httPost;

	}

	public static void main(String[] args) {
		sendTemplateMessage(new String[] { "safsdafsdfsd" }, "4028", new String[] { "fdsaf", "" });
	}
}
