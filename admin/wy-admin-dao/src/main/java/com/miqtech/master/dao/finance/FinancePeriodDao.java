package com.miqtech.master.dao.finance;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.finance.Period;

public interface FinancePeriodDao extends JpaSpecificationExecutor<Period>, PagingAndSortingRepository<Period, Long> {
	/**
	 * 查找所有记录
	 */
	@Override
	@Query("select fp from Period fp")
	List<Period> findAll();

	public List<Period> findByValid(Integer valid);

}
