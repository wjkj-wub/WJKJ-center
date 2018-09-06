package com.miqtech.master.dao.netbar;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarStaffBatchOrder;

public interface NetbarStaffBatchOrderDao extends PagingAndSortingRepository<NetbarStaffBatchOrder, Long>,
		JpaSpecificationExecutor<NetbarStaffBatchOrder> {

}
