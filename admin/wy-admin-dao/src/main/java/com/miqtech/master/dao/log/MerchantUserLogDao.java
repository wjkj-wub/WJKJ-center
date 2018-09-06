package com.miqtech.master.dao.log;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.log.MerchantUserLog;

public interface MerchantUserLogDao extends PagingAndSortingRepository<MerchantUserLog, Long>,
		JpaSpecificationExecutor<MerchantUserLog> {

}
