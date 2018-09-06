package com.miqtech.master.dao.mall;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.mall.CommodityCategory;

/**
 * 商品类别操作Dao
 */
public interface CommodityCategoryDao extends JpaSpecificationExecutor<CommodityCategory>,
		PagingAndSortingRepository<CommodityCategory, Long> {

	List<CommodityCategory> findBySuperTypeAndValid(Integer superType, Integer valid);
}
