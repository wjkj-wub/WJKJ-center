package com.miqtech.master.admin.web.task;

import com.miqtech.master.service.guessing.GuessingInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 竞猜模块定时任务
 *
 * @author zhangyuqi
 * 2017年06月12日
 */
@Component
public class GuessingTask {
	@Autowired
	private GuessingInfoService guessingInfoService;

	/**
	 * 定时任务，每5分钟查询一次guessing_info表，更新竞猜状态
	 */
	@Scheduled(cron = "0 0/1 * * * ?")
	public void updateGuessingStatus() {
		guessingInfoService.updateGuessingStatus();
	}
}
