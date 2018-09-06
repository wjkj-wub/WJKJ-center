package com.miqtech.master.admin.web.task;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.miqtech.master.entity.activity.ActivityOverActivity;
import com.miqtech.master.service.activity.ActivityOverActivityService;

@Component
public class ActivityInfoStatusTask {
	@Autowired
	private ActivityOverActivityService activityOverActivityService;

	@Scheduled(cron = "0 0 0/1 * * ?")
	public void publishActivityInfo() {
		Date now = DateUtils.truncate(new Date(), Calendar.HOUR_OF_DAY);
		List<ActivityOverActivity> activities = activityOverActivityService.findByTimerDateAndValid(now, 0);
		if (CollectionUtils.isNotEmpty(activities)) {
			for (ActivityOverActivity aoa : activities) {
				aoa.setValid(1);//设置文章可见
			}
			activityOverActivityService.save(activities);
		}
	}
}
