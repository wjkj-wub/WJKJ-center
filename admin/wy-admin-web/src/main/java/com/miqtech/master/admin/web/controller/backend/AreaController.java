package com.miqtech.master.admin.web.controller.backend;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.common.SystemArea;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.service.system.SystemAreaService;
import com.miqtech.master.utils.AreaUtil;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.JsonUtils;
import com.miqtech.master.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("area")
public class AreaController extends BaseController {

	private static int IN_GEN_PINYIN_OPERATE = CommonConstant.INT_BOOLEAN_FALSE;// 产生地区拼音操作标识，避免多用户同时操作

	@Autowired
	private SystemAreaService systemAreaService;
	@Autowired
	private StringRedisOperateService stringRedisOperateService;

	/**
	 * 地区树 数据
	 */
	@ResponseBody
	@RequestMapping("tree")
	public JsonResponseMsg tree(String onlyValidArea, String areaCodes) {
		JsonResponseMsg result = new JsonResponseMsg();

		if (StringUtils.isBlank(onlyValidArea)) {
			onlyValidArea = CommonConstant.INT_BOOLEAN_TRUE.toString();
		}

		boolean onlyValid = CommonConstant.INT_BOOLEAN_TRUE.toString().equals(onlyValidArea);
		if (StringUtils.isBlank(areaCodes)) {
			result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, systemAreaService.getTree(onlyValid));
		} else {
			result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS,
					systemAreaService.getTree(areaCodes, onlyValid));
		}
		return result;
	}

	/**
	 * 地区列表
	 */
	@RequestMapping("list")
	public ModelAndView list(String onlyValidArea) {
		ModelAndView mv = new ModelAndView("sys/area");

		if (StringUtils.isBlank(onlyValidArea)) {
			onlyValidArea = CommonConstant.INT_BOOLEAN_TRUE.toString();
		}

		boolean onlyValid = CommonConstant.INT_BOOLEAN_TRUE.toString().equals(onlyValidArea);
		List<SystemArea> areas = systemAreaService.getTree(onlyValid);
		mv.addObject("areas", JsonUtils.objectToString(areas));
		mv.addObject("onlyValidArea", onlyValidArea);
		return mv;
	}

	/**
	 * 查询地区数据
	 */
	@ResponseBody
	@RequestMapping("detail/{id}")
	public JsonResponseMsg detail(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();

		SystemArea area = systemAreaService.findById(id);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, area);

		return result;
	}

	/**
	 * 新增 或 更新 地区数据
	 */
	@ResponseBody
	@RequestMapping("save")
	public JsonResponseMsg save(SystemArea area) {
		JsonResponseMsg result = new JsonResponseMsg();

		if (area != null) {
			Date today = new Date();
			area.setUpdateDate(today);
			if (area.getId() != null) {
				SystemArea old = systemAreaService.findById(area.getId());
				if (old != null) {
					area = BeanUtils.updateBean(old, area);
				}
			} else {
				area.setCreateDate(today);
				area.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			}
			area = systemAreaService.save(area);
		}
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, area);

		return result;
	}

	/**
	 * 删除地区
	 */
	@ResponseBody
	@RequestMapping("delete/{id}")
	public JsonResponseMsg delete(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		systemAreaService.delete(id);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 更新所有有效地区的拼音属性
	 */
	@ResponseBody
	@RequestMapping("updatePinyin")
	public JsonResponseMsg udpatePinyin() {
		JsonResponseMsg result = new JsonResponseMsg();

		// 因操作量较大，全局同时只允许一次请求
		if (CommonConstant.INT_BOOLEAN_TRUE.equals(IN_GEN_PINYIN_OPERATE)) {
			result.fill(CommonConstant.CODE_ERROR_LOGIC, "系统正在更新地区拼音数据中，请勿短时间内重复此操作");
			return result;
		}

		IN_GEN_PINYIN_OPERATE = CommonConstant.INT_BOOLEAN_TRUE;
		systemAreaService.updatePinyin();
		IN_GEN_PINYIN_OPERATE = CommonConstant.INT_BOOLEAN_FALSE;
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);

		return result;
	}

	/**
	 * 更新redis里的缓存信息
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping("updateAreaCodeInRedis")
	public JsonResponseMsg updateAreaCodeInRedis() {
		JsonResponseMsg result = new JsonResponseMsg();
		List<SystemArea> list = systemAreaService.findAll();
		for (SystemArea obj : list) {
			stringRedisOperateService.setData(CommonConstant.AREA_KEY + obj.getAreaCode(), obj.getName());
		}
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 开通地区
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping("open")
	public JsonResponseMsg open(Long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		SystemArea area = systemAreaService.findById(id);
		systemAreaService.open(AreaUtil.getAreaCode(area.getAreaCode()));
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 关闭地区
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping("close")
	public JsonResponseMsg close(Long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		SystemArea area = systemAreaService.findById(id);
		systemAreaService.close(AreaUtil.getAreaCode(area.getAreaCode()));
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 赛事选择下拉框地区数据获取
	 */
	@RequestMapping("/areaInfo")
	@ResponseBody
	public JsonResponseMsg getAreaInfo(String areaCode) {
		JsonResponseMsg result = new JsonResponseMsg();
		// 获取一级地区信息
		if (StringUtils.isBlank(areaCode)) {
			return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS,
					systemAreaService.queryValidRoot());
		}
		// 获取二级地区信息
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS,
				systemAreaService.queryValidChildren(areaCode));
	}
}
