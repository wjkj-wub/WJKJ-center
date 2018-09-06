package com.miqtech.master.dao.amuse;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.amuse.AmuseAppealImg;

/**
 * 娱乐赛申诉图片Dao
 */
public interface AmuseAppealImgDao extends PagingAndSortingRepository<AmuseAppealImg, Long>,
		JpaSpecificationExecutor<AmuseAppealImg> {

	List<AmuseAppealImg> findByAppealId(Long appealId);
}
