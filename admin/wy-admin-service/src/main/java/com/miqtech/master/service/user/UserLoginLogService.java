package com.miqtech.master.service.user;

import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.user.UserLoginLogDao;
import com.miqtech.master.entity.user.UserLoginLog;

/**
 * 用户登录日志
 */
@Component
public class UserLoginLogService {

	@Autowired
	UserLoginLogDao userLoginLogDao;

	/**
	 * 查找用户的登录日志
	 * 
	 * @param userId
	 *            用户id
	 */
	public UserLoginLog findByUserId(Long userId) {
		List<UserLoginLog> logs = userLoginLogDao.findByUserId(userId);
		if (CollectionUtils.isNotEmpty(logs)) {
			return logs.get(0);
		}
		return null;
	}

	/**
	 * 保存或更新登录记录
	 */
	public UserLoginLog save(UserLoginLog loginLog) {
		Date now = new Date();
		if (loginLog.getId() == null) {
			loginLog.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			loginLog.setCreateDate(now);
		}
		return userLoginLogDao.save(loginLog);
	}
}