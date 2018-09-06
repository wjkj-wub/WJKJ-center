package com.miqtech.master.service.mall;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.mall.CommodityCategoryDao;
import com.miqtech.master.entity.mall.CommodityCategory;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

/**
 * 商品类别操作Service
 */
@Component
public class CommodityCategoryService {
	@Autowired
	private CommodityCategoryDao commodityCategoryDao;
	@Autowired
	private QueryDao queryDao;

	public List<Map<String, Object>> queryAll() {
		String sql = "select * from mall_t_commodity_category";
		return queryDao.queryMap(sql);
	}

	/**
	 * 根据id查商品区实体
	 */
	public CommodityCategory getCommodityCategoryById(long categoryId) {
		return commodityCategoryDao.findOne(categoryId);
	}

	/**
	 * 通过大类别查询
	 */
	public List<CommodityCategory> findValidBySuperType(Integer superType) {
		return commodityCategoryDao.findBySuperTypeAndValid(superType, CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * 保存商品展示区
	 */
	public void save(CommodityCategory commodityCategory) {
		commodityCategoryDao.save(commodityCategory);
	}

	/**
	 * 查询所有商品类别
	 */
	public List<Map<String, Object>> getCategoryList() {
		String sqlQuery = "select id, name from mall_t_commodity_category where is_valid=1";
		List<Map<String, Object>> categoryList = queryDao.queryMap(sqlQuery);

		return categoryList;
	}

	/**
	 * ##后台管理##:查询商品类别列表，分页
	 */
	public PageVO listPage(int page, int rows, Map<String, Object> params) {
		String sqlQuery = SqlJoiner.join("select id, name from mall_t_commodity_category where is_valid=1");
		String sqlCount = SqlJoiner.join("select count(1) from mall_t_commodity_category where is_valid=1");
		if (null != params.get("name")) { //根据商品名查询
			sqlQuery = SqlJoiner.join(sqlQuery, " and name like concat('%', :name, '%')");
			sqlCount = SqlJoiner.join(sqlCount, " and name like '%" + params.get("name") + "%'");
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

}
