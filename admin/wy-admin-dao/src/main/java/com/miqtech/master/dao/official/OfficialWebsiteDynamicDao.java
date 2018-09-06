package com.miqtech.master.dao.official;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.website.OfficialWebsiteDynamic;

/**
 * 官网动态新闻、精彩活动、轮播
 */
public interface OfficialWebsiteDynamicDao extends PagingAndSortingRepository<OfficialWebsiteDynamic, Long>,
		JpaSpecificationExecutor<OfficialWebsiteDynamic> {

	OfficialWebsiteDynamic findById(Long id);

}