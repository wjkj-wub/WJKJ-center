package com.miqtech.master.service.pc.taskmatch;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.pc.taskmatch.TaskMatchConditionDao;
import com.miqtech.master.entity.pc.taskmatch.TaskMatchCondition;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 主题任务赛 Service 接口
 *
 * @author shilina
 * @create 2017年09月02日
 */
@Service
public class RuleConditionService {
	@Autowired
	private TaskMatchConditionDao taskMatchConditionDao;
	@Autowired
	private QueryDao queryDao;

	public void save(List<TaskMatchCondition> list) {
		taskMatchConditionDao.save(list);
	}

	public TaskMatchCondition findById(Long id) {
		return taskMatchConditionDao.findOne(id);
	}

	public List<TaskMatchCondition> findByIds(String ids) {
		List<String> idList = Arrays.asList(ids.split(","));
		for (String id : idList) {
			if (!NumberUtils.isNumber(id)) {
				return null;
			}
		}
		String sql = "select * from pc_task_match_condition where id in (" + ids + ")";
		return queryDao.queryObject(sql, TaskMatchCondition.class);
	}

	public List<Map<String, Object>> findConditionInfo(Long id, Integer type) {
		List<TaskMatchCondition> list = findByTargetIdAndType(id, type);
		List<Map<String, Object>> conditionList = new ArrayList<>();
		for (TaskMatchCondition ruleCondition : list) {
			Map<String, Object> map = new HashMap<>();
			map.put("param1", ruleCondition.getParam1());
			map.put("symbol", ruleCondition.getSymbol());
			map.put("param2", ruleCondition.getParam2());
			map.put("result", ruleCondition.getResult());
			map.put("id", ruleCondition.getId());
			conditionList.add(map);
		}
		return conditionList;
	}

	public List<TaskMatchCondition> findByTargetIdAndType(Long id, Integer type) {
		String sql = "select * from pc_task_match_condition where target_id=" + id + " and module_type=" + type
				+ " and is_valid=1";
		return queryDao.queryObject(sql, TaskMatchCondition.class);
	}

}
