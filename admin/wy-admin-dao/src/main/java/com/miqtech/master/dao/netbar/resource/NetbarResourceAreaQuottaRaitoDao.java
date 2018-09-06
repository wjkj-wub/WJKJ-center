package com.miqtech.master.dao.netbar.resource;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.resource.NetbarResourceAreaQuottaRaito;

public interface NetbarResourceAreaQuottaRaitoDao extends
		PagingAndSortingRepository<NetbarResourceAreaQuottaRaito, Long>,
		JpaSpecificationExecutor<NetbarResourceAreaQuottaRaito> {

}