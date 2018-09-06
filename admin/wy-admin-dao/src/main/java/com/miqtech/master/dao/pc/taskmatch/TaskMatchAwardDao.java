package com.miqtech.master.dao.pc.taskmatch;

import com.miqtech.master.entity.pc.taskmatch.TaskMatchAward;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 任务赛Award Dao 接口
 *
 * @author gaohanlin
 * @create 2017年09月01日
 */
public interface TaskMatchAwardDao
		extends JpaSpecificationExecutor<TaskMatchAward>, PagingAndSortingRepository<TaskMatchAward, Long> {
}
