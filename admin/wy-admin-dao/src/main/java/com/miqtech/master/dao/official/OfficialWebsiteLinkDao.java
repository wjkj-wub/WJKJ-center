package com.miqtech.master.dao.official;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.website.OfficialWebsiteLink;

/**
 * 官网视频、游戏链接
 */
public interface OfficialWebsiteLinkDao
		extends PagingAndSortingRepository<OfficialWebsiteLink, Long>, JpaSpecificationExecutor<OfficialWebsiteLink> {

	OfficialWebsiteLink findById(Long id);

}