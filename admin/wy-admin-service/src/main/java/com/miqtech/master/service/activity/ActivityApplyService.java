package com.miqtech.master.service.activity;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.activity.ActivityApplyDao;
import com.miqtech.master.entity.activity.ActivityApply;

/**
 * 赛事信息管理
 */
@Component
public class ActivityApplyService {

	@Autowired
	private ActivityApplyDao activityApplyDao;

	public ActivityApply save(ActivityApply a) {
		if (a == null) {
			return null;
		}
		return activityApplyDao.save(a);
	}

	public ActivityApply findValidByTargetIdAndType(Long targetId, Integer type) {
		List<ActivityApply> applys = activityApplyDao.findByTargetIdAndTypeAndValid(targetId, type,
				CommonConstant.INT_BOOLEAN_TRUE);
		if (CollectionUtils.isEmpty(applys)) {
			return null;
		}

		return applys.get(0);
	}
}