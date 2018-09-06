package com.miqtech.master.dao.lottery;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.lottery.LotteryAward;

public interface LotteryAwardDao extends PagingAndSortingRepository<LotteryAward, Long>,
		JpaSpecificationExecutor<LotteryAward> {

	LotteryAward findById(Long id);

	LotteryAward findByIdAndValid(Long id, Integer valid);

	List<LotteryAward> findByLotteryIdAndName(Long lotteryId, String name);

}
