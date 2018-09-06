package com.miqtech.master.dao.common;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.common.IndexMidImg;

public interface IndexMidImgDao extends PagingAndSortingRepository<IndexMidImg, Long>,
		JpaSpecificationExecutor<IndexMidImg> {
	IndexMidImg findByType(Integer type);

	List<IndexMidImg> findByCategoryAndAreaCodeAndValidOrderBySortDesc(Integer category, String areaCode, int valid);

	List<IndexMidImg> findByCategoryAndAreaCodeAndValid(Integer category, String areaCode, int valid);
}
