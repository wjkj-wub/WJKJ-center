package com.miqtech.master.dao.netbar;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarRechargeActivity;

/**
 * 网吧充值活动DAO
 */
public interface NetbarRechargeActivityDao extends JpaSpecificationExecutor<NetbarRechargeActivity>,
		PagingAndSortingRepository<NetbarRechargeActivity, Long> {
	List<NetbarRechargeActivity> findByNetbarIdAndValidOrderByIdDesc(Integer netbarId, Integer valid);

	List<NetbarRechargeActivity> findByValidAndStartStatusAndNetbarId(Integer valid, Integer startStatus,
			Integer netbarId);

}
