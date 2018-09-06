package com.miqtech.master.admin.web.interceptor;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.miqtech.master.admin.web.annotation.LoginValid;
import com.miqtech.master.admin.web.controller.api.BaseController;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.utils.CookieUtils;
import com.miqtech.master.utils.JsonUtils;

public class LoginValidInterceptor extends HandlerInterceptorAdapter {
	@Autowired
	protected StringRedisOperateService redisOperateService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (handler.getClass().isAssignableFrom(HandlerMethod.class)) {
			LoginValid valid = ((HandlerMethod) handler).getMethodAnnotation(LoginValid.class);
			if (valid == null || valid.valid() == false) {// 没有声明需要权限,或者声明不验证权限
				return true;
			} else {// 登录权限校验
				String userId = CookieUtils.getCookie(request, BaseController.WEB_ADMIN_LOGIN_USERID);
				if (userId == null) { // 管理员用户id为空
					response.getWriter().write(JsonUtils.objectToString(new JsonResponseMsg().fill(-1, "登录信息无效")));// 返回错误信息到前端
					response.setCharacterEncoding("UTF-8");
					response.setContentType("application/json; charset=utf-8");
					return false;
				}
				String token = CookieUtils.getCookie(request, BaseController.WEB_ADMIN_LOGIN_TOKEN);
				long userIdLong = NumberUtils.toLong(userId);
				if (isLoginStatusValid(userIdLong, token)) { // 登录有效性检测
					initTokenExpire(response, userIdLong, token);
					return true;
				} else {
					response.getWriter().write(JsonUtils.objectToString(new JsonResponseMsg().fill(-1, "登录信息无效")));
					response.setCharacterEncoding("UTF-8");
					response.setContentType("application/json; charset=utf-8");
					return false;
				}
			}
		} else {
			return true;
		}
	}

	public boolean isLoginStatusValid(Long userId, String token) {
		if (StringUtils.isBlank(token)) {
			return false;
		}
		String redisToken = redisOperateService.getData(tokenKey(String.valueOf(userId)));
		return StringUtils.isNotBlank(redisToken) && StringUtils.equals(token, redisToken);
	}

	public void initTokenExpire(HttpServletResponse res, Long userId, String token) {
		if (res == null || userId == null || StringUtils.isBlank(token)) {
			return;
		}

		// 初始化cookie
		Cookie tokenCookie = new Cookie(BaseController.WEB_ADMIN_LOGIN_TOKEN, token); // 用户token
		Cookie idCookie = new Cookie(BaseController.WEB_ADMIN_LOGIN_USERID, userId.toString());// 用户id
		tokenCookie.setPath("/");
		idCookie.setPath("/");
		idCookie.setMaxAge(30 * 60);
		tokenCookie.setMaxAge(30 * 60);
		res.addCookie(idCookie);
		res.addCookie(tokenCookie);

		// 延长redis超时时间
		redisOperateService.expire(tokenKey(String.valueOf(userId)), BaseController.ADMIN_USER_TOKEN_TIMEOUT,
				TimeUnit.SECONDS);
	}

	private String tokenKey(String userId) {
		return BaseController.ADMIN_USER_TOKEN_PREFIX + userId.toString();
	}

	protected String getToken(Long userId) {
		return redisOperateService.getData(tokenKey(String.valueOf(userId)));
	}

}
