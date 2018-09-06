package com.miqtech.master.dao.netbar;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarRechargeOrder;

/**
 * 网吧订单操作DAO
 */
public interface NetbarRechargeOrderDao
		extends PagingAndSortingRepository<NetbarRechargeOrder, Long>, JpaSpecificationExecutor<NetbarRechargeOrder> {

	public List<NetbarRechargeOrder> findByOutTradeNoAndValid(String outTradeNo, Integer valid);
}