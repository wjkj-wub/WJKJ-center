package com.miqtech.master.dao.mall;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.mall.MallInvite;

/**
 *邀请好友
 *
 */
public interface MallInviteDao extends PagingAndSortingRepository<MallInvite, Long>,
		JpaSpecificationExecutor<MallInvite> {

	List<MallInvite> findByInviteUserIdAndIsRegisterNot(Long userId, int isRegister);

	List<MallInvite> findByInviteUserIdAndIsRegister(Long userId, int isRegister);

	MallInvite findByInvitedTelephoneAndIsRegister(String phone, Integer isRegister);

	List<MallInvite> findByInvitedTelephone(String phone);
}
