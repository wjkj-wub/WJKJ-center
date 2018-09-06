package com.miqtech.master.dao.netbar;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarUser;

/**
 * 网吧会员数据操作DAO
 */
public interface NetbarUserDao extends PagingAndSortingRepository<NetbarUser, Long>,
		JpaSpecificationExecutor<NetbarUser> {

	/**
	 * 查找某个网吧下面所有的会员信息
	 * @param netbarId 网吧id
	 */
	List<NetbarUser> findByNetbarId(Long netbarId);

	/**
	 * 根据会员id和网吧id查找会员信息
	 * @param userId 会员id
	 * @param netbarId 网吧id
	 */
	NetbarUser findByUserIdAndNetbarId(Long userId, Long netbarId);

}