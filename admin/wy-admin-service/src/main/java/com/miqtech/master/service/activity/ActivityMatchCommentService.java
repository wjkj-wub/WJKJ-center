package com.miqtech.master.service.activity;

import java.math.BigInteger;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.activity.ActivityMatchCommentDao;
import com.miqtech.master.dao.user.UserInfoDao;
import com.miqtech.master.entity.activity.ActivityMatchComment;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.vo.PageVO;

@Component
public class ActivityMatchCommentService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private UserInfoDao userInfoDao;
	@Autowired
	private ActivityMatchCommentDao activityMatchCommentDao;

	public ActivityMatchComment save(ActivityMatchComment comment) {
		return activityMatchCommentDao.save(comment);
	}

	/**
	 * 查询评论信息
	 */
	public PageVO getComments(long matchId, int page, int rows, int tmpTotalCount) {
		String countSql = "select count(1) from activity_r_match_comment where match_id = " + matchId
				+ " and is_valid = 1";
		BigInteger count = (BigInteger) queryDao.query(countSql);
		int limitStart;
		if (tmpTotalCount == 0) {
			limitStart = (page - 1) * rows;
		} else {
			limitStart = ((page - 1) * rows + (count.intValue() - tmpTotalCount));
		}
		String sqlComments = "select mc.id, mc.content, mc.score, DATE_FORMAT(mc.create_date, '%Y-%m-%d %H:%i:%s') create_date,ui.id user_id, ui.nickname user_nickname, ui.icon user_icon from activity_r_match_comment mc left join user_t_info ui on mc.user_id = ui.id and ui.is_valid = 1 where mc.match_id = "
				+ matchId + " and mc.is_valid = 1 order by mc.create_date desc limit " + limitStart + ", " + rows;
		PageVO vo = new PageVO();
		vo.setList(queryDao.queryMap(sqlComments));
		if (page * rows >= count.intValue()) {
			vo.setIsLast(1);
		}
		return vo;
	}

	/**
	 * 发送评论
	 */
	public Map<String, Object> send(long matchId, long userId, String content, int score) {
		ActivityMatchComment activityMatchComment = new ActivityMatchComment();
		activityMatchComment.setMatchId(matchId);
		activityMatchComment.setUserId(userId);
		activityMatchComment.setContent(content);
		activityMatchComment.setScore(score);
		activityMatchComment.setCreateDate(new Date());
		activityMatchCommentDao.save(activityMatchComment);
		Map<String, Object> result = Maps.newHashMap();
		result.put("id", activityMatchComment.getId());
		result.put("content", content);
		result.put("create_date",
				DateUtils.dateToString(activityMatchComment.getCreateDate(), DateUtils.YYYY_MM_DD_HH_MM_SS));
		result.put("score", score);
		UserInfo user = userInfoDao.findOne(userId);
		result.put("user_icon", user.getIcon());
		result.put("user_id", userId);
		result.put("user_nickname", user.getNickname());
		return result;
	}
}