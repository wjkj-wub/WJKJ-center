package com.miqtech.master.admin.web.controller.backend.amuse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.entity.JsonResponseMsg;
import com.miqtech.master.entity.amuse.AmuseActivityInfo;
import com.miqtech.master.entity.amuse.AmuseAppeal;
import com.miqtech.master.entity.amuse.AmuseMsgFeedback;
import com.miqtech.master.entity.amuse.AmuseRewardProgress;
import com.miqtech.master.entity.amuse.AmuseVerify;
import com.miqtech.master.entity.award.AwardRecord;
import com.miqtech.master.entity.common.OperateLog;
import com.miqtech.master.entity.common.SystemUser;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.service.amuse.AmuseActivityInfoService;
import com.miqtech.master.service.amuse.AmuseActivityRecordService;
import com.miqtech.master.service.amuse.AmuseAppealService;
import com.miqtech.master.service.amuse.AmuseMsgFeedbackService;
import com.miqtech.master.service.amuse.AmuseRewardProgressService;
import com.miqtech.master.service.amuse.AmuseVerifyService;
import com.miqtech.master.service.award.AwardRecordService;
import com.miqtech.master.service.common.OperateLogService;
import com.miqtech.master.service.system.SystemUserService;
import com.miqtech.master.thirdparty.service.JpushService;
import com.miqtech.master.thirdparty.util.SMSMessageUtil;
import com.miqtech.master.utils.ExcelUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

/**
 * 申诉
 */
@Controller
@RequestMapping("amuse/appeal")
public class AmuseAppealController extends BaseController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AmuseAppealController.class);

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private AmuseAppealService amuseAppealService;
	@Autowired
	private SystemUserService systemUserService;
	@Autowired
	private AmuseActivityInfoService amuseActivityInfoService;
	@Autowired
	private AmuseVerifyService amuseVerifyService;
	@Autowired
	private AmuseActivityRecordService amuseActivityRecordService;
	@Autowired
	private AmuseMsgFeedbackService amuseMsgFeedbackService;
	@Autowired
	private AmuseRewardProgressService amuseRewardProgressService;
	@Autowired
	private AwardRecordService awardRecordService;
	@Autowired
	private OperateLogService operateLogService;
	@Autowired
	private JpushService msgOperateService;

	/**
	 * 申诉列表
	 */
	@RequestMapping("list/{page}")
	public ModelAndView list(HttpServletRequest request, @PathVariable("page") int page, String state, String telephone,
			String activityId, String beginDate, String endDate, String sysUserId) {
		ModelAndView mv = new ModelAndView("amuse/appealList");

		Map<String, Object> params = Maps.newHashMap();
		params.put("state", state);
		params.put("telephone", telephone);
		params.put("activityId", activityId);
		params.put("beginDate", beginDate);
		params.put("endDate", endDate);
		params.put("sysUserId", sysUserId);
		SystemUser user = Servlets.getSessionUser(request);
		if (SystemUserConstant.TYPE_AMUSE_APPEAL.equals(user.getUserType())) {
			params.put("sysUserId", user.getId());
		}
		mv.addObject("params", params);

		PageVO vo = amuseAppealService.page(page, params);
		pageModels(mv, vo.getList(), page, vo.getTotal());

		List<AmuseActivityInfo> acitivitys = amuseActivityInfoService.findAllValid();
		mv.addObject("activitys", acitivitys);

		List<AmuseMsgFeedback> feedbacks = amuseMsgFeedbackService.findValidByType(AmuseConstant.FEEDBACK_TYPE_APPEAL);
		mv.addObject("feedbacks", feedbacks);

		List<SystemUser> appealUsers = systemUserService.findValidByUserType(SystemUserConstant.TYPE_AMUSE_APPEAL);
		mv.addObject("appealUsers", appealUsers);

		return mv;
	}

	/**
	 * 导出审核列表
	 */
	@RequestMapping("export/{page}")
	public ModelAndView exportPersonal(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("page") int page, String state, String telephone, String activityId, String beginDate,
			String endDate) {
		Map<String, Object> params = Maps.newHashMap();
		params.put("state", state);
		params.put("telephone", telephone);
		params.put("activityId", activityId);
		params.put("beginDate", beginDate);
		params.put("endDate", endDate);
		SystemUser user = Servlets.getSessionUser(request);
		if (SystemUserConstant.TYPE_AMUSE_APPEAL.equals(user.getUserType())) {
			params.put("sysUserId", user.getId());
		}

		PageVO vo = amuseAppealService.page(page, params);
		List<Map<String, Object>> list = vo.getList();

		// 查询excel标题
		String title = "申诉列表";

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
			row[3] = MapUtils.getString(verify, "recordTelephone");
			row[4] = MapUtils.getString(verify, "username");
			Integer stateInt = MapUtils.getInteger(verify, "state");
			String stateStr = "";
			if (null != stateInt) {
				if (stateInt == 0) {
					stateStr = "待重审";
				} else if (stateInt == 1) {
					stateStr = "申诉驳回";
				} else if (stateInt == 2) {
					stateStr = "申诉通过";
				} else if (stateInt == 3) {
					stateStr = "申诉处理";
				} else if (stateInt == 4) {
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
	 * 申诉详情
	 */
	@ResponseBody
	@RequestMapping("detail")
	public JsonResponseMsg detail(String id) {
		JsonResponseMsg result = new JsonResponseMsg().fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);

		if (!NumberUtils.isNumber(id)) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}

		Long idLong = NumberUtils.toLong(id);
		AmuseAppeal appeal = amuseAppealService.findDetailById(idLong);
		if (null != appeal) {

			Map<String, Object> applyInfo = amuseActivityRecordService.getUserActivityApplyInfo(appeal.getUserId(),
					appeal.getActivityId());
			if (MapUtils.isEmpty(applyInfo)) {
				applyInfo = Maps.newHashMap();
			}

			applyInfo.put("id", appeal.getId());
			applyInfo.put("appealCreateDate", appeal.getCreateDate());
			applyInfo.put("appealDescribes", appeal.getDescribes());
			applyInfo.put("appealImgs", appeal.getImgs());
			applyInfo.put("appealState", appeal.getState());

			// 匹配认证信息
			AmuseVerify verify = amuseVerifyService.findValidOneByActivityIdAndUserId(appeal.getActivityId(),
					appeal.getUserId(), true);
			if (null != verify) {
				applyInfo.put("state", verify.getState());
				applyInfo.put("remark", verify.getRemark());
				applyInfo.put("describes", verify.getDescribes());
				applyInfo.put("imgs", verify.getImgs());
				applyInfo.put("createDate", verify.getCreateDate());
			}

			// 匹配申诉原因
			Long categoryId = appeal.getCategoryId();
			if (null != categoryId) {
				AmuseMsgFeedback f = amuseMsgFeedbackService.findById(categoryId);
				if (null != f) {
					applyInfo.put("reason", f.getContent());
				}
			}

			result.setObject(applyInfo);
		}

		return result;
	}

	/**
	 * 申诉处理
	 */
	@ResponseBody
	@RequestMapping("operate")
	public JsonResponseMsg operate(HttpServletRequest req, String[] id, String operate, String remark) {
		JsonResponseMsg result = new JsonResponseMsg();

		List<Long> ids = Lists.newArrayList();
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

		List<AmuseAppeal> appeals = amuseAppealService.findValidByIdIn(ids);
		if (CollectionUtils.isNotEmpty(appeals)) {
			for (AmuseAppeal appeal : appeals) {
				if (!AmuseConstant.APPEAL_STATE_INAPPEAL.equals(appeal.getState())) {
					return result.fill(CommonConstant.CODE_ERROR_PARAM, "申诉订单:" + appeal.getSerial() + "状态已过期,请刷新后重试");
				}
			}
		} else {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, "找不到申诉订单");
		}

		Integer state = null;
		if (StringUtils.equals("1", operate)) {
			// 将认证信息通过，并保存进度
			state = AmuseConstant.APPEAL_STATE_PASSED;
		} else if (StringUtils.equals("2", operate)) {
			state = AmuseConstant.APPEAL_STATE_REFUSED;
		} else {
			state = AmuseConstant.APPEAL_STATE_DEAL;
		}
		amuseAppealService.changeState(ids, state, null, remark);

		// 记录审核进度
		appeals = amuseAppealService.findValidByIdIn(ids);
		if (CollectionUtils.isNotEmpty(appeals)) {
			Integer progressState = 0;
			if (AmuseConstant.APPEAL_STATE_PASSED.equals(state)) {
				progressState = 10;
			} else if (AmuseConstant.APPEAL_STATE_REFUSED.equals(state)) {
				progressState = 7;
			} else {
				progressState = 6;
			}

			List<AmuseRewardProgress> ps = Lists.newArrayList();
			List<AmuseVerify> verifys = Lists.newArrayList();
			List<OperateLog> operateLogs = Lists.newArrayList();
			Date now = new Date();
			for (AmuseAppeal a : appeals) {
				AmuseRewardProgress progress = new AmuseRewardProgress();
				progress.setActivityId(a.getActivityId());
				progress.setUserId(a.getUserId());
				progress.setSysUserId(sysUserId);
				progress.setTargetId(a.getId());
				progress.setType(2);
				progress.setState(progressState);
				progress.setRemark(remark);
				progress.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				progress.setCreateDate(now);
				ps.add(progress);

				if (AmuseConstant.APPEAL_STATE_PASSED.equals(state)) {// 申诉通过时更新认证信息，并记录认证通过进度
					try {
						List<AmuseVerify> uVerifys = amuseVerifyService
								.findValidByActivityIdAndUserIdWithActivityAndUser(a.getActivityId(), a.getUserId());
						if (CollectionUtils.isNotEmpty(uVerifys)) {
							List<AwardRecord> records = null;
							for (AmuseVerify v : uVerifys) {
								// 记录申诉通过记录
								AmuseRewardProgress clone = (AmuseRewardProgress) progress.clone();
								clone.setActivityId(v.getActivityId());
								clone.setUserId(v.getUserId());
								clone.setType(1);
								clone.setTargetId(v.getId());
								clone.setState(9);
								clone.setCreateDate(now);
								ps.add(clone);

								// 修改认证状态
								v.setState(AmuseConstant.VERIFY_STATE_APPEAL_PASSED);
								v.setRemark(remark);
								v.setUpdateDate(now);
								String lessIssueUserSql = SqlJoiner.join("SELECT u.id, count(v.id) count",
										" FROM sys_t_user u LEFT JOIN amuse_t_verify v ON u.id = v.claim_user_id WHERE u.user_type = ",
										SystemUserConstant.TYPE_AMUSE_ISSUE.toString(),
										" GROUP BY u.id ORDER BY count ASC LIMIT 1");
								Map<String, Object> lessIssueUser = queryDao.querySingleMap(lessIssueUserSql);
								Long issueUserId = MapUtils.getLong(lessIssueUser, "id");
								v.setClaimUserId(issueUserId);

								// 后台日志
								OperateLog log = new OperateLog();
								log.setSysType(OperateLogConstant.SYS_TYPE_ADMIN);
								log.setSysUserId(sysUserId);
								log.setThirdId(a.getId());
								log.setType(OperateLogConstant.TYPE_ADMIN_APPEAL_PASSED);
								log.setInfo("申诉订单:" + a.getSerial() + "申诉通过");
								log.setValid(CommonConstant.INT_BOOLEAN_TRUE);
								log.setUpdateDate(now);
								log.setCreateDate(now);
								operateLogs.add(log);

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
										verifys.add(v);

										// 短信通知
										try {
											UserInfo user = v.getUserInfo();
											if (user != null) {
												String award = StringUtils.EMPTY;
												if (AwardConstant.SUB_TYPE_OWN_COIN.equals(awardSubType)) {
													award = awardAmount + "金币";
												} else if (AwardConstant.SUB_TYPE_OWN_REDBAG.equals(awardSubType)) {
													award = awardAmount + "元红包";
												}
												String[] phoneNum = { user.getUsername() };
												String[] params = { activity.getTitle(), award, user.getUsername() };
												SMSMessageUtil.sendTemplateMessage(phoneNum, "6185", params);

												// 后台日志
												OperateLog grantLog = new OperateLog();
												grantLog.setSysType(OperateLogConstant.SYS_TYPE_ADMIN);
												grantLog.setSysUserId(sysUserId);
												grantLog.setThirdId(v.getId());
												grantLog.setType(OperateLogConstant.TYPE_ADMIN_VERIFY_PASSED);
												grantLog.setInfo("订单:" + v.getSerial() + "发放" + award);
												grantLog.setValid(CommonConstant.INT_BOOLEAN_TRUE);
												grantLog.setUpdateDate(now);
												grantLog.setCreateDate(now);
												operateLogs.add(grantLog);
											}
										} catch (Exception e) {
											LOGGER.error("短信发送异常:", e);
										}

										// 发送系统推送
										String msg = activity.getGrantMsg();
										if (StringUtils.isNotBlank(msg)) {
											try {
												msgOperateService.notifyMemberAliasMsg(Msg4UserType.COMMENT.ordinal(),
														v.getUserId(), MsgConstant.PUSH_MSG_TYPE_COMMENT, "娱乐赛奖品发放",
														msg, true, v.getActivityId());
											} catch (Exception e) {
												LOGGER.error("发送系统推送异常:", e);
											}
										}
									}
								}

								// 将审核信息加入到更新列表
								verifys.add(v);
							}
							amuseVerifyService.save(verifys);
							awardRecordService.grant(records, sysUserId, null);
							operateLogService.save(operateLogs);
						}
					} catch (Exception e) {
						LOGGER.error("克隆进度对象异常:", e);
					}
				} else if (AmuseConstant.APPEAL_STATE_REFUSED.equals(state)) {// 申诉驳回,认证状态置于结束
					try {
						// 更新认证时间,做匹配结束状态用
						List<AmuseVerify> uVerifys = amuseVerifyService.findByActivityIdAndUserIdAndValid(
								a.getActivityId(), a.getUserId(), CommonConstant.INT_BOOLEAN_TRUE);
						for (AmuseVerify v : uVerifys) {
							v.setUpdateDate(now);
							verifys.add(v);
						}

						// 记录进度
						AmuseRewardProgress clone = (AmuseRewardProgress) progress.clone();
						clone.setRemark(null);
						clone.setState(8);
						clone.setCreateDate(new Date());
						ps.add(clone);

						// 后台日志
						OperateLog log = new OperateLog();
						log.setSysType(OperateLogConstant.SYS_TYPE_ADMIN);
						log.setSysUserId(sysUserId);
						log.setThirdId(a.getId());
						log.setType(OperateLogConstant.TYPE_ADMIN_APPEAL_REFUSE);
						log.setInfo("申诉订单:" + a.getSerial() + "申诉驳回");
						log.setValid(CommonConstant.INT_BOOLEAN_TRUE);
						log.setUpdateDate(now);
						log.setCreateDate(now);
						operateLogs.add(log);
					} catch (Exception e) {
						LOGGER.error("克隆进度对象异常:", e);
					}
				} else if (AmuseConstant.APPEAL_STATE_DEAL.equals(state)) {
					// 后台日志
					OperateLog log = new OperateLog();
					log.setSysType(OperateLogConstant.SYS_TYPE_ADMIN);
					log.setSysUserId(sysUserId);
					log.setThirdId(a.getId());
					log.setType(OperateLogConstant.TYPE_ADMIN_APPEAL_DEAL);
					log.setInfo("订单:" + a.getSerial() + "审核处理");
					log.setValid(CommonConstant.INT_BOOLEAN_TRUE);
					log.setUpdateDate(now);
					log.setCreateDate(now);
					operateLogs.add(log);
				}
			}
			amuseRewardProgressService.save(ps);
			amuseVerifyService.save(verifys);
			operateLogService.save(operateLogs);
		}

		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}

	/**
	 * 手动分配申诉任务
	 */
	@ResponseBody
	@RequestMapping("allot")
	public JsonResponseMsg allot(String[] id, String sysUserId) {
		JsonResponseMsg result = new JsonResponseMsg();

		if (ArrayUtils.isEmpty(id) || !NumberUtils.isNumber(sysUserId)) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}

		List<Long> ids = new ArrayList<Long>();
		if (ArrayUtils.isNotEmpty(id)) {
			for (String idStr : id) {
				if (NumberUtils.isNumber(idStr)) {
					ids.add(NumberUtils.toLong(idStr));
				}
			}
		}

		Long sysUserIdLong = NumberUtils.toLong(sysUserId);
		amuseAppealService.changeState(ids, AmuseConstant.APPEAL_STATE_INAPPEAL, sysUserIdLong, null);

		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}
}
