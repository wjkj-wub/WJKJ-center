package com.miqtech.master.dao.activity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.activity.ActivityCard;

/**
 * 赛事信息操作DAO
 */
public interface ActivityCardDao extends PagingAndSortingRepository<ActivityCard, Long>,
		JpaSpecificationExecutor<ActivityCard> {

	public ActivityCard findByIdAndValid(Long id, Integer valid);

	public List<ActivityCard> findByUserIdAndValid(Long userId, Integer valid);
}