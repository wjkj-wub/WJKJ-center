package com.miqtech.master.service.system;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.RedbagConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.common.SystemRedbagDao;
import com.miqtech.master.entity.common.SystemRedbag;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;

/**
 * 系统红包service
 */
@Component
public class SystemRedbagService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemRedbagService.class);

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private SystemRedbagDao systemRedbagDao;

	public SystemRedbag save(SystemRedbag redbag) {
		if (redbag == null) {
			return null;
		}

		return systemRedbagDao.save(redbag);
	}

	public List<SystemRedbag> save(List<SystemRedbag> redbags) {
		if (CollectionUtils.isEmpty(redbags)) {
			return null;
		}

		return (List<SystemRedbag>) systemRedbagDao.save(redbags);
	}

	public void delete(long id) {
		SystemRedbag redbag = findOne(id);
		if (redbag != null) {
			redbag.setValid(CommonConstant.INT_BOOLEAN_FALSE);
			save(redbag);
		}
	}

	/**
	 * 查找某种支付类型
	 * @param type 0首次登陆 1注册绑定 2预约支付 3周红包 4分享红包
	 */
	public List<SystemRedbag> queryByPayType(int type) {
		List<SystemRedbag> redbags = systemRedbagDao.queryListByTypeAndValidOrderByMoneyAsc(type, 1);
		return redbags;
	}

	/**
	 * 查询 指定ID范围 且 未使用、未过期 的红包列表
	 */
	public List<Map<String, Object>> queryByIdsAndUserId(String ids, Long userId) {
		String sql = "select ur.amount money from user_r_redbag ur, sys_t_redbag sr where ur.id in (" + ids
				+ ")    and sr.id = ur.redbag_id  and ur.user_id = " + userId
				+ " and ur.is_valid = 1 and date_add(  ur.create_date, interval sr.day day  ) >= now() and ur.is_valid=1 and ur.usable=1";
		return queryDao.queryMap(sql);
	}

	/**
	 * 查找有效的系统红包(包含有效和无效)
	 * @param id 系统红包id
	 */
	public SystemRedbag findOne(Long id) {
		return systemRedbagDao.findOne(id);
	}

	/**
	 * 查找有效的系统红包
	 * @param id 系统红包id
	 */
	public SystemRedbag findValidOne(Long id) {
		return systemRedbagDao.queryByIdAndValid(id, CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * 查询5分钟后开始的红包Id列表
	 */
	public List<Map<String, Object>> queryRecentRedbagIds() {
		String sql = SqlJoiner.join("select id from sys_t_redbag where type = 3",
				" and date_format( begin_time, '%Y-%m-%d %H:%i' ) = date_format( DATE_ADD(now(), INTERVAL 5 MINUTE), '%Y-%m-%d %H:%i' ) and is_valid = 1;");
		return queryDao.queryMap(sql);
	}

	/**
	 * 分页查询
	 */
	public Page<SystemRedbag> page(int page, Map<String, Object> params) {
		params.put("valid", CommonConstant.INT_BOOLEAN_TRUE);
		return systemRedbagDao.findAll(buildSpecification(params), buildPageRequest(page));
	}

	/**
	 * 分页查询条件
	 */
	private Specification<SystemRedbag> buildSpecification(final Map<String, Object> searchParams) {
		Specification<SystemRedbag> spec = new Specification<SystemRedbag>() {
			@Override
			public Predicate toPredicate(Root<SystemRedbag> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> ps = Lists.newArrayList();

				Set<String> keys = searchParams.keySet();
				for (String key : keys) {
					try {
						if (key.equals("beginTime")) {
							Predicate isReleasePredicate = cb.greaterThan(root.get(key).as(String.class),
									String.valueOf(searchParams.get(key)));
							ps.add(isReleasePredicate);
						} else if (key.equals("endTime")) {
							Predicate isReleasePredicate = cb.lessThan(root.get(key).as(String.class),
									String.valueOf(searchParams.get(key)));
							ps.add(isReleasePredicate);
						} else if (key.equals("type")) {
							Predicate isReleasePredicate = cb.equal(root.get(key).as(String.class),
									String.valueOf(searchParams.get(key)));
							ps.add(isReleasePredicate);

							// 查询周红包,且未设置时间范围时 默认只查未过期的周红包
							if (searchParams.get(key).equals(RedbagConstant.REDBAG_TYPE_WEEKLY.toString())
									&& !searchParams.containsKey("endTime")) {
								String nowDateStr = DateUtils.dateToString(new Date(), DateUtils.YYYY_MM_DD);
								Predicate endTimePredicate = cb.greaterThan(root.get("endTime").as(String.class),
										nowDateStr);
								ps.add(endTimePredicate);
							}
						} else {
							Predicate isReleasePredicate = cb.like(root.get(key).as(String.class),
									SqlJoiner.join("%", String.valueOf(searchParams.get(key)), "%"));
							ps.add(isReleasePredicate);
						}
					} catch (Exception e) {
						LOGGER.error("添加查询条件异常：", e);
					}
				}

				query.where(cb.and(ps.toArray(new Predicate[ps.size()])));
				return query.getRestriction();
			}
		};
		return spec;
	}

	/**
	 * 分页排序
	 */
	private PageRequest buildPageRequest(int pageNumber) {
		return new PageRequest(pageNumber - 1, PageUtils.ADMIN_DEFAULT_PAGE_SIZE, new Sort(Direction.DESC, "id"));
	}

	/**
	 * 根据类型查询第一条红包设置信息
	 */
	public Map<String, Object> findOneByType(int type) {
		String sql = SqlJoiner.join("SELECT id, money, `day`, `explain`, type, `restrict`", " FROM sys_t_redbag",
				" WHERE is_valid = 1 AND type = ", String.valueOf(type), " order by create_date desc LIMIT 0, 1");
		return queryDao.querySingleMap(sql);
	}

	/**
	 * 查询商品类型的系统红包ID（系统红包只维护一条type=5的商品类别红包）
	 */
	public long querySystemRedbagId(int type) {
		String sqlQuery = "select r.id from sys_t_redbag r where r.type=" + type + " limit 1";
		Map<String, Object> map = queryDao.querySingleMap(sqlQuery);
		return NumberUtils.toLong(map == null ? null : map.get("id") == null ? null : map.get("id").toString());
	}
	
	/**
	 * 查询悬赏令红包
	 */
	public long queryBountySystemRedbagId(int type,int quota) {
		String sqlQuery = "select r.id from sys_t_redbag r where r.money="+quota+" and r.type=" + type + " limit 1";
		Map<String, Object> map = queryDao.querySingleMap(sqlQuery);
		return NumberUtils.toLong(map == null ? null : map.get("id") == null ? null : map.get("id").toString());
	}

	public SystemRedbag findOndByTypeAndMoney(int type, int amount) {
		List<SystemRedbag> redbags = systemRedbagDao.findByTypeAndMoney(type, amount);
		if (CollectionUtils.isNotEmpty(redbags)) {
			return redbags.get(0);
		} else {
			return null;
		}
	}

	public SystemRedbag findByTypeAndValid(int type, int valid) {
		return systemRedbagDao.findByTypeAndValid(type, 1);
	}

	public List<SystemRedbag> findValidByTypeAndMoneys(int type, List<Integer> moneys) {
		List<SystemRedbag> redbags = systemRedbagDao.findByTypeAndMoneyInAndValid(type, moneys,
				CommonConstant.INT_BOOLEAN_TRUE);
		if (CollectionUtils.isEmpty(redbags)) {
			return null;
		}

		return redbags;
	}
}