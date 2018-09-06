package com.miqtech.master.admin.web.task;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Component;

import com.miqtech.master.service.cohere.CoherePrizeHistoryService;
import com.miqtech.master.service.common.StringRedisOperateService;

@Component
public class CoherePrizeStatusTask {

	@Autowired
	private StringRedisOperateService stringRedisOperateService;

	@Autowired
	private CoherePrizeHistoryService coherePrizeHistoryService;

	private static final String flagKey = "CoherePrizeStatusTask_updateStatus";

	//	@Scheduled(cron = "0 0 0/5 * * ?")
	public void updateStatus() {
		RedisAtomicInteger flag = new RedisAtomicInteger(flagKey,
				stringRedisOperateService.getRedisTemplate().getConnectionFactory());
		if (flag.intValue() > 0) {
			return;
		}
		flag.incrementAndGet();
		List<Map<String, Object>> ids = coherePrizeHistoryService.findUnFinishedPrize();
		if (CollectionUtils.isNotEmpty(ids)) {
			for (Map<String, Object> map : ids) {
				if (map.containsKey("id")) {
					long id = NumberUtils.toLong(map.get("id").toString());
					coherePrizeHistoryService.updateStatus(id);
				}
			}
		}
		flag.set(0);

	}

}
