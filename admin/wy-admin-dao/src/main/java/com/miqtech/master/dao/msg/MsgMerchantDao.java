package com.miqtech.master.dao.msg;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.msg.MsgMerchant;

/**
 * 商户端消息操作DAO
 */
public interface MsgMerchantDao extends PagingAndSortingRepository<MsgMerchant, Long>,
		JpaSpecificationExecutor<MsgMerchant> {

}