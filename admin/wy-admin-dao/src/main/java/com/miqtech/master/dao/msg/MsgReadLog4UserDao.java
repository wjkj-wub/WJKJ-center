package com.miqtech.master.dao.msg;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.msg.MsgReadLog4Sys;

/**
 * 个人消息已读状态操作DAO
 */
public interface MsgReadLog4UserDao extends PagingAndSortingRepository<MsgReadLog4Sys, Long>,
		JpaSpecificationExecutor<MsgReadLog4Sys> {
}