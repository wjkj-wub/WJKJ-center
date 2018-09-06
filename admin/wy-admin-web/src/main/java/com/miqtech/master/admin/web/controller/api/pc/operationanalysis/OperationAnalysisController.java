package com.miqtech.master.admin.web.controller.api.pc.operationanalysis;


import com.miqtech.master.admin.web.annotation.LoginValid;
import com.miqtech.master.admin.web.controller.api.BaseController;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.service.pc.operationanalysis.DataStatisticsAnalysisService;
import com.miqtech.master.service.pc.operationanalysis.UserStatisticsAnalysisService;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 经营分析系统 数据统计
 *
 * @author zhangyuqi
 * @create 2017年09月21日
 */
@Controller
@RequestMapping("/api/operationAnalysis")
public class OperationAnalysisController extends BaseController {

	@Autowired
	private DataStatisticsAnalysisService dataStatisticsAnalysisService;
	@Autowired
	private UserStatisticsAnalysisService userStatisticsAnalysisService;

	/**
	 * 获取数据统计信息
	 */
	@RequestMapping("/data")
	@LoginValid(valid = true)
	@ResponseBody
	public JsonResponseMsg getDataStatistics() {
		JsonResponseMsg result = new JsonResponseMsg();
		Map<String, Object> map = dataStatisticsAnalysisService.getDataStatistics();
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, map);
	}

	/**
	 * 获取分时段统计信息
	 */
	@RequestMapping("/period")
	@LoginValid(valid = true)
	@ResponseBody
	public JsonResponseMsg getPeriodPDataStatistics(Long beginTimeStamp, Long endTimeStamp, Integer type) {
		JsonResponseMsg result = new JsonResponseMsg();
		Date beginDate;
		Date endDate;
		try {
			beginDate = beginTimeStamp == null || beginTimeStamp >= DateUtils.getToday().getTime()
					? DateUtils.getToday()
					: DateUtils.stampToDate(beginTimeStamp, DateUtils.YYYY_MM_DD);
			endDate = endTimeStamp == null || endTimeStamp >= DateUtils.getToday().getTime() ? DateUtils.getTomorrow()
					: DateUtils.stampToDate(endTimeStamp, DateUtils.YYYY_MM_DD);
		} catch (Exception e) {
			beginDate = DateUtils.getToday();
			endDate = DateUtils.getTomorrow();
		}

		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		c1.setTime(beginDate);
		c2.setTime(endDate);
		boolean isGroupHour = false;
		if ((c2.get(Calendar.DAY_OF_YEAR) - c1.get(Calendar.DAY_OF_YEAR)) <= 1) {
			isGroupHour = true;
		}
		Map<String, Object> map = dataStatisticsAnalysisService.getPeriodPDataStatistics(beginDate, endDate,
				isGroupHour, type);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, map);
	}

	/**
	 * 获取用户注册和登录统计
	 * @param type 统计类型：1-注册，2-登录
	 */
	@RequestMapping("/user")
	@LoginValid(valid = true)
	@ResponseBody
	public JsonResponseMsg getUserRegisterOrLoginStatistics(Integer page, Integer pageSize, Integer type) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (type == null) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}

		Map<String, Object> map = userStatisticsAnalysisService.getUserRegisterOrLoginStatistics(page, pageSize, type);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, map);
	}

	/**
	 * 获取用户参与赛事信息统计
	 */
	@RequestMapping("/confront")
	@LoginValid(valid = true)
	@ResponseBody
	public JsonResponseMsg getConfrontStatistics(Integer page, Integer pageSize) {
		JsonResponseMsg result = new JsonResponseMsg();
		Map<String, Object> map = userStatisticsAnalysisService.getConfrontStatistics(page, pageSize);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, map);
	}

	/**
	 * 获取用户平均等待时长
	 */
	@RequestMapping("/waitTime")
	@LoginValid(valid = true)
	@ResponseBody
	public JsonResponseMsg getUserAverageWaitTime() {
		JsonResponseMsg result = new JsonResponseMsg();
		List<Map<String, Object>> list = userStatisticsAnalysisService.getUserAverageWaitTime();
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, list);
	}

	/**
	 * 获取网吧在线终端统计
	 */
	@RequestMapping("/terminal")
	@LoginValid(valid = true)
	@ResponseBody
	public JsonResponseMsg getNetBarOnlineTerminal(Integer page, Integer pageSize, String keyword) {
		JsonResponseMsg result = new JsonResponseMsg();
		page = PageUtils.getPage(page);
		Map<String, Object> map = userStatisticsAnalysisService.getNetBarOnlineTerminal(page, pageSize, keyword);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, map);
	}

	/**
	 * 获取网吧用户统计排名
	 */
	@RequestMapping("/netBar/rank")
	@LoginValid(valid = true)
	@ResponseBody
	public JsonResponseMsg getNetBarRankStatistics(Integer page, Integer pageSize, Integer type, String keyword) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (type == null) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}

		Map<String, Object> map = userStatisticsAnalysisService.getNetBarRankStatistics(page, pageSize, type, keyword);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, map);
	}

	/**
	 * 获取网吧注册或登录用户统计
	 */
	@RequestMapping("/netBar/user")
	@LoginValid(valid = true)
	@ResponseBody
	public JsonResponseMsg getNetBarUserStatistics(Integer page, Integer pageSize, Long netBarId, Integer type) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (type == null || netBarId == null) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}

		Map<String, Object> map = userStatisticsAnalysisService.getNetBarUserStatistics(page, pageSize, netBarId, type);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, map);
	}
}
