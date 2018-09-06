package com.miqtech.master.admin.web.controller.backend.amuse;

import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.SystemUserConstant;
import com.miqtech.master.consts.mall.CommodityConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.amuse.AmuseActivityIcon;
import com.miqtech.master.entity.amuse.AmuseActivityInfo;
import com.miqtech.master.entity.common.SystemUser;
import com.miqtech.master.service.activity.ActivityItemService;
import com.miqtech.master.service.amuse.AmuseActivityIconService;
import com.miqtech.master.service.amuse.AmuseActivityInfoService;
import com.miqtech.master.service.mall.CommodityCategoryService;
import com.miqtech.master.service.system.SysUserAreaService;
import com.miqtech.master.service.system.SystemAreaService;
import com.miqtech.master.thirdparty.util.img.ImgUploadUtil;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;
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
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

/**
 * 网吧娱乐赛管理,审核
 */
@Controller
@RequestMapping("netbarAmuse/")
public class NetbarAmuseController extends BaseController {
	private static final Logger LOGGER = LoggerFactory.getLogger(NetbarAmuseController.class);

	@Autowired
	private AmuseActivityInfoService amuseActivityInfoService;
	@Autowired
	private AmuseActivityIconService amuseActivityIconService;
	@Autowired
	private SystemAreaService systemAreaService;
	@Autowired
	private ActivityItemService activityItemService;
	@Autowired
	private SysUserAreaService sysUserAreaService;
	@Autowired
	private CommodityCategoryService commodityCategoryService;

	/**
	 * 娱乐赛列表
	 */
	@RequestMapping("/list/{page}")
	public ModelAndView commodityList(HttpServletRequest request, @PathVariable("page") int page, String title,
			String netbarName, String way, String valid, String state, String awardType, String startDate,
			String endDate, String areaCode) {
		ModelAndView mv = new ModelAndView("amuse/netbarAmuseList"); //跳转到netbarAmuseList.ftl页面
		SystemUser user = Servlets.getSessionUser(request);
		boolean isActivityAdmin = false;
		if (null == user) {
			backIndex(mv, "登录失败,请检查用户名和密码.");
			return mv;
		} else {
			isActivityAdmin = SystemUserConstant.TYPE_ACTIVITY_ADMIN.equals(user.getUserType()) ? true : false;
		}

		Map<String, Object> params = Maps.newHashMap();
		if (!isActivityAdmin) { //最高管理员
			mv.addObject("superAdmin", "1");
			if (StringUtils.isNotBlank(areaCode)) {
				params.put("areaCode", areaCode.substring(0, 2)); //精确到省级
			}
		} else { //子账号
			String userAreaCode = sysUserAreaService.findUserAreaCode(user.getId());//匹配子账号的地区
			userAreaCode = null == userAreaCode ? "000000" : userAreaCode;
			params.put("areaCode", userAreaCode.substring(0, 2));
		}

		if (StringUtils.isNotBlank(title)) {
			params.put("title", title);
		}
		if (StringUtils.isNotBlank(netbarName)) {
			params.put("netbarName", netbarName);
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
		mv.addObject("provinceList", systemAreaService.queryAllRoot());
		mv.addObject("itemList", activityItemService.queryAll());
		mv.addObject("awardTypeList",
				commodityCategoryService.findValidBySuperType(CommodityConstant.SUPER_TYPE_RESERVE));

		return mv;
	}

	/**
	 * 编辑/审核
	 */
	@ResponseBody
	@RequestMapping("/save")
	public JsonResponseMsg save(HttpServletRequest req, AmuseActivityInfo amuseActivityInfo,
			String applyStartDateString, String startDateString, String endDateString, String verifyEndDateString,
			String iconId, String bannerId) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (null != amuseActivityInfo.getId()) { //编辑
			AmuseActivityInfo amuseActivityInfoUpdate = amuseActivityInfoService.findById(amuseActivityInfo.getId());
			if (null != amuseActivityInfoUpdate) {
				if (StringUtils.isNotBlank(amuseActivityInfo.getTitle())) {
					amuseActivityInfoUpdate.setTitle(amuseActivityInfo.getTitle());
				}
				if (StringUtils.isNotBlank(amuseActivityInfo.getSubTitle())) {
					amuseActivityInfoUpdate.setSubTitle(amuseActivityInfo.getSubTitle());
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

				if (null != amuseActivityInfo.getState()) { //审核状态
					amuseActivityInfoUpdate.setState(amuseActivityInfo.getState());
					if (amuseActivityInfo.getState().toString().equals("2")) { //若通过，则必须上传图片
						MultipartFile fileMain = Servlets.getMultipartFile(req, "icon_File");
						String systemName = "wy-web-admin";
						String src = ImgUploadUtil.genFilePath("amuse");
						// 略缩图
						if (StringUtils.isBlank(iconId) && null == fileMain) {
							result.fill(CommonConstant.CODE_ERROR_PARAM, "请上传略缩图");
							return result;
						} else if (StringUtils.isNotBlank(iconId)) { //编辑略缩图
							if (null != fileMain) {
								AmuseActivityIcon amuseActivityIconUpdate = amuseActivityIconService
										.findById(NumberUtils.toLong(iconId));
								if (null != amuseActivityIconUpdate) {

									Map<String, String> imgPath = ImgUploadUtil.save(fileMain, systemName, src);
									amuseActivityIconUpdate.setIcon(imgPath.get(ImgUploadUtil.KEY_MAP_SRC));
									amuseActivityIconUpdate.setUpdateDate(new Date());
									amuseActivityIconService.save(amuseActivityIconUpdate);
								}
							}
						} else { //新增略缩图
							Map<String, String> imgPath = ImgUploadUtil.save(fileMain, systemName, src);
							AmuseActivityIcon amuseActivityIcon = new AmuseActivityIcon();
							amuseActivityIcon.setActivityId(amuseActivityInfoUpdate.getId());
							amuseActivityIcon.setIcon(imgPath.get(ImgUploadUtil.KEY_MAP_SRC));
							amuseActivityIcon.setIsMain(CommonConstant.INT_BOOLEAN_TRUE);// 主图-->略缩图
							amuseActivityIcon.setValid(CommonConstant.INT_BOOLEAN_TRUE);
							amuseActivityIcon.setCreateDate(new Date());
							amuseActivityIconService.save(amuseActivityIcon);
						}

						amuseActivityInfoUpdate.setReleaseDate(new Date()); //更新发布日期
					}
				} else {
					result.fill(CommonConstant.CODE_ERROR_PARAM, "请选择审核处理方式");
					return result;
				}
				if (null != amuseActivityInfo.getRemark()) {
					amuseActivityInfoUpdate.setRemark(amuseActivityInfo.getRemark());
				}

				if (null != amuseActivityInfo.getTakeType()) {
					amuseActivityInfoUpdate.setTakeType(amuseActivityInfo.getTakeType());
					if (amuseActivityInfoUpdate.getTakeType().toString().equals("2")) {
						amuseActivityInfoUpdate.setTeamNameReq(CommonConstant.INT_BOOLEAN_TRUE);
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
				if (null != amuseActivityInfo.getTelReq()) {
					amuseActivityInfoUpdate.setTelReq(amuseActivityInfo.getTelReq());
				}
				if (null != amuseActivityInfo.getNameReq()) {
					amuseActivityInfoUpdate.setNameReq(amuseActivityInfo.getNameReq());
				}
				if (null != amuseActivityInfo.getAccountReq()) {
					amuseActivityInfoUpdate.setAccountReq(amuseActivityInfo.getAccountReq());
				}
				if (null != amuseActivityInfo.getServerReq()) {
					amuseActivityInfoUpdate.setServerReq(amuseActivityInfo.getServerReq());
				}
				if (null != amuseActivityInfo.getQqReq()) {
					amuseActivityInfoUpdate.setQqReq(amuseActivityInfo.getQqReq());
				}
				if (null != amuseActivityInfo.getIdCardReq()) {
					amuseActivityInfoUpdate.setIdCardReq(amuseActivityInfo.getIdCardReq());
				}
				amuseActivityInfoUpdate.setUpdateDate(new Date());
				amuseActivityInfoUpdate.setVerifyContent(amuseActivityInfo.getVerifyContent());
				amuseActivityInfoUpdate.setGrantMsg(amuseActivityInfo.getGrantMsg());
				amuseActivityInfoService.save(amuseActivityInfoUpdate);
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
	 * 娱乐赛报名统计
	 */
	@RequestMapping("/applyList/{page}")
	public ModelAndView statistic(@PathVariable("page") int page, String activityId, String nickname,
			String telephone) {
		ModelAndView mv = new ModelAndView("amuse/applyList"); //跳转到applyList.ftl页面
		Map<String, Object> params = Maps.newHashMap();
		params.put("activityId", NumberUtils.toLong(activityId, 0));
		if (StringUtils.isNotBlank(nickname)) {
			params.put("nickname", nickname);
		}
		if (StringUtils.isNotBlank(telephone)) {
			params.put("telephone", telephone);
		}
		PageVO pageVO = amuseActivityInfoService.listApply(page, params);

		pageModels(mv, pageVO.getList(), page, pageVO.getTotal());
		mv.addObject("params", params);

		return mv;
	}

}
