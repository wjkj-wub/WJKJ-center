package com.miqtech.master.service.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CacheKeyConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.common.IndexHotDao;
import com.miqtech.master.entity.common.IndexHot;
import com.miqtech.master.entity.common.SystemArea;
import com.miqtech.master.service.system.SystemAreaService;
import com.miqtech.master.utils.AreaUtil;
import com.miqtech.master.utils.SqlJoiner;

@Component
public class IndexHotService {
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private IndexHotDao indexHotDao;
	@Autowired
	private IndexService indexService;
	@Autowired
	private ObjectRedisOperateService objectRedisOperateService;
	@Autowired
	private AthleticsService athleticsService;
	@Autowired
	private SystemAreaService systemAreaService;

	/**首页热门赛事
	 * @param areaCode
	 * @return
	 */
	public List<Map<String, Object>> queryHotActivity(String areaCode) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		String sql = SqlJoiner
				.join("SELECT a.id, 10 type, b.title, a.sort, b.icon img FROM ( index_hot a, activity_t_info b ) WHERE a.is_valid=1 and a.type = 1 AND a.target_id = b.id AND a.area_code =",
						areaCode,
						" UNION SELECT a.id, 11 type, b.title, a.sort, c.icon img FROM ( index_hot a, amuse_t_activity b, amuse_r_activity_icon c ) WHERE a.is_valid=1 and a.type = 2 AND a.target_id = b.id AND b.id = c.activity_id AND c.is_main = 1 AND c.is_valid = 1 AND a.area_code =",
						areaCode, " ORDER BY sort DESC");
		result.addAll(queryDao.queryMap(sql));
		return result;
	}

	/**竞技大厅卡片转动区
	 * @param areaCode
	 * @return
	 */
	public List<Map<String, Object>> queryAthletics(String areaCode) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		String sql = SqlJoiner
				.join("SELECT a.id, 10 type, b.title, a.sort, b.icon img FROM ( index_hot a, activity_t_info b ) WHERE a.is_valid=1 and a.type = 4 AND a.target_id = b.id AND a.area_code =",
						areaCode,
						" UNION SELECT a.id, 11 type, b.title, a.sort, c.icon img FROM ( index_hot a, amuse_t_activity b, amuse_r_activity_icon c ) WHERE a.is_valid=1 and a.type = 5 AND a.target_id = b.id AND b.id = c.activity_id AND c.is_main = 1 AND c.is_valid = 1 AND a.area_code =",
						areaCode, " ORDER BY sort DESC");
		result.addAll(queryDao.queryMap(sql));
		return result;
	}

	/**竞技大厅官方活动推荐
	 * @param areaCode
	 * @return
	 */
	public List<Map<String, Object>> queryAthleticsActivity(String areaCode) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		String sql = SqlJoiner
				.join("SELECT a.id, 10 type, b.title, a.sort, b.icon img FROM ( index_hot a, activity_t_info b ) WHERE a.is_valid=1 and a.type = 6 AND a.target_id = b.id AND a.area_code =",
						areaCode, " ORDER BY sort DESC");
		result.addAll(queryDao.queryMap(sql));
		return result;
	}

	/**竞技大厅娱乐赛推荐
	 * @param areaCode
	 * @return
	 */
	public List<Map<String, Object>> queryAthleticsAmuse(String areaCode) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		String sql = SqlJoiner
				.join("SELECT a.id, 11 type, b.title, a.sort, c.icon img FROM ( index_hot a, amuse_t_activity b, amuse_r_activity_icon c ) WHERE a.is_valid=1 and a.type = 7 AND a.target_id = b.id AND b.id = c.activity_id AND c.is_main = 1 AND c.is_valid = 1 AND a.area_code =",
						areaCode, " ORDER BY sort DESC");
		result.addAll(queryDao.queryMap(sql));
		return result;
	}

	public IndexHot findById(Long id) {
		return indexHotDao.findOne(id);
	}

	public void save(IndexHot indexHot) {
		indexHotDao.save(indexHot);
	}

	public int queryNumByAreaCode(Integer tab, String areaCode) {
		String typeSql = "";
		if (tab == 6) {
			typeSql = " and (type=4 or type=5)";
		} else if (tab == 7) {
			typeSql = " and type=6 ";
		} else if (tab == 8) {
			typeSql = " and type=7 ";
		}
		String sql = SqlJoiner
				.join("select count(1) from index_hot where is_valid=1 and area_code=", areaCode, typeSql);
		Number num = queryDao.query(sql);
		if (num != null) {
			return num.intValue();
		}
		return -1;
	}

	public void addToCache(String areaCode, Integer tab) {
		if (areaCode == null || tab == null) {
			return;
		}
		String code = null;
		areaCode = AreaUtil.getProvinceCode(areaCode);
		if (tab == 2) {
			List<Map<String, Object>> list = indexService.queryHotActivity(areaCode);
			objectRedisOperateService.setData(CacheKeyConstant.API_CACHE_HOT_ACTIVITY + areaCode, list);
			if (areaCode.equals("00")) {
				List<SystemArea> areaList = systemAreaService.queryValidRoot();
				for (SystemArea area : areaList) {
					code = AreaUtil.getProvinceCode(area.getAreaCode());
					list = indexService.queryHotActivity(code);
					objectRedisOperateService.setData(CacheKeyConstant.API_CACHE_HOT_ACTIVITY + code, list);
				}
			}
		} else if (tab == 6) {
			List<Map<String, Object>> list = athleticsService.queryAthleticsRecomend(areaCode);
			objectRedisOperateService.setData(CacheKeyConstant.API_CACHE_CARD + areaCode, list);
			if (areaCode.equals("00")) {
				List<SystemArea> areaList = systemAreaService.queryValidRoot();
				for (SystemArea area : areaList) {
					code = AreaUtil.getProvinceCode(area.getAreaCode());
					list = athleticsService.queryAthleticsRecomend(code);
					objectRedisOperateService.setData(CacheKeyConstant.API_CACHE_CARD + code, list);
				}
			}
		} else if (tab == 7) {
			Map<String, Object> map = athleticsService.activityRecommend(areaCode, 1);
			objectRedisOperateService.setData(CacheKeyConstant.API_CACHE_ACTIVITY_RECOMMEND + areaCode, map);
			if (areaCode.equals("00")) {
				List<SystemArea> areaList = systemAreaService.queryValidRoot();
				for (SystemArea area : areaList) {
					code = AreaUtil.getProvinceCode(area.getAreaCode());
					map = athleticsService.activityRecommend(code, 1);
					objectRedisOperateService.setData(CacheKeyConstant.API_CACHE_ACTIVITY_RECOMMEND + code, map);
				}
			}
		} else if (tab == 8) {
			Map<String, Object> map = athleticsService.activityRecommend(areaCode, 2);
			objectRedisOperateService.setData(CacheKeyConstant.API_CACHE_AMUSE_RECOMMEND + areaCode, map);
			if (areaCode.equals("00")) {
				List<SystemArea> areaList = systemAreaService.queryValidRoot();
				for (SystemArea area : areaList) {
					code = AreaUtil.getProvinceCode(area.getAreaCode());
					map = athleticsService.activityRecommend(code, 2);
					objectRedisOperateService.setData(CacheKeyConstant.API_CACHE_AMUSE_RECOMMEND + code, map);
				}
			}
		}
	}
}
