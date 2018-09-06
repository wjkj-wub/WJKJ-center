package com.miqtech.master.dao.thirdparty;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.thirdparty.ThirdPartyCdkey;

public interface ThirdPartyCdkeyDao extends PagingAndSortingRepository<ThirdPartyCdkey, Long>,
		JpaSpecificationExecutor<ThirdPartyCdkey> {

	public ThirdPartyCdkey findFirst1ByCategoryIdAndIsUsedAndValid(Long categoryId, Integer isUsed, Integer valid);
}
