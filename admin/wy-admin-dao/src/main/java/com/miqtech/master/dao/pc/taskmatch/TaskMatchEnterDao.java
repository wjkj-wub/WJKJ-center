package com.miqtech.master.dao.pc.taskmatch;

import com.miqtech.master.entity.pc.taskmatch.TaskMatchEnter;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 任务赛报名  Dao 接口
 *
 * @author gaohanlin
 * @create 2017年09月01日
 */
public interface TaskMatchEnterDao
		extends JpaSpecificationExecutor<TaskMatchEnter>, PagingAndSortingRepository<TaskMatchEnter, Long> {
}
