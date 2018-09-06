package com.miqtech.master.admin.web.controller.api.mall;

import java.io.IOException;
import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.miqtech.master.admin.web.annotation.LoginValid;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.service.mall.MallCoinOrderService;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.vo.PageVO;

import jxl.write.WriteException;

@Controller
@RequestMapping("api/mall/coin/order")
public class MallCoinOrderController {
	@Autowired
	private MallCoinOrderService mallCoinOrderService;

	/*
	 * 金币充值列表
	 */
	@RequestMapping("list")
	@ResponseBody
	@LoginValid(valid = true)
	public JsonResponseMsg list(HttpServletRequest req) {
		JsonResponseMsg result = new JsonResponseMsg();
		String telephone = req.getParameter("telephone");
		String createDate = req.getParameter("createDate");
		String updateDate = req.getParameter("updateDate");
		String type = req.getParameter("type");
		int page = NumberUtils.toInt(req.getParameter("page")) > 0 ? NumberUtils.toInt(req.getParameter("page")) : 1;
		int pageSize = NumberUtils.toInt(req.getParameter("pageSize")) > 0
				? NumberUtils.toInt(req.getParameter("pageSize")) : PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		PageVO vo = null;
		try {
			vo = mallCoinOrderService.getList(telephone, createDate, updateDate, type, false, page, pageSize);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, vo);
	}

	/*
	 * 金币充值列表导出
	 */
	@RequestMapping("export")
	@ResponseBody
	public void export(HttpServletRequest req, HttpServletResponse res) {
		String telephone = req.getParameter("telephone");
		String createDate = req.getParameter("createDate");
		String updateDate = req.getParameter("updateDate");
		String type = req.getParameter("type");
		int page = NumberUtils.toInt(req.getParameter("page")) > 0 ? NumberUtils.toInt(req.getParameter("page")) : 1;
		int pageSize = NumberUtils.toInt(req.getParameter("pageSize")) > 0
				? NumberUtils.toInt(req.getParameter("pageSize")) : PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		PageVO vo = null;
		try {
			vo = mallCoinOrderService.getList(telephone, createDate, updateDate, type, true, page, pageSize);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		try {
			mallCoinOrderService.export(vo.getList(), res);
		} catch (WriteException | IOException e) {
			e.printStackTrace();
		}
	}

}
