package com.miqtech.master.dao.activity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.activity.ActivityInfo;

/**
 * 赛事信息操作DAO
 */
public interface ActivityInfoDao extends PagingAndSortingRepository<ActivityInfo, Long>,
		JpaSpecificationExecutor<ActivityInfo> {

	/**
	 * 根据id查找赛事信息
	 */
	ActivityInfo findById(Long id);

	/**
	 * 查找所有有效的赛事信息
	 */
	List<ActivityInfo> findByValid(Integer isValid);
}