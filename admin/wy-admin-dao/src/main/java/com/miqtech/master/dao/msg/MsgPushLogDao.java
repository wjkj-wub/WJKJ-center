package com.miqtech.master.dao.msg;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.msg.MsgPushLog;

/**
 * 推送记录操作DAO
 */
public interface MsgPushLogDao
		extends PagingAndSortingRepository<MsgPushLog, Long>, JpaSpecificationExecutor<MsgPushLog> {

}