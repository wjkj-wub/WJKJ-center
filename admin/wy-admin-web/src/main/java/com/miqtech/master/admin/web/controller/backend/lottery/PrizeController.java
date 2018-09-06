package com.miqtech.master.admin.web.controller.backend.lottery;

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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.lottery.LotteryPrize;
import com.miqtech.master.service.lottery.LotteryPrizeService;
import com.miqtech.master.thirdparty.util.img.ImgUploadUtil;
import com.miqtech.master.utils.StringUtils;

@Controller
@RequestMapping("prize/")
public class PrizeController extends BaseController {
	@Autowired
	private LotteryPrizeService lotteryPrizeService;

	/**
	 * 分页列表
	 */
	@RequestMapping("/list/{page}")
	public ModelAndView users(@PathVariable("page") int page, String valid) {
		ModelAndView mv = new ModelAndView("lottery/prizeList");
		Map<String, Object> params = Maps.newHashMap();
		List<LotteryPrize> list = Lists.newArrayList();
		if (StringUtils.isNotBlank(valid)) {
			if (valid.equals("1")) {
				params.put("valid", 1);
				list = lotteryPrizeService.getAllValid();
			} else {
				params.put("valid", 0);
				list = lotteryPrizeService.getAllInvalid();
			}
		} else {
			params.put("valid", 1);
			list = lotteryPrizeService.getAllValid();
		}

		pageModels(mv, list, page, list.size());
		mv.addObject("params", params);

		return mv;
	}

	/**
	 * 新增或更新
	 */
	@ResponseBody
	@RequestMapping("/save")
	public JsonResponseMsg save(HttpServletRequest req, LotteryPrize lotteryPrize) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (lotteryPrize.getId() != null) { //编辑
			LotteryPrize lotteryPrizeUpdate = lotteryPrizeService.getLotteryPrizeById(lotteryPrize.getId());
			if (lotteryPrizeUpdate != null) {
				if (StringUtils.isNotBlank(lotteryPrize.getName())) {
					lotteryPrizeUpdate.setName(lotteryPrize.getName());
				}
				if (StringUtils.isNotBlank(lotteryPrize.getPrice())) {
					lotteryPrizeUpdate.setPrice(lotteryPrize.getPrice());
				}
				MultipartFile file = Servlets.getMultipartFile(req, "iconFile");
				if (file != null) {
					String sysName = "wy-web-admin";
					String src = ImgUploadUtil.genFilePath("prize");
					Map<String, String> imgPaths = ImgUploadUtil.save(file, sysName, src);
					lotteryPrizeUpdate.setIcon(imgPaths.get(ImgUploadUtil.KEY_MAP_SRC));
				}
				lotteryPrizeUpdate.setUpdateDate(new Date());
				lotteryPrizeService.save(lotteryPrizeUpdate);
			}
		} else { //新增
			MultipartFile file = Servlets.getMultipartFile(req, "iconFile");
			if (file == null || !StringUtils.isAllNotBlank(lotteryPrize.getName(), lotteryPrize.getPrice())) { //检查参数
				result.fill(CommonConstant.CODE_ERROR_PARAM, "红色*为必填");
				return result;
			}
			LotteryPrize lotteryPrizeNew = new LotteryPrize();
			String sysName = "wy-web-admin";
			String src = ImgUploadUtil.genFilePath("prize");
			Map<String, String> imgPaths = ImgUploadUtil.save(file, sysName, src);
			lotteryPrizeNew.setIcon(imgPaths.get(ImgUploadUtil.KEY_MAP_SRC));
			lotteryPrizeNew.setName(lotteryPrize.getName());
			lotteryPrizeNew.setPrice(lotteryPrize.getPrice());
			lotteryPrizeNew.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			lotteryPrizeNew.setCreateDate(new Date());
			lotteryPrizeService.save(lotteryPrizeNew);
		}
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 删除/恢复
	 */
	@ResponseBody
	@RequestMapping("/deleteOrRecover/{id}/{valid}")
	public JsonResponseMsg deleteOrRecover(@PathVariable("id") long id, @PathVariable("valid") String valid) {
		JsonResponseMsg result = new JsonResponseMsg();
		lotteryPrizeService.updateValid(id, NumberUtils.toInt(valid));
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}
}
