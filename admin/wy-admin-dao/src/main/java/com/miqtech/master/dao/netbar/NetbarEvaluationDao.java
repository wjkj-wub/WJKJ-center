package com.miqtech.master.dao.netbar;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarEvaluation;

public interface NetbarEvaluationDao extends PagingAndSortingRepository<NetbarEvaluation, Long>,
		JpaSpecificationExecutor<NetbarEvaluation> {

	NetbarEvaluation findByOrderIdAndUserIdAndValid(long orderId, Long userId, int valid);

}