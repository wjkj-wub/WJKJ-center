package com.miqtech.master.dao.mall;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.mall.CommodityHistory;

public interface TurntableVirtualDao
		extends PagingAndSortingRepository<CommodityHistory, Long>, JpaSpecificationExecutor<CommodityHistory> {

}
