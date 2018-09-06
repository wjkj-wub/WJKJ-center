package com.miqtech.master.admin.web.controller.backend.information;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.config.SystemConfig;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.InformationConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.activity.ActivityOverActivity;
import com.miqtech.master.entity.activity.ActivityOverActivityImg;
import com.miqtech.master.entity.activity.ActivityOverActivityModel;
import com.miqtech.master.entity.activity.ActivityOverActivityModule;
import com.miqtech.master.entity.activity.ActivityOverActivityModuleInfo;
import com.miqtech.master.entity.matches.Matches;
import com.miqtech.master.service.activity.ActivityOverActivityImgService;
import com.miqtech.master.service.activity.ActivityOverActivityModelService;
import com.miqtech.master.service.activity.ActivityOverActivityModuleInfoService;
import com.miqtech.master.service.activity.ActivityOverActivityModuleService;
import com.miqtech.master.service.activity.ActivityOverActivityService;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.service.matches.MatchesService;
import com.miqtech.master.thirdparty.util.UpYunUploaderUtils;
import com.miqtech.master.thirdparty.util.img.ImgUploadUtil;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.JsonUtils;
import com.miqtech.master.vo.PageVO;

@Controller
@RequestMapping("overActivity")
public class ActivityOverActivityController extends BaseController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ActivityOverActivityController.class);
	public static final String REDIS_KEY_HLS_TASK_ID = "wy_hls_task_id";

	@Autowired
	private SystemConfig systemConfig;
	@Autowired
	private StringRedisOperateService stringRedisOperateService;
	@Autowired
	private ActivityOverActivityService activityOverActivityService;
	@Autowired
	private ActivityOverActivityModuleService activityOverActivityModuleService;
	@Autowired
	private ActivityOverActivityModuleInfoService activityOverActivityModuleInfoService;
	@Autowired
	private ActivityOverActivityImgService activityOverActivityImgService;
	@Autowired
	private ActivityOverActivityModelService activityOverActivityModelService;
	@Autowired
	private MatchesService matchesService;

	/**
	 * 分页
	 */
	@RequestMapping("list/{listType}/{page}")
	public ModelAndView page(@PathVariable("listType") String listType, @PathVariable("page") int page, String id,
			String moduleId, String title, String type, String efficient, String beginDate, String endDate,
			String timerBeginDate, String timerEndDate, String pid, String isPublished) {
		ModelAndView mv = new ModelAndView();

		if (StringUtils.isBlank(listType)) {
			listType = "1";// 默认查询普通资讯列表
		}
		mv.addObject("listType", listType);

		// 根据不同模块做不同设置
		Map<String, String> params = Maps.newHashMap();
		if ("1".equals(listType)) {// 普通资讯
			mv.setViewName("information/normalInfoList");
		} else if ("2".equals(listType)) {// 专题资讯
			mv.setViewName("information/subjectInfoList");
		} else if ("3".equals(listType)) {// 专题子资讯
			params.put("pid", pid);
			mv.setViewName("information/subjectSubInfoList");

			ActivityOverActivity subject = activityOverActivityService.findById(NumberUtils.toLong(pid, 0));
			mv.addObject("subject", subject);
		} else if ("4".equals(listType)) {// 视频资讯
			mv.setViewName("information/videoInfoList");
		}

		// 设置查询条件
		params.put("id", id);
		params.put("moduleId", moduleId);
		params.put("title", title);
		params.put("type", type);
		params.put("efficient", efficient);
		params.put("isPublished", isPublished);
		params.put("beginDate", beginDate);
		params.put("endDate", endDate);
		params.put("timerBeginDate", timerBeginDate);
		params.put("timerEndDate", timerEndDate);
		params.put("listType", listType);
		mv.addObject("params", params);

		PageVO vo = activityOverActivityService.adminPage(page, params);
		pageModels(mv, vo.getList(), page, vo.getTotal());

		String imgServerDomain = systemConfig.getImgServerDomain();
		mv.addObject("imgServer", imgServerDomain);

		// 查询模块
		if (!"4".equals(listType)) {
			List<ActivityOverActivityModule> modules = activityOverActivityModuleService
					.findRootValidByType(InformationConstant.MODULE_TYPE_INFO);
			mv.addObject("modules", modules);
		} else {
			List<ActivityOverActivityModule> modules = activityOverActivityModuleService
					.findRootValidByType(InformationConstant.MODULE_TYPE_VIDEO);
			mv.addObject("modules", modules);
		}

		return mv;
	}

	/**
	 * 普通资讯新增编辑
	 */
	@RequestMapping("normal/edit")
	public ModelAndView normalEdit(String id) {
		ModelAndView mv = new ModelAndView("information/normalInfoEdit");

		// 查询筛选所需的模块信息
		List<ActivityOverActivityModule> modules = activityOverActivityModuleService
				.getValidTreeByType(InformationConstant.MODULE_TYPE_INFO, false);
		mv.addObject("modules", modules);
		mv.addObject("modulesJson", JsonUtils.objectToString(modules));

		// 查询资讯信息
		long idLong = NumberUtils.toLong(id);
		ActivityOverActivity editObj = activityOverActivityService.findById(idLong);
		mv.addObject("editObj", editObj);

		// 查询资讯模块信息
		List<ActivityOverActivityModuleInfo> infoModules = activityOverActivityModuleInfoService
				.findByOverActivityId(idLong);
		mv.addObject("infoModules", infoModules);

		// 查询资讯图集信息
		List<Map<String, Object>> infoImgs = activityOverActivityImgService.queryValidGroupByOverActivityId(idLong);
		mv.addObject("infoImgsJson", JsonUtils.objectToString(infoImgs));
		List<ActivityOverActivityModel> models = activityOverActivityModelService.findValidAll();
		mv.addObject("modelsJson", JsonUtils.objectToString(models));
		mv.addObject("imgServer", systemConfig.getImgServerDomain());

		return mv;
	}

	/**
	 * 专题资讯编辑
	 */
	@RequestMapping("subject/edit")
	public ModelAndView subjectEdit(String id) {
		ModelAndView mv = new ModelAndView("information/subjectInfoEdit");

		// 查询筛选所需的模块信息
		List<ActivityOverActivityModule> modules = activityOverActivityModuleService
				.getValidTreeByType(InformationConstant.MODULE_TYPE_INFO, true);
		mv.addObject("modulesJson", JsonUtils.objectToString(modules));

		// 查询资讯信息
		long idLong = NumberUtils.toLong(id);
		ActivityOverActivity editObj = activityOverActivityService.findById(idLong);
		mv.addObject("editObj", editObj);

		// 查询资讯模块信息
		List<ActivityOverActivityModuleInfo> infoModules = activityOverActivityModuleInfoService
				.findByOverActivityId(idLong);
		mv.addObject("infoModules", infoModules);
		if (editObj != null) {

			if (null == editObj.getAudition()) {
				mv.addObject("matchesSelectValue", editObj.getActivityId());
				mv.addObject("auditionSelectValue", 0);
			} else if (1 == editObj.getAudition()) {
				mv.addObject("auditionSelectValue", editObj.getActivityId());
				mv.addObject("matchesSelectValue", 0);
			} else {
				mv.addObject("matchesSelectValue", 0);
				mv.addObject("auditionSelectValue", 0);
			}

		} else {
			mv.addObject("matchesSelectValue", 0);
			mv.addObject("auditionSelectValue", 0);
		}

		// 查询资讯图集信息
		List<Map<String, Object>> infoImgs = activityOverActivityImgService.queryValidGroupByOverActivityId(idLong);
		mv.addObject("infoImgsJson", JsonUtils.objectToString(infoImgs));
		List<ActivityOverActivityModel> models = activityOverActivityModelService.findValidAll();
		mv.addObject("modelsJson", JsonUtils.objectToString(models));

		List<Map<String, Object>> matches = matchesService.getAllMatchesInfoList();
		mv.addObject("matchesJson", JsonUtils.objectToString(matches));

		List<Map<String, Object>> auditions = matchesService.getAllAuditionInfoList();//添加海选赛数据列表
		mv.addObject("auditionJson", JsonUtils.objectToString(auditions));

		mv.addObject("imgServer", systemConfig.getImgServerDomain());
		mv.addObject("matchVaild", "1");

		return mv;
	}

	/**
	 * 专题子资讯编辑
	 */
	@RequestMapping("subjectSub/edit")
	public ModelAndView subjectSubEdit(String id, String pid) {
		ModelAndView mv = new ModelAndView("information/subjectSubInfoEdit");
		// 查询专题
		ActivityOverActivity subject = activityOverActivityService.findById(NumberUtils.toLong(pid, 0));
		mv.addObject("subject", subject);

		// 查询资讯信息
		long idLong = NumberUtils.toLong(id);
		ActivityOverActivity editObj = activityOverActivityService.findById(idLong);
		mv.addObject("editObj", editObj);

		// 查询资讯图集信息
		List<Map<String, Object>> infoImgs = activityOverActivityImgService.queryValidGroupByOverActivityId(idLong);
		mv.addObject("infoImgsJson", JsonUtils.objectToString(infoImgs));

		// 查询资讯模块信息
		List<ActivityOverActivityModuleInfo> infoModules = activityOverActivityModuleInfoService
				.findByOverActivityId(idLong);
		mv.addObject("infoModules", infoModules);
		List<ActivityOverActivityModel> models = activityOverActivityModelService.findValidAll();
		mv.addObject("modelsJson", JsonUtils.objectToString(models));

		return mv;
	}

	/**
	 * 视频编辑
	 */
	@RequestMapping("video/edit")
	public ModelAndView videoEdit(String id) {
		ModelAndView mv = new ModelAndView("information/videoInfoEdit");

		// 查询筛选所需的模块信息
		List<ActivityOverActivityModule> modules = activityOverActivityModuleService
				.getValidTreeByType(InformationConstant.MODULE_TYPE_VIDEO, false);
		mv.addObject("modulesJson", JsonUtils.objectToString(modules));

		// 查询资讯信息
		long idLong = NumberUtils.toLong(id);
		ActivityOverActivity editObj = activityOverActivityService.findById(idLong);
		mv.addObject("editObj", editObj);

		// 查询资讯图集信息
		List<Map<String, Object>> infoImgs = activityOverActivityImgService.queryValidGroupByOverActivityId(idLong);
		mv.addObject("infoImgsJson", JsonUtils.objectToString(infoImgs));

		// 查询资讯模块信息
		List<ActivityOverActivityModuleInfo> infoModules = activityOverActivityModuleInfoService
				.findByOverActivityId(idLong);
		mv.addObject("infoModules", infoModules);

		if (editObj != null) {
			String redisKey = REDIS_KEY_HLS_TASK_ID + "_" + editObj.getId();
			String taskId = stringRedisOperateService.getData(redisKey);
			if (StringUtils.isNotBlank(taskId)) {
				String progress = UpYunUploaderUtils.checkHlsFile(taskId);
				mv.addObject("hlsTaskProgress", progress);
			}
		}
		mv.addObject("imgServer", systemConfig.getImgServerDomain());

		return mv;
	}

	/**
	 * 保存
	 */
	@ResponseBody
	@RequestMapping("save")
	public JsonResponseMsg save(HttpServletRequest req, ActivityOverActivity info, String picSetNum, String moduleIds,
			String timerDateStr, String videoUrlUpload, String oldVideoUrl) {
		JsonResponseMsg result = new JsonResponseMsg();

		if (info.getIsPublished() == null) {
			info.setIsPublished(1);
			info.setIsShow(1);
		}
		if (info.getIsAd() == null) {
			info.setIsAd(0);
		}
		// 检查参数
		int picSetNumInt = NumberUtils.toInt(picSetNum, 0);
		if (info == null || InformationConstant.INFO_TYPE_PIC_SET.equals(info.getType()) && picSetNumInt <= 0) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}
		if (StringUtils.isNotBlank(timerDateStr)) {
			try {
				Date timerDate = DateUtils.stringToDateYyyyMMddhhmmss(timerDateStr);
				info.setTimerDate(timerDate);
			} catch (Exception e) {
				LOGGER.error("设置资讯时间异常:", e);
			}
		}

		// 保存资讯信息
		String systemName = "wy-web-admin";
		String src = ImgUploadUtil.genFilePath("information");
		MultipartFile iconFile = Servlets.getMultipartFile(req, "iconFile");
		if (iconFile != null) {// 有图片时上传文件
			Map<String, String> saveResult = ImgUploadUtil.save(iconFile, systemName, src);
			info.setIcon(saveResult.get(ImgUploadUtil.KEY_MAP_SRC));
		}
		MultipartFile coverFile = Servlets.getMultipartFile(req, "coverFile");
		if (coverFile != null) {// 有首图时上传首图
			Map<String, String> saveResult = ImgUploadUtil.save(coverFile, systemName, src);
			info.setCover(saveResult.get(ImgUploadUtil.KEY_MAP_SRC));
		} else {
			Long activityId = info.getActivityId();
			if (null == activityId) {
				activityId = NumberUtils.toLong(req.getParameter("auditionId"));
				String icon = queryDao.query("select icon from audition where id = " + activityId);
				info.setCover(icon);
			} else {
				Matches match = matchesService.findById(info.getActivityId());
				info.setCover(match.getIcon());
			}
		}

		if (StringUtils.contains(moduleIds, "36")) {
			info.setAudition(1);
		}

		if (info.getTitle() == null) {

			Long activityId = info.getActivityId();
			if (null == activityId) {
				activityId = NumberUtils.toLong(req.getParameter("auditionId"));
				String name = queryDao.query("select name from audition where id = " + activityId);
				info.setTitle(name);
				info.setActivityId(activityId);
			} else {
				Matches match = matchesService.findById(info.getActivityId());

				info.setTitle(match.getTitle());
			}

		}
		String taskId = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(videoUrlUpload) && !StringUtils.equals(oldVideoUrl, videoUrlUpload)) {
			info.setVideoUrl(UpYunUploaderUtils.changeExtension(videoUrlUpload, "m3u8"));

			// 自动分片
			taskId = UpYunUploaderUtils.hlsFile(videoUrlUpload);
		}
		boolean isNewSubjectInfo = false;
		if (info.getId() == null && info.getpId() != null && info.getpId() > 0) {
			isNewSubjectInfo = true;
		}

		// 组装视频封面图片
		String videoCoverImgs = getVideoCoverImgs(req, systemName, src);
		info.setVideoCoverImgs(videoCoverImgs);
		info = activityOverActivityService.saveOrUpdate(info);

		// 新增专题子资讯时,更新专题的生效时间
		if (isNewSubjectInfo) {
			ActivityOverActivity subject = activityOverActivityService.findById(info.getpId());
			if (subject != null) {
				subject.setTimerDate(new Date());
				activityOverActivityService.save(subject);
			}
		}

		// 记录分片处理的任务ID,以供查询进度用
		String redisKey = REDIS_KEY_HLS_TASK_ID + "_" + info.getId();
		if (taskId != null) {
			stringRedisOperateService.setData(redisKey, taskId);
		}

		// 保存资讯所属模块信息
		Long infoId = info.getId();
		if (StringUtils.isNotBlank(moduleIds)) {
			activityOverActivityModuleInfoService.resetModuleInfo(infoId, moduleIds);
		}
		if (info.getpId() != null) {
			List<ActivityOverActivityModuleInfo> parentActivityModuleInfo = activityOverActivityModuleInfoService
					.findByOverActivityId(info.getpId());
			if (CollectionUtils.isNotEmpty(parentActivityModuleInfo)) {
				String modules = "";
				for (ActivityOverActivityModuleInfo m : parentActivityModuleInfo) {
					modules = modules + m.getId() + ",";
				}
				modules = StringUtils.removeEnd(modules, ",");
				activityOverActivityModuleInfoService.resetModuleInfo(infoId, modules);
			}

		}

		// 产生图集信息
		if (InformationConstant.INFO_TYPE_PIC_SET.equals(info.getType())) {
			List<ActivityOverActivityImg> ais = Lists.newArrayList();
			for (int i = 1; i <= picSetNumInt; i++) {
				// 获取图集信息
				String remarkParamName = "remark" + i;
				String remark = req.getParameter(remarkParamName);
				String imgParamName = "imgFiles" + i;
				List<MultipartFile> imgFiles = Servlets.getMultipartFiles(req, imgParamName);
				if (CollectionUtils.isEmpty(imgFiles)) {
					String idsParamName = "ids" + i;
					String ids = req.getParameter(idsParamName);
					if (StringUtils.isNotBlank(ids)) {
						activityOverActivityImgService.updateRemarkByIds(remark, ids);
					}
					continue;
				}

				// 保存图片并实例化图集
				Date now = new Date();
				for (MultipartFile file : imgFiles) {
					Map<String, String> saveResult = ImgUploadUtil.save(file, systemName, src, true);// 保留文件名
					String url = saveResult.get(ImgUploadUtil.KEY_MAP_SRC);

					ActivityOverActivityImg ai = new ActivityOverActivityImg();
					ai.setActivityId(infoId);
					ai.setImg(url);
					ai.setRemark(remark);
					ai.setValid(CommonConstant.INT_BOOLEAN_TRUE);
					ai.setUpdateDate(now);
					ai.setCreateDate(now);
					ais.add(ai);
				}
			}

			// 保存
			if (CollectionUtils.isNotEmpty(ais)) {
				activityOverActivityImgService.save(ais);
			}
		}

		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	@Autowired
	private QueryDao queryDao;

	/**
	 * 获取视频封面图片相关参数,并组装为图片地址字符串
	 */
	private String getVideoCoverImgs(HttpServletRequest req, String fileSystemName, String fileSrc) {
		String result = StringUtils.EMPTY;
		if (req == null) {
			return result;
		}

		Joiner joiner = Joiner.on(",");
		String videoCoverImgsNumStr = req.getParameter("videoCoverImgsNum");
		int num = NumberUtils.toInt(videoCoverImgsNumStr, 0);
		for (int i = 1; i <= num; i++) {
			// 获取已存在的图片地址
			String url = req.getParameter("videoCoverUrl" + i);

			// 重新上传图片
			if (StringUtils.isBlank(url)) {
				MultipartFile file = Servlets.getMultipartFile(req, "videoCoverFile" + i);
				if (file != null) {
					Map<String, String> saveResult = ImgUploadUtil.save(file, fileSystemName, fileSrc);
					url = saveResult.get(ImgUploadUtil.KEY_MAP_SRC);
				}
			}

			// 拼接图片地址
			if (StringUtils.isNotBlank(url)) {
				if (i <= 1) {
					result = url;
				} else {
					result = joiner.join(result, url);
				}
			}
		}

		return result;
	}

	/**
	 * 移除图片
	 */
	@ResponseBody
	@RequestMapping("removeImg")
	public JsonResponseMsg removeImg(String ids) {
		JsonResponseMsg result = new JsonResponseMsg();
		activityOverActivityImgService.disabledByIds(ids);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("delete")
	public JsonResponseMsg delete(String id) {
		JsonResponseMsg result = new JsonResponseMsg();
		ActivityOverActivity info = null;
		if (NumberUtils.isNumber(id)) {
			long idLong = NumberUtils.toLong(id);
			info = activityOverActivityService.findById(idLong);

			// 专题资讯有子资讯时不可删除
			if (CommonConstant.INT_BOOLEAN_TRUE.equals(info.getIsSubject())) {
				List<Map<String, Object>> subInfoes = activityOverActivityService.queryValidByPid(idLong);
				if (CollectionUtils.isNotEmpty(subInfoes)) {
					return result.fill(CommonConstant.CODE_ERROR_LOGIC, "当前专题下存在子资讯,请删除所有子资讯后重试");
				}
			}

			info = activityOverActivityService.delete(info);
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, info);
	}

	/**
	 * 置顶
	 */
	@ResponseBody
	@RequestMapping("top")
	public JsonResponseMsg top(String id, String top, String listType) {
		JsonResponseMsg result = new JsonResponseMsg();
		ActivityOverActivity info = null;
		if (NumberUtils.isNumber(id)) {
			long idLong = NumberUtils.toLong(id);
			int topFlag = NumberUtils.toInt(top, 1);
			info = activityOverActivityService.topById(idLong, CommonConstant.INT_BOOLEAN_TRUE.equals(topFlag));
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, info);
	}

	/**
	 * 生效
	 */
	@ResponseBody
	@RequestMapping("show")
	public JsonResponseMsg show(String id, String show) {
		JsonResponseMsg result = new JsonResponseMsg();
		ActivityOverActivity info = null;
		if (NumberUtils.isNumber(id)) {
			long idLong = NumberUtils.toLong(id);
			int showFlag = NumberUtils.toInt(show, 1);
			info = activityOverActivityService.showById(idLong, CommonConstant.INT_BOOLEAN_TRUE.equals(showFlag));
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, info);
	}

	/**
	 * 设为模版
	 */
	@ResponseBody
	@RequestMapping("model")
	public JsonResponseMsg model(String id, String model) {
		JsonResponseMsg result = new JsonResponseMsg();
		ActivityOverActivity info = null;
		if (NumberUtils.isNumber(id)) {
			long idLong = NumberUtils.toLong(id);
			int modelFlag = NumberUtils.toInt(model, 1);
			activityOverActivityModelService.modelByInfoId(idLong, CommonConstant.INT_BOOLEAN_TRUE.equals(modelFlag));
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, info);
	}

}
