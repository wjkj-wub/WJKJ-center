package com.miqtech.master.service.system;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.SystemUserConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.common.OperateDao;
import com.miqtech.master.entity.common.Operate;
import com.miqtech.master.entity.common.SystemUser;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.utils.TreeUtils;

/**
 * 用户权限service
 */
@Component
public class OperateService {

	public static final String OLD_MENU_DATE = "2015-05-01 00:00:00";

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private OperateDao operateDao;
	@Autowired
	private RoleService roleService;
	@Autowired
	private SystemUserService systemUserService;

	/**
	 * 删除菜单
	 */
	public void delete(long id) {
		// 删除菜单
		Date now = new Date();
		Operate operate = operateDao.findOne(id);
		operate.setUpdateDate(now);
		operate.setValid(CommonConstant.INT_BOOLEAN_FALSE);
		operateDao.save(operate);

		// 删除不能匹配上关系的菜单
		String sqlNoParentOperates = "SELECT GROUP_CONCAT(id) ids FROM sys_t_operate c WHERE c.pid != 0 AND NOT EXISTS ( SELECT * FROM sys_t_operate p WHERE c.pid = p.id )";
		String ids = (String) queryDao.querySingleMap(sqlNoParentOperates).get("ids");
		if (StringUtils.isNotBlank(ids)) {
			String deleteIdsSql = SqlJoiner.join("UPDATE sys_t_operate SET is_valid = 0, update_date = '",
					DateUtils.dateToString(now, DateUtils.YYYY_MM_DD_HH_MM_SS), "' WHERE id IN (", ids,
					") OR pid IN (", ids, ")");
			queryDao.update(deleteIdsSql);
		}
	}

	/**
	 * 保存或更新对象
	 */
	public Operate save(Operate operate) {
		if (operate != null) {
			Date now = new Date();
			operate.setUpdateDate(now);
			if (operate.getId() != null) {
				operate = BeanUtils.updateBean(findById(operate.getId()), operate);
			} else {
				operate.setCreateDate(now);
				operate.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			}
			operate = operateDao.save(operate);
		}
		return operate;
	}

	/**
	 * 通过ID查询对象
	 */
	public Operate findById(Long id) {
		return operateDao.findOne(id);
	}

	/**
	 * 获取管理员的菜单
	 */
	public List<Map<String, Object>> getAdminOperates(long adminId, boolean allOperate) {
		// 判断用户是否系统管理员
		boolean isSysAdmin = false;
		SystemUser admin = systemUserService.findById(adminId);
		if (SystemUserConstant.TYPE_SUPER_ADMIN.equals(admin.getUserType())) {
			isSysAdmin = true;
		}

		// 根据用户角色查询菜单
		List<Map<String, Object>> operates = null;
		if (isSysAdmin) {
			operates = getOperateTree();
		} else {
			String userRoleIds = roleService.getUserRoles(adminId);
			if (StringUtils.isBlank(userRoleIds)) {// 没有设置角色的用户不能查出任何菜单
				userRoleIds = "-1";
			}
			operates = getOperateTreeByRoleIds(userRoleIds, allOperate);
		}

		return operates;
	}

	/**
	 * 查询所有权限树
	 */
	public List<Map<String, Object>> getOperateTree() {
		return getOperateTreeByRoleIds(null, true);
	}

	/**
	 * 获取用户的权限树
	 */
	public List<Map<String, Object>> getOperateTreeByUserId(Long userId, boolean allOperate) {
		String rids = roleService.getUserRoles(userId);
		return getOperateTreeByRoleIds(rids, allOperate);
	}

	/**
	 * 根据角色ids查询权限树（rids为null时不限制角色）
	 */
	public List<Map<String, Object>> getOperateTreeByRoleIds(String rids, boolean allOperate) {
		if (rids == null) {
			rids = "0";
		}

		// 获取用户所有菜单
		List<Map<String, Object>> operates = getOperates(rids, allOperate);

		// 排列为树形并返回
		if (CollectionUtils.isNotEmpty(operates)) {
			return TreeUtils.buildOperateTree(operates);
		} else {
			return null;
		}
	}

	/**
	 * 获取所有菜单
	 */
	private List<Map<String, Object>> getOperates(String rids, boolean allOperate) {
		String sql = SqlJoiner
				.join("SELECT DISTINCT t.id, t.pid, t.name, t.order_id orderId, t.url, IF (ISNULL(ro.id), 0, 1) checked",
						" FROM sys_t_operate t LEFT JOIN sys_r_role_operate ro ON t.id = ro.operate_id AND ro.is_valid = 1 AND ro.role_id IN (",
						rids, ")", " WHERE t.is_valid = 1 AND t.create_date >= '", OLD_MENU_DATE, "'");
		if (StringUtils.isNotBlank(rids) && !allOperate) {
			sql = SqlJoiner.join(sql, " AND (ro.role_id in (", rids, "))");
		}
		sql = SqlJoiner.join(sql, " ORDER BY t.pid ASC, t.`order_id` ASC, t.`create_date` ASC");
		return queryDao.queryMap(sql);
	}
}