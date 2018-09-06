package com.miqtech.master.service.ad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.ad.InviteAdvertiseDao;
import com.miqtech.master.entity.common.InviteAdvertise;

@Component
public class InviteAdvertiseService {
	@Autowired
	private InviteAdvertiseDao indexAdvertiseDao;

	public InviteAdvertise save(InviteAdvertise ad) {
		return indexAdvertiseDao.save(ad);
	}

	public InviteAdvertise findByUserIdAndAdId(long userId, long adId) {
		return indexAdvertiseDao.findByUserIdAndAdIdAndValid(userId, adId, 1);
	}

}
