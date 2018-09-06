package com.miqtech.master.dao.event;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

import com.miqtech.master.entity.bounty.BountyPrize;

@Component
public interface EventBountyPrizeDao
		extends PagingAndSortingRepository<BountyPrize, Long>, JpaSpecificationExecutor<BountyPrize> {

}
