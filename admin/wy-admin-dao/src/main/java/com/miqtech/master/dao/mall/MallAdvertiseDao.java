package com.miqtech.master.dao.mall;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.mall.MallAdvertise;

/**
 * 商城广告Dao
 */
public interface MallAdvertiseDao
		extends PagingAndSortingRepository<MallAdvertise, Long>, JpaSpecificationExecutor<MallAdvertise> {

}
