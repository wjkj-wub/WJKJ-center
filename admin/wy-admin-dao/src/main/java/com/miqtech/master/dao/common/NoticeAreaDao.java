package com.miqtech.master.dao.common;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.common.NoticeArea;

/**
 * 商户端通知操作DAO
 */
public interface NoticeAreaDao
		extends PagingAndSortingRepository<NoticeArea, Long>, JpaSpecificationExecutor<NoticeArea> {

	/**
	 * 根据公告Id和areaCode查询有效记录
	 */
	NoticeArea findByNoticeIdAndAreaCode(Long noticeId, String areaCode);

}