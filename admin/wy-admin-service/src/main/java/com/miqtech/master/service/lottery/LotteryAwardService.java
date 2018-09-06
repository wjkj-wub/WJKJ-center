package com.miqtech.master.service.lottery;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.LotteryConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.lottery.LotteryAwardDao;
import com.miqtech.master.entity.lottery.LotteryAward;
import com.miqtech.master.entity.lottery.LotteryHistory;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.PageUtils;
import com.miqtech.master.utils.SqlJoiner;
import com.miqtech.master.vo.PageVO;

@Component
public class LotteryAwardService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private StringRedisOperateService stringRedisOperateService;
	@Autowired
	private LotteryAwardDao lotteryAwardDao;
	@Autowired
	private LotteryChanceService lotteryChanceService;
	@Autowired
	private LotteryHistoryService lotteryHistoryService;
	@Autowired
	private LotteryAwardSeatService lotteryAwardSeatService;

	public LotteryAward findById(Long id) {
		return lotteryAwardDao.findById(id);
	}

	public LotteryAward findValidById(Long id) {
		return lotteryAwardDao.findByIdAndValid(id, CommonConstant.INT_BOOLEAN_TRUE);
	}

	public List<LotteryAward> findByLotteryIdAndName(Long lotteryId, String name) {
		return lotteryAwardDao.findByLotteryIdAndName(lotteryId, name);
	}

	/**
	 * 后台管理：查出所有奖项，分页
	 */
	public PageVO page(int page, Map<String, Object> params) {
		String sqlQuery = SqlJoiner
				.join("select a.id, a.lottery_id lotteryId, o.name lotteryName, p.name prizeName, a.prize_id prizeId, a.name, a.inventory, a.real_inventory realInventory, a.probablity, a.virtual_winners virtualWinners, a.is_valid valid",
						" from lottery_t_award a", " left join lottery_t_option o on o.id=a.lottery_id",
						" left join lottery_t_prize p on p.id=a.prize_id", " where 1=1");
		String sqlTotal = "select count(1) from lottery_t_award where 1=1";
		if (params == null) {
			params = Maps.newHashMap();
		}
		if (params.get("valid") != null) {
			sqlQuery = SqlJoiner.join(sqlQuery, " and a.is_valid=:valid");
			sqlTotal = SqlJoiner.join(sqlTotal, " and is_valid=" + params.get("valid"));
		} else {
			sqlQuery = SqlJoiner.join(sqlQuery, " and a.is_valid=1");
			sqlTotal = SqlJoiner.join(sqlTotal, " and is_valid=1");
		}
		if (params.get("lotteryId") != null) {
			sqlQuery = SqlJoiner.join(sqlQuery, " and a.lottery_id=:lotteryId");
			sqlTotal = SqlJoiner.join(sqlTotal, " and lottery_id=" + params.get("lotteryId"));
		}
		if (page < 1) {
			page = 1;
		}
		sqlQuery = SqlJoiner.join(sqlQuery, " order by a.order asc limit :page, :row");

		params.put("page", (page - 1) * PageUtils.ADMIN_DEFAULT_PAGE_SIZE);
		params.put("row", PageUtils.ADMIN_DEFAULT_PAGE_SIZE);

		PageVO pageVO = new PageVO();
		pageVO.setList(queryDao.queryMap(sqlQuery, params));
		Number total = queryDao.query(sqlTotal);
		if (total != null) {
			pageVO.setTotal(total.intValue());
		}

		return pageVO;
	}

	/**
	 * 根据ID查实体
	 */
	public LotteryAward getLotteryWardById(long id) {
		return lotteryAwardDao.findOne(id);
	}

	/**
	 * 保存
	 */
	public LotteryAward save(LotteryAward lotteryAward) {
		if (lotteryAward != null) {
			Date now = new Date();
			lotteryAward.setUpdateDate(now);
			if (lotteryAward.getId() != null) {
				LotteryAward old = findById(lotteryAward.getId());
				if (old != null) {
					lotteryAward = BeanUtils.updateBean(old, lotteryAward);
				}
			} else {
				lotteryAward.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				lotteryAward.setCreateDate(now);
			}
			return lotteryAwardDao.save(lotteryAward);
		}
		return null;
	}

	/**
	 * 更改valid
	 */

	public void updateValid(long id, int valid) {
		LotteryAward lotteryAward = getLotteryWardById(id);
		if (valid != 1) {
			valid = 0;
		}
		if (lotteryAward != null) {
			lotteryAward.setValid(valid);
			lotteryAward.setUpdateDate(new Date());
			save(lotteryAward);
		}
	}

	/**
	 * 查询余量足够的奖项及奖品信息（数据库计算）
	 */
	public List<Map<String, Object>> getEnoughAwards() {
		String sql = SqlJoiner
				.join("SELECT a.id awardId, a.prize_id prizeId, a.name awardName, a.inventory, a.real_inventory realInventory, a.probablity,",
						" p. NAME prizeName, p.price prizePrice, count(h.id) winCount, if(a.real_inventory = -1, -1, (a.real_inventory - count(h.id))) surplusCount",
						" FROM lottery_t_award a",
						" LEFT JOIN lottery_t_prize p ON a.prize_id = p.id AND p.is_valid = 1",
						" LEFT JOIN lottery_t_history h ON a.id = h.award_id AND h.is_win = 1 AND h.lottery_id = a.lottery_id",
						" WHERE a.is_valid = 1 AND a.probablity is not NULL AND a.probablity > 0",
						" GROUP BY a.id HAVING surplusCount > 0 or surplusCount = -1 ORDER BY a.order ASC");
		return queryDao.queryMap(sql);
	}

	/**
	 * 抽奖
	 */
	public Map<String, Object> draw(Long lotteryId, Long userId) {
		int getAwardTime = 0;
		int maxGetAwardTime = 3;
		Boolean getted = null;
		boolean usedChance = false;
		Map<String, Object> award = null;
		while (!BooleanUtils.isTrue(getted) && getAwardTime < maxGetAwardTime) {
			// 匹配出一个奖项
			award = getAwards(lotteryId);
			getAwardTime += 1;

			Boolean emptyAwards = (Boolean) award.get("emptyAwards");
			if (BooleanUtils.isTrue(emptyAwards)) {// 数据库中已没有余量重组的奖项，不再循环
				getAwardTime = maxGetAwardTime;
			}

			// 使用一次用户的抽奖机会
			if (!usedChance) {
				lotteryChanceService.addUserChance(userId, lotteryId, -1);
				usedChance = true;
			}

			// 检查奖项在redis中的余量是否足够（不够重新抽，最多3次）
			getted = (Boolean) award.get("getted");
			if (BooleanUtils.isTrue(getted)) {
				Number realInventory = (Number) award.get("realInventory");

				Number awardId = (Number) award.get("awardId");
				Joiner joiner = Joiner.on("_");
				String surplusKey = joiner.join(LotteryConstant.REDIS_SURPLUS_LOTTERY_AWARD, lotteryId.toString(),
						awardId.toString());
				RedisConnectionFactory factory = stringRedisOperateService.getRedisTemplate().getConnectionFactory();
				RedisAtomicInteger surplus = new RedisAtomicInteger(surplusKey, factory);
				// 保存用户抽奖历史记录
				if (realInventory.equals(-1) || surplus.decrementAndGet() >= 0) {
					// 余量足够
					Number prizeId = (Number) award.get("prizeId");
					LotteryHistory h = new LotteryHistory();

					h.setUserId(userId);
					h.setLotteryId(lotteryId);
					h.setAwardId(awardId.longValue());
					h.setPrizeId(prizeId.longValue());
					h.setIsWin(CommonConstant.INT_BOOLEAN_TRUE);
					lotteryHistoryService.save(h);
				} else {
					// 余量不足
					surplus.set(0);
					getted = false;
					award = null;

					if (getAwardTime >= maxGetAwardTime - 1) {// 最后一次失败，保存失败记录
						LotteryHistory h = new LotteryHistory();
						h.setUserId(userId);
						h.setLotteryId(lotteryId);
						h.setIsWin(CommonConstant.INT_BOOLEAN_FALSE);
						lotteryHistoryService.save(h);
					}
				}
			}
		}

		// 返回是否抽中状态，及相关属性
		if (MapUtils.isNotEmpty(award)) {
			return award;
		} else {
			Map<String, Object> result = Maps.newHashMap();
			result.put("getted", false);
			result.put("msg", "很遗憾，未抽中奖项");
			return result;
		}
	}

	/**
	 * 从奖项列表中随机取得一个作为中奖项
	 */
	private Map<String, Object> getAwards(Long lotteryId) {
		Map<String, Object> result = Maps.newHashMap();

		// 获取余量足够的所有奖项及奖品信息
		List<Map<String, Object>> enoughAwards = lotteryAwardSeatService.getEnoughAwards(lotteryId);
		if (CollectionUtils.isEmpty(enoughAwards)) {
			result.put("getted", false);
			result.put("emptyAwards", true);
			result.put("msg", "所有奖项都已发完,下次请早些参加");
			return result;
		}

		// 根据概率，排序为小到大序列
		Collections.sort(enoughAwards, new Comparator<Map<String, Object>>() {
			@Override
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				String probablityKey = "probablity";
				Number p1 = (Number) o1.get(probablityKey);
				Number p2 = (Number) o2.get(probablityKey);
				if (p1.equals(p2)) {
					return 0;
				}

				return p1.intValue() - p2.intValue();
			}
		});

		// 在概率范围内产生随机数
		int sumProbablity = 0;
		String probablityKey = "probablity";
		for (Map<String, Object> a : enoughAwards) {
			Number p = (Number) a.get(probablityKey);
			sumProbablity += p.intValue();
		}
		Random random = new Random();
		int r = random.nextInt(sumProbablity);

		// 根据随机数取得奖项
		int floorProbablity = 0;
		if (CollectionUtils.isNotEmpty(enoughAwards)) {
			for (Map<String, Object> a : enoughAwards) {
				Number p = (Number) a.get(probablityKey);
				if (r >= floorProbablity && r < (floorProbablity += p.intValue())) {
					a.put("getted", true);
					a.put("msg", "成功");
					return a;
				}
			}
		}

		result.put("getted", false);
		result.put("msg", "未抽中奖项，请重试");
		return result;
	}

	/**
	 * 通过活动ID查询活动下的所有奖项
	 */
	public List<Map<String, Object>> findValidByLotteryId(Long lotteryId) {
		String sql = SqlJoiner
				.join("SELECT a.id, a. NAME awardName, a.inventory, a.real_inventory realInventroy, a.probablity, a.virtual_winners virtualWinners, p. NAME prizeName",
						" FROM lottery_t_award a LEFT JOIN lottery_t_prize p ON a.prize_id = p.id AND p.is_valid = 1",
						" WHERE a.lottery_id = :lotteryId AND a.is_valid = 1").replaceAll(":lotteryId",
						lotteryId.toString());
		return queryDao.queryMap(sql);
	}

	/**
	 * 获取转盘活动下设置的所有奖项
	 */
	public List<Map<String, Object>> getValidAwardsByLotteryId(Long lotteryId) {
		String sql = SqlJoiner
				.join("SELECT award.*, count(history.historyId) useCount, if(award.realInventory = -1, -1, (award.inventory - count(history.historyId) - award.virtual_winners)) surplusCount FROM (",
						" SELECT DISTINCT a.lottery_id lotteryId, a.id awardId, a.name awardName, a.inventory awardInventory, a.real_inventory realInventory, a.probablity,",
						" p.name prizeName, p.icon prizeIcon, p.price prizePrice, a.create_date createDate, a.`order`, a.inventory, a.virtual_winners",
						" FROM lottery_t_award_seat las LEFT JOIN lottery_t_award a ON las.award_id = a.id LEFT JOIN lottery_t_prize p ON a.prize_id = p.id",
						" WHERE las.lottery_id = :lotteryId ORDER BY a.create_date ASC",
						" ) award LEFT JOIN (",
						" SELECT id historyId, lottery_id lotteryId, award_id FROM lottery_t_history WHERE is_win = 1",
						" ) history ON award.awardId = history.award_id AND award.lotteryId = history.lotteryId GROUP BY awardId ORDER BY award.`order` ASC")
				.replaceAll(":lotteryId", lotteryId.toString());
		return queryDao.queryMap(sql);
	}
}
