package com.miqtech.master.admin.web.controller.backend.official;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.official.OfficialWebsiteConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.website.OfficialWebsiteDynamic;
import com.miqtech.master.service.common.ObjectRedisOperateService;
import com.miqtech.master.service.official.OfficialWebsiteDynamicService;
import com.miqtech.master.thirdparty.util.img.ImgUploadUtil;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

/**
 * 官网新闻动态
 */
@Controller
@RequestMapping("official/website/dynamic")
public class OfficialWebsiteDynamicController extends BaseController {
	private final static Joiner JOINER = Joiner.on("_");
	@Autowired
	private OfficialWebsiteDynamicService officialWebsiteDynamicService;
	@Autowired
	private ObjectRedisOperateService objectRedisOperateService;

	/*
	 * 列表
	 */
	@RequestMapping("/list/{page}")
	public ModelAndView commodityList(HttpServletRequest request, @PathVariable("page") int page, String title,
			String type) {
		ModelAndView mv = new ModelAndView("official/dynamicList");
		Map<String, Object> params = Maps.newHashMap();
		if (StringUtils.isNotBlank(type)) {
			params.put("type", NumberUtils.toInt(type, 0));
		}
		if (StringUtils.isNotBlank(title)) {
			params.put("title", title);
		}

		PageVO pageVO = officialWebsiteDynamicService.pageList(page, params);
		pageModels(mv, pageVO.getList(), page, pageVO.getTotal());
		mv.addObject("params", params);
		return mv;
	}

	/*
	 * 新增/更新
	 */
	@ResponseBody
	@RequestMapping("/save")
	public JsonResponseMsg save(HttpServletRequest req, OfficialWebsiteDynamic officialWebsiteDynamic, String url) {
		JsonResponseMsg result = new JsonResponseMsg();
		//上传图片
		MultipartFile iconFile = Servlets.getMultipartFile(req, "icon_file");
		if (null != iconFile) {
			if (null != officialWebsiteDynamic) {
				String systemName = "wy-web-admin";
				String src = ImgUploadUtil.genFilePath("official_website");
				Map<String, String> imgPath = ImgUploadUtil.save(iconFile, systemName, src);
				officialWebsiteDynamic.setIcon(imgPath.get(ImgUploadUtil.KEY_MAP_SRC));
			}
		}
		if (null != officialWebsiteDynamic.getType() && officialWebsiteDynamic.getType() == 0) {
			if (StringUtils.isNotBlank(url)) {
				officialWebsiteDynamic.setContent(url);
			} else {
				result.fill(CommonConstant.CODE_ERROR_PARAM, "请填写url");
			}
		}

		officialWebsiteDynamicService.save(officialWebsiteDynamic);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/*
	 * 请求对象信息
	 */
	@ResponseBody
	@RequestMapping("/info/{id}")
	public JsonResponseMsg detail(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		OfficialWebsiteDynamic officialWebsiteDynamic = officialWebsiteDynamicService.findById(id);

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, officialWebsiteDynamic);
		return result;
	}

	/*
	 * 删除/恢复
	 */
	@ResponseBody
	@RequestMapping("/validChange/{id}/{valid}")
	public JsonResponseMsg validChange(@PathVariable("id") long id, @PathVariable("valid") int valid) {
		JsonResponseMsg result = new JsonResponseMsg();
		officialWebsiteDynamicService.updateValidById(id, valid);

		OfficialWebsiteDynamic officialWebsiteDynamic = officialWebsiteDynamicService.findById(id);
		if (null != officialWebsiteDynamic) {
			int type = NumberUtils.toInt(officialWebsiteDynamic.getType().toString(), 0);
			if (type == 1 || type == 0) {
				operateRedis(OfficialWebsiteConstant.REDIS_OFFICIALWEBSITE_BANNER, id);
			} else if (type == 2) {
				operateRedis(OfficialWebsiteConstant.REDIS_OFFICIALWEBSITE_MATCH, id);
			} else if (type == 3) {
				operateRedis(OfficialWebsiteConstant.REDIS_OFFICIALWEBSITE_ACTIVITY, id);
			} else if (type == 4) {
				operateRedis(OfficialWebsiteConstant.REDIS_OFFICIALWEBSITE_PROFESSION, id);
			} else if (type == 5) {
				operateRedis(OfficialWebsiteConstant.REDIS_OFFICIALWEBSITE_INFORMATION, id);
			} else if (type == 6) {
				operateRedis(OfficialWebsiteConstant.REDIS_OFFICIALWEBSITE_BEFORE, id);
			}
		}

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	private void operateRedis(String redisKey, long id) {
		objectRedisOperateService.delData(redisKey);
		if (id > 0) {
			objectRedisOperateService.delData(JOINER.join(redisKey, "content", String.valueOf(id)));
		}
	}

}
