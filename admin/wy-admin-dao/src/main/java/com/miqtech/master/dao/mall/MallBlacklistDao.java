package com.miqtech.master.dao.mall;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.mall.MallBlacklist;

/**
 * 商城黑名单
 */
public interface MallBlacklistDao extends PagingAndSortingRepository<MallBlacklist, Long>,
		JpaSpecificationExecutor<MallBlacklist> {
}
