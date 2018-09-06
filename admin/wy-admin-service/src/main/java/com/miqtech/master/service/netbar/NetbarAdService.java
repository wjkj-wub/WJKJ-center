package com.miqtech.master.service.netbar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.QueryDao;
import com.miqtech.master.dao.netbar.NetbarAdDao;
import com.miqtech.master.entity.netbar.NetbarAd;

@Component
public class NetbarAdService {
	@Autowired
	private NetbarAdDao netbarAdDao;

	@Autowired
	private QueryDao queryDao;

	public Iterable<NetbarAd> findAll() {
		return netbarAdDao.findByValid(1);
	}

	public NetbarAd save(NetbarAd netbarAd) {
		return netbarAdDao.save(netbarAd);
	}

	public NetbarAd findById(Long id) {
		return netbarAdDao.findOne(id);
	}

	public int countValidAd() {
		String sql = "select count(1) from netbar_t_ad where is_valid =1 ";
		Number count = queryDao.query(sql);
		return count.intValue();
	}

}
