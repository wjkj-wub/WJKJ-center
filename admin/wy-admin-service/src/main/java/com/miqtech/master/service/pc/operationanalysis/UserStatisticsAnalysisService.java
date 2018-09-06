package com.miqtech.master.service.pc.operationanalysis;

import com.google.common.collect.Maps;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.service.PagingService;
import com.miqtech.master.service.common.ObjectRedisOperateService;
import com.miqtech.master.service.pc.netbar.PcNetbarUserRetentionService;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.utils.StringUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * 经营分析系统 用户统计分析 Service
 *
 * @author zhangyuqi
 * @create 2017年09月22日
 */
@Service
public class UserStatisticsAnalysisService extends PagingService {

	private static final String OPERATION_ANALYSIS_CONFRONT_STATISTICS = "operation_analysis_confront_statistics";
	/** 注册用户类型 */
	private static final Integer OPERATION_ANALYSIS_USER_REGISTER = 1;
	/** 登录用户类型 */
	private static final Integer OPERATION_ANALYSIS_USER_LOGIN = 2;

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private DataStatisticsAnalysisService dataStatisticsAnalysisService;
	@Autowired
	private ObjectRedisOperateService objectRedisOperateService;
	@Autowired
	private PcNetbarUserRetentionService retentionService;

	/**
	 * 获取用户注册或登录统计
	 * @param type 统计类型：1-注册，2-登录
	 */
	public Map<String, Object> getUserRegisterOrLoginStatistics(Integer page, Integer pageSize, Integer type) {
		return getUserRegisterOrLoginStatistics(page, pageSize, type, null);
	}

	/**
	 * 根据网吧Id获取用户注册或登录统计
	 * @param type 统计类型：1-注册，2-登录
	 */
	public Map<String, Object> getUserRegisterOrLoginStatistics(Integer page, Integer pageSize, Integer type,
			Long netBarId) {
		String condition = StringUtils.EMPTY;
		page = PageUtils.getPage(page);
		String selectName = this.getSelectFieldByType(type);

		if (netBarId != null) {
			condition = SqlJoiner.join(" WHERE netbar_id = ", netBarId.toString());
		}

		String totalSql = SqlJoiner.join("SELECT count(DISTINCT DATE(date)) FROM pc_netbar_user_retention", condition);
		String limitSql = PageUtils.getLimitSql(page, pageSize);
		String sql = SqlJoiner.join("SELECT DATE(date) date, SUM(IFNULL(", selectName, ", 0)) userCount",
				" FROM pc_netbar_user_retention ", condition, " GROUP BY DATE(date) ORDER BY date DESC ", limitSql);

		return getPagingMap(totalSql, sql, page);
	}

	/**
	 * 获取赛事参与信息统计
	 */
	public Map<String, Object> getConfrontStatistics(Integer page, Integer pageSize) {
		List<Object> newList = new ArrayList<>();
		page = PageUtils.getPage(page);
		pageSize = pageSize == null ? PageUtils.ADMIN_DEFAULT_PAGE_SIZE : pageSize;
		int start;
		int end;
		// 创赛次数，完赛次数，参赛人次数，完赛人次数
		List<Object> list = objectRedisOperateService.getListValues(OPERATION_ANALYSIS_CONFRONT_STATISTICS);
		if (CollectionUtils.isEmpty(list)) {
			this.cacheAllConfrontStatistics();
			list = objectRedisOperateService.getListValues(OPERATION_ANALYSIS_CONFRONT_STATISTICS);
		}

		if (!CollectionUtils.isEmpty(list)) {
			start = list.size() - (page - 1) * pageSize;
			end = start - pageSize;
			end = end < 0 ? 0 : end;
			for (; start > end; start--) {
				newList.add(list.get(start - 1));
			}
		}
		Number total = CollectionUtils.isEmpty(list) ? 0 : list.size();
		Map<String, Object> map = getPagingMapWithObjectList(total, page, newList);
		// 总完赛场次
		map.put("totalFinishConfrontCount", dataStatisticsAnalysisService.getFinishConfrontCount(null, null));
		// 总完赛人次数
		map.put("totalFinishConfrontUserCount", dataStatisticsAnalysisService.getFinishConfrontUserCount(null, null));
		return map;
	}

	/**
	 * 获取用户平均等待时长
	 */
	public List<Map<String, Object>> getUserAverageWaitTime() {
		String sql = SqlJoiner.join("SELECT mode, AVG(IFNULL(wait_time, 0)) waitTime FROM pc_confront GROUP BY mode");
		List<Map<String, Object>> list = queryDao.queryMap(sql);
		if (!CollectionUtils.isEmpty(list)) {
			for (Map<String, Object> map : list) {
				map.put("waitTime", DateUtils.secondsToTimeString((Number) map.get("waitTime")));
			}
		}
		return list;
	}

	/**
	 * 获取网吧在线终端统计
	 */
	public Map<String, Object> getNetBarOnlineTerminal(Integer page, Integer pageSize, String keyword) {
		List<Map<String, Object>> list = null;
		page = PageUtils.getPage(page);
		String condition = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(keyword)) {
			condition = SqlJoiner.join(condition, " AND n.name like '%", keyword, "%' ");
		}
		String totalSql = SqlJoiner.join("SELECT COUNT(DISTINCT t.netbar_id) FROM pc_terminal t",
				" LEFT JOIN netbar_t_info n ON t.netbar_id = n.id WHERE is_online = 1", condition);

		Number total = queryDao.query(totalSql);
		if (total != null && total.intValue() > 0) {
			String limitSql = PageUtils.getLimitSql(page, pageSize);
			String sql = SqlJoiner.join(
					"SELECT COUNT(t.id) onlineTerminalCount, n.name netBarName, IFNULL(u.registerUser, 0) registerUser",
					" FROM pc_terminal t LEFT JOIN (SELECT COUNT(id) registerUser, netbar_id netBarId FROM pc_user_info",
					" WHERE is_valid = 1 AND netbar_id IS NOT NULL GROUP BY netbar_id) u ON t.netbar_id = u.netBarId",
					" LEFT JOIN netbar_t_info n ON t.netbar_id = n.id WHERE t.is_online = 1 ", condition,
					" GROUP BY t.netbar_id ", limitSql);
			list = queryDao.queryMap(sql);

			if (!CollectionUtils.isEmpty(list)) {
				for (Map<String, Object> mapTmp : list) {
					if (MapUtils.getString(mapTmp, "netBarName") == null) {
						mapTmp.put("netBarName", "未绑定ip的网吧");
					}
				}
			}
		}
		return getPagingMap(total, page, list);
	}

	/**
	 * 获取网吧用户统计排名
	 */
	public Map<String, Object> getNetBarRankStatistics(Integer page, Integer pageSize, Integer type, String keyword) {
		String condition = StringUtils.EMPTY;
		page = PageUtils.getPage(page);
		if (StringUtils.isNotBlank(keyword)) {
			condition = SqlJoiner.join(condition, " AND n.name like '%", keyword, "%' ");
		}

		// 查询覆盖网吧数
		String totalSql = SqlJoiner.join("SELECT wj_netbar_id id FROM pc_wz_netbar WHERE is_installed = 1 ", condition);
		List<Map<String, Object>> netBarIdList = queryDao.queryMap(totalSql);
		Number total = netBarIdList == null ? 0 : netBarIdList.size();

		List<Map<String, Object>> list = null;
		if (total.intValue() > 0) {
			// 组装网吧IdList
			List<Long> netBarIds = new ArrayList<>();
			Long id;
			for (Map<String, Object> objectMap : netBarIdList) {
				id = MapUtils.getLong(objectMap, "id");
				if (id != null) {
					netBarIds.add(id);
				}
			}
			String today = DateUtils.dateToString(new Date(), DateUtils.YYYY_MM_DD);
			// 统计当天的活跃和新增用户数
			retentionService.updateRetentions(Collections.singletonList(today), netBarIds);

			String name = this.getSelectFieldByType(type);
			String limitSql = PageUtils.getLimitSql(page, pageSize);
			String sql = SqlJoiner.join("SELECT n.id, n.name, IFNULL(r.userCount,0) userCount FROM pc_wz_netbar wz",
					" LEFT JOIN netbar_t_info n ON wz.wj_netbar_id = n.id         ",
					" LEFT JOIN (SELECT netbar_id, SUM(IFNULL(", name, ", 0)) userCount",
					"            FROM pc_netbar_user_retention GROUP BY netbar_id) r ON n.id = r.netbar_id",
					" WHERE wz.is_installed = 1 AND n.is_valid = 1 ", condition, " ORDER BY r.userCount DESC, id ASC ",
					limitSql);
			list = queryDao.queryMap(sql);
		}
		return getPagingMap(total, page, list);
	}

	/**
	 * 获取网吧注册或登录用户统计
	 */
	public Map<String, Object> getNetBarUserStatistics(Integer page, Integer pageSize, Long nerBarId, Integer type) {
		return this.getUserRegisterOrLoginStatistics(page, pageSize, type, nerBarId);
	}

	/**
	 * 执行赛事统计，每天凌晨2点执行一次，统计前一天的数据加入到redis缓存
	 */
	public void executeConfrontStatistics() {
		List<Object> list = objectRedisOperateService.getListValues(OPERATION_ANALYSIS_CONFRONT_STATISTICS);
		if (CollectionUtils.isEmpty(list)) {
			this.cacheAllConfrontStatistics();
		}
		String date = DateUtils.dateToString(DateUtils.getYesterday(), DateUtils.YYYY_MM_DD);
		String sql = SqlJoiner.join(
				"SELECT DATE(create_date) createDate, COUNT(id) createCount, SUM(IF(state=2, 1, 0)) finishCount,",
				" SUM(IF(state=2, IF(mode=3, 10, IF(mode=2, 6, 2)), 0)) finishUserCount,",
				" IFNULL(r.enterUser, 0) enterUserCount",
				" FROM pc_confront LEFT JOIN (SELECT DATE(create_date) createDate, COUNT(id) enterUser",
				" 				   FROM pc_confront_user_record WHERE DATE(create_date) = '", date,
				"') r ON r.createDate = DATE(create_date) WHERE DATE(create_date) = '", date, "'");
		Map<String, Object> map = queryDao.querySingleMap(sql);
		if (CollectionUtils.isEmpty(map) || MapUtils.getString(map, "createDate") == null) {
			map = Maps.newHashMap();
			map.put("createDate", date);
			map.put("createCount", 0);
			map.put("finishCount", 0);
			map.put("finishUserCount", 0);
			map.put("enterUserCount", 0);
		}
		objectRedisOperateService.pushListValue(OPERATION_ANALYSIS_CONFRONT_STATISTICS, map);
	}

	/**
	 * 根据类型或查询字段
	 */
	private String getSelectFieldByType(Integer type) {
		String name;
		if (OPERATION_ANALYSIS_USER_LOGIN.equals(type)) {
			name = "active_count";
		} else {
			name = "register_count";
		}
		return name;
	}

	/**
	 * 查询所有的赛事统计，将并补全缺失的日期数据，缓存redis
	 */
	private void cacheAllConfrontStatistics() {
		Date beginDate = null;
		String tmpDate;
		Date nextDate;
		String date = DateUtils.dateToString(DateUtils.getToday(), DateUtils.YYYY_MM_DD);
		String sql = SqlJoiner.join(
				"SELECT DATE(create_date) createDate, COUNT(id) createCount, SUM(IF(state=2, 1, 0)) finishCount,",
				" SUM(IF(state=2, IF(mode=3, 10, IF(mode=2, 6, 2)), 0)) finishUserCount, IFNULL(r.enterUser, 0) enterUserCount",
				" FROM pc_confront LEFT JOIN (SELECT DATE(create_date) createDate, COUNT(id) enterUser ",
				" 				   FROM pc_confront_user_record WHERE create_date IS NOT NULL",
				"		           GROUP BY DATE(create_date)) r ON r.createDate = DATE(create_date)",
				" WHERE create_date < '", date, "' GROUP BY DATE(create_date)");
		List<Map<String, Object>> list = queryDao.queryMap(sql);
		if (!CollectionUtils.isEmpty(list)) {
			List<Object> newList = new ArrayList<>();
			Map<String, Object> newMap;

			for (Map<String, Object> map : list) {
				try {
					tmpDate = MapUtils.getString(map, "createDate");
					if (beginDate == null) {
						beginDate = DateUtils.stringToDate(tmpDate, DateUtils.YYYY_MM_DD);
						newList.add(map);
						continue;
					}

					if (StringUtils.isNotBlank(tmpDate)) {
						nextDate = DateUtils.stringToDate(tmpDate, DateUtils.YYYY_MM_DD);
						if (beginDate.before(nextDate)) {
							for (; nextDate.after(beginDate);) {
								newMap = Maps.newHashMap();
								newMap.put("createDate", DateUtils.dateToString(beginDate, DateUtils.YYYY_MM_DD));
								newMap.put("createCount", 0);
								newMap.put("finishCount", 0);
								newMap.put("finishUserCount", 0);
								newMap.put("enterUserCount", 0);
								newList.add(newMap);
								beginDate = DateUtils.addDays(beginDate, 1);
							}

							if (beginDate.equals(nextDate)) {
								newList.add(map);
								beginDate = DateUtils.addDays(beginDate, 1);
							}
						} else {
							newList.add(map);
							if (beginDate.equals(nextDate)) {
								beginDate = DateUtils.addDays(beginDate, 1);
							}
						}
					}
				} catch (Exception ignored) {

				}
			}
			objectRedisOperateService.delData(OPERATION_ANALYSIS_CONFRONT_STATISTICS);
			objectRedisOperateService.setListValue(OPERATION_ANALYSIS_CONFRONT_STATISTICS, newList);
		}
	}

}
