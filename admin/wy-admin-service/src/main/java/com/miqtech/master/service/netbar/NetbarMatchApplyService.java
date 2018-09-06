package com.miqtech.master.service.netbar;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.netbar.NetbarMatchApplyDao;
import com.miqtech.master.entity.netbar.NetbarMatchApply;

@Component
public class NetbarMatchApplyService {

	@Autowired
	NetbarMatchApplyDao netbarMatchApplyDao;

	public List<NetbarMatchApply> findByNetbarIdAndActivityId(Long netbarId, Long activityId) {
		return netbarMatchApplyDao.findByNetbarIdAndActivityId(netbarId, activityId);
	}

	public List<NetbarMatchApply> findByNetbarIdAndActivityIdAndValid(Long netbarId, Long activityId, int valid) {
		return netbarMatchApplyDao.findByNetbarIdAndActivityIdAndValid(netbarId, activityId, valid);
	}

	public NetbarMatchApply save(NetbarMatchApply apply) {
		return netbarMatchApplyDao.save(apply);

	}
}