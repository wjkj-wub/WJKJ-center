package com.miqtech.master.admin.web.controller.backend.lottery;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.entity.lottery.LotteryOption;
import com.miqtech.master.service.lottery.LotteryHistoryService;
import com.miqtech.master.service.lottery.LotteryOptionService;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.ExcelUtils;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

/**
 * 报表
 */
@Controller
@RequestMapping("lottery/history")
public class LotteryHistoryController extends BaseController {

	private static final Logger LOGGER = LoggerFactory.getLogger(LotteryHistoryController.class);

	@Autowired
	private LotteryOptionService lotteryOptionService;
	@Autowired
	private LotteryHistoryService lotteryHistroyService;

	/**
	 * 查看活动的中奖用户列表
	 */
	@RequestMapping("list/{lotteryId}/{page}")
	public ModelAndView page(@PathVariable("lotteryId") Long lotteryId, @PathVariable("page") int page, String awardId,
			String prizeId, String telephone, String username, String beginDate, String endDate) {
		ModelAndView mv = new ModelAndView("lottery/historyList");

		Map<String, String> params = Maps.newHashMap();
		if (lotteryId != null) {
			params.put("lotteryId", lotteryId.toString());
		}
		if (NumberUtils.isNumber(awardId)) {
			params.put("awardId", awardId);
		}
		if (NumberUtils.isNumber(prizeId)) {
			params.put("prizeId", prizeId);
		}
		if (StringUtils.isNotBlank(telephone)) {
			params.put("telephone", telephone);
		}
		if (StringUtils.isNotBlank(username)) {
			params.put("username", username);
		}
		if (StringUtils.isNotBlank(beginDate)) {
			params.put("beginDate", beginDate);
		}
		if (StringUtils.isNotBlank(endDate)) {
			params.put("endDate", endDate);
		}

		PageVO vo = lotteryHistroyService.page(page, params);
		pageModels(mv, vo.getList(), page, vo.getTotal());
		mv.addObject("params", params);
		mv.addObject("lotteryId", lotteryId);

		return mv;
	}

	/**
	 * 查看活动的中奖用户列表
	 */
	@RequestMapping("export/{lotteryId}/{page}")
	public ModelAndView export(HttpServletResponse res, @PathVariable("lotteryId") Long lotteryId,
			@PathVariable("page") int page, String awardId, String prizeId, String telephone, String username,
			String beginDate, String endDate) {
		Map<String, String> params = Maps.newHashMap();
		if (lotteryId != null) {
			params.put("lotteryId", lotteryId.toString());
		}
		if (NumberUtils.isNumber(awardId)) {
			params.put("awardId", awardId);
		}
		if (NumberUtils.isNumber(prizeId)) {
			params.put("prizeId", prizeId);
		}
		if (StringUtils.isNotBlank(telephone)) {
			params.put("telephone", telephone);
		}
		if (StringUtils.isNotBlank(username)) {
			params.put("username", username);
		}
		if (StringUtils.isNotBlank(beginDate)) {
			params.put("beginDate", beginDate);
		}
		if (StringUtils.isNotBlank(endDate)) {
			params.put("endDate", endDate);
		}

		PageVO vo = lotteryHistroyService.page(page, params);
		List<Map<String, Object>> list = vo.getList();

		// 查询excel标题
		String title = null;
		LotteryOption option = lotteryOptionService.findValidById(lotteryId);
		if (option != null) {
			title = option.getName() + "中奖记录";
		} else {
			title = "中奖记录";
		}

		// 编辑excel内容
		String[][] contents = new String[list.size() + 1][];

		// 设置标题行
		String[] contentTitle = new String[5];
		contentTitle[0] = "#";
		contentTitle[1] = "姓名";
		contentTitle[2] = "联系方式";
		contentTitle[3] = "中奖信息";
		contentTitle[4] = "中奖时间";
		contents[0] = contentTitle;

		// 设置内容
		for (int i = 0; i < list.size(); i++) {
			Map<String, Object> member = list.get(i);
			String[] row = new String[5];
			row[0] = ((Number) member.get("id")).toString();
			row[1] = String.valueOf(member.get("userName"));
			row[2] = String.valueOf(member.get("userTelephone"));
			row[3] = String.valueOf(member.get("awardName")) + " - " + String.valueOf(member.get("prizeName"));
			row[4] = DateUtils.dateToString((Date) member.get("createDate"), DateUtils.YYYY_MM_DD_HH_MM_SS);
			contents[i + 1] = row;
		}

		try {
			ExcelUtils.exportExcel(title, contents, res);
		} catch (Exception e) {
			LOGGER.error("导出数据异常：", e);
		}
		return null;
	}
}
