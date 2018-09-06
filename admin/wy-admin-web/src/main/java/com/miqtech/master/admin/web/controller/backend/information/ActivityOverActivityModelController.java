package com.miqtech.master.admin.web.controller.backend.information;

import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.activity.ActivityOverActivityModel;
import com.miqtech.master.service.activity.ActivityOverActivityModelService;
import com.miqtech.master.vo.PageVO;

/**
 * 模版管理
 */
@Controller
@RequestMapping("overActivity/model")
public class ActivityOverActivityModelController extends BaseController {

	@Autowired
	private ActivityOverActivityModelService activityOverActivityModelService;

	/**
	 * 模版列表
	 */
	@RequestMapping("list/{page}")
	public ModelAndView list(@PathVariable("page") int page) {
		ModelAndView mv = new ModelAndView("information/modelList");

		PageVO vo = activityOverActivityModelService.adminPage(page, null);
		pageModels(mv, vo.getList(), page, vo.getTotal());

		return mv;
	}

	/**
	 * 新增编辑页面
	 */
	@RequestMapping("edit")
	public ModelAndView edit(String id) {
		ModelAndView mv = new ModelAndView("information/modelEdit");

		if (NumberUtils.isNumber(id)) {
			long idLong = NumberUtils.toLong(id);
			ActivityOverActivityModel editObj = activityOverActivityModelService.findById(idLong);
			mv.addObject("editObj", editObj);
		}

		return mv;
	}

	/**
	 * 新增或保存
	 */
	@ResponseBody
	@RequestMapping("save")
	public JsonResponseMsg save(ActivityOverActivityModel model) {
		JsonResponseMsg result = new JsonResponseMsg();
		activityOverActivityModelService.saveOrUpdate(model);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("delete")
	public JsonResponseMsg delete(String id) {
		JsonResponseMsg result = new JsonResponseMsg();

		if (NumberUtils.isNumber(id)) {
			long idLong = NumberUtils.toLong(id);
			activityOverActivityModelService.disableById(idLong);
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}
}
