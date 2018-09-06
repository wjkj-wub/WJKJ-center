package com.miqtech.master.dao.amuse;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.amuse.AmuseActivityComment;

/**
 * 娱乐赛评论Dao
 */
public interface AmuseActivityCommentDao
		extends PagingAndSortingRepository<AmuseActivityComment, Long>, JpaSpecificationExecutor<AmuseActivityComment> {

	List<AmuseActivityComment> findByIdAndValid(Long id, Integer valid);
}
