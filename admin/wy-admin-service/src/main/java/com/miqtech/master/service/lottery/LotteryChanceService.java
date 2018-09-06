package com.miqtech.master.service.lottery;

import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.lottery.LotteryChanceDao;
import com.miqtech.master.entity.lottery.LotteryChance;
import com.miqtech.master.utils.SqlJoiner;

@Component
public class LotteryChanceService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private LotteryChanceDao lotteryChanceDao;

	/**
	 * 初始化用户挑战机会
	 */
	public void initUserChance(Long userId, List<LotteryChance> chances) {
		String updateSql = "DELETE FROM lottery_t_chance WHERE user_id = 1";
		queryDao.update(updateSql);

		lotteryChanceDao.save(chances);
	}

	/**
	 * 查询用户在某个抽奖活动中的抽奖的次数
	 */
	public int getUserChance(Long lotteryId, Long userId) {
		String sql = SqlJoiner
				.join("SELECT chance FROM lottery_t_chance",
						" WHERE user_id = :userId AND lottery_id = :lotteryId AND is_valid = 1")
				.replaceAll(":userId", userId.toString()).replaceAll(":lotteryId", lotteryId.toString());

		Number chance = queryDao.query(sql);
		if (chance == null) {
			chance = 0;
		}

		return chance.intValue();
	}

	/**
	 * 增加 或 减少（负数）用户的抽奖机会
	 */
	public LotteryChance addUserChance(Long userId, Long lotteryId, Integer chance) {
		if (userId == null || lotteryId == null) {
			return null;
		}
		if (chance == null) {
			chance = 1;
		}

		List<LotteryChance> userChances = lotteryChanceDao.findByLotteryIdAndUserId(lotteryId, userId);
		LotteryChance c = null;
		if (CollectionUtils.isNotEmpty(userChances)) {
			c = userChances.get(0);
			if (c.getChance() == null) {
				c.setChance(1);
			}

			c.setChance(c.getChance() + chance);
		} else {
			c = new LotteryChance();
			c.setLotteryId(lotteryId);
			c.setUserId(userId);
			c.setChance(chance);
			c.setValid(CommonConstant.INT_BOOLEAN_TRUE);
			Date now = new Date();
			c.setUpdateDate(now);
			c.setCreateDate(now);
		}

		return lotteryChanceDao.save(c);
	}
}
