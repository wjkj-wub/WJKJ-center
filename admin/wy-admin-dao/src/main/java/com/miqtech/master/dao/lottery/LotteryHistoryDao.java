package com.miqtech.master.dao.lottery;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.lottery.LotteryHistory;

public interface LotteryHistoryDao extends PagingAndSortingRepository<LotteryHistory, Long>,
		JpaSpecificationExecutor<LotteryHistory> {

	LotteryHistory findByIdAndValid(Long id, Integer valid);

}
