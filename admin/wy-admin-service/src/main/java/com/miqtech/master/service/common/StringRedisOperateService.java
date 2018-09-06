package com.miqtech.master.service.common;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class StringRedisOperateService {

	public RedisTemplate<String, String> getRedisTemplate() {
		return redisTemplate;
	}

	private RedisTemplate<String, String> redisTemplate;

	@Autowired
	public void setRedisTemplate(@Qualifier("stringRedisTemplate") RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public Set<String> getKeys(String pattern) {
		return redisTemplate.keys(pattern);
	}

	public boolean verify(String key, String code) {
		String data = getData(key);
		if (StringUtils.equals(code, data)) {
			//delData(key);
			return true;
		}
		return false;
	}

	public void setData(String key, String value) {
		ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
		opsForValue.set(key, value);
	}

	public void setData(String key, String value, long time, TimeUnit timeUnit) {
		ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
		opsForValue.set(key, value, time, timeUnit);
	}

	public String getData(String key) {

		ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
		String serializable = opsForValue.get(key);
		return serializable;
	}

	public void delData(String key) {
		redisTemplate.delete(key);
	}

	public Long getSetSize(String key) {
		SetOperations<String, String> opsForSet = redisTemplate.opsForSet();
		return opsForSet.size(key);
	}

	public Set<String> getSetValues(String key) {
		SetOperations<String, String> opsForSet = redisTemplate.opsForSet();
		Set<String> members = opsForSet.members(key);
		return members;
	}

	public Boolean isSetMember(String key, String value) {
		SetOperations<String, String> opsForSet = redisTemplate.opsForSet();
		return opsForSet.isMember(key, value);
	}

	public void addValuesToSet(String key, String... value) {
		SetOperations<String, String> opsForSet = redisTemplate.opsForSet();
		opsForSet.add(key, value);
	}

	public boolean setContainsValue(String key, String value) {
		SetOperations<String, String> opsForSet = redisTemplate.opsForSet();
		return opsForSet.isMember(key, value);
	}

	public void removeSetValue(String key, Object... value) {
		SetOperations<String, String> opsForSet = redisTemplate.opsForSet();
		opsForSet.remove(key, value);
	}

	public boolean setIfAbsent(String key, String value) {
		ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
		return opsForValue.setIfAbsent(key, value);
	}

	public String getAndSet(String key, String value) {
		ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
		return opsForValue.getAndSet(key, value);
	}

	public List<String> getListValues(String key) {
		ListOperations<String, String> opsForList = redisTemplate.opsForList();
		return opsForList.range(key, 0, -1);
	}

	public void rightPushListValue(String key, String value) {
		ListOperations<String, String> opsForList = redisTemplate.opsForList();
		opsForList.rightPush(key, value);
	}

	public void rightSetListValue(String key, List<String> list) {
		ListOperations<String, String> opsForList = redisTemplate.opsForList();
		opsForList.rightPushAll(key, list);
	}

	public void leftPushListValue(String key, String value) {
		ListOperations<String, String> opsForList = redisTemplate.opsForList();
		opsForList.leftPush(key, value);
	}

	public void lefttSetListValue(String key, List<String> list) {
		ListOperations<String, String> opsForList = redisTemplate.opsForList();
		opsForList.leftPushAll(key, list);
	}

	public void removeListValue(String key, String value) {
		ListOperations<String, String> opsForList = redisTemplate.opsForList();
		opsForList.remove(key, -1, value);
	}

	public Boolean expire(String key, final long timeout, final TimeUnit unit) {
		return redisTemplate.expire(key, timeout, unit);
	}
}
