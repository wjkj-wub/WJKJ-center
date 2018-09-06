package com.miqtech.master.service.activity;

import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.activity.ActivityCardDao;
import com.miqtech.master.entity.activity.ActivityCard;
import com.miqtech.master.utils.BeanUtils;

/**
 * 赛事信息管理
 */
@Component
public class ActivityCardService {

	@Autowired
	private ActivityCardDao activityCardDao;

	public ActivityCard findById(Long id) {
		return activityCardDao.findOne(id);
	}

	public ActivityCard findValidById(Long id) {
		return activityCardDao.findByIdAndValid(id, CommonConstant.INT_BOOLEAN_TRUE);
	}

	public ActivityCard save(ActivityCard card) {
		if (null != card) {
			Date now = new Date();
			card.setUpdateDate(now);
			if (null == card.getId() && null == card.getValid()) {
				card.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				card.setCreateDate(now);
			}
			return activityCardDao.save(card);
		}
		return null;
	}

	/**
	 * 查询用户的参赛卡
	 */
	public ActivityCard findValidOneByUserId(Long userId) {
		List<ActivityCard> cards = activityCardDao.findByUserIdAndValid(userId, CommonConstant.INT_BOOLEAN_TRUE);
		if (CollectionUtils.isNotEmpty(cards)) {
			return cards.get(0);
		}
		return null;
	}

	/**
	 * 新增 或 更新 对象
	 */
	public ActivityCard saveOrUpdate(ActivityCard card) {
		if (null != card) {
			Date now = new Date();
			card.setUpdateDate(now);
			if (null != card.getId()) {
				ActivityCard old = findById(card.getId());
				if (null != old) {
					card = BeanUtils.updateBean(old, card);
				}
			} else {
				card.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				card.setCreateDate(now);
			}
			return activityCardDao.save(card);
		}
		return card;
	}

}