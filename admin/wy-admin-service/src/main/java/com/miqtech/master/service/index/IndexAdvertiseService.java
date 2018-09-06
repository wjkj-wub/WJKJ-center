package com.miqtech.master.service.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.criteria.Predicate;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
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
import com.miqtech.master.consts.CacheKeyConstant;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.common.IndexAdvertiseDao;
import com.miqtech.master.entity.common.IndexAdvertise;
import com.miqtech.master.entity.common.SystemArea;
import com.miqtech.master.service.common.ObjectRedisOperateService;
import com.miqtech.master.service.system.SystemAreaService;
import com.miqtech.master.utils.AreaUtil;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

/**
 * 首页banner广告
 */
@Component
public class IndexAdvertiseService {
	private static final Logger LOGGER = LoggerFactory.getLogger(IndexAdvertiseService.class);

	public static final String API_CACHE_AD = "api_cache_ad"; // 广告banner缓存
	@Autowired
	private IndexAdvertiseDao indexAdvertiseDao;
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private ObjectRedisOperateService objectRedisOperateService;
	@Autowired
	private SystemAreaService systemAreaService;

	public void clearCache() {
		try {
			objectRedisOperateService.delData(API_CACHE_AD);
		} catch (Exception e) {
		}
	}

	/**
	 * 返回所有有效banner广告信息
	 */
	public List<IndexAdvertise> findAll() {
		return indexAdvertiseDao.findByValid(1);
	}

	/**
	 * 根据地区（省份）返回所有有效广告信息
	 * deviceType，设备类型：0-全部；1-IOS；2-Android；3...
	 */
	public List<Map<String, Object>> getAds(String areaCode, int deviceTpe, Integer belong, boolean upLimitVersion) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		String areaCodeSql = "'000000'";
		if (StringUtils.isNotBlank(areaCode)) {
			areaCodeSql = areaCodeSql + ",'" + areaCode + "'";
		}
		String deviceSql = StringUtils.EMPTY;
		if (deviceTpe != 0) {
			deviceSql = " and (d.device_type is null or d.device_type in(0," + deviceTpe + "))";
		}
		String limitVersionSql = "";
		if (!upLimitVersion) {
			limitVersionSql = " and d.type not in(15,16) ";
		}
		String sqlQuery = "";
		String sql = SqlJoiner.join(
				"select d.id, d.title, d.describe, d.img, if(locate('v2/activity/info/share/info',d.url)>0,substring(d.url,locate('=',d.url)+1,length(d.url)),d.target_id) targetId, d.type, d.url,d.sort from index_t_advertise d",
				" right join index_r_advertise_area a on a.advertise_id=d.id  AND a.area_code ='000000' ",
				" where a.is_valid=1 and d.is_valid=1 and d.belong=", String.valueOf(belong), deviceSql,
				limitVersionSql, " ORDER BY a.area_code desc,d.sort DESC limit 0,5");
		if (belong != null) {
			if (StringUtils.isBlank(areaCode)) {
				result.addAll(queryDao.queryMap(sql));
				return result;
			} else {
				areaCodeSql = " AND a.area_code like '" + areaCode + "%' ";
				sqlQuery = SqlJoiner.join(
						"select d.id, d.title, d.describe, d.img, if(locate('v2/activity/info/share/info',d.url)>0,substring(d.url,locate('=',d.url)+1,length(d.url)),d.target_id) targetId, d.type, d.url,d.sort from index_t_advertise d",
						" right join index_r_advertise_area a on a.advertise_id=d.id ", areaCodeSql,
						" where a.is_valid=1 and d.is_valid=1 and d.belong=", String.valueOf(belong), deviceSql,
						limitVersionSql, " ORDER BY a.area_code desc,d.sort DESC limit 0,3");
				if (!areaCode.equals("00")) {
					result.addAll(queryDao.queryMap(sqlQuery));
				}
				result.addAll(queryDao.queryMap(sql));
			}
		} else {
			sqlQuery = SqlJoiner.join(
					"select d.id, d.title, d.describe, d.img, if(locate('v2/activity/info/share/info',d.url)>0,substring(d.url,locate('=',d.url)+1,length(d.url)),d.target_id) targetId, d.type, d.url,d.sort from index_t_advertise d",
					" right join index_r_advertise_area a on a.advertise_id=d.id and a.area_code in(" + areaCodeSql
							+ ")",
					" where a.is_valid=1 and d.is_valid=1 and d.belong=0", deviceSql, limitVersionSql,
					" order by d.sort desc");
			result.addAll(queryDao.queryMap(sqlQuery));
		}
		return result;
	}

	/**
	 * 根据ID查询
	 */
	public IndexAdvertise findById(Long id) {
		return indexAdvertiseDao.findById(id);
	}

	/**
	 * 保存广告
	 */
	public IndexAdvertise saveOrUpdate(IndexAdvertise indexAdvertise) {
		return indexAdvertiseDao.save(indexAdvertise);
	}

	/**
	 * 根据ID删除广告(is_valid置为0)
	 */
	public void deleteById(Long id) {
		IndexAdvertise indexAdvertise = indexAdvertiseDao.findOne(id);
		if (indexAdvertise != null) {
			indexAdvertise.setValid(CommonConstant.INT_BOOLEAN_FALSE);
			indexAdvertiseDao.save(indexAdvertise);
		}
	}

	/**
	 * 恢复删除的广告(is_valid置为1)
	 */
	public void recoverById(long id) {
		IndexAdvertise indexAdvertise = indexAdvertiseDao.findOne(id);
		if (indexAdvertise != null && CommonConstant.INT_BOOLEAN_FALSE.equals(indexAdvertise.getValid())) {
			indexAdvertise.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			saveOrUpdate(indexAdvertise);
		}
	}

	/**
	 * 首页广告后台管理，分页
	 */
	public List<Map<String, Object>> pageList(int page, Map<String, Object> params) {
		if (page < 1) {
			page = 1;
		}
		int rows = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		int start = (page - 1) * rows;
		if (null == params.get("valid")) {
			params.put("valid", 1);
		}
		String sqlQuery = SqlJoiner.join(
				"select i.id, i.title, i.describe, i.img, i.target_id targetId, i.type, i.url, i.sort, i.is_valid valid, i.create_date createDate, i.device_type deviceType",
				" from index_t_advertise i");
		String sqlCount = "select count(1) from index_t_advertise i";
		if (null != params.get("areaCode")) {
			sqlQuery = SqlJoiner.join(sqlQuery,
					" right join index_r_advertise_area a on a.advertise_id=i.id and a.area_code=:areaCode");
			sqlCount = SqlJoiner.join(sqlCount,
					" right join index_r_advertise_area a on a.advertise_id=i.id and a.area_code=:areaCode");
		}
		sqlQuery = SqlJoiner.join(sqlQuery, " where i.is_valid=:valid");
		sqlCount = SqlJoiner.join(sqlCount, " where i.is_valid=:valid");
		if (null != params.get("title")) {
			sqlQuery = SqlJoiner.join(sqlQuery, " and i.title like concat('%',:title,'%')");
			sqlCount = SqlJoiner.join(sqlCount, " and i.title like concat('%',:title,'%')");
		}

		Map<String, Object> totalMap = queryDao.querySingleMap(sqlCount, params);
		sqlQuery = SqlJoiner.join(sqlQuery, " order by i.create_date desc limit :start, :rows");
		params.put("start", start);
		params.put("rows", rows);
		List<Map<String, Object>> list = queryDao.queryMap(sqlQuery, params);

		if (null != totalMap && null != totalMap.get("count(1)")) {
			params.put("total", totalMap.get("count(1)"));
		} else {
			params.put("total", "0");
		}
		return list;
	}

	/**
	 * 分页查询
	 */
	public Page<IndexAdvertise> page(int page, Map<String, Object> params) {
		if (null == params.get("valid")) {
			params.put("valid", CommonConstant.INT_BOOLEAN_TRUE);
		}
		return indexAdvertiseDao.findAll(buildSpecification(params), buildPageRequest(page));
	}

	/**
	 * 分页查询条件
	 */
	private Specification<IndexAdvertise> buildSpecification(final Map<String, Object> searchParams) {
		Specification<IndexAdvertise> spec = (root, query, cb) -> {
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
	 * 首页,竞技banner推荐
	 */
	public List<Map<String, Object>> indexBannerRecommend(String areaCode, String belong) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		String sql = SqlJoiner.join(
				"select * from (select a.device_type,a.id,a.sort,if(a.img is null,b.icon,a.img) img,b.title,a.describe,a.type,a.url from (index_t_advertise a,activity_t_info b,index_r_advertise_area z)",
				" where a.target_id=b.id and a.belong=", belong,
				" and a.type=10 and a.is_valid=1 and a.id=z.advertise_id and z.area_code=", areaCode,
				" union select a.device_type,a.id,a.sort,if(a.img is null,c.icon,a.img) img,b.title,a.describe,a.type,a.url from (index_t_advertise a,amuse_t_activity b,amuse_r_activity_icon c,index_r_advertise_area z)",
				" where a.target_id=b.id and b.id=c.activity_id and c.is_main=1 and c.is_valid=1 and a.belong=", belong,
				" and a.type=11 and a.is_valid=1 and a.id=z.advertise_id and z.area_code=", areaCode,
				" union select a.device_type,a.id,a.sort,a.img,b.title,a.describe,a.type,a.url from (index_t_advertise a,activity_t_matches b,index_r_advertise_area z)",
				" where a.target_id=b.id and a.belong=", belong,
				" and a.type=12 and a.is_valid=1 and a.id=z.advertise_id and z.area_code=", areaCode,
				" union select a.device_type,a.id,a.sort,a.img,b.title,a.describe,a.type,a.url from (index_t_advertise a,activity_t_over_activities b,index_r_advertise_area z)",
				" where a.target_id=b.id and a.belong=", belong,
				" and a.type=15 and a.is_valid=1 and a.id=z.advertise_id and z.area_code=", areaCode,
				" union select a.device_type,a.id,a.sort,a.img,b.title title,a.describe,a.type,a.url from (index_t_advertise a,bounty b,index_r_advertise_area z)",
				" where a.target_id=b.id and a.belong=", belong,
				" and a.type=17 and a.is_valid=1 and a.id=z.advertise_id and z.area_code=", areaCode,
				" union select a.device_type,a.id,a.sort,a.img,a.title,a.describe,a.type,a.url from (index_t_advertise a,index_r_advertise_area z)",
				" where a.belong=", belong,
				" and (a.type=5 or a.type=13 or a.type=14) and a.is_valid=1 and a.id=z.advertise_id and z.area_code=",
				areaCode, ")a order by sort desc");
		result.addAll(queryDao.queryMap(sql));
		return result;
	}

	public int queryAreaNum(String areaCode, Integer belong) {
		String sql = SqlJoiner.join(
				"select count(1) from index_r_advertise_area a left join index_t_advertise b on a.advertise_id=b.id and b.is_valid=1 and b.type>=5 where a.area_code=",
				areaCode, " and b.belong=", String.valueOf(belong));
		Number total = queryDao.query(sql);
		if (total != null) {
			return total.intValue();
		}
		return -1;
	}

	public void addToCache(String areaCode, Integer belong) {
		if (areaCode == null || belong == null) {
			return;
		}
		String code = null;
		areaCode = AreaUtil.getProvinceCode(areaCode);
		for (int i = 0; i < 3; i++) {
			List<Map<String, Object>> list = this.getAds(areaCode, i, belong, true);
			objectRedisOperateService.setData(CacheKeyConstant.API_CACHE_AD + areaCode + i + belong, list);
			if (areaCode.equals("00")) {
				List<SystemArea> areaList = systemAreaService.queryValidRoot();
				if (CollectionUtils.isNotEmpty(areaList)) {
					for (SystemArea area : areaList) {
						code = AreaUtil.getProvinceCode(area.getAreaCode());
						list = this.getAds(code, i, belong, true);
						objectRedisOperateService.setData(CacheKeyConstant.API_CACHE_AD + code + i + belong, list);
					}
				}
			}
		}
	}

	//-------------------------------------前后端分离，查询后台广告分页数据
	/**
	 * 首页广告后台管理，分页
	 */
	public PageVO pageListByBelong(int page, Map<String, Object> params) {
		if (page < 1) {
			page = 1;
		}
		int rows = PageUtils.ADMIN_DEFAULT_PAGE_SIZE / 2;
		int start = (page - 1) * rows;
		if (null == params.get("valid")) {
			params.put("valid", 1);
		}
		String sqlQuery = SqlJoiner.join(
				"select i.id, i.title, i.describe, i.img, i.target_id targetId, i.type, i.url, i.sort, i.is_valid valid, i.create_date createDate,",
				" i.device_type deviceType,i.server_start_date server_start_date ,i.server_end_date server_end_date,",
				" case when i.status=0 then 0 when now()<server_start_date and i.status=1  then 1 ",
				" when now()> server_start_date and (now()<server_end_date or server_end_date is null ) and i.status =1  then 2 ",
				" else  3 end status  from index_t_advertise i");
		String sqlCount = "select count(1) from index_t_advertise i";
		//		if (null != params.get("areaCode")) {
		//			sqlQuery = SqlJoiner.join(sqlQuery,
		//					" right join index_r_advertise_area a on a.advertise_id=i.id and a.area_code=:areaCode");
		//			sqlCount = SqlJoiner.join(sqlCount,
		//					" right join index_r_advertise_area a on a.advertise_id=i.id and a.area_code=:areaCode");
		//		}
		sqlQuery = SqlJoiner.join(sqlQuery, " where i.is_valid=:valid");
		sqlCount = SqlJoiner.join(sqlCount, " where i.is_valid=:valid");
		if (null != params.get("title")) {
			sqlQuery = SqlJoiner.join(sqlQuery, " and i.title like concat('%',:title,'%')");
			sqlCount = SqlJoiner.join(sqlCount, " and i.title like concat('%',:title,'%')");
		}
		if (null != params.get("belong")) {
			sqlQuery = SqlJoiner.join(sqlQuery, " and i.belong =:belong ");
			sqlCount = SqlJoiner.join(sqlCount, " and i.belong =:belong ");
		}

		params.remove("page");
		Map<String, Object> totalMap = queryDao.querySingleMap(sqlCount, params);
		int totalCount = NumberUtils.toInt(totalMap.get("count(1)").toString());
		if (totalCount <= 0) {
			PageVO vo = new PageVO();
			vo.setTotalPage(1);
			return vo;
		}

		sqlQuery = SqlJoiner.join(sqlQuery, " order by i.id desc limit :start, :rows");
		params.put("start", start);
		params.put("rows", rows);
		List<Map<String, Object>> list = queryDao.queryMap(sqlQuery, params);

		if (null != totalMap && null != totalMap.get("count(1)")) {
			params.put("total", totalMap.get("count(1)"));
		} else {
			params.put("total", "0");
		}

		PageVO vo = new PageVO();
		vo.setList(list);
		vo.setCurrentPage(page);
		vo.setTotal(totalCount);
		int isLast = totalCount > page * rows ? 1 : 0;
		vo.setIsLast(isLast);
		int totalPage = (int) Math.ceil((double) totalCount / rows);
		vo.setTotalPage(totalPage == 0 ? 1 : totalPage);
		return vo;
	}

	public void unShelfById(Long id) {
		IndexAdvertise indexAdvertise = indexAdvertiseDao.findOne(id);
		if (indexAdvertise != null) {
			indexAdvertise.setStatus(0);
			indexAdvertiseDao.save(indexAdvertise);
		}
	}

	/**
	 * 统计指定广告的某段时间的按天展示和点击
	 * @param adId 广告id
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 * @param orderType
	 * @return
	 */
	public List<Map<String, Object>> statistic(Long adId, String startDate, String endDate, int orderType) {
		//		String sql = "select * from (select date_format(create_date, '%Y-%m-%d') date,sum(case type when 1 then 1 else 0 end)  imp,sum(case type when 2 then 1 else 0 end)  click "
		//				+ " from index_t_advertise_log where ad_id=" + adId.longValue() + " and  create_date >'" + startDate
		//				+ "' and create_date<='" + endDate + "' group by date_format(create_date, '%Y-%m-%d') ) a order by ";

		String sql = "select imp,click,date_format(date, '%Y-%m-%d') date from index_t_advertise_log_stastics where ad_id="
				+ adId.longValue() + " and  date >'" + startDate + "' and date<='" + endDate + "' order by ";

		switch (orderType) {
		case 0:
			sql = sql + " date desc";
			break;
		case 1:
			sql = sql + " imp desc";
			break;
		case 2:
			sql = sql + " click desc";
			break;
		default:
			sql = sql + " date";
			break;
		}

		return queryDao.queryMap(sql);

	}

}