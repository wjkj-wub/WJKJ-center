package com.miqtech.master.service.amuse;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.amuse.AmuseActivityInfoDao;
import com.miqtech.master.dao.amuse.AmuseAppealDao;
import com.miqtech.master.dao.amuse.AmuseRewardProgressDao;
import com.miqtech.master.dao.amuse.AmuseVerifyDao;
import com.miqtech.master.entity.amuse.AmuseActivityInfo;
import com.miqtech.master.entity.amuse.AmuseAppeal;
import com.miqtech.master.entity.amuse.AmuseRewardProgress;
import com.miqtech.master.entity.amuse.AmuseVerify;
import com.miqtech.master.utils.SqlJoiner;

@Component
public class AmuseRewardProgressService {
	//0提交成功1待审核2审核通过3审核不通过4已发放5申诉待处理6申诉已处理7申诉驳回8结束9待发放10申诉通过
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private AmuseVerifyDao amuseVerifyDao;
	@Autowired
	private AmuseAppealDao amuseAppealDao;
	@Autowired
	private AmuseActivityInfoDao amuseActivityInfoDao;
	@Autowired
	private AmuseRewardProgressDao amuseRewardProgressDao;

	public AmuseRewardProgress findValidById(Long id) {
		return amuseRewardProgressDao.findByIdAndValid(id, CommonConstant.INT_BOOLEAN_TRUE);
	}

	public Map<String, Object> progress(Long activityId, Long userId, Integer finishDay) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		String sql = "";
		Map<String, Object> map = new HashMap<String, Object>();
		List<AmuseVerify> verifyList = amuseVerifyDao.findByActivityIdAndUserIdAndValid(activityId, userId, 1);
		AmuseVerify verify = null;
		Map<String, Object> finish = null;
		if (CollectionUtils.isNotEmpty(verifyList)) {
			verify = verifyList.get(0);
			map.put("verify_describes", verify.getDescribes());
			sql = SqlJoiner.join("select img from amuse_r_verify_img where verify_id=", String.valueOf(verify.getId()));
			map.put("verify_imgs", queryDao.queryMap(sql));
			if (verify.getState() == 5) {
				finish = new HashMap<String, Object>();
				finish.put("state", 8);
				finish.put("create_date", com.miqtech.master.utils.DateUtils.dateToString(verify.getFinishDate(),
						com.miqtech.master.utils.DateUtils.YYYY_MM_DD_HH_MM_SS));
			}
		}
		AmuseAppeal appeal = null;
		List<AmuseAppeal> appeals = amuseAppealDao.findByActivityIdAndUserIdAndValidAndCategoryIdIsNull(activityId,
				userId, 1);
		if (CollectionUtils.isNotEmpty(appeals)) {
			appeal = appeals.get(appeals.size() - 1);
		}
		if (appeal != null) {
			map.put("appeal_describes", appeal.getDescribes());
			sql = SqlJoiner.join("select img from amuse_r_appeal_img where appeal_id=", String.valueOf(appeal.getId()));
			map.put("appeal_imgs", queryDao.queryMap(sql));
		}

		sql = SqlJoiner.join(
				"select state,remark,create_date from amuse_reward_progress where is_valid=1 and activity_id=",
				String.valueOf(activityId), " and user_id=", String.valueOf(userId),
				" order by create_date desc,id desc");
		result.addAll(queryDao.queryMap(sql));
		if (finish != null) {
			result.add(finish);
		}
		map.put("states", result);
		int state;
		Date now = new Date();
		Date deliverDate = null;
		boolean flag = true;
		AmuseActivityInfo amuse = amuseActivityInfoDao.findOne(activityId);
		if (amuse.getDeliverDay() == null) {
			amuse.setDeliverDay(3);
		}
		for (Map<String, Object> m : result) {
			state = (Integer) m.get("state");
			addStateMsg(m, state, amuse.getDeliverDay());
			if (state == 4) {
				AmuseAppeal amuseAppeal = amuseAppealDao
						.findByActivityIdAndUserIdAndValidAndCategoryIdIsNotNull(activityId, userId, 1);
				if (now.getTime() < DateUtils.addDays((Date) m.get("create_date"), finishDay).getTime() && flag
						&& amuseAppeal == null) {
					map.put("appeal_state", 2);
					flag = false;
				}
			} else if (state == 9) {
				deliverDate = DateUtils.addDays((Date) m.get("create_date"), amuse.getDeliverDay());
				if (now.getTime() > deliverDate.getTime() && flag) {
					map.put("appeal_state", 2);
					flag = false;
				}
			} else if (state == 3) {
				List<AmuseAppeal> list = amuseAppealDao.findByActivityIdAndUserIdAndValidAndCategoryIdIsNull(activityId,
						userId, 1);
				if (now.getTime() < DateUtils.addDays((Date) m.get("create_date"), finishDay).getTime() && flag
						&& list.size() == 0) {
					map.put("appeal_state", 1);
					flag = false;
				}
			} else if (state == 2) {
				deliverDate = DateUtils.addDays((Date) m.get("create_date"), amuse.getDeliverDay());
				if (now.getTime() > deliverDate.getTime() && flag) {
					map.put("appeal_state", 2);
					flag = false;
				}
			}
		}
		if (map.get("appeal_state") == null) {
			map.put("appeal_state", 0);
		}
		sql = "select id,content from amuse_msg_feedback where is_valid=1 and type=4";
		map.put("appeal_category", queryDao.queryMap(sql));
		return map;
	}

	public void addStateMsg(Map<String, Object> m, Integer state, int deliverDay) {
		switch (state) {
		case 0:
			m.put("msg", "提交成功");
			break;
		case 1:
			m.put("msg", "等待审核");
			break;
		case 2:
			m.put("msg", "审核通过");
			break;
		case 3:
			m.put("msg", "审核不通过");
			break;
		case 4:
			m.put("msg", "奖品已发放");
			break;
		case 5:
			m.put("msg", "提交申诉成功,等待处理");
			break;
		case 6:
			m.put("msg", "申诉已经处理");
			break;
		case 7:
			m.put("msg", "申诉驳回");
			break;
		case 8:
			m.put("msg", "已结束");
			break;
		case 9:
			Date deliverDate = DateUtils.addDays((Date) m.get("create_date"), deliverDay);
			String str = com.miqtech.master.utils.DateUtils.dateToString(deliverDate,
					com.miqtech.master.utils.DateUtils.YYYY_MM_DD);
			StringBuilder sb = new StringBuilder("奖品待发放(预计");
			sb.append(str.substring(0, 4));
			sb.append("年");
			sb.append(str.substring(5, 7));
			sb.append("月");
			sb.append(str.substring(8, 10));
			sb.append("日发放完成)");
			m.put("msg", sb.toString());
			break;
		case 10:
			m.put("msg", "申诉通过");
			break;
		}
	}

	public Map<String, Object> appealProgress(Long activityId, Long userId) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		List<AmuseAppeal> list = amuseAppealDao.findByActivityIdAndUserIdAndValid(activityId, userId, 1);
		AmuseAppeal appeal = null;
		String sql = "";
		if (CollectionUtils.isNotEmpty(list)) {
			appeal = list.get(0);
			map.put("describes", appeal.getDescribes());
			sql = SqlJoiner.join(
					"select state,remark,create_date from amuse_reward_progress where type=2 and target_id=",
					String.valueOf(appeal.getId()), " order by state");
			result.addAll(queryDao.queryMap(sql));
			sql = SqlJoiner.join("select img from amuse_r_appeal_img where appeal_id=", String.valueOf(appeal.getId()));
			map.put("imgs", queryDao.queryMap(sql));
		}
		map.put("states", result);
		return map;
	}

	public AmuseRewardProgress findByTargetIdAndTypeAndStateAndValid(Long targetId, int type, int state, int valid) {
		List<AmuseRewardProgress> ps = amuseRewardProgressDao.findByTargetIdAndTypeAndStateAndValid(targetId, type,
				state, valid);
		if (CollectionUtils.isNotEmpty(ps)) {
			return ps.get(0);
		}
		return null;
	}

	public AmuseRewardProgress findByActivityIdAndUserIdAndStateAndValid(Long activityId, Long userId, int state,
			int valid) {
		List<AmuseRewardProgress> ps = amuseRewardProgressDao.findByActivityIdAndUserIdAndStateAndValid(activityId,
				userId, state, valid);
		if (CollectionUtils.isNotEmpty(ps)) {
			return ps.get(0);
		}
		return null;
	}

	public void finishProgress(Integer finishDay) {
		String date = com.miqtech.master.utils.DateUtils.dateToString(DateUtils.addDays(new Date(), -finishDay),
				com.miqtech.master.utils.DateUtils.YYYY_MM_DD_HH_MM_SS);
		String sql = "select activity_id,user_id from amuse_t_verify where is_valid=1 and (state=4 or state=2) and update_date<'"
				+ date + "'" + " and update_date>'2016-02-03'";
		List<Map<String, Object>> list = queryDao.queryMap(sql);
		if (CollectionUtils.isNotEmpty(list)) {
			for (Map<String, Object> map : list) {
				AmuseRewardProgress amuseRewardProgress = new AmuseRewardProgress();
				amuseRewardProgress.setActivityId(((Number) map.get("activity_id")).longValue());
				amuseRewardProgress.setUserId(((Number) map.get("user_id")).longValue());
				amuseRewardProgress.setType(1);
				amuseRewardProgress.setState(8);
				amuseRewardProgress.setValid(1);
				amuseRewardProgress.setCreateDate(new Date());
				amuseRewardProgressDao.save(amuseRewardProgress);

			}
		}
		sql = "update amuse_t_verify set state=5,finish_date=now() where is_valid=1 and (state=4 or state=2) and update_date<'"
				+ date + "'" + " and update_date>'2016-02-03'";//上线时需修改
		queryDao.update(sql);
	}

	/**
	 * 保存单个对象
	 */
	public AmuseRewardProgress save(AmuseRewardProgress p) {
		return amuseRewardProgressDao.save(p);
	}

	/**
	 * 保存多个对象
	 */
	public void save(List<AmuseRewardProgress> ps) {
		if (CollectionUtils.isNotEmpty(ps)) {
			amuseRewardProgressDao.save(ps);
		}
	}
}
