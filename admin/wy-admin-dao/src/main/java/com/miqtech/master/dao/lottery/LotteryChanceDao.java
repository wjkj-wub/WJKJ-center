package com.miqtech.master.dao.lottery;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.lottery.LotteryChance;

public interface LotteryChanceDao extends PagingAndSortingRepository<LotteryChance, Long>,
		JpaSpecificationExecutor<LotteryChance> {

	List<LotteryChance> findByLotteryIdAndUserId(Long lotteryId, Long userId);

}
