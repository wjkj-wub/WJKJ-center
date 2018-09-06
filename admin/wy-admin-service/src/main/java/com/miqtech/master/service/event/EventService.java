package com.miqtech.master.service.event;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.entity.Pager;
import com.miqtech.master.vo.PageVO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EventService {
	@Autowired
	private QueryDao queryDao;

	public PageVO queryEventRoundList(Long userId, Integer start, Integer pageSize) {
		Number total = queryDao
				.query("select count(1) from ( oet_event_round a, oet_event c ) where a.event_id = c.id and a.is_valid = 1");
		PageVO vo = new PageVO();
		vo.setTotal(total.intValue());
		if (start + pageSize >= total.intValue()) {
			vo.setIsLast(1);
		}
		vo.setList(queryDao
				.queryMap("select a.id round_id, c.poster, c. name, a.activity_begin, ifnull(a.max_num, 0) max_num, ifnull(b.apply_num, 0) apply_num, if ( now() < a.activity_begin and d.user_id is null, 1, 0 ) to_apply,if(c.create_user_type=1,(select nickname from user_t_info where id=c.sponsor_id),(select nickname from oet_sys_user where id=c.sponsor_id)) sponsor from (oet_event_round a, oet_event c) left join ( select round_id, count(1) apply_num from oet_event_member where is_valid = 1 group by round_id ) b on a.id = b.round_id left join oet_event_member d on a.id = d.round_id and d.is_valid=1 and user_id ="
						+ userId
						+ " where a.event_id = c.id and a.is_valid = 1 and c.is_show=1 order by case when now()<a.activity_begin then 1 else 2 end,a.activity_begin limit "
						+ start + "," + pageSize));
		return vo;
	}

	public Map<String, Object> queryEventDetail(Long userId, Long roundId, Integer start, Integer pageSize) {
		Map<String, Object> result = new HashMap<String, Object>();
		if (start == 0) {
			result = queryDao
					.querySingleMap("select b.id round_id,a.poster, a. name, b.apply_begin, b.apply_end, b.activity_begin, ifnull(c.apply_num, 0) apply_num, ifnull(b.max_num, 0) max_num, a.prize_setting, a.regime_rule,if(a.create_user_type=1,(select nickname from user_t_info where id=a.sponsor_id),(select nickname from oet_sys_user where id=a.sponsor_id)) sponsor, case when ( select id from oet_event_member where is_valid = 1 and user_id ="
							+ userId
							+ " and round_id ="
							+ roundId
							+ " ) is null then 0 else 1 end is_apply, case when ( select signed from oet_event_member where is_valid = 1 and user_id ="
							+ userId
							+ " and round_id ="
							+ roundId
							+ " ) = 1 then 1 else 0 end is_sign, a.need_sign, a.need_sign_minute, if ( now() < b.apply_begin, 1, if ( now() > b.apply_begin and now() < b.apply_end, 2, if ( b. status = 1, 3, if (b. status = 2, 4, 5)))) state from (oet_event a, oet_event_round b) left join ( select round_id, count(1) apply_num from oet_event_member where is_valid = 1 group by round_id ) c on b.id = c.round_id where a.id = b.event_id and b.id = "
							+ roundId);
			dealResult(result);
		}
		if (result != null) {
			result.put(
					"eventProcessList",
					queryDao.queryMap("select round_id, turn, name, match_time, over_time, if(min(state)=-1,1,if(min(state)=0 and max(state)=2,1,min(state))) state from ( select a.round_id, a.turn, a. name, a.match_time, a.over_time, if ( count(is_win) = count(c.id), 2, if ( count(target_id) = 2 and d. status > 1, -1, 0 )) state from oet_event_turn a left join oet_event_group b on a.turn = b.turn left join oet_event_group_seat c on b.id = c.group_id left join oet_event_round d on a.round_id = d.id where a.round_id ="
							+ roundId
							+ " and b.round_id = "
							+ roundId
							+ " and b.is_valid = 1 and c.is_valid = 1 and b.turn is not null group by a.round_id, turn, group_id ) a group by round_id, turn, name, match_time, over_time limit "
							+ start + "," + pageSize));
		}
		return result;
	}

	public void dealResult(Map<String, Object> result) {
		if (result != null && !result.isEmpty()) {
			Integer state = ((Number) result.get("state")).intValue();
			Integer isApply = ((Number) result.get("is_apply")).intValue();
			Integer isSign = ((Number) result.get("is_sign")).intValue();
			Integer needSign = result.get("need_sign") == null ? 0 : ((Number) result.get("need_sign")).intValue();
			Integer needSignMinute = result.get("need_sign_minute") == null ? 0 : ((Number) result
					.get("need_sign_minute")).intValue();
			Integer canClick = 0;
			Integer buttonType = 0;
			String tip = "";
			String buttonText = "";
			Long time = null;
			Long nowTime = System.currentTimeMillis();
			if (state == 1) {//报名未开始
				tip = "距离报名开启";
				buttonText = "报名参赛";
				time = ((Timestamp) result.get("apply_begin")).getTime() - nowTime;
			} else if (state == 2) {//报名中
				if (isApply == 0) {
					tip = "报名即将关闭";
					buttonText = "报名参赛";
					if (result.get("apply_end") != null) {
						time = ((Timestamp) result.get("apply_end")).getTime() - nowTime;
					}
					canClick = 1;
					buttonType = 1;//报名
				} else {
					long signTime = ((Timestamp) result.get("activity_begin")).getTime() - 1000 * 60 * needSignMinute;
					tip = "距离签到开启";
					buttonText = "签到完成 抽签分组";
					if (nowTime < signTime) {
						time = signTime - nowTime;
					}
					canClick = 0;
					buttonType = 3;//签到
				}
			} else if (state == 3) {//报名已结束比赛未开始
				if (isApply == 0) {
					tip = "生成对阵表中";
					buttonText = "赛事即将开始";
				} else {
					if (needSign == null || needSign == 0) {
						time = ((Timestamp) result.get("activity_begin")).getTime() - nowTime;
						buttonText = "查看对阵预览";
						if (time >= 0) {
							tip = "距离开赛";
						} else {
							tip = "赛事即将开始";
							time = null;
						}
						canClick = 1;
						buttonType = 2;//对阵图
					} else {
						long signTime = ((Timestamp) result.get("activity_begin")).getTime() - 1000 * 60
								* needSignMinute;
						if (nowTime < signTime) {
							tip = "距离签到开启";
							buttonText = "签到完成 抽签分组";
							time = signTime - nowTime;
						} else {
							if (isSign == 0) {
								tip = "签到即将关闭";
								buttonText = "立即签到 抽签分组";
								long endSignTime = ((Timestamp) result.get("activity_begin")).getTime();
								time = endSignTime - nowTime;
								canClick = 1;
								buttonType = 3;//签到
							} else {
								tip = "生成对阵表中";
								buttonText = "查看我的对阵";
								canClick = 1;
								buttonType = 4;//赛事进程
							}
						}
					}
				}
			} else if (state == 4) {//进行中
				tip = "赛事进行中";
				canClick = 1;
				if (isApply == 0) {
					buttonText = "查看赛事进程";
					buttonType = 4;//赛事进程
				} else {
					buttonText = "查看对阵信息";
					buttonType = 2;//对阵图
				}
			} else if (state == 5) {//已完结
				tip = "赛事已完结";
				canClick = 1;
				buttonType = 4;//赛事进程
				if (isApply == 0) {
					buttonText = "查看赛事结果";
				} else {
					buttonText = "查看赛事结果";
				}
			}
			result.put("tip", tip);
			result.put("buttonText", buttonText);
			result.put("time", time);
			result.put("canClick", canClick);
			result.put("buttonType", buttonType);
		}
	}

	public Map<String, Object> queryEventProcessList(Long roundId, Integer turn) {
		List<Map<String, Object>> result = queryDao
				.queryMap("select a.round_id, a.turn, a. name, a.match_time, a.over_time,if((d.status=1 or count(c.target_id)=0),0,if((d.status >1  and count(a.round_id)>count(c.target_id)),1,2)) state from oet_event_turn a left join oet_event_round  d on d.id =a.round_id left join oet_event_group b on a.turn = b.turn left join oet_event_group_seat c on b.id = c.group_id where a.round_id = "
						+ roundId
						+ " and b.round_id ="
						+ roundId
						+ " and b.is_valid = 1 and c.is_valid = 1 and b.turn is not null group by a.round_id, turn");
		List<Integer> turnList = new ArrayList<Integer>();
		Map<String, List<Map<String, Object>>> processDetailListMap = new HashMap<String, List<Map<String, Object>>>();
		String sql = "select v1.group_id,v1.round_id,sum(case when v1.score>v2.score then 1 else 0 end) a_score,sum(case when v1.score<v2.score then 1 else 0 end) b_score from(select "
				+ "a.seat_id,"
				+ "a.score,"
				+ "a.best,b.seat,b.group_id,c.round_id "
				+ "from oet_event_score a,oet_event_group_seat b,oet_event_group c "
				+ "where a.seat_id=b.id and b.seat=1 and a.is_valid=1 and b.is_valid=1 and c.id=b.group_id )"
				+ " v1,"
				+ "(select"
				+ " a.seat_id,"
				+ "a.score,"
				+ "a.best,b.seat,b.group_id,c.round_id "
				+ "from oet_event_score a,oet_event_group_seat b,oet_event_group c "
				+ "where a.seat_id=b.id and b.seat=2 and a.is_valid=1 and b.is_valid=1 and c.id=b.group_id)v2 "
				+ "where v1.group_id=v2.group_id  and v1.best=v2.best and v1.round_id="
				+ roundId
				+ " group by v1.group_id";
		List<Map<String, Object>> scoreList = queryDao.queryMap(sql);
		Map<String, Map<String, Object>> scoreListMap = new HashMap<String, Map<String, Object>>();
		if (CollectionUtils.isNotEmpty(scoreList)) {
			for (int i = 0; i < scoreList.size(); i++) {
				scoreListMap.put(scoreList.get(i).get("group_id").toString(), scoreList.get(i));
			}
		}
		if (result != null) {
			for (Map<String, Object> map : result) {
				turnList.add((Integer) map.get("turn"));
				processDetailListMap.put(map.get("turn").toString(), new ArrayList<Map<String, Object>>());
			}
			if (!turnList.isEmpty()) {
				List<Map<String, Object>> processDetailList = queryDao
						.queryMap(buildEventProcessList(roundId, turnList));

				if (CollectionUtils.isNotEmpty(processDetailList)) {
					String turnKey = null;
					for (int i = 0; i < processDetailList.size(); i++) {
						Map<String, Object> map = processDetailList.get(i);
						if (turnKey == null || 0 <= i - 1 && !turnKey.equals(processDetailList.get(i - 1).get("turn"))) {
							turnKey = map.get("turn").toString();
						}
						Map<String, Object> scoreMap = scoreListMap.get(map.get("id").toString());
						if (scoreMap != null) {
							map.put("a_score", scoreMap.get("a_score"));
							map.put("b_score", scoreMap.get("b_score"));
						}
						processDetailListMap.get(turnKey).add(map);
					}
					for (Map<String, Object> map : result) {
						map.put("detailList", processDetailListMap.get(map.get("turn").toString()));
						if (turn != null) {
							if (map.get("turn").equals(turn)) {
								map.put("expand", 1);
							} else {
								map.put("expand", 0);
							}
						}
					}
				}
			}
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("process", result);
		map.putAll(queryDao
				.querySingleMap("select a.name,a.poster from (oet_event a,oet_event_round b) where a.id=b.event_id and b.id="
						+ roundId));

		return map;
	}

	public String buildEventProcessList(Long roundId, List<Integer> turnArray) {
		String sql = "";
		String unionSql = "";
		for (Integer turn : turnArray) {
			if (!sql.equals("")) {
				unionSql = " UNION all ";
			}
			sql = String
					.join(unionSql,
							sql,
							"select d.group_number,turn, max( case when a.seat = 1 then c.icon end ) a_icon, max( case when a.seat = 1 then e.name end ) a_nickname, max( case when a.seat = 1 then a.is_win end ) a_is_win,max(case when a.seat=1 then a.seat_number end) a_seat_number, max( case when a.seat = 2 then c.icon end ) b_icon, max( case when a.seat = 2 then e.name end ) b_nickname, max( case when a.seat = 2 then a.is_win end ) b_is_win,max(case when a.seat=2 then a.seat_number end) b_seat_number,d.next_id,d.next_seat,d.id,g.status from oet_event_group_seat a left join oet_event_score b on a.id = b.seat_id left join oet_event_member e on a.target_id=e.id left join user_t_info c on e.user_id=c.id left join oet_event_group d on a.group_id=d.id left join oet_event_round g on g.id=d.round_id where a.group_id in ( select id from oet_event_group a where a.round_id ="
									+ roundId + " and a.turn = " + turn + " and a.is_valid=1) group by group_id ");
		}
		sql = "select group_number,turn,ifnull(a_icon,'uploads/imgs/user/random/default.png') a_icon,a_nickname,a_is_win,a_seat_number,ifnull(b_icon,'uploads/imgs/user/random/default.png') b_icon,b_nickname,b_is_win,b_seat_number,next_id,next_seat,id,if(a_nickname is null or b_nickname is null,0,if(status >1 and a_is_win is not null,2,if(status >1 and a_is_win is  null,1,0))) state from("
				+ sql + " order by turn,next_id,next_seat)a";
		return sql;
	}

	public void doSign(Long roundId, Long userId) {
		queryDao.update("update oet_event_member set signed=1 where is_valid=1 and user_id=" + userId
				+ " and round_id=" + roundId);
	}

	public PageVO myMatch(Long userId, Integer type, Pager pager) {
		if (type == null) {
			type = 1;
		}
		PageVO vo = new PageVO();
		String totalSql = "";
		String sql = "";
		if (type == 1) {//官方赛
			totalSql = "select count(1) from activity_t_info a left join activity_t_member b on a.id=b.activity_id where b.user_id="
					+ userId + " and b.is_valid=1";
			sql = "select a.id, a.icon, a.title, a.start_time, a.end_time, a.summary,(select count(1) from activity_t_member where is_valid=1 and activity_id=a.id) applyNum, ifnull(( select if ( date_format(now(), '%Y-%m-%d') < date_format(begin_time, '%Y-%m-%d'), 0, if ( date_format(now(), '%Y-%m-%d') <= date_format(over_time, '%Y-%m-%d'), 1, if ( date_format(now(), '%Y-%m-%d') <= date_format(end_time, '%Y-%m-%d'), 2, 3 ))) state from activity_r_rounds where activity_id = a.id and date_format(now(), '%Y-%m-%d') <= date_format(end_time, '%Y-%m-%d') order by round limit 1 ), 3 ) state,d.oet_name sponsor from activity_t_info a left join activity_t_member b on a.id = b.activity_id  left join sys_t_user d on a.create_user_id=d.id where b.user_id ="
					+ userId
					+ " and b.is_valid = 1 order by b.create_date desc limit "
					+ pager.start
					+ ","
					+ pager.pageSize;
		} else {//自发赛
			totalSql = "select count(1) from (oet_event a,oet_event_round d) left join oet_event_member b on d.id=b.round_id where a.id=d.event_id and b.user_id="
					+ userId + " and b.is_valid=1";
			sql = "select e.name item_name,d.regime,a.mode,d.id, a.poster icon, a. name title, d.apply_begin, d.apply_end, d.activity_begin start_time, a.brief summary,(select count(1) from oet_event_member where is_valid=1 and round_id=d.id) applyNum, d.max_num, if ( now() < d.apply_begin, 0, if ( now() < d.apply_end, 1, if (d. status = 3, 3, 2))) state,if(a.create_user_type=1,(select nickname from user_t_info where id=a.sponsor_id),(select nickname from oet_sys_user where id=a.sponsor_id)) sponsor from ( oet_event a, oet_event_round d ) left join oet_event_member b on d.id = b.round_id left join activity_r_items e on a.item_id=e.id where a.is_valid=1 and a.id = d.event_id and b.user_id ="
					+ userId
					+ " and b.is_valid = 1 order by b.create_date desc limit "
					+ pager.start
					+ ","
					+ pager.pageSize;
		}
		Number total = queryDao.query(totalSql);
		vo.setTotal(total.intValue());
		if (pager.start + pager.pageSize >= total.intValue()) {
			vo.setIsLast(1);
		}
		vo.setList(queryDao.queryMap(sql));
		return vo;
	}

	public Map<String, Object> brief(Long roundId) {
		String sql = "SELECT c.id, a.name, a.prize_setting,a.poster, c.max_num, c.apply_begin, c.apply_end, c.activity_begin,"
				+ " (SELECT COUNT(1) FROM oet_event_member d WHERE d.round_id = "
				+ roundId
				+ ") applyNum, "
				+ "IF(a.create_user_type = 1, (SELECT nickname FROM user_t_info WHERE id = a.sponsor_id), (SELECT nickname FROM oet_sys_user WHERE id = a.sponsor_id)) nickname "
				+ "FROM oet_event a " + "LEFT JOIN oet_event_round c " + "ON c.event_id = a.id WHERE c.id = " + roundId;
		Map<String, Object> map = queryDao.querySingleMap(sql);
		return map;
	}

	/**
	 * 查询用于app推荐的自发赛
	 */
	public List<Map<String, Object>> queryEventForAppRecommend() {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		String sql = "select id,name title from oet_event a where a.is_valid=1  order by a.create_date desc";
		result.addAll(queryDao.queryMap(sql));
		return result;
	}

	/**
	 * 比赛分数比值
	 */
	//	public List<Map<String, Object>> getScoreListByGroupId(Integer roundId) {
	//		String sql = "select v1.group_id,v1.round_id,sum(case when v1.score>=v2.score then 1 else 0 end) seatleft,sum(case when v1.score<=v2.score then 1 else 0 end) seatright from(select"
	//				+ "a.seat_id," + "a.score," + "a.best,b.seat,b.group_id,c.round_id "
	//				+ "from oet_event_score a,oet_event_group_seat b,oet_event_group c "
	//				+ "where a.seat_id=b.id and b.seat=1 and a.is_valid=1 and b.is_valid=1 and c.id=b.group_id )" + " v1,"
	//				+ "(select" + "a.seat_id," + "a.score," + "a.best,b.seat,b.group_id,c.round_id "
	//				+ "from oet_event_score a,oet_event_group_seat b,oet_event_group c "
	//				+ "where a.seat_id=b.id and b.seat=2 and a.is_valid=1 and b.is_valid=1 and c.id=b.group_id)v2 "
	//				+ "where v1.group_id=v2.group_id  and v1.best=v2.best and v1.round_id="+roundId+" group by v1.group_id";
	//		List<Map<String, Object>> result = queryDao.queryMap(sql);
	//		Map<String, Map<String, Object>> scoreListMap = new HashMap<String, Map<String, Object>>();
	//		for(int i =0 ; i<result.size();i++){
	//			scoreListMap.put(result.get(i).get("group_id").toString(),result.get(i));
	//		}
	//		return scoreListMap;
	//	}
}
