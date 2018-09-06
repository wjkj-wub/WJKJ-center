package com.miqtech.master.admin.web.controller.api.pc.user;

import com.google.common.collect.Lists;
import com.miqtech.master.admin.web.controller.api.BaseController;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.service.pc.netbar.PcNetbarUserRetentionService;
import com.miqtech.master.service.pc.user.PcUserRetentionService;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.vo.PageVO;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("api/pc/userRetention")
public class PcUserRetentionController extends BaseController {

	@Resource
	private PcUserRetentionService pcUserRetentionService;
	@Resource
	private PcNetbarUserRetentionService pcNetbarUserRetentionService;

	/**
	 * 留存率分页列表
	 */
	@ResponseBody
	@RequestMapping("page")
	public JsonResponseMsg page(String page, String rows, String startDate, String endDate) {
		Integer pageInt = null;
		if (NumberUtils.isNumber(page)) {
			pageInt = NumberUtils.toInt(page);
		}
		Integer rowsInt = null;
		if (NumberUtils.isNumber(rows)) {
			rowsInt = NumberUtils.toInt(rows);
		}
		Long startDateLong = null;
		if (NumberUtils.isNumber(startDate)) {
			startDateLong = NumberUtils.toLong(startDate);
		}
		Long endDateLong = null;
		if (NumberUtils.isNumber(endDate)) {
			endDateLong = NumberUtils.toLong(endDate);
		}
		PageVO pager = pcUserRetentionService.getPager(pageInt, rowsInt, startDateLong, endDateLong);
		return new JsonResponseMsg().fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, pager);
	}

	/**
	 * 初始化用户留存信息
	 */
	@ResponseBody
	@RequestMapping("initUserRetention")
	public JsonResponseMsg initUserRetention() {
		pcUserRetentionService.updateAllRetetionRate();
		return new JsonResponseMsg().fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 * 初始化网吧留存信息
	 */
	@ResponseBody
	@RequestMapping("initNetbarUserRetention")
	public JsonResponseMsg initNetbarUserRetention(String beginDate, String endDate) {
		Date startDate = null;
		Date overDate = null;
		try {
			startDate = DateUtils.stringToDateYyyyMMdd(beginDate);
			overDate = DateUtils.stringToDateYyyyMMdd(endDate);
		} catch (Exception e) {
			return new JsonResponseMsg().fill(CommonConstant.CODE_ERROR_PARAM, "日期格式不正确");
		}

		// 组装日期参数
		List<String> dates = Lists.newArrayList();
		Calendar now = Calendar.getInstance();
		now.setTime(startDate);
		while (now.getTimeInMillis() < overDate.getTime()) {
			String nowStr = DateUtils.dateToString(now.getTime(), DateUtils.YYYY_MM_DD);
			dates.add(nowStr);

			now.add(Calendar.DATE, 1);
		}

		List<Long> netbarIds = pcNetbarUserRetentionService.getPromotionNetbarIds();
		pcNetbarUserRetentionService.updateRetentions(dates, netbarIds);
		return new JsonResponseMsg().fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}
}
