package com.miqtech.master.dao.activity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.activity.ActivityTeam;

/**
 * 比赛团队信息操作DAO
 */
public interface ActivityTeamDao
		extends PagingAndSortingRepository<ActivityTeam, Long>, JpaSpecificationExecutor<ActivityTeam> {
	/**
	 * 根据成员id查找战队信息
	 * @param memId 成员id
	 */
	List<ActivityTeam> findByMemId(Long memId);

	/**
	 * 根据成员id和数据状态查找战队信息
	 * @param memId 成员id
	 * @param valid 0无效 1有效
	 */
	List<ActivityTeam> findByMemIdAndValid(Long memId, int valid);

	List<ActivityTeam> findByActivityIdAndValid(long activityId, int valid);

	List<ActivityTeam> findByActivityIdAndNetbarIdAndRoundAndValid(Long activityId, Long netbarId, Integer round,
			Integer valid);

}