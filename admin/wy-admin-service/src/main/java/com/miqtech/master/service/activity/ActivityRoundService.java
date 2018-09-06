package com.miqtech.master.service.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.activity.ActivityRoundDao;
import com.miqtech.master.entity.activity.ActivityRound;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.utils.StringUtils;

@Component
public class ActivityRoundService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private ActivityRoundDao activityRoundDao;

	public ActivityRound findById(Long id) {
		return activityRoundDao.findByIdAndValid(id, CommonConstant.INT_BOOLEAN_TRUE);
	}

	public ActivityRound save(ActivityRound round) {
		if (round != null) {
			Date now = new Date();
			round.setUpdateDate(now);

			if (round.getId() != null) {
				ActivityRound oldBean = findById(round.getId());
				round = BeanUtils.updateBean(oldBean, round);
			} else {
				round.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				round.setCreateDate(now);
			}
			return activityRoundDao.save(round);
		}
		return null;
	}

	public List<ActivityRound> getRoundsByActivityId(long activityId) {
		return activityRoundDao.findByActivityIdAndValid(activityId, CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * 通过id和场次号删除活动
	 */
	public void deleteByActivityIdAndRound(Long activityId, Integer round) {
		String sql = SqlJoiner.join("UPDATE activity_r_rounds SET is_valid = 0 WHERE activity_id = ",
				activityId.toString(), " AND round = ", round.toString(), " AND is_valid = 1");
		queryDao.update(sql);
	}

	/**
	 * 查询某项赛事的场次信息，及场次设置的网吧列表
	 */
	public List<Map<String, Object>> findActivityRoundNetbarInfo(Long activityId) {
		String sql = SqlJoiner
				.join("select id, activity_id activityId, round, netbars, begin_time beginTime, over_time overTime, end_time endTime from activity_r_rounds where activity_id = ",
						activityId.toString(), " and is_valid = 1");
		List<Map<String, Object>> rounds = queryDao.queryMap(sql);

		if (CollectionUtils.isNotEmpty(rounds)) {
			for (Map<String, Object> r : rounds) {
				String netbarsStr = MapUtils.getString(r, "netbars");
				if (StringUtils.isNotBlank(netbarsStr)) {
					String netbarsSql = SqlJoiner.join("select id, name from netbar_t_info where id in (", netbarsStr,
							") and is_valid = 1 and is_release = 1");
					List<Map<String, Object>> netbars = queryDao.queryMap(netbarsSql);
					r.put("netbars", netbars);
				} else {
					r.remove("netbars");
				}
			}
		}

		return rounds;
	}

	/**
	 * 查询某项赛事的场次信息，及场次下的战队列表
	 */
	public List<Map<String, Object>> findActivityRoundTeamInfo(Long activityId) {
		String sql = SqlJoiner.join("select id, round, netbars from activity_r_rounds where activity_id = ",
				activityId.toString(), " and is_valid = 1");
		List<Map<String, Object>> rounds = queryDao.queryMap(sql);

		String teamsSql = SqlJoiner.join("select * from activity_t_team where activity_id = ", activityId.toString(),
				" and is_valid = 1");
		List<Map<String, Object>> teams = queryDao.queryMap(teamsSql);
		if (CollectionUtils.isNotEmpty(teams)) {
			for (Map<String, Object> r : rounds) {
				Integer roundNum = MapUtils.getInteger(r, "round");
				if (roundNum != null) {
					List<Map<String, Object>> rTeams = new ArrayList<Map<String, Object>>();
					for (Map<String, Object> t : teams) {
						Integer tRoundNum = MapUtils.getInteger(t, "round");
						if (roundNum.equals(tRoundNum)) {
							rTeams.add(t);
						}
					}
					r.put("teams", rTeams);
				}
			}
		}

		return rounds;
	}

	/**
	 * 查询赛事场次及网吧信息
	 */
	public List<Map<String, Object>> getRoundsNetbarInfo(Long activityId) {
		String sql = SqlJoiner.join("SELECT ar.round, ni.id, ni.name FROM activity_r_rounds ar, netbar_t_info ni",
				" WHERE FIND_IN_SET(ni.id, ar.netbars) AND ar.activity_id = ", activityId.toString(),
				" order by ar.round");
		return queryDao.queryMap(sql);
	}

	public boolean isOverdue(String activityId, String round) {
		Number n = queryDao.query("select count(1) from activity_r_rounds where activity_id=" + activityId
				+ " and round=" + round + " and now()<over_time");
		if (n.intValue() <= 0) {
			return true;
		}
		return false;
	}

	public ActivityRound findValidByActivityIdAndRound(Long activityId, Integer round) {
		if (activityId == null) {
			return null;
		}
		if (round == null || round.intValue() <= 0) {
			round = 1;
		}

		List<ActivityRound> rounds = activityRoundDao.findByActivityIdAndRoundAndValid(activityId, round,
				CommonConstant.INT_BOOLEAN_TRUE);
		if (CollectionUtils.isEmpty(rounds)) {
			return null;
		}

		return rounds.get(rounds.size() - 1);
	}

	public List<Map<String, Object>> roundInfo(Long id) {
		return queryDao
				.queryMap("select if ( now() < begin_time, '预热', if ( now() < over_time, '报名', '进行' )) state, group_concat( concat( month (over_time), '月', day (over_time), '日' )) date from activity_r_rounds where is_valid = 1 and now() < end_time and activity_id = "
						+ id + " group by state order by state");
	}
}
