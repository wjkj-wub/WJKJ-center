package com.miqtech.master.service.pc.user;

import com.google.common.collect.Maps;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.pc.user.PcUserLoginHistoryDao;
import com.miqtech.master.entity.pc.user.PcUserLoginHistory;
import com.miqtech.master.utils.SqlJoiner;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class PcUserLoginHistoryService {

	@Resource
	private QueryDao queryDao;
	@Resource
	private PcUserLoginHistoryDao pcUserLoginHistoryDao;

	/**
	 * 插入或更新数据
	 */
	public void insertOrUpdate(PcUserLoginHistory history) {
		if (history == null) {
			return;
		}

		pcUserLoginHistoryDao.save(history);
	}

	/**
	 * 查询日期的某天留存率
	 */
	public List<Map<String, Object>> getPreserveRateByDates(List<String> dates, Integer intervalDay) {
		if (CollectionUtils.isEmpty(dates) || intervalDay == null || intervalDay <= 0) {
			return null;
		}

		// 生成IN条件sql
		Map<String, Object> params = Maps.newHashMap();
		String inQueryStr = queryDao.getInQueryStr("date", dates, params);

		// 生成sql并执行
		String condition = SqlJoiner.join(" WHERE u.is_valid = 1 AND DATE(u.create_date) IN (", inQueryStr, ")");
		String sql = SqlJoiner.join("SELECT *, (preserveCount / registeCount) preserveRate FROM (",
				" SELECT DATE(u.create_date) registeDate, IF(ISNULL(count(DISTINCT u.id)), 0, count(DISTINCT u.id)) registeCount, count(DISTINCT lh.user_id) preserveCount",
				" FROM pc_user_info u", " LEFT JOIN pc_user_login_history lh ON u.id = lh.user_id",
				" AND DATE( ADDDATE(u.create_date, INTERVAL ", intervalDay.toString(), " DAY) ) = DATE(lh.login_date)",
				condition, " GROUP BY DATE(u.create_date)", " ORDER BY u.create_date DESC ) t;");
		return queryDao.queryMap(sql, params);
	}

	/**
	 * 查询活跃数据
	 */
	public List<Map<String, Object>> getLoginCountByDatesAndNetbarIds(List<String> dates, List<Long> netbarIds) {
		if (CollectionUtils.isEmpty(dates) || CollectionUtils.isEmpty(netbarIds)) {
			return null;
		}

		Map<String, Object> params = Maps.newHashMap();
		String datesInSql = queryDao.getInQueryStr("date", dates, params);
		String netbarIdsInSql = queryDao.getInQueryStr("netbarId", netbarIds, params);
		String sql = SqlJoiner.join("SELECT DATE(ulh.login_date) date, ulh.netbar_id netbarId, count(ulh.id) count",
				" FROM pc_user_login_history ulh", " WHERE DATE(ulh.login_date) IN (", datesInSql, ")",
				" AND (ulh.netbar_id IN (", netbarIdsInSql, ") OR ulh.netbar_id IS NULL)", " GROUP BY ulh.netbar_id, DATE(ulh.login_date)");
		return queryDao.queryMap(sql, params);
	}

	/**
	 * 查询注册数据
	 */
	public List<Map<String, Object>> getRegisterCountByDatesAndNetbarIds(List<String> dates, List<Long> netbarIds) {
		if (CollectionUtils.isEmpty(dates) || CollectionUtils.isEmpty(netbarIds)) {
			return null;
		}

		Map<String, Object> params = Maps.newHashMap();
		String datesInSql = queryDao.getInQueryStr("date", dates, params);
		String netbarIdsInSql = queryDao.getInQueryStr("netbarId", netbarIds, params);

		String sql = SqlJoiner.join("SELECT u.netbar_id netbarId, DATE(u.create_date) date, count(u.id) count",
				" FROM pc_user_info u", " WHERE DATE(u.create_date) IN (", datesInSql, ")", " AND (u.netbar_id IN (",
				netbarIdsInSql, ") OR u.netbar_id IS NULL)", " GROUP BY u.netbar_id, DATE(u.create_date)");
		return queryDao.queryMap(sql, params);
	}
}
