package com.miqtech.master.service.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.activity.ActivityOverActivityPraiseDao;
import com.miqtech.master.entity.activity.ActivityOverActivityPraise;

@Component
public class ActivityOverActivityPraiseService {

	@Autowired
	QueryDao queryDao;
	@Autowired
	ActivityOverActivityPraiseDao activityOverActivityPraiseDao;

	/**
	 * 根据ID查询
	 */
	public ActivityOverActivityPraise findById(Long id) {
		return activityOverActivityPraiseDao.findOne(id);
	}

	/**
	 * 保存资讯
	 */
	public ActivityOverActivityPraise saveOrUpdate(ActivityOverActivityPraise item) {

		return activityOverActivityPraiseDao.save(item);

	}

	public ActivityOverActivityPraise findByActivityOverActivityIdAndUserId(long infoId, long userId) {
		return activityOverActivityPraiseDao.findByActivityOverActivityIdAndUserId(infoId, userId);
	}

}