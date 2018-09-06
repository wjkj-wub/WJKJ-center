package com.miqtech.master.admin.web.task.pc.operationanalysis;

import com.miqtech.master.service.pc.operationanalysis.UserStatisticsAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 经营分析系统定时任务
 *
 * @author zhangyuqi
 * 2017年10月16日
 */
@Component
public class OperationAnalysisTask {
	@Autowired
	private UserStatisticsAnalysisService userStatisticsAnalysisService;

	/**
	 * 定时任务，每天凌晨两点统计前一天的数据
	 */
	@Scheduled(cron = "0 0 2 * * ?")
	public void updateGuessingStatus() {
		userStatisticsAnalysisService.executeConfrontStatistics();
	}
}
