package com.miqtech.master.admin.web.controller.backend.lottery;

import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.service.lottery.LotteryHistoryService;

/**
 * 报表
 */
@Controller
@RequestMapping("lottery/report")
public class LotteryReportController extends BaseController {

	@Autowired
	private LotteryHistoryService lotteryHistoryService;

	/**
	 * 请求页面
	 */
	@RequestMapping("history/{id}")
	public ModelAndView history(@PathVariable("id") long lotteryId) {
		ModelAndView mv = new ModelAndView("lottery/report");
		mv.addObject("lotteryId", lotteryId);
		return mv;
	}

	/**
	 * 红包统计--近7周
	 * @param group 分组的方式：1-日（默认），2-月，3-年
	 */
	@RequestMapping("history")
	@ResponseBody
	public Map<String, Object> historyReport(String lotteryId, String beginDate, String endDate, String group) {
		Map<String, Object> result = Maps.newHashMap();

		Long lotteryIdLong = NumberUtils.toLong(lotteryId);
		Map<String, Object> issuesAndUseds = lotteryHistoryService.statis(lotteryIdLong, beginDate, endDate, group);

		result.put("chart", issuesAndUseds);
		result.put("code", "0");
		return result;
	}
}
