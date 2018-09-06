package com.miqtech.master.service.pc.user;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.pc.user.PcUserRetentionDao;
import com.miqtech.master.entity.pc.user.PcUserRetention;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Service
public class PcUserRetentionService {

	private static final Logger logger = LoggerFactory.getLogger(PcUserLoginHistoryService.class);

	@Resource
	private QueryDao queryDao;
	@Resource
	private PcUserRetentionDao pcUserRetentionDao;
	@Resource
	private PcUserLoginHistoryService pcUserLoginHistoryService;

	/**
	 * 批量保存对象
	 */
	public void batchSave(List<PcUserRetention> retentions) {
		if (CollectionUtils.isEmpty(retentions)) {
			return;
		}

		pcUserRetentionDao.save(retentions);
	}

	/**
	 * 通过注册时间查询留存信息
	 */
	public List<PcUserRetention> findByRegistDateIn(List<String> registDates) {
		if (CollectionUtils.isEmpty(registDates)) {
			return null;
		}

		Map<String, Object> params = Maps.newHashMap();
		String inQueryStr = this.getInQueryStr(registDates, params);

		String sql = SqlJoiner.join("SELECT * FROM pc_user_retention WHERE DATE(create_date) IN (", inQueryStr, ");");
		List<Map<String, Object>> resultMaps = queryDao.queryMap(sql, params);
		if (CollectionUtils.isEmpty(resultMaps)) {
			return null;
		}

		List<PcUserRetention> retentions = Lists.newArrayList();
		for (Map<String, Object> resultMap : resultMaps) {
			if (MapUtils.isEmpty(resultMap)) {
				continue;
			}

			PcUserRetention r = getByMap(resultMap);
			retentions.add(r);
		}
		return retentions;
	}

	/**
	 * 将map转为对象
	 */
	private PcUserRetention getByMap(Map<String, Object> retentionMap) {
		if (MapUtils.isEmpty(retentionMap)) {
			return null;
		}

		PcUserRetention r = new PcUserRetention();
		Long id = MapUtils.getLong(retentionMap, "id");
		r.setId(id);
		Date registerDate = (Date) retentionMap.get("regist_date");
		r.setRegistDate(registerDate);
		Integer registUserCount = MapUtils.getInteger(retentionMap, "regist_user_count");
		r.setRegistUserCount(registUserCount);
		Double retentionRateOne = MapUtils.getDouble(retentionMap, "retention_rate_one");
		r.setRetentionRateOne(retentionRateOne);
		Double retentionRateTwo = MapUtils.getDouble(retentionMap, "retention_rate_two");
		r.setRetentionRateTwo(retentionRateTwo);
		Double retentionRateSeven = MapUtils.getDouble(retentionMap, "retention_rate_seven");
		r.setRetentionRateSeven(retentionRateSeven);
		Double retentionRateFourteen = MapUtils.getDouble(retentionMap, "retention_rate_fourteen");
		r.setRetentionRateFourteen(retentionRateFourteen);
		Double retentionRateThirty = MapUtils.getDouble(retentionMap, "retention_rate_thirty");
		r.setRetentionRateThirty(retentionRateThirty);
		Double retentionRateAfterThirty = MapUtils.getDouble(retentionMap, "retention_rate_after_thirty");
		r.setRetentionRateAfterThirty(retentionRateAfterThirty);
		return r;
	}

	/**
	 * 产生in查询字符并设置参数
	 */
	private String getInQueryStr(List<String> dates, Map<String, Object> params) {
		String datesStr = "";
		for (int i = 0; i < dates.size(); i++) {
			if (StringUtils.isNotBlank(datesStr)) {
				datesStr = SqlJoiner.join(datesStr, ", ");
			}

			datesStr = SqlJoiner.join(datesStr, ":date", String.valueOf(i));
			params.put("date" + i, dates.get(i));
		}
		return datesStr;
	}

	/**
	 * 更新留存信息
	 */
	public void updatePreserve(List<String> dates) {
		if (CollectionUtils.isEmpty(dates)) {
			return;
		}

		// 查询已有的留存信息
		List<PcUserRetention> retentions = findByRegistDateIn(dates);

		// 统计最新的留存信息
		List<Map<String, Object>> preserveRates1 = pcUserLoginHistoryService.getPreserveRateByDates(dates, 1);
		List<Map<String, Object>> preserveRates2 = pcUserLoginHistoryService.getPreserveRateByDates(dates, 2);
		List<Map<String, Object>> preserveRates7 = pcUserLoginHistoryService.getPreserveRateByDates(dates, 7);
		List<Map<String, Object>> preserveRates14 = pcUserLoginHistoryService.getPreserveRateByDates(dates, 14);
		List<Map<String, Object>> preserveRates30 = pcUserLoginHistoryService.getPreserveRateByDates(dates, 30);

		Date now = new Date();
		List<PcUserRetention> updateRetentions = Lists.newArrayList();
		for (String dateStr : dates) {
			Date date = null;
			try {
				date = DateUtils.stringToDateYyyyMMdd(dateStr);
			} catch (ParseException e) {
				logger.error("更新留存信息时,时间格式化失败,dateStr:{}", dateStr);
				continue;
			}

			PcUserRetention updateRetention = null;
			if (CollectionUtils.isNotEmpty(retentions)) {
				Iterator<PcUserRetention> retentionsIt = retentions.iterator();
				while (retentionsIt.hasNext()) {
					PcUserRetention r = retentionsIt.next();
					if (r.getRegistDate() == null) {
						continue;
					}
					String registDateStr = DateUtils.dateToString(r.getRegistDate(), DateUtils.YYYY_MM_DD);
					if (dateStr.equals(registDateStr)) {
						updateRetention = r;
						retentionsIt.remove();
						break;
					}
				}
			}

			if (updateRetention == null) {
				updateRetention = new PcUserRetention();
				updateRetention.setRegistDate(date);
			}

			// 查询
			Map<String, Object> preserve1 = this.popPreserve(dateStr, preserveRates1);
			Integer registUserCount = MapUtils.getInteger(preserve1, "registeCount");
			if (registUserCount == null) {
				registUserCount = 0;
			}
			updateRetention.setRegistUserCount(registUserCount);
			Double retentionRateOne = this.getPreserveRateByMap(preserve1);
			updateRetention.setRetentionRateOne(retentionRateOne);
			Double retentionRateTwo = this.popPreserveRate(dateStr, preserveRates2);
			updateRetention.setRetentionRateTwo(retentionRateTwo);
			Double retentionRateSeven = this.popPreserveRate(dateStr, preserveRates7);
			updateRetention.setRetentionRateSeven(retentionRateSeven);
			Double retentionRateFourteen = this.popPreserveRate(dateStr, preserveRates14);
			updateRetention.setRetentionRateFourteen(retentionRateFourteen);
			Double retentionRateThirty = this.popPreserveRate(dateStr, preserveRates30);
			updateRetention.setRetentionRateThirty(retentionRateThirty);
			if (updateRetention.getCreateDate() == null) {
				updateRetention.setCreateDate(now);
			}
			updateRetentions.add(updateRetention);
		}

		this.batchSave(updateRetentions);
	}

	/**
	 * 从列表中查找留存率对象
	 */
	private Map<String, Object> popPreserve(String date, List<Map<String, Object>> preserves) {
		if (StringUtils.isBlank(date) || CollectionUtils.isEmpty(preserves)) {
			return null;
		}

		Iterator<Map<String, Object>> preserveRatesIt = preserves.iterator();
		while (preserveRatesIt.hasNext()) {
			Map<String, Object> pr = preserveRatesIt.next();
			String registeDate = MapUtils.getString(pr, "registeDate");
			if (StringUtils.equals(date, registeDate)) {
				return pr;
			}
		}

		return null;
	}

	/**
	 * 从留存率对象中获取留存率
	 */
	private Double getPreserveRateByMap(Map<String, Object> preserve) {
		if (MapUtils.isEmpty(preserve)) {
			return NumberUtils.DOUBLE_ZERO;
		}

		Double preserveRate = MapUtils.getDouble(preserve, "preserveRate");
		if (preserveRate == null) {
			preserveRate = NumberUtils.DOUBLE_ZERO;
		}
		if (preserveRate > 1.0) {
			preserveRate = 1.0;
		}
		return preserveRate;
	}

	/**
	 * 从列表中查找留存率
	 */
	private Double popPreserveRate(String date, List<Map<String, Object>> preserveRates) {
		if (StringUtils.isBlank(date) || CollectionUtils.isEmpty(preserveRates)) {
			return null;
		}

		Map<String, Object> pr = this.popPreserve(date, preserveRates);
		return this.getPreserveRateByMap(pr);
	}

	/**
	 * 更新最近一个月的留存率
	 */
	public void updateRecentRetetionRate() {
		this.updateRetetionRateByStartDate(null);
	}

	/**
	 * 更新所有留存率
	 */
	public void updateAllRetetionRate() {
		String startDateStr = "2016-09-25";
		Date startDate = null;
		try {
			startDate = DateUtils.stringToDateYyyyMMdd(startDateStr);
		} catch (ParseException e) {
			logger.error("时间格式化异常:", e);
		}

		this.updateRetetionRateByStartDate(startDate);
	}

	/**
	 * 从开始时间起更新留存率
	 */
	private void updateRetetionRateByStartDate(Date startDate) {
		Calendar nowCalendar = Calendar.getInstance();
		if (startDate == null) {
			startDate = DateUtils.getToday();
			startDate = DateUtils.addMonths(startDate, -1);
		}
		nowCalendar.setTime(startDate);
		long taskStartTime = DateUtils.getToday().getTime();

		List<String> dates = Lists.newArrayList();
		while (nowCalendar.getTimeInMillis() < taskStartTime) {
			nowCalendar.add(Calendar.DATE, 1);
			String dateStr = DateUtils.dateToString(nowCalendar.getTime(), DateUtils.YYYY_MM_DD);
			dates.add(dateStr);
		}

		this.updatePreserve(dates);
	}

	/**
	 * 查询分页列表
	 */
	public PageVO getPager(Integer page, Integer rows, Long startDate, Long endDate) {
		if (page == null) {
			page = 1;
		}

		String condition = " WHERE r.id > 0";
		Map<String, Object> params = Maps.newHashMap();
		if (startDate != null) {
			condition = SqlJoiner.join(condition, " AND r.regist_date >= :startDate");
			params.put("startDate", DateUtils.dateToString(new Date(startDate), DateUtils.YYYY_MM_DD_HH_MM_SS));
		}
		if (endDate != null) {
			condition = SqlJoiner.join(condition, " AND r.regist_date < ADDDATE(:endDate, INTERVAL 1 DAY)");
			params.put("endDate", DateUtils.dateToString(new Date(endDate), DateUtils.YYYY_MM_DD_HH_MM_SS));
		}

		String countSql = SqlJoiner.join("SELECT COUNT(1) count FROM `pc_user_retention` r", condition);
		Map<String, Object> totalMap = queryDao.querySingleMap(countSql, params);
		Integer total = MapUtils.getInteger(totalMap, "count");
		if (total == null) {
			total = 0;
		}

		if (rows == null || rows <= 0) {
			rows = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		}

		PageVO pager = new PageVO(page, null, total);
		if (total > 0) {
			String limitSql = null;
			if (page > 0) {
				limitSql = PageUtils.getLimitSql(page, rows);
			}

			String sql = SqlJoiner.join("SELECT r.regist_date registDate, r.regist_user_count registUserCount,",
					" r.retention_rate_one retentionRateOne, r.retention_rate_two retentionRateTwo,",
					" r.retention_rate_seven retentionRateSeven, r.retention_rate_fourteen retentionRateFourteen,",
					" r.retention_rate_thirty retentionRateThirty", " FROM `pc_user_retention` r", condition,
					" ORDER BY r.regist_date DESC", limitSql);
			List<Map<String, Object>> list = queryDao.queryMap(sql, params);
			pager.setList(list);
		}
		return pager;
	}

}
