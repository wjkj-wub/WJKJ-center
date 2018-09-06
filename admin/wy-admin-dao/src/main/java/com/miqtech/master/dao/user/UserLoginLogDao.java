package com.miqtech.master.dao.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.user.UserLoginLog;

/**
 * 用户登录日志操作DAO
 */
public interface UserLoginLogDao extends PagingAndSortingRepository<UserLoginLog, Long>,
		JpaSpecificationExecutor<UserLoginLog> {

	/**
	 * 根据用户id查找用户登录日志
	 */
	List<UserLoginLog> findByUserId(Long userId);

}