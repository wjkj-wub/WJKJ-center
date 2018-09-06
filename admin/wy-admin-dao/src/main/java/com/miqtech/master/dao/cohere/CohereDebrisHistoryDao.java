package com.miqtech.master.dao.cohere;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.cohere.CohereDebrisHistory;

public interface CohereDebrisHistoryDao
		extends JpaSpecificationExecutor<CohereDebrisHistory>, PagingAndSortingRepository<CohereDebrisHistory, Long> {

	public CohereDebrisHistory findByValidAndUserIdAndDrawIdAndIsUsed(Integer isValid,Long userId,Long drawId,Integer isUsed);
	
	public CohereDebrisHistory findByIsUsedAndOutTypeAndValidAndUserIdAndDrawIdAndOutIdIsNull(Integer isUsed, Integer outType, Integer isValid, Long userId, Long drawId);
}
