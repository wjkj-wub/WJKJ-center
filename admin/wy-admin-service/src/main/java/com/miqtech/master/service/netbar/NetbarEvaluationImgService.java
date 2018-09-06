package com.miqtech.master.service.netbar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.netbar.NetbarEvaluationImgDao;
import com.miqtech.master.entity.netbar.NetbarEvaluationImg;

@Component
public class NetbarEvaluationImgService {
	@Autowired
	private NetbarEvaluationImgDao netbarEvaluationImgDao;

	public NetbarEvaluationImg save(NetbarEvaluationImg eva) {
		return netbarEvaluationImgDao.save(eva);
	}

}
