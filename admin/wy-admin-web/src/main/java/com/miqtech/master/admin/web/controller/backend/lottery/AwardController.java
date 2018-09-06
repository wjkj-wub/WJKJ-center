package com.miqtech.master.admin.web.controller.backend.lottery;

import java.util.Date;
import java.util.HashMap;
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

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.LotteryConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.lottery.LotteryAward;
import com.miqtech.master.entity.lottery.LotteryPrize;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.service.lottery.LotteryAwardService;
import com.miqtech.master.service.lottery.LotteryOptionService;
import com.miqtech.master.service.lottery.LotteryPrizeService;
import com.miqtech.master.thirdparty.util.img.ImgUploadUtil;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

@Controller
@RequestMapping("award/")
public class AwardController extends BaseController {

	@Autowired
	private StringRedisOperateService stringRedisOperateService;
	@Autowired
	private LotteryAwardService lotteryAwardService;
	@Autowired
	private LotteryOptionService lotteryOptionService;
	@Autowired
	private LotteryPrizeService lotteryPrizeService;

	/**
	 * 分页列表
	 */
	@RequestMapping("/list/{page}")
	public ModelAndView users(@PathVariable("page") int page, String valid, String lotteryId) {
		ModelAndView mv = new ModelAndView("lottery/awardList");
		Map<String, Object> params = Maps.newHashMap();
		if (StringUtils.isNotBlank(valid)) {
			params.put("valid", valid);
		}
		if (StringUtils.isNotBlank(lotteryId)) {
			params.put("lotteryId", lotteryId);
		}
		PageVO pageVO = lotteryAwardService.page(page, params);
		pageModels(mv, pageVO.getList(), page, pageVO.getTotal());
		mv.addObject("params", params);
		mv.addObject("lotteryList", lotteryOptionService.getAllValid());
		mv.addObject("prizeList", lotteryPrizeService.getAllValid());
		mv.addObject("loggeryId", lotteryId);

		return mv;
	}

	/**
	 * 查询奖项详情
	 */
	@ResponseBody
	@RequestMapping("detail/{id}")
	public JsonResponseMsg detail(@PathVariable("id") long id) {
		JsonResponseMsg result = new JsonResponseMsg();

		HashMap<Object, Object> ro = Maps.newHashMap();

		LotteryAward award = lotteryAwardService.findValidById(id);

		Joiner joiner = Joiner.on("_");
		String surplusKey = joiner.join(LotteryConstant.REDIS_SURPLUS_LOTTERY_AWARD, award.getLotteryId().toString(),
				award.getId().toString());
		String surplus = stringRedisOperateService.getData(surplusKey);

		ro.put("award", award);
		ro.put("surplus", surplus);

		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, ro);
	}

	/**
	 * 新增或更新
	 */
	@ResponseBody
	@RequestMapping("/save")
	public JsonResponseMsg save(HttpServletRequest req, LotteryAward lotteryAward) {
		JsonResponseMsg result = new JsonResponseMsg();

		// 检查奖项名是否已存在
		if (lotteryAward != null && StringUtils.isNotBlank(lotteryAward.getName())
				&& lotteryAward.getLotteryId() != null) {
			List<LotteryAward> awards = lotteryAwardService.findByLotteryIdAndName(lotteryAward.getLotteryId(),
					lotteryAward.getName());
			for (LotteryAward a : awards) {
				Long lotteryId = lotteryAward.getId();
				if (lotteryId == null || a.getId() != lotteryId) {
					return result.fill(CommonConstant.CODE_LOGIN_INVALID, "已存在同名奖项");
				}
			}
		}

		// 新增奖品
		if (lotteryAward.getPrizeId() == null) {
			String prizeName = req.getParameter("prizeName");
			String prizePrice = req.getParameter("prizePrice");
			MultipartFile prizeIcon = Servlets.getMultipartFile(req, "prizeIconFile");
			if (StringUtils.isNotBlank(prizeName) || StringUtils.isNotBlank(prizePrice) || prizeIcon != null) {
				LotteryPrize p = new LotteryPrize();
				if (StringUtils.isNotBlank(prizeName)) {
					p.setName(prizeName);
				}
				if (StringUtils.isNotBlank(prizePrice)) {
					p.setPrice(prizePrice);
				}
				if (prizeIcon != null) {
					Map<String, String> imgUrls = ImgUploadUtil.save(prizeIcon, "wy_web_admin",
							ImgUploadUtil.genFilePath("lottery"));
					p.setIcon(imgUrls.get(ImgUploadUtil.KEY_MAP_SRC));
				}
				p = lotteryPrizeService.save(p);
				lotteryAward.setPrizeId(p.getId());
			}
		}

		if (lotteryAward.getId() != null) { //编辑
			LotteryAward old = lotteryAwardService.getLotteryWardById(lotteryAward.getId());

			if (old.getRealInventory() != lotteryAward.getRealInventory()) {
				// 检查余量不能小于已用量
				Joiner joiner = Joiner.on("_");
				String surplusKey = joiner.join(LotteryConstant.REDIS_SURPLUS_LOTTERY_AWARD, old.getLotteryId()
						.toString(), old.getId().toString());
				String surplus = stringRedisOperateService.getData(surplusKey);
				Integer surplusInt = NumberUtils.toInt(surplus);
				Integer oldInventory = old.getRealInventory();
				Integer newInventory = lotteryAward.getRealInventory();
				Integer redisResult = null;
				if (oldInventory == -1) {
					redisResult = newInventory;
				} else {
					if (newInventory == -1) {
						redisResult = -1;
					} else {
						Integer reduce = oldInventory - newInventory;
						if (reduce > surplusInt) {
							return result.fill(CommonConstant.CODE_ERROR_LOGIC,
									"数量减少过多(已经使用:used)".replaceAll(":used", String.valueOf(oldInventory - surplusInt)));
						}
						redisResult = surplusInt - reduce;
					}
				}

				stringRedisOperateService.setData(surplusKey, redisResult.toString());
			}
			// 保存编辑
			lotteryAward = lotteryAwardService.save(lotteryAward);
		} else { //新增
			if (StringUtils.isBlank(lotteryAward.getName()) || lotteryAward.getInventory() == null
					|| lotteryAward.getRealInventory() == null || lotteryAward.getProbablity() == null
					|| lotteryAward.getVirtualWinners() == null) { //检查参数
				result.fill(CommonConstant.CODE_ERROR_PARAM, "红色*为必填");
				return result;
			}

			LotteryAward lotteryAwardNew = new LotteryAward();
			lotteryAwardNew.setLotteryId(lotteryAward.getLotteryId());
			lotteryAwardNew.setPrizeId(lotteryAward.getPrizeId());
			lotteryAwardNew.setName(lotteryAward.getName());
			lotteryAwardNew.setInventory(lotteryAward.getInventory());
			lotteryAwardNew.setRealInventory(lotteryAward.getRealInventory());
			lotteryAwardNew.setProbablity(lotteryAward.getProbablity());
			lotteryAwardNew.setVirtualWinners(lotteryAward.getVirtualWinners());
			lotteryAwardNew.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			lotteryAwardNew.setCreateDate(new Date());
			lotteryAwardNew = lotteryAwardService.save(lotteryAwardNew);

			// 初始化奖项redis余量
			Joiner joiner = Joiner.on("_");
			String surplusKey = joiner.join(LotteryConstant.REDIS_SURPLUS_LOTTERY_AWARD, lotteryAwardNew.getLotteryId()
					.toString(), lotteryAwardNew.getId().toString());
			stringRedisOperateService.setData(surplusKey, lotteryAwardNew.getRealInventory().toString());
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
		lotteryAwardService.updateValid(id, NumberUtils.toInt(valid));
		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}
}
