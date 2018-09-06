package com.miqtech.master.dao.common;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.common.ApplicationVersion;

/**
 * 客户端版本信息操作DAO
 */
public interface ApplicationVersionDao extends PagingAndSortingRepository<ApplicationVersion, Long>,
		JpaSpecificationExecutor<ApplicationVersion> {

	/**
	 * 查找版本信息
	 * @param type 操作系统类型 1-安卓;2-IOS;
	 * @param valid 0无效 1有效
	 */
	ApplicationVersion findByTypeAndValidAndSystemType(int type, int valid, int systemType);

}