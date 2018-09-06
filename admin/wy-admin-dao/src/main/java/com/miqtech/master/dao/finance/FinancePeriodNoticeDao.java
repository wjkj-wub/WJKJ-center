package com.miqtech.master.dao.finance;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.finance.PeriodNotify;

public interface FinancePeriodNoticeDao extends PagingAndSortingRepository<PeriodNotify, Long>,
		JpaSpecificationExecutor<PeriodNotify> {

	List<PeriodNotify> findByValid(Integer valid);

}
