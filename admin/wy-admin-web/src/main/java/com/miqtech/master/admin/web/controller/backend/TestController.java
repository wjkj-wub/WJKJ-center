package com.miqtech.master.admin.web.controller.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.service.netbar.NetbarRankService;

@Controller
@RequestMapping("test")
public class TestController {

	@Autowired
	private NetbarRankService netbarRankService;

	@ResponseBody
	@RequestMapping("generateNetbarRank")
	public JsonResponseMsg generateNetbarRank() {
		netbarRankService.generateNetbarRank();
		return new JsonResponseMsg().fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}
}
