package com.miqtech.master.admin.web.controller.backend.award;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.OperateLogConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.award.AwardInventory;
import com.miqtech.master.entity.common.OperateLog;
import com.miqtech.master.entity.common.SystemUser;
import com.miqtech.master.service.award.AwardCommodityService;
import com.miqtech.master.service.award.AwardInventoryService;
import com.miqtech.master.service.common.OperateLogService;
import com.miqtech.master.vo.PageVO;

@Controller
@RequestMapping("award/commodity")
public class AwardCommodityController extends BaseController {

	@Autowired
	private AwardInventoryService awardInventoryService;
	@Autowired
	private AwardCommodityService awardCommodityService;
	@Autowired
	private OperateLogService operateLogService;

	/**
	 * 分页
	 */
	@ResponseBody
	@RequestMapping("list/{page}")
	public ModelAndView list(@PathVariable("page") int page, String inventoryId, String isUsed) {
		ModelAndView mv = new ModelAndView("award/commodityList");

		if (!NumberUtils.isNumber(isUsed)) {
			isUsed = "1";
		}
		if (!NumberUtils.isNumber(inventoryId)) {
			inventoryId = "0";
		}

		Long inventoryIdLong = NumberUtils.toLong(inventoryId);
		AwardInventory inventory = awardInventoryService.findById(inventoryIdLong);
		mv.addObject("inventory", inventory);

		Map<String, String> params = Maps.newHashMap();
		params.put("inventoryId", inventoryId);
		params.put("isUsed", isUsed);
		mv.addObject("params", params);

		PageVO vo = awardCommodityService.page(page, params);
		pageModels(mv, vo.getList(), page, vo.getTotal());

		return mv;
	}

	/**
	 * 删除(支持批量)
	 */
	@ResponseBody
	@RequestMapping("delete")
	public JsonResponseMsg delete(HttpServletRequest req, String ids) {
		JsonResponseMsg result = new JsonResponseMsg();

		SystemUser sysUser = Servlets.getSessionUser(req);
		Long sysUserId = sysUser.getId();

		// 组装ID列表
		Date now = new Date();
		List<Long> idLongs = Lists.newArrayList();
		List<OperateLog> operateLogs = Lists.newArrayList();
		if (StringUtils.isNotBlank(ids)) {
			String[] split = ids.split(",");
			if (ArrayUtils.isNotEmpty(split)) {
				for (String s : split) {
					if (NumberUtils.isNumber(s)) {
						long idLong = NumberUtils.toLong(s);
						idLongs.add(idLong);

						// 后台日志
						OperateLog grantLog = new OperateLog();
						grantLog.setSysType(OperateLogConstant.SYS_TYPE_ADMIN);
						grantLog.setSysUserId(sysUserId);
						grantLog.setThirdId(idLong);
						grantLog.setType(OperateLogConstant.TYPE_ADMIN_INVENTORY_COMMODITY_DELETE);
						grantLog.setInfo("删除库存商品");
						grantLog.setValid(CommonConstant.INT_BOOLEAN_TRUE);
						grantLog.setUpdateDate(now);
						grantLog.setCreateDate(now);
						operateLogs.add(grantLog);
					}
				}
			}
		}

		awardCommodityService.delete(idLongs);
		operateLogService.save(operateLogs);

		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 * 根据商品ID获取可用的cdkey
	 */
	@ResponseBody
	@RequestMapping("queryUsefullyCdkeysByInventoryId")
	public JsonResponseMsg queryUsefullyCdkeyByInventoryId(String inventoryId) {
		JsonResponseMsg result = new JsonResponseMsg();

		List<Map<String, Object>> cdkeys = null;
		if (NumberUtils.isNumber(inventoryId)) {
			cdkeys = awardCommodityService.queryUsefullyCdkeysByInventoryId(NumberUtils.toLong(inventoryId));
		}

		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, cdkeys);
	}
}
