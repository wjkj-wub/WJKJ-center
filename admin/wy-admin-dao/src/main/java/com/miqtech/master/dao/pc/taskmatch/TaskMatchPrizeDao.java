package com.miqtech.master.dao.pc.taskmatch;

import com.miqtech.master.entity.pc.taskmatch.TaskMatchPrize;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * 任务赛奖项 Dao 接口
 *
 * @author gaohanlin
 * @create 2017年09月01日
 */
public interface TaskMatchPrizeDao
		extends JpaSpecificationExecutor<TaskMatchPrize>, PagingAndSortingRepository<TaskMatchPrize, Long> {

	@Transactional
	void deleteByTaskId(@Param("task_id") Long taskId);
}
