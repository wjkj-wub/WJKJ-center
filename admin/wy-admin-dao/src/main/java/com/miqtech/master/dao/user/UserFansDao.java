package com.miqtech.master.dao.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.user.UserFans;

/**
 * 用户粉丝操作DAO
 */
public interface UserFansDao extends PagingAndSortingRepository<UserFans, Long>, JpaSpecificationExecutor<UserFans> {

	/**
	 * 根据用户id查找某个用户的粉丝
	 */
	List<UserFans> findByUserId(Long userId);

}