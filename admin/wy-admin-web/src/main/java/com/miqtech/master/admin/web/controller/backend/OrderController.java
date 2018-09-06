package com.miqtech.master.admin.web.controller.backend;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.common.SystemUser;
import com.miqtech.master.entity.netbar.NetbarOrder;
import com.miqtech.master.service.netbar.NetbarOrderService;
import com.miqtech.master.vo.PageVO;

@Controller
@RequestMapping("order")
public class OrderController extends BaseController {
	private static final Logger log = LoggerFactory.getLogger(OrderController.class);
	@Autowired
	private NetbarOrderService netbarOrderService;

	/**
	 * 订单列表
	 */
	@RequestMapping("list/{page}")
	public ModelAndView list(@PathVariable("page") Integer page, String phone, String netbarName, String nickname,
			Integer status) {
		ModelAndView mv = new ModelAndView("/order/list");
		PageVO vo = new PageVO();
		if (StringUtils.isBlank(phone) && StringUtils.isBlank(netbarName) && StringUtils.isBlank(nickname)) {
			this.pageModels(mv, new ArrayList<Object>(), page, 0);
			return mv;
		}
		vo = netbarOrderService.queryOrderForManage(phone, netbarName, nickname, status, page);
		this.pageModels(mv, vo.getList(), page, vo.getTotal());
		mv.addObject("phone", phone);
		mv.addObject("netbarName", netbarName);
		mv.addObject("nickname", nickname);
		mv.addObject("status", status);
		return mv;
	}

	/**
	 * 订单信息
	 */
	@ResponseBody
	@RequestMapping("detail/{id}")
	public JsonResponseMsg detail(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		NetbarOrder netbarOrder = netbarOrderService.findByID(id);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, netbarOrder);
		return result;
	}

	/**
	 * 保存
	 */
	@ResponseBody
	@RequestMapping("save")
	public JsonResponseMsg save(HttpServletRequest request, Long id, Integer status) {
		JsonResponseMsg result = new JsonResponseMsg();
		NetbarOrder netbarOrder = netbarOrderService.findByID(id);
		if (netbarOrder != null) {
			Integer oldStatus = netbarOrder.getStatus();
			netbarOrder.setStatus(status);
			netbarOrderService.saveOrUpdate(netbarOrder);
			SystemUser login = Servlets.getSessionUser(request);
			log.error("id为" + login.getId() + "的" + login.getUsername() + "把id为" + String.valueOf(id) + "的订单" + "状态从"
					+ String.valueOf(oldStatus) + "改为" + String.valueOf(status));
			result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		} else {
			result.fill(-1, "订单不存在!");
		}
		return result;
	}
}
