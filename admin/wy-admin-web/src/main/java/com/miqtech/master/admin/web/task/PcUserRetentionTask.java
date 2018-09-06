package com.miqtech.master.admin.web.task;

import com.google.common.collect.Lists;
import com.miqtech.master.service.pc.netbar.PcNetbarUserRetentionService;
import com.miqtech.master.service.pc.user.PcUserRetentionService;
import com.miqtech.master.utils.DateUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Component
@EnableScheduling
public class PcUserRetentionTask {

	@Resource
	private PcUserRetentionService pcUserRetentionService;
	@Resource
	private PcNetbarUserRetentionService pcNetbarUserRetentionService;

	/**
	 * 每天1点更新用户留存信息
	 */
	@Scheduled(cron = "0 0 1 * * ?")
	public void updateRecentUserRetetionTask() {
		pcUserRetentionService.updateRecentRetetionRate();
	}

	/**
	 * 每天1点更新前一天网吧用户留存信息
	 */
	@Scheduled(cron = "0 0 1 * * ?")
	public void updateRecentNetbarUserRetetionTask() {
		List<Long> netbarIds = pcNetbarUserRetentionService.getPromotionNetbarIds();
		List<String> dates = Lists.newArrayList();
		Date yesterday = DateUtils.getYesterday();
		String yesterdayStr = DateUtils.dateToString(yesterday, DateUtils.YYYY_MM_DD);
		dates.add(yesterdayStr);
		pcNetbarUserRetentionService.updateRetentions(dates, netbarIds);
	}
}
