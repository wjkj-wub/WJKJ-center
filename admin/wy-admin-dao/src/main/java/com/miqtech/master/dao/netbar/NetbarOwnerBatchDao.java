package com.miqtech.master.dao.netbar;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarOwnerBatch;

public interface NetbarOwnerBatchDao extends PagingAndSortingRepository<NetbarOwnerBatch, Long>,
		JpaSpecificationExecutor<NetbarOwnerBatch> {

	List<NetbarOwnerBatch> findByIdIn(List<Long> ids);
}
