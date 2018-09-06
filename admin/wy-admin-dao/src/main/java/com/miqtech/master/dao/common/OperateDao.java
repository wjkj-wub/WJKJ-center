package com.miqtech.master.dao.common;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.common.Operate;

/**
 * 访问权限数据操作DAO
 */
public interface OperateDao extends PagingAndSortingRepository<Operate, Long>, JpaSpecificationExecutor<Operate> {
}