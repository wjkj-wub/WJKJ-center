package com.miqtech.master.dao.lottery;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.lottery.LotteryAwardSeat;

public interface LotteryAwardSeatDao extends PagingAndSortingRepository<LotteryAwardSeat, Long>,
		JpaSpecificationExecutor<LotteryAwardSeat> {

	LotteryAwardSeat findById(Long id);

}
