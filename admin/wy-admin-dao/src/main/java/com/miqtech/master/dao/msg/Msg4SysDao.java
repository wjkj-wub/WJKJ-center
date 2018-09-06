package com.miqtech.master.dao.msg;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.msg.Msg4Sys;

/**
 * 系统消息(商户端和客户端)操作DAO
 */
public interface Msg4SysDao extends PagingAndSortingRepository<Msg4Sys, Long>, JpaSpecificationExecutor<Msg4Sys> {

}