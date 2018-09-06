package com.miqtech.master.service.activity;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.miqtech.master.dao.activity.ActivityOverActivityModuleInfoDao;
import com.miqtech.master.entity.activity.ActivityOverActivityModuleInfo;

@Component
public class ActivityOverActivityModuleInfoService {

	@Autowired
	private ActivityOverActivityModuleInfoDao activityOverActivityModuleInfoDao;

	public List<ActivityOverActivityModuleInfo> findByOverActivityId(Long id) {
		if (id == null) {
			return null;
		}
		return activityOverActivityModuleInfoDao.findByOverActivityId(id);
	}

	public List<ActivityOverActivityModuleInfo> save(List<ActivityOverActivityModuleInfo> mis) {
		return (List<ActivityOverActivityModuleInfo>) activityOverActivityModuleInfoDao.save(mis);
	}

	/**
	 * 重新产生资讯的模块信息
	 */
	public void resetModuleInfo(Long infoId, String moduleIds) {
		// 删除旧的模块信息
		List<ActivityOverActivityModuleInfo> mis = findByOverActivityId(infoId);
		if (CollectionUtils.isNotEmpty(mis)) {
			activityOverActivityModuleInfoDao.delete(mis);
		}

		// 重新生成模块信息
		if (StringUtils.isBlank(moduleIds)) {
			return;
		}
		String[] moduleIdsSplit = moduleIds.split(",");
		mis = Lists.newArrayList();
		if (ArrayUtils.isEmpty(moduleIdsSplit)) {
			return;
		}
		for (String moduleIdStr : moduleIdsSplit) {
			if (NumberUtils.isNumber(moduleIdStr) && NumberUtils.toLong(moduleIdStr) != 30) {
				ActivityOverActivityModuleInfo mi = new ActivityOverActivityModuleInfo();
				mi.setModuleId(NumberUtils.toLong(moduleIdStr));
				mi.setOverActivityId(infoId);
				mis.add(mi);
			} else if (NumberUtils.isNumber(moduleIdStr) && NumberUtils.toLong(moduleIdStr) == 30) {
				mis.clear();
				ActivityOverActivityModuleInfo mi = new ActivityOverActivityModuleInfo();
				mi.setModuleId(NumberUtils.toLong(moduleIdStr));
				mi.setOverActivityId(infoId);
				mis.add(mi);
				break;
			}
		}
		save(mis);
	}
}
