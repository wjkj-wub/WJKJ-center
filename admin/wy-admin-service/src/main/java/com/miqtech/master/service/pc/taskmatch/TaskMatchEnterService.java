package com.miqtech.master.service.pc.taskmatch;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.pc.taskmatch.TaskMatchEnterDao;
import com.miqtech.master.entity.pc.taskmatch.TaskMatchEnter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author shilina
 * @create 2017年09月29日
 */
@Service
public class TaskMatchEnterService {
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private TaskMatchEnterDao taskMatchEnterDao;

	public List<TaskMatchEnter> findByTaskIdAndTypeAndStatus(Long taskId, Integer taskType, Byte status) {
		String sql = "select * from pc_task_match_enter where task_id=" + taskId + " and task_type=" + taskType
				+ " and status=" + status;
		return queryDao.queryObject(sql, TaskMatchEnter.class);
	}

	public void save(List<TaskMatchEnter> list) {
		taskMatchEnterDao.save(list);
	}

}
