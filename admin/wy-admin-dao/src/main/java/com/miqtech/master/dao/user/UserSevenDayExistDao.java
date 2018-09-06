package com.miqtech.master.dao.user;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.user.UserSevenDayExist;

public interface UserSevenDayExistDao
		extends PagingAndSortingRepository<UserSevenDayExist, Long>, JpaSpecificationExecutor<UserSevenDayExist> {
	UserSevenDayExist findByUserId(Long userId);

}