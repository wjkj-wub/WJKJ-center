package com.miqtech.master.service.mp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.mp.MpUserDao;
import com.miqtech.master.entity.mp.MpUser;

@Component
public class MpUserService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private MpUserDao mpUserDao;

	public MpUser saveOrUpdate(MpUser mpUser) {
		return mpUserDao.save(mpUser);
	}

	/**
	 * 通过openId查询公众号用户
	 */
	public MpUser findValidByOpenId(String openId) {
		List<MpUser> mpUsers = mpUserDao.findByOpenIdAndValid(openId, CommonConstant.INT_BOOLEAN_TRUE);
		if (CollectionUtils.isNotEmpty(mpUsers)) {
			return mpUsers.get(0);
		}
		return null;
	}

	/**
	 * 通过openId查询用户信息
	 */
	public Map<String, Object> queryUserinfoByOpenId(String openId) {
		String sql = "SELECT mu.open_id openId, mu.user_id userId, au.username telephone, mu.nickname, mu.headimg FROM"
				+ " mp_t_user mu left join user_t_info au on mu.user_id = au.id and au.is_valid = 1"
				+ " where mu.is_valid = 1 and mu.open_id = :openId";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("openId", openId);
		return queryDao.querySingleMap(sql, params);
	}
}
