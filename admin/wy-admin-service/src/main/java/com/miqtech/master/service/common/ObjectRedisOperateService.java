package com.miqtech.master.service.common;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

@Component
public class ObjectRedisOperateService {

	public RedisTemplate<String, Object> getRedisTemplate() {
		return redisTemplate;
	}

	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	public void setRedisTemplate(@Qualifier("objectRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public void setData(String key, Object value) {
		ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();
		opsForValue.set(key, value);
	}

	public void setData(String key, Object value, long time, TimeUnit timeUnit) {
		ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();
		opsForValue.set(key, value, time, timeUnit);
	}

	public Object getData(String key) {
		ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();
		Object serializable = opsForValue.get(key);
		return serializable;
	}

	public void delData(String key) {
		redisTemplate.delete(key);
	}

	public Long getSetSize(String key) {
		SetOperations<String, Object> opsForSet = redisTemplate.opsForSet();
		return opsForSet.size(key);
	}

	public Set<Object> getSetValues(String key) {
		SetOperations<String, Object> opsForSet = redisTemplate.opsForSet();
		Set<Object> members = opsForSet.members(key);
		return members;
	}

	public void addValuesToSet(String key, Object... value) {
		SetOperations<String, Object> opsForSet = redisTemplate.opsForSet();
		opsForSet.add(key, value);
	}

	public boolean setContainsValue(String key, Object value) {
		SetOperations<String, Object> opsForSet = redisTemplate.opsForSet();
		return opsForSet.isMember(key, value);
	}

	public void removeSetValue(String key, Object... value) {
		SetOperations<String, Object> opsForSet = redisTemplate.opsForSet();
		opsForSet.remove(key, value);
	}

	public boolean setIfAbsent(String key, Object value) {
		ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();
		return opsForValue.setIfAbsent(key, value);
	}

	public Object getAndSet(String key, Object value) {
		ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();
		return opsForValue.getAndSet(key, value);
	}

	public List<Object> getListValues(String key) {
		ListOperations<String, Object> opsForList = redisTemplate.opsForList();
		return opsForList.range(key, 0, -1);
	}

	public void pushListValue(String key, Object value) {
		ListOperations<String, Object> opsForList = redisTemplate.opsForList();
		opsForList.rightPush(key, value);
	}

	public void setListValue(String key, List<Object> list) {
		ListOperations<String, Object> opsForList = redisTemplate.opsForList();
		opsForList.rightPushAll(key, list);
	}

	public void removeListValue(String key, Object value) {
		ListOperations<String, Object> opsForList = redisTemplate.opsForList();
		opsForList.remove(key, -1, value);
	}

	public Boolean expire(String key, final long timeout, final TimeUnit unit) {
		return redisTemplate.expire(key, timeout, unit);
	}
}
