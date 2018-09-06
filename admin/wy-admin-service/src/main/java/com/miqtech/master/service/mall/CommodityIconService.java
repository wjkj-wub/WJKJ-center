package com.miqtech.master.service.mall;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.mall.CommodityIconDao;
import com.miqtech.master.entity.mall.CommodityIcon;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

/**
 * 商品Icon操作service
 */
@Component
public class CommodityIconService {
	@Autowired
	private CommodityIconDao commodityIconDao;
	@Autowired
	private QueryDao queryDao;

	/**
	 * 保存商品Icon
	 */
	public CommodityIcon save(CommodityIcon commodityIcon) {
		if (commodityIcon != null) {
			Date now = new Date();
			commodityIcon.setUpdateDate(now);
			if (commodityIcon.getId() != null) {
				CommodityIcon oldIcon = findById(commodityIcon.getId());
				commodityIcon = BeanUtils.updateBean(oldIcon, commodityIcon);
			} else {
				commodityIcon.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				commodityIcon.setCreateDate(now);
			}
			return commodityIconDao.save(commodityIcon);
		} else {
			return null;
		}
	}

	public CommodityIcon findById(Long id) {
		return commodityIconDao.findOne(id);
	}

	/**
	 * 根据商品ID查主图路径
	 */
	public long getMainIconByCommodityId(long commodityId) {
		String sqlQuery = "select icon from mall_r_commodity_icon where is_main=1 and commodity_id=" + commodityId;
		return Long.parseLong(queryDao.querySingleMap(sqlQuery).get("icon").toString());
	}

	/**
	 * 商品图片分页
	 */
	public PageVO page(Long commodityId, Integer page) {
		if (page == null) {
			page = 1;
		}

		// 查询主体
		String commodityCondition = commodityId != null ? "commodity_id = " + commodityId + " AND" : "";
		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		Integer startRow = (page - 1) * pageSize;
		String sql = SqlJoiner.join("SELECT id, commodity_id commodityId, icon, is_main isMain",
				" FROM mall_r_commodity_icon ci", " WHERE ", commodityCondition,
				" is_valid = 1 ORDER BY is_main DESC, id ASC", " LIMIT ", startRow.toString(), ", ",
				pageSize.toString());
		List<Map<String, Object>> content = queryDao.queryMap(sql);

		// 查询总数
		String countSql = SqlJoiner.join("SELECT COUNT(1) FROM mall_r_commodity_icon ci", " WHERE ",
				commodityCondition, " is_valid = 1");
		Number total = queryDao.query(countSql);

		PageVO vo = new PageVO();
		vo.setList(content);
		vo.setTotal(total.intValue());
		int isLast = total.intValue() > page * pageSize ? 1 : 0;
		vo.setIsLast(isLast);

		return vo;
	}

	/**
	 * 根据商品ID查询图片列表
	 */
	public List<CommodityIcon> findByCommodityId(Long commodityId) {
		return commodityIconDao.findByCommodityIdAndValid(commodityId, CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * 删除
	 * @return 0 成功,-1 主图不能少于一张,-2 副图不能少于一张
	 */
	public int delete(Long id) {
		// 统计是否可以删除
		String sql = SqlJoiner
				.join("SELECT is_main isMain, (SELECT count(1) FROM mall_r_commodity_icon tmp WHERE tmp.is_main = i.is_main AND tmp.commodity_id = i.commodity_id ) count",
						" FROM mall_r_commodity_icon i WHERE i.id = ", id.toString());
		Map<String, Object> statis = queryDao.querySingleMap(sql);
		if (statis != null) {
			Number isMain = (Number) statis.get("isMain");
			Number count = (Number) statis.get("count");

			if (count.intValue() <= 1) {
				return isMain.intValue() == 1 ? -1 : -2;
			}

			commodityIconDao.delete(id);
		}
		return 0;
	}
}
