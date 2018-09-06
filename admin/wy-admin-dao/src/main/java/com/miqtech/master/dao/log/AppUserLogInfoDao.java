package com.miqtech.master.dao.log;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.log.AppUserLogInfo;

public interface AppUserLogInfoDao extends PagingAndSortingRepository<AppUserLogInfo, Long>,
		JpaSpecificationExecutor<AppUserLogInfo> {

}
