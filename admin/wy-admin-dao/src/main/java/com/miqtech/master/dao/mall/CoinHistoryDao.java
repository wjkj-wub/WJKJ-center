package com.miqtech.master.dao.mall;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.mall.CoinHistory;

/**
 * （金币）收入支出历史记录Dao
 */
public interface CoinHistoryDao extends JpaSpecificationExecutor<CoinHistory>,
		PagingAndSortingRepository<CoinHistory, Long> {

	List<CoinHistory> findByUserIdInAndValid(List<Long> userIds, Integer valid);
}
