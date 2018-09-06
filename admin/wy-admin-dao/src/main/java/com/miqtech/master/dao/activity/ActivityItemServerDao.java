package com.miqtech.master.dao.activity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.activity.ActivityServer;

/**
 * 约战竞技项目操作DAO
 */
public interface ActivityItemServerDao extends PagingAndSortingRepository<ActivityServer, Long>,
		JpaSpecificationExecutor<ActivityServer> {

	/**
	 * 根据状态查询竞技项目列表
	 * @param isValid 0无效 1有效
	 */
	List<ActivityServer> findByValid(Integer valid);

	/**
	 * 查找id查询竞技项目信息
	 */
	ActivityServer findById(Long id);
}