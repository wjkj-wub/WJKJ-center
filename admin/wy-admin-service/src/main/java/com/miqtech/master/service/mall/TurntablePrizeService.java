package com.miqtech.master.service.mall;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.mall.TurntablePrizeDao;
import com.miqtech.master.entity.mall.TurntablePrize;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

/**
 * 商品类别操作Service
 */
@Component
public class TurntablePrizeService {
	@Autowired
	private TurntablePrizeDao turntablePrizeDao;
	@Autowired
	private QueryDao queryDao;

	public List<Map<String, Object>> queryAll() {
		String sql = "select * from mall_t_turntable_prize";
		return queryDao.queryMap(sql);
	}

	/**
	 * 根据id查商品区实体
	 */
	public TurntablePrize getTurntablePrizeById(long prizeId) {
		return turntablePrizeDao.findOne(prizeId);
	}

	/**
	 * 保存商品展示区
	 */
	public void save(TurntablePrize TurntablePrize) {
		turntablePrizeDao.save(TurntablePrize);
	}

	/**
	 * ##后台管理##:查询奖品类别列表，分页
	 */
	public PageVO listPage(int page, int rows, Map<String, Object> params) {
		String sqlQuery = SqlJoiner.join(
				"select a.id, a.prize_name prizeName,a.category_id categoryId,if(b.name is null ,a.category_id,b.name ) categoryName,",
				"prize_img prizeImg,a.prize_count prizeCount,",
				"enable_status enableStatus,module_id moduleId,(case when c.commodity_id is null then 0 else count(1) end) as personCount from mall_t_turntable_prize a ",
				" left join mall_t_commodity_category b on a.category_id=b.id  ",
				" LEFT JOIN mall_r_commodity_history c ON a.id=c.commodity_id and c.commodity_source=2",
				" where  a.is_valid=1 and module_id=:moduleId  ");
		String sqlCount = SqlJoiner.join(
				"select count(1) from mall_t_turntable_prize a  where a.is_valid=1 and module_id=",
				params.get("moduleId") + "");
		if (null != params.get("prizeName")) { //根据商品名查询
			sqlQuery = SqlJoiner.join(sqlQuery, "  and prize_name like concat('%', :prizeName, '%')");
			sqlCount = SqlJoiner.join(sqlCount, "  and prize_name like '%" + params.get("prizeName") + "%'");
		}
		if (null != params.get("categoryId")) { //根据商品名查询
			sqlQuery = SqlJoiner.join(sqlQuery, " and a.category_id=:categoryId ");
			sqlCount = SqlJoiner.join(sqlCount, " and a.category_id=" + params.get("categoryId"));
		}

		if (null != params.get("enableStatus")) { //根据商品名查询
			sqlQuery = SqlJoiner.join(sqlQuery, " and a.enable_status=:enableStatus ");
			sqlCount = SqlJoiner.join(sqlCount, " and a.enable_status=" + params.get("enableStatus"));
		}
		sqlQuery = SqlJoiner.join(sqlQuery, " group by a.id");
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
	 * 改变状态
	 */
	public void changeOpen(Integer id, Integer status) {
		String sql = SqlJoiner.join("UPDATE mall_t_turntable_prize SET enable_status=", status + " where id=" + id,
				" and is_valid=1 ");
		queryDao.update(sql);
	}

	/**
	 * 删除奖品
	 */
	public void deletePrize(long id) {
		String sql = SqlJoiner.join("UPDATE mall_t_turntable_prize SET is_valid=0 where id=" + id);
		queryDao.update(sql);
	}

	/**
	 * 根据类别查找奖品
	 */
	public List<Map<String, Object>> findValidByCategoryId(Integer id) {
		String sql = "select a.id,a.prize_name from mall_t_turntable_prize a left join mall_t_turntable b on a.module_id=b.id where a.is_valid=1 and a.enable_status=0 and a.category_id="
				+ id + " and a.is_valid=1 and a.module_id is not null and b.is_valid=1";
		List<Map<String, Object>> categoryList = queryDao.queryMap(sql);
		return categoryList;
	}

}
