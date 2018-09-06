package com.miqtech.master.admin.web.controller.backend.amuse;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.UserConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.common.SystemUser;
import com.miqtech.master.entity.user.UserBlack;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.service.amuse.AmuseActivityRecordService;
import com.miqtech.master.service.user.UserBlackService;
import com.miqtech.master.service.user.UserInfoService;
import com.miqtech.master.utils.ExcelUtils;
import com.miqtech.master.vo.PageVO;

/**
 * 娱乐赛黑名单管理
 */
@Controller
@RequestMapping("amuse/blackList")
public class AmuseBlackListController extends BaseController {

	@Autowired
	private UserBlackService userBlackService;
	@Autowired
	private AmuseActivityRecordService amuseActivityRecordService;
	@Autowired
	private UserInfoService userInfoService;

	/**
	 * 分页列表
	 */
	@RequestMapping("list/{page}")
	public ModelAndView list(@PathVariable("page") int page, String account, String username, String beginDate,
			String endDate) {
		ModelAndView mv = new ModelAndView("amuse/blacklistList");

		Map<String, String> params = Maps.newHashMap();
		params.put("account", account);
		params.put("username", username);
		params.put("beginDate", beginDate);
		params.put("endDate", endDate);
		mv.addObject("params", params);

		PageVO vo = userBlackService.adminPage(page, params);
		pageModels(mv, vo.getList(), page, vo.getTotal());

		return mv;
	}

	/**
	 * 查看用户奖品明细
	 */
	@RequestMapping("prize/list/{page}")
	public ModelAndView prizeList(@PathVariable("page") int page, String userId) {
		ModelAndView mv = new ModelAndView("amuse/blacklistPrizeList");

		Long userIdLong = NumberUtils.toLong(userId, 0);
		PageVO vo = amuseActivityRecordService.userPrizeList(page, userIdLong);
		pageModels(mv, vo.getList(), page, vo.getTotal());

		UserInfo user = userInfoService.findById(userIdLong);
		mv.addObject("userinfo", user);

		Number userPrizeSum = amuseActivityRecordService.userPrizeSum(userIdLong);
		mv.addObject("userPrizeSum", userPrizeSum);

		return mv;
	}

	/**
	 * 批量恢复用户
	 */
	@ResponseBody
	@RequestMapping("recover")
	public JsonResponseMsg recover(String[] ids) {
		JsonResponseMsg result = new JsonResponseMsg();

		if (ArrayUtils.isNotEmpty(ids)) {
			List<Long> ubIds = Lists.newArrayList();
			for (String id : ids) {
				if (NumberUtils.isNumber(id)) {
					ubIds.add(NumberUtils.toLong(id));
				}
			}
			userBlackService.adminRecover(ubIds);
		}

		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 * 批量删除用户黑名单
	 */
	@ResponseBody
	@RequestMapping("delete")
	public JsonResponseMsg delete(HttpServletRequest req, String[] ids) {
		JsonResponseMsg result = new JsonResponseMsg();

		if (ArrayUtils.isNotEmpty(ids)) {
			List<Long> ubIds = Lists.newArrayList();
			for (String id : ids) {
				if (NumberUtils.isNumber(id)) {
					ubIds.add(NumberUtils.toLong(id));
				}
			}
			SystemUser sysUser = Servlets.getSessionUser(req);
			Long sysUserId = sysUser.getId();
			userBlackService.adminRecover(ubIds, true, sysUserId);
		}

		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 * 导入黑名单
	 */
	@ResponseBody
	@RequestMapping("import")
	public JsonResponseMsg inportByStr(String usernames) {
		JsonResponseMsg result = new JsonResponseMsg();

		if (StringUtils.isNotBlank(usernames)) {
			usernames = usernames.replaceAll("\\s", "").replaceAll("，", ",");
		}

		// 根据传入用户名,匹配出要拉黑的用户ID
		String[] usernameSplit = usernames.split(",");
		List<Long> userIds = Lists.newArrayList();
		List<UserBlack> userBlacks = Lists.newArrayList();
		if (ArrayUtils.isNotEmpty(usernameSplit)) {
			List<UserInfo> users = userInfoService.findByValidAndUserNameIn(CommonConstant.INT_BOOLEAN_TRUE,
					usernameSplit);
			if (CollectionUtils.isNotEmpty(users)) {
				for (UserInfo u : users) {
					Long id = u.getId();
					userIds.add(id);

					// 记录拉黑用户记录
					UserBlack userBlack = new UserBlack();
					userBlack.setUserId(id);
					userBlack.setChannel(UserConstant.BLACK_CHANNEL_AMUSE);
					userBlack.setValid(CommonConstant.INT_BOOLEAN_TRUE);
					Date now = new Date();
					userBlack.setCreateDate(now);
					userBlacks.add(userBlack);
				}
			}
		}

		userInfoService.disabledAndOffline(userIds);
		userBlackService.save(userBlacks);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, userIds.size());
	}

	/**
	 * 导入黑名单（excel）
	 */
	@ResponseBody
	@RequestMapping("importExcel")
	public JsonResponseMsg importByExcel(HttpServletRequest req) {
		JsonResponseMsg result = new JsonResponseMsg();

		// 获取excel
		MultipartFile file = Servlets.getMultipartFile(req, "file");
		Workbook wb = ExcelUtils.readMultipartFile(file);
		if (wb == null) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}
		Sheet[] sheets = wb.getSheets();
		if (ArrayUtils.isEmpty(sheets) || sheets.length <= 0) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}

		// 读取工作表及行
		List<String> usernames = Lists.newArrayList();
		Sheet sheet = wb.getSheet(0);
		int rows = sheet.getRows();
		if (rows > 1) {
			for (int r = 1; r < rows; r++) {
				Cell[] cells = sheet.getRow(r);
				if (ArrayUtils.isNotEmpty(cells) && cells.length >= 1) {
					String username = cells[0].getContents();
					if (StringUtils.isNotBlank(username)) {
						usernames.add(username);
					}
				}
			}
		}

		// 根据传入用户名,匹配出要拉黑的用户ID
		List<Long> userIds = Lists.newArrayList();
		List<UserBlack> userBlacks = Lists.newArrayList();
		if (CollectionUtils.isNotEmpty(usernames)) {
			List<UserInfo> users = userInfoService.findByValidAndUserNameIn(CommonConstant.INT_BOOLEAN_TRUE,
					(String[]) usernames.toArray(new String[usernames.size()]));
			if (CollectionUtils.isNotEmpty(users)) {
				for (UserInfo u : users) {
					Long id = u.getId();
					userIds.add(id);

					// 记录拉黑用户记录
					UserBlack userBlack = new UserBlack();
					userBlack.setUserId(id);
					userBlack.setChannel(UserConstant.BLACK_CHANNEL_AMUSE);
					userBlack.setValid(CommonConstant.INT_BOOLEAN_TRUE);
					Date now = new Date();
					userBlack.setCreateDate(now);
					userBlacks.add(userBlack);
				}
			}
		}

		userInfoService.disabledAndOffline(userIds);
		userBlackService.save(userBlacks);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, userIds.size());
	}
}
