package com.miqtech.master.service.mall;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.mall.CommodityAreaDao;
import com.miqtech.master.entity.mall.CommodityArea;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

/**
 * 商品展示区操作service
 */
@Component
public class CommodityAreaService {
	@Autowired
	private CommodityAreaDao commodityAreaDao;
	@Autowired
	private QueryDao queryDao;

	public List<CommodityArea> findValidAll() {
		return commodityAreaDao.findByValid(CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * 根据id查商品区实体
	 */
	public CommodityArea getCommodityAreaById(long areaId) {
		return commodityAreaDao.findOne(areaId);
	}

	/**
	 * 保存商品展示区
	 */
	public void save(CommodityArea commodityArea) {
		commodityAreaDao.save(commodityArea);
	}

	/**
	 * ##后台管理##:查询商品区列表，分页
	 */
	public PageVO listPage(int page, int rows, Map<String, Object> params) {
		String sqlQuery = SqlJoiner.join("select id, area_name areaName from mall_t_commodity_area where is_valid=1");
		String sqlCount = SqlJoiner.join("select count(1) from mall_t_commodity_area", " where is_valid=1");
		if (null != params.get("name")) { //根据商品名查询
			sqlQuery = SqlJoiner.join(sqlQuery, " and area_name like concat('%', :name, '%')");
			sqlCount = SqlJoiner.join(sqlCount, " and area_name like '%" + params.get("name") + "%'");
		}
		params.put("page", (page - 1) * rows);
		params.put("rows", rows);
		sqlQuery = SqlJoiner.join(sqlQuery, " limit :page, :rows");

		PageVO pageVO = new PageVO();
		pageVO.setList(queryDao.queryMap(sqlQuery, params));

		Number total = queryDao.query(sqlCount);
		pageVO.setTotal(total.intValue());

		return pageVO;
	}

	/**
	 * 查询所有商品区
	 */
	public List<Map<String, Object>> getAreaList() {
		String sqlQuery = "select id, area_name areaName from mall_t_commodity_area where is_valid=1";
		List<Map<String, Object>> areaList = queryDao.queryMap(sqlQuery);

		return areaList;
	}

}
