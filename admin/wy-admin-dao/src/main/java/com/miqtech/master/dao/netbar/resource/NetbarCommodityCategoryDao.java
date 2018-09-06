package com.miqtech.master.dao.netbar.resource;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.resource.NetbarCommodityCategory;

public interface NetbarCommodityCategoryDao extends PagingAndSortingRepository<NetbarCommodityCategory, Long>,
		JpaSpecificationExecutor<NetbarCommodityCategory> {
	List<NetbarCommodityCategory> findByValidAndPid(int valid, long pid);

	@Query("select ca from NetbarCommodityCategory ca order by (case when pid=0 then id else pid end ),id")
	List<NetbarCommodityCategory> findAllCategory();

}