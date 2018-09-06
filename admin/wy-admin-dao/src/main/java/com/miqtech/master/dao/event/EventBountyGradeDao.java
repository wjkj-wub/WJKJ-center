package com.miqtech.master.dao.event;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

import com.miqtech.master.entity.bounty.BountyGrade;

@Component
public interface EventBountyGradeDao
		extends PagingAndSortingRepository<BountyGrade, Long>, JpaSpecificationExecutor<BountyGrade> {

}
