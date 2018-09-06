package com.miqtech.master.dao.activity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.activity.ActivityMember;

/**
 * 赛事报名人员操作DAO
 */
public interface ActivityMemberDao extends PagingAndSortingRepository<ActivityMember, Long>,
		JpaSpecificationExecutor<ActivityMember> {

	/**
	 * 根据赛事Id,用户id,数据状态查找报名人员信息
	 * @param userId 用户id
	 * @param isValid 数据状态
	 * @param activityId 赛事id
	 */
	List<ActivityMember> findByUserIdAndValidAndActivityId(Long userId, int isValid, Long activityId);

	/**
	 * 根据赛事Id,用户id,场次,数据状态查找报名人员信息
	 * @param activityId 赛事id
	 * @param userId 用户id
	 * @param round 场次
	 * @param isValid 数据状态
	 */
	List<ActivityMember> findByActivityIdAndUserIdAndRoundAndValid(Long activityId, Long userId, int round, int isValid);

	/**
	 * 根据赛事Id,用户id,场次,数据状态和战队id查找报名人员信息
	 * @param activityId 赛事id
	 * @param userId 用户id
	 * @param round 场次
	 * @param isValid 数据状态
	 * @param teamId 战队id
	 */
	List<ActivityMember> findByActivityIdAndUserIdAndRoundAndValidAndTeamId(Long activityId, Long userId, int round,
			int valid, Long teamId);

	/**
	 * 根据赛事Id,用户id,场次,数据状态和战队id(不等于)查找报名人员信息
	 * @param activityId 赛事id
	 * @param userId 用户id
	 * @param round 场次
	 * @param isValid 数据状态
	 * @param teamId 战队id(不等于)
	 */
	List<ActivityMember> findByActivityIdAndUserIdAndRoundAndValidAndTeamIdNot(Long activityId, Long userId, int round,
			int valid, Long teamId);

	/**
	 * 查找某个战队所有有效成员信息
	 * @param teamId 战队ID
	 * @param isValid 数据状态
	 */
	List<ActivityMember> findByTeamIdAndValid(Long teamId, int isValid);

	/**
	 * 查找某个战队单一成员有效信息
	 * @param teamId 战队id(不等于)
	 * @param userId 用户id
	 * @param isValid 数据状态
	 */
	List<ActivityMember> findByTeamIdAndUserIdAndValid(Long teamId, Long userId, int isValid);

	/**
	 * 根据战队id和是否是队长标识查找有效的团队报名信息
	 * @param teamId 战队ID
	 * @param isMonitor 是否是队长
	 * @param isValid 有效状态
	 */
	ActivityMember findByTeamIdAndIsMonitorAndValid(Long teamId, int isMonitor, int isValid);

	ActivityMember findByTeamIdAndUserIdAndValidAndIsAccept(Long teamId, Long userId, int isValid, int isAccept);

}