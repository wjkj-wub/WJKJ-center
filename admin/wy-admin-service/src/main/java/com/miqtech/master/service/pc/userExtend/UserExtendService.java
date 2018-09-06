package com.miqtech.master.service.pc.userExtend;

import com.miqtech.master.consts.CommonConstant;
import com.miqtech.master.consts.pc.UserExtendConstant;
import com.miqtech.master.dao.pc.detail.ChipDetailDao;
import com.miqtech.master.dao.pc.detail.CoinDetailDao;
import com.miqtech.master.dao.pc.userExtend.UserExtendDao;
import com.miqtech.master.entity.pc.detail.ChipDetail;
import com.miqtech.master.entity.pc.detail.CoinDetail;
import com.miqtech.master.entity.pc.userExtends.UserExtend;
import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.utils.SqlJoiner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 用户娱币积分操作表
 *
 * @author zhangyuqi
 * @create 2017年09月08日
 */
@Service
public class UserExtendService {

	@Autowired
	private RedisConnectionFactory redisConnectionFactory;
	@Autowired
	private StringRedisOperateService redisOperateService;
	@Autowired
	private ChipDetailDao chipDetailDao;
	@Autowired
	private CoinDetailDao coinDetailDao;
	@Autowired
	private UserExtendDao userExtendDao;

	/**
	 * 查询用户扩展信息
	 */
	public UserExtend queryUserExtendById(Long userId) {
		return userExtendDao.findByUserIdAndValid(userId, CommonConstant.INT_BOOLEAN_TRUE);
	}

	/**
	 * 变更用户账户的money，并保存记录历史
	 * @param feeType		费用类型：对应CommonConstant.PC_FEE_TYPE_*
	 * @param direction		1：新增，-1：扣除
	 * @param tradeNo		订单号
	 */
	public boolean saveCoinOrChipHistory(Long userId, Byte feeType, Integer feeAmount, Byte direction, String tradeNo) {
		boolean result = false;
		if (CommonConstant.PC_FEE_TYPE_CHIP.equals(feeType)) {
			result = this.changeChip(userId, feeAmount, direction);
			if (result) {
				ChipDetail chipDetail = new ChipDetail();
				chipDetail.setUserId(userId);
				chipDetail.setType(UserExtendConstant.CHIP_TYPE_TASK_MATCH);
				chipDetail.setTradeNo(tradeNo);
				chipDetail.setAmount(feeAmount);
				chipDetail.setDirection(direction);
				chipDetail.setState((byte) 1);
				chipDetail.setIsValid((byte) 1);
				chipDetail.setCreateDate(new Date());
				chipDetailDao.save(chipDetail);
			}
		} else if (CommonConstant.PC_FEE_TYPE_COIN.equals(feeType)) {
			result = this.changeCoin(userId, feeAmount, direction);
			if (result) {
				CoinDetail coinDetail = new CoinDetail();
				coinDetail.setUserId(userId);
				coinDetail.setType(UserExtendConstant.COIN_TYPE_TASK_MATCH);
				coinDetail.setTradeNo(tradeNo);
				coinDetail.setAmount(feeAmount);
				coinDetail.setDirection(direction);
				coinDetail.setState((byte) 1);
				coinDetail.setIsValid((byte) 1);
				coinDetail.setCreateDate(new Date());
				coinDetailDao.save(coinDetail);
			}
		}
		return result;
	}

	/**
	 * 积分处理 direction=1 新增积分,direction=-1 扣除积分
	 */
	private boolean changeCoin(Long userId, int count, int direction) {
		RedisAtomicInteger coin = new RedisAtomicInteger(
				SqlJoiner.join(CommonConstant.REDIS_USER_COIN, userId.toString()), redisConnectionFactory);
		RedisAtomicInteger userAdd = new RedisAtomicInteger(SqlJoiner.join("userchongfucoin", userId.toString()),
				redisConnectionFactory);
		userAdd.expire(10, TimeUnit.SECONDS);
		if (userAdd.get() > 0) {
			return false;
		}
		userAdd.incrementAndGet();
		UserExtend userExtend = queryUserExtendById(userId);
		if (userExtend == null) {
			return false;
		}

		if (coin.get() <= 0) {
			coin.set(userExtend.getCoin());
		}

		if (direction < 0) { // 扣费
			if (coin.get() < count) {
				return false;
			}
		}
		count = count * direction;
		userExtend.setCoin(userExtend.getCoin() + count);
		userExtend = userExtendDao.save(userExtend);
		if (userExtend != null) {
			coin.addAndGet(count);
			redisOperateService.leftPushListValue("money_cue",
					"{\"userId\":" + userId + ",\"type\":1,\"amount\":" + coin.get() + "}");
			userAdd.set(0);
			return true;
		}
		return false;
	}

	/**
	 * 娱币处理 direction=1 新增娱币,direction=-1 扣除娱币
	 */
	private boolean changeChip(Long userId, int count, int direction) {
		RedisAtomicInteger chip = new RedisAtomicInteger(
				SqlJoiner.join(CommonConstant.REDIS_USER_CHIP, userId.toString()), redisConnectionFactory);
		RedisAtomicInteger userAdd = new RedisAtomicInteger(SqlJoiner.join("userchongfuchip", userId.toString()),
				redisConnectionFactory);
		userAdd.expire(10, TimeUnit.SECONDS);
		if (userAdd.get() > 0) {
			return false;
		}
		userAdd.incrementAndGet();
		UserExtend userExtend = this.queryUserExtendById(userId);
		if (chip.get() <= 0) {
			chip.set(userExtend == null ? 0 : userExtend.getChip());
		}

		if (direction < 0) { // 扣费
			if (chip.get() < count) {
				return false;
			}
		}

		count = count * direction;
		if (userExtend == null) {
			return false;
		}
		userExtend.setCoin(userExtend.getChip() + count);
		userExtend = userExtendDao.save(userExtend);
		if (userExtend != null) {
			chip.addAndGet(count);
			redisOperateService.leftPushListValue("money_cue",
					"{\"userId\":" + userId + ",\"type\":2,\"amount\":" + chip.get() + "}");
			userAdd.set(0);
			return true;
		}
		return false;
	}

}
