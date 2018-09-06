package com.miqtech.master.dao.mall;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.mall.CommodityHistory;

/**
 * （金币）商品历史记录Dao
 */
public interface CommodityHistoryDao
		extends PagingAndSortingRepository<CommodityHistory, Long>, JpaSpecificationExecutor<CommodityHistory> {

}
