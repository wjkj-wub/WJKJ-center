package com.miqtech.master.admin.web.controller.backend.information;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.config.SystemConfig;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.InformationConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.activity.ActivityOverActivity;
import com.miqtech.master.entity.activity.ActivityOverActivityModule;
import com.miqtech.master.service.activity.ActivityOverActivityModuleService;
import com.miqtech.master.service.activity.ActivityOverActivityService;
import com.miqtech.master.thirdparty.util.img.ImgUploadUtil;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.JsonUtils;
import com.miqtech.master.vo.PageVO;

@Controller
@RequestMapping("overActivity/banner")
public class ActivityOverActivityBannerController extends BaseController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ActivityOverActivityBannerController.class);

	@Autowired
	private SystemConfig systemConfig;
	@Autowired
	private ActivityOverActivityService activityOverActivityService;
	@Autowired
	private ActivityOverActivityModuleService activityOverActivityModuleServices;

	/**
	 * 资讯banner列表
	 */
	@RequestMapping("list/{page}")
	public String bannerList(Model model, @PathVariable("page") int page, Integer banner, String dateStart,
			String dateEnd, Long moduleId) {
		PageVO vo = activityOverActivityService.queryBanner(moduleId, banner, dateStart, dateEnd, page);
		List<Map<String, Object>> amodule = activityOverActivityService.queryInfoByModule();
		int maxOrderNum = activityOverActivityService.defaultOrderNum();
		this.pageData(model, vo.getList(), page, vo.getTotal());
		model.addAttribute("maxOrderNum", maxOrderNum);
		model.addAttribute("modules", amodule);
		model.addAttribute("banner", banner);
		model.addAttribute("dateStart", dateStart);
		model.addAttribute("dateEnd", dateEnd);
		model.addAttribute("moduleId", moduleId);
		model.addAttribute("imgServer", systemConfig.getImgServerDomain());
		return "information/bannerList";
	}

	/**
	 * 编辑
	 */
	@RequestMapping("edit")
	public ModelAndView edit(String id) {
		ModelAndView mv = new ModelAndView("information/bannerEdit");

		List<ActivityOverActivityModule> modules = activityOverActivityModuleServices
				.getValidTreeByType(InformationConstant.MODULE_TYPE_INFO, true);
		/*List<Map<String, Object>> modules = activityOverActivityService.queryInfoByModule();*/
		boolean isInsert = true;
		if (NumberUtils.isNumber(id)) {
			long idLong = NumberUtils.toLong(id);
			ActivityOverActivity module = activityOverActivityService.findById(idLong);
			if (module != null) {
				mv.addObject("module", module);
				isInsert = false;
			}
			//通过咨询id查找模块id
			List<Map<String, Object>> moduleIdQuery = activityOverActivityService.queryMouleId(idLong);
			mv.addObject("moduleIdQuery", moduleIdQuery);
		}
		mv.addObject("isInsert", isInsert);
		String modulesJson = JsonUtils.objectToString(modules);
		mv.addObject("modulesJson", modulesJson);
		return mv;
	}

	/**
	 * 资讯banner获取目标对象
	 */
	@ResponseBody
	@RequestMapping("goalobjectquery")
	public String goalobjectquery(HttpServletRequest request) {
		String type = request.getParameter("type");
		String moduleId = request.getParameter("moduleId");
		String infoId = request.getParameter("infoId");

		Integer typeInt = null;
		if (NumberUtils.isNumber(type)) {
			typeInt = NumberUtils.toInt(type);
		}
		Long moduleIdLong = null;
		if (NumberUtils.isNumber(moduleId)) {
			moduleIdLong = NumberUtils.toLong(moduleId);
		}
		Long infoIdLong = null;
		if (NumberUtils.isNumber(infoId)) {
			infoIdLong = NumberUtils.toLong(infoId);
		}

		List<Map<String, Object>> modulesJson = activityOverActivityService.queryBannerInfoByTypeAndModuleId(typeInt,
				moduleIdLong, infoIdLong);
		return JsonUtils.objectToString(modulesJson);
	}

	/**
	 * 更改排序序号
	 */
	@ResponseBody
	@RequestMapping("order")
	public JsonResponseMsg order(Long bannerID, Integer orderNum) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (bannerID != null && orderNum != null) {
			activityOverActivityService.changeOrderNum(bannerID, orderNum);
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 * 更改状态
	 */
	@ResponseBody
	@RequestMapping("statusChange")
	public String statusChange(String id, String valid) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (NumberUtils.isNumber(id)) {
			long idLong = NumberUtils.toLong(id);
			int validInt = NumberUtils.toInt(valid, 1);
			activityOverActivityService.bannerShowById(idLong, CommonConstant.INT_BOOLEAN_TRUE.equals(validInt));
		}
		return JsonUtils.objectToString(result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS));
	}

	@RequestMapping("infoList")
	@ResponseBody
	public String infoList(Integer type) {
		List<Map<String, Object>> result = activityOverActivityService.queryInfoByType(type);
		return JsonUtils.objectToString(result);
	}

	/**
	 * 保存banner
	 */
	@RequestMapping("bannerSave")
	@ResponseBody
	public String bannerSave(HttpServletRequest request, ActivityOverActivity activityOverActivity, String targetId,
			String bannerTimerDateStr, String moduleId, Integer sortNum) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (!NumberUtils.isNumber(targetId)) {
			return JsonUtils
					.objectToString(result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM));
		}
		long targetIdLong = NumberUtils.toLong(targetId);

		MultipartFile imgFile = Servlets.getMultipartFile(request, "imgFile");
		if (imgFile != null) {// 有图片时上传文件
			String systemName = "wy-web-admin";
			String src = ImgUploadUtil.genFilePath("info");
			Map<String, String> saveResult = ImgUploadUtil.save(imgFile, systemName, src);
			activityOverActivity.setBannerIcon(saveResult.get(ImgUploadUtil.KEY_MAP_SRC));
		}
		if (StringUtils.isNotBlank(bannerTimerDateStr)) {
			try {
				Date bannerTimerDate = DateUtils.stringToDateYyyyMMddhhmmss(bannerTimerDateStr);
				activityOverActivity.setBannerTimerDate(bannerTimerDate);
			} catch (Exception e) {
				LOGGER.error("格式化生效时间异常:", e);
			}
		}

		ActivityOverActivity info = activityOverActivityService.findById(targetIdLong);
		if (info == null) {
			return JsonUtils.objectToString(result.fill(CommonConstant.CODE_ERROR_LOGIC, "找不到资讯信息"));
		}

		// 新增banner时,须避免某个模块下,banner数超过8个
		if (!StringUtils.equals(moduleId, targetId)) {
			int maxCount = 8;
			List<Map<String, Object>> tooMuchBannerModules = activityOverActivityService
					.queryTooMuchBannerModulesByInfoId(targetIdLong, maxCount);
			if (CollectionUtils.isNotEmpty(tooMuchBannerModules)) {
				String moduleName = MapUtils.getString(tooMuchBannerModules.get(0), "name");
				return JsonUtils.objectToString(result.fill(CommonConstant.CODE_ERROR_LOGIC,
						"当前资讯的" + moduleName + "模块下,已超过" + maxCount + "个banner,请选择其他模块重试"));
			}

			int maxOrderNum = activityOverActivityService.defaultOrderNum();
			maxOrderNum = maxOrderNum + 1;
			activityOverActivity.setOrderNum(maxOrderNum);
		}

		// 已产生的banner更改目标对象时,失效原banner
		if (NumberUtils.isNumber(moduleId) && NumberUtils.isNumber(targetId)
				&& !StringUtils.equals(moduleId, targetId)) {
			long moduleIdLong = NumberUtils.toLong(moduleId);
			ActivityOverActivity a = new ActivityOverActivity();
			a.setId(moduleIdLong);
			a.setIsBannerShow(CommonConstant.INT_BOOLEAN_FALSE);
			a.setBannerCreateDate(null);
			a = activityOverActivityService.saveOrUpdate(a);

			activityOverActivity.setBannerIcon(a.getBannerIcon());
		}
		//保持banner排序不变
		if (sortNum != 0) {
			activityOverActivity.setOrderNum(sortNum);
		}
		// 查询资讯id
		activityOverActivity.setIsBannerShow(CommonConstant.INT_BOOLEAN_TRUE);
		activityOverActivity.setBannerValid(CommonConstant.INT_BOOLEAN_TRUE);
		activityOverActivity.setId(targetIdLong);
		activityOverActivity.setReadNum(info.getReadNum());
		activityOverActivity.setBannerCreateDate(new Date());
		info = BeanUtils.updateBean(info, activityOverActivity);
		activityOverActivityService.save(info);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return JsonUtils.objectToString(result);
	}

	@ResponseBody
	@RequestMapping("bannerDelete/{id}")
	public String delete(@PathVariable Long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		ActivityOverActivity activityOverActivity = activityOverActivityService.findById(id);
		activityOverActivity.setId(id);
		activityOverActivity.setIsBannerShow(0);
		activityOverActivityService.saveOrUpdate(activityOverActivity);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return JsonUtils.objectToString(result);
	}

}
