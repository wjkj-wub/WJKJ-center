package com.miqtech.master.dao.netbar;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarStaffBatch;

public interface NetbarStaffBatchDao extends PagingAndSortingRepository<NetbarStaffBatch, Long>,
		JpaSpecificationExecutor<NetbarStaffBatch> {

	List<NetbarStaffBatch> findByNetbarIdAndStatus(Long netbarId, Integer status);

	List<NetbarStaffBatch> findByIdInAndValid(Long[] ids, Integer valid);

	List<NetbarStaffBatch> findByIdIn(List<Long> ids);
}
