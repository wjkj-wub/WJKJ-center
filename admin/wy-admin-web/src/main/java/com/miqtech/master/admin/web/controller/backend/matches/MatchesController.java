package com.miqtech.master.admin.web.controller.backend.matches;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.activity.ActivityItem;
import com.miqtech.master.entity.matches.Matches;
import com.miqtech.master.entity.matches.MatchesLeague;
import com.miqtech.master.entity.matches.MatchesOrganiser;
import com.miqtech.master.entity.matches.MatchesProcess;
import com.miqtech.master.service.activity.ActivityItemService;
import com.miqtech.master.service.matches.MatchesCenueService;
import com.miqtech.master.service.matches.MatchesLeagueService;
import com.miqtech.master.service.matches.MatchesOrganiserGameService;
import com.miqtech.master.service.matches.MatchesOrganiserService;
import com.miqtech.master.service.matches.MatchesProcessService;
import com.miqtech.master.service.matches.MatchesService;
import com.miqtech.master.thirdparty.util.img.ImgUploadUtil;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.JsonUtils;
import com.miqtech.master.vo.PageVO;

@Controller
@RequestMapping("matches")
public class MatchesController extends BaseController {
	@Autowired
	private MatchesService matchesService;
	@Autowired
	private MatchesProcessService matchesProcessService;
	@Autowired
	private MatchesOrganiserService matchesOrganiserService;
	@Autowired
	private MatchesOrganiserGameService matchesOrganiserGameService;
	@Autowired
	private MatchesLeagueService matchesLeagueService;
	@Autowired
	private ActivityItemService activityItemService;
	@Autowired
	private MatchesCenueService matchesCenueService;

	/**
	 * 列表
	 *
	 * @throws ParseException
	 */
	@RequestMapping("/list/{page}")
	public ModelAndView list(@PathVariable("page") Integer page, String title, Long organiserId, Long itemsId,
			String beginDate, String endDate, Integer state) throws ParseException {
		ModelAndView mv = new ModelAndView("/matches/matchesList");
		if (page == null) {
			page = 1;
		}
		Map<String, Object> params = Maps.newHashMap();
		if (StringUtils.isNotBlank(title)) {
			params.put("title", title);
		}
		if (organiserId != null) {
			params.put("organiserId", organiserId);
		}
		if (itemsId != null) {
			params.put("itemsId", itemsId);
		}
		if (StringUtils.isNotBlank(beginDate)) {
			params.put("beginDate", beginDate);
		}
		if (StringUtils.isNotBlank(endDate)) {
			params.put("endDate", endDate);
		}
		if (state != null) {
			params.put("state", state);
		}
		mv.addObject("params", params);
		// 主办方列表
		List<MatchesOrganiser> organiserList = matchesOrganiserService.getOrganiserList();
		// 游戏列表
		List<ActivityItem> activityItemList = activityItemService.findAll();
		mv.addObject("organiserList", organiserList);
		mv.addObject("activityItemList", activityItemList);
		PageVO vo = matchesService.getMatchesList(page, params);
		pageModels(mv, vo.getList(), page, vo.getTotal());
		return mv;
	}

	/**
	 * 赛事新增页面
	 */
	@RequestMapping("/edit")
	public ModelAndView edit(Long matchesId) {
		ModelAndView mv = new ModelAndView("/matches/matchesEdit");
		// 查询主办方信息
		List<MatchesOrganiser> organiserList = matchesOrganiserService.getOrganiserList();
		// 查询赛事信息
		mv.addObject("iconIsInsert", 0);
		if (matchesId != null) {
			Matches matches = matchesService.findById(matchesId);
			if (matches != null && matches.getIcon() != null) {
				mv.addObject("iconIsInsert", 1);
			}
			List<Map<String, Object>> processList = matchesProcessService.findByMatchesId(matchesId);
			if (matches != null && matches.getOrganiserId() != null) {
				List<Map<String, Object>> gameList = matchesOrganiserGameService.getGameList(matches.getOrganiserId());
				mv.addObject("gameList", gameList);
				if (matches != null && matches.getItemsId() != null) {
					List<MatchesLeague> leagueList = matchesLeagueService
							.getLeagueListByItemsAndOrgan(matches.getItemsId(), matches.getOrganiserId());
					mv.addObject("leagueList", leagueList);
				}
			}
			mv.addObject("matches", matches);
			mv.addObject("processList", JSONObject.toJSONString(processList));
		}
		mv.addObject("organiserList", organiserList);
		return mv;
	}

	/**
	 * 赛事详情编辑页面页面
	 */
	@RequestMapping("/detailEdit")
	public ModelAndView detailEdit(HttpServletRequest req, Matches matches, Integer type, String processNames,
			String startTimeStrs, String endTimeStrs, String processIds, String delProcessIds) {
		ModelAndView mv = new ModelAndView("/matches/matchesDetailEdit");
		MultipartFile icon = Servlets.getMultipartFile(req, "iconFile");
		String systemName = "wy-web-admin";
		String src = ImgUploadUtil.genFilePath("matches");
		// 保存icon
		if (icon != null && icon.getSize() > 0) {// 有图片时上传文件
			Map<String, String> imgPaths = ImgUploadUtil.save(icon, systemName, src);
			matches.setIcon(imgPaths.get(ImgUploadUtil.KEY_MAP_SRC));
		}
		if (icon != null && icon.getSize() > 0) {
			matchesService.save(matches);
		}
		mv.addObject("matches", matches);
		mv.addObject("processNames", processNames);
		mv.addObject("startTimeStrs", startTimeStrs);
		mv.addObject("endTimeStrs", endTimeStrs);
		mv.addObject("processIds", processIds);
		mv.addObject("delProcessIds", delProcessIds);
		mv.addObject("type", type);
		return mv;
	}

	/**
	 * 赛事奖励制度等保存
	 *
	 * @throws ParseException
	 */
	@RequestMapping("/detailSave")
	public ModelAndView detailSave(Matches match, String summary, String rule, String reward, String processNames,
			String startTimeStrs, String endTimeStrs, String processIds, String delProcessIds) throws ParseException {
		ModelAndView mv = new ModelAndView("/matches/matchesEdit");
		Matches matches = new Matches();
		Long matchesId = match.getId();
		mv.addObject("iconIsInsert", 0);
		if (matchesId != null) {
			matches = matchesService.findById(matchesId);
			if (matches != null && matches.getIcon() != null) {
				mv.addObject("iconIsInsert", 1);
			}
			matches.setValid(1);
			matches.setCreateDate(new Date());
		}
		if (StringUtils.isNotBlank(summary)) {
			matches.setSummary(summary);
		}
		if (StringUtils.isNotBlank(rule)) {
			matches.setRule(rule);
		}
		if (StringUtils.isNotBlank(reward)) {
			matches.setReward(reward);
		}
		matchesService.save(matches);
		match.setId(matches.getId());
		match.setSummary(matches.getSummary());
		match.setRule(matches.getRule());
		match.setReward(matches.getReward());
		mv.addObject("matches", match);
		// 查询主办方信息
		List<MatchesOrganiser> organiserList = matchesOrganiserService.getOrganiserList();
		mv.addObject("organiserList", organiserList);
		if (match != null && match.getOrganiserId() != null) {
			List<Map<String, Object>> gameList = matchesOrganiserGameService.getGameList(match.getOrganiserId());
			mv.addObject("gameList", gameList);
			if (match != null && match.getItemsId() != null) {
				List<MatchesLeague> leagueList = matchesLeagueService.getLeagueListByItemsAndOrgan(match.getItemsId(),
						match.getOrganiserId());
				mv.addObject("leagueList", leagueList);
			}
		}
		if (StringUtils.isNotBlank(processNames)) {
			String[] processName = processNames.split(",");
			String[] startTimeStr = startTimeStrs.split(",");
			String[] endTimeStr = endTimeStrs.split(",");
			String[] processId = processIds.split(",");
			List<Map<String, Object>> matchesProcessList = Lists.newArrayList();
			// 更新赛程
			for (int i = 0; i < processName.length; i++) {
				Map<String, Object> map = new HashMap<>();
				if (!processName[i].equals("1")) {
					map.put("name", processName[i]);
				}
				if (StringUtils.isNotBlank(startTimeStr[i]) && !startTimeStr[i].equals("1")) {
					map.put("start_date", startTimeStr[i]);
				}
				if (StringUtils.isNotBlank(endTimeStr[i]) && !endTimeStr[i].equals("1")) {
					map.put("end_date", endTimeStr[i]);
				}
				if (StringUtils.isNotBlank(processId[i]) && !processId[i].equals("-1")) {
					map.put("id", processId[i]);
				}
				if (MapUtils.isNotEmpty(map)) {
					matchesProcessList.add(map);
				}
			}
			if (CollectionUtils.isNotEmpty(matchesProcessList)) {
				mv.addObject("processList", JsonUtils.objectToString(matchesProcessList));
			}
		}
		mv.addObject("delProcessIds", delProcessIds);
		return mv;
	}

	/**
	 * 赛事保存
	 *
	 * @throws ParseException
	 */
	@RequestMapping("/save")
	@ResponseBody
	public JsonResponseMsg save(HttpServletRequest req, Matches matches, String processNames, String startTimeStrs,
			String endTimeStrs, String processIds, String delProcessIds, Integer type) throws ParseException {
		JsonResponseMsg result = new JsonResponseMsg();
		if (matches.getId() != null) {
			if (matches.getState() != null && matches.getState() == 1) {
				if (StringUtils.isBlank(matches.getTitle()) || StringUtils.isBlank(matches.getPrize())
						|| matches.getOrganiserId() == null || matches.getItemsId() == null
						|| matches.getLeagueId() == null || StringUtils.isBlank(processNames)
						|| StringUtils.isBlank(startTimeStrs) || StringUtils.isBlank(endTimeStrs)) {
					return result.fill(CommonConstant.CODE_ERROR_PARAM, "该赛事已上线，请勿随意更改");
				}
				String[] processName = processNames.split(",");
				String[] startTimeStr = startTimeStrs.split(",");
				String[] endTimeStr = endTimeStrs.split(",");
				for (int i = 0; i < processName.length; i++) {
					if (processName[i].equals("1") || startTimeStr[i].equals("1") || endTimeStr[i].equals("1")) {
						return result.fill(CommonConstant.CODE_ERROR_PARAM, "该赛事已上线，请勿随意更改");
					}
				}
			}
			Matches matches1 = matchesService.findById(matches.getId());
			matches.setSummary(matches1.getSummary());
			matches.setRule(matches1.getRule());
			matches.setReward(matches1.getReward());
			matches.setIcon(matches1.getIcon());
		}
		if (type != null && type == 2) {
			if (matches.getId() == null) {
				return result.fill(CommonConstant.CODE_ERROR_PARAM, "赛事填写不完整");
			}
			if (StringUtils.isBlank(matches.getSummary()) || StringUtils.isBlank(matches.getReward())
					|| StringUtils.isBlank(matches.getRule())) {
				return result.fill(CommonConstant.CODE_ERROR_PARAM, "赛事填写不完整");
			}
			matches.setIsDraft(0);
		} else if (type != null && type == 1) {
			matches.setIsDraft(1);
		}
		if (StringUtils.isNotBlank(startTimeStrs)) {
			String[] startTimeStr = startTimeStrs.split(",");
			String[] endTimeStr = endTimeStrs.split(",");
			for (int i = 0; i < startTimeStr.length - 1; i++) {
				Date end = DateUtils.stringToDateYyyyMMdd(endTimeStr[i]);
				Date startNow = DateUtils.stringToDateYyyyMMdd(startTimeStr[i]);
				Date start = DateUtils.stringToDateYyyyMMdd(startTimeStr[i + 1]);
				if (!end.before(start)) {
					return result.fill(CommonConstant.CODE_ERROR_PARAM, "赛程时间填写不正确");
				}
				if (end.before(startNow)) {
					return result.fill(CommonConstant.CODE_ERROR_PARAM, "赛程时间填写不正确");
				}
			}
		}

		Date now = new Date();
		matches.setValid(1);
		matches.setCreateDate(now);
		MultipartFile icon = Servlets.getMultipartFile(req, "iconFile");
		String systemName = "wy-web-admin";
		String src = ImgUploadUtil.genFilePath("matches");
		// 保存icon
		if (icon != null) {// 有图片时上传文件
			Map<String, String> imgPaths = ImgUploadUtil.save(icon, systemName, src);
			matches.setIcon(imgPaths.get(ImgUploadUtil.KEY_MAP_SRC));
		}
		matchesService.save(matches);
		if (StringUtils.isNotBlank(processNames)) {
			String[] processName = processNames.split(",");
			String[] startTimeStr = startTimeStrs.split(",");
			String[] endTimeStr = endTimeStrs.split(",");
			String[] processId = processIds.split(",");
			List<MatchesProcess> matchesProcessList = Lists.newArrayList();
			// 删除赛程
			if (StringUtils.isNotBlank(delProcessIds)) {
				String[] delProcessId = delProcessIds.split(",");
				for (String element : delProcessId) {
					if (NumberUtils.isNumber(element)) {
						MatchesProcess matchesProcess = new MatchesProcess();
						matchesProcess.setId(NumberUtils.toLong(element.toString()));
						matchesProcess.setValid(0);
						matchesProcessList.add(matchesProcess);
					}
				}
			}
			// 更新赛程
			for (int i = 0; i < processName.length; i++) {
				MatchesProcess matchesProcess = new MatchesProcess();
				if (!processName[i].equals("1")) {
					matchesProcess.setName(processName[i]);
				}
				if (StringUtils.isNotBlank(startTimeStr[i]) && !startTimeStr[i].equals("1")) {
					matchesProcess.setStartDate(DateUtils.stringToDateYyyyMMdd(startTimeStr[i]));
				}
				if (StringUtils.isNotBlank(endTimeStr[i]) && !endTimeStr[i].equals("1")) {
					String end = endTimeStr[i] + " 23:59:59";
					Date endDate = DateUtils.stringToDate(end, "yyyy-MM-dd HH:mm:ss");
					matchesProcess.setEndDate(endDate);
				}
				if (StringUtils.isNotBlank(processId[i]) && !processId[i].equals("-1")) {
					matchesProcess.setId(NumberUtils.toLong(processId[i]));
				}
				if (matchesProcess != null) {
					matchesProcess.setMatchId(matches.getId());
					matchesProcess.setCreateDate(now);
					matchesProcess.setValid(1);
					matchesProcessList.add(matchesProcess);
				}
			}
			if (CollectionUtils.isNotEmpty(matchesProcessList)) {
				matchesProcessService.save(matchesProcessList);
			}
		}
		Map<String, Object> map = Maps.newHashMap();
		map.put("type", type);
		map.put("id", matches.getId());
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, map);
	}

	@RequestMapping("/changeStatus")
	@ResponseBody
	public JsonResponseMsg changeStatus(Long matchesId, Integer type) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (type != null && type == 1) {
			Matches matches = matchesService.findById(matchesId);
			if (matches.getIsDraft() != null && matches.getIsDraft() == 1) {
				return result.fill(CommonConstant.CODE_ERROR_PARAM, "该赛事为草稿，不能上线");
			}
			if (StringUtils.isBlank(matches.getTitle()) || StringUtils.isBlank(matches.getPrize())
					|| matches.getOrganiserId() == null || matches.getItemsId() == null || matches.getLeagueId() == null
					|| StringUtils.isBlank(matches.getIcon()) || StringUtils.isBlank(matches.getSummary())
					|| StringUtils.isBlank(matches.getReward()) || StringUtils.isBlank(matches.getRule())) {
				return result.fill(CommonConstant.CODE_ERROR_PARAM, "该赛事信息不完整,不能上线");
			}
			List<Map<String, Object>> processList = matchesProcessService.findByMatchesId(matches.getId());
			if (CollectionUtils.isEmpty(processList)) {
				return result.fill(CommonConstant.CODE_ERROR_PARAM, "该赛事信息不完整,不能上线");
			}
			int count = matchesCenueService.getCenueCountByMatchesId(matchesId);
			if (count == 0) {
				return result.fill(CommonConstant.CODE_ERROR_PARAM, "赛点信息不能为空");
			}
			matches.setState(1);
			matchesService.save(matches);
		} else {
			Matches matches = matchesService.findById(matchesId);
			matches.setState(0);
			matchesService.save(matches);
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}
}
