package com.miqtech.master.dao.code;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.miqtech.master.entity.code.InviteRecord;

public interface InviteRecordDao extends JpaSpecificationExecutor<InviteRecord>,
		PagingAndSortingRepository<InviteRecord, Long> {

	InviteRecord findByUserIdAndCode(Long userId, String invitationCode);

	InviteRecord findByUserIdAndValid(Long userId, int valid);

}
