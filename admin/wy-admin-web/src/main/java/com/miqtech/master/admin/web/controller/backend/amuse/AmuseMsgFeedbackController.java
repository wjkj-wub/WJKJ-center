package com.miqtech.master.admin.web.controller.backend.amuse;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.amuse.AmuseMsgFeedback;
import com.miqtech.master.service.amuse.AmuseMsgFeedbackService;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

@Controller
@RequestMapping("/amuse/feedback")
public class AmuseMsgFeedbackController extends BaseController {

	@Autowired
	private AmuseMsgFeedbackService amuseMsgFeedbackService;

	/**
	 * 列表
	 */
	@RequestMapping("list/{page}")
	public ModelAndView list(@PathVariable("page") int page, String content, String isAppSetting) {
		ModelAndView mv = new ModelAndView("amuse/msgFeedbackList");

		Map<String, Object> params = Maps.newHashMap();
		if (StringUtils.isNotBlank(content)) {
			params.put("content", content);
		}
		boolean isAppSettingBoolean = CommonConstant.INT_BOOLEAN_TRUE.toString().equals(isAppSetting);
		params.put("isAppSetting", isAppSettingBoolean);

		PageVO vo = amuseMsgFeedbackService.page(page, params);
		pageModels(mv, vo.getList(), page, vo.getTotal());
		mv.addObject("params", params);
		mv.addObject("isAppSetting", isAppSettingBoolean);

		return mv;
	}

	/**
	 * 详情
	 */
	@ResponseBody
	@RequestMapping("detail/{id}")
	public JsonResponseMsg detail(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS,
				amuseMsgFeedbackService.findById(id));
	}

	/**
	 * 保存
	 */
	@ResponseBody
	@RequestMapping("save")
	public JsonResponseMsg save(AmuseMsgFeedback msgFeedback) {
		JsonResponseMsg result = new JsonResponseMsg();
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS,
				amuseMsgFeedbackService.saveOrUpdate(msgFeedback));
	}

	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("delete/{id}")
	public JsonResponseMsg delete(@PathVariable("id") long id) {
		amuseMsgFeedbackService.delete(id);
		return new JsonResponseMsg().fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}
}
