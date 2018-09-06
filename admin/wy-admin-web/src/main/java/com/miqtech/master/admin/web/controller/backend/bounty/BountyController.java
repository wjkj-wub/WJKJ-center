package com.miqtech.master.admin.web.controller.backend.bounty;

import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.consts.BountyConstant;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.bounty.BountyDailyTipDao;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.bounty.Bounty;
import com.miqtech.master.entity.bounty.BountyDailyTip;
import com.miqtech.master.entity.bounty.BountyPrize;
import com.miqtech.master.service.bounty.BountyPrizeService;
import com.miqtech.master.service.bounty.BountyService;
import com.miqtech.master.thirdparty.util.img.ImgUploadUtil;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.vo.PageVO;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("bounty")
public class BountyController extends BaseController {

	@Autowired
	private BountyService bountyService;
	@Autowired
	private BountyPrizeService bountyPrizeService;
	@Autowired
	private BountyDailyTipDao bountyDailyTipDao;

	/**
	 * 列表页面
	 * @throws ParseException 
	 */
	@RequestMapping("list/{page}")
	public ModelAndView list(@PathVariable("page") Integer page, Long itemId) throws ParseException {
		ModelAndView mv = new ModelAndView("bounty/bountyList");
		PageVO vo = bountyService.getPage(page, itemId);
		Map<String, Object> rule = bountyService.getRule(itemId);
		pageModels(mv, vo.getList(), page, vo.getTotal());
		mv.addObject("itemId", itemId);
		mv.addObject("rule", rule == null ? "" : rule.get("rule"));
		return mv;
	}

	/**
	 * 新增 / 编辑 页面
	 * @throws ParseException 
	 */
	@RequestMapping("edit")
	public ModelAndView edit(String id, String itemId) throws ParseException {
		ModelAndView mv = new ModelAndView("bounty/bountyEdit");
		//编辑悬赏令
		if (NumberUtils.isNumber(id)) {
			mv.addObject("itemId", itemId);
			Bounty bounty = bountyService.findById(NumberUtils.toLong(id));
			int status = bountyService.statusBounty(bounty.getStartTime().toString(), bounty.getEndTime().toString(),
					bounty.getStatus().toString());
			if (status != 0) {
				mv.addObject("readonly", "1");
			} else {
				mv.addObject("readonly", "0");
			}
			//每日提醒
			List<BountyDailyTip> lists = bountyDailyTipDao.findByBountyIdOrderByCreateDateDesc(NumberUtils.toLong(id));
			if (lists.size() == 5) {
				Map<String, Object> params = new HashMap<String, Object>(16);
				params.put("dayTipOne", lists.get(0).getContent());
				params.put("dayTipTwo", lists.get(1).getContent());
				params.put("dayTipThree", lists.get(2).getContent());
				params.put("dayTipFour", lists.get(3).getContent());
				params.put("dayTipFive", lists.get(4).getContent());
				mv.addObject("info", getInfo(bounty));
				mv.addObject("params", params);
			}
			return mv;
		}

		//新建悬赏令
		if (NumberUtils.isNumber(itemId)) {
			Map<String, Object> result = new HashMap<String, Object>(16);
			if (itemId.equals("3")) {//王者荣耀
				result.put("icon", "uploads/imgs/bounty/2016/11/28/c07fb95313ea4200ba4b8add0b0704e0.png");
				result.put("cover", "uploads/imgs/bounty/2016/11/28/17c0289680ba4acbb6e21b33887417ed.jpg");
				result.put("itemIcon", "uploads/imgs/bounty/2016/11/28/4320698099b94d32b1207101d3fe417a.png");
			} else if (itemId.equals("1")) { //英雄联盟
				result.put("icon", "uploads/imgs/bounty/2016/11/28/ff918b7505694a2aa78a102c8fe1e579.png");
				result.put("cover", "uploads/imgs/bounty/2016/11/28/6edc6e51c6f749409e770684ede65308.jpg");
				result.put("itemIcon", "uploads/imgs/bounty/2016/11/28/3361d2990cbd44aea9e2e954623f67a3.png");
			}
			mv.addObject("info", result);
			mv.addObject("itemId", itemId);
		}
		return mv;
	}

	/**
	 * 检索字符串
	 * @param bounty
	 * @return
	 */
	private Map<String, Object> getInfo(Bounty bounty) {
		Map<String, Object> result = new HashMap<String, Object>(32);
		String reward = bounty.getReward();
		String target = bounty.getTarget();
		Integer targetType = bounty.getTargetType();
		Long id = bounty.getId();
		result.put("id", id);
		result.put("endTime", bounty.getEndTime());
		result.put("itemId", bounty.getItemId());
		result.put("startTime", bounty.getStartTime());
		result.put("icon", bounty.getIcon());
		result.put("cover", bounty.getCover());
		result.put("itemIcon", bounty.getItemIcon());
		if (reward.contains("金币")) {
			result.put("prizeType", "1");
		} else if (reward.contains("流量")) {
			result.put("prizeType", "2");
		}
		String regEx = "[^0-9]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(reward);
		Long prizeNum = NumberUtils.toLong(m.replaceAll("").trim());
		result.put("prizeNum", prizeNum);
		result.put("targetNum", target.split("\\|")[1]);
		result.put("targetType", targetType + "");
		return result;
	}

	/**
	 * 编辑 / 新增 悬赏令
	 * @throws ParseException 
	 */
	@ResponseBody
	@RequestMapping("save")
	public JsonResponseMsg save(HttpServletRequest req, Long id, String startTimeStr, String endTimeStr, String target,
			String reward, Integer targetType, Long itemId) throws ParseException {
		JsonResponseMsg result = new JsonResponseMsg();
		Bounty bounty = bountyService.findById(id);
		Date startDate = null;
		Date endDate = null;
		Date nowDate = new Date();
		Calendar cal = Calendar.getInstance();
		try {
			startDate = DateUtils.stringToDate(startTimeStr, DateUtils.YYYY_MM_DD_HH_MM_SS);
			cal.setTime(startDate);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			startDate = cal.getTime();
			endDate = DateUtils.stringToDate(endTimeStr, DateUtils.YYYY_MM_DD_HH_MM_SS);
			cal.setTime(endDate);
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			endDate = cal.getTime();
			if (DateUtils.calcDaysBetweenDates(startDate, endDate) != 6) {
				return result.fill(CommonConstant.CODE_ERROR_LOGIC, CommonConstant.MSG_ERROR_LOGIC);
			}
		} catch (Exception expect) {
			return result.fill(CommonConstant.CODE_ERROR_LOGIC, CommonConstant.MSG_ERROR_LOGIC);
		}
		if (bounty == null) {
			bounty = new Bounty();
		}
		List<Map<String, Object>> list = bountyService.findByItemId(itemId, id);
		if (list != null && list.size() > 0) {
			Date endTime = DateUtils.stringToDate(list.get(0).get("end_time").toString().substring(0,
					list.get(0).get("end_time").toString().length() - 2), DateUtils.YYYY_MM_DD_HH_MM_SS);
			bounty.setRule(list.get(0).get("rule").toString());
			if (endTime.after(startDate)) {
				return result.fill(CommonConstant.CODE_ERROR_LOGIC, "日期与存在的悬赏令有重叠");
			}
		}
		if (startDate.before(nowDate)) {
			bounty.setIsPublish(1);
		} else {
			bounty.setIsPublish(0);
		}
		String systemName = "wy-web-admin";
		String src = ImgUploadUtil.genFilePath("bounty");
		MultipartFile iconFile = Servlets.getMultipartFile(req, "icon");
		MultipartFile coverFile = Servlets.getMultipartFile(req, "cover");
		MultipartFile itemIconFile = Servlets.getMultipartFile(req, "itemIcon");
		// 保存icon
		if (iconFile != null) {// 有图片时上传文件
			Map<String, String> imgPaths = ImgUploadUtil.save(iconFile, systemName, src);
			bounty.setIcon(imgPaths.get(ImgUploadUtil.KEY_MAP_SRC));
		} else {
			if (itemId == 1) {//英雄联盟
				bounty.setIcon("uploads/imgs/bounty/2016/11/28/ff918b7505694a2aa78a102c8fe1e579.png"); //英雄联盟
			} else if (itemId == 3) { //王者荣耀
				bounty.setIcon("uploads/imgs/bounty/2016/11/28/c07fb95313ea4200ba4b8add0b0704e0.png"); // 王者荣耀
			}
		}
		if (coverFile != null) {// 有图片时上传文件
			Map<String, String> imgPaths = ImgUploadUtil.save(coverFile, systemName, src);
			bounty.setCover(imgPaths.get(ImgUploadUtil.KEY_MAP_SRC));
		} else {
			if (itemId == 1) {//英雄联盟
				bounty.setCover("uploads/imgs/bounty/2016/11/28/6edc6e51c6f749409e770684ede65308.jpg");
			} else if (itemId == 3) { //王者荣耀
				bounty.setCover("uploads/imgs/bounty/2016/11/28/17c0289680ba4acbb6e21b33887417ed.jpg");
			}
		}
		if (itemIconFile != null) {// 有图片时上传文件
			Map<String, String> imgPaths = ImgUploadUtil.save(itemIconFile, systemName, src);
			bounty.setItemIcon(imgPaths.get(ImgUploadUtil.KEY_MAP_SRC));
		} else {
			if (itemId == 1) {//英雄联盟
				bounty.setItemIcon("uploads/imgs/bounty/2016/11/28/3361d2990cbd44aea9e2e954623f67a3.png");//英雄联盟
			} else if (itemId == 3) { //王者荣耀
				bounty.setItemIcon("uploads/imgs/bounty/2016/11/28/4320698099b94d32b1207101d3fe417a.png");// 王者荣耀
			}
		}
		bounty.setStartTime(startDate);
		bounty.setEndTime(endDate);
		bounty.setTarget(target);
		bounty.setType(1);
		bounty.setOrderType(1);
		bounty.setTargetType(targetType);
		bounty.setReward(reward);
		bounty.setItemId(itemId);
		bounty.setPrizeVirtualNum(0);
		String str = "";
		if (itemId == 1) {
			str += "英雄联盟";
		} else if (itemId == 2) {
			str += "炉石传说";
		} else if (itemId == 3) {
			str += "王者荣耀";
		}
		str = str + "第" + (list.size() + 1) + "期";
		bounty.setTitle(str);
		List<String> tips = new ArrayList<String>();
		tips.add(req.getParameter("dayTipFive"));
		tips.add(req.getParameter("dayTipFour"));
		tips.add(req.getParameter("dayTipThree"));
		tips.add(req.getParameter("dayTipTwo"));
		tips.add(req.getParameter("dayTipOne"));
		BountyDailyTip bountyDailyTip = null;
		cal.setTime(startDate);
		bounty.setStatus(BountyConstant.STATUS_UNDERWAY);
		bounty = bountyService.insertOrUpdate(bounty);
		if (id != null) {
			List<BountyDailyTip> dailyTips = bountyDailyTipDao.findByBountyIdOrderByCreateDateDesc(id);
			for (int i = 0; i < dailyTips.size(); i++) {
				bountyDailyTip = dailyTips.get(i);
				bountyDailyTip.setBountyId(bounty.getId());
				bountyDailyTip.setCreateDate(cal.getTime());
				bountyDailyTip.setContent(tips.get(i));
				bountyDailyTip.setValid(1);
				bountyDailyTipDao.save(bountyDailyTip);
				cal.add(Calendar.DATE, 1);
			}
		} else {
			for (String tipMid : tips) {
				bountyDailyTip = new BountyDailyTip();
				bountyDailyTip.setBountyId(bounty.getId());
				bountyDailyTip.setCreateDate(cal.getTime());
				bountyDailyTip.setContent(tipMid);
				bountyDailyTip.setValid(1);
				bountyDailyTipDao.save(bountyDailyTip);
				cal.add(Calendar.DATE, 1);
			}
		}
		Map<String, Object> r = getInfo(bounty);
		BountyPrize bountyPrize = new BountyPrize();
		bountyPrize.setBountyId(bounty.getId());
		bountyPrize.setAwardNum(NumberUtils.toInt(r.get("prizeNum").toString()));
		if (r.get("prizeType").toString().equals("1")) {
			bountyPrize.setAwardSubType(5);
			bountyPrize.setAwardType(1);
			bountyPrize.setValid(1);
			bountyPrize.setAwardName("网娱金币");
		} else if (r.get("prizeType").toString().equals("2")) {
			bountyPrize.setValid(1);
			bountyPrize.setAwardType(4);
			bountyPrize.setAwardSubType(6);
			bountyPrize.setAwardName("流量");
		}
		bountyPrizeService.save(bountyPrize);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 * 悬赏令规则
	 * @return
	 */
	@ResponseBody
	@RequestMapping("rule")
	public JsonResponseMsg rule(Long itemId, String rule) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (itemId == null) {
			itemId = 1L;
		}
		List<Map<String, Object>> bounties = bountyService.findByItemId(itemId, 0L);
		Bounty b = null;
		for (Map<String, Object> bounty : bounties) {
			b = bountyService.findById(NumberUtils.toLong(bounty.get("id").toString()));
			b.setRule(rule);
			bountyService.save(b);
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}
}
