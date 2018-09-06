package com.miqtech.master.service.activity;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.activity.ActivityOverActivityModuleDao;
import com.miqtech.master.entity.activity.ActivityOverActivityModule;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class ActivityOverActivityModuleService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private ActivityOverActivityModuleDao activityOverActivityModuleDao;

	public List<ActivityOverActivityModule> findAllValid() {
		return activityOverActivityModuleDao.findByValid(CommonConstant.INT_BOOLEAN_TRUE);
	}

	public ActivityOverActivityModule findById(Long id) {
		return activityOverActivityModuleDao.findOne(id);
	}

	public List<ActivityOverActivityModule> findValidByPid(Long pid) {
		if (pid == null) {
			return null;
		}
		return activityOverActivityModuleDao.findByPidAndValid(pid, CommonConstant.INT_BOOLEAN_TRUE);
	}

	public List<ActivityOverActivityModule> findValidByPidIn(List<Long> pids) {
		if (CollectionUtils.isEmpty(pids)) {
			return null;
		}
		return activityOverActivityModuleDao.findByPidInAndValid(pids, CommonConstant.INT_BOOLEAN_TRUE);
	}

	public List<ActivityOverActivityModule> findValidByPidAndOrderNum(Long pid, Integer orderNum) {
		return activityOverActivityModuleDao.findByPidAndOrderNumAndValid(pid, orderNum,
				CommonConstant.INT_BOOLEAN_TRUE);
	}

	public List<ActivityOverActivityModule> findRootValidByType(int type) {
		return activityOverActivityModuleDao.findByTypeAndPidAndValid(type, 0L, CommonConstant.INT_BOOLEAN_TRUE);
	}

	public ActivityOverActivityModule save(ActivityOverActivityModule module) {
		if (module == null) {
			return null;
		}
		return activityOverActivityModuleDao.save(module);
	}

	public List<ActivityOverActivityModule> getValidTreeByType(Integer type, boolean appendMatches) {
		List<ActivityOverActivityModule> rootModules = findRootValidByType(type);
		if (appendMatches) {
			rootModules.addAll(findRootValidByType(3));
			rootModules.addAll(findRootValidByType(4));
		}
		if (CollectionUtils.isEmpty(rootModules)) {
			return null;
		}
		rootModules.sort((o1, o2) -> o1.getOrderNum() > o2.getOrderNum() ? 1 : -1);

		// 查询子级别模块
		List<Long> pids = Lists.newArrayList();
		for (ActivityOverActivityModule m : rootModules) {
			pids.add(m.getId());
		}
		List<ActivityOverActivityModule> children = findValidByPidIn(pids);
		if (CollectionUtils.isEmpty(children)) {
			return rootModules;
		}
		children.sort((o1, o2) -> o1.getOrderNum() > o2.getOrderNum() ? 1 : -1);

		// 匹配子模块与父模块的关系
		for (ActivityOverActivityModule rm : rootModules) {
			for (Iterator<ActivityOverActivityModule> it = children.iterator(); it.hasNext();) {
				ActivityOverActivityModule cm = it.next();
				if (rm.getId().equals(cm.getPid())) {
					rm.addChildren(cm);
					it.remove();
				}
			}
		}
		return rootModules;
	}

	/**
	 * 删除模块
	 */
	public ActivityOverActivityModule delete(Long id) {
		if (id == null) {
			return null;
		}

		// 查询模块
		ActivityOverActivityModule module = findById(id);
		return delete(module);
	}

	/**
	 * 删除模块
	 */
	public ActivityOverActivityModule delete(ActivityOverActivityModule module) {
		if (module == null) {
			return null;
		}

		// 排序设置为最后,避免排序错乱
		changeOrderNum(module.getId(), defaultOrderNum(module.getPid()));

		// 删除模块
		module.setValid(CommonConstant.INT_BOOLEAN_FALSE);
		return save(module);
	}

	/**
	 * 新增或更新
	 */
	public ActivityOverActivityModule saveOrUpdate(ActivityOverActivityModule module) {
		if (module == null) {
			return null;
		}

		Date now = new Date();
		module.setUpdateDate(now);
		if (module.getId() != null) {
			ActivityOverActivityModule old = findById(module.getId());
			module = BeanUtils.updateBean(old, module);
		} else {
			module.setCreateDate(now);
			module.setValid(CommonConstant.INT_BOOLEAN_TRUE);

			if (module.getPid() == null) {
				module.setPid(0L);
			}
			if (module.getOrderNum() == null) {
				module.setOrderNum(defaultOrderNum(module.getPid()));
			}
		}
		return save(module);
	}

	/**
	 * 查询pid下的数量,作为默认排序序号
	 */
	public int defaultOrderNum(Long pid) {
		if (pid == null) {
			pid = 0L;
		}

		String sortNumSql = "SELECT COUNT(1) FROM activity_over_activity_module WHERE is_valid = 1 AND pid = " + pid;
		Number sortNum = queryDao.query(sortNumSql);
		if (sortNum == null) {
			sortNum = 0;
		}
		return sortNum.intValue() + 1;
	}

	/**
	 * 分页
	 */
	public PageVO page(int page, Map<String, String> searchParams) {
		String limit = PageUtils.getLimitSql(page);

		String totalSql = SqlJoiner
				.join("SELECT count(1) FROM activity_over_activity_module m WHERE m.is_valid = 1 AND m.pid = 0");
		Number total = queryDao.query(totalSql);

		List<Map<String, Object>> list = null;
		if (total != null && total.intValue() > 0) {
			String sql = SqlJoiner.join("SELECT m.id, m.order_num orderNum, m.type, m.img, m.name, count(cm.id) count",
					" FROM activity_over_activity_module m",
					" LEFT JOIN activity_over_activity_module cm ON m.id = cm.pid AND cm.is_valid = 1",
					" WHERE m.is_valid = 1 AND m.pid = 0 GROUP BY m.id ORDER BY m.order_num ASC, m.id ASC", limit);
			list = queryDao.queryMap(sql);
		}

		return new PageVO(page, list, total);
	}

	/**
	 * 子模块分页
	 */
	public PageVO childrenPage(int page, Map<String, String> searchParams) {
		String limit = PageUtils.getLimitSql(page);

		String sqlCondition = " WHERE m.is_valid = 1";
		String totalCondtion = sqlCondition;
		String pid = MapUtils.getString(searchParams, "pid");
		if (NumberUtils.isNumber(pid)) {
			sqlCondition = SqlJoiner.join(sqlCondition, " AND m.pid =", pid);
			totalCondtion = SqlJoiner.join(totalCondtion, " AND m.pid = ", pid);
		}

		String totalSql = SqlJoiner.join("SELECT COUNT(1) FROM activity_over_activity_module m",
				" JOIN activity_over_activity_module pm ON m.pid = pm.id", totalCondtion);
		Number total = queryDao.query(totalSql);

		List<Map<String, Object>> list = null;
		if (total != null && total.intValue() > 0) {
			String sql = SqlJoiner.join("SELECT m.id, m.name, m.order_num orderNum, pm.name parentName",
					" FROM activity_over_activity_module m", " JOIN activity_over_activity_module pm ON m.pid = pm.id",
					sqlCondition, " ORDER BY m.order_num ASC, m.id ASC", limit);
			list = queryDao.queryMap(sql);
		}

		return new PageVO(page, list, total);
	}

	/**
	 * 更改资讯的排序序号
	 */
	public void changeOrderNum(long moduleId, int orderNum) {
		// 查询模块对象,并检查是否需要更新操作
		ActivityOverActivityModule info = findById(moduleId);
		if (info == null) {
			return;
		}
		// 更新自身排序序号
		info.setOrderNum(orderNum);
		info.setUpdateDate(new Date());
		save(info);
	}
}
