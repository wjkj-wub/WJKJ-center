package com.miqtech.master.admin.web.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.miqtech.master.service.audition.AuditionMatchDetailLolService;
import com.miqtech.master.service.netbar.NetbarRankService;

@Component
public class NetbarRankTask {

	@Autowired
	private NetbarRankService netbarRankService;
	@Autowired
	private AuditionMatchDetailLolService auditionMatchDetailLolService;

	/**
	 * 产生网吧排名(每周一凌晨3点)
	 */
	@Scheduled(cron = "0 0 3 ? * MON")
	public void generateNetbarRank() {
		netbarRankService.generateNetbarRank();
	}
	
	/**
	 * 增量下载优玩昨日网吧比赛记录(每日凌晨1点)
	 */
	@Scheduled(cron = "0 0 1 * * ?")
	public void saveYesterdayNetbarMatches() {
		auditionMatchDetailLolService.saveNetbarMatches();
	}

}
