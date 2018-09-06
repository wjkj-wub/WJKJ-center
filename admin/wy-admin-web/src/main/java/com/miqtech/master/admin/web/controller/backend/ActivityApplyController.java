package com.miqtech.master.admin.web.controller.backend;

import com.google.common.collect.Lists;
import com.miqtech.master.consts.ActivityConstant;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.activity.*;
import com.miqtech.master.entity.netbar.NetbarInfo;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.service.activity.*;
import com.miqtech.master.service.netbar.NetbarInfoService;
import com.miqtech.master.service.user.UserInfoService;
import com.miqtech.master.utils.ArithUtil;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.ExcelUtils;
import com.miqtech.master.vo.PageVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/activityInfo/apply")
public class ActivityApplyController extends BaseController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ActivityApplyController.class);

	@Autowired
	private UserInfoService userInfoService;
	@Autowired
	private ActivityMemberService activityMemberService;
	@Autowired
	private ActivityTeamService activityTeamService;
	@Autowired
	private ActivityInfoService activityInfoService;
	@Autowired
	private ActivityRoundService activityRoundService;
	@Autowired
	private NetbarInfoService netbarInfoService;
	@Autowired
	private ActivityMatchApplyService activityMatchApplyService;
	@Autowired
	private ActivityMatchService activityMatchService;
	@Autowired
	private ActivityGroupService activityGroupService;
	@Autowired
	private ActivityApplyService activityApplyService;

	/**
	 * 个人报名列表
	 */
	@RequestMapping("personal/{page}")
	public ModelAndView personal(@PathVariable("page") String page, String activityId, String startDate, String endDate,
			String netbarId, String inRecord, String round, String name, String telephone) {
		ModelAndView mv = new ModelAndView("/activity/activityPersonalMembers");

		Map<String, Object> params = new HashMap<String, Object>(16);
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		params.put("round", round);
		params.put("netbar_id", netbarId);
		params.put("in_record", inRecord);
		params.put("activity_id", activityId);
		params.put("name", name);
		params.put("telephone", telephone);

		int pageInt = NumberUtils.toInt(page, 1);
		PageVO vo = activityMemberService.personalMembers(pageInt, params);
		pageModels(mv, vo.getList(), pageInt, vo.getTotal());
		mv.addObject("params", params);

		Long activityIdLong = NumberUtils.toLong(activityId, 0);
		ActivityInfo activityInfo = activityInfoService.findById(activityIdLong);
		mv.addObject("activity", activityInfo);
		ActivityRound activityRound = activityRoundService.findValidByActivityIdAndRound(activityIdLong,
				NumberUtils.toInt(round, 1));
		mv.addObject("round", activityRound);
		NetbarInfo netbar = netbarInfoService.findById(NumberUtils.toLong(netbarId, 0));
		mv.addObject("netbar", netbar);

		return mv;
	}

	/**
	 * 导出个人报名记录
	 */
	@RequestMapping("personal/export/{page}")
	public ModelAndView exportPersonal(HttpServletResponse res, @PathVariable("page") String page, String actId,
			String startDate, String endDate, String netbarId, String inRecord, String round, String name,
			String telephone) {
		// 构建参数
		Map<String, Object> params = new HashMap<String, Object>(16);
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		params.put("round", round);
		params.put("netbar_id", netbarId);
		params.put("in_record", inRecord);
		params.put("activity_id", actId);
		params.put("name", name);
		params.put("telephone", telephone);

		// 查询数据
		int pageInt = NumberUtils.toInt(page, 1);
		PageVO vo = activityMemberService.personalMembers(pageInt, params);
		List<Map<String, Object>> list = vo.getList();

		// 查询excel标题
		Long activityId = NumberUtils.toLong(actId, 0);
		ActivityInfo activityInfo = activityInfoService.findById(activityId);
		ActivityRound activityRound = activityRoundService.findValidByActivityIdAndRound(activityId,
				NumberUtils.toInt(round, 1));
		NetbarInfo netbar = netbarInfoService.findById(NumberUtils.toLong(netbarId, 0));
		String title = null;
		if (activityInfo != null) {
			title = activityInfo.getTitle() + "赛事";
			if (activityRound != null && activityRound.getOverTime() != null) {
				title += " " + DateUtils.dateToString(activityRound.getOverTime(), "yyyy.MM.dd");
			}
			if (netbar != null) {
				title += " " + netbar.getName();
			}
		} else {
			title = "个人报名记录";
		}

		List<String[][]> sheets = Lists.newArrayList();
		List<String> sheetTitles = Lists.newArrayList();
		boolean hasRank = false;

		// 编辑excel内容
		String[][] sheet1 = new String[list.size() + 1][];
		// 设置标题行
		String[] contentTitle = new String[4];
		contentTitle[0] = "选手名称";
		contentTitle[1] = "手机号码";
		contentTitle[2] = "QQ";
		contentTitle[3] = "身份证号码";
		sheet1[0] = contentTitle;
		// 设置内容
		for (int i = 0; i < list.size(); i++) {
			Map<String, Object> member = list.get(i);
			String[] row = new String[4];
			row[0] = MapUtils.getString(member, "name");
			row[1] = MapUtils.getString(member, "telephone");
			row[2] = MapUtils.getString(member, "qq");
			row[3] = MapUtils.getString(member, "idCard");
			sheet1[i + 1] = row;

			// 检查是否有名次信息
			if (!hasRank) {
				Integer rank = MapUtils.getInteger(member, "rank");
				if (rank != null) {
					hasRank = true;
				}
			}
		}
		sheets.add(sheet1);
		sheetTitles.add("个人报名");

		if (hasRank) {
			String[][] rankSheet = new String[list.size() + 1][];
			// 设置标题行
			String[] rankContentTitle = new String[5];
			rankContentTitle[0] = "名次";
			rankContentTitle[1] = "选手名称";
			rankContentTitle[2] = "手机号码";
			rankContentTitle[3] = "QQ";
			rankContentTitle[4] = "身份证号码";
			rankSheet[0] = rankContentTitle;
			// 设置内容
			for (int i = 0; i < list.size(); i++) {
				Map<String, Object> member = list.get(i);
				String[] row = new String[5];
				row[0] = MapUtils.getString(member, "rank");
				row[1] = MapUtils.getString(member, "name");
				row[2] = MapUtils.getString(member, "telephone");
				row[3] = MapUtils.getString(member, "qq");
				row[4] = MapUtils.getString(member, "idCard");
				rankSheet[i + 1] = row;
			}
			sheets.add(rankSheet);
			sheetTitles.add("个人报名(含名次)");
		}

		try {
			ExcelUtils.exportMultipleSheetExcel(title, sheets, sheetTitles, false, res);
		} catch (Exception e) {
			LOGGER.error("导出数据异常：", e);
		}
		return null;
	}

	/**
	 * 导出约战报名信息
	 */
	@RequestMapping("/exportExcel")
	public ModelAndView exportPersonal(HttpServletResponse res, String matchId) {
		// 查询报名数据
		long matchIdLong = NumberUtils.toLong(matchId);
		List<Map<String, Object>> list = activityMatchApplyService.queryApplyInfoByMatchId(matchIdLong);

		// Excel标题
		ActivityMatch matchInfo = activityMatchService.findById(matchIdLong);
		String title = StringUtils.EMPTY;
		if (null != matchInfo) {
			title = "_" + matchInfo.getTitle() + "_报名记录";
		} else {
			title = "_报名记录";
		}

		// 编辑excel内容
		String[][] contents = new String[list.size() + 1][];

		// 设置标题行
		String[] contentTitle = new String[7];
		contentTitle[0] = "用户昵称";
		contentTitle[1] = "真实姓名";
		contentTitle[2] = "性别";
		contentTitle[3] = "手机号";
		contentTitle[4] = "QQ";
		contentTitle[5] = "城市";
		contentTitle[6] = "报名时间";
		contents[0] = contentTitle;

		// 设置内容
		if (CollectionUtils.isNotEmpty(list)) {
			for (int i = 0; i < list.size(); i++) {
				Map<String, Object> map = list.get(i);
				String[] row = new String[7];
				row[0] = null == map.get("nickname") ? "" : map.get("nickname").toString();
				row[1] = null == map.get("realname") ? "" : map.get("realname").toString();
				row[2] = null == map.get("sex") ? "" : map.get("sex").toString();
				row[3] = null == map.get("telephone") ? "" : map.get("telephone").toString();
				row[4] = null == map.get("qq") ? "" : map.get("qq").toString();
				row[5] = null == map.get("city_name") ? "" : map.get("city_name").toString();
				row[6] = null == map.get("create_date") ? ""
						: DateUtils.dateToString((Date) map.get("create_date"), DateUtils.YYYY_MM_DD_HH_MM_SS);
				contents[i + 1] = row;
			}
		}
		// 导出Excel
		try {
			ExcelUtils.exportExcel(title, contents, false, res);
		} catch (Exception e) {
			LOGGER.error("导出数据异常：", e);
		}
		return null;
	}

	/**
	 * 查询个人报名详情
	 */
	@ResponseBody
	@RequestMapping("personal/detail/{id}")
	public JsonResponseMsg personalDetail(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();

		Map<String, Object> member = activityMemberService.getMemberDetailById(id);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, member);

		return result;
	}

	/**
	 * 保存个人报名
	 */
	@ResponseBody
	@RequestMapping("personal/save")
	public JsonResponseMsg personalSave(ActivityMember member, String netbarId) {
		JsonResponseMsg result = new JsonResponseMsg();

		if (member.getId() != null) {
			ActivityMember old = activityMemberService.findById(member.getId());
			BeanUtils.updateBean(old, member);
			activityMemberService.savePersonal(old, netbarId);
		}

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 删除个人报名记录
	 */
	@ResponseBody
	@RequestMapping("personal/delete/{id}")
	public JsonResponseMsg personalDelete(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		ActivityApply apply = activityApplyService.findValidByTargetIdAndType(id, ActivityConstant.APPLY_TYPE_PERSON);
		if (apply != null) {
			activityGroupService.resetGroups(apply.getActivityId(), apply.getRound(), apply.getNetbarId(), false);
		}
		activityMemberService.deletePersonMember(id);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 战队列表
	 */
	@RequestMapping("team/{page}")
	public ModelAndView team(@PathVariable("page") String page, String activityId, String startDate, String endDate,
			String netbarId, String inRecord, String round, String teamId, String name, String telephone) {
		ModelAndView mv = new ModelAndView("/activity/activityTeamMembers");

		Map<String, Object> params = new HashMap<String, Object>(16);
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		params.put("round", round);
		params.put("netbar_id", netbarId);
		params.put("in_record", inRecord);
		params.put("activity_id", activityId);
		params.put("team_id", teamId);
		params.put("name", name);
		params.put("telephone", telephone);

		int pageInt = NumberUtils.toInt(page, 1);
		PageVO vo = activityTeamService.teamMembers(pageInt, params);
		pageModels(mv, vo.getList(), pageInt, vo.getTotal());
		mv.addObject("params", params);
		List<ActivityTeam> teams = null;
		if (NumberUtils.isNumber(activityId) && NumberUtils.isNumber(netbarId)) {
			teams = activityTeamService.findValidByActivityIdAndNetbarIdAndRound(NumberUtils.toLong(activityId),
					NumberUtils.toLong(netbarId), NumberUtils.toInt(round, 1));
		} else {
			teams = activityTeamService.getTeamsByActivityId(NumberUtils.toLong(activityId, 0));
		}
		mv.addObject("teams", teams);

		return mv;
	}

	/**
	 * 战队个人资料
	 */
	@ResponseBody
	@RequestMapping("team/detail/{id}")
	public JsonResponseMsg teamDetail(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();

		Map<String, Object> member = activityMemberService.getTeamDetailById(id);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, member);

		return result;
	}

	/**
	 * 保存战队报名
	 */
	@ResponseBody
	@RequestMapping("team/save")
	public JsonResponseMsg teamSave(ActivityMember member, String netbarId) {
		JsonResponseMsg result = new JsonResponseMsg();

		if (member.getId() != null) {
			ActivityMember old = activityMemberService.findById(member.getId());
			BeanUtils.updateBean(old, member);
			activityMemberService.saveTeam(old, netbarId);
		}

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 导出战队报名记录
	 */
	@RequestMapping("team/export/{page}")
	public ModelAndView exportTeam(HttpServletResponse res, @PathVariable("page") String page, String actId,
			String startDate, String endDate, String netbarId, String inRecord, String round, String teamId,
			String name, String telephone) {
		// 构建参数
		Map<String, Object> params = new HashMap<String, Object>(16);
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		params.put("round", round);
		params.put("netbar_id", netbarId);
		params.put("in_record", inRecord);
		params.put("activity_id", actId);
		params.put("team_id", teamId);
		params.put("name", name);
		params.put("telephone", telephone);

		// 查询数据
		int pageInt = NumberUtils.toInt(page, 1);
		PageVO vo = activityTeamService.teamMembers(pageInt, params);
		List<Map<String, Object>> list = vo.getList();

		// 查询excel标题
		Long activityId = NumberUtils.toLong(actId, 0);
		ActivityInfo activityInfo = activityInfoService.findById(activityId);
		ActivityRound activityRound = activityRoundService.findValidByActivityIdAndRound(activityId,
				NumberUtils.toInt(round, 1));
		NetbarInfo netbar = netbarInfoService.findById(NumberUtils.toLong(netbarId, 0));
		String title = null;
		if (activityInfo != null) {
			title = activityInfo.getTitle() + "赛事";
			if (activityRound != null && activityRound.getOverTime() != null) {
				title += " " + DateUtils.dateToString(activityRound.getOverTime(), "yyyy.MM.dd");
			}
			if (netbar != null) {
				title += " " + netbar.getName();
			}
		} else {
			title = "个人报名记录";
		}

		List<String[][]> sheets = Lists.newArrayList();
		List<String> sheetTitles = Lists.newArrayList();
		boolean hasRank = false;

		// 编辑excel内容
		String[][] sheet1 = new String[list.size() + 1][];

		// 设置标题行
		String[] contentTitle = new String[5];
		contentTitle[0] = "战队名称";
		contentTitle[1] = "选手名称";
		contentTitle[2] = "手机号码";
		contentTitle[3] = "QQ";
		contentTitle[4] = "身份证号码";
		sheet1[0] = contentTitle;

		// 设置内容
		for (int i = 0; i < list.size(); i++) {
			Map<String, Object> member = list.get(i);
			String[] row = new String[5];
			row[0] = MapUtils.getString(member, "teamName");
			row[1] = MapUtils.getString(member, "name");
			row[2] = MapUtils.getString(member, "telephone");
			row[3] = MapUtils.getString(member, "qq");
			row[4] = MapUtils.getString(member, "idCard");
			sheet1[i + 1] = row;

			// 检查是否有名次信息
			if (!hasRank) {
				Integer rank = MapUtils.getInteger(member, "rank");
				if (rank != null) {
					hasRank = true;
				}
			}
		}
		sheets.add(sheet1);
		sheetTitles.add("战队报名");

		if (hasRank) {
			String[][] rankSheet = new String[list.size() + 1][];
			// 设置标题行
			String[] rankContentTitle = new String[6];
			rankContentTitle[0] = "名次";
			rankContentTitle[1] = "战队名称";
			rankContentTitle[2] = "选手名称";
			rankContentTitle[3] = "手机号码";
			rankContentTitle[4] = "QQ";
			rankContentTitle[5] = "身份证号码";
			rankSheet[0] = rankContentTitle;
			// 设置内容
			for (int i = 0; i < list.size(); i++) {
				Map<String, Object> member = list.get(i);
				String[] row = new String[6];
				row[0] = MapUtils.getString(member, "rank");
				row[1] = MapUtils.getString(member, "teamName");
				row[2] = MapUtils.getString(member, "name");
				row[3] = MapUtils.getString(member, "telephone");
				row[4] = MapUtils.getString(member, "qq");
				row[5] = MapUtils.getString(member, "idCard");
				rankSheet[i + 1] = row;
			}
			sheets.add(rankSheet);
			sheetTitles.add("战队报名(含名次)");
		}

		try {
			ExcelUtils.exportMultipleSheetExcel(title, sheets, sheetTitles, false, res);
		} catch (Exception e) {
			LOGGER.error("导出数据异常：", e);
		}
		return null;
	}

	/**
	 * 删除个人报名记录
	 */
	@ResponseBody
	@RequestMapping("team/delete/{id}")
	public JsonResponseMsg teamDelete(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		ActivityMember member = activityMemberService.findById(id);
		if (member != null) {
			ActivityApply apply = activityApplyService.findValidByTargetIdAndType(member.getTeamId(),
					ActivityConstant.APPLY_TYPE_TEAM);
			if (apply != null) {
				activityGroupService.resetGroups(apply.getActivityId(), apply.getRound(), apply.getNetbarId(), true);
			}
		}
		activityMemberService.deleteTeamMember(id);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 个人报名
	 */
	@RequestMapping("personal/apply")
	@ResponseBody
	public JsonResponseMsg personalApply(String activityId, String round, String netbarId, String name, String idCard,
			String telephone, String qq, String labor, String server) {
		JsonResponseMsg result = new JsonResponseMsg();

		// 检查参数是否正确
		if (!NumberUtils.isNumber(activityId) || !NumberUtils.isNumber(round) || !NumberUtils.isNumber(netbarId)
				|| com.miqtech.master.utils.StringUtils.isAllBlank(name, telephone)) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}

		// 查询用户信息
		UserInfo user = userInfoService.queryByName(telephone);
		if (user == null) {
			return result.fill(CommonConstant.CODE_ERROR_LOGIC, "不存在此号码注册的用户");
		}

		// 创建赛事成员
		Long activityIdLong = NumberUtils.toLong(activityId);
		Long netbarIdLong = NumberUtils.toLong(netbarId);
		Integer roundInt = NumberUtils.toInt(round);
		boolean applied = activityInfoService.submitPersonalApply(activityIdLong, user.getId(), netbarIdLong, name,
				telephone, idCard, qq, labor, roundInt);

		if (applied) {
			// 暂停1s以确保数据已同步
			try {
				Thread.sleep(1000);
			} catch (Exception expect) {
			}

			Map<String, Object> member = activityMemberService.queryValidByTelephoneAndActivityAndNetbarIdAndRound(
					telephone, activityIdLong, netbarIdLong, roundInt);
			if (MapUtils.isNotEmpty(member) && MapUtils.getLong(member, "memberId") != null) {
				// 查询最后一条分组信息
				Map<String, Object> group = activityGroupService.queryLastByActivityIdAndNetbarIdAndRound(
						activityIdLong, netbarIdLong, roundInt, CommonConstant.INT_BOOLEAN_FALSE);
				Integer groupSeatNumber = MapUtils.getInteger(group, "seatNumber");
				if (groupSeatNumber != null) {
					int seatNumber = groupSeatNumber + 1;
					// 产生分组记录
					ActivityGroup g = new ActivityGroup();
					g.setActivityId(activityIdLong.intValue());
					g.setNetbarId(netbarIdLong.intValue());
					g.setTargetId(MapUtils.getLong(member, "memberId").intValue());
					g.setRound(roundInt);
					g.setSeatNumber(seatNumber);
					int groupNumber = (int) Math.ceil(ArithUtil.div(seatNumber, 2));
					g.setGroupNumber(groupNumber);
					int seat = seatNumber % 2 == 0 ? 2 : 1;
					g.setSeat(seat);
					g.setIsTeam(CommonConstant.INT_BOOLEAN_FALSE);
					g.setValid(CommonConstant.INT_BOOLEAN_TRUE);
					Date now = new Date();
					g.setUpdateDate(now);
					g.setCreateDate(now);
					activityGroupService.save(g);
				}

			}

			return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		} else {
			return result.fill(CommonConstant.CODE_ERROR_LOGIC, "用户已经报名过此赛事");
		}
	}

	@ResponseBody
	@RequestMapping("team/apply")
	public JsonResponseMsg teamApply(String activityId, String round, String teamId, String name, String idCard,
			String telephone, String qq, String labor, String server, String netbarId, String teamName) {
		JsonResponseMsg result = new JsonResponseMsg();

		// 检查参数是否正确
		if (!NumberUtils.isNumber(activityId) || !NumberUtils.isNumber(round)
				|| com.miqtech.master.utils.StringUtils.isAllBlank(name, telephone)) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}

		// 查询用户信息
		UserInfo user = userInfoService.queryByName(telephone);
		if (user == null) {
			return result.fill(CommonConstant.CODE_ERROR_LOGIC, "不存在此号码注册的用户");
		}

		if (NumberUtils.isNumber(teamId)) {// 加入战队
			// 判断战队人数
			Long teamIdLong = NumberUtils.toLong(teamId);
			List<Map<String, Object>> members = activityMemberService.queryValidByTeamId(teamIdLong);
			if (CollectionUtils.isNotEmpty(members) && members.size() >= 5) {
				return result.fill(-1, "战队人数已满");
			}

			int status = activityMemberService.joinTeam(teamIdLong, user.getId(), name, telephone, idCard, qq, labor,
					null, null);
			if (status == 0) {
				result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
			} else if (status == -2) {
				result.fill(-3, "你只能加入一个该比赛下的战队");
			} else if (status == -1) {
				result.fill(-4, "团队信息已无效");
			} else if (status == -3) {
				result.fill(-5, "加入失败,战队人数已满");
			} else if (status == -4) {
				result.fill(-6, "无法加入战队,此次比赛已经截止报名.");
			} else if (status == -5) {
				result.fill(-7, "已申请加入.");
			}
		} else {// 创建战队
			if (NumberUtils.isNumber(netbarId) && NumberUtils.isNumber(round)) {
				Long activityIdLong = NumberUtils.toLong(activityId);
				Long netbarIdLong = NumberUtils.toLong(netbarId);
				Integer roundInt = NumberUtils.toInt(round);
				Map<String, Object> map = activityInfoService.createTeam(activityIdLong, user.getId(), netbarIdLong,
						teamName, name, server, telephone, idCard, qq, labor, roundInt);
				if (map.get("teamId") != null) {
					result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, map);
				} else {
					result.fill(10, "战队已报名,不能重复报名", map);
				}
			} else {
				result.fill(CommonConstant.CODE_ERROR_PARAM, "请选择网吧或场次");
			}
		}

		return result;
	}

	/**
	 * 选择场次和网吧
	 */
	@RequestMapping("choose")
	public ModelAndView choose(String activityId, String isTeam) {
		ModelAndView mv = new ModelAndView("activity/chooseRound");
		mv.addObject("isTeam", NumberUtils.toInt(isTeam, 0));
		mv.addObject("activityId", activityId);

		if (NumberUtils.isNumber(activityId)) {
			long activityIdLong = NumberUtils.toLong(activityId);
			List<Map<String, Object>> rounds = activityRoundService.findActivityRoundNetbarInfo(activityIdLong);
			mv.addObject("rounds", rounds);
		}

		return mv;
	}

	/**
	 * 签到
	 */
	@ResponseBody
	@RequestMapping("sign")
	public JsonResponseMsg sign(String isTeam, String id, String signed) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (!NumberUtils.isNumber(isTeam) || !NumberUtils.isNumber(id) || !NumberUtils.isNumber(signed)) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}

		int resultSigned = CommonConstant.INT_BOOLEAN_FALSE;
		int signedInt = NumberUtils.toInt(signed);
		if ("1".equals(isTeam)) {
			ActivityTeam team = activityTeamService.sign(NumberUtils.toLong(id), signedInt);
			ActivityMember monitor = activityMemberService.findByTeamIdAndValidAndIsMonitor(team.getId(),
					CommonConstant.INT_BOOLEAN_TRUE, CommonConstant.INT_BOOLEAN_TRUE);
			if (monitor != null) {
				monitor.setSigned(signedInt);
				activityMemberService.save(monitor);
			}
			if (team != null && CommonConstant.INT_BOOLEAN_TRUE.equals(team.getSigned())) {
				resultSigned = CommonConstant.INT_BOOLEAN_TRUE;
			}
		} else {
			ActivityMember member = activityMemberService.sign(NumberUtils.toLong(id), signedInt);
			if (member != null && CommonConstant.INT_BOOLEAN_TRUE.equals(member.getSigned())) {
				resultSigned = CommonConstant.INT_BOOLEAN_TRUE;
			}
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, resultSigned);
	}
}
