package com.miqtech.master.dao.netbar;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarCardCoupon;

public interface NetbarCardCouponDao
		extends PagingAndSortingRepository<NetbarCardCoupon, Long>, JpaSpecificationExecutor<NetbarCardCoupon> {

	List<NetbarCardCoupon> findByCouponSettleIdIn(List<Long> couponSettleIds);

	List<NetbarCardCoupon> findByIdInAndValid(Long[] idsArray, Integer intBooleanTrue);
}
