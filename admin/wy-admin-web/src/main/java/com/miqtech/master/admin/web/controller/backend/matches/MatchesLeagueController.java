package com.miqtech.master.admin.web.controller.backend.matches;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.matches.MatchesLeague;
import com.miqtech.master.service.matches.MatchesLeagueService;
import com.miqtech.master.service.matches.MatchesOrganiserGameService;
import com.miqtech.master.thirdparty.util.img.ImgUploadUtil;
import com.miqtech.master.vo.PageVO;

@Controller
@RequestMapping("league")
public class MatchesLeagueController extends BaseController {

	@Autowired
	private MatchesOrganiserGameService matchesOrganiserGameService;
	@Autowired
	private MatchesLeagueService matchesLeagueService;

	/**
	 * 列表
	 * 
	 * @throws ParseException
	 */
	@RequestMapping("/list/{page}")
	public ModelAndView list(@PathVariable("page") Integer page, Long organiserId) throws ParseException {
		if (page == null) {
			page = 1;
		}
		ModelAndView mv = new ModelAndView("/matches/leagueList");
		PageVO vo = matchesLeagueService.getLeagueList(page, organiserId);
		pageModels(mv, vo.getList(), page, vo.getTotal());
		mv.addObject("organiserId", organiserId);
		return mv;
	}

	/**
	 * 赛事新增页面
	 */
	@RequestMapping("/edit")
	public ModelAndView edit(Long organiserId, Long leagueId) {
		ModelAndView mv = new ModelAndView("/matches/leagueEdit");
		List<Map<String, Object>> gameList = matchesOrganiserGameService.getGameList(organiserId);
		mv.addObject("gameList", gameList);
		mv.addObject("organiserId", organiserId);
		mv.addObject("isInsert", 0);
		if (leagueId != null) {
			MatchesLeague matchesLeague = matchesLeagueService.findById(leagueId);
			mv.addObject("matchesLeague", matchesLeague);
			if (StringUtils.isNotBlank(matchesLeague.getLogo())) {
				mv.addObject("isInsert", 1);
			}
		}
		return mv;
	}

	/**
	 * 赛事新增页面
	 */
	@RequestMapping("/save")
	@ResponseBody
	public JsonResponseMsg save(HttpServletRequest req, Long organiserId, String name, Long itemsId,
			Long matchesLeagueId) {
		JsonResponseMsg result = new JsonResponseMsg();
		MatchesLeague matchesLeague = new MatchesLeague();
		MultipartFile logo = Servlets.getMultipartFile(req, "logo");
		String systemName = "wy-web-admin";
		String src = ImgUploadUtil.genFilePath("leagueLogo");
		if (matchesLeagueId != null) {
			MatchesLeague matchesLeagueExit = matchesLeagueService.findById(matchesLeagueId);
			matchesLeague.setLogo(matchesLeagueExit.getLogo());
		}
		// 保存icon
		if (logo != null) {
			if (logo != null) {// 有图片时上传文件
				Map<String, String> imgPaths = ImgUploadUtil.save(logo, systemName, src);
				matchesLeague.setLogo(imgPaths.get(ImgUploadUtil.KEY_MAP_SRC));
			}
		}
		matchesLeague.setId(matchesLeagueId);
		matchesLeague.setOrganiserId(organiserId);
		matchesLeague.setName(name);
		matchesLeague.setItemsId(itemsId);
		matchesLeague.setCreateDate(new Date());
		matchesLeague.setValid(1);
		matchesLeagueService.save(matchesLeague);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, organiserId);
	}

	/**
	 * 根据主办方和游戏获取赛事列表
	 */
	@RequestMapping("/leaguelistByOrganiserId")
	@ResponseBody
	public JsonResponseMsg list(Long itemsId, Long organiserId) {
		JsonResponseMsg result = new JsonResponseMsg();
		List<MatchesLeague> leagueList = matchesLeagueService.getLeagueListByItemsAndOrgan(itemsId, organiserId);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, leagueList);
	}
}
