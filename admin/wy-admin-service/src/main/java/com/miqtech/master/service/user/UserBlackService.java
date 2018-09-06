package com.miqtech.master.service.user;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.UserConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.user.UserBlackDao;
import com.miqtech.master.entity.amuse.AmuseActivityRecord;
import com.miqtech.master.entity.amuse.AmuseVerify;
import com.miqtech.master.entity.award.AwardRecord;
import com.miqtech.master.entity.mall.CoinHistory;
import com.miqtech.master.entity.user.UserBlack;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.entity.user.UserRedbag;
import com.miqtech.master.service.amuse.AmuseActivityRecordService;
import com.miqtech.master.service.amuse.AmuseVerifyService;
import com.miqtech.master.service.award.AwardRecordService;
import com.miqtech.master.service.mall.CoinHistoryService;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

/**
 * 黑名单service
 */
@Component
public class UserBlackService {
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private UserBlackDao userBlackDao;
	@Autowired
	private UserInfoService userInfoService;
	@Autowired
	private CoinHistoryService coinHistoryService;
	@Autowired
	private UserRedbagService userRedbagService;
	@Autowired
	private AwardRecordService awardRecordService;
	@Autowired
	private AmuseActivityRecordService amuseActivityRecordService;
	@Autowired
	private AmuseVerifyService amuseVerifyService;

	/*
	 * 查实体
	 */
	public UserBlack findById(long id) {
		return userBlackDao.findOne(id);
	}

	/**
	 * 查询多个有效实体
	 */
	public List<UserBlack> findValidByIdIn(List<Long> ids) {
		if (CollectionUtils.isNotEmpty(ids)) {
			return userBlackDao.findByIdInAndValid(ids, CommonConstant.INT_BOOLEAN_TRUE);
		}
		return null;
	}

	/*
	 * 根据userId查黑名单用户
	 */
	public UserBlack findByUserIdAndChannel(long userId, int channel) {
		return userBlackDao.findByUserIdAndChannelAndValidAndIsWhite(userId, channel, 1, 0);
	}

	/*
	 * 保存
	 */
	public UserBlack save(UserBlack userBlack) {
		if (null != userBlack) {
			Date now = new Date();
			if (null != userBlack.getId()) {
				UserBlack old = findById(userBlack.getId());
				if (null != old) {
					userBlack = BeanUtils.updateBean(old, userBlack);
					userBlack.setUpdateDate(now);
				}
			} else {
				userBlack.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				userBlack.setCreateDate(now);
			}
			return userBlackDao.save(userBlack);
		}
		return null;
	}

	/**
	 * 批量保存
	 */
	public List<UserBlack> save(List<UserBlack> ubs) {
		return (List<UserBlack>) userBlackDao.save(ubs);
	}

	/*
	 * 黑名单列表
	 */
	public PageVO userBlackPage(int page, Map<String, Object> searchParams) {
		String join = StringUtils.EMPTY;
		String totalJoin = StringUtils.EMPTY;
		String condition = " WHERE b.is_valid = 1 AND b.channel=2";
		String totalCondition = " WHERE b.is_valid = 1 AND b.channel=2";

		Map<String, Object> params = Maps.newHashMap();
		if (MapUtils.isNotEmpty(searchParams)) {
			String telephone = MapUtils.getString(searchParams, "telephone");
			if (StringUtils.isNotBlank(telephone)) {
				String likeTelephone = "%" + telephone + "%";
				join = SqlJoiner.join(join, " RIGHT JOIN user_t_info u ON u.id=b.user_id AND u.username LIKE '",
						likeTelephone, "'");
				totalJoin = SqlJoiner.join(totalJoin,
						" RIGHT JOIN user_t_info u ON u.id=b.user_id AND u.username LIKE '", likeTelephone, "'");
			} else {
				join = SqlJoiner.join(join, " LEFT JOIN user_t_info u ON u.id=b.user_id");
			}
			// 提交时间
			String beginDate = MapUtils.getString(searchParams, "beginDate");
			if (StringUtils.isNotBlank(beginDate)) {
				condition = SqlJoiner.join(condition, " AND b.create_date >= :beginDate");
				params.put("beginDate", beginDate);
				totalCondition = SqlJoiner.join(totalCondition, " AND b.create_date >= '", beginDate, "'");
			}
			String endDate = MapUtils.getString(searchParams, "endDate");
			if (StringUtils.isNotBlank(endDate)) {
				condition = SqlJoiner.join(condition, " AND b.create_date < ADDDATE(:endDate, INTERVAL 1 DAY)");
				params.put("endDate", endDate);
				totalCondition = SqlJoiner.join(totalCondition, " AND b.create_date < ADDDATE('", endDate,
						"', INTERVAL 1 DAY)");
			}
			String account = MapUtils.getString(searchParams, "account");
			if (StringUtils.isNotBlank(account)) {
				String likeAccount = "%" + account + "%";
				condition = SqlJoiner.join(condition, " AND b.account LIKE '", likeAccount, "'");
				totalCondition = SqlJoiner.join(totalCondition, " AND b.account LIKE '", likeAccount, "'");
			}
		}

		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		page = page < 1 ? 1 : page;
		Integer startRow = (page - 1) * pageSize;
		String sqlLimit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());

		String sql = SqlJoiner.join(
				"SELECT b.id, b.create_date date, b.account, b.user_id userId, u.telephone, b.is_white isWhite",
				" FROM user_t_black b", join, condition, " ORDER BY b.create_date DESC", sqlLimit);

		String totalSql = SqlJoiner.join("SELECT COUNT(1) FROM user_t_black b ", totalJoin, totalCondition);
		Number totalNum = queryDao.query(totalSql);
		int total = 0;
		if (totalNum != null) {
			total = totalNum.intValue();
		}
		List<Map<String, Object>> list = null;
		if (total > 0) {
			list = queryDao.queryMap(sql, params);
		}

		PageVO vo = new PageVO();
		vo.setList(list);
		vo.setTotal(total);
		return vo;
	}

	/**
	 * 查询后台所需格式的分页列表
	 */
	public PageVO adminPage(int page, Map<String, String> searchParams) {
		String condition = " WHERE ub.is_valid = 1 AND ub.channel = " + UserConstant.BLACK_CHANNEL_AMUSE;
		String totalCondition = condition;
		Map<String, Object> params = Maps.newHashMap();

		String account = MapUtils.getString(searchParams, "account");
		if (StringUtils.isNotBlank(account)) {
			String likeAccount = "%" + account + "%";
			params.put("account", likeAccount);
			condition = SqlJoiner.join(condition, " AND (aar.telephone LIKE :account OR aar.qq LIKE :account)");
			totalCondition = SqlJoiner.join(totalCondition, " AND (aar.telephone LIKE '", likeAccount,
					"' OR aar.qq LIKE '", likeAccount, "')");
		}
		String username = MapUtils.getString(searchParams, "username");
		if (StringUtils.isNotBlank(username)) {
			String likeUsername = "%" + username + "%";
			params.put("username", likeUsername);
			condition = SqlJoiner.join(condition, " AND ui.username LIKE :username");
			totalCondition = SqlJoiner.join(totalCondition, " AND ui.username LIKE '", likeUsername, "'");
		}
		String beginDate = MapUtils.getString(searchParams, "beginDate");
		if (StringUtils.isNotBlank(beginDate)) {
			params.put("beginDate", beginDate);
			condition = SqlJoiner.join(condition, " AND ub.create_date >= :beginDate");
			totalCondition = SqlJoiner.join(totalCondition, " AND ub.create_date >= '", beginDate, "'");
		}
		String endDate = MapUtils.getString(searchParams, "endDate");
		if (StringUtils.isNotBlank(endDate)) {
			params.put("endDate", endDate);
			condition = SqlJoiner.join(condition, " AND ub.create_date < ADDDATE(:endDate, INTERVAL 1 DAY)");
			totalCondition = SqlJoiner.join(totalCondition, " AND ub.create_date < ADDDATE('", endDate,
					"', INTERVAL 1 DAY)");
		}

		// 排序
		String order = " ORDER BY ub.create_date DESC";

		// 分页
		String limit = "";
		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page > 0) {
			Integer startRow = (page - 1) * pageSize;
			limit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());
		}

		String sql = SqlJoiner.join("SELECT ub.id, ui.id userId, ui.username, ub.create_date createDate",
				" FROM user_t_black ub LEFT JOIN user_t_info ui ON ub.user_id = ui.id",
				" LEFT JOIN amuse_r_activity_record aar ON ui.id = aar.user_id", condition, " GROUP BY ub.id", order,
				limit);
		List<Map<String, Object>> list = queryDao.queryMap(sql, params);

		String totalSql = SqlJoiner.join(
				"SELECT COUNT(1) FROM ( SELECT 1 FROM user_t_black ub LEFT JOIN user_t_info ui ON ub.user_id = ui.id",
				" LEFT JOIN amuse_r_activity_record aar ON ui.id = aar.user_id", totalCondition, " GROUP BY ub.id ) T");
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

	/*
	 * 批量恢复黑名单
	 */
	public void recoverBlackByUserIds(String ids) {
		String sql = SqlJoiner.join("UPDATE user_t_black SET is_valid=", CommonConstant.INT_BOOLEAN_FALSE.toString(),
				" WHERE user_id IN(", ids, ")");
		queryDao.update(sql);
	}
	
	/*
	 * 批量加入白名单
	 */
	public void addWhiteByUserIds(String ids, int oper) {
		oper = oper == 1 ? 1 : 0;
		String sql = SqlJoiner.join("UPDATE user_t_black SET is_white=", String.valueOf(oper), " WHERE user_id IN(",
				ids, ")");
		queryDao.update(sql);
	}

	/*
	 * 根据渠道查出所有黑名单用户id（逗号隔开）
	 */
	public String getUserIdsByChannel(int channel) {
		String userIds = StringUtils.EMPTY;
		String sql = SqlJoiner.join(
				"SELECT group_concat(DISTINCT user_id)userIds FROM user_t_black WHERE is_white<>1 AND is_valid=",
				CommonConstant.INT_BOOLEAN_TRUE.toString(), " AND channel=", String.valueOf(channel));
		Map<String, Object> map = queryDao.querySingleMap(sql);
		if (MapUtils.isNotEmpty(map)) {
			Object obj = map.get("userIds");
			userIds = null == obj ? "" : obj.toString();
		}
		return userIds;
	}

	/*
	 * 根据渠道查出所有黑名单account（逗号隔开）
	 */
	public String getAccountsByChannel(int channel) {
		String accounts = StringUtils.EMPTY;
		String sql = SqlJoiner.join(
				"SELECT group_concat(DISTINCT concat('''',account,''''))accounts FROM user_t_black WHERE is_white<>1 AND is_valid=",
				CommonConstant.INT_BOOLEAN_TRUE.toString(), " AND channel=", String.valueOf(channel));
		Map<String, Object> map = queryDao.querySingleMap(sql);
		if (MapUtils.isNotEmpty(map)) {
			Object obj = map.get("accounts");
			accounts = null == obj ? "" : obj.toString();
		}
		return accounts;
	}

	/**
	 * 批量恢复后台黑名单用户（只恢复,不操作数据）
	 */
	public void adminRecover(List<Long> ids) {
		adminRecover(ids, false, null);
	}

	/**
	 * 批量恢复后台黑名单用户
	 */
	@Transactional
	public void adminRecover(List<Long> ids, boolean deletePrizes, Long sysUserId) {
		if (CollectionUtils.isEmpty(ids)) {
			return;
		}

		List<UserBlack> ubs = findValidByIdIn(ids);
		if (CollectionUtils.isNotEmpty(ubs)) {
			// 确定需启用的用户ID
			List<Long> userIds = Lists.newArrayList();
			for (UserBlack ub : ubs) {
				Long userId = ub.getUserId();
				if (userId != null) {
					userIds.add(userId);
					ub.setValid(CommonConstant.INT_BOOLEAN_FALSE);
				}
			}

			// 删除兑换历史
			if (deletePrizes) {
				// 删除用户红包
				List<UserRedbag> userRedbags = userRedbagService.findValidByUserIdIn(userIds);
				if (CollectionUtils.isNotEmpty(userRedbags)) {
					for (UserRedbag ur : userRedbags) {
						ur.setValid(CommonConstant.INT_BOOLEAN_FALSE);
						ur.setDelUserId(sysUserId);
					}
					userRedbagService.save(userRedbags);
				}

				// 删除用户金币历史
				List<CoinHistory> coinHistories = coinHistoryService.findValidByUserIdIn(userIds);
				if (CollectionUtils.isNotEmpty(coinHistories)) {
					for (CoinHistory ch : coinHistories) {
						ch.setValid(CommonConstant.INT_BOOLEAN_FALSE);
						ch.setDelUserId(sysUserId);
					}
					coinHistoryService.save(coinHistories);
				}

				// 删除发放记录
				List<AwardRecord> awardRecords = awardRecordService.findValidByUserIdIn(userIds);
				if (CollectionUtils.isNotEmpty(awardRecords)) {
					for (AwardRecord ar : awardRecords) {
						ar.setValid(CommonConstant.INT_BOOLEAN_FALSE);
						ar.setDelUserId(sysUserId);
					}
					awardRecordService.save(awardRecords);
				}

				// 删除娱乐赛报名信息
				List<AmuseActivityRecord> aars = amuseActivityRecordService.findValidByUserIdIn(userIds);
				if (CollectionUtils.isNotEmpty(aars)) {
					for (AmuseActivityRecord aar : aars) {
						aar.setValid(CommonConstant.INT_BOOLEAN_FALSE);
					}
					amuseActivityRecordService.save(aars);
				}

				// 删除娱乐赛认证信息
				List<AmuseVerify> verifies = amuseVerifyService.findValidByUserIdIn(userIds);
				if (CollectionUtils.isNotEmpty(verifies)) {
					for (AmuseVerify v : verifies) {
						v.setValid(CommonConstant.INT_BOOLEAN_FALSE);
					}
					amuseVerifyService.save(verifies);
				}
			}

			// 启用指定用户
			List<UserInfo> users = userInfoService.findByIdIn(userIds);
			if (CollectionUtils.isNotEmpty(users)) {
				for (UserInfo u : users) {
					if (deletePrizes) {
						u.setCoin(0);
					}
					u.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				}
				userInfoService.save(users);
			}

			// 删除黑名单信息
			save(ubs);
		}
	}

	public UserBlack findByUserIdAndAccountAndChannel(Long userId, String account, int channel) {
		return userBlackDao.findByUserIdAndAccountAndChannel(userId, account, channel);
	}

	public Object findByAccountAndChannel(String account, int channel) {
		return userBlackDao.findByAccountAndChannelAndValidAndIsWhite(account, channel, 1, 0);
	}
}
