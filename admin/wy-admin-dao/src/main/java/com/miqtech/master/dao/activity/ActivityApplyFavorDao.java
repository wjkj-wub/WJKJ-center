package com.miqtech.master.dao.activity;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.activity.ActivityApplyFavor;

/**
 * 赛事点赞记录操作DAO
 */
public interface ActivityApplyFavorDao extends PagingAndSortingRepository<ActivityApplyFavor, Long>,
		JpaSpecificationExecutor<ActivityApplyFavor> {

}