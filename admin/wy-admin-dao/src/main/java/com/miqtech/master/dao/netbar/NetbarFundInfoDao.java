package com.miqtech.master.dao.netbar;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarFundInfo;

/**
 * 网吧资金信息操作DAO
 */
public interface NetbarFundInfoDao extends PagingAndSortingRepository<NetbarFundInfo, Long>,
		JpaSpecificationExecutor<NetbarFundInfo> {

	List<NetbarFundInfo> findByNetbarIdIn(Collection<Long> netbarIds);

	NetbarFundInfo findByNetbarIdAndValid(Long netbarId, Integer valid);
}