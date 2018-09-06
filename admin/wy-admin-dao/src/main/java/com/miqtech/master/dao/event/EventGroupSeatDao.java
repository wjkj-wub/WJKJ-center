package com.miqtech.master.dao.event;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.event.EventGroupSeat;

public interface EventGroupSeatDao
		extends PagingAndSortingRepository<EventGroupSeat, Long>, JpaSpecificationExecutor<EventGroupSeat> {
}
