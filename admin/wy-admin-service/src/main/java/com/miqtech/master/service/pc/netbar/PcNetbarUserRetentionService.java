package com.miqtech.master.service.pc.netbar;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.pc.netbar.PcNetbarUserRetentionDao;
import com.miqtech.master.entity.pc.netbar.PcNetbarUserRetention;
import com.miqtech.master.service.pc.user.PcUserLoginHistoryService;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.SqlJoiner;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class PcNetbarUserRetentionService {

	private static final Logger logger = LoggerFactory.getLogger(PcNetbarUserRetention.class);

	@Resource
	private QueryDao queryDao;
	@Resource
	private PcNetbarUserRetentionDao netbarUserRetentionDao;
	@Resource
	private PcUserLoginHistoryService pcUserLoginHistoryService;

	/**
	 * 批量保存数据
	 */
	public void batchSave(List<PcNetbarUserRetention> retentions) {
		if (CollectionUtils.isEmpty(retentions)) {
			return;
		}

		netbarUserRetentionDao.save(retentions);
	}

	/**
	 * 通过日期和网吧列表查询留存记录
	 */
	public List<PcNetbarUserRetention> findByDateInAndNetbarIdIn(List<String> dates, List<Long> netbarIds) {
		if (CollectionUtils.isEmpty(dates) || CollectionUtils.isEmpty(netbarIds)) {
			return null;
		}

		String sql = "SELECT * FROM `pc_netbar_user_retention`";
		Map<String, Object> params = Maps.newHashMap();

		// 组装dates参数
		String datesInSql = queryDao.getInQueryStr("date", dates, params);
		String netbarIdsInSql = queryDao.getInQueryStr("netbarId", netbarIds, params);
		sql = SqlJoiner.join(sql, " WHERE DATE(date) IN (", datesInSql, ") AND (netbar_id IN (", netbarIdsInSql,
				") OR netbar_id IS NULL)");

		// 查询数据
		List<Map<String, Object>> retentionMaps = queryDao.queryMap(sql, params);
		if (CollectionUtils.isEmpty(retentionMaps)) {
			return null;
		}

		// 转换map为对象
		List<PcNetbarUserRetention> retentions = Lists.newArrayList();
		for (Map<String, Object> retentionMap : retentionMaps) {
			PcNetbarUserRetention retention = this.getByRetentionMap(retentionMap);
			if (retention != null) {
				retentions.add(retention);
			}
		}
		return retentions;
	}

	/**
	 * 转换map为对象
	 */
	public PcNetbarUserRetention getByRetentionMap(Map<String, Object> retentionMap) {
		if (MapUtils.isEmpty(retentionMap)) {
			return null;
		}

		PcNetbarUserRetention retention = new PcNetbarUserRetention();
		Long id = MapUtils.getLong(retentionMap, "id");
		retention.setId(id);
		Date date = (Date) retentionMap.get("date");
		retention.setDate(date);
		Long netbarId = MapUtils.getLong(retentionMap, "netbar_id");
		retention.setNetbarId(netbarId);
		Integer registerCount = MapUtils.getInteger(retentionMap, "register_count");
		retention.setRegisterCount(registerCount);
		Integer activeCount = MapUtils.getInteger(retentionMap, "active_count");
		retention.setActiveCount(activeCount);
		return retention;
	}

	/**
	 * 更新网吧注册、活跃信息
	 */
	public void updateRetentions(List<String> dates, List<Long> netbarIds) {
		if (CollectionUtils.isEmpty(dates) || CollectionUtils.isEmpty(netbarIds)) {
			return;
		}

		List<PcNetbarUserRetention> dbRetentions = this.findByDateInAndNetbarIdIn(dates, netbarIds);

		// 统计新注册数
		List<Map<String, Object>> loginCountMaps = pcUserLoginHistoryService.getLoginCountByDatesAndNetbarIds(dates,
				netbarIds);

		// 统计活跃数
		List<Map<String, Object>> registerCountMaps = pcUserLoginHistoryService
				.getRegisterCountByDatesAndNetbarIds(dates, netbarIds);

		// 按日期、网吧匹配
		netbarIds.add(null);
		List<PcNetbarUserRetention> updateRetentions = Lists.newArrayList();
		for (String date : dates) {
			if (StringUtils.isBlank(date)) {
				continue;
			}

			for (Long netbarId : netbarIds) {
				if (netbarId == null) {
					System.out.println("1");
				}
				// 查找数据库网吧留存数据
				PcNetbarUserRetention r = this.findRetentionByDateStrAndNetbarId(date, netbarId, dbRetentions);
				if (r == null) {// 找不到留存信息，初始化一个
					r = new PcNetbarUserRetention();
					r.setNetbarId(netbarId);
					try {
						r.setDate(DateUtils.stringToDateYyyyMMdd(date));
					} catch (ParseException e) {
						logger.error("日期格式化异常", e);
						continue;
					}
				}

				// 查找注册数
				Integer registerCount = this.findRegisterCountOrActiveCountByDateStrAndNetbarId(date, netbarId,
						registerCountMaps);
				r.setRegisterCount(registerCount);

				// 查找活跃数
				Integer activeCount = this.findRegisterCountOrActiveCountByDateStrAndNetbarId(date, netbarId,
						loginCountMaps);
				r.setActiveCount(activeCount);
				updateRetentions.add(r);
			}
		}

		// 保存
		this.batchSave(updateRetentions);
	}

	/**
	 * 查询推广网吧ID列表
	 */
	public List<Long> getPromotionNetbarIds() {
		String sql = "SELECT wj_netbar_id netbarId FROM pc_wz_netbar";
		List<Map<String, Object>> maps = queryDao.queryMap(sql);
		if (CollectionUtils.isEmpty(maps)) {
			return null;
		}

		List<Long> netbarIds = Lists.newArrayList();
		for (Map<String, Object> map : maps) {
			Long netbarId = MapUtils.getLong(map, "netbarId");
			if (netbarId == null) {
				continue;
			}

			netbarIds.add(netbarId);
		}
		return netbarIds;
	}

	/**
	 * 从列表里查找留存信息
	 */
	private PcNetbarUserRetention findRetentionByDateStrAndNetbarId(String date, Long netbarId,
			List<PcNetbarUserRetention> retentions) {
		if (StringUtils.isBlank(date) || CollectionUtils.isEmpty(retentions)) {
			return null;
		}

		Iterator<PcNetbarUserRetention> dbRetentionsIt = retentions.iterator();
		while (dbRetentionsIt.hasNext()) {
			PcNetbarUserRetention dbRetention = dbRetentionsIt.next();
			Date dbRetentionDate = dbRetention.getDate();
			if (dbRetentionDate == null) {
				dbRetentionsIt.remove();
				continue;
			}

			String dbRetentionDateStr = DateUtils.dateToString(dbRetentionDate, DateUtils.YYYY_MM_DD);
			Long dbRetentionNetbarId = dbRetention.getNetbarId();
			boolean nullEquals = netbarId == null && dbRetentionNetbarId == null;
			boolean notNullEquals = netbarId != null && netbarId.equals(dbRetentionNetbarId);
			if (date.equals(dbRetentionDateStr) && (nullEquals || notNullEquals)) {
				dbRetentionsIt.remove();
				return dbRetention;
			}
		}

		return null;
	}

	/**
	 * 从列表中查找注册数或活跃数
	 */
	private Integer findRegisterCountOrActiveCountByDateStrAndNetbarId(String date, Long netbarId,
			List<Map<String, Object>> maps) {
		Integer result = 0;
		if (StringUtils.isBlank(date) || CollectionUtils.isEmpty(maps)) {
			return result;
		}

		Iterator<Map<String, Object>> mapsIt = maps.iterator();
		while (mapsIt.hasNext()) {
			Map<String, Object> map = mapsIt.next();
			if (MapUtils.isEmpty(map)) {
				mapsIt.remove();
				continue;
			}

			String mapDate = MapUtils.getString(map, "date");
			Long mapNetbarId = MapUtils.getLong(map, "netbarId");
			boolean nullEquals = netbarId == null && mapNetbarId == null;
			boolean notNullEquals = netbarId != null && netbarId.equals(mapNetbarId);
			if (StringUtils.equals(date, mapDate) && (nullEquals || notNullEquals)) {
				result = MapUtils.getInteger(map, "count");
				mapsIt.remove();
				break;
			}
		}

		if (result == null) {
			result = 0;
		}
		return result;
	}
}
