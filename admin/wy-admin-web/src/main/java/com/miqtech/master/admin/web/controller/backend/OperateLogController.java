package com.miqtech.master.admin.web.controller.backend;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.consts.SystemUserConstant;
import com.miqtech.master.entity.common.SystemUser;
import com.miqtech.master.service.common.OperateLogService;
import com.miqtech.master.service.system.SystemUserService;
import com.miqtech.master.vo.PageVO;

@Controller
@RequestMapping("operateLog")
public class OperateLogController extends BaseController {

	@Autowired
	private OperateLogService operateLogService;
	@Autowired
	private SystemUserService systemUserService;

	/**
	 * 分页
	 */
	@RequestMapping("list/{page}")
	public ModelAndView page(@PathVariable("page") int page, String sysUserId, String info, String beginDate,
			String endDate) {
		ModelAndView mv = new ModelAndView("operateLog/list");

		Map<String, String> params = Maps.newHashMap();
		params.put("sysUserId", sysUserId);
		params.put("info", info);
		params.put("beginDate", beginDate);
		params.put("endDate", endDate);
		mv.addObject("params", params);

		PageVO vo = operateLogService.adminPage(page, params);
		pageModels(mv, vo.getList(), page, vo.getTotal());

		List<Integer> userTypes = Lists.newArrayList();
		userTypes.add(SystemUserConstant.TYPE_AMUSE_APPEAL);
		userTypes.add(SystemUserConstant.TYPE_AMUSE_VERIFY);
		userTypes.add(SystemUserConstant.TYPE_AMUSE_ISSUE);
		userTypes.add(SystemUserConstant.TYPE_ACTIVITY_ADMIN);
		userTypes.add(SystemUserConstant.TYPE_NORMAL_ADMIN);
		List<SystemUser> sysUsers = systemUserService.findValidByUserTypeIn(userTypes);
		mv.addObject("sysUsers", sysUsers);

		return mv;
	}
}
