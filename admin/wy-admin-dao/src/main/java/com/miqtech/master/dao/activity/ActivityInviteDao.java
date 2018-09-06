package com.miqtech.master.dao.activity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.activity.ActivityInvite;

/**
 * 约战信息操作DAO
 */
public interface ActivityInviteDao extends PagingAndSortingRepository<ActivityInvite, Long>,
		JpaSpecificationExecutor<ActivityInvite> {

	ActivityInvite findByIdAndValid(Long inviteId, Integer valid);

	ActivityInvite findByIdAndStatusAndValid(Long inviteId, Integer status, Integer valid);

	List<ActivityInvite> findByTypeAndTargetIdAndInvitedTelephoneIn(Integer type, Long targetId, String[] telephones);
}