package com.miqtech.master.admin.web.controller.backend.mall;

import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.mall.TaskConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.mall.MallTask;
import com.miqtech.master.service.mall.MallTaskService;
import com.miqtech.master.vo.PageVO;

/**
 * 任务管理
 */
@Controller
@RequestMapping("malltask/")
public class MallTaskController extends BaseController {

	@Autowired
	private MallTaskService mallTaskService;

	@RequestMapping("list/{page}")
	public ModelAndView page(@PathVariable("page") int page) {
		ModelAndView mv = new ModelAndView("mall/taskList");

		Map<String, String> params = Maps.newHashMap();
		params.put("type", TaskConstant.TASK_TYPE_DAILY.toString());

		PageVO vo = mallTaskService.adminPage(page, params);

		pageModels(mv, vo.getList(), page, vo.getTotal());

		return mv;
	}

	@RequestMapping("edit")
	public ModelAndView edit(String id) {
		ModelAndView mv = new ModelAndView("mall/taskEdit");

		if (NumberUtils.isNumber(id)) {
			Long idLong = NumberUtils.toLong(id);
			MallTask task = mallTaskService.findById(idLong);
			mv.addObject("editObj", task);
		}

		return mv;
	}

	@ResponseBody
	@RequestMapping("save")
	public JsonResponseMsg save(MallTask task) {
		if (task == null || task.getId() == null) {
			return new JsonResponseMsg().fill(-1, "参数错误");
		}

		task = mallTaskService.save(task);
		return new JsonResponseMsg().fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, task);
	}
}
