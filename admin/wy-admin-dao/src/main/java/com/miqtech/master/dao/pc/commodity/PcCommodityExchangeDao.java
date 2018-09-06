package com.miqtech.master.dao.pc.commodity;

import com.miqtech.master.entity.pc.commodity.PcCommodityExchange;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;


public interface PcCommodityExchangeDao extends PagingAndSortingRepository<PcCommodityExchange, Long>,
		JpaSpecificationExecutor<PcCommodityExchange> {

	PcCommodityExchange findByIdAndIsValid(long id, Boolean valid);

}