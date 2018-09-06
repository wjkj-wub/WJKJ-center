package com.miqtech.master.dao.game;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.game.GameComment;

/**
 * 游戏评论操作DAO
 */
public interface GameCommentDao extends PagingAndSortingRepository<GameComment, Long>,
		JpaSpecificationExecutor<GameComment> {
}