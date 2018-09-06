package com.miqtech.master.service.system;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
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
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.SystemUserConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.common.SystemUserDao;
import com.miqtech.master.entity.common.SystemUser;
import com.miqtech.master.utils.EncodeUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

/**
 * 系统用户service
 */
@Component
public class SystemUserService {

	@Autowired
	private SystemUserDao systemUserDao;
	@Autowired
	private QueryDao queryDao;

	/**
	 * 用户登录
	 * @param name 用户名称
	 * @param password 密码
	 */
	public SystemUser findByUsernameAndPasswordAndUserTypes(String name, String password, List<Integer> userTypes) {
		password = EncodeUtils.base64Md5(password);
		List<SystemUser> users = systemUserDao.findByUsernameAndPasswordAndUserTypeIn(name, password, userTypes);
		if (CollectionUtils.isNotEmpty(users)) {
			return users.get(0);
		}
		return null;
	}

	/**查询区域管理员
	 * @param name
	 * @param password
	 * @param type
	 * @param min
	 * @param max
	 * @return
	 */
	public SystemUser findByUsernameAndPasswordAndUserTypeLessThan(String name, String password, int max) {
		password = EncodeUtils.base64Md5(password);
		return systemUserDao.findByUsernameAndPasswordAndValidAndUserTypeLessThan(name, password, 1, max);
	}

	private PageRequest buildPageRequest(int pageNumber) {
		return new PageRequest(pageNumber - 1, PageUtils.ADMIN_DEFAULT_PAGE_SIZE, new Sort(Direction.ASC, "id"));
	}

	public Page<SystemUser> page(int page, Map<String, Object> params) {
		PageRequest pageRequest = buildPageRequest(page);
		Specification<SystemUser> spec = buildSpecification(params);
		return systemUserDao.findAll(spec, pageRequest);
	}

	@SuppressWarnings({ "rawtypes" })
	private Specification<SystemUser> buildSpecification(final Map<String, Object> searchParams) {
		Specification<SystemUser> spec = new Specification<SystemUser>() {
			@Override
			public Predicate toPredicate(Root<SystemUser> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> ps = Lists.newArrayList();
				Object typeObj = searchParams.get("type");
				if (typeObj != null) {
					int type = NumberUtils.toInt(typeObj.toString());
					Path typePath = root.get("userType");
					if (type == -1) {// 管理系统中使用的用户:普通管理员、赛事约战子账号
						List<Predicate> userTypePredicate = Lists.newArrayList();
						userTypePredicate.add(cb.equal(typePath, SystemUserConstant.TYPE_ACTIVITY_ADMIN));
						userTypePredicate.add(cb.equal(typePath, SystemUserConstant.TYPE_NORMAL_ADMIN));
						userTypePredicate.add(cb.equal(typePath, SystemUserConstant.TYPE_AMUSE_APPEAL));
						userTypePredicate.add(cb.equal(typePath, SystemUserConstant.TYPE_AMUSE_ISSUE));
						userTypePredicate.add(cb.equal(typePath, SystemUserConstant.TYPE_AMUSE_VERIFY));
						ps.add(cb.or(userTypePredicate.toArray(new Predicate[ps.size()])));
					} else {
						Predicate typePredicate = cb.equal(typePath, type);
						ps.add(typePredicate);
					}
				}
				Object usernameObj = searchParams.get("username");
				if (usernameObj != null) {
					Path<String> usernamePath = root.get("username");
					Predicate usernamePredicate = cb.like(usernamePath, usernameObj.toString());
					ps.add(usernamePredicate);
				}
				query.where(cb.and(ps.toArray(new Predicate[ps.size()])));
				return query.getRestriction();
			}
		};
		return spec;
	}

	public PageVO nativePage(int page, Map<String, Object> searchParams) {
		String condition = " WHERE su.is_valid = 1";
		String totalCondition = condition;
		Map<String, Object> params = Maps.newHashMap();

		String username = MapUtils.getString(searchParams, "username");
		if (StringUtils.isNotBlank(username)) {
			String likeUsername = "%" + username + "%";
			params.put("username", likeUsername);
			condition = SqlJoiner.join(condition, " AND su.username LIKE :username");
			totalCondition = SqlJoiner.join(totalCondition, " AND su.username LIKE '", likeUsername, "'");
		}
		String type = MapUtils.getString(searchParams, "type");
		if (NumberUtils.isNumber(type)) {
			condition = SqlJoiner.join(condition, " AND su.user_type = ", type);
			totalCondition = SqlJoiner.join(condition, " AND su.user_type = ", type);
		}
		String roleId = MapUtils.getString(searchParams, "roleId");
		if (NumberUtils.isNumber(roleId)) {
			condition = SqlJoiner.join(condition, " AND ur.role_id = ", roleId);
			totalCondition = SqlJoiner.join(totalCondition, " AND ur.role_id = ", roleId);
		}
		String areaCode = MapUtils.getString(searchParams, "areaCode");
		if (NumberUtils.isNumber(areaCode)) {
			String likeAreaCode = areaCode.replaceAll("0", "") + "%";// 按省级别查询
			params.put("areaCode", likeAreaCode);
			condition = SqlJoiner.join(condition, " AND ua.area_code LIKE :areaCode");
			totalCondition = SqlJoiner.join(totalCondition, " AND ua.area_code LIKE '", likeAreaCode, "'");
		}

		String limit = "";
		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page > 0) {
			Integer startRow = (page - 1) * pageSize;
			limit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());
		}

		String sql = SqlJoiner.join("SELECT t.*, sa.name areaName, belongSa.name belongAreaName ",
				"FROM (SELECT su.id, su.username, su.realname, su.telephone, su.is_valid valid, ua.area_code areaCode, su.area_code belongAreaCode",
				" FROM sys_t_user su LEFT JOIN sys_r_user_role ur ON su.id = ur.sys_user_id AND ur.is_valid = 1",
				" LEFT JOIN sys_r_user_area ua ON su.id = ua.sys_user_id", condition, " GROUP BY su.id ORDER BY su.id",
				limit, ") t LEFT JOIN sys_t_area sa ON LEFT(sa.area_code, 2) = LEFT(t.areaCode, 2)",
				" LEFT JOIN sys_t_area belongSa ON LEFT(belongSa.area_code, 2) = LEFT(t.belongAreaCode, 2)",
				" GROUP BY t.id ORDER BY t.id");
		List<Map<String, Object>> list = queryDao.queryMap(sql, params);

		String totalSql = SqlJoiner.join(
				"SELECT COUNT(1) FROM (SELECT su.id, su.username, su.realname, su.telephone, su.is_valid valid",
				" FROM sys_t_user su LEFT JOIN sys_r_user_role ur ON su.id = ur.sys_user_id",
				" LEFT JOIN sys_r_user_area ua ON su.id = ua.sys_user_id", totalCondition,
				" GROUP BY su.id ORDER BY su.id) su");
		Number total = queryDao.query(totalSql);
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

	public SystemUser findById(Long id) {
		return systemUserDao.findOne(id);
	}

	public SystemUser findByUsername(String username) {
		List<SystemUser> users = systemUserDao.findByUsername(username);
		if (CollectionUtils.isNotEmpty(users)) {
			return users.get(0);
		}
		return null;
	}

	public SystemUser findByUsernameAndUserType(String username, Integer userType) {
		return systemUserDao.findByUsernameAndUserType(username, userType);
	}

	public SystemUser save(SystemUser user) {
		Date now = new Date();
		user.setUpdateDate(now);
		if (user.getId() == null) {
			user.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			user.setCreateDate(now);
		}

		return systemUserDao.save(user);
	}

	/**
	 * 根据用户类型和名称密码查找用户信息
	 * @param username 用户名
	 * @param password 密码
	 * @param userType 用户类型 0超级管理员 1普通管理员 2录入子系统管理员 3录入子系统审核人员 4录入子系统录入人员
	 */
	public SystemUser findByUsernameAndPasswordAndUserType(String username, String password, Integer userType) {
		if (userType == null) {
			userType = 1;
		}
		SystemUser user = systemUserDao.findByUsernameAndPasswordAndUserTypeAndValid(username,
				EncodeUtils.base64Md5(password), userType, CommonConstant.INT_BOOLEAN_TRUE);

		// 检查用户是否超管
		if (user == null) {
			user = systemUserDao.findByUsernameAndPasswordAndUserTypeAndValid(username, EncodeUtils.base64Md5(password),
					SystemUserConstant.TYPE_SUPER_ADMIN, CommonConstant.INT_BOOLEAN_TRUE);
		}

		return user;
	}

	public List<SystemUser> findByParentId(Long parentId) {
		return systemUserDao.findByParentIdAndValid(parentId, 1);
	}

	/**查询子账号
	 * @param parentId
	 * @return
	 */
	public List<Map<String, Object>> queryByParentId(Long parentId) {
		String sql = "select id,username,realname from sys_t_user where is_valid=1 and parent_id="
				+ String.valueOf(parentId);
		return queryDao.queryMap(sql);
	}

	public SystemUser findByUserTypeAndParentIdIsNull(Integer userType) {
		return systemUserDao.findByUserTypeAndParentIdIsNull(userType);
	}

	public void delete(Long id) {
		systemUserDao.delete(id);
	}

	/**邀请码系统查询用户
	 * @param realname
	 * @param username
	 * @param telephone
	 * @param areaCode
	 * @return
	 */
	public PageVO queryUser(String realname, String username, String telephone, String areaCode, Integer page,
			SystemUser user) {
		String sql = "";
		String realnameSql = "";
		String usernameSql = "";
		String telephoneSql = "";
		String areaCodeSql = "";
		if (StringUtils.isNotBlank(realname)) {
			realnameSql = SqlJoiner.join(" and realname like '%", realname, "%'");
		}
		if (StringUtils.isNotBlank(username)) {
			usernameSql = SqlJoiner.join(" and username like '%", username, "%'");
		}
		if (StringUtils.isNotBlank(telephone)) {
			telephoneSql = SqlJoiner.join(" and telephone like '%", telephone, "%'");
		}
		if (StringUtils.isNotBlank(areaCode)) {
			areaCodeSql = SqlJoiner.join(" and a.area_code like '", areaCode, "%'");
		}
		int pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page == null) {
			page = 1;
		}
		int start = (page - 1) * pageSize;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", start);
		params.put("pageSize", pageSize);
		String limitSql = " limit :start,:pageSize";
		sql = SqlJoiner.join(
				"select count(1) from sys_t_user a,sys_t_area b where user_type=11 and a.area_code=b.area_code and parent_id=",
				String.valueOf(user.getId()), realnameSql, usernameSql, telephoneSql, areaCodeSql);
		PageVO vo = new PageVO();
		Number totalCount = queryDao.query(sql);
		if (totalCount != null) {
			vo.setTotal(totalCount.intValue());
		}

		if (start + pageSize >= vo.getTotal()) {
			vo.setIsLast(1);
		}
		sql = SqlJoiner.join(
				"select a.id,a.realname,a.username,a.telephone,a.area_code,b.name from sys_t_user a,sys_t_area b where user_type=11 and a.area_code=b.area_code and parent_id=",
				String.valueOf(user.getId()), realnameSql, usernameSql, telephoneSql, areaCodeSql,
				" order by a.create_date desc", limitSql);
		vo.setList(queryDao.queryMap(sql, params));
		vo.setCurrentPage(page);
		return vo;
	}

	public void del(Long id) {
		systemUserDao.delete(id);
	}

	/**查询子账号数
	 * @param parentId
	 * @return
	 */
	public Integer querySubAccountNum(Long parentId) {
		String sql = "select count(1) from sys_t_user where is_valid=1 and parent_id=" + String.valueOf(parentId);
		Number totalCount = queryDao.query(sql);
		if (totalCount != null) {
			return totalCount.intValue();
		}
		return -1;
	}

	public void del(SystemUser user) {
		systemUserDao.delete(user);
	}

	/*
	 * 查商城用户帐号信息列表
	 */
	public PageVO mallUserPage(int page, Map<String, Object> params) {
		int pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		page = page < 1 ? 1 : page;
		int startRow = (page - 1) * pageSize;
		Integer adminType = SystemUserConstant.TYPE_MALL_ADMIN;
		Integer verifyType = SystemUserConstant.TYPE_MALL_VERIFY;
		Integer grantType = SystemUserConstant.TYPE_MALL_GRANT;

		String condition = SqlJoiner.join(" WHERE is_valid=1 AND user_type IN(", adminType.toString(), ", ",
				verifyType.toString(), ", ", grantType.toString(), ")");
		if (null != params.get("username")) {
			condition = SqlJoiner.join(condition, " AND username LIKE '%", params.get("username").toString(), "%'");
		}
		String sql = SqlJoiner.join("SELECT id, username, realname, create_date, user_type FROM sys_t_user", condition,
				" LIMIT ", String.valueOf(startRow), ", ", String.valueOf(pageSize));

		String sqlTotal = SqlJoiner.join("SELECT COUNT(1) FROM sys_t_user", condition);
		Number totalNum = queryDao.query(sqlTotal);
		int total = 0;
		if (totalNum != null) {
			total = totalNum.intValue();
		}
		List<Map<String, Object>> list = null;
		if (total > 0) {
			list = queryDao.queryMap(sql);
		} else {
			list = Lists.newArrayList();
		}

		PageVO vo = new PageVO();
		vo.setList(list);
		vo.setTotal(total);
		return vo;
	}

	/**
	 * 查询某种类型的有效用户
	 */
	public List<SystemUser> findValidByUserType(Integer userType) {
		return systemUserDao.findByUserTypeAndValid(userType, CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * 查询商城用户
	 */
	public List<Map<String, Object>> findMallUsers() {
		String sql = SqlJoiner.join("SELECT id, username FROM sys_t_user WHERE is_valid=1 AND user_type in (",
				SystemUserConstant.TYPE_MALL_ADMIN.toString(), ",", SystemUserConstant.TYPE_MALL_VERIFY.toString(), ",",
				SystemUserConstant.TYPE_MALL_GRANT.toString(), ")");
		return queryDao.queryMap(sql);
	}

	/**
	 * 重置娱乐赛操作用户的处理数量
	 */
	public void resetAmuseAdminOperate(Integer userType) {
		if (SystemUserConstant.TYPE_AMUSE_VERIFY.equals(userType)) {
			String updateSql = "UPDATE amuse_t_verify SET bak_update_user_id = update_user_id, update_user_id = NULL WHERE update_user_id IS NOT NULL";
			queryDao.update(updateSql);
		} else if (SystemUserConstant.TYPE_AMUSE_ISSUE.equals(userType)) {
			String updateSql = "UPDATE amuse_t_verify SET bak_claim_user_id = claim_user_id, claim_user_id = NULL WHERE claim_user_id IS NOT NULL";
			queryDao.update(updateSql);
		} else if (SystemUserConstant.TYPE_AMUSE_APPEAL.equals(userType)) {
			String updateSql = "UPDATE amuse_t_appeal SET bak_update_user_id = update_user_id, update_user_id = NULL WHERE update_user_id IS NOT NULL";
			queryDao.update(updateSql);
		}
	}

	public List<SystemUser> findValidByUserTypeIn(List<Integer> userTypes) {
		return systemUserDao.findByUserTypeInAndValid(userTypes, CommonConstant.INT_BOOLEAN_TRUE);
	}
}