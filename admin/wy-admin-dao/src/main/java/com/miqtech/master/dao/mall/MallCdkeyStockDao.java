package com.miqtech.master.dao.mall;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.mall.MallCdkeyStock;

public interface MallCdkeyStockDao extends JpaSpecificationExecutor<MallCdkeyStock>,
		PagingAndSortingRepository<MallCdkeyStock, Long> {
	MallCdkeyStock findByCommodityId(Long id);
}
