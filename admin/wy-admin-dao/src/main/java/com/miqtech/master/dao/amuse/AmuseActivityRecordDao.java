package com.miqtech.master.dao.amuse;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.amuse.AmuseActivityRecord;
import com.miqtech.master.entity.mall.CommodityIcon;

/**
 * 娱乐赛报名记录操作Dao
 */
public interface AmuseActivityRecordDao extends PagingAndSortingRepository<AmuseActivityRecord, Long>,
		JpaSpecificationExecutor<CommodityIcon> {

	List<AmuseActivityRecord> findByIdAndValid(Long id, Integer valid);

	List<AmuseActivityRecord> findByActivityIdAndUserIdAndValid(Long activityId, Long userId, Integer valid);

	List<AmuseActivityRecord> findByActivityIdAndUserIdAndStateAndValid(Long activityId, Long userId, Integer state,
			Integer valid);

	List<AmuseActivityRecord> findByActivityIdInAndUserIdInAndStateAndValid(List<Long> activityIds, List<Long> userIds,
			Integer state, Integer valid);

	List<AmuseActivityRecord> findByUserIdInAndValid(List<Long> userIds, Integer valid);
}
