package com.miqtech.master.dao.amuse;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.amuse.AmuseAwardType;

/**
 * 娱乐赛奖品类型Dao
 */
public interface AmuseAwardTypeDao extends PagingAndSortingRepository<AmuseAwardType, Long>,
		JpaSpecificationExecutor<AmuseAwardType> {

	List<AmuseAwardType> findByIdAndValid(Long id, Integer valid);

}
