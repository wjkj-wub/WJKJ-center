package com.miqtech.master.service.amuse;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.consts.AmuseConstant;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.SystemUserConstant;
import com.miqtech.master.consts.UserConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.amuse.AmuseRewardProgressDao;
import com.miqtech.master.dao.amuse.AmuseVerifyDao;
import com.miqtech.master.entity.amuse.AmuseActivityInfo;
import com.miqtech.master.entity.amuse.AmuseActivityRecord;
import com.miqtech.master.entity.amuse.AmuseRewardProgress;
import com.miqtech.master.entity.amuse.AmuseVerify;
import com.miqtech.master.entity.amuse.AmuseVerifyImg;
import com.miqtech.master.entity.user.UserBlack;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.service.user.UserBlackService;
import com.miqtech.master.service.user.UserInfoService;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

/**
 * 娱乐赛审核service
 */
@Component
public class AmuseVerifyService {
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private AmuseVerifyDao amuseVerifyDao;
	@Autowired
	private AmuseRewardProgressDao amuseRewardProgressDao;
	@Autowired
	private AmuseVerifyImgService amuseVerifyImgService;
	@Autowired
	private AmuseActivityInfoService amuseActivityInfoService;
	@Autowired
	private UserInfoService userInfoService;
	@Autowired
	private AmuseActivityRecordService amuseActivityRecordService;
	@Autowired
	private UserBlackService userBlackService;

	public List<AmuseVerify> findByActivityIdAndUserIdAndValid(Long activityId, Long userId, Integer valid) {
		return amuseVerifyDao.findByActivityIdAndUserIdAndValid(activityId, userId, valid);
	}

	public List<AmuseVerify> findValidByActivityIdAndUserIdWithActivityAndUser(Long activityId, Long userId) {
		List<AmuseVerify> verifys = findByActivityIdAndUserIdAndValid(activityId, userId,
				CommonConstant.INT_BOOLEAN_TRUE);
		if (CollectionUtils.isNotEmpty(verifys)) {
			// 查询赛事及用户
			List<Long> activityIds = new ArrayList<Long>();
			List<Long> userIds = new ArrayList<Long>();
			for (AmuseVerify v : verifys) {
				Long aId = v.getActivityId();
				if (aId != null) {
					activityIds.add(aId);
				}
				Long uId = v.getUserId();
				if (uId != null) {
					userIds.add(uId);
				}
			}

			// 匹配认证的赛事信息及用户信息
			List<UserInfo> users = userInfoService.findValidByIds(userIds);
			List<AmuseActivityInfo> activitys = amuseActivityInfoService.findValidByIds(activityIds);
			for (AmuseVerify v : verifys) {
				if (CollectionUtils.isNotEmpty(users)) {
					for (UserInfo u : users) {
						Long uId = v.getUserId();
						if (uId != null && uId.equals(u.getId())) {
							v.setUserInfo(u);
							break;
						}
					}
				}
				if (CollectionUtils.isNotEmpty(activitys)) {
					for (AmuseActivityInfo a : activitys) {
						Long aId = v.getActivityId();
						if (aId != null && aId.equals(a.getId())) {
							v.setActivityInfo(a);
							break;
						}
					}
				}
			}
		}
		return verifys;
	}

	public AmuseVerify findValidOne(Long id) {
		return amuseVerifyDao.findByIdAndValid(id, CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * 查询有效的认证信息，附带认证图片及赛事信息
	 */
	public AmuseVerify findValidOneDetail(Long id) {
		AmuseVerify verify = findValidOne(id);
		if (verify != null) {
			AmuseActivityInfo activity = amuseActivityInfoService.findById(verify.getActivityId());
			verify.setActivityInfo(activity);
			List<AmuseVerifyImg> imgs = amuseVerifyImgService.findByVerifyId(verify.getId());
			verify.setImgs(imgs);
			return verify;
		}
		return null;
	}

	/**
	 * 更新或保存对象，自动初始化好必要属性
	 */
	public AmuseVerify autoSave(AmuseVerify verify) {
		if (verify != null) {
			Date now = new Date();
			verify.setUpdateDate(now);
			if (verify.getId() != null) {
				AmuseVerify old = amuseVerifyDao.findOne(verify.getId());
				if (old != null) {
					verify = BeanUtils.updateBean(old, verify);
				}
			} else {
				verify.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				verify.setCreateDate(now);
			}
			return amuseVerifyDao.save(verify);
		}

		return null;
	}

	/**
	 * 保存
	 */
	public AmuseVerify save(AmuseVerify verify) {
		if (verify != null) {
			return amuseVerifyDao.save(verify);
		}
		return null;
	}

	/**
	 * 批量保存
	 */
	public void save(List<AmuseVerify> vs) {
		if (CollectionUtils.isNotEmpty(vs)) {
			amuseVerifyDao.save(vs);
		}
	}

	/**
	 * 提交审核
	 */
	public AmuseVerify commitVerify(Long activityId, Long userId, String describes, String[] imgs, Long handleUserId) {
		AmuseActivityInfo activity = amuseActivityInfoService.findById(activityId);
		return commitVerify(activity, userId, describes, imgs);
	}

	/**
	 * 提交审核
	 */
	@Transactional
	public AmuseVerify commitVerify(AmuseActivityInfo activity, Long userId, String describes, String[] imgs) {
		if (null == activity || null == activity.getId()) {
			return null;
		}

		// 保存审核信息
		Long activityId = activity.getId();
		AmuseVerify verify = new AmuseVerify();
		verify.setActivityId(activityId);
		verify.setUserId(userId);
		verify.setDescribes(describes);
		Integer serialType = activity.getWay() == 1 ? 2 : 1;
		Integer serialAwardType = activity.getAwardType();
		verify.setSerial(amuseActivityInfoService.genSerial(serialType, serialAwardType));
		// 自动认领
		String lessVerifyUserSql = SqlJoiner
				.join("SELECT u.id, count(v.id) count FROM sys_t_user u LEFT JOIN amuse_t_verify v ON u.id = v.update_user_id WHERE u.user_type = ",
						SystemUserConstant.TYPE_AMUSE_VERIFY.toString(),
						" GROUP BY u.id ORDER BY count ASC, id ASC LIMIT 1");
		Map<String, Object> lessVerifyUser = queryDao.querySingleMap(lessVerifyUserSql);
		boolean isAlloted = false;// 是否已自动分配管理员
		if (MapUtils.isNotEmpty(lessVerifyUser)) {
			Long handleUserId = MapUtils.getLong(lessVerifyUser, "id");
			verify.setState(AmuseConstant.VERIFY_STATE_INVERIFY);
			verify.setUpdateUserId(handleUserId);
			isAlloted = true;
		} else {// 未添加审核管理员
			verify.setState(AmuseConstant.VERIFY_STATE_AWAIT);
		}
		verify = autoSave(verify);
		saveProgress(verify.getId(), activityId, userId, isAlloted);

		// 上传图片
		if (verify != null && ArrayUtils.isNotEmpty(imgs)) {
			List<AmuseVerifyImg> updateImgs = new ArrayList<AmuseVerifyImg>();
			for (String i : imgs) {
				AmuseVerifyImg updateImg = new AmuseVerifyImg();
				updateImg.setImg(i);
				updateImg.setVerifyId(verify.getId());
				updateImgs.add(updateImg);
			}
			amuseVerifyImgService.save(updateImgs);
			verify.setImgs(updateImgs);
		}

		return verify;
	}

	/**
	 * 流程记录
	 */
	public void saveProgress(Long id, Long activityId, Long userId, boolean isAlloted) {
		AmuseRewardProgress progress = new AmuseRewardProgress();
		progress.setActivityId(activityId);
		progress.setUserId(userId);
		progress.setTargetId(id);
		progress.setType(1);
		progress.setState(0);
		progress.setValid(1);
		progress.setCreateDate(new Date());
		amuseRewardProgressDao.save(progress);

		if (isAlloted) {
			try {
				AmuseRewardProgress clone = (AmuseRewardProgress) progress.clone();
				clone.setState(1);
				clone.setCreateDate(new Date());
				clone.setId(null);
				amuseRewardProgressDao.save(clone);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 查找第一条用户在某个活动下的审核记录
	 */
	public AmuseVerify findValidOneByActivityIdAndUserId(Long activityId, Long userId) {
		return findValidOneByActivityIdAndUserId(activityId, userId, false);
	}

	/**
	 * 查找第一条用户在某个活动下的审核记录
	 */
	public AmuseVerify findValidOneByActivityIdAndUserId(Long activityId, Long userId, boolean withImgs) {
		List<AmuseVerify> verifies = amuseVerifyDao.findByActivityIdAndUserIdAndValid(activityId, userId,
				CommonConstant.INT_BOOLEAN_TRUE);

		if (CollectionUtils.isNotEmpty(verifies)) {
			AmuseVerify verify = verifies.get(0);
			if (verify != null && withImgs) {
				List<AmuseVerifyImg> imgs = amuseVerifyImgService.findByVerifyId(verify.getId());
				verify.setImgs(imgs);
			}
			return verify;
		}

		return null;
	}

	/**
	 * 取消用户在某娱乐赛下的所有认证申请
	 */
	public void cancelUserActivityVerify(Long activityId, Long userId) {
		if (activityId != null && userId != null) {
			String sql = SqlJoiner.join("UPDATE amuse_t_verify SET is_valid = ",
					CommonConstant.INT_BOOLEAN_FALSE.toString(), " WHERE activity_id = ", activityId.toString(),
					" AND user_id = ", userId.toString(), " AND is_valid != ",
					CommonConstant.INT_BOOLEAN_FALSE.toString());
			queryDao.update(sql);
		}
	}

	/**
	 * 后台管理：娱乐赛奖品发放列表
	 */
	public PageVO grantPage(int page, Map<String, Object> searchParams) {
		String condition = " WHERE v.is_valid = 1";
		String totalCondition = " WHERE v.is_valid = 1";
		String noGive = String.valueOf(AmuseConstant.VERIFY_STATE_NOGIVE);
		String gived = String.valueOf(AmuseConstant.VERIFY_STATE_GIVED);
		String appealPassed = String.valueOf(AmuseConstant.VERIFY_STATE_APPEAL_PASSED);
		Map<String, Object> params = Maps.newHashMap();
		if (MapUtils.isNotEmpty(searchParams)) {
			// 领奖帐号
			String account = MapUtils.getString(searchParams, "account");
			if (StringUtils.isNotBlank(account)) {
				String likeAccount = "%" + account + "%";
				condition = SqlJoiner.join(condition,
						" AND (r.telephone LIKE :account OR r.qq LIKE :account OR r.game_account LIKE :account)");
				params.put("account", likeAccount);
				totalCondition = SqlJoiner.join(totalCondition, " AND r.telephone LIKE '", likeAccount,
						"' OR r.qq LIKE '", likeAccount, "' OR r.game_account LIKE '", likeAccount, "'");
			}
			String username = MapUtils.getString(searchParams, "username");
			if (StringUtils.isNotBlank(username)) {
				String likeUsername = "%" + username + "%";
				condition = SqlJoiner.join(condition, " AND u.username LIKE :username");
				params.put("username", likeUsername);
				totalCondition = SqlJoiner.join(totalCondition, " AND u.username LIKE '", likeUsername, "'");
			}
			// 提交时间
			String beginDate = MapUtils.getString(searchParams, "beginDate");
			if (StringUtils.isNotBlank(beginDate)) {
				condition = SqlJoiner.join(condition, " AND v.create_date >= :beginDate");
				params.put("beginDate", beginDate);
				totalCondition = SqlJoiner.join(totalCondition, " AND v.create_date >= '", beginDate, "'");
			}
			String endDate = MapUtils.getString(searchParams, "endDate");
			if (StringUtils.isNotBlank(endDate)) {
				condition = SqlJoiner.join(condition, " AND v.create_date < ADDDATE(:endDate, INTERVAL 1 DAY)");
				params.put("endDate", endDate);
				totalCondition = SqlJoiner.join(totalCondition, " AND v.create_date < ADDDATE('", endDate,
						"', INTERVAL 1 DAY)");
			}
			// 审核时间
			String verifyBeginDate = MapUtils.getString(searchParams, "verifyBeginDate");
			if (StringUtils.isNotBlank(verifyBeginDate)) {
				condition = SqlJoiner.join(condition, " AND v.update_date >= :verifyBeginDate");
				params.put("verifyBeginDate", verifyBeginDate);
				totalCondition = SqlJoiner.join(totalCondition, " AND v.update_date >= '", verifyBeginDate, "'");
			}
			String verifyEndDate = MapUtils.getString(searchParams, "verifyEndDate");
			if (StringUtils.isNotBlank(verifyEndDate)) {
				condition = SqlJoiner.join(condition, " AND v.update_date < ADDDATE(:verifyEndDate, INTERVAL 1 DAY)");
				params.put("verifyEndDate", verifyEndDate);
				totalCondition = SqlJoiner.join(totalCondition, " AND v.update_date < ADDDATE('", verifyEndDate,
						"', INTERVAL 1 DAY)");
			}
			String state = MapUtils.getString(searchParams, "state");
			if (NumberUtils.isNumber(state)) {
				if (state.equals(noGive) || state.equals(appealPassed)) {
					condition = SqlJoiner.join(condition, " AND v.state IN ( ", noGive, ", ", appealPassed, ")");
					totalCondition = SqlJoiner.join(totalCondition, " AND v.state IN (", noGive, ", ", appealPassed,
							")");
				} else {
					condition = SqlJoiner.join(condition, " AND v.state = ", state);
					totalCondition = SqlJoiner.join(totalCondition, " AND v.state = ", state);
				}
			} else {
				condition = SqlJoiner.join(condition, " AND v.state in(", noGive, ", ", gived, ", ", appealPassed, ")");
				totalCondition = SqlJoiner.join(totalCondition, " AND v.state in(", noGive, ", ", gived, ", ",
						appealPassed, ")");
			}
			Long sysUserId = MapUtils.getLong(searchParams, "sysUserId");
			if (sysUserId != null) {
				condition = SqlJoiner.join(condition, " AND v.claim_user_id = ", sysUserId.toString());
				totalCondition = SqlJoiner.join(totalCondition, " AND v.claim_user_id = ", sysUserId.toString());
			}
		}

		String limit = "";
		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page > 0) {
			Integer startRow = (page - 1) * pageSize;
			limit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());
		}

		String sql = SqlJoiner
				.join("SELECT v.*, arp.create_date grantDate, mcc.name awardTypeName FROM (",
						"SELECT v.id, v.activity_id activityId, v.serial, v.user_id userId, v.create_date createDate, v.update_date updateDate, v.update_user_id updateUserId, v.remark, v.state",
						", a.title, a.start_date startdate, a.end_date endDate, a.reward, u.username, u.telephone, r.game_account gameAccount, r.telephone applyTel",
						", r.name applyName, r.server, r.qq, r.id_card idCard, r.team_name teamName, v.claim_user_id claimUserId, a.award_type awardType, a.award_sub_type awardSubType, a.award_amount awardAmount",
						" FROM amuse_t_verify v",
						" LEFT JOIN amuse_t_activity a ON v.activity_id = a.id",
						" LEFT JOIN user_t_info u ON v.user_id = u.id",
						" LEFT JOIN amuse_r_activity_record r ON r.user_id = v.user_id and r.activity_id = v.activity_id and r.state=1",
						condition,
						" GROUP BY v.id ORDER BY v.create_date DESC",
						limit,
						") v",
						" LEFT JOIN amuse_reward_progress arp ON v.userId = arp.user_id AND v.activityId = arp.activity_id AND arp.state = 4",
						" LEFT JOIN mall_t_commodity_category mcc ON v.awardType = 3 AND v.awardSubType = mcc.id",
						" GROUP BY v.id ORDER BY v.createDate DESC");
		List<Map<String, Object>> list = queryDao.queryMap(sql, params);

		String totalSql = SqlJoiner
				.join("SELECT COUNT(1) FROM amuse_t_verify v ",
						" LEFT JOIN amuse_t_activity a ON v.activity_id = a.id",
						" LEFT JOIN user_t_info u ON v.user_id = u.id",
						" LEFT JOIN amuse_r_activity_record r ON r.user_id = v.user_id and r.activity_id = v.activity_id and r.state=1",
						totalCondition);
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

	/**
	 * 查看审核列表分页
	 */
	public PageVO page(int page, Map<String, Object> searchParams) {
		String iCondition = " WHERE v.is_valid = 1";
		String eCondition = " WHERE v.is_valid = 1";
		String iTotalCondition = iCondition;
		String eTotalCondition = eCondition;
		Map<String, Object> params = Maps.newHashMap();
		if (MapUtils.isNotEmpty(searchParams)) {
			String username = MapUtils.getString(searchParams, "username");
			if (StringUtils.isNotBlank(username)) {
				String likeUsername = "%" + username + "%";
				params.put("likeUsername", likeUsername);
				eCondition = SqlJoiner.join(eCondition, " AND u.username LIKE :likeUsername");
				eTotalCondition = SqlJoiner.join(eTotalCondition, " AND u.username LIKE '", likeUsername, "'");
			}
			String telephone = MapUtils.getString(searchParams, "telephone");
			if (StringUtils.isNotBlank(telephone)) {
				String likeTelephone = "%" + telephone + "%";
				params.put("liekTelephone", likeTelephone);
				eCondition = SqlJoiner.join(eCondition, " AND ar.telephone LIKE :liekTelephone");
				eTotalCondition = SqlJoiner.join(eTotalCondition, " AND ar.telephone LIKE '", likeTelephone, "'");
			}
			String activityId = MapUtils.getString(searchParams, "activityId");
			if (NumberUtils.isNumber(activityId)) {
				iCondition = SqlJoiner.join(iCondition, " AND v.activity_id = ", activityId);
				iTotalCondition = SqlJoiner.join(iTotalCondition, " AND v.activity_id = ", activityId);
			}
			String beginDate = MapUtils.getString(searchParams, "beginDate");
			if (StringUtils.isNotBlank(beginDate)) {
				iCondition = SqlJoiner.join(iCondition, " AND v.create_date >= :beginDate");
				params.put("beginDate", beginDate);
				iTotalCondition = SqlJoiner.join(iTotalCondition, " AND v.create_date >= '", beginDate, "'");
			}
			String endDate = MapUtils.getString(searchParams, "endDate");
			if (StringUtils.isNotBlank(endDate)) {
				iCondition = SqlJoiner.join(iCondition, " AND v.create_date < ADDDATE(:endDate, INTERVAL 1 DAY)");
				params.put("endDate", endDate);
				iTotalCondition = SqlJoiner.join(iTotalCondition, " AND v.create_date < ADDDATE('", endDate,
						"', INTERVAL 1 DAY)");
			}
			String state = MapUtils.getString(searchParams, "state");
			if (NumberUtils.isNumber(state)) {
				iCondition = SqlJoiner.join(iCondition, " AND v.state = ", state);
				iTotalCondition = SqlJoiner.join(iTotalCondition, " AND v.state = ", state);
			}
			Long userId = MapUtils.getLong(searchParams, "userId");
			if (null != userId) {
				iCondition = SqlJoiner.join(iCondition, " AND v.update_user_id = ", userId.toString());
				iTotalCondition = SqlJoiner.join(iTotalCondition, " AND v.update_user_id = ", userId.toString());
			}
			String isSpecial = MapUtils.getString(searchParams, "isSpecial");
			if (CommonConstant.INT_BOOLEAN_TRUE.toString().equals(isSpecial)) {
				iCondition = SqlJoiner.join(iCondition, " AND v.is_special = 1");
				iTotalCondition = SqlJoiner.join(iTotalCondition, " AND v.is_special = 1");
			}
		}

		String limit = "";
		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page > 0) {
			Integer startRow = (page - 1) * pageSize;
			limit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());
		}

		String sql = SqlJoiner
				.join("SELECT v.id, v.serial, a.title activityName, a.reward activityReward, a.award_type awardType, a.award_sub_type awardSubType,",
						" ar.telephone honoree, ar.qq, ar.game_account gameAccount, u.username, v.state, v.remark, v.is_valid valid, v.update_user_id updateUserId, v.create_date createDate, v.claim_user_id claimUserId",
						" FROM ( SELECT * FROM amuse_t_verify v ",
						iCondition,
						" ) v JOIN user_t_info u ON u.id = v.user_id JOIN amuse_t_activity a ON a.id = v.activity_id",
						" JOIN amuse_r_activity_record ar ON ar.activity_id = v.activity_id AND ar.user_id = v.user_id AND ar.is_valid = 1",
						eCondition, " GROUP BY v.id ORDER BY v.create_date DESC", limit);
		List<Map<String, Object>> list = queryDao.queryMap(sql, params);

		String totalSql = SqlJoiner
				.join("SELECT COUNT(1) count FROM ( SELECT 1 FROM ( SELECT * FROM amuse_t_verify v ",
						iTotalCondition,
						" ) v JOIN user_t_info u ON u.id = v.user_id JOIN amuse_t_activity a ON a.id = v.activity_id",
						" JOIN amuse_r_activity_record ar ON ar.activity_id = v.activity_id AND ar.user_id = v.user_id AND ar.is_valid = 1",
						eTotalCondition, " GROUP BY v.id ) t");
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

	/**
	 * 更改认证状态
	 */
	public void changeState(Long id, Long updateUserId, Integer state) {
		changeState(id, updateUserId, state, null, null);
	}

	/**
	 * 更改认证状态
	 */
	public void changeState(Long id, Long updateUserId, Integer state, String remark) {
		changeState(id, updateUserId, state, remark, null);
	}

	/**
	 * 更改认证状态
	 */
	public void changeState(Long id, Long updateUserId, Integer state, String remark, Integer isSpecial) {
		List<Long> ids = new ArrayList<Long>();
		ids.add(id);
		changeState(ids, updateUserId, state, remark, isSpecial);
	}

	/**
	 * 支持批量更改认证状态
	 */
	public void changeState(List<Long> ids, Long updateUserId, Integer state, String remark, Integer blacklist) {
		String specialSql = "";
		if (state != null) {
			specialSql = SqlJoiner.join(specialSql, ", uv.state = ", state.toString());
		}
		if (CommonConstant.INT_BOOLEAN_TRUE.equals(blacklist)) {
			specialSql = SqlJoiner.join(specialSql, ", uv.is_special = 1");
		}
		if (StringUtils.isNotBlank(remark)) {
			specialSql = SqlJoiner.join(specialSql, ", uv.remark = '", remark, "'");
		}
		if (AmuseConstant.VERIFY_STATE_NOGIVE.equals(state)) {
			specialSql = SqlJoiner.join(specialSql, ", uv.claim_user_id = v.id");
		}

		String condition = " WHERE uv.ID ";
		if (CollectionUtils.isNotEmpty(ids)) {
			if (ids.size() > 1) {// 批量更改
				condition = SqlJoiner.join(condition, " IN (");
				for (Iterator<Long> it = ids.iterator(); it.hasNext();) {
					Long id = it.next();
					condition = SqlJoiner.join(condition, id.toString());
					if (it.hasNext()) {
						condition = SqlJoiner.join(condition, ",");
					}
				}
				condition = SqlJoiner.join(condition, ")");
			} else {// 单个更改
				Long id = ids.get(0);
				condition = SqlJoiner.join(condition, " = ", id.toString());
			}

			// 拉黑用户
			if (CommonConstant.INT_BOOLEAN_TRUE.equals(blacklist)) {
				String userlistSql = SqlJoiner.join("SELECT DISTINCT user_id userId FROM amuse_t_verify uv", condition);
				List<Map<String, Object>> userlist = queryDao.queryMap(userlistSql);
				if (CollectionUtils.isNotEmpty(userlist)) {
					List<Long> idLongs = Lists.newArrayList();
					List<UserBlack> userBlacks = Lists.newArrayList();
					for (Map<String, Object> u : userlist) {
						// 记录拉黑用户的ID
						Long id = MapUtils.getLong(u, "userId");
						if (null != id) {
							idLongs.add(id);
						}

						// 记录拉黑用户记录
						UserBlack userBlack = new UserBlack();
						userBlack.setUserId(id);
						userBlack.setChannel(UserConstant.BLACK_CHANNEL_AMUSE);
						userBlack.setValid(CommonConstant.INT_BOOLEAN_TRUE);
						Date now = new Date();
						userBlack.setCreateDate(now);
						userBlacks.add(userBlack);
					}
					userInfoService.disabledAndOffline(idLongs);
					userBlackService.save(userBlacks);
				}
			}

			// 更新状态
			String sql = SqlJoiner
					.join("UPDATE amuse_t_verify uv, ( SELECT u.id, count(v.id) count",
							" FROM sys_t_user u LEFT JOIN amuse_t_verify v ON u.id = v.claim_user_id WHERE u.user_type = 13 GROUP BY u.id ORDER BY count ASC LIMIT 1",
							" ) v SET uv.update_date = NOW(), uv.update_user_id = ", updateUserId.toString(),
							specialSql, condition);
			queryDao.update(sql);
		}
	}

	/**
	 * 修改认证的发放人
	 */
	public void changeClaimUserId(List<Long> ids, Long claimUserId) {
		if (CollectionUtils.isNotEmpty(ids)) {
			String idsStr = "";
			for (Iterator<Long> it = ids.iterator(); it.hasNext();) {
				Long id = it.next();
				if (id != null) {
					idsStr = SqlJoiner.join(idsStr, id.toString());
					if (it.hasNext()) {
						idsStr = SqlJoiner.join(idsStr, ",");
					}
				}
			}

			// 更新状态
			String sql = SqlJoiner.join("UPDATE amuse_t_verify SET update_date = NOW(), claim_user_id = ",
					claimUserId.toString(), " WHERE id IN (", idsStr, ")");
			queryDao.update(sql);
		}
	}

	/**
	 * 通过多个ID查询认证信息
	 */
	public List<AmuseVerify> findValidByIds(List<Long> ids) {
		return amuseVerifyDao.findByIdInAndValid(ids, CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * 通过多个ID查询认证信息，附带赛事及用户信息
	 */
	public List<AmuseVerify> findValidByIdsWithActivityAndUser(List<Long> ids) {
		return queryValid(ids, true, true, false);
	}

	public List<AmuseVerify> findValidByIdsWithActivityAndUserAndRecord(List<Long> ids) {
		return queryValid(ids, true, true, true);
	}

	/**
	 * 查询指定用户所有有效认证
	 */
	public List<AmuseVerify> findValidByUserIdIn(List<Long> userIds) {
		if (CollectionUtils.isNotEmpty(userIds)) {
			return amuseVerifyDao.findByUserIdInAndValid(userIds, CommonConstant.INT_BOOLEAN_TRUE);
		}
		return null;
	}

	/**
	 * 查询有效的认证信息,可选是否配置 用户信息、娱乐赛信息、报名信息
	 */
	private List<AmuseVerify> queryValid(List<Long> ids, boolean withUserInfo, boolean withActivityInfo,
			boolean withActivityRecord) {
		List<AmuseVerify> verifys = findValidByIds(ids);
		if (CollectionUtils.isNotEmpty(verifys)) {
			// 匹配 赛事、用户ID
			List<Long> activityIds = new ArrayList<Long>();
			List<Long> userIds = new ArrayList<Long>();
			for (AmuseVerify v : verifys) {
				if (withActivityRecord || withActivityInfo) {
					Long activityId = v.getActivityId();
					if (activityId != null) {
						activityIds.add(activityId);
					}
				}
				if (withActivityRecord || withUserInfo) {
					Long userId = v.getUserId();
					if (userId != null) {
						userIds.add(userId);
					}
				}
			}

			// 匹配认证的赛事信息、用户信息及报名信息
			List<UserInfo> users = null;
			List<AmuseActivityInfo> activitys = null;
			List<AmuseActivityRecord> records = null;
			if (withUserInfo) {
				users = userInfoService.findValidByIds(userIds);
			}
			if (withActivityInfo) {
				activitys = amuseActivityInfoService.findValidByIds(activityIds);
			}
			if (withActivityRecord) {
				records = amuseActivityRecordService.findGrantByActivityIdInAndUserIdIn(activityIds, userIds);
			}
			for (AmuseVerify v : verifys) {
				Long userId = v.getUserId();
				Long activityId = v.getActivityId();
				if (CollectionUtils.isNotEmpty(users)) {
					for (UserInfo u : users) {
						if (userId != null && userId.equals(u.getId())) {
							v.setUserInfo(u);
							break;
						}
					}
				}
				if (CollectionUtils.isNotEmpty(activitys)) {
					for (AmuseActivityInfo a : activitys) {
						if (activityId != null && activityId.equals(a.getId())) {
							v.setActivityInfo(a);
							break;
						}
					}
				}
				if (CollectionUtils.isNotEmpty(records)) {
					for (AmuseActivityRecord r : records) {
						if (userId != null && activityId != null && userId.equals(r.getUserId())
								&& activityId.equals(r.getActivityId())) {
							v.setActivityRecord(r);
							break;
						}
					}
				}
			}
		}
		return verifys;
	}

	/**
	 * 根据多个赛事及用户信息,查询认证信息
	 */
	public List<AmuseVerify> findValidByActivityIdInAndUserIdInAndState(List<Long> activityIds, List<Long> userIds,
			Integer state) {
		return amuseVerifyDao.findByActivityIdInAndUserIdInAndStateAndValid(activityIds, userIds, state,
				CommonConstant.INT_BOOLEAN_TRUE);
	}
}
