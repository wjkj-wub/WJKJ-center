package com.miqtech.master.dao.bounty;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.bounty.BountyGrade;

public interface BountyGradeDao
		extends PagingAndSortingRepository<BountyGrade, Long>, JpaSpecificationExecutor<BountyGrade> {
	List<BountyGrade> findByBountyIdAndValidOrderByCreateDateDesc(Long bountyId, Integer valid);
	
	List<BountyGrade> findByBountyIdAndValidAndUserIdOrderByCreateDateDesc(Long bountyId, Integer valid, Long userId);
}
