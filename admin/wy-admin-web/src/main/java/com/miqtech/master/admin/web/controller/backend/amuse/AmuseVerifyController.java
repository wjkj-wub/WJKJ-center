package com.miqtech.master.admin.web.controller.backend.amuse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.admin.web.controller.backend.BaseController;
import com.miqtech.master.admin.web.util.Servlets;
import com.miqtech.master.consts.AmuseConstant;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.Msg4UserType;
import com.miqtech.master.consts.MsgConstant;
import com.miqtech.master.consts.OperateLogConstant;
import com.miqtech.master.consts.SystemUserConstant;
import com.miqtech.master.consts.award.AwardConstant;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.amuse.AmuseActivityInfo;
import com.miqtech.master.entity.amuse.AmuseMsgFeedback;
import com.miqtech.master.entity.amuse.AmuseRewardProgress;
import com.miqtech.master.entity.amuse.AmuseVerify;
import com.miqtech.master.entity.award.AwardRecord;
import com.miqtech.master.entity.common.OperateLog;
import com.miqtech.master.entity.common.SystemUser;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.service.amuse.AmuseActivityInfoService;
import com.miqtech.master.service.amuse.AmuseActivityRecordService;
import com.miqtech.master.service.amuse.AmuseMsgFeedbackService;
import com.miqtech.master.service.amuse.AmuseRewardProgressService;
import com.miqtech.master.service.amuse.AmuseVerifyService;
import com.miqtech.master.service.award.AwardRecordService;
import com.miqtech.master.service.common.OperateLogService;
import com.miqtech.master.service.system.SystemUserService;
import com.miqtech.master.thirdparty.service.JpushService;
import com.miqtech.master.thirdparty.util.SMSMessageUtil;
import com.miqtech.master.utils.ExcelUtils;
import com.miqtech.master.vo.PageVO;

/**
 * 审核发放管理
 */
@Controller
@RequestMapping("amuse/verify")
public class AmuseVerifyController extends BaseController {

	private static final ExecutorService pool = Executors
			.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	private static final Logger LOGGER = LoggerFactory.getLogger(AmuseVerifyController.class);

	@Autowired
	private AmuseActivityInfoService amuseActivityInfoService;
	@Autowired
	private AmuseVerifyService amuseVerifyService;
	@Autowired
	private JpushService msgOperateService;
	@Autowired
	private AmuseActivityRecordService amuseActivityRecordService;
	@Autowired
	private AmuseMsgFeedbackService amuseMsgFeedbackService;
	@Autowired
	private SystemUserService systemUserService;
	@Autowired
	private AmuseRewardProgressService amuseRewardProgressService;
	@Autowired
	private AwardRecordService awardRecordService;
	@Autowired
	private OperateLogService operateLogService;

	/**
	 * 审核列表
	 */
	@RequestMapping("list/{page}")
	public ModelAndView list(HttpServletRequest request, @PathVariable("page") int page, String telephone,
			String userId, String activityId, String beginDate, String endDate, String state, String isSpecial) {
		ModelAndView mv = new ModelAndView("amuse/verifyList");

		Map<String, Object> searchParams = Maps.newHashMap();
		searchParams.put("activityId", activityId);
		searchParams.put("userId", userId);
		searchParams.put("telephone", telephone);
		searchParams.put("beginDate", beginDate);
		searchParams.put("endDate", endDate);
		searchParams.put("state", state);
		searchParams.put("isSpecial", isSpecial);
		SystemUser sysUser = Servlets.getSessionUser(request);
		if (SystemUserConstant.TYPE_AMUSE_VERIFY.equals(sysUser.getUserType())) {
			searchParams.put("userId", sysUser.getId());
		}
		mv.addObject("params", searchParams);

		PageVO vo = amuseVerifyService.page(page, searchParams);
		pageModels(mv, vo.getList(), page, vo.getTotal());

		List<AmuseMsgFeedback> feedbacks = amuseMsgFeedbackService.findValidByType(AmuseConstant.FEEDBACK_TYPE_VERIFY);
		mv.addObject("feedbacks", feedbacks);

		List<AmuseActivityInfo> acitivitys = amuseActivityInfoService.findAllValid();
		mv.addObject("activitys", acitivitys);

		List<SystemUser> verifyUsers = systemUserService.findValidByUserType(SystemUserConstant.TYPE_AMUSE_VERIFY);
		mv.addObject("verifyUsers", verifyUsers);

		return mv;
	}

	/**
	 * 导出审核列表
	 */
	@RequestMapping("export/{page}")
	public ModelAndView exportPersonal(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("page") int page, String username, String activityId, String beginDate, String endDate,
			String state, String isSpecial) {
		ModelAndView mv = new ModelAndView("amuse/verifyList");

		Map<String, Object> searchParams = Maps.newHashMap();
		searchParams.put("activityId", activityId);
		searchParams.put("username", username);
		searchParams.put("beginDate", beginDate);
		searchParams.put("endDate", endDate);
		searchParams.put("state", state);
		searchParams.put("isSpecial", isSpecial);
		SystemUser user = Servlets.getSessionUser(request);
		if (SystemUserConstant.TYPE_AMUSE_VERIFY.equals(user.getUserType())) {
			searchParams.put("userId", user.getId());
		}
		mv.addObject("params", searchParams);

		PageVO vo = amuseVerifyService.page(page, searchParams);
		List<Map<String, Object>> list = vo.getList();

		// 查询excel标题
		String title = "审核列表";

		// 编辑excel内容
		String[][] contents = new String[list.size() + 1][];

		// 设置标题行
		String[] contentTitle = new String[7];
		contentTitle[0] = "订单编号";
		contentTitle[1] = "赛事名";
		contentTitle[2] = "奖励";
		contentTitle[3] = "领奖者账号";
		contentTitle[4] = "网娱账号";
		contentTitle[5] = "状态";
		contentTitle[6] = "备注";
		contents[0] = contentTitle;

		// 设置内容
		for (int i = 0; i < list.size(); i++) {
			Map<String, Object> verify = list.get(i);
			String[] row = new String[7];
			row[0] = MapUtils.getString(verify, "serial");
			row[1] = MapUtils.getString(verify, "activityName");
			row[2] = MapUtils.getString(verify, "activityReward");
			row[3] = MapUtils.getString(verify, "honoree");
			row[4] = MapUtils.getString(verify, "username");
			Integer stateInt = MapUtils.getInteger(verify, "state");
			String stateStr = "";
			if (null != stateInt) {
				if (stateInt == 0) {
					stateStr = "未认领";
				} else if (stateInt == 1) {
					stateStr = "待审核";
				} else if (stateInt == 2) {
					stateStr = "拒绝";
				} else if (stateInt == 3) {
					stateStr = "审核通过";
				} else if (stateInt == 4) {
					stateStr = "已发放";
				} else if (stateInt == 5) {
					stateStr = "结束";
				}
			}
			row[5] = stateStr;
			row[6] = MapUtils.getString(verify, "remark");
			contents[i + 1] = row;
		}

		try {
			ExcelUtils.exportExcel(title, contents, false, response);
		} catch (Exception e) {
			LOGGER.error("导出数据异常：", e);
		}
		return null;
	}

	/**
	 * 认领
	 */
	@RequestMapping("charge")
	public String charge(HttpServletRequest req, String id) {
		SystemUser user = Servlets.getSessionUser(req);
		if (NumberUtils.isNumber(id)) {
			Long idLong = NumberUtils.toLong(id);
			AmuseVerify verify = amuseVerifyService.findValidOne(idLong);
			if (verify != null && AmuseConstant.VERIFY_STATE_AWAIT.equals(verify.getState())) {
				amuseVerifyService.changeState(idLong, user.getId(), AmuseConstant.VERIFY_STATE_INVERIFY);
			}
		}
		return "redirect:/amuse/verify/list/1?state=0";
	}

	/**
	 * 认证详情
	 */
	@ResponseBody
	@RequestMapping("detail")
	public JsonResponseMsg detail(String id) {
		JsonResponseMsg result = new JsonResponseMsg().fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		if (NumberUtils.isNumber(id)) {
			AmuseVerify verify = amuseVerifyService.findValidOneDetail(NumberUtils.toLong(id));

			if (null != verify) {
				Map<String, Object> applyInfo = amuseActivityRecordService.getUserActivityApplyInfo(verify.getUserId(),
						verify.getActivityId());
				if (MapUtils.isEmpty(applyInfo)) {
					applyInfo = Maps.newHashMap();
				}

				applyInfo.put("id", verify.getId());
				applyInfo.put("remark", verify.getRemark());
				applyInfo.put("describes", verify.getDescribes());
				applyInfo.put("imgs", verify.getImgs());
				applyInfo.put("createDate", verify.getCreateDate());
				result.setObject(applyInfo);
			}
		}
		return result;
	}

	/**
	 * 审核处理
	 */
	@ResponseBody
	@RequestMapping("operate")
	public JsonResponseMsg operate(HttpServletRequest req, String[] id, String operate, String remark) {
		JsonResponseMsg result = new JsonResponseMsg();

		List<Long> ids = new ArrayList<Long>();
		if (ArrayUtils.isNotEmpty(id)) {
			for (String idStr : id) {
				if (NumberUtils.isNumber(idStr)) {
					ids.add(NumberUtils.toLong(idStr));
				}
			}
		}
		if (CollectionUtils.isEmpty(ids) || !NumberUtils.isNumber(operate)) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}

		SystemUser sysUser = Servlets.getSessionUser(req);
		Long sysUserId = sysUser.getId();

		// 检查订单状态，避免重复提交
		List<AmuseVerify> verifys = amuseVerifyService.findValidByIdsWithActivityAndUser(ids);
		if (CollectionUtils.isNotEmpty(verifys)) {
			for (AmuseVerify v : verifys) {
				if (!AmuseConstant.VERIFY_STATE_INVERIFY.equals(v.getState())) {// 非待处理状态的订单,重复操作
					return result.fill(CommonConstant.CODE_ERROR_PARAM, "订单:" + v.getState() + "已处理,请刷新重试");
				}
			}
		} else {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, "找不到认证信息");
		}

		// 更改订单状态
		Integer state = null;
		Integer isSpecial = CommonConstant.INT_BOOLEAN_FALSE;
		if (StringUtils.equals("1", operate)) {// 审核通过
			state = AmuseConstant.VERIFY_STATE_NOGIVE;
			amuseVerifyService.changeState(ids, sysUserId, state, remark, isSpecial);
		} else if (StringUtils.equals("2", operate) || StringUtils.equals("3", operate)) {// 审核拒绝
			if (StringUtils.equals("3", operate)) {// 拉黑用户
				isSpecial = CommonConstant.INT_BOOLEAN_TRUE;
			}

			state = AmuseConstant.VERIFY_STATE_REFUSED;
			amuseVerifyService.changeState(ids, sysUserId, state, remark, isSpecial);

			// 发送通知
			if (CollectionUtils.isNotEmpty(ids)) {
				final List<Long> notifyIds = ids;
				pool.execute(new Runnable() {
					@Override
					public void run() {
						for (Long id : notifyIds) {
							AmuseVerify verify = amuseVerifyService.findValidOne(id);
							if (null != verify) {
								AmuseActivityInfo amuse = amuseActivityInfoService.findById(verify.getActivityId());
								String content = "您参加的" + amuse.getTitle()
										+ "娱乐赛，凭证不能达标。若有异议，请在领奖页申诉重新提交凭证，我们会尽快联系您处理。";
								msgOperateService.notifyMemberAliasMsg(Msg4UserType.AMUSE.ordinal(),
										verify.getUserId(), MsgConstant.PUSH_MSG_TYPE_AMUSE, "娱乐赛提示信息", content, true,
										amuse.getId());
							}
						}
					}
				});
			}
		}

		// 记录审核进度
		Date now = new Date();
		verifys = amuseVerifyService.findValidByIdsWithActivityAndUser(ids);
		if (CollectionUtils.isNotEmpty(verifys)) {
			List<AmuseRewardProgress> ps = Lists.newArrayList();
			List<OperateLog> operateLogs = Lists.newArrayList();
			List<AwardRecord> records = null;
			for (AmuseVerify v : verifys) {
				if (AmuseConstant.VERIFY_STATE_NOGIVE.equals(state)) {
					// 审核通过记录
					AmuseRewardProgress progress = new AmuseRewardProgress();
					progress.setActivityId(v.getActivityId());
					progress.setUserId(v.getUserId());
					progress.setSysUserId(sysUserId);
					progress.setTargetId(v.getId());
					progress.setType(1);
					progress.setState(2);
					progress.setRemark(remark);
					progress.setValid(CommonConstant.INT_BOOLEAN_TRUE);
					progress.setCreateDate(now);
					ps.add(progress);

					// 后台日志
					OperateLog log = new OperateLog();
					log.setSysType(OperateLogConstant.SYS_TYPE_ADMIN);
					log.setSysUserId(sysUserId);
					log.setThirdId(v.getId());
					log.setType(OperateLogConstant.TYPE_ADMIN_VERIFY_PASSED);
					log.setInfo("订单:" + v.getSerial() + "审核通过");
					log.setValid(CommonConstant.INT_BOOLEAN_TRUE);
					log.setUpdateDate(now);
					log.setCreateDate(now);
					operateLogs.add(log);

					try {
						// 待发放记录
						AmuseRewardProgress clone = (AmuseRewardProgress) progress.clone();
						clone.setState(9);
						clone.setCreateDate(now);
						ps.add(clone);

						// 自有商品自动发放
						AmuseActivityInfo activity = v.getActivityInfo();
						if (activity != null) {
							Integer awardType = activity.getAwardType();
							Integer awardSubType = activity.getAwardSubType();
							Integer awardAmount = activity.getAwardAmount();
							if (AwardConstant.TYPE_OWN.equals(awardType)
									&& (AwardConstant.SUB_TYPE_OWN_COIN.equals(awardSubType)
											|| AwardConstant.SUB_TYPE_OWN_REDBAG.equals(awardSubType))
									&& awardAmount != null) {
								// 自有商品自动发放记录
								AmuseRewardProgress grantProgress = (AmuseRewardProgress) clone.clone();
								grantProgress.setState(4);
								grantProgress.setCreateDate(now);
								ps.add(grantProgress);

								// 增加奖品发放任务
								AwardRecord r = new AwardRecord();
								r.setUserId(v.getUserId());
								r.setType(awardType);
								r.setSubType(awardSubType);
								r.setAmount(awardAmount.doubleValue());
								r.setSourceType(AwardConstant.SOURCE_TYPE_AMUSE);
								r.setSourceTargetId(v.getActivityId());
								r.setValid(CommonConstant.INT_BOOLEAN_TRUE);
								r.setUpdateUserId(sysUserId);
								r.setCreateUserId(sysUserId);
								r.setUpdateDate(now);
								r.setCreateDate(now);
								if (records == null) {
									records = Lists.newArrayList();
								}
								records.add(r);

								// 更新审核状态为已发放,标记发放人为系统自动发放
								v.setState(AmuseConstant.VERIFY_STATE_GIVED);
								v.setClaimUserId(0L);

								// 短信通知
								UserInfo user = v.getUserInfo();
								if (user != null) {
									String award = StringUtils.EMPTY;
									if (AwardConstant.SUB_TYPE_OWN_COIN.equals(awardSubType)) {
										award = awardAmount + "金币";
									} else if (AwardConstant.SUB_TYPE_OWN_REDBAG.equals(awardSubType)) {
										award = awardAmount + "元红包";
									}

									try {
										String[] phoneNum = { user.getUsername() };
										String[] params = { activity.getTitle(), award, user.getUsername() };
										SMSMessageUtil.sendTemplateMessage(phoneNum, "6185", params);
									} catch (Exception e) {
										LOGGER.error("短信发送异常:", e);
									}

									// 后台日志
									OperateLog grantLog = new OperateLog();
									grantLog.setSysType(OperateLogConstant.SYS_TYPE_ADMIN);
									grantLog.setSysUserId(sysUserId);
									grantLog.setThirdId(v.getId());
									grantLog.setType(OperateLogConstant.TYPE_ADMIN_GRANT);
									grantLog.setInfo("订单:" + v.getSerial() + "发放" + award);
									grantLog.setValid(CommonConstant.INT_BOOLEAN_TRUE);
									grantLog.setUpdateDate(now);
									grantLog.setCreateDate(now);
									operateLogs.add(grantLog);
								}

								// 发送系统推送
								String msg = activity.getGrantMsg();
								if (StringUtils.isNotBlank(msg)) {
									try {
										msgOperateService.notifyMemberAliasMsg(Msg4UserType.COMMENT.ordinal(),
												v.getUserId(), MsgConstant.PUSH_MSG_TYPE_COMMENT, "娱乐赛奖品发放", msg, true,
												v.getActivityId());
									} catch (Exception e) {
										LOGGER.error("发送系统推送异常:", e);
									}
								}
							}
						}
					} catch (Exception e) {
						LOGGER.error("拷贝对象异常:", e);
					}
				} else if (AmuseConstant.VERIFY_STATE_REFUSED.equals(state)) {
					AmuseRewardProgress progress = new AmuseRewardProgress();
					progress.setActivityId(v.getActivityId());
					progress.setUserId(v.getUserId());
					progress.setSysUserId(sysUser.getId());
					progress.setTargetId(v.getId());
					progress.setType(1);
					progress.setState(3);
					progress.setRemark(remark);
					progress.setValid(CommonConstant.INT_BOOLEAN_TRUE);
					progress.setCreateDate(now);

					// 后台日志
					OperateLog grantLog = new OperateLog();
					grantLog.setSysType(OperateLogConstant.SYS_TYPE_ADMIN);
					grantLog.setSysUserId(sysUserId);
					grantLog.setThirdId(v.getId());
					grantLog.setType(OperateLogConstant.TYPE_ADMIN_VERIFY_REFUSE);
					grantLog.setInfo("订单:" + v.getSerial() + "审核拒绝");
					grantLog.setValid(CommonConstant.INT_BOOLEAN_TRUE);
					grantLog.setUpdateDate(now);
					grantLog.setCreateDate(now);
					operateLogs.add(grantLog);

					ps.add(progress);
				}
			}
			amuseVerifyService.save(verifys);
			amuseRewardProgressService.save(ps);
			awardRecordService.grant(records, sysUserId, null);
			operateLogService.save(operateLogs);
		}

		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 * 任务分配
	 */
	@ResponseBody
	@RequestMapping("allot")
	public JsonResponseMsg allot(String[] id, String sysUserId) {
		JsonResponseMsg result = new JsonResponseMsg();

		List<Long> ids = Lists.newArrayList();
		if (ArrayUtils.isNotEmpty(id)) {
			for (String idStr : id) {
				if (NumberUtils.isNumber(idStr)) {
					ids.add(NumberUtils.toLong(idStr));
				}
			}
		}
		if (CollectionUtils.isEmpty(ids) || !NumberUtils.isNumber(sysUserId)) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}

		Long sysUserIdLong = NumberUtils.toLong(sysUserId);
		amuseVerifyService.changeState(ids, sysUserIdLong, AmuseConstant.VERIFY_STATE_INVERIFY, null, null);

		return result;
	}

}
