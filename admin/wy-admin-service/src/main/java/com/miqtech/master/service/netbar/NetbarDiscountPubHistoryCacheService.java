package com.miqtech.master.service.netbar;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;
import com.miqtech.master.utils.DateUtils;

/**
 * 网吧优惠信息发布历史频次控制service
 */
@Component
public class NetbarDiscountPubHistoryCacheService {

	private final static Joiner JOINER = Joiner.on("_");
	private final static String PLATFORM_KEY_PREFIX = "master_netbar_diacount_platform_";
	private final static String MEMBER_KEY_PREFIX = "master_netbar_diacount_member_";

	private RedisTemplate<Serializable, Serializable> redisTemplate;

	@Autowired
	public void setRedisTemplate(
			@Qualifier("defaultRedisTemplate") RedisTemplate<Serializable, Serializable> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public RedisTemplate<Serializable, Serializable> getRedisTemplate() {
		return redisTemplate;
	}

	public void addSendedToPlatformToCache(Long netbarId) {
		addDataToCache(PLATFORM_KEY_PREFIX, netbarId);
	}

	public boolean canSendToPlatform(Long netbarId) {
		return getData(PLATFORM_KEY_PREFIX, netbarId) == null;
	}

	public void addSendedToMemberToCache(Long netbarId) {
		addDataToCache(MEMBER_KEY_PREFIX, netbarId);
	}

	public boolean canSendToMember(Long netbarId) {
		return getData(MEMBER_KEY_PREFIX, netbarId) == null;
	}

	private void addDataToCache(String prefix, Long netbarId) {
		String key = joinKey(prefix, netbarId);
		ValueOperations<Serializable, Serializable> opsForValue = redisTemplate.opsForValue();
		opsForValue.set(key, netbarId, 24, TimeUnit.HOURS);
	}

	private Serializable getData(String prefix, Long netbarId) {
		String key = joinKey(prefix, netbarId);
		ValueOperations<Serializable, Serializable> opsForValue = redisTemplate.opsForValue();
		Serializable serializable = opsForValue.get(key);
		return serializable;
	}

	private String joinKey(String prefix, Long netbarId) {
		return JOINER.join(prefix, DateUtils.dateToString(new Date(), DateUtils.YYYY_MM_DD), netbarId);
	}

}