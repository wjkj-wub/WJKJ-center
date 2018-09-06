package com.miqtech.master.dao.netbar.resource;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.resource.NetbarResourceCommodityProperty;

public interface NetbarResourceCommodityPropertyDao extends
		PagingAndSortingRepository<NetbarResourceCommodityProperty, Long>,
		JpaSpecificationExecutor<NetbarResourceCommodityProperty> {

	NetbarResourceCommodityProperty findById(Long id);

	NetbarResourceCommodityProperty findByIdAndValid(Long id, Integer valid);

	List<NetbarResourceCommodityProperty> findByCommodityIdAndStatusAndValid(Long commodityId, Integer status,
			Integer valid);

	List<NetbarResourceCommodityProperty> findByIdIn(List<Long> ids);

}