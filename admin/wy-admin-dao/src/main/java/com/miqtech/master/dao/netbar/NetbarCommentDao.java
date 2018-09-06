package com.miqtech.master.dao.netbar;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarComment;

/**
 * 网吧评论操作DAO
 */
public interface NetbarCommentDao extends PagingAndSortingRepository<NetbarComment, Long>,
		JpaSpecificationExecutor<NetbarComment> {
}