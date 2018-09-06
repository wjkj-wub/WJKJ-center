package com.miqtech.master.dao.pc.taskmatch;

import com.miqtech.master.entity.pc.taskmatch.TaskMatchRankLabel;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * 排行榜任务赛-标签  Dao
 *
 * @author gaohanlin
 * @create 2017年09月01日
 */
public interface TaskMatchRankLabelDao
		extends JpaSpecificationExecutor<TaskMatchRankLabel>, PagingAndSortingRepository<TaskMatchRankLabel, Long> {

	@Transactional
	void deleteByTaskId(@Param("task_id") Long taskId);
}
