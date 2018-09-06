package com.miqtech.master.dao.netbar;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarOrderDisagree;

/**
 * 有异议订单操作DAO
 */
public interface NetbarOrderDisagreeDao extends PagingAndSortingRepository<NetbarOrderDisagree, Long>,
		JpaSpecificationExecutor<NetbarOrderDisagree> {
}