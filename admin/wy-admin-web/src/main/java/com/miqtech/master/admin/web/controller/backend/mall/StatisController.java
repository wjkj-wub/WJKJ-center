package com.miqtech.master.admin.web.controller.backend.mall;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.service.mall.CoinHistoryService;
import com.miqtech.master.service.mall.MallInviteService;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.JsonUtils;

@Controller
@RequestMapping("mall/statis")
public class StatisController extends BaseController {

	@Autowired
	private CoinHistoryService coinHistoryService;
	@Autowired
	private MallInviteService mallInviteService;

	/**
	 * 统计金币情况
	 */
	@RequestMapping("report/coin")
	public ModelAndView coinStatis(String beginDate, String endDate, String threshold, String type, String limit,
			String today) {
		ModelAndView mv = new ModelAndView("mall/coinReport");

		if (CommonConstant.INT_BOOLEAN_TRUE.toString().equals(today)) {
			String now = DateUtils.dateToString(new Date(), DateUtils.YYYY_MM_DD);
			beginDate = endDate = now;
		}

		Integer thresholdInt = null;
		if (NumberUtils.isNumber(threshold)) {
			thresholdInt = NumberUtils.toInt(threshold);
		}
		List<Map<String, Object>> statis = coinHistoryService.statisUserCoinHistory(beginDate, endDate, thresholdInt,
				type, NumberUtils.toInt(limit));
		mv.addObject("statis", JsonUtils.objectToString(statis));

		Map<String, Object> params = Maps.newHashMap();
		params.put("beginDate", beginDate);
		params.put("endDate", endDate);
		params.put("threshold", threshold);
		params.put("type", type);
		params.put("limit", limit);
		mv.addObject("params", params);

		return mv;
	}

	/**
	 * 统计邀请情况
	 */
	@RequestMapping("report/invite")
	public ModelAndView inviteStatis(String beginDate, String endDate, String threshold, String order, String limit,
			String today) {
		ModelAndView mv = new ModelAndView("mall/inviteReport");

		if (CommonConstant.INT_BOOLEAN_TRUE.toString().equals(today)) {
			String now = DateUtils.dateToString(new Date(), DateUtils.YYYY_MM_DD);
			beginDate = endDate = now;
		}

		Integer thresholdInt = null;
		if (NumberUtils.isNumber(threshold)) {
			thresholdInt = NumberUtils.toInt(threshold);
		}

		List<Map<String, Object>> statis = mallInviteService.statis(beginDate, endDate, thresholdInt, order,
				NumberUtils.toInt(limit));
		mv.addObject("statis", JsonUtils.objectToString(statis));

		Map<String, Object> params = Maps.newHashMap();
		params.put("beginDate", beginDate);
		params.put("endDate", endDate);
		params.put("threshold", thresholdInt);
		params.put("order", order);
		params.put("limit", limit);
		mv.addObject("params", params);

		return mv;
	}
}
