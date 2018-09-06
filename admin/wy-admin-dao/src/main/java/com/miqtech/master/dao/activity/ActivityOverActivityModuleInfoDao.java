package com.miqtech.master.dao.activity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.activity.ActivityOverActivityModuleInfo;

public interface ActivityOverActivityModuleInfoDao extends
		PagingAndSortingRepository<ActivityOverActivityModuleInfo, Long>,
		JpaSpecificationExecutor<ActivityOverActivityModuleInfo> {

	List<ActivityOverActivityModuleInfo> findByOverActivityId(Long overActivityId);

}
