package com.miqtech.master.service.netbar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.netbar.NetbarEvaluationPraiseDao;
import com.miqtech.master.entity.netbar.NetbarEvaluationPraise;

@Component
public class NetbarEvaluationPraiseService {
	@Autowired
	private NetbarEvaluationPraiseDao netbarEvaluationPraiseDao;

	public boolean isExist(long evaId, Long userId) {
		NetbarEvaluationPraise evaPraise = netbarEvaluationPraiseDao.findByEvaIdAndUserIdAndValid(evaId, userId, 1);
		return evaPraise != null;
	}
}
