package com.miqtech.master.service.index;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.common.IndexAdvertiseAreaDao;
import com.miqtech.master.entity.common.IndexAdvertiseArea;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

/**
 * 首页广告的地区
 */
@Component
public class IndexAdvertiseAreaService {

	@Autowired
	private IndexAdvertiseAreaDao indexAdvertiseAreaDao;
	@Autowired
	private QueryDao queryDao;

	/**
	 * 根据ID查询
	 */
	public IndexAdvertiseArea findById(Long id) {
		return indexAdvertiseAreaDao.findOne(id);
	}

	/**
	 * 根据广告ID查出所有地区记录
	 */
	public PageVO getAreasByAdId(Long adId, int page, int valid) {
		if (valid != 0) {
			valid = 1;
		}
		String sqlAdArea = SqlJoiner
				.join("select a.id, a.advertise_id adId, a.area_code areaCode, a.is_valid valid, e.name from index_r_advertise_area a",
						" left join sys_t_area e on e.area_code=a.area_code",
						" where a.is_valid=:valid and a.advertise_id=:adId");
		String sqlTotal = "select count(1) from index_r_advertise_area where is_valid=1 and advertise_id=" + adId;
		Map<String, Object> params = Maps.newHashMap();
		params.put("valid", valid);
		params.put("adId", adId);
		params.put("start", (page - 1) * PageUtils.ADMIN_DEFAULT_PAGE_SIZE);
		params.put("rows", PageUtils.ADMIN_DEFAULT_PAGE_SIZE);
		sqlAdArea = SqlJoiner.join(sqlAdArea, "  limit :start, :rows");
		Number total = queryDao.query(sqlTotal);
		PageVO pageVO = new PageVO();
		pageVO.setTotal(total.intValue());
		pageVO.setList(queryDao.queryMap(sqlAdArea, params));
		return pageVO;
	}

	/**
	 * 查询广告-地区记录信息
	 */
	public Map<String, Object> queryRecord(long adId, String areaCode) {
		String sqlQuery = "select id, advertise_id advertiseId, is_valid valid from index_r_advertise_area where advertise_id="
				+ adId + " and area_code=" + areaCode + " limit 1";
		List<Map<String, Object>> list = queryDao.queryMap(sqlQuery);
		if (CollectionUtils.isNotEmpty(list)) {
			if (list.get(0) != null) {
				return list.get(0);
			}
		}
		return null;
	}

	/**
	 * 保存
	 */
	public IndexAdvertiseArea save(IndexAdvertiseArea indexAdvertiseArea) {
		return indexAdvertiseAreaDao.save(indexAdvertiseArea);
	}

	/**
	 * 根据ID删除(is_valid置为0)
	 */
	public void deleteById(Long id) {
		IndexAdvertiseArea indexAdvertise = indexAdvertiseAreaDao.findOne(id);
		if (indexAdvertise != null) {
			indexAdvertise.setValid(CommonConstant.INT_BOOLEAN_FALSE);
			save(indexAdvertise);
		}
	}

	public IndexAdvertiseArea findByAdvertiseId(Long id) {
		return indexAdvertiseAreaDao.findByAdvertiseId(id);
	}

}