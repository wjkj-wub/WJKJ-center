package com.miqtech.master.dao.user;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.user.ThirdPartyLogin;

public interface ThirdPartyLoginDao extends PagingAndSortingRepository<ThirdPartyLogin, Long>,
		JpaSpecificationExecutor<ThirdPartyLogin> {

}