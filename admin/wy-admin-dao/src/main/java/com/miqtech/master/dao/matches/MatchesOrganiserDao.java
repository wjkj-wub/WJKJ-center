package com.miqtech.master.dao.matches;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.matches.MatchesOrganiser;

public interface MatchesOrganiserDao
		extends JpaSpecificationExecutor<MatchesOrganiser>, PagingAndSortingRepository<MatchesOrganiser, Long> {

	/**
	 * 根据状态有效值
	 * 
	 * @param isValid
	 *            0无效 1有效
	 */
	List<MatchesOrganiser> findByValid(Integer isValid);
}
