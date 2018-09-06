package com.miqtech.master.dao.mp;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.mp.MpUser;

/**
 * 微信用户操作DAO
 */
public interface MpUserDao extends PagingAndSortingRepository<MpUser, Long>, JpaSpecificationExecutor<MpUser> {

	/**
	 * 根据微信的openId和数据状态查找用户信息
	 * @param openId 微信openID
	 * @param valid 数据状态
	 */
	List<MpUser> findByOpenIdAndValid(String openId, Integer valid);

}
