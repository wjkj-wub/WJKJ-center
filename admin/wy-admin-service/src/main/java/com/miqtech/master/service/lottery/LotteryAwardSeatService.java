package com.miqtech.master.service.lottery;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.lottery.LotteryAwardSeatDao;
import com.miqtech.master.entity.lottery.LotteryAwardSeat;
import com.miqtech.master.utils.BeanUtils;
import com.miqtech.master.utils.SqlJoiner;

@Component
public class LotteryAwardSeatService {

	@Autowired
	private QueryDao queryDao;
	@Autowired
	private LotteryAwardSeatDao lotteryAwardSeatDao;

	/**
	 * 通过ID查询
	 */
	public LotteryAwardSeat findById(Long id) {
		return lotteryAwardSeatDao.findById(id);
	}

	/**
	 * 保存
	 */
	public LotteryAwardSeat save(LotteryAwardSeat awardSeat) {
		if (awardSeat != null) {
			Date now = new Date();
			awardSeat.setUpdateDate(now);
			if (awardSeat.getId() == null) {
				awardSeat.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				awardSeat.setCreateDate(now);
			} else {
				LotteryAwardSeat oldSetting = findById(awardSeat.getId());
				if (oldSetting != null) {
					awardSeat = BeanUtils.updateBean(oldSetting, awardSeat);
				}
			}
			return lotteryAwardSeatDao.save(awardSeat);
		}
		return null;
	}

	/**
	 * 查询余量足够的奖项及奖品信息（数据库计算）
	 */
	public List<Map<String, Object>> getEnoughAwards(Long lotteryId) {
		String sql = SqlJoiner
				.join("SELECT las.seat,a.id awardId, a.prize_id prizeId, a.name awardName, a.inventory, a.real_inventory realInventory, a.probablity,",
						" p.name prizeName, p.icon prizeIcon, p.price prizePrice, count(h.id) winCount, if(a.real_inventory = -1, -1, (a.real_inventory - count(h.id))) surplusCount",
						" FROM lottery_t_award_seat las LEFT JOIN lottery_t_award a ON las.award_id = a.id",
						" LEFT JOIN lottery_t_prize p ON a.prize_id = p.id AND p.is_valid = 1",
						" LEFT JOIN lottery_t_history h ON a.id = h.award_id AND h.is_win = 1 AND h.lottery_id = a.lottery_id",
						" WHERE las.lottery_id = :lotteryId AND a.is_valid = 1 AND a.probablity is not NULL AND a.probablity > 0",
						" GROUP BY las.id, a.id HAVING surplusCount > 0 or surplusCount = -1 ORDER BY las.seat ASC")
				.replaceAll(":lotteryId", lotteryId.toString());
		return queryDao.queryMap(sql);
	}

	/**
	 * 查询活动下的所有盘格设置
	 */
	public List<Map<String, Object>> findByLotteryId(Long lotteryId) {
		String sql = SqlJoiner.join("SELECT DISTINCT id, lottery_id lotteryId, award_id awardId, seat",
				" FROM lottery_t_award_seat", " WHERE is_valid = 1 AND lottery_id = :lotteryId GROUP BY seat")
				.replaceAll(":lotteryId", lotteryId.toString());
		return queryDao.queryMap(sql);
	}

	/**
	 * 删除活动的盘格设置
	 */
	public void deleteOldSetting(Long lotteryId, Integer seat) {
		String sql = "UPDATE lottery_t_award_seat SET is_valid = 0, update_date = NOW() WHERE lottery_id = :lotteryId AND seat = :seat AND is_valid = 1"
				.replaceAll(":lotteryId", lotteryId.toString()).replaceAll(":seat", seat.toString());
		queryDao.update(sql);
	}
}
