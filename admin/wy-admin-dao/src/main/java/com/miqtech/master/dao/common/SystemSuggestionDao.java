package com.miqtech.master.dao.common;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.common.SystemSuggestion;

/**
 * 用户反馈操作DAO
 */
public interface SystemSuggestionDao extends PagingAndSortingRepository<SystemSuggestion, Long>,
		JpaSpecificationExecutor<SystemSuggestion> {

}