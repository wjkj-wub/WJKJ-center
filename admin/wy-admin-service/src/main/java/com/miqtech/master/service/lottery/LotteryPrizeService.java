package com.miqtech.master.service.lottery;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.dao.lottery.LotteryPrizeDao;
import com.miqtech.master.entity.lottery.LotteryPrize;
import com.miqtech.master.utils.BeanUtils;

@Component
public class LotteryPrizeService {
	@Autowired
	private LotteryPrizeDao lotteryPrizeDao;

	public LotteryPrize findById(Long id) {
		return lotteryPrizeDao.findById(id);
	}

	/**
	 * 查出所有有效奖品
	 */
	public List<LotteryPrize> getAllValid() {
		return lotteryPrizeDao.findAllValid();
	}

	/**
	 * 查出所有无效奖品
	 */
	public List<LotteryPrize> getAllInvalid() {
		return lotteryPrizeDao.findAllInvalid();
	}

	/**
	 * 根据ID查实体
	 */
	public LotteryPrize getLotteryPrizeById(long id) {
		return lotteryPrizeDao.findOne(id);
	}

	/**
	 * 保存
	 */
	public LotteryPrize save(LotteryPrize lotteryPrize) {
		if (lotteryPrize != null) {
			Date now = new Date();
			lotteryPrize.setUpdateDate(now);
			if (lotteryPrize.getId() != null) {
				LotteryPrize old = findById(lotteryPrize.getId());
				lotteryPrize = BeanUtils.updateBean(old, lotteryPrize);
			} else {
				lotteryPrize.setValid(CommonConstant.INT_BOOLEAN_TRUE);
				lotteryPrize.setCreateDate(now);
			}
			return lotteryPrizeDao.save(lotteryPrize);
		}
		return null;
	}

	/**
	 * 更改valid
	 */

	public void updateValid(long id, int valid) {
		LotteryPrize lotteryPrize = getLotteryPrizeById(id);
		if (valid != 1) {
			valid = 0;
		}
		if (lotteryPrize != null) {
			lotteryPrize.setValid(valid);
			lotteryPrize.setUpdateDate(new Date());
			save(lotteryPrize);
		}
	}
}
