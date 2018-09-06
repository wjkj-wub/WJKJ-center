package com.miqtech.master.service.mall;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.mall.MallBlacklistDao;
import com.miqtech.master.entity.mall.MallBlacklist;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.service.user.UserInfoService;
import com.miqtech.master.utils.IdentityUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

/**
 * 商城黑名单
 */
@Component
public class MallBlacklistService {

	private static final String REDIS_USER_TOKEN = "wy_api_user_token_";
	private static final Integer TYPE_BLACKLIST_INLIST = 1;
	private static final Integer TYPE_BLACKLIST_OUTLIST = 2;

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private MallBlacklistDao mallBlacklistDao;
	@Autowired
	private UserInfoService userInfoService;
	@Autowired
	private StringRedisOperateService stringRedisOperateService;

	public MallBlacklist save(MallBlacklist b) {
		if (b != null) {
			Date now = new Date();
			b.setUpdateDate(now);
			b.setCreateDate(now);
			b.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			return mallBlacklistDao.save(b);
		}

		return null;
	}

	/**
	 * 删除用户黑名单记录
	 */
	public void deleteUserRecord(Long userId) {
		String sql = SqlJoiner.join("UPDATE mall_t_blacklist SET is_valid = 0 WHERE user_id = ", userId.toString());
		queryDao.update(sql);
	}

	/**
	 * 拉黑用户
	 */
	public void disabledUser(Long userId, String telephone, String qq, String remark) {
		if (userId == null) {
			return;
		}

		// 更改用户状态
		userInfoService.delete(userId);

		// 覆盖token
		String token = IdentityUtils.uuidWithoutSplitter();
		stringRedisOperateService.setData(REDIS_USER_TOKEN + userId.toString(), token);

		// 删除历史记录
		deleteUserRecord(userId);

		// 增加黑名单记录
		MallBlacklist b = new MallBlacklist();
		b.setUserId(userId);
		b.setTelephone(telephone);
		b.setQq(qq);
		b.setRemark(remark);
		b.setType(TYPE_BLACKLIST_INLIST);
		save(b);
	}

	/**
	 * 取消拉黑
	 */
	public void enabledUser(Long userId, String telephone, String qq, String remark) {
		if (userId == null) {
			return;
		}

		UserInfo user = userInfoService.findById(userId);
		if (user != null) {
			user.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			userInfoService.save(user);

			// 删除历史记录
			deleteUserRecord(userId);

			// 增加黑名单记录
			MallBlacklist b = new MallBlacklist();
			b.setUserId(userId);
			b.setTelephone(telephone);
			b.setQq(qq);
			b.setRemark(remark);
			b.setType(TYPE_BLACKLIST_OUTLIST);
			save(b);
		}
	}

	public PageVO page(int page, String telephone, String qq, String beginDate, String endDate) {
		Map<String, Object> params = Maps.newHashMap();
		String condition = "";
		if (StringUtils.isNotBlank(telephone)) {
			telephone = SqlJoiner.join("%", telephone, "%");
			condition = SqlJoiner.join(condition, " AND telephone like :telephone");
			params.put("telephone", telephone);
		}
		if (StringUtils.isNotBlank(qq)) {
			qq = SqlJoiner.join("%", qq, "%");
			condition = SqlJoiner.join(condition, " AND qq like :qq");
			params.put("qq", qq);
		}
		if (StringUtils.isNotBlank(beginDate)) {
			condition = SqlJoiner.join(condition, " AND create_date >= :beginDate");
			params.put("beginDate", beginDate);
		}
		if (StringUtils.isNotBlank(endDate)) {
			condition = SqlJoiner.join(condition, " AND create_date < ADDDATE(:endDate, INTERVAL 1 DAY)");
			params.put("endDate", endDate);
		}

		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		Integer startRow = (page - 1) * pageSize;
		String sql = SqlJoiner
				.join("SELECT user_id userId, telephone, qq, remark, type, is_valid, create_date createDate FROM mall_t_blacklist b WHERE 1",
						condition, " AND is_valid = 1 AND type = 1", " LIMIT ", startRow.toString(), ", ",
						pageSize.toString());
		List<Map<String, Object>> list = queryDao.queryMap(sql, params);

		String countSql = SqlJoiner.join("SELECT COUNT(1) FROM mall_t_blacklist b WHERE 1");
		if (StringUtils.isNotBlank(telephone)) {
			countSql = SqlJoiner.join(countSql, " AND telephone like '", telephone, "'");
		}
		if (StringUtils.isNotBlank(qq)) {
			countSql = SqlJoiner.join(countSql, " AND qq like '", qq, "'");
		}
		if (StringUtils.isNotBlank(beginDate)) {
			countSql = SqlJoiner.join(countSql, " AND create_date >= '", beginDate, "'");
		}
		if (StringUtils.isNotBlank(endDate)) {
			countSql = SqlJoiner.join(countSql, " AND create_date < ADDDATE('", endDate, "', INTERVAL 1 DAY)");
		}
		countSql = SqlJoiner.join(countSql, " AND is_valid = 1 AND type = 1");
		Number total = (Number) queryDao.query(countSql);
		if (total == null) {
			total = 0;
		}

		PageVO vo = new PageVO();
		vo.setList(list);
		vo.setTotal(total.intValue());
		int isLast = total.intValue() > page * pageSize ? 1 : 0;
		vo.setIsLast(isLast);
		return vo;
	}
}
