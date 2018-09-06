package com.miqtech.master.dao.netbar.resource;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.resource.NetbarResourceCommodity;

public interface NetbarResourceCommodityDao extends PagingAndSortingRepository<NetbarResourceCommodity, Long>,
		JpaSpecificationExecutor<NetbarResourceCommodity> {

	NetbarResourceCommodity findById(Long id);

	NetbarResourceCommodity findByIdAndValid(Long id, Integer valid);

}