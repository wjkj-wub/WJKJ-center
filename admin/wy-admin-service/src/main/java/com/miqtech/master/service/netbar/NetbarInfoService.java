package com.miqtech.master.service.netbar;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.common.SystemAreaDao;
import com.miqtech.master.dao.common.SystemUserDao;
import com.miqtech.master.dao.netbar.NetbarInfoDao;
import com.miqtech.master.dao.user.UserFavorDao;
import com.miqtech.master.entity.Pager;
import com.miqtech.master.entity.common.SystemArea;
import com.miqtech.master.entity.common.SystemUser;
import com.miqtech.master.entity.netbar.NetbarInfo;
import com.miqtech.master.entity.user.UserFavor;
import com.miqtech.master.service.amuse.AmuseActivityInfoService;
import com.miqtech.master.service.base.BaseService;
import com.miqtech.master.service.system.SystemAreaService;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

/**
 * 网吧信息Service
 */
@Component
public class NetbarInfoService extends BaseService {
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private NetbarInfoDao netbarInfoDao;
	@Autowired
	private UserFavorDao userFavorDao;
	@Autowired
	private SystemAreaService systemAreaService;
	@Autowired
	private SystemUserDao systemUserDao;
	@Autowired
	private JedisConnectionFactory redisConnectionFactory;
	@Autowired
	private SystemAreaDao areaDao;

	private PageRequest buildPageRequest(int pageNumber) {
		return new PageRequest(pageNumber - 1, PageUtils.ADMIN_DEFAULT_PAGE_SIZE, new Sort(Direction.DESC, "id"));
	}

	@SuppressWarnings({ "rawtypes" })
	private Specification<NetbarInfo> buildSpecification(final Map<String, Object> searchParams) {
		Specification<NetbarInfo> spec = new Specification<NetbarInfo>() {
			@Override
			public Predicate toPredicate(Root<NetbarInfo> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				int isRelease = NumberUtils.toInt(searchParams.get("isRelease").toString());
				List<Predicate> ps = Lists.newArrayList();

				Path isReleasePath = root.get("isRelease");
				Predicate isReleasePredicate = cb.equal(isReleasePath, isRelease);
				ps.add(isReleasePredicate);

				Path validPath = root.get("valid");
				Predicate validPredicate = cb.equal(validPath, 1);// 有效
				ps.add(validPredicate);
				query.where(cb.and(ps.toArray(new Predicate[ps.size()])));
				return query.getRestriction();
			}
		};
		return spec;
	}

	/**
	 * 网吧分页数据查询功能
	 */
	public Page<NetbarInfo> page(int page, Map<String, Object> params) {
		PageRequest pageRequest = buildPageRequest(page);
		Specification<NetbarInfo> spec = buildSpecification(params);
		return netbarInfoDao.findAll(spec, pageRequest);
	}

	/**
	 * 查找某个网吧实体数据
	 */
	public NetbarInfo findById(Long netbarId) {
		if (netbarId == null) {
			return null;
		}
		return netbarInfoDao.findOne(netbarId);
	}

	/**
	 * 保存网吧信息
	 */
	public NetbarInfo save(NetbarInfo netbar) {
		return netbarInfoDao.save(netbar);
	}

	/**
	 * 保存更新网吧信息
	 */
	public NetbarInfo saveOrUpdate(NetbarInfo netbar) {
		if (null != netbar) {
			Date now = new Date();
			netbar.setUpdateDate(now);
			if (null != netbar.getId()) {
				NetbarInfo old = findById(netbar.getId());
				if (null != old) {
					netbar = BeanUtils.updateBean(old, netbar);
				}
			} else {
				netbar.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				netbar.setCreateDate(now);
			}
			return netbarInfoDao.save(netbar);
		}
		return netbar;
	}

	/**
	 * 根据邀请码查找网吧列表信息
	 */
	public List<NetbarInfo> findByInvitationCode(String invitationCode, int valid) {
		return netbarInfoDao.findByInvitationCodeAndValid(invitationCode, valid);
	}

	/**
	 * 查找用户预定或者支付过的网吧
	 */
	private List<Map<String, Object>> findReservedOrPayedNetbars(String longitude, String latitude, Long userId,
			int page, int pageSize) {
		int start = (page - 1) * pageSize;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", userId);
		double longitudeDouble = NumberUtils.toDouble(longitude);
		double latitudeDouble = NumberUtils.toDouble(latitude);
		String calcDistince = "";
		String orderByDistince = "";
		if (longitudeDouble > 0 && latitudeDouble > 0) {
			params.put("longitude", longitudeDouble);
			params.put("latitude", latitudeDouble);
			calcDistince = " calc_distance (longitude, :longitude, latitude, :latitude) distance,";
			orderByDistince = " order by calc_distance (longitude, :longitude, latitude, :latitude) asc ";
		}
		params.put("start", start);
		params.put("pageSize", pageSize);
		String sql = SqlJoiner.join("select a.*,",
				" case when ( rebate > 0 and rebate <= 90 and now() >= rebate_start_date and now() <= rebate_end_date ) or (a.discount_info is not null and length(trim(a.discount_info)) >= 1 ) then 1 else 0 end has_rebate, ",
				" case when b.is_valid = 1 then 1 else 0 end is_order, ",
				" case when count(m.id) > 1 then 1 else 0 end is_hot ",
				" from ( select a.longitude, a.latitude, a.id, a.name netbar_name, a.address, a.price, a.price_per_hour, a.icon, a.is_recommend, ",
				calcDistince, " a.rebate, a.rebate_start_date, a.rebate_end_date, a.discount_info",
				" from netbar_t_info a ", " where a.is_release = 1 and a.is_valid = 1 ",
				" and a.id in (select distinct netbar_id from netbar_r_order where user_id = :userId union select distinct netbar_id from netbar_r_reservation where user_id = :userId)",
				orderByDistince, " limit :start, :pageSize) a", " left join netbar_t_merchant b on a.id =b.netbar_id ",
				" left join activity_t_matches m on m.netbar_id = a.id and m.is_start = 0 and m.begin_time > now()",
				" group by a.id");
		return queryDao.queryMap(sql, params);
	}

	/**
	 * 查找附近的网吧
	 * 
	 * @param areaCode
	 *            地区code
	 * @param longitude
	 * @param latitude
	 * @param page
	 *            当前页数
	 * @param pageSize
	 *            每页数量
	 * @param excludeIds
	 *            排除重复的网吧id
	 */
	private List<Map<String, Object>> findNearestNetbars(String areaCode, String longitude, String latitude, int page,
			int pageSize, List<Long> excludeIds) {
		Map<String, Object> params = new HashMap<String, Object>();
		int start = (page - 1) * pageSize;
		double longitudeDouble = NumberUtils.toDouble(longitude);
		double latitudeDouble = NumberUtils.toDouble(latitude);
		String calcDistince = "";
		String orderByDistince = "";
		if (longitudeDouble > 0 && latitudeDouble > 0) {
			params.put("longitude", longitudeDouble);
			params.put("latitude", latitudeDouble);
			calcDistince = " calc_distance (longitude, :longitude, latitude, :latitude) distance,";
			orderByDistince = " order by calc_distance (longitude, :longitude, latitude, :latitude) asc ";
		}
		params.put("start", start);
		String andAreaCodeSql = StringUtils.EMPTY;

		if (StringUtils.isNotBlank(areaCode)) {
			if (StringUtils.endsWith(areaCode, "00")) {
				areaCode = StringUtils.substring(areaCode, 0, 4);
				andAreaCodeSql = "and substr(area_code,1,4) = " + areaCode;
			} else {
				andAreaCodeSql = "and area_code = " + areaCode;
			}
		}
		params.put("pageSize", pageSize);
		String andExcludeIdSql = StringUtils.EMPTY;
		if (CollectionUtils.isNotEmpty(excludeIds)) {
			params.put("excludeIds", excludeIds);
			andExcludeIdSql = " and a.id not in (:excludeIds) ";
		}

		String querySql = SqlJoiner.join("select a.*,",
				" case when ( rebate > 0 and rebate <= 90 and now() >= rebate_start_date and now() <= rebate_end_date ) or (a.discount_info is not null and length(trim(a.discount_info)) >= 1 ) then 1 else 0 end has_rebate, ",
				" case when b.is_valid = 1 then 1 else 0 end is_order, ",
				" case when count(m.id) > 1 then 1 else 0 end is_hot ",
				" from ( select a.longitude, a.latitude, a.id, a.name netbar_name, a.address, a.price, a.price_per_hour, a.icon,a.is_recommend, ",
				calcDistince, " a.rebate, a.rebate_start_date, a.rebate_end_date, a.discount_info",
				" from netbar_t_info a ",
				" where a.is_release = 1 and a.is_valid = 1 and longitude is not null and latitude is not null ",
				andExcludeIdSql, andAreaCodeSql, orderByDistince, " limit :start,:pageSize) a",
				" left join netbar_t_merchant b on a.id = b.netbar_id ",
				" left join activity_t_matches m on m.netbar_id = a.id and m.is_start = 0 and m.begin_time > now() ",
				" group by a.id ");
		return queryDao.queryMap(querySql, params);
	}

	/**
	 * 首页大师推荐,最多显示10条数据,只显示可支付的,排序依次(有经纬度时)距离,(无经纬度时)约战数.
	 */
	public List<Map<String, Object>> findRecommendNetbars(String areaCode, String longitude, String latitude, int page,
			int pageSize) {
		Map<String, Object> params = new HashMap<String, Object>();
		int start = (page - 1) * pageSize;
		double longitudeDouble = NumberUtils.toDouble(longitude);
		double latitudeDouble = NumberUtils.toDouble(latitude);
		String calcDistince = "";
		String orderBy = "";
		String limitDistanceSql = "";
		if (longitudeDouble > 0 && latitudeDouble > 0) {
			params.put("longitude", longitudeDouble);
			params.put("latitude", latitudeDouble);
			calcDistince = " calc_distance (longitude, :longitude, latitude, :latitude) distance,";
			orderBy = " order by calc_distance (longitude, :longitude, latitude, :latitude) asc";
			limitDistanceSql = SqlJoiner.join(" and ABS(longitude-", longitude, ")<=1 and ABS(latitude-", latitude,
					")<=1 ");
		} else {
			return Lists.newArrayList();
		}
		params.put("start", start);
		String andAreaCodeSql = StringUtils.EMPTY;

		if (StringUtils.isNotBlank(areaCode)) {
			SystemArea systemArea = systemAreaService.findByCode(areaCode);
			if (systemArea != null && systemArea.getValid() == 1) {
				if (StringUtils.endsWith(areaCode, "00")) {
					areaCode = StringUtils.substring(areaCode, 0, 4);
					andAreaCodeSql = "and substr(area_code,1,4) = " + areaCode;
				} else {
					andAreaCodeSql = "and area_code = " + areaCode;
				}
			}
		}
		params.put("pageSize", pageSize);
		String querySql = SqlJoiner.join(
				" select a.longitude, a.latitude, a.id, a.name netbar_name, a.address, a.price, a.price_per_hour, a.icon,a.is_recommend,1 is_order,",
				calcDistince,
				" 	 case when ( rebate > 0 and rebate <= 90 and now() >= rebate_start_date and now() <= rebate_end_date ) or (a.discount_info is not null and length(trim(a.discount_info)) >= 1 ) then 1 else 0 end has_rebate, ",
				" 	 if((hot is null or hot=0),0,1) is_hot,if(g.netbar_id is null and i.netbar_id is null,0,1) is_activity,if(h.netbar_id is null,0,1) is_benefit ",
				" from      (netbar_t_info a,netbar_t_merchant b) ",
				"left join(select a.netbar_id from netbar_resource_order a,netbar_resource_commodity_property b,netbar_resource_commodity c,netbar_commodity_category d where a.is_valid=1 and a.status=3 and a.property_id=b.id and date_format(b.settl_date, '%Y-%m-%d')=date_format(now(), '%Y-%m-%d') and a.commodity_id=c.id and c.category_id=d.id and d.is_show_app=1)g on a.id=g.netbar_id ",
				" LEFT JOIN ( SELECT a.netbar_id FROM netbar_resource_order a, netbar_resource_commodity_property b, netbar_resource_commodity c, netbar_commodity_category d WHERE a. STATUS = 3 AND a.is_valid = 1 AND a.property_id = b.id AND a.commodity_id = c.id AND c.category_id = d.id AND d. NAME = '增值券' AND b.validity IS NOT NULL AND now() > date_format(a.create_date, '%Y-%m-%d') AND now() < date_format( date_add( a.create_date, INTERVAL 1 + b.validity DAY ), '%Y-%m-%d' )) h ON a.id = h.netbar_id ",
				" left join (select netbar_id from netbar_t_recharge_activity a where  a.start_status=1 and a.is_valid=1)i on a.id=i.netbar_id ",
				" where  a.is_release = 1 and a.is_valid = 1  and a.id = b.netbar_id and b.is_valid=1 and longitude is not null and latitude is not null ",
				limitDistanceSql, andAreaCodeSql, " group by a.id ", orderBy, "  limit :start,:pageSize  ");
		return queryDao.queryMap(querySql, params);
	}

	/**
	 * 查找有约战的网吧
	 */
	private List<Map<String, Object>> findByMatch(String longitude, String latitude, int page, int pageSize,
			List<Long> excludeIds) {
		Map<String, Object> params = new HashMap<String, Object>();
		int start = (page - 1) * pageSize;
		double longitudeDouble = NumberUtils.toDouble(longitude);
		double latitudeDouble = NumberUtils.toDouble(latitude);
		String calcDistince = "";
		if (longitudeDouble > 0 && latitudeDouble > 0) {
			params.put("longitude", longitudeDouble);
			params.put("latitude", latitudeDouble);
			calcDistince = " calc_distance (longitude, :longitude, latitude, :latitude) distance,";
		}
		params.put("start", start);
		params.put("pageSize", pageSize);
		String excludeIdSql = StringUtils.EMPTY;
		if (CollectionUtils.isNotEmpty(excludeIds)) {
			params.put("excludeIds", excludeIds);
			excludeIdSql = " and a.id not in (:excludeIds) ";
		}
		String sql = SqlJoiner.join(
				" select a.longitude, a.latitude, a.id, a.name netbar_name, a.address, a.price, a.price_per_hour, a.icon,a.is_recommend ,",
				calcDistince,
				" 	case when ( rebate > 0 and rebate <= 90 and now() >= rebate_start_date and now() <= rebate_end_date ) or (a.discount_info is not null and length(trim(a.discount_info)) >= 1 ) then 1 else 0 end has_rebate, ",
				" 	case when b.is_valid = 1 then 1 else 0 end is_order, ",
				" 	case when count(m.id) > 1 then 1 else 0 end is_hot ", " from netbar_t_info a ",
				" 	left join netbar_t_merchant b on a.id = b.netbar_id ",
				" 	left join activity_t_matches m on m.netbar_id = a.id and m.is_start = 0 and m.begin_time > now()",
				" where a.is_release = 1 and a.is_valid = 1 ", excludeIdSql,
				" group by a.id order by count(m.id) desc limit :start, :pageSize ");
		return queryDao.queryMap(sql, params);
	}

	/**
	 * 猜你喜欢
	 */
	public List<Map<String, Object>> findRecommendNetbars(Long userId, String longitude, String latitude,
			String areaCode) {
		List<Map<String, Object>> result = Lists.newArrayList();
		List<Long> excludeIds = Lists.newArrayList();//
		// 记录已经查找到的网吧id,用于后续sql排除重复网吧
		List<Map<String, Object>> orderedList = null;
		// 最近预订或支付的网吧
		int orderedNumber = 0;
		if (userId.longValue() > 0) {
			orderedList = findReservedOrPayedNetbars(longitude, latitude, userId, 1, 5);
			orderedNumber = orderedList.size();
			for (Map<String, Object> netbar : orderedList) {
				excludeIds.add(NumberUtils.toLong(netbar.get("id").toString()));
			}
			result.addAll(orderedList);
		}
		// 距离最近的网吧
		int nearestNumber = 10;
		if (CollectionUtils.isNotEmpty(orderedList)) {
			nearestNumber = 10 - orderedNumber;
		}
		List<Map<String, Object>> nearestList = findNearestNetbars(areaCode, longitude, latitude, 1, nearestNumber,
				excludeIds);
		// 正在约战数最多的网吧
		int matchNumber = 15 - orderedNumber;
		if (CollectionUtils.isNotEmpty(nearestList)) {
			matchNumber = matchNumber - nearestList.size();
			for (Map<String, Object> netbar : nearestList) {
				excludeIds.add(NumberUtils.toLong(netbar.get("id").toString()));
			}
			result.addAll(nearestList);
		}
		List<Map<String, Object>> matchList = findByMatch(longitude, latitude, 1, matchNumber, excludeIds);
		if (CollectionUtils.isNotEmpty(matchList)) {
			result.addAll(matchList);
		}
		return result;

	}

	/**
	 * 查询网吧总数(条件同findAllNetbars)
	 * 
	 * @return
	 */
	public int getTotal() {
		String sqlTotal = "select count(1) from netbar_t_info where is_release=1 and is_valid=1 ";
		Number total = (Number) queryDao.query(sqlTotal);
		return total.intValue();
	}

	/**
	 * 查所有有效网吧id，name
	 */
	public List<Map<String, Object>> findAllNetbarIdandName() {
		String sqlQuery = "select id,name netbarName from netbar_t_info where is_valid=1";
		return queryDao.queryMap(sqlQuery);
	}

	/**
	 * 查指定地区（省份）有效网吧id，name
	 */
	public List<Map<String, Object>> findAllNetbarIdandNameByAreaCode(String areaCode) {
		String sqlQuery = SqlJoiner.join(
				"select id,name netbarName from netbar_t_info where is_valid=1 and area_code like CONCAT(LEFT(",
				areaCode, ",2),'%')");
		return queryDao.queryMap(sqlQuery);
	}

	/**
	 * 所有网吧列表
	 * 
	 * @param type
	 *            type=1按距离排序type=2按是否推荐排序type=3按约战数(即热度)排序
	 *            按距离排序作为次级的排序条件,若无地理位置,用网吧权重替代
	 */
	public PageVO findAllNetbars(String longitude, String latitude, int page, int pageSize, String areaCode, int type) {
		String andAreaCodeCountSql = StringUtils.EMPTY;
		String limitDistanceSql = "";
		if (StringUtils.isNotBlank(areaCode)) {
			if (StringUtils.endsWith(areaCode, "00")) {
				areaCode = StringUtils.substring(areaCode, 0, 4);
				andAreaCodeCountSql = " and substr(area_code,1,4) = " + areaCode;
			} else {
				andAreaCodeCountSql = " and area_code = " + areaCode;
			}
		} else {
			double longitudeDouble = NumberUtils.toDouble(longitude);
			double latitudeDouble = NumberUtils.toDouble(latitude);
			if (longitudeDouble > 0 && latitudeDouble > 0) {
				limitDistanceSql = SqlJoiner.join(" and ABS(longitude-", longitude, ")<=1 and ABS(latitude-", latitude,
						")<=1 ");
			}
		}
		String countSql = "select count(1) from netbar_t_info where is_release=1 and is_valid=1 " + andAreaCodeCountSql;
		Number totalCount = queryDao.query(countSql);
		if (null == totalCount || totalCount.intValue() <= 0) {
			return new PageVO(new ArrayList<Map<String, Object>>());
		}
		PageVO vo = new PageVO();
		if (page * pageSize >= totalCount.intValue()) {
			vo.setIsLast(1);
		}
		Map<String, Object> params = Maps.newHashMap();
		int start = (page - 1) * pageSize;
		double longitudeDouble = NumberUtils.toDouble(longitude);
		double latitudeDouble = NumberUtils.toDouble(latitude);
		String calcDistince = " calc_distance (longitude, :longitude, latitude, :latitude) distance,";
		String orderBy = "";
		params.put("start", start);
		params.put("pageSize", pageSize);
		params.put("longitude", longitudeDouble);
		params.put("latitude", latitudeDouble);
		String andAreaCodeSql = andAreaCodeCountSql;
		// 排序条件
		if (type == 0) {
			type = 1;
		}
		orderBy = getOrderBy(params, longitudeDouble, latitudeDouble, type, areaCode);
		String sql = SqlJoiner.join(
				" select a.longitude, a.latitude, a.id, a.name netbar_name, a.address, a.price, a.price_per_hour, a.icon,",
				calcDistince,
				" 	case when ( rebate > 0 and rebate <= 90 and now() >= rebate_start_date and now() <= rebate_end_date ) or (a.discount_info is not null and length(trim(a.discount_info)) >= 1 ) then 1 else 0 end has_rebate, ",
				" 	case when b.is_valid = 1 then 1 else 0 end is_order, ", " 	a.is_recommend, ",
				" 	if((hot is null or hot=0),0,1) is_hot,if(g.netbar_id is null and i.netbar_id is null,0,1) is_activity,if(h.netbar_id is null,0,1) is_benefit ",
				" from netbar_t_info a ", " 	left join netbar_t_merchant b on a.id = b.netbar_id and b.is_valid=1",
				" 	left join activity_t_matches m on m.netbar_id = a.id and m.is_start = 0 and m.begin_time > now()",
				" left join(select a.netbar_id from netbar_resource_order a,netbar_resource_commodity_property b,netbar_resource_commodity c,netbar_commodity_category d where a.is_valid=1 and a.status=3 and a.property_id=b.id and date_format(b.settl_date, '%Y-%m-%d')=date_format(now(), '%Y-%m-%d') and a.commodity_id=c.id and c.category_id=d.id and d.is_show_app=1)g on a.id=g.netbar_id ",
				" LEFT JOIN ( SELECT a.netbar_id FROM netbar_resource_order a, netbar_resource_commodity_property b, netbar_resource_commodity c, netbar_commodity_category d WHERE a. STATUS = 3 AND a.is_valid = 1 AND a.property_id = b.id AND a.commodity_id = c.id AND c.category_id = d.id AND d. NAME = '增值券' AND b.validity IS NOT NULL AND now() > date_format(a.create_date, '%Y-%m-%d') AND now() < date_format( date_add( a.create_date, INTERVAL 1 + b.validity DAY ), '%Y-%m-%d' )) h ON a.id = h.netbar_id ",
				" left join (select netbar_id from netbar_t_recharge_activity a where  a.start_status=1 and a.is_valid=1)i on a.id=i.netbar_id ",
				" where a.is_release = 1 and a.is_valid = 1 and longitude is not null and latitude is not null",
				andAreaCodeSql, limitDistanceSql, " group by a.id ", orderBy, " limit :start, :pageSize ");
		List<Map<String, Object>> result = queryDao.queryMap(sql, params);
		if (CollectionUtils.isNotEmpty(result)) {
			vo.setList(result);
		} else {
			vo.setList(new ArrayList<Map<String, Object>>());
		}
		return vo;
	}

	/**
	 * 所有网吧列表v3.1.2
	 * 
	 * @param longitude
	 * @param latitude
	 * @param page
	 * @param pageSize
	 * @param areaCode
	 * @param type
	 * @return
	 */
	public Map<String, Object> findAllNetbars(Double longitude, Double latitude, Integer page, Integer pageSize,
			String areaCode, Integer type) {
		Map<String, Object> result = new HashMap<String, Object>();
		PageVO vo = new PageVO();
		String distanceSql = "";
		String activitySql = "";
		String activityJoinSql = "";
		String orderBy = " order by distance";
		String appendDistance = "";
		Map<String, Object> params = new HashMap<String, Object>();
		if (page == null) {
			page = 1;
		}
		if (pageSize == null) {
			pageSize = PageUtils.API_DEFAULT_PAGE_SIZE;
		}
		int start = (page - 1) * pageSize;
		params.put("start", start);
		params.put("pageSize", pageSize);
		String totalSql = SqlJoiner.join(
				"SELECT count(1) FROM netbar_t_info a WHERE a.is_release = 1 AND a.is_valid = 1 AND longitude IS NOT NULL AND latitude IS NOT NULL AND area_code like '",
				areaCode, "%'");
		if (longitude != null && latitude != null) {
			distanceSql = ",calc_distance (longitude, :longitude, latitude, :latitude) distance ";
			params.put("longitude", longitude);
			params.put("latitude", latitude);
			appendDistance = ",distance";
		} else if (type == 1) {// 没有经纬度不能按距离排序,按热度排序
			type = 2;
		}
		if (type == 2) {
			orderBy = SqlJoiner.join(" order by hot desc", appendDistance);
		} else if (type == 3) {
			activitySql = ",IFNULL(battle_num,0)*0.3+IFNULL(amuse_num,0)*0.8+IFNULL(service_num,0)*1.1 activity_value";
			activityJoinSql = " LEFT JOIN ( SELECT count(netbar_id) battle_num, netbar_id FROM activity_t_matches WHERE is_start = 0 AND begin_time > now() GROUP BY netbar_id ) c ON a.id = c.netbar_id LEFT JOIN ( SELECT count(netbar_id) amuse_num, netbar_id FROM amuse_t_activity WHERE type = 3 AND is_release = 1 AND start_date < now() AND now() < end_date AND is_valid = 1 AND state = 2 GROUP BY netbar_id ) d ON a.id = d.netbar_id LEFT JOIN ( SELECT count(netbar_id) service_num, netbar_id FROM netbar_resource_order WHERE is_valid = 1 AND STATUS = 3 GROUP BY netbar_id ) e ON a.id = e.netbar_id ";
			orderBy = SqlJoiner.join(" order by activity_value desc", appendDistance);
		}
		String sql = SqlJoiner.join(
				"SELECT DISTINCT a.id,a.longitude, a.latitude, a.name netbar_name, a.address, a.price, a.price_per_hour, a.icon, CASE WHEN b.is_valid = 1 THEN 1 ELSE 0 END is_order,IF ((hot IS NULL OR hot = 0), 0, 1) is_hot, IFNULL(avgScore, 5.0) avgScore, IF (g.netbar_id IS NULL and i.netbar_id IS NULL, 0, 1) is_activity, IF (h.netbar_id IS NULL, 0, 1) is_benefit ",
				activitySql, distanceSql,
				" FROM netbar_t_info a LEFT JOIN netbar_t_merchant b ON a.id = b.netbar_id AND b.is_valid = 1 LEFT JOIN ( SELECT a.netbar_id FROM netbar_resource_order a, netbar_resource_commodity_property b, netbar_resource_commodity c, netbar_commodity_category d WHERE a.is_valid = 1 AND a. STATUS = 3 AND a.property_id = b.id AND date_format(b.settl_date, '%Y-%m-%d') = date_format(now(), '%Y-%m-%d') AND a.commodity_id = c.id AND c.category_id = d.id AND d.is_show_app = 1 order by a.create_date desc limit 1) g ON a.id = g.netbar_id LEFT JOIN ( SELECT a.netbar_id FROM netbar_resource_order a, netbar_resource_commodity_property b, netbar_resource_commodity c, netbar_commodity_category d WHERE a. STATUS = 3 AND a.is_valid = 1 AND a.property_id = b.id AND a.commodity_id = c.id AND c.category_id = d.id AND d. NAME = '红包' AND b.validity IS NOT NULL AND now() > date_format(a.create_date, '%Y-%m-%d') AND now() < date_format( date_add( a.create_date, INTERVAL 1 + b.validity DAY ), '%Y-%m-%d' ) order by a.create_date desc limit 1) h ON a.id = h.netbar_id left join (SELECT netbar_id, round(( 20 + sum(enviroment) + sum(equipment) + sum(network) + sum(service)) / (count(1) * 4 + 4), 1 ) avgScore FROM netbar_t_evaluation ne join netbar_t_info ni on ni.id = ne.netbar_id WHERE ni.is_valid = 1 and area_code like '",
				areaCode, "%' GROUP BY netbar_id)f on a.id=f.netbar_id ", activityJoinSql,
				" left join (select netbar_id from netbar_t_recharge_activity a where  a.start_status=1 and a.is_valid=1)i on a.id=i.netbar_id  ",
				" WHERE a.is_release = 1 AND a.is_valid = 1 AND longitude IS NOT NULL AND latitude IS NOT NULL AND area_code like '",
				areaCode, "%'", orderBy, " limit :start,:pageSize");
		this.setVO(queryDao.query(totalSql), queryDao.queryMap(sql, params), vo, page, pageSize);
		result.put("all_netbar", vo);
		if (page == 1 && longitude != null && latitude != null) {
			sql = SqlJoiner.join(
					"SELECT DISTINCT e.id, e.icon, e.name netbar_name FROM netbar_resource_order a, netbar_resource_commodity_property b, netbar_resource_commodity c, netbar_commodity_category d, netbar_t_info e WHERE a. STATUS = 3 AND a.is_valid = 1 AND a.property_id = b.id AND a.commodity_id = c.id AND c.category_id = d.id AND d. NAME = '红包' AND b.validity IS NOT NULL AND now() < date_format( date_add( a.create_date, INTERVAL 1 + b.validity DAY ), '%Y-%m-%d' ) AND a.netbar_id = e.id AND calc_distance ( e.longitude, ",
					String.valueOf(longitude), ", e.latitude, ", String.valueOf(latitude),
					") < 5 ORDER BY e.levels DESC LIMIT 0, 6");
			result.put("hot_netbar", queryDao.queryMap(sql));
		}
		return result;
	}

	/**
	 * 一键预订网吧列表(只显示可预订支付的)
	 * 
	 * @param type
	 *            type=0预定过的优先显示,再按距离排序type=1按距离排序type=2按是否推荐排序type=3按约战数(即热度)
	 *            排序.
	 */
	public PageVO findAllNetbarsForOrder(String longitude, String latitude, Long userId, int page, int pageSize,
			String areaCode, int type) {
		String andAreaCodeCountSql = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(areaCode)) {
			if (StringUtils.endsWith(areaCode, "00")) {
				areaCode = StringUtils.substring(areaCode, 0, 4);
				andAreaCodeCountSql = " and substr(area_code,1,4) = " + areaCode;
			} else {
				andAreaCodeCountSql = " and area_code = " + areaCode;
			}
		}
		String countSql = "select count(1) from netbar_t_info a,netbar_t_merchant b where a.is_release=1 and a.is_valid=1 and a.id = b.netbar_id and b.is_valid=1 and longitude is not null and latitude is not null"
				+ andAreaCodeCountSql;
		Number totalCount = queryDao.query(countSql);
		if (null == totalCount || totalCount.intValue() <= 0) {
			return new PageVO(new ArrayList<Map<String, Object>>());
		}
		PageVO vo = new PageVO();
		if (page * pageSize >= totalCount.intValue()) {
			vo.setIsLast(1);
		}
		Map<String, Object> params = Maps.newHashMap();
		int start = (page - 1) * pageSize;
		double longitudeDouble = NumberUtils.toDouble(longitude);
		double latitudeDouble = NumberUtils.toDouble(latitude);
		String limitDistanceSql = "";
		if (longitudeDouble > 0 && latitudeDouble > 0) {
			limitDistanceSql = SqlJoiner.join(" and ABS(longitude-", longitude, ")<=1 and ABS(latitude-", latitude,
					")<=1 ");
		}
		String calcDistince = " calc_distance (longitude, :longitude, latitude, :latitude) distance,";
		String orderBy = "";
		params.put("start", start);
		params.put("pageSize", pageSize);
		params.put("longitude", longitudeDouble);
		params.put("latitude", latitudeDouble);
		String andAreaCodeSql = andAreaCodeCountSql;

		// 排序条件
		orderBy = getOrderBy(params, longitudeDouble, latitudeDouble, type, areaCode);
		// 预订过的查询条件
		String sort = "";
		String join = "";
		if (type == 0) {
			sort = " case when count(r.id)>0 then 0 else 1 end sort,";
			join = " left join netbar_r_reservation r on r.netbar_id=a.id and r.user_id=:userId";
			params.put("userId", userId);
		}
		String sql = SqlJoiner.join(
				" select a.longitude, a.latitude, a.id, a.name netbar_name, a.address, a.price, a.price_per_hour, a.icon,",
				calcDistince, sort,
				" 	case  when ( rebate > 0 and rebate <= 90 and now() >= rebate_start_date and now() <= rebate_end_date ) or a.discount_info is not null or length(trim(a.discount_info)) >= 1  then 1 else 0 end has_rebate, ",
				" 	1 is_order, ", " 	a.is_recommend, ", " 	case when count(m.id) > 1 then 1 else 0 end is_hot ",
				" from (netbar_t_info a,netbar_t_merchant b) ",
				" 	left join activity_t_matches m on m.netbar_id = a.id and m.is_start = 0 and m.begin_time > now()",
				join,
				" where a.is_release = 1 and a.is_valid = 1 and a.id = b.netbar_id and b.is_valid=1 and longitude is not null and latitude is not null",
				limitDistanceSql, andAreaCodeSql, " group by a.id ", orderBy, " limit :start, :pageSize ");
		List<Map<String, Object>> result = queryDao.queryMap(sql, params);
		if (CollectionUtils.isNotEmpty(result)) {
			vo.setList(result);
		} else {
			vo.setList(new ArrayList<Map<String, Object>>());
		}
		return vo;
	}

	/**
	 * 拼接orderby sql
	 */
	private String getOrderBy(Map<String, Object> params, double longitudeDouble, double latitudeDouble, int type,
			String areaCode) {
		String orderBy = "";
		if (type == 0) {
			if (longitudeDouble > 0 && latitudeDouble > 0) {
				orderBy = " order by sort,calc_distance (longitude, :longitude, latitude, :latitude) asc";
			} else {
				// 网吧权重
				orderBy = " order by sort asc,count(m.id) desc,(count(m.id)+(case when b.is_valid = 1 then 3 else 0 end)+(case when a.is_recommend = 1 then 6 else 0 end))*((case when substr(area_code,1,4) =:areaCode  then 1 else 0 end)) desc";
				params.put("areaCode", areaCode.length() > 4 ? StringUtils.substring(areaCode, 0, 4) : areaCode);
			}
		} else if (longitudeDouble > 0 && latitudeDouble > 0 && type == 1) {
			orderBy = " order by calc_distance (longitude, :longitude, latitude, :latitude) asc";
		} else if (type == 2) {
			if (longitudeDouble > 0 && latitudeDouble > 0) {
				orderBy = " order by is_recommend desc,calc_distance (longitude, :longitude, latitude, :latitude) asc";
			} else {
				// 网吧权重
				orderBy = " order by is_recommend desc,(count(m.id)+(case when b.is_valid = 1 then 3 else 0 end)+(case when a.is_recommend = 1 then 6 else 0 end))*((case when substr(area_code,1,4) =:areaCode  then 1 else 0 end)) desc";
				params.put("areaCode", areaCode.length() > 4 ? StringUtils.substring(areaCode, 0, 4) : areaCode);
			}
		} else if (type == 3) {
			if (longitudeDouble > 0 && latitudeDouble > 0) {
				orderBy = " order by count(m.id) desc,calc_distance (longitude, :longitude, latitude, :latitude) asc";
			} else {
				// 网吧权重
				orderBy = " order by count(m.id) desc,(count(m.id)+(case when b.is_valid = 1 then 3 else 0 end)+(case when a.is_recommend = 1 then 6 else 0 end))*((case when substr(area_code,1,4) =:areaCode  then 1 else 0 end)) desc";
				params.put("areaCode", areaCode.length() > 4 ? StringUtils.substring(areaCode, 0, 4) : areaCode);
			}
		}
		return orderBy;
	}

	/**
	 * 搜索页面最近的五条网吧名称
	 */
	public List<Map<String, Object>> findFiveNearestNetbars(String longitude, String latitude, String areaCode) {
		Map<String, Object> params = new HashMap<String, Object>();
		double longitudeDouble = NumberUtils.toDouble(longitude);
		double latitudeDouble = NumberUtils.toDouble(latitude);
		String limitDistanceSql = "";
		if (longitudeDouble > 0 && latitudeDouble > 0) {
			limitDistanceSql = SqlJoiner.join(" and ABS(longitude-", longitude, ")<=1 and ABS(latitude-", latitude,
					")<=1 ");
		}
		String areaSql = "";
		if (longitudeDouble > 0 && latitudeDouble > 0) {
			params.put("longitude", longitudeDouble);
			params.put("latitude", latitudeDouble);
			if (StringUtils.isNotBlank(areaCode)) {
				if (areaCode.endsWith("00")) {
					areaSql = " and area_code like '" + areaCode.substring(0, 4) + "%'";
				} else {
					areaSql = " and area_code=" + areaCode;
				}
			}
			String sql = SqlJoiner.join("select id, name netbar_name from netbar_t_info",
					" where is_release = 1 and is_valid = 1 and longitude is not null and latitude is not null ",
					limitDistanceSql, areaSql,
					" order by calc_distance (longitude, :longitude, latitude, :latitude) asc limit 0, 5 ");
			return queryDao.queryMap(sql, params);
		} else {
			return new ArrayList<Map<String, Object>>();
		}
	}

	/**
	 * 按名称搜索网吧
	 */
	public List<Map<String, Object>> searchNetbarByName(String netbarName, String longitude, String latitude,
			int type) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("netbarName", netbarName);
		double longitudeDouble = NumberUtils.toDouble(longitude);
		double latitudeDouble = NumberUtils.toDouble(latitude);
		String calcDistince = "";
		String orderBy = "";
		if (longitudeDouble > 0 && latitudeDouble > 0) {
			params.put("longitude", longitudeDouble);
			params.put("latitude", latitudeDouble);
			calcDistince = " calc_distance (longitude, :longitude, latitude, :latitude) distance,";
			orderBy = " order by  calc_distance (longitude, :longitude, latitude, :latitude)  asc ";
		}
		String sql = "";
		if (type == 0) {
			sql = SqlJoiner.join("select a.*,",
					" case when ( rebate > 0 and rebate <= 90 and now() >= rebate_start_date and now() <= rebate_end_date ) or (a.discount_info is not null and length(trim(a.discount_info)) >= 1 ) then 1 else 0 end has_rebate, ",
					" case when b.is_valid = 1 then 1 else 0 end is_order, ",
					" if(g.netbar_id is null  and i.netbar_id is null ,0,1) is_activity,if(h.netbar_id is null,0,1) is_benefit",
					" from ( select a.longitude, a.latitude, a.id, a.name netbar_name, a.address, a.price, a.price_per_hour, a.icon, a.is_recommend,",
					calcDistince,
					" a.rebate, a.rebate_start_date, a.rebate_end_date, a.discount_info,if((a.hot is null or a.hot=0),0,1) is_hot ",
					" from netbar_t_info a ",
					" where name like concat('%', :netbarName, '%') and a.is_release = 1 and a.is_valid = 1 ", orderBy,
					" limit 0,100) a", " left join netbar_t_merchant b on a.id = b.netbar_id  and b.is_valid=1",
					" left join activity_t_matches m on m.netbar_id = a.id and m.is_start = 0 and m.begin_time > now() ",
					" left join(select a.netbar_id from netbar_resource_order a,netbar_resource_commodity_property b,netbar_resource_commodity c,netbar_commodity_category d where a.is_valid=1 and a.status=3 and a.property_id=b.id and date_format(b.settl_date, '%Y-%m-%d')=date_format(now(), '%Y-%m-%d') and a.commodity_id=c.id and c.category_id=d.id and d.is_show_app=1)g on a.id=g.netbar_id ",
					" LEFT JOIN ( SELECT a.netbar_id FROM netbar_resource_order a, netbar_resource_commodity_property b, netbar_resource_commodity c, netbar_commodity_category d WHERE a. STATUS = 3 AND a.is_valid = 1 AND a.property_id = b.id AND a.commodity_id = c.id AND c.category_id = d.id AND d. NAME = '增值券' AND b.validity IS NOT NULL AND now() > date_format(a.create_date, '%Y-%m-%d') AND now() < date_format( date_add( a.create_date, INTERVAL 1 + b.validity DAY ), '%Y-%m-%d' )) h ON a.id = h.netbar_id ",
					" left join (select netbar_id from netbar_t_recharge_activity a where  a.start_status=1 and a.is_valid=1)i on a.id=i.netbar_id ",
					" group by a.id", orderBy);
		} else {// 限制只查询可预订网吧
			sql = SqlJoiner.join(
					" select a.longitude, a.latitude, a.id, a.name netbar_name, a.address, a.price, a.price_per_hour, a.icon,  ",
					calcDistince,
					" 	 case when ( rebate > 0 and rebate <= 90 and now() >= rebate_start_date and now() <= rebate_end_date ) or (a.discount_info is not null and length(trim(a.discount_info)) >= 1 ) then 1 else 0 end has_rebate, ",
					"  	 case when b.is_valid = 1 then 1 else 0 end is_order, ",
					" 	 if((hot is null or hot=0),0,1) is_hot,if(g.netbar_id is null and i.netbar_id is null,0,1) is_activity,if(h.netbar_id is null,0,1) is_benefit ",
					" from (netbar_t_info a,netbar_t_merchant b)",
					"	left join activity_t_matches m on m.netbar_id = a.id and m.is_start = 0 and m.begin_time > now() ",
					" left join(select a.netbar_id from netbar_resource_order a,netbar_resource_commodity_property b,netbar_resource_commodity c,netbar_commodity_category d where a.is_valid=1 and a.status=3 and a.property_id=b.id and date_format(b.settl_date, '%Y-%m-%d')=date_format(now(), '%Y-%m-%d') and a.commodity_id=c.id and c.category_id=d.id and d.is_show_app=1)g on a.id=g.netbar_id ",
					" LEFT JOIN ( SELECT a.netbar_id FROM netbar_resource_order a, netbar_resource_commodity_property b, netbar_resource_commodity c, netbar_commodity_category d WHERE a. STATUS = 3 AND a.is_valid = 1 AND a.property_id = b.id AND a.commodity_id = c.id AND c.category_id = d.id AND d. NAME = '增值券' AND b.validity IS NOT NULL AND now() > date_format(a.create_date, '%Y-%m-%d') AND now() < date_format( date_add( a.create_date, INTERVAL 1 + b.validity DAY ), '%Y-%m-%d' )) h ON a.id = h.netbar_id ",
					" left join (select netbar_id from netbar_t_recharge_activity a where  a.start_status=1 and a.is_valid=1)i on a.id=i.netbar_id ",
					" where name like concat('%', :netbarName, '%') and a.is_release = 1 and a.is_valid = 1 and a.id = b.netbar_id and b.is_valid=1 ",
					"  group by a.id  ", orderBy, " limit 0,100");
		}
		return queryDao.queryMap(sql, params);
	}

	/**
	 * 查找地图上10公里内的网吧信息
	 */
	public List<Map<String, Object>> findNearestNetbarsOnMap(String longitude, String latitude) {
		double longitudeDouble = NumberUtils.toDouble(longitude);
		double latitudeDouble = NumberUtils.toDouble(latitude);
		if (longitudeDouble > 0 && latitudeDouble > 0) {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("longitude", longitudeDouble);
			params.put("latitude", latitudeDouble);
			String querySql = SqlJoiner.join(
					"select id, name netbar_name,price_per_hour,longitude,latitude,icon icon, calc_distance ( longitude, :longitude, latitude, :latitude ) distance",
					"    from netbar_t_info where is_release = 1 and is_valid = 1 and calc_distance (longitude, :longitude, latitude, :latitude)<=10");
			List<Map<String, Object>> queryMap = queryDao.queryMap(querySql, params);
			if (CollectionUtils.isNotEmpty(queryMap)) {
				return queryMap;
			}
		}
		return new ArrayList<Map<String, Object>>();
	}

	/**
	 * 查看网吧详情
	 * 
	 * @param latitude
	 * @param longitude
	 */
	public Map<String, Object> detail(Long netbarInfoId, String longitude, String latitude) {

		Map<String, Object> params = new HashMap<String, Object>();
		String distinceSql = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(longitude) && StringUtils.isNotBlank(latitude)) {
			params.put("longitude", longitude);
			params.put("latitude", latitude);
			distinceSql = " ,calc_distance ( a.longitude, :longitude, a.latitude, :latitude ) distance ";
		}
		params.put("netbarInfoId", netbarInfoId);
		String sql = "select a.id ,a.area_id,a.name netbar_name,a.address,a.presentation,a.cpu,a.memory,a.graphics,a.display,a.telephone,case when a.discount_info is not null then a.discount_info else '' end discount_info ,a.seating,a.price,a.price_per_hour ,a.score,a.icon,a.longitude,a.latitude,case  when a.rebate > 0 and a.rebate <= 90 and now() >= a.rebate_start_date and now() <= a.rebate_end_date then 1 else 0 end has_rebate ,a.rebate  , count(m.id) is_order, count(am.id) is_recommend "
				+ distinceSql
				+ "from  netbar_t_info a left join netbar_t_merchant m on a.id = m.netbar_id and m.is_valid = 1 left join activity_t_matches am on am.netbar_id = a.id and am.is_start = 1 and NOW() > am.begin_time and NOW() < am.over_time  where a.id=:netbarInfoId";
		Map<String, Object> result = queryDao.querySingleMap(sql, params);

		params.remove("longitude");
		params.remove("latitude");

		sql = "select a.user_id,id,title,people_num,begin_time,spoils,icon,item_name,match_id,apply_num,nickname,item_pic,telephone,by_merchant from (select b.user_id,b.id,b.title,b.people_num,b.begin_time,b.spoils,b.icon,c.name item_name,c.pic item_pic,u.telephone, b.by_merchant,u.nickname from activity_t_matches b,activity_r_items c, user_t_info u  where b.netbar_id=:netbarInfoId and b.is_start!=1 and u.id = b.user_id and NOW()<b.begin_time and b.item_id=c.id and b.is_valid = 1 order by b.by_merchant desc, b.create_date desc)a left join  (select match_id,count(*) apply_num from activity_r_match_apply where is_valid = 1 group by match_id )b on a.id=b.match_id ";
		List<Map<String, Object>> matches = queryDao.queryMap(sql, params);
		result.put("matches", matches);
		sql = "select url from netbar_r_imgs where netbar_id=:netbarInfoId and is_valid=1  and verified=1 ";
		List<Map<String, Object>> imgs = queryDao.queryMap(sql, params);
		result.put("imgs", imgs);

		String evaluationSql = "select  totalEva, case when avgScore is null then 5.0 else avgScore end avgScore,case when enviroment is null then 5.0 else enviroment end enviroment,  case when equipment is null then 5.0 else equipment end equipment,  case when network is null then 5.0 else network end network, case when service is null then 5.0 else service end service,case when (b.contentEva is null) then 0  else b.contentEva end contentEva "
				+ " from (select        netbar_id, count(1) totalEva, round(( 20+sum(enviroment) + sum(equipment) + sum(network) + sum(service) ) /  (count(1) *4+4), 1        ) avgScore,"
				+ " ROUND((5+sum(enviroment)) / (count(1)+1), 1) enviroment, ROUND((5+sum(equipment)) /  (count(1)+1), 1) equipment, ROUND((5+sum(network)) /  (count(1)+1), 1) network,ROUND((5+sum(service)) /  (count(1)+1), 1) service "
				+ " from         netbar_t_evaluation    where netbar_id = " + netbarInfoId
				+ " and is_valid = 1) a left join (select netbar_id, count(1) contentEva from  netbar_t_evaluation "
				+ " where netbar_id = " + netbarInfoId
				+ " and is_valid = 1 and ( (content is not null and content <> '') or  id  in (select eva_id from netbar_r_evaluation_imgs ))) b on a.netbar_id = b.netbar_id  ";

		Map<String, Object> eva = queryDao.querySingleMap(evaluationSql);
		result.put("eva", eva);

		String amuseSql = "select id,title,start_date startDate,virtual_apply,max_num maxNum from amuse_t_activity where verify_end_date> now() and netbar_id ="
				+ netbarInfoId + " and is_valid = 1 order by create_date desc limit 1";
		Map<String, Object> amuse = queryDao.querySingleMap(amuseSql);

		if (MapUtils.isNotEmpty(amuse)) {
			result.put("amuse", amuse);
			long amuseId = NumberUtils.toLong(amuse.get("id").toString());
			String sqlbanner = "select icon from amuse_r_activity_icon where is_main = 1 and activity_id=" + amuseId
					+ " limit 1";
			Map<String, Object> bannerMap = queryDao.querySingleMap(sqlbanner);
			if (MapUtils.isNotEmpty(bannerMap)) {
				amuse.put("banner", bannerMap.get("icon"));
			} else {
				amuse.put("banner", null);
			}

			int applyerNum = amuseActivityInfoService.getApplyNumByAmuseId(amuseId);
			int applyNum = applyerNum + NumberUtils
					.toInt(null == amuse.get("virtual_apply") ? "" : amuse.get("virtual_apply").toString());
			if (null != amuse.get("max_num")) {
				int maxNum = NumberUtils.toInt(amuse.get("max_num").toString());
				if (applyNum > maxNum) {
					applyNum = maxNum;
				}
			}
			amuse.put("applyNum", applyNum);
			amuse.remove("virtual_apply");
			amuse.remove("maxNum");
		}

		return result;
	}

	public String getNetbarResourceListSql(String netbarId) {
		return SqlJoiner.join(
				"SELECT a.commodity_id, a.property_id, c.url, b.name, b.interest_num FROM netbar_resource_order a, netbar_resource_commodity_property b, netbar_resource_commodity c ,netbar_commodity_category d WHERE a.netbar_id =",
				netbarId,
				" AND a.is_valid = 1 AND a. STATUS = 3 AND a.property_id = b.id and date_format(b.settl_date, '%Y-%m-%d') = date_format(now(), '%Y-%m-%d') AND a.commodity_id = c.id and c.category_id=d.id and d.is_show_app=1 ORDER BY b.settl_date");
	}

	/**
	 * 查看网吧竞技活动
	 * 
	 * @param latitude
	 * @param longitude
	 */
	public Map<String, Object> activities(String netbarId) {
		Map<String, Object> result = new HashMap<String, Object>();
		// String sql = SqlJoiner
		// .join("select a.user_id
		// releaser_id,id,title,people_num,begin_time,spoils,icon
		// releaser_icon,item_name,match_id,apply_num,nickname,item_pic,telephone,by_merchant,way,server,sex
		// from (select
		// b.user_id,b.id,b.title,b.people_num,b.begin_time,b.spoils,c.name
		// item_name,c.pic item_pic,u.telephone,
		// b.by_merchant,u.nickname,u.icon,b.way,b.server,u.sex from
		// activity_t_matches b,activity_r_items c, user_t_info u where
		// b.netbar_id=",
		// netbarId,
		// " and b.is_start!=1 and u.id = b.user_id and NOW()<b.begin_time and
		// b.item_id=c.id and b.is_valid = 1 order by b.by_merchant desc,
		// b.create_date desc)a left join (select match_id,count(*) apply_num
		// from activity_r_match_apply where is_valid = 1 group by match_id )b
		// on a.id=b.match_id");
		// result.put("matches", queryDao.queryMap(sql));
		//
		// String amuseSql = SqlJoiner
		// .join("select id,title,start_date startDate,virtual_apply,max_num
		// maxNum,IF (now() < apply_start, 2, IF (now() >= apply_start AND now()
		// < start_date, 1, IF (now() >= start_date AND now() < end_date, 5,
		// if(now() < verify_end_date, 4, 6)))) timeStatus from amuse_t_activity
		// where verify_end_date> now() and netbar_id =",
		// netbarId, " and is_valid = 1 order by create_date desc limit 1");
		// Map<String, Object> amuse = queryDao.querySingleMap(amuseSql);
		//
		// if (MapUtils.isNotEmpty(amuse)) {
		// result.put("amuse", amuse);
		// long amuseId = NumberUtils.toLong(amuse.get("id").toString());
		// String sqlbanner = "select icon from amuse_r_activity_icon where
		// is_main = 1 and activity_id=" + amuseId
		// + " limit 1";
		// Map<String, Object> bannerMap = queryDao.querySingleMap(sqlbanner);
		// if (MapUtils.isNotEmpty(bannerMap)) {
		// amuse.put("banner", bannerMap.get("icon"));
		// } else {
		// amuse.put("banner", null);
		// }
		//
		// int applyerNum =
		// amuseActivityInfoService.getApplyNumByAmuseId(amuseId);
		// int applyNum = applyerNum
		// + (NumberUtils.toInt(null == amuse.get("virtual_apply") ? "" :
		// amuse.get("virtual_apply")
		// .toString()));
		// if (null != amuse.get("max_num")) {
		// int maxNum = NumberUtils.toInt(amuse.get("max_num").toString());
		// if (applyNum > maxNum) {
		// applyNum = maxNum;
		// }
		// }
		// amuse.put("applyNum", applyNum);
		// amuse.remove("virtual_apply");
		// amuse.remove("maxNum");
		// }
		result.put("services", queryDao.queryMap(getNetbarResourceListSql(netbarId)));
		return result;
	}

	/**
	 * 查看网吧基本信息
	 * 
	 * @param latitude
	 * @param longitude
	 */
	public Map<String, Object> baseInfo(String userId, String netbarId, String longitude, String latitude) {
		Map<String, Object> result = null;
		Map<String, Object> params = new HashMap<String, Object>();
		String sql = "";
		String distanceSql = "";
		String favorSql = "";
		String favorJoinSql = "";
		if (StringUtils.isNotBlank(longitude) && StringUtils.isNotBlank(latitude)) {
			params.put("longitude", longitude);
			params.put("latitude", latitude);
			distanceSql = " ,calc_distance ( a.longitude, :longitude, a.latitude, :latitude ) distance ";
		}
		if (StringUtils.isNotBlank(userId)) {
			favorSql = " ,case when c.id is null then 0 else 1 end faved ";
			favorJoinSql = SqlJoiner.join(
					" left join user_r_favor c on a.id=c.sub_id and c.type=1 and c.is_valid=1 and c.user_id=", userId);
		}
		sql = SqlJoiner.join(
				"SELECT a.id,a.longitude,a.latitude,a.icon,price_per_hour, name, levels, a.address, telephone, seating, tag, cpu, memory, graphics, display, discount_info, CASE WHEN b.is_valid = 1 THEN 1 ELSE 0 END is_order,case  when a.rebate > 0 and a.rebate <= 90 and now() >= a.rebate_start_date and now() <= a.rebate_end_date then 1 else 0 end has_rebate ,a.rebate",
				favorSql, distanceSql,
				" FROM netbar_t_info a LEFT JOIN netbar_t_merchant b ON a.id = b.netbar_id AND b.is_valid = 1 ",
				favorJoinSql, " WHERE a.id = ", netbarId);
		result = queryDao.querySingleMap(sql, params);
		sql = SqlJoiner.join("select url from netbar_r_imgs where netbar_id=", netbarId,
				" and is_valid=1  and verified=1");
		result.put("imgs", queryDao.queryMap(sql));
		sql = SqlJoiner.join(
				"select area_name,price,rebate_price from netbar_area_price where is_valid=1 and netbar_id=", netbarId);
		result.put("area", queryDao.queryMap(sql));
		result.put("services", queryDao.queryMap(getNetbarResourceListSql(netbarId)));
		sql = "select a.trade_no, if ( a.buy_num - if (used_num is null, 0, used_num) > 0, 1, 0 ) has_left, c.redbag_id, if ( date_format( date_add(a.create_date, interval 1 day), '%Y-%m-%d' ) < now(), 1, 0 ) is_start, date_format( date_add(a.create_date, interval 1 day), '%Y-%m-%d' ) start_date, date_format(a.expire_date, '%Y-%m-%d') end_date from ( netbar_resource_order a, netbar_resource_commodity b, netbar_resource_commodity_property c, netbar_commodity_category d ) LEFT JOIN ( SELECT e.trade_no, count(e.value_add_card_id) used_num FROM user_value_added_card e, sys_value_added_card f WHERE e.netbar_id ="
				+ netbarId
				+ " AND e.value_add_card_id = f.id and ((e.is_valid=0 or e.is_valid=-1) or (e.is_valid=1 and now()<e.expire_date)) GROUP BY e.trade_no ) z ON a.trade_no = z.trade_no WHERE a.netbar_id ="
				+ netbarId
				+ " AND a. STATUS = 3 AND a.is_valid = 1 AND a.commodity_id = b.id AND a.property_id = c.id AND b.category_id = d.id AND d. NAME = '增值券' AND c.validity IS NOT NULL AND now() > date_format(a.create_date, '%Y-%m-%d') AND now() < date_format(a.expire_date, '%Y-%m-%d') ORDER BY has_left desc, is_start desc, a.create_date desc LIMIT 1";
		Map<String, Object> redbagInfo = queryDao.querySingleMap(sql);
		Date now = new Date();
		if (redbagInfo == null || redbagInfo.get("redbag_id") == null) {
			result.put("pay_word", "支付网费");
			result.put("is_exclusive", 0);
		} else {
			String redbagId = ((Number) redbagInfo.get("redbag_id")).toString();
			String tradeNo = String.valueOf(redbagInfo.get("trade_no"));
			result.put("is_exclusive", 1);
			if (((Number) redbagInfo.get("is_start")).intValue() == 0) {
				try {
					Date startDate = DateUtils.stringToDate((String) redbagInfo.get("start_date"),
							DateUtils.YYYY_MM_DD);
					result.put("pay_word", "支付网费");
					result.put("remain_time_to_start", startDate.getTime() - now.getTime());
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else {
				RedisAtomicInteger leftNum = new RedisAtomicInteger(CommonConstant.MERCHANT_REDBAG_LEFT_NUM + tradeNo,
						redisConnectionFactory);
				if (leftNum.get() == -1
						|| leftNum.get() == 0 && ((Number) redbagInfo.get("has_left")).intValue() == 0) {
					result.put("pay_word", "优惠支付");
					result.put("pay_tip", "当前时段减免已被领取完");
				} else {
					if (StringUtils.isBlank(userId)) {
						result.put("pay_word", "优惠支付");
						result.put("pay_tip", "活动进行中");
					} else {
						sql = SqlJoiner.join("select id from user_value_added_card where value_add_card_id=", redbagId,
								" and user_id=", userId, " and trade_no='", tradeNo, "' and netbar_id=", netbarId,
								" and date_format(create_date, '%Y-%m-%d')=date_format(now(), '%Y-%m-%d')");
						Map<String, Object> userRedbag = queryDao.querySingleMap(sql);
						if (userRedbag == null) {
							result.put("pay_word", "优惠支付");
							result.put("pay_tip", "活动进行中");
						} else {
							try {
								Date endDate = DateUtils.stringToDate((String) redbagInfo.get("end_date"),
										DateUtils.YYYY_MM_DD);
								if (DateUtils.dateToString(now, DateUtils.YYYY_MM_DD)
										.equals(redbagInfo.get("end_date"))) {
									result.put("pay_word", "优惠支付");
									result.put("remain_time_to_end",
											DateUtils.addDays(endDate, 1).getTime() - now.getTime());
								} else {
									Date nextDay = DateUtils.addDays(now, 1);
									nextDay = DateUtils.stringToDate(
											DateUtils.dateToString(nextDay, DateUtils.YYYY_MM_DD),
											DateUtils.YYYY_MM_DD);
									result.put("pay_word", "优惠支付");
									result.put("again_get_time", nextDay.getTime() - now.getTime());
								}
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		sql = "select count(eva.id) total from netbar_t_evaluation eva  where  eva.netbar_id =" + netbarId;
		Map<String, Object> total = queryDao.querySingleMap(sql);
		Number totalCount = (Number) total.get("total");
		if (totalCount != null) {
			result.put("totalEva", totalCount.intValue());
		}
		return result;
	}

	/**
	 * 网吧评价
	 * 
	 * @param userId
	 * @param netbarId
	 * @return
	 */
	public Map<String, Object> eva(String userId, String netbarId, Pager pager, Integer lastTotal) {
		Map<String, Object> result = new HashMap<String, Object>();
		String sql = "";
		sql = SqlJoiner.join("select count(1) from netbar_t_merchant where netbar_id=", netbarId, " and is_valid=1");
		Number count = queryDao.query(sql);
		if (count.intValue() == 0) {
			return null;
		}
		if (pager.page == 1) {
			sql = "select  totalEva, case when avgScore is null then 5.0 else avgScore end avgScore,case when enviroment is null then 5.0 else enviroment end enviroment,  case when equipment is null then 5.0 else equipment end equipment,  case when network is null then 5.0 else network end network, case when service is null then 5.0 else service end service "
					+ " from (select        netbar_id, count(1) totalEva, round(( 20+sum(enviroment) + sum(equipment) + sum(network) + sum(service) ) /  (count(1) *4+4), 1        ) avgScore,"
					+ " ROUND((5+sum(enviroment)) / (count(1)+1), 1) enviroment, ROUND((5+sum(equipment)) /  (count(1)+1), 1) equipment, ROUND((5+sum(network)) /  (count(1)+1), 1) network,ROUND((5+sum(service)) /  (count(1)+1), 1) service "
					+ " from         netbar_t_evaluation    where netbar_id = " + netbarId + " and is_valid = 1) a ";
			result.put("scores", queryDao.querySingleMap(sql));
		}
		PageVO vo = new PageVO();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("netbarId", netbarId);
		sql = "select count(eva.id) total from netbar_t_evaluation eva  where  eva.netbar_id = :netbarId ";
		Map<String, Object> total = queryDao.querySingleMap(sql, params);
		Number totalCount = (Number) total.get("total");
		vo.setTotal(totalCount.intValue());
		if (pager.total >= totalCount.intValue()) {
			vo.setIsLast(1);
		}
		int limitStart;
		if (lastTotal == null) {
			limitStart = (pager.page - 1) * pager.pageSize;
		} else {
			limitStart = (pager.page - 1) * pager.pageSize + totalCount.intValue() - lastTotal;
		}
		params.put("start", limitStart);
		String praiseSql = StringUtils.EMPTY;
		String columnSql = StringUtils.EMPTY;

		if (NumberUtils.toLong(userId) > 0) {
			praiseSql = " left join netbar_t_evaluation_praise p         on p.eva_id = eva.id        and p.user_id = :userId ";
			columnSql = " ,case        when count(p.id) > 0        then 1        else 0    end isPraised ";
			params.put("userId", userId);
		}

		if (totalCount.intValue() > 0) {
			sql = SqlJoiner.join(
					"select    eva.user_id ,eva.id, eva.praised, eva.content,  if((eva.content is null or eva.content='') and (group_concat(img.url order by img.create_date ) is null),1,0) is_no_comment,eva.is_anonymous, ui.nickname, ui.icon, round(        (            enviroment + equipment + network + service        ) / 4, 1    ) avgScore, eva.create_date ,group_concat(img.url order by img.create_date ) as imgs  ",
					columnSql,
					"  from    netbar_t_evaluation eva    left join user_t_info ui on eva.user_id = ui.id left join netbar_r_evaluation_imgs img on img.eva_id = eva.id  ",
					praiseSql,
					" where  eva.netbar_id = :netbarId group by eva.id order by eva.create_date desc  limit :start,:pageSize");
			params.put("start", pager.start);
			params.put("pageSize", pager.pageSize);
			vo.setList(queryDao.queryMap(sql, params));
			result.put("comments", vo);
		}
		return result;
	}

	@Autowired
	private AmuseActivityInfoService amuseActivityInfoService;

	/**
	 * 收藏网吧
	 */
	public int favor(Long userId, Long netbarId) {
		UserFavor uf = userFavorDao.findByUserIdAndSubIdAndType(userId, netbarId, 1);
		if (null == uf) {
			uf = new UserFavor();
			uf.setCreateDate(new Date());
			uf.setSubId(netbarId);
			uf.setType(1);
			uf.setUserId(userId);
			uf.setValid(1);
			userFavorDao.save(uf);
			return 0;
		} else {
			Integer valid = uf.getValid();
			if (null == valid || valid == 0) {
				uf.setValid(1);
				userFavorDao.save(uf);
				return 0;
			} else {
				return 1;
			}
		}
	}

	/**
	 * 网吧是否已收藏
	 */
	public boolean isFaved(Long userId, Long netbarId) {
		UserFavor uf = userFavorDao.findByUserIdAndSubIdAndType(userId, netbarId, 1);
		if (null == uf) {
			return false;
		}
		Integer valid = uf.getValid();
		if (null == valid || valid == 0) {
			return false;
		}
		return true;
	}

	/**
	 * 取消网吧收藏
	 */
	public int unfavor(Long userId, Long netbarId) {
		UserFavor uf = userFavorDao.findByUserIdAndSubIdAndType(userId, netbarId, 1);
		if (null == uf) {
			return 1;
		} else {
			Integer valid = uf.getValid();
			if (null != valid && valid != 0) {
				uf.setValid(0);
				userFavorDao.save(uf);
				return 0;
			} else {
				return 1;
			}
		}
	}

	/**
	 * 我收藏的网吧
	 */
	public PageVO favedNetbars(Long userId, String longitude, String latitude, int page, int pageSize) {
		String countSql = "select count(id)  from user_r_favor where type=1 and is_valid = 1 and user_id=" + userId;
		Number totalCount = queryDao.query(countSql);
		if (null == totalCount || totalCount.intValue() <= 0) {
			return new PageVO(new ArrayList<Map<String, Object>>());
		}
		int start = (page - 1) * pageSize;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", userId);
		params.put("start", start);
		params.put("pageSize", pageSize);
		String querySql = SqlJoiner.join(
				"select a.id, a.longitude, a.latitude, a.icon, a.name netbar_name, a.address, a.price, a.price_per_hour,a.is_recommend,",
				" case  when a.rebate > 0 and a.rebate <= 90 and now() >= a.rebate_start_date and now() <= a.rebate_end_date then 1 else 0 end has_rebate ,",
				" 	case when b.is_valid = 1 then 1 else 0 end is_order, ",
				" 	case when count(m.id) > 1 then 1 else 0 end is_hot ", " from netbar_t_info a ",
				"	left join netbar_t_merchant b on a.id = b.netbar_id and b.is_valid=1 ",
				"	left join activity_t_matches m on m.netbar_id = a.id and m.is_start = 0 and m.begin_time > now() ",
				" where a.id in (select sub_id from  user_r_favor f where f.type = 1  and f.user_id = :userId and f.is_valid = 1 ) ",
				" group by a.id  order by b.create_date desc limit :start, :pageSize ");
		int isLast = 0;
		if (page * pageSize >= totalCount.intValue()) {
			isLast = 1;
		}
		return new PageVO(queryDao.queryMap(querySql, params), isLast);
	}

	/**
	 * 查询某个网吧的折扣信息
	 * 
	 * @param netbarId
	 *            网吧id
	 */
	public int findRebate(Long netbarId) {
		String sql = SqlJoiner.join(
				" select rebate from netbar_t_info where rebate >= 50 and rebate <= 90	and now() >= rebate_start_date and now() <= rebate_end_date and id =",
				netbarId.toString());
		Number rebate = queryDao.query(sql);
		if (null == rebate) {
			return 100;
		} else {
			return rebate.intValue();
		}
	}

	/**
	 * 审核人员网吧列表
	 */
	public PageVO netbarAreaLimit(Integer isRelease, Long userId, int page, String netbarName, String telephone,
			String areaCode, Integer source, String beginReleaseTime, String endReleaseTime) {
		String sql = StringUtils.EMPTY;
		PageVO vo = new PageVO();
		String netbarNameSql = StringUtils.EMPTY;
		String telephoneSql = StringUtils.EMPTY;
		String areaCodeSql = StringUtils.EMPTY;
		String beginTimeSql = StringUtils.EMPTY;
		String endTimeSql = StringUtils.EMPTY;
		String sourceSql = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(netbarName)) {
			netbarNameSql = SqlJoiner.join(" and a.name like '%", netbarName, "%'");
		}
		if (StringUtils.isNotBlank(telephone)) {
			telephoneSql = SqlJoiner.join(" and a.telephone like '%", telephone, "%'");
		}
		if (StringUtils.isNotBlank(beginReleaseTime)) {
			beginTimeSql = SqlJoiner.join(" and a.release_date > '", beginReleaseTime, "'");
		}
		if (StringUtils.isNotBlank(endReleaseTime)) {
			endTimeSql = SqlJoiner.join(" and a.release_date < '", endReleaseTime, "'");
		}
		if (StringUtils.isNotBlank(areaCode)) {
			if (areaCode.substring(2, 6).equals("0000")) {
				areaCode = areaCode.substring(0, 2) + "%";
			} else if (areaCode.substring(4, 6).equals("00")) {
				areaCode = areaCode.substring(0, 4) + "%";
			}
			areaCodeSql = SqlJoiner.join(" and a.area_code like'", areaCode, "'");
		}
		if (source != null) {
			sourceSql = SqlJoiner.join(" and a.source=", String.valueOf(source));
		}
		sql = SqlJoiner.join("select count(1) from netbar_t_info a,sys_t_area b", " where a.is_release=",
				String.valueOf(isRelease), netbarNameSql, telephoneSql, areaCodeSql, beginTimeSql, endTimeSql,
				sourceSql,
				" and a.is_valid=1 and a.area_code=b.area_code and b.id in(select area_id from sys_r_user_area where sys_user_id=",
				String.valueOf(userId), ")");
		Number totalCount = queryDao.query(sql);
		if (totalCount != null) {
			vo.setTotal(totalCount.intValue());
		}
		int pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		int start = (page - 1) * pageSize;
		if (start + pageSize >= vo.getTotal()) {
			vo.setIsLast(1);
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("isRelease", isRelease);
		params.put("userId", userId);
		params.put("start", start);
		params.put("pageSize", pageSize);
		sql = SqlJoiner.join(
				"select a.*,x.realname from (netbar_t_info a,sys_t_area b) left join sys_t_user x on a.create_user_id=x.id ",
				" where a.is_release=:isRelease", netbarNameSql, telephoneSql, areaCodeSql, beginTimeSql, endTimeSql,
				sourceSql,
				" and a.is_valid=1 and a.area_code=b.area_code and b.id in(select area_id from sys_r_user_area where sys_user_id=:userId) limit :start,:pageSize");
		vo.setCurrentPage(page);
		vo.setList(queryDao.queryMap(sql, params));
		return vo;
	}

	/**
	 * 管理员已发布未发布全部网吧
	 * 
	 * @param isRelease
	 * @param userId
	 * @param page
	 * @param netbarName
	 * @param telephone
	 * @param areaCode
	 * @return
	 */
	public PageVO netbarIsRelease(Integer isRelease, int page, String netbarName, String telephone, String startDate,
			String endDate, Integer source) {
		String sql = StringUtils.EMPTY;
		PageVO vo = new PageVO();
		String netbarNameSql = StringUtils.EMPTY;
		String telephoneSql = StringUtils.EMPTY;
		String startDateSql = "";
		String endDateSql = "";
		String isReleaseSql = "";
		String sourceSql = "";
		if (StringUtils.isNotBlank(netbarName)) {
			netbarNameSql = SqlJoiner.join(" and a.name like '%", netbarName, "%'");
		}
		if (StringUtils.isNotBlank(telephone)) {
			telephoneSql = SqlJoiner.join(" and a.telephone like '%", telephone, "%'");
		}
		if (StringUtils.isNotBlank(startDate)) {
			startDateSql = SqlJoiner.join(" and a.create_date>'", startDate, "'");
		}
		if (StringUtils.isNotBlank(endDate)) {
			endDateSql = SqlJoiner.join(" and a.create_date<'", endDate, "'");
		}
		if (isRelease != null) {
			isReleaseSql = " and a.is_release=" + isRelease;
		}
		if (source != null) {
			sourceSql = " and a.source=" + source;
		}
		sql = SqlJoiner.join("select count(1) from netbar_t_info a  where a.is_valid=1 ", isReleaseSql, startDateSql,
				sourceSql, endDateSql, netbarNameSql, telephoneSql);
		Number totalCount = queryDao.query(sql);
		if (totalCount != null) {
			vo.setTotal(totalCount.intValue());
		}
		int pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		int start = (page - 1) * pageSize;
		if (start + pageSize >= vo.getTotal()) {
			vo.setIsLast(1);
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", start);
		params.put("pageSize", pageSize);
		sql = SqlJoiner.join(
				"select a.*,x.realname from netbar_t_info a left join sys_t_user x on a.create_user_id=x.id ",
				" where a.is_valid=1 ", isReleaseSql, startDateSql, endDateSql, netbarNameSql, telephoneSql, sourceSql,
				" order by create_date desc limit :start,:pageSize");
		vo.setCurrentPage(page);
		vo.setList(queryDao.queryMap(sql, params));
		return vo;
	}

	/**
	 * 查找已发布和有效的网吧列表
	 * 
	 * @param isRelease
	 *            是否发布 0未发布 1发布
	 * @param valid
	 *            0无效 1有效
	 * @param name
	 *            网吧名称
	 * @param telephone
	 *            手机号
	 */
	public List<NetbarInfo> findByIsReleaseAndValid(int isRelease, int valid, String name, String telephone) {
		if (!StringUtils.isNotBlank(name) && !StringUtils.isNotBlank(telephone)) {
			return new ArrayList<NetbarInfo>();
		}
		if (name == null) {
			name = "";
		}
		name = SqlJoiner.join("%", name, "%");
		if (telephone == null) {
			telephone = "";
		}
		telephone = SqlJoiner.join("%", telephone, "%");
		return netbarInfoDao.findByIsReleaseAndValidAndNameLikeAndTelephoneLike(isRelease, valid, name, telephone);
	}

	/**
	 * 查找未被申领的已发布网吧
	 * 
	 * @param isRelease
	 *            是否发布 0未发布 1发布
	 * @param valid
	 *            0无效 1有效
	 * @param name
	 *            网吧名称
	 * @param telephone
	 *            手机号
	 */
	public List<Map<String, Object>> findUnRegedMerchantNetbar(String name, String telephone, String areaCode,
			long createUserId) {
		if (!StringUtils.isNotBlank(name) && !StringUtils.isNotBlank(telephone) && StringUtils.isBlank(areaCode)) {
			return Lists.newArrayList();
		}
		String areaSql = "";
		if (StringUtils.isNotBlank(areaCode)) {
			areaSql = " and n.area_code like '" + areaCode + "%'";
		}
		if (name == null) {
			name = "";
		}
		name = SqlJoiner.join("%", name, "%");
		if (telephone == null) {
			telephone = "";
		}
		telephone = SqlJoiner.join("%", telephone, "%");

		String sql = "select name ,telephone ,id,address from    netbar_t_info n "
				+ " where n.is_valid = 1     and n.is_release = 1" + areaSql + "  and n.name like '" + name
				+ "'    and n.telephone like '" + telephone + "'    and n.id not in "
				+ "  (select         netbar_id    from       netbar_t_merchant    where is_valid = 1 and netbar_id is not null)";

		if (createUserId > 0) {
			sql = sql + " and n.id  in "
					+ "  (select         netbar_id    from netbar_t_info_tmp where is_valid = 1 and clerk_user_id= "
					+ createUserId + ")";
		}

		return queryDao.queryMap(sql);
	}

	/**
	 * 通过id字符串查询网吧列表
	 */
	public List<NetbarInfo> findByIds(String ids) {
		List<Long> idLongs = new ArrayList<Long>();
		String[] idList = ids.split(",");
		for (String id : idList) {
			if (NumberUtils.isNumber(id)) {
				idLongs.add(NumberUtils.toLong(id));
			}
		}
		return netbarInfoDao.findByValidAndIdIn(CommonConstant.INT_BOOLEAN_TRUE, idLongs);
	}

	/**
	 * 通过areaCode查询网吧数据
	 */
	public List<NetbarInfo> findByAreaCodeLike(String areaCode) {
		Joiner join = Joiner.on("");
		if (areaCode.length() > 4) {
			areaCode = areaCode.substring(0, 4);
		}
		String queryAreaCode = join.join(areaCode, "%");
		return netbarInfoDao.findByAreaCodeLikeAndValidAndIsRelease(queryAreaCode, CommonConstant.INT_BOOLEAN_TRUE,
				CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * 查询期望网吧、地区的 树结构
	 */
	public List<Map<String, Object>> getAreasNetbarsTree(String netbars, String areas) {
		boolean onlyValidArea = true;
		List<SystemArea> areasTree = systemAreaService.getTree(areas, onlyValidArea);
		List<NetbarInfo> netbarInfos = findByIds(netbars);

		List<Map<String, Object>> root = new ArrayList<Map<String, Object>>();
		for (SystemArea province : areasTree) {
			Map<String, Object> mapProvince = new HashMap<String, Object>();
			mapProvince.put("id", province.getId());
			mapProvince.put("name", province.getName());
			mapProvince.put("open", true);
			List<Map<String, Object>> mapCities = new ArrayList<Map<String, Object>>();
			if (CollectionUtils.isNotEmpty(province.getChildren())) {
				for (SystemArea city : province.getChildren()) {
					Map<String, Object> mapCity = new HashMap<String, Object>();
					mapCity.put("id", city.getId());
					mapCity.put("name", city.getName());
					mapCity.put("open", true);
					List<Map<String, Object>> mapNetbars = new ArrayList<Map<String, Object>>();
					if (CollectionUtils.isNotEmpty(netbarInfos)) {
						for (Iterator<NetbarInfo> netbarsIt = netbarInfos.iterator(); netbarsIt.hasNext();) {
							NetbarInfo netbar = netbarsIt.next();
							if (city.getAreaCode().startsWith(netbar.getAreaCode().substring(0, 4))) {
								Map<String, Object> mapNetbar = new HashMap<String, Object>();
								mapNetbar.put("id", netbar.getId());
								mapNetbar.put("name", netbar.getName());
								mapNetbars.add(mapNetbar);
								netbarsIt.remove();
							}
						}
					}
					if (CollectionUtils.isNotEmpty(mapNetbars)) {
						mapCity.put("children", mapNetbars);
						mapCities.add(mapCity);
					}
				}
			}
			if (CollectionUtils.isNotEmpty(mapCities)) {
				mapProvince.put("children", mapCities);
				root.add(mapProvince);
			}
		}
		return root;
	}

	/**
	 * 录入系统区域管理员分配到的网吧
	 * 
	 * @return
	 */
	public PageVO queryNetbarForStatistics(String areaCode, String startDate, String endDate, Integer isAssigned,
			String netbarName, String telephone, Integer page, Long userId, boolean isPage, Long searchId,
			Integer levels) {
		String ids = "";
		if (searchId != null) {
			ids = getSubAccountIds(searchId);
		} else {
			ids = getSubAccountIds(userId);
		}
		String createDateSql = "";
		String updateDateSql = "";
		String b_createDateSql = "";
		if (StringUtils.isNotBlank(startDate)) {
			createDateSql = SqlJoiner.join(" and a.create_date>='" + startDate + "' and a.create_date<'",
					endDate + "'");
			b_createDateSql = SqlJoiner.join(" and b.create_date>='" + startDate + "' and b.create_date<'",
					endDate + "'");
			updateDateSql = SqlJoiner.join(" and a.update_date>='" + startDate + "' and a.update_date<'",
					endDate + "'");
		}
		Map<String, Object> params = new HashMap<String, Object>();
		int pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		int start = (page - 1) * pageSize;
		String limitSql = "";
		if (isPage == true) {
			params.put("start", start);
			params.put("pageSize", pageSize);
			limitSql = " limit :start,:pageSize";
		}
		String conditionSql = " where 1=1 ";
		if (StringUtils.isNotBlank(areaCode)) {
			conditionSql = SqlJoiner.join(conditionSql, " and area_code like '", areaCode, "'");
		}
		if (isAssigned != null) {
			if (isAssigned == 0) {
				conditionSql = SqlJoiner.join(conditionSql, " and isAssigned=0 ");
			} else {
				conditionSql = SqlJoiner.join(conditionSql, " and isAssigned=1 ");
			}
		}
		if (StringUtils.isNotBlank(netbarName)) {
			netbarName = "%" + netbarName + "%";
			conditionSql = SqlJoiner.join(conditionSql, " and name like '", netbarName, "'");
		}
		if (StringUtils.isNotBlank(telephone)) {
			telephone = "%" + telephone + "%";
			conditionSql = SqlJoiner.join(conditionSql, " and telephone like '", telephone, "'");
		}
		if (levels != null) {
			conditionSql += " and levels=" + levels;
		}
		String inSql = "";
		StringBuilder sb = new StringBuilder();
		String netbarIdsSql = SqlJoiner.join(
				"select id from (SELECT a.levels,a.id,a.name,a.address,a.telephone,a.area_code,count(b.id) total_member_num,c.first_name,c.second_name,c.third_name,c.fourth_name,CASE WHEN (d.user_type = 8) OR (c.sys_user_id <> ",
				String.valueOf(userId),
				") THEN 1 ELSE 0 END isAssigned FROM (netbar_t_info a,sys_r_user_netbar c,sys_t_user d) LEFT JOIN netbar_r_user b ON a.id = b.netbar_id WHERE a.is_valid = 1 and a.is_release=1 AND a.id = c.netbar_id AND c.sys_user_id IN (",
				ids, ") and c.sys_user_id = d.id GROUP BY a.id)z ", conditionSql, limitSql);
		List<Map<String, Object>> idList = queryDao.queryMap(netbarIdsSql, params);
		if (CollectionUtils.isNotEmpty(idList)) {
			for (Map<String, Object> map : idList) {
				sb.append(map.get("id").toString() + ",");
			}
			sb = sb.replace(sb.length() - 1, sb.length(), "");
			inSql = " and a.netbar_id in (" + sb + ")";
		}
		String totalMemberSql = SqlJoiner.join(
				"(select * from (SELECT a.levels,a.id,a.name,a.address,a.telephone,a.area_code,count(b.id) total_member_num,c.first_name,c.second_name,c.third_name,c.fourth_name,CASE WHEN (d.user_type = 8) OR (c.sys_user_id <> ",
				String.valueOf(userId),
				") THEN 1 ELSE 0 END isAssigned FROM (netbar_t_info a,sys_r_user_netbar c,sys_t_user d) LEFT JOIN netbar_r_user b ON a.id = b.netbar_id ",
				b_createDateSql,
				"WHERE a.is_valid = 1 and a.is_release=1  AND a.id = c.netbar_id AND c.sys_user_id IN (", ids,
				") and c.sys_user_id = d.id GROUP BY a.id)z ", conditionSql, limitSql, ")a left join ");
		String newMemberSql = SqlJoiner.join(
				"( SELECT a.netbar_id, count(1) new_member_num FROM netbar_r_user a WHERE a.is_first = 1 ", inSql,
				createDateSql, "GROUP BY a.netbar_id ) b ON a.id = b.netbar_id LEFT JOIN ");
		String acceptSql = SqlJoiner.join(
				"( SELECT a.netbar_id, count(1) accept_num FROM netbar_r_reservation a WHERE a.is_receive = 1 ", inSql,
				updateDateSql, "GROUP BY a.netbar_id ) c ON a.id = c.netbar_id LEFT JOIN ");
		String paySql = SqlJoiner.join(
				"( SELECT a.netbar_id, count(1) pay_num, sum(redbag_amount) redbag_amount, sum(amount) amount FROM netbar_r_order a WHERE a. STATUS >= 1 AND a.reserve_id = 0 ",
				inSql, createDateSql, "GROUP BY a.netbar_id ) d ON a.id = d.netbar_id LEFT JOIN ");
		String reserveSql = SqlJoiner.join(
				"( SELECT a.netbar_id, count(1) reserve_num FROM netbar_r_reservation a where a.is_valid=1 ", inSql,
				createDateSql, "GROUP BY a.netbar_id ) e ON a.id = e.netbar_id LEFT JOIN ");
		String battleSql = SqlJoiner.join(
				"( SELECT a.netbar_id, count(1) battle_num FROM activity_t_matches a where a.is_valid=1 ", inSql,
				createDateSql, " GROUP BY a.netbar_id ) f ON a.id = f.netbar_id LEFT JOIN ");
		String fundSql = "( SELECT a.netbar_id, a.usable_quota, a.usable_quota + a.accounts total_quota FROM netbar_fund_info a where a.is_valid = 1 "
				+ inSql + ") g ON a.id = g.netbar_id LEFT JOIN ";
		String resourceOrderSql = "( SELECT a.netbar_id, count(1) buy_num FROM netbar_resource_order a where a.is_valid = 1 and status = 3 "
				+ inSql + createDateSql + " GROUP BY a.netbar_id) h ON a.id = h.netbar_id ";
		String sql = "";
		PageVO vo = new PageVO();
		sql = SqlJoiner.join(
				"select count(1) from (SELECT  a.levels,a.id,a.name,a.address,a.telephone,a.area_code,CASE WHEN (c.user_type=8) OR (b.sys_user_id <> ",
				String.valueOf(userId),
				") THEN 1 ELSE 0 END isAssigned FROM (netbar_t_info a,sys_r_user_netbar b,sys_t_user c)  where a.is_valid = 1 and a.is_release=1 and a.id=b.netbar_id and b.sys_user_id=c.id AND b.sys_user_id IN(",
				ids, "))a ", conditionSql);
		Number totalCount = queryDao.query(sql);
		if (totalCount != null) {
			vo.setTotal(totalCount.intValue());
		}

		if (start + pageSize >= vo.getTotal()) {
			vo.setIsLast(1);
		}

		sql = SqlJoiner.join(
				"select a.id,a.name,a.address,a.telephone,a.area_code,total_member_num,IFNULL(new_member_num,0) new_member_num,IFNULL(accept_num,0) accept_num,IFNULL(pay_num,0) pay_num,IFNULL(redbag_amount,0) redbag_amount,IFNULL(amount,0) amount,IFNULL(reserve_num,0) reserve_num,IFNULL(battle_num,0) battle_num,first_name,second_name,third_name,fourth_name,isAssigned,ifnull(usable_quota,0) usable_quota,ifnull(total_quota,0) total_quota,ifnull(buy_num,0) buy_num from ",
				totalMemberSql, newMemberSql, acceptSql, paySql, reserveSql, battleSql, fundSql, resourceOrderSql);
		vo.setCurrentPage(page);
		vo.setList(queryDao.queryMap(sql, params));
		return vo;
	}

	/**
	 * 录入系统区域管理员分配到的网吧(全部数据)
	 * 
	 * @return
	 */
	public List<Map<String, Object>> queryNetbarForStatisticsAll(String areaCode, String startDate, String endDate,
			Integer isAssigned, String netbarName, String telephone, Long userId, Long searchId) {
		String ids = "";
		if (searchId != null) {
			ids = getSubAccountIds(searchId);
		} else {
			ids = getSubAccountIds(userId);
		}
		String createDateSql = "";
		String updateDateSql = "";
		if (StringUtils.isNotBlank(startDate)) {
			createDateSql = SqlJoiner.join(" and b.create_date>='" + startDate + "' and b.create_date<'",
					endDate + "'");
			updateDateSql = SqlJoiner.join(" and b.update_date>='" + startDate + "' and b.update_date<'",
					endDate + "'");
		}
		String conditionSql = " where 1=1 ";
		if (StringUtils.isNotBlank(areaCode)) {
			conditionSql = SqlJoiner.join(conditionSql, " and area_code like '", areaCode, "'");
		}
		if (isAssigned != null) {
			if (isAssigned == 0) {
				conditionSql = SqlJoiner.join(conditionSql, " and isAssigned=0 ");
			} else {
				conditionSql = SqlJoiner.join(conditionSql, " and isAssigned=1 ");
			}
		}
		if (StringUtils.isNotBlank(netbarName)) {
			netbarName = "%" + netbarName + "%";
			conditionSql = SqlJoiner.join(conditionSql, " and name like '", netbarName, "'");
		}
		if (StringUtils.isNotBlank(telephone)) {
			telephone = "%" + telephone + "%";
			conditionSql = SqlJoiner.join(conditionSql, " and telephone like '", telephone, "'");
		}

		String totalMemberSql = SqlJoiner.join(
				"(select * from (SELECT a.id,a. NAME,a.address,a.telephone,a.area_code,count(b.id) total_member_num,CASE WHEN (d.user_type = 8) OR (c.sys_user_id <> ",
				String.valueOf(userId),
				") THEN 1 ELSE 0 END isAssigned FROM (netbar_t_info a,sys_r_user_netbar c,sys_t_user d) LEFT JOIN netbar_r_user b ON a.id = b.netbar_id ",
				createDateSql,
				" WHERE a.is_valid = 1 and a.is_release=1  and a.id = c.netbar_id AND c.sys_user_id IN (", ids,
				") and c.sys_user_id = d.id GROUP BY a.id)z ", conditionSql, ")a left join ");
		String newMemberSql = SqlJoiner.join(
				"(select a.id,count(b.id) new_member_num from netbar_t_info a left join netbar_r_user b on a.id=b.netbar_id ",
				createDateSql, " and is_first=1 where a.is_valid=1 and a.is_release=1",
				" group by a.id)b on a.id=b.id left join ");
		String acceptSql = SqlJoiner.join(
				"(select a.id,count(b.id) accept_num from netbar_t_info a left join netbar_r_reservation b on a.id=b.netbar_id ",
				updateDateSql,
				" and b.is_receive=1 where a.is_valid=1 and a.is_release=1 group by a.id)c on c.id=a.id left join ");
		String paySql = SqlJoiner.join(
				"(select a.id,count(b.id) pay_num,sum(redbag_amount) redbag_amount,sum(amount) amount from netbar_t_info a left join netbar_r_order b on a.id=b.netbar_id and b.status>=1 and b.reserve_id=0 ",
				createDateSql, " where a.is_valid=1 and a.is_release=1 group by a.id)d on a.id=d.id left join ");
		String reserveSql = SqlJoiner.join(
				"(select a.id,count(b.id) reserve_num from netbar_t_info a left join netbar_r_reservation b on a.id=b.netbar_id ",
				createDateSql,
				" where a.is_valid=1 and a.is_release=1 and b.is_valid=1 group by a.id)e on e.id=a.id left join ");
		String battleSql = SqlJoiner.join(
				"(select a.id,count(b.id) battle_num from netbar_t_info a left join activity_t_matches b on a.id=b.netbar_id ",
				createDateSql, " where a.is_valid=1 and a.is_release=1 and b.is_valid=1 group by a.id)f on a.id=f.id ");
		String sql = SqlJoiner.join(
				"select IFNULL(sum(total_member_num),0) total_member_sum, IFNULL(sum(new_member_num),0) new_member_sum, IFNULL(sum(accept_num),0) accept_sum, IFNULL(sum(pay_num),0) pay_sum, IFNULL(sum(redbag_amount),0) redbag_sum, IFNULL(sum(amount),0) amount_sum, IFNULL(sum(reserve_num),0) reserve_sum, IFNULL(sum(battle_num),0) battle_sum from ",
				totalMemberSql, newMemberSql, acceptSql, paySql, reserveSql, battleSql);
		return queryDao.queryMap(sql);
	}

	/**
	 * 录入系统区域管理员分配到的网吧id
	 * 
	 * @return
	 */
	public List<Map<String, Object>> queryNetbarIdForStatistics(String areaCode, Integer isAssigned, String netbarName,
			String telephone, Long userId, Long searchId) {
		String ids = "";
		if (searchId != null) {
			ids = getSubAccountIds(searchId);
		} else {
			ids = getSubAccountIds(userId);
		}
		String conditionSql = " where a.is_valid=1 and a.is_release=1 and b.sys_user_id in(" + ids + ")";
		if (StringUtils.isNotBlank(areaCode)) {
			String[] array = areaCode.split("-");
			if (array != null && array.length > 0) {
				areaCode = array[array.length - 1];
				if (areaCode.substring(2, 6).equals("0000")) {
					areaCode = areaCode.substring(0, 2) + "%";
				} else if (areaCode.substring(4, 6).equals("00")) {
					areaCode = areaCode.substring(0, 4) + "%";
				}
				conditionSql = SqlJoiner.join(conditionSql, " and a.area_code like '", areaCode, "'");
			}
		}
		if (isAssigned != null) {
			if (isAssigned == 0) {
				conditionSql = SqlJoiner.join(conditionSql, " and c.user_type<>8 and  b.sys_user_id=",
						String.valueOf(userId), " ");
			} else {
				conditionSql = SqlJoiner.join(conditionSql, " and (c.user_type=8 or b.sys_user_id <> ",
						String.valueOf(userId), ") ");
			}
		}
		if (StringUtils.isNotBlank(netbarName)) {
			netbarName = "%" + netbarName + "%";
			conditionSql = SqlJoiner.join(conditionSql, " and a.name like '", netbarName, "'");
		}
		if (StringUtils.isNotBlank(telephone)) {
			telephone = "%" + telephone + "%";
			conditionSql = SqlJoiner.join(conditionSql, " and a.telephone like '", telephone, "'");
		}
		String sql = "";
		sql = SqlJoiner.join(
				"select a.id from netbar_t_info a left join sys_r_user_netbar b on a.id=b.netbar_id  left join sys_t_user c on b.sys_user_id=c.id ",
				conditionSql);
		return queryDao.queryMap(sql);
	}

	/**
	 * 得到当前账号子账号的id(递归)
	 * 
	 * @param userId
	 * @return
	 */
	public String getSubAccountIds(Long userId) {
		StringBuilder sb = new StringBuilder();
		SystemUser systemUser = systemUserDao.findOne(userId);
		if (systemUser != null) {
			sb.append(systemUser.getId() + ",");
			if (systemUser.getUserType() == 5) {
				List<SystemUser> second = systemUserDao.findByParentIdAndValid(systemUser.getId(), 1);
				for (SystemUser secondObj : second) {
					sb.append(secondObj.getId() + ",");
					List<SystemUser> third = systemUserDao.findByParentIdAndValid(secondObj.getId(), 1);
					for (SystemUser thirdObj : third) {
						sb.append(thirdObj.getId() + ",");
						List<SystemUser> fourth = systemUserDao.findByParentIdAndValid(thirdObj.getId(), 1);
						for (SystemUser fourObj : fourth) {
							sb.append(fourObj.getId() + ",");
						}
					}
				}
			} else if (systemUser.getUserType() == 6) {
				sb.append(systemUser.getId() + ",");
				List<SystemUser> third = systemUserDao.findByParentIdAndValid(systemUser.getId(), 1);
				for (SystemUser thirdObj : third) {
					sb.append(thirdObj.getId() + ",");
					List<SystemUser> fourth = systemUserDao.findByParentIdAndValid(thirdObj.getId(), 1);
					for (SystemUser fourObj : fourth) {
						sb.append(fourObj.getId() + ",");
					}
				}
			} else if (systemUser.getUserType() == 7) {
				sb.append(systemUser.getId() + ",");
				List<SystemUser> fourth = systemUserDao.findByParentIdAndValid(systemUser.getId(), 1);
				for (SystemUser fourObj : fourth) {
					sb.append(fourObj.getId() + ",");
				}
			}
		}
		return sb.substring(0, sb.length() - 1);
	}

	/**
	 * 当前账号次一级子账号
	 * 
	 * @param userId
	 * @return
	 */
	public List<SystemUser> getLevelDownAccount(Long userId) {
		return systemUserDao.findByParentIdAndValid(userId, 1);
	}

	public List<NetbarInfo> findByValid(Integer valid) {
		return netbarInfoDao.findByValid(valid);
	}

	/**
	 * 获取网吧的地区信息
	 */
	public SystemArea getNetbarArea(Long netbarId) {
		String sql = SqlJoiner.join(
				"SELECT a.* FROM sys_t_area a LEFT JOIN netbar_t_info n ON a.area_code = n.area_code WHERE n.id = ",
				netbarId.toString());
		Map<String, Object> areaMap = queryDao.querySingleMap(sql);
		if (MapUtils.isNotEmpty(areaMap)) {
			SystemArea area = new SystemArea();
			area.setId(MapUtils.getLong(areaMap, "id"));
			area.setName(MapUtils.getString(areaMap, "name"));
			area.setAreaCode(MapUtils.getString(areaMap, "area_code"));
			area.setPid(MapUtils.getLong(areaMap, "pid"));
			area.setOrderId(MapUtils.getInteger(areaMap, "order_id"));
			area.setPinyin(MapUtils.getString(areaMap, "pinyin"));
			area.setValid(MapUtils.getInteger(areaMap, "is_valid"));
			return area;
		}
		return null;
	}

	/**
	 * 根据网吧名模糊查询
	 */
	public List<NetbarInfo> findValidByName(String name) {
		String likeName = "%" + name + "%";
		return netbarInfoDao.findByNameLikeAndValid(likeName, CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * 更新网吧热标签
	 *
	 */
	public void updateHotTag() {
		String sql = "select a.id from netbar_t_info a,netbar_t_merchant b where a.is_valid=1 and is_release=1 and a.id=b.netbar_id and b.is_valid=1";
		List<Map<String, Object>> ids = queryDao.queryMap(sql);
		String id;
		for (Map<String, Object> m : ids) {
			id = m.get("id").toString();
			sql = SqlJoiner.join(
					"update netbar_t_info set hot = ( select sum(rate) * 0.2 rate from ( select count(1) * 0.2 rate from netbar_r_order where netbar_id =",
					id,
					" and status = 1 and is_valid = 2 and create_date > date_add(now(), interval - 1 day) and create_date < now() union all select count(1) * 0.8 rate from user_r_redbag a, sys_t_redbag b where a.netbar_id =",
					id,
					" and a.create_date > date_add(now(), interval - 1 day) and a.create_date < now() and a.redbag_id = b.id and b.type = 8 ) a ) where id = ",
					id);
			queryDao.update(sql);
		}
		sql = "select start, total, a.area_code from ( select count(a.id) total, substr(a.area_code, 1, 4) area_code from netbar_t_info a, netbar_t_merchant b where a.is_valid = 1 and is_release = 1 and a.id = b.netbar_id and b.is_valid = 1 group by substr(a.area_code, 1, 4)) a left join ( select round(count(a.id) * 0.25) start, substr(a.area_code, 1, 4) area_code from netbar_t_info a, netbar_t_merchant b where a.is_valid = 1 and is_release = 1 and a.id = b.netbar_id and b.is_valid = 1 group by substr(a.area_code, 1, 4)) b on a.area_code = b.area_code";
		List<Map<String, Object>> scope = queryDao.queryMap(sql);
		String start;
		String total;
		String areaCode;
		for (Map<String, Object> m : scope) {
			start = m.get("start").toString();
			total = m.get("total").toString();
			areaCode = m.get("area_code").toString();
			sql = SqlJoiner.join(
					"update netbar_t_info set hot = 0 where id in ( select id from ( select a.id from netbar_t_info a, netbar_t_merchant b where a.is_valid = 1 and is_release = 1 and a.id = b.netbar_id and b.is_valid = 1 and a.area_code like '",
					areaCode, "%' order by a.hot desc, id limit ", start, ", ", total, " ) a )");
			queryDao.update(sql);
		}
		System.out.println(1);
	}

	public List<Map<String, Object>> queryNetbarByLevelsAndAreacode(int level, String provinceCode) {
		String sql = "select * from netbar_t_info where is_valid=1 and  area_code like '"
				+ StringUtils.substring(provinceCode, 0, 2) + "%' and levels = " + level;
		return queryDao.queryMap(sql);
	}

	public List<Map<String, Object>> netbarAreaLimit(int isRelease, Long userId, String netbarName, String telephone,
			String areaCode, Integer source, String beginReleaseTime, String endReleaseTime) {

		String sql = StringUtils.EMPTY;
		String netbarNameSql = StringUtils.EMPTY;
		String telephoneSql = StringUtils.EMPTY;
		String areaCodeSql = StringUtils.EMPTY;
		String beginTimeSql = StringUtils.EMPTY;
		String endTimeSql = StringUtils.EMPTY;
		String sourceSql = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(netbarName)) {
			netbarNameSql = SqlJoiner.join(" and a.name like '%", netbarName, "%'");
		}
		if (StringUtils.isNotBlank(telephone)) {
			telephoneSql = SqlJoiner.join(" and a.telephone like '%", telephone, "%'");
		}
		if (StringUtils.isNotBlank(beginReleaseTime)) {
			beginTimeSql = SqlJoiner.join(" and a.release_date > '", beginReleaseTime, "'");
		}
		if (StringUtils.isNotBlank(endReleaseTime)) {
			endTimeSql = SqlJoiner.join(" and a.release_date < '", endReleaseTime, "'");
		}
		if (StringUtils.isNotBlank(areaCode)) {
			if (areaCode.substring(2, 6).equals("0000")) {
				areaCode = areaCode.substring(0, 2) + "%";
			} else if (areaCode.substring(4, 6).equals("00")) {
				areaCode = areaCode.substring(0, 4) + "%";
			}
			areaCodeSql = SqlJoiner.join(" and a.area_code like'", areaCode, "'");
		}
		if (source != null) {
			sourceSql = SqlJoiner.join(" and a.source=", String.valueOf(source));
		}

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("isRelease", isRelease);
		params.put("userId", userId);
		sql = SqlJoiner.join(
				"select a.*,x.realname from (netbar_t_info a,sys_t_area b) left join sys_t_user x on a.create_user_id=x.id ",
				" where a.is_release=:isRelease", netbarNameSql, telephoneSql, areaCodeSql, beginTimeSql, endTimeSql,
				sourceSql,
				" and a.is_valid=1 and a.area_code=b.area_code and b.id in(select area_id from sys_r_user_area where sys_user_id=:userId) ");
		return queryDao.queryMap(sql, params);
	}

	/**
	 * 查询平所有开通了商户端的网吧
	 */
	public List<Map<String, Object>> queryMerchantNetbars() {
		String sql = SqlJoiner.join("SELECT n.id, n.area_code areaCode",
				" FROM netbar_t_info n JOIN netbar_t_merchant m ON n.id = m.netbar_id",
				" WHERE n.is_release = 1 and m.is_valid = 1");
		return queryDao.queryMap(sql);
	}

	public List<Map<String, Object>> queryActivityNetbar(Long activityId, Integer round, String netbarName) {
		if (activityId == null || round == null || StringUtils.isBlank(netbarName)) {
			return null;
		}

		String sql = "SELECT n.id, n.`name` FROM activity_r_rounds r JOIN netbar_t_info n ON FIND_IN_SET(n.id, r.netbars) WHERE r.activity_id = "
				+ activityId + " AND round = " + round + " AND n.`name` = '" + netbarName + "' ORDER BY n.id";
		return queryDao.queryMap(sql);
	}

	public String findAreaCodeByNetbarId(long netbarIdLong) {
		String sql = "select area_code from netbar_t_info where id = " + netbarIdLong;
		return queryDao.query(sql);
	}

	public PageVO getNetbarList(Integer page, String netbarName, String proviceName, String cityName, String townName) {
		String querySql = "";
		if (StringUtils.isNotBlank(townName)) {
			querySql += " and a.area_code=" + townName;
		} else if (StringUtils.isNotBlank(cityName)) {
			cityName = cityName.substring(0, 4);
			querySql += " and a.area_code like '" + cityName + "%'";
		} else if (StringUtils.isNotBlank(proviceName)) {
			proviceName = proviceName.substring(0, 2);
			querySql += " and a.area_code like '" + proviceName + "%'";
		}
		if (StringUtils.isNotBlank(netbarName)) {
			querySql += " and a.name like '" + netbarName + "%'";
		}
		String totalSql = "select count(*) from netbar_t_info a left join sys_t_area b on a.area_code=b.area_code and b.is_valid=1 where a.is_valid=1 and a.is_release=1 ";
		totalSql += querySql;
		Number totalCount = queryDao.query(totalSql);
		if (totalCount != null && totalCount.intValue() > 0) {
			int pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
			int start = (page - 1) * pageSize;
			String limitSql = " limit " + start + "," + pageSize;
			String sql = "select a.name,a.id,a.area_code,a.address,b.name areaName,b.pid from netbar_t_info a left join sys_t_area b on a.area_code=b.area_code and b.is_valid=1 where a.is_valid=1 and a.is_release=1 ";
			sql += querySql + " order by a.id" + limitSql;
			List<Map<String, Object>> netbarList = queryDao.queryMap(sql);
			netbarList = getNetbarInfo(netbarList);
			PageVO vo = new PageVO(netbarList);
			if (page * pageSize >= totalCount.intValue()) {
				vo.setIsLast(1);
			}
			vo.setTotal(totalCount.intValue());
			return vo;
		}
		return new PageVO(new ArrayList<>());
	}

	/**
	 * 根据网吧pid值获取网吧地区信息
	 * 
	 * @return
	 */
	private List<Map<String, Object>> getNetbarInfo(List<Map<String, Object>> netbarList) {
		if (CollectionUtils.isNotEmpty(netbarList)) {
			for (int i = 0; i < netbarList.size(); i++) {
				Map<String, Object> map = netbarList.get(i);
				if (map.get("pid") != null) {
					if (NumberUtils.toLong(map.get("pid").toString()) == 1) {
						map.put("firName", map.get("areaName") != null ? map.get("areaName").toString() : "");
						map.put("secName", "");
						map.put("thiName", "");
						netbarList.set(i, map);
						continue;
					} else if (NumberUtils.toLong(map.get("pid").toString()) != 1) {
						Long pid = NumberUtils.toLong(map.get("pid").toString());
						SystemArea secArea = areaDao.findOne(pid);
						if (secArea != null && secArea.getPid() != null && secArea.getPid() == 1) {
							map.put("firName", secArea.getName());
							map.put("secName", map.get("areaName") != null ? map.get("areaName").toString() : "");
							map.put("thiName", "");
							netbarList.set(i, map);
							continue;
						} else if (secArea != null && secArea.getPid() != null && secArea.getPid() != 1) {
							SystemArea thiArea = areaDao.findOne(secArea.getPid());
							if (thiArea != null && thiArea.getPid() != null && thiArea.getPid() == 1) {
								map.put("firName", thiArea.getName());
								map.put("secName", secArea.getName());
								map.put("thiName", map.get("areaName") != null ? map.get("areaName").toString() : "");
								netbarList.set(i, map);
							}
							continue;
						}
					}

				}
			}
		}
		return netbarList;
	}

	/**
	 * 查询网吧所占的省份
	 */
	public int getProviceCount(String ids) {
		String sql = "select count(distinct left(area_code,2)) as provinceCount from netbar_t_info where id in(" + ids
				+ ")";
		Number count = queryDao.query(sql);
		if (count != null && count.intValue() > 0) {
			return count.intValue();
		}
		return 0;
	}
}