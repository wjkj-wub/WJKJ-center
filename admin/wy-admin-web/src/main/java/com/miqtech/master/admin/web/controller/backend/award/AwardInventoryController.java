package com.miqtech.master.admin.web.controller.backend.award;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.award.AwardCommodity;
import com.miqtech.master.entity.award.AwardInventory;
import com.miqtech.master.service.award.AwardCommodityService;
import com.miqtech.master.service.award.AwardInventoryService;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.ExcelUtils;
import com.miqtech.master.vo.PageVO;

@Controller
@RequestMapping("award/inventory")
public class AwardInventoryController extends BaseController {

	@Autowired
	private AwardInventoryService awardInventoryService;
	@Autowired
	private AwardCommodityService awardCommodityService;

	/**
	 * 分页列表
	 */
	@RequestMapping("list/{page}")
	public ModelAndView page(@PathVariable("page") int page, String name, String valid, String order) {
		ModelAndView mv = new ModelAndView("award/inventoryList");

		Map<String, String> searchParams = Maps.newHashMap();
		searchParams.put("name", name);
		searchParams.put("valid", valid);
		mv.addObject("params", searchParams);

		String orderCol = null;
		String orderType = "DESC";
		if (!"1".equals(order)) {
			orderCol = "unusedCount";
		} else {
			orderCol = "ai.import_time";
		}
		mv.addObject("order", order);

		PageVO vo = awardInventoryService.page(page, searchParams, orderCol, orderType);
		pageModels(mv, vo.getList(), page, vo.getTotal());

		return mv;
	}

	/**
	 * 查看详情
	 */
	@ResponseBody
	@RequestMapping("detail")
	public JsonResponseMsg detail(Long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		Map<String, Object> inventory = awardInventoryService.queryByIdWithTotalCountUnusedCount(id);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, inventory);
	}

	/**
	 * 新增或编辑
	 */
	@ResponseBody
	@RequestMapping("save")
	public JsonResponseMsg save(String startTimeStr, String endTimeStr, AwardInventory inventory) {
		JsonResponseMsg result = new JsonResponseMsg();
		try {
			inventory.setStartTime(DateUtils.stringToDate(startTimeStr, DateUtils.YYYY_MM_DD_HH_MM_SS));
		} catch (Exception expired) {
		}
		try {
			inventory.setEndTime(DateUtils.stringToDate(endTimeStr, DateUtils.YYYY_MM_DD_HH_MM_SS));
		} catch (Exception expired) {
		}
		awardInventoryService.insertOrUpdate(inventory);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 * 启用
	 */
	@ResponseBody
	@RequestMapping("enabled")
	public JsonResponseMsg abled(Long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		awardInventoryService.abled(id);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 * 禁用
	 */
	@ResponseBody
	@RequestMapping("disabled")
	public JsonResponseMsg disabled(Long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		awardInventoryService.disabled(id);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 * 导入
	 */
	@ResponseBody
	@RequestMapping("import")
	public JsonResponseMsg importCommodity(HttpServletRequest req, String id, String startTimeStr, String endTimeStr,
			AwardInventory inventory) {
		JsonResponseMsg result = new JsonResponseMsg();
		Long inventoryId = null;
		if (!NumberUtils.isNumber(id)) {// 先新增商品再导入
			try {
				inventory.setStartTime(DateUtils.stringToDate(startTimeStr, DateUtils.YYYY_MM_DD_HH_MM_SS));
			} catch (Exception expired) {
			}
			try {
				inventory.setEndTime(DateUtils.stringToDate(endTimeStr, DateUtils.YYYY_MM_DD_HH_MM_SS));
			} catch (Exception expired) {
			}
			inventory = awardInventoryService.insertOrUpdate(inventory);
			inventoryId = inventory.getId();
		} else {// 根据ID导入
			inventoryId = NumberUtils.toLong(id);
			inventory = awardInventoryService.findById(inventoryId);
			if (inventory == null) {
				return result.fill(CommonConstant.CODE_ERROR_LOGIC, "不存在商品");
			}
		}

		// 获取excel
		MultipartFile file = Servlets.getMultipartFile(req, "file");
		Workbook wb = ExcelUtils.readMultipartFile(file);
		if (wb == null) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}
		Sheet[] sheets = wb.getSheets();
		if (ArrayUtils.isEmpty(sheets) || sheets.length <= 0) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}

		// 读取工作表及行
		Sheet sheet = wb.getSheet(0);
		int rows = sheet.getRows();
		if (rows > 1) {
			Date now = new Date();
			inventory.setImportTime(now);

			List<AwardCommodity> commodities = Lists.newArrayList();
			for (int r = 1; r < rows; r++) {
				Cell[] cells = sheet.getRow(r);
				if (ArrayUtils.isNotEmpty(cells) && cells.length >= 3) {
					// 读取excel单元格内容
					String cdkey = cells[0].getContents();
					String sts = cells[1].getContents();
					String ets = cells[2].getContents();

					// 实例化商品,并加入到待保存列表中
					AwardCommodity c = new AwardCommodity();
					c.setInventoryId(inventoryId);
					c.setCdkey(cdkey);
					try {
						Date startTime = DateUtils.stringToDate(sts, DateUtils.YYYY_MM_DD_HH_MM_SS);
						c.setStartTime(startTime);
					} catch (Exception expect) {
					}
					try {
						Date endTime = DateUtils.stringToDate(ets, DateUtils.YYYY_MM_DD_HH_MM_SS);
						c.setEndTime(endTime);
					} catch (Exception expect) {
					}
					c.setIsUsed(CommonConstant.INT_BOOLEAN_FALSE);
					c.setValid(CommonConstant.INT_BOOLEAN_TRUE);
					c.setUpdateDate(now);
					c.setCreateDate(now);
					commodities.add(c);
				}
			}
			// 保存商品
			awardCommodityService.save(commodities);
			awardInventoryService.save(inventory);
		}

		// 组装返回结果
		Map<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("inventory", inventory);
		resultMap.put("importNum", rows - 1);

		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, resultMap);
	}

	/**
	 * 获取 商品 及 兑换码
	 */
	@ResponseBody
	@RequestMapping("getInventoryAndCdkey")
	public JsonResponseMsg getInventoryAndCdkey() {
		JsonResponseMsg result = new JsonResponseMsg();
		Map<String, Object> resultMap = Maps.newHashMap();

		// 查询可用的商品
		List<Map<String, Object>> inventories = awardInventoryService.queryUsefullyInventories();
		resultMap.put("inventories", inventories);

		// 查询首个商品下可用的cdkey
		List<Map<String, Object>> cdkeys = null;
		if (CollectionUtils.isNotEmpty(inventories)) {
			Long inventoryId = MapUtils.getLong(inventories.get(0), "id");
			cdkeys = awardCommodityService.queryUsefullyCdkeysByInventoryId(inventoryId);
		}
		resultMap.put("cdkeys", cdkeys);

		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, resultMap);
	}
}
