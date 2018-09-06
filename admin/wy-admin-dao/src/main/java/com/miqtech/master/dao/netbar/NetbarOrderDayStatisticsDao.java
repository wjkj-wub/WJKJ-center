package com.miqtech.master.dao.netbar;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarOrderDayStatistics;

public interface NetbarOrderDayStatisticsDao
		extends PagingAndSortingRepository<NetbarOrderDayStatistics, Long>,
		JpaSpecificationExecutor<NetbarOrderDayStatistics> {

}