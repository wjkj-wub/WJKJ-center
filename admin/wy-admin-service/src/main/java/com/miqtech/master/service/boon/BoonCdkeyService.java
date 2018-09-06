package com.miqtech.master.service.boon;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.miqtech.master.consts.BoonConstant;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.RedbagConstant;
import com.miqtech.master.consts.mall.CoinConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.boon.BoonCdkeyDao;
import com.miqtech.master.entity.boon.BoonCdkey;
import com.miqtech.master.entity.common.SystemRedbag;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.entity.user.UserRedbag;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.service.mall.CoinHistoryService;
import com.miqtech.master.service.system.SystemRedbagService;
import com.miqtech.master.service.user.UserInfoService;
import com.miqtech.master.service.user.UserRedbagService;
import com.miqtech.master.thirdparty.util.SMSMessageUtil;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class BoonCdkeyService {

	private static List<BoonCdkey> tencentSkinCdkeys = null;

	public static enum MsgEnum {
		CODE, MSG, TYPE, AMOUNT
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(BoonCdkeyService.class);

	@Autowired
	private StringRedisOperateService stringRedisOperateService;
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private BoonCdkeyDao boonCdkeyDao;
	@Autowired
	private SystemRedbagService systemRedbagService;
	@Autowired
	private UserRedbagService userRedbagService;
	@Autowired
	private CoinHistoryService coinHistoryService;
	@Autowired
	private UserInfoService userInfoService;

	public PageVO list(int page, Map<String, String> searchParams) {
		String condition = " WHERE 1=1";
		String totalCondition = condition;
		Map<String, Object> params = Maps.newHashMap();

		String cdkey = MapUtils.getString(searchParams, "cdkey");
		if (StringUtils.isNotBlank(cdkey)) {
			String likeCdkey = SqlJoiner.join("%", cdkey, "%");
			condition = SqlJoiner.join(condition, " AND cdkey LIKE :cdkey");
			totalCondition = SqlJoiner.join(totalCondition, " AND cdkey LIKE '", likeCdkey, "'");
			params.put("cdkey", likeCdkey);
		}
		String production = MapUtils.getString(searchParams, "production");
		if (StringUtils.isNotBlank(production)) {
			String likeProduction = SqlJoiner.join("%", production, "%");
			condition = SqlJoiner.join(condition, " AND production LIKE :production");
			totalCondition = SqlJoiner.join(totalCondition, " AND production LIKE '", likeProduction, "'");
			params.put("production", likeProduction);
		}
		String type = MapUtils.getString(searchParams, "type");
		if (NumberUtils.isNumber(type)) {
			condition = SqlJoiner.join(condition, " AND type = ", type);
			totalCondition = SqlJoiner.join(totalCondition, " AND type = ", type);
		}
		String isUsed = MapUtils.getString(searchParams, "isUsed");
		if (CommonConstant.INT_BOOLEAN_TRUE.toString().equals(isUsed)) {
			condition = SqlJoiner.join(condition, " AND used_date IS NOT NULL");
			totalCondition = SqlJoiner.join(totalCondition, " AND used_date IS NOT NULL");
		} else if (CommonConstant.INT_BOOLEAN_FALSE.toString().equals(isUsed)) {
			condition = SqlJoiner.join(condition, " AND used_date IS NULL");
			totalCondition = SqlJoiner.join(totalCondition, " AND used_date IS NULL");
		}
		String createDateBegin = MapUtils.getString(searchParams, "createDateBegin");
		if (StringUtils.isNotBlank(createDateBegin)) {
			condition = SqlJoiner.join(condition, " AND create_date >= '", createDateBegin, "'");
			totalCondition = SqlJoiner.join(totalCondition, " AND create_date >= '", createDateBegin, "'");
		}
		String createDateEnd = MapUtils.getString(searchParams, "createDateEnd");
		if (StringUtils.isNotBlank(createDateEnd)) {
			condition = SqlJoiner.join(condition, " AND create_date < ADDDATE('", createDateEnd, "', INTERVAL 1 DAY)");
			totalCondition = SqlJoiner.join(totalCondition, " AND create_date < ADDDATE('", createDateEnd,
					"', INTERVAL 1 DAY)");
		}

		String totalSql = SqlJoiner.join("SELECT COUNT(1) FROM boon_t_cdkey", totalCondition);
		Number total = queryDao.query(totalSql);

		List<Map<String, Object>> list = null;
		if (total != null && total.intValue() > 0) {
			String limitSql = PageUtils.getLimitSql(page);
			String sql = SqlJoiner.join("SELECT id, CONVERT (cdkey, CHAR) cdkey, type, amount, production,",
					" expired_date expiredDate, create_date createDate, used_date usedDate", " FROM boon_t_cdkey",
					condition, limitSql);
			list = queryDao.queryMap(sql, params);
		}

		PageVO vo = new PageVO(page, list, total);
		return vo;
	}

	public List<BoonCdkey> findByProduction(String production) {
		return boonCdkeyDao.findByProduction(production);
	}

	public BoonCdkey save(BoonCdkey cdkey) {
		return boonCdkeyDao.save(cdkey);
	}

	public void save(List<BoonCdkey> cdkeys) {
		boonCdkeyDao.save(cdkeys);
	}

	/**
	 * 生产CDKEY
	 */
	public void produceCdkeys(Integer cdkeyType, Integer amount, Integer number, String expiredDate,
			String production) {
		try {
			if (amount == null || number == null || amount < 0 || number <= 0 || cdkeyType == null) {
				return;
			}

			// 准备cdkey相关设置
			Date ed = DateUtils.stringToDate(expiredDate, DateUtils.YYYY_MM_DD_HH_MM_SS);
			Long targetId = null;
			if (BoonConstant.BOON_CDKEY_TYPE_REDBAG.equals(cdkeyType)) {
				SystemRedbag config = systemRedbagService.findOndByTypeAndMoney(RedbagConstant.REDBAG_TYPE_CDKEY,
						amount);
				if (config != null) {
					targetId = config.getId();
				} else {
					LOGGER.error("找不到红包配置");
					return;
				}
			} else {
				if (!BoonConstant.BOON_CDKEY_TYPE_COIN.equals(cdkeyType)) {
					LOGGER.error("cdkey类型不正确");
					return;
				}
			}

			// 为生产数量分批
			int batch = 1;
			int batchNum = 10000;
			if (number > batchNum) {
				batch = number / batchNum;
			}

			// 分批生产cdkey
			Date now = new Date();
			for (int b = 0; b < batch; b++) {
				// 计算当前批次执行数
				int thisBatch = number - b * batchNum;
				if (thisBatch > batchNum) {
					thisBatch = batchNum;
				}

				// 生产cdkey
				List<BoonCdkey> cdkeys = new ArrayList<BoonCdkey>();
				for (int i = 0; i < thisBatch; i++) {
					cdkeys.add(produceCdkey(cdkeyType, targetId, amount, ed, now, production));
				}
				save(cdkeys);
			}

			Date endDate = new Date();
			LOGGER.error("生产完成,总计用时:" + (endDate.getTime() - now.getTime()) + "ms");
		} catch (ParseException e) {
			LOGGER.error("格式化时间异常:", e);
		} catch (Exception e) {
			LOGGER.error("未知异常", e);
		}
	}

	/**
	 * 在数据库中产生一条cdkey
	 */
	public BoonCdkey produceCdkey(Integer cdkeyType, Long targetId, Integer amount, Date expiredDate, Date produceDate,
			String production) {
		String cdkey = genCdkey();
		BoonCdkey c = new BoonCdkey();
		c.setType(cdkeyType);
		c.setTargetId(targetId);
		c.setCdkey(cdkey);
		c.setAmount(amount);
		c.setExpiredDate(expiredDate);
		c.setUpdateDate(produceDate);
		c.setCreateDate(produceDate);
		c.setProduction(production);
		return c;
	}

	/**
	 * 随机产生cdkey
	 */
	public String genCdkey() {
		int cdkeyLen = 12;
		String caseStr = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

		StringBuilder cdkey = new StringBuilder();
		for (int i = 0; i < cdkeyLen; i++) {
			Random r = new Random();
			int index = r.nextInt(caseStr.length());
			cdkey.append(caseStr.charAt(index));
		}

		return cdkey.toString();
	}

	/**
	 * 查询cdkey
	 */
	public BoonCdkey findByCdkey(String cdkey) {
		return boonCdkeyDao.findByCdkey(cdkey);
	}

	/**
	 * 查询用户兑换过的cdkey
	 */
	public List<BoonCdkey> findByUserId(Long userId) {
		return boonCdkeyDao.findByUserId(userId);
	}

	/**
	 * 查询用户在某批次下的兑换
	 */
	public List<BoonCdkey> findByUserIdAndProduction(Long userId, String production) {
		return boonCdkeyDao.findByUserIdAndProduction(userId, production);
	}

	/**
	 * 兑换cdkey
	 * @throws ParseException 
	 * @throws InterruptedException 
	 */
	public EnumMap<MsgEnum, Object> exchange(Long userId, String cdkey) throws ParseException, InterruptedException {
		EnumMap<MsgEnum, Object> result = new EnumMap<MsgEnum, Object>(MsgEnum.class);

		if (userId == null || StringUtils.isBlank(cdkey)) {
			result.put(MsgEnum.CODE, -1);
			result.put(MsgEnum.MSG, "缺少必要参数");
			return result;
		}

		BoonCdkey c = boonCdkeyDao.findByCdkey(cdkey);
		if (c == null) {
			result.put(MsgEnum.CODE, -2);
			result.put(MsgEnum.MSG, "不存在该cdkey");
			return result;
		}

		if (c.getUserId() != null || c.getUsedDate() != null) {
			result.put(MsgEnum.CODE, -3);
			result.put(MsgEnum.MSG, "cdkey已使用");
			return result;
		}

		Date now = new Date();
		Date expiredDate = c.getExpiredDate();
		if (expiredDate != null && (expiredDate.before(now) || expiredDate.equals(now))) {
			result.put(MsgEnum.CODE, -4);
			result.put(MsgEnum.MSG, "cdkey已过期");
			return result;
		}

		List<BoonCdkey> userCdKeys = findByUserIdAndProduction(userId, c.getProduction());
		if (CollectionUtils.isNotEmpty(userCdKeys)) {
			result.put(MsgEnum.CODE, -6);
			result.put(MsgEnum.MSG, "用户已经兑换过红包");
			return result;
		}

		// 处理cdkey
		if (BoonConstant.BOON_CDKEY_TYPE_REDBAG.equals(c.getType())) {
			if (c.getAmount() == null || c.getAmount() <= 0) {
				result.put(MsgEnum.CODE, -5);
				result.put(MsgEnum.MSG, "cdkey配置不正确");
				return result;
			}

			SystemRedbag redbag = systemRedbagService.findOne(c.getTargetId());
			if (redbag != null) {
				c.setUserId(userId);
				c.setUsedDate(now);
				boonCdkeyDao.save(c);

				UserRedbag ur = new UserRedbag();
				ur.setUserId(userId);
				ur.setRedbagId(redbag.getId());
				ur.setUsable(CommonConstant.INT_BOOLEAN_TRUE);
				ur.setAmount(redbag.getMoney());
				ur.setNetbarType(1);
				ur.setNetbarId(0L);
				ur.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				ur.setUpdateDate(now);
				ur.setCreateDate(now);
				ur.setLimitMinMoney(0);
				userRedbagService.save(ur, true);
			}
		} else if (BoonConstant.BOON_CDKEY_TYPE_COIN.equals(c.getType())) {
			if (c.getAmount() == null || c.getAmount() <= 0) {
				result.put(MsgEnum.CODE, -5);
				result.put(MsgEnum.MSG, "cdkey配置不正确");
				return result;
			}

			c.setUserId(userId);
			c.setUsedDate(now);
			boonCdkeyDao.save(c);

			coinHistoryService.addGoldHistoryPub(userId, c.getId(), CoinConstant.HISTORY_TYPE_CDKEY, c.getAmount(), 1);
		} else if (BoonConstant.BOON_CDKEY_TYPE_SKIN_WANGYU.equals(c.getType())) {
			// 检查用户注册时间
			UserInfo user = userInfoService.findById(userId);
			if (user == null || user.getCreateDate() == null) {
				result.put(MsgEnum.CODE, -7);
				result.put(MsgEnum.MSG, "错误的用户信息");
				return result;
			}
			Date beginDate = DateUtils.stringToDate("2016-08-08 00:00:00", DateUtils.YYYY_MM_DD_HH_MM_SS);
			Date endDate = DateUtils.stringToDate("2016-08-10 23:59:59", DateUtils.YYYY_MM_DD_HH_MM_SS);
			Date createDate = user.getCreateDate();
			if (!beginDate.before(createDate) || !endDate.after(createDate)) {
				result.put(MsgEnum.CODE, -8);
				result.put(MsgEnum.MSG, "注册时间不在活动范围");
				return result;
			}

			// 获取未使用的cdkey
			BoonCdkey tencentCdkey = null;
			if (!CollectionUtils.isEmpty(tencentSkinCdkeys)) {
				for (Iterator<BoonCdkey> it = tencentSkinCdkeys.iterator(); it.hasNext();) {
					BoonCdkey tsc = it.next();
					if (tsc.getUserId() != null || tsc.getUsedDate() != null || tsc.getExpiredDate() == null
							|| tsc.getExpiredDate().before(now)) {// 已使用或已过期
						it.remove();
						continue;
					}

					String redisKey = "wy_api_tencent_cdkey_used_" + tsc.getId();
					RedisConnectionFactory factory = stringRedisOperateService.getRedisTemplate()
							.getConnectionFactory();
					RedisAtomicInteger used = new RedisAtomicInteger(redisKey, factory);
					used.expireAt(tsc.getExpiredDate());
					if (used.incrementAndGet() <= 1) {
						tencentCdkey = tsc;
						it.remove();
						break;
					} else {
						it.remove();
					}
				}
			}

			if (tencentCdkey == null) {
				result.put(MsgEnum.CODE, -9);
				result.put(MsgEnum.MSG, "奖品已发完");
				return result;
			}

			// 更新腾讯皮肤为已使用
			tencentCdkey.setUserId(userId);
			tencentCdkey.setUsedDate(now);
			save(tencentCdkey);

			// 更新cdkey状态为已使用
			c.setUserId(userId);
			c.setUsedDate(now);
			save(c);

			// 发送短信通知
			try {
				String[] phoneNum = { user.getUsername() };
				String[] params = { tencentCdkey.getCdkey() };
				SMSMessageUtil.sendTemplateMessage(phoneNum, "3018091", params);
			} catch (Exception e) {
				LOGGER.error("发送短信失败:", e);
			}
		} else {
			result.put(MsgEnum.CODE, -5);
			result.put(MsgEnum.MSG, "cdkey配置不正确");
			return result;
		}

		result.put(MsgEnum.TYPE, c.getType());
		result.put(MsgEnum.AMOUNT, c.getAmount());
		result.put(MsgEnum.CODE, 0);
		result.put(MsgEnum.MSG, "成功");
		return result;
	}

	public List<Map<String, Object>> statisByProduction(String production) {
		if (StringUtils.isBlank(production)) {
			return null;
		}

		String sql = SqlJoiner.join(
				"select sum(amount) sumAmount, amount, if(type = 1, '红包', if(type = 2, '金币', type)) type, count(1) number, expired_date expiredDate, user_id userId, used_date usedDate, production",
				" from boon_t_cdkey where production = '" + production
						+ "' GROUP BY amount, type ORDER BY type ASC, amount ASC, id ASC");
		return queryDao.queryMap(sql);
	}

	/**
	 * 通过生产用途查询cdkey
	 */
	public List<Map<String, Object>> queryByProduction(String production) {
		if (StringUtils.isBlank(production)) {
			return null;
		}

		String sql = SqlJoiner.join(
				"SELECT IF ( type = 1, '红包', IF (type = 2, '金币', type) ) type, amount, CONVERT (cdkey, CHAR) cdkey,",
				" expired_date expiredDate, production FROM boon_t_cdkey WHERE production = '", production, "'",
				" ORDER BY type ASC, amount ASC, id ASC");
		List<Map<String, Object>> list = queryDao.queryMap(sql);
		return list;
	}

	public BoonCdkey findOneNotUsedByType(Integer type) {
		if (type == null) {
			return null;
		}

		String sql = SqlJoiner.join("SELECT id, user_id, target_id, CONVERT (cdkey, CHAR) cdkey, production,",
				" expired_date, used_date, update_date, create_date, type, amount",
				" FROM boon_t_cdkey where user_id is null and expired_date > now() and type = ", type.toString(),
				" ORDER BY create_date, id LIMIT 1");
		Map<String, Object> cdkey = queryDao.querySingleMap(sql);
		if (MapUtils.isEmpty(cdkey)) {
			return null;
		}

		BoonCdkey c = new BoonCdkey();
		c.setId(MapUtils.getLong(cdkey, "id"));
		c.setUserId(MapUtils.getLong(cdkey, "user_id"));
		c.setTargetId(MapUtils.getLong(cdkey, "target_id"));
		c.setCdkey(MapUtils.getString(cdkey, "cdkey"));
		c.setProduction(MapUtils.getString(cdkey, "production"));
		c.setExpiredDate((Date) cdkey.get("expired_date"));
		c.setUsedDate((Date) cdkey.get("used_date"));
		c.setUpdateDate((Date) cdkey.get("update_date"));
		c.setCreateDate((Date) cdkey.get("create_date"));
		c.setType(MapUtils.getInteger(cdkey, "type"));
		c.setAmount(MapUtils.getInteger(cdkey, "amount"));
		return c;
	}

	public List<BoonCdkey> findUseableCdkeys(Integer type, Date expiredDate) {
		if (type == null || expiredDate == null) {
			return null;
		}

		return boonCdkeyDao.findByTypeAndExpiredDateGreaterThanAndUserIdIsNullAndUsedDateIsNull(type, expiredDate);
	}

	public void reloadTencentSkinCdkeys() {
		tencentSkinCdkeys = findUseableCdkeys(BoonConstant.BOON_CDKEY_TYPE_SKIN_TENCENT, new Date());
		LOGGER.error("皮肤CDKEY(腾讯)已经更新");
	}
}
