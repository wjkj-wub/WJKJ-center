package com.miqtech.master.dao.activity;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.activity.WjMatch;

/**
 * 网竞联赛操作DAO
 */
public interface WjMatchDao extends PagingAndSortingRepository<WjMatch, Long>, JpaSpecificationExecutor<WjMatch> {

	/**
	 * 根据id查找网竞联赛信息
	 */
	WjMatch findById(Long id);

}