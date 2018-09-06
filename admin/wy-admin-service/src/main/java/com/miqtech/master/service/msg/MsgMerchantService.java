package com.miqtech.master.service.msg;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.msg.MsgMerchantDao;
import com.miqtech.master.entity.msg.MsgMerchant;
import com.miqtech.master.entity.netbar.NetbarMerchant;

@Component
public class MsgMerchantService {
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private MsgMerchantDao msgMerchantDao;

	public List<Map<String, Object>> findNewest(NetbarMerchant currentMerchant, int type) {
		String sql;
		Map<String, Object> params = Maps.newHashMap();
		params.put("merchantId", currentMerchant.getId());
		params.put("type", type);
		if (type == 3) {
			sql = " select     t.* from    (select        content, create_date     from        msg_t_merchant     where merchant_id =  :merchantId and type = :type   "
					+ " union   select        content, create_date     from        msg_t_sys msg) t order by create_date desc limit 0, 100  ";
		} else {
			sql = "select  content, create_date  from     msg_t_merchant where merchant_id = :merchantId     and type = :type order by create_date desc limit 0, 100 ";
		}
		return queryDao.queryMap(sql, params);
	}

	public void updateReadStatus(NetbarMerchant currentMerchant, int type) {
		String sql = "update msg_t_merchant set is_read =1 where type = " + type + " and merchant_id="
				+ currentMerchant.getId();
		queryDao.update(sql);

	}

	private final static Joiner JOINER = Joiner.on("_");
	private final static String NEWEST_CULTURAL_BUREAU_NOTIFY_PREFIX = "wy_merchant_cultural_notify_newest";
	private RedisTemplate<String, Long> redisTemplate;

	@Autowired
	public void setRedisTemplate(@Qualifier("defaultRedisTemplate") RedisTemplate<String, Long> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public RedisTemplate<String, Long> getRedisTemplate() {
		return redisTemplate;
	}

	public Long getDataFromCache(Long merchantId) {
		String key = JOINER.join(NEWEST_CULTURAL_BUREAU_NOTIFY_PREFIX, merchantId);
		ValueOperations<String, Long> opsForValue = redisTemplate.opsForValue();
		return opsForValue.get(key);
	}

	public void addDataToCache(Long merchantId, long newestId) {
		String key = JOINER.join(NEWEST_CULTURAL_BUREAU_NOTIFY_PREFIX, merchantId);
		ValueOperations<String, Long> opsForValue = redisTemplate.opsForValue();
		opsForValue.set(key, newestId);
	}

	public Map<String, Object> findNewestCulturalBureauNotifyByMerchantId(Long merchantId) {
		String sql = "select * from msg_t_merchant where merchant_id=" + merchantId
				+ " and type=4 order by id desc limit 0,1";
		return queryDao.querySingleMap(sql);
	}

	public void save(List<MsgMerchant> msgs) {
		msgMerchantDao.save(msgs);
	}

	public List<Map<String, Object>> findContentByMerchantIdAndType(Long merchantId,
			int msgMerchantTypeCulturalBureauNotify) {
		String sql = "select content,create_date from msg_t_merchant where merchant_id=" + merchantId + " and type="
				+ msgMerchantTypeCulturalBureauNotify + " order by create_date desc";
		return queryDao.queryMap(sql);
	}
}