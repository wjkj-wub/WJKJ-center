package com.miqtech.master.service.common;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CacheKeyConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.common.IndexMidImgDao;
import com.miqtech.master.entity.common.IndexMidImg;
import com.miqtech.master.entity.common.SystemArea;
import com.miqtech.master.service.system.SystemAreaService;
import com.miqtech.master.utils.AreaUtil;
import com.miqtech.master.utils.SqlJoiner;

@Component
public class IndexMidImgService {
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private IndexMidImgDao indexMidImgDao;
	@Autowired
	private IndexService indexService;
	@Autowired
	private ObjectRedisOperateService objectRedisOperateService;
	@Autowired
	private SystemAreaService systemAreaService;

	public List<Map<String, Object>> queryMid1(String areaCode) {
		if (StringUtils.isBlank(areaCode)) {
			areaCode = "000000";
		}
		String sql = SqlJoiner
				.join("SELECT a.id,b.title, 10 type, a.sort, b.icon img FROM ( index_mid_img a, activity_t_info b ) WHERE a.is_valid=1 and a.category = 1 AND a.type = 1 AND a.target_id = b.id AND a.area_code =",
						areaCode,
						" UNION SELECT a.id,b.title, 11 type, a.sort, c.icon img FROM ( index_mid_img a, amuse_t_activity b, amuse_r_activity_icon c ) WHERE a.is_valid=1 and a.category = 1 AND a.type = 2 AND a.target_id = b.id AND b.id = c.activity_id AND c.is_main = 1 AND c.is_valid = 1 AND a.area_code =",
						areaCode, " ORDER BY sort DESC");
		return queryDao.queryMap(sql);

	}

	public List<Map<String, Object>> queryMid2(String areaCode) {
		if (StringUtils.isBlank(areaCode)) {
			areaCode = "000000";
		}
		String sql = SqlJoiner
				.join("SELECT a.id, b.title, 10 type, a.sort, b.icon img, NULL url FROM ( index_mid_img a, activity_t_info b ) WHERE a.is_valid=1 and a.category = 2 AND a.type = 1 AND a.target_id = b.id AND a.area_code =",
						areaCode,
						" UNION SELECT a.id, b.title, 11 type, a.sort, c.icon img, NULL url FROM ( index_mid_img a, amuse_t_activity b, amuse_r_activity_icon c ) WHERE a.is_valid=1 and a.category = 2 AND a.type = 2 AND a.target_id = b.id AND b.id = c.activity_id AND c.is_main = 1 AND c.is_valid = 1 AND a.area_code =",
						areaCode,
						" UNION SELECT a.id, NULL title, case when a.type=4 then 5 when a.type=5 then 13 end type, a.sort, a.img, a.url FROM index_mid_img a WHERE a.is_valid=1 and a.category = 2 AND a.type NOT IN (1, 2) AND a.area_code =",
						areaCode, " ORDER BY sort DESC");
		return queryDao.queryMap(sql);

	}

	public void save(IndexMidImg indexMidImg) {
		indexMidImgDao.save(indexMidImg);
	}

	public IndexMidImg findById(Long id) {
		return indexMidImgDao.findOne(id);
	}

	public List<IndexMidImg> findByCategoryAndAreaCodeAndValid(Integer category, String areaCode, int valid) {
		return indexMidImgDao.findByCategoryAndAreaCodeAndValid(category, areaCode, valid);
	}

	public List<Map<String, Object>> queryByCategoryAndAreaCode(Integer category, String areaCode) {
		String sql = SqlJoiner.join("select * from index_mid_img where is_valid=1 and category=",
				String.valueOf(category), " and area_code like '", areaCode, "%'", " order by sort desc");
		return queryDao.queryMap(sql);
	}

	public void addToCache(String areaCode, Integer tab) {
		if (areaCode == null || tab == null) {
			return;
		}
		String code = null;
		areaCode = AreaUtil.getProvinceCode(areaCode);
		if (tab == 3) {
			Map<String, Object> map = indexService.queryMid1(areaCode);
			objectRedisOperateService.setData(CacheKeyConstant.API_CACHE_MID1 + areaCode, map);
			if (areaCode.equals("00")) {
				List<SystemArea> areaList = systemAreaService.queryValidRoot();
				for (SystemArea area : areaList) {
					code = AreaUtil.getProvinceCode(area.getAreaCode());
					map = indexService.queryMid1(code);
					objectRedisOperateService.setData(CacheKeyConstant.API_CACHE_MID1 + code, map);
				}
			}
		} else if (tab == 4) {
			Map<String, Object> map = indexService.queryMid2(areaCode);
			objectRedisOperateService.setData(CacheKeyConstant.API_CACHE_MID2 + areaCode, map);
			if (areaCode.equals("00")) {
				List<SystemArea> areaList = systemAreaService.queryValidRoot();
				for (SystemArea area : areaList) {
					code = AreaUtil.getProvinceCode(area.getAreaCode());
					map = indexService.queryMid2(code);
					objectRedisOperateService.setData(CacheKeyConstant.API_CACHE_MID2 + code, map);
				}
			}
		}
	}
}
