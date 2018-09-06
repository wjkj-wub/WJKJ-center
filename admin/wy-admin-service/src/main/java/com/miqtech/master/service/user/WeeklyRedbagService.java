package com.miqtech.master.service.user;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;
import com.miqtech.master.dao.common.SystemRedbagDao;
import com.miqtech.master.dao.user.UserRedbagDao;
import com.miqtech.master.entity.common.SystemRedbag;
import com.miqtech.master.entity.user.UserRedbag;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.JsonUtils;
import com.miqtech.master.utils.StringUtils;

/**
 * 周红包service
 */
@Component
public class WeeklyRedbagService {

	private static final Joiner JOINER = Joiner.on("_");
	private static final String KEY_CURRENT_SYSTEM_REDBAG = "wy_redbag_weekly_config";// 本周红包的信息：wy_redbag_weekly_config_{date}

	@Autowired
	private SystemRedbagDao systemRedbagDao;
	@Autowired
	private UserRedbagDao userRedbagDao;
	@Autowired
	private StringRedisOperateService stringRedisOperateService;

	public SystemRedbag getCurrentSystemRedbag() {
		// 优先从redis中取
		SystemRedbag redbag = null;
		Date nowDate = new Date();
		String key = JOINER.join(KEY_CURRENT_SYSTEM_REDBAG, DateUtils.dateToString(nowDate, DateUtils.YYYY_MM_DD));
		String redbagStr = stringRedisOperateService.getData(key);
		if (StringUtils.isBlank(redbagStr)) {
			redbag = systemRedbagDao
					.findByTypeAndValidAndBeginTimeLessThanAndEndTimeGreaterThan(3, 1, nowDate, nowDate);
			if (redbag != null) {
				String srStr = JsonUtils.objectToString(redbag);
				stringRedisOperateService.setData(key, srStr);
			}
		} else {
			redbag = JsonUtils.stringToObject(redbagStr, SystemRedbag.class);
		}
		return redbag;
	}

	public Map<String, Object> validateTime() throws ParseException {
		Map<String, Object> data = new HashMap<String, Object>();
		SystemRedbag redbag = getCurrentSystemRedbag();

		if (redbag != null) {// 红包活动时间内
			data.put("type", 1);
			data.put("redbagId", redbag.getId());
			data.put("totalAmount", redbag.getTotalmoney());
			data.put("beginTime", redbag.getBeginTime());
		} else {
			Date nowDate = new Date();
			List<SystemRedbag> redbags = systemRedbagDao.findByTypeAndValidAndBeginTimeGreaterThanOrderByBeginTime(3,
					1, nowDate);
			if (CollectionUtils.isNotEmpty(redbags)) {// 查询到下一次红包活动
				redbag = redbags.get(0);
				data.put("type", 2);
				data.put("redbagId", redbag.getId());
				data.put("nowDate", DateUtils.dateToString(nowDate, DateUtils.YYYYMMDDHHMMSS));
				data.put("redbagTime", DateUtils.dateToString(redbag.getBeginTime(), DateUtils.YYYYMMDDHHMMSS));
				data.put("totalAmount", redbag.getTotalmoney());
				data.put("beginTime", redbag.getBeginTime());
			} else {// 没有下次红包活动
				data.put("type", 3);
			}
		}
		return data;
	}

	/**
	 * 保存用户红包
	 * @param userId 用户id
	 * @param redbagId 系统红包id
	 * @param amount 红包金额
	 */
	public UserRedbag saveRedbag(Long userId, Long redbagId, int amount) {
		UserRedbag userRedbag = new UserRedbag();
		userRedbag.setUserId(userId);
		userRedbag.setRedbagId(redbagId);
		userRedbag.setNetbarType(1);
		userRedbag.setValid(1);
		userRedbag.setUsable(1);
		userRedbag.setAmount(amount);
		userRedbag.setCreateDate(new Date());
		userRedbag.setNetbarId(0L);
		return userRedbagDao.save(userRedbag);
	}
}
