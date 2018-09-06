package com.miqtech.master.admin.web.controller.backend;

import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.activity.ActivityItem;
import com.miqtech.master.service.activity.ActivityItemService;
import com.miqtech.master.thirdparty.util.img.ImgUploadUtil;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.StringUtils;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("activityIteam/")
public class ActivityItemController extends BaseController {
	@Autowired
	private ActivityItemService activityItemService;

	/**
	 * 分页列表
	 */
	@RequestMapping("/list/{page}")
	public ModelAndView users(@PathVariable("page") int page, String name) {
		ModelAndView mv = new ModelAndView("activity/itemList"); //跳转到itemList.ftl页面

		Map<String, Object> params = new HashMap<String, Object>();
		if (StringUtils.isNotBlank(name)) {
			params.put("name", name);
		}

		PageVO pageVO = activityItemService.getItemList(page, PageUtils.ADMIN_DEFAULT_PAGE_SIZE, params);

		mv.addObject("list", pageVO.getList());
		mv.addObject("currentPage", page);
		mv.addObject("isLastPage", PageUtils.isBottom(page, pageVO.getTotal()));//0已到最后一页 1可以加载下一页
		mv.addObject("totalCount", pageVO.getTotal());
		mv.addObject("totalPage", PageUtils.calcTotalPage(pageVO.getTotal()));
		mv.addObject("params", params);
		return mv;
	}

	/**
	 * 请求详细信息
	 */
	@ResponseBody
	@RequestMapping("/detail/{itemId}")
	public JsonResponseMsg detail(@PathVariable("itemId") long itemId) {
		JsonResponseMsg result = new JsonResponseMsg();
		ActivityItem activityItem = activityItemService.findById(itemId);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, activityItem);
		return result;
	}

	/**
	 * 新增或更新
	 */
	@ResponseBody
	@RequestMapping("/save")
	public JsonResponseMsg save(HttpServletRequest req, String id, String name, String serverRequired) {
		JsonResponseMsg result = new JsonResponseMsg();
		ActivityItem activityItem = null;
		if (StringUtils.isNotBlank(id)) { //编辑
			activityItem = activityItemService.findById(NumberUtils.toLong(id));
			if (activityItem == null) {
				result.fill(CommonConstant.CODE_ERROR_LOGIC, "不存在的赛事项目");
				return result;
			}
			if (StringUtils.isNotBlank(name)) {
				activityItem.setName(name);
				activityItem.setUpdateDate(new Date());
			}
			if (StringUtils.isNotBlank(serverRequired)) {
				activityItem.setServerRequired(NumberUtils.toInt(serverRequired));
			}
			// Icon
			String systemName = "wy-web-admin";
			String src = ImgUploadUtil.genFilePath("activity");
			MultipartFile file = Servlets.getMultipartFile(req, "iconFile");
			if (file != null) { // 有图片时上传
				Map<String, String> imgPaths = ImgUploadUtil.save(file, systemName, src);
				activityItem.setIcon(imgPaths.get(ImgUploadUtil.KEY_MAP_SRC));
			}
			// 图片
			file = Servlets.getMultipartFile(req, "picFile");
			if (file != null) { // 有图片时上传文件
				Map<String, String> imgPaths = ImgUploadUtil.save(file, systemName, src);
				activityItem.setPic(imgPaths.get(ImgUploadUtil.KEY_MAP_SRC));
				activityItem.setPicMedia(imgPaths.get(ImgUploadUtil.KEY_MAP_MEDIA));
				activityItem.setPicThumb(imgPaths.get(ImgUploadUtil.KEY_MAP_THUMB));
			}

			activityItemService.saveOrUpdate(activityItem);

			result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
			return result;
		} else { //新增
			//检查参数
			if (StringUtils.isBlank(name)) {
				result.fill(CommonConstant.CODE_ERROR_PARAM, "项目名称必填");
			}
			if (StringUtils.isBlank(serverRequired)) {
				result.fill(CommonConstant.CODE_ERROR_PARAM, "请选择是否需要服务器");
			}

			activityItem = new ActivityItem();

			activityItem.setName(name);
			activityItem.setServerRequired(NumberUtils.toInt(serverRequired)); //是否需要服务器
			activityItem.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			activityItem.setCreateDate(new Date());
			// 上传icon
			String systemName = "wy-web-admin";
			String src = ImgUploadUtil.genFilePath("activity");
			MultipartFile file = Servlets.getMultipartFile(req, "iconFile");
			if (file != null) { // 有图片时上传
				Map<String, String> imgPaths = ImgUploadUtil.save(file, systemName, src);
				activityItem.setIcon(imgPaths.get(ImgUploadUtil.KEY_MAP_SRC));
			}
			// 上传图片
			file = Servlets.getMultipartFile(req, "picFile");
			if (file != null) { // 有图片时上传文件
				Map<String, String> imgPaths = ImgUploadUtil.save(file, systemName, src);
				activityItem.setPic(imgPaths.get(ImgUploadUtil.KEY_MAP_SRC));
				activityItem.setPicMedia(imgPaths.get(ImgUploadUtil.KEY_MAP_MEDIA));
				activityItem.setPicThumb(imgPaths.get(ImgUploadUtil.KEY_MAP_THUMB));
			}

			activityItemService.saveOrUpdate(activityItem);
		}

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("/delete/{itemId}")
	public JsonResponseMsg delete(@PathVariable("itemId") long itemId) {
		JsonResponseMsg result = new JsonResponseMsg();
		activityItemService.deleteById(itemId);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}
}
