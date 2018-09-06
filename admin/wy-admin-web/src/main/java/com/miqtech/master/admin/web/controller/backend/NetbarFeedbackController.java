package com.miqtech.master.admin.web.controller.backend;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.netbar.NetbarFeedback;
import com.miqtech.master.service.netbar.NetbarFeedbackService;
import com.miqtech.master.utils.JsonUtils;
import com.miqtech.master.vo.PageVO;

@Controller
@RequestMapping("netbarFeedback")
public class NetbarFeedbackController extends BaseController {
	@Autowired
	private NetbarFeedbackService netbarFeedbackService;

	@RequestMapping("list/{page}")
	public String list(HttpServletRequest request, Model model, String phone, String content, Integer state,
			String date, @PathVariable Integer page) {
		PageVO vo = netbarFeedbackService.queryList(phone, content, state, date, page);
		this.pageData(model, vo.getList(), page, vo.getTotal());
		model.addAttribute("phone", phone);
		model.addAttribute("content", content);
		model.addAttribute("state", state);
		model.addAttribute("date", date);
		return "netbar/feedback";
	}

	@RequestMapping("detail/{id}")
	@ResponseBody
	public String detail(@PathVariable Long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		result.setObject(netbarFeedbackService.queryById(id));
		return JsonUtils.objectToString(result);
	}

	@RequestMapping("deal")
	@ResponseBody
	public String deal(Long id, Integer state) {
		JsonResponseMsg result = new JsonResponseMsg();
		NetbarFeedback netbarFeedback = netbarFeedbackService.findById(id);
		netbarFeedback.setState(state);
		netbarFeedbackService.save(netbarFeedback);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return JsonUtils.objectToString(result);
	}

}
