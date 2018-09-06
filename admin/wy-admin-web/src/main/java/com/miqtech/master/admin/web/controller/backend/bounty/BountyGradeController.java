package com.miqtech.master.admin.web.controller.backend.bounty;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Lists;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.Msg4UserType;
import com.miqtech.master.consts.MsgConstant;
import com.miqtech.master.consts.mall.CoinConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.bounty.Bounty;
import com.miqtech.master.entity.bounty.BountyGrade;
import com.miqtech.master.entity.bounty.BountyPrize;
import com.miqtech.master.service.bounty.BountyGradeService;
import com.miqtech.master.service.bounty.BountyPrizeService;
import com.miqtech.master.service.bounty.BountyService;
import com.miqtech.master.service.mall.CoinHistoryService;
import com.miqtech.master.thirdparty.service.JpushService;
import com.miqtech.master.thirdparty.util.JPushUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

@Controller
@RequestMapping("bounty")
public class BountyGradeController extends BaseController {
	@Autowired
	private BountyGradeService bountyGradeService;
	@Autowired
	private BountyPrizeService bountyPrizeService;
	@Autowired
	private BountyService bountyService;
	@Autowired
	private JpushService msgOperateService;
	@Autowired
	private CoinHistoryService coinHistoryService;

	/**
	 * 悬赏令成绩分页
	 * @throws ParseException
	 */
	@ResponseBody
	@RequestMapping("prizeCheck/list/{bountyId}/{page}")
	public ModelAndView list(@PathVariable("page") int page, @PathVariable("bountyId") String bountyId,
			String submitDateStart, String submitDateEnd, String telephone, Integer isMarked) throws ParseException {
		ModelAndView mv = new ModelAndView("bounty/gradeCheckList");
		int rows = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		PageVO vo = bountyGradeService.getItems(bountyId, submitDateStart, submitDateEnd, telephone, page, rows,
				isMarked);
		Map<String, Object> bountyInfo = bountyService.getBountyInfo(NumberUtils.toLong(bountyId));
		pageModels(mv, vo.getList(), page, vo.getTotal());
		mv.addObject("info", bountyInfo);
		mv.addObject("telephone", telephone);
		mv.addObject("submitDateStart", submitDateStart);
		mv.addObject("submitDateEnd", submitDateEnd);
		mv.addObject("isMarked", isMarked);
		mv.addObject("bountyId", bountyId);
		return mv;
	}

	/**
	 * 保存标记成绩
	 * grade为1则说明成绩审核通过 0为审核不通过
	 */
	@ResponseBody
	@RequestMapping("saveGrade")
	public JsonResponseMsg saveGrade(Long id, Integer grade, Long userId) {
		JsonResponseMsg result = new JsonResponseMsg();
		Integer gradeNull = Optional.ofNullable(grade).orElse(0);
		if (gradeNull < 0) {
			return result.fill(CommonConstant.CODE_ERROR_LOGIC, CommonConstant.MSG_ERROR_LOGIC);
		}
		BountyGrade bountyGrade = bountyGradeService.findById(id);
		Bounty bounty = bountyService.findById(bountyGrade.getBountyId());
		if (bounty.getStatus() == 1) {
			if (bounty.getStatus() == 1) {
				return result.fill(CommonConstant.CODE_SUCCESS, "该悬赏令已经审核结束!不能重复审核。");
			}
		}
		if (bountyGrade.getUserId().intValue() != userId.intValue()) {
			return result.fill(CommonConstant.CODE_ERROR_LOGIC, CommonConstant.MSG_ERROR_LOGIC);
		}
		if (gradeNull <= 0) {
			bountyGrade.setGrade(0);
			bountyGrade.setState(2);
		} else {
			bountyGrade.setGrade(gradeNull);
			bountyGrade.setState(3);
		}
		bountyGradeService.saveOrUpdate(bountyGrade);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 * 审核结束判断
	 * @throws ParseException
	 */
	@ResponseBody
	@RequestMapping("bountyFinish")
	public JsonResponseMsg bountyFinish(Integer visualNum, String id) throws ParseException {
		JsonResponseMsg result = new JsonResponseMsg();
		Long bountyId = NumberUtils.toLong(id);
		Bounty bounty = bountyService.findById(bountyId);
		bounty.setPrizeVirtualNum(visualNum == null ? 0 : visualNum);
		bounty = bountyService.save(bounty);
		//将悬赏令活动标记为审核结束
		if (bounty.getStatus() == 1) {
			return result.fill(CommonConstant.CODE_SUCCESS, "该悬赏令已经审核结束!不能重复审核。");
		}
		bounty.setStatus(1);
		bountyService.save(bounty);
		List<Map<String, Object>> allList = bountyGradeService.getAllBountyGradeByBountyId(bountyId, 1);
		List<BountyGrade> bountyGradeList = Lists.newArrayList();
		if (CollectionUtils.isNotEmpty(allList)) {
			for (int i = 0; i < allList.size(); i++) {
				BountyGrade bountyGrade = bountyGradeService.findById(MapUtils.getLong(allList.get(i), "id"));
				bountyGrade.setState(2);
				bountyGradeList.add(bountyGrade);
			}
			bountyGradeService.save(bountyGradeList);
			//向用户推送消息活动已结束
			String content = "悬赏令活动已结束，结果已判定";
			for (int i = 0; i < allList.size(); i++) {
				if (allList.get(i).get("user_id") != null) {
					String environment = systemConfig.getEnvironment();
					if (StringUtils.equals(environment, "test") || StringUtils.equals(environment, "dev")) {
						JPushUtils.setOnlie(false);
					}
					msgOperateService.notifyMemberAliasMsg(Msg4UserType.BOUNTY.ordinal(),
							NumberUtils.toLong(allList.get(i).get("user_id").toString()),
							MsgConstant.PUSH_MSG_TYPE_BOUNTY, "悬赏令消息", content, true, bountyId);
				}
			}
			List<Map<String, Object>> gradeWin = bountyGradeService.getWinUser(bountyId);
			if (bountyPrizePub(gradeWin, bountyPrizeService.findValidByBountyId(bountyId)) == 1) {
				return result.fill(CommonConstant.CODE_SUCCESS, "获奖人数为0,奖金未发放");
			}
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	//直接进行发放
	private int bountyPrizePub(List<Map<String, Object>> isGetLists, BountyPrize bountyPrize) {
		Integer visualNum = bountyService.findById(bountyPrize.getBountyId()).getPrizeVirtualNum();
		Integer winNumVisual = isGetLists.size() + visualNum;
		if (winNumVisual == 0) {
			return 1;
		}
		double awardNum = Math.ceil(NumberUtils.toDouble(bountyPrize.getAwardNum().toString()) / winNumVisual);
		for (Map<String, Object> isGetList : isGetLists) {
			coinHistoryService.addGoldHistoryPub(NumberUtils.toLong(isGetList.get("user_id").toString()), 0L,
					CoinConstant.HISTORY_TYPE_AWARD.intValue(), new Double(awardNum).intValue(),
					CoinConstant.HISTORY_DIRECTION_INCOME.intValue());
		}
		return 0;
	}

}
