package com.miqtech.master.admin.web.controller.backend;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.common.ApplicationVersion;
import com.miqtech.master.service.application.ApplicationVersionService;
import com.miqtech.master.utils.BeanUtils;

@Controller
@RequestMapping("app/version")
public class ApplicationVersionController extends BaseController {
	@Autowired
	private ApplicationVersionService applicationVersionService;

	/**
	 * app版本列表
	 */
	@RequestMapping("/list")
	public ModelAndView list() {
		ModelAndView mv = new ModelAndView("app/versionList");
		List<ApplicationVersion> versions = applicationVersionService.findAll();
		pageModels(mv, versions, 1, versions.size());
		mv.addObject("totalPage", 1);
		return mv;
	}

	/**
	 * app版本详细信息
	 */
	@ResponseBody
	@RequestMapping("/detail/{id}")
	public JsonResponseMsg detail(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		ApplicationVersion applicationVersion = applicationVersionService.findById(id);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, applicationVersion);
		return result;
	}

	/**
	 * 新增或更新
	 */
	@ResponseBody
	@RequestMapping("/save")
	public JsonResponseMsg save(ApplicationVersion appVersion) {
		JsonResponseMsg result = new JsonResponseMsg();

		if (appVersion != null) {
			Date today = new Date();
			appVersion.setUpdateDate(today);
			if (appVersion.getId() != null) {
				ApplicationVersion old = applicationVersionService.findById(appVersion.getId());
				appVersion = BeanUtils.updateBean(old, appVersion);
			} else {
				appVersion.setCreateDate(today);
				appVersion.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			}
			applicationVersionService.save(appVersion);
		}
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);

		return result;
	}

	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("/delete/{id}")
	public JsonResponseMsg delete(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		applicationVersionService.delete(id);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

}
