package com.miqtech.master.service.system;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

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
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.common.SystemMerchantNoticeDao;
import com.miqtech.master.entity.common.SystemMerchantNotice;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;

/**
 * 系统通知(商户)service
 */
@Component
public class SystemMerchantNoticeService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemMerchantNoticeService.class);

	@Autowired
	private SystemMerchantNoticeDao systemMerchantNoticeDao;
	@Autowired
	private QueryDao queryDao;

	public SystemMerchantNotice findById(Long id) {
		return systemMerchantNoticeDao.findOne(id);
	}

	public SystemMerchantNotice save(SystemMerchantNotice notice) {
		return systemMerchantNoticeDao.save(notice);
	}

	public void delete(long id) {
		systemMerchantNoticeDao.delete(id);
	}

	/*
	 * 查询公告的地区code
	 */
	public List<Map<String, Object>> getAreaCodesByNoticeId(long noticeId) {
		String sqlQuery = "SELECT area_code areaCode FROM sys_r_notice_area WHERE notice_id = " + noticeId;
		return queryDao.queryMap(sqlQuery);
	}

	/**
	 * 后台管理：商户系统公告列表
	 */
	public List<Map<String, Object>> pageList(int page, Map<String, Object> params) {
		if (page < 1) {
			page = 1;
		}
		int rows = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		int start = (page - 1) * rows;
		if (null == params.get("valid")) {
			params.put("valid", CommonConstant.INT_BOOLEAN_TRUE);
		}
		String sqlQuery = "select i.id, i.title, i.content, i.is_valid valid, i.create_date createDate , ifnull(readCount,0) readCount";
		String sqlFrom = " from (select  id, title, content, create_date, is_valid from sys_t_merchant_notice  where is_valid=:valid";
		String userCount = "select count(1) from sys_t_merchant_notice where is_valid=:valid";

		if (null != params.get("beginDate")) {
			sqlFrom = SqlJoiner.join(sqlFrom, " and create_date >= :beginDate");
			userCount = SqlJoiner.join(userCount, " and create_date >= :beginDate");
		}
		if (null != params.get("endDate")) {
			sqlFrom = SqlJoiner.join(sqlFrom, " and create_date <= :endDate");
			userCount = SqlJoiner.join(userCount, " and create_date <= :endDate");
		}
		if (null != params.get("title")) {
			sqlFrom = SqlJoiner.join(sqlFrom, " and title like concat('%',:title,'%')");
			userCount = SqlJoiner.join(userCount, " and title like concat('%',:title,'%')");
		}
		Map<String, Object> userCountMap = queryDao.querySingleMap(userCount, params);
		if (null == params.get("no_limit")) {
			params.put("start", start);
			params.put("rows", rows);
			sqlFrom = SqlJoiner.join(sqlFrom, " order by id limit :start, :rows");
		} else {
			params.remove("no_limit");
		}
		sqlFrom = SqlJoiner.join(sqlFrom, ")i");

		sqlQuery = SqlJoiner.join(sqlQuery, sqlFrom,
				" left join (select count(1) readCount, notice_id from sys_r_notice_read group by notice_id)l on l.notice_id=i.id");
		sqlQuery = SqlJoiner.join(sqlQuery, " order by i.create_date desc, readCount desc");
		List<Map<String, Object>> list = queryDao.queryMap(sqlQuery, params);

		if (null != userCountMap && null != userCountMap.get("count(1)")) {
			params.put("total", userCountMap.get("count(1)"));
		} else {
			params.put("total", "0");
		}
		return list;
	}

	/**
	 * 分页查询条件
	 */
	private Specification<SystemMerchantNotice> buildSpecification(final Map<String, Object> searchParams) {
		Specification<SystemMerchantNotice> spec = new Specification<SystemMerchantNotice>() {
			@Override
			public Predicate toPredicate(Root<SystemMerchantNotice> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> ps = Lists.newArrayList();
				if (searchParams == null) {
					return null;
				}
				Set<String> keys = searchParams.keySet();
				for (String key : keys) {
					try {
						Predicate isReleasePredicate = cb.like(root.get(key).as(String.class),
								SqlJoiner.join("%", String.valueOf(searchParams.get(key)), "%"));
						ps.add(isReleasePredicate);
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
	 * 分页数据
	 */
	public Page<SystemMerchantNotice> page(int page, Map<String, Object> params) {
		PageRequest pageRequest = buildPageRequest(page);
		Specification<SystemMerchantNotice> spec = buildSpecification(params);
		return systemMerchantNoticeDao.findAll(spec, pageRequest);
	}

	/**
	 * 5条最新公告
	 */
	public List<Map<String, Object>> page5() {
		String sqlQuery = "select * from sys_t_merchant_notice order by create_date desc limit 5";
		return queryDao.queryMap(sqlQuery);
	}

	/**
	 * 5条最新公告,根据地区（省份）
	 */
	public List<Map<String, Object>> page5ByAreaCode(String areaCode) {
		String sqlQuery = SqlJoiner.join(
				"SELECT n.* FROM sys_t_merchant_notice n WHERE n.id IN ( SELECT notice_id FROM sys_r_notice_area WHERE area_code LIKE CONCAT(LEFT(",
				areaCode, ", 2), '%') OR area_code = '000000' ) ORDER BY n.create_date DESC LIMIT 5");
		return queryDao.queryMap(sqlQuery);
	}

}