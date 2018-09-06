package com.miqtech.master.admin.web.task;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.entity.common.IndexAdvertise;
import com.miqtech.master.service.common.ObjectRedisOperateService;
import com.miqtech.master.service.index.IndexAdvertiseService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class AdStatusChangeTask {

	@Autowired
	private IndexAdvertiseService indexAdvertiseService;

	@Autowired
	private QueryDao queryDao;

	@Scheduled(cron = "0 0/1 * * * ?")
	public void changeStatusTask() {
		unshelfStartAd();
		unshelfBannerAd();
		flushRedisAdData();

	}

	private void flushRedisAdData() {
		objectRedisOperateService.delData(API_CACHE_AD);
		objectRedisOperateService.delData(API_CACHE_AD_START);
	}

	private void unshelfBannerAd() {
		String sql = "select id from index_t_advertise where belong = 0 and is_valid =1 and status =1 and now()> server_start_date and now()< server_end_date order by server_start_date desc";
		List<Map<String, Object>> query = queryDao.queryMap(sql);
		if (CollectionUtils.isNotEmpty(query)) {
			if (query.size() > 4) {
				for (int i = 0; i < 4; i++) {
					query.remove(0);
				}
				for (Map<String, Object> data : query) {
					long id = NumberUtils.toLong(data.get("id").toString());
					IndexAdvertise indexAdvertise = indexAdvertiseService.findById(id);
					indexAdvertise.setStatus(0);
					indexAdvertiseService.saveOrUpdate(indexAdvertise);
				}
			}
		}

	}

	private void unshelfStartAd() {
		String sql = "select id from index_t_advertise where belong = 3 and is_valid =1 and status =1 and now()> server_start_date and now()< server_end_date order by server_start_date desc";
		List<Map<String, Object>> query = queryDao.queryMap(sql);
		if (CollectionUtils.isNotEmpty(query)) {
			if (query.size() > 1) {
				query.remove(0);
				for (Map<String, Object> data : query) {
					long id = NumberUtils.toLong(data.get("id").toString());
					IndexAdvertise indexAdvertise = indexAdvertiseService.findById(id);
					indexAdvertise.setStatus(0);
					indexAdvertiseService.saveOrUpdate(indexAdvertise);
				}
			}
		}
	}

	@Autowired
	private ObjectRedisOperateService objectRedisOperateService;
	// 广告banner缓存
	public static final String API_CACHE_AD = "api_cache_ad";
	// 启动广告缓存
	public static final String API_CACHE_AD_START = "api_cache_ad_start";
}
