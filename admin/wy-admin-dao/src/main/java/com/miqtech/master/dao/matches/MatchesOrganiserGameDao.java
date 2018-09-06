package com.miqtech.master.dao.matches;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.matches.MatchesOrganiserGame;

public interface MatchesOrganiserGameDao
		extends JpaSpecificationExecutor<MatchesOrganiserGame>, PagingAndSortingRepository<MatchesOrganiserGame, Long> {

	public List<MatchesOrganiserGame> findByOrganiserId(Long organiserId);
}
