package com.miqtech.master.service.common;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;

@Component
public class SMSVerifyService {

	public static final int SMS_CODE_TYPE_REG = 1;//用户注册
	public static final int SMS_CODE_TYPE_FIND = 2;//找回密码
	public static final int SMS_CODE_TYPE_USE_REDBAG = 3;//使用红包
	public static final int SMS_CODE_TYPE_QUICK_LOGIN = 4;//快捷登录
	public static final int SMS_CODE_TYPE_THIRD_LOGIN = 5;//第三方登录
	@Autowired
	StringRedisOperateService stringRedisOperateService;
	/*商户端系统前缀*/
	public static final String SMS_CODE_MERCHANT_EDIT_PHONE = "master_merchant_sms_edit_phone";
	public static final String SMS_CODE_MERCHANT_EDIT_ACCOUNT = "master_merchant_sms_edit_account";
	public static final String SMS_CODE_MERCHANT_EDIT_PASSWORD = "master_merchant_sms_edit_password";
	public static final String SMS_CODE_MERCHANT_EDIT_REBATE = "master_merchant_sms_edit_rebate";
	/*原生app接口前缀*/
	public static final String SMS_CODE_API_USER_REG = "master_api_sms_user_reg";
	public static final String SMS_CODE_API_USER_REG_ONE_MINUTE_LIMIT = "master_api_sms_reg_limit";
	public static final String SMS_CODE_API_FIND_PASSWORD = "master_api_sms_user_find_password";
	public static final String SMS_CODE_API_FIND_PASSWORD_ONE_MINUTE_LIMIT = "master_api_sms_findpassword_limit";
	public static final String SMS_CODE_API_QUICK_LOGIN = "master_api_sms_quick_login";
	public static final String SMS_CODE_API_QUICK_LOGIN_ONE_MINUTE_LIMIT = "master_api_sms_quicklogin_limit";
	public static final String SMS_CODE_API_QUICK_LOGIN_TWO_HOUR_LIMIT = "master_api_sms_quicklogin_two_hour_limit";
	public static final String SMS_CODE_API_THIRD_LOGIN = "master_api_sms_third_login";
	public static final String SMS_CODE_API_THIRD_LOGIN_ONE_MINUTE_LIMIT = "master_api_sms_thirdlogin_limit";
	public static final String SMS_CODE_API_THIRD_LOGIN_TWO_HOUR_LIMIT = "master_api_sms_thirdlogin_two_hour_limit";

	public static final String SMS_CODE_API_USE_REDBAG = "master_api_sms_use_redbag";
	public static final String SMS_CODE_API_USE_REDBAG_ONE_MINUTE_LIMIT = "master_api_sms_use_redbag_limit";
	public static final String SMS_CODE_API_FIND_PASSWORD_COMMIT = "master_api_sms_user_find_password_commit";

	private final static Joiner JOINER = Joiner.on("_");

	public boolean verify(String prefix, String phone, String code) {
		String key = joinKey(prefix, phone);
		return stringRedisOperateService.verify(key, code);
	}

	public void addDataToCache(String prefix, String phone, String code) {
		String key = joinKey(prefix, phone);
		stringRedisOperateService.setData(key, code, 10, TimeUnit.MINUTES);

	}

	public void addDataToCache(String prefix, String phone, String value, int hour) {
		String limitKey = joinKey(prefix, phone);
		stringRedisOperateService.setData(limitKey, value, hour, TimeUnit.HOURS);
	}

	public void addLimitDataToCache(String prefix, String phone) {
		String limitKey = joinKey(prefix, phone);
		stringRedisOperateService.setData(limitKey, "1", 1, TimeUnit.MINUTES);
	}

	public String joinKey(String prefix, String phone) {
		return JOINER.join(prefix, phone);
	}

	public boolean canSendSMSCode(String mobile, int type) {
		if (type == SMSVerifyService.SMS_CODE_TYPE_REG) {
			String key = joinKey(SMS_CODE_API_USER_REG_ONE_MINUTE_LIMIT, mobile);
			String data = stringRedisOperateService.getData(key);
			if (StringUtils.equals("1", data)) {
				return false;
			}
			return true;
		} else if (type == SMSVerifyService.SMS_CODE_TYPE_FIND) {
			String key = joinKey(SMS_CODE_API_FIND_PASSWORD_ONE_MINUTE_LIMIT, mobile);
			String data = stringRedisOperateService.getData(key);
			if (StringUtils.equals("1", data)) {
				return false;
			}
			return true;
		} else if (type == SMSVerifyService.SMS_CODE_TYPE_USE_REDBAG) {
			String key = joinKey(SMS_CODE_API_USE_REDBAG_ONE_MINUTE_LIMIT, mobile);
			String data = stringRedisOperateService.getData(key);
			if (StringUtils.equals("1", data)) {
				return false;
			}
			return true;
		} else if (type == SMSVerifyService.SMS_CODE_TYPE_QUICK_LOGIN) {
			String key = joinKey(SMS_CODE_API_QUICK_LOGIN_ONE_MINUTE_LIMIT, mobile);
			String data = stringRedisOperateService.getData(key);
			if (StringUtils.equals("1", data)) {
				return false;
			}
			return true;
		}
		return true;
	}
}
