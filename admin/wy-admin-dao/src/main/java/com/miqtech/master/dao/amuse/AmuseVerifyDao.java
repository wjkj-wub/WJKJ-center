package com.miqtech.master.dao.amuse;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.amuse.AmuseVerify;

/**
 * 娱乐赛审核Dao
 */
public interface AmuseVerifyDao extends PagingAndSortingRepository<AmuseVerify, Long>,
		JpaSpecificationExecutor<AmuseVerify> {

	List<AmuseVerify> findByActivityIdAndUserIdAndValid(Long activityId, Long userId, Integer valid);

	AmuseVerify findByIdAndValid(Long id, Integer valid);

	List<AmuseVerify> findByIdInAndValid(List<Long> ids, Integer valid);

	List<AmuseVerify> findByActivityIdInAndUserIdInAndStateAndValid(List<Long> activityIds, List<Long> userIds,
			Integer state, Integer valid);

	List<AmuseVerify> findByUserIdInAndValid(List<Long> userIds, Integer valid);
}
