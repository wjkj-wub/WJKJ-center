package com.miqtech.master.dao.netbar;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.netbar.NetbarInfo;

/**
 * 网吧信息操作DAO
 */
public interface NetbarInfoDao extends PagingAndSortingRepository<NetbarInfo, Long>,
		JpaSpecificationExecutor<NetbarInfo> {
	/**
	 * 根据邀请码和状态查找网吧信息
	 * @param invitation 邀请码
	 * @param valid 0无效 1有效
	 */
	List<NetbarInfo> findByInvitationCodeAndValid(String invitation, int valid);

	/**
	 * 根据是否发布状态,有效状态 网吧名称 电话进行模糊查询网吧信息
	 * @param isRelease 是否发布：0-否;1-是;
	 * @param valid 是否有效：1-有效;0-无效;
	 * @param name 网吧名称
	 * @param telephone 手机号
	 */
	List<NetbarInfo> findByIsReleaseAndValidAndNameLikeAndTelephoneLike(int isRelease, int valid, String name,
			String telephone);

	/**
	 * 根据areaCode查询网吧数据
	 */
	List<NetbarInfo> findByAreaCodeLikeAndValidAndIsRelease(String areaCode, int valid, int isRelease);

	/**
	 * 通过id字符串查询网吧
	 */
	List<NetbarInfo> findByValidAndIdIn(int valid, List<Long> ids);

	List<NetbarInfo> findByValid(Integer valid);

	List<NetbarInfo> findByNameLikeAndValid(String name, Integer valid);
}