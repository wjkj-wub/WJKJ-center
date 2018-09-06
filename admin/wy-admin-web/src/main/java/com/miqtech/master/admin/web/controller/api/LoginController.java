package com.miqtech.master.admin.web.controller.api;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;
import com.miqtech.master.admin.web.annotation.CrossDomain;
import com.miqtech.master.admin.web.annotation.LoginValid;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.SystemUserConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.common.SystemUser;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.service.system.SystemUserService;
import com.miqtech.master.utils.CookieUtils;
import com.miqtech.master.utils.IdentityUtils;

/**
 * 登录相关操作接口
 */
@Controller("adminApiLoginController")
@RequestMapping("/api")

public class LoginController extends BaseController {

	@Autowired
	private SystemUserService systemUserService;
	@Autowired
	private StringRedisOperateService stringRedisOperateService;

	/**
	 * 登录接口
	 */
	@RequestMapping(value = "login")
	@ResponseBody
	@CrossDomain(value = true)
	public JsonResponseMsg login(HttpServletResponse res, String username, String password)
			throws UnsupportedEncodingException {
		JsonResponseMsg result = new JsonResponseMsg();
		if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, "账号密码不能为空");
		}

		List<Integer> userTypes = Lists.newArrayList();
		userTypes.add(SystemUserConstant.TYPE_NORMAL_ADMIN);
		userTypes.add(SystemUserConstant.TYPE_SUPER_ADMIN);
		userTypes.add(SystemUserConstant.TYPE_ACTIVITY_ADMIN);
		userTypes.add(SystemUserConstant.TYPE_AMUSE_VERIFY);
		userTypes.add(SystemUserConstant.TYPE_AMUSE_APPEAL);
		userTypes.add(SystemUserConstant.TYPE_AMUSE_ISSUE);
		SystemUser user = systemUserService.findByUsernameAndPasswordAndUserTypes(username, password, userTypes);
		if (user == null) {
			return result.fill(-1, "登录失败,请检查用户名和密码.");
		}
		String token = IdentityUtils.uuidWithoutSplitter();
		Cookie tokenCookie = new Cookie(BaseController.WEB_ADMIN_LOGIN_TOKEN, token); // 用户token
		Cookie idCookie = new Cookie(BaseController.WEB_ADMIN_LOGIN_USERID, user.getId().toString());// 用户id
		tokenCookie.setPath("/");
		idCookie.setPath("/");
		idCookie.setMaxAge(30 * 60);
		tokenCookie.setMaxAge(30 * 60);
		res.addCookie(idCookie);
		res.addCookie(tokenCookie);
		addTokenToCache(user.getId().toString(), token);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, username);
	}

	/**
	 * 判断用户是否登录：
	 * LoginValid 注解已在拦截器中判断，故此处直接返回成功即可
	 */
	@LoginValid(valid = true)
	@RequestMapping("/isLoggedIn")
	@ResponseBody
	public JsonResponseMsg isLogin() {
		return new JsonResponseMsg().fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 * 登出
	 */
	@RequestMapping(value = "loginOut")
	@ResponseBody
	public JsonResponseMsg loginOut(HttpServletRequest request) {
		String userId = CookieUtils.getCookie(request, BaseController.WEB_ADMIN_LOGIN_USERID);
		if (NumberUtils.isNumber(userId)) {
			stringRedisOperateService.delData(BaseController.ADMIN_USER_TOKEN_PREFIX + userId);
		}
		return new JsonResponseMsg().fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}
}