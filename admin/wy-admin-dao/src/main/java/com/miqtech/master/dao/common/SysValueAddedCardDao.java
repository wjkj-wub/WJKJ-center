package com.miqtech.master.dao.common;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.common.SysValueAddedCard;

public interface SysValueAddedCardDao
		extends PagingAndSortingRepository<SysValueAddedCard, Long>, JpaSpecificationExecutor<SysValueAddedCard> {

}
