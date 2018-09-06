package com.miqtech.master.admin.web.controller.backend.amuse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
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
import com.miqtech.master.entity.amuse.AmuseActivityRecord;
import com.miqtech.master.entity.amuse.AmuseRewardProgress;
import com.miqtech.master.entity.amuse.AmuseVerify;
import com.miqtech.master.entity.award.AwardRecord;
import com.miqtech.master.entity.common.OperateLog;
import com.miqtech.master.entity.common.SystemUser;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.service.amuse.AmuseRewardProgressService;
import com.miqtech.master.service.amuse.AmuseVerifyService;
import com.miqtech.master.service.award.AwardCommodityService;
import com.miqtech.master.service.award.AwardRecordService;
import com.miqtech.master.service.common.OperateLogService;
import com.miqtech.master.service.system.SystemUserService;
import com.miqtech.master.thirdparty.service.JpushService;
import com.miqtech.master.thirdparty.util.SMSMessageUtil;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

/**
 * 发放管理
 */
@Controller
@RequestMapping("amuse/grant")
public class AmuseGrantController extends BaseController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AmuseVerifyController.class);

	@Autowired
	private AmuseVerifyService amuseVerifyService;
	@Autowired
	private JpushService msgOperateService;
	@Autowired
	private AmuseRewardProgressService amuseRewardProgressService;
	@Autowired
	private SystemUserService systemUserService;
	@Autowired
	private AwardRecordService awardRecordService;
	@Autowired
	private AwardCommodityService awardCommodityService;
	@Autowired
	private OperateLogService operateLogService;

	/**
	 * 发放列表
	 */
	@RequestMapping("list/{page}")
	public ModelAndView list(HttpServletRequest request, @PathVariable("page") int page, String username,
			String beginDate, String endDate, String state, String verifyBeginDate, String verifyEndDate,
			String account) {
		ModelAndView mv = new ModelAndView("amuse/grantList");
		SystemUser sysUser = Servlets.getSessionUser(request);
		Map<String, Object> searchParams = Maps.newHashMap();
		searchParams.put("username", username);
		searchParams.put("beginDate", beginDate);
		searchParams.put("endDate", endDate);
		searchParams.put("state", state);
		searchParams.put("verifyBeginDate", verifyBeginDate);
		searchParams.put("verifyEndDate", verifyEndDate);
		searchParams.put("account", account);
		if (SystemUserConstant.TYPE_AMUSE_ISSUE.equals(sysUser.getUserType())) {
			searchParams.put("sysUserId", sysUser.getId());
		}
		mv.addObject("params", searchParams);
		mv.addObject("loginUserId", sysUser.getId());

		PageVO vo = amuseVerifyService.grantPage(page, searchParams);
		pageModels(mv, vo.getList(), page, vo.getTotal());

		List<SystemUser> grantUsers = systemUserService.findValidByUserType(SystemUserConstant.TYPE_AMUSE_ISSUE);
		mv.addObject("grantUsers", grantUsers);

		return mv;
	}

	/**
	 * 认领
	 */
	@ResponseBody
	@RequestMapping("/claim/{id}/{loginUserId}")
	public JsonResponseMsg claim(@PathVariable("id") long id, @PathVariable("loginUserId") long loginUserId) {
		JsonResponseMsg result = new JsonResponseMsg();
		AmuseVerify amuseVerify = amuseVerifyService.findValidOne(id);
		if (null != amuseVerify) {
			amuseVerify.setClaimUserId(loginUserId);
			amuseVerifyService.save(amuseVerify);
		}

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}

	/**
	 * 完成发放(只做标记的发放操作)
	 */
	/*@ResponseBody
	@RequestMapping("/handle")
	public JsonResponseMsg handle(HttpServletRequest request, long id, String remark, long loginUserId, String account) {
		JsonResponseMsg result = new JsonResponseMsg();
		AmuseVerify amuseVerify = amuseVerifyService.findValidOne(id);
		if (null == amuseVerify.getClaimUserId()) {
			result.fill(CommonConstant.CODE_ERROR_LOGIC, "请先认领");
			return result;
		} else if (!amuseVerify.getClaimUserId().equals(loginUserId)) {
			result.fill(CommonConstant.CODE_ERROR_LOGIC, "已被他人认领");
			return result;
		}
		if (null != amuseVerify) {
			amuseVerify.setRemark(remark);
			amuseVerify.setState(AmuseConstant.VERIFY_STATE_GIVED); //发放完成
			AmuseVerify v = amuseVerifyService.save(amuseVerify);

			// 保存发放进度
			SystemUser sysUser = Servlets.getSessionUser(request);
			AmuseRewardProgress progress = new AmuseRewardProgress();
			progress.setActivityId(v.getActivityId());
			progress.setUserId(v.getUserId());
			progress.setSysUserId(sysUser.getId());
			progress.setTargetId(v.getId());
			progress.setType(2);
			progress.setState(4);
			progress.setRemark(remark);
			progress.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			progress.setCreateDate(new Date());
			amuseRewardProgressService.save(progress);

			// 推送发放提示消息
			account = null == account ? StringUtils.EMPTY : account;
			AmuseActivityInfo amuse = amuseActivityInfoService.findById(amuseVerify.getActivityId());
			String content = "您参加的" + amuse.getTitle() + "娱乐赛，奖品已发放至" + account
					+ "，请注意查收。若有异议，请在领奖页申诉或APP客服处提出，我们会尽快联系您处理。";
			msgOperateService.notifyMemberAliasMsg(Msg4UserType.AMUSE.ordinal(), amuseVerify.getUserId(),
					MsgConstant.PUSH_MSG_TYPE_AMUSE, "娱乐赛提示信息", content, true, amuse.getId());
		}

		result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
		return result;
	}*/

	/**
	 * 发放(支持批量)
	 */
	@ResponseBody
	@RequestMapping("grant")
	public JsonResponseMsg grant(HttpServletRequest request, String[] id, String inventoryId) {
		JsonResponseMsg result = new JsonResponseMsg();

		// 转化传入id参数为长整型
		List<Long> ids = new ArrayList<Long>();
		if (ArrayUtils.isNotEmpty(id)) {
			for (String idStr : id) {
				if (NumberUtils.isNumber(idStr)) {
					ids.add(NumberUtils.toLong(idStr));
				}
			}
		}
		if (CollectionUtils.isEmpty(ids)) {
			return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
		}

		// 根据认证信息,产生发放内容
		List<AmuseVerify> verifys = amuseVerifyService.findValidByIdsWithActivityAndUserAndRecord(ids);
		if (CollectionUtils.isNotEmpty(verifys)) {
			List<AmuseVerify> dealVerifys = Lists.newArrayList();
			List<AmuseRewardProgress> dealProgress = Lists.newArrayList();
			List<AwardRecord> records = Lists.newArrayList();
			List<OperateLog> operateLogs = Lists.newArrayList();
			List<Map<String, Object>> pushMsgs = Lists.newArrayList();
			SystemUser sysUser = Servlets.getSessionUser(request);
			Long sysUserId = sysUser.getId();
			Date now = new Date();
			Integer operateAwardType = null;
			Integer operateAmount = 0;
			for (AmuseVerify v : verifys) {
				// 判断是否在发放状态
				if (!AmuseConstant.VERIFY_STATE_NOGIVE.equals(v.getState())
						&& !AmuseConstant.VERIFY_STATE_APPEAL_PASSED.equals(v.getState())) {
					return result.fill(CommonConstant.CODE_ERROR_LOGIC, "订单:" + v.getSerial() + "当前不处于待发放状态");
				}

				// 匹配奖品类型
				AmuseActivityInfo activity = v.getActivityInfo();
				Integer awardType = activity.getAwardType();
				Integer awardSubType = activity.getAwardSubType();
				Integer awardAmount = activity.getAwardAmount();
				if (activity == null || awardType == null || awardSubType == null || awardAmount == null) {
					return result.fill(CommonConstant.CODE_ERROR_LOGIC, "订单:" + v.getSerial() + "的活动信息不完整");
				}
				operateAmount += awardAmount;

				// 检查不同的商品类别不可批量发放
				if (operateAwardType == null) {
					operateAwardType = awardType;
				}

				// 记录自有类型的发放记录 并 发放
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
				records.add(r);

				// 修改认证状态为已发放
				v.setState(AmuseConstant.VERIFY_STATE_GIVED);
				dealVerifys.add(v);

				// 记录认证状态
				Long activityId = v.getActivityId();
				Long userId = v.getUserId();
				AmuseRewardProgress progress = new AmuseRewardProgress();
				progress.setActivityId(activityId);
				progress.setUserId(userId);
				progress.setSysUserId(sysUserId);
				progress.setTargetId(v.getId());
				progress.setType(1);
				progress.setState(4);
				progress.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				progress.setCreateDate(now);
				dealProgress.add(progress);

				// 发送系统推送
				String grantMsg = activity.getGrantMsg();
				if (StringUtils.isNotBlank(grantMsg)) {
					Map<String, Object> pushMsg = Maps.newHashMap();
					pushMsg.put("msg", grantMsg);
					pushMsg.put("userId", userId);
					pushMsg.put("activityId", activityId);
					pushMsgs.add(pushMsg);
				}

				// 发送短信通知
				if (AwardConstant.TYPE_OWN.equals(awardType) && (AwardConstant.SUB_TYPE_OWN_COIN.equals(awardSubType)
						|| AwardConstant.SUB_TYPE_OWN_REDBAG.equals(awardSubType)) && awardAmount != null) {// 自有商品
					// 组装奖品内容
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
						grantLog.setType(OperateLogConstant.TYPE_ADMIN_VERIFY_REFUSE);
						grantLog.setInfo("订单:" + v.getSerial() + "发放" + award);
						grantLog.setValid(CommonConstant.INT_BOOLEAN_TRUE);
						grantLog.setUpdateDate(now);
						grantLog.setCreateDate(now);
						operateLogs.add(grantLog);
					}
				} else if (AwardConstant.TYPE_RECHARGE.equals(awardType)
						&& (AwardConstant.SUB_TYPE_RECHARGE_FLOW.equals(awardSubType)
								|| AwardConstant.SUB_TYPE_RECHARGE_QQ_COIN.equals(awardSubType)
								|| AwardConstant.SUB_TYPE_RECHARGE_TELEPHOEN.equals(awardSubType))) {// 第三方充值
					// 组装奖品内容 并 检查报名信息是否完善
					AmuseActivityRecord record = v.getActivityRecord();
					if (StringUtils.isBlank(record.getTelephone())) {
						return result.fill(CommonConstant.CODE_ERROR_LOGIC, "订单:" + v.getSerial() + "的报名信息不完善(缺少手机号码)");
					}
					String award = StringUtils.EMPTY;
					String telephone = record.getTelephone();
					String account = StringUtils.EMPTY;
					if (AwardConstant.SUB_TYPE_RECHARGE_FLOW.equals(awardSubType)) {
						award = awardAmount + "M流量";
						account = telephone;
					} else if (AwardConstant.SUB_TYPE_RECHARGE_QQ_COIN.equals(awardSubType)) {
						if (StringUtils.isBlank(record.getQq())) {
							return result.fill(CommonConstant.CODE_ERROR_LOGIC,
									"订单:" + v.getSerial() + "的报名信息不完善(缺少QQ号码)");
						}
						award = awardAmount + "Q币";
						account = record.getQq();
					} else if (AwardConstant.SUB_TYPE_RECHARGE_TELEPHOEN.equals(awardSubType)) {
						award = awardAmount + "元话费";
						account = telephone;
					}

					try {
						String[] phoneNum = { telephone };
						String[] params = { activity.getTitle(), award, account };
						SMSMessageUtil.sendTemplateMessage(phoneNum, "6183", params);
					} catch (Exception e) {
						LOGGER.error("短信发送异常:", e);
					}

					// 后台日志
					OperateLog grantLog = new OperateLog();
					grantLog.setSysType(OperateLogConstant.SYS_TYPE_ADMIN);
					grantLog.setSysUserId(sysUserId);
					grantLog.setThirdId(v.getId());
					grantLog.setType(OperateLogConstant.TYPE_ADMIN_VERIFY_REFUSE);
					grantLog.setInfo("订单:" + v.getSerial() + "发放" + award);
					grantLog.setValid(CommonConstant.INT_BOOLEAN_TRUE);
					grantLog.setUpdateDate(now);
					grantLog.setCreateDate(now);
					operateLogs.add(grantLog);
				} else {
					// 后台日志
					OperateLog grantLog = new OperateLog();
					grantLog.setSysType(OperateLogConstant.SYS_TYPE_ADMIN);
					grantLog.setSysUserId(sysUserId);
					grantLog.setThirdId(v.getId());
					grantLog.setType(OperateLogConstant.TYPE_ADMIN_VERIFY_REFUSE);
					grantLog.setInfo("订单:" + v.getSerial() + "已发放" + activity.getAwardAmount() + "个库存商品");
					grantLog.setValid(CommonConstant.INT_BOOLEAN_TRUE);
					grantLog.setUpdateDate(now);
					grantLog.setCreateDate(now);
					operateLogs.add(grantLog);
				}
			}

			// 库存类 检查 商品可用余量
			Long inventoryIdLong = null;
			if (AwardConstant.TYPE_REPERTORY.equals(operateAwardType)) {
				if (!NumberUtils.isNumber(inventoryId)) {
					return result.fill(CommonConstant.CODE_ERROR_PARAM, CommonConstant.MSG_ERROR_PARAM);
				}

				inventoryIdLong = NumberUtils.toLong(inventoryId);
				List<Map<String, Object>> cdkeys = awardCommodityService
						.queryUsefullyCdkeysByInventoryId(inventoryIdLong);
				if (CollectionUtils.isNotEmpty(records)
						&& (CollectionUtils.isEmpty(cdkeys) || cdkeys.size() < operateAmount)) {
					return result.fill(CommonConstant.CODE_ERROR_LOGIC, "奖品数量不足");
				}
			}

			// 发送系统推送
			try {
				if (CollectionUtils.isNotEmpty(pushMsgs)) {
					for (Map<String, Object> push : pushMsgs) {
						String msg = MapUtils.getString(push, "msg");
						Long userId = MapUtils.getLong(push, "userId");
						Long activityId = MapUtils.getLong(push, "activityId");
						msgOperateService.notifyMemberAliasMsg(Msg4UserType.COMMENT.ordinal(), userId,
								MsgConstant.PUSH_MSG_TYPE_COMMENT, "娱乐赛奖品发放", msg, true, activityId);
					}
				}
			} catch (Exception e) {
				LOGGER.error("发送系统推送异常:", e);
			}

			// 发放 及 相关操作
			awardRecordService.grant(records, sysUserId, inventoryIdLong);
			amuseVerifyService.save(dealVerifys);
			amuseRewardProgressService.save(dealProgress);
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

		List<Long> ids = new ArrayList<Long>();
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
		amuseVerifyService.changeClaimUserId(ids, sysUserIdLong);

		return result.fill(CommonConstant.CODE_SUCCESS, CommonConstant.MSG_SUCCESS);
	}
}
