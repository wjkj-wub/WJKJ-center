package com.miqtech.master.service.user;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.miqtech.master.config.SystemConfig;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.Msg4UserType;
import com.miqtech.master.consts.MsgConstant;
import com.miqtech.master.consts.RedbagConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.user.UserRedbagDao;
import com.miqtech.master.dao.user.UserValueAddedCardDao;
import com.miqtech.master.entity.common.SysValueAddedCard;
import com.miqtech.master.entity.common.SystemRedbag;
import com.miqtech.master.entity.netbar.NetbarOrder;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.entity.user.UserRedbag;
import com.miqtech.master.entity.user.UserValueAddedCard;
import com.miqtech.master.service.common.SMSVerifyService;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.service.system.SysValueAddedCardService;
import com.miqtech.master.service.system.SystemRedbagService;
import com.miqtech.master.thirdparty.service.JpushService;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.*;

/**
 * 红包功能service
 */
@Component
public class UserRedbagService {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserRedbagService.class);

	public final static String USE_REDBAG_DAY_LIMIT_PREFIX = "use_redbag_day_limit_";//用户使用红包每天限制计数器前缀
	public final static String DEVICE_USE_REDBAG_DAY_LIMIT_PREFIX = "device_use_redbag_day_limit_";//设备使用红包每天限制计数器前缀
	public final static String USER_COUPON_DAY_LIMIT = "user_coupon_day_limit";//用户使用增值券限制
	public final static String DEVICE_COUPON_DAY_LIMIT = "device_coupon_day_limit";//设备使用增值券限制
	private final static Joiner joiner = Joiner.on("_");

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private UserRedbagDao userRedbagDao;
	@Autowired
	private JpushService msgOperateService;
	@Autowired
	private SystemRedbagService serviceRedbagService;
	@Autowired
	private StringRedisOperateService redisOperateService;
	@Autowired
	private UserInfoService userInfoService;
	@Autowired
	private SMSVerifyService smsVerifyService;
	@Autowired
	private SystemRedbagService systemRedbagService;
	@Autowired
	private JedisConnectionFactory redisConnectionFactory;
	@Autowired
	SysValueAddedCardService sysValueAddedCardService;
	@Autowired
	UserValueAddedCardDao userValueAddedCardDao;

	@Autowired
	private SystemConfig systemConfig;
	public final static Map<Integer, Integer> minMoneyConfigMap = Maps.newHashMap();

	private void resetMinMoney(UserRedbag insertRB) {
		if (MapUtils.isEmpty(minMoneyConfigMap)) {
			String minMoneyConfig = systemConfig.getMinMoneyConfig();
			String[] config = StringUtils.split(minMoneyConfig, ";");
			if (ArrayUtils.isNotEmpty(config)) {
				for (String cnf : config) {
					String[] singleConfig = StringUtils.split(cnf, ",");
					if (ArrayUtils.isNotEmpty(singleConfig) && singleConfig.length == 2) {
						minMoneyConfigMap.put(NumberUtils.toInt(singleConfig[0]), NumberUtils.toInt(singleConfig[1]));
					}
				}
			}
		}
		Integer value = minMoneyConfigMap.get(insertRB.getAmount());
		insertRB.setLimitMinMoney(value == null ? 0 : value);
	}

	/**
	 * 根据用户ID查询所有有效的红包
	 */
	public List<UserRedbag> findValidByUserIdIn(List<Long> userIds) {
		if (CollectionUtils.isNotEmpty(userIds)) {
			return userRedbagDao.findByUserIdInAndValid(userIds, CommonConstant.INT_BOOLEAN_TRUE);
		}
		return null;
	}

	/**
	 * 根据ID查询
	 */
	public List<UserRedbag> findByIdIn(List<Long> ids) {
		return userRedbagDao.findByIdIn(ids);
	}

	/**
	 * 保存用户红包
	 */
	public UserRedbag save(UserRedbag insertRB, boolean isReset) {
		if (isReset) {
			resetMinMoney(insertRB);
		}
		return userRedbagDao.save(insertRB);
	}

	/**
	 * 保存用户红包(批量)
	 */
	public List<UserRedbag> save(List<UserRedbag> urs) {
		return (List<UserRedbag>) userRedbagDao.save(urs);
	}

	/**
	 * 根据用户ID、系统红包ID（类型），查最新插入的用户红包ID
	 */
	public long queryLatestUserRedbagIdByParams(long userId, long redbagId) {
		String sqlQuery = "select r.id,r.create_date from user_r_redbag r where r.user_id=" + userId
				+ " and r.redbag_id=" + redbagId + " order by r.create_date desc";
		return Long.parseLong(queryDao.querySingleMap(sqlQuery).get("id").toString());
	}

	/**
	 * 首次登陆赠送红包
	 */
	public void firstLoginRedbag(Long userId) {

		List<UserRedbag> redbags = userRedbagDao.findByRedbagIdAndUserId(3L, userId);
		if (CollectionUtils.isNotEmpty(redbags)) {
			LOGGER.error("赠送红包流程错误,用户{}可多次获取登录红包.", userId);
			return;
		}
		try {
			int money = 0;
			List<SystemRedbag> sysRedBag = serviceRedbagService.queryByPayType(0);
			Calendar cal = Calendar.getInstance();
			cal.set(2016, 3, 1, 0, 0, 0);
			long nowTimeMillis = System.currentTimeMillis();
			if (nowTimeMillis > cal.getTime().getTime()) {
				sysRedBag.addAll(serviceRedbagService.queryByPayType(9));//赛事红包
			}
			if (CollectionUtils.isNotEmpty(sysRedBag)) {
				for (SystemRedbag systemRedbag : sysRedBag) {
					money += systemRedbag.getMoney().intValue();
					UserRedbag insertRB = new UserRedbag();
					insertRB.setCreateDate(new Date());
					insertRB.setUserId(userId);
					insertRB.setRedbagId(systemRedbag.getId());
					insertRB.setValid(1);
					insertRB.setNetbarType(1);
					insertRB.setNetbarId(0L);
					insertRB.setAmount(systemRedbag.getMoney());//设置红包金额为系统红包金额
					insertRB.setUsable(systemRedbag.getRestrict() == 0 ? 1 : 0);//无限制表示可用红包,否则不可用
					if (nowTimeMillis > cal.getTime().getTime()) {
						insertRB.setLimitMinMoney(10);//满10元可用
						save(insertRB, false);
					} else {
						save(insertRB, true);
					}
				}
				msgOperateService.notifyMemberAliasMsg(Msg4UserType.SYS_REDBAG.ordinal(), userId,
						MsgConstant.PUSH_MSG_TYPE_REDBAG, "系统消息",
						"首次登陆网娱大师，赠送您" + sysRedBag.size() + "个红包(共计" + money + "元)快去“我的红包”查看使用吧。", true, null);// 系统消息,推送到手机
			}
		} catch (Exception e) {
			LOGGER.error("赠送红包异常", e);
		}
	}

	/**
	 * 查看当前用户可用红包
	 */
	public PageVO currentRedbag(Long userId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", userId);
		String sql = SqlJoiner.join(
				"select a.id, a.amount money, b.explain, a.create_date begin_date,a.limit_min_money min_money, date_add(a.create_date, interval b.day day) end_date, b.day, a.usable",
				" from user_r_redbag a, sys_t_redbag b",
				" where a.user_id = :userId and a.redbag_id = b.id and a.is_valid = 1",
				"    and a.create_date <= now() and now() <= date_add(a.create_date, interval b.day day)",
				" order by a.amount desc,a.create_date desc ");
		return new PageVO(queryDao.queryMap(sql, params), 1);
	}

	/**
	 * 查看当前用户可用红包,并返回当天已用数量
	 */
	public Map<String, Object> currentRedbagWithUsedInfo(Long userId, String payAmount, String netbarId) {
		Map<String, Object> result = Maps.newHashMap();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", userId);
		Double d = NumberUtils.toDouble(payAmount);
		if (d == 0.0) {
			d = 1000d;
		}
		params.put("payAmount", d);
		String netbarIdSql = "";
		if (StringUtils.isNotBlank(netbarId)) {
			netbarIdSql = " a.netbar_id=" + netbarId + " and ";
		}
		String sql = SqlJoiner.join(
				"select bb.id,bb.type, bb.money, bb.explain, bb.begin_date, bb.end_date, bb.day, bb.usable,bb.pay_amount_canuse,bb.limit_min_money min_money,bb.need_validate need_validate,name from (",
				"select a.id,b.type, a.amount money, b.explain, a.create_date begin_date, date_add(a.create_date, interval b.day day) end_date, b.day, a.usable ,a.limit_min_money ,case when a.limit_min_money>:payAmount then 0 else 1 end pay_amount_canuse",
				",case when a.redbag_id=3 then 1 else 0 end need_validate ,null name ",
				" from user_r_redbag a, sys_t_redbag b",
				" where a.user_id = :userId and a.redbag_id = b.id and b.type<>8 and b.type<>10 and a.is_valid = 1",
				" and a.create_date <= now() and now() <= date_add(a.create_date, interval b.day day) union all ( select a.id, 8 type, a.amount money, b.explain, a.create_date begin_date, CONCAT(date_format(a.create_date, '%Y-%m-%d'),' 23:59:59') end_date, 1 day, a.usable, a.limit_min_money, if (a.limit_min_money <=:payAmount, 1, 0) pay_amount_canuse, 0 need_validate,c.name from user_r_redbag a, sys_t_redbag b,netbar_t_info c where ",
				netbarIdSql,
				"a.netbar_id=c.id and a.user_id =:userId and a.redbag_id = b.id and (b.type = 10 or b.type=8) and a.is_valid = 1 and date_format(a.create_date, '%y-%m-%d') = date_format(now(), '%y-%m-%d'))) bb ",
				" order by  case when (bb.type=8 or bb.type=10) then 2 else bb.type=1 end DESC,bb.end_date asc,bb.pay_amount_canuse desc ,bb.usable desc ,bb.money desc ");
		result.put("list", queryDao.queryMap(sql, params));
		result.put("isLast", 1);
		result.put("canUseRedbagNum", NumberUtils.toInt(systemConfig.getRedbagDayLimit()));
		String day = DateUtils.dateToString(new Date(), DateUtils.YYYY_MM_DD);

		String redisDataKey = joiner.join(USE_REDBAG_DAY_LIMIT_PREFIX, day, userId.toString());
		RedisAtomicInteger usedCount = new RedisAtomicInteger(redisDataKey,
				redisOperateService.getRedisTemplate().getConnectionFactory());
		result.put("usedRedbagNum", usedCount.get());
		return result;
	}

	/**
	 * 查看历史红包(已使用和过期红包)
	 */
	public PageVO historyRedbag(Long userId, int page, int pageSize) {
		PageVO vo = new PageVO();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", userId);
		int start = (page - 1) * pageSize;
		params.put("start", start);
		params.put("pageSize", pageSize);
		String querySql = SqlJoiner.join(
				" select a.id,b.type, a.amount money, b.explain, a.create_date begin_date, date_add(a.create_date, interval b.day day) end_date, b.day,",
				"     case when a.is_valid = 0 or a.is_valid=-1 then 1  else 2 end cause,null name from user_r_redbag a, sys_t_redbag b",
				" where a.user_id = :userId  and b.type<>8 and (a.is_valid != 1 or date_add(a.create_date, interval b.day day) < now())",
				"     and a.redbag_id = b.id union all ( select a.id, b.type, a.amount money, b.explain, a.create_date begin_date, CONCAT(SUBSTR(date_format(a.create_date, '%Y-%m-%d'),1,10),' 23:59:59') end_date, b. day, if ( a.is_valid = 0 or a.is_valid =- 1, 1, 2 ) cause,c.name from user_r_redbag a, sys_t_redbag b,netbar_t_info c where a.netbar_id=c.id and a.user_id = :userId and b.type = 8 and CONCAT(date_format(a.create_date, '%Y-%m-%d'),' 23:59:59')<now() and a.redbag_id = b.id ) order by begin_date desc limit :start,:pageSize");
		List<Map<String, Object>> queryMap = queryDao.queryMap(querySql, params);
		vo.setList(queryMap);
		if (CollectionUtils.isNotEmpty(queryMap)) {
			String countSql = SqlJoiner.join(
					"select count(a.id) num from user_r_redbag a,sys_t_redbag b where a.user_id=", userId.toString(),
					" and a.redbag_id=b.id and (((a.is_valid != 1 or date_add(a.create_date, interval b.day day) < now()) and type<>8) or (type=8 and a.create_date<now()))");
			Number totalCount = queryDao.query(countSql);
			vo.setTotal(totalCount.intValue());
			if (page * pageSize >= totalCount.intValue()) {
				vo.setIsLast(1);
			}
		}
		return vo;
	}

	/**
	 * 使用红包后,更新红包状态 0表示红包已使用
	 * @param userId
	 */
	public void useRedBags(String rids, Long userId) {
		if (StringUtils.isNotBlank(rids)) {
			try {
				String sql = SqlJoiner.join("update user_r_redbag set is_valid=0 and usable=1 where id in (", rids,
						")");
				queryDao.update(sql);
			} catch (Exception e) {
				LOGGER.error("设置红包已使用状态异常,红包id:[{}]", rids);
			}
		}
	}

	public void addTodayUsedMount(String rids, Long userId) {
		String day = DateUtils.dateToString(new Date(), DateUtils.YYYY_MM_DD);
		String redisDataKey = joiner.join(USE_REDBAG_DAY_LIMIT_PREFIX, day, userId.toString());
		RedisAtomicInteger usedCount = new RedisAtomicInteger(redisDataKey,
				redisOperateService.getRedisTemplate().getConnectionFactory());
		String[] split = StringUtils.split(rids, ",");
		if (split != null && split.length > 0) {
			usedCount.addAndGet(split.length);
		}
	}

	public void divideTodayUsedMount(NetbarOrder order) {
		String day = DateUtils.dateToString(order.getCreateDate(), DateUtils.YYYY_MM_DD);
		String redisDataKey = joiner.join(USE_REDBAG_DAY_LIMIT_PREFIX, day, order.getUserId().toString());
		RedisAtomicInteger usedCount = new RedisAtomicInteger(redisDataKey,
				redisOperateService.getRedisTemplate().getConnectionFactory());
		String[] split = StringUtils.split(order.getRids(), ",");
		if (split != null && split.length > 0) {
			int divideMount = -split.length;//负数
			usedCount.addAndGet(divideMount);
		}
	}

	/**
	 * 修改设备的使用红包数量
	 */
	public void addTodayDeviceUsedMount(String deviceId, Long userId) {

		if (StringUtils.isNotBlank(deviceId)) {
			String day = DateUtils.dateToString(new Date(), DateUtils.YYYY_MM_DD);
			String redisDataKey = joiner.join(DEVICE_USE_REDBAG_DAY_LIMIT_PREFIX, day, deviceId);
			RedisAtomicInteger usedCount = new RedisAtomicInteger(redisDataKey,
					redisOperateService.getRedisTemplate().getConnectionFactory());
			usedCount.incrementAndGet();
		}

	}

	/**
	 * 恢复设备的使用红包数量
	 */
	public void divideTodayDeviceUsedMount(String deviceId, Long userId) {
		if (StringUtils.isNotBlank(deviceId)) {
			String day = DateUtils.dateToString(new Date(), DateUtils.YYYY_MM_DD);
			String redisDataKey = joiner.join(DEVICE_USE_REDBAG_DAY_LIMIT_PREFIX, day, deviceId);
			RedisAtomicInteger usedCount = new RedisAtomicInteger(redisDataKey,
					redisOperateService.getRedisTemplate().getConnectionFactory());
			usedCount.decrementAndGet();
		}
	}

	/**
	 * 网吧商户端后台操作:用户到店,启用红包
	 */
	public void updateUnusableRedbag(Long userId) {
		String sql = SqlJoiner.join("update user_r_redbag set usable=1 where is_valid=1 and usable = 0  and user_id = ",
				userId.toString());
		queryDao.update(sql);
	}

	/**
	 * 更新红包为使用中状态(usable=0,is_valid=-1),当使用成功后修改usable=1 is_valid=0 删除订单时修改为usable=1 is_valid=1
	 */
	public void updateRedbagToInUseStatus(String rids) {
		if (StringUtils.isNotBlank(rids)) {
			try {
				String sql = SqlJoiner.join("update user_r_redbag set usable=0,is_valid=-1 where id in ( ", rids, ")");
				queryDao.update(sql);
			} catch (Exception e) {
				LOGGER.error("更新红包为使用中状态,红包id:[{}]", rids);
			}
		}

	}

	/**
	 * 删除订单时恢复已经使用的红包
	 */
	public void recoveryRedbag(NetbarOrder order) {
		if (StringUtils.isNotBlank(order.getRids())) {
			try {
				if (order.getStatus() < 1) {
					String sql = SqlJoiner.join("update user_r_redbag set usable=1,is_valid=1 where id in ( ",
							order.getRids(), ")");
					queryDao.update(sql);
					divideTodayUsedMount(order);
				}
			} catch (Exception e) {
				LOGGER.error("删除订单时恢复红包异常,红包id:[{}]", order.getRids());
			}
		}

	}

	/**
	 * 查询用户对某个红包的领取记录
	 */
	public List<UserRedbag> getUserRedbags(Long userId, Long redbagId, Date beginTime) {
		return userRedbagDao.findByRedbagIdAndUserIdAndValidAndCreateDateGreaterThan(redbagId, userId, 1, beginTime);
	}

	/**
	 * 查询领取红包用户 领取的第一个红包
	 */
	public Map<String, Object> getUserFirstRedbag(Long redbagId) {
		String sql = SqlJoiner.join("SELECT ur.* FROM user_r_redbag ur",
				" LEFT JOIN sys_t_redbag sr ON ur.redbag_id = sr.id AND sr.is_valid = 1",
				" WHERE user_id = ( SELECT user_id FROM user_r_redbag WHERE id = ", redbagId.toString(),
				" ) AND sr.type = 4 ORDER BY create_date ASC LIMIT 0, 1;");
		return queryDao.querySingleMap(sql);
	}

	/**
	 * 查询分享红包的信息
	 */
	public Map<String, Object> getShareRedbagUserinfo(Long redbagId) {
		String sql = SqlJoiner.join("SELECT ur.amount, ui.telephone FROM user_r_redbag ur",
				" LEFT JOIN user_t_info ui ON ur.user_id = ui.id",
				" LEFT JOIN sys_t_redbag sr ON ur.redbag_id = sr.id AND sr.is_valid = 1 AND sr.type = 4 AND ui.is_valid = 1",
				" WHERE ur.id = ", redbagId.toString());
		return queryDao.querySingleMap(sql);
	}

	/**
	 * 校验当前用户当天是否还能使用指定数量的红包
	 */
	public boolean canUseRedbag(String rids, Long userId) {
		if (StringUtils.isBlank(rids)) {
			return true;
		}
		String day = DateUtils.dateToString(new Date(), DateUtils.YYYY_MM_DD);
		String redisDataKey = joiner.join(USE_REDBAG_DAY_LIMIT_PREFIX, day, userId.toString());
		RedisAtomicInteger usedCount = new RedisAtomicInteger(redisDataKey,
				redisOperateService.getRedisTemplate().getConnectionFactory());
		int usedNum = usedCount.get();
		if (usedNum >= 2) {
			return false;
		}
		if (StringUtils.isNotBlank(rids)) {
			String[] splitIds = StringUtils.split(rids, ",");
			if (splitIds.length > 0) {
				String[] split = StringUtils.split(rids, ",");
				if (split != null && split.length > 0) {
					return usedNum + split.length <= NumberUtils.toInt(systemConfig.getRedbagDayLimit());
				}
			}
		}
		return true;
	}

	/**
	 * 校验当前设备当天是否还能使用红包
	 */
	public boolean deviceCanUseRedbag(String rids, String deviceId) {
		if (StringUtils.isBlank(rids)) {
			return true;
		}
		if (StringUtils.isBlank(deviceId)) {
			return true;
		}
		String day = DateUtils.dateToString(new Date(), DateUtils.YYYY_MM_DD);
		String redisDataKey = joiner.join(DEVICE_USE_REDBAG_DAY_LIMIT_PREFIX, day, deviceId);
		RedisAtomicInteger usedCount = new RedisAtomicInteger(redisDataKey,
				redisOperateService.getRedisTemplate().getConnectionFactory());
		int usedNum = usedCount.get();
		if (usedNum >= 2) {
			return false;
		}
		return true;
	}

	/**
	 * 查看当前用户的可用红包数
	 */
	public int currentRedbagNum(Long userIdLong, String netbarId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", userIdLong);
		String sql = "";
		if (StringUtils.isNotBlank(netbarId)) {
			sql = SqlJoiner.join(
					"select count(1) amount from user_r_redbag a, sys_t_redbag b where a.user_id =:userId and a.redbag_id = b.id and a.is_valid = 1 and (( a.create_date <= now() and now() <= date_add( a.create_date, interval b. day day ) and type <> 8 ) or ( type = 8 and date_format(a.create_date, '%y-%m-%d') = date_format(now(), '%y-%m-%d') and a.netbar_id=",
					netbarId, "))");
		} else {
			sql = SqlJoiner.join("select count(1) amount", " from user_r_redbag a, sys_t_redbag b",
					" where a.user_id = :userId and a.redbag_id = b.id and a.is_valid = 1",
					"    and a.create_date <= now() and now() <= date_add(a.create_date, interval b.day day)");
		}
		Map<String, Object> result = queryDao.querySingleMap(sql, params);
		Number amount = (Number) result.get("amount");
		return amount.intValue();
	}

	/**
	 * 统计周红包的发放或使用情况
	 */
	public Map<String, Object> statisRecentWeeklyRedbag(String beginDate, String endDate, String group,
			boolean onlyUsed) {
		String sql = genStatisRecentWeeklyRedbag(beginDate, endDate, group, onlyUsed);
		List<Map<String, Object>> statis = queryDao.queryMap(sql);

		// 行列转换
		Map<String, Object> dateStatis = new HashMap<String, Object>();
		if (CollectionUtils.isNotEmpty(statis)) {
			for (Map<String, Object> s : statis) {
				Object oDate = s.get("date");
				String date = null;
				if (oDate instanceof Date) {
					date = DateUtils.dateToString((Date) s.get("date"), DateUtils.YYYY_MM_DD);
				} else {
					date = (String) oDate;
				}
				Map<String, Object> ns = new HashMap<String, Object>();
				ns.put("count", s.get("count"));
				ns.put("amount", s.get("amount"));
				dateStatis.put(date, ns);
			}
		}
		return dateStatis;
	}

	/**
	 * 产生统计周红包的sql
	 */
	private String genStatisRecentWeeklyRedbag(String beginDate, String endDate, String group, boolean onlyUsed) {
		String dateFormat = null;
		String dateUnit = null;
		if ("2".equals(group)) {
			dateFormat = "DATE_FORMAT(ur.create_date, '%Y-%m-01')";
			dateUnit = "MONTH";
		} else if ("3".equals(group)) {
			dateFormat = "DATE_FORMAT(ur.create_date, '%Y-01-01')";
			dateUnit = "YEAR";
		} else {
			dateFormat = "DATE(ur.create_date)";
			dateUnit = "DAY";
		}

		String used = "";
		if (onlyUsed) {
			used = " AND ur.is_valid = 0";
		}

		return SqlJoiner
				.join("SELECT * FROM (", " SELECT count(1) count, SUM(amount) amount, :dateFormat date",
						" FROM user_r_redbag ur LEFT JOIN sys_t_redbag sr ON ur.redbag_id = sr.id",
						" WHERE sr.type = 3 AND ur.create_date >= ':beginDate' AND ur.create_date < ADDDATE(':endDate', INTERVAL 1 :dateUnit) :used",
						" GROUP BY :dateFormat", " ORDER BY ur.create_date DESC", " ) a ORDER BY date ASC")
				.replaceAll(":dateFormat", dateFormat).replaceAll(":beginDate", beginDate)
				.replaceAll(":endDate", endDate).replaceAll(":used", used).replaceAll(":dateUnit", dateUnit);
	}

	/**
	 * 统计发放、使用的周红包
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> statisIssueAndUsedWeeklyRedbag(String beginDate, String endDate, String group) {
		// 初始化参数
		int field = Calendar.DAY_OF_YEAR;
		if ("2".equals(group)) {
			field = Calendar.MONTH;
		} else if ("3".equals(group)) {
			field = Calendar.YEAR;
		}

		if (StringUtils.isBlank(beginDate) && StringUtils.isBlank(endDate)) {
			Date dEndDate = new Date();
			endDate = DateUtils.dateToString(dEndDate, DateUtils.YYYY_MM_DD);

			Calendar cEndDAte = Calendar.getInstance();
			cEndDAte.add(field, -7);
			beginDate = DateUtils.dateToString(cEndDAte.getTime(), DateUtils.YYYY_MM_DD);
		} else {
			try {
				if (StringUtils.isBlank(beginDate)) {
					Date dBeginDate = DateUtils.stringToDate(endDate, DateUtils.YYYY_MM_DD);
					Calendar c = Calendar.getInstance();
					c.setTime(dBeginDate);
					c.add(field, -7);
					beginDate = DateUtils.dateToString(c.getTime(), DateUtils.YYYY_MM_DD);
				}
				if (StringUtils.isBlank(endDate)) {
					Date dEndDate = DateUtils.stringToDate(beginDate, DateUtils.YYYY_MM_DD);
					Calendar c = Calendar.getInstance();
					c.setTime(dEndDate);
					c.add(field, 7);
					endDate = DateUtils.dateToString(c.getTime(), DateUtils.YYYY_MM_DD);
				}
			} catch (ParseException e) {
				LOGGER.error("格式化时间异常:", e);
			}
		}

		// 检查时间间隔，选择合适的分组
		Date bgDate = null;
		Date edDate = null;
		try {
			bgDate = DateUtils.stringToDate(beginDate, DateUtils.YYYY_MM_DD);
			edDate = DateUtils.stringToDate(endDate, DateUtils.YYYY_MM_DD);
			long gap = edDate.getTime() - bgDate.getTime();
			long yearGap = 12L * 30L * 24L * 60L * 60L * 1000;
			long monthGap = 31L * 24L * 60L * 60L * 1000L;
			if (gap > yearGap) {// 时间差大于12个月，采用年分组
				group = "3";
				field = Calendar.YEAR;
			} else if (gap > monthGap) {// 时间差小于12个月，大于30天，采用月分组
				group = "2";
				field = Calendar.MONTH;
			} else {
				group = "1";
				field = Calendar.DAY_OF_MONTH;
			}
		} catch (ParseException e) {
			LOGGER.error("格式化时间异常:", e);
		}

		// 统计数据
		Map<String, Object> issueStatis = statisRecentWeeklyRedbag(beginDate, endDate, group, false);
		Map<String, Object> usedStatis = statisRecentWeeklyRedbag(beginDate, endDate, group, true);
		Calendar c = Calendar.getInstance();
		c.setTime(bgDate);

		List<String> dates = new ArrayList<String>();
		List<Object> issuesCount = new ArrayList<Object>();
		List<Object> issuesAmount = new ArrayList<Object>();
		List<Object> usedsCount = new ArrayList<Object>();
		List<Object> usedsAmount = new ArrayList<Object>();

		// 重新组装数据
		if ("2".equals(group)) {
			c.set(Calendar.DAY_OF_MONTH, 1);
			Calendar edCalendar = Calendar.getInstance();
			edCalendar.setTime(edDate);
			edCalendar.set(Calendar.DAY_OF_MONTH, 2);
			edDate = edCalendar.getTime();
		} else if ("3".equals(group)) {
			c.set(Calendar.MONTH, 1);
			c.set(Calendar.DAY_OF_YEAR, 1);
			Calendar edCalendar = Calendar.getInstance();
			edCalendar.setTime(edDate);
			edCalendar.set(Calendar.MONTH, 1);
			edCalendar.set(Calendar.DAY_OF_YEAR, 1);
			edDate = edCalendar.getTime();
		}

		int i = 0;
		while (c.getTime().compareTo(edDate) <= 0 && i < 30) {// 最多显示30个点
			String date = null;
			if ("2".equals(group)) {
				date = DateUtils.dateToString(c.getTime(), "MM");
				dates.add(date + "月");
			} else if ("3".equals(group)) {
				date = DateUtils.dateToString(c.getTime(), "yyyy");
				dates.add(date + "年");
			} else {
				date = DateUtils.dateToString(c.getTime(), DateUtils.YYYY_MM_DD);
				dates.add(date);
			}

			Map<String, Object> iStatis = (Map<String, Object>) issueStatis
					.get(DateUtils.dateToString(c.getTime(), DateUtils.YYYY_MM_DD));
			Number iCount = null;
			Number iAmount = null;
			if (iStatis != null) {
				iCount = (Number) iStatis.get("count");
				iAmount = (Number) iStatis.get("amount");
			}
			if (iCount == null) {
				iCount = 0;
			}
			if (iAmount == null) {
				iAmount = 0;
			}
			issuesCount.add(iCount);
			issuesAmount.add(iAmount);

			Map<String, Object> uStatis = (Map<String, Object>) usedStatis.get(date);
			Number uCount = null;
			Number uAmount = null;
			if (uStatis != null) {
				uCount = (Number) uStatis.get("count");
				uAmount = (Number) uStatis.get("amount");
			}
			if (uCount == null) {
				uCount = 0;
			}
			if (uAmount == null) {
				uAmount = 0;
			}
			usedsCount.add(uCount);
			usedsAmount.add(uAmount);

			c.add(field, 1);
			i += 1;
		}

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("dates", dates);
		result.put("issuesCount", issuesCount);
		result.put("issuesAmount", issuesAmount);
		result.put("usedsCount", usedsCount);
		result.put("usedsAmount", usedsAmount);
		result.put("beginDate", beginDate);
		result.put("endDate", endDate);
		result.put("group", group);

		return result;
	}

	/**
	 * 统计全部周红包的抢、使用情况
	 */
	public Map<String, Object> statisAllRedbag() {
		String sql = SqlJoiner.join("SELECT * FROM (",
				" SELECT IF ( ISNULL(count(1)), 0, count(1) ) weeklyCount, IF ( isnull(SUM(amount)), 0, SUM(amount) ) weeklyAmount",
				" FROM user_r_redbag ur LEFT JOIN sys_t_redbag sr ON ur.redbag_id = sr.id WHERE sr.type = 3",
				" ) issue LEFT JOIN (",
				" SELECT IF ( ISNULL(count(1)), 0, count(1) ) weeklyUsedCount, IF ( isnull(SUM(amount)), 0, SUM(amount) ) weeklyUsedAmount",
				" FROM user_r_redbag ur LEFT JOIN sys_t_redbag sr ON ur.redbag_id = sr.id WHERE sr.type = 3 AND ur.is_valid = 0",
				" ) used ON 1 = 1 LEFT JOIN (",
				" SELECT IF ( ISNULL(count(1)), 0, count(1) ) overdueWeeklyCount, IF ( isnull(SUM(amount)), 0, SUM(amount) ) overdueWeeklyAmount",
				" FROM user_r_redbag ur LEFT JOIN sys_t_redbag sr ON ur.redbag_id = sr.id",
				" WHERE sr.type = 3 AND ADDDATE( sr.create_date, INTERVAL sr.day DAY ) < now() AND ur.is_valid = 1",
				" ) overdue ON 1 = 1 LEFT JOIN (",
				" SELECT IF ( ISNULL(count(1)), 0, count(1) ) allCount, IF ( isnull(SUM(amount)), 0, SUM(amount) ) allAmount FROM user_r_redbag ur LEFT JOIN sys_t_redbag sr ON ur.redbag_id = sr.id",
				" ) survey ON 1 = 1 LIMIT 1");
		return queryDao.querySingleMap(sql);
	}

	/**
	 * 统计分享红包的发放情况
	 */
	public List<Map<String, Object>> statisShareRedbag(Integer valid, Integer count) {
		if (count == null) {// 默认查最近7条数据
			count = 7;
		}

		String sql = SqlJoiner.join("SELECT count(1) count, sum(ur.amount) amount, date(ur.create_date) date",
				" FROM user_r_redbag ur WHERE", " ur.redbag_id IN ( SELECT id FROM sys_t_redbag sr WHERE sr.type = 4 )",
				valid != null ? " AND ur.is_valid = " + valid : "",
				" GROUP BY date(ur.create_date) ORDER BY ur.create_date DESC LIMIT 0, ", count.toString());

		return queryDao.queryMap(sql);
	}

	/**周红包统计
	 * @param week
	 * @return
	 */
	public List<Map<String, Object>> weekRedBag(Integer week) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("start", 0);
		params.put("end", week);
		String limitSql = " limit :start,:end";
		String sql = SqlJoiner.join(
				"select date_format(begin_time, '%Y-%m-%d') date,num,money,used_num,used_money from ",
				"(select a.redbag_id,count(1) num,sum(amount) money ", "from user_r_redbag a ", "where a.redbag_id in(",
				"select id from sys_t_redbag b ", "where b.type=3 and b.is_valid=1) ",
				"group by a.redbag_id)a LEFT JOIN ",
				"(select a.redbag_id,count(1) used_num,sum(amount) used_money from user_r_redbag a ",
				"where a.redbag_id in(select id from sys_t_redbag b where b.type=3 and b.is_valid=1) and a.is_valid=0 group by a.redbag_id)b on a.redbag_id=b.redbag_id left JOIN( select id,begin_time from sys_t_redbag)c on a.redbag_id=c.id order by a.redbag_id desc",
				limitSql);
		return queryDao.queryMap(sql, params);
	}

	public boolean isRedbagNeedValidateUserPhone(String rid) {
		String sql = "select count(1) from user_r_redbag where id = " + rid + " and redbag_id =3";
		Number query = queryDao.query(sql);
		return query != null && query.intValue() > 0;
	}

	public boolean regTypeRedbagValidate(Long userId, String smsCode) {
		UserInfo user = userInfoService.findById(userId);
		String mobile = user.getUsername();
		return smsVerifyService.verify(SMSVerifyService.SMS_CODE_API_USE_REDBAG, mobile, smsCode);

	}

	/*
	 * 	清空黑名单用户兑换的红包
	 */
	public void clearExchangeRedbags(String ids) {
		long redbagId = systemRedbagService.querySystemRedbagId(RedbagConstant.REDBAG_TYPE_MALL);
		String sql = SqlJoiner.join("UPDATE user_r_redbag SET is_valid=", CommonConstant.INT_BOOLEAN_FALSE.toString(),
				" WHERE redbag_id=", String.valueOf(redbagId), " AND user_id IN(", ids, ")");
		queryDao.update(sql);
	}

	/**发放网吧专属红包
	 * @param userId
	 * @param netbarId
	 * @return
	 */
	public Map<String, Object> sendExclusiveRedbag(String userId, String netbarId) {
		Map<String, Object> result = new HashMap<String, Object>();
		String sql = "SELECT if ( a.buy_num - if (used_num is null, 0, used_num) > 0, 1, 0 ) has_left,a.trade_no, a.buy_num - IF ( z.used_num IS NULL, 0, used_num ) left_num,if(date_format(date_add(a.create_date, INTERVAL 1 day), '%Y-%m-%d')<now(),1,0) is_start, a.id, c.redbag_id, c.conditions FROM ( netbar_resource_order a, netbar_resource_commodity b, netbar_resource_commodity_property c, netbar_commodity_category d ) LEFT JOIN ( SELECT e.trade_no, count(e.redbag_id) used_num FROM user_r_redbag e, sys_t_redbag f WHERE e.netbar_id ="
				+ netbarId
				+ " AND e.redbag_id = f.id AND f.type = 8 GROUP BY e.trade_no ) z ON a.trade_no = z.trade_no WHERE a.netbar_id ="
				+ netbarId
				+ " AND a. STATUS = 3 AND a.is_valid = 1 AND a.commodity_id = b.id AND a.property_id = c.id AND b.category_id = d.id AND d. NAME = '红包' AND c.validity IS NOT NULL AND now() > date_format(a.create_date, '%Y-%m-%d') AND now() < date_format( date_add( a.create_date, INTERVAL 1 + c.validity DAY ), '%Y-%m-%d' ) ORDER BY has_left desc,is_start desc,a.create_date desc LIMIT 1";
		Map<String, Object> m = queryDao.querySingleMap(sql);
		if (m == null) {
			result.put("code", 1);
			return result;
		}
		String redbagId = String.valueOf(m.get("redbag_id"));
		String tradeNo = String.valueOf(m.get("trade_no"));
		if (redbagId == null || tradeNo == null) {
			result.put("code", 1);
			return result;
		} else {
			sql = SqlJoiner.join("select count(1) from user_r_redbag where user_id=", userId, " and redbag_id=",
					redbagId, " and netbar_id=", netbarId, " and trade_no='", tradeNo,
					"' and date_format(create_date,'%Y-%m-%d')=date_format(now(),'%Y-%m-%d')");
			Number n = queryDao.query(sql);
			if (n.intValue() > 0) {
				result.put("code", 3);
				return result;
			}
			if (((Number) m.get("is_start")).intValue() == 0) {
				result.put("code", 4);
				return result;
			}

			RedisAtomicInteger leftNum = new RedisAtomicInteger(CommonConstant.MERCHANT_REDBAG_LEFT_NUM + tradeNo,
					redisConnectionFactory);
			if (leftNum.get() == -1 || leftNum.get() == 0 && ((Number) m.get("left_num")).intValue() <= 0) {
				result.put("code", 2);
				return result;
			}
			if (leftNum.get() > 0) {
				leftNum.set(leftNum.get() - 1 == 0 ? -1 : leftNum.get() - 1);//库存消耗到0时设为-1,用以区分不存在的key返回的0
			}
			SystemRedbag systemRedbag = systemRedbagService.findOne(NumberUtils.toLong(redbagId));
			UserRedbag userRedbag = new UserRedbag();
			userRedbag.setUserId(NumberUtils.toLong(userId));
			userRedbag.setRedbagId(NumberUtils.toLong(redbagId));
			userRedbag.setUsable(1);
			userRedbag.setAmount(systemRedbag.getMoney());
			userRedbag.setNetbarType(2);
			userRedbag.setNetbarId(NumberUtils.toLong(netbarId));
			userRedbag.setValid(1);
			userRedbag.setCreateDate(new Date());
			if (m.get("conditions") != null) {
				userRedbag.setLimitMinMoney(((Number) m.get("conditions")).intValue());
			}
			userRedbag.setTradeNo((String) m.get("trade_no"));
			userRedbagDao.save(userRedbag);
			result.put("id", userRedbag.getId());
			result.put("money", userRedbag.getAmount());
			result.put("min_money", userRedbag.getLimitMinMoney());
			result.put("end_date", DateUtils.dateToString(new Date(), DateUtils.YYYY_MM_DD));
			return result;
		}
	}

	/**发放增值券
	 * @param userId
	 * @param netbarId
	 * @return
	 */
	public Map<String, Object> sendCard(String userId, String netbarId) {
		Map<String, Object> result = new HashMap<String, Object>();
		String sql = "SELECT if ( a.buy_num - if (used_num is null, 0, used_num) > 0, 1, 0 ) has_left, a.trade_no, a.buy_num - IF ( z.used_num IS NULL, 0, used_num ) left_num, if ( date_format( date_add(a.create_date, INTERVAL 1 day), '%Y-%m-%d' ) < now(), 1, 0 ) is_start, a.id, c.redbag_id card_id, c.conditions FROM ( netbar_resource_order a, netbar_resource_commodity b, netbar_resource_commodity_property c, netbar_commodity_category d ) LEFT JOIN ( SELECT e.trade_no, count(e.value_add_card_id) used_num FROM user_value_added_card e, sys_value_added_card f WHERE e.netbar_id ="
				+ netbarId
				+ " AND e.value_add_card_id = f.id and ((e.is_valid=0 or e.is_valid=-1) or (e.is_valid=1 and now()<e.expire_date)) GROUP BY e.trade_no ) z ON a.trade_no = z.trade_no WHERE a.netbar_id ="
				+ netbarId
				+ " AND a. STATUS = 3 AND a.is_valid = 1 AND a.commodity_id = b.id AND a.property_id = c.id AND b.category_id = d.id AND d. NAME = '增值券' AND c.validity IS NOT NULL AND now() > date_format(a.create_date, '%Y-%m-%d') AND now() < date_format(a.expire_date, '%Y-%m-%d') ORDER BY has_left desc, is_start desc, a.create_date desc LIMIT 1";
		Map<String, Object> m = queryDao.querySingleMap(sql);
		if (m == null) {
			result.put("code", 1);
			return result;
		}
		String cardId = String.valueOf(m.get("card_id"));
		String tradeNo = String.valueOf(m.get("trade_no"));
		if (cardId == null || tradeNo == null) {
			result.put("code", 1);
			return result;
		} else {
			sql = SqlJoiner.join("select count(1) from user_value_added_card where user_id=", userId,
					" and value_add_card_id=", cardId, " and netbar_id=", netbarId, " and trade_no='", tradeNo,
					"' and date_format(create_date,'%Y-%m-%d')=date_format(now(),'%Y-%m-%d')");
			Number n = queryDao.query(sql);
			if (n.intValue() > 0) {
				result.put("code", 3);
				return result;
			}
			if (((Number) m.get("is_start")).intValue() == 0) {
				result.put("code", 4);
				return result;
			}

			RedisAtomicInteger leftNum = new RedisAtomicInteger(CommonConstant.MERCHANT_REDBAG_LEFT_NUM + tradeNo,
					redisConnectionFactory);
			if (leftNum.get() == -1 || leftNum.get() == 0 && ((Number) m.get("left_num")).intValue() <= 0) {
				result.put("code", 2);
				return result;
			}
			if (leftNum.get() > 0) {
				leftNum.set(leftNum.get() - 1 == 0 ? -1 : leftNum.get() - 1);//库存消耗到0时设为-1,用以区分不存在的key返回的0
			}
			SysValueAddedCard sysValueAddedCard = sysValueAddedCardService.findOne(NumberUtils.toLong(cardId));
			UserValueAddedCard userValueAddedCard = new UserValueAddedCard();
			userValueAddedCard.setUserId(NumberUtils.toLong(userId));
			userValueAddedCard.setValueAddCardId(NumberUtils.toLong(cardId));
			userValueAddedCard.setUsable(1);
			userValueAddedCard.setAmount(sysValueAddedCard.getAmount());
			userValueAddedCard.setNetbarId(NumberUtils.toLong(netbarId));
			userValueAddedCard.setValid(1);
			userValueAddedCard.setCreateDate(new Date());
			try {
				userValueAddedCard.setExpireDate(DateUtils.stringToDate(
						DateUtils.dateToString(DateUtils.addDays(new Date(), 1), DateUtils.YYYY_MM_DD),
						DateUtils.YYYY_MM_DD));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			if (m.get("conditions") != null) {
				userValueAddedCard.setLimitMinMoney(((Number) m.get("conditions")).intValue());
			}
			userValueAddedCard.setTradeNo((String) m.get("trade_no"));
			userValueAddedCardDao.save(userValueAddedCard);
			result.put("id", userValueAddedCard.getId());
			result.put("money", userValueAddedCard.getAmount());
			result.put("min_money", userValueAddedCard.getLimitMinMoney());
			result.put("end_date", userValueAddedCard.getExpireDate());
			return result;
		}
	}

	/**
	 * 激活赛事红包
	 */
	public void activateRedbag(Long userId) {
		SystemRedbag systemRedbag = systemRedbagService.findOne(53L);
		List<UserRedbag> list = userRedbagDao.findByRedbagIdAndUserId(53L, userId);
		if (CollectionUtils.isNotEmpty(list)) {
			UserRedbag userRedbag = list.get(0);
			if (userRedbag.getValid() == 1 && userRedbag.getUsable() == 0 && DateUtils
					.addDays(userRedbag.getCreateDate(), systemRedbag.getDay())
					.getTime() > System.currentTimeMillis()) {//激活未过期的赛事红包
				userRedbag.setUsable(1);
				userRedbagDao.save(userRedbag);
			}
		}
	}

	public UserRedbag findOne(Long id) {
		return userRedbagDao.findOne(id);
	}

	public Map<String, Object> leftTotal(Long userId) {
		Map<String, Object> result = new HashMap<String, Object>();
		Number number = queryDao
				.query("select sum(total) from ( select count(1) total from user_r_redbag a, sys_t_redbag b where a.user_id ="
						+ userId
						+ " and a.redbag_id = b.id and b.type <> 8 and b.type <> 10 and a.is_valid = 1 and a.create_date <= now() and now() <= date_add( a.create_date, interval b. day day ) union all select count(1) total from user_value_added_card a where a.is_valid = 1 and a.usable = 1 and user_id ="
						+ userId + " and now() < a.expire_date ) a");
		if (number == null) {
			result.put("leftTotal", 0);
		} else {
			result.put("leftTotal", number.intValue());
		}
		return result;
	}

	/**增值券使用校验
	 * @param userId
	 * @param cardId
	 * @param netbarId
	 * @param origAmount
	 * @return
	 */
	public Map<String, Integer> payWithCardCheck(Long userId, Long cardId, String deviceId, Long netbarId,
			double origAmount, double amount) {
		Map<String, Integer> result = new HashMap<String, Integer>();
		Map<String, Object> map = queryDao.querySingleMap(
				"select id,amount from user_value_added_card a where a.is_valid=1 and now()<a.expire_date and usable=1 and a.user_id="
						+ userId + " and id=" + cardId + "  and netbar_id=" + netbarId
						+ " and if(limit_min_money is null,0,limit_min_money)<=" + origAmount);
		if (map == null || map.isEmpty()) {
			result.put("code", 1);
			return result;//增值券不存在或不满足使用条件
		}
		if (cardLimit(joiner.join(USE_REDBAG_DAY_LIMIT_PREFIX, DateUtils.dateToString(new Date(), DateUtils.YYYY_MM_DD),
				userId.toString()))) {
			result.put("code", 2);
			return result;//该账号已经超过每日使用2个增值券数量上限.
		}
		if (cardLimit(joiner.join(DEVICE_USE_REDBAG_DAY_LIMIT_PREFIX,
				DateUtils.dateToString(new Date(), DateUtils.YYYY_MM_DD), deviceId.toString()))) {
			result.put("code", 3);
			return result;//一台设备每天只能使用2次增值券哦.
		}
		int cardAmount = ((Number) map.get("amount")).intValue();
		if (amount != origAmount + cardAmount) {
			result.put("code", 4);
			return result;//增值券的金额不对
		}
		result.put("code", 0);
		result.put("cardAmount", ((Number) map.get("amount")).intValue());
		return result;
	}

	/**更新增值券状态
	 * @param cardId
	 * @param userId
	 */
	public void updateCardState(Long cardId, String deviceId, Long userId) {
		queryDao.update("update user_value_added_card set usable=0,is_valid=-1 where id=" + cardId);
		updateCardLimit(joiner.join(USE_REDBAG_DAY_LIMIT_PREFIX,
				DateUtils.dateToString(new Date(), DateUtils.YYYY_MM_DD), userId.toString()));
		updateCardLimit(joiner.join(DEVICE_USE_REDBAG_DAY_LIMIT_PREFIX,
				DateUtils.dateToString(new Date(), DateUtils.YYYY_MM_DD), deviceId));
	}

	/**检查增值券每日使用限制
	 * @param key
	 * @param cardId
	 * @param userId
	 * @return
	 */
	public boolean cardLimit(String key) {
		RedisAtomicInteger usedCount = new RedisAtomicInteger(key,
				redisOperateService.getRedisTemplate().getConnectionFactory());
		int usedNum = usedCount.get();
		if (usedNum >= 2) {
			return true;
		}
		return false;
	}

	/**更新增值券每日使用限制
	 * @param key
	 * @param cardId
	 * @param userId
	 */
	public void updateCardLimit(String key) {
		RedisAtomicInteger value = new RedisAtomicInteger(key,
				redisOperateService.getRedisTemplate().getConnectionFactory());
		value.incrementAndGet();
	}

	/**支付之后更新增值券状态
	 * @param rids
	 * @param userId
	 */
	public void updateCardStatePayed(Long cardId, Long userId) {
		if (cardId != null) {
			try {
				String sql = SqlJoiner.join("update user_value_added_card set is_valid=0,usable=1 where id=" + cardId);
				queryDao.update(sql);
			} catch (Exception e) {
				LOGGER.error("设置增值券已使用状态异常,红包id:[{}]", cardId);
			}
		}
	}

}
