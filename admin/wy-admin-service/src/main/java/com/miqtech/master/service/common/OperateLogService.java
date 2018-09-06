package com.miqtech.master.service.common;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.OperateLogConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.common.OperateLogDao;
import com.miqtech.master.entity.common.OperateLog;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

/**
 * 网娱后台商城后台操作记录
 *
 */
@Component
public class OperateLogService {
	@Autowired
	private OperateLogDao operateLogDao;
	@Autowired
	private QueryDao queryDao;

	/*
	 * 保存
	 */
	public OperateLog save(OperateLog operateLog) {
		if (operateLog != null) {
			return operateLogDao.save(operateLog);
		}
		return null;
	}

	public List<OperateLog> save(List<OperateLog> logs) {
		if (CollectionUtils.isNotEmpty(logs)) {
			return (List<OperateLog>) operateLogDao.save(logs);
		}
		return null;
	}

	/*
	 * 操作日志记录
	 */
	public void operateLog(long sysUserId, Long thirdId, String info) {
		OperateLog operateLog = new OperateLog();
		operateLog.setSysType(OperateLogConstant.SYS_TYPE_MALL);
		operateLog.setSysUserId(sysUserId);
		operateLog.setThirdId(thirdId);
		operateLog.setInfo(info);
		operateLog.setValid(CommonConstant.INT_BOOLEAN_TRUE);
		operateLog.setCreateDate(new Date());

		save(operateLog);
	}

	/**
	 * 记录操作日志(后台)
	 */
	public OperateLog adminOperateLog(long sysUserId, Long thirdId, Integer type, String info) {
		OperateLog operateLog = new OperateLog(OperateLogConstant.SYS_TYPE_ADMIN, sysUserId, thirdId, type, info);
		return save(operateLog);
	}

	/*
	 * 黑名单列表
	 */
	public PageVO page(int page, Map<String, Object> searchParams) {
		String join = " LEFT JOIN sys_t_user u ON u.id=b.sys_user_id";
		String condition = " WHERE b.is_valid = 1 AND b.sys_type=2";
		String totalCondition = " WHERE b.is_valid = 1 AND b.sys_type=2";

		Map<String, Object> params = Maps.newHashMap();
		if (MapUtils.isNotEmpty(searchParams)) {
			// 操作时间
			String beginDate = MapUtils.getString(searchParams, "beginDate");
			if (StringUtils.isNotBlank(beginDate)) {
				condition = SqlJoiner.join(condition, " AND b.create_date >= :beginDate");
				params.put("beginDate", beginDate);
				totalCondition = SqlJoiner.join(totalCondition, " AND b.create_date >= '", beginDate, "'");
			}
			String endDate = MapUtils.getString(searchParams, "endDate");
			if (StringUtils.isNotBlank(endDate)) {
				condition = SqlJoiner.join(condition, " AND b.create_date < ADDDATE(:endDate, INTERVAL 1 DAY)");
				params.put("endDate", endDate);
				totalCondition = SqlJoiner.join(totalCondition, " AND b.create_date < ADDDATE('", endDate,
						"', INTERVAL 1 DAY)");
			}
			// 操作用户
			String sysUserId = MapUtils.getString(searchParams, "sysUserId");
			if (StringUtils.isNotBlank(sysUserId)) {
				condition = SqlJoiner.join(condition, " AND b.sys_user_id =", sysUserId);
				totalCondition = SqlJoiner.join(totalCondition, " AND b.sys_user_id =", sysUserId);
			}
			// 操作内容
			String info = MapUtils.getString(searchParams, "info");
			if (StringUtils.isNotBlank(info)) {
				String likeInfo = "%" + info + "%";
				condition = SqlJoiner.join(condition, " AND b.info LIKE '", likeInfo, "'");
				totalCondition = SqlJoiner.join(totalCondition, " AND b.info LIKE '", likeInfo, "'");
			}
		}

		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		page = page < 1 ? 1 : page;
		Integer startRow = (page - 1) * pageSize;
		String sqlLimit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());

		String sql = SqlJoiner.join("SELECT b.id, b.create_date date, b.info, b.sys_user_id sysUserId, u.username",
				" FROM operate_log b", join, condition, " ORDER BY b.create_date DESC", sqlLimit);

		String totalSql = SqlJoiner.join("SELECT COUNT(1) FROM operate_log b ", totalCondition);
		Number totalNum = queryDao.query(totalSql);
		int total = 0;
		if (totalNum != null) {
			total = totalNum.intValue();
		}
		List<Map<String, Object>> list = null;
		if (total > 0) {
			list = queryDao.queryMap(sql, params);
		}

		PageVO vo = new PageVO();
		vo.setList(list);
		vo.setTotal(total);
		return vo;
	}

	public PageVO adminPage(int page, Map<String, String> searchParams) {
		String condition = " WHERE ol.is_valid = 1 AND ol.sys_type = " + OperateLogConstant.SYS_TYPE_ADMIN;
		String totalCondition = condition;
		Map<String, Object> params = Maps.newHashMap();

		String sysUserId = MapUtils.getString(searchParams, "sysUserId");
		if (NumberUtils.isNumber(sysUserId)) {
			condition = SqlJoiner.join(condition, " AND ol.sys_user_id = ", sysUserId);
			totalCondition = SqlJoiner.join(totalCondition, " AND ol.sys_user_id = ", sysUserId);
		}
		String info = MapUtils.getString(searchParams, "info");
		if (StringUtils.isNotBlank(info)) {
			String likeInfo = "%" + info + "%";
			params.put("info", likeInfo);
			condition = SqlJoiner.join(condition, " AND ol.info LIKE :info");
			totalCondition = SqlJoiner.join(totalCondition, " AND ol.info LIKE '", likeInfo, "'");
		}
		String beginDate = MapUtils.getString(searchParams, "beginDate");
		if (StringUtils.isNotBlank(beginDate)) {
			params.put("beginDate", beginDate);
			condition = SqlJoiner.join(condition, " AND ol.create_date >= :beginDate");
			totalCondition = SqlJoiner.join(totalCondition, " AND ol.create_date >= '", beginDate, "'");
		}
		String endDate = MapUtils.getString(searchParams, "endDate");
		if (StringUtils.isNotBlank(endDate)) {
			params.put("endDate", endDate);
			condition = SqlJoiner.join(condition, " AND ol.create_date < :endDate");
			totalCondition = SqlJoiner.join(totalCondition, " AND ol.create_date < '", endDate, "'");
		}

		// 排序
		String order = " ORDER BY ol.create_date DESC";

		// 分页
		String limit = "";
		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page > 0) {
			Integer startRow = (page - 1) * pageSize;
			limit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());
		}

		String sql = SqlJoiner
				.join("SELECT ol.id, su.realname sysUserRealName, ol.info, ol.type, ol.third_id thirdId, ol.create_date createDate",
						" FROM operate_log ol JOIN sys_t_user su ON ol.sys_user_id = su.id", condition, order, limit);
		List<Map<String, Object>> list = queryDao.queryMap(sql, params);

		String totalSql = SqlJoiner.join(
				"SELECT COUNT(1) FROM operate_log ol JOIN sys_t_user su ON ol.sys_user_id = su.id", totalCondition);
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
}
