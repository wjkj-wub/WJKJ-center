package com.miqtech.master.dao.activity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.activity.ActivityAdvertise;

/**
 * 赛事广告信息操作DAO
 */
public interface ActivityAdvertiseDao extends PagingAndSortingRepository<ActivityAdvertise, Long>,
		JpaSpecificationExecutor<ActivityAdvertise> {
	/**
	 * 查找id查询广告
	 */
	ActivityAdvertise findById(Long id);

	/**
	 * 查找有效的广告
	 */
	List<ActivityAdvertise> findByValid(Integer valid);

}