package com.miqtech.master.dao.common;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.common.NoticeRead;

/**
 * 商户端通知操作DAO
 */
public interface NoticeReadDao
		extends PagingAndSortingRepository<NoticeRead, Long>, JpaSpecificationExecutor<NoticeRead> {

	NoticeRead findById(Long id);
}