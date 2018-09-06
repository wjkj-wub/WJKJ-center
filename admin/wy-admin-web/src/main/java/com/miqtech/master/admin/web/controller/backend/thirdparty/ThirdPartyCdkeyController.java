package com.miqtech.master.admin.web.controller.backend.thirdparty;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.miqtech.master.config.SystemConfig;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.thirdparty.ThirdPartyCdkey;
import com.miqtech.master.entity.thirdparty.ThirdPartyCdkeyCategory;
import com.miqtech.master.service.thirdparty.ThirdPartyCdkeyCategoryService;
import com.miqtech.master.service.thirdparty.ThirdPartyCdkeyService;
import com.miqtech.master.utils.ExcelUtils;
import com.miqtech.master.vo.PageVO;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

@Controller
@RequestMapping("thirdparty/cdkey")
public class ThirdPartyCdkeyController extends BaseController {

	@Autowired
	private SystemConfig systemConfig;
	@Autowired
	private ThirdPartyCdkeyCategoryService thirdPartyCdkeyCategoryService;
	@Autowired
	private ThirdPartyCdkeyService thirdPartyCdkeyService;

	/**
	 * 第三方类目列表
	 */
	@RequestMapping("category/list/{page}")
	public ModelAndView categoryList(@PathVariable("page") int page, String name) {
		ModelAndView mv = new ModelAndView("thirdparty/categoryList");

		Map<String, String> params = Maps.newHashMap();
		params.put("name", name);
		mv.addObject("params", params);

		PageVO vo = thirdPartyCdkeyCategoryService.page(page, params);
		pageModels(mv, vo.getList(), page, vo.getTotal());

		String appDomain = systemConfig.getAppDomain();
		mv.addObject("appDomain", appDomain);

		return mv;
	}

	/**
	 * 编辑
	 */
	@RequestMapping("category/edit")
	public ModelAndView categoryEdit(String id) {
		ModelAndView mv = new ModelAndView("thirdparty/categoryEdit");

		if (NumberUtils.isNumber(id)) {
			ThirdPartyCdkeyCategory editObj = thirdPartyCdkeyCategoryService.findById(NumberUtils.toLong(id));
			mv.addObject("editObj", editObj);
		}

		return mv;
	}

	/**
	 * 保存类目
	 */
	@ResponseBody
	@RequestMapping("category/save")
	public JsonResponseMsg save(ThirdPartyCdkeyCategory category) {
		if (category == null || StringUtils.isBlank(category.getName())) {
			return new JsonResponseMsg().fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}

		String name = category.getName();
		List<ThirdPartyCdkeyCategory> categories = thirdPartyCdkeyCategoryService.findValidByName(name);
		if (CollectionUtils.isNotEmpty(categories)) {
			return new JsonResponseMsg().fill(CommonConstant.CODE_ERROR_LOGIC, "名称已存在,请更改后重试");
		}

		category = thirdPartyCdkeyCategoryService.saveOrUpdate(category);
		return new JsonResponseMsg().fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, category);
	}

	/**
	 * cdkey分页列表
	 */
	@RequestMapping("list/{page}")
	public ModelAndView list(@PathVariable("page") int page, String categoryId, String cdkey, String isUsed) {
		ModelAndView mv = new ModelAndView("thirdparty/cdkeyList");

		Map<String, String> params = Maps.newHashMap();
		params.put("categoryId", categoryId);
		params.put("cdkey", cdkey);
		params.put("isUsed", isUsed);
		mv.addObject("params", params);

		PageVO vo = thirdPartyCdkeyService.page(page, params);
		pageModels(mv, vo.getList(), page, vo.getTotal());

		return mv;
	}

	/**
	 * 删除cdkey
	 */
	@ResponseBody
	@RequestMapping("delete")
	public JsonResponseMsg delete(String id) {
		if (NumberUtils.isNumber(id)) {
			long idLong = NumberUtils.toLong(id);
			thirdPartyCdkeyService.delete(idLong);
		}
		return new JsonResponseMsg().fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 * cdkey导入
	 */
	@ResponseBody
	@RequestMapping("import")
	public JsonResponseMsg importCdkey(HttpServletRequest req, String categoryId) {
		if (!NumberUtils.isNumber(categoryId)) {
			return new JsonResponseMsg().fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}

		MultipartFile file = Servlets.getMultipartFile(req, "file");
		Workbook excel = ExcelUtils.readMultipartFile(file);

		if (excel == null) {
			return new JsonResponseMsg().fill(CommonConstant.CODE_ERROR_PARAM, "缺少文件");
		}

		// 将表格中的cdkey转化为对象
		List<ThirdPartyCdkey> cdkeys = Lists.newArrayList();
		Sheet sheet = excel.getSheet(0);
		int rows = sheet.getRows();
		if (rows > 1) {
			Long categoryIdLong = NumberUtils.toLong(categoryId);
			Date now = new Date();
			for (int r = 1; r < rows; r++) {
				Cell[] cells = sheet.getRow(r);
				if (ArrayUtils.isNotEmpty(cells) && cells.length >= 1) {
					String cdkey = cells[0].getContents();
					if (StringUtils.isNotBlank(cdkey)) {
						ThirdPartyCdkey thirdPartyCdkey = new ThirdPartyCdkey();
						thirdPartyCdkey.setCategoryId(categoryIdLong);
						thirdPartyCdkey.setCdkey(cdkey.trim());
						thirdPartyCdkey.setIsUsed(CommonConstant.INT_BOOLEAN_FALSE);
						thirdPartyCdkey.setValid(CommonConstant.INT_BOOLEAN_TRUE);
						thirdPartyCdkey.setUpdateDate(now);
						thirdPartyCdkey.setCreateDate(now);
						cdkeys.add(thirdPartyCdkey);
					}
				}
			}

			thirdPartyCdkeyService.save(cdkeys);
		}

		return new JsonResponseMsg().fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}
}
