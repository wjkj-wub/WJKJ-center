package com.miqtech.master.service.system;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.utils.StringUtils;

/**
 * 用户和角色关系service
 */
@Component
public class SystemUserRoleService {

	@Autowired
	private QueryDao queryDao;

	/**
	 * 重新设置用户角色
	 */
	public void saveUserRoles(Long userId, String roleIds) {
		if (userId != null && StringUtils.isNotBlank(roleIds)) {
			// 删除用户角色
			String deleteSql = SqlJoiner.join("DELETE FROM sys_r_user_role WHERE sys_user_id = ", userId.toString());
			queryDao.update(deleteSql);

			// 保存用户新角色
			String[] cRoleIds = roleIds.split(",");
			if (cRoleIds != null) {
				for (String roleIdStr : cRoleIds) {
					if (NumberUtils.isNumber(roleIdStr)) {
						String insertSql = SqlJoiner
								.join("INSERT INTO sys_r_user_role(sys_user_id, role_id, is_valid, update_date, create_date) VALUE(",
										userId.toString(), ",", roleIdStr, ",1,NOW(),NOW())");
						queryDao.update(insertSql);
					}
				}
			}
		}
	}

	/**
	 * 查询用户所有角色
	 */
	public List<Map<String, Object>> getUserRoles(Long userId) {
		String sql = SqlJoiner
				.join("SELECT role_id FROM sys_r_user_role ur WHERE ur.is_valid = 1 AND ur.sys_user_id = ",
						userId.toString());
		return queryDao.queryMap(sql);
	}
}