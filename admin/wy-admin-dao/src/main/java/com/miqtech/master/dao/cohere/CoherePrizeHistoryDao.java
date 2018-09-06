package com.miqtech.master.dao.cohere;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.cohere.CoherePrizeHistory;

public interface CoherePrizeHistoryDao
		extends JpaSpecificationExecutor<CoherePrizeHistory>, PagingAndSortingRepository<CoherePrizeHistory, Long> {

	public List<CoherePrizeHistory> findByUserIdAndStateAndValid(Long userId, Integer state, Integer valid);
}
