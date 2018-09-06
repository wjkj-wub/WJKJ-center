package com.miqtech.master.dao.bounty;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.bounty.BountyPrize;

public interface BountyPrizeDao
		extends PagingAndSortingRepository<BountyPrize, Long>, JpaSpecificationExecutor<BountyPrize> {

	BountyPrize findByBountyIdAndValid(Long bountyId, Integer valid);
}
