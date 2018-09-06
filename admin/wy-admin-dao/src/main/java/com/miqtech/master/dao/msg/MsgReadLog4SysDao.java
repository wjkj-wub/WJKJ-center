package com.miqtech.master.dao.msg;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.msg.MsgReadLog4Sys;

/**
 * 系统消息已读状态操作DAO
 */
public interface MsgReadLog4SysDao extends PagingAndSortingRepository<MsgReadLog4Sys, Long>,
		JpaSpecificationExecutor<MsgReadLog4Sys> {
	/**
	 * 根据用户id,系统消息id和类型查找系统消息阅读信息
	 * @param userId 用户id
	 * @param id 系统消息id
	 * @param type 系统消息类型
	 */
	MsgReadLog4Sys findByUserIdAndMsgIdAndType(Long userId, Long id, int type);
}