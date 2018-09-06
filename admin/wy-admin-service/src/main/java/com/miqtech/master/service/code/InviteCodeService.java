package com.miqtech.master.service.code;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.code.InviteCodeDao;
import com.miqtech.master.entity.code.InviteCode;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class InviteCodeService {
	@Autowired
	private InviteCodeDao inviteCodeDao;
	@Autowired
	private QueryDao queryDao;

	public InviteCode findByUserId(Long userId) {
		return inviteCodeDao.findByUserId(userId);
	}

	public InviteCode findByCodeAndValid(String code, int valid) {
		return inviteCodeDao.findByCodeAndValid(code, valid);
	}

	public InviteCode findByCode(String code) {
		return inviteCodeDao.findByCode(code);
	}

	public void save(InviteCode inviteCode) {
		inviteCodeDao.save(inviteCode);
	}

	public InviteCode findById(Long id) {
		return inviteCodeDao.findOne(id);
	}

	public void del(InviteCode code) {
		inviteCodeDao.delete(code);
	}

	/**邀请码列表分页查询
	 * @param code
	 * @param name
	 * @param username
	 * @param phone
	 * @param areaCode
	 * @param page
	 * @return
	 */
	public PageVO queryList(String code, String name, String username, String phone, String areaCode, Integer page,
			String createUserId, boolean isPage, boolean isAdmin, String startDate, String endDate) {
		String sql = "";
		String codeSql = "";
		String nameSql = "";
		String usernameSql = "";
		String phoneSql = "";
		String areaSql = "";
		String dateSql = "";
		String dateSqlExist = "";
		if (StringUtils.isNotBlank(code)) {
			codeSql = " and a.code like '%" + code.trim() + "%'";
		}
		if (StringUtils.isNotBlank(name)) {
			nameSql = " and a.name like '%" + name.trim() + "%'";
		}
		if (StringUtils.isNotBlank(username)) {
			usernameSql = " and b.username like '%" + username.trim() + "%'";
		}
		if (StringUtils.isNotBlank(phone)) {
			phoneSql = " and a.phone like '%" + phone.trim() + "%'";
		}
		if (StringUtils.isNotBlank(areaCode)) {
			areaSql = " and locate('," + areaCode + "',a.area_code)<>0";
		}
		if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
			dateSql = " and date_format(c.create_date, '%Y-%m-%d') >= '" + startDate
					+ "' AND date_format(c.create_date, '%Y-%m-%d') <= '" + endDate + "'";
			dateSqlExist = " and date_format(y.invite_date, '%Y-%m-%d') >= '" + startDate
					+ "' AND date_format(y.invite_date, '%Y-%m-%d') <= '" + endDate + "'";
		}
		int pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page == null) {
			page = 1;
		}
		int start = (page - 1) * pageSize;
		Map<String, Object> params = new HashMap<String, Object>();
		sql = SqlJoiner
				.join("select count(1) from invitecode_invitecode a left join sys_t_user b on a.user_id=b.id where a.is_valid=1 and (a.create_user_id=",
						createUserId, " or a.user_id=", createUserId, ")", codeSql, nameSql, usernameSql, phoneSql,
						areaSql);
		PageVO vo = new PageVO();
		Number totalCount = queryDao.query(sql);
		if (totalCount != null) {
			vo.setTotal(totalCount.intValue());
		}

		if (start + pageSize >= vo.getTotal()) {
			vo.setIsLast(1);
		}
		List<Map<String, Object>> result;
		String numSql = "";
		String codeTmp = "";
		Integer total = 0;
		Integer surviveTotal = 0;
		if (isPage) {
			params.put("start", start);
			params.put("pageSize", pageSize);
			String limitSql = " limit :start,:pageSize";
			sql = SqlJoiner
					.join("SELECT x.*, count(y.user_id) amount FROM ( SELECT a.id, a.code, b.username, a.name, a.phone, a.area_code, a.user_id, a.create_date, count(DISTINCT c.id) num FROM invitecode_invitecode a LEFT JOIN sys_t_user b ON a.user_id = b.id LEFT JOIN invitecode_record c ON c. CODE LIKE CONCAT(a. CODE, '%') ",
							dateSql,
							" WHERE a.is_valid = 1 AND ( a.create_user_id = ",
							createUserId,
							" OR a.user_id = ",
							createUserId,
							" ) ",
							codeSql,
							nameSql,
							usernameSql,
							phoneSql,
							areaSql,
							" GROUP BY a. CODE ) x LEFT JOIN user_seven_day_exist y ON y.invite_code LIKE CONCAT(x. CODE, '%') ",
							dateSqlExist,
							" GROUP BY CODE ORDER BY SUBSTR( x. CODE, 1, CHAR_LENGTH(x. CODE) - 2 ), x.create_date DESC",
							limitSql);
			vo.setCurrentPage(page);
			result = queryDao.queryMap(sql, params);
			vo.setList(result);
			if (start == 0 && !isAdmin && !result.isEmpty()) {
				Map<String, Object> first = result.get(0);
				Number userId = (Number) first.get("user_id");
				if (userId != null && userId.toString().equals(createUserId)) {
					codeTmp = (String) first.get("code");
					numSql = SqlJoiner.join("select count(1) num from invitecode_record c where  c.code = '", codeTmp,
							"'", dateSql, " group by c.code='", codeTmp, "'");
					Map<String, Object> numMap = queryDao.querySingleMap(numSql);
					first.put("num", numMap == null ? 0 : numMap.get("num") == null ? 0 : numMap.get("num"));
					numSql = SqlJoiner.join(
							"select count(1) amount from user_seven_day_exist y where  y.invite_code = '", codeTmp,
							"'", dateSqlExist, " group by y.invite_code='", codeTmp, "'");
					numMap = queryDao.querySingleMap(numSql);
					first.put("amount", numMap == null ? 0 : numMap.get("amount") == null ? 0 : numMap.get("amount"));
				}
			}
		} else {
			sql = SqlJoiner
					.join("SELECT x.*, count(y.user_id) amount FROM ( SELECT a.id, a.code, b.username, a.name, a.phone, a.area_code, a.user_id, a.create_date, count(DISTINCT c.id) num FROM invitecode_invitecode a LEFT JOIN sys_t_user b ON a.user_id = b.id LEFT JOIN invitecode_record c ON c. CODE LIKE CONCAT(a. CODE, '%') ",
							dateSql,
							" WHERE a.is_valid = 1 AND ( a.create_user_id = ",
							createUserId,
							" OR a.user_id = ",
							createUserId,
							" ) ",
							codeSql,
							nameSql,
							usernameSql,
							phoneSql,
							areaSql,
							" GROUP BY a. CODE ) x LEFT JOIN user_seven_day_exist y ON y.invite_code LIKE CONCAT(x. CODE, '%') ",
							dateSqlExist,
							" GROUP BY CODE ORDER BY SUBSTR( x. CODE, 1, CHAR_LENGTH(x. CODE) - 2 ), x.create_date DESC");
			result = queryDao.queryMap(sql, params);
			vo.setList(result);
			Map<String, Object> first = result.get(0);
			Number userId = (Number) first.get("user_id");
			if (userId != null && userId.toString().equals(createUserId)) {
				codeTmp = (String) first.get("code");
				numSql = SqlJoiner.join("select count(1) num from invitecode_record c where  c.code = '", codeTmp, "'",
						dateSql, " group by c.code='", codeTmp, "'");
				Map<String, Object> numMap = queryDao.querySingleMap(numSql);
				first.put("num", numMap == null ? 0 : numMap.get("num") == null ? 0 : numMap.get("num"));
				numSql = SqlJoiner.join("select count(1) amount from user_seven_day_exist y where  y.invite_code = '",
						codeTmp, "'", dateSqlExist, " group by y.invite_code='", codeTmp, "'");
				numMap = queryDao.querySingleMap(numSql);
				first.put("amount", numMap == null ? 0 : numMap.get("amount") == null ? 0 : numMap.get("amount"));
			}
		}
		List<Map<String, Object>> l = vo.getList();
		for (Map<String, Object> map : l) {
			Number n = (Number) map.get("num");
			total += n.intValue();
			n = (Number) map.get("amount");
			surviveTotal += n.intValue();
		}
		vo.setRemain(String.valueOf(total) + "," + String.valueOf(surviveTotal));
		return vo;

	}

	/**
	 * 通过区域code查找邀请码
	 */
	public List<Map<String, Object>> queryByAreaCode(String[] areaCode) {
		String codeSql = "";
		Joiner joiner = Joiner.on(" or ");
		List<String> list = new ArrayList<String>();
		for (String s : areaCode) {
			list.add("locate('" + s + "',a.area_code)<>0");
		}
		codeSql = joiner.join(list);
		String sql = SqlJoiner.join("select code from invitecode_invitecode a where ", codeSql);
		return queryDao.queryMap(sql);
	}

}
