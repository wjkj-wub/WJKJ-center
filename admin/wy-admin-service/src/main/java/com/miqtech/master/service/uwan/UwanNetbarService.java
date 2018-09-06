package com.miqtech.master.service.uwan;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.miqtech.master.dao.uwan.UwanNetbarDao;
import com.miqtech.master.entity.uwan.UwanNetbar;

@Service
public class UwanNetbarService {

	@Autowired
	private UwanNetbarDao uwanNetbarDao;

	/**
	 * 批量更新
	 */
	public void batchUpdate(List<UwanNetbar> netbars) {
		if (CollectionUtils.isEmpty(netbars)) {
			return;
		}

		uwanNetbarDao.save(netbars);
	}

	/**
	 * 根据来源查找绑定网吧
	 */
	public List<UwanNetbar> findBySource(Integer source) {
		if (source == null) {
			return null;
		}

		return uwanNetbarDao.findBySource(source);
	}
}
