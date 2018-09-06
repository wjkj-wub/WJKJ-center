package com.miqtech.master.service.netbar;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.NetbarOwnerBatchOrderDao;
import com.miqtech.master.entity.netbar.NetbarOwnerBatchOrder;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class NetbarOwnerBatchOrderService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private NetbarOwnerBatchOrderDao netbarOwnerBatchOrderDao;

	public void save(List<NetbarOwnerBatchOrder> obos) {
		netbarOwnerBatchOrderDao.save(obos);
	}

	/**
	 * 更新订单状态
	 */
	public void updateOwnerBatchOrder(String ownerBatchIds, Integer status) {
		if (StringUtils.isBlank(ownerBatchIds) || status == null) {
			return;
		}

		// 查询须更新的订单
		String orderIdsSql = SqlJoiner.join("SELECT order_id id FROM netbar_r_staff_batch_order sbo",
				" LEFT JOIN netbar_t_staff_batch sb ON sbo.batch_id = sb.id",
				" LEFT JOIN netbar_r_owner_batch_order obo ON sb.id = obo.staff_batch_id", " WHERE obo.batch_id IN (",
				ownerBatchIds, ")");
		List<Map<String, Object>> orderIds = queryDao.queryMap(orderIdsSql);

		// 组装id
		String idsStr = StringUtils.EMPTY;
		if (CollectionUtils.isNotEmpty(orderIds)) {
			for (Map<String, Object> orderIdMap : orderIds) {
				Long id = MapUtils.getLong(orderIdMap, "id");
				if (StringUtils.isNotBlank(idsStr)) {
					idsStr = SqlJoiner.join(idsStr, ",");
				}
				idsStr = SqlJoiner.join(idsStr, id.toString());
			}
		}

		// 更新订单
		String sql = SqlJoiner.join("UPDATE netbar_r_order o SET o.status = ", status.toString(), " WHERE o.id IN (",
				idsStr, ")");
		queryDao.update(sql);
	}

	/**
	 * 查看业主交接班详情订单
	 */
	public PageVO ownerBatchOrderPage(Long batchId, int page, Map<String, Object> params) {
		String sqlCondition = " WHERE ob.is_valid = 1";
		Map<String, Object> sqlParams = Maps.newHashMap();
		String totalCondition = " WHERE ob.is_valid = 1";

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
			sqlCondition = SqlJoiner.join(sqlCondition, " AND o.create_date < ADDDATE(:endDate, INTERVAL 1 DAY)");
			sqlParams.put("endDate", endDate);
			totalCondition = SqlJoiner.join(totalCondition, " AND o.create_date < ADDDATE('", endDate,
					"', INTERVAL 1 DAY)");
		}
		String areaCode = MapUtils.getString(params, "areaCode");
		if (StringUtils.isNotBlank(areaCode)) {
			String likeAreaCode = com.miqtech.master.utils.StringUtils.reduceAreaCode(areaCode) + "%";
			sqlCondition = SqlJoiner.join(sqlCondition, " AND n.area_code LIKE :areaCode");
			sqlParams.put("areaCode", likeAreaCode);
			totalCondition = SqlJoiner.join(totalCondition, " AND n.area_code LIKE '", likeAreaCode, "'");
		}

		sqlCondition = SqlJoiner.join(sqlCondition, " AND ob.id = ", batchId.toString());
		totalCondition = SqlJoiner.join(totalCondition, " AND ob.id = ", batchId.toString());

		String limit = "";
		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page > 0) {
			Integer startRow = (page - 1) * pageSize;
			limit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());
		}
		String sql = SqlJoiner.join(
				"SELECT o.id, o.create_date createDate, o.update_date updateDate, u.username telephone, o.user_nickname nickname, o.status,",
				" o.total_amount totalAmount, o.amount, o.rebate_amount rebateAmount, o.redbag_amount redbagAmount, o.value_added_amount valueAddedAmount, o.score_amount scoreAmount",
				" FROM netbar_r_order o LEFT JOIN netbar_r_staff_batch_order sbo ON o.id = sbo.order_id",
				" LEFT JOIN netbar_t_staff_batch sb ON sbo.batch_id = sb.id AND sb.is_valid = 1 LEFT JOIN netbar_r_owner_batch_order obo ON obo.staff_batch_id = sb.id",
				" LEFT JOIN netbar_t_owner_batch ob ON obo.batch_id = ob.id LEFT JOIN netbar_t_info n ON o.netbar_id = n.id",
				" LEFT JOIN user_t_info u ON o.user_id = u.id", sqlCondition, " ORDER BY o.id ASC", limit);
		List<Map<String, Object>> list = queryDao.queryMap(sql, sqlParams);

		String totalSql = SqlJoiner.join("SELECT COUNT(1)",
				" FROM netbar_r_order o LEFT JOIN netbar_r_staff_batch_order sbo ON o.id = sbo.order_id",
				" LEFT JOIN netbar_t_staff_batch sb ON sbo.batch_id = sb.id AND sb.is_valid = 1 LEFT JOIN netbar_r_owner_batch_order obo ON obo.staff_batch_id = sb.id",
				" LEFT JOIN netbar_t_owner_batch ob ON obo.batch_id = ob.id LEFT JOIN netbar_t_info n ON o.netbar_id = n.id",
				" LEFT JOIN user_t_info u ON o.user_id = u.id", totalCondition);
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
	 * 计算上一个周期的网吧配合基数(只计算商户的现金收入，排除红包)
	 */
	public double stasticNetbarQuota(Long netbarId, Date stasticDate) {
		if (stasticDate == null || netbarId == null) {
			return 0.00;
		}
		String sql = SqlJoiner.join("SELECT",
				" 	sum(amount) totalAmount                                                                          ",
				" FROM                                                                                   ",
				" 	(                                                                                    ",
				" 		SELECT                                                                           ",
				" 			*,                                                                           ",
				" 		IF (                                                                             ",
				" 			@user_id = user_id                                                           ",
				" 			AND @netbar_id = netbar_id                                                   ",
				" 			AND @date = date(create_date) ,@rank \\:=@rank + 1 ,@rank \\:= 1                 ",
				" 		) AS rank,                                                                       ",
				" 		@user_id \\:=user_id,                                                             ",
				" 		@netbar_id \\:=netbar_id,                                                         ",
				" 		@date \\:=date(create_date)                                                       ",
				" 	FROM                                                                                 ",
				" 		(                                                                                ",
				" 			SELECT                                                                       ",
				" 				o.*                                                                      ",
				" 			FROM                                                                         ",
				" 				netbar_t_owner_batch ob                                                  ",
				" 			JOIN netbar_r_owner_batch_order obo ON ob.id = obo.batch_id                  ",
				" 			JOIN netbar_r_staff_batch_order sbo ON obo.staff_batch_id = sbo.batch_id     ",
				" 			JOIN netbar_r_order o ON sbo.order_id = o.id                                 ",
				" 			join netbar_fund_detail nfd on ob.ser_numbers = nfd.ser_numbers and nfd.type = 4 and nfd.direction = 1",
				" 			WHERE                                                                        ",
				" 				ob.is_valid = 1                                                          ",
				" 			AND ob.netbar_id = ", netbarId.toString(),
				" 			AND nfd.create_date >= '2016-05-06 00:00:00' 			AND nfd.create_date < now() 			AND ob.status = 2                                                           ",
				" 			ORDER BY                                                                     ",
				" 				user_id,                                                                 ",
				" 				netbar_id,                                                               ",
				" 				date(o.create_date)                                                      ",
				" 		) base,                                                                          ",
				" 		(                                                                                ",
				" 			SELECT                                                                       ",
				" 				@rownum \\:=0,                                                            ",
				" 				@user_id \\:=NULL,                                                        ",
				" 				@netbar_id \\:=NULL ,@date \\:=NULL,                                       ",
				" 				@rank \\:=0                                                               ",
				" 		) ran                                                                            ",
				" 	ORDER BY                                                                             ",
				" 		user_id,                                                                         ",
				" 		netbar_id,                                                                       ",
				" 		create_date                                                                      ",
				" 	) result                                                                             ",
				" WHERE                                                                                  ",
				" 	rank <= 2");

		Map<String, Object> result = queryDao.querySingleMap(sql);
		if (null != result) {
			Number totalAmount = (Number) result.get("totalAmount");
			totalAmount = totalAmount == null ? 0.00 : totalAmount.doubleValue();
			return totalAmount.doubleValue();
		}
		return 0.00;

	}
}
