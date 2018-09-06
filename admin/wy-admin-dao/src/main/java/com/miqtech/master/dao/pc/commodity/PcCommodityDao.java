package com.miqtech.master.dao.pc.commodity;

import com.miqtech.master.entity.pc.commodity.PcCommodity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;


public interface PcCommodityDao extends PagingAndSortingRepository<PcCommodity, Long>,
		JpaSpecificationExecutor<PcCommodity> {

	PcCommodity findByIdAndIsValid(long id, Boolean valid);

}