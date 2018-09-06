package com.miqtech.master.admin.web.controller.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.amuse.AmuseAppeal;
import com.miqtech.master.entity.common.SystemSuggestion;
import com.miqtech.master.service.amuse.AmuseAppealService;
import com.miqtech.master.service.system.SystemSuggestionService;
import com.miqtech.master.utils.JsonUtils;
import com.miqtech.master.vo.PageVO;

@Controller
@RequestMapping("suggestion")
public class SuggestionController extends BaseController {
	@Autowired
	private SystemSuggestionService systemSuggestionService;
	@Autowired
	private AmuseAppealService amuseAppealService;

	@RequestMapping("list/{page}")
	public String list(Model model, @PathVariable("page") int page, String phone, String content, Integer state,
			String date) {
		PageVO vo = systemSuggestionService.queryList(phone, content, state, date, page);
		this.pageData(model, vo.getList(), page, vo.getTotal());
		model.addAttribute("phone", phone);
		model.addAttribute("content", content);
		model.addAttribute("state", state);
		model.addAttribute("date", date);
		return "suggestion/list";
	}

	@RequestMapping("detail/{id}")
	@ResponseBody
	public String detail(@PathVariable Long id, int type) {
		JsonResponseMsg result = new JsonResponseMsg();
		result.setObject(systemSuggestionService.queryDetail(id, type));
		return JsonUtils.objectToString(result);
	}

	@RequestMapping("deal")
	@ResponseBody
	public String deal(Long id, Integer state, int type, String remark) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (type == 1) {
			SystemSuggestion systemSuggestion = systemSuggestionService.findById(id);
			systemSuggestion.setState(state);
			systemSuggestion.setRemark(remark);
			systemSuggestionService.save(systemSuggestion);
		} else if (type == 2) {
			AmuseAppeal amuseAppeal = amuseAppealService.findById(id);
			amuseAppeal.setState(state);
			amuseAppeal.setRemark(remark);
			amuseAppealService.save(amuseAppeal);
		}
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return JsonUtils.objectToString(result);
	}
}
