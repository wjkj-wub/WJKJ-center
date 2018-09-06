package com.miqtech.master.dao.common;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.common.OperateLog;

public interface OperateLogDao
		extends PagingAndSortingRepository<OperateLog, Long>, JpaSpecificationExecutor<OperateLog> {
}