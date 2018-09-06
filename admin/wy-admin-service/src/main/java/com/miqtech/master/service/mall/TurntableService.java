package com.miqtech.master.service.mall;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.mall.CoinConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.mall.CommodityHistoryDao;
import com.miqtech.master.dao.mall.TurntableInfoDao;
import com.miqtech.master.dao.mall.TurntablePrizeDao;
import com.miqtech.master.dao.mall.TurntableRuleDao;
import com.miqtech.master.dao.user.UserInfoDao;
import com.miqtech.master.dao.user.UserRedbagDao;
import com.miqtech.master.entity.mall.CommodityCategory;
import com.miqtech.master.entity.mall.CommodityHistory;
import com.miqtech.master.entity.mall.TurntableInfo;
import com.miqtech.master.entity.mall.TurntablePrize;
import com.miqtech.master.entity.mall.TurntableRule;
import com.miqtech.master.entity.user.UserInfo;
import com.miqtech.master.service.amuse.AmuseActivityInfoService;
import com.miqtech.master.service.common.ObjectRedisOperateService;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.service.system.SystemRedbagService;
import com.miqtech.master.utils.JsonUtils;
import com.miqtech.master.utils.SqlJoiner;

/**
 * （金币）转盘操作service
 */
@Component
public class TurntableService {
	@Autowired
	private TurntableInfoDao turntableDao;

	@Autowired
	private TurntableRuleDao turntableRuleDao;

	@Autowired
	private QueryDao queryDao;

	@Autowired
	private JedisConnectionFactory redisConnectionFactory;
	@Autowired
	private StringRedisOperateService stringRedisOperateService;
	@Autowired
	UserInfoDao userInfoDao;
	@Autowired
	CoinHistoryService coinHistoryService;
	@Autowired
	private CommodityCategoryService commodityCategoryService;
	@Autowired
	TurntablePrizeDao TurntablePrizeDao;
	@Autowired
	AmuseActivityInfoService amuseActivityInfoService;
	@Autowired
	CommodityHistoryDao commodityHistoryDao;
	@Autowired
	ObjectRedisOperateService objectRedisOperateService;
	@Autowired
	SystemRedbagService systemRedbagService;
	@Autowired
	UserRedbagDao userRedbagDao;
	@Autowired
	CommodityHistoryService commodityHistoryService;
	@Autowired
	private StringRedisOperateService redisOperateService;

	/**
	 * 根据id查模块
	 */
	public TurntableInfo getTurntableById(long turntableId) {
		return turntableDao.findOne(turntableId);
	}

	/**
	 * 保存模块
	 */
	public void save(TurntableInfo turntableInfo) {
		turntableDao.save(turntableInfo);
	}

	/**
	 * 保存规则
	 */
	public void saveRule(TurntableRule turntableRule) {
		turntableRuleDao.save(turntableRule);
	}

	/**
	 * 根据id查规则
	 */
	public TurntableRule getTurntableRule() {
		return turntableRuleDao.findOne((long) 1);
	}

	/**
	 * 查看转盘状态
	 */
	public int isStart() {
		String sqlQuery = "select c.id,c.is_start isStart from mall_t_turntable_start  c where c.is_valid=1 and c.id=1";
		Map<String, Object> isStartMap = queryDao.querySingleMap(sqlQuery);
		if (null == isStartMap) {
			String insertsql = "insert into mall_t_turntable_start(id,is_start,is_valid) values(1,0,1)";
			queryDao.update(insertsql);
		}
		isStartMap = queryDao.querySingleMap(sqlQuery);
		byte startb = (byte) isStartMap.get("isStart");
		int start = startb;
		return start;
	}

	/**
	 * 改变转盘状态
	 */
	public void changeStart(Integer isstart) {
		String sql = SqlJoiner.join("UPDATE mall_t_turntable_start SET is_start=", isstart + " where id=1");
		queryDao.update(sql);
	}

	/**
	 * 删除模块
	 */
	public void deleteModule(String ids) {
		String sql = SqlJoiner.join("UPDATE mall_t_turntable SET is_valid=0 where FIND_IN_SET(id,'" + ids + "')  ");
		queryDao.update(sql);
	}

	/**
	 * 检查转盘是否可以开启
	 */
	public boolean checkCanDrowPrize() {
		String sql = SqlJoiner
				.join("SELECT  id moduleId,  prizeCount, if(vv.canDrowCount is null ,0,vv.canDrowCount) canDrowCount FROM (",
						"select c.id, c.module_name moduleName,c.probability,c.prize_count prizeCount,",
						"(select count(1)  from mall_t_turntable_prize a where a.enable_status!=-1 and  c.id=a.module_id and a.is_valid=1  group by a.module_id",
						")  canDrowCount ", "from mall_t_turntable c   where c.is_valid=1) vv");
		boolean flag = true;
		List<Map<String, Object>> countMaps = queryDao.queryMap(sql);
		if (countMaps != null) {
			for (Map<String, Object> countMap : countMaps) {
				int count = ((Number) countMap.get("canDrowCount")).intValue();
				int prizeCount = ((Number) countMap.get("prizeCount")).intValue();
				//可抽数量小于3
				if (count < 3) {
					flag = false;
					break;
				}
				//可抽数量小于奖品数量
				if (prizeCount > count) {
					flag = false;
					break;
				}
			}
		}
		return flag;
	}

	/**
	 * 查出所有转盘信息
	 */
	public List<Map<String, Object>> turntableList() {
		String sqlQuery = SqlJoiner
				.join("SELECT  id, moduleName,probability,  prizeCount, if(vv.canDrowCount is null ,0,vv.canDrowCount) canDrowCount FROM (",
						"select c.id, c.module_name moduleName,c.probability,c.prize_count prizeCount,",
						"(select count(1) can_draw_count from mall_t_turntable_prize a2 where a2.enable_status!=-1 and  c.id=a2.module_id  and a2.is_valid=1  group by a2.module_id",
						")  canDrowCount ", "from mall_t_turntable c   where c.is_valid=1) vv");
		return queryDao.queryMap(sqlQuery);
	}

	/**转盘奖品信息
	 * @return
	 */
	public Map<String, Object> wheelPrizeInfo(Long userId) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("code", -1);//转盘不可用
		result.put("isFirst", 1);
		Integer isStart = ((Number) queryDao.query("select is_start from mall_t_turntable_start where is_valid=1"))
				.intValue();
		StringBuilder sql = new StringBuilder("");
		if (isStart == 1) {
			List<Map<String, Object>> moduleList = queryDao
					.queryMap("select a.id, a.prize_count, b.total_count, if ( a.prize_count <= b.total_count, 1, 0 ) can_lottery from mall_t_turntable a left join ( select module_id, count(id) total_count from mall_t_turntable_prize where is_valid = 1 and enable_status = 0 and module_id is not null group by module_id ) b on a.id = b.module_id where a.is_valid = 1");
			if (CollectionUtils.isNotEmpty(moduleList)) {
				for (int i = 0; i < moduleList.size(); i++) {
					Map<String, Object> map = moduleList.get(i);
					Integer can_lottery = ((Number) map.get("can_lottery")).intValue();
					Integer module_id = ((Number) map.get("id")).intValue();
					Integer prize_count = ((Number) map.get("prize_count")).intValue();
					if (can_lottery == 0 || prize_count < 1) {
						result.put("code", -1);
						return result;
					} else {
						String unionSql = "";
						if (i > 0) {
							unionSql = " union all ";
						}
						sql.append(unionSql
								+ "select * from (select id,module_id,prize_name,prize_img,prize_count,case when category_id in(1,5) then 1 when category_id=3 then 2 when category_id in(6,8) then 3 when category_id=99 then 99 else 4 end commodity_type from mall_t_turntable_prize where module_id="
								+ module_id + " and is_valid=1 and enable_status=0 ORDER BY RAND() LIMIT "
								+ prize_count + ")a" + i);
					}
				}
				sql.append(" ORDER BY RAND() ");
				List<Map<String, Object>> prizes = queryDao.queryMap(sql.toString());
				if (CollectionUtils.isNotEmpty(prizes) && prizes.size() == 12) {
					result.put("code", 0);
					result.put("prizes", JsonUtils.objectToString(prizes));
					objectRedisOperateService.setData(CommonConstant.WHEEL_USER_PRIZES + userId, prizes);
					result.put(
							"rollInfo",
							queryDao.queryMap("select a.id, concat( substr( if ( a.user_id is null, a.virtual_phone, b.username ), 1, 3 ), '****', substr( if ( a.user_id is null, a.virtual_phone, b.username ), 8, 11 )) phone, c.prize_name from mall_r_commodity_history a left join user_t_info b on a.user_id = b.id left join mall_t_turntable_prize c on a.commodity_id = c.id where a.is_valid = 1 and a.commodity_source = 2 and a.is_get = 1 order by a.create_date desc limit 30"));
				}
			}
		}
		String freeLottery = stringRedisOperateService.getData(CommonConstant.WHEEL_FREE_LOTTERY + userId);
		Integer isFirst = 1;
		if (freeLottery == null) {
			String wheelNeedCoin = stringRedisOperateService.getData(CommonConstant.WHEEL_LOTTERY_NEEED_COIN);
			if (wheelNeedCoin == null) {
				stringRedisOperateService.setData(CommonConstant.WHEEL_LOTTERY_NEEED_COIN, CommonConstant.NEED_COIN);
			}
			String userFirstLottery = stringRedisOperateService.getData(CommonConstant.WHEEL_USER_FIRST_LOTTERY
					+ userId);
			if (userFirstLottery == null) {
				stringRedisOperateService.setData(CommonConstant.WHEEL_USER_FIRST_LOTTERY + userId, "1");
			} else {
				isFirst = 0;
			}
			if (!CommonConstant.NEED_COIN.equals(stringRedisOperateService
					.getData(CommonConstant.WHEEL_LOTTERY_NEEED_COIN))) {
				stringRedisOperateService.setData(CommonConstant.WHEEL_USER_FIRST_LOTTERY + userId, null);
			} //判断是否是第一次抽奖,这个设计也是醉了
			result.put("isFirst", isFirst);
		} else {
			result.put("isFirst", 0);
		}
		return result;

	}

	/**转盘抽奖
	 * @param alreadyLotteryId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> lottery(Long userId) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("code", -1);//抽奖失败
		boolean limit = redisOperateService.setIfAbsent("wheel_lottery_limit_" + userId, "1");
		if (limit) {
			redisOperateService.expire("wheel_lottery_limit_" + userId, 2, TimeUnit.SECONDS);
		}
		String freeLottery = stringRedisOperateService.getData(CommonConstant.WHEEL_FREE_LOTTERY + userId);
		UserInfo userInfo = userInfoDao.findOne(userId);
		if ((freeLottery != null || (userInfo.getCoin() == null ? 0 : userInfo.getCoin()) >= NumberUtils
				.toInt(CommonConstant.NEED_COIN)) && limit) {
			Number totalPercent = queryDao.query("select sum(probability) from mall_t_turntable where is_valid=1");
			if (totalPercent != null && totalPercent.intValue() == 100) {
				List<Map<String, Object>> moduleList = queryDao
						.queryMap("select id,probability from mall_t_turntable where is_valid=1");
				if (CollectionUtils.isNotEmpty(moduleList)) {
					Double tmp = 0d;
					List<Map<String, Object>> lotteryList = new ArrayList<Map<String, Object>>();
					for (Map<String, Object> obj : moduleList) {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("module_id", obj.get("id"));
						map.put("min", tmp);
						tmp += ((Number) obj.get("probability")).doubleValue();
						map.put("max", tmp);
						lotteryList.add(map);
					}
					Random random = new Random();
					double lotteryRandom = random.nextDouble() * 100;
					Long lotteryModuleId = 0L;//中奖模块
					for (Map<String, Object> obj : lotteryList) {
						if (lotteryRandom >= (double) obj.get("min") && lotteryRandom < (double) obj.get("max")) {
							lotteryModuleId = ((BigInteger) obj.get("module_id")).longValue();
							break;
						}
					}
					List<Map<String, Object>> prizes = (List<Map<String, Object>>) objectRedisOperateService
							.getData(CommonConstant.WHEEL_USER_PRIZES + userId);
					List<Map<String, Object>> prizesList = new ArrayList<Map<String, Object>>();
					Map<String, Object> noPrize = null;
					for (Map<String, Object> map : prizes) {
						if (((Number) map.get("module_id")).longValue() == lotteryModuleId) {
							prizesList.add(map);
						}
						if (((Number) map.get("commodity_type")).intValue() == 99) {
							noPrize = map;
						}
					}
					Map<String, Object> prize = prizesList.get(new Random().nextInt(prizesList.size()));
					if (prize != null && !prize.isEmpty()) {
						Long prizeId = ((Number) prize.get("id")).longValue();
						if (((Number) prize.get("prize_count")).intValue() != -1) {
							RedisAtomicInteger leftNum = new RedisAtomicInteger(
									CommonConstant.MALL_COMMODITY_WHEEL_LEFT_NUM + prizeId, redisConnectionFactory);
							if (leftNum.get() == -1) {//库存不足
								queryDao.update("update mall_t_turntable_start set is_start=0 where is_valid=1");
								result.put("code", -2);
								prize = noPrize;
								if (prize == null) {
									return result;
								}
								prizeId = ((Number) prize.get("id")).longValue();
							} else if (leftNum.get() > 0) {
								leftNum.set(leftNum.get() - 1 <= 0 ? -1 : leftNum.get() - 1);
							} else {
								return result;
							}
						}
						result.put("code", 0);
						result.put("prizeId", prizeId);
						result.put("prizeName", prize.get("prize_name"));
						result.put("prizeImg", prize.get("prize_img"));
						result.put("prizeType", prize.get("commodity_type"));
						result.put(
								"historyId",
								saveRecord(prizeId, ((Number) prize.get("commodity_type")).intValue(), userId,
										freeLottery));
					} else {
						queryDao.update("update mall_t_turntable_start set is_start=0 where is_valid=1");
						result.put("code", -2);//转盘已关闭
						return result;
					}
				}
			}
		} else {
			result.put("code", -3);//金币不足
		}
		return result;
	}

	public Long saveRecord(Long prizeId, Integer categoryId, Long userId, String freeLottery) {
		TurntablePrize turntablePrize = TurntablePrizeDao.findOne(prizeId);
		CommodityHistory commodityHistory = new CommodityHistory();
		if (freeLottery == null) {
			commodityHistory.setCoin(NumberUtils.toInt(CommonConstant.NEED_COIN));
			coinHistoryService.addGoldHistoryPub(userId, prizeId, CoinConstant.HISTORY_TYPE_WHEEL_LOTTERY,
					NumberUtils.toInt(CommonConstant.NEED_COIN), CoinConstant.HISTORY_DIRECTION_EXPEND);
		} else {
			commodityHistory.setCoin(0);
			stringRedisOperateService.delData(CommonConstant.WHEEL_FREE_LOTTERY + userId);
		}
		commodityHistory.setCommodityId(prizeId);
		commodityHistory.setCommoditySource(2);
		commodityHistory.setUserId(userId);
		commodityHistory.setStatus(-1);
		CommodityCategory commodityCategory = commodityCategoryService.getCommodityCategoryById(turntablePrize
				.getCategoryId());
		commodityHistory.setTranNo(amuseActivityInfoService.genSerial(0, commodityCategory == null ? 1
				: commodityCategory.getSuperType()));
		commodityHistory.setIsGet(1);
		if (categoryId == 99) {
			commodityHistory.setValid(0);
		} else {
			commodityHistory.setValid(1);
		}
		commodityHistory.setCreateDate(new Date());
		commodityHistory.setNum(1);
		commodityHistory.setInformation(turntablePrize.getInformationDefualt());
		//当中奖记录为真实记录且类型为实物和虚拟充值时需要设置审核用户id
		commodityHistory.setCreateUserId(commodityHistoryService.getSysUserIdOfLeastOrder(1)); //分配给订单最少的审核人员
		commodityHistory.setUpdateDate(new Date());
		commodityHistoryDao.save(commodityHistory);

		//自动发放金币红包
		commodityHistoryService.autoExchangeRedbagAndCoin(prizeId, turntablePrize.getCategoryId(),
				turntablePrize.getPrizeQuota(), userId, CoinConstant.HISTORY_TYPE_WHEEL_LOTTERY);
		return commodityHistory.getId();
	}
}
