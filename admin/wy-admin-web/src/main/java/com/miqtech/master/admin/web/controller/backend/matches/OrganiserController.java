package com.miqtech.master.admin.web.controller.backend.matches;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Lists;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.matches.MatchesLeague;
import com.miqtech.master.entity.matches.MatchesOrganiser;
import com.miqtech.master.entity.matches.MatchesOrganiserGame;
import com.miqtech.master.service.matches.MatchesLeagueService;
import com.miqtech.master.service.matches.MatchesOrganiserGameService;
import com.miqtech.master.service.matches.MatchesOrganiserService;
import com.miqtech.master.thirdparty.util.img.ImgUploadUtil;
import com.miqtech.master.utils.JsonUtils;
import com.miqtech.master.vo.PageVO;

@Controller
@RequestMapping("organiser")
public class OrganiserController extends BaseController {
	@Autowired
	private MatchesOrganiserService matchesOrganiserService;
	@Autowired
	private MatchesLeagueService matchesLeagueService;
	@Autowired
	private MatchesOrganiserGameService matchesOrganiserGameService;

	/**
	 * 主办方列表
	 * 
	 * @throws ParseException
	 */
	@RequestMapping("/list/{page}")
	public ModelAndView list(@PathVariable("page") Integer page) throws ParseException {
		if (page == null) {
			page = 1;
		}
		ModelAndView mv = new ModelAndView("/matches/organiserList");
		PageVO vo = matchesOrganiserService.getOrganiserList(page);
		pageModels(mv, vo.getList(), page, vo.getTotal());
		return mv;
	}

	/**
	 * 主办方新增或编辑
	 */
	@RequestMapping("/edit")
	public ModelAndView edit(String organiserId) {
		ModelAndView mv = new ModelAndView("/matches/organiserEdit");
		Long organiserIdNum = NumberUtils.toLong(organiserId);
		// 查询游戏列表信息
		List<Map<String, Object>> infos = matchesOrganiserGameService.getGameInfoList(organiserIdNum);
		MatchesOrganiser info = matchesOrganiserService.findOne(organiserIdNum);
		mv.addObject("isInsert", 0);
		if (info != null && StringUtils.isNotBlank(info.getLogo())) {
			mv.addObject("isInsert", 1);
		}
		// 转换数据结构
		for (Map<String, Object> gameInfo : infos) {
			gameInfo.put("pId", 0);
			if (gameInfo.get("nocheck").toString().equals("0")) {// 未选
				gameInfo.put("nocheck", true);
			} else {
				gameInfo.put("nocheck", false);
			}
		}
		mv.addObject("info", info);
		mv.addObject("itemList", JsonUtils.objectToString(infos));
		return mv;
	}

	/**
	 * 主办方保存
	 */
	@RequestMapping("/save")
	@ResponseBody
	public JsonResponseMsg save(HttpServletRequest req, String name, String gameIds, Long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		MatchesOrganiser matchesOrganiser = new MatchesOrganiser();
		Date now = new Date();
		MultipartFile logo = Servlets.getMultipartFile(req, "logo");
		String systemName = "wy-web-admin";
		String src = ImgUploadUtil.genFilePath("organiserLogo");
		// 保存icon
		if (id != null && logo == null) {
			MatchesOrganiser match = matchesOrganiserService.findOne(id);
			matchesOrganiser.setLogo(match.getLogo());
		}
		if (logo != null) {
			if (logo != null) {// 有图片时上传文件
				Map<String, String> imgPaths = ImgUploadUtil.save(logo, systemName, src);
				matchesOrganiser.setLogo(imgPaths.get(ImgUploadUtil.KEY_MAP_SRC));
			}
		}
		matchesOrganiser.setName(name);
		matchesOrganiser.setValid(1);
		matchesOrganiser.setCreateDate(now);
		matchesOrganiser.setId(id);
		matchesOrganiserService.save(matchesOrganiser);
		if (StringUtils.isNotBlank(gameIds)) {
			// 将之前的游戏信息设为无效
			List<MatchesOrganiserGame> gameList = Lists.newArrayList();
			if (id != null) {
				List<MatchesOrganiserGame> existList = matchesOrganiserGameService.findAllByOrganiserId(id);
				if (CollectionUtils.isNotEmpty(existList)) {
					for (int m = 0; m < existList.size(); m++) {
						MatchesOrganiserGame matchesOrganiserGame = existList.get(m);
						matchesOrganiserGame.setValid(0);
						gameList.add(matchesOrganiserGame);
					}
				}
			}
			String[] gameId = gameIds.split(",");
			for (int i = 0; i < gameId.length; i++) {
				MatchesOrganiserGame matchesOrganiserGame = new MatchesOrganiserGame();
				matchesOrganiserGame.setItemsId(NumberUtils.toLong(gameId[i]));
				matchesOrganiserGame.setOrganiserId(matchesOrganiser.getId());
				matchesOrganiserGame.setCreateDate(now);
				matchesOrganiserGame.setValid(1);
				gameList.add(matchesOrganiserGame);
			}
			matchesOrganiserGameService.save(gameList);
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 * 赛事主办方删除,附带主办方下所属的所有赛事删除
	 * 
	 * @param organiserId
	 * @return
	 */
	@RequestMapping("/delete")
	@ResponseBody
	public JsonResponseMsg delete(String organiserId) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (!NumberUtils.isNumber(organiserId)) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}
		Long organiserIdNum = NumberUtils.toLong(organiserId);
		MatchesOrganiser matchesOrganiser = matchesOrganiserService.findOne(organiserIdNum);
		matchesOrganiser.setValid(0);
		matchesOrganiserService.save(matchesOrganiser);
		// 修改主办方下所有的游戏置无效
		List<MatchesOrganiserGame> matchesOrganiserGames = matchesOrganiserGameService
				.findAllByOrganiserId(organiserIdNum);
		for (MatchesOrganiserGame matchesOrganiserGame : matchesOrganiserGames) {
			matchesOrganiserGame.setValid(0);
		}
		matchesOrganiserGameService.save(matchesOrganiserGames);
		// 主主办方下所有的赛事置为无效
		List<MatchesLeague> matchesLeagues = matchesLeagueService.findByOrganiserId(organiserIdNum);
		for (MatchesLeague matchesLeague : matchesLeagues) {
			matchesLeague.setValid(0);
		}
		matchesLeagueService.save(matchesLeagues);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

}
