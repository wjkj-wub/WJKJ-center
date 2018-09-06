package com.miqtech.master.dao.mall;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.mall.UserRecInfo;

/**
 * 用户收货信息DAO
 */
public interface UserRecInfoDao extends PagingAndSortingRepository<UserRecInfo, Long>,
		JpaSpecificationExecutor<UserRecInfo> {

	List<UserRecInfo> findByUserIdAndValid(Long userId, Integer valid);

	UserRecInfo findByUserIdAndHistoryIdAndValid(Long userId, Long historyId, int valid);

	UserRecInfo findByUserIdAndHistoryIdIsNullAndValid(Long userId, int valid);

}
