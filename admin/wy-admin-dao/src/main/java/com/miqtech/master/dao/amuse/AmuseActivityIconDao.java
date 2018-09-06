package com.miqtech.master.dao.amuse;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.amuse.AmuseActivityIcon;

/**
 * 娱乐赛信息操作Dao
 */
public interface AmuseActivityIconDao
		extends PagingAndSortingRepository<AmuseActivityIcon, Long>, JpaSpecificationExecutor<AmuseActivityIcon> {

	List<AmuseActivityIcon> findByIdAndValid(Long id, Integer valid);
}
