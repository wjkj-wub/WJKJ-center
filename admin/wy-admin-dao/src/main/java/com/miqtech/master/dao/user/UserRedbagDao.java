package com.miqtech.master.dao.user;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.user.UserRedbag;

/**
 * 用户红包操作DAO
 */
public interface UserRedbagDao extends PagingAndSortingRepository<UserRedbag, Long>,
		JpaSpecificationExecutor<UserRedbag> {

	/**
	 * 根据系统红包id和用户id查找某个用户获取的红包(包含无效或者过期红包)
	 */
	List<UserRedbag> findByRedbagIdAndUserId(Long sysRedBagId, Long userId);

	/**
	 * 根据系统红包id和用户id查找某个用户获取的红包(只包含有效红包)
	 */
	List<UserRedbag> findByRedbagIdAndUserIdAndValidAndCreateDateGreaterThan(Long sysRedBagId, Long userId,
			Integer valid, Date beginTime);

	/**
	 * 根据用户ID和有效性查询红包
	 */
	List<UserRedbag> findByUserIdInAndValid(List<Long> userIds, Integer valid);

	/**
	 * 根据ID查询
	 */
	List<UserRedbag> findByIdIn(List<Long> ids);
}