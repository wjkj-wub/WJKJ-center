package com.miqtech.master.admin.web.controller.backend;

import com.alibaba.fastjson.JSONObject;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.activity.ActivityGroup;
import com.miqtech.master.service.activity.ActivityGroupService;
import com.miqtech.master.utils.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("activityGroup/")
public class ActivityGroupController extends BaseController {
	@Autowired
	private ActivityGroupService activityGroupService;

	/**
	 * 分页列表
	 */
	@RequestMapping("/list/{type}")
	public ModelAndView groupList(Integer activityId, Integer netbarId, Integer round,
			@PathVariable("type") Integer type) {
		ModelAndView mv = new ModelAndView("activity/groupList"); //跳转到groupList.ftl页面
		mv.addObject("activityId", activityId);
		mv.addObject("netbarId", netbarId);
		mv.addObject("round", round);
		mv.addObject("type", type);
		List<Map<String, Object>> groups = activityGroupService.groupList(activityId, netbarId, round, type);
		if (groups == null || groups.size() == 0) {
			List<ActivityGroup> groupnew = activityGroupService.generateGroup(activityId, netbarId, round, type);
			mv.addObject("list", JSONObject.toJSONString(groupnew));
			return mv;
		}
		mv.addObject("list", JSONObject.toJSONString(groups));
		return mv;
	}

	@ResponseBody
	@RequestMapping("/rand/{type}")
	public JsonResponseMsg randgroupList(Integer activityId, Integer netbarId, Integer round,
			@PathVariable("type") Integer type) {
		JsonResponseMsg result = new JsonResponseMsg();
		List<Map<String, Object>> groups = activityGroupService.groupList(activityId, netbarId, round, type);
		if (groups == null || groups.size() == 0) {
			result.fill(CommonConstant.CODE_ERROR_LOGIC, "分组数据为空，请先生成分组");
			return result;
		}
		activityGroupService.generateRandGroup(activityId, netbarId, round, type);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);

		return result;
	}

	@ResponseBody
	@RequestMapping("/rank")
	public JsonResponseMsg rankgroup(Long id, Integer rank) {
		JsonResponseMsg result = new JsonResponseMsg();
		activityGroupService.updateRank(id, rank);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);

		return result;
	}

	@ResponseBody
	@RequestMapping("reset")
	public JsonResponseMsg reset(String activityId, String round, String netbarId, String isTeam) {
		if (NumberUtils.isNumber(activityId) && NumberUtils.isNumber(round) && NumberUtils.isNumber(netbarId)) {
			activityGroupService.resetGroups(NumberUtils.toLong(activityId), NumberUtils.toInt(round),
					NumberUtils.toLong(netbarId), StringUtils.equals("1", isTeam));
		}

		return new JsonResponseMsg().fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

}
