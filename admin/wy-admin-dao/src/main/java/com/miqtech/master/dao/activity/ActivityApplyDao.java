package com.miqtech.master.dao.activity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.activity.ActivityApply;

/**
 * 赛事报名DAO
 */
public interface ActivityApplyDao
		extends PagingAndSortingRepository<ActivityApply, Long>, JpaSpecificationExecutor<ActivityApply> {

	/**
	 * 根据赛事,场次,报名类型和报名对象id查找报名记录
	 * @param activityId 赛事ID
	 * @param targetId 战队或个人ID
	 * @param type 类型：1-个人；2-战队
	 * @param round 场次
	 * @param valid 数据状态
	 */
	List<ActivityApply> findByActivityIdAndTargetIdAndTypeAndRoundAndValid(Long activityId, Long targetId, int type,
			int round, int valid);

	/**
	 * 根据场次和战队id查找报名记录
	 * @param targetId 战队或个人ID
	 * @param type 类型：1-个人；2-战队
	 * @param round 场次
	 * @param valid 数据状态
	 */
	ActivityApply findByTargetIdAndTypeAndRoundAndValid(Long targetId, int type, Integer round, int valid);

	List<ActivityApply> findByTargetIdAndTypeAndValid(Long targetId, Integer type, Integer valid);

}