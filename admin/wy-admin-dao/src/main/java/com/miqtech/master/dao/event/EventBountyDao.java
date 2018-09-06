package com.miqtech.master.dao.event;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

import com.miqtech.master.entity.bounty.Bounty;

@Component
public interface EventBountyDao
		extends PagingAndSortingRepository<Bounty, Long>, JpaSpecificationExecutor<Bounty> {

}
