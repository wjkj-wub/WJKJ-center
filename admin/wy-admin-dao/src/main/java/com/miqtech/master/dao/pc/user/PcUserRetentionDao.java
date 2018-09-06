package com.miqtech.master.dao.pc.user;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.pc.user.PcUserRetention;

public interface PcUserRetentionDao
		extends PagingAndSortingRepository<PcUserRetention, Long>, JpaSpecificationExecutor<PcUserRetention> {

}
