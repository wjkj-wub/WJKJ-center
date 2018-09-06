package com.miqtech.master.dao.finance;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.finance.PayNotice;

public interface FinancePayNoticeDao extends JpaSpecificationExecutor<PayNotice>,
		PagingAndSortingRepository<PayNotice, Long> {

	public List<PayNotice> findByAmountLessThanEqualAndValid(Double amount, Integer valid);

	public List<PayNotice> findByValid(Integer valid);
}
