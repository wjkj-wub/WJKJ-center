package com.miqtech.master.dao.mall;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.mall.TurntableInfo;

/**
 * （金币）转盘操作Dao
 */
public interface TurntableInfoDao
		extends PagingAndSortingRepository<TurntableInfo, Long>, JpaSpecificationExecutor<TurntableInfo> {

}
