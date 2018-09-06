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

import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.official.OfficialWebsiteConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.website.OfficialWebsiteLink;
import com.miqtech.master.service.common.ObjectRedisOperateService;
import com.miqtech.master.service.official.OfficialWebsiteLinkService;
import com.miqtech.master.thirdparty.util.img.ImgUploadUtil;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

/**
 * 官网连接（视频、toy小游戏）
 */
@Controller
@RequestMapping("official/website/link")
public class OfficialWebsiteLinkController extends BaseController {
	@Autowired
	private OfficialWebsiteLinkService officialWebsiteLinkService;
	@Autowired
	private ObjectRedisOperateService objectRedisOperateService;

	/*
	 * 列表
	 */
	@RequestMapping("/list/{page}")
	public ModelAndView commodityList(HttpServletRequest request, @PathVariable("page") int page, String title,
			String type) {
		ModelAndView mv = new ModelAndView("official/linkList");
		Map<String, Object> params = Maps.newHashMap();
		if (StringUtils.isNotBlank(type)) {
			params.put("type", NumberUtils.toInt(type, 0));
		}
		if (StringUtils.isNotBlank(title)) {
			params.put("title", title);
		}

		PageVO pageVO = officialWebsiteLinkService.pageList(page, params);
		pageModels(mv, pageVO.getList(), page, pageVO.getTotal());
		mv.addObject("params", params);
		return mv;
	}

	/*
	 * 新增/更新
	 */
	@ResponseBody
	@RequestMapping("/save")
	public JsonResponseMsg save(HttpServletRequest req, OfficialWebsiteLink officialWebsiteLink) {
		JsonResponseMsg result = new JsonResponseMsg();
		//上传图片
		MultipartFile iconFile = Servlets.getMultipartFile(req, "icon_file");
		if (null != iconFile) {
			if (null != officialWebsiteLink) {
				String systemName = "wy-web-admin";
				String src = ImgUploadUtil.genFilePath("official_website");
				Map<String, String> imgPath = ImgUploadUtil.save(iconFile, systemName, src);
				officialWebsiteLink.setIcon(imgPath.get(ImgUploadUtil.KEY_MAP_SRC));
			}
		}

		officialWebsiteLinkService.save(officialWebsiteLink);
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
		OfficialWebsiteLink officialWebsiteLink = officialWebsiteLinkService.findById(id);

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, officialWebsiteLink);
		return result;
	}

	/*
	 * 删除/恢复
	 */
	@ResponseBody
	@RequestMapping("/validChange/{id}/{valid}")
	public JsonResponseMsg validChange(@PathVariable("id") long id, @PathVariable("valid") int valid) {
		JsonResponseMsg result = new JsonResponseMsg();
		officialWebsiteLinkService.updateValidById(id, valid);

		OfficialWebsiteLink officialWebsiteLink = officialWebsiteLinkService.findById(id);
		if (null != officialWebsiteLink) {
			int type = NumberUtils.toInt(officialWebsiteLink.getType().toString(), 0);
			if (type == 1) {
				operateRedis(OfficialWebsiteConstant.REDIS_OFFICIALWEBSITE_VIDEO);
			} else if (type == 2) {
				operateRedis(OfficialWebsiteConstant.REDIS_OFFICIALWEBSITE_TOY);
			}
		}

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	private void operateRedis(String redisKey) {
		objectRedisOperateService.delData(redisKey);
	}

}
