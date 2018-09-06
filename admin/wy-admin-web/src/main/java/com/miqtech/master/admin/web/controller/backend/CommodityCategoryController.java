package com.miqtech.master.admin.web.controller.backend;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Maps;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.award.AwardConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.mall.CommodityCategory;
import com.miqtech.master.service.mall.CommodityCategoryService;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

@Controller
@RequestMapping("commodityCategory")
public class CommodityCategoryController extends BaseController {

	@Autowired
	private CommodityCategoryService commodityCategoryService;

	/**
	 * 分页列表
	 */
	@RequestMapping("list/{page}")
	public ModelAndView page(@PathVariable("page") int page, String name) {
		ModelAndView mv = new ModelAndView("mall/commodityCategoryList");

		Map<String, Object> params = Maps.newHashMap();
		if (StringUtils.isNotBlank(name)) {
			params.put("name", name);
		}
		mv.addObject("params", params);

		PageVO vo = commodityCategoryService.listPage(page, PageUtils.ADMIN_DEFAULT_PAGE_SIZE, params);
		pageModels(mv, vo.getList(), page, vo.getTotal());

		return mv;
	}

	/**
	 * 新增
	 */
	@ResponseBody
	@RequestMapping("save")
	public JsonResponseMsg save(CommodityCategory commodityCategory) {
		JsonResponseMsg result = new JsonResponseMsg();

		if (commodityCategory != null) {
			Date now = new Date();
			commodityCategory.setUpdateDate(now);
			if (commodityCategory.getId() != null) {
				CommodityCategory old = commodityCategoryService.getCommodityCategoryById(commodityCategory.getId());
				commodityCategory = BeanUtils.updateBean(old, commodityCategory);
				commodityCategory.setType(old.getId().intValue());
			} else {
				commodityCategory.setSuperType(AwardConstant.TYPE_REPERTORY);
				commodityCategory.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				commodityCategory.setCreateDate(now);
			}
			commodityCategoryService.save(commodityCategory);
		}

		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}
}
