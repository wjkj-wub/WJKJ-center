package com.miqtech.master.dao.pc.taskmatch;

import com.miqtech.master.entity.pc.taskmatch.TaskMatchCondition;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 主题任务赛 Dao 接口
 *
 * @author shilina
 * @create 2017年09月02日
 */
public interface TaskMatchConditionDao
		extends JpaSpecificationExecutor<TaskMatchCondition>, PagingAndSortingRepository<TaskMatchCondition, Long> {
}
