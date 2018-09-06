package com.miqtech.master.service.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.QueryDao;

@Component
public class EventRoundService {
	@Autowired
	private QueryDao queryDao;
	
	public Integer selectMemCount(Long round){
		String sql ="select max_num from oet_event_round where is_valid=1 and id ="+round;
		Number count = queryDao.query(sql);
		return count.intValue();
	}
}
