package com.miqtech.master.service.event;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Component;

import com.miqtech.master.consts.CacheKeyConstant;
import com.miqtech.master.dao.QueryDao;

@Component
public class EventGroupSeatService {
	@Autowired
	private QueryDao queryDao;
	@Autowired
	private JedisConnectionFactory redisConnectionFactory;

	public boolean saveTargetByAdd(Long roundId, Long targetId) {
		boolean isApply = false;
		RedisAtomicInteger maxseatnumber = new RedisAtomicInteger(CacheKeyConstant.OET_ROUND_SEATNUMBER + roundId,
				redisConnectionFactory);
		String sql = "select b.id from oet_event_group_seat b "
				+ "left join oet_event_group a on a.id=b.group_id " 
				+ "and a.is_valid=1 where "
				+ "a.round_id=" + roundId
				+ " and b.seat_number="+maxseatnumber.incrementAndGet()
				+ " and b.is_valid=1 order by b.id limit 1";
		Map<String, Object> map = queryDao.querySingleMap(sql);
		if(map != null){
			sql = "update oet_event_group_seat set target_id =" + targetId + "  where id =" + map.get("id");
			queryDao.update(sql);
			isApply = true;
		}
		return isApply;
	}
}
