package com.miqtech.master.admin.web.controller.backend.system;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.common.Operate;
import com.miqtech.master.service.system.OperateService;
import com.miqtech.master.utils.JsonUtils;

@Controller
@RequestMapping("system/operate")
public class SystemOperateController extends BaseController {

	@Autowired
	private OperateService operateService;

	/**
	 * 权限树列表
	 */
	@RequestMapping("list")
	public ModelAndView tree() {
		ModelAndView mv = new ModelAndView("system/operateList");
		List<Map<String, Object>> operateTree = operateService.getOperateTree();
		mv.addObject("operateTree", JsonUtils.objectToString(operateTree));
		return mv;
	}

	/**
	 * 保存对象
	 */
	@ResponseBody
	@RequestMapping("save")
	public JsonResponseMsg save(Operate operate) {
		JsonResponseMsg result = new JsonResponseMsg();
		operateService.save(operate);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 删除菜单
	 */
	@ResponseBody
	@RequestMapping("delete/{id}")
	public JsonResponseMsg delete(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		operateService.delete(id);
		return result;
	}

}
