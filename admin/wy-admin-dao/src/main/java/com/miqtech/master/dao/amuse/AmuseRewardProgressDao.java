package com.miqtech.master.dao.amuse;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.amuse.AmuseRewardProgress;

public interface AmuseRewardProgressDao extends PagingAndSortingRepository<AmuseRewardProgress, Long>,
		JpaSpecificationExecutor<AmuseRewardProgress> {

	AmuseRewardProgress findByIdAndValid(Long id, Integer valid);

	List<AmuseRewardProgress> findByTargetIdAndTypeAndStateAndValid(Long targetId, int type, int state, int valid);

	List<AmuseRewardProgress> findByActivityIdAndUserIdAndStateAndValid(Long activityId, Long userId, int state,
			int valid);
}
