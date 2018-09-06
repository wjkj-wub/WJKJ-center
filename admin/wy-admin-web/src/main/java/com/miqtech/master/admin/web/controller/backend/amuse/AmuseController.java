package com.miqtech.master.admin.web.controller.backend.amuse;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.SystemUserConstant;
import com.miqtech.master.consts.mall.CommodityConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.amuse.AmuseActivityIcon;
import com.miqtech.master.entity.amuse.AmuseActivityInfo;
import com.miqtech.master.entity.common.SystemArea;
import com.miqtech.master.entity.common.SystemUser;
import com.miqtech.master.service.activity.ActivityItemService;
import com.miqtech.master.service.amuse.AmuseActivityIconService;
import com.miqtech.master.service.amuse.AmuseActivityInfoService;
import com.miqtech.master.service.mall.CommodityCategoryService;
import com.miqtech.master.service.netbar.NetbarInfoService;
import com.miqtech.master.service.system.SysUserAreaService;
import com.miqtech.master.service.system.SystemAreaService;
import com.miqtech.master.thirdparty.util.img.ImgUploadUtil;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.ExcelUtils;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 娱乐赛管理
 */
@Controller
@RequestMapping("amuse/")
public class AmuseController extends BaseController {
	private static final Logger LOGGER = LoggerFactory.getLogger(AmuseController.class);

	@Autowired
	private AmuseActivityInfoService amuseActivityInfoService;
	@Autowired
	private NetbarInfoService netbarInfoService;
	@Autowired
	private AmuseActivityIconService amuseActivityIconService;
	@Autowired
	private CommodityCategoryService commodityCategoryService;
	@Autowired
	private SystemAreaService systemAreaService;
	@Autowired
	private ActivityItemService activityItemService;
	@Autowired
	private SysUserAreaService sysUserAreaService;

	/**
	 * 娱乐赛列表
	 */
	@RequestMapping("/list/{page}")
	public ModelAndView commodityList(HttpServletRequest request, @PathVariable("page") int page, String title,
			String way, String valid, String state, String awardType, String startDate, String endDate,
			String areaCode) {
		ModelAndView mv = new ModelAndView("amuse/activityList"); //跳转到activityList.ftl页面

		SystemUser user = Servlets.getSessionUser(request);
		boolean isActivityAdmin = false;
		if (null == user) {
			backIndex(mv, "登录失败,请检查用户名和密码.");
			return mv;
		} else {
			isActivityAdmin = SystemUserConstant.TYPE_ACTIVITY_ADMIN.equals(user.getUserType()) ? true : false;
		}

		Map<String, Object> params = Maps.newHashMap();
		params.put("wy", 1); //区分官方娱乐赛和网吧娱乐赛

		if (!isActivityAdmin) { //最高管理员
			mv.addObject("superAdmin", "1");
			mv.addObject("provinceList", systemAreaService.queryAllRoot());
			if (StringUtils.isNotBlank(areaCode)) {
				params.put("areaCode", areaCode.substring(0, 2)); //精确到省级
			}
		} else { //子账号
			String userAreaCode = sysUserAreaService.findUserAreaCode(user.getId()); // 匹配子账号的地区
			userAreaCode = null == userAreaCode ? "000000" : userAreaCode;
			params.put("areaCode", userAreaCode.substring(0, 2));
			List<SystemArea> singleList = Lists.newArrayList();
			SystemArea systemArea = systemAreaService.findByCode(userAreaCode.substring(0, 2) + "0000");
			singleList.add(systemArea);
			mv.addObject("provinceList", singleList);
		}
		if (StringUtils.isNotBlank(title)) {
			params.put("title", title);
		}
		if (StringUtils.isNotBlank(way)) {
			params.put("way", way);
		}
		if (StringUtils.isNotBlank(valid)) {
			params.put("valid", valid);
		}
		int flag = 0;
		if (StringUtils.isNotBlank(state)) {
			params.put("state", state);
			if (state.equals("4")) { //过期状态
				params.remove("state");
				endDate = DateUtils.dateToString(new Date(), DateUtils.YYYY_MM_DD_HH_MM_SS);
				flag = 1;
			}
		}
		if (StringUtils.isNotBlank(awardType)) {
			params.put("awardType", awardType);
		}
		if (StringUtils.isNotBlank(startDate)) {
			params.put("startDate", startDate);
		}
		if (StringUtils.isNotBlank(endDate)) {
			params.put("endDate", endDate);
		}
		PageVO pageVO = amuseActivityInfoService.listPage(page, params);

		pageModels(mv, pageVO.getList(), page, pageVO.getTotal());
		params.put("areaCode", areaCode); //还原参数
		if (flag == 1) {
			params.remove("endDate");
		}
		mv.addObject("params", params);
		mv.addObject("awardTypeList",
				commodityCategoryService.findValidBySuperType(CommodityConstant.SUPER_TYPE_RESERVE));
		mv.addObject("itemList", activityItemService.queryAll());

		return mv;
	}

	/**
	 * 新增或更新
	 */
	@ResponseBody
	@RequestMapping("/save")
	public JsonResponseMsg save(HttpServletRequest req, AmuseActivityInfo amuseActivityInfo,
			String applyStartDateString, String startDateString, String endDateString, String verifyEndDateString,
			String iconId, String bannerId, String superAdmin) {
		JsonResponseMsg result = new JsonResponseMsg();
		SystemUser user = Servlets.getSessionUser(req);
		boolean isActivityAdmin = false;
		if (null == user) {
			result.fill(CommonConstant.CODE_ERROR_LOGIC, "登录已失效，请重新登录");
			return result;
		} else {
			isActivityAdmin = SystemUserConstant.TYPE_ACTIVITY_ADMIN.equals(user.getUserType()) ? true : false;
		}

		if (null != amuseActivityInfo.getId()) { //编辑
			AmuseActivityInfo amuseActivityInfoUpdate = amuseActivityInfoService.findById(amuseActivityInfo.getId());
			if (null != amuseActivityInfoUpdate) {
				if (StringUtils.isNotBlank(amuseActivityInfo.getTitle())) {
					amuseActivityInfoUpdate.setTitle(amuseActivityInfo.getTitle());
				}
				if (StringUtils.isNotBlank(amuseActivityInfo.getSubTitle())) {
					amuseActivityInfoUpdate.setSubTitle(amuseActivityInfo.getSubTitle());
				}
				if (null != amuseActivityInfo.getWay()) {
					int way = NumberUtils.toInt(amuseActivityInfo.getWay().toString());
					if (isActivityAdmin && way == 2) { //子账号只能发布线下比赛
						result.fill(CommonConstant.CODE_ERROR_PARAM, "子账号只能发布线下比赛");
						return result;
					}
					if (way == 1 && null == amuseActivityInfo.getNetbarId()
							&& null == amuseActivityInfoUpdate.getNetbarId()) {
						result.fill(CommonConstant.CODE_ERROR_PARAM, "线下赛事，请选择一个网吧");
						return result;
					}
					amuseActivityInfoUpdate.setWay(way);
					if (amuseActivityInfoUpdate.getWay().toString().equals("1")) { //线下
						amuseActivityInfoUpdate.setType(2);
						if (null != amuseActivityInfo.getNetbarId()) {
							amuseActivityInfoUpdate.setNetbarId(amuseActivityInfo.getNetbarId());
						}
					} else { //线上
						amuseActivityInfoUpdate.setType(1);
						amuseActivityInfoUpdate.setNetbarId(null);
					}
				} else if (StringUtils.isBlank(superAdmin)) { //子账号
					if (null != amuseActivityInfo.getNetbarId()) {
						amuseActivityInfoUpdate.setNetbarId(amuseActivityInfo.getNetbarId());
					}
				}
				if (StringUtils.isNotBlank(amuseActivityInfo.getServer())) {
					amuseActivityInfoUpdate.setServer(amuseActivityInfo.getServer());
				}
				if (StringUtils.isNotBlank(amuseActivityInfo.getReward())) {
					amuseActivityInfoUpdate.setReward(amuseActivityInfo.getReward());
				}
				if (StringUtils.isNotBlank(amuseActivityInfo.getRule())) {
					amuseActivityInfoUpdate.setRule(amuseActivityInfo.getRule());
				}
				if (null != amuseActivityInfo.getContactType()) {
					amuseActivityInfoUpdate.setContactType(amuseActivityInfo.getContactType());
				}
				if (StringUtils.isNotBlank(amuseActivityInfo.getContact())) {
					amuseActivityInfoUpdate.setContact(amuseActivityInfo.getContact());
				}
				if (null != amuseActivityInfo.getMaxNum()) {
					amuseActivityInfoUpdate.setMaxNum(amuseActivityInfo.getMaxNum());
				}
				if (null != amuseActivityInfo.getVirtualApply()) {
					amuseActivityInfoUpdate.setVirtualApply(amuseActivityInfo.getVirtualApply());
				}
				if (StringUtils.isNotBlank(applyStartDateString)) {
					Date applyStartDate = null;
					try {
						applyStartDate = DateUtils.stringToDateYyyyMMddhhmmss(applyStartDateString);
					} catch (ParseException e) {
						LOGGER.error("日期转换日常:" + e);
					}
					amuseActivityInfoUpdate.setApplyStart(applyStartDate);
				}
				if (StringUtils.isNotBlank(startDateString)) {
					Date startDate = null;
					try {
						startDate = DateUtils.stringToDateYyyyMMddhhmmss(startDateString);
					} catch (ParseException e) {
						LOGGER.error("日期转换日常:" + e);
					}
					amuseActivityInfoUpdate.setStartDate(startDate);
				}
				if (StringUtils.isNotBlank(endDateString)) {
					Date endDate = null;
					try {
						endDate = DateUtils.stringToDateYyyyMMddhhmmss(endDateString);
					} catch (ParseException e) {
						LOGGER.error("日期转换日常:" + e);
					}
					amuseActivityInfoUpdate.setEndDate(endDate);
				}
				if (StringUtils.isNotBlank(verifyEndDateString)) {
					Date verifyEndDate = null;
					try {
						verifyEndDate = DateUtils.stringToDateYyyyMMddhhmmss(verifyEndDateString);
					} catch (ParseException e) {
						LOGGER.error("日期转换日常:" + e);
					}
					amuseActivityInfoUpdate.setVerifyEndDate(verifyEndDate);
				}
				MultipartFile fileMain = Servlets.getMultipartFile(req, "icon_File");
				if (null != fileMain) {
					AmuseActivityIcon amuseActivityIconUpdate = amuseActivityIconService
							.findById(NumberUtils.toLong(iconId));
					if (null != amuseActivityIconUpdate) {
						String systemName = "wy-web-admin";
						String src = ImgUploadUtil.genFilePath("amuse");
						Map<String, String> imgPath = ImgUploadUtil.save(fileMain, systemName, src);
						amuseActivityIconUpdate.setIcon(imgPath.get(ImgUploadUtil.KEY_MAP_SRC));

						amuseActivityIconUpdate.setUpdateDate(new Date());
						amuseActivityIconService.save(amuseActivityIconUpdate);
					}
				}
				MultipartFile banner = Servlets.getMultipartFile(req, "banner_File");
				if (null != banner) {
					AmuseActivityIcon amuseActivityBannerUpdate = amuseActivityIconService
							.findById(NumberUtils.toLong(bannerId));
					if (null != amuseActivityBannerUpdate) {
						String systemName = "wy-web-admin";
						String src = ImgUploadUtil.genFilePath("amuse");
						Map<String, String> imgPath = ImgUploadUtil.save(banner, systemName, src);
						amuseActivityBannerUpdate.setIcon(imgPath.get(ImgUploadUtil.KEY_MAP_SRC));

						amuseActivityBannerUpdate.setUpdateDate(new Date());
						amuseActivityIconService.save(amuseActivityBannerUpdate);
					}
				}
				if (null != amuseActivityInfo.getState()) {
					amuseActivityInfoUpdate.setState(amuseActivityInfo.getState());
					if (amuseActivityInfo.getState().toString().equals("2")) {
						amuseActivityInfoUpdate.setReleaseDate(new Date());
					}
				}

				if (null != amuseActivityInfo.getTakeType()) {
					amuseActivityInfoUpdate.setTakeType(amuseActivityInfo.getTakeType());
					if (amuseActivityInfoUpdate.getTakeType().toString().equals("2")) {
						amuseActivityInfoUpdate.setTeamNameReq(CommonConstant.INT_BOOLEAN_TRUE);
					}
					if (amuseActivityInfoUpdate.getTakeType().toString().equals("1")) {
						amuseActivityInfoUpdate.setTeamNameReq(CommonConstant.INT_BOOLEAN_FALSE);
					}
				}
				if (null != amuseActivityInfo.getItemId()) {
					amuseActivityInfoUpdate.setItemId(amuseActivityInfo.getItemId());
				}
				if (null != amuseActivityInfo.getAwardType()) {
					amuseActivityInfoUpdate.setAwardType(amuseActivityInfo.getAwardType());
				}
				if (null != amuseActivityInfo.getAwardSubType()) {
					amuseActivityInfoUpdate.setAwardSubType(amuseActivityInfo.getAwardSubType());
				}
				if (null != amuseActivityInfo.getAwardAmount()) {
					amuseActivityInfoUpdate.setAwardAmount(amuseActivityInfo.getAwardAmount());
				}

				amuseActivityInfoUpdate.setTelReq(amuseActivityInfo.getTelReq());
				amuseActivityInfoUpdate.setNameReq(amuseActivityInfo.getNameReq());
				amuseActivityInfoUpdate.setAccountReq(amuseActivityInfo.getAccountReq());
				amuseActivityInfoUpdate.setServerReq(amuseActivityInfo.getServerReq());
				amuseActivityInfoUpdate.setQqReq(amuseActivityInfo.getQqReq());
				amuseActivityInfoUpdate.setIdCardReq(amuseActivityInfo.getIdCardReq());
				amuseActivityInfoUpdate.setSummary(amuseActivityInfo.getSummary());
				amuseActivityInfoUpdate.setDeliverDay(amuseActivityInfo.getDeliverDay());
				amuseActivityInfoUpdate.setVerifyContent(amuseActivityInfo.getVerifyContent());
				amuseActivityInfoUpdate.setGrantMsg(amuseActivityInfo.getGrantMsg());
				amuseActivityInfoUpdate.setUpdateDate(new Date());
				amuseActivityInfoService.save(amuseActivityInfoUpdate);
			}
		} else { //新增
			//检查略缩图是否已传
			MultipartFile fileMain = Servlets.getMultipartFile(req, "icon_File");
			if (null == fileMain) {
				result.fill(CommonConstant.CODE_ERROR_PARAM, "主图为必传");
				return result;
			}
			//检查Banner是否已传
			MultipartFile banner = Servlets.getMultipartFile(req, "banner_File");
			//统一参数
			if (!StringUtils.isAllNotBlank(amuseActivityInfo.getTitle(), amuseActivityInfo.getSubTitle(),
					amuseActivityInfo.getServer(), amuseActivityInfo.getReward(), amuseActivityInfo.getRule(),
					amuseActivityInfo.getContact(), applyStartDateString, startDateString, endDateString,
					verifyEndDateString)
					|| amuseActivityInfo.getTelReq() == null && amuseActivityInfo.getNameReq() == null
							&& amuseActivityInfo.getIdCardReq() == null && amuseActivityInfo.getAccountReq() == null
							&& amuseActivityInfo.getServerReq() == null && amuseActivityInfo.getQqReq() == null) {
				result.fill(CommonConstant.CODE_ERROR_PARAM, "红色*选项为必填");
				return result;
			}
			Object o = amuseActivityInfo.getWay();
			int way = NumberUtils.toInt(null == o ? "1" : o.toString());
			if (isActivityAdmin && way == 2) { //子账号只能发布线下比赛
				result.fill(CommonConstant.CODE_ERROR_PARAM, "子账号只能发布线下比赛");
				return result;
			}
			if (way == 1 && null == amuseActivityInfo.getNetbarId()) { //线下
				result.fill(CommonConstant.CODE_ERROR_PARAM, "线下赛事，请选择一个网吧");
				return result;
			}
			//参数检查完毕,新增赛事
			AmuseActivityInfo amuseActivityInfoNew = new AmuseActivityInfo();
			amuseActivityInfoNew.setTitle(amuseActivityInfo.getTitle());
			amuseActivityInfoNew.setSubTitle(amuseActivityInfo.getSubTitle());

			if (isActivityAdmin && null == amuseActivityInfo.getNetbarId()) { //子账号只能发布线下比赛
				result.fill(CommonConstant.CODE_ERROR_PARAM, "请选择一个网吧（子账号只能发布线下比赛）");
				return result;
			}
			if (isActivityAdmin) {
				way = 1;
			}
			if (way == 2) {
				amuseActivityInfoNew.setType(1); //官方线上
			} else if (way == 1) {
				amuseActivityInfoNew.setType(2); //官方线下
				amuseActivityInfoNew.setNetbarId(amuseActivityInfo.getNetbarId());
			}
			amuseActivityInfoNew.setWay(way);
			amuseActivityInfoNew.setServer(amuseActivityInfo.getServer());
			amuseActivityInfoNew.setReward(amuseActivityInfo.getReward());
			amuseActivityInfoNew.setRule(amuseActivityInfo.getRule());
			amuseActivityInfoNew.setMaxNum(amuseActivityInfo.getMaxNum());
			amuseActivityInfoNew.setVirtualApply(amuseActivityInfo.getVirtualApply());
			amuseActivityInfoNew.setContactType(amuseActivityInfo.getContactType());
			amuseActivityInfoNew.setContact(amuseActivityInfo.getContact());
			amuseActivityInfoNew.setTakeType(amuseActivityInfo.getTakeType());
			if (amuseActivityInfoNew.getTakeType().toString().equals("2")) {
				amuseActivityInfoNew.setTeamNameReq(CommonConstant.INT_BOOLEAN_TRUE);
			}
			amuseActivityInfoNew.setItemId(amuseActivityInfo.getItemId());
			amuseActivityInfoNew.setAwardType(amuseActivityInfo.getAwardType());
			amuseActivityInfoNew.setAwardSubType(amuseActivityInfo.getAwardSubType());
			amuseActivityInfoNew.setAwardAmount(amuseActivityInfo.getAwardAmount());
			amuseActivityInfoNew.setTelReq(amuseActivityInfo.getTelReq());
			amuseActivityInfoNew.setNameReq(amuseActivityInfo.getNameReq());
			amuseActivityInfoNew.setAccountReq(amuseActivityInfo.getAccountReq());
			amuseActivityInfoNew.setServerReq(amuseActivityInfo.getServerReq());
			amuseActivityInfoNew.setQqReq(amuseActivityInfo.getQqReq());
			amuseActivityInfoNew.setIdCardReq(amuseActivityInfo.getIdCardReq());
			amuseActivityInfoNew.setRecommendSign(-1);
			amuseActivityInfoNew.setSummary(amuseActivityInfo.getSummary());
			amuseActivityInfoNew.setDeliverDay(amuseActivityInfo.getDeliverDay());
			amuseActivityInfoNew.setVerifyContent(amuseActivityInfo.getVerifyContent());
			amuseActivityInfoNew.setGrantMsg(amuseActivityInfo.getGrantMsg());

			Date applyStartDate = null;
			Date startDate = null;
			Date endDate = null;
			Date verifyEndDate = null;
			try {
				applyStartDate = DateUtils.stringToDateYyyyMMddhhmmss(applyStartDateString);
				startDate = DateUtils.stringToDateYyyyMMddhhmmss(startDateString);
				endDate = DateUtils.stringToDateYyyyMMddhhmmss(endDateString);
				verifyEndDate = DateUtils.stringToDateYyyyMMddhhmmss(verifyEndDateString);
			} catch (ParseException e) {
				LOGGER.error("日期转换日常:" + e);
			}
			amuseActivityInfoNew.setApplyStart(applyStartDate);
			amuseActivityInfoNew.setStartDate(startDate);
			amuseActivityInfoNew.setEndDate(endDate);
			amuseActivityInfoNew.setVerifyEndDate(verifyEndDate);
			amuseActivityInfoNew.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			amuseActivityInfoNew.setCreateUserId(user.getId()); //娱乐赛列表根据帐号权限走
			amuseActivityInfoNew.setState(amuseActivityInfo.getState());
			if (amuseActivityInfoNew.getState().toString().equals("2")) { //状态，0-待审核，1-审核被拒，2-发布，3-未发布
				amuseActivityInfoNew.setReleaseDate(new Date());
			}
			amuseActivityInfoNew.setCreateDate(new Date());

			amuseActivityInfoService.save(amuseActivityInfoNew);

			//上传主图片（略缩图）
			String systemName = "wy-web-admin";
			String src = ImgUploadUtil.genFilePath("amuse");
			if (fileMain != null) {
				Map<String, String> imgPath = ImgUploadUtil.save(fileMain, systemName, src);
				AmuseActivityIcon amuseActivityIcon = new AmuseActivityIcon();
				amuseActivityIcon.setActivityId(amuseActivityInfoNew.getId()); //拿到刚保存的赛事ID
				amuseActivityIcon.setIcon(imgPath.get(ImgUploadUtil.KEY_MAP_SRC));
				amuseActivityIcon.setIsMain(CommonConstant.INT_BOOLEAN_TRUE); //主图
				amuseActivityIcon.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				amuseActivityIcon.setCreateDate(new Date());

				amuseActivityIconService.save(amuseActivityIcon);
			}

			//上传Banner
			if (banner != null) {
				Map<String, String> bannerPath = ImgUploadUtil.save(banner, systemName, src);
				AmuseActivityIcon amuseActivityBanner = new AmuseActivityIcon();
				amuseActivityBanner.setActivityId(amuseActivityInfoNew.getId()); //拿到刚保存的赛事ID
				amuseActivityBanner.setIcon(bannerPath.get(ImgUploadUtil.KEY_MAP_SRC));
				amuseActivityBanner.setIsMain(CommonConstant.INT_BOOLEAN_FALSE); //非主图
				amuseActivityBanner.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				amuseActivityBanner.setCreateDate(new Date());

				amuseActivityIconService.save(amuseActivityBanner);
			}
		}

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 请求详细信息
	 */
	@ResponseBody
	@RequestMapping("/info/{amuseId}")
	public JsonResponseMsg detail(@PathVariable("amuseId") long amuseId) {
		JsonResponseMsg result = new JsonResponseMsg();
		AmuseActivityInfo commodityInfo = amuseActivityInfoService.findById(amuseId);

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, commodityInfo);
		return result;
	}

	/**
	 * 删除/恢复
	 */
	@ResponseBody
	@RequestMapping("/validChange/{amuseId}/{valid}")
	public JsonResponseMsg validChange(@PathVariable("amuseId") long amuseId, @PathVariable("valid") int valid) {
		JsonResponseMsg result = new JsonResponseMsg();

		amuseActivityInfoService.updateStatusByAmuseId(amuseId, valid);

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 发布/取消发布
	 */
	@ResponseBody
	@RequestMapping("/stateChange/{amuseId}/{state}")
	public JsonResponseMsg stateChange(@PathVariable("amuseId") long amuseId, @PathVariable("state") int state) {
		JsonResponseMsg result = new JsonResponseMsg();

		AmuseActivityInfo amuseActivityInfo = amuseActivityInfoService.findById(amuseId);
		if (null != amuseActivityInfo) {
			amuseActivityInfo.setState(state);
			amuseActivityInfo.setUpdateDate(new Date());
			amuseActivityInfoService.save(amuseActivityInfo);
		}

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * AJAX根据地区请求网吧信息
	 */
	@ResponseBody
	@RequestMapping("/getNetbars/{areaCode}")
	public JsonResponseMsg stateChange(@PathVariable("areaCode") String areaCode) {
		JsonResponseMsg result = new JsonResponseMsg();

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS,
				netbarInfoService.findAllNetbarIdandNameByAreaCode(areaCode));
		return result;
	}

	/**
	 * 娱乐赛报名统计
	 */
	@RequestMapping("/applyList/{page}")
	public ModelAndView statistic(@PathVariable("page") int page, String activityId, String nickname, String telephone,
			String qq, String startDate, String endDate, String flag) {
		ModelAndView mv = new ModelAndView("amuse/applyList"); //跳转到applyList.ftl页面
		Map<String, Object> params = Maps.newHashMap();
		params.put("activityId", NumberUtils.toLong(activityId, 0));
		if (StringUtils.isNotBlank(nickname)) {
			params.put("nickname", nickname);
		}
		if (StringUtils.isNotBlank(telephone)) {
			params.put("telephone", telephone);
		}
		if (StringUtils.isNotBlank(qq)) {
			params.put("qq", qq);
		}
		if (StringUtils.isNotBlank(startDate)) {
			params.put("startDate", startDate);
		}
		if (StringUtils.isNotBlank(endDate)) {
			params.put("endDate", endDate);
		}
		PageVO pageVO = amuseActivityInfoService.listApply(page, params);

		pageModels(mv, pageVO.getList(), page, pageVO.getTotal());
		mv.addObject("params", params);
		mv.addObject("flag", flag);

		return mv;
	}

	/**
	 * 导出报名用户信息
	 */
	@RequestMapping("/exportExcel")
	public ModelAndView exportApplyUserInfo(HttpServletResponse res, String amuseId) {
		// 查询报名数据
		long amuseIdLong = NumberUtils.toLong(amuseId);
		Map<String, Object> params = Maps.newHashMap();
		params.put("activityId", NumberUtils.toLong(amuseId));
		params.put("no_limit", 1);
		List<Map<String, Object>> list = amuseActivityInfoService.listApply(1, params).getList();

		// Excel标题
		AmuseActivityInfo amuseActivityInfo = amuseActivityInfoService.findById(amuseIdLong);
		String title = StringUtils.EMPTY;
		title = "【" + amuseActivityInfo.getTitle() + "】报名用户信息";

		// 编辑excel内容
		String[][] contents = new String[list.size() + 1][];

		// 设置标题行
		String[] contentTitle = new String[9];
		contentTitle[0] = "#";
		contentTitle[1] = "昵称";
		contentTitle[2] = "手机号";
		contentTitle[3] = "QQ号";
		contentTitle[4] = "游戏账号";
		contentTitle[5] = "游戏大区";
		contentTitle[6] = "身份证号";
		contentTitle[7] = "团队名称";
		contentTitle[8] = "报名时间";
		contents[0] = contentTitle;

		// 设置内容
		if (CollectionUtils.isNotEmpty(list)) {
			int i = 0;
			for (Map<String, Object> map : list) {
				String[] row = new String[9];
				row[0] = null == map.get("id") ? "" : map.get("id").toString();
				row[1] = null == map.get("nickname") ? "" : map.get("nickname").toString();
				row[2] = null == map.get("telephone") ? "" : map.get("telephone").toString();
				row[3] = null == map.get("qq") ? "" : map.get("qq").toString();
				row[4] = null == map.get("gameAccount") ? "" : map.get("gameAccount").toString();
				row[5] = null == map.get("server") ? "" : map.get("server").toString();
				row[6] = null == map.get("idCard") ? "" : map.get("idCard").toString();
				row[7] = null == map.get("teamName") ? "" : map.get("teamName").toString();
				row[8] = null == map.get("createDate") ? ""
						: DateUtils.dateToString((Date) map.get("createDate"), DateUtils.YYYY_MM_DD_HH_MM_SS);
				contents[i + 1] = row;
				i++;
			}
		}
		// 导出Excel
		try {
			ExcelUtils.exportExcel(title, contents, false, res);
		} catch (Exception e) {
			LOGGER.error("导出数据异常：", e);
		}
		return null;
	}

}
