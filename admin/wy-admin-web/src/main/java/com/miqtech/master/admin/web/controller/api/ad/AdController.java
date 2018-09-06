package com.miqtech.master.admin.web.controller.api.ad;

import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.annotation.CrossDomain;
import com.miqtech.master.admin.web.annotation.LoginValid;
import com.miqtech.master.admin.web.controller.api.BaseController;
import com.miqtech.master.consts.ad.AdConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.common.IndexAdvertise;
import com.miqtech.master.service.activity.ActivityOverActivityService;
import com.miqtech.master.service.audition.AuditionService;
import com.miqtech.master.service.index.IndexAdvertiseService;
import com.miqtech.master.service.pc.taskmatch.TaskMatchRankService;
import com.miqtech.master.service.pc.taskmatch.TaskMatchThemeService;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.ExcelUtils;
import com.miqtech.master.vo.PageVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("api/ad")
public class AdController extends BaseController {

	@Autowired
	private IndexAdvertiseService indexAdvertiseService;
	@Autowired
	private ActivityOverActivityService activityOverActivityService;
	@Autowired
	private AuditionService auditionService;
	@Autowired
	private TaskMatchThemeService taskMatchThemeService;
	@Autowired
	private TaskMatchRankService taskMatchRankService;

	/**
	 * 获取广告列表
	 * @param req http请求
	 * @param type 0 首页banner广告 3 启动页广告
	 * @param page 当前页数
	 * @return 列表json数据
	 */
	@RequestMapping("list")
	@ResponseBody
	@LoginValid(valid = true)
	@CrossDomain(value = true)
	public JsonResponseMsg list(HttpServletRequest req, Integer type, Integer page) {
		Map<String, Object> params = Maps.newHashMap();
		if (type == null) {
			type = 3;//默认启动页 广告类型（0 首页banner  3 启动页广告 4 商户端登录页面 5 商户端浮窗）
		}
		params.put("belong", type);
		if (null == page) {
			page = 1;
		}
		String title = req.getParameter("title");
		if (StringUtils.isNotBlank(title)) {
			params.put("title", title);
		}
		params.put("page", page);
		PageVO pageVO = indexAdvertiseService.pageListByBelong(page, params);
		return new JsonResponseMsg().fill(1, "查询成功", pageVO);
	}

	/**
	 * 保存广告
	 * @param req http请求
	 * @return 保存操作结果
	 */
	@RequestMapping(value = "save", method = { RequestMethod.POST })
	@ResponseBody
	@LoginValid(valid = true)
	@CrossDomain(value = true)
	public JsonResponseMsg save(HttpServletRequest req) {

		Long id = NumberUtils.toLong(req.getParameter("id"));
		IndexAdvertise iAdvertise = null;
		if (id <= 0) {
			iAdvertise = new IndexAdvertise();
			iAdvertise.setCreateDate(new Date());
			iAdvertise.setValid(1);
		} else {
			iAdvertise = indexAdvertiseService.findById(id);
		}

		String belong = req.getParameter("belong");//0 首页banner  3启动页
		String title = req.getParameter("title");
		String imgUrl = req.getParameter("imgUrl");
		String adType = req.getParameter("adType");
		String targetInfo = req.getParameter("targetInfo");
		String serverStartDate = req.getParameter("serverStartDate");
		String serverEndDate = req.getParameter("serverEndDate");
		iAdvertise.setBelong(NumberUtils.toInt(belong));
		iAdvertise.setImg(imgUrl);

		try {
			iAdvertise.setServerStartDate(DateUtils.stringToDate(serverStartDate, DateUtils.YYYY_MM_DD_HH_MM_SS));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		try {
			iAdvertise.setServerEndDate(DateUtils.stringToDate(serverEndDate, DateUtils.YYYY_MM_DD_HH_MM_SS));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		iAdvertise.setTitle(title);
		int type = NumberUtils.toInt(adType);

		iAdvertise.setStatus(1);
		iAdvertise.setValid(1);
		if (type == AdConstant.ADVERTISE_TYPE_INFO || type == AdConstant.ADVERTISE_TYPE_AUDITION
				|| type == AdConstant.ADVERTISE_TYPE_THEME || type == AdConstant.ADVERTISE_TYPE_RANK) {
			iAdvertise.setTargetId(NumberUtils.toLong(targetInfo));
		} else if (type == AdConstant.ADVERTISE_TYPE_AD) {
			iAdvertise.setUrl(targetInfo);
		}
		iAdvertise.setType(type);
		indexAdvertiseService.saveOrUpdate(iAdvertise);
		indexAdvertiseService.clearCache();
		return new JsonResponseMsg().fill(1, "添加成功");
	}

	/**
	 * 删除广告
	 * @param req
	 * @param id 删除的广告id
	 * @return
	 */
	@RequestMapping("del")
	@ResponseBody
	@LoginValid(valid = true)
	@CrossDomain(value = true)
	public JsonResponseMsg del(HttpServletRequest req, Long id) {
		indexAdvertiseService.deleteById(id);
		indexAdvertiseService.clearCache();
		return new JsonResponseMsg().fill(1, "删除数据成功");
	}

	/**
	 * 下架广告
	 * @param req
	 * @param id 下架的广告id
	 * @return
	 */
	@RequestMapping("unShelf")
	@ResponseBody
	@LoginValid(valid = true)
	@CrossDomain(value = true)
	public JsonResponseMsg unShelf(HttpServletRequest req, Long id) {
		indexAdvertiseService.unShelfById(id);
		indexAdvertiseService.clearCache();
		return new JsonResponseMsg().fill(1, "下架数据成功");
	}

	/**
	 * 统计展现和点击次数
	 * @param req
	 * @param id 下架的广告id
	 * @return
	 */
	@RequestMapping("statistic")
	@ResponseBody
	@LoginValid(valid = true)
	@CrossDomain(value = true)
	public JsonResponseMsg statistic(HttpServletRequest req, Long id) {
		int orderType = NumberUtils.toInt(req.getParameter("orderType"));

		String startDate = req.getParameter("startDate");
		String endDate = req.getParameter("endDate");

		if (StringUtils.isAnyBlank(startDate, endDate)) {
			Date now = new Date();
			endDate = DateUtils.dateToString(now, DateUtils.YYYY_MM_DD);
			startDate = DateUtils.dateToString(DateUtils.addDays(now, -36500), DateUtils.YYYY_MM_DD);
		}
		List<Map<String, Object>> statistic = indexAdvertiseService.statistic(id, startDate, endDate, orderType);
		return new JsonResponseMsg().fill(1, "查询数据成功", statistic);
	}

	/**
	 * 导出统计展现和点击次数(导出全部数据)
	 * @param req
	 * @param id 下架的广告id
	 * @return
	 */
	@RequestMapping("export")
	@LoginValid(valid = true)
	@CrossDomain(value = true)
	public ModelAndView export(HttpServletRequest req, HttpServletResponse res, Long id) {
		String[] columnName = { "日期", "展现", "点击" };
		String startDate = req.getParameter("startDate");
		String endDate = req.getParameter("endDate");
		if (StringUtils.isAnyBlank(startDate, endDate)) {
			Date now = new Date();
			endDate = DateUtils.dateToString(now, DateUtils.YYYY_MM_DD);
			startDate = DateUtils.dateToString(DateUtils.addDays(now, -36500), DateUtils.YYYY_MM_DD);
		}
		List<Map<String, Object>> statistic = indexAdvertiseService.statistic(id, startDate, endDate, 0);

		// 查询excel标题
		String title = "广告详细数据";
		try {
			res.setHeader("content-disposition",
					"attachment;filename=" + new String(title.getBytes(), "ISO8859-1") + ".xls");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		// 编辑excel内容
		String[][] contents = new String[statistic.size() + 1][];

		// 设置标题行
		contents[0] = columnName;
		// 设置内容
		if (CollectionUtils.isNotEmpty(statistic)) {
			for (int i = 0; i < statistic.size(); i++) {
				Map<String, Object> obj = statistic.get(i);
				String[] row = new String[3];
				row[0] = MapUtils.getString(obj, "date");
				row[1] = MapUtils.getString(obj, "imp");
				row[2] = MapUtils.getString(obj, "click");
				contents[i + 1] = row;
			}
		}
		try {
			ExcelUtils.exportExcel(title, contents, false, res);
		} catch (Exception e) {

		}
		return null;
	}

	/**
	 * 获取广告列表
	 * @param type 15-资讯 17-赛事 18-主题 19-排行榜
	 * @return
	 */
	@RequestMapping("info")
	@ResponseBody
	@CrossDomain(value = true)
	public JsonResponseMsg info(Integer type) {
		Date now = new Date();
		String endDate = DateUtils.dateToString(now, DateUtils.YYYY_MM_DD_HH_MM_SS);
		String startDate = DateUtils.dateToString(DateUtils.addDays(now, -60), DateUtils.YYYY_MM_DD);
		List<Map<String, Object>> infos = null;

		switch (type) {
		case AdConstant.ADVERTISE_TYPE_INFO:
			infos = activityOverActivityService.queryInfoForAppRecommend(startDate, endDate);
			break;
		case AdConstant.ADVERTISE_TYPE_AUDITION:
			infos = auditionService.queryInfoForAppRecommend(startDate, endDate);
			break;
		case AdConstant.ADVERTISE_TYPE_THEME:
			infos = taskMatchThemeService.queryInfoForAppRecommend(startDate, endDate);
			break;
		case AdConstant.ADVERTISE_TYPE_RANK:
			infos = taskMatchRankService.queryInfoForAppRecommend(startDate, endDate);
			break;
		default:
			break;
		}
		return new JsonResponseMsg().fill(1, "查询数据成功", infos);
	}


}
