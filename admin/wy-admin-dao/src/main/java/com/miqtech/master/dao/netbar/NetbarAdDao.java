package com.miqtech.master.dao.netbar;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarAd;

/**
 * 网吧pc客户端广告
 */
public interface NetbarAdDao extends PagingAndSortingRepository<NetbarAd, Long>, JpaSpecificationExecutor<NetbarAd> {

	Iterable<NetbarAd> findByValid(Integer valid);

}