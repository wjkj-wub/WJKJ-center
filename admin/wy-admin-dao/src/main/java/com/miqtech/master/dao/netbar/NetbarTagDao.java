package com.miqtech.master.dao.netbar;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarTag;

public interface NetbarTagDao extends PagingAndSortingRepository<NetbarTag, Long>, JpaSpecificationExecutor<NetbarTag> {

}