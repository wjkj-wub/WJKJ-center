package com.miqtech.master.admin.web.controller.backend;

import com.google.common.collect.Maps;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.common.NoticeArea;
import com.miqtech.master.entity.common.SystemMerchantNotice;
import com.miqtech.master.service.system.NoticeAreaService;
import com.miqtech.master.service.system.SystemAreaService;
import com.miqtech.master.service.system.SystemMerchantNoticeService;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("merchantNotice")
public class MerchantNoticeController extends BaseController {

	@Autowired
	private SystemMerchantNoticeService systemMerchantNoticeService;
	@Autowired
	private SystemAreaService systemAreaService;
	@Autowired
	private NoticeAreaService noticeAreaService;

	/**
	 * 分页
	 */
	@RequestMapping("list/{page}")
	public ModelAndView list(@PathVariable("page") int page, String title, String beginDate, String endDate) {
		ModelAndView mv = new ModelAndView("sys/merchantNoticeList");

		Map<String, Object> params = new HashMap<String, Object>(16);
		if (StringUtils.isNotBlank(title)) {
			params.put("title", title);
		}
		if (StringUtils.isNotBlank(beginDate)) {
			params.put("beginDate", beginDate);
		}
		if (StringUtils.isNotBlank(endDate)) {
			params.put("endDate", endDate);
		}

		List<Map<String, Object>> list = systemMerchantNoticeService.pageList(page, params);
		pageModels(mv, list, page, NumberUtils.toInt(params.get("total").toString()));
		mv.addObject("params", params);
		mv.addObject("provinceList", systemAreaService.queryAllRoot());
		return mv;
	}

	/**
	 * 详情
	 */
	@ResponseBody
	@RequestMapping("detail/{id}")
	public JsonResponseMsg detail(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		SystemMerchantNotice notice = systemMerchantNoticeService.findById(id);
		List<Map<String, Object>> areaCodes = systemMerchantNoticeService.getAreaCodesByNoticeId(id);

		Map<String, Object> map = Maps.newHashMap();
		map.put("notice", notice);
		map.put("areaCodes", areaCodes);

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, map);
		return result;
	}

	/**
	 * 新增/编辑
	 */
	@ResponseBody
	@RequestMapping("save")
	public JsonResponseMsg save(SystemMerchantNotice systemMerchantNotice, HttpServletRequest request) {
		JsonResponseMsg result = new JsonResponseMsg();

		if (systemMerchantNotice != null) {
			Date date = new Date();
			systemMerchantNotice.setUpdateDate(date);
			if (systemMerchantNotice.getId() != null) {
				SystemMerchantNotice old = systemMerchantNoticeService.findById(systemMerchantNotice.getId());
				systemMerchantNotice = BeanUtils.updateBean(old, systemMerchantNotice);
			} else {
				systemMerchantNotice.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				systemMerchantNotice.setCreateDate(date);
			}
			systemMerchantNoticeService.save(systemMerchantNotice);

			long noticeId = systemMerchantNotice.getId();
			String areaCode = request.getParameter("areaCode"); //全国
			if (StringUtils.isNotBlank(areaCode)) {
				saveOrUpdateNoticeArea(areaCode, noticeId);
			}

			for (int i = 0; i < 34; i++) { //省份
				String areaCodeN = request.getParameter("areaCode" + i);
				if (StringUtils.isNotBlank(areaCodeN)) {
					saveOrUpdateNoticeArea(areaCodeN, noticeId);
				}
			}

		}
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);

		return result;
	}

	/**
	 * 保存或更新公告地区
	 */
	private void saveOrUpdateNoticeArea(String areaCode, long noticeId) {
		if (StringUtils.isNotBlank(areaCode)) { //全国
			NoticeArea noticeArea = noticeAreaService.findByNoticeIdAndAreaCode(noticeId, areaCode);
			if (null == noticeArea) {
				NoticeArea noticeAreaNew = new NoticeArea();
				noticeAreaNew.setNoticeId(noticeId);
				noticeAreaNew.setAreaCode(areaCode);
				noticeAreaNew.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				noticeAreaNew.setCreateDate(new Date());
				noticeAreaService.save(noticeAreaNew);
			} else if (!noticeArea.getValid().equals(CommonConstant.INT_BOOLEAN_TRUE)) {
				noticeArea.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				noticeArea.setUpdateDate(new Date());
				noticeAreaService.save(noticeArea);
			}
		}
	}

	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("delete/{id}")
	public JsonResponseMsg delete(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();

		SystemMerchantNotice systemMerchantNotice = systemMerchantNoticeService.findById(id);
		if (null != systemMerchantNotice) {
			systemMerchantNotice.setValid(CommonConstant.INT_BOOLEAN_FALSE);
			systemMerchantNotice.setUpdateDate(new Date());
			systemMerchantNoticeService.save(systemMerchantNotice);
		}
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);

		return result;
	}
}
