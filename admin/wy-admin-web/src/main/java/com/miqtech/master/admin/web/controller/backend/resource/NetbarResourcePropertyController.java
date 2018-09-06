package com.miqtech.master.admin.web.controller.backend.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.netbar.resource.NetbarCommodityCategory;
import com.miqtech.master.service.netbar.resource.NetbarResourcePropertyService;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.JsonUtils;

@Controller
@RequestMapping("netbar/resource/property")
public class NetbarResourcePropertyController extends BaseController {
	@Autowired
	private NetbarResourcePropertyService netbarResourcePropertyService;

	@RequestMapping("list")
	public String page(Model model) {
		model.addAttribute("ratio", netbarResourcePropertyService.netbarResourceAreaRaitoList());
		model.addAttribute("parent", netbarResourcePropertyService.findByValidAndPid(1, 0));
		model.addAttribute("category", netbarResourcePropertyService.netbarCommodityCategoryList());
		return "resource/propertyList";
	}

	@ResponseBody
	@RequestMapping("updateRatio")
	public int updateRatio(String areaCode, Float vip_ratio, Float gold_ratio, Float jewel_ratio) {
		netbarResourcePropertyService.updateRatio(areaCode, vip_ratio, gold_ratio, jewel_ratio);
		return 1;
	}

	@RequestMapping("save")
	@ResponseBody
	public String save(NetbarCommodityCategory category) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (StringUtils.isBlank(category.getName())) {
			return JsonUtils.objectToString(result.fill(-2, "类别名称不能为空"));
		}
		if (category.getName().equals("红包")) {
			return JsonUtils.objectToString(result.fill(-1, "不能新增红包类别"));
		}
		if (category.getName().equals("增值券")) {
			return JsonUtils.objectToString(result.fill(-1, "不能新增增值券类别"));
		}
		category.setIsShowApp(category.getIsShowApp() == null ? 0 : category.getIsShowApp());
		if (category.getId() == null) {
			category.setValid(1);
			category.setCreateDate(new Date());
			netbarResourcePropertyService.saveNetbarCommodityCategory(category);
		} else {
			NetbarCommodityCategory old = netbarResourcePropertyService.findCategoryById(category.getId());
			BeanUtils.updateBean(old, category);
			netbarResourcePropertyService.saveNetbarCommodityCategory(old);
		}
		return JsonUtils.objectToString(result);
	}

	@RequestMapping("detail")
	@ResponseBody
	public String detail(Long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		result.setObject(netbarResourcePropertyService.findCategoryById(id));
		return JsonUtils.objectToString(result);
	}

	@RequestMapping("del/{id}")
	@ResponseBody
	public String del(@PathVariable Long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		netbarResourcePropertyService.del(id);
		return JsonUtils.objectToString(result);
	}

	@RequestMapping(value = "trend")
	@ResponseBody
	public Map<String, Object> trend(String areaCode) {
		List<Map<String, Object>> list = netbarResourcePropertyService.trend(areaCode);
		List<String> categories = new ArrayList<String>();
		Map<String, Object> vip = new HashMap<String, Object>();
		List<Float> vipData = new ArrayList<Float>();
		Map<String, Object> gold = new HashMap<String, Object>();
		List<Float> goldData = new ArrayList<Float>();
		Map<String, Object> jewel = new HashMap<String, Object>();
		//		List<Float> jewelData = new ArrayList<Float>();
		for (Map<String, Object> map : list) {
			categories.add((String) map.get("create_date"));
			vipData.add(((Number) map.get("vip_ratio")).floatValue());
			goldData.add(((Number) map.get("gold_ratio")).floatValue());
			//jewelData.add(((Number) map.get("jewel_ratio")).floatValue());
		}
		vip.put("name", "会员");
		vip.put("data", vipData);
		gold.put("name", "金牌");
		gold.put("data", goldData);
		//		jewel.put("name", "钻石");
		//		jewel.put("data", jewelData);
		List<Map<String, Object>> series = new ArrayList<Map<String, Object>>();
		series.add(vip);
		series.add(gold);
		series.add(jewel);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("categories", categories);
		result.put("series", series);
		result.put("status", "success");
		return result;
	}
}
