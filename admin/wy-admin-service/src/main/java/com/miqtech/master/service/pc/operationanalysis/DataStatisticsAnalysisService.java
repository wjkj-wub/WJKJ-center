package com.miqtech.master.service.pc.operationanalysis;

import com.google.common.collect.Maps;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.service.common.ObjectRedisOperateService;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.utils.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 经营分析系统 数据统计分析 Service
 *
 * @author zhangyuqi
 * @create 2017年09月21日
 */
@Service
public class DataStatisticsAnalysisService {
	private static final Logger LOGGER = LoggerFactory.getLogger(DataStatisticsAnalysisService.class);
	/** 查询新增用户类型 */
	private static final Integer TYPE_REGISTER_USER = 1;
	/** 查询登录用户类型 */
	private static final Integer TYPE_LOGIN_USER = 2;
	/** 查询创建比赛类型 */
	private static final Integer TYPE_CREATE_CONFRONT = 3;
	/** 查询完成比赛类型 */
	private static final Integer TYPE_FINISH_CONFRONT = 4;
	/** 查询参赛人数类型 */
	private static final Integer TYPE_ENTER_CONFRONT_USER = 5;
	/** 查询完赛人数类型 */
	private static final Integer TYPE_FINISH_CONFRONT_USER = 6;
	/** 24时计时 最后hour */
	private static final Integer DAY_HOUR = 23;
	/** 返回数据时段key */
	private static final String HOUR_KEY = "hour";
	/** 返回数据日期key */
	private static final String DATE_KEY = "date";
	/** 返回数据值key */
	private static final String VALUE_KEY = "countValue";
	/** 在线用户的keys */
	private static final String ONLINE_USER_KEYS = "gameServer_online_user_ids*";

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private ObjectRedisOperateService objectRedisOperateService;
	@Autowired
	private StringRedisOperateService redisOperateService;

	/**
	 * 获取数据统计信息：
	 * 注册用户数，当前在线用户数，当前在线终端数，当前在线网吧数，覆盖网吧数
	 */
	public Map<String, Object> getDataStatistics() {
		Map<String, Object> map = new HashMap<>(16);
		map.put("registerUserCount", this.getRegisterUserCount(null, null));
		map.put("onlineUserCount", this.getOnlineUserCount());
		map.put("onlineTerminalCount", this.getOnlineTerminalCount());
		map.put("onlineNerBarCount", this.getOnlineNerBarCount());
		map.put("coverNerBarCount", this.getCoverNerBarCount());
		return map;
	}

	/**
	 * 获取分时段统计信息：
	 * 注册用户数，登录用户数，创建比赛数，完成比赛数，参赛人次数，完赛人次数，登录用户平均在线时长
	 */
	public Map<String, Object> getPeriodPDataStatistics(Date beginDate, Date endDate, boolean isGroupHour,
			Integer type) {
		String begin = DateUtils.dateToString(beginDate, DateUtils.YYYY_MM_DD);
		String end = DateUtils.dateToString(endDate, DateUtils.YYYY_MM_DD);
		type = type == null ? TYPE_REGISTER_USER : type;
		Map<String, Object> map = new HashMap<>(16);
		map.put("registerUserCount", this.getRegisterUserCount(begin, end));
		map.put("onlineUserCount", this.getLoginUserCount(begin, end));
		map.put("createConfrontCount", this.getCreateConfrontCount(begin, end));
		map.put("finishConfrontCount", this.getFinishConfrontCount(begin, end));
		map.put("enterConfrontUserCount", this.getEnterConfrontUserCount(begin, end));
		map.put("finishConfrontUserCount", this.getFinishConfrontUserCount(begin, end));
		map.put("averageOnlineTime", this.getAverageOnlineTime(begin, end));

		List<Map<String, Object>> eChartList = this.getEChartList(begin, end, isGroupHour, type);
		int hour = 1;
		Map<String, Object> newEChartMap;
		Date tmpDate;
		int tmpHour;
		List<Map<String, Object>> newEChartList = new ArrayList<>();
		if (CollectionUtils.isEmpty(eChartList)) {
			// 数据库没有数据，为查询区间补充数据
			eChartList = new ArrayList<>();
			newEChartMap = Maps.newHashMap();
			if (isGroupHour) {
				newEChartMap.put(HOUR_KEY, DAY_HOUR);
			} else {
				newEChartMap.put(DATE_KEY, DateUtils.dateToString(endDate, DateUtils.YYYY_MM_DD));
			}
			newEChartMap.put(VALUE_KEY, 0);
			eChartList.add(newEChartMap);
		} else {
			Map<String, Object> eChartM = eChartList.get(eChartList.size() - 1);
			// 如果截止时间非endDate,则补充最后日期
			if (isGroupHour) {
				if (!DAY_HOUR.equals(MapUtils.getInteger(eChartM, HOUR_KEY))) {
					newEChartMap = Maps.newHashMap();
					newEChartMap.put(HOUR_KEY, DAY_HOUR);
					newEChartMap.put(VALUE_KEY, 0);
					eChartList.add(newEChartMap);
				}
			} else {
				Date lastDay;
				String lastDayStr = MapUtils.getString(eChartM, DATE_KEY);
				if (StringUtils.isNotBlank(lastDayStr)) {
					try {
						lastDay = DateUtils.stringToDate(lastDayStr, DateUtils.YYYY_MM_DD);
						if (endDate.after(lastDay)) {
							newEChartMap = Maps.newHashMap();
							newEChartMap.put(DATE_KEY, end);
							newEChartMap.put(VALUE_KEY, 0);
							eChartList.add(newEChartMap);
						}
					} catch (Exception ignored) {
					}
				}
			}
		}

		for (Map<String, Object> eChartMap : eChartList) {
			if (isGroupHour) {
				// 当前时刻数据为空补0
				tmpHour = MapUtils.getInteger(eChartMap, HOUR_KEY);
				if (hour < tmpHour) {
					for (; hour < tmpHour; hour++) {
						newEChartMap = Maps.newHashMap();
						newEChartMap.put(HOUR_KEY, hour);
						newEChartMap.put(VALUE_KEY, 0);
						newEChartList.add(newEChartMap);
					}

					if (hour == tmpHour) {
						newEChartList.add(eChartMap);
						hour++;
					}
				} else {
					newEChartList.add(eChartMap);
					if (hour == tmpHour) {
						hour++;
					}
				}
			} else {
				try {
					tmpDate = DateUtils.stringToDate(MapUtils.getString(eChartMap, DATE_KEY), DateUtils.YYYY_MM_DD);
					if (beginDate.before(tmpDate)) {
						for (; tmpDate.after(beginDate);) {
							newEChartMap = Maps.newHashMap();
							newEChartMap.put(DATE_KEY, DateUtils.dateToString(beginDate, DateUtils.YYYY_MM_DD));
							newEChartMap.put(VALUE_KEY, 0);
							newEChartList.add(newEChartMap);
							beginDate = DateUtils.addDays(beginDate, 1);
						}

						if (beginDate.equals(tmpDate)) {
							newEChartList.add(eChartMap);
							beginDate = DateUtils.addDays(beginDate, 1);
						}
					} else {
						newEChartList.add(eChartMap);
						if (beginDate.equals(tmpDate)) {
							beginDate = DateUtils.addDays(beginDate, 1);
						}
					}
				} catch (Exception e) {
					LOGGER.error("经营分析系统解析数据库日期 {} 格式发生错误：{}", MapUtils.getString(eChartMap, DATE_KEY), e);
				}
			}
		}
		map.put("eChartList", newEChartList);
		return map;
	}

	/**
	 * 获取在beginDate-endDate时间内完成的比赛数
	 */
	int getFinishConfrontCount(String beginDate, String endDate) {
		String sql = SqlJoiner.join("SELECT COUNT(id) FROM pc_confront where is_valid = 1 AND state = 2");
		if (beginDate != null && endDate != null) {
			sql = SqlJoiner.join(sql, " AND create_date >= '", beginDate, "' AND create_date <= '", endDate, "'");
		}
		Number count = queryDao.query(sql);
		return count == null ? 0 : count.intValue();
	}

	/**
	 * 获取在beginDate-endDate时间内的完赛人次数
	 */
	int getFinishConfrontUserCount(String beginDate, String endDate) {
		String sql = SqlJoiner
				.join("SELECT SUM(IF(state=2, IF(mode=3, 10, IF(mode=2, 6, 2)), 0)) FROM pc_confront where state = 2");
		if (beginDate != null && endDate != null) {
			sql = SqlJoiner.join(sql, " AND create_date >= '", beginDate, "' AND create_date <= '", endDate, "'");
		}
		Number count = queryDao.query(sql);
		return count == null ? 0 : count.intValue();
	}

	/**
	 * 获取所有或在beginDate-endDate时间内注册用户数
	 */
	private int getRegisterUserCount(String beginDate, String endDate) {
		String sql = "SELECT COUNT(id) FROM pc_user_info where is_valid = 1";
		if (StringUtils.isAllNotBlank(beginDate, endDate)) {
			sql = SqlJoiner.join(sql, " AND create_date >= '", beginDate, "' AND create_date < '", endDate, "'");
		}
		Number count = queryDao.query(sql);
		return count.intValue();
	}

	/**
	 * 获取当前在线用户数
	 */
	private long getOnlineUserCount() {
		Long size;
		long count = 0;
		Set<String> keySet = redisOperateService.getKeys(ONLINE_USER_KEYS);
		if (CollectionUtils.isEmpty(keySet)) {
			for (String key : keySet) {
				size = objectRedisOperateService.getSetSize(key);
				count += size == null ? 0 : size;
			}
		}
		return count;
	}

	/**
	 * 获取当前在线终端数
	 */
	private int getOnlineTerminalCount() {
		String sql = "SELECT COUNT(id) FROM pc_terminal WHERE is_valid = 1 AND is_online = 1";
		Number count = queryDao.query(sql);
		return count == null ? 0 : count.intValue();
	}

	/**
	 * 获取在线网吧数
	 */
	private int getOnlineNerBarCount() {
		String sql = "SELECT COUNT(DISTINCT netbar_id) FROM pc_terminal WHERE is_valid = 1 AND is_online = 1";
		Number count = queryDao.query(sql);
		return count == null ? 0 : count.intValue();
	}

	/**
	 * 获取覆盖网吧数
	 */
	private int getCoverNerBarCount() {
		String sql = "SELECT COUNT(id) FROM pc_wz_netbar WHERE is_installed = 1";
		Number count = queryDao.query(sql);
		return count == null ? 0 : count.intValue();
	}

	/**
	 * 获取在beginDate-endDate时间内登录的用户数
	 */
	private int getLoginUserCount(String beginDate, String endDate) {
		String sql = SqlJoiner.join("SELECT COUNT(id) FROM pc_user_login_history where login_date >= '", beginDate,
				"' AND login_date <= '", endDate, "'");
		Number count = queryDao.query(sql);
		return count == null ? 0 : count.intValue();
	}

	/**
	 * 获取在beginDate-endDate时间内创建的比赛数
	 */
	private int getCreateConfrontCount(String beginDate, String endDate) {
		String sql = SqlJoiner.join("SELECT COUNT(id) FROM pc_confront where create_date >= '", beginDate,
				"' AND create_date <= '", endDate, "'");
		Number count = queryDao.query(sql);
		return count == null ? 0 : count.intValue();
	}

	/**
	 * 获取在beginDate-endDate时间内的参赛人次数
	 */
	private int getEnterConfrontUserCount(String beginDate, String endDate) {
		String sql = SqlJoiner.join("SELECT COUNT(id) FROM pc_confront_user_record where create_date >= '", beginDate,
				"' AND create_date <= '", endDate, "'");
		Number count = queryDao.query(sql);
		return count == null ? 0 : count.intValue();
	}

	/**
	 * 获取在beginDate-endDate时间内的用户平均在线时长
	 * 当
	 */
	private String getAverageOnlineTime(String beginDate, String endDate) {
		String sql = SqlJoiner.join(
				"SELECT AVG(IFNULL(online_time, IF(login_date > CURDATE(), UNIX_TIMESTAMP(NOW())-UNIX_TIMESTAMP(login_date), 24*3600))) ",
				" FROM pc_user_login_history where login_date >= '", beginDate, "' AND login_date <= '", endDate, "'");
		Number count = queryDao.query(sql);
		return DateUtils.secondsToTimeString(count);
	}

	/**
	 * 获取在beginDate-endDate时间内用户分布数据
	 */
	private List<Map<String, Object>> getEChartList(String beginDate, String endDate, Boolean isGroupHour,
			Integer type) {
		String sql = "SELECT DATE(r.create_date) date,";
		if (isGroupHour) {
			sql = "SELECT HOUR(r.create_date) hour,";
		}

		if (TYPE_REGISTER_USER.equals(type)) {
			sql = SqlJoiner.join(sql, " COUNT(id) countValue FROM pc_user_info r where is_valid = 1");
		} else if (TYPE_LOGIN_USER.equals(type)) {
			return this.getLoginUserByGroup(beginDate, endDate, isGroupHour);
		} else if (TYPE_CREATE_CONFRONT.equals(type)) {
			sql = SqlJoiner.join(sql, " COUNT(id) countValue FROM pc_confront r where is_valid = 1");
		} else if (TYPE_FINISH_CONFRONT.equals(type)) {
			sql = SqlJoiner.join(sql, " COUNT(id) countValue FROM pc_confront r where is_valid = 1 AND state = 2");
		} else if (TYPE_ENTER_CONFRONT_USER.equals(type)) {
			sql = SqlJoiner.join(sql, " COUNT(id) countValue FROM pc_confront_user_record r where 1 = 1");
		} else if (TYPE_FINISH_CONFRONT_USER.equals(type)) {
			sql = SqlJoiner.join(sql, " COUNT(r.id) countValue FROM pc_confront_user_record r",
					" LEFT JOIN pc_confront c ON r.confront_id = c.id where c.is_valid = 1 AND c.state = 2");
		}

		sql = SqlJoiner.join(sql, " AND r.create_date >= '", beginDate, "' AND r.create_date < '", endDate, "'");
		if (isGroupHour) {
			sql = SqlJoiner.join(sql, " GROUP BY HOUR(r.create_date)");
		} else {
			sql = SqlJoiner.join(sql, " GROUP BY DATE(r.create_date)");
		}
		return queryDao.queryMap(sql);
	}

	/**
	 * 获取在beginDate-endDate时间登录用户的分布数据
	 */
	private List<Map<String, Object>> getLoginUserByGroup(String beginDate, String endDate, boolean isGroupHour) {
		String sql = "SELECT DATE(login_date) date,";
		if (isGroupHour) {
			sql = "SELECT HOUR(login_date) hour,";
		}
		sql = SqlJoiner.join(sql, " COUNT(id) countValue FROM pc_user_login_history where login_date >= '", beginDate,
				"' AND login_date <= '", endDate, "' ");
		if (isGroupHour) {
			sql = SqlJoiner.join(sql, " GROUP BY HOUR(login_date)");
		} else {
			sql = SqlJoiner.join(sql, " GROUP BY DATE(login_date)");
		}
		return queryDao.queryMap(sql);
	}
}