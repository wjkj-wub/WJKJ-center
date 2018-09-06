package com.miqtech.master.service.award;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.award.AwardCommodityDao;
import com.miqtech.master.entity.award.AwardCommodity;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class AwardCommodityService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private AwardCommodityDao awardCommodityDao;

	public List<AwardCommodity> save(List<AwardCommodity> commodities) {
		return (List<AwardCommodity>) awardCommodityDao.save(commodities);
	}

	/**
	 * 分页
	 */
	public PageVO page(int page, Map<String, String> searchParams) {
		String condition = " WHERE c.is_valid = 1";
		String totalCondition = condition;

		String isUsed = MapUtils.getString(searchParams, "isUsed");
		if (NumberUtils.isNumber(isUsed)) {
			condition = SqlJoiner.join(condition, " AND c.is_used = ", isUsed);
			totalCondition = SqlJoiner.join(totalCondition, " AND c.is_used = ", isUsed);
		}
		String inventoryId = MapUtils.getString(searchParams, "inventoryId");
		if (NumberUtils.isNumber(inventoryId)) {
			condition = SqlJoiner.join(condition, " AND c.inventory_id = ", inventoryId);
			totalCondition = SqlJoiner.join(totalCondition, " AND c.inventory_id = ", inventoryId);
		}

		// 排序
		String order = " ORDER BY";
		if (!CommonConstant.INT_BOOLEAN_FALSE.toString().equals(isUsed)) {// 查询已使用列表
			order = SqlJoiner.join(order, " c.used_time DESC,");
		}
		order = SqlJoiner.join(order, " c.create_date DESC");

		// 分页
		String limit = "";
		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page > 0) {
			Integer startRow = (page - 1) * pageSize;
			limit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());
		}

		String sql = SqlJoiner.join(
				"SELECT c.id, c.cdkey, c.start_time startTime, c.end_time endTime, c.used_time usedTime, c.is_valid valid, c.create_date createDate",
				" FROM award_t_commodity c", condition, order, limit);
		List<Map<String, Object>> list = queryDao.queryMap(sql);

		String totalSql = SqlJoiner.join("SELECT COUNT(1) FROM award_t_commodity c", totalCondition);
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

	/**
	 * 批量删除
	 */
	public void delete(List<Long> ids) {
		if (CollectionUtils.isNotEmpty(ids)) {
			String idsStr = "";
			for (Long id : ids) {
				if (idsStr.length() > 0) {
					idsStr = SqlJoiner.join(idsStr, ",");
				}
				idsStr = SqlJoiner.join(idsStr, id.toString());
			}
			String updateSql = SqlJoiner.join("UPDATE award_t_commodity SET is_valid = 0 WHERE id IN (", idsStr, ")");
			queryDao.update(updateSql);
		}
	}

	/**
	 * 查询可用的cdkey
	 */
	public List<Map<String, Object>> queryUsefullyCdkeysByInventoryId(Long inventoryId) {
		if (inventoryId != null) {
			String sql = SqlJoiner.join("SELECT cdkey FROM award_t_commodity c",
					" WHERE now() >= c.start_time AND NOW() < c.end_time AND is_used != 1 AND is_valid = 1 AND c.inventory_id = ",
					inventoryId.toString());
			return queryDao.queryMap(sql);
		}
		return null;
	}

	/**
	 * 查询可用的商品(awardCommodity对象)
	 */
	public List<AwardCommodity> findUsefullyByInventoryId(Long inventoryId) {
		return awardCommodityDao.findUsefullyByInventoryId(inventoryId);
	}

}
