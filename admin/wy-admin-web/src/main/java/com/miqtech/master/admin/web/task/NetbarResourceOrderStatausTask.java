package com.miqtech.master.admin.web.task;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Component;

import com.miqtech.master.service.common.StringRedisOperateService;
import com.miqtech.master.service.netbar.resource.NetbarResourceOrderService;

@Component
public class NetbarResourceOrderStatausTask {

	private final static String REDIS_KEY_UNCONFIRM_OPERATE = "wy_netbar_resource_unconfirm_operate";
	private final static String REDIS_KEY_EXPIRED_OPERATE = "wy_netbar_resource_expired_operate";
	private final static String REDIS_KEY_EXPIRED_REDBAG_OPERATE = "wy_netbar_resource_expired_operate";

	private final static String REDIS_KEY_EXPIRED_VALUE_ADDED_OPERATE = "wy_netbar_resource_value_added_operate";

	@Autowired
	private StringRedisOperateService stringRedisOperateService;
	@Autowired
	private NetbarResourceOrderService netbarResourceOrderService;

	/**
	 * 取消未确认或过期订单(每小时做一次检查)
	 */
	//@Scheduled(cron = "0 30 */1 * * ?")
	public void cancelUnconfirmedOrders() {
		if (isOperable(REDIS_KEY_UNCONFIRM_OPERATE)) {
			netbarResourceOrderService.cancelUnconfirmedOrders();
		}
	}

	/**
	 * 取消有效期过期订单(每天1点执行一次检查)
	 */
	//@Scheduled(cron = "0 0 1 * * ?")
	public void cancelExpiredOrders() {
		if (isOperable(REDIS_KEY_EXPIRED_OPERATE)) {
			netbarResourceOrderService.cancelExpiredOrders();
		}
	}

	/**
	 * 取消过期红包,并回滚金额(每天2点执行一次)
	 */
	//@Scheduled(cron = "0 0 2 * * ?")
	public void cancelExpiredRedbags() {
		if (isOperable(REDIS_KEY_EXPIRED_REDBAG_OPERATE)) {
			netbarResourceOrderService.refundExpiredRedbag();
		}
	}

	/**
	 * 取消过期增值券,并回滚金额(每天0点30执行一次)
	 */
	//@Scheduled(cron = "0 30 0 * * ?")
	public void cancelExpiredValueAdded() {
		if (isOperable(REDIS_KEY_EXPIRED_VALUE_ADDED_OPERATE)) {
			netbarResourceOrderService.refundExpiredValueAdded();
		}
	}

	/**
	 * 检查某项操作是否为第一次执行(30min内),避免多个实例重复执行
	 */
	private boolean isOperable(String key) {
		RedisConnectionFactory factory = stringRedisOperateService.getRedisTemplate().getConnectionFactory();
		RedisAtomicInteger operateTime = new RedisAtomicInteger(key, factory);
		Long expire = operateTime.getExpire();
		if (expire == null || expire <= 0) {
			operateTime.expire(30, TimeUnit.MINUTES);
		}

		return operateTime.incrementAndGet() <= 1;
	}

}
