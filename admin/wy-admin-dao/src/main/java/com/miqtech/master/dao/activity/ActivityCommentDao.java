package com.miqtech.master.dao.activity;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.activity.ActivityComment;

/**
 * 赛事评论操作DAO
 */
public interface ActivityCommentDao extends PagingAndSortingRepository<ActivityComment, Long>,
		JpaSpecificationExecutor<ActivityComment> {

}