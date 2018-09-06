package com.miqtech.master.dao.comment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.comment.CommentShortcut;

/**
 * 快捷评论Dao
 */
public interface CommentShortcutDao
		extends PagingAndSortingRepository<CommentShortcut, Long>, JpaSpecificationExecutor<CommentShortcut> {

	CommentShortcut findByIdAndValid(Long id, Integer valid);

	List<CommentShortcut> findByValidOrderBySortNum(Integer valid);
}
