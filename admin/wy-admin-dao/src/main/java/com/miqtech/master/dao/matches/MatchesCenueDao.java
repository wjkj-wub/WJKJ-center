package com.miqtech.master.dao.matches;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.matches.MatchesCenue;

public interface MatchesCenueDao
		extends JpaSpecificationExecutor<MatchesCenue>, PagingAndSortingRepository<MatchesCenue, Long> {

}
