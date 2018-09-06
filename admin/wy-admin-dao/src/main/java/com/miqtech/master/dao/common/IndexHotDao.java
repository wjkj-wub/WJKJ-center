package com.miqtech.master.dao.common;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.common.IndexHot;

public interface IndexHotDao extends PagingAndSortingRepository<IndexHot, Long>, JpaSpecificationExecutor<IndexHot> {
	List<IndexHot> findByTypeAndAreaCodeAndValid(Integer type, String areaCode, int valid);

}
