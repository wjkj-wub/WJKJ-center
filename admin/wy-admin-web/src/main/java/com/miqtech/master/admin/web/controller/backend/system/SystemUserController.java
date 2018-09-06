package com.miqtech.master.admin.web.controller.backend.system;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.SystemUserConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.common.Role;
import com.miqtech.master.entity.common.SystemArea;
import com.miqtech.master.entity.common.SystemUser;
import com.miqtech.master.service.system.RoleService;
import com.miqtech.master.service.system.SysUserAreaService;
import com.miqtech.master.service.system.SystemAreaService;
import com.miqtech.master.service.system.SystemUserRoleService;
import com.miqtech.master.service.system.SystemUserService;
import com.miqtech.master.utils.JsonUtils;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

@Controller
@RequestMapping("system/user")
public class SystemUserController extends BaseController {

	@Autowired
	private RoleService roleService;
	@Autowired
	private SystemUserService systemUserService;
	@Autowired
	private SystemUserRoleService systemUserRoleService;
	@Autowired
	private SystemAreaService systemAreaService;
	@Autowired
	private SysUserAreaService sysUserAreaService;

	/**
	 * 分页列表
	 */
	@RequestMapping("/list/{page}")
	public ModelAndView users(@PathVariable("page") int page, String username, String type, String roleId,
			String areaCode) {
		ModelAndView mv = new ModelAndView("system/userList");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("type", type);
		params.put("username", username);
		params.put("roleId", roleId);
		params.put("areaCode", areaCode);

		PageVO vo = systemUserService.nativePage(page, params);
		pageModels(mv, vo.getList(), page, vo.getTotal());
		mv.addObject("params", params);

		// 查询系统所有角色
		List<Role> roles = roleService.findAll();
		mv.addObject("roles", roles);

		// 初始化地区数据
		List<SystemArea> areas = systemAreaService.getTree(true);
		mv.addObject("areas", JsonUtils.objectToString(areas));
		mv.addObject("areasObj", areas);

		return mv;
	}

	/**
	 * 查询系统用户详情
	 */
	@ResponseBody
	@RequestMapping("detail/{id}")
	public JsonResponseMsg detail(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		SystemUser user = systemUserService.findById(id);
		if (user != null) {
			user.setAreas(sysUserAreaService.findBySysUserId(id));
			result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, user);
		}
		return result;
	}

	/**
	 * 新增或更新
	 */
	@ResponseBody
	@RequestMapping("/save")
	public JsonResponseMsg save(String id, String username, String realname, String password, String qq,
			String telephone, String email, String userType, String[] area, String areaCode) {
		JsonResponseMsg result = new JsonResponseMsg();

		if (StringUtils.isAllBlank(username, realname, password, qq, telephone, email)
				&& !NumberUtils.isNumber(userType)) {
			result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
			return result;
		}

		SystemUser user = null;
		if (StringUtils.isNotBlank(id)) {
			user = systemUserService.findById(NumberUtils.toLong(id));
			if (user == null) {
				result.fill(CommonConstant.CODE_ERROR_LOGIC, "不存在用户");
				return result;
			}
		} else {
			SystemUser oldUser = systemUserService.findByUsername(username);
			if (oldUser != null) {
				result.fill(CommonConstant.CODE_ERROR_LOGIC, "已存在此用户名");
				return result;
			}

			user = new SystemUser();
		}

		if (StringUtils.isNotBlank(username)) {
			user.setUsername(username);
		}
		if (StringUtils.isNotBlank(realname)) {
			user.setRealname(realname);
		}
		if (StringUtils.isNotBlank(password)) {
			user.setEncryptPassword(password);
		}
		if (StringUtils.isNotBlank(telephone)) {
			user.setTelephone(telephone);
		}
		if (StringUtils.isNotBlank(qq)) {
			user.setQq(qq);
		}
		if (StringUtils.isNotBlank(email)) {
			user.setEmail(email);
		}
		if (StringUtils.isNotBlank(userType)) {
			user.setUserType(NumberUtils.toInt(userType, 1));
		}
		if (StringUtils.isNotBlank(areaCode)) {
			user.setAreaCode(areaCode);
		}

		// 新增或编辑 娱乐赛 审核、申诉、发放 用户时 重置用户操作数量
		if (SystemUserConstant.TYPE_AMUSE_VERIFY.equals(user.getUserType())
				|| SystemUserConstant.TYPE_AMUSE_APPEAL.equals(user.getUserType())
				|| SystemUserConstant.TYPE_AMUSE_ISSUE.equals(user.getUserType())) {
			systemUserService.resetAmuseAdminOperate(user.getUserType());
		}

		user = systemUserService.save(user);

		// 更新用户地区设置
		sysUserAreaService.update(area, user.getId());

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 删除系统用户
	 */
	@ResponseBody
	@RequestMapping("delete/{id}")
	public JsonResponseMsg delete(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		systemUserService.delete(id);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 保存用户角色
	 */
	@ResponseBody
	@RequestMapping("saveRoles")
	public JsonResponseMsg saveRoles(String userId, String roleIds) {
		JsonResponseMsg result = new JsonResponseMsg();

		if (StringUtils.isBlank(roleIds) || !NumberUtils.isNumber(userId)) {
			result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
			return result;
		}

		systemUserRoleService.saveUserRoles(NumberUtils.toLong(userId), roleIds);

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 查询用户的所有角色
	 */
	@ResponseBody
	@RequestMapping("roles/{userId}")
	public JsonResponseMsg roles(@PathVariable("userId") long userId) {
		JsonResponseMsg result = new JsonResponseMsg();
		List<Map<String, Object>> roles = systemUserRoleService.getUserRoles(userId);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, roles);
		return result;
	}

}
