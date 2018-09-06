package com.miqtech.master.dao.thirdparty;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.thirdparty.ThirdPartyCdkeyCategory;

public interface ThirdPartyCdkeyCategoryDao extends PagingAndSortingRepository<ThirdPartyCdkeyCategory, Long>,
		JpaSpecificationExecutor<ThirdPartyCdkeyCategory> {

	List<ThirdPartyCdkeyCategory> findByNameAndValid(String name, Integer valid);

}
