package com.miqtech.master.service.netbar;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.NetbarMerchantDao;
import com.miqtech.master.entity.netbar.NetbarMerchant;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.EncodeUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class NetbarMerchantService {

	@Autowired
	NetbarMerchantDao merchantDao;

	public NetbarMerchant findByUserNameAndPassword(String phone, String password) {
		password = EncodeUtils.base64Md5(password);
		return merchantDao.findByUsernameAndPasswordAndValid(phone, password, 1);
	}

	public NetbarMerchant save(NetbarMerchant merchant) {
		return merchantDao.save(merchant);
	}

	public NetbarMerchant findById(Long id) {
		return merchantDao.findOne(id);
	}

	public NetbarMerchant findByNetbarId(Long netbarId) {
		return merchantDao.findByNetbarIdAndValid(netbarId, 1);
	}

	private PageRequest buildPageRequest(int pageNumber) {
		return new PageRequest(pageNumber - 1, PageUtils.ADMIN_DEFAULT_PAGE_SIZE, new Sort(Direction.DESC, "id"));
	}

	public Page<NetbarMerchant> page(int page, Map<String, Object> params) {
		PageRequest pageRequest = buildPageRequest(page);
		Specification<NetbarMerchant> spec = buildSpecification(params);
		return merchantDao.findAll(spec, pageRequest);
	}

	@SuppressWarnings({ "rawtypes" })
	private Specification<NetbarMerchant> buildSpecification(final Map<String, Object> searchParams) {
		Specification<NetbarMerchant> spec = new Specification<NetbarMerchant>() {
			@Override
			public Predicate toPredicate(Root<NetbarMerchant> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				int valid = NumberUtils.toInt(searchParams.get("status").toString());
				List<Predicate> ps = Lists.newArrayList();
				Path validPath = root.get("valid");
				Predicate validPredicate = cb.equal(validPath, valid);//有效
				ps.add(validPredicate);
				query.where(cb.and(ps.toArray(new Predicate[ps.size()])));
				return query.getRestriction();
			}
		};
		return spec;
	}

	@Autowired
	private QueryDao queryDao;

	public List<Map<String, Object>> findMerchantByAreaCode(String areaCode) {
		String sql = "select id from netbar_t_merchant where netbar_id in"
				+ "(select id from netbar_t_info where area_code = '" + areaCode
				+ "' and is_valid = 1 and is_release = 1) and is_valid = 1 ";
		return queryDao.queryMap(sql);
	}

	public NetbarMerchant findValidByUsername(String phone) {
		return merchantDao.findByUsernameAndValid(phone, 1);
	}

	public NetbarMerchant findByUsername(String username) {
		return merchantDao.findByUsername(username);
	}

	/**审核人员商户列表
	 */
	public PageVO merchantAreaLimit(Integer status, Long userId, int page, String netbarName, String telephone,
			String areaCode, String startDate, String endDate, Integer source, Integer levels) {
		String sql = "";
		PageVO vo = new PageVO();
		String netbarNameSql = "";
		String telephoneSql = "";
		String areaCodeSql = "";
		String startDateSql = "";
		String endDateSql = "";
		String sourceSql = "";
		String levelsSql = "";
		if (StringUtils.isNotBlank(startDate)) {
			startDateSql = SqlJoiner.join(" and a.create_date>'", startDate, "'");
		}
		if (StringUtils.isNotBlank(endDate)) {
			endDateSql = SqlJoiner.join(" and a.create_date<'", endDate, "'");
		}
		if (StringUtils.isNotBlank(netbarName)) {
			netbarNameSql = SqlJoiner.join(" and c.name like '%", netbarName, "%'");
		}
		if (StringUtils.isNotBlank(telephone)) {
			telephoneSql = SqlJoiner.join(" and a.owner_telephone like '%", telephone, "%'");
		}
		if (StringUtils.isNotBlank(areaCode)) {
			if (areaCode.substring(2, 6).equals("0000")) {
				areaCode = areaCode.substring(0, 2) + "%";
			} else if (areaCode.substring(4, 6).equals("00")) {
				areaCode = areaCode.substring(0, 4) + "%";
			}
			areaCodeSql = SqlJoiner.join(" and c.area_code like '", areaCode, "'");
		}
		if (source != null) {
			sourceSql = SqlJoiner.join(" and a.source=", String.valueOf(source));
		}
		if (levels != null) {
			levelsSql = SqlJoiner.join(" and c.levels=", String.valueOf(levels));
		}
		sql = SqlJoiner.join("select count(1) from netbar_t_merchant a,sys_t_area b,netbar_t_info c",
				" where a.is_valid=", String.valueOf(status), netbarNameSql, telephoneSql, areaCodeSql, startDateSql,
				endDateSql, sourceSql, levelsSql,
				" and a.netbar_id=c.id and c.area_code=b.area_code and b.id in(select area_id from sys_r_user_area where sys_user_id=",
				String.valueOf(userId), ")");
		Number totalCount = queryDao.query(sql);
		if (totalCount != null) {
			vo.setTotal(totalCount.intValue());
		}
		int pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		String limitSql = StringUtils.EMPTY;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("status", status);
		params.put("userId", userId);
		if (page > 0) {

			int start = (page - 1) * pageSize;
			if (start + pageSize >= vo.getTotal()) {
				vo.setIsLast(1);
			}
			params.put("start", start);
			params.put("pageSize", pageSize);
			limitSql = " limit :start,:pageSize ";

		}
		sql = SqlJoiner.join(
				"select a.*,c.name netbarName,c.id netbarId,x.realname,c.levels from (netbar_t_merchant a,sys_t_area b,netbar_t_info c) left join sys_t_user x on a.create_user_id=x.id ",
				" where a.is_valid=:status", netbarNameSql, telephoneSql, areaCodeSql, startDateSql, endDateSql,
				sourceSql, levelsSql,
				" and a.netbar_id=c.id and c.area_code=b.area_code and b.id in(select area_id from sys_r_user_area where sys_user_id=:userId) ",
				limitSql);
		vo.setCurrentPage(page);
		vo.setList(queryDao.queryMap(sql, params));
		return vo;
	}

	public boolean isUsernameExist(String username) {
		String sql = "select count(1) from netbar_t_merchant where username ='" + username
				+ "' and (is_valid =1 or is_valid=2)";
		Number count = queryDao.query(sql);
		return count == null ? false : count.intValue() > 0 ? true : false;
	}

	public boolean isMerchantValidedExist(String username, Long merchantId) {
		String sql = "";
		if (merchantId == null) {
			sql = "select count(1) from netbar_t_merchant where username ='" + username + "' and is_valid=1";
		} else {
			sql = "select count(1) from netbar_t_merchant where username ='" + username + "' and is_valid=1 and id <>"
					+ merchantId.longValue();
		}
		Number count = queryDao.query(sql);
		return count == null ? false : count.intValue() > 0 ? true : false;
	}

	public int findValidNetbarCount(String ownerTelephone) {

		String countNetbar = "select count(m.id)  from netbar_t_merchant m, netbar_t_info i where m.netbar_id = i.id  and m.owner_telephone = '"
				+ ownerTelephone + "'  and m.is_valid = 1  and i.is_valid = 1 ";

		Number count = queryDao.query(countNetbar);
		if (null != count) {
			return count.intValue();
		}
		return 0;
	}

	public List<Map<String, Object>> statisticNetbarDescData(String ownerTelephone) {
		String statisticSql = "SELECT mer.netbar_id id, info.name name, count(DISTINCT nuser.user_id) memberCount, case when dtotal_amount is null then  0.00 else dtotal_amount end  todayAmount,case when wtotal_amount is null then  0.00 else wtotal_amount end  weekAmount,case when stotal_amount is null then  0.00 else stotal_amount end  allAmount   "
				+ " FROM netbar_t_merchant mer JOIN netbar_t_info info ON mer.netbar_id = info.id AND info.is_valid = 1 LEFT JOIN netbar_r_user nuser ON mer.netbar_id = nuser.netbar_id"
				+ " AND nuser.is_valid = 1 LEFT JOIN ( SELECT netbar_id, sum(total_amount) total_amount,"
				+ "		sum(  IF ( create_date BETWEEN :dayStartDate AND :statisEndDate AND STATUS >= 1, total_amount, 0 ) ) dtotal_amount,"
				+ "		sum( IF ( create_date BETWEEN :weekStartDate AND :statisEndDate AND STATUS >= 1, total_amount, 0 ) ) wtotal_amount,"
				+ "		sum( IF (STATUS = 4, total_amount, 0) ) stotal_amount FROM netbar_r_order WHERE STATUS >= 1 AND netbar_id = 1 GROUP BY netbar_id"
				+ " ) norder ON mer.netbar_id = norder.netbar_id WHERE mer.is_valid = 1 AND owner_telephone = :ownerTelephone  GROUP BY mer.netbar_id, mer.owner_telephone, info. NAME";

		Map<String, Object> params = Maps.newHashMap();
		Date now = new Date();
		String dayStartDate = DateUtils.dateToString(now, "yyyy-MM-dd 00:00:00");
		String endDate = DateUtils.dateToString(now, "yyyy-MM-dd 23:59:59");
		String weekStartDate = DateUtils.dateToString(DateUtils.addDays(now, -7), "yyyy-MM-dd 23:59:59");
		params.put("ownerTelephone", ownerTelephone);
		params.put("dayStartDate", dayStartDate);
		params.put("statisEndDate", endDate);
		params.put("weekStartDate", weekStartDate);

		return queryDao.queryMap(statisticSql, params);
	}

	public int countUnAuditorNetbarMerchant(Long systemUserId) {
		String sql = SqlJoiner.join("select count(1) from netbar_t_merchant a,sys_t_area b,netbar_t_info c",
				" where a.is_valid=2",
				" and a.netbar_id=c.id and c.area_code=b.area_code and b.id in(select area_id from sys_r_user_area where sys_user_id=",
				String.valueOf(systemUserId), ")");

		Number totalCount = queryDao.query(sql);
		return totalCount == null ? 0 : totalCount.intValue();
	}

}