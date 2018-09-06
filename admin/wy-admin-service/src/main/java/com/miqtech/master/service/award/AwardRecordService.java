package com.miqtech.master.service.award;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.miqtech.master.consts.AmuseConstant;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.RedbagConstant;
import com.miqtech.master.consts.award.AwardConstant;
import com.miqtech.master.consts.mall.CoinConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.award.AwardRecordDao;
import com.miqtech.master.entity.amuse.AmuseActivityInfo;
import com.miqtech.master.entity.amuse.AmuseActivityRecord;
import com.miqtech.master.entity.amuse.AmuseVerify;
import com.miqtech.master.entity.award.AwardCommodity;
import com.miqtech.master.entity.award.AwardInventory;
import com.miqtech.master.entity.award.AwardRecord;
import com.miqtech.master.entity.mall.CoinHistory;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.entity.user.UserRedbag;
import com.miqtech.master.service.amuse.AmuseActivityInfoService;
import com.miqtech.master.service.amuse.AmuseActivityRecordService;
import com.miqtech.master.service.amuse.AmuseVerifyService;
import com.miqtech.master.service.mall.CoinHistoryService;
import com.miqtech.master.service.system.SystemRedbagService;
import com.miqtech.master.service.user.UserInfoService;
import com.miqtech.master.service.user.UserRedbagService;
import com.miqtech.master.thirdparty.service.juhe.JuheFlow;
import com.miqtech.master.thirdparty.service.juhe.JuheGameCharge;
import com.miqtech.master.thirdparty.service.juhe.JuheTelCharge;
import com.miqtech.master.thirdparty.util.SMSMessageUtil;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;

@Component
public class AwardRecordService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AwardRecordService.class);
	private static ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private AwardRecordDao awardRecordDao;
	@Autowired
	private UserInfoService userInfoService;
	@Autowired
	private CoinHistoryService coinHistoryService;
	@Autowired
	private SystemRedbagService systemRedbagService;
	@Autowired
	private UserRedbagService userRedbagService;
	@Autowired
	private AwardCommodityService awardCommodityService;
	@Autowired
	private AmuseActivityInfoService amuseActivityInfoService;
	@Autowired
	private AwardInventoryService awardInventoryService;
	@Autowired
	private AmuseActivityRecordService amuseActivityRecordService;
	@Autowired
	private AmuseVerifyService amuseVerifyService;

	public List<AwardRecord> findValidByUserIdIn(List<Long> userIds) {
		if (CollectionUtils.isNotEmpty(userIds)) {
			return awardRecordDao.findByUserIdInAndValid(userIds, CommonConstant.INT_BOOLEAN_TRUE);
		}
		return null;
	}

	/**
	 * 保存发放记录(批量)
	 */
	public List<AwardRecord> save(List<AwardRecord> rs) {
		return (List<AwardRecord>) awardRecordDao.save(rs);
	}

	/**
	 * 发放
	 */
	public void grant(List<AwardRecord> records, Long sysUserId, Long inventoryId) {
		if (CollectionUtils.isNotEmpty(records)) {
			// 根据发放类型，分类任务
			List<Long> coinUserIds = Lists.newArrayList();
			List<AwardRecord> ownCoinRecords = Lists.newArrayList();
			List<AwardRecord> ownRedbagRecords = Lists.newArrayList();
			List<AwardRecord> repertoryRecords = Lists.newArrayList();
			List<AwardRecord> rechargeRecords = Lists.newArrayList();
			for (AwardRecord r : records) {
				Integer type = r.getType();
				Integer subType = r.getSubType();
				Double amount = r.getAmount();

				// 根据类型归类
				if (type != null && subType != null && amount != null && r.getSourceType() != null
						&& r.getSourceTargetId() != null && r.getUserId() != null) {// 校验必须字段
					if (AwardConstant.TYPE_OWN.equals(type) && (AwardConstant.SUB_TYPE_OWN_COIN.equals(subType)
							|| AwardConstant.SUB_TYPE_OWN_REDBAG.equals(subType))) {// 自有类型
						r.setStatus(AwardConstant.STATUS_SUCCESS);
						r.setChecked(CommonConstant.INT_BOOLEAN_FALSE);
						if (AwardConstant.SUB_TYPE_OWN_COIN.equals(subType)) {
							coinUserIds.add(r.getUserId());
							ownCoinRecords.add(r);
						} else {
							ownRedbagRecords.add(r);
						}
					} else if (AwardConstant.TYPE_REPERTORY.equals(type)) {
						r.setStatus(AwardConstant.STATUS_SUCCESS);
						r.setChecked(CommonConstant.INT_BOOLEAN_FALSE);
						repertoryRecords.add(r);
					} else if (AwardConstant.TYPE_RECHARGE.equals(type)) {
						r.setStatus(AwardConstant.STATUS_COMMIT);
						r.setChecked(CommonConstant.INT_BOOLEAN_FALSE);
						rechargeRecords.add(r);
					}
				}
			}

			// 发放各类型
			dealCoinRecords(ownCoinRecords, coinUserIds, sysUserId);
			dealRedbagRecords(ownRedbagRecords, sysUserId);
			dealRepertoryRecords(repertoryRecords, sysUserId, inventoryId);
			dealRechargeRecords(rechargeRecords, sysUserId);
		}
	}

	/**
	 * 处理 自有金币 发放任务
	 */
	private void dealCoinRecords(List<AwardRecord> coinRecords, List<Long> coinUserIds, Long sysUserId) {
		if (CollectionUtils.isNotEmpty(coinRecords)) {
			// 查询并更新用户信息
			List<AwardRecord> dealRecords = Lists.newArrayList();// 成功操作的记录
			List<UserInfo> coinUsers = userInfoService.findValidByIds(coinUserIds);
			for (AwardRecord r : coinRecords) {
				// 增加用户金币
				if (CollectionUtils.isNotEmpty(coinUsers)) {
					for (UserInfo u : coinUsers) {
						if (r.getUserId().equals(u.getId())) {
							u.setCoin(u.getCoin() + r.getAmount().intValue());
							dealRecords.add(r);
							break;
						}
					}
				}
			}

			if (CollectionUtils.isNotEmpty(dealRecords)) {
				// 保存用户信息
				coinUsers = userInfoService.save(coinUsers);

				// 保存发放记录
				dealRecords = save(dealRecords);

				// 产生金币来源记录
				List<CoinHistory> historys = Lists.newArrayList();
				Date now = new Date();
				for (AwardRecord r : dealRecords) {
					CoinHistory h = new CoinHistory();
					h.setUserId(r.getUserId());
					h.setType(CoinConstant.HISTORY_TYPE_AWARD);
					h.setTargetId(r.getId());
					h.setCoin(r.getAmount().intValue());
					h.setDirection(CoinConstant.HISTORY_DIRECTION_INCOME);
					h.setValid(CommonConstant.INT_BOOLEAN_TRUE);
					h.setUpdateUserId(sysUserId);
					h.setCreateUserId(sysUserId);
					h.setUpdateDate(now);
					h.setCreateDate(now);
					historys.add(h);
				}
				historys = coinHistoryService.save(historys);

				// 更新发放记录的targetId
				for (AwardRecord r : dealRecords) {
					for (CoinHistory h : historys) {
						if (r.getId().equals(h.getTargetId())) {
							r.setTargetId(h.getId());
							break;
						}
					}
				}
				dealRecords = save(dealRecords);
			}
		}
	}

	/**
	 * 处理 自有红包 发放任务
	 */
	private void dealRedbagRecords(List<AwardRecord> redbagRecords, Long sysUserId) {
		if (CollectionUtils.isNotEmpty(redbagRecords)) {
			Map<String, Object> systemRedbag = systemRedbagService.findOneByType(RedbagConstant.REDBAG_TYPE_AWARD);
			Long sysRedbagId = MapUtils.getLong(systemRedbag, "id");
			if (sysRedbagId != null) {
				redbagRecords = save(redbagRecords);

				Date now = new Date();
				List<UserRedbag> redbags = new ArrayList<UserRedbag>();
				for (AwardRecord r : redbagRecords) {
					UserRedbag redbag = new UserRedbag();
					redbag.setUserId(r.getUserId());
					redbag.setRedbagId(sysRedbagId);
					redbag.setUsable(CommonConstant.INT_BOOLEAN_TRUE);
					redbag.setAmount(r.getAmount().intValue());
					redbag.setNetbarType(1);
					redbag.setNetbarId(0L);
					redbag.setValid(CommonConstant.INT_BOOLEAN_TRUE);
					redbag.setUpdateDate(now);
					redbag.setCreateDate(now);
					redbag.setUpdateUserId(r.getId());
					redbags.add(redbag);
				}
				redbags = userRedbagService.save(redbags);

				// 更新发放记录的targetId
				for (AwardRecord r : redbagRecords) {
					for (UserRedbag redbag : redbags) {
						if (r.getId().equals(redbag.getUpdateUserId())) {
							r.setTargetId(redbag.getId());
						}
					}
				}
				redbagRecords = save(redbagRecords);

				// 重置updateUserId
				for (UserRedbag r : redbags) {
					r.setUpdateUserId(null);
				}
				redbags = userRedbagService.save(redbags);
			}
		}
	}

	/**
	 * 处理 库存 发放任务
	 */
	private void dealRepertoryRecords(List<AwardRecord> repertoryRecords, Long sysUserId, Long inventoryId) {
		if (CollectionUtils.isNotEmpty(repertoryRecords) && inventoryId != null) {
			// 处理娱乐赛库存发放任务
			List<AwardRecord> amuseRecords = Lists.newArrayList();
			for (AwardRecord r : repertoryRecords) {
				if (AwardConstant.SOURCE_TYPE_AMUSE.equals(r.getSourceType())) {
					amuseRecords.add(r);
				}
			}

			// 发放
			List<AwardCommodity> cdkeys = awardCommodityService.findUsefullyByInventoryId(inventoryId);
			if (CollectionUtils.isNotEmpty(amuseRecords) && CollectionUtils.isNotEmpty(cdkeys)) {
				// 查询商品信息
				AwardInventory inventory = awardInventoryService.findValidById(inventoryId);
				if (inventory == null) {
					return;
				}

				amuseRecords = save(amuseRecords);

				// 查询 娱乐赛信息 及 用户信息，用于匹配通知内容
				List<Long> activityIds = Lists.newArrayList();
				List<Long> userIds = Lists.newArrayList();
				for (AwardRecord r : amuseRecords) {
					activityIds.add(r.getSourceTargetId());
					userIds.add(r.getUserId());
				}
				List<AmuseActivityInfo> activities = amuseActivityInfoService.findValidByIds(activityIds);
				List<UserInfo> users = userInfoService.findValidByIds(userIds);
				if (CollectionUtils.isEmpty(activities) || CollectionUtils.isEmpty(users)) {
					return;
				}

				List<AwardCommodity> dealCdkeys = Lists.newArrayList();
				Date now = new Date();
				for (AwardRecord r : amuseRecords) {
					AmuseActivityInfo activity = null;
					UserInfo user = null;
					for (AmuseActivityInfo a : activities) {
						if (r.getSourceTargetId().equals(a.getId())) {
							activity = a;
							break;
						}
					}
					for (UserInfo u : users) {
						if (r.getUserId().equals(u.getId())) {
							user = u;
							break;
						}
					}
					if (activity == null || user == null) {// 找不到赛事信息 或 用户信息，无法继续发放
						continue;
					}

					// 一次发放多个库存商品
					Integer amount = r.getAmount().intValue();
					int cdkeyIndex = 0;
					for (Iterator<AwardCommodity> it = cdkeys.iterator(); it.hasNext();) {
						AwardCommodity cdkey = it.next();
						if (!CommonConstant.INT_BOOLEAN_TRUE.equals(cdkey.getIsUsed())) {// 未使用
							cdkey.setIsUsed(CommonConstant.INT_BOOLEAN_TRUE);
							cdkey.setUsedTime(now);
							cdkey.setAwardRecordId(r.getId());
							dealCdkeys.add(cdkey);
							it.remove();

							// 更新奖品记录的targetId为最后一个商品的ID
							r.setTargetId(cdkey.getId());

							// 发送通知
							final String[] phoneNum = { user.getUsername() };
							final String[] params = { activity.getTitle(), inventory.getName(), cdkey.getCdkey() };
							pool.execute(() -> {
								try {
									SMSMessageUtil.sendTemplateMessage(phoneNum, "8121", params);
								} catch (Exception e) {
									LOGGER.error("短信发送异常:", e);
								}
							});

							cdkeyIndex += 1;
							if (cdkeyIndex >= amount) {
								break;
							}
						}
					}
				}

				// 保存 商品 及 更新后的奖品记录
				awardCommodityService.save(dealCdkeys);
				amuseRecords = save(amuseRecords);
			}
		}
	}

	/**
	 * 发放第三方充值类奖品
	 */
	public void dealRechargeRecords(List<AwardRecord> rechargeRecords, Long sysUserId) {
		if (CollectionUtils.isNotEmpty(rechargeRecords)) {
			// 查询报名信息,作为发放帐号
			List<Long> amuseActivityIds = Lists.newArrayList();
			List<Long> amuseUserIds = Lists.newArrayList();
			for (AwardRecord r : rechargeRecords) {
				if (AwardConstant.SOURCE_TYPE_AMUSE.equals(r.getSourceType())) {// 娱乐赛发放类型
					Long activityId = r.getSourceTargetId();
					Long userId = r.getUserId();
					amuseActivityIds.add(activityId);
					amuseUserIds.add(userId);
				}
			}
			List<AmuseActivityRecord> records = amuseActivityRecordService
					.findGrantByActivityIdInAndUserIdIn(amuseActivityIds, amuseUserIds);
			List<AmuseVerify> verifies = amuseVerifyService.findValidByActivityIdInAndUserIdInAndState(amuseActivityIds,
					amuseUserIds, AmuseConstant.VERIFY_STATE_NOGIVE);

			// 匹配娱乐赛报名信息,并发放
			if (CollectionUtils.isNotEmpty(records) && CollectionUtils.isNotEmpty(verifies)) {
				rechargeRecords = save(rechargeRecords);
				for (AwardRecord r : rechargeRecords) {
					if (!AwardConstant.SOURCE_TYPE_AMUSE.equals(r.getSourceType())) {
						continue;
					}

					// 匹配认证信息
					AmuseVerify verify = null;
					for (AmuseVerify v : verifies) {
						if (r.getSourceTargetId().equals(v.getActivityId()) && r.getUserId().equals(v.getUserId())) {
							verify = v;
							break;
						}
					}
					if (verify == null) {
						continue;
					}

					// 匹配报名信息
					AmuseActivityRecord activityRecord = null;
					for (AmuseActivityRecord aar : records) {
						if (r.getSourceTargetId().equals(aar.getActivityId())
								&& r.getUserId().equals(aar.getUserId())) {
							activityRecord = aar;
							break;
						}
					}
					if (activityRecord == null) {
						continue;
					}

					// 匹配类型,并发放
					Integer subType = r.getSubType();
					try {
						if (AwardConstant.SUB_TYPE_RECHARGE_FLOW.equals(subType)) {
							String telephone = activityRecord.getTelephone();
							Double amount = r.getAmount();
							if (StringUtils.isNotBlank(telephone)) {
								// 接口充值
								Map<String, String> params = Maps.newHashMap();
								params.put("phone", telephone);
								params.put("quota", String.valueOf(amount.intValue()));
								params.put("orderid", verify.getSerial());
								String result = JuheFlow.flowCharge(params);

								// 更新充值状态
								JSONObject object = new Gson().fromJson(result, JSONObject.class);
								if (object != null) {
									// 记录请求结果
									String reason = object.getString("reason");
									r.setRemark(reason);

									Integer code = object.getInteger("error_code");
									if (code != null && code.equals(0)) {
										r.setStatus(AwardConstant.STATUS_SUCCESS);
										LOGGER.error("充值:" + verify.getSerial() + ";为帐号:" + telephone + "成功充值" + amount
												+ "M流量");
									} else {
										r.setStatus(AwardConstant.STATUS_MSG_FAIL);
										LOGGER.error("充值:" + verify.getSerial() + ";充值失败:" + result);
									}
								} else {
									LOGGER.error("充值:" + verify.getSerial() + ";充值失败:" + result);
								}
							}
						} else if (AwardConstant.SUB_TYPE_RECHARGE_QQ_COIN.equals(subType)) {
							String qq = activityRecord.getQq();
							Double amount = r.getAmount();
							Map<String, String> params = Maps.newHashMap();
							params.put("cardid", "220612");
							params.put("cardnum", String.valueOf(amount.intValue()));
							params.put("orderid", verify.getSerial());
							params.put("game_userid", qq);
							String result = JuheGameCharge.order(params);

							// 更新充值状态
							JSONObject object = new Gson().fromJson(result, JSONObject.class);
							if (object != null) {
								// 记录请求结果
								String reason = object.getString("reason");
								r.setRemark(reason);

								Integer code = object.getInteger("error_code");
								if (code != null && code.equals(0)) {
									r.setStatus(AwardConstant.STATUS_SUCCESS);
									LOGGER.error("充值:" + verify.getSerial() + ";为帐号:" + qq + "成功充值" + amount + "Q币");
								} else {
									r.setStatus(AwardConstant.STATUS_MSG_FAIL);
									LOGGER.error("充值:" + verify.getSerial() + ";充值失败:" + result);
								}
							} else {
								LOGGER.error("充值:" + verify.getSerial() + ";充值失败:" + result);
							}
						} else if (AwardConstant.SUB_TYPE_RECHARGE_TELEPHOEN.equals(subType)) {
							String telephone = activityRecord.getTelephone();
							Double amount = r.getAmount();
							String result = null;
							try {
								result = JuheTelCharge.onlineOrder(telephone, amount.intValue(), verify.getSerial());
							} catch (Exception e) {
								LOGGER.error("聚合平台话费充值接口异常：", e);
							}

							// 更新充值状态
							JSONObject object = new Gson().fromJson(result, JSONObject.class);
							if (object != null) {
								// 记录请求结果
								String reason = object.getString("reason");
								r.setRemark(reason);

								Integer code = object.getInteger("error_code");
								if (code != null && code.equals(0)) {
									r.setStatus(AwardConstant.STATUS_SUCCESS);
									LOGGER.error(
											"充值:" + verify.getSerial() + ";为帐号:" + telephone + "成功充值" + amount + "元话费");
								} else {
									r.setStatus(AwardConstant.STATUS_MSG_FAIL);
									LOGGER.error("充值:" + verify.getSerial() + ";充值失败:" + result);
								}
							} else {
								LOGGER.error("充值:" + verify.getSerial() + ";充值失败:" + result);
							}
						}
					} catch (Exception e) {
						LOGGER.error("第三方充值异常:", e);
					}
				}
			}
		}
	}

	/**
	 * 分页
	 */
	public PageVO page(int page, Map<String, String> searchParams) {
		String condition = " WHERE ar.is_valid = 1 AND ar.source_type = 1";
		String totalCondition = condition;
		String exCondition = " WHERE ar.is_valid = 1";
		String exTotalCondition = exCondition;
		Map<String, Object> params = Maps.newHashMap();

		String account = MapUtils.getString(searchParams, "account");
		if (StringUtils.isNotBlank(account)) {
			String likeAccount = "%" + account + "%";
			exCondition = SqlJoiner.join(exCondition, " AND (aar.telephone LIKE :account OR aar.qq LIKE :account)");
			params.put("account", likeAccount);
			exTotalCondition = SqlJoiner.join(exTotalCondition, " AND (aar.telephone LIKE '", likeAccount,
					"' OR aar.qq LIKE '", likeAccount, "')");
		}
		String activityId = MapUtils.getString(searchParams, "activityId");
		if (NumberUtils.isNumber(activityId)) {
			condition = SqlJoiner.join(condition, " AND ar.source_target_id = ", activityId);
			totalCondition = SqlJoiner.join(totalCondition, " AND ar.source_target_id = ", activityId);
		}
		String startDate = MapUtils.getString(searchParams, "startDate");
		if (StringUtils.isNotBlank(startDate)) {
			condition = SqlJoiner.join(condition, " AND ar.create_date >= :startDate");
			params.put("startDate", startDate);
			totalCondition = SqlJoiner.join(totalCondition, " AND ar.create_date >= '", startDate, "'");
		}
		String endDate = MapUtils.getString(searchParams, "endDate");
		if (StringUtils.isNotBlank(endDate)) {
			condition = SqlJoiner.join(condition, " AND ar.create_date < ADDDATE(:endDate, INTERVAL 1 DAY)");
			params.put("endDate", endDate);
			totalCondition = SqlJoiner.join(totalCondition, " AND ar.create_date < ADDDATE('", endDate,
					"', INTERVAL 1 DAY)");
		}
		String type = MapUtils.getString(searchParams, "type");
		if (NumberUtils.isNumber(type)) {
			condition = SqlJoiner.join(condition, " AND ar.type = ", type);
			totalCondition = SqlJoiner.join(totalCondition, " AND ar.type = ", type);
		}
		String subType = MapUtils.getString(searchParams, "subType");
		if (NumberUtils.isNumber(subType)) {
			condition = SqlJoiner.join(condition, " AND ar.sub_type = ", subType);
			totalCondition = SqlJoiner.join(totalCondition, " AND ar.sub_type = ", subType);
		}

		// 排序
		String order = " ORDER BY ar.create_date DESC";

		// 分页
		String limit = "";
		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page > 0) {
			Integer startRow = (page - 1) * pageSize;
			limit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());
		}

		String sql = SqlJoiner.join("SELECT ar.id, av.serial, aar.telephone, aar.qq, u.username,",
				" aa.title activityTitle, ar.type awardType, ar.sub_type awardSubType, ar.amount, ar.create_date createDate, mcc.name awardTypeName",
				" FROM (SELECT * FROM award_t_record ar", condition, ") ar",
				" JOIN amuse_t_verify av ON ar.source_target_id = av.activity_id AND ar.user_id = av.user_id",
				" JOIN amuse_r_activity_record aar ON ar.source_target_id = aar.activity_id AND ar.user_id = aar.user_id",
				" JOIN user_t_info u ON ar.user_id = u.id", " JOIN amuse_t_activity aa ON ar.source_target_id = aa.id",
				" LEFT JOIN mall_t_commodity_category mcc ON ar.type = 3 AND ar.sub_type = mcc.id", exCondition,
				" GROUP BY ar.id", order, limit);
		List<Map<String, Object>> list = queryDao.queryMap(sql, params);

		String totalSql = SqlJoiner.join("SELECT COUNT(1) FROM (SELECT 1", " FROM (SELECT * FROM award_t_record ar",
				totalCondition, ") ar",
				" JOIN amuse_t_verify av ON ar.source_target_id = av.activity_id AND ar.user_id = av.user_id",
				" JOIN amuse_r_activity_record aar ON ar.source_target_id = aar.activity_id AND ar.user_id = aar.user_id",
				" JOIN user_t_info u ON ar.user_id = u.id", " JOIN amuse_t_activity aa ON ar.source_target_id = aa.id",
				exTotalCondition, " GROUP BY ar.id", order, ") T");
		Number total = queryDao.query(totalSql);
		if (total == null) {
			total = 0;
		}

		PageVO vo = new PageVO();
		vo.setList(list);
		vo.setTotal(total.intValue());
		int isLast = total.intValue() > page * pageSize ? 1 : 0;
		vo.setIsLast(isLast);
		return vo;
	}
}
