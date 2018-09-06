package com.miqtech.master.dao.mall;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.mall.TurntableRule;

/**
 * 转盘商品操作Dao
 */
public interface TurntableRuleDao extends JpaSpecificationExecutor<TurntableRule>,
		PagingAndSortingRepository<TurntableRule, Long> {

}
