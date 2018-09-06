package com.miqtech.master.dao.netbar;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarClientInfo;

public interface NetbarClientInfoDao
		extends PagingAndSortingRepository<NetbarClientInfo, Long>, JpaSpecificationExecutor<NetbarClientInfo> {

	NetbarClientInfo findByNetbarId(Long netbarId);
}