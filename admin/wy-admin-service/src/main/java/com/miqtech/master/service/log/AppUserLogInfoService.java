package com.miqtech.master.service.log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.log.AppUserLogInfoDao;
import com.miqtech.master.entity.log.AppUserLogInfo;

@Component
public class AppUserLogInfoService {
	@Autowired
	private AppUserLogInfoDao appUserLogInfoDao;

	public void save(AppUserLogInfo logInfo) {
		appUserLogInfoDao.save(logInfo);
	}

}
