package com.miqtech.master.dao.pc.taskmatch;

import com.miqtech.master.entity.pc.taskmatch.TaskMatchRankRecord;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 赛事排行榜记录  Dao 接口
 *
 * @author gaohanlin
 * @create 2017年09月01日
 */
public interface TaskMatchRankRecordDao
		extends JpaSpecificationExecutor<TaskMatchRankRecord>, PagingAndSortingRepository<TaskMatchRankRecord, Long> {
}
