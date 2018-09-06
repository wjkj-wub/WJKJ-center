package com.miqtech.master.dao.amuse;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.amuse.AmuseAppeal;

/**
 * 娱乐赛申诉Dao
 */
public interface AmuseAppealDao extends PagingAndSortingRepository<AmuseAppeal, Long>,
		JpaSpecificationExecutor<AmuseAppeal> {

	List<AmuseAppeal> findByActivityIdAndUserIdAndValid(Long activityId, Long userId, Integer valid);

	List<AmuseAppeal> findByActivityIdAndUserIdAndValidOrderByCreateDateAsc(Long activityId, Long userId, Integer valid);

	List<AmuseAppeal> findByActivityIdAndUserIdAndValidAndCategoryIdIsNull(Long activityId, Long userId, Integer valid);

	AmuseAppeal findByActivityIdAndUserIdAndValidAndCategoryIdIsNotNull(Long activityId, Long userId, Integer valid);

	List<AmuseAppeal> findByIdInAndValid(List<Long> ids, Integer valid);
}
