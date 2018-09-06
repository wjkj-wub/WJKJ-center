package com.miqtech.master.service.netbar;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.transaction.Transactional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.NetbarConstant;
import com.miqtech.master.consts.OrderConstants;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.NetbarOrderDao;
import com.miqtech.master.entity.netbar.NetbarFundDetail;
import com.miqtech.master.entity.netbar.NetbarFundInfo;
import com.miqtech.master.entity.netbar.NetbarInfo;
import com.miqtech.master.entity.netbar.NetbarMerchant;
import com.miqtech.master.entity.netbar.NetbarOrder;
import com.miqtech.master.entity.netbar.NetbarStaff;
import com.miqtech.master.entity.netbar.NetbarStaffBatch;
import com.miqtech.master.entity.netbar.NetbarStaffBatchOrder;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.service.user.UserRedbagService;
import com.miqtech.master.thirdparty.util.SMSMessageUtil;
import com.miqtech.master.utils.ArithUtil;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class NetbarOrderService {

	private final static String CREATE_ORDER_LIMIT_PREFIX = "wy_api_order_create_limit";
	private final static String PAY_REQ_INOPERATING = "wy_merchant_pay_req_inoperating_";

	private final static Joiner JOINER = Joiner.on("_");
	private final static Logger LOGGER = LoggerFactory.getLogger(NetbarOrderService.class);
	private final static Joiner joiner = Joiner.on("_");

	@Autowired
	private StringRedisOperateService redisOperateService;
	@Autowired
	private NetbarOrderDao netbarOrderDao;
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private UserRedbagService userRedbagService;
	@Autowired
	private NetbarStaffBatchService netbarStaffBatchService;
	@Autowired
	private NetbarStaffBatchOrderService netbarStaffBatchOrderService;
	@Autowired
	private NetbarFundInfoService netbarFundInfoService;
	@Autowired
	private NetbarFundDetailService netbarFundDetailService;

	public NetbarOrder findByID(Long id) {
		return netbarOrderDao.findOne(id);
	}

	public NetbarOrder findByReserveId(long reserverId) {
		return netbarOrderDao.findByReserveId(reserverId);
	}

	public NetbarOrder saveOrUpdate(NetbarOrder netbarOrder) {
		return netbarOrderDao.save(netbarOrder);
	}

	public void updateStatusByNetbarId(NetbarMerchant currentMerchant, int status) {
		Date date = new Date();
		String sql = "update netbar_r_order set status=" + status + ", operate_staff_id=0 ,update_date='"
				+ DateUtils.dateToString(date, "yyyy-MM-dd HH:mm:ss") + "' where netbar_id="
				+ currentMerchant.getNetbarId() + " and (status=1 or status=3)  ";
		queryDao.update(sql);
	}

	public void updateStatusByNetbarId(NetbarStaff currentStaff, int status) {
		Date date = new Date();
		String sql = "update netbar_r_order set status=" + status + ", operate_staff_id=" + currentStaff.getId()
				+ " ,update_date='" + DateUtils.dateToString(date, "yyyy-MM-dd HH:mm:ss") + "' where netbar_id="
				+ currentStaff.getNetbarId() + " and (status=1 or status=3) ";
		queryDao.update(sql);
	}

	@Transactional
	public void packStaffBatchOrders(Long netbarId, Long staffId) {
		if (netbarId == null) {
			return;
		}

		// 查询符合条件的订单
		List<NetbarOrder> orders = netbarOrderDao.findReadypayOrdersByNetbarId(netbarId);
		if (CollectionUtils.isNotEmpty(orders)) {
			// 加入频次控制,防止雇员和业主一起点击
			String key = PAY_REQ_INOPERATING + netbarId;
			if (CommonConstant.INT_BOOLEAN_TRUE.toString().equals(redisOperateService.getData(key))) {
				return;
			}
			redisOperateService.setData(key, CommonConstant.INT_BOOLEAN_TRUE.toString(), 15, TimeUnit.MINUTES);

			Double totalAmount = 0.0;
			Double amount = 0.0;
			Double rebateAmount = 0.0;
			Double redbagAmount = 0.0;
			Double scoreAmount = 0.0;
			Double valueAddedAmount = 0.0;
			int orderNum = 0;
			Date earliestDate = null;
			Date latestDate = null;
			List<NetbarStaffBatchOrder> batchOrders = new ArrayList<NetbarStaffBatchOrder>();
			for (NetbarOrder o : orders) {
				// 更新订单状态
				o.setStatus(OrderConstants.ORDER_STATUS_CLEAR);
				o.setOperateStaffId(staffId);
				o.setUpdateDate(new Date());
				// 记录最早最晚时间
				Date orderCreateDate = o.getCreateDate();
				if (earliestDate == null || orderCreateDate != null && earliestDate.after(orderCreateDate)) {
					earliestDate = orderCreateDate;
				}
				if (latestDate == null || orderCreateDate != null && latestDate.before(orderCreateDate)) {
					latestDate = orderCreateDate;
				}

				// 统计批次信息
				Double ta = o.getTotalAmount();
				if (ta != null) {
					totalAmount = ArithUtil.add(totalAmount, ta);
				}
				Double a = o.getAmount();
				if (a != null) {
					amount = ArithUtil.add(amount, a);
				}
				Double rba = o.getRebateAmount();
				if (rba != null) {
					rebateAmount = ArithUtil.add(rebateAmount, rba);
				}
				Double rda = o.getRedbagAmount();
				if (rda != null) {
					redbagAmount = ArithUtil.add(redbagAmount, rda);
				}
				Double sa = o.getScoreAmount();
				if (sa != null) {
					scoreAmount = ArithUtil.add(scoreAmount, sa);
				}
				Integer vaa = o.getValueAddedAmount();
				if (vaa != null) {
					valueAddedAmount = ArithUtil.add(valueAddedAmount, vaa);
				}
				orderNum += 1;

				// 记录批次订单
				NetbarStaffBatchOrder bo = new NetbarStaffBatchOrder();
				bo.setOrderId(o.getId());
				batchOrders.add(bo);
			}

			// 保存批次
			NetbarStaffBatch batch = new NetbarStaffBatch();
			batch.setNetbarId(netbarId);
			batch.setTotalAmount(totalAmount);
			batch.setAmount(amount);
			batch.setRebateAmount(rebateAmount);
			batch.setRedbagAmount(redbagAmount);
			batch.setScoreAmount(scoreAmount);
			batch.setValueAddedAmount(valueAddedAmount);
			batch.setStatus(OrderConstants.STAFF_BATCH_PAYED);
			batch.setOrderNum(orderNum);
			batch.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			Date now = new Date();
			batch.setUpdateUserId(staffId);
			batch.setCreateUserId(staffId);
			batch.setUpdateDate(now);
			batch.setCreateDate(now);
			batch.setEarliestDate(earliestDate);
			batch.setLatestDate(latestDate);
			batch = netbarStaffBatchService.save(batch);

			// 保存批次订单
			for (NetbarStaffBatchOrder bo : batchOrders) {
				bo.setBatchId(batch.getId());
			}
			netbarStaffBatchOrderService.save(batchOrders);

			// 更新订单
			netbarOrderDao.save(orders);
			redisOperateService.setData(key, CommonConstant.INT_BOOLEAN_FALSE.toString());
			//添加交易明细,更新用户金额信息
			NetbarFundInfo currFundInfo = netbarFundInfoService.findByNetbarId(netbarId);
			if (currFundInfo == null) {
				currFundInfo = new NetbarFundInfo(netbarId);
				currFundInfo.setAccounts(totalAmount);
				currFundInfo.setUsableQuota(0.00);
				currFundInfo.setValid(1);
				currFundInfo.setCreateDate(now);
				currFundInfo.setNetbarId(netbarId);
				currFundInfo.setQuota(0.00);
				currFundInfo.setQuotaRatio(0.00);
				currFundInfo.setUsableQuota(0.00);
				currFundInfo.setSettlAccounts(0.00);
				currFundInfo.setUsablePay(0.00);
				currFundInfo.setUsableQuota(0.00);
			} else {
				currFundInfo.setAccounts(currFundInfo.getAccounts() + totalAmount);
			}
			netbarFundInfoService.save(currFundInfo);
			if (totalAmount > 0) {
				NetbarFundDetail fd = new NetbarFundDetail();
				fd.setAmount(totalAmount);
				fd.setCreateDate(now);
				fd.setDirection(1);
				fd.setNetbarId(netbarId);
				fd.setQuotaRatio(0.00);
				fd.setResidual(currFundInfo.getAccounts());
				fd.setSettlAccounts(totalAmount);
				fd.setStatus(2);
				fd.setType(4);
				fd.setValid(1);
				netbarFundDetailService.save(fd);
			}
		}

	}

	/**
	 * 网吧业主:统计网吧结款订单：日期、<!--红包付款、积分付款、第三方支付、-->总金额
	 */
	public List<Map<String, Object>> statisticSettlementOrders(long netbarId, String beginDate, String endDate) {
		String statsticSQL = null;
		if (StringUtils.isNotBlank(beginDate) && StringUtils.isNotBlank(endDate)) {
			statsticSQL = "select * from (select DATE_FORMAT(create_date,'%Y-%m-%d') date, sum(order_num) count, sum(total_amount) total_amount from netbar_t_owner_batch where is_valid = 1 and status = 2 and netbar_id = "
					+ netbarId + " and create_date >= '" + beginDate + "' and create_date <= '" + endDate
					+ "' group by date) a ORDER by date ASC";
		} else {
			statsticSQL = "select * from (select DATE_FORMAT(create_date,'%Y-%m-%d') date, sum(order_num) count, sum(total_amount) total_amount from netbar_t_owner_batch where is_valid = 1 and status = 2 and netbar_id = "
					+ netbarId + " group by date order by date desc limit 0, 7) a ORDER by date ASC";
		}
		return queryDao.queryMap(statsticSQL);
	}

	/**
	 * 网吧雇员:统计网吧结款订单：日期、<!--红包付款、积分付款、第三方支付、-->总金额
	 */
	public List<Map<String, Object>> statisticStaffSettlementOrders(long staffId, long netbarId, String beginDate,
			String endDate) {
		String statsticSQL = null;
		if (StringUtils.isNotBlank(beginDate) && StringUtils.isNotBlank(endDate)) {
			statsticSQL = "select * from (select DATE_FORMAT(create_date,'%Y-%m-%d') date, sum(order_num) count, sum(total_amount) total_amount from netbar_t_owner_batch where is_valid = 1 and status = 2 and create_user_id ="
					+ staffId + " and netbar_id = " + netbarId + " and create_date >= '" + beginDate
					+ "' and create_date <= '" + endDate + "' group by date) a ORDER by date ASC";
		} else {
			statsticSQL = "select * from (select DATE_FORMAT(create_date,'%Y-%m-%d') date, sum(order_num) count, sum(total_amount) total_amount from netbar_t_owner_batch where is_valid = 1 and  status = 2 and create_user_id ="
					+ staffId + " and netbar_id = " + netbarId
					+ " group by date order by date desc limit 0, 7) a ORDER by date ASC";
		}
		return queryDao.queryMap(statsticSQL);
	}

	/**
	 * 统计网吧结款订单：日期、总金额
	 */
	public List<Map<String, Object>> statisticTotalAmount(long netbarId, String beginDate, String endDate) {
		String statsticSQL = "select  sum(total_amount) total_amount from netbar_r_order where status >=1 and netbar_id = "
				+ netbarId + " and create_date >= '" + beginDate + "' and create_date <= '" + endDate + "' ";
		return queryDao.queryMap(statsticSQL);
	}

	/**
	 * 统计网吧可申请核销金额：日期、总金额
	 */
	public List<Map<String, Object>> toPayOrderAmount(long netbarId, String beginDate, String endDate) {
		String statsticSQL = "select sum(total_amount) total_amount from netbar_t_staff_batch where status = 1 and  netbar_id =  "
				+ netbarId;
		return queryDao.queryMap(statsticSQL);
	}

	/**
	 * 统计网吧可申请结款订单：日期、总金额
	 */
	public List<Map<String, Object>> todayOrderAmount(long netbarId, String beginDate, String endDate) {
		String dateSql = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(beginDate) && StringUtils.isNotBlank(endDate)) {
			dateSql = " and create_date >= '" + beginDate + "' and create_date <= '" + endDate + "'";
		}
		String statsticSQL = "select  sum(total_amount) total_amount from netbar_r_order where status >=1 and netbar_id = "
				+ netbarId + dateSql;
		return queryDao.queryMap(statsticSQL);
	}

	/**
	 * 统计网吧某个员工结款订单：日期、总金额
	 */
	public List<Map<String, Object>> statisticStaffTotalAmount(long netbarId, long staffId) {
		String statsticSQL = "select  sum(total_amount) total_amount from netbar_r_order where status >1 and netbar_id = "
				+ netbarId + "' and operate_staff_id=" + staffId;
		return queryDao.queryMap(statsticSQL);
	}

	/**
	 * 统计网吧结款统计(网娱大师支付给网吧)列表
	 */
	public List<Map<String, Object>> statisticsOrder(int type, String netbarName, String startDate, String endDate,
			int status, String areaCode, String rate) {
		String areaSql = "";
		if (StringUtils.isNotBlank(areaCode)) {
			if (areaCode.substring(2, 6).equals("0000")) {
				areaCode = areaCode.substring(0, 2) + "%";
			} else if (areaCode.substring(4, 6).equals("00")) {
				areaCode = areaCode.substring(0, 4) + "%";
			}
			areaSql = " and ni.area_code like '" + areaCode + "' ";
		}
		String formatPattern = "%Y-%m-%d";
		if (type == 1) {
			formatPattern = "%Y";
		} else if (type == 2) {
			formatPattern = "%Y-%m";
		}
		String statusSql = StringUtils.EMPTY;
		if (status == 1) {//待支付
			statusSql = "  no.status > 1 and no.status <= 3 ";
		} else if (status == 2) {//已经支付
			statusSql = "  no.status =4 ";
		}
		String dateSql = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
			try {
				Date start = DateUtils.stringToDateYyyyMMdd(startDate);
				Date end = DateUtils.stringToDateYyyyMMdd(endDate);
				startDate = DateUtils.dateToString(start, "yyyy-MM-dd 00:00:00");
				endDate = DateUtils.dateToString(end, "yyyy-MM-dd 23:59:59");
				dateSql = " and no.create_date<='" + endDate + "' and no.create_date>='" + startDate + "' ";
			} catch (ParseException e) {
				LOGGER.error("支付网吧列表查询异常,日期格式错误", e);
				e.printStackTrace();
			}

		}
		String nameSql = StringUtils.EMPTY;
		netbarName = StringUtils.trim(netbarName);
		if (StringUtils.isNotBlank(netbarName)) {
			nameSql = " and ni.name like '%" + netbarName + "%'";
		}
		String statsticSQL = null;
		statsticSQL = SqlJoiner.join("	select ni.id,ni.name, date_format(no.create_date,'", formatPattern,
				"') date,sum(no.total_amount) tamount,sum(no.amount) amount,sum(no.score_amount) samount,sum(no.redbag_amount) ramount,",
				" sum(IFNULL(no.amount, 0) + IFNULL(no.redbag_amount, 0) * ", rate, "+ IFNULL(no.score_amount, 0) * ",
				rate,
				") payAmount,nm.weixin_pay_account,nm.ali_pay_account ,nm.bank_card_id,nm.bank_name,nm.bank_username",
				" from netbar_r_order no left join netbar_t_info ni on no.netbar_id = ni.id left join netbar_t_merchant nm on no.netbar_id = nm.netbar_id and nm.is_valid=1  where ",
				statusSql, " and no.total_amount > 0 ", areaSql, dateSql, nameSql,
				"  group by date_format(no.create_date, '", formatPattern, "'), ni.id ",
				"  order by ni.id, no.create_date desc ");
		return queryDao.queryMap(statsticSQL);
	}

	public List<Map<String, Object>> findPageDataByMapParams(NetbarMerchant currentMerchant,
			Map<String, Object> params) {
		Object pageString = params.get("page");
		int page = pageString == null ? 1 : NumberUtils.toInt(pageString.toString());
		page = page <= 0 ? 1 : page;
		int start = (page - 1) * PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (params.containsKey("page")) {
			params.remove("page");
		}
		params.put("start", start);
		params.put("pageSize", PageUtils.ADMIN_DEFAULT_PAGE_SIZE);
		params.put("netbarId", currentMerchant.getNetbarId());
		String querySql = "";
		if (params.containsKey("nickname")) {
			querySql = querySql + " and  nr.user_nickname like :nickname ";
		}

		if (params.containsKey("batchId")) {
			querySql = querySql
					+ " and  nr.id in(select order_id from netbar_r_staff_batch_order where batch_id =:batchId) ";
		}

		if (params.containsKey("idcardNum")) {
			querySql = querySql + " and  nr.merchant_comment like :idcardNum ";
		}

		if (params.containsKey("pcSeatNum")) {
			querySql = querySql + " and  nr.pc_seat_num like :pcSeatNum ";
		}

		if (params.containsKey("telephone")) {
			querySql = querySql + " and u.telephone like :telephone ";

		}

		if (params.containsKey("beginTime")) {
			querySql = querySql + " and nr.create_date > :beginTime and nr.create_date < :endTime ";
		}

		querySql = querySql + " and nr.total_amount>0  ";
		if (params.containsKey("status")) {
			querySql = querySql + " and nr.status =:status  ";
			if (params.get("status").equals("4")) {
				querySql = querySql + "  and merchant_comment is not null  ";
			}
		}

		String sql = " select nr.id, nr.user_nickname username, u.telephone telephone,  "
				+ "case when ( nr.status > 0  and nr.merchant_comment is not null ) then 2  "
				+ " when (nr.status > 0 and nr.merchant_comment is  null   ) then 1   " + " else 0   end status ,"
				+ " nr.create_date createDate,nr.order_type orderType,nr.pc_seat_num pcSeatNum, nr.pc_seat_card_num pcSeatCardNum ,nr.merchant_comment merchantComment,nr.out_trade_no outTradeNo, nr.total_amount totalAmount, nr.redbag_amount redbadPay, nr.score_amount scorePay, nr.value_added_amount valueAddedPay, nr.amount thirdPay, nr.type thirdPayType , nr.user_use_status userUseStatus  "
				+ " from netbar_r_order nr left join user_t_info u on nr.user_id = u.id  where nr.netbar_id = :netbarId    "
				+ querySql + " and nr.total_amount > 0  order by nr.id desc limit :start, :pageSize ";
		return queryDao.queryMap(sql, params);
	}

	public List<Map<String, Object>> findExportDataByMapParams(NetbarMerchant currentMerchant,
			Map<String, Object> params) {
		params.put("netbarId", currentMerchant.getNetbarId());
		String querySql = "";
		if (params.containsKey("nickname")) {
			querySql = querySql + " and  nr.user_nickname like :nickname ";
		}

		if (params.containsKey("batchId")) {
			querySql = querySql
					+ " and  nr.id in(select order_id from netbar_r_staff_batch_order where batch_id =:batchId) ";
		}

		if (params.containsKey("idcardNum")) {
			querySql = querySql + " and  nr.merchant_comment like :idcardNum ";
		}

		if (params.containsKey("pcSeatNum")) {
			querySql = querySql + " and  nr.pc_seat_num like :pcSeatNum ";
		}

		if (params.containsKey("telephone")) {
			querySql = querySql + " and u.telephone like :telephone ";

		}

		if (params.containsKey("beginTime")) {
			querySql = querySql + " and nr.create_date > :beginTime and nr.create_date < :endTime ";
		}

		querySql = querySql + " and nr.total_amount>0  ";
		if (params.containsKey("status")) {
			querySql = querySql + " and nr.status =:status and merchant_comment is not null  ";
		}

		String sql = " select nr.id, nr.user_nickname username, u.telephone telephone,  "
				+ "case when ( nr.status > 0  and nr.merchant_comment is not null ) then 2  "
				+ " when (nr.status > 0 and nr.merchant_comment is  null   ) then 1   " + " else 0   end status ,"
				+ " nr.create_date createDate,nr.order_type orderType,nr.pc_seat_num pcSeatNum, nr.pc_seat_card_num pcSeatCardNum ,nr.merchant_comment merchantComment,nr.out_trade_no outTradeNo, nr.total_amount totalAmount, nr.redbag_amount redbadPay, nr.score_amount scorePay, nr.value_added_amount valueAddedPay, nr.amount thirdPay, nr.type thirdPayType , nr.user_use_status userUseStatus  "
				+ " from netbar_r_order nr left join user_t_info u on nr.user_id = u.id  where nr.netbar_id = :netbarId    "
				+ querySql + " and nr.total_amount > 0  order by nr.id desc  ";
		return queryDao.queryMap(sql, params);
	}

	public Map<String, Object> findSumDataByMapParams(NetbarMerchant currentMerchant, Map<String, Object> params) {
		if (params.containsKey("start")) {
			params.remove("start");
		}
		if (params.containsKey("pageSize")) {
			params.remove("pageSize");
		}
		params.put("netbarId", currentMerchant.getNetbarId());
		String querySql = "";
		if (params.containsKey("nickname")) {
			querySql = querySql + " and nr.user_id in (select id from user_t_info where  user_nickname like :nickname ";
			if (params.containsKey("telephone")) {
				querySql = querySql + " or telephone like :telephone ) ";
			} else {
				querySql = querySql + " ) ";
			}
		} else {
			if (params.containsKey("telephone")) {
				querySql = querySql
						+ " and nr.user_id in (select id from user_t_info where telephone like :telephone ) ";
			}
		}
		if (params.containsKey("beginTime")) {
			querySql = querySql + " and nr.create_date > :beginTime and nr.create_date < :endTime ";
		}
		if (params.containsKey("idcardNum")) {
			querySql = querySql + " and  nr.merchant_comment like :idcardNum ";
		}

		if (params.containsKey("pcSeatNum")) {
			querySql = querySql + " and  nr.pc_seat_num like :pcSeatNum ";
		}

		if (params.containsKey("batchId")) {
			querySql = querySql
					+ " and  nr.id in(select order_id from netbar_r_staff_batch_order where batch_id =:batchId) ";
		}
		querySql = querySql + " and nr.total_amount>0  ";
		if (params.containsKey("status")) {
			querySql = querySql + " and nr.status =:status and merchant_comment is not null  ";
		}
		String sql = " select count(1) totalCount, sum(nr.total_amount) sumTotal, sum(nr.redbag_amount) sumRedbad, sum(nr.score_amount) sumScore,sum(nr.value_added_amount) sumValueAddedAmount, sum(nr.amount) sumThird "
				+ " from netbar_r_order nr where nr.netbar_id = :netbarId " + querySql + " and nr.total_amount > 0";
		return queryDao.querySingleMap(sql, params);
	}

	public PageVO findNetbarOrderList(Long userId, int page, int pageSize) {
		int start = (page - 1) * pageSize;
		Map<String, Object> params = new HashMap<String, Object>();
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		params.put("userId", userId);
		String sql = "";
		Map<String, Object> total = null;
		sql = "select count(*) total from (select a.id from netbar_r_order a,netbar_t_info b where a.user_id=:userId and a.is_valid!=0 and a.netbar_id=b.id and a.total_amount > 0  and (("
				+ " a.status <  1 and now() >= a.create_date   and now() <= date_add(a.create_date, interval 1 day) ) or a.status >= 1 ))c";
		total = queryDao.querySingleMap(sql, params);
		sql = SqlJoiner.join(
				"select a.id order_id,a.reserve_id,a.out_trade_no,a.order_type orderType,a.trade_no,a.nonce_str,a.type,amount,",
				" a.is_valid,a.create_date,if(a.status>=1,if(eva.id>0,2,1),0) status,a.netbar_type,a.netbar_amount,a.netbar_id,a.rebate_amount,",
				" a.prepay_id,a.user_id,a.redbag_amount,a.score_amount,a.total_amount,b.icon,b.name netbar_name ",
				" from netbar_r_order a left join netbar_t_info b on a.netbar_id = b.id left join netbar_t_evaluation eva on eva.order_id = a.id ",
				" where a.user_id=:userId and a.is_valid!=0 and a.netbar_id=b.id and a.total_amount > 0 ",
				" and (( a.status < 1 and now() >= a.create_date and now() <= date_add(a.create_date, interval 1 day)) or a.status >= 1) ",
				" group by a.id order by create_date desc  limit :start,:pageSize");
		params.put("start", start);
		params.put("pageSize", pageSize);
		result = queryDao.queryMap(sql, params);
		PageVO vo = new PageVO();
		vo.setList(result);
		BigInteger bi = (BigInteger) total.get("total");
		if (page * pageSize >= bi.intValue()) {
			vo.setIsLast(1);
		}
		return vo;
	}

	public boolean delOrder(Long orderId, Long userId, String deviceId) {
		NetbarOrder order = netbarOrderDao.findOne(orderId);
		if (order != null && order.getValid() != 0 && userId.equals(order.getUserId())) {
			if (order.getValueAddedId() == null) {
				userRedbagService.recoveryRedbag(order);//恢复红包可用
			} else {
				queryDao.update(
						"update user_value_added_card set is_valid=1,usable=1 where id=" + order.getValueAddedId());
			}
			recoveryCardLimit(joiner.join(UserRedbagService.USE_REDBAG_DAY_LIMIT_PREFIX,
					DateUtils.dateToString(new Date(), DateUtils.YYYY_MM_DD), userId.toString()));
			recoveryCardLimit(joiner.join(UserRedbagService.DEVICE_USE_REDBAG_DAY_LIMIT_PREFIX,
					DateUtils.dateToString(new Date(), DateUtils.YYYY_MM_DD), deviceId.toString()));
			order.setValid(0);
			netbarOrderDao.save(order);
			return true;
		}
		return false;
	}

	/**
	 * 恢复增值券使用限制
	 */
	public void recoveryCardLimit(String key) {
		RedisAtomicInteger usedCount = new RedisAtomicInteger(key,
				redisOperateService.getRedisTemplate().getConnectionFactory());
		usedCount.decrementAndGet();
	}

	public NetbarOrder findByOutTradeNo(String outTradeNo) {
		return netbarOrderDao.findByOutTradeNo(outTradeNo);
	}

	public boolean limitCreateOrder(Long userId) {
		try {
			String key = JOINER.join(CREATE_ORDER_LIMIT_PREFIX, userId);
			String data = redisOperateService.getData(key);
			if (StringUtils.isNotBlank(data)) {
				return true;
			}
		} catch (Exception e) {
			LOGGER.error("获取用户[{}]频次控制标识异常:", userId, e);
		}
		return false;
	}

	public void setLimitOrderFlag(Long userId) {
		try {
			String key = JOINER.join(CREATE_ORDER_LIMIT_PREFIX, userId);
			redisOperateService.setData(key, userId.toString(), 10, TimeUnit.SECONDS);
		} catch (Exception e) {
			LOGGER.error("设置用户[{}]频次控制标识异常:", userId, e);
		}
	}

	/**
	 * 设置某个网吧的某天订单为已支付状态
	 */
	public void setOrderStatusToPayed(String netbarId, String date) {
		Long netbarIdLong = NumberUtils.toLong(netbarId);
		String dateFormat = StringUtils.EMPTY;
		if (StringUtils.length(date) == 4) {
			dateFormat = "%Y";
		} else if (StringUtils.length(date) == 7) {
			dateFormat = "%Y-%m";
		} else if (StringUtils.length(date) == 10) {
			dateFormat = "%Y-%m-%d";
		} else {
			return;
		}
		String sql = "update netbar_r_order set status = 4 where status=2 and netbar_id=" + netbarIdLong
				+ " and  DATE_FORMAT(create_date,'" + dateFormat + "') ='" + date + "'";
		queryDao.update(sql);
	}

	/**
	 * 设置某网吧的某条订单为已经支付
	 */
	public void setOrderStatusToPayedByNetbarIdAndOrderId(String netbarId, String orderId) {
		String sql = "update netbar_r_order set status = 4 where status=3 and netbar_id=" + NumberUtils.toLong(netbarId)
				+ " and  id=" + NumberUtils.toLong(orderId);
		queryDao.update(sql);
	}

	/**
	 * 支付订单详情
	 */
	public Map<String, Object> orderDetail(Long orderId, Long userId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderId", orderId);
		params.put("userId", userId);
		String sql = SqlJoiner.join(
				" select a.value_added_amount,a.id order_id,a.prepay_id,a.out_trade_no,a.order_type orderType,a.netbar_id,a.trade_no,a.nonce_str,a.type, if(a.status>=1,if(eva.id>0,2,1),0) status, a.create_date, b.icon, ifnull(b.cs_phone,'4006902530') cs_phone, b.name netbar_name,a.total_amount-ifnull(a.value_added_amount,0) total_amount,",
				" case  when b.rebate > 0 and b.rebate <= 90 and now() >= b.rebate_start_date and now() <= b.rebate_end_date then b.rebate else 100 end rebate, ",
				" a.rebate_amount rebate_amount, a.redbag_amount, a.amount, count(f.id) netbar_fav_status ,a.already_lottery already_lottery",
				" from netbar_r_order a  left join netbar_t_info b on a.netbar_id = b.id",
				" left join user_r_favor f on f.user_id = a.user_id and a.netbar_id = f.sub_id and f.type = 1 and f.is_valid = 1 "
						+ "left join netbar_t_evaluation eva on eva.order_id = a.id ",
				" where a.id = :orderId and a.user_id=:userId");
		return queryDao.querySingleMap(sql, params);
	}

	/**
	 * 查询有异议订单
	 */
	public List<Map<String, Object>> disagreeOrders(String netbarName, String startDate, String endDate, String rate) {
		String dateSql = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
			try {
				Date start = DateUtils.stringToDateYyyyMMdd(startDate);
				Date end = DateUtils.stringToDateYyyyMMdd(endDate);
				startDate = DateUtils.dateToString(start, "yyyy-MM-dd 00:00:00");
				endDate = DateUtils.dateToString(end, "yyyy-MM-dd 23:59:59");
				dateSql = " and no.create_date<='" + endDate + "' and no.create_date>='" + startDate + "' ";
			} catch (ParseException e) {
				LOGGER.error("支付网吧列表查询异常,日期格式错误", e);
				e.printStackTrace();
			}
		}
		String nameSql = StringUtils.EMPTY;
		netbarName = StringUtils.trim(netbarName);
		if (StringUtils.isNotBlank(netbarName)) {
			nameSql = " and ni.name like '%" + netbarName + "%'";
		}
		String statsticSQL = null;
		statsticSQL = SqlJoiner.join(
				"	select no.id orderId,ni.id netbarId,ni.name, no.create_date date,no.total_amount tamount,no.amount amount,no.score_amount samount,no.redbag_amount ramount,",
				" sum(IFNULL(no.amount, 0) + IFNULL(no.redbag_amount, 0) * ", rate, "+ IFNULL(no.score_amount, 0) * ",
				rate, ") payAmount,nm.weixin_pay_account,nm.ali_pay_account ,nm.bank_card_id",
				" from netbar_r_order no left join netbar_t_info ni on no.netbar_id = ni.id left join netbar_t_merchant nm on no.netbar_id = nm.netbar_id and nm.is_valid=1    ",
				"where  no.status =3  and no.total_amount > 0 ", dateSql, nameSql,
				"  order by ni.id, no.create_date desc ");

		return queryDao.queryMap(statsticSQL);
	}

	/**
	 * 用户支付次数统计
	 */
	public PageVO userPayNum(String mobile, int page) {
		String mobileSql = "";
		String sql = "";
		PageVO vo = new PageVO();
		if (StringUtils.isNotBlank(mobile)) {
			mobileSql = " and username=" + mobile;
		}
		sql = SqlJoiner.join(
				"select count(1) total from (select user_id,username,count(1) num from netbar_r_order a left join user_t_info b on a.user_id=b.id where status>=1 and user_id is not null ",
				mobileSql, " group by user_id,username)a");
		Number totalCount = queryDao.query(sql);
		if (totalCount != null) {
			vo.setTotal(totalCount.intValue());
		}
		int pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		int start = (page - 1) * pageSize;
		if (start + pageSize >= vo.getTotal()) {
			vo.setIsLast(1);
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", start);
		params.put("pageSize", pageSize);
		sql = SqlJoiner.join(
				"select user_id,username,count(1) num,IFNULL(sum(total_amount),0) total_amount,IFNULL(sum(amount),0) amount,IFNULL(sum(redbag_amount),0) redbag_amount,IFNULL(sum(score_amount),0) score_amount",
				" from netbar_r_order a left join user_t_info b on a.user_id=b.id where status>=1 and user_id is not null ",
				mobileSql, " group by user_id,username order by num desc limit :start,:pageSize");
		vo.setCurrentPage(page);
		vo.setList(queryDao.queryMap(sql, params));
		return vo;
	}

	/**
	 * 查询支付成功且有效的订单
	 */
	public NetbarOrder findSuccessOrderById(Long id) {
		return netbarOrderDao.findByIdAndStatusGreaterThanAndValidGreaterThan(id, 1, 1);
	}

	/**
	 * 查询用户当天有效的分享红包订单 （修改此方法后须同步修改下面的方法 getUserShareRedbagValidOrdersByDate）
	 */
	public List<Map<String, Object>> getUserTodaysShareRedbagValidOrder(long userId) {
		String sql = SqlJoiner.join("SELECT id, user_nickname FROM netbar_r_order",
				" WHERE DATE_FORMAT(create_date, '%y-%m-%d') = CURRENT_DATE ()", " AND user_id = ",
				String.valueOf(userId),
				" AND status >= 1 AND total_amount >=5 and reserve_id = 0 ORDER BY create_date ASC LIMIT 0, 1");
		return queryDao.queryMap(sql);
	}

	/**
	 * 查询用户某天有效的分享红包订单 （修改此方法后须同步修改上面的方法 getUserTodaysShareRedbagValidOrder）
	 */
	public List<Map<String, Object>> getUserShareRedbagValidOrdersByDate(long userId, String date) {
		String sql = SqlJoiner.join(
				"SELECT id, user_id, user_nickname, is_valid, status, create_date FROM netbar_r_order  WHERE DATE_FORMAT(create_date, '%Y-%m-%d') = '",
				date, "' AND user_id = ", String.valueOf(userId),
				" AND status >= 1 AND total_amount >=5 AND reserve_id = 0 ORDER BY create_date ASC LIMIT 0, 1");
		return queryDao.queryMap(sql);
	}

	/**前七天支付次数
	 * @param netbarId
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public List<Map<String, Object>> lastSevenDayPay(String netbarId, String startDate, String endDate) {
		String sql = SqlJoiner.join(
				"select date_format(b.create_date, '%Y-%m-%d') date,count(id) num from netbar_r_order b where netbar_id=",
				netbarId, " and b.create_date>='", startDate, "' and b.create_date<='", endDate,
				"'  and b.reserve_id=0 group by date_format(b.create_date, '%Y-%m-%d')");
		return queryDao.queryMap(sql);
	}

	public void setAllOrderToPaid(String netbarName, String startDate, String endDate, int type, String areaCode) {
		String areaSql = "";
		if (StringUtils.isNotBlank(areaCode)) {
			if (areaCode.substring(2, 6).equals("0000")) {
				areaCode = areaCode.substring(0, 2) + "%";
			} else if (areaCode.substring(4, 6).equals("00")) {
				areaCode = areaCode.substring(0, 4) + "%";
			}
			areaSql = " and ni.area_code like '" + areaCode + "' ";
		}
		String statusSql = "  no.status > 1 and no.status <= 3 ";
		String dateSql = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
			try {
				Date start = DateUtils.stringToDateYyyyMMdd(startDate);
				Date end = DateUtils.stringToDateYyyyMMdd(endDate);
				startDate = DateUtils.dateToString(start, "yyyy-MM-dd 00:00:00");
				endDate = DateUtils.dateToString(end, "yyyy-MM-dd 23:59:59");
				dateSql = " and no.create_date<='" + endDate + "' and no.create_date>='" + startDate + "' ";
			} catch (ParseException e) {
				LOGGER.error("支付网吧列表查询异常,日期格式错误", e);
				e.printStackTrace();
			}

		}
		String nameSql = StringUtils.EMPTY;
		netbarName = StringUtils.trim(netbarName);
		if (StringUtils.isNotBlank(netbarName)) {
			nameSql = " and ni.name like '%" + netbarName + "%'";
		}
		String statsticSQL = null;
		statsticSQL = SqlJoiner.join(
				"	select no.id from netbar_r_order no left join netbar_t_info ni on no.netbar_id = ni.id left join netbar_t_merchant nm on no.netbar_id = nm.netbar_id and nm.is_valid=1  where ",
				statusSql, " and no.total_amount > 0 ", areaSql, dateSql, nameSql);
		List<Map<String, Object>> ids = queryDao.queryMap(statsticSQL);

		String orderIds = StringUtils.EMPTY;

		if (CollectionUtils.isNotEmpty(ids)) {
			for (Map<String, Object> id : ids) {
				orderIds = orderIds + "," + id.get("id").toString();
			}
			orderIds = StringUtils.removeStart(orderIds, ",");
			String sql = "update netbar_r_order set status = 4 where status=2 and id in(" + orderIds + ")";
			queryDao.update(sql);
		}

	}

	public void setOrderStatusToPayed(String ids) {
		String sql = "update netbar_r_order set status = 4 where status=2 and id in (" + ids + ")";
		queryDao.update(sql);
	}

	/**
	 * 获取前一天完成支付的订单
	 */
	public List<Map<String, Object>> getYesterdayFinishOrders() {
		String sql = SqlJoiner.join(
				"SELECT o.netbar_id netbarId, o.user_id userId, n.name netbarName,n.area_code areaCode,",
				" u.nickname userName, u.username telephone, o.id orderId, o.status, o.type payType, o.out_trade_no outTradeNo,",
				" o.trade_no tradeNo, o.total_amount totalAmount, o.amount, o.rebate_amount rebateAmount, o.redbag_amount redbagAmount,",
				" o.score_amount scoreAmount, o.create_date orderDate, IF(o.order_type = 1, 3, IF(o.reserve_id = 0, 2, 1)) type",
				" FROM netbar_r_order o LEFT JOIN user_t_info u ON o.user_id = u.id LEFT JOIN netbar_t_info n ON o.netbar_id = n.id",
				" WHERE DATE(o.create_date) = :createDate 	and o.status>=1");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("createDate", DateUtils.dateToString(DateUtils.getYesterday(), DateUtils.YYYY_MM_DD));
		return queryDao.queryMap(sql, params);
	}

	/**
	 * 统计前一天申请支付的订单金额
	 */
	public List<Map<String, Object>> statisYesterdayOrders() {
		String sql = SqlJoiner.join(
				"SELECT sum(total_amount) totalAmount, sum(rebate_amount) rebateAmount, CONCAT(SUBSTR(n.area_code,1,2),'0000') areaCode, a.name areaName",
				" FROM netbar_r_order o LEFT JOIN user_t_info u ON o.user_id = u.id LEFT JOIN netbar_t_info n ON o.netbar_id = n.id LEFT JOIN sys_t_area a ON n.area_code = a.area_code",
				" WHERE DATE(o.create_date) = :createDate AND  status = ",
				OrderConstants.ORDER_STATUS_APPLYING.toString(), " GROUP BY SUBSTR(n.area_code,2,2)");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("createDate", DateUtils.dateToString(DateUtils.getYesterday(), DateUtils.YYYY_MM_DD));
		return queryDao.queryMap(sql, params);
	}

	/**
	 * 查找网吧的已申请订单按操作人员分组分页数据
	 */
	public List<Map<String, Object>> findToPayOrderData(NetbarMerchant currentMerchant, Map<String, Object> params) {
		Object pageString = params.get("page");
		int page = pageString == null ? 1 : NumberUtils.toInt(pageString.toString());
		int start = (page - 1) * PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (params.containsKey("page")) {
			params.remove("page");
		}
		params.put("start", start);
		params.put("pageSize", PageUtils.ADMIN_DEFAULT_PAGE_SIZE);
		params.put("netbarId", currentMerchant.getNetbarId());
		String querySql = StringUtils.EMPTY;
		if (params.containsKey("operateName")) {
			querySql = querySql + " and  s.name like :operateName ";
		}

		String sql = " select  s.name, count(1) totalCount, sum(total_amount) totalAmount,sum(amount) amount, sum(redbag_amount) redbagAmount, sum(score_amount) scoreAmount, o.update_date updateDate "
				+ " from netbar_r_order o left join netbar_t_staff s on o.operate_staff_id = s.id "
				+ " where o.status = 2 and o.netbar_id = :netbarId and total_amount > 0 " + querySql
				+ " group by o.operate_staff_id, o.update_date "
				+ " order by  o.update_date  desc limit :start, :pageSize ";

		return queryDao.queryMap(sql, params);
	}

	/**
	 * 查找网吧的已申请订单总的统计数据
	 */
	public Map<String, Object> findToPayOrderSumData(NetbarMerchant currentMerchant, Map<String, Object> params) {
		if (params.containsKey("start")) {
			params.remove("start");
		}
		if (params.containsKey("pageSize")) {
			params.remove("pageSize");
		}
		params.put("netbarId", currentMerchant.getNetbarId());
		String querySql = StringUtils.EMPTY;
		if (params.containsKey("operateName")) {
			querySql = querySql + " and  s.name like :operateName ";
		}
		String sql = " select     count(1) totalCount, sum(amount) amount, sum(totalAmount) totalAmount, sum(redbagAmount) redbagAmount, sum(scoreAmount) scoreAmount from "
				+ " (select   count(1) totalCount,sum(amount) amount, sum(total_amount) totalAmount, sum(redbag_amount) redbagAmount, sum(score_amount) scoreAmount "
				+ " from netbar_r_order o left join netbar_t_staff s on o.operate_staff_id = s.id "
				+ " where o.status = 2 and o.netbar_id = :netbarId and total_amount > 0 " + querySql
				+ " group by o.operate_staff_id, o.update_date) a ";
		return queryDao.querySingleMap(sql, params);
	}

	/**admin后台订单管理
	 * @param phone
	 * @param netbarName
	 * @param nickname
	 * @return
	 */
	public PageVO queryOrderForManage(String phone, String netbarName, String nickname, Integer status, Integer page) {
		String sql = "";
		String phoneSql = "";
		String netbarNameSql = "";
		String nicknameSql = "";
		String statusSql = "";
		PageVO vo = new PageVO();
		if (StringUtils.isNotBlank(phone)) {
			phoneSql = " and c.username like '%" + phone + "%'";
		}
		if (StringUtils.isNotBlank(netbarName)) {
			netbarNameSql = " and b.name like '%" + netbarName + "%'";
		}
		if (StringUtils.isNotBlank(nickname)) {
			nicknameSql = " and c.nickname like '%" + nickname + "%'";
		}
		if (status != null) {
			statusSql = " and a.status=" + String.valueOf(status);
		}
		sql = SqlJoiner.join(
				"select count(1) from netbar_r_order a,netbar_t_info b,user_t_info c where a.netbar_id=b.id and a.user_id=c.id ",
				phoneSql, netbarNameSql, nicknameSql, statusSql);
		Number totalCount = queryDao.query(sql);
		if (totalCount != null) {
			vo.setTotal(totalCount.intValue());
		}
		int pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page == null) {
			page = 1;
		}
		int start = (page - 1) * pageSize;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", start);
		params.put("pageSize", pageSize);
		sql = SqlJoiner.join(
				"select a.id,c.nickname user_nickname,a.type,a.total_amount,a.amount,a.rebate_amount,a.redbag_amount,a.score_amount,a.status,a.is_valid,a.order_type,a.create_date,b.name,c.username from netbar_r_order a,netbar_t_info b,user_t_info c where a.netbar_id=b.id and a.user_id=c.id ",
				phoneSql, netbarNameSql, nicknameSql, statusSql, " order by a.create_date desc",
				" limit :start,:pageSize");
		vo.setCurrentPage(page);
		vo.setList(queryDao.queryMap(sql, params));
		return vo;
	}

	/**查询用户已付款订单id
	 * @param netbarId
	 * @return
	 */
	public String queryPaidOrderIds(Long netbarId) {
		String sql = "SELECT nr.id FROM netbar_r_order nr WHERE nr.netbar_id = " + String.valueOf(netbarId)
				+ " AND nr.total_amount > 0 AND (nr. STATUS = 1 OR nr. STATUS = 3) AND total_amount > 0 ORDER BY nr.create_date DESC";
		List<Map<String, Object>> list = queryDao.queryMap(sql);
		StringBuilder sb = new StringBuilder();
		if (CollectionUtils.isNotEmpty(list)) {
			for (Map<String, Object> map : list) {
				sb.append(map.get("id") + ",");
			}
		}
		return sb.toString();
	}

	/**
	 * 统计各地区需要结算支付的金额
	 */
	public List<Map<String, Object>> statisticPay(String areaCode) {
		List<Map<String, Object>> list = Lists.newArrayList();
		String sqlSum = "select sum(o.total_amount - o.rebate_amount) sum, a.name,";
		//查询级别，全国-，省级-，市级-，区-
		Integer level = null;
		String sub = org.apache.commons.lang3.StringUtils.EMPTY;
		if (areaCode.equals("000000")) {
			level = 2;
			sub = "0000";
		} else if (areaCode.substring(2).equals("0000")) {
			level = 4;
			sub = "00";
		} else if (areaCode.substring(4).equals("00")) {
			level = 6;
		}
		sqlSum = SqlJoiner.join(sqlSum,
				" concat(left(n.area_code, :level), :sub) areaCode from netbar_r_order o left join netbar_t_info n ON o.netbar_id = n.id",
				" left join sys_t_area a on a.area_code = concat(left(n.area_code, :level), :sub)",
				" where  o.status>=2 and o.status<4 and n.area_code like ':areaCodePrefix%' group by left(n.area_code, :level)");
		sqlSum = sqlSum.replaceAll(":areaCodePrefix", com.miqtech.master.utils.StringUtils.reduceAreaCode(areaCode));
		Map<String, Object> params = Maps.newHashMap();
		params.put("level", level);
		params.put("sub", sub);
		list = queryDao.queryMap(sqlSum, params);
		return list;
	}

	/**
	 * 统计网吧的
	 */
	public Map<String, Object> statisticNetbarRevenue(Long netbarId) {
		if (netbarId == null) {
			return null;
		}

		String sql = SqlJoiner.join(
				"SELECT sum(total_amount) totalAmount FROM netbar_r_order WHERE is_valid > 0 AND status != ",
				NetbarConstant.NETBAR_RESOURCE_ORDER_STATUS_CANCEL.toString(), " AND netbar_id = ",
				netbarId.toString());
		return queryDao.querySingleMap(sql);
	}

	public Long firstOrderToday(String userId) {
		Number n = queryDao.query("select id from netbar_r_order where user_id=" + userId
				+ " and status>=1 and date_format(create_date, '%Y-%m-%d')=date_format(now(), '%Y-%m-%d')  and DATE_ADD(create_date,INTERVAL 1 day)>now() order by create_date limit 1");
		if (n == null) {
			return 0L;
		} else {
			return n.longValue();
		}
	}

	public boolean isTodayPayedOrder(Long orderId) {
		Map<String, Object> map = queryDao.querySingleMap("select id from netbar_r_order where id=" + orderId
				+ " and is_valid=2 and status>0 and date_format(create_date,'%Y-%m-%d')='"
				+ DateUtils.dateToString(new Date(), DateUtils.YYYY_MM_DD) + "'");
		if (map != null) {
			return true;
		}
		return false;
	}

	/**
	 * 同步一鍵支付訂單
	 * @param netbarId
	 * @param status
	 * @param pageSize
	 * @param pageNum
	 * @return
	 */
	public PageVO findOneKeyPayPage(Long netbarId, int status, int pageSize, int pageNum) {
		String statusSql = "";
		if (status != -1) {
			if (status == 0) {//未完成支付
				statusSql = " and status=" + String.valueOf(status);
			} else if (status == 1) {//完成支付 收银未确认
				statusSql = " and merchant_comment is null and status>0";
			} else if (status == 2) {//完成支付 收银已确认
				statusSql = " and merchant_comment is not null and status>0";
			}
		}
		String countSql = "select count(1) from netbar_r_order where netbar_id = " + netbarId
				+ " and is_valid >0 and order_type = 2 " + statusSql;
		Number totalCount = queryDao.query(countSql);
		PageVO vo = new PageVO();
		if (totalCount != null) {
			vo.setTotal(totalCount.intValue());
		}
		vo.setCurrentPage(pageNum);
		if (totalCount.intValue() > 0) {
			int start = (pageNum - 1) * pageSize;
			String sql = "select id orderId,netbar_id netbarId,out_trade_no wyTradeNo,amount,case when (status>0 and merchant_comment is not null) then 2 when (status>0 and merchant_comment is null) then 1 else 0 end status "
					+ " from netbar_r_order where netbar_id = " + netbarId + "  and is_valid > 0  and order_type = 2 "
					+ statusSql + " order by create_date desc " + " limit " + start + ", " + pageSize + " ";
			vo.setList(queryDao.queryMap(sql));
		}
		return vo;
	}

	public Number unSettleOrderCount(Long netbarId, Long staffId) {
		String sql = " select   count(1) from  netbar_r_order " + " where netbar_id = " + netbarId
				+ "   and is_valid > 0 and    status = 1   and merchant_comment is null  ";
		return queryDao.query(sql);
	}

	public Number staffToPayOrderAmount(Long netbarId, Long staffId) {
		String sql = "select   sum(total_amount) from  netbar_r_order " + " where netbar_id = " + netbarId
				+ "   and is_valid > 0   and status = 1   and merchant_comment is not null    and operate_staff_id = "
				+ staffId;
		return queryDao.query(sql);
	}

	public Number staffTotalAmount(Long netbarId, Long staffId) {
		String sql = "select   sum(total_amount) from  netbar_t_staff_batch " + " where netbar_id = " + netbarId
				+ "   and is_valid > 0   and status =3     and create_user_id = " + staffId;

		return queryDao.query(sql);
	}

	public List<Map<String, Object>> findUnSettlePage(NetbarMerchant currentMerchant) {
		Long netbarId = currentMerchant.getNetbarId();
		Map<String, Object> params = Maps.newHashMap();
		params.put("netbarId", netbarId);
		String querySql = "";
		querySql = querySql + " and nr.status = 1  and merchant_comment is null ";
		String sql = " select nr.id, nr.user_nickname username, u.telephone telephone,  "
				+ "case when ( nr.status > 0  and nr.merchant_comment is not null and nr.order_type =2) then 2  "
				+ " when (nr.status > 0 and nr.merchant_comment is  null and nr.order_type =2    ) then 1  "
				+ " when (  nr.status >0 and (nr.order_type is null or nr.order_type !=2) and (nr.status=1 or nr.status = 3)) then 2  "
				+ " else 0   end status ,"
				+ " nr.create_date createDate, nr.pc_seat_num pcSeatNum, nr.pc_seat_card_num pcSeatCardNum ,nr.order_type orderType,nr.merchant_comment merchantComment,nr.out_trade_no outTradeNo, nr.total_amount totalAmount, nr.redbag_amount redbadPay, nr.score_amount scorePay, nr.value_added_amount valueAddedPay, nr.amount thirdPay, nr.type thirdPayType , nr.user_use_status userUseStatus  "
				+ " from netbar_r_order nr left join user_t_info u on nr.user_id = u.id  where nr.netbar_id = :netbarId    "
				+ querySql + " and nr.total_amount > 0  order by nr.id desc ";
		return queryDao.queryMap(sql, params);
	}

	public final static String ONEKEY_PAY_MERCHANT_MONITOR_PREFIX = "merchant_monitor_okp_";//已支付网娱的一键支付集合
	public final static String MERCHANT_CURRENT_STAFF_TOKEN_PREFIX = "merchant_snitch_current_staff_info_";//当前网吧的班次人员信息
	public final static String MERCHANT_CURRENT_STAFF_SNITCH_STATUS_PREFIX = "merchant_cur_snitch_status_";//短信小报告状态 0 未发送, 1已发送

	public final static String ONEKEY_PAY_NERBARS = "onekey_pay_nerbars";//有一键支付类型订单的网吧

	/**
	 * 订单状态改动通知
	 * @param netbarId
	 * @param orderId
	 * @param status 1支付成功  2 确认成功
	 */
	public void orderStatusChange(Long netbarId, Long orderId, int status) {
		redisOperateService.addValuesToSet(ONEKEY_PAY_NERBARS, netbarId.toString());//记录到网吧列表
		String key = ONEKEY_PAY_MERCHANT_MONITOR_PREFIX + netbarId;
		if (1 == status) {//支付平台通知
			//将订单数据加入缓存set
			redisOperateService.addValuesToSet(key, orderId + "_" + System.currentTimeMillis());
		} else if (2 == status) {//收银人员确认
			long currentTime = System.currentTimeMillis();
			//获取所有网吧未确认订单
			//1.判断日期,对订单进行操作,删除缓存中数据
			Set<String> datas = redisOperateService.getSetValues(key);
			for (String orderTime : datas) {
				String[] orderTimeArr = orderTime.split("_");
				long btwTime = currentTime - NumberUtils.toLong(orderTimeArr[1]);
				if (btwTime <= 60000) {
					redisOperateService.removeSetValue(key, orderTime);
				}
			}
		}
	}

	/**
	 * 更新网吧的班次人员信息
	 * @param netbarId
	 * @param orderId
	 * @param status 1支付成功  2 确认成功
	 */
	public void upgradeStaffInfo(Long netbarId, String staffInfo) {
		redisOperateService.setData(MERCHANT_CURRENT_STAFF_TOKEN_PREFIX + netbarId, staffInfo);//获取当前班次
	}

	public Set<String> getAllOnekeyOrderNetbarIds() {
		return redisOperateService.getSetValues(ONEKEY_PAY_NERBARS);//获取网吧id
	}

	/**
	 * 清除班次订单确认信息
	 * @param netbarId
	 */
	public void clearMerchantMonitor(Long netbarId) {
		String key = ONEKEY_PAY_MERCHANT_MONITOR_PREFIX + netbarId;
		redisOperateService.delData(key);
		key = MERCHANT_CURRENT_STAFF_TOKEN_PREFIX + netbarId;
		redisOperateService.delData(key);
	}

	/**
	 * 清除班次订单确认信息
	 */
	public void resetOrderInfo(Long netbarId) {
		String key = ONEKEY_PAY_MERCHANT_MONITOR_PREFIX + netbarId;
		redisOperateService.delData(key);
	}

	@Autowired
	private NetbarStaffService netbarStaffService;
	@Autowired
	private NetbarMerchantService netbarMerchantService;
	@Autowired
	private NetbarInfoService netbarInfoService;

	public void sendSnitchMsg(Long netbarId, Long currentTime) {
		String staffInfo = redisOperateService.getData(MERCHANT_CURRENT_STAFF_TOKEN_PREFIX + netbarId);//获取当前班次
		String snitchStatus = redisOperateService.getData(MERCHANT_CURRENT_STAFF_SNITCH_STATUS_PREFIX + staffInfo);
		if (StringUtils.equals("1", snitchStatus)) {
			return;
		}
		//获取现在网吧已经超时的未确认订单数量
		String key = ONEKEY_PAY_MERCHANT_MONITOR_PREFIX + netbarId;
		Set<String> datas = redisOperateService.getSetValues(key);
		int overtimeCount = 0;
		for (String orderTime : datas) {
			String[] orderTimeArr = orderTime.split("_");
			long btwTime = currentTime - NumberUtils.toLong(orderTimeArr[1]);
			if (btwTime > 60000) {
				overtimeCount = overtimeCount + 1;
			}
		}
		if (overtimeCount >= 3) {
			String[] tokenId = staffInfo.split("_");
			Long staffId = NumberUtils.toLong(tokenId[1]);
			NetbarStaff staff = netbarStaffService.findById(staffId);
			NetbarMerchant merchant = netbarMerchantService.findByNetbarId(netbarId);
			NetbarInfo netbar = netbarInfoService.findById(netbarId);
			String[] phoneNum = { merchant.getOwnerTelephone() };
			String[] params = { netbar.getName(), staff.getName() };
			SMSMessageUtil.sendTemplateMessage(phoneNum, "3032153", params);
			redisOperateService.setData(MERCHANT_CURRENT_STAFF_SNITCH_STATUS_PREFIX + staffInfo, "1", 1, TimeUnit.DAYS);
			resetOrderInfo(netbarId);
		}

	}

}