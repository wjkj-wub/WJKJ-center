package com.miqtech.master.dao.netbar;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarEvaluationPraise;

public interface NetbarEvaluationPraiseDao extends PagingAndSortingRepository<NetbarEvaluationPraise, Long>,
		JpaSpecificationExecutor<NetbarEvaluationPraise> {

	NetbarEvaluationPraise findByEvaIdAndUserIdAndValid(long evaId, Long userId, int valid);

}