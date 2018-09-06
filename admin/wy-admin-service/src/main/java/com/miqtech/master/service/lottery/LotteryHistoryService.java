package com.miqtech.master.service.lottery;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.lottery.LotteryHistoryDao;
import com.miqtech.master.entity.lottery.LotteryHistory;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class LotteryHistoryService {

	private static final Logger LOGGER = LoggerFactory.getLogger(LotteryHistoryService.class);

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private LotteryHistoryDao lotteryHistoryDao;

	public LotteryHistory findById(Long id) {
		return lotteryHistoryDao.findByIdAndValid(id, CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * 保存历史记录
	 */
	public LotteryHistory save(LotteryHistory lotteryHistory) {
		if (lotteryHistory != null) {
			Date now = new Date();
			lotteryHistory.setUpdateDate(now);
			if (lotteryHistory.getId() == null) {
				lotteryHistory.setCreateDate(now);
				lotteryHistory.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			} else {
				lotteryHistory = BeanUtils.updateBean(findById(lotteryHistory.getId()), lotteryHistory);
			}
			return lotteryHistoryDao.save(lotteryHistory);
		}
		return null;
	}

	/**
	 * 通过活动ID和用户ID查询中奖历史
	 */
	public List<Map<String, Object>> findWinHistoryByLotteryIdAndUserId(Long lotteryId, Long userId) {
		String sql = SqlJoiner
				.join("SELECT h.id historyId, h.has_get hasGet, h.user_id userId, h.is_win isWin, h.create_date createDate, a.name awardName, p.name prizeName, p.icon prizeIcon",
						" FROM lottery_t_history h LEFT JOIN lottery_t_award a ON h.award_id = a.id",
						" LEFT JOIN lottery_t_prize p ON h.prize_id = p.id",
						" WHERE h.user_id = :userId AND h.lottery_id = :lotteryId AND h.is_win = 1",
						" ORDER BY h.create_date DESC").replaceAll(":userId", userId.toString())
				.replaceAll(":lotteryId", lotteryId.toString());
		return queryDao.queryMap(sql);
	}

	/**
	 * 后台分页
	 */
	public PageVO page(int page, Map<String, String> params) {
		String sql = SqlJoiner
				.join("SELECT h.id, h.user_id userId, h.award_id awardId, h.prize_id prizeId, h.is_win isWin, h.is_valid isValid, h.create_date createDate, p.name prizeName, a.name awardName, u.name userName, u.telephone userTelephone",
						" FROM lottery_t_history h LEFT JOIN lottery_t_award a ON h.award_id = a.id AND h.lottery_id = a.lottery_id AND a.is_valid = 1",
						" LEFT JOIN lottery_t_prize p ON h.prize_id = p.id AND p.is_valid = 1",
						" LEFT JOIN mall_r_user_info u ON h.user_id = u.user_id", buildConditions(page, params));
		List<Map<String, Object>> list = queryDao.queryMap(sql);

		String countSql = SqlJoiner.join("SELECT COUNT(1) FROM lottery_t_history h", buildConditions(0, params));
		Number count = (Number) queryDao.query(countSql);
		if (count == null) {
			count = 0;
		}

		PageVO vo = new PageVO();
		vo.setList(list);
		vo.setTotal(count.longValue());
		vo.setIsLast(page * PageUtils.ADMIN_DEFAULT_PAGE_SIZE >= count.intValue() ? 1 : 0);

		return vo;
	}

	/**
	 * 产生查询条件,page <= 0 时，不分页
	 */
	private String buildConditions(int page, Map<String, String> params) {
		String conditions = " WHERE 1";

		// 默认只查询中奖记录
		if (params.get("isWin") == null) {
			params.put("isWin", "1");
		}

		if (params != null) {
			Set<String> keys = params.keySet();
			if (CollectionUtils.isNotEmpty(keys)) {
				for (String k : keys) {
					String value = params.get(k);
					if (k.equals("valid")) {
						conditions = SqlJoiner.join(conditions, " AND h.is_valid = ", value);
					} else if (k.equals("isWin")) {
						conditions = SqlJoiner.join(conditions, " AND h.is_win = ", value);
					} else if (k.equals("lotteryId")) {
						conditions = SqlJoiner.join(conditions, " AND h.lottery_id = ", value);
					} else if (k.equals("awardId")) {
						conditions = SqlJoiner.join(conditions, " AND h.award_id = ", value);
					} else if (k.equals("prizeId")) {
						conditions = SqlJoiner.join(conditions, " AND h.prize_id = ", value);
					} else if (k.equals("beginDate")) {
						conditions = SqlJoiner.join(conditions, " AND h.create_date >= '", value, "'");
					} else if (k.equals("endDate")) {
						conditions = SqlJoiner.join(conditions, " AND h.create_date < ADDDATE('", value,
								"',INTERVAL 1 DAY)");
					} else if (k.equals("username")) {
						conditions = SqlJoiner.join(conditions, " AND u.name LIKE '%", value, "%'");
					} else if (k.equals("telephone")) {
						conditions = SqlJoiner.join(conditions, " AND u.telephone LIKE '%", value, "%'");
					}
				}
			}
		}

		conditions = SqlJoiner.join(conditions, " ORDER BY h.create_date DESC");

		if (page > 0) {
			Integer rows = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
			Integer startRow = (page - 1) * rows;
			conditions = SqlJoiner.join(conditions, " LIMIT ", startRow.toString(), ",", rows.toString());
		}

		return conditions;
	}

	/**
	 * 统计抽奖历史
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> statis(Long lotteryId, String beginDate, String endDate, String group) {
		// 初始化参数
		int field = Calendar.DAY_OF_YEAR;
		if ("2".equals(group)) {
			field = Calendar.MONTH;
		} else if ("3".equals(group)) {
			field = Calendar.YEAR;
		}

		if (StringUtils.isBlank(beginDate) && StringUtils.isBlank(endDate)) {
			Date dEndDate = new Date();
			endDate = DateUtils.dateToString(dEndDate, DateUtils.YYYY_MM_DD);

			Calendar cEndDAte = Calendar.getInstance();
			cEndDAte.add(field, -7);
			beginDate = DateUtils.dateToString(cEndDAte.getTime(), DateUtils.YYYY_MM_DD);
		} else {
			try {
				if (StringUtils.isBlank(beginDate)) {
					Date dBeginDate = DateUtils.stringToDate(endDate, DateUtils.YYYY_MM_DD);
					Calendar c = Calendar.getInstance();
					c.setTime(dBeginDate);
					c.add(field, -7);
					beginDate = DateUtils.dateToString(c.getTime(), DateUtils.YYYY_MM_DD);
				}
				if (StringUtils.isBlank(endDate)) {
					Date dEndDate = DateUtils.stringToDate(beginDate, DateUtils.YYYY_MM_DD);
					Calendar c = Calendar.getInstance();
					c.setTime(dEndDate);
					c.add(field, 7);
					endDate = DateUtils.dateToString(c.getTime(), DateUtils.YYYY_MM_DD);
				}
			} catch (ParseException e) {
				LOGGER.error("格式化时间异常:", e);
			}
		}

		// 检查时间间隔，选择合适的分组
		Date bgDate = null;
		Date edDate = null;
		try {
			bgDate = DateUtils.stringToDate(beginDate, DateUtils.YYYY_MM_DD);
			edDate = DateUtils.stringToDate(endDate, DateUtils.YYYY_MM_DD);
			long gap = edDate.getTime() - bgDate.getTime();
			long yearGap = 12L * 30L * 24L * 60L * 60L * 1000;
			long monthGap = 31L * 24L * 60L * 60L * 1000L;
			if (gap > yearGap) {// 时间差大于12个月，采用年分组
				group = "3";
				field = Calendar.YEAR;
			} else if (gap > monthGap) {// 时间差小于12个月，大于30天，采用月分组
				group = "2";
				field = Calendar.MONTH;
			} else {
				group = "1";
				field = Calendar.DAY_OF_MONTH;
			}
		} catch (ParseException e) {
			LOGGER.error("格式化时间异常:", e);
		}

		// 统计数据
		Map<String, Object> issueStatis = statisHistory(lotteryId, beginDate, endDate, group, false);
		Map<String, Object> usedStatis = statisHistory(lotteryId, beginDate, endDate, group, true);
		Calendar c = Calendar.getInstance();
		c.setTime(bgDate);

		List<String> dates = new ArrayList<String>();
		List<Object> issuesCount = new ArrayList<Object>();
		List<Object> usedsCount = new ArrayList<Object>();

		// 重新组装数据
		if ("2".equals(group)) {
			c.set(Calendar.DAY_OF_MONTH, 1);
			Calendar edCalendar = Calendar.getInstance();
			edCalendar.setTime(edDate);
			edCalendar.set(Calendar.DAY_OF_MONTH, 1);
			edDate = edCalendar.getTime();
		} else if ("3".equals(group)) {
			c.set(Calendar.MONTH, 1);
			c.set(Calendar.DAY_OF_YEAR, 1);
			Calendar edCalendar = Calendar.getInstance();
			edCalendar.setTime(edDate);
			edCalendar.set(Calendar.MONTH, 1);
			edCalendar.set(Calendar.DAY_OF_YEAR, 1);
			edDate = edCalendar.getTime();
		}

		int i = 0;
		while (c.getTime().before(edDate) && i < 30) {// 最多显示30个点
			c.add(field, 1);
			i += 1;

			String date = DateUtils.dateToString(c.getTime(), DateUtils.YYYY_MM_DD);
			dates.add(date);

			Map<String, Object> iStatis = (Map<String, Object>) issueStatis.get(date);
			Number iCount = null;
			if (iStatis != null) {
				iCount = (Number) iStatis.get("count");
			}
			if (iCount == null) {
				iCount = 0;
			}
			issuesCount.add(iCount);

			Map<String, Object> uStatis = (Map<String, Object>) usedStatis.get(date);
			Number uCount = null;
			if (uStatis != null) {
				uCount = (Number) uStatis.get("count");
			}
			if (uCount == null) {
				uCount = 0;
			}
			usedsCount.add(uCount);
		}

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("dates", dates);
		result.put("allCount", issuesCount);
		result.put("winCount", usedsCount);
		result.put("beginDate", beginDate);
		result.put("endDate", endDate);
		result.put("group", group);

		return result;
	}

	/**
	 * 统计抽奖历史
	 */
	private Map<String, Object> statisHistory(Long lotteryId, String beginDate, String endDate, String group,
			boolean onlyWin) {
		String dateFormat = null;
		String dateUnit = null;
		if ("2".equals(group)) {
			dateFormat = "DATE_FORMAT(h.create_date, '%Y-%m-01')";
			dateUnit = "MONTH";
		} else if ("3".equals(group)) {
			dateFormat = "DATE_FORMAT(h.create_date, '%Y-01-01')";
			dateUnit = "YEAR";
		} else {
			dateFormat = "DATE(h.create_date)";
			dateUnit = "DAY";
		}

		String isWin = onlyWin ? " AND is_win = 1" : "";

		String sql = SqlJoiner
				.join("SELECT count(1) count, :dateFormat date FROM lottery_t_history h",
						" WHERE lottery_id = :lotteryId AND h.create_date >= ':beginDate' AND h.create_date < ADDDATE(':endDate', INTERVAL 1 :dateUnit) :isWin",
						" GROUP BY :dateFormat ORDER BY create_date ASC")
				.replaceAll(":lotteryId", lotteryId.toString()).replaceAll(":isWin", isWin)
				.replaceAll(":dateFormat", dateFormat).replaceAll(":beginDate", beginDate)
				.replaceAll(":endDate", endDate).replaceAll(":dateUnit", dateUnit);
		List<Map<String, Object>> list = queryDao.queryMap(sql);

		// 行列转换
		Map<String, Object> dateStatis = new HashMap<String, Object>();
		if (CollectionUtils.isNotEmpty(list)) {
			for (Map<String, Object> s : list) {
				Object oDate = s.get("date");
				String date = null;
				if (oDate instanceof Date) {
					date = DateUtils.dateToString((Date) s.get("date"), DateUtils.YYYY_MM_DD);
				} else {
					date = (String) oDate;
				}
				Map<String, Object> ns = new HashMap<String, Object>();
				ns.put("count", s.get("count"));
				dateStatis.put(date, ns);
			}
		}

		return dateStatis;
	}
}
