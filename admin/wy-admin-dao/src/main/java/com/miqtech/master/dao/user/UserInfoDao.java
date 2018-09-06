package com.miqtech.master.dao.user;

import com.miqtech.master.entity.user.UserInfo;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * 用户信息操作DAO
 */
public interface UserInfoDao extends PagingAndSortingRepository<UserInfo, Long>, JpaSpecificationExecutor<UserInfo> {

	/**
	 * 根据用户名密码查找用户信息
	 */
	UserInfo findByUsernameAndPasswordAndValid(String phone, String password, int valid);

	/**
	 * 根据用户名查找用户信息
	 */
	UserInfo findByUsernameAndValid(String phone, int valid);

	/**
	 * 根据手机号查找用户信息
	 */
	UserInfo findByTelephoneAndValid(String phone, int valid);

	/**
	 * 根据昵称查找用户信息
	 */
	List<UserInfo> findByNickname(String nickname);

	/**
	 * 根据用户名查找
	 */
	UserInfo findByUsername(String phone);

	List<UserInfo> findByValidAndUsernameIn(int valid, String[] phoneNumArray);

	List<UserInfo> findByIdInAndValid(List<Long> ids, Integer valid);

	List<UserInfo> findByIdIn(List<Long> ids);

	UserInfo findByIdAndValid(Long id, int valid);
}