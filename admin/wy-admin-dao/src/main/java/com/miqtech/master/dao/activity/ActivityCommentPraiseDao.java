package com.miqtech.master.dao.activity;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.activity.ActivityCommentPraise;

public interface ActivityCommentPraiseDao extends PagingAndSortingRepository<ActivityCommentPraise, Long>,
		JpaSpecificationExecutor<ActivityCommentPraise> {
	ActivityCommentPraise findByUserIdAndCommentId(Long userId, Long commentId);
}
