package com.miqtech.master.dao.common;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.common.AppDynamicEntry;

public interface AppDynamicEntryDao extends PagingAndSortingRepository<AppDynamicEntry, Long>,
		JpaSpecificationExecutor<AppDynamicEntry> {
	List<AppDynamicEntry> findByIsShowOrderBySortDesc(Integer isShow);
}