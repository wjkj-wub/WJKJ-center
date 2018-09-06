package com.miqtech.master.service.pc.taskmatch;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.pc.taskmatch.TaskMatchLimitHeroDao;
import com.miqtech.master.entity.pc.taskmatch.TaskMatchLimitHero;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shilina
 */
@Service
public class TaskMatchLimitHeroService {
	@Autowired
	private TaskMatchLimitHeroDao taskMatchLimitHeroDao;
	@Autowired
	private QueryDao queryDao;

	public void save(List<TaskMatchLimitHero> list) {
		taskMatchLimitHeroDao.save(list);
	}

	public List<Map<String, Object>> findHeroId(Long id, Integer type) {
		List<TaskMatchLimitHero> list = findByTargetIdAndType(id, type);
		List<Map<String, Object>> heroList = new ArrayList<>();
		for (TaskMatchLimitHero taskMatchLimitHero : list) {
			Map<String, Object> map = new HashMap<>();
			map.put("lol_hero_id", taskMatchLimitHero.getLolHeroId());
		}
		return heroList;

	}

	public List<TaskMatchLimitHero> findByTargetIdAndType(Long id, Integer type) {
		String sql = "select * from pc_task_match_limit_hero where task_id=" + id + " and task_type=" + type
				+ " and is_valid=1";
		return queryDao.queryObject(sql, TaskMatchLimitHero.class);
	}

}
