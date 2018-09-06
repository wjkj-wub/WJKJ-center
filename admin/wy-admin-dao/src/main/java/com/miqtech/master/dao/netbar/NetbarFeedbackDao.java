package com.miqtech.master.dao.netbar;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarFeedback;

public interface NetbarFeedbackDao extends PagingAndSortingRepository<NetbarFeedback, Long>,
		JpaSpecificationExecutor<NetbarFeedback> {

}
