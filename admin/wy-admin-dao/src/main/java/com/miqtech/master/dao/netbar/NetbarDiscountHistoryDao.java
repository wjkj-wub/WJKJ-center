package com.miqtech.master.dao.netbar;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarDiscountHistory;

/**
 * 网吧优惠信息历史数据操作DAO
 */
public interface NetbarDiscountHistoryDao extends PagingAndSortingRepository<NetbarDiscountHistory, Long>,
		JpaSpecificationExecutor<NetbarDiscountHistory> {

}
