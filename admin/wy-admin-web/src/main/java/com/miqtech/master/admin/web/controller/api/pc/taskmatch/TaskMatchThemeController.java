package com.miqtech.master.admin.web.controller.api.pc.taskmatch;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.miqtech.master.admin.web.annotation.LoginValid;
import com.miqtech.master.admin.web.controller.api.BaseController;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.taskMatch.TaskMatchConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.pc.taskmatch.TaskMatchCondition;
import com.miqtech.master.entity.pc.taskmatch.TaskMatchLimitHero;
import com.miqtech.master.entity.pc.taskmatch.TaskMatchTheme;
import com.miqtech.master.enumConstant.taskMatch.TaskMatchConditionParamEnum;
import com.miqtech.master.enumConstant.taskMatch.TaskMatchConditionSymbolEnum;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.service.pc.taskmatch.RuleConditionService;
import com.miqtech.master.service.pc.taskmatch.TaskMatchLimitHeroService;
import com.miqtech.master.service.pc.taskmatch.TaskMatchThemeService;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 主题任务赛后台管理操作
 *
 * @author zhangyuqi
 * @create 2017年09月02日
 */
@Controller
@RequestMapping("/api/taskMatch/theme")
public class TaskMatchThemeController extends BaseController {

	@Autowired
	private TaskMatchThemeService taskMatchThemeService;
	@Autowired
	private RuleConditionService ruleConditionService;
	@Autowired
	private TaskMatchLimitHeroService taskMatchLimitHeroService;
	@Autowired
	private StringRedisOperateService stringRedisOperateService;

	private final Joiner joiner = Joiner.on("_");
	private final int TASK_NAME_LENGTH_LIMIT = 48;
	private final String DEFAULT_HREO_ID = "0";

	private static final Logger LOGGER = Logger.getLogger(TaskMatchThemeController.class);

	/**
	 * 获取领取主题任务用户列表
	 * @param id    主题任务id
	 */
	@RequestMapping("/record")
	@LoginValid(valid = true)
	@ResponseBody
	public JsonResponseMsg getThemeTaskUserRecord(Long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (id == null) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}

		Map<String, Object> map = taskMatchThemeService.getThemeTaskRecordStatistics(id);
		map.put("recordList", taskMatchThemeService.getThemeTaskUserRecord(id));
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, map);
	}

	/**
	 * 主题任务赛列表
	 */
	@RequestMapping("/list")
	@ResponseBody
	public JsonResponseMsg list(String start, String end, Integer status, String keyword, Integer page,
			Integer pageSize) {
		JsonResponseMsg result = new JsonResponseMsg();
		PageVO vo = taskMatchThemeService.getThemeList(start, end, status, keyword, page, pageSize);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, vo);
	}

	/**
	 * 主题任务赛条件参数列表
	 */
	@RequestMapping("/paramList")
	@ResponseBody
	public JsonResponseMsg paramList() {
		JsonResponseMsg result = new JsonResponseMsg();
		List<Map<String, Object>> list = TaskMatchConditionParamEnum.getList();
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, list);
	}

	/**
	 * 主题任务赛符号列表
	 */
	@RequestMapping("/symbolList")
	@ResponseBody
	public JsonResponseMsg symbolList() {
		JsonResponseMsg result = new JsonResponseMsg();
		List<Map<String, Object>> list = TaskMatchConditionSymbolEnum.getList();
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, list);
	}

	/**
	 * 主题任务赛上架
	 */
	@RequestMapping("/release")
	@ResponseBody
	public JsonResponseMsg release(Long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (id == null) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, "id不能为空");
		}

		TaskMatchTheme theme = taskMatchThemeService.findById(id);
		if (theme == null) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, "该赛事不存在");
		}

		if (!new Date().before(theme.getEndDate())) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, "该赛事已超过结束时间，不能上架");
		}

		if (TaskMatchConstant.THEME_RELEASED.equals(theme.getIsRelease())) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, "该赛事已上架");
		}
		theme.setIsRelease(TaskMatchConstant.THEME_RELEASED);
		Date now = new Date();
		if (!now.before(theme.getBeginDate())) {
			if (TaskMatchConstant.THEME_RELEASED.equals(theme.getIsRelease())) {
				theme.setStatus(TaskMatchConstant.THEME_STATUS_PROCESS);
				theme.setUpdateDate(now);
			}

		}
		taskMatchThemeService.save(theme);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 *主题任务赛创建
	 */
	@ResponseBody
	@RequestMapping("create")
	public JsonResponseMsg create(HttpServletRequest req, TaskMatchTheme taskMatchTheme, String condition,
			String beginTime, String endTime, String heroId, Integer limitTimes) {
		JsonResponseMsg result = new JsonResponseMsg();
		Long userId = this.getUserIdFromCookie(req);
		if (userId == null) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, "请先登录");
		}

		String limitKey = joiner.join(TaskMatchConstant.TASK_MATCH_THEME_CREATE_LIMIT, userId);
		RedisAtomicInteger joinLimit = new RedisAtomicInteger(limitKey,
				stringRedisOperateService.getRedisTemplate().getConnectionFactory());
		joinLimit.expire(2, TimeUnit.SECONDS);
		if (joinLimit.incrementAndGet() > 1) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, "请勿重复提交");
		}

		try {
			taskMatchTheme.setBeginDate(DateUtils
					.stringToDate(DateUtils.stampToDate(beginTime, DateUtils.YYYY_MM_DD_HH), DateUtils.YYYY_MM_DD_HH));
			taskMatchTheme.setEndDate(DateUtils.stringToDate(DateUtils.stampToDate(endTime, DateUtils.YYYY_MM_DD_HH),
					DateUtils.YYYY_MM_DD_HH));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		//判断参数是否已填
		Date now = new Date();
		String name = taskMatchTheme.getName();
		if (StringUtils.isAnyBlank(name, taskMatchTheme.getImgUrl(), taskMatchTheme.getGameRule(), condition, heroId)) {
			joinLimit.expireAt(now);
			return result.fill(CommonConstant.CODE_ERROR_PARAM, "请检查参数是否全部填写");
		}
		if (taskMatchTheme.getFeeType() == null || taskMatchTheme.getFeeAmount() == null || limitTimes == null
				|| taskMatchTheme.getTotalAward() == null || taskMatchTheme.getTotalAwardType() == null
				|| taskMatchTheme.getDifficulty() == null || taskMatchTheme.getType() == null) {
			joinLimit.expireAt(now);
			return result.fill(CommonConstant.CODE_ERROR_PARAM, "请检查参数是否全部填写");
		}
		if (beginTime == null || endTime == null) {
			joinLimit.expireAt(now);
			return result.fill(CommonConstant.CODE_ERROR_PARAM, "请检查参数是否全部填写");
		}
		if (StringUtils.length(name) > TASK_NAME_LENGTH_LIMIT) {
			joinLimit.expireAt(now);
			return result.fill(CommonConstant.CODE_ERROR_PARAM, "赛事名长度超过限制");
		}
		if (taskMatchThemeService.isNameRepeat(name, null)) {
			joinLimit.expireAt(now);
			return result.fill(CommonConstant.CODE_ERROR_PARAM, "赛事名已存在");
		}

		taskMatchTheme.setStatus(TaskMatchConstant.THEME_STATUS_PREPARE);
		taskMatchTheme.setIsValid(CommonConstant.INT_BOOLEAN_TRUE);
		taskMatchTheme.setCreateDate(now);
		if (!now.before(taskMatchTheme.getBeginDate())) {
			if (TaskMatchConstant.THEME_RELEASED.equals(taskMatchTheme.getIsRelease())) {
				taskMatchTheme.setStatus(TaskMatchConstant.THEME_STATUS_PROCESS);
			}

		}
		taskMatchThemeService.save(taskMatchTheme);
		Long id = taskMatchTheme.getId();

		//保存条件信息
		JSONObject jsonObject = JSONObject.parseObject(condition);
		JSONArray conditionArray = jsonObject.getJSONArray("condition");
		List<TaskMatchCondition> list = new ArrayList<>();
		for (Iterator it = conditionArray.iterator(); it.hasNext();) {
			JSONObject object = (JSONObject) it.next();
			Integer param1 = object.getInteger("param1");
			Integer param2 = object.getInteger("param2");
			Integer paramResult = object.getInteger("result");
			String symbol = object.getString("symbol");
			TaskMatchCondition ruleCondition = new TaskMatchCondition(taskMatchTheme.getId(),
					TaskMatchConstant.CONDITION_MODULE_TYPE_THEME, symbol, param1, param2, paramResult,
					CommonConstant.INT_BOOLEAN_TRUE, now);
			list.add(ruleCondition);
		}
		if (CollectionUtils.isNotEmpty(list)) {
			ruleConditionService.save(list);
		}

		//保存英雄信息
		if (StringUtils.isNotBlank(heroId) && !DEFAULT_HREO_ID.equals(heroId)) {
			List<TaskMatchLimitHero> heroList = new ArrayList<>();
			List<String> heroIdList = Arrays.asList(heroId.split(","));
			for (String heroIdStr : heroIdList) {
				long heroid = NumberUtils.toLong(heroIdStr);
				TaskMatchLimitHero hero = new TaskMatchLimitHero(id, TaskMatchConstant.TASK_TYPE_THEME, heroid,
						CommonConstant.INT_BOOLEAN_TRUE, now);
				heroList.add(hero);

			}
			if (CollectionUtils.isNotEmpty(heroIdList)) {
				taskMatchLimitHeroService.save(heroList);
			}
		}
		joinLimit.expireAt(now);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 * 主题任务赛查询
	 */
	@ResponseBody
	@RequestMapping("query")
	public JsonResponseMsg query(Long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		TaskMatchTheme theme = taskMatchThemeService.findById(id);
		Map<String, Object> map = BeanUtils.beanToMap(theme);

		//查询条件信息
		List<Map<String, Object>> conditionList = ruleConditionService.findConditionInfo(id,
				TaskMatchConstant.CONDITION_MODULE_TYPE_THEME);
		if (CollectionUtils.isNotEmpty(conditionList)) {
			map.put("condition", conditionList.toArray());
		} else {
			map.put("condition", 0);
		}

		//查询英雄信息
		List<Map<String, Object>> heroidList = taskMatchLimitHeroService.findHeroId(id,
				TaskMatchConstant.TASK_TYPE_THEME);
		if (CollectionUtils.isNotEmpty(heroidList)) {
			map.put("heroId", heroidList.toArray());
		} else {
			map.put("heroId", 0);
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, map);
	}

	/**
	 *主题任务赛创建
	 */
	@ResponseBody
	@LoginValid(valid = true)
	@RequestMapping("modify")
	public JsonResponseMsg modify(TaskMatchTheme taskMatchTheme, String condition, String deleteConditionIds,
			String heroId, Integer limitTimes, String beginTime, String endTime, Integer modifyType) {
		JsonResponseMsg result = new JsonResponseMsg();
		//判断信息是否已填
		String name = taskMatchTheme.getName();
		if (StringUtils.isAnyBlank(name, taskMatchTheme.getImgUrl(), taskMatchTheme.getGameRule(), condition, heroId)) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, "请检查参数是否全部填写");
		}
		if (taskMatchTheme.getFeeType() == null || taskMatchTheme.getFeeAmount() == null || limitTimes == null
				|| taskMatchTheme.getTotalAward() == null || taskMatchTheme.getTotalAwardType() == null
				|| taskMatchTheme.getDifficulty() == null) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, "请检查参数是否全部填写");
		}

		Date now = new Date();
		try {
			if (StringUtils.isNotBlank(beginTime)) {
				taskMatchTheme.setBeginDate(DateUtils.stringToDate(
						DateUtils.stampToDate(beginTime, DateUtils.YYYY_MM_DD_HH), DateUtils.YYYY_MM_DD_HH));
			}
			if (StringUtils.isNotBlank(endTime)) {
				taskMatchTheme.setEndDate(DateUtils.stringToDate(
						DateUtils.stampToDate(endTime, DateUtils.YYYY_MM_DD_HH), DateUtils.YYYY_MM_DD_HH));
			}
		} catch (ParseException e) {
			LOGGER.info("主题任务赛修改创建时日期转换出错");
		}

		if (modifyType == 2) {
			if (!taskMatchTheme.getEndDate().after(now)) {
				return result.fill(CommonConstant.CODE_ERROR_PARAM, "赛事时间有误，无法再次创建");
			}
		}

		if (StringUtils.length(name) > TASK_NAME_LENGTH_LIMIT) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, "赛事名长度超过限制");
		}
		if (taskMatchThemeService.isNameRepeat(name, taskMatchTheme.getId())) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, "赛事名已存在");
		}
		taskMatchTheme.setIsValid(CommonConstant.INT_BOOLEAN_TRUE);

		//更改赛事状态
		if (TaskMatchConstant.THEME_UNRELEASED.equals(taskMatchTheme.getIsRelease())
				&& now.before(taskMatchTheme.getEndDate())) {
			taskMatchTheme.setStatus(TaskMatchConstant.THEME_STATUS_PREPARE);
		} else if (TaskMatchConstant.THEME_RELEASED.equals(taskMatchTheme.getIsRelease())
				&& !now.before(taskMatchTheme.getBeginDate()) && now.before(taskMatchTheme.getEndDate())) {
			taskMatchTheme.setStatus(TaskMatchConstant.THEME_STATUS_PROCESS);
		} else if (TaskMatchConstant.THEME_RELEASED.equals(taskMatchTheme.getIsRelease())
				&& now.before(taskMatchTheme.getBeginDate())) {
			taskMatchTheme.setStatus(TaskMatchConstant.THEME_STATUS_PREPARE);
		}
		if (taskMatchTheme.getCreateDate() == null) {
			taskMatchTheme.setCreateDate(now);
		}
		taskMatchThemeService.save(taskMatchTheme);
		Long id = taskMatchTheme.getId();

		//保存条件信息
		JSONObject jsonObject = JSONObject.parseObject(condition);
		JSONArray conditionArray = jsonObject.getJSONArray("condition");
		List<TaskMatchCondition> list = new ArrayList<>();
		for (Iterator it = conditionArray.iterator(); it.hasNext();) {
			JSONObject object = (JSONObject) it.next();
			Integer param1 = object.getInteger("param1");
			Integer param2 = object.getInteger("param2");
			Integer paramResult = object.getInteger("result");
			String symbol = object.getString("symbol");
			Long conditionId = object.getLong("id");
			TaskMatchCondition ruleCondition = new TaskMatchCondition(taskMatchTheme.getId(),
					TaskMatchConstant.CONDITION_MODULE_TYPE_THEME, symbol, param1, param2, paramResult,
					CommonConstant.INT_BOOLEAN_TRUE, now);
			boolean isSetId = (modifyType == null || modifyType != 2) && conditionId != null;
			if (isSetId) {
				ruleCondition.setId(conditionId);
			}
			list.add(ruleCondition);
		}
		//删除条件信息
		if (StringUtils.isNotBlank(deleteConditionIds)) {
			List<TaskMatchCondition> deleteRuleConditionList = ruleConditionService.findByIds(deleteConditionIds);
			for (TaskMatchCondition ruleCondition : deleteRuleConditionList) {
				ruleCondition.setIsValid(CommonConstant.INT_BOOLEAN_FALSE);
				list.add(ruleCondition);
			}
		}
		if (CollectionUtils.isNotEmpty(list)) {
			ruleConditionService.save(list);
		}

		//保存英雄信息
		List<TaskMatchLimitHero> heroList = new ArrayList<>();
		List<TaskMatchLimitHero> deleteHeroList = taskMatchLimitHeroService.findByTargetIdAndType(id,
				TaskMatchConstant.TASK_TYPE_THEME);
		if (CollectionUtils.isNotEmpty(deleteHeroList)) {
			for (TaskMatchLimitHero hero : deleteHeroList) {
				hero.setIsValid(CommonConstant.INT_BOOLEAN_FALSE);
				heroList.add(hero);
			}
		}
		if (StringUtils.isNotBlank(heroId) && !DEFAULT_HREO_ID.equals(heroId)) {
			List<String> heroIdList = Arrays.asList(heroId.split(","));
			for (String heroIdStr : heroIdList) {
				long heroid = NumberUtils.toLong(heroIdStr);
				TaskMatchLimitHero hero = new TaskMatchLimitHero(id, TaskMatchConstant.TASK_TYPE_THEME, heroid,
						CommonConstant.INT_BOOLEAN_TRUE, now);
				heroList.add(hero);

			}
		}
		if (CollectionUtils.isNotEmpty(heroList)) {
			taskMatchLimitHeroService.save(heroList);
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/*
	*主题任务赛删除
	*/
	@ResponseBody
	@LoginValid(valid = true)
	@RequestMapping("delete")
	public JsonResponseMsg delete(Long id) {
		JsonResponseMsg result = new JsonResponseMsg();

		TaskMatchTheme theme = taskMatchThemeService.findById(id);
		if (TaskMatchConstant.THEME_RELEASED.equals(theme.getIsRelease())) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, "商品已上架，无法删除");
		}
		//删除条件信息
		List<TaskMatchCondition> conditionList = ruleConditionService.findByTargetIdAndType(id,
				TaskMatchConstant.CONDITION_MODULE_TYPE_THEME);
		if (CollectionUtils.isNotEmpty(conditionList)) {
			for (TaskMatchCondition ruleCondition : conditionList) {
				ruleCondition.setIsValid(CommonConstant.INT_BOOLEAN_FALSE);
			}
			ruleConditionService.save(conditionList);
		}

		//删除英雄信息
		List<TaskMatchLimitHero> heroList = taskMatchLimitHeroService.findByTargetIdAndType(id,
				TaskMatchConstant.TASK_TYPE_THEME);
		if (CollectionUtils.isNotEmpty(heroList)) {
			for (TaskMatchLimitHero hero : heroList) {
				hero.setIsValid(CommonConstant.INT_BOOLEAN_FALSE);
			}
			taskMatchLimitHeroService.save(heroList);
		}

		//删除任务赛信息
		theme.setIsValid(CommonConstant.INT_BOOLEAN_FALSE);
		taskMatchThemeService.save(theme);

		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

}
