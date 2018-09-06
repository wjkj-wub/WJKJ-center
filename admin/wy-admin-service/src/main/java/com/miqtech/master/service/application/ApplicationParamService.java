package com.miqtech.master.service.application;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.common.ApplicationParamDao;
import com.miqtech.master.entity.common.ApplicationParam;

@Component
public class ApplicationParamService {

	@Autowired
	private ApplicationParamDao applicationParamDao;

	/**
	 * 根据类别查找版本信息
	 * @param type 1-app;2-后台
	 */
	public List<ApplicationParam> findByType() {
		return applicationParamDao.findByValid(1);
	}
}