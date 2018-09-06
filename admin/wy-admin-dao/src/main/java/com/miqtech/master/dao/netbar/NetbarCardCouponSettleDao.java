package com.miqtech.master.dao.netbar;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarCardCouponSettle;

public interface NetbarCardCouponSettleDao extends PagingAndSortingRepository<NetbarCardCouponSettle, Long>,
		JpaSpecificationExecutor<NetbarCardCouponSettle> {

	public List<NetbarCardCouponSettle> findByIdIn(List<Long> ids);
}
