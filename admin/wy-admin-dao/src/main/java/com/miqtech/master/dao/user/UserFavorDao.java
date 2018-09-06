package com.miqtech.master.dao.user;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.user.UserFavor;

/**
 * 用户收藏操作DAO
 */
public interface UserFavorDao extends PagingAndSortingRepository<UserFavor, Long>, JpaSpecificationExecutor<UserFavor> {

	/**
	 * 根据用户id,收藏对象类型和收藏对象id查找用户收藏数据
	 * @param userId 用户id
	 * @param objId 对象id
	 * @param type 类型 收藏的类别:1-网吧;2-游戏;3-赛事
	 */
	UserFavor findByUserIdAndSubIdAndType(Long userId, Long objId, int type);

	UserFavor findByUserIdAndSubIdAndTypeAndValid(long userId, long subId, int type, int valid);
}