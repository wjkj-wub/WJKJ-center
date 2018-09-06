package com.miqtech.master.service.netbar;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.Msg4UserType;
import com.miqtech.master.consts.MsgConstant;
import com.miqtech.master.consts.ReserveSmsConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.NetbarReservationDao;
import com.miqtech.master.dao.user.UserInfoDao;
import com.miqtech.master.entity.netbar.NetbarInfo;
import com.miqtech.master.entity.netbar.NetbarMerchant;
import com.miqtech.master.entity.netbar.NetbarOrder;
import com.miqtech.master.entity.netbar.NetbarReservation;
import com.miqtech.master.entity.netbar.NetbarStaff;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.service.user.UserInfoService;
import com.miqtech.master.service.user.UserRedbagService;
import com.miqtech.master.thirdparty.service.JpushService;
import com.miqtech.master.thirdparty.util.TradeNoUtil;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class NetbarReservationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(NetbarReservationService.class);

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private NetbarReservationDao netbarReservationDao;
	@Autowired
	private JpushService msgOperateService;
	@Autowired
	private NetbarInfoService netbarInfoService;
	@Autowired
	private UserInfoDao userInfoDao;
	@Autowired
	private UserRedbagService userRedbagService;
	@Autowired
	private UserInfoService userInfoService;
	@Autowired
	private NetbarOrderService netbarOrderService;
	@Autowired
	private StringRedisOperateService stringRedisOperateService;
	@Autowired
	private NetbarUserService netbarUserService;

	private final static Joiner JOINER = Joiner.on("_");
	private final static String NEWEST_RESERVATION_PREFIX = "master_netbar_reservation_newest";
	private RedisTemplate<String, Long> redisTemplate;

	@Autowired
	public void setRedisTemplate(@Qualifier("defaultRedisTemplate") RedisTemplate<String, Long> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public RedisTemplate<String, Long> getRedisTemplate() {
		return redisTemplate;
	}

	public void addDataToCache(Long netbarId, Long reservationId) {
		String key = JOINER.join(NEWEST_RESERVATION_PREFIX, netbarId);
		ValueOperations<String, Long> opsForValue = redisTemplate.opsForValue();
		opsForValue.set(key, reservationId);
	}

	public Long getDataFromCache(Long netbarId) {
		String key = JOINER.join(NEWEST_RESERVATION_PREFIX, netbarId);
		ValueOperations<String, Long> opsForValue = redisTemplate.opsForValue();
		return opsForValue.get(key);
	}

	private int convertStatusToIsReceive(int status) {
		int isReceive = 0;
		if (status == 1) {
			isReceive = 0;
		} else if (status == 2) {
			isReceive = 1;
		} else if (status == 3) {
			isReceive = -1;
		}
		return isReceive;
	}

	public int accept(NetbarMerchant currentMerchant, Long id) {
		NetbarReservation nr = netbarReservationDao.findOne(id);
		if (nr != null) {

			if (validReservation(currentMerchant, nr)) {
				if (nr.getValid().equals(2)) {
					return 1;
				}
				if (nr.getValid().equals(0)) {
					return -2;
				}
				if (nr.getAmount() <= 0) {
					zeroReserveCofirm(nr.getUserId().toString(), id.toString(), nr.getUserId());
				}
				nr.setIsReceive(1);
				nr.setUpdateDate(new Date());
				netbarReservationDao.save(nr);
				try {
					NetbarInfo netbar = netbarInfoService.findById(currentMerchant.getNetbarId());
					msgOperateService.notifyMemberAliasMsg(Msg4UserType.ORDER_RESERVE.ordinal(), nr.getUserId(),
							MsgConstant.PUSH_MSG_TYPE_ORDER_RESERVATION, "【" + netbar.getName() + "】接单了",
							netbar.getName() + "接受了您的预定。", true, nr.getId());
				} catch (Exception e) {
					LOGGER.error("JPUSH推送信息异常:", e.getMessage());
				}
				return 0;
			}
		}
		return -1;

	}

	private boolean validReservation(NetbarMerchant currentMerchant, NetbarReservation nr) {
		return nr.getNetbarId().equals(currentMerchant.getNetbarId()) && nr.getIsReceive().equals(0);
	}

	public int ignore(NetbarMerchant currentMerchant, Long id) {
		NetbarReservation nr = netbarReservationDao.findOne(id);
		if (nr != null) {
			if (validReservation(currentMerchant, nr)) {
				if (nr.getValid().equals(2)) {
					return 1;
				}
				if (nr.getValid().equals(0)) {
					return -2;
				}
				nr.setIsReceive(-1);
				netbarReservationDao.save(nr);
				try {
					NetbarInfo netbar = netbarInfoService.findById(currentMerchant.getNetbarId());
					msgOperateService.notifyMemberAliasMsg(Msg4UserType.ORDER_RESERVE.ordinal(), nr.getUserId(),
							MsgConstant.PUSH_MSG_TYPE_ORDER_RESERVATION, "【" + netbar.getName() + "】拒绝了你的预订",
							"很抱歉，您的预定不成功，建议更换一家网吧尝试！", true, nr.getId());
				} catch (Exception e) {
					LOGGER.error("JPUSH推送信息异常:", e.getMessage());
				}
				return 0;
			} else {
				return 1;
			}
		}
		return -1;
	}

	public int arrive(NetbarMerchant currentMerchant, Long id) {
		NetbarReservation nr = netbarReservationDao.findOne(id);
		if (nr != null) {
			Integer arrive = nr.getArrive();
			Long netbarId = currentMerchant.getNetbarId();
			if (nr.getNetbarId().equals(netbarId) && (arrive == null || arrive.equals(0))) {
				if (canChangeToArrive(id)) {
					nr.setArrive(1);
					nr.setIsReceive(1);
					nr.setUpdateDate(new Date());
					netbarReservationDao.save(nr);
					userRedbagService.updateUnusableRedbag(nr.getUserId());//设置用户没有启用的有效红包启用
					return 0;
				}
				return 1;
			}
		}
		return -1;
	}

	private boolean canChangeToArrive(Long reservationId) {
		String sql = "select count(1) num from netbar_r_reservation where is_valid > 0 and id =  " + reservationId;
		Number count = queryDao.query(sql);
		return count == null ? false : count.intValue() >= 1;
	}

	public long findNewestReservationByNetbarId(Long netbarId) {
		String sql = "select id from netbar_r_reservation where is_valid=1 and netbar_Id = " + netbarId
				+ " order by create_date desc limit 1 ";
		Number id = queryDao.query(sql);
		return id == null ? 0 : id.longValue();
	}

	public PageVO findNetbarReservationList(Long userId, int page, int pageSize) {
		int start = (page - 1) * pageSize;
		String sql = "";
		Map<String, Object> params = new HashMap<String, Object>();
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		Map<String, Object> total = null;
		params.put("userId", userId);
		sql = "select count(*) total from  (select a.is_valid,b.price,a.id,a.netbar_id,a.user_id,a.seating,a.telephone,a.is_receive,a.reservation_time,a.amount,a.overpay,a.create_date,b.icon,b.name  from netbar_r_reservation a,netbar_t_info b where a.user_id=:userId and a.netbar_id=b.id)a left JOIN (select reserve_id,status from netbar_r_order )b on a.id=b.reserve_id";
		total = queryDao.querySingleMap(sql, params);
		sql = "select is_valid,price,id reserve_id,netbar_id,user_id,seating,telephone,is_receive,reservation_time,amount,overpay,create_date,hours,icon,name netbar_name,status from  (select a.is_valid,b.price,a.id,a.netbar_id,a.user_id,a.seating,a.telephone,a.is_related,a.is_receive,a.reservation_time,a.amount,a.overpay,a.create_date,a.hours,b.icon,b.name  from netbar_r_reservation a,netbar_t_info b where a.user_id=:userId and a.netbar_id=b.id and a.is_valid!=-1)a left JOIN (select reserve_id,status from netbar_r_order )b on a.id=b.reserve_id order by create_date desc limit :start,:pageSize";
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

	public Map<String, Object> findReserveDetail(Long reserveId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("reserveId", reserveId);
		String sql = "select a.is_valid,a.id reserve_id,a.netbar_id,a.telephone,a.user_id,a.seating,a.is_receive,a.arrive,a.reservation_time begin_time,date_add(reservation_time, interval a.hours hour) end_time,a.overpay,a.create_date,a.hours,b.icon,b.name netbar_name,b.telephone netbar_tel,IFNULL(status,0) status from (netbar_r_reservation a,netbar_t_info b) left JOIN  (select reserve_id,status from netbar_r_order)b on a.id=b.reserve_id  where a.id=:reserveId and a.netbar_id=b.id and a.is_valid!=-1 order by create_date desc";
		Map<String, Object> result = queryDao.querySingleMap(sql, params);
		if (result == null) {
			return new HashMap<String, Object>();
		}
		Integer state = null;
		Integer isReceive = result.get("is_receive") == null ? 0 : ((Byte) result.get("is_receive")).intValue();
		Integer isValid = result.get("is_valid") == null ? 1 : ((Byte) result.get("is_valid")).intValue();
		Integer status = result.get("status") == null ? 0 : ((BigInteger) result.get("status")).intValue();
		Integer arrive = result.get("arrive") == null ? 0 : ((Byte) result.get("arrive")).intValue();
		Integer overpay = (BigDecimal) result.get("overpay") == null ? 0
				: ((BigDecimal) result.get("overpay")).intValue();
		if (arrive.equals(1)) {
			state = 8;//网吧确认用户已到店
		} else if (status >= 1 && overpay.equals(0)) {
			state = 7;//订单已确认
		} else if (status >= 1) {
			state = 6;//订单已支付
		} else if (status.equals(-1)) {
			state = 5;//订单支付失败
		} else if (isValid.equals(0) && isReceive.equals(1)) {
			state = 4;//订单已取消(网吧接单后)
		} else if (isReceive.equals(-1)) {
			state = 3;//网吧已拒单
		} else if (isReceive.equals(1)) {
			state = 2;//网吧已接单
		} else if (isValid.equals(0) && isReceive.equals(0)) {
			state = 1;//订单已取消
		} else if (isReceive.equals(0)) {
			state = 0;//订单待处理
		}
		result.put("state", state);
		return result;
	}

	public Map<String, Object> doReserve(Long netbarId, Long userId, int num, String remark, String reserveDate,
			int hours, double amount) {
		NetbarReservation netbarReserve = new NetbarReservation();
		netbarReserve.setNetbarId(netbarId);
		netbarReserve.setUserId(userId);
		netbarReserve.setSeating(num);
		netbarReserve.setRemark(remark);
		netbarReserve.setIsRelated(1);//默认设置为连座
		try {
			netbarReserve.setReservationTime(DateUtils.stringToDateYyyyMMddhhmmss(reserveDate));
		} catch (ParseException e) {
			LOGGER.error("一键预定,预定时间转换异常:", e.getMessage());
		}
		netbarReserve.setValid(1);
		netbarReserve.setHours(hours);
		netbarReserve.setIsReceive(0);
		netbarReserve.setAmount(amount);
		netbarReserve.setCreateDate(new Date());
		netbarReserve.setCreateUserId(userId);
		UserInfo userInfo = userInfoDao.findOne(userId);
		if (userInfo != null) {
			netbarReserve.setTelephone(userInfo.getTelephone());
		}
		netbarReserve = netbarReservationDao.save(netbarReserve);
		msgOperateService.notifyMemberAliasMsg(Msg4UserType.ORDER_RESERVE.ordinal(), userId,
				MsgConstant.PUSH_MSG_TYPE_ORDER_RESERVATION, "预定信息发送成功", "您的预定信息已发送成功，请等待网吧响应。", true,
				netbarReserve.getId());
		String sql = "select name netbar_name,icon,address from netbar_t_info where id=:netbarId";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("netbarId", netbarId);

		//加入发送短信队列
		addToRedisForSms(netbarReserve);
		netbarUserService.bindNetbar(userId, netbarId);

		// 返回预定结果
		Map<String, Object> result = null;
		List<Map<String, Object>> netbars = queryDao.queryMap(sql, params);
		if (CollectionUtils.isNotEmpty(netbars)) {
			result = netbars.get(0);
		} else {
			result = new HashMap<String, Object>();
		}
		result.put("reserveId", netbarReserve.getId());
		result.put("reserve_id", netbarReserve.getId());

		// 检查是否允许分享红包
		int canShareRedbag = CommonConstant.INT_BOOLEAN_FALSE;
		// 启用分享红包时须取消以下注释代码
		/*Map<String, Object> firstReserve = getUserTodaysFirstPublishReserve(userId);
		if (firstReserve != null) {
			Number id = (Number) firstReserve.get("id");
			if (netbarReserve.getId().equals(id.longValue())) {
				canShareRedbag = CommonConstant.INT_BOOLEAN_TRUE;
			}
		}*/
		result.put("canShareRedbag", canShareRedbag);

		return result;
	}

	/**
	 * 获取用户当天第一笔预定订单
	 */
	public Map<String, Object> getUserTodaysFirstPublishReserve(long userId) {
		String sql = SqlJoiner.join("SELECT id, telephone FROM netbar_r_reservation",
				" WHERE DATE_FORMAT(create_date, '%Y-%m-%d') = CURRENT_DATE () AND user_id = ", String.valueOf(userId),
				" ORDER BY create_date ASC LIMIT 0, 1");
		return queryDao.querySingleMap(sql);
	}

	/**
	 * 查询用户某天发布的第一笔预定订单
	 */
	public Map<String, Object> getUserFirstPublishReserveByDate(long userId, String date) {
		String sql = SqlJoiner.join(
				"SELECT id, telephone FROM netbar_r_reservation  WHERE DATE_FORMAT(create_date, '%Y-%m-%d') = '", date,
				"' AND user_id = ", String.valueOf(userId), " ORDER BY create_date ASC LIMIT 0, 1");
		return queryDao.querySingleMap(sql);
	}

	/************************* 雇员操作 ***************************/

	public int accept(NetbarStaff currentStaff, Long id) {
		NetbarReservation nr = netbarReservationDao.findOne(id);
		if (nr != null) {
			if (nr.getNetbarId().equals(currentStaff.getNetbarId()) && nr.getIsReceive().equals(0)) {
				if (nr.getValid().equals(2)) {
					return 1;
				}
				nr.setOperateStaffId(currentStaff.getId());
				nr.setIsReceive(1);
				nr.setUpdateDate(new Date());
				netbarReservationDao.save(nr);
				try {
					NetbarInfo netbar = netbarInfoService.findById(currentStaff.getNetbarId());
					msgOperateService.notifyMemberAliasMsg(Msg4UserType.ORDER_RESERVE.ordinal(), nr.getUserId(),
							MsgConstant.PUSH_MSG_TYPE_ORDER_RESERVATION, "【" + netbar.getName() + "】接单了",
							netbar.getName() + "接受了您的预定，请支付押金确认。", true, nr.getId());
				} catch (Exception e) {
					LOGGER.error("JPUSH推送信息异常:", e.getMessage());
				}
				return 0;
			}
		}
		return -1;
	}

	public int arrive(NetbarStaff currentStaff, Long id) {
		NetbarReservation nr = netbarReservationDao.findOne(id);
		if (nr != null) {
			Integer arrive = nr.getArrive();
			Long netbarId = currentStaff.getNetbarId();
			if (nr.getNetbarId().equals(netbarId) && (arrive == null || arrive.equals(0))) {
				if (canChangeToArrive(id)) {
					nr.setArrive(1);
					nr.setUpdateDate(new Date());
					nr.setOperateStaffId(currentStaff.getId());
					netbarReservationDao.save(nr);
					userRedbagService.updateUnusableRedbag(nr.getUserId());//设置用户没有启用的有效红包启用
					return 0;
				}
				return 1;
			}
		}
		return -1;
	}

	public int ignore(NetbarStaff currentStaff, Long id) {
		NetbarReservation nr = netbarReservationDao.findOne(id);
		if (nr != null) {
			if (nr.getNetbarId().equals(currentStaff.getNetbarId()) && nr.getIsReceive().equals(0)) {
				if (nr.getValid().equals(2)) {
					return 1;
				}
				nr.setOperateStaffId(currentStaff.getId());
				nr.setIsReceive(-1);
				netbarReservationDao.save(nr);
				try {
					NetbarInfo netbar = netbarInfoService.findById(currentStaff.getNetbarId());
					msgOperateService.notifyMemberAliasMsg(Msg4UserType.ORDER_RESERVE.ordinal(), nr.getUserId(),
							MsgConstant.PUSH_MSG_TYPE_ORDER_RESERVATION, "【" + netbar.getName() + "】拒绝了你的预订",
							"很抱歉，您的预定不成功，建议更换一家网吧尝试！", true, nr.getId());
				} catch (Exception e) {
					LOGGER.error("JPUSH推送信息异常:", e.getMessage());
				}
				return 0;
			} else {
				return 1;
			}
		}
		return -1;
	}

	public boolean cancleReserve(Long reserveId) {
		NetbarReservation netbarReservation = netbarReservationDao.findOne(reserveId);
		if (netbarReservation != null) {
			netbarReservation.setValid(0);
			netbarReservationDao.save(netbarReservation);
			//删除短信发送
			delReserveSmsNotify(reserveId);
			return true;
		}
		return false;
	}

	/**
	 * @param reserveId
	 * 删除短信发送
	 */
	public void delReserveSmsNotify(Long reserveId) {
		Set<String> set = stringRedisOperateService.getSetValues(ReserveSmsConstant.RESERVE_KEYS);
		if (set != null) {
			for (String s : set) {
				String[] array = s.split(",");
				if (array.length == 5) {
					if (array[0].equals(String.valueOf(reserveId))) {
						stringRedisOperateService.removeSetValue(ReserveSmsConstant.RESERVE_KEYS, s);
					}
				}
			}
		}
	}

	public boolean delReserve(Long reserveId) {
		NetbarReservation netbarReservation = netbarReservationDao.findOne(reserveId);
		if (netbarReservation != null) {
			netbarReservation.setValid(-1);
			netbarReservationDao.save(netbarReservation);
			return true;
		}
		return false;
	}

	public Map<String, Object> reserveToOrder(Long userId, Long reserveId) {
		NetbarOrder order = netbarOrderService.findByReserveId(reserveId);
		if (null == order) {
			NetbarReservation netbarReservation = netbarReservationDao.findOne(reserveId);
			UserInfo userInfo = userInfoDao.findOne(userId);
			NetbarOrder netbarOrder = new NetbarOrder();
			netbarOrder.setReserveId(reserveId);
			netbarOrder.setOutTradeNo(TradeNoUtil.getOutTradeNo(userId.toString(), reserveId.toString()));
			netbarOrder.setTotalAmount(netbarReservation.getAmount());
			netbarOrder.setAmount(netbarReservation.getAmount());
			netbarOrder.setValid(1);
			netbarOrder.setCreateDate(new Date());
			netbarOrder.setCreateUserId(userId);
			netbarOrder.setStatus(0);
			netbarOrder.setUserId(userId);
			netbarOrder.setUserNickname(userInfo.getNickname());
			netbarOrder.setNetbarId(netbarReservation.getNetbarId());
			netbarOrderService.saveOrUpdate(netbarOrder);
		}

		Map<String, Object> params = Maps.newHashMap();
		params.put("reserveId", reserveId);
		String sql = "select a.id order_id,b.icon,b.name,b.address,a.amount from netbar_r_order a,netbar_t_info b where a.reserve_id=:reserveId and a.netbar_id=b.id and a.is_valid=1";
		return queryDao.querySingleMap(sql, params);
	}

	public void updateAfterPayed(Long id) {
		NetbarReservation reservation = netbarReservationDao.findOne(id);
		reservation.setValid(2);// 用户已经确认支付订单
		netbarReservationDao.save(reservation);
	}

	public NetbarReservation findById(Long reserveId) {
		return netbarReservationDao.findOne(reserveId);
	}

	/**
	 * 统计 所有雇员预定订单操作数据
	 */
	public Map<String, Object> statisticOperate(NetbarMerchant merchant) {
		long netbarId = merchant.getNetbarId();
		Map<String, Object> result = new HashMap<String, Object>();
		String countSql = "SELECT count(1) count FROM netbar_r_reservation r WHERE r.is_valid >= 1 and r.is_receive = 1  and r.netbar_id = "
				+ netbarId;
		Number query = queryDao.query(countSql);
		result.put("count", query);
		int totalCount = query.intValue();
		if (totalCount > 0) {
			String operateSql = "SELECT count(r.id) count, s.name FROM netbar_r_reservation r LEFT JOIN netbar_t_staff s ON r.operate_staff_id = s.id AND s.is_valid = 1 WHERE r.is_valid >= 1 AND r.operate_staff_id > 0 and arrive = 1 and  r.netbar_id = "
					+ netbarId + " GROUP BY r.operate_staff_id";
			List<Map<String, Object>> queryMap = queryDao.queryMap(operateSql);
			result.put("operates", queryMap);
			if (CollectionUtils.isNotEmpty(queryMap)) {
				int count = 0;
				for (Map<String, Object> map : queryMap) {
					Number staffCount = (Number) map.get("count");
					count += staffCount.intValue();
				}
				HashMap<String, Object> newHashMap = Maps.newHashMap();
				newHashMap.put("count", totalCount - count);
				newHashMap.put("name", merchant.getOwnerName());
				queryMap.add(newHashMap);
			}
		}
		return result;
	}

	public List<Map<String, Object>> findPageDataByMapParams(NetbarMerchant currentMerchant,
			Map<String, Object> params) {
		int status = NumberUtils.toInt(params.get("status").toString());
		Object pageObj = params.get("page");
		int start = (NumberUtils.toInt(pageObj.toString(), 1) - 1) * PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		params.put("start", start);
		params.put("pageSize", PageUtils.ADMIN_DEFAULT_PAGE_SIZE);

		params.put("netbarId", currentMerchant.getNetbarId());
		String sql = "select   r.id, r.user_id, r.seating, u.telephone, r.is_related, r.overpay, r.reservation_time, r.create_date,r.remark,r.arrive, u.nickname "
				+ " from    netbar_r_reservation r left join user_t_info u         on r.user_id = u.id  where  r.netbar_id = :netbarId";
		String orderAndLimit = " order by r.create_date desc limit :start, :pageSize ";
		if (status <= 3) {
			int isReceive = convertStatusToIsReceive(status);
			params.put("isReceive", isReceive);
			sql = sql + "   and r.is_receive = :isReceive and (r.is_valid=1 or r.is_valid=2) ";
		} else if (status == 4) {
			sql = sql + "   and r.is_valid=0 ";
		}
		if (params.containsKey("nickname")) {
			sql = sql + " and  u.nickname like :nickname ";
		}
		if (params.containsKey("telephone")) {
			sql = sql + " and u.telephone like :telephone ";
		}
		if (params.containsKey("related")) {
			int related = Integer.parseInt(params.get("related").toString());
			if (related == 0) {
				sql = sql + " and  r.seating <=1 ";
			} else if (related == 1) {
				sql = sql + " and  r.seating >1 ";
			}
			params.remove("related");
		}
		if (params.containsKey("beginTime")) {
			sql = sql + " and r.create_date > :beginTime and r.create_date < :endTime ";
		}
		if (params.containsKey("playBeginTime")) {
			sql = sql + " and r.reservation_time > :playBeginTime and r.reservation_time < :playEndTime ";
		}
		sql = sql + orderAndLimit;
		params.remove("page");
		params.remove("status");

		return queryDao.queryMap(sql, params);
	}

	public Long findTotalCountByMapParams(NetbarMerchant currentMerchant, Map<String, Object> params) {
		int status = NumberUtils.toInt(params.get("status").toString());
		params.put("netbarId", currentMerchant.getNetbarId());
		String sql = null;
		if (status <= 3) {
			int isReceive = convertStatusToIsReceive(status);
			params.put("isReceive", isReceive);
			sql = "select count(1) totalNum from netbar_r_reservation r where r.netbar_id = :netbarId  and (r.is_valid=1 or r.is_valid=2)  and r.is_receive = :isReceive";

		} else if (status == 4) {
			sql = "select count(1) totalNum from netbar_r_reservation r where r.netbar_id = :netbarId  and r.is_valid=0  ";
		}
		if (params.containsKey("nickname")) {
			sql = sql + " and r.user_id in (select id from user_t_info u where  u.nickname like :nickname ";
			if (params.containsKey("telephone")) {
				sql = sql + " and u.telephone like :telephone ) ";
			} else {
				sql = sql + " ) ";
			}
		} else {
			if (params.containsKey("telephone")) {
				sql = sql
						+ " and r.user_id in (select id from user_t_info u where   and u.telephone like :telephone ) ";
			}
		}
		if (params.containsKey("related")) {
			int related = Integer.parseInt(params.get("related").toString());
			if (related == 0) {
				sql = sql + " and  r.seating <=1 ";
			} else if (related == 1) {
				sql = sql + " and  r.seating >1 ";
			}
			params.remove("related");
		}
		if (params.containsKey("beginTime")) {
			sql = sql + " and r.create_date > :beginTime and r.create_date < :endTime ";
		}
		if (params.containsKey("playBeginTime")) {
			sql = sql + " and r.reservation_time > :playBeginTime and r.reservation_time < :playEndTime ";
		}
		params.remove("start");
		params.remove("pageSize");
		params.remove("status");
		Map<String, Object> result = queryDao.querySingleMap(sql, params);
		if (null != result) {
			Number count = (Number) result.get("totalNum");
			return count == null ? 0 : count.longValue();
		} else {
			return 0L;
		}
	}

	/**
	 * 统计雇员某段时间的接单情况
	 */
	public List<Map<String, Object>> statisticOperateByStaffId(long staffId, String beginDate, String endDate) {
		String statisticSql = "select DATE_FORMAT(update_date,'%Y-%m-%d') date, count(1) count from netbar_r_reservation where operate_staff_id = "
				+ staffId + " and update_date >= '" + beginDate + "' and update_date <= '" + endDate
				+ "' and is_receive = 1 and is_valid >= 1 and arrive = 1 group by date order by date asc";
		return queryDao.queryMap(statisticSql);
	}

	/**
	 * 0金额预定订单确认去店
	 * @return
	 */
	public NetbarOrder zeroReserveCofirm(String userId, String reserveId, Long userIdLong) {
		long reserveIdLong = NumberUtils.toLong(reserveId);
		NetbarReservation netbarReservation = findById(reserveIdLong);
		netbarReservation.setValid(2);
		netbarReservationDao.save(netbarReservation);
		UserInfo userInfo = userInfoService.findById(userIdLong);
		NetbarOrder netbarOrder = new NetbarOrder();
		netbarOrder.setReserveId(reserveIdLong);
		netbarOrder.setOutTradeNo(TradeNoUtil.getOutTradeNo(userId, reserveId));
		netbarOrder.setTotalAmount(netbarReservation.getAmount());
		netbarOrder.setAmount(netbarReservation.getAmount());
		netbarOrder.setCreateDate(new Date());
		netbarOrder.setCreateUserId(userIdLong);
		netbarOrder.setStatus(0);
		netbarOrder.setUserId(userIdLong);
		netbarOrder.setUserNickname(userInfo.getNickname());
		netbarOrder.setNetbarId(netbarReservation.getNetbarId());
		netbarOrder.setType(0);
		netbarOrder.setValid(2);
		netbarOrder.setStatus(1);
		return netbarOrderService.saveOrUpdate(netbarOrder);
	}

	public boolean hasValidReservation(Long userId) {
		String sql = "select count(id) from netbar_r_reservation where is_valid=1 and is_receive in (0,1) and user_id ="
				+ userId;
		Number number = queryDao.query(sql);
		if (null != number) {
			return number.intValue() > 0 ? true : false;
		}
		return false;
	}

	public NetbarReservation saveOrUpdate(NetbarReservation netbarReservation) {
		return netbarReservationDao.save(netbarReservation);

	}

	/**
	 * 后台统计网吧每天到店数据
	 */
	public PageVO reserverStatistic(int type, String netbarName, String startDate, String endDate) {
		PageVO pageVO = new PageVO();
		String formatPattern = "%Y-%m-%d";
		if (type == 1) {
			formatPattern = "%Y";
		} else if (type == 2) {
			formatPattern = "%Y-%m";
		}
		String dateSql = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
			try {
				Date start = DateUtils.stringToDateYyyyMMdd(startDate);
				Date end = DateUtils.stringToDateYyyyMMdd(endDate);
				startDate = DateUtils.dateToString(start, "yyyy-MM-dd 00:00:00");
				endDate = DateUtils.dateToString(end, "yyyy-MM-dd 23:59:59");
				dateSql = " and nr.update_date<='" + endDate + "' and nr.update_date>='" + startDate + "' ";
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
		String statsticSQL = "";
		statsticSQL = SqlJoiner.join(" select sum(countNum) sumNum from (select  ni.name,date_format(nr.create_date, '",
				formatPattern,
				"') date, if(count(distinct nr.user_id) >= 30, 30, count(distinct nr.user_id)) countNum ",
				" from netbar_r_reservation nr left join netbar_t_info ni on nr.netbar_id = ni.id where nr.arrive = 1 and nr.is_valid > 0",
				dateSql, nameSql, " group by date_format(nr.create_date, '", formatPattern, "') desc,nr.netbar_id)a ");
		Number totalCount = queryDao.query(statsticSQL);
		if (totalCount != null) {
			pageVO.setTotal(totalCount.longValue());
		}
		statsticSQL = SqlJoiner.join(" select  ni.name,date_format(nr.create_date, '", formatPattern,
				"') date, if(count(distinct nr.user_id) >= 30, 30, count(distinct nr.user_id)) countNum ",
				" from netbar_r_reservation nr left join netbar_t_info ni on nr.netbar_id = ni.id where nr.arrive = 1 and nr.is_valid > 0",
				dateSql, nameSql, " group by date_format(nr.create_date, '", formatPattern, "') desc,nr.netbar_id ",
				" order by nr.netbar_id desc, date_format(nr.create_date, '", formatPattern, "') desc  ");
		pageVO.setList(queryDao.queryMap(statsticSQL));
		return pageVO;
	}

	public void addToRedisForSms(NetbarReservation netbarReservation) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(netbarReservation.getId()).append(",").append(netbarReservation.getTelephone()).append(",");
			NetbarInfo netbarInfo = netbarInfoService.findById(netbarReservation.getNetbarId());
			if (netbarInfo != null) {
				sb.append(netbarInfo.getName()).append(",");
			}
			sb.append(netbarReservation.getSeating()).append(",")
					.append(DateUtils.dateToString(netbarReservation.getReservationTime(), DateUtils.YYYY_MM_DD_HH_MM));
			String value = sb.toString();
			stringRedisOperateService.addValuesToSet(ReserveSmsConstant.RESERVE_KEYS, value);
			long time = netbarReservation.getReservationTime().getTime() - ReserveSmsConstant.RESERVE_MINUTE * 60 * 1000
					- System.currentTimeMillis();
			stringRedisOperateService.setData(value, "1", time, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			LOGGER.error("短信通知异常：", e);
		}
	}

	/**用户发起预订统计
	 * @param mobile
	 * @param page
	 * @return
	 */
	public PageVO userReserveNum(String mobile, int page) {
		String mobileSql = "";
		String sql = "";
		PageVO vo = new PageVO();
		if (StringUtils.isNotBlank(mobile)) {
			mobileSql = " where username=" + mobile;
		}
		sql = SqlJoiner.join(
				"select count(1) total from (select count(1) from netbar_r_reservation a left join user_t_info b on a.user_id=b.id ",
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
				"select user_id,username,count(1) num from netbar_r_reservation a left join user_t_info b on a.user_id=b.id ",
				mobileSql, " group by user_id,username order by num desc limit :start,:pageSize");
		vo.setCurrentPage(page);
		vo.setList(queryDao.queryMap(sql, params));
		return vo;
	}

	/**前七天接单次数
	 * @param netbarId
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public List<Map<String, Object>> lastSevenDayAccept(String netbarId, String startDate, String endDate) {
		String sql = SqlJoiner.join(
				"select date_format(b.update_date, '%Y-%m-%d') date,count(id) num from netbar_r_reservation b where netbar_id=",
				netbarId, " and b.update_date>='", startDate, "' and b.update_date<='", endDate,
				"' and b.is_receive=1 group by date_format(b.update_date, '%Y-%m-%d')");
		return queryDao.queryMap(sql);
	}

	/**前七天预订次数
	 * @param netbarId
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public List<Map<String, Object>> lastSevenDayReserve(String netbarId, String startDate, String endDate) {
		String sql = SqlJoiner.join(
				"select date_format(b.create_date, '%Y-%m-%d') date,count(id) num  from netbar_r_reservation b where netbar_id=",
				netbarId, " and b.create_date>='", startDate, "' and b.create_date<='", endDate,
				"' group by date_format(b.create_date, '%Y-%m-%d')");
		return queryDao.queryMap(sql);
	}
}