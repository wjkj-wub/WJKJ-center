package com.miqtech.master.dao.award;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.award.AwardInventory;

public interface AwardInventoryDao extends PagingAndSortingRepository<AwardInventory, Long>,
		JpaSpecificationExecutor<AwardInventory> {

	AwardInventory findByIdAndValid(Long id, Integer valid);

}
