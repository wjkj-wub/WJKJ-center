package com.miqtech.master.service.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.activity.WjMatchDao;
import com.miqtech.master.entity.activity.WjMatch;

@Component
public class WjMatchService {

	@Autowired
	private WjMatchDao wjMatchDao;

	public WjMatch save(WjMatch match) {
		return wjMatchDao.save(match);
	}
}
