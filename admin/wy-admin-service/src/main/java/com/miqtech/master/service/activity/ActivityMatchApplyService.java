package com.miqtech.master.service.activity;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.activity.ActivityMatchApplyDao;
import com.miqtech.master.entity.activity.ActivityMatchApply;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class ActivityMatchApplyService {

	@Autowired
	QueryDao queryDao;

	@Autowired
	ActivityMatchApplyDao activityMatchApplyDao;

	/**
	 * 通过id获取报名信息
	 */
	public Map<String, Object> getApplyById(long id) {
		String sql = "select * from activity_r_match_apply where id = " + id;
		return queryDao.querySingleMap(sql);
	}

	/**
	 * 保存约战报名记录
	 */
	public ActivityMatchApply save(ActivityMatchApply apply) {
		return activityMatchApplyDao.save(apply);
	}

	/**
	 * 获取某个约战所有报名信息
	 */
	public List<ActivityMatchApply> findAllAppliersByMatchId(long id) {
		return activityMatchApplyDao.findAllAppliersByMatchId(id);
	}

	/**
	 * 查某个约战报名信息
	 */
	public List<Map<String, Object>> queryApplyInfoByMatchId(long matchId) {
		String sqlQuery = SqlJoiner
				.join(" select a.create_date, u.nickname, u.realname, if(u.sex=1,'女','男') sex, u.telephone, u.qq, u.city_name",
						" from activity_r_match_apply a", " left join user_t_info u on u.id=a.user_id",
						" where a.is_valid=1 and a.match_id=" + matchId);

		return queryDao.queryMap(sqlQuery);
	}

	/**
	 * 根据网吧ID查约战报名信息,分页
	 */
	public PageVO applyInfoPageByMatchId(int page, long matchId) {
		if (page < 1) {
			page = 1;
		}
		PageVO pageVO = new PageVO();
		Map<String, Object> params = Maps.newHashMap();
		params.put("matchId", matchId);
		int start = (page - 1) * PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		params.put("start", start);
		params.put("rows", PageUtils.ADMIN_DEFAULT_PAGE_SIZE);

		String sqlQuery = SqlJoiner.join(
				" select a.create_date, u.nickname, u.realname, u.sex, u.telephone, u.qq, u.city_name",
				" from activity_r_match_apply a", " left join user_t_info u on u.id=a.user_id",
				" where a.is_valid=1 and a.match_id=:matchId", " limit :start, :rows");

		pageVO.setList(queryDao.queryMap(sqlQuery, params));
		pageVO.setTotal(queryApplyCountByNetbarId(matchId));
		pageVO.setCurrentPage(page);
		if ((start + PageUtils.ADMIN_DEFAULT_PAGE_SIZE) >= pageVO.getTotal()) {
			pageVO.setIsLast(1);
		}

		return pageVO;
	}

	/**
	 * 根据网吧ID查约战报名总数
	 */
	public int queryApplyCountByNetbarId(long matchId) {
		String sqlCount = " select count(1) from  activity_r_match_apply  where is_valid=1 and match_id=" + matchId;
		Number count = queryDao.query(sqlCount);
		if (null != count) {
			return count.intValue();
		}
		return 0;
	}

	public int countAppliers(Long id) {
		String sql = "select count(1) from activity_r_match_apply where match_id=" + id + " and is_valid=1 ";
		Number totalCount = queryDao.query(sql);
		return totalCount.intValue();
	}

	public void invocateUser(Long matchId, String[] ids) {
		if (ArrayUtils.isEmpty(ids)) {
			return;
		}
		List<ActivityMatchApply> applys = Lists.newArrayList();
		Date createDate = new Date();
		for (String id : ids) {
			long idLong = NumberUtils.toLong(id);
			if (idLong > 0) {
				ActivityMatchApply findByMatchIdAndUserIdAndValid = activityMatchApplyDao
						.findByMatchIdAndUserIdAndValid(matchId, idLong, 1);
				if (findByMatchIdAndUserIdAndValid == null) {
					ActivityMatchApply e = new ActivityMatchApply();
					e.setCreateDate(createDate);
					e.setMatchId(matchId);
					e.setUserId(idLong);
					e.setValid(1);
					applys.add(e);
				}
			}
		}
		if (applys.size() > 0) {
			activityMatchApplyDao.save(applys);
		}

	}

	public boolean isApplied(Long id, String ids) {
		String sql = "select count(1) from activity_r_match_apply where match_id=" + id + " and user_id in(" + ids
				+ ") and is_valid=1 ";
		Number totalCount = queryDao.query(sql);
		return totalCount.intValue() > 0;

	}

	public boolean isPhoneNumberUserApplied(Long id, String telphone) {
		String sql = "select count(1) from activity_r_match_apply apply,user_t_info info  where apply.match_id=" + id
				+ " and info.id  = apply.user_id and info.username='" + telphone
				+ "' and apply.is_valid=1 and info.is_valid =1 ";

		Number totalCount = queryDao.query(sql);
		return totalCount.intValue() > 0;

	}

	/**
	 * 分页
	 */
	public PageVO page(int page, Map<String, Object> queryParams) {
		Map<String, Object> params = Maps.newHashMap();
		String sqlCondition = " WHERE ma.is_valid = 1";
		String totalCondtion = " WHERE ma.is_valid = 1";

		String username = MapUtils.getString(queryParams, "username");
		if (StringUtils.isNotBlank(username)) {
			String likeUsername = "%" + username + "%";
			sqlCondition = SqlJoiner.join(sqlCondition, " AND u.username LIKE :username");
			params.put("username", likeUsername);
			totalCondtion = SqlJoiner.join(totalCondtion, " AND u.username LIKE '", likeUsername, "'");
		}
		String matchId = MapUtils.getString(queryParams, "matchId");
		if (NumberUtils.isNumber(matchId)) {
			sqlCondition = SqlJoiner.join(sqlCondition, " AND m.id = ", matchId);
			totalCondtion = SqlJoiner.join(totalCondtion, " AND m.id = ", matchId);
		} else {
			sqlCondition = SqlJoiner.join(sqlCondition, " AND m.id = 0");
			totalCondtion = SqlJoiner.join(totalCondtion, " AND m.id = 0");
		}
		String beginDate = MapUtils.getString(queryParams, "beginDate");
		if (StringUtils.isNotBlank(beginDate)) {
			sqlCondition = SqlJoiner.join(sqlCondition, " AND ma.create_date >= :beginDate");
			params.put("beginDate", beginDate);
			totalCondtion = SqlJoiner.join(totalCondtion, " AND ma.create_date >= '", beginDate, "'");
		}
		String endDate = MapUtils.getString(queryParams, "endDate");
		if (StringUtils.isNotBlank(endDate)) {
			sqlCondition = SqlJoiner.join(sqlCondition, " AND ma.create_date < :endDate");
			params.put("endDate", endDate);
			totalCondtion = SqlJoiner.join(totalCondtion, " AND ma.create_date < '", endDate, "'");
		}

		String limit = "";
		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page > 0) {
			Integer startRow = (page - 1) * pageSize;
			limit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());
		}

		String sql = SqlJoiner
				.join("SELECT m.id, m.title, m.item_id itemId, i.`name` itemName, u.nickname, u.username,",
						" u.create_date userCreateDate, ma.create_date applyCreateDate, n.`name` netbarName, m.create_date matchCreateDate",
						" FROM activity_r_match_apply ma LEFT JOIN activity_t_matches m ON ma.match_id = m.id",
						" LEFT JOIN user_t_info u ON ma.user_id = u.id LEFT JOIN activity_r_items i ON m.item_id = i.id",
						" LEFT JOIN netbar_t_info n ON m.netbar_id = n.id", sqlCondition,
						" ORDER BY ma.create_date DESC", limit);
		List<Map<String, Object>> list = queryDao.queryMap(sql, params);

		String totalSql = SqlJoiner.join(
				"SELECT COUNT(1) FROM activity_r_match_apply ma LEFT JOIN activity_t_matches m ON ma.match_id = m.id",
				" LEFT JOIN user_t_info u ON ma.user_id = u.id LEFT JOIN activity_r_items i ON m.item_id = i.id",
				" LEFT JOIN netbar_t_info n ON m.netbar_id = n.id", totalCondtion, " ORDER BY ma.create_date DESC");
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
	 * 网吧能否发布约战
	 */
	public boolean canPubMatch(long userId, String date) {
		String sql = "select count(1) from activity_t_matches where begin_time >='" + date
				+ "' and is_valid = 1 and by_merchant=1 and user_id=" + userId;
		Number query = queryDao.query(sql);

		return query.intValue() > 0;
	}
}