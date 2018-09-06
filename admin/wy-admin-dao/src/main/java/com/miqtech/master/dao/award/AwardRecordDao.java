package com.miqtech.master.dao.award;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.award.AwardRecord;

public interface AwardRecordDao extends PagingAndSortingRepository<AwardRecord, Long>,
		JpaSpecificationExecutor<AwardRecord> {

	List<AwardRecord> findByUserIdInAndValid(List<Long> userIds, Integer valid);
}
