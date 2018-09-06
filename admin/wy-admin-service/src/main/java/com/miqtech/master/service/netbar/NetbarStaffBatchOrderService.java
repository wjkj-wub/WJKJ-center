package com.miqtech.master.service.netbar;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.NetbarStaffBatchOrderDao;
import com.miqtech.master.entity.netbar.NetbarStaffBatchOrder;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class NetbarStaffBatchOrderService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private NetbarStaffBatchOrderDao netbarStaffBatchOrderDao;

	public void save(List<NetbarStaffBatchOrder> batchOrders) {
		netbarStaffBatchOrderDao.save(batchOrders);
	}

	/**
	 * 雇员交接班详情分页
	 */
	public PageVO page(Long batchId, int page, Map<String, Object> params) {
		String sqlCondition = " WHERE 1";
		Map<String, Object> sqlParams = Maps.newHashMap();
		String totalCondition = " WHERE 1";

		if (batchId != null) {
			sqlCondition = SqlJoiner.join(sqlCondition, " AND sbo.batch_id = ", batchId.toString());
			totalCondition = SqlJoiner.join(totalCondition, " AND sbo.batch_id = ", batchId.toString());
		}
		String telephone = MapUtils.getString(params, "telephone");
		if (StringUtils.isNotBlank(telephone)) {
			sqlCondition = SqlJoiner.join(sqlCondition, " AND u.username LIKE :telephone");
			String likeTelephoen = "%" + telephone + "%";
			sqlParams.put("telephone", likeTelephoen);
			totalCondition = SqlJoiner.join(totalCondition, " AND u.username LIKE '", likeTelephoen, "'");
		}
		String nickname = MapUtils.getString(params, "nickname");
		if (StringUtils.isNotBlank(nickname)) {
			sqlCondition = SqlJoiner.join(sqlCondition, " AND user_nickname LIKE :nickname");
			String likeNickname = "%" + nickname + "%";
			sqlParams.put("nickname", likeNickname);
			totalCondition = SqlJoiner.join(totalCondition, " AND user_nickname LIKE '", likeNickname, "'");
		}
		String beginDate = MapUtils.getString(params, "beginDate");
		if (StringUtils.isNotBlank(beginDate)) {
			sqlCondition = SqlJoiner.join(sqlCondition, " AND o.create_date >= :beginDate");
			sqlParams.put("beginDate", beginDate);
			totalCondition = SqlJoiner.join(totalCondition, " AND o.create_date >= '", beginDate, "'");
		}
		String endDate = MapUtils.getString(params, "endDate");
		if (StringUtils.isNotBlank(endDate)) {
			sqlCondition = SqlJoiner.join(sqlCondition, " AND o.create_date <=:endDate");
			sqlParams.put("endDate", endDate);
			totalCondition = SqlJoiner.join(totalCondition, " AND o.create_date <='", endDate, "'");
		}

		String limit = "";
		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page > 0) {
			Integer startRow = (page - 1) * pageSize;
			limit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());
		}
		String sql = SqlJoiner.join(
				"SELECT o.id, o.create_date createDate, o.update_date updateDate,o.user_use_status userUseStatus,",
				" u.username telephone, o.user_nickname userNickname, o.total_amount totalAmount,",
				" o.amount, o.rebate_amount rebateAmount, o.redbag_amount redbagAmount, o.score_amount scoreAmount, o.value_added_amount valueAddedAmount",
				" FROM netbar_r_staff_batch_order sbo LEFT JOIN netbar_r_order o ON sbo.order_id = o.id",
				" LEFT JOIN user_T_info u ON o.user_id = u.id", sqlCondition, " ORDER BY o.create_date ASC", limit);
		List<Map<String, Object>> list = queryDao.queryMap(sql, sqlParams);

		String totalSql = SqlJoiner.join("SELECT COUNT(1)",
				" FROM netbar_r_staff_batch_order sbo LEFT JOIN netbar_r_order o ON sbo.order_id = o.id",
				" LEFT JOIN user_T_info u ON o.user_id = u.id", totalCondition);
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

	public Map<String, Object> statisNetbarStaffBatch(Long batchId, Map<String, Object> params) {
		String sqlCondition = " WHERE 1";
		Map<String, Object> sqlParams = Maps.newHashMap();
		if (batchId != null) {
			sqlCondition = SqlJoiner.join(sqlCondition, " AND sbo.batch_id = ", batchId.toString());
		}
		String telephone = MapUtils.getString(params, "telephone");
		if (StringUtils.isNotBlank(telephone)) {
			sqlCondition = SqlJoiner.join(sqlCondition, " AND u.username LIKE :telephone");
			String likeTelephoen = "%" + telephone + "%";
			sqlParams.put("telephone", likeTelephoen);
		}
		String nickname = MapUtils.getString(params, "nickname");
		if (StringUtils.isNotBlank(nickname)) {
			sqlCondition = SqlJoiner.join(sqlCondition, " AND user_nickname LIKE :nickname");
			String likeNickname = "%" + nickname + "%";
			sqlParams.put("nickname", likeNickname);
		}
		String beginDate = MapUtils.getString(params, "beginDate");
		if (StringUtils.isNotBlank(beginDate)) {
			sqlCondition = SqlJoiner.join(sqlCondition, " AND o.create_date >= :beginDate");
			sqlParams.put("beginDate", beginDate);
		}
		String endDate = MapUtils.getString(params, "endDate");
		if (StringUtils.isNotBlank(endDate)) {
			sqlCondition = SqlJoiner.join(sqlCondition, " AND o.create_date < ADDDATE(:endDate, INTERVAL 1 DAY)");
			sqlParams.put("endDate", endDate);
		}

		String sql = SqlJoiner.join(
				"SELECT SUM(total_amount) totalAmount, SUM(amount) amount, SUM(redbag_amount) redbagAmount, SUM(rebate_amount) rebateAmount, SUM(score_amount) scoreAmount,SUM(value_added_amount) valueAddedAmount",
				" FROM netbar_r_staff_batch_order sbo LEFT JOIN netbar_r_order o ON sbo.order_id = o.id",
				" LEFT JOIN user_T_info u ON o.user_id = u.id", sqlCondition);
		return queryDao.querySingleMap(sql, params);
	}

	/**
	 * 获取交接班的详细订单，并按日期倒序排序
	 */
	public List<Map<String, Object>> getStaffBatchsOrders(List<Long> ids) {
		if (CollectionUtils.isNotEmpty(ids)) {
			StringBuffer sb = new StringBuffer();
			for (Long id : ids) {
				if (sb.length() > 0) {
					sb.append(",");
				}
				sb.append(id.toString());
			}
			return getStaffBatchsOrders(sb.toString());
		}
		return null;
	}

	/**
	 * 获取交接班的详细订单，并按日期倒序排序
	 */
	public List<Map<String, Object>> getStaffBatchsOrders(String ids) {
		String sql = SqlJoiner.join("select o.id, o.create_date createDate from netbar_r_order o",
				" left join netbar_r_staff_batch_order sbo on o.id = sbo.order_id", " where sbo.batch_id in (", ids,
				" ) order by o.create_date desc;");
		return queryDao.queryMap(sql);
	}
}
