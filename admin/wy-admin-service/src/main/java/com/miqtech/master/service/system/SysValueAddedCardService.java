package com.miqtech.master.service.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.common.SysValueAddedCardDao;
import com.miqtech.master.entity.common.SysValueAddedCard;

@Component
public class SysValueAddedCardService {
	@Autowired
	private SysValueAddedCardDao sysValueAddedCardDao;

	public SysValueAddedCard save(SysValueAddedCard sysValueAddedCard) {
		return sysValueAddedCardDao.save(sysValueAddedCard);
	}

	public SysValueAddedCard findOne(Long id) {
		return sysValueAddedCardDao.findOne(id);
	}

}
