package com.miqtech.master.dao.uwan;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.uwan.UwanNetbar;

/**
 * 网吧pc客户端广告
 */
public interface UwanNetbarDao
		extends PagingAndSortingRepository<UwanNetbar, Long>, JpaSpecificationExecutor<UwanNetbar> {

	List<UwanNetbar> findBySource(Integer source);

}