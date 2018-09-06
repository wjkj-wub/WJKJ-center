package com.miqtech.master.dao.common;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.common.RoleOperate;

/**
 * 角色权限操作DAO
 */
public interface RoleOperateDao extends PagingAndSortingRepository<RoleOperate, Long>,
		JpaSpecificationExecutor<RoleOperate> {
}