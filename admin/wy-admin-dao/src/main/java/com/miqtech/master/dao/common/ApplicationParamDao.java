package com.miqtech.master.dao.common;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.common.ApplicationParam;

public interface ApplicationParamDao
		extends PagingAndSortingRepository<ApplicationParam, Long>, JpaSpecificationExecutor<ApplicationParam> {

	List<ApplicationParam> findByValid(Integer valid);

}