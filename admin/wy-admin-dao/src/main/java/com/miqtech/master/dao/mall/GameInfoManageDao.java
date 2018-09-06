package com.miqtech.master.dao.mall;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.mall.MallGameInfo;

public interface GameInfoManageDao
		extends PagingAndSortingRepository<MallGameInfo, Long>, JpaSpecificationExecutor<MallGameInfo> {
	
	public MallGameInfo findByIdAndValid(Long id , Integer valid);
}
