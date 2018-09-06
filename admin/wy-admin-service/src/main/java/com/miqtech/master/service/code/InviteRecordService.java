package com.miqtech.master.service.code;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.code.InviteCodeDao;
import com.miqtech.master.dao.code.InviteRecordDao;
import com.miqtech.master.dao.user.UserSevenDayExistDao;
import com.miqtech.master.entity.code.InviteCode;
import com.miqtech.master.entity.code.InviteRecord;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.entity.user.UserSevenDayExist;
import com.miqtech.master.utils.SqlJoiner;

@Component
public class InviteRecordService {
	@Autowired
	private InviteRecordDao inviteRecordDao;
	@Autowired
	private InviteCodeDao inviteCodeDao;
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private UserSevenDayExistDao userSevenDayExistDao;

	/**一定时间段的邀请码的邀请数(暂时废弃)
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public List<Map<String, Object>> queryInviteNum(String startDate, String endDate, String userId) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		String codeSql = "select code,name from invitecode_invitecode where user_id=" + userId;
		Map<String, Object> m = queryDao.querySingleMap(codeSql);
		String code = "";
		String name = "";
		String sql = "";
		if (m != null) {
			code = (String) m.get("code");
			name = (String) m.get("name");
			Integer length = code.length();
			sql = SqlJoiner.join("select '", name, "' name,count(1) num from invitecode_record a where  a.code like '",
					code, "%' AND date_format(a.create_date, '%Y-%m-%d') >= '", startDate,
					"' AND date_format(a.create_date, '%Y-%m-%d') <= '", endDate, "' group by SUBSTR(a.code,1,",
					String.valueOf(length), ")='", code, "'");
			result.addAll(queryDao.queryMap(sql));
		}
		codeSql = "select code,name from invitecode_invitecode where create_user_id=" + userId;
		List<Map<String, Object>> list = queryDao.queryMap(codeSql);
		for (Map<String, Object> map : list) {
			code = (String) map.get("code");
			name = (String) map.get("name");
			Integer length = code.length();
			length = code.length();
			sql = SqlJoiner.join("select '", name, "' name,count(1) num from invitecode_record a where  a.code like '",
					code, "%' AND date_format(a.create_date, '%Y-%m-%d') >= '", startDate,
					"' AND date_format(a.create_date, '%Y-%m-%d') <= '", endDate, "' group by SUBSTR(a.code,1,",
					String.valueOf(length), ")='", code, "'");
			result.addAll(queryDao.queryMap(sql));
		}
		return result;
	}

	/**一定时间段的单个邀请码的邀请数
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public List<Map<String, Object>> querySingleInviteNum(String codeId, String startDate, String endDate) {
		InviteCode invitecode = inviteCodeDao.findOne(Long.valueOf(codeId));
		String name = invitecode.getName();
		String code = invitecode.getCode();
		Integer length = code.length();
		String sql = SqlJoiner
				.join("select '",
						name,
						"' name,date_format(a.create_date, '%Y-%m-%d') date,count(1) num from invitecode_record a where  a.code like '",
						code, "%' AND date_format(a.create_date, '%Y-%m-%d') >= '", startDate,
						"' AND date_format(a.create_date, '%Y-%m-%d') <= '", endDate,
						"' group by date_format(a.create_date, '%Y-%m-%d'),SUBSTR(a.code,1,", String.valueOf(length),
						")='", code, "'");
		return queryDao.queryMap(sql);
	}

	public InviteRecord saveOrUpdate(InviteRecord record) {
		return inviteRecordDao.save(record);

	}

	@Autowired
	private InviteCodeService inviteCodeService;

	public InviteRecord bindInviteCode(UserInfo userInfo, String bindInviteCode, Date date) {
		if (StringUtils.isNotBlank(bindInviteCode)) {
			InviteCode inviteCode = inviteCodeService.findByCode(bindInviteCode);
			if (inviteCode != null) {
				InviteRecord record = new InviteRecord();
				record.setValid(1);
				record.setCreateDate(new Date());
				record.setUserId(userInfo.getId());
				record.setCodeId(inviteCode.getId());
				record.setCode(inviteCode.getCode());
				return this.saveOrUpdate(record);
			}
		}
		return null;
	}

	public InviteRecord findByUserAndCode(Long userId, String invitationCode) {
		return inviteRecordDao.findByUserIdAndCode(userId, invitationCode);
	}

	public InviteRecord findByUserId(Long userId) {
		return inviteRecordDao.findByUserIdAndValid(userId, 1);
	}

	/**记录七日留存
	 * @param userId
	 */
	public void logSevenDayExist(long userId) {
		InviteRecord inviteRecord = inviteRecordDao.findByUserIdAndValid(userId, 1);
		if (inviteRecord != null) {
			UserSevenDayExist userSevenDayExist = userSevenDayExistDao.findByUserId(userId);
			if (userSevenDayExist == null) {
				Date inviteDate = inviteRecord.getCreateDate();
				Date startDate = DateUtils.addDays(inviteDate, 1);
				Date endDate = DateUtils.addDays(inviteDate, 8);
				Date now = new Date();
				if (startDate.getTime() < now.getTime() && now.getTime() < endDate.getTime()) {
					UserSevenDayExist obj = new UserSevenDayExist();
					obj.setValid(1);
					obj.setUserId(userId);
					obj.setInviteCode(inviteRecord.getCode());
					obj.setInviteDate(inviteRecord.getCreateDate());
					obj.setCreateDate(now);
					userSevenDayExistDao.save(obj);
				}
			}

		}
	}
}
