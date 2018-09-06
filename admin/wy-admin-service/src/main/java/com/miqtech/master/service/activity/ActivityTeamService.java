package com.miqtech.master.service.activity;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.activity.ActivityTeamDao;
import com.miqtech.master.entity.activity.ActivityTeam;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class ActivityTeamService {

	@Autowired
	QueryDao queryDao;
	@Autowired
	ActivityTeamDao activityTeamDao;
	@Autowired
	ActivityInfoService activityInfoService;

	public ActivityTeam save(ActivityTeam t) {
		if (t == null) {
			return null;
		}
		return activityTeamDao.save(t);
	}

	/**
	 * 战队详情
	 */
	public Map<String, Object> teamDetail(long teamId, int memberLimitSize, String userId) {
		Map<String, Object> result = null;
		String limitSql = StringUtils.EMPTY;
		boolean limitMember = memberLimitSize > 0;
		if (limitMember) {
			limitSql = " limit 0," + memberLimitSize;
		}

		// 通用参数
		Map<String, Object> paramActId = new HashMap<String, Object>();
		paramActId.put("teamId", teamId);

		// 选择战队信息
		String sqlTeam = SqlJoiner.join(
				"select c.over_time,t.qrcode,t.id team_id, t.round, t.name team_name, n.name netbar_name,n.address,a.start_time, a.end_time, a.title, a.icon activity_icon,if(date_add(a.end_time, INTERVAL 1 DAY) >= NOW(), if(a.begin_time > NOW(), 2, if(date_add(a.over_time, INTERVAL 1 DAY) < NOW(), 3, 1)), 4) status from activity_t_team t",
				" left join netbar_t_info n on n.id = t.netbar_id and n.is_valid = 1",
				" left join activity_t_info a on a.id = t.activity_id and a.is_valid = 1 left join activity_r_rounds c on t.activity_id=c.activity_id and t.round=c.round ",
				" where t.id = :teamId and t.is_valid = 1");

		result = queryDao.querySingleMap(sqlTeam, paramActId);
		if (userId != null) {
			String sql = SqlJoiner.join(
					"select is_accept,is_valid from activity_t_member where is_accept=0 and team_id=",
					String.valueOf(teamId), " and user_id=" + userId);
			// 是否已申请加入战队
			Map<String, Object> map = queryDao.querySingleMap(sql);
			if (map != null) {
				if (map.get("is_accept") != null && ((Byte) map.get("is_accept")).intValue() == 0) {
					result.put("state", 1);
				}
				if (((Byte) map.get("is_valid")).intValue() == 1) {
					result.put("state", 3);
				}
			}
			// 队长是否已邀请用户
			sql = SqlJoiner.join("select a.status from activity_r_invite a,user_t_info b where b.id=", userId,
					" and b.username=a.invited_telephone and a.type=2 and a.target_id=", String.valueOf(teamId));
			map = queryDao.querySingleMap(sql);
			if (map != null && map.get("status") != null && ((Byte) map.get("status")).intValue() == 0) {
				result.put("state", 2);
			}
			// 查找邀请记录
			sql = "select a.id invocation_id from activity_r_invite a,user_t_info b where a.invited_telephone=b.username and a.type=2 and status=0 and target_id="
					+ String.valueOf(teamId) + " and b.id=" + userId;
			map = queryDao.querySingleMap(sql);
			if (map != null && map.get("invocation_id") != null) {
				result.put("invocation_id", ((BigInteger) map.get("invocation_id")).longValue());
			}
		}
		// 查询到相应战队后的 补充信息
		if (result != null) {
			// 战队的成员
			String sqlMembers = SqlJoiner.join(
					"select u.id ,m.id member_id, m.name nickname, u.icon icon, m.telephone, m.is_monitor, m.labor from activity_t_member m ",
					"left join user_t_info u on m.user_id = u.id  where m.team_id = :teamId and m.is_valid = 1 and m.is_enter = 1  order by m.create_date desc ",
					limitSql);
			result.put("members", queryDao.queryMap(sqlMembers, paramActId));
			if (limitMember) {
				String memberCountSql = " select count(1) from activity_t_member m where m.team_id = " + teamId
						+ " and m.is_valid = 1 and m.is_enter = 1 ";
				result.put("memberCount", queryDao.query(memberCountSql));
			}
			String sql = SqlJoiner.join("SELECT COUNT(*) inviteNum FROM activity_t_member WHERE team_id=",
					String.valueOf(teamId), " AND is_valid=0 AND is_accept=0 and (is_read<>1 or is_read is null)");
			result.put("inviteNum", queryDao.query(sql));
		}
		return result;
	}

	/**
	 * 根据参数map生成个人报名信息的查询条件
	 */
	private String teamMembersConditions(Map<String, Object> params) {
		String sql = "";
		Set<String> keys = params.keySet();
		if (keys != null && !keys.isEmpty()) {
			for (Iterator<String> it = keys.iterator(); it.hasNext();) {
				String key = it.next();
				String param = (String) params.get(key);
				if (StringUtils.isNotBlank(param)) {
					if (key.equals("startDate")) {
						sql = SqlJoiner.join(sql, " AND m.create_date", " >= '", param, "'");
					} else if (key.equals("endDate")) {
						sql = SqlJoiner.join(sql, " AND m.create_date", " < ADDDATE('", param, "',INTERVAL 1 DAY)");
					} else if (key.equals("netbar_id")) {
						sql = SqlJoiner.join(sql, " AND a.", key, " = '", param, "'");
					} else if (key.startsWith("%")) {
						sql = SqlJoiner.join(sql, " AND m.", key, " LIKE '%", param, "%'");
					} else if ("name".equals(key)) {
						sql = SqlJoiner.join(sql, " AND m.name LIKE '%", param, "%'");
					} else if ("telephone".equals(key)) {
						sql = SqlJoiner.join(sql, " AND m.telephone LIKE '%", param, "%'");
					} else {
						sql = SqlJoiner.join(sql, " AND m.", key, " = '", param, "'");
					}
				}
			}
		}
		return sql;
	}

	/**
	 * 分页查询战队报名的用户信息 page: 不传入参数时不做分页
	 */
	public PageVO teamMembers(int page, Map<String, Object> params) {
		PageVO result = new PageVO();

		// 列表数据
		String sql = SqlJoiner.join(
				"SELECT n.name netbarName, t.id teamId, t.name teamName, m.id, m.is_monitor isMonitor, m.activity_id, m.name , t.server , m.id_card idCard, m.telephone, m.qq, m.labor, m.create_date createDate, m.round, g.rank, t.signed, g.seat_number seatNumber, g.group_number groupNumber,",
				" (select count(1) from activity_t_member where is_valid = 1 and activity_id = m.activity_id and team_id = m.team_id) teamCount",
				" FROM activity_t_member m", " LEFT JOIN activity_t_team t ON m.team_id = t.id AND t.is_valid = 1",
				" LEFT JOIN activity_r_apply a ON a.target_id = t.id AND a.type = 2",
				" LEFT JOIN netbar_t_info n ON a.netbar_id = n.id AND n.is_valid = 1",
				" left join activity_group g on g.target_id = t.id and g.is_team = 1 and g.is_valid = 1",
				" WHERE m.is_valid = 1 AND m.round > 0 and m.team_id != 0");
		sql = SqlJoiner.join(sql, teamMembersConditions(params));// 查询条件
		sql = SqlJoiner.join(sql,
				" ORDER BY if(teamCount >= 5, 0, 1),m.activity_id ASC, m.team_id ASC, m.id DESC, m.round ASC");// 排序方式
		if (page > 0) {
			sql = SqlJoiner.join(sql, " LIMIT ", String.valueOf((page - 1) * PageUtils.ADMIN_DEFAULT_PAGE_SIZE), ", ",
					String.valueOf(PageUtils.ADMIN_DEFAULT_PAGE_SIZE));// 分页
		}
		List<Map<String, Object>> list = queryDao.queryMap(sql);
		result.setList(list);

		// 总数
		String sqlCount = SqlJoiner.join("SELECT COUNT(1)", " FROM activity_t_member m",
				" LEFT JOIN activity_t_team t ON m.team_id = t.id AND t.is_valid = 1",
				" LEFT JOIN activity_r_apply a ON a.target_id = t.id AND a.type = 2",
				" LEFT JOIN netbar_t_info n ON a.netbar_id = n.id AND n.is_valid = 1",
				" WHERE m.is_valid = 1 AND m.round > 0 and m.team_id != 0");
		sqlCount = SqlJoiner.join(sqlCount, teamMembersConditions(params));
		Number count = queryDao.query(sqlCount);
		result.setTotal(count.longValue());
		if (page * PageUtils.ADMIN_DEFAULT_PAGE_SIZE >= count.longValue()) {
			result.setIsLast(CommonConstant.INT_BOOLEAN_TRUE);
		}
		return result;
	}

	/**
	 * 查询赛事的所有战队
	 */
	public List<ActivityTeam> getTeamsByActivityId(long activityId) {
		return activityTeamDao.findByActivityIdAndValid(activityId, CommonConstant.INT_BOOLEAN_TRUE);
	}

	public List<ActivityTeam> findValidByActivityIdAndNetbarIdAndRound(Long activityId, Long netbarId, Integer round) {
		if (activityId == null || netbarId == null) {
			return null;
		}
		if (round == null) {
			round = 1;
		}

		return activityTeamDao.findByActivityIdAndNetbarIdAndRoundAndValid(activityId, netbarId, round,
				CommonConstant.INT_BOOLEAN_TRUE);
	}

	public Map<String, Object> queryActivityNameByTeamId(Long teamId) {
		String sql = "select title,b.id id from activity_t_team a,activity_t_info b where a.activity_id=b.id and a.id="
				+ String.valueOf(teamId);
		return queryDao.querySingleMap(sql);
	}

	public ActivityTeam findOne(Long id) {
		return activityTeamDao.findOne(id);
	}

	/**
	 * 查询战队指定信息
	 * @param teamId
	 * @return team名称 team当前人数和申请人数
	 */
	public Map<String, Object> getTeamBriefInfo(Long teamId, Long userId) {
		Map<String, Object> map = new HashMap<String, Object>();
		String sql = SqlJoiner.join(
				"select count(1) enterNum , att.name team_name ,us.icon icon, att.id team_id,att.activity_id id from activity_t_member atm left join activity_t_team att on atm.team_id=att.id left join user_t_info us on us.id=atm.user_id where atm.is_valid=1 and att.is_valid=1 and atm.is_enter=1 and att.id=",
				String.valueOf(teamId), " AND att.is_valid=1");
		map = queryDao.querySingleMap(sql); //已经加入战队的人数
		sql = SqlJoiner.join("SELECT COUNT(*) inviteNum FROM activity_t_member WHERE team_id=", String.valueOf(teamId),
				" AND is_enter=0 AND is_accept=0");
		Number num = queryDao.query(sql);//申请人数
		map.put("inviteNum", num);
		ActivityTeam team = this.findOne(teamId);
		map.putAll(
				activityInfoService.qrcode(userId, team.getActivityId(), team.getRound(), team.getNetbarId(), teamId));
		return map;
	}

	//战队娱口令有效时间
	public Map<String, Object> isValidByTeamId(Long teamId) {
		Map<String, Object> result = new HashMap<String, Object>();
		String sql = "select arr.over_time overTime, arr.end_time endTime from activity_r_rounds arr left join activity_t_team att on att.activity_id=arr.activity_id where arr.is_valid=1"
				+ " and date_add(arr.over_time,INTERVAL 1 day)>now() and att.is_valid=1 and att.round=arr.round and att.id="
				+ teamId;
		Map<String, Object> info = queryDao.querySingleMap(sql);
		if (info == null) {
			result.put("code", 1);// 娱口令无效
			return result;
		} else {
			result.put("validTime", info.get("overTime")); //娱口令有效时间
			return result;
		}
	}

	//查询战队对应的赛事
	public Map<String, Object> isRoundByTeamId(Long teamId) {
		Map<String, Object> result = new HashMap<String, Object>();
		String sql = "select arr.over_time overTime, arr.end_time endTime from activity_r_rounds arr left join activity_t_team att on att.activity_id=arr.activity_id where "
				+ " att.round=arr.round and att.id=" + teamId;
		Map<String, Object> info = queryDao.querySingleMap(sql);
		result.put("validTime", info.get("overTime")); //娱口令有效时间
		return result;
	}

	//判断当前报名用户是否为战队成员
	public Map<String, Object> isCaptain(Long userId, Long teamId) {
		Map<String, Object> result = new HashMap<String, Object>();
		String sql = "select count(1) from activity_t_member where is_valid=1 and is_enter=1 and team_id=" + teamId
				+ " and user_id=" + userId;
		Number n = queryDao.query(sql);
		if (n == null || n.intValue() == 0) {
			return result;
		} else {
			result.put("code", 1);
			return result;
		}
	}

	// 签到或取消签到
	public ActivityTeam sign(Long id, Integer signed) {
		if (id == null) {
			return null;
		}

		if (signed == null) {
			signed = CommonConstant.INT_BOOLEAN_TRUE;
		}

		ActivityTeam team = findOne(id);
		if (team == null) {
			return null;
		}

		team.setSigned(signed);
		team.setUpdateDate(new Date());
		return activityTeamDao.save(team);
	}

	public Map<String, Object> getTeamByActivityIdAndNetbarIdAndRoundAndTeamName(Long activityId, Long netbarId,
			Integer round, String teamName) {
		if (activityId == null || netbarId == null || round == null || StringUtils.isBlank(teamName)) {
			return null;
		}

		String sql = SqlJoiner.join("SELECT t.id teamId FROM activity_t_team t",
				" JOIN activity_r_apply a ON t.id = a.target_id AND a.type = 2", " WHERE t.is_valid = 1 AND t.name = '",
				teamName, "' AND a.activity_id = ", activityId.toString(), " AND a.netbar_id = ", netbarId.toString(),
				" AND a.round = ", round.toString(), " ORDER BY a.create_date desc LIMIT 1");
		return queryDao.querySingleMap(sql);
	}
}