package com.miqtech.master.dao.activity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.activity.ActivityGroup;

/**
 * 赛事分组操作DAO
 */
public interface ActivityGroupDao extends PagingAndSortingRepository<ActivityGroup, Long>,
		JpaSpecificationExecutor<ActivityGroup> {

	ActivityGroup findByIdAndValid(long id, int valid);

	List<ActivityGroup> findByActivityIdAndNetbarIdAndRoundAndIsTeamAndValid(Integer activityId,Integer netbarId,Integer round,Integer isTeam, Integer valid);

}