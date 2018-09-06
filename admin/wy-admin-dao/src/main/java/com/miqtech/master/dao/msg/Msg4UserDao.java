package com.miqtech.master.dao.msg;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.miqtech.master.entity.msg.Msg4User;

/**
 * 客户端用户信息操作DAO
 */
public interface Msg4UserDao extends PagingAndSortingRepository<Msg4User, Long>, JpaSpecificationExecutor<Msg4User> {
	/**
	 * 根据用户id和已读状态统计消息数量
	 * @param userId 用户id
	 * @param readType  阅读状态：0-未读;1-已读;
	 * @return
	 */
	@Query("select count(a.id) from Msg4User a where a.userId = :userId and a.isRead = :readType")
	int countByUserIdAndIsRead(@Param("userId") Long userId, @Param("readType") int readType);

	/**
	 * 根据用户id和已读状态统计消息数量
	 * @param userId 用户id
	 * @param readType  阅读状态：0-未读;1-已读;
	 * @param type 0系统消息 1红包消息 2会员消息 3预定消息 4支付消息 5赛事消息 6约战消息
	 */
	@Query("select count(a.id) from Msg4User a where a.userId = :userId and a.isRead = :readType and a.type=:type")
	int countByUserIdAndIsReadAndType(@Param("userId") Long userId, @Param("readType") int readType,
			@Param("type") int type);

}