package com.miqtech.master.dao.cohere;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.cohere.CohereDraw;

public interface CohereDrawDao
		extends JpaSpecificationExecutor<CohereDraw>, PagingAndSortingRepository<CohereDraw, Long> {

}
