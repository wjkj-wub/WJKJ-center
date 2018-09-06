package com.miqtech.master.admin.web.controller.api.pc.lol;

import com.miqtech.master.admin.web.controller.api.BaseController;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.service.pc.lol.LolHeroRoleService;
import com.miqtech.master.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 英雄联盟英雄相关接口
 */
@Controller
@RequestMapping("/api/lol/heroRole")
public class LolHeroRoleController extends BaseController {

	@Autowired
	private LolHeroRoleService lolHeroRoleService;
	@Autowired
	private StringRedisOperateService stringRedisOperateService;

	private static final String heroRoleKey = "confront_lol_heroRoleKey";
	private static final Logger LOGGER = LoggerFactory.getLogger(LolHeroRoleController.class);

	@RequestMapping("list")
	@ResponseBody
	public JsonResponseMsg list(HttpServletRequest req, HttpServletResponse res) {
		JsonResponseMsg result = new JsonResponseMsg();
		String listJson = stringRedisOperateService.getData(heroRoleKey);
		String tagCn = req.getParameter("tag_cn");
		if (StringUtils.isBlank(listJson)) {
			List<Map<String, Object>> list = lolHeroRoleService.getLolHeroRoleListByTag(tagCn);
			listJson = JsonUtils.objectToString(list);
			stringRedisOperateService.setData(heroRoleKey, listJson);
			return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, list);
		}
		try {
			List<Map<String, Object>> list = JsonUtils.stringToCollection(listJson, ArrayList.class, HashMap.class);
			if (StringUtils.isNotBlank(tagCn)) {
				list = list.stream().filter(a -> a.get("tag_cn").toString().equals(tagCn)).collect(Collectors.toList());
			}
			return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, list);
		} catch (Exception e) {
			LOGGER.error("英雄列表String转list出错");
		}
		return null;
	}

	/**
	 * 根据英雄ID获取英雄头像
	 */
	@RequestMapping("/icon")
	@ResponseBody
	public JsonResponseMsg getHeroIconById(Long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (id == null || id.intValue() == 0) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}

		String icon = lolHeroRoleService.getHeroIconById(id);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, icon);
	}
}
