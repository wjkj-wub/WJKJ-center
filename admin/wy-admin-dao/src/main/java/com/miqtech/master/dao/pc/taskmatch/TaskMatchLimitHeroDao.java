package com.miqtech.master.dao.pc.taskmatch;

import com.miqtech.master.entity.pc.taskmatch.TaskMatchLimitHero;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * 任务赛-LOL英雄限制 Dao
 *
 * @author gaohanlin
 * @create 2017年09月01日
 */
public interface TaskMatchLimitHeroDao
		extends JpaSpecificationExecutor<TaskMatchLimitHero>, PagingAndSortingRepository<TaskMatchLimitHero, Long> {

	@Transactional
	void deleteByTaskId(@Param("task_id") Long taskId);
}
