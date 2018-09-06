package com.miqtech.master.dao.common;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.common.SystemMerchantNotice;

/**
 * 商户端通知操作DAO
 */
public interface SystemMerchantNoticeDao extends PagingAndSortingRepository<SystemMerchantNotice, Long>,
		JpaSpecificationExecutor<SystemMerchantNotice> {
	/**
	 * 根据id查找系统通知
	 * @param id 通知id
	 */
	SystemMerchantNotice findById(Long id);

	/**
	 * 查找五条系统通知并按时间倒序
	 */
	@Query(value = "select * from sys_t_merchant_notice  order by create_date limit 0,5", nativeQuery = true)
	List<SystemMerchantNotice> findNewestFiveNotice();
}