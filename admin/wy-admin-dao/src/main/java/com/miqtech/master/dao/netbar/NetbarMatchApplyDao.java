package com.miqtech.master.dao.netbar;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarMatchApply;

/**
 * 网吧赛点申请操作DAO
 */
public interface NetbarMatchApplyDao extends PagingAndSortingRepository<NetbarMatchApply, Long>,
		JpaSpecificationExecutor<NetbarMatchApply> {

	/**
	 * 根据网吧id和赛事id查找网吧赛点申请记录
	 * @param netbarId 网吧id
	 * @param activityId 赛事id
	 */
	List<NetbarMatchApply> findByNetbarIdAndActivityId(Long netbarId, Long activityId);

	/**
	 * 根据网吧id和赛事id查找网吧赛点申请记录
	 * @param netbarId 网吧id
	 * @param activityId 赛事id
	 * @param valid 是否有效状态
	 */
	List<NetbarMatchApply> findByNetbarIdAndActivityIdAndValid(Long netbarId, Long activityId, int valid);
}