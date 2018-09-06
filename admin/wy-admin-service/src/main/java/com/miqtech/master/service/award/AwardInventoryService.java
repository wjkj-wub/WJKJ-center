package com.miqtech.master.service.award;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.award.AwardInventoryDao;
import com.miqtech.master.entity.award.AwardInventory;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class AwardInventoryService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private AwardInventoryDao awardInventoryDao;

	public AwardInventory findById(Long id) {
		return awardInventoryDao.findOne(id);
	}

	public AwardInventory findValidById(Long id) {
		return awardInventoryDao.findByIdAndValid(id, CommonConstant.INT_BOOLEAN_TRUE);
	}

	public AwardInventory save(AwardInventory inventory) {
		return awardInventoryDao.save(inventory);
	}

	public AwardInventory insertOrUpdate(AwardInventory inventory) {
		if (inventory != null) {
			Date now = new Date();
			inventory.setUpdateDate(now);
			if (inventory.getId() != null) {
				AwardInventory old = findById(inventory.getId());
				inventory = BeanUtils.updateBean(old, inventory);
			} else {
				inventory.setCreateDate(now);
				inventory.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			}
			return awardInventoryDao.save(inventory);
		}
		return null;
	}

	/**
	 * 启用
	 */
	public void abled(Long id) {
		if (id != null) {
			AwardInventory i = new AwardInventory();
			i.setId(id);
			i.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			insertOrUpdate(i);
		}
	}

	/**
	 * 禁用
	 */
	public void disabled(Long id) {
		if (id != null) {
			AwardInventory i = new AwardInventory();
			i.setId(id);
			i.setValid(CommonConstant.INT_BOOLEAN_FALSE);
			insertOrUpdate(i);
		}
	}

	/**
	 * 分页查询
	 */
	public PageVO page(int page, Map<String, String> searchParams, String orderCol, String orderType) {
		Map<String, Object> params = Maps.newHashMap();
		String condition = " WHERE 1=1";
		String totalCondition = condition;
		String order = " ORDER BY";

		// 查询条件
		String name = MapUtils.getString(searchParams, "name");
		if (StringUtils.isNotBlank(name)) {
			String likeName = "%" + name + "%";
			params.put("name", likeName);
			condition = SqlJoiner.join(condition, " AND name LIKE :name");
			totalCondition = SqlJoiner.join(totalCondition, " AND name LIKE '", likeName, "'");
		}
		String valid = MapUtils.getString(searchParams, "valid");
		if (NumberUtils.isNumber(valid)) {
			if ("1".equals(valid)) {
				condition = SqlJoiner.join(condition, " AND NOW() >= start_time AND NOW() < end_time AND is_valid = 1");
				totalCondition = SqlJoiner.join(totalCondition,
						" AND NOW() >= start_time AND NOW() < end_time AND is_valid = 1");
			} else {
				condition = SqlJoiner.join(condition, " AND (NOW() < start_time OR NOW() > end_time OR is_valid != 1)");
				totalCondition = SqlJoiner.join(totalCondition,
						" AND (NOW() < start_time OR NOW() > end_time OR is_valid != 1)");
			}
		}

		// 排序
		if (StringUtils.isNotBlank(orderType)) {
			if (StringUtils.isBlank(orderType)) {
				orderType = "DESC";
			}
			order = SqlJoiner.join(order, " ", orderCol, " ", orderType, ", ai.create_date DESC");
		} else {
			order = SqlJoiner.join(order, " ai.import_time DESC, ai.create_date DESC");
		}

		// 分页
		String limit = "";
		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page > 0) {
			Integer startRow = (page - 1) * pageSize;
			limit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());
		}

		String sql = SqlJoiner
				.join("SELECT ai.id, ai.name, sum(if(ISNULL(ac.id), 0, 1)) totalCount, sum(IF(ac.is_used = 0, 1, 0)) unusedCount,",
						" ai.start_time startTime, ai.end_time endTime, ai.is_valid valid, ai.import_time importTime, ai.create_date createDate",
						" FROM ( SELECT * FROM award_t_inventory ai", condition,
						") ai LEFT JOIN award_t_commodity ac ON ac.inventory_id = ai.id and ac.is_valid = 1",
						" GROUP BY ai.id", order, limit);
		List<Map<String, Object>> list = queryDao.queryMap(sql, params);

		String totalSql = SqlJoiner.join("SELECT COUNT(1) FROM award_t_inventory ai", totalCondition);
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
	 * 查询详情
	 */
	public Map<String, Object> queryByIdWithTotalCountUnusedCount(Long id) {
		if (id != null) {
			String sql = SqlJoiner
					.join("SELECT ai.id, ai.name, sum(if(ISNULL(ac.id), 0, 1)) totalCount, sum(IF(ac.is_used = 0, 1, 0)) unusedCount,",
							" ai.start_time startTime, ai.end_time endTime, ai.is_valid valid, ai.import_time importTime, ai.create_date createDate",
							" FROM ( SELECT * FROM award_t_inventory ai WHERE id = ", id.toString(), " ) ai",
							" LEFT JOIN award_t_commodity ac ON ac.inventory_id = ai.id AND ac.is_valid = 1");
			return queryDao.querySingleMap(sql);
		}
		return null;
	}

	/**
	 * 获取库存足够的物品
	 */
	public List<Map<String, Object>> queryUsefullyInventories() {
		String sql = SqlJoiner
				.join("SELECT i.id, i.name, count(c.id) count FROM award_t_inventory i",
						" JOIN award_t_commodity c ON i.id = c.inventory_id AND c.is_valid = 1 AND c.is_used != 1 AND now() >= c.start_time AND NOW() < c.end_time",
						" WHERE i.is_valid = 1 AND now() >= i.start_time AND NOW() < i.end_time GROUP BY i.id HAVING count > 0");
		return queryDao.queryMap(sql);
	}
}
