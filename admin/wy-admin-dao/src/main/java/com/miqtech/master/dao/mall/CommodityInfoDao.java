package com.miqtech.master.dao.mall;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.mall.CommodityInfo;

/**
 * （金币）商品操作Dao
 */
public interface CommodityInfoDao extends PagingAndSortingRepository<CommodityInfo, Long>,
		JpaSpecificationExecutor<CommodityInfo> {
	CommodityInfo findByIdAndStatusAndValid(Long id, int status, int valid);

}
