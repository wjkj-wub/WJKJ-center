package com.miqtech.master.dao.bounty;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.bounty.Bounty;

public interface BountyDao extends PagingAndSortingRepository<Bounty, Long>, JpaSpecificationExecutor<Bounty> {
	List<Bounty> findByItemIdOrderByEndTimeDesc(Long itemId);
}
