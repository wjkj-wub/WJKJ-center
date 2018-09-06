package com.miqtech.master.service.netbar;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.NetbarUserDao;
import com.miqtech.master.entity.netbar.NetbarMerchant;
import com.miqtech.master.entity.netbar.NetbarUser;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;

@Component
public class NetbarUserService {

	@Autowired
	private NetbarUserDao netbarUserDao;
	@Autowired
	private QueryDao queryDao;

	public List<Map<String, Object>> findPageData(NetbarMerchant currentMerchant, int page, int type, Date createDate,
			String nickname, String telephone) {
		Map<String, Object> params = Maps.newHashMap();
		int start = (page - 1) * PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		params.put("start", start);
		params.put("pageSize", PageUtils.ADMIN_DEFAULT_PAGE_SIZE);
		params.put("netbarId", currentMerchant.getNetbarId());
		StringBuffer isFirstSql = new StringBuffer();
		if (type == 0) {

		} else if (type == 1) {
			isFirstSql.append("	and is_first=:isFirst ");
			params.put("isFirst", 1);
		} else {
			isFirstSql.append("	and is_first=:isFirst ");
			params.put("isFirst", 0);
		}

		if (createDate != null) {
			isFirstSql
					.append(" and n.create_date > :createDate and n.create_date < date_add(:createDate, INTERVAL 1 DAY)");
			params.put("createDate", DateUtils.dateToString(createDate, DateUtils.YYYY_MM_DD));
		}
		String nicknameSql = "";
		String telephoneSql = "";
		if (StringUtils.isNotBlank(nickname)) {
			nicknameSql = " and u.nickname like '%" + nickname + "%'";
		}
		if (StringUtils.isNotBlank(telephone)) {
			telephoneSql = " and u.telephone like '%" + telephone + "%'";
		}
		String sql = " select n.netbar_id nid,n.is_first isFirst, n.user_id uid, u.nickname, u.realname, u.telephone, RPAD(SUBSTRING(IFNULL(u.idcard, \"\"), 1, 6), 18, '*'    ) idcard, DATE_FORMAT(n.create_date, '%Y-%m-%d %H:%i:%s') createDate,IFNULL(sum(amount),0) amount"
				+ " from netbar_r_user n left join user_t_info u on   n.user_id = u.id  left join netbar_r_order a on n.user_id = a.user_id  and a.netbar_id =:netbarId  where n.netbar_id = :netbarId and a.status>=1 "
				+ nicknameSql
				+ telephoneSql
				+ isFirstSql
				+ " group by n.user_id order by n.create_date desc limit :start, :pageSize ";
		return queryDao.queryMap(sql, params);
	}

	public long countMember(NetbarMerchant currentMerchant, int type, Date createDate, String nickname, String telephone) {
		StringBuffer isFirstSql = new StringBuffer();
		String nicknameSql = "";
		String telephoneSql = "";
		if (StringUtils.isNotBlank(nickname)) {
			nicknameSql = " and b.nickname like '%" + nickname + "%'";
		}
		if (StringUtils.isNotBlank(telephone)) {
			telephoneSql = " and b.username like '%" + telephone + "%'";
		}
		if (type == 0) {

		} else if (type == 1) {
			isFirstSql.append("	and a.is_first=1 ");
		} else {
			isFirstSql.append("	and a.is_first=0 ");
		}

		if (createDate != null) {
			String createDateStr = DateUtils.dateToString(createDate, DateUtils.YYYY_MM_DD);
			isFirstSql.append(" and a.create_date > '" + createDateStr + "' and a.create_date < date_add('"
					+ createDateStr + "', INTERVAL 1 DAY)");
		}

		String sql = "select count(a.id) from (netbar_r_user a,user_t_info b) where a.user_id=b.id and a.netbar_id="
				+ currentMerchant.getNetbarId() + isFirstSql + nicknameSql + telephoneSql;
		Number count = queryDao.query(sql);
		return count.intValue();
	}

	public List<NetbarUser> findByNetbarId(Long netbarId) {
		return netbarUserDao.findByNetbarId(netbarId);
	}

	/**
	 * 注册用户作为网吧的会员
	 */
	public NetbarUser bindNetbar(final Long userId, final Long netbarId) {
		NetbarUser netbarUser = netbarUserDao.findByUserIdAndNetbarId(userId, netbarId);
		if (null == netbarUser) {
			netbarUser = new NetbarUser();
			netbarUser.setCreateDate(new Date());
			netbarUser.setNetbarId(netbarId);
			netbarUser.setUserId(userId);
			netbarUser.setValid(1);
			netbarUser.setIsRegister(1);
			long count = netbarUserDao.count(new Specification<NetbarUser>() {
				@Override
				public Predicate toPredicate(Root<NetbarUser> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					Path<NetbarUser> userIdPath = root.get("userId");
					Predicate userIdPredicate = cb.equal(userIdPath, userId);
					query.where(cb.and(userIdPredicate));
					return query.getRestriction();
				}
			});
			if (count > 0) {
				netbarUser.setIsFirst(0);
			} else {
				netbarUser.setIsFirst(1);
			}
			netbarUserDao.save(netbarUser);
		}
		return netbarUser;
	}

	/**前七天新增会员
	 * @param netbarId
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public List<Map<String, Object>> lastSevenDayNewMember(String netbarId, String startDate, String endDate) {
		String sql = SqlJoiner
				.join("select date_format(b.create_date, '%Y-%m-%d') date,count(id) num  from netbar_r_user b where netbar_id=",
						netbarId, " and b.create_date>='", startDate, "' and b.create_date<='", endDate,
						"' group by date_format(b.create_date, '%Y-%m-%d')");
		return queryDao.queryMap(sql);
	}
}