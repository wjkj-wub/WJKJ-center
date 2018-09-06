package com.miqtech.master.dao.netbar;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarFundDetail;

/**
 * 网吧资金明细操作DAO
 */
public interface NetbarFundDetailDao
		extends PagingAndSortingRepository<NetbarFundDetail, Long>, JpaSpecificationExecutor<NetbarFundDetail> {

	List<NetbarFundDetail> findBySerNumbersAndDirectionAndValid(String serNumbers, Integer direction, Integer valid);

	List<NetbarFundDetail> findByNetbarIdAndValidAndStatusAndDirectionAndType(Long netbarId, int valid, int status,
			int direction, int type);

	List<NetbarFundDetail> findBySerNumbersAndValid(String serNumbers, Integer valid);

	NetbarFundDetail findBySerNumbersAndValidAndType(String requestSN, Integer intBooleanTrue, int type);
}