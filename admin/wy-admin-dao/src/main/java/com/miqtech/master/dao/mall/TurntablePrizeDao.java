package com.miqtech.master.dao.mall;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.mall.TurntablePrize;

/**
 * 转盘商品操作Dao
 */
public interface TurntablePrizeDao extends JpaSpecificationExecutor<TurntablePrize>,
		PagingAndSortingRepository<TurntablePrize, Long> {

}
