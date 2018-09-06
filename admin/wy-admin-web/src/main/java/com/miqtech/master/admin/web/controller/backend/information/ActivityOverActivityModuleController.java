package com.miqtech.master.admin.web.controller.backend.information;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.config.SystemConfig;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.activity.ActivityOverActivityModule;
import com.miqtech.master.service.activity.ActivityOverActivityModuleService;
import com.miqtech.master.thirdparty.util.img.ImgUploadUtil;
import com.miqtech.master.vo.PageVO;

@Controller
@RequestMapping("overActivity/module")
public class ActivityOverActivityModuleController extends BaseController {

	@Autowired
	private SystemConfig systemConfig;
	@Autowired
	private ActivityOverActivityModuleService activityOverActivityModuleService;

	/**
	 * 模块列表
	 */
	@RequestMapping("list/{page}")
	public ModelAndView list(@PathVariable("page") int page) {
		ModelAndView mv = new ModelAndView("information/moduleList");

		Map<String, String> params = Maps.newHashMap();
		PageVO vo = activityOverActivityModuleService.page(page, params);
		pageModels(mv, vo.getList(), page, vo.getTotal());

		int maxOrderNum = activityOverActivityModuleService.defaultOrderNum(0L);
		mv.addObject("maxOrderNum", maxOrderNum);
		mv.addObject("imgServer", systemConfig.getImgServerDomain());

		return mv;
	}

	/**
	 * 编辑
	 */
	@RequestMapping("edit")
	public ModelAndView edit(String id) {
		ModelAndView mv = new ModelAndView("information/moduleEdit");

		boolean isInsert = true;
		if (NumberUtils.isNumber(id)) {
			long idLong = NumberUtils.toLong(id);
			ActivityOverActivityModule module = activityOverActivityModuleService.findById(idLong);
			if (module != null) {
				mv.addObject("module", module);
				isInsert = false;
			}
		}
		mv.addObject("isInsert", isInsert);

		return mv;
	}

	/**
	 * 子模块列表
	 */
	@RequestMapping("children/list/{page}")
	public ModelAndView childrenList(@PathVariable("page") int page, String pid) {
		ModelAndView mv = new ModelAndView("information/moduleChildrenList");

		Map<String, String> params = Maps.newHashMap();
		params.put("pid", pid);
		PageVO vo = activityOverActivityModuleService.childrenPage(page, params);
		pageModels(mv, vo.getList(), page, vo.getTotal());

		long pidLong = NumberUtils.toLong(pid);
		int maxOrderNum = activityOverActivityModuleService.defaultOrderNum(pidLong);
		mv.addObject("maxOrderNum", maxOrderNum);
		mv.addObject("pid", pid);

		return mv;
	}

	/**
	 * 子模块编辑
	 */
	@RequestMapping("children/edit")
	public ModelAndView childrenEdit(String id, String pid) {
		ModelAndView mv = new ModelAndView("information/moduleChildrenEdit");

		boolean isInsert = true;
		if (NumberUtils.isNumber(id)) {
			long idLong = NumberUtils.toLong(id);
			ActivityOverActivityModule module = activityOverActivityModuleService.findById(idLong);
			if (module != null) {
				mv.addObject("module", module);
				isInsert = false;
				pid = module.getPid().toString();
			}
		}
		mv.addObject("isInsert", isInsert);
		mv.addObject("pid", pid);

		return mv;
	}

	/**
	 * 保存
	 */
	@ResponseBody
	@RequestMapping("save")
	public JsonResponseMsg save(HttpServletRequest req, ActivityOverActivityModule module) {
		JsonResponseMsg result = new JsonResponseMsg();

		// 保存图片
		String systemName = "wy-web-admin";
		String src = ImgUploadUtil.genFilePath("informationModule");
		MultipartFile imgFile = Servlets.getMultipartFile(req, "imgFile");
		if (imgFile != null) {// 有图片时上传文件
			Map<String, String> saveResult = ImgUploadUtil.save(imgFile, systemName, src);
			module.setImg(saveResult.get(ImgUploadUtil.KEY_MAP_SRC));
		}

		// 保存模块
		module = activityOverActivityModuleService.saveOrUpdate(module);

		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, module);
	}

	/**
	 * 更改排序序号
	 */
	@ResponseBody
	@RequestMapping("order")
	public JsonResponseMsg order(String moduleId, String orderNum) {
		JsonResponseMsg result = new JsonResponseMsg();

		if (!NumberUtils.isNumber(moduleId) || !NumberUtils.isNumber(orderNum)) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}

		long moduleIdLong = NumberUtils.toLong(moduleId);
		ActivityOverActivityModule module = activityOverActivityModuleService.findById(moduleIdLong);
		if (module == null) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, "找不到指定模块");
		}

		// 不能重复
		int orderNumInt = NumberUtils.toInt(orderNum);
		List<ActivityOverActivityModule> orderNumModules = activityOverActivityModuleService.findValidByPidAndOrderNum(
				module.getPid(), orderNumInt);
		if (CollectionUtils.isNotEmpty(orderNumModules)) {
			return result.fill(CommonConstant.CODE_ERROR_LOGIC, "排序序号不能重复");
		}

		activityOverActivityModuleService.changeOrderNum(moduleIdLong, orderNumInt);

		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("delete")
	public JsonResponseMsg delete(String moduleId) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (!NumberUtils.isNumber(moduleId)) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}
		long moduleIdLong = NumberUtils.toLong(moduleId);
		List<ActivityOverActivityModule> children = activityOverActivityModuleService.findValidByPid(moduleIdLong);
		if (CollectionUtils.isNotEmpty(children)) {
			return result.fill(CommonConstant.CODE_ERROR_LOGIC, "当前模块存在子级模块，不可以删除");
		}
		activityOverActivityModuleService.delete(moduleIdLong);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}
}
