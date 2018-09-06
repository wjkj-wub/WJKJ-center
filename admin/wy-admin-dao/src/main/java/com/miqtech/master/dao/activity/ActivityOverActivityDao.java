package com.miqtech.master.dao.activity;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.activity.ActivityOverActivity;

/**
 * 赛事资讯操作DAO
 */
public interface ActivityOverActivityDao extends PagingAndSortingRepository<ActivityOverActivity, Long>,
		JpaSpecificationExecutor<ActivityOverActivity> {
	
	List<ActivityOverActivity> findByTimerDateAndValid(Date now, int valid);

	List<ActivityOverActivity> findByIdIn(List<Long> ids);
}