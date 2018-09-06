package com.miqtech.master.service.amuse;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.amuse.AmuseActivityRecordDao;
import com.miqtech.master.entity.amuse.AmuseActivityInfo;
import com.miqtech.master.entity.amuse.AmuseActivityRecord;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

/**
 * 娱乐赛操作service
 */
@Component
public class AmuseActivityRecordService {
	@Autowired
	private AmuseActivityRecordDao activityRecordDao;
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private AmuseActivityInfoService amuseActivityInfoService;

	/**
	 * 保存
	 */
	public void save(AmuseActivityRecord activityRecord) {
		if (activityRecord != null) {
			activityRecordDao.save(activityRecord);
		}
	}

	/**
	 * 批量保存
	 */
	public List<AmuseActivityRecord> save(List<AmuseActivityRecord> ars) {
		if (CollectionUtils.isNotEmpty(ars)) {
			return (List<AmuseActivityRecord>) activityRecordDao.save(ars);
		}
		return null;
	}

	/**
	 * 根据ID查实体
	 */
	public AmuseActivityRecord findById(Long id) {
		return activityRecordDao.findOne(id);
	}

	/**
	 * 查询指定用户的有效报名记录
	 */
	public List<AmuseActivityRecord> findValidByUserIdIn(List<Long> userIds) {
		if (CollectionUtils.isNotEmpty(userIds)) {
			return activityRecordDao.findByUserIdInAndValid(userIds, CommonConstant.INT_BOOLEAN_TRUE);
		}
		return null;
	}

	/**
	 * 根据娱乐赛ID查已报名人数
	 */
	public int queryNumByAmuseActivityId(long activityId) {
		String sqlNum = "select count(1) from amuse_r_activity_record r where r.is_valid=1 and r.activity_id="
				+ activityId;
		Number num = queryDao.query(sqlNum);
		if (null != num) {
			return num.intValue();
		}
		return 0;
	}

	/**
	 * 娱乐赛活动报名，返回:-1-参数错误；0-报名成功；1-报名人数已满；2-赛事未开始；3-赛事已结束
	 */
	public int amuseApply(Long activityId, Long userId) {
		if (null != activityId && null != userId) {
			AmuseActivityInfo amuseActivityInfo = amuseActivityInfoService.findById(activityId);
			int timeStatus = amuseActivityInfoService.getAmuseTimeStatusById(activityId);
			if (timeStatus == 0) {
				return 2;
			} else if (timeStatus == 2) {
				return 3;
			}
			if (null == amuseActivityInfo.getMaxNum()
					|| queryNumByAmuseActivityId(activityId) < amuseActivityInfo.getMaxNum()) {
				AmuseActivityRecord amuseActivityRecord = new AmuseActivityRecord();
				amuseActivityRecord.setActivityId(activityId);
				amuseActivityRecord.setUserId(userId);
				amuseActivityRecord.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				amuseActivityRecord.setCreateDate(new Date());

				save(amuseActivityRecord);
				return 0;
			} else {
				return 1;
			}
		}
		return -1;
	}

	/**
	 * 查询用户在某个赛事的报名记录
	 */
	public AmuseActivityRecord findValidOneByActivityIdAndUserId(Long activityId, Long userId) {
		List<AmuseActivityRecord> records = activityRecordDao.findByActivityIdAndUserIdAndValid(activityId, userId,
				CommonConstant.INT_BOOLEAN_TRUE);
		if (CollectionUtils.isNotEmpty(records)) {
			return records.get(0);
		}
		return null;
	}

	/**
	 * 查询未完成的报名记录
	 */
	public List<AmuseActivityRecord> findUnfinishByActivityIdAndUserId(Long activityId, Long userId) {
		return activityRecordDao.findByActivityIdAndUserIdAndStateAndValid(activityId, userId, 0,
				CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * 查询待发放的报名信息
	 */
	public List<AmuseActivityRecord> findGrantByActivityIdInAndUserIdIn(List<Long> activityIds, List<Long> userIds) {
		return activityRecordDao.findByActivityIdInAndUserIdInAndStateAndValid(activityIds, userIds, 1,
				CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * 取消用户某赛事的报名信息
	 */
	public void cancelUserActivityRecord(Long activityId, Long userId) {
		if (activityId != null && userId != null) {
			String sql = SqlJoiner.join("UPDATE amuse_r_activity_record SET state=-1, is_valid=",
					CommonConstant.INT_BOOLEAN_FALSE.toString(), " WHERE activity_id = ", activityId.toString(),
					" AND user_id = ", userId.toString(), " AND is_valid != ",
					CommonConstant.INT_BOOLEAN_FALSE.toString());
			queryDao.update(sql);
		}
	}

	/**我的赛事-娱乐赛
	 * @param userId
	 * @return
	 */
	public PageVO myAmuse(String userId, Integer page, Integer pageSize) {
		PageVO vo = new PageVO();
		String limitSql = " limit :start,:pageSize";
		Map<String, Object> params = new HashMap<String, Object>();
		if (pageSize == null) {
			pageSize = PageUtils.API_DEFAULT_PAGE_SIZE;
		}
		params.put("start", (page - 1) * pageSize);
		params.put("pageSize", pageSize);
		String sql = SqlJoiner
				.join("select count(DISTINCT a.id) from (amuse_t_activity a,amuse_r_activity_record b) where a.id=b.activity_id and b.is_valid=1 and b.state<> -1 and b.user_id=",
						userId);
		Number total = queryDao.query(sql);
		if (total != null) {
			vo.setTotal(total.intValue());
			if (page * pageSize >= total.intValue()) {
				vo.setIsLast(1);
			}
		}
		sql = SqlJoiner
				.join("SELECT c.icon mainIcon, a.id, a.title, a.start_date startDate, IF (now() < a.apply_start, 2, IF (now() >= a.apply_start AND now() < a.start_date, 1, IF (now() >= a.start_date AND now() < a.end_date, 5, 4))) status FROM ( amuse_t_activity a, amuse_r_activity_record b ) LEFT JOIN amuse_r_activity_icon c ON a.id = c.activity_id AND c.is_main = 1 AND c.is_valid = 1 WHERE a.id = b.activity_id AND b.is_valid = 1 and b.state<> -1 AND b.user_id =",
						userId, " GROUP BY a.id order by b.create_date desc", limitSql);
		vo.setList(queryDao.queryMap(sql, params));
		return vo;
	}

	/**
	 * 查询用户在某赛事下的报名信息及赛事信息
	 */
	public Map<String, Object> getUserActivityApplyInfo(Long userId, Long activityId) {
		if (null != userId && null != activityId) {
			String sql = SqlJoiner
					.join("SELECT ar.name, ar.telephone, ar.qq, ar.game_account gameAccount, ar.server, a.title activityName, a.reward activityReward, a.start_date startDate",
							" FROM amuse_r_activity_record ar JOIN amuse_t_activity a ON ar.activity_id = a.id",
							" WHERE ar.activity_id = ", activityId.toString(), " AND ar.user_id = ", userId.toString(),
							" AND ar.is_valid = 1 LIMIT 1");
			return queryDao.querySingleMap(sql);
		}

		return null;
	}

	public PageVO userPrizeList(int page, Long userId) {
		if (userId == null) {
			userId = 0L;
		}

		String condition = " WHERE aar.is_valid = 1 AND aar.user_id = " + userId;
		String totalCondition = condition;

		// 排序
		String order = " ORDER BY aar.create_date DESC";

		// 分页
		String limit = "";
		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page > 0) {
			Integer startRow = (page - 1) * pageSize;
			limit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());
		}

		String sql = SqlJoiner
				.join("SELECT aar.id, ui.username, aar.telephone, aar.qq, aa.award_type awardType, aa.award_sub_type awardSubType, aa.award_amount awardAmount, aar.create_date createDate",
						" FROM amuse_r_activity_record aar JOIN amuse_t_activity aa ON aar.activity_id = aa.id AND aa.is_valid = 1",
						" JOIN amuse_t_verify av ON aar.user_id = av.user_id AND aar.activity_id = av.activity_id AND av.is_valid = 1",
						" JOIN user_t_info ui ON aar.user_id = ui.id", condition, order, limit);
		List<Map<String, Object>> list = queryDao.queryMap(sql);

		String totalSql = SqlJoiner
				.join("SELECT COUNT(1)",
						" FROM amuse_r_activity_record aar JOIN amuse_t_activity aa ON aar.activity_id = aa.id AND aa.is_valid = 1",
						" JOIN amuse_t_verify av ON aar.user_id = av.user_id AND aar.activity_id = av.activity_id AND av.is_valid = 1",
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
	 * 统计用户奖品总数
	 */
	public Number userPrizeSum(Long userId) {
		if (userId != null) {
			String sql = SqlJoiner
					.join("SELECT sum(aa.award_amount) sum FROM amuse_r_activity_record aar",
							" JOIN amuse_t_activity aa ON aar.activity_id = aa.id AND aa.is_valid = 1",
							" JOIN amuse_t_verify av ON aar.user_id = av.user_id AND aar.activity_id = av.activity_id AND av.is_valid = 1",
							" WHERE aar.is_valid = 1 AND aar.user_id = 21");
			return queryDao.query(sql);
		}
		return null;
	}
}
