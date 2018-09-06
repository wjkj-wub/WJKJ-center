package com.miqtech.master.dao.activity;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.activity.ActivityMatchComment;

/**
 * 约战评论操作DAO
 */
public interface ActivityMatchCommentDao extends PagingAndSortingRepository<ActivityMatchComment, Long>,
		JpaSpecificationExecutor<ActivityMatchComment> {
}