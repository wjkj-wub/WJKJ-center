package com.miqtech.master.admin.web.controller.backend.lottery;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.lottery.LotteryAwardSeat;
import com.miqtech.master.entity.lottery.LotteryOption;
import com.miqtech.master.service.lottery.LotteryAwardSeatService;
import com.miqtech.master.service.lottery.LotteryAwardService;

@Controller
@RequestMapping("lottery/awards")
public class LotteryAwardSeatsController extends BaseController {

	@Autowired
	private LotteryAwardService lotteryAwardService;
	@Autowired
	private LotteryAwardSeatService lotteryAwardSeatService;

	/**
	 * 查询活动的盘格设置
	 */
	@ResponseBody
	@RequestMapping("seats/{lotteryId}")
	public JsonResponseMsg awardSeats(@PathVariable("lotteryId") long lotteryId) {
		JsonResponseMsg result = new JsonResponseMsg();
		Map<String, Object> ro = Maps.newHashMap();
		List<Map<String, Object>> seats = lotteryAwardSeatService.findByLotteryId(lotteryId);
		List<Map<String, Object>> awards = lotteryAwardService.findValidByLotteryId(lotteryId);
		ro.put("seats", seats);
		ro.put("awards", awards);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS, ro);
	}

	/**
	 * 更新活动的盘格设置
	 */
	@ResponseBody
	@RequestMapping("update")
	public JsonResponseMsg udpate(LotteryOption option) {
		JsonResponseMsg result = new JsonResponseMsg();

		List<LotteryAwardSeat> awardSeats = null;
		if (option != null) {
			awardSeats = option.getAwardSeats();
		}

		if (CollectionUtils.isNotEmpty(awardSeats)) {
			for (LotteryAwardSeat s : awardSeats) {
				if (s == null || s.getLotteryId() == null || s.getSeat() == null) {// 缺少必要的设置项
					continue;
				}

				// 重新设置盘格时，删除活动的此盘格位置设置，避免重复设置
				if (s.getId() == null) {
					lotteryAwardSeatService.deleteOldSetting(s.getLotteryId(), s.getSeat());
				}

				lotteryAwardSeatService.save(s);
			}
		}

		return result;
	}
}
