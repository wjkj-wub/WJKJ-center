package com.miqtech.master.admin.web.controller.backend;

import com.miqtech.master.admin.web.task.UserTask;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.service.user.UserInfoService;
import com.miqtech.master.thirdparty.util.SMSMessageUtil;
import com.miqtech.master.utils.JsonUtils;
import com.miqtech.master.utils.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/user")
public class UserController extends BaseController {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
	@Autowired
	private UserInfoService userInfoService;

	/**
	 * 分页列表
	 */
	@RequestMapping("/list/{page}")
	public ModelAndView users(@PathVariable("page") int page, String username, String valid, String beginDate,
			String endDate) {
		ModelAndView mv = new ModelAndView("user/list");

		Map<String, Object> params = new HashMap<String, Object>(16);
		if (StringUtils.isNotBlank(username)) {
			params.put("username", username);
		}
		if (StringUtils.isNotBlank(valid)) {
			params.put("valid", valid);
		}
		if (StringUtils.isNotBlank(beginDate)) {
			params.put("beginDate", beginDate);
		}
		if (StringUtils.isNotBlank(endDate)) {
			params.put("endDate", endDate);
		}

		List<Map<String, Object>> list = userInfoService.pageList(page, params);
		pageModels(mv, list, page, UserTask.currentUserCount);
		mv.addObject("params", params);
		return mv;
	}

	/**
	 * 请求详细信息
	 */
	@ResponseBody
	@RequestMapping("/detail/{userId}")
	public JsonResponseMsg detail(@PathVariable("userId") long userId) {
		JsonResponseMsg result = new JsonResponseMsg();
		UserInfo user = userInfoService.findById(userId);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, user);
		return result;
	}

	/**
	 * 新增或更新
	 */
	@ResponseBody
	@RequestMapping("/save")
	public JsonResponseMsg save(String id, String nickname, String password, String telephone, String score,
			String realName, String idCard, String qq, String sex) {
		JsonResponseMsg result = new JsonResponseMsg();

		if (StringUtils.isAllBlank(nickname, password, telephone, score)
				|| (StringUtils.isNotBlank(id) && !NumberUtils.isNumber(id))
				|| (StringUtils.isNotBlank(score) && !NumberUtils.isNumber(score))) {
			result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
			return result;
		}

		UserInfo user = null;
		Date today = new Date();
		if (StringUtils.isNotBlank(id)) {
			user = userInfoService.findById(NumberUtils.toLong(id));
			if (user == null) {
				result.fill(CommonConstant.CODE_ERROR_LOGIC, "不存在用户");
				return result;
			}
		} else {
			user = new UserInfo();
			user.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			user.setCreateDate(today);
		}
		user.setUpdateDate(today);

		if (StringUtils.isNotBlank(nickname)) {
			user.setNickname(nickname);
		}
		if (StringUtils.isNotBlank(password)) {
			user.setEncryptPassword(password);
		}
		if (StringUtils.isNotBlank(telephone)) {
			user.setTelephone(telephone);
		}
		if (StringUtils.isNotBlank(score)) {
			user.setScore(NumberUtils.toInt(score));
		}
		if (StringUtils.isNotBlank(realName)) {
			user.setRealName(realName);
		}
		if (StringUtils.isNotBlank(idCard)) {
			user.setIdCard(idCard);
		}
		if (StringUtils.isNotBlank(qq)) {
			user.setQq(qq);
		}
		if (NumberUtils.isNumber(sex)) {
			user.setSex(NumberUtils.toInt(sex, 0));
		}

		userInfoService.saveOrUpdate(user);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("/delete/{userId}")
	public JsonResponseMsg delete(@PathVariable("userId") long userId) {
		JsonResponseMsg result = new JsonResponseMsg();
		userInfoService.delete(userId);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 激活用户
	 */
	@ResponseBody
	@RequestMapping("/enabled/{userId}")
	public JsonResponseMsg enabled(@PathVariable("userId") long userId) {
		JsonResponseMsg result = new JsonResponseMsg();
		userInfoService.enabledUser(userId);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 新增注册用户
	 */
	@ResponseBody
	@RequestMapping("/register")
	public JsonResponseMsg recover(String mobile, String password) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (!regExpTelephone(mobile)) {
			result.fill(CommonConstant.CODE_ERROR_PARAM, "手机号格式不正确");
			return result;
		}
		if (StringUtils.isBlank(password)) {
			password = "123456";
		}
		//调API注册接口
		String urlReturn = sendGet("http://api.wangyuhudong.com/register?mobile=" + mobile + "&password=" + password);
		result = JsonUtils.stringToObject(urlReturn, JsonResponseMsg.class);
		if (result.getCode() != 0) {
			return result;
		}
		//给用户发送注册信息短信
		String[] phoneNum = { mobile };
		String[] params = { mobile, password };
		SMSMessageUtil.sendTemplateMessage(phoneNum, "8036", params);

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * HTTP调注册接口
	 */
	public static String sendGet(String url) {
		String result = StringUtils.EMPTY;
		BufferedReader in = null;
		try {
			URL realUrl = new URL(url);
			// 打开与URL的连接
			URLConnection connection = realUrl.openConnection();
			// 设置通用请求属性
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 建立实际连接
			connection.connect();
			// 定义 BufferedReader输入流读取URL的响应
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			LOGGER.error("调注册接口异常", e);
		}
		// 关闭输入流
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				LOGGER.error("关闭注册接口输入流异常", e2);
			}
		}
		return result;
	}

	/**
	 * 手机号格式
	 */
	protected static boolean regExpTelephone(String telephone) {
		String regex = "^(13[0-9]|15[0-9]|17[0-9]|18[0-9]|14[0-9])[0-9]{8}$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(telephone);
		return matcher.find(); //boolean
	}

	//	/**
	//	 * 导出用户信息
	//	 */
	//	@RequestMapping("/exportExcel")
	//	public ModelAndView exportUsersInfo(HttpServletResponse res, String page, String beginDate, String endDate,
	//			String username, String valid) {
	//		// 查询报名数据
	//		int pageInt = NumberUtils.toInt(page, 1);
	//		Map<String, Object> params = Maps.newHashMap();
	//		if (StringUtils.isNotBlank(username)) {
	//			params.put("username", username);
	//		}
	//		if (StringUtils.isNotBlank(valid)) {
	//			params.put("valid", valid);
	//		}
	//		if (StringUtils.isNotBlank(beginDate)) {
	//			params.put("beginDate", beginDate);
	//		}
	//		if (StringUtils.isNotBlank(endDate)) {
	//			params.put("endDate", endDate);
	//		}
	//		List<Map<String, Object>> list = userInfoService.pageList(pageInt, params);
	//
	//		// Excel标题
	//		String title = StringUtils.EMPTY;
	//		if (StringUtils.isNotBlank(page)) {
	//			title = "_第" + page + "页_注册用户信息";
	//		} else {
	//			title = "_全部_注册用户信息";
	//		}
	//
	//		// 编辑excel内容
	//		String[][] contents = new String[list.size() + 1][];
	//
	//		// 设置标题行
	//		String[] contentTitle = new String[5];
	//		contentTitle[0] = "用户名";
	//		contentTitle[1] = "昵称";
	//		contentTitle[2] = "金币数";
	//		contentTitle[3] = "登录次数";
	//		contentTitle[4] = "注册时间";
	//		contents[0] = contentTitle;
	//
	//		// 设置内容
	//		if (CollectionUtils.isNotEmpty(list)) {
	//			for (int i = 0; i < list.size(); i++) {
	//				Map<String, Object> map = list.get(i);
	//				String[] row = new String[5];
	//				row[0] = null == map.get("username") ? "" : map.get("username").toString();
	//				row[1] = null == map.get("nickname") ? "" : map.get("nickname").toString();
	//				row[2] = null == map.get("coin") ? "" : map.get("coin").toString();
	//				row[3] = null == map.get("loginCount") ? "" : map.get("loginCount").toString();
	//				row[4] = null == map.get("createDate") ? ""
	//						: DateUtils.dateToString((Date) map.get("createDate"), DateUtils.YYYY_MM_DD_HH_MM_SS);
	//				contents[i + 1] = row;
	//			}
	//		}
	//		// 导出Excel
	//		try {
	//			ExcelUtils.exportExcel(title, contents, res);
	//		} catch (Exception e) {
	//			LOGGER.error("导出数据异常：", e);
	//		}
	//		return null;
	//	}

}
