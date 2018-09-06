package com.miqtech.master.dao.pc.user;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.pc.user.PcUserLoginHistory;

public interface PcUserLoginHistoryDao
		extends PagingAndSortingRepository<PcUserLoginHistory, Long>, JpaSpecificationExecutor<PcUserLoginHistory> {

}
