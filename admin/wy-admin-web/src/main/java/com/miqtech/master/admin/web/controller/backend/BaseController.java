package com.miqtech.master.admin.web.controller.backend;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;

import com.miqtech.master.config.SystemConfig;
import com.miqtech.master.utils.PageUtils;

public class BaseController {

	@Autowired
	protected SystemConfig systemConfig;


	/**
	 * 全局变量
	 */
	@ModelAttribute
	public void initPath(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		String base = request.getContextPath();
		String fullPath = request.getScheme() + "://" + request.getServerName() + base;
		model.addAttribute("ctx", base);
		model.addAttribute("imgServer", systemConfig.getImgServerDomain());
		model.addAttribute("fullPath", fullPath);
	}

	protected void backIndex(ModelAndView mv, String msg) {
		mv.addObject("sitemesh", "N");
		mv.addObject("msg", msg);
		mv.setViewName("forward:/");
	}

	protected void backError(ModelAndView mv, String msg, String url) {
		mv.addObject("sitemesh", "N");
		mv.addObject("backUrl", url);
		mv.addObject("msg", msg);
		mv.setViewName("error/error");
	}

	protected <T> void pageModels(ModelAndView mv, List<T> list, int page, long count) {
		mv.addObject("list", list);
		mv.addObject("currentPage", page);
		mv.addObject("isLastPage", PageUtils.isBottom(page, count));//0已到最后一页 1可以加载下一页
		mv.addObject("totalCount", count);
		mv.addObject("totalPage", PageUtils.calcTotalPage(count));
	}

	@SuppressWarnings("rawtypes")
	protected void pageData(Model model, List list, int page, long count) {
		model.addAttribute("result", list);
		model.addAttribute("currentPage", page);
		model.addAttribute("isLastPage", PageUtils.isBottom(page, count));//0已到最后一页 1可以加载下一页
		model.addAttribute("totalCount", count);
		model.addAttribute("totalPage", PageUtils.calcTotalPage(count));
	}
	
}
