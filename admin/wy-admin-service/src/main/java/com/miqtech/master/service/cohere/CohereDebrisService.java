package com.miqtech.master.service.cohere;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miqtech.master.consts.CacheKeyConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.cohere.CohereDebrisDao;
import com.miqtech.master.entity.cohere.CohereDebris;
import com.miqtech.master.entity.cohere.CohereDebrisHistory;
import com.miqtech.master.entity.cohere.CohereDraw;
import com.miqtech.master.entity.cohere.CoherePrize;
import com.miqtech.master.entity.cohere.CoherePrizeHistory;
import com.miqtech.master.service.common.ObjectRedisOperateService;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.utils.DateUtils;
import com.miqtech.master.utils.StringUtils;

@Component
public class CohereDebrisService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private CohereDebrisDao cohereDebrisDao;
	@Autowired
	private JedisConnectionFactory redisConnectionFactory;
	@Autowired
	private CohereDebrisHistoryService cohereDebrisHistoryService;
	@Autowired
	private CohereDrawService cohereDrawService;
	@Autowired
	private ObjectRedisOperateService objectRedisOperateService;
	@Autowired
	private StringRedisOperateService stringRedisOperateService;
	@Autowired
	private CoherePrizeService coherePrizeService;
	@Autowired
	private CoherePrizeHistoryService coherePrizeHistoryService;

	/**
	 * 批量保存碎片信息
	 *
	 * @param entities
	 * @return
	 */
	public List<CohereDebris> saveOrUpdate(List<CohereDebris> entities, Long activityId) {
		List<CohereDebris> list = new ArrayList<>();
		for (CohereDebris cohereDebris : entities) {
			list.add(saveOrUpdate(cohereDebris));
		}
		probabilitySection(activityId, entities, true); // 设置抽取概率
		return list;
	}

	/**
	 * 保存碎片信息
	 *
	 * @param entities
	 * @return
	 */
	public CohereDebris saveOrUpdate(CohereDebris cohereDebris) {
		return cohereDebrisDao.save(cohereDebris);
	}

	/**
	 * 得到活动下的所有碎片
	 *
	 * @param activityId
	 * @return
	 */
	public List<CohereDebris> findByActivityId(Long activityId) {
		return cohereDebrisDao.findByActivityId(activityId);
	}

	/**
	 * 得到活动下的所有碎片
	 *
	 * @param activityId
	 * @return
	 */
	public List<Map<String, Object>> getSomeByActivityId(Long activityId) {
		String sql = "select id,if(isnull(counts),0,counts) counts,url,probability from cohere_debris where activity_id="
				+ activityId;
		return queryDao.queryMap(sql);
	}

	/**
	 * 抽取碎片
	 *
	 * @param urlcode
	 * @param debrisId
	 * @return
	 */
	public Map<String, Object> extractDebris(Long activityId, Integer type, Long userId, String urlcode) {
		Map<String, Object> map = Maps.newHashMap();
		boolean canExtractFlag = canExtra(type, userId, activityId);
		if (!canExtractFlag) {
			map.put("state", "5");
			map.put("result", "无剩余抽奖次数");
			return map; // 无剩余次数
		}

		List<CohereDebris> cohereDerisList = cohereDebrisDao.findByActivityId(activityId);
		List<List<String>> probabilitySection = probabilitySection(activityId, cohereDerisList, true);
		if (probabilitySection.size() == 1) { // 至此活动已无可用碎片
			map.put("state", "4");
			map.put("result", "没有可抽的碎片");
			return map;
		}
		Integer maxRandom = NumberUtils.toInt(probabilitySection.get(probabilitySection.size() - 1).get(0)); // 随机数的取值区间
		Integer mid = new Random().nextInt(maxRandom) + 1;
		while (true) {
			if (probabilitySection.size() == 1) { // 至此活动已无可用碎片
				map.put("state", "4");
				map.put("result", "没有可抽的碎片");
				return map;
			}
			for (int i = 0; i < probabilitySection.size() - 1; i++) {
				if (NumberUtils.toInt(probabilitySection.get(i).get(0)) < mid
						&& NumberUtils.toInt(probabilitySection.get(i + 1).get(0)) >= mid) {
					map = extract(cohereDerisList, NumberUtils.toLong(probabilitySection.get(i).get(1)), type, userId,
							activityId, urlcode);
					if (map != null) { // 抢到对应的碎片
						map.put("state", "0");
						map.put("result", "抢到碎片");
						return map;
					} else {
						break; // 该碎片无剩余值
					}
				}
			}
			probabilitySection = probabilitySection(activityId, cohereDerisList, false);
		}
	}

	private boolean canExtra(int type, Long userId, Long activityId) {
		int leaveChance = 1;
		if (type == 2) {
			leaveChance = wxLeftChance(userId, activityId);
		} else {
			leaveChance = leftChance(userId, activityId);
		}
		if (leaveChance <= 0) {
			return false;
		}
		if (type == 1) {
			decrementChance(userId, activityId);
			String key = CacheKeyConstant.DEBRIS_CHANCE_USED + activityId.toString() + "_" + userId.toString();
			RedisAtomicInteger usedChance = new RedisAtomicInteger(key,
					stringRedisOperateService.getRedisTemplate().getConnectionFactory());
			if (usedChance.get() > 2) {
				return false;
			}
		} else if (type == 2) {
			int count = decrementWxChance(userId, activityId);
			if (count > 1) {
				return false;
			}
		}
		return true;
	}

	private int decrementWxChance(Long userId, Long activityId) {
		String key = CacheKeyConstant.DEBRIS_WX_CHANCE_USED + activityId.toString() + "_" + userId.toString();
		RedisAtomicInteger wxUsedChance = new RedisAtomicInteger(key,
				stringRedisOperateService.getRedisTemplate().getConnectionFactory());
		long seconds = DateUtils.surplusTodaySencods() * 1000;
		wxUsedChance.expire(seconds, TimeUnit.MILLISECONDS);
		return wxUsedChance.incrementAndGet();

	}

	private int wxLeftChance(Long userId, Long activityId) {
		String date = DateUtils.dateToString(DateUtils.getToday(), DateUtils.YYYY_MM_DD_HH_MM_SS);
		String endDate = DateUtils.dateToString(DateUtils.getToday(), "yyyy-MM-dd 23:59:59");
		Number count = queryDao.query(" select count(1) from cohere_draw where user_id =" + userId.toString()
				+ "  and debris_id in (select id from cohere_debris where activity_id =" + activityId.toString() + ")"
				+ " and is_valid = 1 and type =2 and create_date <'" + endDate + "' and create_date >= '" + date + "'");
		if (null == count) {
			return 1;
		} else {
			if (count.intValue() > 0) {
				return 0;
			}
			return 1;
		}
	}

	public static void main(String[] args) {
		System.out.println(DateUtils.dateToString(DateUtils.getToday(), DateUtils.YYYY_MM_DD_HH_MM_SS));
	}

	/**
	 * 抽取碎片是否成功
	 *
	 * @param debrisId
	 * @param urlcode
	 * @return
	 */
	private Map<String, Object> extract(List<CohereDebris> cohereDerisList, Long debrisId, Integer type, Long userId,
			Long activityId, String urlCode) {
		RedisAtomicInteger debrisCount = new RedisAtomicInteger(CacheKeyConstant.COHERE_DEBRIS_COUNT + debrisId,
				redisConnectionFactory);
		if (debrisCount.intValue() > 0) {
			int decrementAndGet = debrisCount.decrementAndGet();
			if (decrementAndGet < 0 && decrementAndGet > -99) {
				debrisCount.set(0);
				probabilitySection(activityId, cohereDerisList, true);
				return null;
			}
		} else {
			if ((debrisCount.get() < 0) && (debrisCount.get() > -99)) {
				debrisCount.set(0);
				probabilitySection(activityId, cohereDerisList, true);
				return null;
			} else if (debrisCount.get() == 0) {
				return null;
			}
		}
		CohereDraw cohereDraw = new CohereDraw();
		cohereDraw.setCreateDate(new Date());
		cohereDraw.setDebrisId(debrisId);
		cohereDraw.setType(type);
		cohereDraw.setValid(1);
		cohereDraw.setUserId(userId);
		cohereDraw.setCreateUserId(userId);
		cohereDraw.setUpdateUserId(userId);
		cohereDraw.setUpdateDate(new Date());
		CohereDraw midCohereDraw = cohereDrawService.saveOrUpdate(cohereDraw);
		CohereDebrisHistory cohereDebrisHistory = new CohereDebrisHistory();
		cohereDebrisHistory.setCreateDate(new Date());
		cohereDebrisHistory.setInType(type);
		cohereDebrisHistory.setDrawId(midCohereDraw.getId());
		cohereDebrisHistory.setIsUsed(1);// 未使用
		cohereDebrisHistory.setUrlCode(urlCode);
		cohereDebrisHistory.setValid(1);
		cohereDebrisHistory.setUrlCode(urlCode);
		cohereDebrisHistory.setDebrisId(midCohereDraw.getDebrisId());
		cohereDebrisHistory.setUserId(midCohereDraw.getUserId());
		cohereDebrisHistoryService.saveOrUpdate(cohereDebrisHistory);
		Map<String, Object> result = new HashMap<>();
		CohereDebris cohereDebris = cohereDebrisDao.findOne(midCohereDraw.getDebrisId());
		result.put("id", debrisId);
		result.put("drawId", midCohereDraw.getId());
		result.put("debrisName", cohereDebris.getTitle());
		result.put("debrisUrl", cohereDebris.getUrl());
		result.put("debrisNum", cohereDebris.getNum());
		return result;
	}

	/**
	 * 抽取碎片生成概率区间 二位数组 一维代表概率区间 二维代表碎片id
	 *
	 * @param activityId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<List<String>> probabilitySection(Long activityId, List<CohereDebris> cohereDerisList, boolean isnew) {
		if (!isnew) {
			return (List<List<String>>) objectRedisOperateService.getData("deris_probability_" + activityId);
		}
		List<List<String>> midList = new ArrayList<List<String>>();
		RedisAtomicInteger debrisCount = null;
		Integer probability = 0;
		List<String> list = new ArrayList<String>();
		for (CohereDebris cohereDebris : cohereDerisList) {
			debrisCount = new RedisAtomicInteger(CacheKeyConstant.COHERE_DEBRIS_COUNT + cohereDebris.getId(),
					redisConnectionFactory);
			if (debrisCount.get() == -99 || debrisCount.get() > 0) {
				list.add(probability.toString());
				list.add(cohereDebris.getId().toString());
				probability += cohereDebris.getProbability();
				midList.add(list);
				list = new ArrayList<String>();
			}
		}
		list.add(probability.toString());
		list.add("0");
		midList.add(list);
		objectRedisOperateService.setData("deris_probability_" + activityId, midList);
		return midList;
	}

	public Map<String, List<Long>> getDebrisNumsByActivityIdAndUserId(Long activityId, Long userId) {
		String sql = "select distinct b.id,c.num,c.title from cohere_debris_history a left join cohere_draw b on a.draw_id=b.id LEFT JOIN cohere_debris c ON b.debris_id = c.id where a.is_valid=1 and b.is_valid=1 and c.is_valid=1 and a.user_id="
				+ userId + " and a.is_used=1 " + " and c.activity_id=" + activityId
				+ " and (a.out_type is null and a.out_id is null) order by c.num,b.id";
		List<Map<String, Object>> list = queryDao.queryMap(sql);
		List<Long> firList = Lists.newArrayList();
		List<Long> secList = Lists.newArrayList();
		List<Long> thiList = Lists.newArrayList();
		List<Long> ForList = Lists.newArrayList();
		Map<String, List<Long>> drawIdListMap = Maps.newHashMap();
		for (int i = 0; i < list.size(); i++) {
			Map<String, Object> map = list.get(i);
			if ((int) map.get("num") == 1) {
				firList.add(NumberUtils.toLong(map.get("id").toString()));
			} else if ((int) map.get("num") == 2) {
				secList.add(NumberUtils.toLong(map.get("id").toString()));
			} else if ((int) map.get("num") == 3) {
				thiList.add(NumberUtils.toLong(map.get("id").toString()));
			} else if ((int) map.get("num") == 4) {
				ForList.add(NumberUtils.toLong(map.get("id").toString()));
			}
		}
		drawIdListMap.put("firList", firList);
		drawIdListMap.put("secList", secList);
		drawIdListMap.put("thiList", thiList);
		drawIdListMap.put("forList", ForList);
		return drawIdListMap;
	}

	/**
	 * 新增翻盘机会 .
	 *
	 * @param source
	 *            1app登录 2微信分享成功
	 * @param userId
	 *            用户id
	 */
	public void incrementChance(Long userId, Long activityId, Integer source) {
		// 将翻牌机会来源、已使用翻牌机会次数存入redis
		String chanceSourcekey = CacheKeyConstant.DEBRIS_CHANCE_SOURCE + activityId.toString() + "_"
				+ userId.toString();
		if (source == 1) {
			stringRedisOperateService.addValuesToSet(chanceSourcekey, source.toString());
			long seconds = DateUtils.surplusTodaySencods() * 1000;
			stringRedisOperateService.expire(chanceSourcekey, seconds, TimeUnit.MILLISECONDS);
		} else if (source == 2) {
			stringRedisOperateService.addValuesToSet(chanceSourcekey, source.toString());
		}
	}

	/**
	 * 新增登录翻盘机会 .
	 *
	 * @param source
	 *            1app登录 2微信分享成功
	 * @param userId
	 *            用户id
	 */
	public void loginIncrementChance(Long userId, Integer source) {
		String date = DateUtils.dateToString(new Date(), DateUtils.YYYY_MM_DD_HH_MM_SS);

		List<Map<String, Object>> activityIds = queryDao
				.queryMap("select id from cohere_activity where is_valid =1 and begin_time<'" + date
						+ "' and end_time>='" + date + "' ");
		if (CollectionUtils.isNotEmpty(activityIds)) {
			for (Map<String, Object> map : activityIds) {
				Object id = map.get("id");
				if (id == null) {
					continue;
				}
				Number activityId = (Number) id;
				incrementChance(userId, activityId.longValue(), source);
			}
		}

	}

	/**
	 * 减少翻拍次数(翻牌后调用)
	 *
	 * @return 返回剩余次数
	 */
	public int decrementChance(Long userId, Long activityId) {
		String key = CacheKeyConstant.DEBRIS_CHANCE_USED + activityId.toString() + "_" + userId.toString();
		RedisAtomicInteger usedChance = new RedisAtomicInteger(key,
				stringRedisOperateService.getRedisTemplate().getConnectionFactory());
		long seconds = DateUtils.surplusTodaySencods() * 1000;
		usedChance.expire(seconds, TimeUnit.MILLISECONDS);
		usedChance.incrementAndGet();
		return leftChance(userId, activityId);
	}

	/**
	 * 获取剩余次数
	 *
	 * @param userId
	 */
	public int leftChance(Long userId, Long activityId) {
		String chanceSourceKey = CacheKeyConstant.DEBRIS_CHANCE_SOURCE + activityId.toString() + "_"
				+ userId.toString();
		Long allNum = stringRedisOperateService.getSetSize(chanceSourceKey);
		if (null == allNum || allNum.intValue() == 0) {
			return 0;
		}

		String chanceUsedKey = CacheKeyConstant.DEBRIS_CHANCE_USED + activityId.toString() + "_" + userId.toString();
		RedisAtomicInteger usedChanceNum = new RedisAtomicInteger(chanceUsedKey,
				stringRedisOperateService.getRedisTemplate().getConnectionFactory());
		int leftNum = allNum.intValue() - usedChanceNum.get();
		return leftNum;
	}

	/**
	 * 根据活动id获取碎片图片url
	 */
	public List<Map<String, Object>> getDebrisUrlByActivityId(Long activityId) {
		String sql = "select a.num,a.url,a.id,a.title from cohere_debris a where a.is_valid=1 and a.activity_id="
				+ activityId + " and a.num>0 order by a.num";
		return queryDao.queryMap(sql);
	}

	/**
	 * 融合对应碎片
	 *
	 * @param drawIds
	 * @param userId
	 * @param activityId
	 * @return
	 */
	public Map<String, Object> cohere(Long userId, Long activityId, Long... drawIds) {
		clearToCohereData(activityId, userId);
		Map<String, Object> result = new HashMap<>();
		List<CohereDebrisHistory> mids = new ArrayList<>();
		Long coherePrizeHistoryId = 0L;
		mids = cohereDebrisRexp(userId, drawIds);
		if (mids == null) {
			result.put("state", "-5");
			return result;
		}
		CoherePrizeHistory coherePrizeHistory = new CoherePrizeHistory();
		coherePrizeHistory.setState(0);// 用户未申请
		coherePrizeHistory.setUserId(userId);
		coherePrizeHistory.setValid(1);
		coherePrizeHistory.setCreateDate(new Date());
		coherePrizeHistory.setCreateUserId(userId);
		coherePrizeHistory.setUpdateDate(new Date());
		HashSet<Long> list = new HashSet<Long>();
		for (Long drawId : drawIds) {
			try {
				list.add(cohereDrawService.findOne(drawId).getDebrisId());
			} catch (Exception e) {
				result.put("state", "-5");
				return result;
			}
		}
		if (list.size() == 4) { // 融合4片碎片100%成功得到奖品一
			CoherePrize coherePrize = coherePrizeService.getCoherePrizeByActivityId(activityId).get(0);// 得到奖品一
			RedisAtomicInteger prizeCount = new RedisAtomicInteger(
					CacheKeyConstant.COHERE_PRIZE_COUNT + coherePrize.getId(), redisConnectionFactory);
			if (prizeCount.intValue() > 0) {
				int decrementAndGet = prizeCount.decrementAndGet();
				if (decrementAndGet < 0 && decrementAndGet > -99) {
					prizeCount.set(0);
					saveCohereFalse(userId, activityId, result, mids, coherePrizeHistory);
					result.put("state", "0");
					return result; // 未抢到
				}
			} else {
				if ((prizeCount.get() < 0) && (prizeCount.get() > -99)) {
					prizeCount.set(0);
					saveCohereFalse(userId, activityId, result, mids, coherePrizeHistory);
					result.put("state", "0");
					return result; // 未抢到
				} else if (prizeCount.get() == 0) {
					saveCohereFalse(userId, activityId, result, mids, coherePrizeHistory);
					result.put("state", "0");
					return result; // 未抢到
				}
			}
			coherePrizeHistory.setPrizeId(coherePrize.getId());
			coherePrizeHistory.setIsGet(1);
			coherePrizeHistoryId = coherePrizeHistoryService.saveOrUpdate(coherePrizeHistory).getId();
			updateHis(mids, coherePrizeHistoryId);
			this.clearToCohereData(activityId, userId);
			result.put("state", "1");
			result.put("prizeId", coherePrizeHistoryId);
			result.put("prizeName", coherePrize.getName());
			return result;
		} else if (list.size() == 3) { // 融合4片以下
			List<List<String>> probabilityCohere = probabilityCohere(activityId, true);
			if (probabilityCohere.size() == 1) {
				saveCohereFalse(userId, activityId, result, mids, coherePrizeHistory);
				result.put("state", "0");
				return result; // 未抢到
			}
			Integer mid = new Random().nextInt(
					NumberUtils.toInt(probabilityCohere.get(probabilityCohere.size() - 1).get(0).toString())) + 1;
			for (int i = 0; i < probabilityCohere.size() - 1; i++) {
				if (NumberUtils.toInt(probabilityCohere.get(i).get(0)) < mid
						&& NumberUtils.toInt(probabilityCohere.get(i + 1).get(0)) >= mid) {
					if (NumberUtils.toLong(probabilityCohere.get(i).get(1)) == 0) {// 抽中谢谢惠顾
						coherePrizeHistory.setPrizeId(0L);
						coherePrizeHistory.setIsGet(0);
						coherePrizeHistoryId = coherePrizeHistoryService.saveOrUpdate(coherePrizeHistory).getId();
						result.put("state", "0");
						this.clearToCohereData(activityId, userId);
						updateHis(mids, coherePrizeHistoryId);
						return result;
					} else { // 抽中奖品
						CoherePrize coherePrize = coherePrizeService
								.findById(NumberUtils.toLong(probabilityCohere.get(i).get(1)));
						RedisAtomicInteger prizeCount = new RedisAtomicInteger(
								CacheKeyConstant.COHERE_PRIZE_COUNT + coherePrize.getId(), redisConnectionFactory);
						if (prizeCount.intValue() > 0) {
							int decrementAndGet = prizeCount.decrementAndGet();
							if (decrementAndGet < 0 && decrementAndGet > -99) {
								prizeCount.set(0);
								saveCohereFalse(userId, activityId, result, mids, coherePrizeHistory);
								result.put("state", "0");
								return result; // 未抢到
							}
						} else {
							if ((prizeCount.get() < 0) && (prizeCount.get() > -99)) {
								prizeCount.set(0);
								saveCohereFalse(userId, activityId, result, mids, coherePrizeHistory);
								result.put("state", "0");
								return result; // 未抢到
							} else if (prizeCount.get() == 0) {
								saveCohereFalse(userId, activityId, result, mids, coherePrizeHistory);
								result.put("state", "0");
								return result; // 未抢到
							}
						}
						coherePrizeHistory.setIsGet(1);
						coherePrizeHistory.setPrizeId(NumberUtils.toLong(probabilityCohere.get(i).get(1)));
						coherePrizeHistoryId = coherePrizeHistoryService.saveOrUpdate(coherePrizeHistory).getId();
						updateHis(mids, coherePrizeHistoryId);
						probabilityCohere = probabilityCohere(activityId, false);
						result.put("state", "1");
						result.put("prizeId", coherePrizeHistoryId);
						result.put("prizeName", coherePrize.getName());
						return result;
					}
				}
			}
			saveCohereFalse(userId, activityId, result, mids, coherePrizeHistory);
			result.put("state", "0");
			return result; // 未抢到
		}
		result.put("state", "-5");
		return result;
	}

	private void saveCohereFalse(Long userId, Long activityId, Map<String, Object> result,
			List<CohereDebrisHistory> mids, CoherePrizeHistory coherePrizeHistory) {
		Long coherePrizeHistoryId;
		coherePrizeHistory.setPrizeId(0L);
		coherePrizeHistory.setIsGet(0);
		coherePrizeHistoryId = coherePrizeHistoryService.saveOrUpdate(coherePrizeHistory).getId();
		updateHis(mids, coherePrizeHistoryId);
		this.clearToCohereData(activityId, userId);
		result.put("state", "0");
	}

	private void updateHis(List<CohereDebrisHistory> mids, Long coherePrizeHistoryId) {
		for (CohereDebrisHistory midDebris : mids) {
			midDebris.setOutId(coherePrizeHistoryId);
			midDebris.setOutType(1);
			midDebris.setUpdateDate(new Date());
			cohereDebrisHistoryService.saveOrUpdate(midDebris);
		}
	}

	/**
	 * 校验碎片
	 */
	private List<CohereDebrisHistory> cohereDebrisRexp(Long userId, Long... drawIds) {
		List<CohereDebrisHistory> mids = new ArrayList<>();
		CohereDebrisHistory cohereDebrisHistory = null;
		for (Long drawId : drawIds) {
			cohereDebrisHistory = cohereDebrisHistoryService.getPepareCohereDebris(userId, drawId);
			mids.add(cohereDebrisHistory);
			if (cohereDebrisHistory == null) {
				return null;
			}
		}
		return mids;
	}

	/**
	 * 添加到待融合狀態
	 */
	public int toUseCohere(Long activityId, Long drawId, Long userId, int num) {
		String key = CacheKeyConstant.COHERE_DEBRIS_TOUSE + activityId.toString() + "_" + userId.toString();
		String data = stringRedisOperateService.getData(key);
		if (null != data) {
			if (StringUtils.contains(data, num + "_") && (!StringUtils.contains(data, num + "_0"))) {
				return -2;// 已经存在相应序号的碎片待融合
			}
		}
		CohereDebrisHistory cohereDebrisHistory = cohereDebrisHistoryService.findByValidAndUserIdAndDrawIdAndIsUsed(1,
				userId, drawId);
		if (cohereDebrisHistory == null || cohereDebrisHistory.getIsUsed() == null
				|| cohereDebrisHistory.getIsUsed() == 0) {
			return -1;// 碎片信息不存在或已经使用
		}
		upgradeToCohereData(key, data, num, drawId);
		cohereDebrisHistory.setIsUsed(0); // 已经使用该碎片
		cohereDebrisHistory.setOutType(1);// 融合类别
		cohereDebrisHistory = cohereDebrisHistoryService.saveOrUpdate(cohereDebrisHistory);
		return 0;
	}

	public List<Map<String, Object>> currentInUseDebris(Long activityId, Long userId) {
		String key = CacheKeyConstant.COHERE_DEBRIS_TOUSE + activityId.toString() + "_" + userId.toString();
		String data = stringRedisOperateService.getData(key);
		if (StringUtils.isNotBlank(data)) {
			List<Map<String, Object>> result = Lists.newArrayList();
			String[] nds = StringUtils.split(data, ",");
			if (ArrayUtils.isNotEmpty(nds)) {
				for (String nd : nds) {
					String[] singleNd = StringUtils.split(nd, "_");
					int cacheNum = NumberUtils.toInt(singleNd[0]);
					long cacheDrawId = NumberUtils.toLong(singleNd[1]);
					Map<String, Object> map = Maps.newHashMap();
					map.put("num", cacheNum);
					map.put("drawId", cacheDrawId);
					result.add(map);
				}
				return result;
			}
		}
		return null;
	}

	/**
	 * 融合碎片id是否无效
	 *
	 * @param drawIds
	 * @param activityId
	 * @param userId
	 * @return
	 */
	public boolean isInvalidDrawIdToCohere(String[] drawIds, Long activityId, Long userId) {
		String key = CacheKeyConstant.COHERE_DEBRIS_TOUSE + activityId.toString() + "_" + userId.toString();
		String data = stringRedisOperateService.getData(key);
		if (StringUtils.isNotBlank(data)) {
			String[] nds = StringUtils.split(data, ",");
			if (ArrayUtils.isNotEmpty(nds)) {
				for (String nd : nds) {
					boolean currentFlag = false;
					for (String toCheckId : drawIds) {
						boolean contails = StringUtils.endsWith(nd, "_" + toCheckId);
						if (contails) {
							currentFlag = true;
							break;
						}
					}
					if (currentFlag) {
						continue;
					} else {
						return true;
					}
				}
			}
		}
		return false;

	}

	private void upgradeToCohereData(String key, String data, int num, Long drawId) {
		String result = "";
		if (null == data) {
			result = num + "_" + drawId.longValue();
		} else if (StringUtils.contains(data, num + "_")) {
			String[] nds = StringUtils.split(data, ",");// activityId_userId_num_debrisId
			if (ArrayUtils.isNotEmpty(nds)) {
				for (String nd : nds) {
					String[] singleNd = StringUtils.split(nd, "_");
					int cacheNum = NumberUtils.toInt(singleNd[0]);
					if (cacheNum == num) {
						result = result + "," + num + "_" + drawId.longValue();
					} else {
						result = result + "," + nd;
					}
				}
			}
		} else {
			result = data + "," + num + "_" + drawId.longValue();
		}

		stringRedisOperateService.setData(key, result);
	}

	public void clearToCohereData(Long activityId, Long userId) {
		String key = CacheKeyConstant.COHERE_DEBRIS_TOUSE + activityId.toString() + "_" + userId.toString();
		stringRedisOperateService.delData(key);
	}

	/**
	 * 融合碎片生成二维概率区间 二维内容为0时为谢谢惠顾
	 *
	 * @param type
	 *            融合4片以下该值为0，融合4片该值为1
	 * @param activityId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<List<String>> probabilityCohere(Long activityId, boolean isnew) {
		if (!isnew) {
			return (List<List<String>>) objectRedisOperateService.getData("cohere_probability_" + activityId);
		}
		List<List<String>> midList = new ArrayList<List<String>>();
		Integer probability = 0;
		RedisAtomicInteger prizeCount = null;
		List<Map<String, Object>> coherePrizes = coherePrizeService.getCoherePrizeByActivityIdExp(activityId);
		List<String> list = new ArrayList<String>();
		for (Map<String, Object> coherePrize : coherePrizes) {
			prizeCount = new RedisAtomicInteger(CacheKeyConstant.COHERE_PRIZE_COUNT + coherePrize.get("id").toString(),
					redisConnectionFactory);
			if (NumberUtils.toInt(coherePrize.get("probability").toString()) > 0) {
				if (prizeCount.get() == -99 || prizeCount.get() > 0) {
					list.add(probability.toString());
					list.add(coherePrize.get("id").toString());
					probability += NumberUtils.toInt(coherePrize.get("probability").toString());
					midList.add(list);
					list = new ArrayList<String>();
				}
			}
		}
		if (probability < 100) {
			list = new ArrayList<String>();
			list.add(probability.toString());
			list.add("0");
			midList.add(list);
			list = new ArrayList<String>();
			list.add(100 + "");
			list.add("");
			midList.add(list);
		} else {
			list.add(probability.toString());
			list.add("");
			midList.add(list);
		}
		objectRedisOperateService.setData("cohere_probability_" + activityId, midList);
		return midList;
	}

	/**
	 * 索取碎片信息
	 *
	 * @param debrisId
	 * @return
	 */
	public Map<String, Object> getClaimDebris(Long debrisId) {
		String sql = "select   cd.title debrisName, cd.num debrisNum, cp.counts prizeCounts,cp.name prizeName, url debrisUrl,  url_crosswise prizeCrosswiseUrl,  url_vertical prizeVerticalUrl,"
				+ " ca.title activityTitle,  ca.rule activityRule from "
				+ " cohere_debris cd   left join cohere_activity ca     on ca.id = cd.activity_id "
				+ " left join cohere_prize cp     on cp.activity_id = cd.activity_id "
				+ " where cd.is_valid = 1   and cp.is_valid = 1   and ca.is_valid = 1   and cd.id = "
				+ debrisId.toString();
		return queryDao.querySingleMap(sql);
	}

	/**
	 * 微信领取被人分享碎片
	 *
	 * @param userId
	 * @param drawId
	 * @param shareUserId
	 * @param urlCode
	 * @return
	 */
	public int wxGetShareDebris(Long userId, Long drawId, long shareUserId, String urlCode) {
		Map<String, Object> sharedDebris = cohereDebrisHistoryService.getSharedDebris(drawId, shareUserId);
		if (MapUtils.isNotEmpty(sharedDebris)) {
			if (0 == NumberUtils.toInt(sharedDebris.get("status").toString())) {
				Long debrisId = NumberUtils.toLong(sharedDebris.get("debrisId").toString());
				if (debrisId <= 0) {
					return 1;
				}
				Long cdhId = NumberUtils.toLong(sharedDebris.get("id").toString());
				CohereDebrisHistory cdh = cohereDebrisHistoryService.findById(cdhId);
				cdh.setOutType(2);
				cdh.setOutId(userId);
				cohereDebrisHistoryService.saveOrUpdate(cdh);
				CohereDebrisHistory newcdh = new CohereDebrisHistory();
				Date createDate = new Date();
				newcdh.setCreateDate(createDate);
				newcdh.setDrawId(drawId);
				newcdh.setDebrisId(debrisId);
				newcdh.setInType(3);
				newcdh.setInId(shareUserId);
				newcdh.setIsUsed(1);
				newcdh.setUrlCode(urlCode);
				newcdh.setValid(1);
				newcdh.setUserId(userId);
				cohereDebrisHistoryService.saveOrUpdate(newcdh);
				return 0;// 领取成功
			}
		}
		return 1;// 领取失败
	}

	/**
	 * 预先保存该活动下的所有碎片
	 *
	 * @param activityId
	 * @param num
	 * @return
	 */
	public List<CohereDebris> saveAuto(Long activityId, Integer num) {
		CohereDebris cohereDebris = null;
		List<CohereDebris> list = new ArrayList<>();
		for (int i = 0; i < num; i++) {
			cohereDebris = new CohereDebris();
			cohereDebris.setActivityId(activityId);
			cohereDebris.setCreateDate(new Date());
			cohereDebris.setValid(1);
			cohereDebris.setNum(i + 1);
			saveOrUpdate(cohereDebris);
			list.add(cohereDebris);
		}
		return list;
	}

	/**
	 * 返回已发活动下碎片
	 *
	 * @param debrisId
	 * @return
	 */
	public Integer sendDebrisCount(Long debrisId) {
		String sql = "SELECT count(1) FROM master.cohere_draw where debris_id=" + debrisId + " and is_valid=1;";
		Number num = queryDao.query(sql);
		return num.intValue();
	}

	/**
	 * 分享成功后设置碎片已用
	 *
	 * @param userId
	 * @param targetId
	 */
	public void giveProcess(Long userId, long drawId) {
		CohereDebrisHistory debrisHistory = cohereDebrisHistoryService.findByValidAndUserIdAndDrawIdAndIsUsed(1, userId,
				drawId);
		debrisHistory.setIsUsed(0);
		debrisHistory.setOutType(2);// 赠送类别
		debrisHistory.setUpdateDate(new Date());
		cohereDebrisHistoryService.saveOrUpdate(debrisHistory);

	}

}
