package com.miqtech.master.service.netbar.resource;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.config.SystemConfig;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.NetbarConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.resource.NetbarResourceOrderDao;
import com.miqtech.master.entity.netbar.resource.NetbarResourceCommodityProperty;
import com.miqtech.master.entity.netbar.resource.NetbarResourceOrder;
import com.miqtech.master.entity.user.UserRedbag;
import com.miqtech.master.service.netbar.NetbarFundInfoService;
import com.miqtech.master.service.user.UserRedbagService;
import com.miqtech.master.thirdparty.util.SMSMessageUtil;
import com.miqtech.master.thirdparty.util.ShortUrlUtils;
import com.miqtech.master.utils.*;
import com.miqtech.master.vo.PageVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 资源商城订单Service
 */
@Component
public class NetbarResourceOrderService {

	public static final Logger LOGGER = LoggerFactory.getLogger(NetbarResourceOrderService.class);
	private static final String URL_SERVER_CONFIRM = "netbar/resource/toConfirmB?orderId=";// 服务确认

	ExecutorService exe = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

	@Autowired
	private SystemConfig systemConfig;
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private NetbarResourceOrderDao netbarResourceOrderDao;
	@Autowired
	private NetbarFundInfoService netbarFundInfoService;
	@Autowired
	private NetbarResourceCommodityPropertyService netbarResourceCommodityPropertyService;
	@Autowired
	private UserRedbagService userRedbagService;
	@Autowired
	private NetbarCommodityCategoryService netbarCommodityCategoryService;
	@Autowired
	private JedisConnectionFactory redisConnectionFactory;

	public NetbarResourceOrder findById(Long id) {
		if (id != null) {
			return netbarResourceOrderDao.findOne(id);
		}
		return null;
	}

	public NetbarResourceOrder findValidById(Long id) {
		return netbarResourceOrderDao.findByIdAndValid(id, CommonConstant.INT_BOOLEAN_TRUE);
	}

	public List<NetbarResourceOrder> findValidByIds(List<Long> ids) {
		return netbarResourceOrderDao.findByIdInAndValid(ids, CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * 查询网吧对某个商品的购买记录
	 */
	public List<NetbarResourceOrder> findValidByCommodityIdAndNetbarId(Long commodityId, Long netbarId) {
		List<Integer> status = Lists.newArrayList();
		status.add(NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_UNCHECK);
		status.add(NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_NETBAR);
		status.add(NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_SERVER);
		status.add(NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_CHECKED);
		status.add(NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_EXPIRED);
		return netbarResourceOrderDao.findByCommodityIdAndNetbarIdAndValidAndStatusIn(commodityId, netbarId,
				CommonConstant.INT_BOOLEAN_TRUE, status);
	}

	public NetbarResourceOrder save(NetbarResourceOrder o) {
		if (o.getId() == null) {
			if (o.getCreateDate() == null) {
				o.setCreateDate(new Date());
			}
			if (o.getValid() == null) {
				o.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			}
		}
		return netbarResourceOrderDao.save(o);
	}

	public List<NetbarResourceOrder> save(List<NetbarResourceOrder> orders) {
		return (List<NetbarResourceOrder>) netbarResourceOrderDao.save(orders);
	}

	public NetbarResourceOrder insertOrUpdate(NetbarResourceOrder o) {
		if (o != null) {
			Date now = new Date();
			o.setUpdateDate(now);
			if (o.getId() != null) {
				NetbarResourceOrder old = findById(o.getId());
				o = BeanUtils.updateBean(old, o);
			} else {
				o.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				o.setCreateDate(now);
			}
			return save(o);
		}
		return null;
	}

	/**
	 * 取消订单
	 */
	public NetbarResourceOrder cancel(Long id) {
		NetbarResourceOrder order = findById(id);
		return processCancle(order, true);
	}

	/**
	 * 取消订单
	 */
	public NetbarResourceOrder cancel(NetbarResourceOrder order) {
		Date serveDate = order.getServeDate();
		if (serveDate != null) {
			Date tomorrow = new Date();
			tomorrow = DateUtils.addDays(tomorrow, 1);
			if (serveDate.before(tomorrow)) {// 距订单生效时间不足24小时,不退款
				return processCancle(order, false);
			}
		}

		// 服务方确认且网吧未确认,不退款
		if (NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_SERVER.equals(order.getStatus())) {
			return processCancle(order, false);
		}

		return processCancle(order, true);
	}

	private NetbarResourceOrder processCancle(NetbarResourceOrder order, boolean backFund) {
		if (order != null && !NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_CANCEL.equals(order.getStatus())) {
			if (backFund) {//
				order.setStatus(NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_CANCEL);
			} else {
				order.setStatus(NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_SUCCESS_EXPIRE);
			}
			order.setUpdateDate(new Date());
			order = save(order);

			// 回滚网吧资金
			if (backFund) {
				String tradeNo = order.getTradeNo();
				boolean operated = netbarFundInfoService.refund(tradeNo);
				if (!operated) {
					LOGGER.error("回滚网吧资金失败:" + tradeNo);
				}
			}

			// 回滚商品余量
			Long propertyId = order.getPropertyId();
			NetbarResourceCommodityProperty property = netbarResourceCommodityPropertyService.findById(propertyId);
			Integer buyNum = order.getBuyNum();
			property.setInventory(property.getInventory() + buyNum);
			property = netbarResourceCommodityPropertyService.save(property);

			return order;
		}
		return null;
	}

	/**
	 * 分页列表
	 */
	public PageVO page(int page, Map<String, String> searchParam) {
		String condition = " WHERE nro.is_valid = 1";
		String totalCondition = condition;

		// 查询条件
		Map<String, Object> params = Maps.newHashMap();
		String query = MapUtils.getString(searchParam, "query");
		if (StringUtils.isNotBlank(query)) {
			String likeQuery = "%" + query + "%";
			params.put("query", likeQuery);
			condition = SqlJoiner.join(condition,
					" AND ( nrc.name LIKE :query OR n.name LIKE :query OR m.owner_telephone LIKE :query OR nrcp.name LIKE :query )");
			totalCondition = SqlJoiner.join(totalCondition, " AND ( nrc.name LIKE '", likeQuery, "' OR n.name LIKE '",
					likeQuery, "' OR m.owner_telephone LIKE '", likeQuery, "' OR nrcp.name LIKE '", likeQuery, "' )");
		}
		String beginDate = MapUtils.getString(searchParam, "beginDate");
		if (StringUtils.isNotBlank(beginDate)) {
			params.put("beginDate", beginDate);
			condition = SqlJoiner.join(condition, " AND nro.effective_date >= :beginDate");
			totalCondition = SqlJoiner.join(totalCondition, " AND nro.effective_date >= '", beginDate, "'");
		}
		String endDate = MapUtils.getString(searchParam, "endDate");
		if (StringUtils.isNotBlank(endDate)) {
			params.put("endDate", endDate);
			condition = SqlJoiner.join(condition, " AND nro.effective_date < ADDDATE(:endDate, INTERVAL 1 DAY)");
			totalCondition = SqlJoiner.join(totalCondition, " AND nro.effective_date < ADDDATE('", endDate,
					"', INTERVAL 1 DAY)");
		}
		String areaCode = MapUtils.getString(searchParam, "areaCode");
		if (NumberUtils.isNumber(areaCode)) {
			condition = SqlJoiner.join(condition, " AND LEFT(n.area_code, 2) = LEFT('", areaCode, "', 2)");
			totalCondition = SqlJoiner.join(totalCondition, " AND LEFT(n.area_code, 2) = LEFT('", areaCode, "', 2)");
		}
		String levels = MapUtils.getString(searchParam, "levels");
		if (NumberUtils.isNumber(levels)) {
			condition = SqlJoiner.join(condition, " AND n.levels = " + levels);
			totalCondition = SqlJoiner.join(totalCondition, " AND n.levels = " + levels);
		}
		String status = MapUtils.getString(searchParam, "status");
		if (NumberUtils.isNumber(status)) {
			if (NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_CHECKED.toString().equals(status)) {
				condition = SqlJoiner.join(condition, " AND nro.status in (",
						NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_CHECKED.toString(), ", ",
						NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_SUCCESS_EXPIRE.toString(), ")");
				totalCondition = SqlJoiner.join(totalCondition, " AND nro.status in (",
						NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_CHECKED.toString(), ", ",
						NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_SUCCESS_EXPIRE.toString(), ")");
			} else {
				condition = SqlJoiner.join(condition, " AND nro.status = " + status);
				totalCondition = SqlJoiner.join(totalCondition, " AND nro.status = " + status);
			}
		}
		String hasComment = MapUtils.getString(searchParam, "hasComment");
		if (NumberUtils.isNumber(hasComment)) {
			if (CommonConstant.INT_BOOLEAN_TRUE.toString().equals(hasComment)) {
				condition = SqlJoiner.join(condition,
						" AND nro.comments IS NOT NULL AND LENGTH(TRIM(nro.comments)) > 0");
				totalCondition = SqlJoiner.join(totalCondition,
						" AND nro.comments IS NOT NULL AND LENGTH(TRIM(nro.comments)) > 0");
			} else {
				condition = SqlJoiner.join(condition, " AND (nro.comments IS NULL OR LENGTH(TRIM(nro.comments)) <= 0)");
				totalCondition = SqlJoiner.join(totalCondition,
						" AND (nro.comments IS NULL OR LENGTH(TRIM(nro.comments)) <= 0)");
			}
		}
		String hasRemark = MapUtils.getString(searchParam, "hasRemark");
		if (NumberUtils.isNumber(hasRemark)) {
			if (CommonConstant.INT_BOOLEAN_TRUE.toString().equals(hasRemark)) {
				condition = SqlJoiner.join(condition, " AND nro.remarks IS NOT NULL AND LENGTH(TRIM(nro.remarks)) > 0");
				totalCondition = SqlJoiner.join(totalCondition,
						" AND nro.remarks IS NOT NULL AND LENGTH(TRIM(nro.remarks)) > 0");
			} else {
				condition = SqlJoiner.join(condition, " AND (nro.remarks IS NULL OR LENGTH(TRIM(nro.remarks)) <= 0)");
				totalCondition = SqlJoiner.join(totalCondition,
						" AND (nro.remarks IS NULL OR LENGTH(TRIM(nro.remarks)) <= 0)");
			}
		}
		String type = MapUtils.getString(searchParam, "type");// 类型:1-自有,2-第三方
		if (NumberUtils.isNumber(type)) {
			condition = SqlJoiner.join(condition, " AND ncc.pid = ", type);
			totalCondition = SqlJoiner.join(totalCondition, " AND ncc.pid = ", type);
		}
		String netbarId = MapUtils.getString(searchParam, "netbarId");
		if (NumberUtils.isNumber(netbarId)) {
			condition = SqlJoiner.join(condition, " AND nro.netbar_id = " + netbarId);
			totalCondition = SqlJoiner.join(totalCondition, " AND nro.netbar_id = " + netbarId);
		}

		// 分页
		String limit = "";
		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page > 0) {
			Integer startRow = (page - 1) * pageSize;
			limit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());
		}

		// 查询总数
		String totalSql = SqlJoiner.join(
				"SELECT COUNT(1) FROM ( SELECT 1 FROM netbar_resource_order nro JOIN netbar_resource_commodity nrc ON nro.commodity_id = nrc.id",
				" JOIN netbar_resource_commodity_property nrcp ON nro.property_id = nrcp.id",
				" JOIN netbar_t_info n ON nro.netbar_id = n.id JOIN netbar_t_merchant m ON nro.netbar_id = m.netbar_id AND m.is_valid = 1",
				" JOIN sys_t_area sa ON nrc.province = sa.area_code JOIN netbar_commodity_category ncc ON nrc.category_id = ncc.id",
				totalCondition, " GROUP BY nro.id) t");
		Number total = queryDao.query(totalSql);
		if (total == null) {
			total = 0;
		}

		// 查询列表
		List<Map<String, Object>> list = null;
		if (total.longValue() > 0) {
			String sql = SqlJoiner.join(
					"SELECT nro.id, nro.status, nro.trade_no tradeNo, nro.remarks, nro.comments, nro.effective_date  createDate, nrc.name commodityName, nrc.province provinceAreaCode,",
					" nrcp.name propertyName, n.name netbarName,n.id netbarId , n.address netbarAddress, n.area_code netbarAreaCode, m.owner_telephone ownerTelephone, nro.buy_num buyNum, nro.serve_date serveDate, sa.name provinceName, nrcp.settl_date settleDate, nrcp.conditions conditions,",
					" nrc.executes, nrc.execute_phone executePhone, n.levels, nrcp.cate_type cateType,nro.total_amount totalAmount, nro.amount amount ,nro.quota_amount quotaAmount,nrc.description,ncc.id categoryId,ncc1.name typeName,",
					" if(nrcp.cate_type = 0 and nro.status not in (-1, 3) and TIMESTAMPDIFF( HOUR, now(), serve_date ) > 0 and TIMESTAMPDIFF( HOUR, now(), serve_date ) < 24, 1, 0) nearOverdue",
					" FROM netbar_resource_order nro JOIN netbar_resource_commodity nrc ON nro.commodity_id = nrc.id",
					" JOIN netbar_resource_commodity_property nrcp ON nro.property_id = nrcp.id",
					" JOIN netbar_t_info n ON nro.netbar_id = n.id JOIN netbar_t_merchant m ON nro.netbar_id = m.netbar_id AND m.is_valid = 1",
					" JOIN sys_t_area sa ON LEFT(n.area_code, 2) = LEFT(sa.area_code, 2)  and sa.pid=1 JOIN netbar_commodity_category ncc ON nrc.category_id = ncc.id JOIN netbar_commodity_category ncc1 ON ncc1.id = ncc.pid",
					condition, " GROUP BY nro.id ORDER BY nearOverdue desc, nro.effective_date DESC", limit);
			list = queryDao.queryMap(sql, params);
		}

		PageVO vo = new PageVO();
		vo.setList(list);
		vo.setTotal(total.intValue());
		int isLast = total.intValue() > page * pageSize ? 1 : 0;
		vo.setIsLast(isLast);
		return vo;
	}

	/**
	 * 查询交易明细前10
	 */
	public List<Map<String, Object>> queryDetailOrder10(Long netbarId) {
		String sql = "select  a.create_date,a.type,a.amount,a.status from netbar_fund_detail a where a.netbar_id="
				+ netbarId + " order by a.create_date desc limit 0,10 ";
		return queryDao.queryMap(sql);
	}

	/**
	 * 统计总记录数 总金额数
	 */
	public Map<String, Object> statis(Map<String, String> searchParam) {
		String condition = " WHERE nro.is_valid = 1";
		// 查询条件
		Map<String, Object> params = Maps.newHashMap();
		String query = MapUtils.getString(searchParam, "query");
		if (StringUtils.isNotBlank(query)) {
			String likeQuery = "%" + query + "%";
			params.put("query", likeQuery);
			condition = SqlJoiner.join(condition,
					" AND ( nrc.name LIKE :query OR n.name LIKE :query OR m.owner_telephone LIKE :query OR nrcp.name LIKE :query )");
		}
		String beginDate = MapUtils.getString(searchParam, "beginDate");
		if (StringUtils.isNotBlank(beginDate)) {
			params.put("beginDate", beginDate);
			condition = SqlJoiner.join(condition, " AND nro.effective_date >= :beginDate");
		}
		String endDate = MapUtils.getString(searchParam, "endDate");
		if (StringUtils.isNotBlank(endDate)) {
			params.put("endDate", endDate);
			condition = SqlJoiner.join(condition, " AND nro.effective_date < ADDDATE(:endDate, INTERVAL 1 DAY)");
		}
		String areaCode = MapUtils.getString(searchParam, "areaCode");
		if (NumberUtils.isNumber(areaCode)) {
			condition = SqlJoiner.join(condition, " AND LEFT(n.area_code, 2) = LEFT('", areaCode, "', 2)");
		}
		String levels = MapUtils.getString(searchParam, "levels");
		if (NumberUtils.isNumber(levels)) {
			condition = SqlJoiner.join(condition, " AND n.levels = " + levels);
		}
		String status = MapUtils.getString(searchParam, "status");
		if (NumberUtils.isNumber(status)) {
			condition = SqlJoiner.join(condition, " AND nro.status = " + status);
		}
		String hasComment = MapUtils.getString(searchParam, "hasComment");
		if (NumberUtils.isNumber(hasComment)) {
			if (CommonConstant.INT_BOOLEAN_TRUE.toString().equals(hasComment)) {
				condition = SqlJoiner.join(condition,
						" AND nro.comments IS NOT NULL AND LENGTH(TRIM(nro.comments)) > 0");
			} else {
				condition = SqlJoiner.join(condition, " AND (nro.comments IS NULL OR LENGTH(TRIM(nro.comments)) <= 0)");
			}
		}
		String hasRemark = MapUtils.getString(searchParam, "hasRemark");
		if (NumberUtils.isNumber(hasRemark)) {
			if (CommonConstant.INT_BOOLEAN_TRUE.toString().equals(hasRemark)) {
				condition = SqlJoiner.join(condition, " AND nro.remarks IS NOT NULL AND LENGTH(TRIM(nro.remarks)) > 0");
			} else {
				condition = SqlJoiner.join(condition, " AND (nro.remarks IS NULL OR LENGTH(TRIM(nro.remarks)) <= 0)");
			}
		}
		String type = MapUtils.getString(searchParam, "type");// 类型:1-自有,2-第三方
		if (NumberUtils.isNumber(type)) {
			condition = SqlJoiner.join(condition, " AND ncc.pid = ", type);
		}
		String netbarId = MapUtils.getString(searchParam, "netbarId");
		if (NumberUtils.isNumber(netbarId)) {
			condition = SqlJoiner.join(condition, " AND nro.netbar_id = " + netbarId);
		}

		String sql = SqlJoiner.join(
				" SELECT count(1) count, sum(total_amount) sumTotalAmount,count(distinct n.id) netbarCount",
				" FROM netbar_resource_order nro JOIN netbar_resource_commodity nrc ON nro.commodity_id = nrc.id",
				" JOIN netbar_resource_commodity_property nrcp ON nro.property_id = nrcp.id",
				" JOIN netbar_t_info n ON nro.netbar_id = n.id JOIN netbar_t_merchant m ON nro.netbar_id = m.netbar_id AND m.is_valid = 1",
				" JOIN sys_t_area sa ON nrc.province = sa.area_code JOIN netbar_commodity_category ncc ON nrc.category_id = ncc.id JOIN netbar_commodity_category ncc1 ON ncc1.id = ncc.pid",
				condition);
		return queryDao.querySingleMap(sql, params);
	}

	/**
	 * 统计总记录数 总金额数
	 */
	public Map<String, Object> statis(String beginTime, String endTime, Long netbarId) {
		String sql = "SELECT count(1) count, sum(total_amount) sumTotalAmount, sum(amount) sumAmount, sum(quota_amount) sumQuotaAmount FROM netbar_resource_order where create_date>='"
				+ beginTime + "' and create_date <= '" + endTime + "'and is_valid = 1 and netbar_id =" + netbarId;
		return queryDao.querySingleMap(sql);
	}

	/**
	 * 添加购买频次限制
	 */
	public void addBuyLimit(long netbarId) {
		String key = "wy_merchant_resource_buy_limit" + netbarId;
		RedisAtomicInteger leftNum = new RedisAtomicInteger(key, redisConnectionFactory);
		leftNum.incrementAndGet();
		leftNum.expire(60, TimeUnit.SECONDS);
	}

	/**
	 * 购买商品
	 */
	public synchronized int buy(long commodityId, long propertyId, long netbarId, float totalAmount, float quotaAmount,
			float payAmount, float amount, int buyNum, String merchantTel, String executorTel, boolean isRedbag) {
		// 更新项目余量
		NetbarResourceCommodityProperty property = netbarResourceCommodityPropertyService.findById(propertyId);
		Integer inventory = property.getInventory();
		if (inventory == null || inventory <= 0) {
			return -1;
		}
		if (inventory - buyNum < 0) {
			return -1;
		}

		try {
			addBuyLimit(netbarId);
		} catch (Exception e) {
			LOGGER.error("记录网吧购买频次异常:", e);
		}

		property.setInventory(inventory - buyNum);
		property = netbarResourceCommodityPropertyService.save(property);

		// 保存订单信息
		NetbarResourceOrder o = new NetbarResourceOrder();
		o.setNetbarId(netbarId);
		o.setCommodityId(commodityId);
		o.setPropertyId(propertyId);
		o.setTotalAmount(totalAmount);
		o.setQuotaAmount(quotaAmount);
		o.setPayAmount(payAmount);
		o.setAmount(amount);
		o.setBuyNum(buyNum);
		if (NetbarConstant.NETBAR_RESOURCE_COMMODITY_CATE_TYPE_DATE.equals(property.getCateType())) {// 按日期出售
			o.setServeDate(property.getSettlDate());
		}
		Integer expireDate = property.getValidity();
		if (expireDate == null) {
			expireDate = 1;
		}
		if (isRedbag) {
			Date ed = DateUtils.getToday();
			ed = org.apache.commons.lang3.time.DateUtils.setHours(ed, 23);
			ed = org.apache.commons.lang3.time.DateUtils.setMinutes(ed, 59);
			ed = org.apache.commons.lang3.time.DateUtils.setSeconds(ed, 59);
			o.setExpireDate(DateUtils.addDays(ed, expireDate));
		} else {
			if (NetbarConstant.NETBAR_RESOURCE_COMMODITY_CATE_TYPE_DATE.equals(property.getCateType())) {// 按日期出售
				Date ed = property.getSettlDate();
				ed = org.apache.commons.lang3.time.DateUtils.setHours(ed, 23);
				ed = org.apache.commons.lang3.time.DateUtils.setMinutes(ed, 59);
				ed = org.apache.commons.lang3.time.DateUtils.setSeconds(ed, 59);
				o.setExpireDate(ed);
			} else {
				Date ed = DateUtils.getToday();
				ed = org.apache.commons.lang3.time.DateUtils.setHours(ed, 23);
				ed = org.apache.commons.lang3.time.DateUtils.setMinutes(ed, 59);
				ed = org.apache.commons.lang3.time.DateUtils.setSeconds(ed, 59);
				o.setExpireDate(DateUtils.addDays(ed, expireDate));
			}
		}
		String tradeNo = o.generateTradeNo();
		if (isRedbag) {
			o.setStatus(NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_CHECKED);
		} else {
			o.setStatus(NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_NETBAR);
		}
		o = save(o);

		if (isRedbag) {
			addRedbagLeftNum(o);
		}

		// 扣除资金及配额奖金
		Map<String, Object> config = Maps.newHashMap();
		config.put("amount", amount);
		config.put("quota", quotaAmount);
		config.put("serNumbers", tradeNo);
		config.put("type", NetbarConstant.NETBAR_FUND_DETAIL_TYPE_CONSUME);
		config.put("direction", NetbarConstant.NETBAR_FUND_DETAIL_DIRECTION_EXPAND);
		Map<Long, Map<String, Object>> netbarAmounts = Maps.newHashMap();
		netbarAmounts.put(netbarId, config);
		netbarFundInfoService.addAccountsBatch(netbarAmounts);
		final Long orderId = o.getId();
		if (!isRedbag && StringUtils.isNotBlank(executorTel)) {
			String propertyName = property.getPropertyName();
			exe.execute(() -> {
				try {
					String[] phoneNum1 = { merchantTel };
					String[] params1 = { propertyName };
					SMSMessageUtil.sendTemplateMessage(phoneNum1, "3022002", params1);
				} catch (Exception e1) {
					LOGGER.error("资源商品购买网吧商户确认短信异常:", e1);
				}

				// 发送服务商确认短信
				try {
					String[] phoneNum2 = { executorTel };
					String url = ShortUrlUtils.toShortUrl(systemConfig.getAdminDomain() + URL_SERVER_CONFIRM + orderId)
							+ " ";
					String[] params2 = { url };
					SMSMessageUtil.sendTemplateMessage(phoneNum2, "7232", params2);
				} catch (Exception e2) {
					LOGGER.error("资源商品购买服务商确认短信异常:", e2);
				}
			});
		}

		return 0;

	}

	/**把红包的剩余数量添加到redis
	 * @param tradeNo
	 */
	public void addRedbagLeftNum(NetbarResourceOrder order) {
		String key = CommonConstant.MERCHANT_REDBAG_LEFT_NUM + order.getTradeNo();
		RedisAtomicInteger leftNum = new RedisAtomicInteger(key, redisConnectionFactory);
		leftNum.set(order.getBuyNum());
		leftNum.expire(order.getExpireDate().getTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

	/**
	 * 查询所有过期未确认的订单
	 */
	public List<NetbarResourceOrder> queryUnconfirmedExpiredOrders() {
		// 先通过原生sql查询
		String sql = "SELECT id FROM netbar_resource_order nro WHERE status IN (0, 1, 2) AND ADDDATE( nro.create_date, INTERVAL 10 HOUR ) <= now() AND is_valid = 1";
		List<Map<String, Object>> orders = queryDao.queryMap(sql);
		if (CollectionUtils.isNotEmpty(orders)) {
			// 根据ID查询完整的实体
			List<Long> ids = Lists.newArrayList();
			for (Map<String, Object> o : orders) {
				ids.add(MapUtils.getLong(o, "id"));
			}
			return findValidByIds(ids);
		}
		return null;
	}

	/**
	 * 取消超时未确认订单
	 */
	public void cancelUnconfirmedOrders() {
		// 查询未确认订单
		List<NetbarResourceOrder> orders = queryUnconfirmedExpiredOrders();

		// 区分是否为网吧单方面未确认订单，做区别处理
		List<NetbarResourceOrder> netbarUncheckedOrders = Lists.newArrayList();
		if (CollectionUtils.isNotEmpty(orders)) {
			for (NetbarResourceOrder o : orders) {
				if (NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_SERVER.equals(o.getStatus())) {// 服务商确认且网吧未确认，不退款
					processCancle(o, false);
				} else {// 正常取消订单
					processCancle(o, true);
				}
			}
		}
		save(netbarUncheckedOrders);
	}

	public Map<String, Object> queryDetailById(Long orderId) {
		String sql = " select nro.trade_no tradeNo,nro.id orderId,  nro.create_date createDate,nro.comments comments ,"
				+ "    nrc.name commodityName,nrc.url commodityUrl,   nro.buy_num buyNum, nro.serve_date serveDate, "
				+ "    nrcp.settl_date settleDate, nrc.executes, nrc.execute_phone executePhone from"
				+ "    netbar_resource_order nro  join netbar_resource_commodity nrc "
				+ "        on nro.commodity_id = nrc.id  join netbar_resource_commodity_property nrcp "
				+ "        on nro.property_id = nrcp.id  where nro.id = " + orderId;
		return queryDao.querySingleMap(sql);
	}

	/**
	 * 查看红包使用情况
	 * @param orderId
	 * @param status
	 * @param page
	 * @return
	 */
	public PageVO findOrderRedbagList(Long orderId, int status, int page) {
		page = page <= 0 ? 1 : page;
		int start = (page - 1) * PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		String statusSql = "";
		if (status == 0) {
			statusSql = " and ur.is_valid = " + status;
		}
		PageVO result = new PageVO();
		String sql = " select * from (select nro.buy_num-(@rowNO \\:= @rowNo + 1) as leftnum,ur.create_date,ur.is_valid,ur.netbar_id,ur.usable,ur.amount,ur.limit_min_money,ur.trade_no,"
				+ "  ui.nickname,ui.id,ui.telephone  from  user_r_redbag  ur "
				+ "  left join user_t_info  ui  on ur.user_id = ui.id"
				+ " left join netbar_resource_order nro  on ur.trade_no =nro.trade_no   where ur.trade_no =  "
				+ "  (select         trade_no     from        netbar_resource_order     where id = " + orderId + ") "
				+ statusSql
				+ " order by ur.create_date asc)tt ,(select @rowNO \\:= 0) b order by create_date desc  limit " + start
				+ ", " + PageUtils.ADMIN_DEFAULT_PAGE_SIZE;

		List<Map<String, Object>> queryMap = queryDao.queryMap(sql);
		result.setList(queryMap);
		String countSql = "select count(id) from   user_r_redbag  ur  where  ur.trade_no =    (select trade_no from netbar_resource_order where id = "
				+ orderId + ") " + statusSql;
		Number totalCount = queryDao.query(countSql);
		result.setCurrentPage(page);
		result.setIsLast(PageUtils.isBottom(page, totalCount.intValue()));
		result.setTotal(totalCount.intValue());
		return result;
	}

	/**
	 * 查看增值券使用情况
	 * @param orderId
	 * @param status
	 * @param page
	 * @return
	 */
	public PageVO findValueAddedCardList(Long orderId, int status, int page) {
		page = page <= 0 ? 1 : page;
		int start = (page - 1) * PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		String statusSql = "";
		if (status == 0 || status == 1 || status == -1) {
			statusSql = " and ur.is_valid = " + status;
		}
		PageVO result = new PageVO();
		String sql = " select * from (select nro.buy_num-(@rowNO \\:= @rowNo + 1) as leftnum,ur.create_date,ur.is_valid,ur.netbar_id,ur.usable,ur.amount,ur.trade_no,"
				+ "  ui.nickname,ui.id,ui.telephone  from  user_value_added_card  ur "
				+ "  left join user_t_info  ui  on ur.user_id = ui.id"
				+ " left join netbar_resource_order nro  on ur.trade_no =nro.trade_no   where ur.trade_no =  "
				+ "  (select         trade_no     from        netbar_resource_order     where id = " + orderId + ") "
				+ statusSql
				+ " order by ur.create_date asc)tt ,(select @rowNO \\:= 0) b order by create_date desc  limit " + start
				+ ", " + PageUtils.ADMIN_DEFAULT_PAGE_SIZE;

		List<Map<String, Object>> queryMap = queryDao.queryMap(sql);
		result.setList(queryMap);
		String countSql = "select count(id) from   user_value_added_card  ur  where  ur.trade_no =    (select trade_no from netbar_resource_order where id = "
				+ orderId + ") " + statusSql;
		Number totalCount = queryDao.query(countSql);
		result.setCurrentPage(page);
		result.setIsLast(PageUtils.isBottom(page, totalCount.intValue()));
		result.setTotal(totalCount.intValue());
		return result;
	}

	/**
	 * 查询当天须处理的过期服务(非取消、过期状态的,过期时间在昨天的)
	 */
	public List<NetbarResourceOrder> queryExpiredOrders() {
		List<Integer> statuses = Lists.newArrayList();
		statuses.add(NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_UNCHECK);
		statuses.add(NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_SERVER);
		statuses.add(NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_NETBAR);
		statuses.add(NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_CHECKED);
		Date expireDateBegin = DateUtils.getYesterday();
		Date expireDateEnd = expireDateBegin;
		expireDateEnd = DateUtils.addHours(expireDateEnd, 23);
		expireDateEnd = DateUtils.addMinutes(expireDateEnd, 59);
		expireDateEnd = DateUtils.addSeconds(expireDateEnd, 59);
		return netbarResourceOrderDao.findByStatusInAndExpireDateGreaterThanEqualAndExpireDateLessThanEqualAndValid(
				statuses, expireDateBegin, expireDateEnd, CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * 取消过期订单,并回滚资金
	 */
	public void cancelExpiredOrders() {
		List<NetbarResourceOrder> orders = queryExpiredOrders();
		if (CollectionUtils.isNotEmpty(orders)) {
			for (NetbarResourceOrder o : orders) {
				// 查询订单类型是否为红包
				Long commodityId = o.getCommodityId();
				boolean isRedbag = false;
				Map<String, Object> category = netbarCommodityCategoryService
						.queryByNetbarResourceCommodityId(commodityId);
				String categoryName = MapUtils.getString(category, "name");
				if ("红包".equals(categoryName)) {
					isRedbag = true;
				}
				boolean isValueAdded = false;
				if ("增值券".equals(categoryName)) {
					isValueAdded = true;
				}

				if (isRedbag) {// 红包按未用部分退款
					// 统计发放红包数量
					Number usedAmount = queryDao.query(
							SqlJoiner.join("SELECT sum(amount) amount FROM user_r_redbag ur WHERE ur.trade_no = '",
									o.getTradeNo(), "'"));
					if (usedAmount == null) {
						usedAmount = 0.0;
					}

					// 按支付比例计算退款资金、配额奖金的数额
					Float totalAmount = o.getTotalAmount();
					Float amount = o.getAmount();
					Double unusedAmount = ArithUtil.sub(totalAmount.doubleValue(), usedAmount.doubleValue());
					if (unusedAmount > 0.0) {
						Double refundAmount = ArithUtil.mul(ArithUtil.div(amount, totalAmount), unusedAmount);
						Double refundQuota = ArithUtil.sub(unusedAmount, refundAmount);

						// 记录回滚资金
						Map<Long, Map<String, Object>> netbarAmounts = Maps.newHashMap();
						Long netbarId = o.getNetbarId();
						Map<String, Object> config = Maps.newHashMap();
						config.put("amount", refundAmount);
						config.put("quota", refundQuota);
						config.put("serNumbers", o.getTradeNo());
						config.put("type", NetbarConstant.NETBAR_FUND_DETAIL_TYPE_REFUND);
						config.put("direction", NetbarConstant.NETBAR_FUND_DETAIL_DIRECTION_REFUND);
						netbarAmounts.put(netbarId, config);
						netbarFundInfoService.addAccountsBatch(netbarAmounts);
					}
				} else if (isValueAdded) {// 非红包,是增值券,按确认状态决定是否退款
					// 统计已使用的增值券金额
					String tradeNo = o.getTradeNo();
					Number usedAmount = queryDao.query(SqlJoiner.join(
							"SELECT sum(amount) amount FROM user_value_added_card ur WHERE ur.trade_no = '", tradeNo,
							"' and is_valid = 0"));
					if (usedAmount == null) {
						usedAmount = 0.0;
					}

					// 获取支付金额,并减去已退金额,防止重复退款
					Double totalAmount = o.getTotalAmount().doubleValue();
					String refundedAmountSql = SqlJoiner.join(
							"select sum(amount) from netbar_fund_detail where ser_numbers = '", tradeNo,
							"' and direction = 0 and is_valid = 1");
					Number refundedAmount = queryDao.query(refundedAmountSql);
					if (refundedAmount == null) {
						refundedAmount = 0;
					}

					// 按支付比例计算退款资金、配额奖金的数额
					Float amount = o.getAmount();
					Double unusedAmount = ArithUtil.sub(ArithUtil.sub(totalAmount, refundedAmount.doubleValue()),
							usedAmount.doubleValue());
					if (unusedAmount > 0.0) {
						Double refundAmount = ArithUtil.mul(ArithUtil.div(amount, totalAmount), unusedAmount);
						Double refundQuota = ArithUtil.sub(unusedAmount, refundAmount);

						// 记录回滚资金
						Map<Long, Map<String, Object>> netbarAmounts = Maps.newHashMap();
						Long netbarId = o.getNetbarId();
						Map<String, Object> config = Maps.newHashMap();
						config.put("amount", refundAmount);
						config.put("quota", refundQuota);
						config.put("serNumbers", o.getTradeNo());
						config.put("type", NetbarConstant.NETBAR_FUND_DETAIL_TYPE_REFUND);
						config.put("direction", NetbarConstant.NETBAR_FUND_DETAIL_DIRECTION_REFUND);
						netbarAmounts.put(netbarId, config);
						netbarFundInfoService.addAccountsBatch(netbarAmounts);
					}
				} else {// 非红包并且非增值券,按确认状态决定是否退款
					if (NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_UNCHECK.equals(o.getStatus())
							|| NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_NETBAR.equals(o.getStatus())) {// 双方未确认 或 服务方单方未确认 则退款
						// 更新订单状态为已取消
						o.setStatus(NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_CANCEL);
						save(o);

						// 记录回滚资金
						Map<Long, Map<String, Object>> netbarAmounts = Maps.newHashMap();
						Long netbarId = o.getNetbarId();
						Map<String, Object> config = Maps.newHashMap();
						config.put("amount", o.getAmount());
						config.put("quota", o.getQuotaAmount());
						config.put("serNumbers", o.getTradeNo());
						config.put("type", NetbarConstant.NETBAR_FUND_DETAIL_TYPE_REFUND);
						config.put("direction", NetbarConstant.NETBAR_FUND_DETAIL_DIRECTION_REFUND);
						netbarAmounts.put(netbarId, config);
						netbarFundInfoService.addAccountsBatch(netbarAmounts);
					} else if (NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_SERVER.equals(o.getStatus())) {
						// 更新订单状态
						o.setStatus(NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_SUCCESS_EXPIRE);
						save(o);
					}
				}

				// 暂停1秒,确保上次操作已完成
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					LOGGER.error("暂停异常:", e);
				}
			}
		}
	}

	/**
	 * 获取用户的购买记录
	 */
	public List<Map<String, Object>> netbarOrderHistory(Long netbarId) {
		if (netbarId == null) {
			return null;
		}

		String sql = "SELECT * FROM netbar_resource_order WHERE netbar_id = " + netbarId;
		return queryDao.queryMap(sql);
	}

	/**
	 * 检查用户是否首次购买资源商品
	 */
	public boolean firstBuy(Long netbarId) {
		List<Map<String, Object>> history = netbarOrderHistory(netbarId);
		return CollectionUtils.isEmpty(history);
	}

	/**
	 * 过期红包退款
	 */
	public void refundExpiredRedbag() {
		// 查询过期的资源商城类型用户红包
		String sql = SqlJoiner.join("SELECT nrp.id propertyId, nro.id orderId, nro.total_amount tradeTotalAmount,",
				" nro.amount tradeAmount, nro.quota_amount tradeQuota, ur.trade_no tradeNo, ur.id redbagId,",
				" ur.user_id userId, ur.usable, ur.amount, ur.netbar_id netbarId, ur.create_date createDate",
				" FROM ( SELECT ur.* FROM user_r_redbag ur JOIN sys_t_redbag sr ON ur.redbag_id = sr.id AND sr.is_valid = 1",
				" WHERE ur.is_valid = 1 AND ur.usable = 1 AND ur.trade_no IS NOT NULL",
				" AND ADDDATE( ur.create_date, INTERVAL sr. DAY DAY ) < now() ) ur",
				" JOIN netbar_resource_order nro ON nro.trade_no = ur.trade_no AND nro.is_valid = 1",
				" JOIN netbar_resource_commodity_property nrp ON nro.property_id = nrp.id AND nrp.is_valid = 1 GROUP BY ur.id");
		List<Map<String, Object>> expiredRedbags = queryDao.queryMap(sql);

		if (CollectionUtils.isEmpty(expiredRedbags)) {
			return;
		}

		// 整理红包ID及退款金额
		List<Long> redbagIds = Lists.newArrayList();
		Map<String, Map<String, Object>> tradeRedbagAmounts = Maps.newHashMap();
		for (Map<String, Object> r : expiredRedbags) {
			Long netbarId = MapUtils.getLong(r, "netbarId");
			Long redbagId = MapUtils.getLong(r, "redbagId");
			Integer amount = MapUtils.getInteger(r, "amount");
			String tradeNo = MapUtils.getString(r, "tradeNo");
			Double tradeAmount = MapUtils.getDouble(r, "tradeAmount");
			Double tradeQuota = MapUtils.getDouble(r, "tradeQuota");
			Double tradeTotalAmuount = MapUtils.getDouble(r, "tradeTotalAmount");
			if (amount == null) {
				amount = 0;
			}

			// 记录红包ID,以更新状态
			redbagIds.add(redbagId);

			// 以订单为组,计算网吧应退款总额
			Map<String, Object> netbarConfig = tradeRedbagAmounts.get(tradeNo);
			if (MapUtils.isEmpty(netbarConfig)) {
				netbarConfig = Maps.newHashMap();
				netbarConfig.put("netbarId", netbarId);
				netbarConfig.put("tradeNo", tradeNo);
				netbarConfig.put("tradeAmount", tradeAmount);
				netbarConfig.put("tradeQuota", tradeQuota);
				netbarConfig.put("tradeTotalAmount", tradeTotalAmuount);
			}
			Integer netbarAmount = MapUtils.getInteger(netbarConfig, "amount");
			if (netbarAmount == null) {
				netbarAmount = 0;
			}
			netbarAmount += amount;
			netbarConfig.put("amount", netbarAmount);

			tradeRedbagAmounts.put(tradeNo, netbarConfig);
		}

		// 查询红包，并设置红包为不可用
		List<UserRedbag> redbags = userRedbagService.findByIdIn(redbagIds);
		if (CollectionUtils.isNotEmpty(redbags)) {
			for (UserRedbag r : redbags) {
				r.setUsable(CommonConstant.INT_BOOLEAN_FALSE);
			}
			userRedbagService.save(redbags);
		}

		// 回滚网吧金额
		Set<String> tradeNoes = tradeRedbagAmounts.keySet();
		if (CollectionUtils.isNotEmpty(tradeNoes)) {
			// 退款以订单为单位
			for (String tradeNoKey : tradeNoes) {
				Map<String, Object> config = tradeRedbagAmounts.get(tradeNoKey);
				Long netbarId = MapUtils.getLong(config, "netbarId");
				String tradeNo = MapUtils.getString(config, "tradeNo");
				double amount = MapUtils.getInteger(config, "amount").doubleValue();
				Double tradeAmount = MapUtils.getDouble(config, "tradeAmount");
				Double tradeTotalAmuount = MapUtils.getDouble(config, "tradeTotalAmount");

				// 按购买比例计算退款金额及配额奖金
				Double refundAmount = ArithUtil.mul(ArithUtil.div(tradeAmount, tradeTotalAmuount), amount);
				refundAmount = ArithUtil.scale(refundAmount, 2);
				Double refundQuota = ArithUtil.sub(amount, refundAmount);

				// 增加用户资金
				Map<String, Object> addConfig = Maps.newHashMap();
				addConfig.put("amount", refundAmount);
				addConfig.put("quota", refundQuota);
				addConfig.put("serNumbers", tradeNo);
				addConfig.put("type", NetbarConstant.NETBAR_FUND_DETAIL_TYPE_REFUND);
				addConfig.put("direction", NetbarConstant.NETBAR_FUND_DETAIL_DIRECTION_REFUND);

				Map<Long, Map<String, Object>> netbarAmounts = Maps.newHashMap();
				netbarAmounts.put(netbarId, addConfig);
				netbarFundInfoService.addAccountsBatch(netbarAmounts);

				// 暂停1秒,确保上次操作已完成
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					LOGGER.error("暂停异常:", e);
				}
			}
		}
	}

	/**
	 * 检查网吧下是否有在用的红包订单
	 */
	public boolean hasAvailableOrder(long netbarId) {
		String sql = SqlJoiner.join("SELECT nro.id, nro.status, nro.buy_num buyNum, count(urr.id) redbagCount FROM ( ",
				"SELECT nro.* FROM netbar_resource_order nro join netbar_resource_commodity nrc on nro.commodity_id = nrc.id",
				" join netbar_commodity_category ncc on nrc.category_id = ncc.id WHERE nro.netbar_id = ",
				String.valueOf(netbarId),
				" AND nro.expire_date > now() and nro.is_valid =1 AND nro. STATUS IN (0, 1, 2, 3) AND ncc.name = '红包'",
				" ) nro LEFT JOIN ( SELECT * FROM user_r_redbag urr WHERE urr.trade_no IS NOT NULL",
				" ) urr ON nro.trade_no = urr.trade_no  AND nro.status = 3",
				" GROUP BY nro.id HAVING redbagCount < buyNum");
		List<Map<String, Object>> availableOrders = queryDao.queryMap(sql);
		return CollectionUtils.isNotEmpty(availableOrders);
	}

	/**
	 * 过期增值券退款
	 */
	public void refundExpiredValueAdded() {
		// 查询未用且过期的增值券
		String expiredAddedSql = SqlJoiner.join("SELECT id, trade_no tradeNo, create_date createDate",
				" FROM user_value_added_card WHERE date(create_date) = date( ADDDATE(now(), INTERVAL - 1 DAY) )",
				" AND is_valid = 1 AND usable = 1");
		List<Map<String, Object>> expiredAddeds = queryDao.queryMap(expiredAddedSql);

		// 为增值券所在的订单回滚余量
		if (CollectionUtils.isNotEmpty(expiredAddeds)) {
			for (Map<String, Object> added : expiredAddeds) {
				String tradeNo = MapUtils.getString(added, "tradeNo");
				if (StringUtils.isNotBlank(tradeNo)) {
					String key = CommonConstant.MERCHANT_REDBAG_LEFT_NUM + tradeNo;
					RedisAtomicInteger count = new RedisAtomicInteger(key, redisConnectionFactory);
					count.incrementAndGet();
					LOGGER.error("为订单:" + tradeNo + "回滚一个增值券,对应增值券ID:" + MapUtils.getLong(added, "id"));
				}
			}
		}

		// 查询支付过期的增值券订单
		String unpayAddedSql = SqlJoiner.join("SELECT id, trade_no tradeNo, create_date createDate",
				" FROM user_value_added_card WHERE date(create_date) = date( ADDDATE(now(), INTERVAL - 1 DAY) )",
				" AND is_valid = -1");
		List<Map<String, Object>> unpayAdded = queryDao.queryMap(unpayAddedSql);

		// 未增值券所在的订单回滚余量
		if (CollectionUtils.isNotEmpty(unpayAdded)) {
			for (Map<String, Object> added : unpayAdded) {
				String tradeNo = MapUtils.getString(added, "tradeNo");
				if (StringUtils.isNotBlank(tradeNo)) {
					String key = CommonConstant.MERCHANT_REDBAG_LEFT_NUM + tradeNo;
					RedisAtomicInteger count = new RedisAtomicInteger(key, redisConnectionFactory);
					count.incrementAndGet();
					LOGGER.error("为订单:" + tradeNo + "回滚一个增值券,对应增值券ID:" + MapUtils.getLong(added, "id"));
				}
			}
		}
	}
}
