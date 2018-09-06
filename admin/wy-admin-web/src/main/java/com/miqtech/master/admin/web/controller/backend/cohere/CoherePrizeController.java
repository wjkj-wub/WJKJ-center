package com.miqtech.master.admin.web.controller.backend.cohere;

import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.config.SystemConfig;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.user.UserInfoDao;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.boon.BoonCdkey;
import com.miqtech.master.entity.cohere.CohereActivity;
import com.miqtech.master.entity.cohere.CoherePrize;
import com.miqtech.master.entity.cohere.CoherePrizeHistory;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.service.boon.BoonCdkeyService;
import com.miqtech.master.service.cohere.CohereActivityService;
import com.miqtech.master.service.cohere.CoherePrizeHistoryService;
import com.miqtech.master.service.cohere.CoherePrizeService;
import com.miqtech.master.utils.ExcelUtils;
import com.miqtech.master.vo.PageVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抢皮肤发奖管理
 * @author renchen
 *
 */
@Controller
@RequestMapping("cohere/activity")
public class CoherePrizeController extends BaseController {

	@Autowired
	private CoherePrizeService coherePrizeService;
	@Autowired
	private CoherePrizeHistoryService coherePrizeHistoryService;
	@Autowired
	private CohereActivityService cohereActivityService;
	@Autowired
	private BoonCdkeyService boonCdkeyService;
	@Autowired
	private UserInfoDao userInfoDao;
	@Autowired
	private SystemConfig systemConfig;

	/** 发奖管理首页
	 * @return
	 */
	@RequestMapping("/{page}")
	public ModelAndView index(HttpServletRequest req, @PathVariable("page") Integer page, Long activityId) {
		ModelAndView mv = new ModelAndView();
		CohereActivity cohereActivity = cohereActivityService.getCohereActivitybyId(activityId);
		Map<String, Object> params = new HashMap<String, Object>(32);
		params.put("activityTitle", cohereActivity.getTitle());
		params.put("activityId", cohereActivity.getId());
		params.put("activityBeginTime", cohereActivity.getBeginTime());
		params.put("activityEndTime", cohereActivity.getEndTime());
		//搜索
		params.put("prizeType",
				StringUtils.isBlank(req.getParameter("prizeType")) ? "" : req.getParameter("prizeType"));
		params.put("prizeState",
				StringUtils.isBlank(req.getParameter("prizeState")) ? "" : req.getParameter("prizeState"));
		params.put("searchUser",
				StringUtils.isBlank(req.getParameter("searchUser")) ? "" : req.getParameter("searchUser").trim());
		params.put("startTime",
				StringUtils.isBlank(req.getParameter("startTime")) ? "" : req.getParameter("startTime"));
		params.put("endTime", StringUtils.isBlank(req.getParameter("endTime")) ? "" : req.getParameter("endTime"));

		params.put("page", page);
		params.put("activityId", activityId);
		PageVO vo = coherePrizeService.searchInfo(params);
		pageModels(mv, vo.getList(), page, vo.getTotal());
		mv.setViewName("/cohere/cohereActivityPrize");
		mv.addObject("params", params);
		return mv;
	}

	/**
	 * 奖品发放疑问
	 */
	@RequestMapping("question")
	@ResponseBody
	public JsonResponseMsg question(Long prizeHistoryId) {
		JsonResponseMsg result = new JsonResponseMsg();
		CoherePrizeHistory coherePrizeHistory = coherePrizeHistoryService.findOne(prizeHistoryId);
		if (coherePrizeHistory.getQuestion() == null || coherePrizeHistory.getQuestion().intValue() == 0) {
			coherePrizeHistory.setQuestion(1);
		} else {
			coherePrizeHistory.setQuestion(0);
		}
		coherePrizeHistoryService.saveOrUpdate(coherePrizeHistory);
		return result.fill(0, "质疑成功");
	}

	/** 发放奖品
	 * @param prizeHistoryId
	 * @param type 0单个发奖 1批量发奖
	 * @return
	 */
	@RequestMapping("sendPrize")
	@ResponseBody
	public JsonResponseMsg sendPrize(HttpServletRequest req, Integer type, String prizeHistoryIds, String activityId) {
		JsonResponseMsg result = new JsonResponseMsg();
		Map<String, Object> params = new HashMap<String, Object>(16);
		//搜索
		params.put("prizeType",
				StringUtils.isBlank(req.getParameter("prizeType")) ? "" : req.getParameter("prizeType"));
		params.put("prizeState",
				StringUtils.isBlank(req.getParameter("prizeState")) ? "" : req.getParameter("prizeState"));
		params.put("searchUser",
				StringUtils.isBlank(req.getParameter("searchUser")) ? "" : req.getParameter("searchUser").trim());
		params.put("startTime",
				StringUtils.isBlank(req.getParameter("startTime")) ? "" : req.getParameter("startTime"));
		params.put("endTime", StringUtils.isBlank(req.getParameter("endTime")) ? "" : req.getParameter("endTime"));
		List<Map<String, Object>> infos = coherePrizeHistoryService.getPrizeInfo(params, activityId, prizeHistoryIds,
				type);
		CoherePrizeHistory coherePrizeHistory = null;
		CoherePrize coherePrize = null;
		if (infos != null) {
			if (infos.size() == 1) {
				for (Map<String, Object> info : infos) {
					long id = NumberUtils.toLong(info.get("id").toString());
					if (coherePrizeHistoryService.canSendPrize(id)) {
						coherePrizeHistoryService.setSended(id);
						coherePrizeHistory = coherePrizeHistoryService.findOne(id);
						coherePrize = coherePrizeService.findById(NumberUtils.toLong(info.get("prizeId").toString()));
						int i = sendPrize(result, coherePrizeHistory, coherePrize);
						if (i == -1) {
							return result.fill(-1, "发放失败");
						} else if (i == -2) {
							return result.fill(-2, "没有奖励或者已经领奖");
						} else if (i == -3) {
							return result.fill(-3, "cdk不足");
						} else if (i == -4) {
							return result.fill(-4, "第三方发放系统(聚合平台)余额不足.请联系运营同事充值");
						}
					} else {
						return result.fill(-5, "一笔订单只能发放一次");
					}
				}
			} else {
				if (type == 1) {
					return result.fill(1, "不可发放");
				}
				return result.fill(1, "无可发放的奖品记录");
			}
		} else {
			return result.fill(1, "无可发放的奖品记录");
		}
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	private int sendPrize(JsonResponseMsg result, CoherePrizeHistory coherePrizeHistory, CoherePrize coherePrize) {
		if (coherePrize.getType().toString().equals("2")) { //Q币
			if (!coherePrizeHistory.getState().toString().equals("3")) {//未发放
				coherePrizeHistory.setTranNo(coherePrizeHistoryService.genSerial(1));
				int mid = 0;
				if (!systemConfig.getEnvironment().equals("online")) {
					mid = coherePrizeHistoryService.chargeQB(coherePrizeHistory, 0);
				} else {
					mid = coherePrizeHistoryService.chargeQB(coherePrizeHistory, coherePrize.getValue().intValue());
				}
				if (mid == 0) { //充值成功
					coherePrizeHistory.setState(3); //发放成功
					UserInfo info = userInfoDao.findOne(coherePrizeHistory.getUserId());
					if (coherePrize.getValue().toString().equals("10")) {
						String[] phoneNum = { info.getTelephone() };
						String[] params = { info.getUsername(), coherePrizeHistory.getAccount(),
								coherePrizeHistory.getAccount() };
						coherePrizeHistoryService.sendMsg(phoneNum, "3033300", params);
					} else if (coherePrize.getValue().toString().equals("49")) {
						String[] phoneNum = { info.getTelephone() };
						String[] params = { info.getUsername(), coherePrizeHistory.getAccount(),
								coherePrizeHistory.getAccount() };
						coherePrizeHistoryService.sendMsg(phoneNum, "3031354", params);
					}
					coherePrizeHistoryService.saveOrUpdate(coherePrizeHistory);
					return 0; //充值成功
				} else if (mid == -2) {
					coherePrizeHistory.setState(2); //充值失败
					coherePrizeHistoryService.saveOrUpdate(coherePrizeHistory);
					return -4; //充值失败 ,充值系统余额不足
				} else if (mid == 1) {
					coherePrizeHistory.setState(4); //充值中....
					coherePrizeHistoryService.saveOrUpdate(coherePrizeHistory);
					return 1; //充值中...
				} else {
					coherePrizeHistory.setState(2); //充值失败
					coherePrizeHistoryService.saveOrUpdate(coherePrizeHistory);
					return -1; //充值失败,联系开发人员
				}
			} else {
				return -2;//没有奖励要发放或者已经领奖
			}
		} else if (coherePrize.getType().toString().equals("1")) { //CDK
			if (!coherePrizeHistory.getState().toString().equals("3")) {//未发放
				BoonCdkey boonCdkey = boonCdkeyService.findOneNotUsedByType(5);
				if (boonCdkey == null) {
					return -3; //cdk不足
				}
				boonCdkey.setUsedDate(new Date());
				boonCdkey.setUserId(coherePrizeHistory.getUserId());
				UserInfo info = userInfoDao.findOne(coherePrizeHistory.getUserId());
				String[] phoneNum = { info.getTelephone() };
				String[] params = { info.getUsername(), boonCdkey.getCdkey(), coherePrizeHistory.getAccount() };
				coherePrizeHistoryService.sendMsg(phoneNum, "3031355", params);
				coherePrizeHistory.setState(3);
				coherePrizeHistoryService.saveOrUpdate(coherePrizeHistory);
				boonCdkeyService.save(boonCdkey);
				return 0;
			} else {
				return -1;//充值失败
			}
		} else {
			return -2;//没有奖励要发放或者已经领奖
		}
	}

	/**
	 * 手动更改奖励发放状态
	 * @param 奖品记录id
	 */
	@RequestMapping("/checkChargeStatusHand")
	@ResponseBody
	public JsonResponseMsg changeChargeStatus(String prizeHistoryId) {
		JsonResponseMsg result = new JsonResponseMsg();
		if (StringUtils.isBlank(prizeHistoryId)) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}
		CoherePrizeHistory coherePrizeHistory = coherePrizeHistoryService.findOne(NumberUtils.toLong(prizeHistoryId));
		coherePrizeHistory.setState(3);
		coherePrizeHistoryService.saveOrUpdate(coherePrizeHistory);
		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 * 导出列表
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping("/export")
	@ResponseBody
	public ModelAndView export(HttpServletRequest req, HttpServletResponse response, Integer type, Long activityId,
			String prizeHistoryIds) throws UnsupportedEncodingException {
		Map<String, Object> params = new HashMap<String, Object>(16);
		params.put("prizeType",
				StringUtils.isBlank(req.getParameter("prizeType")) ? "" : req.getParameter("prizeType"));
		params.put("prizeState",
				StringUtils.isBlank(req.getParameter("prizeState")) ? "" : req.getParameter("prizeState"));
		params.put("searchUser",
				StringUtils.isBlank(req.getParameter("searchUser")) ? "" : req.getParameter("searchUser").trim());
		params.put("startTime",
				StringUtils.isBlank(req.getParameter("startTime")) ? "" : req.getParameter("startTime"));
		params.put("endTime", StringUtils.isBlank(req.getParameter("endTime")) ? "" : req.getParameter("endTime"));
		List<Map<String, Object>> list = coherePrizeService.export(params, prizeHistoryIds, type, activityId);
		if (list != null) {
			String title = "奖品记录";
			String[][] contents = new String[list.size() + 1][];
			response.setHeader("content-disposition",
					"attachment;filename=" + new String(title.getBytes(), "ISO8859-1") + ".xls");
			// 设置标题行
			String[] contentTitle = new String[11];
			contentTitle[0] = "序号";
			contentTitle[1] = "用户注册手机";
			contentTitle[2] = "昵称";
			contentTitle[3] = "兑奖时间";
			contentTitle[4] = "QQ";
			contentTitle[5] = "游戏大区";
			contentTitle[6] = "游戏ID";
			contentTitle[7] = "兑奖奖项";
			contentTitle[8] = "当前状态";
			contentTitle[9] = "用户注册时间";
			contents[0] = contentTitle;
			// 设置内容
			String state = "";
			if (CollectionUtils.isNotEmpty(list)) {
				for (int i = 0; i < list.size(); i++) {
					Map<String, Object> obj = list.get(i);
					String[] row = new String[11];
					row[0] = MapUtils.getString(obj, "id");
					row[1] = MapUtils.getString(obj, "telephone");
					row[2] = MapUtils.getString(obj, "nickname");
					row[3] = MapUtils.getString(obj, "create_date");
					row[4] = MapUtils.getString(obj, "account");
					row[5] = MapUtils.getString(obj, "serveName");
					row[6] = MapUtils.getString(obj, "gameName");
					row[7] = MapUtils.getString(obj, "prizeName");
					state = MapUtils.getString(obj, "state").toString();
					if (state.equals("0")) {
						row[8] = "用户未申请";
					} else if (state.equals("1")) {
						row[8] = "申请未发放";
					} else if (state.equals("2")) {
						row[8] = "发放失败";
					} else if (state.equals("3")) {
						row[8] = "发放成功";
					}
					row[9] = MapUtils.getString(obj, "registerTime");
					contents[i + 1] = row;
				}
			}
			try {
				ExcelUtils.exportExcel2(title, contents, false, response);
			} catch (Exception e) {

			}
		}

		return null;
	}

}
