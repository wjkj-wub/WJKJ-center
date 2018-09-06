package com.miqtech.master.dao.pc.taskmatch;

import com.miqtech.master.entity.pc.taskmatch.TaskMatchRank;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 任务赛奖项  Dao 接口
 *
 * @author gaohanlin
 * @create 2017年09月01日
 */
public interface TaskMatchRankDao
		extends JpaSpecificationExecutor<TaskMatchRank>, PagingAndSortingRepository<TaskMatchRank, Long> {
}
