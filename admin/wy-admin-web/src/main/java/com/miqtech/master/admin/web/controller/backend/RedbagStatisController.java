package com.miqtech.master.admin.web.controller.backend;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.service.user.UserRedbagService;
import com.miqtech.master.utils.JsonUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 红包统计
 */
@Controller
@RequestMapping("redbag/statis")
public class RedbagStatisController extends BaseController {

	@Autowired
	private UserRedbagService userRedbagService;

	/**
	 * 统计分享红包的领取情况及使用情况
	 */
	@RequestMapping("shareRedbag")
	public ModelAndView statisShareRedbag(String count) {
		ModelAndView mv = new ModelAndView("redbag/statis");

		Integer countInt = NumberUtils.toInt(count, 7);
		List<Map<String, Object>> getStatis = userRedbagService.statisShareRedbag(null, countInt);
		mv.addObject("gettedShareRedbags", JsonUtils.objectToString(getStatis));
		List<Map<String, Object>> useStatis = userRedbagService.statisShareRedbag(CommonConstant.INT_BOOLEAN_FALSE,
				countInt);
		mv.addObject("usedShareRedbags", JsonUtils.objectToString(useStatis));

		mv.addObject("count", countInt);
		mv.addObject("paramCount", count);

		return mv;
	}

	/**每周抢红包统计
	 * @param week
	 * @return
	 */
	@RequestMapping("weekRedbag")
	public String weekRedbag(Integer week, Model model) {
		if (week == null) {
			week = 5;
		}
		List<Map<String, Object>> list = userRedbagService.weekRedBag(week);
		List<String> categories = new ArrayList<String>();
		List<Map<String, Object>> numData = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> moneyData = new ArrayList<Map<String, Object>>();
		List<Integer> num = new ArrayList<>();
		List<Integer> usedNum = new ArrayList<>();
		List<Integer> money = new ArrayList<>();
		List<Integer> usedMoney = new ArrayList<>();
		if (list != null) {
			for (Map<String, Object> map : list) {
				categories.add((String) map.get("date"));
				num.add(((BigInteger) map.get("num")).intValue());
				usedNum.add(((BigInteger) map.get("used_num")).intValue());
				money.add(((BigDecimal) map.get("money")).intValue());
				usedMoney.add(((BigDecimal) map.get("used_money")).intValue());
			}
		}
		Map<String, Object> numMap = new HashMap<String, Object>(16);
		Map<String, Object> usedNumMap = new HashMap<String, Object>(16);
		Map<String, Object> moneyMap = new HashMap<String, Object>(16);
		Map<String, Object> usedMoneyMap = new HashMap<String, Object>(16);
		numMap.put("name", "每周抢红包总个数");
		numMap.put("data", num);
		usedNumMap.put("name", "每周抢红包使用个数");
		usedNumMap.put("data", usedNum);
		moneyMap.put("name", "每周抢红包总金额");
		moneyMap.put("data", money);
		usedMoneyMap.put("name", "每周抢红包使用金额");
		usedMoneyMap.put("data", usedMoney);
		numData.add(numMap);
		numData.add(usedNumMap);
		moneyData.add(moneyMap);
		moneyData.add(usedMoneyMap);
		model.addAttribute("categories", JsonUtils.objectToString(categories));
		model.addAttribute("numData", JsonUtils.objectToString(numData));
		model.addAttribute("moneyData", JsonUtils.objectToString(moneyData));
		model.addAttribute("week", week);
		return "redbag/weekRedbag";
	}
}
