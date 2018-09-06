package com.miqtech.master.admin.web.controller.backend;

import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.consts.ActivityConstant;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.SystemUserConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.activity.ActivityItem;
import com.miqtech.master.entity.activity.ActivityMatch;
import com.miqtech.master.entity.common.SystemArea;
import com.miqtech.master.entity.common.SystemUser;
import com.miqtech.master.entity.netbar.NetbarInfo;
import com.miqtech.master.service.activity.ActivityItemServerService;
import com.miqtech.master.service.activity.ActivityItemService;
import com.miqtech.master.service.activity.ActivityMatchApplyService;
import com.miqtech.master.service.activity.ActivityMatchService;
import com.miqtech.master.service.netbar.NetbarInfoService;
import com.miqtech.master.service.system.SysUserAreaService;
import com.miqtech.master.service.system.SystemAreaService;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.ExcelUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.vo.PageVO;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("activityMatch/")
public class ActivityMatchController extends BaseController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ActivityMatchController.class);

	@Autowired
	private ActivityMatchService activityMatchService;
	@Autowired
	private ActivityItemServerService activityItemServerService;
	@Autowired
	private ActivityItemService activityItemService;
	@Autowired
	private SystemAreaService systemAreaService;
	@Autowired
	private SysUserAreaService sysUserAreaService;
	@Autowired
	private NetbarInfoService netbarInfoService;
	@Autowired
	private ActivityMatchApplyService activityMatchApplyService;

	/**
	 * 分页列表
	 */
	@RequestMapping("/list/{page}")
	public ModelAndView users(@PathVariable("page") int page, HttpServletRequest request, String title,
			String startDateMin, String startDateMax, String username, String areaCode, String itemId, String way) {
		ModelAndView mv = new ModelAndView("activity/matchList"); //跳转到matchList.ftl页面

		Map<String, Object> params = new HashMap<String, Object>(16);
		if (StringUtils.isNotBlank(title)) {
			params.put("title", title);
		}
		if (StringUtils.isNotBlank(startDateMin)) {
			params.put("startDateMin", startDateMin);
		}
		if (StringUtils.isNotBlank(startDateMax)) {
			params.put("startDateMax", startDateMax);
		}
		if (StringUtils.isNotBlank(username)) {
			params.put("username", username);
		}
		if (NumberUtils.isNumber(itemId)) {
			params.put("itemId", itemId);
		}
		if (NumberUtils.isNumber(way)) {
			params.put("way", way);
		}

		// 匹配登陆用户的地区设置
		SystemUser user = Servlets.getSessionUser(request);
		if (SystemUserConstant.TYPE_ACTIVITY_ADMIN.equals(user.getUserType())) {
			String uac = sysUserAreaService.findUserAreaCode(user.getId());
			params.put("areaCode", uac);
		} else {
			if (StringUtils.isNotBlank(areaCode)) {
				params.put("areaCode", areaCode);
			}
		}

		PageVO pageVO = activityMatchService.getMatchList(page, PageUtils.ADMIN_DEFAULT_PAGE_SIZE, params);
		List<ActivityItem> itemList = activityItemService.findAll();

		mv.addObject("list", pageVO.getList());
		mv.addObject("itemList", itemList);
		mv.addObject("currentPage", page);
		mv.addObject("isLastPage", PageUtils.isBottom(page, pageVO.getTotal()));//0已到最后一页 1可以加载下一页
		mv.addObject("totalCount", pageVO.getTotal());
		mv.addObject("totalPage", PageUtils.calcTotalPage(pageVO.getTotal()));
		mv.addObject("params", params);

		// 匹配游戏列表及地区列表
		List<ActivityItem> items = activityItemService.findAll();
		mv.addObject("items", items);
		List<SystemArea> provinces = systemAreaService.queryValidRoot();
		mv.addObject("provinces", provinces);
		return mv;
	}

	/**
	 * 根据竞技项目ID查服务器
	 */
	@ResponseBody
	@RequestMapping("/server/{itemId}")
	public JsonResponseMsg serverListByItemId(@PathVariable("itemId") long itemId) {
		JsonResponseMsg result = new JsonResponseMsg();
		Object serverList = activityItemServerService.getServerListByItemId(itemId, -1).get("serverList");
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, serverList);
		return result;
	}

	/**
	 * 请求详细信息
	 */
	@ResponseBody
	@RequestMapping("/detail/{matchId}")
	public JsonResponseMsg detail(@PathVariable("matchId") long matchId) {
		JsonResponseMsg result = new JsonResponseMsg();
		Map<String, Object> match = activityMatchService.findValidByIdWithNetbarInfo(matchId);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, match);
		return result;
	}

	/**
	 * 编辑更新
	 */
	@ResponseBody
	@RequestMapping("/save")
	public JsonResponseMsg save(ActivityMatch activityMatch, String idString, String itemIdString,
			String peopleNumString, String wayString, String serverString, String remarkYY, String remarkWX,
			String remarkQQ, String beginTimeParam, String netbarId) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (null != activityMatch.getPeopleNum() && !NumberUtils.isNumber(activityMatch.getPeopleNum().toString())) {
			result.fill(CommonConstant.CODE_ERROR_PARAM, "人数非数字");
			return result;
		}

		ActivityMatch activityMatchUpdate = null;
		if (NumberUtils.isNumber(idString)) {
			activityMatchUpdate = activityMatchService.findById(Long.parseLong(idString));
			if (activityMatchUpdate == null) {
				result.fill(CommonConstant.CODE_ERROR_LOGIC, "不存在的约战");
				return result;
			}
		} else {
			activityMatchUpdate = new ActivityMatch();
			activityMatchUpdate.setCreateDate(new Date());
			activityMatchUpdate.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			activityMatchUpdate.setIsStart(CommonConstant.INT_BOOLEAN_FALSE);
			activityMatchUpdate.setByMerchant(CommonConstant.INT_BOOLEAN_FALSE);
			activityMatchUpdate.setUserId(0L);
		}
		if (StringUtils.isNotBlank(activityMatch.getTitle())) {
			activityMatchUpdate.setTitle(activityMatch.getTitle());
		}
		if (StringUtils.isNotBlank(itemIdString)) {
			activityMatchUpdate.setItemId(Long.parseLong(itemIdString));
		}
		if (StringUtils.isNotBlank(serverString)) {
			activityMatchUpdate.setServer(serverString);
		}
		if (!com.miqtech.master.utils.StringUtils.isAllBlank(remarkYY, remarkWX, remarkQQ)) {
			activityMatchUpdate.setRemark("YY房号:" + remarkYY + " 微信号:" + remarkWX + " QQ号:" + remarkQQ);
		}
		if (StringUtils.isNotBlank(activityMatch.getRule())) {
			activityMatchUpdate.setRule(activityMatch.getRule());
		}
		if (StringUtils.isNotBlank(peopleNumString)) {
			activityMatchUpdate.setPeopleNum(Integer.parseInt(peopleNumString));
		}
		if (StringUtils.isNotBlank(beginTimeParam)) {
			Date beginDate = null;
			try {
				beginDate = DateUtils.stringToDateYyyyMMddhhmmss(beginTimeParam);
			} catch (ParseException e) {
				LOGGER.error("约战管理，转Date类型异常：" + e);
			}
			activityMatchUpdate.setBeginTime(beginDate);
		}
		if (StringUtils.equals(ActivityConstant.MATCH_WAY_ONLINE.toString(), wayString)) {
			activityMatchUpdate.setWay(ActivityConstant.MATCH_WAY_ONLINE);
		} else {
			if (NumberUtils.isNumber(netbarId)) {
				NetbarInfo netbar = netbarInfoService.findById(NumberUtils.toLong(netbarId));
				if (netbar != null) {
					activityMatchUpdate.setAddress(netbar.getName());
					activityMatchUpdate.setNetbarId(netbar.getId());
				}
			}
			activityMatchUpdate.setWay(ActivityConstant.MATCH_WAY_OFFLINE);
		}

		activityMatchService.saveOrUpdate(activityMatchUpdate);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);

		return result;
	}

	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("/delete/{matchId}")
	public JsonResponseMsg delete(@PathVariable("matchId") long matchId) {
		JsonResponseMsg result = new JsonResponseMsg();
		activityMatchService.deleteById(matchId);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 查看约战报名列表
	 */
	@RequestMapping("/members/{page}")
	public ModelAndView memberPage(@PathVariable("page") int page, String username, String matchId, String beginDate,
			String endDate) {
		ModelAndView mv = new ModelAndView("activity/matchMemberList");

		Map<String, Object> params = Maps.newHashMap();
		params.put("username", username);
		params.put("matchId", matchId);
		params.put("beginDate", beginDate);
		params.put("endDate", endDate);

		PageVO vo = activityMatchApplyService.page(page, params);
		pageModels(mv, vo.getList(), page, vo.getTotal());
		mv.addObject("params", params);

		return mv;
	}

	/**
	 * 导出约战报名
	 */
	@RequestMapping("/members/export/{page}")
	public ModelAndView membersExport(HttpServletResponse res, @PathVariable("page") int page, String username,
			String matchId, String beginDate, String endDate) {
		Map<String, Object> params = Maps.newHashMap();
		params.put("username", username);
		params.put("matchId", matchId);
		params.put("beginDate", beginDate);
		params.put("endDate", endDate);

		PageVO vo = activityMatchApplyService.page(page, params);

		String title = "约战报名情况";

		// 编辑excel内容
		List<Map<String, Object>> list = vo.getList();
		String[][] contents = new String[list.size() + 1][];

		// 设置标题行
		String[] contentTitle = new String[9];
		contentTitle[0] = "序号";
		contentTitle[1] = "约战名";
		contentTitle[2] = "竞技项目";
		contentTitle[3] = "用户昵称";
		contentTitle[4] = "用户手机号码";
		contentTitle[5] = "注册时间";
		contentTitle[6] = "报名约战时间";
		contentTitle[7] = "约战网吧名称";
		contentTitle[8] = "发布时间";
		contents[0] = contentTitle;

		// 设置内容
		for (int i = 0; i < list.size(); i++) {
			Map<String, Object> member = list.get(i);
			String[] row = new String[9];
			row[0] = MapUtils.getString(member, "id");
			row[1] = MapUtils.getString(member, "title");
			row[2] = MapUtils.getString(member, "itemName");
			row[3] = MapUtils.getString(member, "nickname");
			row[4] = MapUtils.getString(member, "username");
			row[5] = MapUtils.getString(member, "userCreateDate");
			row[6] = MapUtils.getString(member, "applyCreateDate");
			row[7] = MapUtils.getString(member, "netbarName");
			row[8] = MapUtils.getString(member, "matchCreateDate");
			contents[i + 1] = row;
		}

		try {
			ExcelUtils.exportExcel(title, contents, false, res);
		} catch (Exception e) {
			LOGGER.error("导出数据异常：", e);
		}
		return null;
	}
}
