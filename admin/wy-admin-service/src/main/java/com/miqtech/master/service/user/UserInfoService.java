package com.miqtech.master.service.user;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.persistence.criteria.Predicate;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.mall.CoinConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.mall.MallInviteDao;
import com.miqtech.master.dao.user.ThirdPartyLoginDao;
import com.miqtech.master.dao.user.UserInfoDao;
import com.miqtech.master.entity.mall.MallInvite;
import com.miqtech.master.entity.user.ThirdPartyLogin;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.entity.user.UserLoginLog;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.service.mall.CoinHistoryService;
import com.miqtech.master.service.mall.MallInviteService;
import com.miqtech.master.thirdparty.util.UpYunUploaderUtils;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.EncodeUtils;
import com.miqtech.master.utils.IdentityUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;

/**
 * 用户信息相关操作service
 */
@Component
public class UserInfoService {

	private static final String REDIS_USER_TOKEN = "wy_api_user_token_";

	private static ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("发送红包线程池").build();
	private final static ScheduledExecutorService scheduledExecutor = Executors
			.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2, threadFactory);
	private static final Logger LOGGER = LoggerFactory.getLogger(UserInfoService.class);

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private UserInfoDao userInfoDao;
	@Autowired
	private UserLoginLogService userLoginLogService;
	@Autowired
	private MallInviteService mallInviteService;
	@Autowired
	private CoinHistoryService coinHistoryService;
	@Autowired
	private MallInviteDao mallInviteDao;
	@Autowired
	private StringRedisOperateService stringRedisOperateService;
	@Autowired
	private ThirdPartyLoginDao thirdPartyLoginDao;

	public UserInfo save(UserInfo user) {
		return userInfoDao.save(user);
	}

	public List<UserInfo> save(List<UserInfo> users) {
		return (List<UserInfo>) userInfoDao.save(users);
	}

	public void delete(long userId) {
		UserInfo user = userInfoDao.findOne(userId);
		if (user != null) {
			user.setValid(CommonConstant.INT_BOOLEAN_FALSE);
			userInfoDao.save(user);
		}
	}

	public UserInfo findById(Long userId) {
		return userInfoDao.findOne(userId);
	}

	public UserInfo findByNickname(String nickname) {
		List<UserInfo> list = userInfoDao.findByNickname(nickname);
		if (CollectionUtils.isNotEmpty(list)) {
			return list.get(0);
		}
		return null;
	}

	public UserInfo findByTelephoneAndValid(String phone, int valid) {
		return userInfoDao.findByTelephoneAndValid(phone, valid);
	}

	public UserInfo queryByNameAndPwd(String username, String password) {
		return userInfoDao.findByUsernameAndPasswordAndValid(username, password, 1);
	}

	public UserInfo queryByName(String username) {
		return userInfoDao.findByUsernameAndValid(username, 1);
	}

	public UserInfo queryAllByName(String username) {
		return userInfoDao.findByUsername(username);
	}

	public UserInfo saveOrUpdate(UserInfo userInfo) {
		registerGetCoin(userInfo.getUsername());
		if (userInfo.getSex() == null) {
			userInfo.setSex(0);
		}
		return userInfoDao.save(userInfo);
	}

	/**
	 * 分页排序
	 */
	private PageRequest buildPageRequest(int pageNumber) {
		return new PageRequest(pageNumber - 1, PageUtils.ADMIN_DEFAULT_PAGE_SIZE,
				new Sort(Direction.DESC, "createDate"));
	}

	/**
	 * 分页查询条件
	 */
	private Specification<UserInfo> buildSpecification(final Map<String, Object> searchParams) {
		Specification<UserInfo> spec = (root, query, cb) -> {
			List<Predicate> ps = Lists.newArrayList();

			Set<String> keys = searchParams.keySet();
			for (String key : keys) {
				String value = String.valueOf(searchParams.get(key));
				try {
					if ("beginDate".equals(key)) {
						Predicate isReleasePredicate1 = cb.greaterThanOrEqualTo(root.get("createDate").as(String.class),
								String.valueOf(value));
						ps.add(isReleasePredicate1);
					} else if ("endDate".equals(key)) {
						Date endDate = DateUtils.stringToDate(String.valueOf(value), DateUtils.YYYY_MM_DD);
						endDate = DateUtils.addDays(endDate, 1);
						Predicate isReleasePredicate2 = cb.lessThan(root.get("createDate").as(String.class),
								DateUtils.dateToString(endDate, DateUtils.YYYY_MM_DD));
						ps.add(isReleasePredicate2);
					} else {
						Predicate isReleasePredicate3 = cb.like(root.get(key).as(String.class),
								SqlJoiner.join("%", value, "%"));
						ps.add(isReleasePredicate3);
					}
				} catch (Exception e) {
					LOGGER.error("添加查询条件异常：", e);
				}
			}

			query.where(cb.and(ps.toArray(new Predicate[ps.size()])));
			return query.getRestriction();
		};
		return spec;
	}

	/**
	 * 分页查询
	 */
	public Page<UserInfo> page(int page, Map<String, Object> params) {
		if (params.get("valid") == null) {
			params.put("valid", CommonConstant.INT_BOOLEAN_TRUE);
		}
		return userInfoDao.findAll(buildSpecification(params), buildPageRequest(page));
	}

	/**
	 * 后台管理：用户列表，分页--sql
	 */
	public List<Map<String, Object>> pageList(int page, Map<String, Object> params) {
		if (page < 1) {
			page = 1;
		}
		int rows = PageUtils.ADMIN_DEFAULT_PAGE_SIZE;
		int start = (page - 1) * rows;
		if (null == params.get("valid")) {
			params.put("valid", CommonConstant.INT_BOOLEAN_TRUE);
		}
		String sql = "select  id,  username,  telephone,  nickname, is_valid valid,icon   from user_t_info  where is_valid=:valid";
		params.put("start", start);
		params.put("rows", rows);
		sql = SqlJoiner.join(sql, " order by id limit :start, :rows");
		List<Map<String, Object>> list = queryDao.queryMap(sql, params);

		return list;
	}

	public UserInfo registerAndBindCode(String mobile, String nickname, String password, String invitationCode,
			String channelName, String deviceId) {
		registerGetCoin(mobile);
		return registerAndBindCode(mobile, nickname, password, invitationCode, null, channelName, deviceId);
	}

	public UserInfo registerAndBindCode(String mobile, String nickname, String password, String invitationCode,
			Integer isReserve, String channelName, String deviceId) {
		if (isReserve == null || !CommonConstant.INT_BOOLEAN_TRUE.equals(isReserve)) {
			isReserve = CommonConstant.INT_BOOLEAN_FALSE;
		}

		UserInfo userInfo = new UserInfo();
		userInfo.setUsername(mobile);
		userInfo.setTelephone(mobile);
		userInfo.setPassword(EncodeUtils.base64Md5(password));
		nickname = StringUtils.isNotBlank(nickname) ? nickname
				: StringUtils.replace(mobile, StringUtils.substring(mobile, 3, 7), "****");
		userInfo.setNickname(nickname);
		userInfo.setValid(1);
		userInfo.setCreateDate(new Date());
		if (StringUtils.isNotBlank(channelName)) {
			userInfo.setAndroidChannelName(channelName);
		}
		if (StringUtils.isNotBlank(deviceId)) {
			userInfo.setDeviceId(deviceId);
		}
		String headUrl = "uploads/imgs/user/random/" + (new Random().nextInt(97) + 1) + ".jpg";
		userInfo.setIcon(headUrl);
		userInfo.setIconMedia(headUrl);
		userInfo.setIconThumb(headUrl);
		userInfo.setIsReserve(isReserve);
		userInfo.setCoin(0);
		if (StringUtils.isNotBlank(invitationCode)) {
			userInfo.setInvitationCode(invitationCode);
		}
		userInfo.setUpdateDate(new Date());
		userInfo = userInfoDao.save(userInfo);
		giveScoreAndRedbag(userInfo);
		return userInfo;
	}

	/*积分记录和首次登陆发放红包*/
	private void giveScoreAndRedbag(final UserInfo user) {
		scheduledExecutor.schedule(() -> {
			try {
				giveRedbag(user.getId().longValue());
			} catch (Exception e) {
				LOGGER.error("[Manual-Operation]延迟8秒发送红包异常:", e);
			}
		}, 8, TimeUnit.SECONDS);//解决客户端注册完成后直接登陆,不发放红包和积分

	}

	/* 发送红包 */
	public void giveRedbag(Long userId) {
		UserLoginLog userLoginLog = userLoginLogService.findByUserId(userId);
		if (userLoginLog == null || userLoginLog.getIsSendRedbag() == null
				|| CommonConstant.INT_BOOLEAN_FALSE.equals(userLoginLog.getIsSendRedbag())) {
			if (null != userLoginLog) {
				userLoginLog.setIsSendRedbag(1);
				userLoginLog.setUpdateDate(new Date());
				userLoginLogService.save(userLoginLog);
			} else {
				userLoginLog = new UserLoginLog();
				userLoginLog.setIsSendRedbag(1);
				userLoginLog.setUserId(userId);
				userLoginLog.setCombo(0L);
				userLoginLog.setValid(1);
				userLoginLog.setCreateDate(new Date());
				userLoginLog.setUpdateDate(new Date());
				userLoginLog = userLoginLogService.save(userLoginLog);
			}
			//取消注册红包发放
			//			Date now = new Date();
			//			Date _20160801 = null;
			//			try {
			//				_20160801 = DateUtils.stringToDateYyyyMMddhhmmss("2016-08-01 00:00:00");
			//			} catch (ParseException e) {
			//				e.printStackTrace();
			//			}
			//			if (now.before(_20160801)) {
			//				userRedbagService.firstLoginRedbag(userId);
			//			}
		}
	}

	/**
	 * 统计网吧邀请注册数据
	 */
	public Map<String, Object> statisticInvotation(Long netbarId, Date startDate, Date endDate) {
		String andDateSql = StringUtils.EMPTY;
		if (null != startDate && null != endDate) {
			andDateSql = " and update_date >= '" + DateUtils.dateToString(startDate, "yyyy-MM-dd 00:00:00")
					+ "' and update_date<='" + DateUtils.dateToString(endDate, "yyyy-MM-dd 23:59:59") + "'";
		}
		/*统计按网吧员工统计*/
		Map<String, Object> result = new HashMap<String, Object>();
		String statisticSql = " select count(u.id) countNum, u.invitation_code invitationCode, t.name name from user_t_info u,"
				+ " (select invitation_code, name from netbar_t_info where id = " + netbarId
				+ " union select invitation_code, name from netbar_t_staff where netbar_id = " + netbarId + ") t "
				+ " where u.invitation_code = t.invitation_code " + andDateSql + " group by u.invitation_code ";
		result.put("operates", queryDao.queryMap(statisticSql));
		/*计算总数*/
		String countSql = " select     count(u.id) from    user_t_info u "
				+ " where u.invitation_code in     (select        invitation_code    from       netbar_t_info     where id = "
				+ netbarId
				+ "    union    select        invitation_code    from        netbar_t_staff    where netbar_id = "
				+ netbarId + " )   " + andDateSql;
		result.put("count", queryDao.query(countSql));
		return result;
	}

	/**
	 * 统计网吧邀请注册数据
	 */
	public Map<String, Object> statisticStaffInvotation(Long staffId, Long netbarId, Date startDate, Date endDate) {
		String andDateSql = StringUtils.EMPTY;
		if (null != startDate && null != endDate) {
			andDateSql = " and update_date >= '" + DateUtils.dateToString(startDate, "yyyy-MM-dd 00:00:00")
					+ "' and update_date<='" + DateUtils.dateToString(endDate, "yyyy-MM-dd 23:59:59") + "'";
		}
		/*统计按网吧员工统计*/
		Map<String, Object> result = new HashMap<String, Object>();
		String statisticSql = " select count(u.id) countNum, u.invitation_code invitationCode, t.name name from user_t_info u,"
				+ " (select invitation_code, name from netbar_t_staff where id=" + staffId + ") t "
				+ " where u.invitation_code = t.invitation_code " + andDateSql + " group by u.invitation_code ";
		result.put("operates", queryDao.queryMap(statisticSql));
		/*计算总数*/
		String countSql = " select count(u.id) from user_t_info u "
				+ " where u.invitation_code in ( select invitation_code from netbar_t_staff where id=" + staffId + " ) "
				+ andDateSql;
		result.put("count", queryDao.query(countSql));
		return result;
	}

	/**
	 * 统计时间段内注册用户数量
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 */
	public List<Map<String, Object>> statisticRegisterNum(String startDate, String endDate) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		String sql = "select DATE_FORMAT(create_date, '%Y-%m-%d') create_date,count(1) num from user_t_info where is_valid=1 and create_date>=:startDate and create_date<=:endDate group by DATE_FORMAT(create_date, '%Y-%m-%d') order by create_date desc";
		return queryDao.queryMap(sql, params);
	}

	public List<Map<String, Object>> queryByUsernames(String userNames) {
		if (userNames.charAt(userNames.length() - 1) == ',') {
			userNames = userNames.substring(0, userNames.length() - 1);
		}
		String sql = SqlJoiner.join(
				"select * from (select a.id,a.username telephone,a.nickname,a.icon,b.labor from user_t_info a  left join activity_t_member b on a.id=b.user_id where a.username in (",
				userNames, ") order by b.create_date desc)a group by id limit 0,10");
		return queryDao.queryMap(sql);
	}

	/**
	 * 激活用户
	 */
	public void enabledUser(long userId) {
		UserInfo user = findById(userId);
		if (user != null && CommonConstant.INT_BOOLEAN_FALSE.equals(user.getValid())) {
			user.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			saveOrUpdate(user);
		}
	}

	private final static Joiner JOINER = Joiner.on("_");
	@Autowired
	private StringRedisOperateService redisOperateService;

	public boolean limitRegisterCoin(String mobile) {
		try {
			RedisConnectionFactory factory = redisOperateService.getRedisTemplate().getConnectionFactory();
			String key = JOINER.join("REGISTER_COIN_LIMIT_PREFIX", DateUtils.dateToString(new Date(), "yyyy-MM-dd"),
					mobile);
			RedisAtomicInteger registerCoinCount = new RedisAtomicInteger(key, factory);
			int andIncrement = registerCoinCount.incrementAndGet();
			return andIncrement > 5;
		} catch (Exception e) {
			LOGGER.error("获取用户[{}]每日邀请频次控制标识异常:", mobile, e);
		}
		return false;
	}

	/**邀请好友注册得金币
	 * @param mobile
	 */
	public void registerGetCoin(String mobile) {
		//如果该注册用户是被邀请用户,邀请用户增加金币
		MallInvite mallInvite = mallInviteService.findByInvitedTelephoneAndIsRegister(mobile, 0);
		if (mallInvite != null) {
			UserInfo userInfo = this.findById(mallInvite.getInviteUserId());
			if (userInfo.getValid() != 1) {
				return;
			}
			coinHistoryService.addGoldHistoryPub(mallInvite.getInviteUserId(), mallInvite.getId(),
					CoinConstant.HISTORY_TYPE_INVITATION, CoinConstant.INVITATION_COIN,
					CoinConstant.HISTORY_DIRECTION_INCOME);
			mallInvite.setIsRegister(1);
			mallInviteDao.save(mallInvite);
		}
	}

	/**
	 * 为用户增加金币
	 */
	public void addCoin(Long userId, Integer coin) {
		String sql = SqlJoiner.join("UPDATE user_t_info set coin = coin + ", coin.toString(), " WHERE id = ",
				userId.toString());
		queryDao.update(sql);
	}

	/**
	 * 通过ids查询用户
	 */
	public List<Map<String, Object>> findByIds(String ids) {
		if (StringUtils.isNotBlank(ids)) {
			String sql = SqlJoiner.join("SELECT * FROM user_t_info WHERE id in (", ids, ")");
			return queryDao.queryMap(sql);
		}
		return Lists.newArrayList();
	}

	/**
	 * 通过id列表查询有效用户
	 */
	public List<UserInfo> findValidByIds(List<Long> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return null;
		}
		return userInfoDao.findByIdInAndValid(ids, CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * 查询指定ID的用户
	 */
	public List<UserInfo> findByIdIn(List<Long> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return null;
		}
		return userInfoDao.findByIdIn(ids);
	}

	public List<UserInfo> findByValidAndUserNameIn(int valid, String[] phoneNumArray) {
		if (ArrayUtils.isNotEmpty(phoneNumArray)) {
			return userInfoDao.findByValidAndUsernameIn(valid, phoneNumArray);
		}
		return null;
	}

	/**
	 * 禁用用户
	 */
	public void disabledUser(Long id) {
		UserInfo user = findById(id);
		if (null != user && CommonConstant.INT_BOOLEAN_TRUE.equals(user.getValid())) {
			user.setValid(CommonConstant.INT_BOOLEAN_FALSE);
			save(user);
		}
	}

	/*
	 * 批量归零用户金币
	 */
	public void zeroCoinByUserIds(String ids) {
		String sql = SqlJoiner.join("UPDATE user_t_info SET coin = 0 WHERE id IN(", ids, ")");
		queryDao.update(sql);
	}

	/*
	 * 根据手机号查出用户id（逗号隔开）
	 */
	public String getUserIdsByTelephones(String telephones) {
		String userIds = StringUtils.EMPTY;
		String sql = SqlJoiner.join("SELECT group_concat(id) userIds FROM user_t_info WHERE is_valid=",
				CommonConstant.INT_BOOLEAN_TRUE.toString(), " AND username IN(", telephones, ")");
		Map<String, Object> map = queryDao.querySingleMap(sql);
		if (MapUtils.isNotEmpty(map)) {
			Object obj = map.get("userIds");
			userIds = null == obj ? "" : obj.toString();
		}
		return userIds;
	}

	/**
	 * 批量禁用用户,并将用户踢下线
	 */
	public void disabledAndOffline(Long id) {
		List<Long> ids = new ArrayList<Long>();
		ids.add(id);
		disabledAndOffline(ids);
	}

	/**
	 * 批量禁用用户,并将用户踢下线
	 */
	public void disabledAndOffline(List<Long> ids) {
		if (CollectionUtils.isNotEmpty(ids)) {
			List<UserInfo> users = findValidByIds(ids);
			for (UserInfo u : users) {
				// 覆盖token
				String token = IdentityUtils.uuidWithoutSplitter();
				stringRedisOperateService.setData(REDIS_USER_TOKEN + u.getId().toString(), token);
				u.setValid(CommonConstant.INT_BOOLEAN_FALSE);
			}
			save(users);
		}
	}

	@Transactional
	public UserInfo bindMobilephone(UserInfo user, String openId, Integer platform, String username, String icon,
			String nickname, Integer sex) {
		if (user == null) {
			user = new UserInfo();
			user.setUsername(username);
			if (StringUtils.isNotBlank(icon)) {
				user.setIcon(uploadIcon(icon));
			}
			user.setTelephone(username);
			UserInfo exist = this.findByNickname(nickname);
			if (exist == null) {
				user.setNickname(nickname);
			} else {
				if (nickname == null) {
					if (platform == 1) {
						nickname = "wy_qq_";
					} else if (platform == 2) {
						nickname = "wy_weixin_";
					} else {
						nickname = "wy_weibo_";
					}
				}
				user.setNickname(nickname + username.substring(username.length() - 4, username.length()));
			}
			if (sex != null) {
				user.setSex(sex == 1 ? 0 : 1);
			}
			user.setValid(1);
			user.setCreateDate(new Date());
			userInfoDao.save(user);
		} else {
			if (user.getIsUpdated() == null) {
				Calendar cal = Calendar.getInstance();
				cal.set(2016, 4, 1, 0, 0, 0);
				//if (user.getCreateDate().getTime() > cal.getTimeInMillis()) {
				if (StringUtils.isNotBlank(icon)) {
					user.setIcon(uploadIcon(icon));
				}
				if (StringUtils.isNotBlank(nickname)) {
					UserInfo exist = this.findByNickname(nickname);
					if (exist == null) {
						user.setNickname(nickname);
					} else {
						user.setNickname(nickname + username.substring(username.length() - 4, username.length()));
					}
				}
				if (sex != null) {
					user.setSex(sex == 1 ? 0 : 1);
				}
				userInfoDao.save(user);
				//}
			}
		}
		ThirdPartyLogin login = new ThirdPartyLogin();
		login.setOpenId(openId);
		login.setPlatform(platform);
		login.setUsername(username);
		login.setIcon(icon);
		login.setNickname(nickname);
		if (sex != null) {
			login.setSex(sex == 1 ? 0 : 1);
		}
		login.setValid(1);
		login.setCreateDate(new Date());
		thirdPartyLoginDao.save(login);
		return user;
	}

	public String uploadIcon(String icon) {
		HttpClient httpClient = HttpClients.custom().build();
		HttpGet httpGet = new HttpGet(icon);
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(10000).setConnectTimeout(30000).build();
		httpGet.setConfig(requestConfig);
		byte[] result;
		try {
			HttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			result = EntityUtils.toByteArray(entity);
			icon = UpYunUploaderUtils.uploadImgBinary(result, "uploads/imgs/third_party", UUID.randomUUID().toString());
		} catch (Exception e) {
		} finally {
		}
		return icon;
	}

	//批量增加用户金币
	public void addCoinByIds(Long[] ids, Integer[] coins) {
		String sql = "update user_t_info set coin=(case id ";
		for (int i = 0; i < ids.length; i++) {
			if (ids[i] == 0) {
				break;
			}
			sql = SqlJoiner.join(sql, " when ", ids[i].toString(), " then ", coins[i].toString());
		}
		sql = SqlJoiner.join(sql, " end) where id in (");
		for (Long id : ids) {
			if (id == 0) {
				break;
			}
			sql = SqlJoiner.join(sql, id.toString(), ",");
		}
		sql = sql.substring(0, sql.length() - 1);
		queryDao.update(SqlJoiner.join(sql, ")"));
	}

	public long countUser() {
		String sql = "select count(id) from user_t_info i where i.is_valid=1";
		Number query = queryDao.query(sql);
		if (query == null) {
			return 0;
		}
		return query.longValue();
	}

}
