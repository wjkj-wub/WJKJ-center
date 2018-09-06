package com.miqtech.master.dao.amuse;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.amuse.AmuseActivityInfo;
import com.miqtech.master.entity.mall.CommodityIcon;

/**
 * 娱乐赛Icon操作Dao
 */
public interface AmuseActivityInfoDao extends PagingAndSortingRepository<AmuseActivityInfo, Long>,
		JpaSpecificationExecutor<CommodityIcon> {

	AmuseActivityInfo findByIdAndValid(Long id, Integer valid);

	List<AmuseActivityInfo> findByValid(Integer valid);

	List<AmuseActivityInfo> findByIdInAndValid(List<Long> ids, Integer valid);

}
