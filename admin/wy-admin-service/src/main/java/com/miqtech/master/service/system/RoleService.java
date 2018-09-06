package com.miqtech.master.service.system;

import java.util.Date;
import java.util.HashMap;
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
import com.miqtech.master.dao.common.RoleDao;
import com.miqtech.master.entity.common.Role;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;

/**
 * 角色service
 */
@Component
public class RoleService {

	private static final Logger LOGGER = LoggerFactory.getLogger(RoleService.class);

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private RoleDao roleDao;

	public Role save(Role role) {
		if (role != null) {
			Date now = new Date();
			role.setUpdateDate(now);
			if (role.getId() == null) {
				role.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				role.setCreateDate(now);
			}
			roleDao.save(role);
		}
		return role;
	}

	public void delete(Long id) {
		Role role = findById(id);
		if (role != null) {
			role.setValid(CommonConstant.INT_BOOLEAN_FALSE);
			save(role);
		}
	}

	public List<Role> findAll() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("valid", CommonConstant.INT_BOOLEAN_TRUE);

		return roleDao.findAll(buildSpecification(params));
	}

	public Role findById(Long id) {
		return roleDao.findOne(id);
	}

	/**
	 * 获取用户的所有角色ID
	 */
	public String getUserRoles(Long adminId) {
		String sql = SqlJoiner
				.join("SELECT GROUP_CONCAT(ur.role_id) FROM sys_r_user_role ur LEFT JOIN sys_t_role r ON ur.role_id = r.id AND r.is_valid = 1 WHERE ur.is_valid = 1 AND ur.sys_user_id = ",
						adminId.toString());
		return queryDao.query(sql);
	}

	/**
	 * 分页查询
	 */
	public Page<Role> page(int page, Map<String, Object> params) {
		params.put("valid", CommonConstant.INT_BOOLEAN_TRUE);
		return roleDao.findAll(buildSpecification(params), buildPageRequest(page));
	}

	/**
	 * 分页排序
	 */
	private PageRequest buildPageRequest(int pageNumber) {
		return new PageRequest(pageNumber - 1, PageUtils.ADMIN_DEFAULT_PAGE_SIZE, new Sort(Direction.ASC, "id"));
	}

	/**
	 * 分页查询条件
	 */
	private Specification<Role> buildSpecification(final Map<String, Object> searchParams) {
		Specification<Role> spec = new Specification<Role>() {
			@Override
			public Predicate toPredicate(Root<Role> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> ps = Lists.newArrayList();

				if (searchParams != null) {
					Set<String> keys = searchParams.keySet();
					for (String key : keys) {
						try {
							Object value = searchParams.get(key);
							if (value != null) {
								Predicate isReleasePredicate = cb.like(root.get(key).as(String.class),
										SqlJoiner.join("%", String.valueOf(searchParams.get(key)), "%"));
								ps.add(isReleasePredicate);
							}
						} catch (Exception e) {
							LOGGER.error("添加查询条件异常：", e);
						}
					}
				}

				query.where(cb.and(ps.toArray(new Predicate[ps.size()])));
				return query.getRestriction();
			}
		};
		return spec;
	}
}