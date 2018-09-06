package com.miqtech.master.dao.common;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.common.SystemUser;

/**
 * 系统用户信息操作DAO
 */
public interface SystemUserDao extends PagingAndSortingRepository<SystemUser, Long>,
		JpaSpecificationExecutor<SystemUser> {

	/**
	 * 根据用户名和密码查找用户信息
	 * @param name
	 * @param password
	 */
	List<SystemUser> findByUsernameAndPasswordAndUserTypeIn(String username, String password, List<Integer> userTypes);

	/**
	 * 根据用户名查找用户信息
	 */
	List<SystemUser> findByUsername(String username);

	/**
	 * 根据用户名类别查找用户信息
	 */
	SystemUser findByUsernameAndUserType(String username, Integer type);

	/**
	 * 根据用户名类别查找用户信息
	 */
	SystemUser findByUsernameAndPasswordAndUserTypeLessThan(String username, String password, int max);

	/**
	 * 根据用户名,密码,用户类型查找用户信息
	 * @param username
	 * @param password
	 * @param userType 0超级管理员 1普通管理员  2表示录入系统中的管理员  3表示录入系统的审核员 4表示录入系统的录入人员
	 * @param valid 0有效 1无效
	 */
	SystemUser findByUsernameAndPasswordAndUserTypeAndValid(String username, String password, Integer userType,
			Integer valid);

	List<SystemUser> findByParentIdAndValid(Long parentId, int valid);

	SystemUser findByUserTypeAndParentIdIsNull(Integer userType);

	SystemUser findByUsernameAndPasswordAndValidAndUserTypeLessThan(String name, String password, int valid, int max);

	List<SystemUser> findByUserTypeAndValid(Integer userType, Integer valid);

	List<SystemUser> findByUserTypeInAndValid(List<Integer> userTypes, Integer valids);

}