package com.miqtech.master.admin.web.controller.backend.matches;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.common.SystemArea;
import com.miqtech.master.entity.matches.Matches;
import com.miqtech.master.entity.matches.MatchesCenue;
import com.miqtech.master.entity.matches.MatchesProcess;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.service.matches.MatchesCenueService;
import com.miqtech.master.service.matches.MatchesProcessService;
import com.miqtech.master.service.matches.MatchesService;
import com.miqtech.master.service.netbar.NetbarInfoService;
import com.miqtech.master.service.system.SystemAreaService;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.vo.PageVO;

@Controller
@RequestMapping("matchesCenue")
public class MatchesCenueController extends BaseController {
	private static final Logger logger = LoggerFactory.getLogger("MatchesCenueController.class");
	@Autowired
	private NetbarInfoService netbarInfoService;
	@Autowired
	private SystemAreaService systemAreaService;
	@Autowired
	private MatchesProcessService matchesProcessService;
	@Autowired
	private MatchesCenueService matchesCenueService;
	@Autowired
	private MatchesService matchesService;
	@Autowired
	private StringRedisOperateService objectRedisOperateService;

	/**
	 * 赛点编辑页面
	 * 
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping("/cenueEdit/{page}")
	public ModelAndView cenueEdit(HttpServletRequest req, HttpServletResponse res, Long matchesId, Long processId,
			@PathVariable("page") Integer page, String info, String checkedId, String currentNetbarId,
			String netbarName, String proviceName, String cityName, String townName, Long currentProcessId,
			Integer isFlag) throws UnsupportedEncodingException {
		ModelAndView mv = new ModelAndView("/matches/cenueEdit");
		if (page == null || page < 0) {
			page = 1;
		}
		List<Map<String, Object>> processList = matchesProcessService.findByMatchesId(matchesId);
		logger.info("isFlag的值" + isFlag);
		logger.info("processList的长度" + processList.size());
		// 网吧列表
		PageVO vo = netbarInfoService.getNetbarList(page, netbarName, proviceName, cityName, townName);
		if (processId == null) {
			if (CollectionUtils.isNotEmpty(processList)) {
				long process = NumberUtils.toLong(processList.get(0).get("id").toString());
				processId = process;
				currentProcessId = process;
			}
		}
		logger.info("processId" + processId);
		if (processId != null) {
			MatchesProcess matchesProcess = matchesProcessService.findById(processId);
			mv.addObject("startDate", DateUtils.dateToString(matchesProcess.getStartDate(), "yyyy-MM-dd"));
			mv.addObject("endDate", DateUtils.dateToString(matchesProcess.getEndDate(), "yyyy-MM-dd"));
			String cookieKey = "info_" + processId;
			String value = objectRedisOperateService.getData(cookieKey);
			if ((isFlag != null && isFlag == 1) || value.equals("1")) {
				if (CollectionUtils.isNotEmpty(processList)) {
					for (int i = 0; i < processList.size(); i++) {
						String id = processList.get(i).get("id").toString();
						objectRedisOperateService.setData("info_" + id, "1");
					}
				}
				value = "";
				List<Map<String, Object>> list = matchesCenueService.findByProcessId(processId);
				if (CollectionUtils.isNotEmpty(list)) {
					Map<String, Object> matchesCenue = list.get(0);
					value += ":" + matchesCenue.get("netbar_id") + "," + matchesCenue.get("fightDates") + ","
							+ matchesCenue.get("division") + ":";
					for (int i = 1; i < list.size(); i++) {
						matchesCenue = list.get(i);
						value += matchesCenue.get("netbar_id") + "," + matchesCenue.get("fightDates") + ","
								+ matchesCenue.get("division") + ":";
					}
				}
				objectRedisOperateService.setData(cookieKey, value);
				logger.info("点击下一步之后存入redis中的值" + value);
			}
			logger.info("当前页checkedId" + checkedId);
			logger.info("当前页currentNetbarId" + currentNetbarId);
			if (StringUtils.isNotBlank(checkedId)) {
				String[] checked = checkedId.toString().split(",");
				if (StringUtils.isNotBlank(currentNetbarId)) {
					for (int i = 1; i < checked.length; i++) {
						String id = ":" + checked[i] + ",";
						if (!StringUtils.contains(currentNetbarId, id)) {
							value = StringUtils.replace(value, id, ":-1,");
						}
					}
					String[] currentNetbar = currentNetbarId.split(":");
					for (int i = 1; i < currentNetbar.length; i++) {
						String[] addIdArray = currentNetbar[i].split(",");
						String addId = ":" + addIdArray[0] + ",";
						if (value.indexOf(addId) < 0) {
							String addIds = addIdArray[0] + "," + addIdArray[1] + "," + addIdArray[2] + ":";
							value += addIds;
						} else {
							value = StringUtils.replace(value, addId, ":-1,");
							String addIds = addIdArray[0] + "," + addIdArray[1] + "," + addIdArray[2] + ":";
							value += addIds;
						}
					}
				} else {
					// 将checkId里的id全部从cookie里移除
					for (int i = 1; i < checked.length; i++) {
						String[] checkeds = checked[i].split(",");
						String removeIds = ":" + checkeds[0] + ",";
						value.replaceAll(removeIds, ":-1,");
					}

				}
			} else {
				// 将currentNetbarId里的id全部加入到cookie里
				if (StringUtils.isNotBlank(currentNetbarId)) {
					if (StringUtils.isBlank(value)) {
						value = currentNetbarId;
					} else {
						value += currentNetbarId.substring(1, currentNetbarId.length());
					}
				}
			}
			if (StringUtils.isNotBlank(value)) {
				logger.info("当前页保存之后的redis值" + value);
				objectRedisOperateService.setData(cookieKey, value);
			}
			if (currentProcessId != processId) {
				MatchesProcess matchesProcess1 = matchesProcessService.findById(currentProcessId);
				mv.addObject("startDate", DateUtils.dateToString(matchesProcess1.getStartDate(), "yyyy-MM-dd"));
				mv.addObject("endDate", DateUtils.dateToString(matchesProcess1.getEndDate(), "yyyy-MM-dd"));
				String cookieKey1 = "info_" + currentProcessId;
				String value1 = objectRedisOperateService.getData(cookieKey1);
				if (StringUtils.isBlank(value1) || value1.equals("1")) {
					value1 = "";
					List<Map<String, Object>> list = matchesCenueService.findByProcessId(currentProcessId);
					if (CollectionUtils.isNotEmpty(list)) {
						Map<String, Object> matchesCenue = list.get(0);
						value1 += ":" + matchesCenue.get("netbar_id") + "," + matchesCenue.get("fightDates") + ","
								+ matchesCenue.get("division") + ":";
						for (int i = 1; i < list.size(); i++) {
							matchesCenue = list.get(i);
							value1 += matchesCenue.get("netbar_id") + "," + matchesCenue.get("fightDates") + ","
									+ matchesCenue.get("division") + ":";
						}
					}
					objectRedisOperateService.setData(cookieKey1, value1);
					logger.info("切换赛程之后的赛程的redis值" + value1);
				}
				List<Map<String, Object>> netbarList = vo.getList();
				String checkedIds = "";
				if (StringUtils.isNotBlank(value1)) {
					for (int i = 0; i < netbarList.size(); i++) {
						Map<String, Object> map = netbarList.get(i);
						String netbarId = ":" + map.get("id") + ",";
						if (value1.indexOf(netbarId) >= 0) {
							if (StringUtils.isBlank(checkedIds)) {
								checkedIds = ",";
							}
							map.put("checked", 1);
							String targets = value1.substring(value1.indexOf(netbarId));
							String[] target = targets.split(":");
							String[] ta = target[1].split(",");
							checkedIds += map.get("id") + ",";
							map.put("fightDate", Arrays.asList(ta[1].split("；")));
							map.put("division", ta[2]);
						}
						netbarList.set(i, map);
					}
				}
				vo.setList(netbarList);
				mv.addObject("checkedIds", checkedIds);
			} else {
				List<Map<String, Object>> netbarList = vo.getList();
				String checkedIds = "";
				if (StringUtils.isNotBlank(value)) {
					for (int i = 0; i < netbarList.size(); i++) {
						Map<String, Object> map = netbarList.get(i);
						String netbarId = ":" + map.get("id") + ",";
						if (value.indexOf(netbarId) >= 0) {
							if (StringUtils.isBlank(checkedIds)) {
								checkedIds = ",";
							}
							map.put("checked", 1);
							String targets = value.substring(value.indexOf(netbarId));
							String[] target = targets.split(":");
							String[] ta = target[1].split(",");
							checkedIds += map.get("id") + ",";
							map.put("fightDate", Arrays.asList(ta[1].split("；")));
							map.put("division", ta[2]);
						}
						netbarList.set(i, map);
					}
					vo.setList(netbarList);
				}
				mv.addObject("checkedIds", checkedIds);
			}
		}
		Map<String, Object> params = Maps.newHashMap();
		params.put("netbarName", netbarName);
		params.put("proviceName", proviceName);
		params.put("cityName", cityName);
		params.put("townName", townName);
		params.put("matchesId", matchesId);
		params.put("processId", currentProcessId);
		// 一级地区信息
		List<SystemArea> areaList = systemAreaService.queryValidRoot();
		// 子级地区信息
		if (StringUtils.isNotBlank(proviceName)) {
			List<SystemArea> cityList = systemAreaService.queryValidChildren(proviceName);
			mv.addObject("cityList", cityList);
		}
		if (StringUtils.isNotBlank(cityName)) {
			List<SystemArea> townList = systemAreaService.queryValidChildren(cityName);
			mv.addObject("townList", townList);
		}
		mv.addObject("params", params);
		mv.addObject("processList", processList);
		mv.addObject("areaList", areaList);
		pageModels(mv, vo.getList(), page, vo.getTotal());
		return mv;
	}

	@RequestMapping("/saveLine")
	@ResponseBody
	public JsonResponseMsg saveLine(HttpServletRequest request, HttpServletResponse response, String currentNetbarId,
			Long processId, String checkedId, Integer type, boolean flag)
			throws ParseException, UnsupportedEncodingException {
		JsonResponseMsg result = new JsonResponseMsg();
		String cookieKey = "info_" + processId;
		if (flag) {
			Map<String, Object> map = new HashMap<>();
			String cookieValues = objectRedisOperateService.getData(cookieKey);
			if (StringUtils.isNotBlank(cookieValues) && cookieValues.equals("1")) {
				return result.fill(CommonConstant.CODE_PERMISSION_DENY, "请勿重新点击");
			}
			if (StringUtils.isNotBlank(cookieValues) && !StringUtils.startsWith(cookieValues, ":")) {
				cookieValues = ":" + cookieValues;
			}
			String[] currentId = currentNetbarId.split(":");
			for (int i = 1; i < currentId.length; i++) {
				String[] addIdArray = currentId[i].split(",");
				String addId = ":" + addIdArray[0] + ",";
				String addIds = addIdArray[0] + "," + addIdArray[1] + "," + addIdArray[2] + ":";
				if (StringUtils.isNotBlank(cookieValues) && cookieValues.indexOf(addId) >= 0) {
					cookieValues = StringUtils.replace(cookieValues, addId, ":-1,");
				}
				cookieValues += addIds;
			}
			if (StringUtils.isNotBlank(checkedId)) {
				String[] checkedIds = checkedId.split(",");
				for (int i = 0; i < checkedIds.length; i++) {
					if (NumberUtils.isNumber(checkedIds[i])) {
						String containsKey = ":" + checkedIds[i] + ",";
						if (!StringUtils.contains(currentNetbarId, containsKey)) {
							cookieValues = cookieValues.replace(containsKey, ":-1,");
						}
					}
				}
			}
			logger.info("确认发布之后redis中的值" + cookieValues);
			objectRedisOperateService.setData(cookieKey, cookieValues);
			// 查询赛点数及网吧数
			String[] cookieVaue = cookieValues.split(":");
			int cenueCount = 0;
			int provinceCount = 0;
			String netbarIds = "";
			for (int i = 0; i < cookieVaue.length; i++) {
				if (StringUtils.isNotBlank(cookieVaue[i])) {
					String[] netbarValue = cookieVaue[i].split(",");
					if (NumberUtils.toLong(netbarValue[0]) > 0) {
						String[] netbarDateValue = netbarValue[1].split("；");
						for (int m = 0; m < netbarDateValue.length; m++) {
							cenueCount++;
						}
						netbarIds += netbarValue[0] + ",";
					}
				}
			}
			// 查询省份数
			if (StringUtils.isNotBlank(netbarIds)) {
				String[] netbarIdsArray = netbarIds.split(",");
				String netbat = "";
				for (int m = 0; m < netbarIdsArray.length; m++) {
					String netbarId = netbarIdsArray[m];
					if (NumberUtils.isNumber(netbarId)) {
						netbat += NumberUtils.toLong(netbarId) + ",";
					}
				}
				if (StringUtils.isNotBlank(netbat)) {
					netbat = netbat.substring(0, netbat.length() - 1);
					provinceCount = netbarInfoService.getProviceCount(netbat);
				}
			}
			MatchesProcess matchProcess = matchesProcessService.findById(processId);
			map.put("provinceCount", provinceCount);
			map.put("cenueCount", cenueCount);
			Long matcheId = matchProcess.getMatchId();
			List<Map<String, Object>> processList = matchesProcessService.findByMatchesId(matcheId);
			if (CollectionUtils.isNotEmpty(processList)) {
				for (int i = 0; i < processList.size(); i++) {
					Map<String, Object> processMap = processList.get(i);
					Date startDate = (Date) processMap.get("start_date");
					processMap.put("start_date", DateUtils.dateToString(startDate, "yyyy-MM-dd"));
					Date endDate = (Date) processMap.get("end_date");
					processMap.put("end_date", DateUtils.dateToString(endDate, "yyyy-MM-dd"));
					processList.set(i, processMap);
				}
			}
			map.put("processList", processList);
			Map<String, Object> matchesMap = matchesService.getInfo(matcheId);
			map.put("matcheName", matchesMap.get("title"));
			map.put("organiserName", matchesMap.get("organiserName"));
			map.put("itemName", matchesMap.get("itemName"));
			map.put("type", type);
			map.put("processId", processId);
			return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, map);
		}
		String cookieValues = objectRedisOperateService.getData(cookieKey);
		if (cookieValues.equals("1")) {
			return result.fill(CommonConstant.CODE_PERMISSION_DENY, "请勿重新点击");
		}
		logger.info("存到数据库中的redis值" + cookieValues);
		Date now = new Date();
		List<MatchesCenue> cenueList = Lists.newArrayList();
		List<MatchesCenue> existList = matchesCenueService.findEntityByProcessId(processId);
		if (CollectionUtils.isNotEmpty(existList)) {
			for (int i = 0; i < existList.size(); i++) {
				MatchesCenue matchesCenue = existList.get(i);
				matchesCenue.setValid(0);
				cenueList.add(matchesCenue);
			}
		}
		if (StringUtils.isNotBlank(cookieValues)) {
			String[] cookieVaue = cookieValues.split(":");

			for (int i = 0; i < cookieVaue.length; i++) {
				if (StringUtils.isNotBlank(cookieVaue[i])) {
					String[] netbarValue = cookieVaue[i].split(",");
					if (!netbarValue[0].equals("-1") && NumberUtils.isNumber(netbarValue[0])
							&& netbarValue.length == 3) {
						String[] netbarDateValue = netbarValue[1].split("；");
						for (int m = 0; m < netbarDateValue.length; m++) {
							MatchesCenue matchesCenue = new MatchesCenue();
							matchesCenue.setNetbarId(NumberUtils.toLong(netbarValue[0]));
							matchesCenue.setFightDate(DateUtils.stringToDateYyyyMMdd(netbarDateValue[m]));
							matchesCenue.setDivision(netbarValue[2]);
							matchesCenue.setMatchProcessId(processId);
							matchesCenue.setValid(1);
							matchesCenue.setCreateDate(now);
							cenueList.add(matchesCenue);
						}
					}
				}
			}
			if (CollectionUtils.isNotEmpty(cenueList)) {
				matchesCenueService.save(cenueList);
			}
		}
		if (type != null && type == 2) {
			Matches matches = matchesService.findById(matchesProcessService.findById(processId).getMatchId());
			matches.setState(1);
			matches.setUpdateDate(now);
			matchesService.save(matches);
		}
		MatchesProcess matchesProcess = matchesProcessService.findById(processId);
		List<Map<String, Object>> processList = matchesProcessService.findByMatchesId(matchesProcess.getMatchId());
		if (CollectionUtils.isNotEmpty(processList)) {
			for (int i = 0; i < processList.size(); i++) {
				Map<String, Object> map = processList.get(i);
				String key = "info_" + map.get("id");
				if (StringUtils.isNotBlank(objectRedisOperateService.getData(key))) {
					objectRedisOperateService.setData(key, "1");
				}
			}
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

}
