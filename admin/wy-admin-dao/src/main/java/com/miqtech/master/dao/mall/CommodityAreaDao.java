package com.miqtech.master.dao.mall;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.mall.CommodityArea;

/**
 * 商品展示区操作Dao
 */
public interface CommodityAreaDao extends JpaSpecificationExecutor<CommodityArea>,
		PagingAndSortingRepository<CommodityArea, Long> {

	List<CommodityArea> findByValid(Integer valid);

}
