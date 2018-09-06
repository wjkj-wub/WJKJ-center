package com.miqtech.master.service.mall;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.mall.MallInviteDao;
import com.miqtech.master.dao.user.UserInfoDao;
import com.miqtech.master.entity.mall.MallInvite;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.utils.StringUtils;

/**
 * 邀请好友
 *
 */
@Component
public class MallInviteService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private MallInviteDao mallInviteDao;
	@Autowired
	private UserInfoDao userInfoDao;

	/**do邀请好友
	 * @param userId
	 * @param phone
	 * @return
	 */
	public int inviteFriend(Long userId, String phone) {
		if (!NumberUtils.isNumber(phone) || mallInviteDao.findByInvitedTelephoneAndIsRegister(phone, 0) != null
				|| userInfoDao.findByUsername(phone) != null) {
			return 1;
		} else if (limitInvite(userId)) {
			return 3;
		}
		MallInvite mallInvite = new MallInvite();
		mallInvite.setInviteUserId(userId);
		mallInvite.setInvitedTelephone(phone);
		mallInvite.setIsRegister(0);
		mallInvite.setCreateDate(new Date());
		mallInviteDao.save(mallInvite);
		return 0;
	}

	public boolean limitInvite(Long userId) {
		String sql = SqlJoiner.join("select count(1) from mall_t_invite where invite_user_id=", String.valueOf(userId),
				" and is_register<>2 and date_format(create_date, '%Y-%m-%d')='",
				DateUtils.dateToString(new Date(), DateUtils.YYYY_MM_DD), "'");
		Number total = queryDao.query(sql);
		return total.intValue() >= 10;
	}

	/**根据邀请人id查找
	 * @param userId
	 * @return
	 */
	public List<MallInvite> findByInviteUserIdAndIsRegisterNot(Long userId, int isRegister) {
		return mallInviteDao.findByInviteUserIdAndIsRegisterNot(userId, isRegister);
	}

	/**根据被邀请人号码及是否已注册查找
	 * @param phone
	 * @param isRegister
	 * @return
	 */
	public MallInvite findByInvitedTelephoneAndIsRegister(String phone, Integer isRegister) {
		return mallInviteDao.findByInvitedTelephoneAndIsRegister(phone, isRegister);
	}

	/**
	 * 统计邀请
	 */
	public List<Map<String, Object>> statis(String beginDate, String endDate, Integer threshold, String order,
			Integer limit) {
		if (limit < 1) {
			limit = 10;
		}
		if (limit > 30) {
			limit = 30;
		}

		String groupField = "count";
		if ("2".equals(order)) {
			groupField = "registerCount";
		}

		String having = "";
		if (threshold != null) {
			having = " HAVING :groupField > :threshold".replaceAll(":groupField", groupField).replaceAll(":threshold",
					threshold.toString());
		}

		String conditions = " WHERE 1";
		if (StringUtils.isNotBlank(beginDate)) {
			conditions = SqlJoiner.join(conditions, " AND i.create_date >= ':beginDate'").replaceAll(":beginDate",
					beginDate);
		}
		if (StringUtils.isNotBlank(endDate)) {
			conditions = SqlJoiner.join(conditions, " AND i.create_date < ADDDATE(':endDate',INTERVAL 1 DAY)")
					.replaceAll(":endDate", endDate);
		}

		String sql = SqlJoiner
				.join("SELECT u.username, count(i.id) count, sum(IF(is_register = 1, 1, 0)) registerCount",
						" FROM mall_t_invite i LEFT JOIN user_t_info u ON i.invite_user_id = u.id :conditions",
						" GROUP BY i.invite_user_id :having ORDER BY :groupField DESC LIMIT 0, :limit")
				.replaceAll(":conditions", conditions).replaceAll(":having", having)
				.replaceAll(":groupField", groupField).replaceAll(":limit", limit.toString());
		return queryDao.queryMap(sql);
	}

	public void inviteTimeOut() {
		String sql = "update mall_t_invite set is_register=2 where is_register=0 and create_date<date_add(now(), interval -1 day)";
		queryDao.update(sql);
	}
}
