package com.miqtech.master.dao.netbar.resource;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.resource.NetbarResourceOrder;

public interface NetbarResourceOrderDao extends PagingAndSortingRepository<NetbarResourceOrder, Long>,
		JpaSpecificationExecutor<NetbarResourceOrder> {

	public NetbarResourceOrder findByIdAndValid(Long id, Integer valid);

	public List<NetbarResourceOrder> findByIdInAndValid(List<Long> ids, Integer valid);

	public List<NetbarResourceOrder> findByCommodityIdAndNetbarIdAndValidAndStatusIn(Long commodityId, Long netbarId,
			Integer valid, List<Integer> status);

	public List<NetbarResourceOrder> findByStatusInAndExpireDateGreaterThanEqualAndExpireDateLessThanEqualAndValid(
			List<Integer> status, Date expireDateBegin, Date expireDateEnd, Integer valid);
}