package com.miqtech.master.service.cohere;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.cohere.CohereDrawDao;
import com.miqtech.master.entity.cohere.CohereDraw;

@Component
public class CohereDrawService {
	@Autowired
	private CohereDrawDao cohereDrawDao;

	public CohereDraw saveOrUpdate(CohereDraw cohereDraw) {
		return cohereDrawDao.save(cohereDraw);
	}
	
	public CohereDraw findOne(Long id){
		return cohereDrawDao.findOne(id);
	}

}
