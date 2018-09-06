package com.miqtech.master.admin.web.controller.api;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.exception.ParameterErrorException;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.utils.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.sql.SQLDataException;
import java.util.concurrent.TimeUnit;

public class BaseController {

	private static final Logger LOGGER = LoggerFactory.getLogger(BaseController.class);

	public static final String ADMIN_USER_TOKEN_PREFIX = "wy_admin_user_token_";
	public static final Integer ADMIN_USER_TOKEN_TIMEOUT = 1800;// 单位:秒

	public static final String WEB_ADMIN_LOGIN_TOKEN = "wy_admin_netbar_token";
	public static final String WEB_ADMIN_LOGIN_USERID = "wy_admin_netbar_userid";

	@Resource
	protected StringRedisOperateService redisOperateService;

	protected boolean isLoginStatusValid(Long userId, String token) {
		if (StringUtils.isBlank(token)) {
			return false;
		}
		String redisToken = redisOperateService.getData(tokenKey(String.valueOf(userId)));
		return StringUtils.isNotBlank(redisToken) && StringUtils.equals(token, redisToken);
	}

	protected boolean isLoginStatusInValid(Long userId, String token) {
		return !isLoginStatusValid(userId, token);
	}

	protected boolean isNotLogined(Long userId) {
		return null == userId || userId.longValue() <= 0;
	}

	protected void addTokenToCache(String userId, String token) {
		redisOperateService.setData(tokenKey(userId), token, ADMIN_USER_TOKEN_TIMEOUT, TimeUnit.SECONDS);
	}

	private String tokenKey(String userId) {
		return ADMIN_USER_TOKEN_PREFIX + userId.toString();
	}

	protected String getToken(Long userId) {
		return redisOperateService.getData(tokenKey(String.valueOf(userId)));
	}

	protected JsonResponseMsg checkUserValid(Long userId, String token, JsonResponseMsg result) {
		if (isNotLogined(userId)) {
			return result.fill(CommonConstant.CODE_NOT_LOGIN, CommonConstant.MSG_NOT_LOGIN);
		}
		if (isLoginStatusInValid(userId, token)) {
			return result.fill(CommonConstant.CODE_LOGIN_INVALID, CommonConstant.MSG_LOGIN_INVALID);
		}
		return result;
	}

	/**
	 * 从cookie中获取userId
	 */
	protected Long getUserIdFromCookie(HttpServletRequest request) {
		String userId = CookieUtils.getCookie(request, BaseController.WEB_ADMIN_LOGIN_USERID);
		return userId == null ? null : NumberUtils.toLong(userId);
	}

	/**
	 * 异常统一处理
	 */
	@ExceptionHandler
	@ResponseBody
	public JsonResponseMsg exceptionHandler(Exception ex) {
		JsonResponseMsg result = new JsonResponseMsg();

		if (ex instanceof SQLDataException) {
			result.fill(CommonConstant.CODE_ERROR_DATABASE, CommonConstant.MSG_ERROR_DATABASE);
		} else if (ex instanceof ParameterErrorException) {
			result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		} else {
			result.fill(CommonConstant.CODE_ERROR_LOGIC, CommonConstant.MSG_ERROR_LOGIC);
		}

		LOGGER.error(result.getResult() + "：{}", ex);

		return result;
	}

}
