package com.miqtech.master.service.mall;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
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
import com.miqtech.master.dao.mall.MallAdvertiseDao;
import com.miqtech.master.entity.mall.MallAdvertise;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;

/**
 * 商城广告service
 */
@Component
public class MallAdvertiseService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MallAdvertiseService.class);
	@Autowired
	private MallAdvertiseDao mallAdvertiseDao;
	@Autowired
	private QueryDao queryDao;

	/**
	 * 保存商城广告
	 */
	public void save(MallAdvertise mallAdvertise) {
		mallAdvertiseDao.save(mallAdvertise);
	}

	/**
	 * 广告列表(有效的)
	 * deviceType，设备类型：0-全部；1-IOS；2-Android；3...
	 */
	public List<Map<String, Object>> list(int deviceTpe) {
		String deviceSql = StringUtils.EMPTY;
		if (deviceTpe != 0) {
			deviceSql = " and (device_type is null or device_type in(0," + deviceTpe + "))";
		}
		String sqlQuery = SqlJoiner.join(
				"select id, banner, type, target_id targetId, url from mall_t_advertise where is_valid=1", deviceSql);
		return queryDao.queryMap(sqlQuery);
	}

	/**
	 * 根据id查广告实体
	 */
	public MallAdvertise getAdvertiseById(long id) {
		return mallAdvertiseDao.findOne(id);
	}

	/**
	 * 删除/恢复
	 */
	public void updateValidById(long id, int valid) {
		MallAdvertise mallAdvertise = getAdvertiseById(id);
		mallAdvertise.setValid(valid);
		mallAdvertise.setUpdateDate(new Date());
		save(mallAdvertise);
	}

	/**
	 * ##后台管理##:查询商城顶部广告列表，分页
	 */
	public Page<MallAdvertise> page(int page, Map<String, Object> params) {
		if (null == params.get("valid")) {
			params.put("valid", CommonConstant.INT_BOOLEAN_TRUE);
		}
		return mallAdvertiseDao.findAll(buildSpecification(params), buildPageRequest(page));
	}

	/**
	 * 分页查询条件
	 */
	private Specification<MallAdvertise> buildSpecification(final Map<String, Object> searchParams) {
		Specification<MallAdvertise> spec = new Specification<MallAdvertise>() {
			@Override
			public Predicate toPredicate(Root<MallAdvertise> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> ps = Lists.newArrayList();

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
}
