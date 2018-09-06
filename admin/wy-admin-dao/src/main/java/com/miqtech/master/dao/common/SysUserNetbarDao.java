package com.miqtech.master.dao.common;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.common.SysUserNetbar;

public interface SysUserNetbarDao extends PagingAndSortingRepository<SysUserNetbar, Long>,
		JpaSpecificationExecutor<SysUserNetbar> {
	SysUserNetbar findByNetbarId(Long netbarId);

	List<SysUserNetbar> findBySysUserId(Long id);
}
