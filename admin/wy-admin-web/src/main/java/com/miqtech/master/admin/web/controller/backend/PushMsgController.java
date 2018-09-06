package com.miqtech.master.admin.web.controller.backend;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Maps;
import com.miqtech.master.config.SystemConfig;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.Msg4UserType;
import com.miqtech.master.consts.MsgConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.activity.ActivityOverActivity;
import com.miqtech.master.entity.activity.ActivityOverActivityModule;
import com.miqtech.master.entity.common.SystemArea;
import com.miqtech.master.entity.mall.CommodityInfo;
import com.miqtech.master.entity.msg.MsgPushLog;
import com.miqtech.master.service.activity.ActivityInfoService;
import com.miqtech.master.service.activity.ActivityOverActivityModuleService;
import com.miqtech.master.service.activity.ActivityOverActivityService;
import com.miqtech.master.service.amuse.AmuseActivityInfoService;
import com.miqtech.master.service.bounty.BountyService;
import com.miqtech.master.service.mall.CommodityService;
import com.miqtech.master.service.msg.MsgPushLogService;
import com.miqtech.master.service.system.SystemAreaService;
import com.miqtech.master.thirdparty.service.JpushService;
import com.miqtech.master.thirdparty.util.JPushUtils;
import com.miqtech.master.utils.JsonUtils;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

@Controller
@RequestMapping("push")
public class PushMsgController extends BaseController {
	private static final Logger LOGGER = LoggerFactory.getLogger(PushMsgController.class);
	@Autowired
	private JpushService msgOperateService;
	@Autowired
	private SystemAreaService systemAreaService;
	@Autowired
	private ActivityOverActivityService activityOverActivityService;
	@Autowired
	private ActivityOverActivityModuleService activityOverActivityModuleService;
	@Autowired
	private AmuseActivityInfoService amuseActivityInfoService;
	@Autowired
	private ActivityInfoService activityInfoService;
	@Autowired
	private CommodityService commodityService;

	@Autowired
	private SystemConfig systemConfig;

	@RequestMapping("/pushPage")
	public ModelAndView test() {
		ModelAndView mv = new ModelAndView("/sys/push/push"); //跳转到push.ftl页面
		// 匹配地区树数据
		List<SystemArea> areas = systemAreaService.getTree(true);
		mv.addObject("areas", areas);
		List<ActivityOverActivityModule> modules = activityOverActivityModuleService.findValidByPid(0L);
		mv.addObject("modules", modules);
		return mv;
	}

	@ResponseBody
	@RequestMapping("/message")
	public JsonResponseMsg pushMessage(HttpServletRequest request, String title, String content, String way, String id,
			String now, int category) {

		String envPrefix = "";
		String environment = systemConfig.getEnvironment();
		if (StringUtils.equals(environment, "test") || StringUtils.equals(environment, "dev")) {
			envPrefix = "test_";
			JPushUtils.setOnlie(false);
		}

		JsonResponseMsg result = new JsonResponseMsg();
		if (StringUtils.isBlank(title)) {
			result.fill(CommonConstant.CODE_ERROR_PARAM, "推送标题为空");
			return result;
		}
		if (StringUtils.isBlank(content)) {
			result.fill(CommonConstant.CODE_ERROR_PARAM, "推送内容为空");
			return result;
		}

		MsgPushLog log = new MsgPushLog();
		log.setTitle(title);
		log.setContent(content);
		log.setCreateDate(new Date());
		log.setInfoType(category);

		String moduleId = request.getParameter("moduleName");
		ActivityOverActivityModule activityModule = activityOverActivityModuleService
				.findById(NumberUtils.toLong(moduleId));

		if (null != activityModule) {
			log.setModuleName(activityModule.getName());
		}
		String subModuleId = request.getParameter("subModuleName");
		ActivityOverActivityModule subActivityModule = activityOverActivityModuleService
				.findById(NumberUtils.toLong(subModuleId));
		if (null != subActivityModule) {
			log.setSubModuleName(subActivityModule.getName());
		}
		log.setValid(1);

		int type = 0;
		long typeTargetId = 0;//信息id

		int fav = 0;//是否按兴趣推送
		String data = null;
		String extendData = null;
		String categoryFlag = "";
		switch (category) {
		case 1: {
			categoryFlag = MsgConstant.PUSH_MSG_TYPE_SYS;
			type = Msg4UserType.SYS_NOTIFY.ordinal(); //系统 :系统消息
			break;
		}
		case 4: {
			categoryFlag = MsgConstant.PUSH_MSG_TYPE_AMUSE;
			type = Msg4UserType.AMUSE.ordinal();//娱乐赛消息
			typeTargetId = NumberUtils.toLong(request.getParameter("hidden_amuse"));
			data = typeTargetId + "";
			String infoTitle = request.getParameter("amuse_title");
			log.setInfoTitle(infoTitle);
			break;
		}
		case 3: {
			categoryFlag = MsgConstant.PUSH_MSG_TYPE_ACTIVITY;
			type = Msg4UserType.ACTIVITY.ordinal(); //官方赛
			typeTargetId = NumberUtils.toLong(request.getParameter("hidden_activity"));
			data = typeTargetId + "";
			String infoTitle = request.getParameter("activity_title");
			log.setInfoTitle(infoTitle);
			break;
		}
		case 2: {

			categoryFlag = MsgConstant.PUSH_MSG_TYPE_INFORMATION;
			type = Msg4UserType.INFORMATION.ordinal(); //资讯
			typeTargetId = NumberUtils.toLong(request.getParameter("hidden_infomation"));
			data = typeTargetId + "";
			fav = NumberUtils.toInt(request.getParameter("fav"));//按兴趣  0
			ActivityOverActivity aoa = activityOverActivityService.findById(typeTargetId);
			if (aoa == null) {
				result.fill(CommonConstant.CODE_ERROR_PARAM, "推送的资讯不存在,请重新选择");
				return result;
			}
			Integer aoaType = aoa.getType();
			if (aoaType == null) {
				result.fill(CommonConstant.CODE_ERROR_PARAM, "您推送的资讯类别有问题,请检查资讯信息");
				return result;
			}
			extendData = "{\"type\":" + aoaType.intValue() + "}";
			String infoTitle = request.getParameter("information_title");
			log.setInfoTitle(infoTitle);
			break;
		}
		case 5: {
			categoryFlag = MsgConstant.PUSH_MSG_TYPE_MATCH;
			type = Msg4UserType.MATCH.ordinal(); //约战

			break;
		}
		case 6: {
			categoryFlag = MsgConstant.PUSH_MSG_TYPE_MALL;
			type = Msg4UserType.MALL.ordinal(); //金币商城
			break;
		}
		case 8: {
			categoryFlag = MsgConstant.PUSH_MSG_TYPE_COIN_TASK;
			type = Msg4UserType.COIN_TASK.ordinal(); //金币任务
			break;
		}
		case 7: {//金币商城商品
			//金币商城商品
			typeTargetId = NumberUtils.toLong(request.getParameter("hidden_commodity"));
			CommodityInfo aoa = commodityService.getCommodityById(typeTargetId);
			if (aoa == null) {
				result.fill(CommonConstant.CODE_ERROR_PARAM, "推送的商品不存在,请重新选择");
				return result;
			}
			if (aoa.getAreaId() == 3) {
				categoryFlag = MsgConstant.PUSH_MSG_TYPE_ROBTREASURE;
				type = Msg4UserType.ROBTREASURE.ordinal();
			} else {
				categoryFlag = MsgConstant.PUSH_MSG_TYPE_AWARD_COMMODITY;
				type = Msg4UserType.AWARD_COMMODITY.ordinal();
			}
			data = typeTargetId + "";
			extendData = "{\"type\":" + aoa.getAreaId() + "}";
			String infoTitle = request.getParameter("commodity_title");
			log.setInfoTitle(infoTitle);
			break;
		}
		case 9: {
			categoryFlag = MsgConstant.PUSH_MSG_TYPE_BOUNTY;
			type = Msg4UserType.AMUSE.ordinal();//悬赏令消息
			typeTargetId = NumberUtils.toLong(request.getParameter("hidden_bounty"));
			data = typeTargetId + "";
			String infoTitle = request.getParameter("bounty_title");
			log.setInfoTitle(infoTitle);
			break;
		}

		default: {
			break;
		}
		}
		if (category <= 0) {
			result.fill(CommonConstant.CODE_ERROR_PARAM, "请选择推送类型");
			return result;
		}
		log.setInfoId(typeTargetId);
		int clientType = Integer.parseInt(way);
		log.setClientType(clientType);
		try {
			if (1 == clientType) { //单个用户
				if (category == 1) {
					result.fill(CommonConstant.CODE_ERROR_PARAM, "系统消息只能推送给全体用户");
					return result;
				}
				if (fav > 0) {
					result.fill(CommonConstant.CODE_ERROR_PARAM, "单个用户不能进行兴趣推送");
					return result;
				}
				if (StringUtils.isNotBlank(id)) {
					if (!NumberUtils.isNumber(id)) {
						result.fill(CommonConstant.CODE_ERROR_PARAM, "推送目标ID参数错误");
						return result;
					}
				} else {
					result.fill(CommonConstant.CODE_ERROR_PARAM, "推送目标ID不能为空");
					return result;
				}
				log.setClientInfo(id);
				msgOperateService.notifyMemberAliasMsgWithoutDB(type, Long.parseLong(id), categoryFlag, content, data,
						extendData);
			} else if (2 == clientType) { //全体用户
				String tag = systemConfig.getJpushClientTag();
				if (fav > 0) {
					tag = envPrefix + "members_info_module_" + subModuleId;
				}
				if (category == 1) {
					msgOperateService.notifyAllSysMsg(tag, title, content, true);
				} else {
					msgOperateService.notifyMemberTagMsgWithoutDB(tag, categoryFlag, content, data, extendData);
				}

			} else if (3 == clientType) { //区域
				if (category == 1) {
					result.fill(CommonConstant.CODE_ERROR_PARAM, "系统消息只能推送给全体用户");
					return result;
				}
				String areaCode = request.getParameter("areaCode");
				if (StringUtils.isBlank(areaCode)) {
					result.fill(CommonConstant.CODE_ERROR_PARAM, "推送地区参数错误");
					return result;
				}
				log.setClientInfo(areaCode);
				String tag = envPrefix + "members_area_" + areaCode;
				if (fav > 0) {
					tag = envPrefix + "members_area_info_module_" + areaCode + "_" + subModuleId;
				}
				msgOperateService.notifyMemberTagMsgWithoutDB(tag, categoryFlag, content, data, extendData);
			} else {
				result.fill(CommonConstant.CODE_ERROR_PARAM, "请选择推送对象");
				return result;
			}
		} catch (Exception e) {
			LOGGER.error("推送消息异常：" + e);
		}

		pushMsgLogService.save(log);
		result.fill(CommonConstant.CODE_SUCCESS, "消息已推送！");
		return result;
	}

	@Autowired
	private MsgPushLogService pushMsgLogService;

	/**
	 * 推送历史记录列表
	 */
	@RequestMapping("list/{page}")
	public ModelAndView pushMsgPage(HttpServletRequest request, @PathVariable(value = "page") Integer page) {
		ModelAndView mv = new ModelAndView("sys/push/list");
		Map<String, Object> params = Maps.newHashMap();
		if (page == null) {
			page = 1;
		}
		params.put("page", page);
		String title = request.getParameter("title");
		if (StringUtils.isNotBlank(title)) {
			params.put("title", title);
		}

		String startDate = request.getParameter("startDate");
		String endDate = request.getParameter("endDate");
		if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
			params.put("startDate", startDate);
			params.put("endDate", endDate);
		}

		String infoTypeString = request.getParameter("infoType");
		int infoType = NumberUtils.toInt(infoTypeString);

		if (infoType > 0) {
			params.put("infoType", infoType);
		}

		mv.addObject("params", params);
		PageVO vo = pushMsgLogService.page(params);
		pageModels(mv, vo.getList(), page, vo.getTotal());
		return mv;
	}

	/**
	 * 推送历史记录列表
	 */
	@RequestMapping("informations")
	@ResponseBody
	public String informations(HttpServletRequest request) {
		Map<String, Object> result = Maps.newHashMap();
		String title = request.getParameter("query");

		Long subModuleId = NumberUtils.toLong(request.getParameter("subModuleId"));

		List<Map<String, Object>> informations = activityOverActivityService.findValidByTitleAndModule(title,
				subModuleId);
		result.put("informations", informations);
		return JsonUtils.objectToString(result);
	}

	@RequestMapping("commoditys")
	@ResponseBody
	public String commoditys(HttpServletRequest request) {
		Map<String, Object> result = Maps.newHashMap();
		String name = request.getParameter("query");
		Long areaId = NumberUtils.toLong(request.getParameter("areaId"));
		List<Map<String, Object>> commoditys = commodityService.findValidByTitleAndModule(name, areaId);
		result.put("commoditys", commoditys);
		return JsonUtils.objectToString(result);
	}

	@RequestMapping("activities")
	@ResponseBody
	public String activities(HttpServletRequest request) {
		Map<String, Object> result = Maps.newHashMap();
		String title = request.getParameter("query");
		List<Map<String, Object>> data = activityInfoService.findValidByTitle(title);
		result.put("activities", data);
		return JsonUtils.objectToString(result);
	}

	@RequestMapping("amuses")
	@ResponseBody
	public String amuses(HttpServletRequest request) {
		Map<String, Object> result = Maps.newHashMap();
		String title = request.getParameter("query");
		List<Map<String, Object>> data = amuseActivityInfoService.findValidByTitle(title);
		result.put("amuses", data);
		return JsonUtils.objectToString(result);
	}

	@Autowired
	private BountyService bountyService;

	@RequestMapping("bounties")
	@ResponseBody
	public String bounties(HttpServletRequest request) {
		Map<String, Object> result = Maps.newHashMap();
		String name = request.getParameter("query");
		List<Map<String, Object>> bounties = bountyService.findValidByTitle(name);
		result.put("bounties", bounties);
		return JsonUtils.objectToString(result);
	}

	/**
	 * 推送历史记录列表
	 */
	@RequestMapping("subModules")
	@ResponseBody
	public String subModules(HttpServletRequest request) {
		String moduleId = request.getParameter("moduleId");
		Map<String, Object> result = Maps.newHashMap();
		long pId = NumberUtils.toLong(moduleId);
		if (pId > 0) {
			List<ActivityOverActivityModule> subModules = activityOverActivityModuleService.findValidByPid(pId);
			result.put("subModules", subModules);
		}
		return JsonUtils.objectToString(result);
	}
}
