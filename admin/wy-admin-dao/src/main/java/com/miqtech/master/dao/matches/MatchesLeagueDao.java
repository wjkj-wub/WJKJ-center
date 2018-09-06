package com.miqtech.master.dao.matches;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.matches.MatchesLeague;

public interface MatchesLeagueDao
		extends JpaSpecificationExecutor<MatchesLeague>, PagingAndSortingRepository<MatchesLeague, Long> {

	public List<MatchesLeague> findByItemsIdAndOrganiserIdAndValid(Long itemsId, Long organiserId, Integer valid);

	public List<MatchesLeague> findByOrganiserId(Long organiserId);
}
