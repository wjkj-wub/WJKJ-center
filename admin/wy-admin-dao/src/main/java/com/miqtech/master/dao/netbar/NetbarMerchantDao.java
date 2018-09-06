package com.miqtech.master.dao.netbar;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarMerchant;

/**
 * 网吧商户信息操作DAO
 */
public interface NetbarMerchantDao extends PagingAndSortingRepository<NetbarMerchant, Long>,
		JpaSpecificationExecutor<NetbarMerchant> {

	/**
	 * 根据商户名称 密码 状态查找商户信息
	 * @param phone 商户管理员手机号
	 * @param password 管理员密码
	 * @param valid 0无效 1有效
	 */
	NetbarMerchant findByUsernameAndPasswordAndValid(String phone, String password, int valid);

	/**
	 * 根据网吧id和数据窗台查找商户信息
	 * @param netbarId 网吧id
	 * @param valid 0无效 1有效
	 */
	NetbarMerchant findByNetbarIdAndValid(Long netbarId, int valid);

	/**
	 * 根据商户手机号查找商户信息
	 * @param phone 商户手机号
	 */
	NetbarMerchant findByUsername(String phone);

	NetbarMerchant findByUsernameAndValid(String phone, int i);
}