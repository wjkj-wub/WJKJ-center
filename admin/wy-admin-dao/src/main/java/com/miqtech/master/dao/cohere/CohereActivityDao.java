package com.miqtech.master.dao.cohere;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.activity.ActivityAdvertise;
import com.miqtech.master.entity.cohere.CohereActivity;

public interface CohereActivityDao
		extends PagingAndSortingRepository<CohereActivity, Long>, JpaSpecificationExecutor<ActivityAdvertise> {

	/**
	 * 查找id查询活动
	 */
	CohereActivity findById(Long id);
}
