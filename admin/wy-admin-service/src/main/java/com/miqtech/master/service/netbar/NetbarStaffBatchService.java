package com.miqtech.master.service.netbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.OrderConstants;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.NetbarStaffBatchDao;
import com.miqtech.master.entity.netbar.NetbarStaffBatch;
import com.miqtech.master.entity.netbar.NetbarStaffBatchOrder;
import com.miqtech.master.utils.ArithUtil;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class NetbarStaffBatchService {

	private static final Logger LOGGER = LoggerFactory.getLogger(NetbarStaffBatchOrder.class);

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private NetbarStaffBatchDao netbarStaffBatchDao;
	@Autowired
	private NetbarStaffBatchOrderService netbarStaffBatchOrderService;

	public NetbarStaffBatch save(NetbarStaffBatch batch) {
		return netbarStaffBatchDao.save(batch);
	}

	public List<NetbarStaffBatch> save(List<NetbarStaffBatch> sbs) {
		if (CollectionUtils.isNotEmpty(sbs)) {
			return (List<NetbarStaffBatch>) netbarStaffBatchDao.save(sbs);
		}
		return null;
	}

	public NetbarStaffBatch findById(Long id) {
		return netbarStaffBatchDao.findOne(id);
	}

	public List<NetbarStaffBatch> findValidByIdIn(List<Long> ids) {
		if (CollectionUtils.isNotEmpty(ids)) {
			Long[] idsArray = ids.toArray(new Long[ids.size()]);
			return netbarStaffBatchDao.findByIdInAndValid(idsArray, CommonConstant.INT_BOOLEAN_TRUE);
		}
		return null;
	}

	public List<NetbarStaffBatch> findByIdIn(List<Long> ids) {
		if (CollectionUtils.isNotEmpty(ids)) {
			return netbarStaffBatchDao.findByIdIn(ids);
		}
		return null;
	}

	/**
	 * 通过网吧id和status查询雇员交接班
	 */
	public List<NetbarStaffBatch> findByNetbarIdAndStatus(Long netbarId, Integer status) {
		return netbarStaffBatchDao.findByNetbarIdAndStatus(netbarId, status);
	}

	/**
	 * 分页
	 */
	public PageVO page(int page, Long netbarId, Long staffId, String staffName, String beginTime, String endTime) {
		Map<String, Object> params = Maps.newHashMap();
		String condition = " where  sb.is_valid = 1";
		if (staffId != null) {
			condition = SqlJoiner.join(condition, " AND ns.id = ", staffId.toString());

		}

		if (netbarId != null) {
			condition = SqlJoiner.join(condition, " AND sb.netbar_id = " + netbarId.toString());
		}
		if (StringUtils.isNotBlank(beginTime)) {
			condition = SqlJoiner.join(condition, " AND sb.create_date >= '", beginTime, "'");
		}
		if (StringUtils.isNotBlank(endTime)) {
			condition = SqlJoiner.join(condition, " AND sb.create_date < '", endTime, "'");
		}
		if (StringUtils.isNotBlank(staffName)) {
			condition = SqlJoiner.join(condition, " AND ns.name='", staffName, "'");
		}

		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		Integer startRow = (page - 1) * pageSize;
		String sql = SqlJoiner.join("select a.id,a.total_amount,a.order_num,a.create_date,",
				" sum(if(nro.order_type = 2,nro.total_amount, 0)) pcamount,",
				" sum(	if(	  nro.order_type is null  or nro.order_type<>2,  nro.total_amount,0) ) appamount ",
				" ,if( a.name is null,\"网吧业主\", a.name) staffname",
				"	from (select sb.*,ns.name from	netbar_t_staff_batch sb  left join netbar_t_staff ns on ns.id = sb.create_user_id ",
				condition, " order by create_date desc limit ", startRow.toString(), ", ", pageSize.toString(), ") a ",
				"  left join netbar_r_staff_batch_order sbo 	on a.id = sbo.batch_id ",
				" left join netbar_r_order nro 	on nro.id = sbo.order_id ", "group by a.id     order by a.id desc  ");

		List<Map<String, Object>> list = queryDao.queryMap(sql, params);

		String tCondition = " WHERE sb.is_valid = 1";
		if (StringUtils.isNotBlank(staffName)) {
			if ("网吧业主".equals(staffName)) {
				condition = SqlJoiner.join(condition, " AND sb.create_user_id = 0");
			} else {
				tCondition = SqlJoiner.join(tCondition, " AND s.name = '" + staffName + "'");
			}
		}

		if (staffId != null) {
			tCondition = SqlJoiner.join(tCondition, " AND ns.id = ", staffId.toString());
		}
		if (netbarId != null) {
			tCondition = SqlJoiner.join(tCondition, " AND sb.netbar_id = " + netbarId.toString());
		}
		if (StringUtils.isNotBlank(beginTime)) {
			tCondition = SqlJoiner.join(tCondition, " AND sb.create_date >= '", beginTime, "'");
		}
		if (StringUtils.isNotBlank(endTime)) {
			tCondition = SqlJoiner.join(tCondition, " AND sb.create_date < '", endTime, "'");
		}
		String totalSql = SqlJoiner.join("SELECT COUNT(1)",
				" FROM netbar_t_staff_batch sb LEFT JOIN netbar_t_staff ns ON sb.create_user_id = ns.id", tCondition);
		Number total = (Number) queryDao.query(totalSql);
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
	 * 收入管理分页
	 */
	public PageVO incomeOrderPage(int page, Map<String, Object> params) {
		Map<String, Object> sqlParams = Maps.newHashMap();
		String sqlCondition = " WHERE 1 = 1";
		String totalCondition = " WHERE 1 = 1";
		String nameOrTelephone = MapUtils.getString(params, "nameOrTelephone");
		if (StringUtils.isNotBlank(nameOrTelephone)) {
			sqlCondition = SqlJoiner.join(sqlCondition,
					" AND (u.nickname LIKE :nameOrTelephone OR u.username LIKE :nameOrTelephone)");
			sqlParams.put("nameOrTelephone", "%" + nameOrTelephone + "%");
			totalCondition = SqlJoiner.join(totalCondition, " AND (u.nickname LIKE '%", nameOrTelephone,
					"%' OR u.username LIKE '%", nameOrTelephone, "%')");
		}
		String netbarName = MapUtils.getString(params, "netbarName");
		if (StringUtils.isNotBlank(netbarName)) {
			String likeNetbarName = "%" + netbarName + "%";
			sqlCondition = SqlJoiner.join(sqlCondition, " AND n.name LIKE :netbarName");
			sqlParams.put("netbarName", likeNetbarName);
			totalCondition = SqlJoiner.join(totalCondition, " AND n.name LIKE '", likeNetbarName, "'");
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
		String netbarId = MapUtils.getString(params, "netbarId");
		if (NumberUtils.isNumber(netbarId)) {
			sqlCondition = SqlJoiner.join(sqlCondition, " AND n.id = :netbarId");
			sqlParams.put("netbarId", NumberUtils.toLong(netbarId));
			totalCondition = SqlJoiner.join(totalCondition, " AND n.id = ", netbarId);
		}
		String tradeNo = MapUtils.getString(params, "tradeNo");
		if (StringUtils.isNotBlank(tradeNo)) {
			sqlCondition = SqlJoiner.join(sqlCondition, " AND o.trade_no = :tradeNo");
			sqlParams.put("tradeNo", tradeNo);
			totalCondition = SqlJoiner.join(totalCondition, " AND o.trade_no = '", tradeNo, "'");
		}
		String areaCode = MapUtils.getString(params, "areaCode");
		if (StringUtils.isNotBlank(areaCode)) {
			areaCode = com.miqtech.master.utils.StringUtils.reduceAreaCode(areaCode);
			sqlCondition = SqlJoiner.join(sqlCondition, " AND n.area_code LIKE :areaCode");
			sqlParams.put("areaCode", areaCode + "%");
			totalCondition = SqlJoiner.join(totalCondition, " AND n.area_code LIKE '", areaCode, "%'");
		}
		String payType = MapUtils.getString(params, "payType");// 支付方式
		if (NumberUtils.isNumber(payType)) {
			sqlCondition = SqlJoiner.join(sqlCondition, " AND o.type = :payType");
			sqlParams.put("payType", payType);
			totalCondition = SqlJoiner.join(totalCondition, " AND o.type = ", payType);
		}
		String state = MapUtils.getString(params, "state");// 是否结款
		if (NumberUtils.isNumber(state)) {
			if ("1".equals(state)) {
				sqlCondition = SqlJoiner.join(sqlCondition, " AND o.status >= 1 AND o.status < 2");
				totalCondition = SqlJoiner.join(totalCondition, " AND o.status >= 1 AND o.status < 2");
			} else if ("2".equals(state)) {
				sqlCondition = SqlJoiner.join(sqlCondition, " AND o.status = 2");
				totalCondition = SqlJoiner.join(totalCondition, " AND o.status = 2");
			} else if ("3".equals(state)) {
				sqlCondition = SqlJoiner.join(sqlCondition, " AND o.status = 4");
				totalCondition = SqlJoiner.join(totalCondition, " AND o.status = 4");
			}
		}
		sqlCondition = SqlJoiner.join(sqlCondition, " AND o.status > 0");
		totalCondition = SqlJoiner.join(totalCondition, " AND o.status > 0");

		String limit = "";
		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page > 0) {
			Integer startRow = (page - 1) * pageSize;
			limit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());
		}
		String primaryTable = null;
		if (StringUtils.equals("2", state)) {
			primaryTable = "netbar_r_staff_batch_order sbo LEFT JOIN netbar_r_order o ON sbo.order_id = o.id";
		} else {
			primaryTable = "netbar_r_order o";
		}
		String sql = SqlJoiner.join(
				"SELECT o.id, u.nickname, u.username, o.create_date createDate, o.trade_no tradeNo, o.order_type orderType,",
				" o.out_trade_no outTradeNo, o.total_amount totalAmount, o.amount, o.rebate_amount rebateAmount,",
				" o.redbag_amount redbagAmount, o.score_amount scoreAmount,o.value_added_amount valueAddedAmount, o.type, n.id netbarId, n.`name` netbarName, o.`status`, u.username telephone",
				" FROM ", primaryTable,
				" LEFT JOIN netbar_t_info n ON o.netbar_id = n.id LEFT JOIN user_t_info u ON u.id = o.user_id",
				sqlCondition, " ORDER BY o.create_date DESC", limit);
		List<Map<String, Object>> list = queryDao.queryMap(sql, sqlParams);

		String totalSql = SqlJoiner.join("SELECT COUNT(1) FROM ", primaryTable,
				" LEFT JOIN netbar_t_info n ON o.netbar_id = n.id LEFT JOIN user_t_info u ON u.id = o.user_id",
				totalCondition);
		Number total = (Number) queryDao.query(totalSql);

		PageVO vo = new PageVO();
		vo.setList(list);
		vo.setTotal(total.intValue());
		int isLast = total.intValue() > page * pageSize ? 1 : 0;
		vo.setIsLast(isLast);
		return vo;
	}

	/**
	 * 统计收入管理
	 */
	public Map<String, Object> statisIncomeOrder(Map<String, Object> params) {
		Map<String, Object> sqlParams = Maps.newHashMap();
		String sqlCondition = " WHERE 1 = 1";
		String totalCondition = " WHERE 1 = 1";
		String nameOrTelephone = MapUtils.getString(params, "nameOrTelephone");
		if (StringUtils.isNotBlank(nameOrTelephone)) {
			sqlCondition = SqlJoiner.join(sqlCondition,
					" AND (u.nickname LIKE :nameOrTelephone OR u.username LIKE :nameOrTelephone)");
			sqlParams.put("nameOrTelephone", "%" + nameOrTelephone + "%");
			totalCondition = SqlJoiner.join(totalCondition, " AND (u.nickname LIKE '%", nameOrTelephone,
					"%' OR u.username LIKE '%", nameOrTelephone, "%')");
		}
		String netbarName = MapUtils.getString(params, "netbarName");
		if (StringUtils.isNotBlank(netbarName)) {
			String likeNetbarName = "%" + netbarName + "%";
			sqlCondition = SqlJoiner.join(sqlCondition, " AND n.name LIKE :netbarName");
			sqlParams.put("netbarName", likeNetbarName);
			totalCondition = SqlJoiner.join(totalCondition, " AND n.name LIKE '", likeNetbarName, "'");
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
		String netbarId = MapUtils.getString(params, "netbarId");
		if (NumberUtils.isNumber(netbarId)) {
			sqlCondition = SqlJoiner.join(sqlCondition, " AND n.id = :netbarId");
			sqlParams.put("netbarId", NumberUtils.toLong(netbarId));
			totalCondition = SqlJoiner.join(totalCondition, " AND n.id = ", netbarId);
		}
		String tradeNo = MapUtils.getString(params, "tradeNo");
		if (StringUtils.isNotBlank(tradeNo)) {
			sqlCondition = SqlJoiner.join(sqlCondition, " AND o.trade_no = :tradeNo");
			sqlParams.put("tradeNo", tradeNo);
			totalCondition = SqlJoiner.join(totalCondition, " AND o.trade_no = '", tradeNo, "'");
		}
		String areaCode = MapUtils.getString(params, "areaCode");
		if (StringUtils.isNotBlank(areaCode)) {
			areaCode = com.miqtech.master.utils.StringUtils.reduceAreaCode(areaCode);
			sqlCondition = SqlJoiner.join(sqlCondition, " AND n.area_code LIKE :areaCode");
			sqlParams.put("areaCode", areaCode + "%");
			totalCondition = SqlJoiner.join(totalCondition, " AND n.area_code LIKE '%", areaCode, "'");
		}
		String payType = MapUtils.getString(params, "payType");// 支付方式
		if (NumberUtils.isNumber(payType)) {
			sqlCondition = SqlJoiner.join(sqlCondition, " AND o.type = :payType");
			sqlParams.put("payType", payType);
			totalCondition = SqlJoiner.join(totalCondition, " AND o.type = ", payType);
		}
		Integer payed = MapUtils.getInteger(params, "payed");// 是否结款
		if (payed != null) {
			if (CommonConstant.INT_BOOLEAN_TRUE.equals(payed)) {
				sqlCondition = SqlJoiner.join(sqlCondition, " AND o.status >= 4");
				totalCondition = SqlJoiner.join(totalCondition, " AND o.status >= 4");
			} else {
				sqlCondition = SqlJoiner.join(sqlCondition, " AND o.status < 4");
				totalCondition = SqlJoiner.join(totalCondition, " AND o.status < 4");
			}
		}
		String state = MapUtils.getString(params, "state");// 是否结款
		if (NumberUtils.isNumber(state)) {
			if ("1".equals(state)) {
				sqlCondition = SqlJoiner.join(sqlCondition, " AND o.status >= 1 AND o.status < 2");
				totalCondition = SqlJoiner.join(totalCondition, " AND o.status >= 1 AND o.status < 2");
			} else if ("2".equals(state)) {
				sqlCondition = SqlJoiner.join(sqlCondition, " AND o.status = 2");
				totalCondition = SqlJoiner.join(totalCondition, " AND o.status = 2");
			} else if ("3".equals(state)) {
				sqlCondition = SqlJoiner.join(sqlCondition, " AND o.status = 4");
				totalCondition = SqlJoiner.join(totalCondition, " AND o.status = 4");
			}
		}
		sqlCondition = SqlJoiner.join(sqlCondition, " AND o.status > 0");
		totalCondition = SqlJoiner.join(totalCondition, " AND o.status > 0");
		sqlCondition = SqlJoiner.join(sqlCondition, " AND o.is_valid >= 0");
		totalCondition = SqlJoiner.join(totalCondition, " AND o.is_valid >= 0");

		String sql = SqlJoiner.join(
				"SELECT count(1) count, sum(o.total_amount) sumTotalAmount, sum(o.value_added_amount) sumValueAddedAmount, sum(o.amount) sumAmount, sum(o.rebate_amount) sumRebateAmount, sum(o.redbag_amount) sumRedbagAmount",
				" FROM netbar_r_order o LEFT JOIN netbar_t_info n ON o.netbar_id = n.id LEFT JOIN user_t_info u ON u.id = o.user_id",
				sqlCondition);
		return queryDao.querySingleMap(sql, sqlParams);
	}

	/**
	 * 更新员工交接班状态
	 */
	public void updateStaffBatchByOwnerBatchIds(String ids, Integer status) {
		if (StringUtils.isBlank(ids) || status == null) {
			return;
		}

		String sql = SqlJoiner.join(
				"UPDATE netbar_t_staff_batch sb LEFT JOIN netbar_r_owner_batch_order obo ON sb.id = obo.staff_batch_id",
				" LEFT JOIN netbar_t_owner_batch ob ON obo.batch_id = ob.id SET sb.status = ", status.toString(),
				" WHERE ob.id IN (", ids, ")");
		queryDao.update(sql);
	}

	/**
	 * 初始化无雇员交接班信息的订单，自动产生雇员交接班信息及明细
	 */
	public void initNoStaffBatchOrders(Long netbarIdParam) {
		String condition = "";
		if (netbarIdParam != null) {
			condition = " AND o.netbar_id = " + netbarIdParam;
		}
		// 查询无雇员交接班的，已申请结款状态的所有订单信息，并根据网吧顺序排列
		String noStaffBatchOrdersSql = SqlJoiner.join(
				"SELECT o.id, o.total_amount, o.amount, o.rebate_amount, o.redbag_amount, o.score_amount, o.status, o.is_valid, o.netbar_id, sbo.batch_id, sbo.order_id",
				" FROM netbar_r_order o LEFT JOIN netbar_r_staff_batch_order sbo ON o.id = sbo.order_id",
				" WHERE sbo.id IS NULL AND o.status = 2 and o.total_amount > 0 ", condition,
				" order by o.netbar_id asc");
		List<Map<String, Object>> noStaffBatchOrders = queryDao.queryMap(noStaffBatchOrdersSql);
		if (CollectionUtils.isNotEmpty(noStaffBatchOrders)) {
			Long netbarId = null;
			Double totalAmount = 0.0;
			Double amount = 0.0;
			Double rebateAmount = 0.0;
			Double redbagAmount = 0.0;
			Double scoreAmount = 0.0;
			Integer orderNum = 0;
			List<Long> orderIds = new ArrayList<>();
			for (Map<String, Object> order : noStaffBatchOrders) {
				Long oNetbarId = MapUtils.getLong(order, "netbar_id");

				if (netbarId == null) {
					netbarId = oNetbarId;
				}

				if (!oNetbarId.equals(netbarId)) {// 非延续上次统计的网吧
					// 产生一笔交接班信息
					addOneStaffBatch(netbarId, totalAmount, amount, rebateAmount, redbagAmount, scoreAmount, orderNum,
							orderIds);

					// 初始化累计内容
					netbarId = oNetbarId;
					totalAmount = 0.0;
					amount = 0.0;
					rebateAmount = 0.0;
					redbagAmount = 0.0;
					scoreAmount = 0.0;
					orderNum = 0;
					orderIds = new ArrayList<>();
				}

				// 计算各类金额总和，并记录orderIds
				Double oTotalAmount = MapUtils.getDouble(order, "total_amount");
				if (oTotalAmount != null) {
					totalAmount = ArithUtil.add(totalAmount, oTotalAmount);
				}
				Double oAmount = MapUtils.getDouble(order, "amount");
				if (oAmount != null) {
					amount = ArithUtil.add(amount, oAmount);
				}
				Double oRebateAmount = MapUtils.getDouble(order, "rebate_amount");
				if (oRebateAmount != null) {
					rebateAmount = ArithUtil.add(rebateAmount, oRebateAmount);
				}
				Double oRedbagAmount = MapUtils.getDouble(order, "redbag_amount");
				if (oRedbagAmount != null) {
					redbagAmount = ArithUtil.add(redbagAmount, oRedbagAmount);
				}
				Double oScoreAmount = MapUtils.getDouble(order, "score_amount");
				if (oScoreAmount != null) {
					scoreAmount = ArithUtil.add(scoreAmount, oScoreAmount);
				}
				orderNum += 1;
				Long oId = MapUtils.getLong(order, "id");
				orderIds.add(oId);
			}

			// 产生最后一笔已申请未交接的订单交接班
			addOneStaffBatch(netbarId, totalAmount, amount, rebateAmount, redbagAmount, scoreAmount, orderNum,
					orderIds);
		}
	}

	/**
	 * 产生一笔交接班
	 */
	public void addOneStaffBatch(Long netbarId, Double totalAmount, Double amount, Double rebateAmount,
			Double redbagAmount, Double scoreAmount, Integer orderNum, List<Long> orderIds) {
		Date now = null;
		Calendar today = Calendar.getInstance();
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		now = today.getTime();

		if (netbarId != null) {
			// 保存雇员交接班数据
			NetbarStaffBatch batch = new NetbarStaffBatch();
			batch.setNetbarId(netbarId);
			batch.setTotalAmount(totalAmount);
			batch.setAmount(amount);
			batch.setRebateAmount(rebateAmount);
			batch.setRedbagAmount(redbagAmount);
			batch.setScoreAmount(scoreAmount);
			batch.setStatus(OrderConstants.STAFF_BATCH_UNAPPLY);
			batch.setOrderNum(orderNum);
			batch.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			batch.setUpdateUserId(0L);
			batch.setCreateUserId(0L);
			batch.setUpdateDate(now);
			batch.setCreateDate(now);
			batch = save(batch);

			// 保存批次订单
			List<NetbarStaffBatchOrder> nsbos = new ArrayList<>();
			for (Long oId : orderIds) {
				NetbarStaffBatchOrder bo = new NetbarStaffBatchOrder();
				bo.setBatchId(batch.getId());
				bo.setOrderId(oId);
				nsbos.add(bo);
			}
			netbarStaffBatchOrderService.save(nsbos);

			LOGGER.error("初始化一批已申请未交接订单：batchId " + batch.getId());
		}
	}

	/**
	 * 查询网吧交接班情况
	 * @param page
	 * @param params
	 * @return
	 */

	public PageVO list(int page, String areaCode, String netbarName, String beginDate, String endDate) {
		String condition = " where  sb.is_valid = 1";
		if (StringUtils.isNotBlank(beginDate)) {
			condition = SqlJoiner.join(condition, " AND sb.create_date >= '", beginDate, "'");
		}
		if (StringUtils.isNotBlank(endDate)) {
			condition = SqlJoiner.join(condition, " AND sb.create_date < '", endDate, "'");
		}
		if (StringUtils.isNotBlank(netbarName) || StringUtils.isNotBlank(areaCode)) {
			String netbarCondition = "where is_valid = 1";
			if (StringUtils.isNotBlank(areaCode)) {
				netbarCondition = netbarCondition + " and area_code like '" + areaCode + "%'";
			}
			if (StringUtils.isNotBlank(netbarName)) {
				netbarCondition = netbarCondition + "  and name like '%" + netbarName + "%'";
			}
			String netbarSql = "select id from netbar_t_info " + netbarCondition;
			condition = SqlJoiner.join(condition, " AND sb.netbar_id in (", netbarSql, ")");
		}

		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		Integer startRow = (page - 1) * pageSize;
		String sql = SqlJoiner.join(
				" select  sb.total_amount, sb.amount, sb.status, sb.order_num, sb.create_date, n.name ",
				" from netbar_t_staff_batch sb  left join netbar_t_info n  on n.id = sb.netbar_id ", condition,
				" order  by sb.create_date desc limit ", startRow.toString(), ", 30");

		List<Map<String, Object>> list = queryDao.queryMap(sql);
		String totalSql = SqlJoiner.join("SELECT COUNT(1) FROM netbar_t_staff_batch sb ", condition);
		Number total = (Number) queryDao.query(totalSql);
		if (total == null) {
			total = 0;
		}

		PageVO vo = new PageVO();
		vo.setList(list);
		vo.setTotal(total.intValue());
		vo.setCurrentPage(page);
		int isLast = total.intValue() > page * pageSize ? 1 : 0;
		vo.setIsLast(isLast);
		return vo;
	}

}
