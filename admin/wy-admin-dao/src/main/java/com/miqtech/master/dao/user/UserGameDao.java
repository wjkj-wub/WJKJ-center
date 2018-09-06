package com.miqtech.master.dao.user;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.user.UserGame;

/**
 * 用户游戏资料操作DAO
 */
public interface UserGameDao extends PagingAndSortingRepository<UserGame, Long>, JpaSpecificationExecutor<UserGame> {

	/**
	 * 根据资料id和用户id查找用户的游戏资料
	 */
	UserGame findByIdAndUserId(long id, long userIdLong);

}