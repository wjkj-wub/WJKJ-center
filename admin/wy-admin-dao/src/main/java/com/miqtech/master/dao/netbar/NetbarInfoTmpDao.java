package com.miqtech.master.dao.netbar;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarInfoTmp;

/**
 * 网吧临时表操作DAO
 */
public interface NetbarInfoTmpDao extends PagingAndSortingRepository<NetbarInfoTmp, Long>,
		JpaSpecificationExecutor<NetbarInfoTmp> {

	/**
	 * 根据名称查找网吧信息
	 * @param name 网吧名称
	 */
	List<NetbarInfoTmp> findByName(String name);

	/**
	 * 根据正式表网吧id查找临时数据
	 * @param id 网吧id(正式表)
	 */
	NetbarInfoTmp findByNetbarId(Long id);

	/**
	 * 根据商户id查找临时网吧数据
	 * @param id 商户id
	 */
	NetbarInfoTmp findByMerchantId(Long id);
}