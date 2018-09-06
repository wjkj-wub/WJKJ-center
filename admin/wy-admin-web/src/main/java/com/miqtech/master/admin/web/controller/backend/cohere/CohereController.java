package com.miqtech.master.admin.web.controller.backend.cohere;

import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.consts.CacheKeyConstant;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.cohere.CohereActivity;
import com.miqtech.master.entity.cohere.CohereDebris;
import com.miqtech.master.entity.cohere.CoherePrize;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.service.cohere.CohereActivityService;
import com.miqtech.master.service.cohere.CohereDebrisService;
import com.miqtech.master.service.cohere.CoherePrizeService;
import com.miqtech.master.service.user.UserInfoService;
import com.miqtech.master.thirdparty.util.img.ImgUploadUtil;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.ExcelUtils;
import com.miqtech.master.vo.PageVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 抢皮肤管理
 * 
 * @author renchen
 *
 */
@Controller
@RequestMapping("cohere/activity")
public class CohereController extends BaseController {

	@Autowired
	private CohereActivityService cohereActivityService;
	@Autowired
	private CohereDebrisService cohereDebrisService;
	@Autowired
	private CoherePrizeService coherePrizeService;
	@Autowired
	private JedisConnectionFactory redisConnectionFactory;
	@Autowired
	private UserInfoService userInfoService;

	/**
	 * 管理首页
	 * 
	 * @param page
	 * @param title
	 * @param startTimeBegin
	 * @param startTimeEnd
	 * @param state
	 * @return
	 */
	@RequestMapping("/list/{page}")
	public ModelAndView index(@PathVariable("page") Integer page, String findTitle, String startTimeBegin,
			String startTimeEnd, String state) {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("/cohere/cohereActivityList");
		PageVO vo = new PageVO();
		vo = cohereActivityService.getActivityInfoForWeb(page, findTitle, startTimeBegin, startTimeEnd, state);
		mv.addObject("list", vo.getList());
		mv.addObject("currentPage", page);
		Map<String, Object> params = new HashMap<String, Object>(16);
		params.put("startTimeBegin", startTimeBegin);
		params.put("startTimeEnd", startTimeEnd);
		params.put("state", state);
		params.put("findTitle", findTitle);
		mv.addObject("params", params);
		mv.addObject("isLastPage", vo.getIsLast());// 0已到最后一页 1可以加载下一页
		mv.addObject("totalCount", vo.getTotal());
		mv.addObject("totalPage", Math.ceil(vo.getTotal() / 5.0));
		return mv;
	}

	/**
	 * 新建活动页面
	 * 
	 * @param activityId
	 * @return
	 */
	@RequestMapping("/edit/activity")
	public ModelAndView editActivity() {
		ModelAndView mv = new ModelAndView();
		Map<String, String> params = Maps.newHashMap();
		params.put("state", "0");
		mv.addObject("params", params);
		mv.setViewName("/cohere/cohereActivity");
		return mv;
	}

	/**
	 * 新建|编辑活动概率页面
	 * 
	 * @param activityId
	 * @return
	 */
	@RequestMapping("/edit/probability")
	public ModelAndView editProbability(Long activityId) {
		ModelAndView mv = new ModelAndView();
		Map<String, Object> params = Maps.newHashMap();
		params.put("state", "0");
		if (activityId != null) {
			params.put("activityId", activityId.toString());
			List<Map<String, Object>> debrisList = cohereDebrisService.getSomeByActivityId(activityId);
			List<Map<String, Object>> prizesList = coherePrizeService.getSomeByActivityId(activityId);
			params.put("debrisOneNum", debrisList.get(0).get("counts"));
			params.put("debrisOneProbability", debrisList.get(0).get("probability"));
			params.put("debrisTwoNum", debrisList.get(1).get("counts"));
			params.put("debrisTwoProbability", debrisList.get(1).get("probability"));
			params.put("debrisThreeNum", debrisList.get(2).get("counts"));
			params.put("debrisThreeProbability", debrisList.get(2).get("probability"));
			params.put("debrisFourNum", debrisList.get(3).get("counts"));
			params.put("debrisFourProbability", debrisList.get(3).get("probability"));

			params.put("prizeOneNum", prizesList.get(0).get("counts"));
			params.put("prizeOneProbability", prizesList.get(0).get("probability"));
			params.put("prizeTwoNum", prizesList.get(1).get("counts"));
			params.put("prizeTwoProbability", prizesList.get(1).get("probability"));
			params.put("prizeThreeNum", prizesList.get(2).get("counts"));
			params.put("prizeThreeProbability", prizesList.get(2).get("probability"));
			params.put("prizeFourNum", prizesList.get(3).get("counts"));
			params.put("prizeFourProbability", prizesList.get(3).get("probability"));

			params.put("prizeOneType", prizesList.get(0).get("type"));
			params.put("prizeOneValue", prizesList.get(0).get("value"));
			params.put("prizeTwoType", prizesList.get(1).get("type"));
			params.put("prizeTwoValue", prizesList.get(1).get("value"));
			params.put("prizeThreeType", prizesList.get(2).get("type"));
			params.put("prizeThreeValue", prizesList.get(2).get("value"));
			params.put("prizeFourType", prizesList.get(3).get("type"));
			params.put("prizeFourValue", prizesList.get(3).get("value"));

			CohereActivity cohereActivity = cohereActivityService.getCohereActivitybyId(activityId);
			params.put("activityBeginTime", cohereActivity.getBeginTime());
			params.put("activityEndTime", cohereActivity.getEndTime());
			params.put("title", cohereActivity.getTitle());
			// 更新操作时间
			cohereActivityService.invalidCacheActivityInfo(activityId);
		}
		mv.addObject("params", params);
		mv.setViewName("/cohere/cohereActivityProbability");
		return mv;
	}

	/**
	 * 保存活动概率
	 * 
	 * @param activityId
	 * @param type
	 *            1保存不开启活动 2保存开启活动
	 * @return
	 */
	@RequestMapping("/save/probability")
	@ResponseBody
	public JsonResponseMsg saveProbability(HttpServletRequest req, Long activityId, Integer type) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (activityId == null) {
			return null;
		}
		List<String> debrisContents = new ArrayList<>(); // 碎片内容
		debrisContents.add(req.getParameter("debrisOneNum"));
		debrisContents.add(req.getParameter("debrisTwoNum"));
		debrisContents.add(req.getParameter("debrisThreeNum"));
		debrisContents.add(req.getParameter("debrisFourNum"));
		debrisContents.add(req.getParameter("debrisOneProbability"));
		debrisContents.add(req.getParameter("debrisTwoProbability"));
		debrisContents.add(req.getParameter("debrisThreeProbability"));
		debrisContents.add(req.getParameter("debrisFourProbability"));
		List<CohereDebris> list = cohereDebrisService.findByActivityId(activityId);
		for (int i = 0; i < list.size(); i++) {
			list.get(i).setProbability(NumberUtils.toInt(debrisContents.get(i + 4)));
			list.get(i).setNum(i + 1);
			// 概率置于redis
			RedisAtomicInteger debrisCount = new RedisAtomicInteger(
					CacheKeyConstant.COHERE_DEBRIS_COUNT + list.get(i).getId(), redisConnectionFactory);
			if (NumberUtils.toInt(debrisContents.get(i)) == -99) {
				debrisCount.set(-99);
				list.get(i).setCounts(NumberUtils.toInt(debrisContents.get(i)));
			} else {
				if (debrisCount.get() == -99) {
					return result.fill(-1, "碎片" + (i + 1) + "数量设置错误,只能设置比原先更多");
				}
				if (NumberUtils.toInt(debrisContents.get(i)) < list.get(i).getCounts()) {
					return result.fill(-1, "碎片" + (i + 1) + "数量设置错误,只能设置比原先更多");
				} else {
					debrisCount.addAndGet(NumberUtils.toInt(debrisContents.get(i)) - list.get(i).getCounts());
					list.get(i).setCounts(NumberUtils.toInt(debrisContents.get(i)));
				}
			}
		}
		cohereDebrisService.saveOrUpdate(list, activityId); // 批量保存碎片信息

		List<String> prizeContents = new ArrayList<>(); // 奖品内容
		prizeContents.add(req.getParameter("prizeOneNum"));
		prizeContents.add(req.getParameter("prizeTwoNum"));
		prizeContents.add(req.getParameter("prizeThreeNum"));
		prizeContents.add(req.getParameter("prizeFourNum"));

		prizeContents.add(req.getParameter("prizeOneProbability"));
		prizeContents.add(req.getParameter("prizeTwoProbability"));
		prizeContents.add(req.getParameter("prizeThreeProbability"));
		prizeContents.add(req.getParameter("prizeFourProbability"));

		prizeContents.add(req.getParameter("prizeOneType"));
		prizeContents.add(req.getParameter("prizeTwoType"));
		prizeContents.add(req.getParameter("prizeThreeType"));
		prizeContents.add(req.getParameter("prizeFourType"));

		prizeContents.add(req.getParameter("prizeOneValue"));
		prizeContents.add(req.getParameter("prizeTwoValue"));
		prizeContents.add(req.getParameter("prizeThreeValue"));
		prizeContents.add(req.getParameter("prizeFourValue"));

		List<CoherePrize> lists = coherePrizeService.getCoherePrizeByActivityId(activityId);
		for (int i = 0; i < lists.size(); i++) {
			lists.get(i).setProbability(NumberUtils.toByte(prizeContents.get(i + 4)));
			lists.get(i).setType(NumberUtils.toByte(prizeContents.get(i + 8)));
			lists.get(i).setValue(NumberUtils.toDouble(prizeContents.get(i + 12)));
			lists.get(i).setNum(i + 1);
			// 奖品数置于redis
			RedisAtomicInteger prizeCount = new RedisAtomicInteger(
					CacheKeyConstant.COHERE_PRIZE_COUNT + lists.get(i).getId(), redisConnectionFactory);
			if (NumberUtils.toInt(prizeContents.get(i)) == -99) {
				prizeCount.set(-99);
				lists.get(i).setCounts(NumberUtils.toInt(prizeContents.get(i)));
			} else {
				if (prizeCount.get() == -99) {
					return result.fill(-1, "奖品" + (i + 1) + "数量设置错误,只能设置比原先更多");
				}
				if (NumberUtils.toInt(prizeContents.get(i)) < lists.get(i).getCounts()) {
					return result.fill(-1, "奖品" + (i + 1) + "数量设置错误,只能设置比原先更多");
				} else {
					prizeCount.addAndGet(NumberUtils.toInt(prizeContents.get(i)) - lists.get(i).getCounts());
					lists.get(i).setCounts(NumberUtils.toInt(prizeContents.get(i)));
				}
			}
		}
		coherePrizeService.saveOrUpdate(lists, activityId); // 批量保存奖品信息
		CohereActivity cohereActivity = cohereActivityService.getCohereActivitybyId(activityId);
		// 设置最近更新时间
		cohereActivity.setUpdateDate(new Date());
		cohereActivityService.saveOrUpdate(cohereActivity);
		if (type == 1) {
			cohereActivity.setState(1);
		} else {
			cohereActivity.setState(2);
		}
		cohereActivityService.saveOrUpdate(cohereActivity);
		return result.fill(0, "保存成功");
	}

	/**
	 * 保存活动
	 * 
	 * @param activityId
	 * @param activityBeginTime
	 * @param activityEndTime
	 * @return
	 */
	@RequestMapping("/save")
	@ResponseBody
	public JsonResponseMsg saveActivity(HttpServletRequest req, HttpServletResponse res, Long activityId,
			String activityBeginTime, String activityEndTime) {
		JsonResponseMsg result = new JsonResponseMsg();
		String systemName = "wy-web-admin";
		String src = ImgUploadUtil.genFilePath("cohere");
		List<MultipartFile> files = new ArrayList<>();
		files.add(((MultipartHttpServletRequest) req).getFile("debrisOneFile") != null
				? Servlets.getMultipartFile(req, "debrisOneFile") : null); // 道具一
		files.add(((MultipartHttpServletRequest) req).getFile("debrisTwoFile") != null
				? Servlets.getMultipartFile(req, "debrisTwoFile") : null); // 道具二
		files.add(((MultipartHttpServletRequest) req).getFile("debrisThreeFile") != null
				? Servlets.getMultipartFile(req, "debrisThreeFile") : null); // 道具三
		files.add(((MultipartHttpServletRequest) req).getFile("debrisFourFile") != null
				? Servlets.getMultipartFile(req, "debrisFourFile") : null); // 道具四
		files.add(((MultipartHttpServletRequest) req).getFile("prizeOneFile1") != null
				? Servlets.getMultipartFile(req, "prizeOneFile1") : null); // 奖品一竖屏
		files.add(((MultipartHttpServletRequest) req).getFile("prizeOneFile2") != null
				? Servlets.getMultipartFile(req, "prizeOneFile2") : null); // 奖品一横屏
		files.add(((MultipartHttpServletRequest) req).getFile("prizeTwoFile") != null
				? Servlets.getMultipartFile(req, "prizeTwoFile") : null); // 奖品二
		files.add(((MultipartHttpServletRequest) req).getFile("prizeThreeFile") != null
				? Servlets.getMultipartFile(req, "prizeThreeFile") : null); // 奖品三
		files.add(((MultipartHttpServletRequest) req).getFile("prizeFourFile") != null
				? Servlets.getMultipartFile(req, "prizeFourFile") : null); // 奖品四
		List<String> titles = new ArrayList<>(); // 所有标题
		titles.add(req.getParameter("debrisOne"));
		titles.add(req.getParameter("debrisTwo"));
		titles.add(req.getParameter("debrisThree"));
		titles.add(req.getParameter("debrisFour"));
		titles.add(req.getParameter("prizeOne"));
		titles.add(req.getParameter("prizeTwo"));
		titles.add(req.getParameter("prizeThree"));
		titles.add(req.getParameter("prizeFour"));
		// 活动标题和时间
		String activityTitle = req.getParameter("title");
		List<CoherePrize> prizeList = new ArrayList<>();
		List<CohereDebris> debrisList = new ArrayList<>();
		CohereActivity cohereActivity = new CohereActivity();
		List<Map<String, String>> mid = upLoad(files, systemName, src);

		if (activityId != null) {
			cohereActivity = cohereActivityService.getCohereActivitybyId(activityId);
			// 设置道具
			debrisList = cohereDebrisService.findByActivityId(activityId);
			// 设置奖品
			prizeList = coherePrizeService.getCoherePrizeByActivityId(activityId);
		} else { // 新建活动相关奖品和碎片
			cohereActivity = cohereActivityService.saveOrUpdate(cohereActivity);
			prizeList = coherePrizeService.saveAuto(cohereActivity.getId(), 4);
			debrisList = cohereDebrisService.saveAuto(cohereActivity.getId(), 4);
		}
		// 设置最近更新时间
		cohereActivity.setUpdateDate(new Date());
		cohereActivityService.saveOrUpdate(cohereActivity);
		try {
			cohereActivity.setBeginTime(DateUtils.stringToDate(activityBeginTime, DateUtils.YYYY_MM_DD_HH_MM_SS));
			cohereActivity.setEndTime(DateUtils.stringToDate(activityEndTime, DateUtils.YYYY_MM_DD_HH_MM_SS));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		cohereActivity.setTitle(activityTitle);
		cohereActivity.setValid(1);
		cohereActivity.setState(1);
		// 设置信息
		for (int i = 0; i < debrisList.size(); i++) {
			debrisList.get(i).setUrl(mid.get(i).get(ImgUploadUtil.KEY_MAP_SRC));
			debrisList.get(i).setTitle(titles.get(i));
			debrisList.get(i).setCounts(0);
			debrisList.get(i).setCreateDate(new Date());
			debrisList.get(i).setProbability(0);
			debrisList.get(i).setValid(1);
		}
		for (int i = 0; i < prizeList.size(); i++) {
			if (i == 0) {
				prizeList.get(0).setUrlVertical(mid.get(5).get(ImgUploadUtil.KEY_MAP_SRC));
				prizeList.get(0).setUrlCrosswise(mid.get(4).get(ImgUploadUtil.KEY_MAP_SRC));
				prizeList.get(0).setName(titles.get(4));
			} else {
				prizeList.get(i).setUrlVertical(mid.get(i + 5).get(ImgUploadUtil.KEY_MAP_SRC));
				prizeList.get(i).setName(titles.get(i + 4));
			}
			prizeList.get(i).setCreateDate(new Date());
			prizeList.get(i).setCounts(0);
			prizeList.get(i).setProbability((byte) 0);
			prizeList.get(i).setValid(1);
		}
		cohereDebrisService.saveOrUpdate(debrisList, activityId); // 批量保存碎片信息
		cohereActivityService.saveOrUpdate(cohereActivity);// 保存活动信息
		coherePrizeService.saveOrUpdate(prizeList, activityId); // 批量保存奖品信息
		Map<String, String> params = Maps.newHashMap();
		params.put("state", "0");
		params.put("activityId", cohereActivity.getId().toString());
		params.put("activityTitle", cohereActivity.getTitle());
		params.put("activityBeginTime", cohereActivity.getBeginTime().toString());
		params.put("activityEndTime", cohereActivity.getEndTime().toString());
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, params);
	}

	/**
	 * 批量上传文件
	 * 
	 * @param files
	 * @param systemName
	 * @param src
	 * @return
	 */
	private List<Map<String, String>> upLoad(List<MultipartFile> files, String systemName, String src) {
		List<Map<String, String>> result = new ArrayList<>();
		for (int i = 0; i < files.size(); i++) {
			if (files.get(i) != null) {
				result.add(ImgUploadUtil.save(files.get(i), systemName, src));
			}
		}
		return result;
	}

	/**
	 * 统计页面
	 * 
	 * @param page
	 * @return
	 */
	@RequestMapping("/statistics")
	public ModelAndView statistics(Long activityId, Integer page, Integer type, String date, Long userId,
			String searchDate) {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("/cohere/cohereActivityStatistics");
		if (page == null) {
			page = 1;
		}
		// 查询活动信息
		CohereActivity cohereActivity = cohereActivityService.getCohereActivitybyId(activityId);
		mv.addObject("cohereActivity", cohereActivity);
		// 查看今昨两天的统计信息
		Date todayDate = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String todayString = format.format(todayDate);
		Date yesDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
		String yesString = format.format(yesDate);
		String dateString = "'" + todayString + "','" + yesString + "'";
		List<Map<String, Object>> list = cohereActivityService.getActivityStatiDailyList(activityId, dateString, page);
		Map<String, Object> todayMap = Maps.newHashMap();
		Map<String, Object> yesMap = Maps.newHashMap();
		if (CollectionUtils.isNotEmpty(list)) {
			for (int i = 0; i < list.size(); i++) {
				Map<String, Object> map = list.get(i);
				if (map.get("datecreate").toString().equals(todayString)) {
					todayMap = list.get(i);
				} else if (map.get("datecreate").toString().equals(yesString)) {
					yesMap = list.get(i);
				}
			}

		}
		// 查询统计信息
		if (type == null) {
			mv.addObject("type", 1);
			PageVO statisticsInfoList = cohereActivityService.getActivityStatisticsInfo(activityId, page, searchDate,
					true, "all");
			mv.addObject("searchDate", searchDate);
			mv.addObject("statisticsInfoList", statisticsInfoList);
			pageModels(mv, statisticsInfoList.getList(), page, statisticsInfoList.getTotal());
		} else if (type == 2) {
			mv.addObject("type", 2);
			PageVO statisticsInfoList = cohereActivityService.getActivityStatiDailyInfo(activityId, date, page, true,
					"all");
			mv.addObject("statisticsInfoListDaily", statisticsInfoList);
			mv.addObject("date", date);
			pageModels(mv, statisticsInfoList.getList(), page, statisticsInfoList.getTotal());
		} else if (type == 3) {
			mv.addObject("type", 3);
			PageVO statisticsInfoList = cohereActivityService.getActivityStatiUser(activityId, userId, page, "all");
			UserInfo userInfo = userInfoService.findById(userId);
			mv.addObject("userInfo", userInfo);
			mv.addObject("statisticsInfoListUser", statisticsInfoList);
			mv.addObject("activityUserId", userId);
			pageModels(mv, statisticsInfoList.getList(), page, statisticsInfoList.getTotal());
		}
		mv.addObject("todayMap", todayMap);
		mv.addObject("yesMap", yesMap);
		return mv;
	}

	/**
	 * 统计页面
	 * 
	 * @param page
	 * @return
	 */
	@RequestMapping("/statistics/export")
	public ModelAndView export(HttpServletRequest request, HttpServletResponse response, String dayString) {
		String searchDate = request.getParameter("searchDate");
		Long activityId = NumberUtils.toLong(request.getParameter("activityId").toString());
		PageVO vo = cohereActivityService.getActivityStatisticsInfo(activityId, 1, searchDate, false, dayString);

		// 查询excel标题
		String title = "拼肤活动后台统计";

		// 编辑excel内容
		List<Map<String, Object>> list = vo.getList();
		String[][] contents = new String[list.size() + 1][];

		// 设置标题行
		String[] contentTitle = new String[4];
		contentTitle[0] = "日期";
		contentTitle[1] = "总道具发出数/次数";
		contentTitle[2] = "奖品兑换数";
		contentTitle[3] = "参与人数";
		contents[0] = contentTitle;
		// 设置内容
		if (CollectionUtils.isNotEmpty(list)) {
			for (int i = 0; i < list.size(); i++) {
				Map<String, Object> obj = list.get(i);
				String[] row = new String[4];
				row[0] = MapUtils.getString(obj, "datecreate");
				row[1] = Integer
						.toString((MapUtils.getInteger(obj, "sum1") != null ? MapUtils.getInteger(obj, "sum1") : 0)
								+ (MapUtils.getInteger(obj, "sum2") != null ? MapUtils.getInteger(obj, "sum2") : 0)
								+ (MapUtils.getInteger(obj, "sum3") != null ? MapUtils.getInteger(obj, "sum3") : 0)
								+ (MapUtils.getInteger(obj, "sum4") != null ? MapUtils.getInteger(obj, "sum4") : 0));
				row[2] = Integer
						.toString((MapUtils.getInteger(obj, "psum1") != null ? MapUtils.getInteger(obj, "psum1") : 0)
								+ (MapUtils.getInteger(obj, "psum2") != null ? MapUtils.getInteger(obj, "psum2") : 0)
								+ (MapUtils.getInteger(obj, "psum3") != null ? MapUtils.getInteger(obj, "psum3") : 0)
								+ (MapUtils.getInteger(obj, "psum4") != null ? MapUtils.getInteger(obj, "psum4") : 0));
				row[3] = MapUtils.getString(obj, "personCount");
				contents[i + 1] = row;
			}
		}
		try {
			ExcelUtils.exportExcel(title, contents, false, response);
		} catch (Exception e) {

		}
		return null;
	}

	/**
	 * 当日详情统计页面
	 * 
	 * @param page
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping("/statistics/detail/export")
	public ModelAndView detailExport(HttpServletRequest request, HttpServletResponse response)
			throws UnsupportedEncodingException {
		String days = request.getParameter("days");
		String userIdString = request.getParameter("userIdString");
		Long activityId = NumberUtils.toLong(request.getParameter("activityId").toString());
		PageVO vo = cohereActivityService.getActivityStatiDailyInfo(activityId, days, 1, false, userIdString);
		// 查询excel标题
		String title = "拼肤活动后台统计" + request.getParameter("days");
		response.setHeader("content-disposition",
				"attachment;filename=" + new String(title.getBytes(), "ISO8859-1") + ".xls");
		// 编辑excel内容
		List<Map<String, Object>> list = vo.getList();
		String[][] contents = new String[list.size() + 1][];

		// 设置标题行
		String[] contentTitle = new String[11];
		contentTitle[0] = "用户手机";
		contentTitle[1] = "用户昵称";
		contentTitle[2] = "注册时间";
		contentTitle[3] = "道具一";
		contentTitle[4] = "道具二";
		contentTitle[5] = "道具三";
		contentTitle[6] = "道具四";
		contentTitle[7] = "奖品一";
		contentTitle[8] = "奖品二";
		contentTitle[9] = "奖品三";
		contentTitle[10] = "奖品四";
		contents[0] = contentTitle;
		// 设置内容
		if (CollectionUtils.isNotEmpty(list)) {
			for (int i = 0; i < list.size(); i++) {
				Map<String, Object> obj = list.get(i);
				String[] row = new String[11];
				row[0] = MapUtils.getString(obj, "telephone");
				row[1] = MapUtils.getString(obj, "nickname");
				row[2] = MapUtils.getString(obj, "create_date");
				row[3] = MapUtils.getString(obj, "firDraw");
				row[4] = MapUtils.getString(obj, "secDraw");
				row[5] = MapUtils.getString(obj, "thiDraw");
				row[6] = MapUtils.getString(obj, "forDraw");
				row[7] = MapUtils.getString(obj, "firPrize");
				row[8] = MapUtils.getString(obj, "secPrize");
				row[9] = MapUtils.getString(obj, "thiPrize");
				row[10] = MapUtils.getString(obj, "forPrize");

				contents[i + 1] = row;
			}
		}

		try {
			ExcelUtils.exportExcel2(title, contents, false, response);
		} catch (Exception e) {

		}
		return null;
	}

	/**
	 * 用户统计信息导出
	 */
	@RequestMapping("/statistics/user/export")
	public ModelAndView userExport(HttpServletRequest request, HttpServletResponse response) {
		Long userId = NumberUtils.toLong(request.getParameter("userId"));
		Long activityId = NumberUtils.toLong(request.getParameter("activityId").toString());
		String dayString = request.getParameter("dayString");
		PageVO vo = cohereActivityService.getActivityStatiUser(activityId, userId, 1, dayString);
		// 查询excel标题
		String title = "拼肤活动后台统计";
		// 编辑excel内容
		List<Map<String, Object>> list = vo.getList();
		String[][] contents = new String[list.size() + 1][];
		// 设置标题行
		String[] contentTitle = new String[9];
		contentTitle[0] = "记录时间";
		contentTitle[1] = "道具一";
		contentTitle[2] = "道具二";
		contentTitle[3] = "道具三";
		contentTitle[4] = "道具四";
		contentTitle[5] = "奖品一";
		contentTitle[6] = "奖品二";
		contentTitle[7] = "奖品三";
		contentTitle[8] = "奖品四";
		contents[0] = contentTitle;
		// 设置内容
		if (CollectionUtils.isNotEmpty(list)) {
			for (int i = 0; i < list.size(); i++) {
				Map<String, Object> obj = list.get(i);
				String[] row = new String[9];
				row[0] = MapUtils.getString(obj, "days");
				row[1] = MapUtils.getString(obj, "firDraw");
				row[2] = MapUtils.getString(obj, "secDraw");
				row[3] = MapUtils.getString(obj, "thiDraw");
				row[4] = MapUtils.getString(obj, "forDraw");
				row[5] = MapUtils.getString(obj, "firPrize");
				row[6] = MapUtils.getString(obj, "secPrize");
				row[7] = MapUtils.getString(obj, "thiPrize");
				row[8] = MapUtils.getString(obj, "forPrize");
				contents[i + 1] = row;
			}
		}
		try {
			ExcelUtils.exportExcel(title, contents, false, response);
		} catch (Exception e) {

		}
		return null;
	}

}
