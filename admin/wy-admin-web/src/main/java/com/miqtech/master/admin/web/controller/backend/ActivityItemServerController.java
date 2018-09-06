package com.miqtech.master.admin.web.controller.backend;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.activity.ActivityItem;
import com.miqtech.master.entity.activity.ActivityServer;
import com.miqtech.master.service.activity.ActivityItemServerService;
import com.miqtech.master.service.activity.ActivityItemService;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

@Controller
@RequestMapping("activityIteamServer/")
public class ActivityItemServerController extends BaseController {
	@Autowired
	private ActivityItemServerService activityItemServerService;
	@Autowired
	private ActivityItemService activityItemService;

	/**
	 * 分页列表，服务器
	 */
	@RequestMapping("/listServer/{page}")
	public ModelAndView listServer(@PathVariable("page") int page, String id, String name, long itemId, String isParent,
			String pId) {
		ModelAndView mv =null;
		Map<String, Object> params = new HashMap<String, Object>(16);
		if (isParent.equals("1")) {
			mv = new ModelAndView("activity/itemServerParentList"); //跳转到游戏区页面
		} else {
			mv = new ModelAndView("activity/itemServerList"); //从游戏区跳转到对应的服务器页面
			if (StringUtils.isNotBlank(pId)) {
				String serverName = org.apache.commons.lang3.StringUtils.EMPTY;
				ActivityServer activityServer = activityItemServerService.findById(NumberUtils.toLong(pId));
				if (null != activityServer) {
					serverName = activityServer.getServerName();
				}
				mv.addObject("serverName", serverName);
				params.put("pId", pId);
			}
		}

		params.put("itemId", itemId);

		if (StringUtils.isNotBlank(name)) {
			params.put("name", name);
		}
		if (StringUtils.isNotBlank(pId)) {
		}

		PageVO pageVO = activityItemServerService.getServerList(page, PageUtils.ADMIN_DEFAULT_PAGE_SIZE, params,
				isParent);
		String itemName=org.apache.commons.lang3.StringUtils.EMPTY;
		ActivityItem activityItem = activityItemService.findById(itemId);
		if (null != activityItem) {
			itemName = activityItem.getName();
		}
		mv.addObject("list", pageVO.getList());
		mv.addObject("currentPage", page);
		mv.addObject("isLastPage", PageUtils.isBottom(page, pageVO.getTotal()));//0已到最后一页 1可以加载下一页
		mv.addObject("totalCount", pageVO.getTotal());
		mv.addObject("totalPage", PageUtils.calcTotalPage(pageVO.getTotal()));
		mv.addObject("params", params);
		mv.addObject("itemName", itemName);
		return mv;
	}

	/**
	 * 请求详细信息
	 */
	@ResponseBody
	@RequestMapping("/detail/{serverId}")
	public JsonResponseMsg detail(@PathVariable("serverId") long serverId) {
		JsonResponseMsg result = new JsonResponseMsg();
		ActivityServer activityServer = activityItemServerService.findById(serverId);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, activityServer);
		return result;
	}

	/**
	 * 新增或更新
	 */
	@ResponseBody
	@RequestMapping("/save")
	public JsonResponseMsg save(String id, String itemId, String serverName, String parentName, String parentId) {
		JsonResponseMsg result = new JsonResponseMsg();

		if (StringUtils.isNotBlank(id)) { // 编辑
			ActivityServer activityServer = activityItemServerService.findById(NumberUtils.toLong(id));
			if (null != activityServer) {
				if (StringUtils.isNotBlank(parentId)) { //服务器
					if (StringUtils.isNotBlank(serverName)) {
						activityServer.setServerName(serverName);
					}
				} else { //游戏区
					if (StringUtils.isNotBlank(parentName)) {
						activityServer.setServerName(parentName);
					}
				}
				activityServer.setUpdateDate(new Date());
				activityItemServerService.saveOrUpdate(activityServer);
				result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
			}
		} else { //新增
			if (StringUtils.isNotBlank(itemId)) {
				ActivityServer serverNew = new ActivityServer();
				if (StringUtils.isNotBlank(parentId)) { //服务器
					if (activityItemServerService.judgeServerExist(serverName, Long.parseLong(itemId),
							Long.parseLong(parentId))) {
						result.fill(CommonConstant.CODE_ERROR_LOGIC, "本游戏区中服务器重名");
						return result;
					}
					serverNew.setParentServerId(NumberUtils.toLong(parentId));
					serverNew.setServerName(serverName);
				} else { //游戏区
					List<Map<String, Object>> judge = activityItemServerService.judgeParentServerExist(parentName,
							Long.parseLong(itemId));
					if (!Collections.EMPTY_LIST.equals(judge)) {
						if (null != judge.get(0)) {
							Map<String, Object> map = judge.get(0);
							if (map.get("valid").equals("1")) {
								result.fill(CommonConstant.CODE_ERROR_LOGIC, "该游戏下游戏区重名");
								return result;
							} else {
								if (null != map.get("id")) {
									serverNew = activityItemServerService.findById((Long) map.get("id"));
								}
							}
						}
					}
					serverNew.setParentServerId((long) 0);
					serverNew.setServerName(parentName);
				}
				serverNew.setItemId(NumberUtils.toLong(itemId));
				serverNew.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				serverNew.setCreateDate(new Date());
				serverNew.setServerRequired(CommonConstant.INT_BOOLEAN_TRUE);
				activityItemServerService.saveOrUpdate(serverNew);
			} else {
				result.fill(CommonConstant.CODE_ERROR_LOGIC, CommonConstant.MSG_ERROR_LOGIC);
				return result;
			}
		}

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("/delete/{itemId}")
	public JsonResponseMsg delete(@PathVariable("itemId") long itemId) {
		JsonResponseMsg result = new JsonResponseMsg();
		activityItemServerService.deleteById(itemId);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}
}
