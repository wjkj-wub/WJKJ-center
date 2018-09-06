package com.miqtech.master.service.system;

import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.utils.SqlJoiner;

/**
 * 角色和权限server
 */
@Component
public class RoleOperateService {

	@Autowired
	private QueryDao queryDao;

	/**
	 * 更新角色权限
	 */
	public void updateRoleOperates(Long roleId, String operateIds) {
		if (roleId != null && operateIds != null) {
			// 删除原权限
			String sql = SqlJoiner.join("DELETE FROM sys_r_role_operate WHERE role_id = ", roleId.toString(),
					" AND create_date >= '", OperateService.OLD_MENU_DATE, "'");
			queryDao.update(sql);

			// 新增权限
			String insertSql = "";
			String[] ids = operateIds.split(",");
			for (String id : ids) {
				if (NumberUtils.isNumber(id)) {
					insertSql = SqlJoiner
							.join("INSERT INTO sys_r_role_operate(role_id, operate_id, is_valid, update_date, create_date) VALUES(",
									roleId.toString(), ",", id, ",1,NOW(),NOW());");
					queryDao.update(insertSql);
				}
			}
		}
	}
}