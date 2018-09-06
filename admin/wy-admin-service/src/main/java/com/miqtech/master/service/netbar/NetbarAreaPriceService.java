package com.miqtech.master.service.netbar;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.miqtech.master.dao.netbar.NetbarAreaPriceDao;
import com.miqtech.master.entity.netbar.NetbarAreaPrice;

@Component
public class NetbarAreaPriceService {
	@Autowired
	NetbarAreaPriceDao netbarAreaPriceDao;

	public List<NetbarAreaPrice> queryAllByNetbarIdAndValid(Long netbarId, int valid) {
		return netbarAreaPriceDao.findAllByNetbarIdAndValid(netbarId, valid);
	}

	public Iterable<NetbarAreaPrice> save(List<NetbarAreaPrice> stringToCollection) {
		return netbarAreaPriceDao.save(stringToCollection);
	}

	public NetbarAreaPrice save(NetbarAreaPrice netbarAreaPrice) {
		return netbarAreaPriceDao.save(netbarAreaPrice);
	}

	public void deleteByNetbarId(Long netbarId) {
		netbarAreaPriceDao.delete(queryAllByNetbarIdAndValid(netbarId, 1));
	}

}
