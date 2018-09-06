package com.miqtech.master.service.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.user.UserInfoDao;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

/**
 * 粉丝功能service
 */
@Component
public class UserFansService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private UserInfoDao userInfoDao;

	/**
	 * 根据用户id和粉丝id查user_t_fans表，判断是否已经是粉丝
	 */
	public boolean judgeByUseridAndFansid(Long userId, Long fansId) {
		String sqlCount = "select count(1) from user_t_fans f where f.is_valid = 1 and (f.is_black not in (1) or f.is_black is null) and f.user_id = "
				+ userId + " and f.fan_id = " + fansId;
		Number total = queryDao.query(sqlCount);
		return total.intValue() > 0;
	}

	/**
	 * 根据用户id和粉丝id查user_t_fans表，查是否有粉丝记录(包含有效和无效)
	 */
	public boolean judgeByUseridAndFansidWithoutValid(Long userId, Long fansId) {
		String sqlCount = "select count(1) from user_t_fans f where f.user_id = " + userId + " and f.fan_id = "
				+ fansId;
		Number total = (Number) queryDao.query(sqlCount);
		return total.intValue() > 0;
	}

	/**
	 * 根据用户id和拉黑用户id查user_t_fans表，查是否有拉黑记录(拉黑与is_valid状态无关)
	 */
	public boolean judgeByUseridAndBlackid(Long userId, Long blackId) {
		String sqlCount = "select count(1) from user_t_fans f where  f.user_id = " + userId + " and f.fan_id = "
				+ blackId;
		Number total = (Number) queryDao.query(sqlCount);
		return total.intValue() > 0;
	}

	/**
	 * 查用户黑名单数量(black为1)
	 */
	public int queryBlackNum(Long userId) {
		String sqlCount = "select count(1) from user_t_fans f where f.is_black =1 and f.user_id = " + userId;
		Number total = (Number) queryDao.query(sqlCount);
		return total.intValue();
	}

	/**
	 * 根据用户id和关注的人id查user_t_fans表,判断是否已经是关注的人
	 */
	public boolean judgeByUseridAndConsernid(Long userId, Long consernId) {
		String sqlCount = "select count(1) from user_t_fans f where f.is_valid = 1 and (f.is_black not in (1) or f.is_black is null) and f.user_id = "
				+ consernId + " and f.fan_id = " + userId;
		Number total = (Number) queryDao.query(sqlCount);
		return total.intValue() > 0;
	}

	/**
	 * 根据用户id和搜索昵称（开头）模糊搜索该用户的粉丝
	 */
	public List<Map<String, Object>> findFansByNickname(Long userId, String nickName) {
		List<Map<String, Object>> fansList = new ArrayList<Map<String, Object>>();
		String sqlUser = "select u.id, u.nickname, u.icon from user_t_info u where u.is_valid = 1 and u.nickname like '%"
				+ nickName + "%'";
		List<Map<String, Object>> fansUserList = queryDao.queryMap(sqlUser);
		if (null != fansUserList) {
			for (Map<String, Object> map : fansUserList) {
				if (judgeByUseridAndFansid(userId, Long.valueOf(map.get("id").toString()))) {
					fansList.add(map);
				}
			}
		} else {
			return null;
		}

		return fansList;
	}

	/**
	 * 根据用户id和搜索昵称（开头）模糊搜索该用户关注的人
	 */
	public List<Map<String, Object>> findConcernByNickname(Long userId, String nickName) {
		List<Map<String, Object>> concernList = new ArrayList<Map<String, Object>>();
		String sqlUser = "select u.id, u.nickname, u.icon from user_t_info u where u.is_valid = 1 and u.nickname like '%"
				+ nickName + "%'";
		List<Map<String, Object>> concernUserList = queryDao.queryMap(sqlUser);
		if (null != concernUserList) {
			for (Map<String, Object> map : concernUserList) {
				if (judgeByUseridAndConsernid(userId, Long.valueOf(map.get("id").toString()))) {
					concernList.add(map);
				}
			}
		} else {
			return null;
		}

		return concernList;
	}

	/**
	 * 根据用户id找到fans
	 */
	public PageVO findById(int page, int rows, Long userId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", userId);
		String limitSql = StringUtils.EMPTY;
		if (page > 0 && rows > 0) {
			limitSql = " limit :pageStart, :pageNum ";
			params.put("pageStart", (page - 1) * rows);
			params.put("pageNum", rows);
		}
		String sqlQueFan = "select f.fan_id from user_t_fans f where f.user_id = :userId and f.is_valid = 1 and (f.is_black not in (1) or f.is_black is null) order by create_date desc "
				+ limitSql;

		List<Map<String, Object>> fansList = queryDao.queryMap(sqlQueFan, params); //查出user_t_fans表中，该用户id的所有粉丝id
		//查询出所有粉丝id的用户对象，逐一放入list中
		List<Map<String, Object>> userInfoList = new ArrayList<>();
		for (Map<String, Object> map : fansList) {
			String sqlQueUser = "select u.id, u.icon, u.nickname from user_t_info u where u.id = " + map.get("fan_id");
			Map<String, Object> fansMap = queryDao.querySingleMap(sqlQueUser);
			//判断是否关注了该粉丝
			if (judgeByUseridAndConsernid(userId, Long.parseLong(map.get("fan_id").toString()))) {
				fansMap.put("is_valid", 1);//已关注，is_valid为IOS端自定义字段
			} else {
				fansMap.put("is_valid", 0);//未关注
			}
			userInfoList.add(fansMap);
		}

		PageVO vo = new PageVO();
		vo.setList(userInfoList);
		// 分页
		String sqlCount = "select count(1) from user_t_fans f where f.is_valid = 1 and (f.is_black not in (1) or f.is_black is null) and f.user_id = "
				+ userId;
		Number total = (Number) queryDao.query(sqlCount);
		vo.setTotal(total.longValue());
		if (page * rows >= total.intValue()) {
			vo.setIsLast(1);
		}
		return vo;
	}

	/**
	 * 该用户id此处作为粉丝id，找到关注的人
	 */
	public PageVO findByFansId(int page, int rows, Long userId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", userId);
		String sqlQueFan = "select f.user_id from user_t_fans f where f.fan_id = :userId and f.is_valid = 1 and (f.is_black not in (1) or f.is_black is null) order by create_date desc ";
		List<Map<String, Object>> fansList = queryDao.queryMap(sqlQueFan, params); //查出user_t_fans表中，粉丝id为该userId的所有用户id
		//查询出所有用户id的用户对象，逐一放入list中
		List<Map<String, Object>> userInfoList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(fansList)) {
			for (Map<String, Object> map : fansList) {
				String sqlQueUser = "select u.id, u.icon, u.nickname from user_t_info u where u.id = "
						+ map.get("user_id");
				Map<String, Object> concernMap = queryDao.querySingleMap(sqlQueUser);
				concernMap.put("is_valid", 1);//默认设置已关注
				userInfoList.add(concernMap);
			}
		}

		PageVO vo = new PageVO();
		vo.setList(userInfoList);
		vo.setTotal(userInfoList.size());
		vo.setIsLast(1);
		return vo;
	}

	/**
	 * 查用户拉黑的名单
	 */
	public PageVO findBlackListById(int page, int rows, Long userId) {
		String sqlQueBlack = "select f.fan_id from user_t_fans f where f.user_id = :userId   and f.is_black = 1 ";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", userId);
		List<Map<String, Object>> blackList = queryDao.queryMap(sqlQueBlack, params); //查出user_t_fans表中，被用户拉黑的fan_id
		//查询出所有用户id的用户对象，逐一放入list中
		List<Map<String, Object>> userInfoList = new ArrayList<>();
		for (Map<String, Object> map : blackList) {
			String sqlQueUser = "select u.id, u.nickname, u.icon, 1 status from user_t_info u where u.id = "
					+ map.get("fan_id");
			userInfoList.add(queryDao.querySingleMap(sqlQueUser));
		}

		PageVO vo = new PageVO();
		vo.setList(userInfoList);
		vo.setIsLast(1);
		vo.setTotal(userInfoList.size());
		//		// 分页
		//		String sqlCount = "select count(1) from user_t_fans f where   f.is_black = 1 and f.user_id = " + userId;
		//		Number total = (Number) queryDao.query(sqlCount);
		//		if (page * rows >= total.intValue()) {
		//		}
		return vo;
	}

	/**
	 * 查询我是否被拉黑
	 */
	public boolean judgeIsBlack(Long userId, Long concernId) {
		String sqlCount = "select count(1) from user_t_fans where user_id = " + concernId + " and fan_id = " + userId
				+ " and is_black = 1";
		Number total = (Number) queryDao.query(sqlCount);
		return total.intValue() > 0;
	}

	/**
	 * 新增关注的人记录
	 */
	public void addConcern(Long userId, Long concernId) {
		String sqlConcern = "insert into user_t_fans(user_id, fan_id, is_valid, is_black, create_date, update_date, create_user_id, update_user_id) values('"
				+ concernId + "', '" + userId + "', 1, 0, NOW(), NOW(), '" + userId + "', '" + userId + "')";
		queryDao.update(sqlConcern);
	}

	/**
	 * 新增状态为拉黑：1 的记录
	 */
	public void addBlack(Long userId, Long blackUserId) {
		String sqlBlack = "insert into user_t_fans(user_id, fan_id, is_valid, is_black, create_date, update_date, create_user_id, update_user_id) values('"
				+ userId + "', '" + blackUserId + "', 0, 1, NOW(), NOW(), '" + userId + "', '" + userId + "')";
		queryDao.update(sqlBlack);
	}

	/**
	 * 更新拉黑状态（拉黑不关心is_valid的状态）
	 */
	public void updateBlack(Long userId, Long blackUserId, int type) {
		String sqlBlack = "update user_t_fans f set f.is_black = " + type + ",f.update_date = NOW(),f.update_user_id = "
				+ userId + " where f.user_id = " + userId + " and f.fan_id = " + blackUserId;
		queryDao.update(sqlBlack);
	}

	/**
	 * 根据用户id和关注的人更新user_t_fans表状态
	 */
	public void updateValid(Long userId, Long consernId, int type) {
		String sqlConcern = "update user_t_fans f set f.is_valid = " + type
				+ ",f.update_date = NOW(),f.update_user_id = " + userId + " where f.user_id = " + consernId
				+ " and f.fan_id = " + userId;
		queryDao.update(sqlConcern);
	}

	/**
	 * 根据用户id统计赛事，粉丝，关注的人数量
	 */
	public Map<String, Number> statistics(Long userId) {
		UserInfo user = userInfoDao.findOne(userId);
		Map<String, Number> statisticsMap = Maps.newHashMap();
		Number activityTotal = 0;
		Number fansTotal = 0;
		Number concernTotal = 0;
		if (user != null && userId < 4200000) {
			String sqlActivityCount = "select count( distinct  activity_id) id  from activity_t_member am  where am.is_valid = 1  and am.user_id = "
					+ userId; //该用户活动数量
			String sqlFansCount = "select count(1) from user_t_fans f where f.is_valid = 1 and (f.is_black not in (1) or f.is_black is null) and f.user_id = "
					+ userId; //该用户粉丝数量
			String sqlConcernCount = "select count(1) from user_t_fans f where f.is_valid = 1 and (f.is_black not in (1) or f.is_black is null) and f.fan_id = "
					+ userId; //该用户关注的人数量
			Map<String, Object> params = Maps.newHashMap();
			params.put("username", user.getUsername());
			//			String sqlActivityMatchInviteCount = "select     count(1) inviteNum from    activity_r_invite i    LEFT JOIN activity_t_matches m        ON i.target_id = m.id "
			//					+ " AND m.is_valid = 1 where i.type = 1    and i.is_valid = 1    and i.status = 0    and i.invited_telephone = :username    AND m.is_valid = 1    AND m.is_start = 0    AND m.begin_time >= NOW() ";
			//			Map<String, Object> inviteMatchMap = queryDao.querySingleMap(sqlActivityMatchInviteCount, params);
			//			if (MapUtils.isNotEmpty(inviteMatchMap)) {
			//				Number count = (Number) inviteMatchMap.get("inviteNum");
			//				statisticsMap.put("activityMatchInviteCount", count.intValue());//约战邀请数量
			//			}
			statisticsMap.put("activityMatchInviteCount", 0);//约战邀请数量4.0版本后删除约战
			String sqlActivityInviteCount = SqlJoiner.join("SELECT count(1) inviteNum FROM activity_r_invite i",
					" LEFT JOIN activity_t_team t ON i.target_id = t.id LEFT JOIN activity_t_info ai ON t.activity_id = ai.id AND t.is_valid = 1",
					" WHERE i.type = 2 AND i.is_valid = 1 AND i.status = 0 AND i.invited_telephone = :username AND t.is_valid = 1 AND ai.begin_time <= NOW() AND date_add(ai.over_time, INTERVAL 1 DAY) > NOW()");
			Map<String, Object> inviteActivityMap = queryDao.querySingleMap(sqlActivityInviteCount, params);
			if (MapUtils.isNotEmpty(inviteActivityMap)) {
				Number count = (Number) inviteActivityMap.get("inviteNum");
				statisticsMap.put("activityInviteCount", count.intValue());//战队邀请数量
			}

			String sqlUnEvaOrderCount = "select     count(1) from    netbar_r_order o where "
					+ "o.id not in    (select      order_id    from        netbar_t_evaluation     where is_valid = 1        and user_id = "
					+ userId + ")  "
					+ "  and o.is_valid >= 1  and status>=1  and o.total_amount > 0    and o.reserve_id = 0    and o.user_id = "
					+ userId;
			Number unEvaOrderCount = (Number) queryDao.query(sqlUnEvaOrderCount);
			statisticsMap.put("unEvaOrderCount", unEvaOrderCount);

			activityTotal = (Number) queryDao.query(sqlActivityCount);

			fansTotal = (Number) queryDao.query(sqlFansCount);

			concernTotal = (Number) queryDao.query(sqlConcernCount);
		}
		statisticsMap.put("activityTotal", activityTotal);
		statisticsMap.put("fansTotal", fansTotal);
		statisticsMap.put("concernTotal", concernTotal);
		return statisticsMap;
	}
}
