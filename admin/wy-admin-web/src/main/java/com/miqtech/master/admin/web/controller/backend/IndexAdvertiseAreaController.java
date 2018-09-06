package com.miqtech.master.admin.web.controller.backend;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Maps;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.common.IndexAdvertiseArea;
import com.miqtech.master.service.index.IndexAdvertiseAreaService;
import com.miqtech.master.service.system.SystemAreaService;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

@Controller
@RequestMapping("indexAdvertiseArea/")
public class IndexAdvertiseAreaController extends BaseController {
	@Autowired
	private IndexAdvertiseAreaService indexAdvertiseAreaService;
	@Autowired
	private SystemAreaService systemAreaService;
	/**
	 * 广告的地区记录
	 */
	@RequestMapping("/list/{page}")
	public ModelAndView areas(@PathVariable("page") int page, String adId, String valid, String title) {
		ModelAndView mv = new ModelAndView("activity/advertiseAreaList");
		int validInt = 1;
		if (StringUtils.isNotBlank(valid)) {
			validInt = NumberUtils.toInt(valid);
		}
		PageVO pageVO = indexAdvertiseAreaService.getAreasByAdId(NumberUtils.toLong(adId), page, validInt);
		pageModels(mv, pageVO.getList(), page, pageVO.getTotal());
		Map<String, Object> params = Maps.newHashMap();
		params.put("valid", validInt);
		params.put("title", title);
		params.put("adId", adId);
		mv.addObject("params", params);
		mv.addObject("provinceList", systemAreaService.queryAllRoot());

		return mv;
	}

	/**
	 * 地区编辑、添加
	 */
	@ResponseBody
	@RequestMapping("/save")
	public JsonResponseMsg save(HttpServletRequest req, IndexAdvertiseArea indexAdvertiseArea) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (indexAdvertiseArea != null) {
			if (indexAdvertiseArea.getId() != null) { //编辑
				IndexAdvertiseArea indexAdvertiseAreaUpdate = indexAdvertiseAreaService
						.findById(indexAdvertiseArea.getId());
				if (indexAdvertiseAreaUpdate == null) {
					result.fill(CommonConstant.CODE_ERROR_LOGIC, "不存在该地区记录");
					return result;
				}
				if (StringUtils.isNotBlank(indexAdvertiseArea.getAreaCode())) {
					//判断记录是否存在
					Map<String, Object> record = indexAdvertiseAreaService
							.queryRecord(indexAdvertiseAreaUpdate.getAdvertiseId(), indexAdvertiseArea.getAreaCode());
					if (record == null) {
						indexAdvertiseAreaUpdate.setAreaCode(indexAdvertiseArea.getAreaCode());
						indexAdvertiseAreaUpdate.setValid(CommonConstant.INT_BOOLEAN_TRUE);
						indexAdvertiseAreaUpdate.setUpdateDate(new Date());
					} else if (record.get("valid") != null && record.get("valid").equals("1")) {
								result.fill(CommonConstant.CODE_ERROR_LOGIC, "修改的地区记录已存在");
								return result;
					} else {
						indexAdvertiseAreaUpdate.setValid(CommonConstant.INT_BOOLEAN_TRUE);
						indexAdvertiseAreaUpdate.setUpdateDate(new Date());
					}
				}
				indexAdvertiseAreaService.save(indexAdvertiseAreaUpdate);
				result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
			} else { //添加
				if (indexAdvertiseArea.getAdvertiseId() == null
						|| StringUtils.isBlank(indexAdvertiseArea.getAreaCode())) {
					result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
					return result;
				}
				//判断记录是否存在
				Map<String, Object> record = indexAdvertiseAreaService.queryRecord(indexAdvertiseArea.getAdvertiseId(),
						indexAdvertiseArea.getAreaCode());
				if (record == null) {
					IndexAdvertiseArea indexAdvertiseAreaNew = new IndexAdvertiseArea();
					indexAdvertiseAreaNew.setAdvertiseId(indexAdvertiseArea.getAdvertiseId());
					indexAdvertiseAreaNew.setAreaCode(indexAdvertiseArea.getAreaCode());
					indexAdvertiseAreaNew.setValid(CommonConstant.INT_BOOLEAN_TRUE);
					indexAdvertiseAreaNew.setCreateDate(new Date());
					indexAdvertiseAreaService.save(indexAdvertiseAreaNew);
				} else if (record.get("valid") != null && record.get("valid").equals("1")) {
							result.fill(CommonConstant.CODE_ERROR_PARAM, "该地区记录已存在，请选择其他地区");
							return result;
				} else {
					IndexAdvertiseArea indexAdvertiseAreaRecord = indexAdvertiseAreaService
							.findById(NumberUtils.toLong(record.get("id") == null ? "" : record.get("id").toString()));
					if (indexAdvertiseAreaRecord != null) {
						indexAdvertiseAreaRecord.setValid(CommonConstant.INT_BOOLEAN_TRUE);
						indexAdvertiseAreaRecord.setUpdateDate(new Date());
						indexAdvertiseAreaService.save(indexAdvertiseAreaRecord);
					}
				}
			}
		} else {
			result.fill(CommonConstant.CODE_ERROR_LOGIC, CommonConstant.MSG_ERROR_LOGIC);
			return result;
		}

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 删除/恢复
	 */
	@RequestMapping("updateValid")
	@ResponseBody
	public JsonResponseMsg updateValid(long id, int valid) {
		JsonResponseMsg result = new JsonResponseMsg();
		IndexAdvertiseArea indexAdvertiseArea = indexAdvertiseAreaService.findById(id);
		if (indexAdvertiseArea != null) {
			if (valid == 0 || valid == 1) {
				indexAdvertiseArea.setValid(valid);
				indexAdvertiseArea.setUpdateDate(new Date());
				indexAdvertiseAreaService.save(indexAdvertiseArea);
			}
		}
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}
}
