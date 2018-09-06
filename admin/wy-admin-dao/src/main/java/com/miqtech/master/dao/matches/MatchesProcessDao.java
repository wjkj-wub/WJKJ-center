package com.miqtech.master.dao.matches;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.matches.MatchesProcess;

public interface MatchesProcessDao
		extends JpaSpecificationExecutor<MatchesProcess>, PagingAndSortingRepository<MatchesProcess, Long> {

}
