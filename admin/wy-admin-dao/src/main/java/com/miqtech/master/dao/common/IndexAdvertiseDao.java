package com.miqtech.master.dao.common;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.common.IndexAdvertise;

/**
 * 首页广告操作DAO
 */
public interface IndexAdvertiseDao
		extends PagingAndSortingRepository<IndexAdvertise, Long>, JpaSpecificationExecutor<IndexAdvertise> {

	/**
	 * 查找id查询广告
	 */
	IndexAdvertise findById(Long id);

	/**
	 * 根据状态查找首页广告信息
	 * @param valid 0无效 1有效
	 */
	List<IndexAdvertise> findByValid(Integer valid);

}