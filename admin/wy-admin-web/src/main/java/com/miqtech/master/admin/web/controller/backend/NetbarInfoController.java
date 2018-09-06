package com.miqtech.master.admin.web.controller.backend;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.netbar.NetbarInfo;
import com.miqtech.master.service.netbar.NetbarInfoService;
import com.miqtech.master.vo.PageVO;

@Controller
@RequestMapping("netbar")
public class NetbarInfoController extends BaseController {

	@Autowired
	private NetbarInfoService netbarInfoService;

	/**
	 * 通过areaCode查询网吧列表
	 */
	@ResponseBody
	@RequestMapping("queryByAreaCode")
	public JsonResponseMsg queryByAreaCode(String areaCode) {
		JsonResponseMsg result = new JsonResponseMsg();

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		if (StringUtils.isNotBlank(areaCode)) {
			List<NetbarInfo> netbars = netbarInfoService.findByAreaCodeLike(areaCode);
			result.setObject(netbars);
		}

		return result;
	}

	/**
	 * 通过网吧名查询网吧列表
	 */
	@ResponseBody
	@RequestMapping("queryByName")
	public JsonResponseMsg queryByName(String name) {
		JsonResponseMsg result = new JsonResponseMsg();

		List<NetbarInfo> netbars = null;
		if (StringUtils.isNotBlank(name)) {
			netbars = netbarInfoService.findValidByName(name);
		}

		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, netbars);
	}

	/**
	 * 查询网吧列表
	 */
	@RequestMapping("queryNetbaList")
	public ModelAndView queryNetbarList(Integer page) {
		ModelAndView mv = new ModelAndView("matches/cenueEdit");
		if (page == null || page < 0) {
			page = 1;
		}
		PageVO vo = netbarInfoService.getNetbarList(page, null, null, null, null);
		pageModels(mv, vo.getList(), page, vo.getTotal());
		return mv;
	}
}
