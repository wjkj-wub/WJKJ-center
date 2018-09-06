package com.miqtech.master.dao.user;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.user.UserRechargePrize;

/**
 * 用户相册操作DAO
 */
public interface UserRechargePrizeDao extends PagingAndSortingRepository<UserRechargePrize, Long>,
		JpaSpecificationExecutor<UserRechargePrize> {

}