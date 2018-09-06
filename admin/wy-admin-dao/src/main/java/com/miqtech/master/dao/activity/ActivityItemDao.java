package com.miqtech.master.dao.activity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.activity.ActivityItem;

/**
 * 约战竞技项目操作DAO
 */
public interface ActivityItemDao extends PagingAndSortingRepository<ActivityItem, Long>,
		JpaSpecificationExecutor<ActivityItem> {

	/**
	 * 根据状态有效的竞技项目列表
	 * @param isValid 0无效 1有效
	 */
	List<ActivityItem> findByValid(Integer isValid);

	/**
	 * 查找id查询竞技项目信息
	 */
	ActivityItem findById(Long id);
}