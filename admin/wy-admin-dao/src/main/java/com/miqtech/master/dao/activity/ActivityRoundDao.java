package com.miqtech.master.dao.activity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.activity.ActivityRound;

/**
 * 赛事信息操作DAO
 */
public interface ActivityRoundDao
		extends PagingAndSortingRepository<ActivityRound, Long>, JpaSpecificationExecutor<ActivityRound> {

	ActivityRound findByIdAndValid(long id, int valid);

	List<ActivityRound> findByActivityIdAndValid(long activityId, int valid);

	List<ActivityRound> findByActivityIdAndRoundAndValid(long activityId, int round, int valid);

}