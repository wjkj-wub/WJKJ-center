package com.miqtech.master.dao.netbar;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarAreaPrice;

public interface NetbarAreaPriceDao
		extends PagingAndSortingRepository<NetbarAreaPrice, Long>, JpaSpecificationExecutor<NetbarAreaPrice> {

	List<NetbarAreaPrice> findAllByNetbarIdAndValid(Long netbarId, int valid);

}