package com.miqtech.master.admin.web.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.miqtech.master.service.user.UserInfoService;

@Component
public class UserTask {
	@Autowired
	private UserInfoService userInfoService;
	public static long currentUserCount = 20646760L;

	@Scheduled(cron = "0 0 3 * * ?")
	public void reload() {
		long countUser = userInfoService.countUser();
		if (countUser > 0) {
			currentUserCount = countUser;
		}
	}
}
