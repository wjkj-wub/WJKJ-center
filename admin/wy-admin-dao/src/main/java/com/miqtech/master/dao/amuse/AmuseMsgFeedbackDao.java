package com.miqtech.master.dao.amuse;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.amuse.AmuseMsgFeedback;

/**
 * 娱乐赛反馈消息模版
 */
public interface AmuseMsgFeedbackDao extends PagingAndSortingRepository<AmuseMsgFeedback, Long>,
		JpaSpecificationExecutor<AmuseMsgFeedback> {

	List<AmuseMsgFeedback> findByTypeAndValid(Integer type, Integer valid);
}
