package com.miqtech.master.dao.mall;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.mall.CommodityIcon;

/**
 * 商品Icon操作Dao
 */
public interface CommodityIconDao extends PagingAndSortingRepository<CommodityIcon, Long>,
		JpaSpecificationExecutor<CommodityIcon> {

	List<CommodityIcon> findByCommodityIdAndValid(Long commodityId, Integer valid);
}
