package com.miqtech.master.admin.web.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.service.netbar.NetbarFundInfoService;
import com.miqtech.master.service.netbar.resource.NetbarResourcePropertyService;

@Component
public class NetbarAreaRatioTask {

	@Autowired
	private NetbarResourcePropertyService netbarResourcePropertyService;
	@Autowired
	private NetbarFundInfoService netbarFundInfoService;

	//@Scheduled(cron = "0 0 0 ? * FRI")
	public void execute() {
		netbarResourcePropertyService.autoUpdate();
		netbarFundInfoService.updateRatio();
	}

}
