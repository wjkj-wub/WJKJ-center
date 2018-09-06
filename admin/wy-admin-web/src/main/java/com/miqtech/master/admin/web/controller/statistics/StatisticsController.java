package com.miqtech.master.admin.web.controller.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.service.StatisticsService;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

@Controller
@RequestMapping("statistics")
public class StatisticsController extends BaseController {

	@Autowired
	private StatisticsService statisticsService;

	@RequestMapping("/{type}/{page}")
	public ModelAndView netbar(@PathVariable int page, @PathVariable int type) {
		ModelAndView mv = new ModelAndView();
		if(type==1||type==2||type==3){
			 mv = wbModelAndView(type, page);
		}else{
			mv= userModelAndView(type, page);
		}
		return mv;
		
	}
	
	private ModelAndView wbModelAndView(int type,int page){
		ModelAndView mv = new ModelAndView("statistics/page_new");
		PageVO pageVO = statisticsService.getPageVOByType(page, null, type);
		pageModels(mv, pageVO.getList(), page, pageVO.getTotal());
		List<Map<String, Object>> statistics = statisticsService.getAllByType(type);
		List<Map<String, Object>> chatData = statisticsService.getWbChartData(type);
		mv.addObject("statisticsJSONString", JSON.toJSONString(statistics));
		mv.addObject("chatData", JSON.toJSONString(chatData));
		mv.addObject("type", type);
		try {
			Thread.sleep(RandomUtils.nextInt(2, 4) * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return mv;
	}
	
	@RequestMapping("/getDayData")
	@ResponseBody
	public String wbDayData(String month, String type){
		if(StringUtils.isBlank(type)||StringUtils.isBlank(month)){
			return "传入的参数有误";
		}
		List<Map<String, Object>> wbChatDataByMonth = statisticsService.getWbChatDataByMonth(Integer.valueOf(type), month);
		return JSON.toJSONString(wbChatDataByMonth);
	}
	
	private ModelAndView userModelAndView(int type,int page){
		ModelAndView mv = new ModelAndView("statistics/page");
		PageVO pageVO = statisticsService.getPageVOByType(page, null, type);
		pageModels(mv, pageVO.getList(), page, pageVO.getTotal());

		List<Map<String, Object>> statistics = statisticsService.getAllByType(type);
		mv.addObject("statisticsJSONString", JSON.toJSONString(statistics));
		mv.addObject("type", type);

		try {
			Thread.sleep(RandomUtils.nextInt(2, 5) * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return mv;
	}

}
