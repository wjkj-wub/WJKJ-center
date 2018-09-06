package com.miqtech.master.dao.bounty;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.bounty.BountyDailyTip;

public interface BountyDailyTipDao
		extends PagingAndSortingRepository<BountyDailyTip, Long>, JpaSpecificationExecutor<BountyDailyTip> {

	public List<BountyDailyTip> findByBountyIdOrderByCreateDateDesc(Long bountyId);
}
