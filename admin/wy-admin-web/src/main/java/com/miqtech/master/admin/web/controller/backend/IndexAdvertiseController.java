package com.miqtech.master.admin.web.controller.backend;

import java.util.Date;
import java.util.List;
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
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.common.IndexAdvertise;
import com.miqtech.master.service.activity.ActivityInfoService;
import com.miqtech.master.service.game.GameInfoService;
import com.miqtech.master.service.index.IndexAdvertiseService;
import com.miqtech.master.service.netbar.NetbarInfoService;
import com.miqtech.master.service.system.SystemAreaService;
import com.miqtech.master.thirdparty.util.img.ImgUploadUtil;
import com.miqtech.master.utils.StringUtils;

@Controller
@RequestMapping("indexAdvertise/")
public class IndexAdvertiseController extends BaseController {
	@Autowired
	private IndexAdvertiseService indexAdvertiseService;
	@Autowired
	private NetbarInfoService netbarInfoService;
	@Autowired
	private GameInfoService gameInfoService;
	@Autowired
	private ActivityInfoService activityInfoService;
	@Autowired
	private SystemAreaService systemAreaService;

	/**
	 * 分页列表
	 */
	@RequestMapping("/list/{page}")
	public ModelAndView users(@PathVariable("page") int page, String title, String valid, String areaCode) {
		ModelAndView mv = new ModelAndView("activity/advertiseList"); //跳转到advertiseList.ftl页面

		Map<String, Object> params = Maps.newHashMap();
		if (StringUtils.isNotBlank(title)) {
			params.put("title", title);
		}
		if (StringUtils.isNotBlank(valid)) {
			params.put("valid", valid);
		} else {
			params.put("valid", 1);
		}
		if (StringUtils.isNotBlank(areaCode)) {
			params.put("areaCode", areaCode);
		}

		List<Map<String, Object>> list = indexAdvertiseService.pageList(page, params);
		pageModels(mv, list, page, Integer.parseInt(params.get("total").toString()));

		mv.addObject("netbarList", netbarInfoService.findAllNetbarIdandName());
		mv.addObject("gameList", gameInfoService.findAllGameIdandName());
		mv.addObject("activityList", activityInfoService.findAllActivityIdandTitle());
		mv.addObject("provinceList", systemAreaService.queryAllRoot());
		mv.addObject("params", params);
		return mv;
	}

	/**
	 * 请求详细信息
	 */
	@ResponseBody
	@RequestMapping("/detail/{id}")
	public JsonResponseMsg detail(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		IndexAdvertise advertise = indexAdvertiseService.findById(id);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, advertise);
		return result;
	}

	/**
	 * 新增或更新
	 */
	@ResponseBody
	@RequestMapping("/save")
	public JsonResponseMsg save(HttpServletRequest req, IndexAdvertise indexAdvertise, String targetIdNetbar,
			String targetIdGame, String targetIdActivity) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (indexAdvertise != null) {
			if (indexAdvertise.getId() != null) { //编辑
				IndexAdvertise indexAdvertiseUpdate = indexAdvertiseService.findById(indexAdvertise.getId());
				if (indexAdvertiseUpdate == null) {
					result.fill(CommonConstant.CODE_ERROR_LOGIC, "不存在的广告");
					return result;
				}
				if (StringUtils.isNotBlank(indexAdvertise.getTitle())) {
					indexAdvertiseUpdate.setTitle(indexAdvertise.getTitle());
				}
				if (StringUtils.isNotBlank(indexAdvertise.getDescribe())) {
					indexAdvertiseUpdate.setDescribe(indexAdvertise.getDescribe());
				}
				if (indexAdvertise.getType() != null) {
					indexAdvertiseUpdate.setType(indexAdvertise.getType());
					//类型：1-网吧;2-手游;3-赛事;4-网娱官方活动; 5-广告；
					if (1 == indexAdvertise.getType()) {//网吧
						indexAdvertiseUpdate.setTargetId(NumberUtils.toLong(targetIdNetbar));
						indexAdvertiseUpdate.setUrl("wangba/wbDetail.html?id=" + indexAdvertiseUpdate.getTargetId());
					} else if (2 == indexAdvertise.getType()) {//手游
						indexAdvertiseUpdate.setTargetId(NumberUtils.toLong(targetIdGame));
						indexAdvertiseUpdate.setUrl("shouyou/syDetail.html?id=" + indexAdvertiseUpdate.getTargetId());
					} else if (3 == indexAdvertise.getType()) {//赛事
						indexAdvertiseUpdate.setTargetId(NumberUtils.toLong(targetIdActivity));
						indexAdvertiseUpdate.setUrl("saishi/ssDetail.html?id=" + indexAdvertiseUpdate.getTargetId());
					} else {//外连接
						if (null != indexAdvertise.getUrl()) {
							indexAdvertiseUpdate.setUrl(indexAdvertise.getUrl());
						}
					}
				}
				if (null != indexAdvertise.getDeviceType()) {
					indexAdvertiseUpdate.setDeviceType(indexAdvertise.getDeviceType());
				}
				MultipartFile file = Servlets.getMultipartFile(req, "imgFile");
				if (file != null) {
					// 上传图片
					String systemName = "wy-web-admin";
					String src = ImgUploadUtil.genFilePath("indexAdvertise");
					if (file != null) { // 有图片时上传
						Map<String, String> imgPaths = ImgUploadUtil.save(file, systemName, src);
						indexAdvertiseUpdate.setImg(imgPaths.get(ImgUploadUtil.KEY_MAP_SRC));
						indexAdvertiseUpdate.setImgMedia(imgPaths.get(ImgUploadUtil.KEY_MAP_MEDIA));
						indexAdvertiseUpdate.setImgThumb(imgPaths.get(ImgUploadUtil.KEY_MAP_THUMB));
					}
				}
				indexAdvertiseUpdate.setUpdateDate(new Date());
				indexAdvertiseService.saveOrUpdate(indexAdvertiseUpdate);
				result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
			} else { //新增
				//检查参数
				MultipartFile file = Servlets.getMultipartFile(req, "imgFile");
				if (null == file) {
					result.fill(CommonConstant.CODE_ERROR_PARAM, "图片为必传");
					return result;
				}
				if (!StringUtils.isAllNotBlank(indexAdvertise.getTitle(), indexAdvertise.getDescribe(),
						indexAdvertise.getUrl()) || null == indexAdvertise.getType()
						|| null == indexAdvertise.getDeviceType()) {
					result.fill(CommonConstant.CODE_ERROR_PARAM, "红色*选项为必填");
					return result;
				}

				//检查完毕
				IndexAdvertise indexAdvertiseNew = new IndexAdvertise();
				indexAdvertiseNew.setTitle(indexAdvertise.getTitle());
				indexAdvertiseNew.setDescribe(indexAdvertise.getDescribe());
				indexAdvertiseNew.setType(indexAdvertise.getType());
				//类型：1-网吧;2-手游;3-赛事;4-网娱官方活动; 5-广告；
				if (1 == indexAdvertise.getType()) {//网吧
					indexAdvertiseNew.setTargetId(NumberUtils.toLong(targetIdNetbar));
					indexAdvertiseNew.setUrl("wangba/wbDetail.html?id=" + indexAdvertiseNew.getTargetId());
				} else if (2 == indexAdvertise.getType()) {//手游
					indexAdvertiseNew.setTargetId(NumberUtils.toLong(targetIdGame));
					indexAdvertiseNew.setUrl("shouyou/syDetail.html?id=" + indexAdvertiseNew.getTargetId());
				} else if (3 == indexAdvertise.getType()) {//赛事
					indexAdvertiseNew.setTargetId(NumberUtils.toLong(targetIdActivity));
					indexAdvertiseNew.setUrl("saishi/ssDetail.html?id=" + indexAdvertiseNew.getTargetId());
				} else {//外连接
					if (null != indexAdvertise.getUrl()) {
						indexAdvertiseNew.setUrl(indexAdvertise.getUrl());
					}
				}
				indexAdvertiseNew.setDeviceType(indexAdvertise.getDeviceType());
				indexAdvertiseNew.setValid(CommonConstant.INT_BOOLEAN_FALSE); //增加完成，默认不发布，需要手动发布
				indexAdvertiseNew.setCreateDate(new Date());
				// 上传图片
				String systemName = "wy-web-admin";
				String src = ImgUploadUtil.genFilePath("indexAdvertise");
				Map<String, String> imgPaths = ImgUploadUtil.save(file, systemName, src);
				indexAdvertiseNew.setImg(imgPaths.get(ImgUploadUtil.KEY_MAP_SRC));
				indexAdvertiseNew.setImgMedia(imgPaths.get(ImgUploadUtil.KEY_MAP_MEDIA));
				indexAdvertiseNew.setImgThumb(imgPaths.get(ImgUploadUtil.KEY_MAP_THUMB));

				indexAdvertiseService.saveOrUpdate(indexAdvertiseNew);
				result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
			}
		} else {
			result.fill(CommonConstant.CODE_ERROR_LOGIC, CommonConstant.MSG_ERROR_LOGIC);
		}

		return result;
	}

	/**
	 * 删除
	 */
	@ResponseBody
	@RequestMapping("/delete/{id}")
	public JsonResponseMsg delete(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		indexAdvertiseService.deleteById(id);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 恢复删除的广告
	 */
	@ResponseBody
	@RequestMapping("/recover/{id}")
	public JsonResponseMsg recover(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();
		indexAdvertiseService.recoverById(id);
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

}
