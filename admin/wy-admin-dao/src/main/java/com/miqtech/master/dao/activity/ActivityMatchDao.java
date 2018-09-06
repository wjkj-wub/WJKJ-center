package com.miqtech.master.dao.activity;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.activity.ActivityMatch;

/**
 * 约战信息操作DAO
 */
public interface ActivityMatchDao extends PagingAndSortingRepository<ActivityMatch, Long>,
		JpaSpecificationExecutor<ActivityMatch> {

	/**
	 * 查找指定id的约战信息
	 */
	ActivityMatch findById(Long id);

	/**
	 * 查找有效并指定状态的约战信息
	 * @param startStatus 约战状态 是否开始：1-已开始;0-未开始;
	 * @param valid 数据状态
	 */
	Iterable<ActivityMatch> findByIsStartAndValid(int startStatus, int valid);
}