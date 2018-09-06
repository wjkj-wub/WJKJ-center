package com.miqtech.master.admin.web.controller.backend.matches;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.service.matches.MatchesOrganiserGameService;

@Controller
@RequestMapping("organiserGame")
public class OrganiserGameController extends BaseController {

	@Autowired
	private MatchesOrganiserGameService matchesOrganiserGameService;

	/**
	 * 列表
	 */
	@RequestMapping("/gameList")
	@ResponseBody
	public JsonResponseMsg list(Long organiserId) {
		JsonResponseMsg result = new JsonResponseMsg();
		List<Map<String, Object>> gameList = matchesOrganiserGameService.getGameList(organiserId);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, gameList);
	}

}
