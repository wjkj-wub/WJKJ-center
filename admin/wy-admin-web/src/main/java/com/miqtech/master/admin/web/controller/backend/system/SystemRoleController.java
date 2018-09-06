package com.miqtech.master.admin.web.controller.backend.system;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.common.Role;
import com.miqtech.master.service.system.OperateService;
import com.miqtech.master.service.system.RoleOperateService;
import com.miqtech.master.service.system.RoleService;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.StringUtils;

/**
 * 系统用户角色管理
 */
@Controller
@RequestMapping("system/role")
public class SystemRoleController extends BaseController {

	@Autowired
	private RoleService roleService;
	@Autowired
	private OperateService operateService;
	@Autowired
	private RoleOperateService roleOperateService;

	/**
	 * 分页
	 */
	@RequestMapping("list/{page}")
	public ModelAndView page(@PathVariable("page") int page, String roleName) {
		ModelAndView mv = new ModelAndView("/system/roleList");

		// 处理参数
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("roleName", roleName);

		Page<Role> vo = roleService.page(page, params);
		pageModels(mv, vo.getContent(), page, vo.getTotalElements());
		mv.addObject("params", params);

		return mv;
	}

	/**
	 * 查询角色详情
	 */
	@ResponseBody
	@RequestMapping("detail/{id}")
	public JsonResponseMsg detail(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		Role role = roleService.findById(id);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, role);
		return result;
	}

	/**
	 * 保存角色信息
	 */
	@ResponseBody
	@RequestMapping("save")
	public JsonResponseMsg save(Role role, String operateIds) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (role != null) {
			if (role.getId() != null) {// 新增时须基于旧值做更新
				role = BeanUtils.updateBean(roleService.findById(role.getId()), role);
			}
			role = roleService.save(role);

			// 更新角色权限
			if (StringUtils.isNotBlank(operateIds)) {
				roleOperateService.updateRoleOperates(role.getId(), operateIds);
			}
		}
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 删除角色
	 */
	@ResponseBody
	@RequestMapping("delete/{id}")
	public JsonResponseMsg delete(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		roleService.delete(id);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 查询角色权限树
	 */
	@ResponseBody
	@RequestMapping("operates/{id}")
	public JsonResponseMsg operates(@PathVariable("id") Long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		List<Map<String, Object>> operates = operateService.getOperateTreeByRoleIds(id.toString(), true);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, operates);
		return result;
	}

}
