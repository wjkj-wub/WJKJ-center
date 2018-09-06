package com.miqtech.master.dao.event;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.event.EventMember;

public interface EventMemberDao
		extends PagingAndSortingRepository<EventMember, Long>, JpaSpecificationExecutor<EventMember> {

	public List<EventMember> findByTeamId(Long teamId);

	public List<EventMember> findByUserId(Long userId);

}
