package com.miqtech.master.dao.event;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.event.EventRound;

public interface EventRoundDao
		extends PagingAndSortingRepository<EventRound, Long>, JpaSpecificationExecutor<EventRound> {
	
}
