package com.miqtech.master.dao.activity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.activity.ActivityMatchApply;

/**
 * 约战报名操作DAO
 */
public interface ActivityMatchApplyDao
		extends PagingAndSortingRepository<ActivityMatchApply, Long>, JpaSpecificationExecutor<ActivityMatchApply> {

	/**
	 * 查找某个约战信息的报名列表
	 */
	List<ActivityMatchApply> findAllAppliersByMatchId(long id);

	/**
	 * 查找已经参加约战信息
	 */
	ActivityMatchApply findByMatchIdAndUserIdAndValid(Long matchId, long idLong, int i);

}