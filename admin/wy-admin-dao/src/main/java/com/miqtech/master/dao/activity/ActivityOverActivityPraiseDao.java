package com.miqtech.master.dao.activity;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.activity.ActivityOverActivityPraise;

/**
 *  赛事资讯用户点赞记录操作DAO
 */
public interface ActivityOverActivityPraiseDao extends PagingAndSortingRepository<ActivityOverActivityPraise, Long>,
		JpaSpecificationExecutor<ActivityOverActivityPraise> {

	ActivityOverActivityPraise findByActivityOverActivityIdAndUserId(long infoId, long userId);

}