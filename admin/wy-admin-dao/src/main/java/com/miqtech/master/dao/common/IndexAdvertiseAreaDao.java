package com.miqtech.master.dao.common;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.common.IndexAdvertiseArea;

/**
 * 首页广告操作DAO
 */
public interface IndexAdvertiseAreaDao extends PagingAndSortingRepository<IndexAdvertiseArea, Long>,
		JpaSpecificationExecutor<IndexAdvertiseArea> {
	IndexAdvertiseArea findByAdvertiseId(Long id);
}