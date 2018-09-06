package com.miqtech.master.dao.lottery;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.lottery.LotteryOption;

public interface LotteryOptionDao extends PagingAndSortingRepository<LotteryOption, Long>,
		JpaSpecificationExecutor<LotteryOption> {

	LotteryOption findByIdAndValid(Long id, Integer valid);

	LotteryOption findById(Long id);

	/**
	 * 查找有效的抽奖活动
	 */
	@Query("select sa from LotteryOption sa where valid=1 order by id")
	List<LotteryOption> findAllValid();
}
