package com.miqtech.master.admin.web.controller.api.pc.taskmatch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.miqtech.master.admin.web.controller.api.BaseController;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.pc.taskmatch.TaskMatchRank;
import com.miqtech.master.service.pc.taskmatch.TaskMatchRankService;
import com.miqtech.master.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * 排行榜任务赛后台管理操作
 *
 * @author gaohanlin
 * @create 2017年09月01日
 */
@Controller
@RequestMapping("/api/taskMatch/rank")
public class TaskMatchRankController extends BaseController {

	@Autowired
	private TaskMatchRankService taskMatchRankService;

	/**
	 * 获取排位赛列表
	 * @param
	 */
	@RequestMapping("/list")
	@ResponseBody
	public JsonResponseMsg getMatchRankList(Integer page, Integer rows, String startDate, String endDate, String status,
			String keyword) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (page == null) {
			return result.fill(-2, "page不能为空");
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS,
				taskMatchRankService.matchRankList(page, rows, startDate, endDate, status, keyword));
	}

	/**
	 * 删除排位赛
	 * @param
	 */
	@RequestMapping("/del")
	@ResponseBody
	public JsonResponseMsg delMatchRank(Long taskId) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (taskId == null) {
			return result.fill(-2, "任务ID不能为空");
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS,
				taskMatchRankService.delMatchRank(taskId));
	}

	/**
	 * 获取赛事排行榜
	 * @param
	 */
	@RequestMapping("/rankingList")
	@ResponseBody
	public JsonResponseMsg rankingList(Integer page, Integer rows, String taskId) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (page == null) {
			return result.fill(-2, "page不能为空");
		}
		if (taskId == null) {
			return result.fill(-2, "任务赛ID不能为空");
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS,
				taskMatchRankService.rankingList(page, rows, taskId));
	}

	/**
	 * 新建或者编辑任务赛
	 */
	@RequestMapping("/addOrEdit")
	@ResponseBody
	public JsonResponseMsg addOrEdit(HttpServletRequest req, HttpServletResponse res) {

		JsonResponseMsg result = new JsonResponseMsg();

		String data = req.getParameter("data");
		JSONObject jsonObject = JSON.parseObject(data);

		TaskMatchRank taskMatchRank = new TaskMatchRank();
		taskMatchRank.setName(jsonObject.getString("name"));
		try {
			Date enterDate = DateUtils.stringToDate(
					DateUtils.stampToDate(jsonObject.getString("enterDate"), DateUtils.YYYY_MM_DD_HH_MM),
					DateUtils.YYYY_MM_DD_HH_MM);
			taskMatchRank.setEnterDate(enterDate);
			Date startDate = DateUtils.stringToDate(
					DateUtils.stampToDate(jsonObject.getString("startDate"), DateUtils.YYYY_MM_DD_HH_MM),
					DateUtils.YYYY_MM_DD_HH_MM);
			Date endDate = DateUtils.stringToDate(
					DateUtils.stampToDate(jsonObject.getString("endDate"), DateUtils.YYYY_MM_DD_HH_MM),
					DateUtils.YYYY_MM_DD_HH_MM);
			taskMatchRank.setEnterDate(enterDate);
			taskMatchRank.setStartDate(startDate);
			taskMatchRank.setEndDate(endDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		taskMatchRank.setLabels(jsonObject.getString("labels")); //标签串
		taskMatchRank.setTaskExplain(jsonObject.getString("taskExplain"));
		taskMatchRank.setConditionExplain(jsonObject.getString("conditionExplain"));
		taskMatchRank.setGameRule(jsonObject.getString("gameRule"));
		taskMatchRank.setAwardRule(jsonObject.getString("awardRule"));
		taskMatchRank.setImgUrl(jsonObject.getString("imgUrl"));

		taskMatchRank.setFeeType(Byte.valueOf(jsonObject.getString("feeType")));
		if (jsonObject.getString("feeAmount") != null && !jsonObject.getString("feeAmount").equals("")) {
			taskMatchRank.setFeeAmount(Integer.parseInt(jsonObject.getString("feeAmount")));
		}
		taskMatchRank.setType(Integer.parseInt(jsonObject.getString("type")));
		String limitHeroIds = jsonObject.getString("limitHeroIds"); //限定英雄ID串
		if (jsonObject.getString("limitTimes") != null) {
			taskMatchRank.setLimitTimes(Integer.parseInt(jsonObject.getString("limitTimes"))); //限定场次
		}
		taskMatchRank.setTotalAwardType(Byte.valueOf(jsonObject.getString("totalAwardType")));
		taskMatchRank.setTotalAward(Integer.parseInt(jsonObject.getString("totalAward")));
		String prizes = jsonObject.getString("prizes"); //奖品设置串  1,2,3,4,5;6,7,8,9,10;
		taskMatchRank.setIsValid(Byte.valueOf("1"));

		Integer operaType = Integer.valueOf(jsonObject.getString("operaType"));
		if (operaType == 1) {//新建活动
			return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS,
					taskMatchRankService.addRankTask(taskMatchRank, limitHeroIds, prizes));
		} else if (operaType == 2) { //编辑活动
			if (jsonObject.getString("taskId") != null) {
				taskMatchRank.setId(Long.valueOf(jsonObject.getString("taskId")));
				taskMatchRankService.editRankTask(taskMatchRank, limitHeroIds, prizes);
				return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);

			} else {
				return result.fill(-2, "参数不足");
			}

		}
		return null;

	}

	/**
	 * 获取排位赛信息
	 * @param
	 */
	@RequestMapping("/rankInfo")
	@ResponseBody
	public JsonResponseMsg getRankInfo(Long taskId) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (taskId == null) {
			return result.fill(-2, "taskId不能为空");
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS,
				taskMatchRankService.getMatchRank(taskId));
	}

	/**
	 * 编辑战绩
	 * @param
	 */
	@RequestMapping("/editMilitaryExploit")
	@ResponseBody
	public JsonResponseMsg editMilitaryExploit(Long userId, Long taskId, Integer playTimes, Integer cumulativeKill) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (userId == null) {
			return result.fill(-2, "userId不能为空");
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS,
				taskMatchRankService.editMilitaryExploit(userId, taskId, playTimes, cumulativeKill));
	}

	/**
	 * 发送消息
	 * @param
	 */
	@RequestMapping("/sendMsg")
	@ResponseBody
	public JsonResponseMsg sendMsg(Long userId, String msg) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (userId == null) {
			return result.fill(-2, "userId不能为空");
		}
		if (msg == null) {
			return result.fill(-2, "消息内容不能为空");
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS,
				taskMatchRankService.insertMsg(userId, msg));
	}

	/**
	 * 获取发奖信息
	 * @param
	 */
	@RequestMapping("/getAwardInfo")
	@ResponseBody
	public JsonResponseMsg getAwardInfo(Long userId) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (userId == null) {
			return result.fill(-2, "userId不能为空");
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS,
				taskMatchRankService.getAwardInfo(userId));
	}

	/**
	 * 审核
	 * @param
	 */
	@RequestMapping("/audit")
	@ResponseBody
	public JsonResponseMsg audit(Long taskId) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (taskId == null) {
			return result.fill(-2, "taskId不能为空");
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, taskMatchRankService.audit(taskId));
	}

	/**
	 * 取消资格
	 * @param
	 */
	@RequestMapping("/cancelQualification")
	@ResponseBody
	public JsonResponseMsg cancelQualification(Long taskId, Long userId) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (taskId == null) {
			return result.fill(-2, "taskId不能为空");
		}
		if (userId == null) {
			return result.fill(-2, "userId不能为空");
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS,
				taskMatchRankService.cancelQualification(taskId, userId));
	}

	/**
	 * 启用排位赛
	 * @param
	 */
	@RequestMapping("/enable")
	@ResponseBody
	public JsonResponseMsg enable(Long taskId) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (taskId == null) {
			return result.fill(-2, "taskId不能为空");
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS,
				taskMatchRankService.enable(taskId));
	}

}
