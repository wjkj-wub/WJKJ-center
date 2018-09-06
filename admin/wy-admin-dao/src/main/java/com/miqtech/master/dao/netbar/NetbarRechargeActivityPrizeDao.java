package com.miqtech.master.dao.netbar;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarRechargeActivityPrize;

/**
 * 网吧充值活动DAO
 */
public interface NetbarRechargeActivityPrizeDao extends JpaSpecificationExecutor<NetbarRechargeActivityPrize>,
		PagingAndSortingRepository<NetbarRechargeActivityPrize, Long> {

	List<NetbarRechargeActivityPrize> findByActivityIdAndValid(Integer activityId, Integer valid);
}
