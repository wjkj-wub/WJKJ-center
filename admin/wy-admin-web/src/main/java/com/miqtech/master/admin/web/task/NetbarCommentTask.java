package com.miqtech.master.admin.web.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.miqtech.master.service.netbar.NetbarCommentService;

/**
 * 定时统计网吧标签
 * @author Administrator
 *
 */
@Component
public class NetbarCommentTask {

	@Autowired
	private NetbarCommentService netbarCommentService;

	/**
	 * 每天1点统计网吧标签
	 */
	@Scheduled(cron = "0 0 0/4 * * ?")
	public void netbarTag() {
		netbarCommentService.updateNetBarComment();
		netbarCommentService.updateNetBarScore();
	}
}
