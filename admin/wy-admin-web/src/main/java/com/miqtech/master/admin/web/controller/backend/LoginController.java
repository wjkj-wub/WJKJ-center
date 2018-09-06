package com.miqtech.master.admin.web.controller.backend;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Lists;
import com.miqtech.captcha.service.ConfigurableCaptchaService;
import com.miqtech.captcha.utils.encoder.EncoderHelper;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.config.SystemConfig;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.SystemUserConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.common.SystemUser;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.service.system.OperateService;
import com.miqtech.master.service.system.SystemSuggestionService;
import com.miqtech.master.service.system.SystemUserService;
import com.miqtech.master.thirdparty.util.img.ImgUploadUtil;
import com.miqtech.master.utils.EncodeUtils;

@Controller
public class LoginController extends BaseController {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

	private static final String SESSION_KEY_CAPTCHA = "captcha";

	protected static final String REDISKEY = "login_user_id_";
	private static final String VIEW_INDEX = "sys/index";

	@Autowired
	private SystemConfig systemConfig;
	@Autowired
	private SystemUserService systemUserService;
	@Autowired
	private OperateService operateService;
	@Autowired
	private SystemSuggestionService systemSuggestionService;
	@Autowired
	StringRedisOperateService stringRedisOperateService;

	/**
	 * 登陆验证码
	 */
	@RequestMapping("captcha")
	public ModelAndView captcha(HttpServletRequest req, HttpServletResponse res) {
		ConfigurableCaptchaService service = new ConfigurableCaptchaService();
		try {
			ServletOutputStream os = res.getOutputStream();
			String code = EncoderHelper.getChallangeAndWriteImage(service, "png", os);
			req.getSession().setAttribute(SESSION_KEY_CAPTCHA, code);
		} catch (IOException e) {
			LOGGER.error("获取输出流异常", e);
		}
		return null;
	}

	/**
	 * 登陆
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/login")
	public ModelAndView login(HttpServletRequest request, HttpServletResponse response, String username,
			String password, String captcha) {
		ModelAndView mv = new ModelAndView();
		if (request.getMethod().toUpperCase().equals("GET")) {
			backIndex(mv, null);
			return mv;
		}

		String environment = systemConfig.getEnvironment();
		boolean isOnline = StringUtils.equals(environment, "online");

		if (StringUtils.isBlank(username) || StringUtils.isBlank(password)
				|| isOnline && StringUtils.isBlank(captcha)) {
			SystemUser user = (SystemUser) request.getSession().getAttribute("user");
			if (user != null) {
				mv.setViewName(VIEW_INDEX);
				return mv;
			}
			backIndex(mv, "请输入用户名密码及验证码登录.");
			return mv;
		}

		String sessionCaptcha = (String) request.getSession().getAttribute(SESSION_KEY_CAPTCHA);
		if (isOnline && !StringUtils.equals(sessionCaptcha, captcha)) {
			backIndex(mv, "请输入正确的验证码.");
			return mv;
		}

		List<Integer> userTypes = Lists.newArrayList();
		userTypes.add(SystemUserConstant.TYPE_NORMAL_ADMIN);
		userTypes.add(SystemUserConstant.TYPE_SUPER_ADMIN);
		userTypes.add(SystemUserConstant.TYPE_ACTIVITY_ADMIN);
		userTypes.add(SystemUserConstant.TYPE_AMUSE_VERIFY);
		userTypes.add(SystemUserConstant.TYPE_AMUSE_APPEAL);
		userTypes.add(SystemUserConstant.TYPE_AMUSE_ISSUE);
		SystemUser user = systemUserService.findByUsernameAndPasswordAndUserTypes(username, password, userTypes);

		if (null != user) {
			// 查询用户菜单
			boolean allOperates = SystemUserConstant.TYPE_SUPER_ADMIN.equals(user.getUserType()) ? true : false;
			List<Map<String, Object>> menu = operateService.getOperateTreeByUserId(user.getId(), allOperates);
			Servlets.setSessionUser(request, user);
			request.getSession().setAttribute("menu", menu);
			request.getSession().setAttribute("name", user.getRealname());
			request.getSession().setAttribute("icon", user.getIcon());
			request.getSession().setAttribute("sitemesh", "Y");
			request.getSession().setAttribute("role", user.getUserType() - 1);
			request.getSession().setAttribute("userId", user.getId());
			request.getSession().setAttribute("oetName", user.getOetName());

			stringRedisOperateService.setData(REDISKEY + user.getId(),
					String.valueOf(systemSuggestionService.getNnhandleNum()), 1, TimeUnit.DAYS);

			String url = null;
			if (!SystemUserConstant.TYPE_ACTIVITY_ADMIN.equals(user.getUserType())
					&& !SystemUserConstant.TYPE_AMUSE_VERIFY.equals(user.getUserType())
					&& !SystemUserConstant.TYPE_AMUSE_APPEAL.equals(user.getUserType())
					&& !SystemUserConstant.TYPE_AMUSE_ISSUE.equals(user.getUserType())) {
				url = "/suggestion/list/1";
			} else {
				if (CollectionUtils.isNotEmpty(menu)) {
					Map<String, Object> m = menu.get(0);
					List<Map<String, Object>> children = (List<Map<String, Object>>) m.get("children");
					if (CollectionUtils.isNotEmpty(children)) {
						url = MapUtils.getString(children.get(0), "url");
					} else {
						url = MapUtils.getString(m, "url");
					}
				}
			}

			// 重定向到菜单第一项，不采用ModelAndView，避免带上通用参数，匹配不上菜单
			try {
				response.sendRedirect(url);
				return null;
			} catch (IOException e) {
				LOGGER.error("重定向异常：", e);
				mv.addObject(url);
			}

			mv.setViewName(VIEW_INDEX);
			return mv;
		} else {
			backIndex(mv, "登录失败,请检查用户名和密码.");
		}

		return mv;
	}

	/**
	 * 登出
	 */
	@RequestMapping(value = "/logout")
	public ModelAndView logout(HttpServletRequest request) {
		ModelAndView mv = new ModelAndView();
		backIndex(mv, "欢迎使用后台管理系统...");
		HttpSession session = request.getSession();
		session.invalidate();
		return mv;

	}

	/**
	 * 查询用户反馈消息未读数目
	 */
	@ResponseBody
	@RequestMapping(value = "/unhandleNum")
	public JsonResponseMsg unhandleFeedbackNum(HttpServletRequest req, String userId) {
		JsonResponseMsg result = new JsonResponseMsg();
		String num = stringRedisOperateService.getData(REDISKEY + userId);
		if (StringUtils.isBlank(num)) {
			num = "0";
		}
		result.setObject(num);

		return result;
	}

	/**
	 * 删除redis中的未读消息数
	 */
	@ResponseBody
	@RequestMapping(value = "/delNum")
	public JsonResponseMsg delFeedbackNum(HttpServletRequest req, String userId) {
		JsonResponseMsg result = new JsonResponseMsg();
		stringRedisOperateService.delData(REDISKEY + userId);

		result.setObject(0);
		return result;
	}

	/**
	 * 修改登陆用户的密码
	 */
	@ResponseBody
	@RequestMapping(value = "/changePwd")
	public JsonResponseMsg changePwd(HttpServletRequest req, String password, String oldPassword, String realname,
			String oetName) {
		JsonResponseMsg result = new JsonResponseMsg();

		SystemUser user = (SystemUser) req.getSession().getAttribute("user");
		user = systemUserService.findByUsernameAndPasswordAndUserType(user.getUsername(), oldPassword,
				user.getUserType());

		if (user == null) {
			result.fill(CommonConstant.CODE_ERROR_LOGIC, "原密码输入不正确");
			return result;
		}

		String systemName = "wy-web-admin";
		String src = ImgUploadUtil.genFilePath("sysUser");
		MultipartFile iconFile = Servlets.getMultipartFile(req, "iconFile");
		if (iconFile != null) {
			Map<String, String> saveResult = ImgUploadUtil.save(iconFile, systemName, src, true);// 保留文件名
			String url = saveResult.get(ImgUploadUtil.KEY_MAP_SRC);
			user.setIcon(url);
		}

		user.setPassword(EncodeUtils.base64Md5(password));
		if (StringUtils.isNotBlank(realname)) {
			user.setRealname(realname);
		}
		if (StringUtils.isNotBlank(oetName)) {
			user.setOetName(oetName);
		}
		systemUserService.save(user);

		req.getSession().setAttribute("icon", user.getIcon());
		req.getSession().setAttribute("name", user.getRealname());
		req.getSession().setAttribute("oetName", user.getOetName());
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}
}
