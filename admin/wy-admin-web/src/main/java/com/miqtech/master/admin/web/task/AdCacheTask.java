package com.miqtech.master.admin.web.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.miqtech.master.service.index.IndexAdvertiseService;

@Component
public class AdCacheTask {
	@Autowired
	private IndexAdvertiseService indexAdvertiseService;

	@Scheduled(cron = "0 0 0 * * ?")
	public void clearCache() {
		indexAdvertiseService.clearCache();
	}
}
