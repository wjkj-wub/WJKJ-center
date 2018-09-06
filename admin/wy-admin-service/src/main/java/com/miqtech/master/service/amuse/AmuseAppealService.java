package com.miqtech.master.service.amuse;

import java.util.ArrayList;
import java.util.Date;
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

import com.google.common.collect.Maps;
import com.miqtech.master.consts.AmuseConstant;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.SystemUserConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.amuse.AmuseAppealDao;
import com.miqtech.master.dao.amuse.AmuseRewardProgressDao;
import com.miqtech.master.entity.amuse.AmuseActivityInfo;
import com.miqtech.master.entity.amuse.AmuseAppeal;
import com.miqtech.master.entity.amuse.AmuseAppealImg;
import com.miqtech.master.entity.amuse.AmuseRewardProgress;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

/**
 * 娱乐赛申诉service
 */
@Component
public class AmuseAppealService {
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private AmuseAppealDao amuseAppealDao;
	@Autowired
	private AmuseAppealImgService amuseAppealImgService;
	@Autowired
	private AmuseRewardProgressDao amuseRewardProgressDao;
	@Autowired
	private AmuseActivityInfoService amuseActivityInfoService;

	public AmuseAppeal autoSave(AmuseAppeal appeal) {
		if (appeal != null) {
			Date now = new Date();
			appeal.setUpdateDate(now);

			Long id = appeal.getId();
			if (id != null) {
				AmuseAppeal old = amuseAppealDao.findOne(id);
				if (old != null) {
					appeal = BeanUtils.updateBean(old, appeal);
				}
			} else {
				appeal.setCreateDate(now);
				appeal.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			}

			return amuseAppealDao.save(appeal);
		}
		return null;
	}

	/**
	 * 查询用户在某场比赛的第一条申诉记录
	 */
	public AmuseAppeal findValidOneByActivityIdAndUserId(Long activityId, Long userId) {
		return findValidOneByActivityIdAndUserId(activityId, userId, false);
	}

	/**
	 * 查询用户在某场比赛的第一条申诉记录
	 */
	public AmuseAppeal findValidOneByActivityIdAndUserId(Long activityId, Long userId, boolean withImgs) {
		List<AmuseAppeal> appeals = amuseAppealDao.findByActivityIdAndUserIdAndValid(activityId, userId,
				CommonConstant.INT_BOOLEAN_TRUE);
		if (CollectionUtils.isNotEmpty(appeals)) {
			AmuseAppeal appeal = appeals.get(0);
			if (withImgs) {
				List<AmuseAppealImg> imgs = amuseAppealImgService.findByAppealId(appeal.getId());
				appeal.setImgs(imgs);
			}
			return appeal;
		}
		return null;
	}

	/**
	 * 提交申诉
	 */
	public AmuseAppeal commitAppeal(Long activityId, Long userId, String describes, String[] imgs) {
		if (null == activityId) {
			return null;
		}

		AmuseActivityInfo activity = amuseActivityInfoService.findById(activityId);

		// 保存申诉信息
		AmuseAppeal appeal = new AmuseAppeal();
		appeal.setActivityId(activityId);
		appeal.setUserId(userId);
		appeal.setDescribes(describes);
		appeal.setState(AmuseConstant.APPEAL_STATE_INAPPEAL);
		Integer serialType = activity.getWay() == 1 ? 2 : 1;
		Integer serialAwardType = activity.getAwardType();
		appeal.setSerial(amuseActivityInfoService.genSerial(serialType, serialAwardType));
		appeal = autoSave(appeal);

		// 保存图片
		if (appeal != null && ArrayUtils.isNotEmpty(imgs)) {
			List<AmuseAppealImg> saveImgs = new ArrayList<AmuseAppealImg>();
			for (String i : imgs) {
				AmuseAppealImg saveImg = new AmuseAppealImg();
				saveImg.setAppealId(appeal.getId());
				saveImg.setImg(i);
				saveImgs.add(saveImg);
			}
			amuseAppealImgService.save(saveImgs);
			appeal.setImgs(saveImgs);
		}

		return appeal;
	}

	/**
	 * 提交申诉
	 */
	@Transactional
	public AmuseAppeal v2CommitAppeal(Long activityId, Long userId, String describes, String[] imgs, Long categoryId) {
		if (null == activityId) {
			return null;
		}

		AmuseActivityInfo activity = amuseActivityInfoService.findById(activityId);
		String sysUserSql = SqlJoiner
				.join("select u.id, count(1) count from sys_t_user u left join amuse_t_appeal a on u.id = a.update_user_id where u.user_type = ",
						SystemUserConstant.TYPE_AMUSE_APPEAL.toString(), " group by u.id order by count asc limit 1");
		Map<String, Object> lessAppealUser = queryDao.querySingleMap(sysUserSql);
		Long sysUserId = MapUtils.getLong(lessAppealUser, "id");
		// 保存申诉信息
		AmuseAppeal appeal = new AmuseAppeal();
		appeal.setActivityId(activityId);
		appeal.setUserId(userId);
		appeal.setDescribes(describes);
		appeal.setState(0);
		appeal.setUpdateUserId(sysUserId);
		if (categoryId != null) {
			appeal.setCategoryId(categoryId);
		}
		Integer serialType = activity.getWay() == 1 ? 2 : 1;
		Integer serialAwardType = activity.getAwardType();
		appeal.setSerial(amuseActivityInfoService.genSerial(serialType, serialAwardType));
		appeal = autoSave(appeal);

		// 保存图片
		if (appeal != null && ArrayUtils.isNotEmpty(imgs)) {
			List<AmuseAppealImg> saveImgs = new ArrayList<AmuseAppealImg>();
			for (String i : imgs) {
				AmuseAppealImg saveImg = new AmuseAppealImg();
				saveImg.setAppealId(appeal.getId());
				saveImg.setImg(i);
				saveImgs.add(saveImg);
			}
			amuseAppealImgService.save(saveImgs);
			appeal.setImgs(saveImgs);
		}
		AmuseRewardProgress progress = new AmuseRewardProgress();
		progress.setActivityId(activityId);
		progress.setUserId(userId);
		progress.setTargetId(appeal.getId());
		progress.setType(2);
		progress.setState(5);
		progress.setValid(1);
		progress.setCreateDate(new Date());
		amuseRewardProgressDao.save(progress);
		return appeal;

	}

	public AmuseAppeal findById(Long id) {
		return amuseAppealDao.findOne(id);
	}

	/**
	 * 获取申诉详情(附带图片)
	 * @param id
	 * @return
	 */
	public AmuseAppeal findDetailById(Long id) {
		AmuseAppeal appeal = findById(id);
		if (null != appeal && null != appeal.getId()) {
			List<AmuseAppealImg> imgs = amuseAppealImgService.findByAppealId(appeal.getId());
			appeal.setImgs(imgs);
		}
		return appeal;
	}

	/**
	 * 获取申诉详情（附带图片）
	 */
	public AmuseAppeal findValidDetailByActivityIdAndUserId(Long activityId, Long userId) {
		AmuseAppeal appeal = findByActivityIdAndUserIdAndValidAndCategoryIdIsNotNull(activityId, userId,
				CommonConstant.INT_BOOLEAN_TRUE);
		if (null != appeal && null != appeal.getId()) {
			List<AmuseAppealImg> imgs = amuseAppealImgService.findByAppealId(appeal.getId());
			appeal.setImgs(imgs);
		}
		return appeal;
	}

	public void save(AmuseAppeal amuseAppeal) {
		amuseAppealDao.save(amuseAppeal);
	}

	public AmuseAppeal findByActivityIdAndUserIdAndValidAndCategoryIdIsNull(Long activityId, Long userId, Integer valid) {
		List<AmuseAppeal> appeals = amuseAppealDao.findByActivityIdAndUserIdAndValidAndCategoryIdIsNull(activityId,
				userId, valid);
		if (CollectionUtils.isNotEmpty(appeals)) {
			return appeals.get(appeals.size() - 1);
		}
		return null;
	}

	public AmuseAppeal findByActivityIdAndUserIdAndValidAndCategoryIdIsNotNull(Long activityId, Long userId,
			Integer valid) {
		return amuseAppealDao.findByActivityIdAndUserIdAndValidAndCategoryIdIsNotNull(activityId, userId, valid);
	}

	public PageVO page(int page, Map<String, Object> searchParams) {
		String iCondition = " WHERE a.is_valid = 1";
		String eCondition = " WHERE a.is_valid = 1";
		String iTotalCondition = iCondition;
		String eTotalCondition = eCondition;
		Map<String, Object> params = Maps.newHashMap();

		String state = MapUtils.getString(searchParams, "state");
		if (NumberUtils.isNumber(state)) {
			iCondition = SqlJoiner.join(iCondition, " AND a.state = ", state);
			iTotalCondition = SqlJoiner.join(iTotalCondition, " AND a.state = ", state);
		}
		String telephone = MapUtils.getString(searchParams, "telephone");
		if (StringUtils.isNotBlank(telephone)) {
			String likeTelephone = "%" + telephone + "%";
			params.put("likeTelephone", likeTelephone);
			eCondition = SqlJoiner.join(eCondition, " AND ar.telephone LIKE :likeTelephone");
			eTotalCondition = SqlJoiner.join(eTotalCondition, " AND ar.telephone LIKE '", likeTelephone, "'");
		}
		String activityId = MapUtils.getString(searchParams, "activityId");
		if (NumberUtils.isNumber(activityId)) {
			iCondition = SqlJoiner.join(iCondition, " AND a.activity_id = ", activityId);
			iTotalCondition = SqlJoiner.join(iTotalCondition, " AND a.activity_id = ", activityId);
		}
		String beginDate = MapUtils.getString(searchParams, "beginDate");
		if (StringUtils.isNotBlank(beginDate)) {
			params.put("beginDate", beginDate);
			iCondition = SqlJoiner.join(iCondition, " AND a.create_date >= :beginDate");
			iTotalCondition = SqlJoiner.join(iTotalCondition, " AND a.create_date >= '", beginDate, "'");
		}
		String endDate = MapUtils.getString(searchParams, "endDate");
		if (StringUtils.isNotBlank(endDate)) {
			params.put("endDate", endDate);
			iCondition = SqlJoiner.join(iCondition, " AND a.create_date < ADDDATE(:endDate, INTERVAL 1 DAY)");
			iTotalCondition = SqlJoiner.join(iTotalCondition, " AND a.create_date < ADDDATE('", endDate,
					"', INTERVAL 1 DAY)");
		}
		Long sysUserId = MapUtils.getLong(searchParams, "sysUserId");
		if (null != sysUserId) {
			iCondition = SqlJoiner.join(iCondition, " AND a.update_user_id = " + sysUserId.toString());
			iTotalCondition = SqlJoiner.join(iTotalCondition, " AND a.update_user_id = " + sysUserId.toString());
		}

		String limit = "";
		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page > 0) {
			Integer startRow = (page - 1) * pageSize;
			limit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());
		}

		String sql = SqlJoiner
				.join("SELECT a.id, a.serial, act.title activityName, act.reward activityReward, act.award_type awardType, act.award_sub_type awardSubType,",
						" ar.telephone recordTelephone, ar.qq, ar.game_account gameAccount, u.username, a.state, a.remark, a.update_user_id updateUserId FROM (",
						" SELECT * FROM amuse_t_appeal a",
						iCondition,
						" ORDER BY a.create_date DESC",
						limit,
						" ) a JOIN amuse_t_activity act ON a.activity_id = act.id",
						" LEFT JOIN amuse_r_activity_record ar ON a.activity_id = ar.activity_id AND a.user_id = ar.user_id",
						" JOIN user_t_info u ON a.user_id = u.id", eCondition,
						" GROUP BY a.id ORDER BY a.create_date DESC");
		List<Map<String, Object>> list = queryDao.queryMap(sql, params);

		String totalSql = SqlJoiner.join("SELECT COUNT(1) count FROM ( SELECT 1 FROM (",
				" SELECT * FROM amuse_t_appeal a", iTotalCondition,
				" ) a JOIN amuse_t_activity act ON a.activity_id = act.id",
				" LEFT JOIN amuse_r_activity_record ar ON a.activity_id = ar.activity_id AND a.user_id = ar.user_id",
				" JOIN user_t_info u ON a.user_id = u.id", eTotalCondition, " GROUP BY a.id) t");
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
	 * 修改申诉状态
	 */
	public void changeState(List<Long> ids, Integer state, Long sysUserId, String remark) {
		if (CollectionUtils.isNotEmpty(ids) && null != state) {
			String idsStr = "";
			for (Long i : ids) {
				if (idsStr.length() > 0) {
					idsStr = SqlJoiner.join(idsStr, ",");
				}
				idsStr = SqlJoiner.join(idsStr, i.toString());
			}

			String updateUserIdSql = "";
			if (null != sysUserId) {
				updateUserIdSql = ", update_user_id = " + sysUserId;
			}
			String remarkSql = "";
			if (StringUtils.isNotBlank(remark)) {
				remarkSql = ", remark = '" + remark + "'";
			}

			// 更新状态
			String sql = SqlJoiner.join("UPDATE amuse_t_appeal SET state = ", state.toString(), updateUserIdSql,
					remarkSql, ", update_date = NOW() WHERE id IN (", idsStr, ")");
			queryDao.update(sql);
		}
	}

	/**
	 * 通过ID列表查询有效数据
	 */
	public List<AmuseAppeal> findValidByIdIn(List<Long> ids) {
		return amuseAppealDao.findByIdInAndValid(ids, CommonConstant.INT_BOOLEAN_TRUE);
	}
}
