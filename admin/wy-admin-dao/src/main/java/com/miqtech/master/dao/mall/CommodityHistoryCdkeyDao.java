package com.miqtech.master.dao.mall;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.mall.CommodityHistoryCdkey;

public interface CommodityHistoryCdkeyDao extends PagingAndSortingRepository<CommodityHistoryCdkey, Long>,
		JpaSpecificationExecutor<CommodityHistoryCdkey> {

}
