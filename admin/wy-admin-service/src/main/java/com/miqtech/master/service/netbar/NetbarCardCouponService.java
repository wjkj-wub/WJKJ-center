package com.miqtech.master.service.netbar;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Maps;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.common.SystemRedbagDao;
import com.miqtech.master.dao.netbar.NetbarAndCouponDao;
import com.miqtech.master.dao.netbar.NetbarCardCouponCategoryDao;
import com.miqtech.master.dao.netbar.NetbarCardCouponDao;
import com.miqtech.master.dao.netbar.NetbarOrderDao;
import com.miqtech.master.dao.user.UserRedbagDao;
import com.miqtech.master.entity.Pager;
import com.miqtech.master.entity.common.SystemRedbag;
import com.miqtech.master.entity.netbar.NetbarAndCoupon;
import com.miqtech.master.entity.netbar.NetbarCardCoupon;
import com.miqtech.master.entity.netbar.NetbarCardCouponCategory;
import com.miqtech.master.entity.netbar.NetbarCardCouponSettle;
import com.miqtech.master.entity.netbar.NetbarMerchant;
import com.miqtech.master.entity.netbar.NetbarOrder;
import com.miqtech.master.entity.user.UserRedbag;
import com.miqtech.master.utils.ArithUtil;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class NetbarCardCouponService {
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private NetbarCardCouponDao netbarCardCouponDao;
	@Autowired
	private SystemRedbagDao systemRedbagDao;
	@Autowired
	private NetbarCardCouponCategoryDao netbarCardCouponCategoryDao;
	@Autowired
	private NetbarAndCouponDao netbarAndCouponDao;
	@Autowired
	private UserRedbagDao userRedbagDao;
	@Autowired
	private NetbarOrderDao netbarOrderDao;

	public List<NetbarCardCoupon> save(List<NetbarCardCoupon> cards) {
		return (List<NetbarCardCoupon>) netbarCardCouponDao.save(cards);
	}

	public List<NetbarCardCoupon> findByCouponSettleIds(List<Long> couponSettleIds) {
		if (CollectionUtils.isNotEmpty(couponSettleIds)) {
			return netbarCardCouponDao.findByCouponSettleIdIn(couponSettleIds);
		}
		return null;
	}

	public PageVO cardCoupon(String userId, Integer type, Pager pager, Double payAmount, Long netbarId) {
		if (type == null) {
			type = 1;
		}
		String sql = " a.is_valid=1 and now()<a.expire_date and a.user_id=" + userId;
		String addValueSql = " a.is_valid=1 and a.usable=1 and user_id=" + userId + " and now()<a.expire_date ";
		if (type == 2) {
			sql = " (a.is_valid=0 or now()>a.expire_date) and a.user_id=" + userId;
			addValueSql = " a.usable=1 and (a.is_valid=0 or now()>a.expire_date) and user_id=" + userId + " ";
		}
		PageVO vo = new PageVO();
		if (payAmount == null) {
			Number total = queryDao
					.query("select sum(total) from ( select count(1) total from user_recharge_prize a where " + sql
							+ " union all select count(1) total from user_value_added_card a where " + addValueSql
							+ " ) a");
			if (total != null) {
				if (pager.total >= total.intValue()) {
					vo.setIsLast(1);
				}
				vo.setTotal(total.intValue());
			}
			vo.setList(queryDao
					.queryMap("select a.id, b.prize_name name, c. name netbar_name, a.expire_date end_date, a.create_date start_date, if (a.is_valid = 1, 1, 2) status, null limit_min_money, 2 type, 1 enabled, null amount from user_recharge_prize a left join netbar_t_recharge_activity_prize b on a.recharge_prize_id = b.id left join netbar_t_info c on a.netbar_id = c.id where b.prize_name<>'谢谢惠顾' and "
							+ sql
							+ " union all select a.id, concat( a.amount, '元网费增值券' ) name, b. name netbar_name,a.expire_date end_date, a.create_date start_date, if (a.is_valid = 1, 1, 2) status, c.limit_min_money,1 type,1 enabled,a.amount  from user_value_added_card a left join netbar_t_info b on a.netbar_id = b.id left join sys_value_added_card c on a.value_add_card_id=c.id where "
							+ addValueSql + " order by start_date desc limit " + pager.start + "," + pager.pageSize));
		} else {
			vo.setList(queryDao
					.queryMap("select a.id, concat( a.amount, '元网费增值券' ) name, a.expire_date end_date, b. name netbar_name, a.create_date start_date, if (a.is_valid = 1, 1, 2) status,c.limit_min_money,1 type,if(c.limit_min_money<="
							+ payAmount
							+ " and a.netbar_id="
							+ netbarId
							+ ",1,0) enabled,a.amount  from user_value_added_card a left join netbar_t_info b on a.netbar_id = b.id left join sys_value_added_card c on a.value_add_card_id = c.id where "
							+ addValueSql + " order by start_date desc"));
			vo.setIsLast(1);
		}
		return vo;
	}

	public void useCardCoupon(String id) {
		String sql = "update user_recharge_prize set is_valid=0 where id=" + id;
		queryDao.update(sql);
	}

	public Map<String, Object> lottery(String netbarId, Long userId, Long orderId) {
		Map<String, Object> result = new HashMap<String, Object>();
		String sql = "";
		NetbarOrder netbarOrder = netbarOrderDao.findOne(orderId);
		if (netbarOrder.getAlreadyLottery() == null || netbarOrder.getAlreadyLottery() == 0) {
			sql = "select a.id,a.name,a.probability,a.type,a.redbag_id,a.amount,a.min_money from (netbar_card_coupon_category a,netbar_and_coupon b) left join sys_t_redbag c on a.redbag_id=c.id where a.is_valid=1 and a.state=1 and a.id=b.category_id and b.netbar_id="
					+ netbarId + " order by a.probability desc";
			List<Map<String, Object>> list = queryDao.queryMap(sql);
			if (CollectionUtils.isNotEmpty(list)) {
				Random r = new Random();
				for (Map<String, Object> map : list) {
					double probability = ((Number) map.get("probability")).doubleValue();
					String probabilityStr = ((Number) map.get("probability")).toString();
					int index = probabilityStr.indexOf(".");
					int pow = (int) Math.pow(10d, probabilityStr.length() - index + 1);
					int min = pow * (int) probability;
					int max = pow * 100;
					int random = r.nextInt(max) + 1;
					if (random <= min) {
						result.put("id", map.get("id"));
						result.put("name", map.get("name"));
						saveLottery(((Number) map.get("id")).longValue(), Long.valueOf(netbarId), userId, map);
						break;
					}
				}
			}
			if (result.isEmpty()) {
				result.put("name", "谢谢惠顾");
			}
			//更新状态以抽奖
			sql = "update netbar_r_order set already_lottery=1 where id=" + orderId;
			queryDao.update(sql);
		}
		return result;
	}

	public void saveLottery(Long categoryId, Long netbarId, Long userId, Map<String, Object> map) {
		if (((Number) map.get("type")).intValue() == 3) {
			UserRedbag userRedbag = new UserRedbag();
			userRedbag.setUserId(userId);
			userRedbag.setRedbagId(((Number) map.get("redbag_id")).longValue());
			userRedbag.setUsable(1);
			userRedbag.setAmount((int) ((Number) map.get("amount")).doubleValue());
			userRedbag.setNetbarType(2);
			userRedbag.setNetbarId(netbarId);
			userRedbag.setValid(1);
			userRedbag.setCreateDate(new Date());
			userRedbag.setLimitMinMoney(((Number) map.get("min_money")).intValue());
			userRedbagDao.save(userRedbag);
		}
		NetbarCardCoupon netbarCardCoupon = new NetbarCardCoupon();
		netbarCardCoupon.setCategoryId(categoryId);
		netbarCardCoupon.setNetbarId(netbarId);
		netbarCardCoupon.setUserId(userId);
		netbarCardCoupon.setValid(1);
		netbarCardCoupon.setStatus(1);
		netbarCardCoupon.setCreateDate(new Date());
		netbarCardCouponDao.save(netbarCardCoupon);
	}

	public PageVO queryList(String name, Integer type, Pager pager) {
		String nameSql = "";
		String typeSql = "";
		PageVO vo = new PageVO();
		if (StringUtils.isNotBlank(name)) {
			nameSql = " and a.name like '%" + name.trim() + "%'";
		}
		if (type != null) {
			typeSql = " and a.type=" + type;
		}
		String sql = "select count(1) from netbar_card_coupon_category a where a.is_valid=1" + nameSql + typeSql;
		Number total = queryDao.query(sql);
		if (total != null) {
			vo.setTotal(total.intValue());
		}
		sql = "select a.state,a.id,a.name,a.type,a.amount,a.probability,a.start_date,a.end_date,a.create_date,group_concat(c.name) as names from (netbar_card_coupon_category a,netbar_and_coupon b) left join netbar_t_info c on b.netbar_id=c.id where a.is_valid=1 and a.id=b.category_id "
				+ nameSql
				+ typeSql
				+ " group by a.id order by a.create_date desc"
				+ " limit "
				+ pager.start
				+ ","
				+ pager.pageSize;
		vo.setList(queryDao.queryMap(sql));
		return vo;
	}

	public void save(NetbarCardCouponCategory netbarCardCouponCategory, String netbars) {
		netbarCardCouponCategory.setCreateDate(new Date());
		netbarCardCouponCategory.setValid(1);
		netbarCardCouponCategory.setState(0);
		if (netbarCardCouponCategory.getType() == 3) {
			SystemRedbag systemRedbag = new SystemRedbag();
			systemRedbag.setMoney((int) netbarCardCouponCategory.getAmount().doubleValue());
			systemRedbag.setExplain("支付网费时使用,限定网吧");
			systemRedbag.setType(10);
			systemRedbag.setRestrict(0);
			systemRedbag.setValid(1);
			systemRedbag.setCreateDate(new Date());
			systemRedbag.setBeginTime(netbarCardCouponCategory.getStartDate());
			systemRedbag.setEndTime(netbarCardCouponCategory.getEndDate());
			systemRedbagDao.save(systemRedbag);
			netbarCardCouponCategory.setRedbagId(systemRedbag.getId());
		}
		netbarCardCouponCategoryDao.save(netbarCardCouponCategory);
		if (StringUtils.isNotBlank(netbars)) {
			String[] ids = netbars.split(",");
			List<NetbarAndCoupon> list = new ArrayList<NetbarAndCoupon>();
			for (String s : ids) {
				NetbarAndCoupon netbarAndCoupon = new NetbarAndCoupon();
				netbarAndCoupon.setCategoryId(netbarCardCouponCategory.getId());
				netbarAndCoupon.setNetbarId(Long.valueOf(s));
				netbarAndCoupon.setValid(1);
				netbarAndCoupon.setCreateDate(new Date());
				list.add(netbarAndCoupon);
			}
			netbarAndCouponDao.save(list);
		}
	}

	public int isNetbarExistCoupon(Long netbarId, String orderId) {
		String sql = "select count(1) from netbar_and_coupon a,netbar_card_coupon_category b where a.is_valid=1 and b.state=1 and a.category_id=b.id and b.start_date<now() and now()<b.end_date and a.netbar_id="
				+ netbarId + " and b.create_date<(select create_date from netbar_r_order where id=" + orderId + ")";
		Number n = queryDao.query(sql);
		if (n != null) {
			return n.intValue();
		}
		return 0;
	}

	public Map<String, Object> detail(String id) {
		String sql = "select a.id,a.type,a.name,a.amount,a.probability,a.min_money,a.start_date,a.end_date,GROUP_CONCAT(b.netbar_id) netbar_ids,GROUP_CONCAT(concat(substr(c.area_code,1,4),'00')) area_codes from netbar_card_coupon_category a,netbar_and_coupon b,netbar_t_info c where a.id=b.category_id and b.netbar_id=c.id and a.id="
				+ id;
		return queryDao.querySingleMap(sql);
	}

	public void switchover(String id) {
		String sql = "update netbar_card_coupon_category a set a.state=if(a.state=0 or a.state=2,1,2) where is_valid=1 and id="
				+ id;
		queryDao.update(sql);
	}

	public void del(String id) {
		String sql = "update netbar_card_coupon_category a set a.is_valid=0 where a.state=0 and a.id=" + id;
		queryDao.update(sql);
	}

	/**
	 * 商戶端查詢卡券領取分頁數據
	 * params 包含 頁數,起止時間,狀態
	 * @return 
	 */
	public PageVO page(Map<String, Object> params) {
		String conditonSql = "";
		if (params.containsKey("likeQuery")) {
			conditonSql += " and (uti.username like '%" + params.get("likeQuery") + "%' or uti.nickname like '%"
					+ params.get("likeQuery") + "%' ) ";
		}
		if (params.containsKey("netbarId")) {
			conditonSql += " and ncc.netbar_id  = " + params.get("netbarId");
		}

		if (params.containsKey("status")) {
			conditonSql += " and ncc.status  = " + params.get("status");
		}

		if (params.containsKey("startDate") && params.containsKey("endDate")) {
			conditonSql += " and ncc.create_date >= '" + params.get("startDate") + "' and ncc.create_date <= '"
					+ params.get("endDate") + " 23:59:59'";
		}

		// 查询总数
		String totalCountSql = " select    count(1) 	from    netbar_card_coupon ncc   left join user_t_info uti "
				+ "	on ncc.user_id = uti.id 	where    ncc.is_valid = 1  " + conditonSql;

		Number total = queryDao.query(totalCountSql);
		if (total == null || total.intValue() == 0) {
			return new PageVO();
		}

		Integer page = (Integer) params.get("page");

		// 分页
		String limit = "";
		Integer pageSize = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		if (page <= 0) {
			page = 1;
		}

		Integer startRow = (page - 1) * pageSize;
		limit = SqlJoiner.join(" LIMIT ", startRow.toString(), ", ", pageSize.toString());
		String sql = " select    ncc.id, ncc.status,ncc.create_date,ncc.remark,  uti.nickname, uti.username telephone, nccc.name,nccc.amount, nccc.start_date, nccc.end_date  "
				+ "	from    netbar_card_coupon ncc  left join user_t_info uti "
				+ "	on ncc.user_id = uti.id left join netbar_card_coupon_category nccc on ncc.category_id = nccc.id  "
				+ "	where    ncc.is_valid = 1  " + conditonSql + " order by ncc.create_date desc " + limit;

		List<Map<String, Object>> dataList = queryDao.queryMap(sql);
		PageVO vo = new PageVO();
		vo.setCurrentPage(page);
		vo.setList(dataList);
		vo.setTotal(total.intValue());
		int isLast = total.intValue() > page * pageSize ? 1 : 0;
		vo.setIsLast(isLast);
		return vo;
	}

	public Map<String, Object> stasticsInfo(Map<String, Object> params) {
		String conditonSql = "";
		if (params.containsKey("likeQuery")) {
			conditonSql += " and (uti.username like '%" + params.get("likeQuery") + "%' or uti.nickname like '%"
					+ params.get("likeQuery") + "%' ) ";
		}
		if (params.containsKey("netbarId")) {
			conditonSql += " and ncc.netbar_id  = " + params.get("netbarId");
		}

		if (params.containsKey("startDate") && params.containsKey("endDate")) {
			conditonSql += " and ncc.create_date >= '" + params.get("startDate") + "' and ncc.create_date <= '"
					+ params.get("endDate") + " 23:59:59'";
		}

		String sql = " select   ncc.status ,sum(nccc.amount) amount "
				+ "	from    netbar_card_coupon ncc  left join user_t_info uti "
				+ "	on ncc.user_id = uti.id left join netbar_card_coupon_category nccc on ncc.category_id = nccc.id  "
				+ "	where    ncc.is_valid = 1  " + conditonSql + " group by ncc.status ";

		List<Map<String, Object>> dataList = queryDao.queryMap(sql);
		double toAmount = 0.00;
		double totalAmount = 0.00;
		if (CollectionUtils.isNotEmpty(dataList)) {
			for (Map<String, Object> map : dataList) {
				Object object = map.get("status");
				String status = object == null ? "1" : object.toString();
				double acount = ((Number) map.get("amount")).doubleValue();
				if (status.equals("2")) {
					toAmount += acount;
				}
				totalAmount += acount;
			}
		}

		Map<String, Object> result = Maps.newHashMap();
		result.put("toAmount", toAmount);
		result.put("totalAmount", totalAmount);
		return result;
	}

	public NetbarCardCoupon findById(long id) {
		return netbarCardCouponDao.findOne(id);
	}

	public NetbarCardCoupon save(NetbarCardCoupon cc) {
		return netbarCardCouponDao.save(cc);

	}

	@Autowired
	private NetbarCardCouponSettleService netbarCardCouponSettleService;

	@Transactional
	public int applyPay(NetbarMerchant merchant, String[] idStrs) {
		if (merchant == null || merchant.getId() == null || merchant.getNetbarId() == null) {
			return -1;
		}
		Long netbarId = merchant.getNetbarId();

		String ids = StringUtils.EMPTY;
		for (String idStr : idStrs) {
			if (NumberUtils.isNumber(idStr)) {
				ids = ids + "," + idStr;
			}
		}
		ids = StringUtils.removeStart(ids, ",");
		String sql = "select ncc.status, ncc.netbar_id, nccc.amount from    netbar_card_coupon ncc     left join netbar_card_coupon_category nccc "
				+ " on ncc.category_id = nccc.id where ncc.id in (" + ids + ") and ncc.status = 2";

		List<Map<String, Object>> nccs = queryDao.queryMap(sql);
		if (CollectionUtils.isNotEmpty(nccs)) {

			// 统计雇员提交订单数额
			Double totalAmount = 0.0;
			Integer orderNum = 0;

			for (Map<String, Object> ncc : nccs) {
				//只处理未申请支付的订单，否则前台提示刷新页面
				Object statusObject = ncc.get("status");
				if (null != statusObject) {
					if (2 != ((Number) statusObject).intValue()) {
						return -3;
					}
					Number nid = (Number) ncc.get("netbar_id");
					if (nid == null || netbarId.longValue() != nid.longValue()) {
						return -5;
					}

					Number amount = (Number) ncc.get("amount");
					if (null != amount) {
						totalAmount = ArithUtil.add(totalAmount, amount.doubleValue());
					}
					orderNum += 1;
				} else {
					return -3;
				}

			}

			NetbarCardCouponSettle settle = new NetbarCardCouponSettle();
			settle.setAmount(totalAmount);
			settle.setCreateDate(new Date());
			settle.setNetbarId(netbarId);
			settle.setNum(orderNum);
			settle.setValid(1);
			settle.setStatus(0);

			NetbarCardCouponSettle settleResult = netbarCardCouponSettleService.save(settle);
			String updateSql = "update netbar_card_coupon set status =3,coupon_settle_id="
					+ settleResult.getId().longValue() + " where id in (" + ids + ")";
			queryDao.update(updateSql);

			return 0;
		} else {
			return -4;
		}

	}

	public List<NetbarCardCoupon> findValidByIdIn(List<Long> ids) {
		if (CollectionUtils.isNotEmpty(ids)) {
			Long[] idsArray = ids.toArray(new Long[ids.size()]);
			return netbarCardCouponDao.findByIdInAndValid(idsArray, CommonConstant.INT_BOOLEAN_TRUE);
		}
		return null;
	}
}
