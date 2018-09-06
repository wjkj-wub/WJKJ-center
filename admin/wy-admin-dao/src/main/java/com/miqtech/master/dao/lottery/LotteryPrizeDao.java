package com.miqtech.master.dao.lottery;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.lottery.LotteryPrize;

public interface LotteryPrizeDao extends PagingAndSortingRepository<LotteryPrize, Long>,
		JpaSpecificationExecutor<LotteryPrize> {

	LotteryPrize findById(Long id);

	/**
	 * 查找有效的奖品
	 */
	@Query("select sa from LotteryPrize sa where valid=1 order by id")
	List<LotteryPrize> findAllValid();

	/**
	 * 查找无效的奖品
	 */
	@Query("select sa from LotteryPrize sa where valid<>1 order by id")
	List<LotteryPrize> findAllInvalid();
}
