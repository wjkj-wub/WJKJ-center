package com.miqtech.master.dao.cohere;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.cohere.CohereDebris;

public interface CohereDebrisDao
		extends JpaSpecificationExecutor<CohereDebris>, PagingAndSortingRepository<CohereDebris, Long> {

	public List<CohereDebris> findByActivityId(Long activityId);
}
