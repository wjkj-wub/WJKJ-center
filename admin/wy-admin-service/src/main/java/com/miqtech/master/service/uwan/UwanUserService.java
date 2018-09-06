package com.miqtech.master.service.uwan;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.uwan.UwanUserDao;
import com.miqtech.master.entity.uwan.UwanUser;

@Service
public class UwanUserService {

	private static final Logger logger = LoggerFactory.getLogger(UwanUserService.class);

	@Autowired
	private UwanUserDao uwanUserDao;

	/**
	 * 通过多个uwanUserId查询绑定关系
	 */
	public List<UwanUser> findValidByUwanUserIds(List<String> uwanUserIds) {
		if (CollectionUtils.isEmpty(uwanUserIds)) {
			return null;
		}

		return uwanUserDao.findByUwanUserIdInAndValid(uwanUserIds, CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * 批量保存
	 */
	public void batchUpdate(List<UwanUser> users) {
		if (CollectionUtils.isEmpty(users)) {
			return;
		}

		try {
			uwanUserDao.save(users);
		} catch (Exception e) {
			logger.error("批量更新优玩用户信息异常:", e);
		}
	}
}
