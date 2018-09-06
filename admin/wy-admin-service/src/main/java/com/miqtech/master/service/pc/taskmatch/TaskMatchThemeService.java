package com.miqtech.master.service.pc.taskmatch;

import com.miqtech.master.consts.pc.RateConstant;
import com.miqtech.master.consts.taskMatch.TaskMatchConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.pc.taskmatch.TaskMatchThemeDao;
import com.miqtech.master.entity.pc.taskmatch.TaskMatchTheme;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.utils.StringUtils;
import com.miqtech.master.vo.PageVO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 主题任务赛 Service 接口
 *
 * @author zhangyuqi
 * @create 2017年09月02日
 */
@Service
public class TaskMatchThemeService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private TaskMatchThemeDao taskMatchThemeDao;

	public TaskMatchTheme save(TaskMatchTheme taskMatchTheme) {
		return taskMatchThemeDao.save(taskMatchTheme);
	}

	public TaskMatchTheme findById(Long id) {
		return taskMatchThemeDao.findOne(id);
	}

	public void save(List<TaskMatchTheme> list) {
		taskMatchThemeDao.save(list);
	}


	/**
	 * 获取领取主题任务用户列表
	 */
	public List<Map<String, Object>> getThemeTaskUserRecord(Long id) {
		if (id == null) {
			return null;
		}

		String sql = SqlJoiner.join("SELECT u.nickname, u.id userId, e.play_times playTimes, e.create_date date,",
				" e.status status, IF(e.status = 2, t.total_award, null) awardAmount FROM pc_task_match_enter e",
				" LEFT JOIN pc_task_match_theme t ON t.id = e.task_id LEFT JOIN pc_user_info u ON e.user_id = u.id",
				" WHERE e.task_type = 2 AND e.is_valid = 1 AND e.status != 0 AND u.is_valid = 1 AND e.task_id = ",
				id.toString());
		return queryDao.queryMap(sql);
	}

	public boolean isNameRepeat(String name, Long id) {
		String sql = "select * from pc_task_match_theme where name ='" + name
				+ "' and is_valid=1 and status BETWEEN 0 and 1";
		if (id != null) {
			sql += " and id!=" + id;
		}
		List<TaskMatchTheme> list = queryDao.queryObject(sql, TaskMatchTheme.class);
		return CollectionUtils.isNotEmpty(list);
	}

	/**
	 * 获取领取主题任务用户列表统计数据
	 */
	public Map<String, Object> getThemeTaskRecordStatistics(Long id) {
		if (id == null) {
			return null;
		}

		Integer rate = RateConstant.RMB_TO_CHIP;

		String sql = SqlJoiner.join("SELECT count(e.id) receivedCount, count(DISTINCT a.id) finishCount,",
				" sum(IF(a.fee_type = 3, a.fee_amount*", rate.toString(), ",a.fee_amount)) sum",
				" FROM pc_task_match_enter e LEFT JOIN pc_task_match_award a ON e.task_id = a.task_id",
				" AND e.user_id = a.user_id AND a.task_type = 2 AND a.is_valid = 1",
				" WHERE e.task_type = 2 AND e.is_valid = 1 AND e.status != 0 AND e.task_id = ", id.toString());
		return queryDao.querySingleMap(sql);
	}

	public List<TaskMatchTheme> findByStatus() {
		String sql = "select * from pc_task_match_theme where  status BETWEEN 0 and 1 and is_valid=1";
		return queryDao.queryObject(sql, TaskMatchTheme.class);
	}

	/**
	 *获取主题任务赛列表
	 */
	public PageVO getThemeList(String start, String end, Integer status, String name, Integer page, Integer pageSize) {
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotBlank(start)) {
			sb.append(" and theme.begin_date>='").append(DateUtils.stampToDate(start, DateUtils.YYYY_MM_DD))
					.append("'");
		}
		if (StringUtils.isNotBlank(end)) {
			sb.append(" and theme.end_date<='").append(DateUtils.stampToDate(end, DateUtils.YYYY_MM_DD)).append("'");
		}
		if (status != null) {
			if (status == -1) {
				sb.append(" and theme.is_release=0");
			} else {
				sb.append(" and theme.status=").append(status);
			}

		}
		if (StringUtils.isNotBlank(name)) {
			sb.append(" and theme.name like '%").append(name).append("%'");
		}
		if (page == null || page <= 0) {
			page = 1;
		}
		if (pageSize == null || pageSize <= 0) {
			pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		}
		String totalSql = "SELECT count(distinct theme.id) "
				+ "FROM pc_task_match_theme theme LEFT JOIN pc_task_match_fee fee ON theme.id = fee.task_id AND task_type = "
				+ TaskMatchConstant.TASK_TYPE_THEME + " AND record_type = " + TaskMatchConstant.RECORD_TYPE_APPLY
				+ " and fee.is_valid=1 where 1=1 and theme.is_valid=1";
		totalSql += sb.toString();
		Number count = queryDao.query(totalSql);
		if (count != null && count.intValue() > 0) {
			int startLimit = (page - 1) * pageSize;
			Integer rate = RateConstant.RMB_TO_CHIP;
			String sql = "SELECT theme.id,theme. NAME,count(DISTINCT enter.user_id) userCount,"
					+ "sum(ceil(if(fee.fee_type = 3,fee.fee_amount*" + rate.toString() + ",fee.fee_amount))) amount,"
					+ "if(theme.`status`<=1 and theme.is_release=0,-1,theme.`status`)`status`,theme.type,"
					+ "IF (theme.`status` = 0 AND theme.is_release = 0,0,NULL) is_release "
					+ "FROM pc_task_match_theme theme "
					+ "left join pc_task_match_enter enter on theme.id=enter.task_id and enter.task_type="
					+ TaskMatchConstant.TASK_TYPE_THEME + " and enter.is_valid=1 "
					+ "LEFT JOIN pc_task_match_fee fee ON theme.id = fee.task_id AND fee.task_type = "
					+ TaskMatchConstant.TASK_TYPE_THEME + " AND fee.record_type = "
					+ TaskMatchConstant.RECORD_TYPE_APPLY
					+ " and fee.is_valid=1 and fee.user_id=enter.user_id where theme.is_valid=1"
					+ sb.toString() + " GROUP BY theme.id ORDER BY theme.`status` asc,theme.create_date DESC "
					+ "limit " + startLimit + "," + pageSize;
			List<Map<String, Object>> list = queryDao.queryMap(sql);
			return new PageVO(page, list, count.intValue(), pageSize);
		}
		return new PageVO();
	}

	public List<Map<String, Object>> queryInfoForAppRecommend(String startDate, String endDate) {
		String sql = "select id,name title from pc_task_match_theme where is_valid=1 and is_release=1 and create_date>'"
				+ startDate + "' and create_date <'" + endDate + "' order by create_date desc";
		return queryDao.queryMap(sql);

	}

}
