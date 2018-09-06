package com.miqtech.master.service.activity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.activity.ActivityGroupDao;
import com.miqtech.master.entity.activity.ActivityGroup;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.utils.StringUtils;

@Component
public class ActivityGroupService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private ActivityGroupDao activityGroupDao;

	public ActivityGroup findById(Long id) {
		return activityGroupDao.findByIdAndValid(id, CommonConstant.INT_BOOLEAN_TRUE);
	}

	public List<Map<String, Object>> groupList(Integer activityId, Integer netbarId, Integer round, Integer type) {
		String sql = "select a.id,target_id target_id,b.name targetName,group_number groupNumber,seat_number seatNumber, seat, rank from"
				+ " activity_group a ";
		if (type == 1) {
			sql += " left join activity_t_member b ";
		} else {
			sql += " left join activity_t_team b ";
		}
		sql += " on a.target_id=b.id and b.is_valid=1 ";

		sql += " where a.is_valid=1 and a.activity_id=" + activityId + " and a.netbar_id=" + netbarId + " and a.round="
				+ round + " and a.is_team=" + (type == 2 ? 1 : 0);
		return queryDao.queryMap(sql);
	}

	public ActivityGroup save(ActivityGroup round) {
		if (round != null) {
			Date now = new Date();
			round.setUpdateDate(now);

			if (round.getId() != null) {
				ActivityGroup oldBean = findById(round.getId());
				round = BeanUtils.updateBean(oldBean, round);
			} else {
				round.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				round.setCreateDate(now);
			}
			return activityGroupDao.save(round);
		}
		return null;
	}

	/**
	* 生成分组
	* @param activityId
	* @param netbarId
	* @param round
	* @param type
	* @return
	*/
	public List<ActivityGroup> generateGroup(Integer activityId, Integer netbarId, Integer round, Integer type) {
		List<ActivityGroup> groups = new ArrayList<ActivityGroup>();
		String sql = StringUtils.EMPTY;
		if (type == 1) {
			sql = SqlJoiner.join("SELECT a.activity_id activityId,a.netbar_id netbarId,m.id targetId, ",
					" m. NAME targetName FROM activity_r_apply a ",
					" JOIN activity_t_member m on a.target_id = m.id and m.is_valid = 1 ",
					" WHERE a.activity_id =" + activityId, " and a.round = " + round, " AND a.netbar_id =" + netbarId,
					" AND a.is_valid = 1", " AND a.type =" + type, " ORDER BY ", "	a.create_date;");
		} else {
			sql = SqlJoiner.join("SELECT a.activity_id activityId,a.netbar_id netbarId,m.id targetId, ",
					" ( SELECT count(1) FROM activity_t_member tmp WHERE tmp.is_valid = 1 AND tmp.activity_id = m.activity_id AND tmp.team_id = m.id ) memberCount,",
					" m.NAME targetName FROM activity_r_apply a ",
					" JOIN activity_t_team m on a.target_id = m.id and m.is_valid = 1 ",
					" WHERE a.activity_id =" + activityId, " and a.round = " + round, " AND a.netbar_id =" + netbarId,
					" AND a.is_valid = 1", " AND a.type =" + type, " HAVING memberCount >= 5 ORDER BY a.create_date;");
		}
		List<Map<String, Object>> rounds = queryDao.queryMap(sql);
		if (rounds != null && rounds.size() > 0) {
			int count = rounds.size();
			int groupCount = count % 2 == 0 ? count / 2 : (count / 2 + 1);
			int seatNumber = 0;
			for (int i = 0; i < groupCount; i++) {
				ActivityGroup groupu = new ActivityGroup();
				ActivityGroup groupd = new ActivityGroup();
				groupu.setActivityId(activityId);
				groupu.setNetbarId(netbarId);
				groupu.setTargetId(((Number) rounds.get(seatNumber).get("targetId")).intValue());
				groupu.setTargetName((String) rounds.get(seatNumber).get("targetName"));
				groupu.setGroupNumber(i + 1);
				groupu.setRound(round);
				groupu.setCreateDate(new Date());
				groupu.setSeatNumber(seatNumber + 1);
				groupu.setSeat(1);
				groupu.setIsTeam(type == 1 ? 0 : 1);
				groupu.setValid(1);
				seatNumber++;
				groups.add(groupu);
				activityGroupDao.save(groupu);
				if (seatNumber + 1 > count) {
					break;
				}
				groupd.setActivityId(activityId);
				groupd.setNetbarId(netbarId);
				groupd.setTargetId(((Number) rounds.get(seatNumber).get("targetId")).intValue());
				groupd.setTargetName((String) rounds.get(seatNumber).get("targetName"));
				groupd.setGroupNumber(i + 1);
				groupd.setRound(round);
				groupd.setCreateDate(new Date());
				groupd.setSeatNumber(seatNumber + 1);
				groupd.setSeat(2);
				groupd.setValid(1);
				groupd.setIsTeam(type == 1 ? 0 : 1);
				groups.add(groupd);
				activityGroupDao.save(groupd);
				seatNumber++;
			}
		}
		return groups;
	}

	/**
	 * 随机排列
	 * @param activityId
	 * @param netbarId
	 * @param round
	 * @param type
	 * @return
	 */
	public List<ActivityGroup> generateRandGroup(Integer activityId, Integer netbarId, Integer round, Integer type) {
		List<ActivityGroup> groups = findValidByActivityIdAndRoundAndNetbarIdAndIsTeam(activityId, round, netbarId,
				type == 2);
		if (CollectionUtils.isEmpty(groups)) {
			return null;
		}

		List<Integer> targetIds = Lists.newArrayList();
		for (ActivityGroup g : groups) {
			targetIds.add(g.getTargetId());
			g.setTargetId(null);
		}

		targetIds.sort(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return RandomUtils.nextInt(0, 2) > 0 ? 1 : -1;
			}
		});

		for (int i = 0; i < groups.size(); i++) {
			groups.get(i).setTargetId(targetIds.get(i));
		}
		return (List<ActivityGroup>) activityGroupDao.save(groups);
	}

	public List<ActivityGroup> findValidByActivityIdAndRoundAndNetbarIdAndIsTeam(Integer activityId, Integer round,
			Integer netbarId, boolean isTeam) {
		if (activityId == null || netbarId == null) {
			return null;
		}
		if (round == null) {
			round = 1;
		}

		String isTeamStr = isTeam ? "1" : "0";
		String sql = SqlJoiner.join("SELECT * FROM activity_group WHERE is_valid = 1 AND activity_id = ",
				activityId.toString(), " AND round = ", round.toString(), " AND netbar_id = ", netbarId.toString(),
				" AND is_team = ", isTeamStr);
		List<Map<String, Object>> list = queryDao.queryMap(sql);
		if (CollectionUtils.isEmpty(list)) {
			return null;
		}

		List<ActivityGroup> groups = Lists.newArrayList();
		for (Map<String, Object> obj : list) {
			ActivityGroup g = new ActivityGroup();
			g.setId(MapUtils.getLong(obj, "id"));
			g.setActivityId(MapUtils.getInteger(obj, "activity_id"));
			g.setNetbarId(MapUtils.getInteger(obj, "netbar_id"));
			g.setTargetId(MapUtils.getInteger(obj, "target_id"));
			g.setRound(MapUtils.getInteger(obj, "round"));
			g.setGroupNumber(MapUtils.getInteger(obj, "group_number"));
			g.setSeatNumber(MapUtils.getInteger(obj, "seat_number"));
			g.setSeat(MapUtils.getInteger(obj, "seat"));
			g.setRank(MapUtils.getInteger(obj, "rank"));
			g.setIsTeam(MapUtils.getInteger(obj, "is_team"));
			g.setValid(MapUtils.getInteger(obj, "is_valid"));
			g.setUpdateUserId(MapUtils.getLong(obj, "update_user_id"));
			g.setCreateUserId(MapUtils.getLong(obj, "create_user_id"));
			g.setUpdateDate((Date) obj.get("update_date"));
			g.setCreateDate((Date) obj.get("create_date"));
			groups.add(g);
		}

		return groups;
	}

	/**
	 * 设置名次
	 * @param groupId
	 * @param rank
	 */
	public void updateRank(Long groupId, Integer rank) {
		String sql1 = SqlJoiner.join("UPDATE activity_group a INNER JOIN ",
				"(select activity_Id,netbar_id,round,is_team from activity_group where id=" + groupId + ") b on  ",
				"a.activity_Id=b.activity_Id and a.netbar_id=b.netbar_id and a.round =b.round and a.is_team=b.is_team and a.rank="
						+ rank,
				" SET rank = null WHERE  a.is_valid = 1 ");
		String sql2 = SqlJoiner.join("UPDATE activity_group SET rank =" + rank, " WHERE id = ", groupId.toString(),
				" AND is_valid = 1");
		queryDao.update(sql1);
		queryDao.update(sql2);
	}

	/**
	 * 查询最后一条分组信息
	 */
	public Map<String, Object> queryLastByActivityIdAndNetbarIdAndRound(Long activityId, Long netbarId, Integer round,
			Integer isTeam) {
		if (activityId == null || netbarId == null || round == null) {
			return null;
		}

		String sql = SqlJoiner.join(
				"SELECT id, group_number groupNumber, seat_number seatNumber FROM activity_group WHERE activity_id = ",
				activityId.toString(), " AND netbar_id = ", netbarId.toString(), " AND round = ", round.toString(),
				" AND is_valid = 1 AND is_team = ", isTeam.toString(), " ORDER BY seat_number DESC LIMIT 1");
		return queryDao.querySingleMap(sql);
	}

	public void resetGroups(Long activityId, Integer round, Long netbarId, boolean isTeam) {
		if (activityId == null || round == null || netbarId == null) {
			return;
		}

		Date now = new Date();
		List<ActivityGroup> groups = findValidByActivityIdAndRoundAndNetbarIdAndIsTeam(activityId.intValue(), round,
				netbarId.intValue(), isTeam);
		if (CollectionUtils.isEmpty(groups)) {
			return;
		}
		for (ActivityGroup g : groups) {
			g.setValid(CommonConstant.INT_BOOLEAN_FALSE);
			g.setUpdateDate(now);
		}
		activityGroupDao.save(groups);
	}

}
