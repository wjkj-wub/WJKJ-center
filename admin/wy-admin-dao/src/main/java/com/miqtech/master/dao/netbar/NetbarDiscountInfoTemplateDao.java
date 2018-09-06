package com.miqtech.master.dao.netbar;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarDiscountInfoTemplate;

/**
 * 网吧优惠信息模板操作DAO
 */
public interface NetbarDiscountInfoTemplateDao extends PagingAndSortingRepository<NetbarDiscountInfoTemplate, Long>,
		JpaSpecificationExecutor<NetbarDiscountInfoTemplate> {
}